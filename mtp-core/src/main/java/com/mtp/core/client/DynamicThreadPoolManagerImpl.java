package com.mtp.core.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mtp.core.api.*;
import com.mtp.core.model.ClientProperties;
import com.mtp.core.model.ThreadPoolConfig;
import com.mtp.core.model.ThreadPoolStatus;
import com.mtp.core.netty.ConfigChangeEvent;
import com.mtp.core.netty.MessageType;
import com.mtp.core.netty.NettyClient;
import com.mtp.core.tp.TpThreadPoolExecutor;
import com.mtp.core.util.ExecutorUtil;
import com.mtp.core.util.Md5Util;
import com.mtp.core.util.NetworkUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Objects;

/**
 * 动态线程池管理器实现类
 * 负责线程池的创建、注册、配置刷新、状态监控等功能
 */
@Slf4j
public class DynamicThreadPoolManagerImpl implements DynamicThreadPoolManager {

    private final String instanceId;
    private final Map<String, ThreadPoolExecutor> executors;
    private final Map<String, ThreadPoolConfig> configs;
    private final Set<String> registeredPools;
    private final ConfigCenterClient configCenterClient;
    private final NettyClient nettyClient;
    private final String applicationName;
    private final String ip;
    private final Integer port;
    private final ScheduledExecutorService scheduledExecutor;
    private final AtomicBoolean isConfigCenterAvailable;
    private ClientProperties clientProperties;

    public DynamicThreadPoolManagerImpl(ConfigCenterClient configCenterClient, 
                                        NettyClient nettyClient, String applicationName, 
                                        String ip, Integer port) {
        this.configCenterClient = configCenterClient;
        this.nettyClient = nettyClient;
        this.applicationName = applicationName;
        this.ip = ip != null ? ip : NetworkUtil.getLocalIp();
        this.port = port;
        this.instanceId = Md5Util.generateInstanceId(applicationName, ip, port);
        this.executors = new ConcurrentHashMap<>();
        this.configs = new ConcurrentHashMap<>();
        this.registeredPools = ConcurrentHashMap.newKeySet();
        this.isConfigCenterAvailable = new AtomicBoolean(false);
        this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("mtp-client-scheduler");
            return t;
        });

        subscribeMessage();
        nettyClient.start();
        nettyClient.awaitConnect();
        pullClientServerConfigs();
        startRetryTask();
    }

    private void subscribeMessage() {
        MessageBus.bus.subscribe(MessageBusTopic.CONFIG_CHANGE, (MessageListener<ConfigChangeEvent>) message -> {
            ConfigChangeEvent event = message.getContent();
            if (applicationName.equals(event.getApplicationName()) && event.getConfigs() != null) {
                for (ThreadPoolConfig remoteConfig : event.getConfigs()) {
                    String poolName = remoteConfig.getPoolName();
                    if (configs.containsKey(poolName)) {
                        ThreadPoolConfig localConfig = configs.get(poolName);
                        if (hasConfigChanged(localConfig, remoteConfig)) {
                            String changedFields = getChangedFields(localConfig, remoteConfig);
                            log.info("[{}] Received config change from server: {}", poolName, changedFields);
                            doRefreshPool(poolName, remoteConfig);
                        }
                    }
                }
            }
        });

        MessageBus.bus.subscribe(MessageBusTopic.RE_REGISTER, m -> {
            Optional<ThreadPoolConfig> op = configs.values().stream().findFirst();
            if (op.isPresent()) {
                ThreadPoolConfig config = op.get();
                this.nettyClient.sendNotification(MessageType.RE_REGISTER, config);
            }
        });

        MessageBus.bus.subscribe(MessageBusTopic.GET_THREAD_POOL_STATUS, (MessageListener<String>) message -> {
            String poolName = message.getContent();
            ThreadPoolStatus status = getPoolStatus(poolName);
            try {
                configCenterClient.reportStatus(status);
            } catch (Exception e) {
                log.error("[{}] Failed to report status: [{}]", poolName, configCenterClient.getConfigCenterUrl());
            }
        });
    }

    @Override
    public void registerPool(String poolName, ThreadPoolConfig config) {
        if (!StringUtils.hasText(poolName) || config == null) {
            throw new IllegalArgumentException("poolName and config cannot be null");
        }
        if (executors.containsKey(poolName)) {
            throw new IllegalStateException("Pool already exists: " + poolName);
        }

        config.setApplicationName(applicationName);
        config.setIp(ip);
        config.setPort(port);
        config.setInstanceId(instanceId);
        config.setRegisterTime(System.currentTimeMillis());

        ThreadPoolExecutor executor = createExecutor(config);
        executors.put(poolName, executor);
        configs.put(poolName, config);

        if (configCenterClient != null) {
            doRegister(poolName, config);
        }
    }

    private void doRegister(String poolName, ThreadPoolConfig config) {
        try {
            configCenterClient.register(config);
            registeredPools.add(poolName);
            isConfigCenterAvailable.set(true);
        } catch (Exception e) {
            registeredPools.remove(poolName);
            isConfigCenterAvailable.set(false);
        }
    }

    private void pullClientServerConfigs() {
        try {
            this.clientProperties = nettyClient.sendRequest(MessageType.GET_CLIENT_SERVER_CONFIG, null, new TypeReference<ClientProperties>() {
            });
        } catch (Exception e) {
            log.error("Failed to pull client server configs", e);
        }
    }

    @Override
    public void unregisterPool(String poolName) {
        ThreadPoolConfig config = configs.remove(poolName);
        ThreadPoolExecutor executor = executors.remove(poolName);
        if (executor != null) {
            executor.shutdown();
            registeredPools.remove(poolName);

            if (configCenterClient != null && config != null) {
                try {
                    configCenterClient.unregister(config.getInstanceId(), poolName);
                } catch (Exception e) {
                    log.error("Failed to unregister pool: " + poolName, e);
                }
            }
        }
    }

    @Override
    public Executor getExecutor(String poolName) {
        return executors.get(poolName);
    }

    @Override
    public void refreshPool(String poolName, ThreadPoolConfig newConfig) {
        ThreadPoolExecutor executor = executors.get(poolName);
        if (executor == null) {
            throw new IllegalStateException("Pool not found: " + poolName);
        }

        configs.put(poolName, newConfig);

        executor.setCorePoolSize(newConfig.getCorePoolSize());
        executor.setMaximumPoolSize(newConfig.getMaxPoolSize());
        executor.setKeepAliveTime(newConfig.getKeepAliveSeconds(), TimeUnit.SECONDS);
        executor.setRejectedExecutionHandler(newConfig.getRejectedExecutionHandler());

        /*if (configCenterClient != null && registeredPools.contains(poolName)) {
            try {
                configCenterClient.updateConfig(newConfig);
            } catch (Exception e) {
            }
        }*/
    }

    private void doRefreshPool(String poolName, ThreadPoolConfig newConfig) {
        ThreadPoolExecutor executor = executors.get(poolName);
        if (executor == null) {
            log.warn("[{}] Pool not found for refresh", poolName);
            return;
        }

        configs.put(poolName, newConfig);

        executor.setCorePoolSize(newConfig.getCorePoolSize());
        executor.setMaximumPoolSize(newConfig.getMaxPoolSize());
        executor.setKeepAliveTime(newConfig.getKeepAliveSeconds(), TimeUnit.SECONDS);
        executor.setRejectedExecutionHandler(newConfig.getRejectedExecutionHandler());

        log.info("[{}] Pool refreshed with new config: corePoolSize={}, maxPoolSize={}, queueCapacity={}, keepAliveSeconds={}, rejectedPolicy={}",
            poolName, newConfig.getCorePoolSize(), newConfig.getMaxPoolSize(), newConfig.getQueueCapacity(), newConfig.getKeepAliveSeconds(), newConfig.getRejectedPolicy());
    }

    @Override
    public void reportStatus() {
        executors.forEach((poolName, executor) -> {
            ThreadPoolStatus status = getPoolStatus(poolName);
            if (status != null && configCenterClient != null) {
                try {
                    configCenterClient.reportStatus(status);
                } catch (Exception e) {
                    log.error("[{}] Failed to report status: [{}]", poolName, configCenterClient.getConfigCenterUrl());
                }
            }
        });
    }

    @Override
    public List<ThreadPoolStatus> getAllStatuses() {
        List<ThreadPoolStatus> statuses = new ArrayList<>();
        executors.forEach((poolName, executor) -> {
            statuses.add(getPoolStatus(poolName));
        });
        return statuses;
    }

    @Override
    public List<String> getAllPoolNames() {
        return new ArrayList<>(executors.keySet());
    }

    @Override
    public ThreadPoolStatus getPoolStatus(String poolName) {
        ThreadPoolExecutor executor = executors.get(poolName);
        if (executor == null) {
            return null;
        }

        long taskCount = 0;
        long completedTaskCount = 0;
        if (executor instanceof TpThreadPoolExecutor) {
            TpThreadPoolExecutor tpExecutor = (TpThreadPoolExecutor) executor;
            taskCount = tpExecutor.getTaskCount();
            completedTaskCount = tpExecutor.getCompletedTaskCount();
        }

        ThreadPoolStatus status = new ThreadPoolStatus();
        status.setPoolName(poolName);
        status.setApplicationName(applicationName);
        status.setInstanceId(instanceId);
        status.setIp(ip);
        status.setPort(port);
        status.setCorePoolSize(executor.getCorePoolSize());
        status.setMaxPoolSize(executor.getMaximumPoolSize());
        status.setActiveCount(executor.getActiveCount());
        status.setPoolSize(executor.getPoolSize());
        status.setTaskCount(taskCount);
        status.setCompletedTaskCount(completedTaskCount);
        //todo 队列大小
        status.setQueueSize(executor.getQueue() != null ? executor.getQueue().size() : 0);
        status.setQueueCapacity(ExecutorUtil.blockingQueueCapacity(executor));
        status.setUpdateTime(System.currentTimeMillis());

        return status;
    }

    private void startRetryTask() {
        scheduledExecutor.scheduleAtFixedRate(
            this::retryRegistrationAndReport,
            clientProperties.getHeartbeatInterval(),
            clientProperties.getHeartbeatInterval(),
            TimeUnit.SECONDS
        );
    }

    private void retryRegistrationAndReport() {
        if (configCenterClient == null) {
            return;
        }

        configs.forEach((poolName, config) -> {
            if (!registeredPools.contains(poolName)) {
                doRegister(poolName, config);
            }
        });

        pullConfigChanges();
        reportStatus();
    }

    private void pullConfigChanges() {
        if (configCenterClient == null) {
            return;
        }

        configs.forEach((poolName, currentConfig) -> {
            try {
                List<ThreadPoolConfig> remoteConfigs = configCenterClient.getConfigsByInstanceId(instanceId, poolName);
                if (remoteConfigs != null && !remoteConfigs.isEmpty()) {
                    ThreadPoolConfig remoteConfig = remoteConfigs.get(0);
                    if (hasConfigChanged(currentConfig, remoteConfig)) {
                        String changedFields = getChangedFields(currentConfig, remoteConfig);
                        log.info("[{}] Config changed from config center: {}", poolName, changedFields);
                        refreshPool(poolName, remoteConfig);
                    }
                }
            } catch (Exception e) {
                log.debug("[{}] Failed to pull config changes: {}", poolName, e.getMessage());
            }
        });
    }

    private boolean hasConfigChanged(ThreadPoolConfig local, ThreadPoolConfig remote) {
        return !Objects.equals(local.getCorePoolSize(), remote.getCorePoolSize())
            || !Objects.equals(local.getMaxPoolSize(), remote.getMaxPoolSize())
            || !Objects.equals(local.getQueueCapacity(), remote.getQueueCapacity())
            || !Objects.equals(local.getKeepAliveSeconds(), remote.getKeepAliveSeconds())
            || !Objects.equals(local.getRejectedPolicy(), remote.getRejectedPolicy());
    }

    private String getChangedFields(ThreadPoolConfig local, ThreadPoolConfig remote) {
        StringBuilder sb = new StringBuilder();
        appendChange(sb, "corePoolSize", local.getCorePoolSize(), remote.getCorePoolSize());
        appendChange(sb, "maxPoolSize", local.getMaxPoolSize(), remote.getMaxPoolSize());
        appendChange(sb, "queueCapacity", local.getQueueCapacity(), remote.getQueueCapacity());
        appendChange(sb, "keepAliveSeconds", local.getKeepAliveSeconds(), remote.getKeepAliveSeconds());
        appendChange(sb, "rejectedPolicy", local.getRejectedPolicy(), remote.getRejectedPolicy());
        return sb.toString();
    }

    private void appendChange(StringBuilder sb, String fieldName, Object oldVal, Object newVal) {
        if (!Objects.equals(oldVal, newVal)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(fieldName).append(" [").append(oldVal).append("->").append(newVal).append("]");
        }
    }

    public void stop() {
        if (nettyClient != null) {
            nettyClient.stop();
        }
        scheduledExecutor.shutdown();
        executors.values().forEach(ThreadPoolExecutor::shutdown);
        executors.clear();
        configs.clear();
        registeredPools.clear();
    }

    public boolean isConfigCenterAvailable() {
        return isConfigCenterAvailable.get();
    }

    public Set<String> getRegisteredPools() {
        return new HashSet<>(registeredPools);
    }

    private ThreadPoolExecutor createExecutor(ThreadPoolConfig config) {
        ThreadFactory threadFactory = r -> {
            Thread t = new Thread(r);
            t.setName(config.getPoolName() + "-worker-" + t.getId());
            return t;
        };

        return new TpThreadPoolExecutor(
            config,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(config.getQueueCapacity()),
            threadFactory
        );
    }
}