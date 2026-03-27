package com.mtp.config.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mtp.config.center.config.MtpProperties;
import com.mtp.config.center.model.ClientStatus;
import com.mtp.config.center.repository.ConfigCenterRepository;
import com.mtp.core.model.ApplicationInfo;
import com.mtp.core.model.ThreadPoolConfig;
import com.mtp.core.model.ThreadPoolStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConfigCenterService implements InitializingBean, DisposableBean {

    private final ConfigCenterRepository repository;
    private ScheduledExecutorService mtpServerScheduler;
    private final MtpProperties mtpProperties;

    public ConfigCenterService(ConfigCenterRepository repository, MtpProperties mtpProperties) {
        this.repository = repository;
        this.mtpProperties = mtpProperties;
    }

    public void registerConfig(ThreadPoolConfig config) {
        validateConfig(config);
        config.setRegisterTime(System.currentTimeMillis());
        repository.saveConfig(config);
        log.info("Registered config: [{}:{}]", config.getInstanceId(), config.getPoolName());
    }

    public void unregisterConfig(String instanceId, String poolName) {
        repository.deleteConfig(instanceId, poolName);
        repository.deleteStatus(instanceId, poolName);
        log.info("Unregistered config: [{}:{}]", instanceId, poolName);
    }

    public void updateConfig(ThreadPoolConfig config) {
        validateConfig(config);
        repository.updateConfig(config);
        log.info("Updated config: [{}:{}]", config.getInstanceId(), config.getPoolName());
    }

    public void updateConfigById(String instanceId, String poolName, ThreadPoolConfig newConfig) {
        validateConfig(newConfig);
        ThreadPoolConfig existing = repository.findConfig(instanceId, poolName);
        if (existing != null) {
            existing.setCorePoolSize(newConfig.getCorePoolSize());
            existing.setMaxPoolSize(newConfig.getMaxPoolSize());
            existing.setQueueCapacity(newConfig.getQueueCapacity());
            existing.setKeepAliveSeconds(newConfig.getKeepAliveSeconds());
            existing.setRejectedPolicy(newConfig.getRejectedPolicy());
            repository.updateConfig(existing);
            log.info("Updated config by id: [{}:{}]", instanceId, poolName);
        }
    }

    public int updateConfigsByAppAndPoolName(String applicationName, String poolName, ThreadPoolConfig newConfig) {
        validateConfig(newConfig);
        List<ThreadPoolConfig> configs = repository.findConfigsByPoolName(applicationName, poolName);

        for (ThreadPoolConfig config : configs) {
            config.setCorePoolSize(newConfig.getCorePoolSize());
            config.setMaxPoolSize(newConfig.getMaxPoolSize());
            config.setQueueCapacity(newConfig.getQueueCapacity());
            config.setKeepAliveSeconds(newConfig.getKeepAliveSeconds());
            config.setRejectedPolicy(newConfig.getRejectedPolicy());
            repository.updateConfig(config);
            log.info("Updated config for all instances: [{}:{}]", config.getInstanceId(), poolName);
        }
        return configs.size();
    }

    private void validateConfig(ThreadPoolConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }
        if (config.getApplicationName() == null || config.getApplicationName().trim().isEmpty()) {
            throw new IllegalArgumentException("Application name cannot be empty");
        }
        if (config.getPoolName() == null || config.getPoolName().trim().isEmpty()) {
            throw new IllegalArgumentException("Pool name cannot be empty");
        }
        if (config.getMaxPoolSize() == null || config.getMaxPoolSize() < 0) {
            throw new IllegalArgumentException("Max pool size must be >= 0");
        }
        if (config.getCorePoolSize() == null || config.getCorePoolSize() < 0) {
            throw new IllegalArgumentException("Core pool size must be >= 0");
        }
        if (config.getCorePoolSize() > config.getMaxPoolSize()) {
            throw new IllegalArgumentException("Core pool size cannot be greater than max pool size");
        }
        if (config.getQueueCapacity() == null || config.getQueueCapacity() < 0) {
            throw new IllegalArgumentException("Queue capacity must be >= 0");
        }
        if (config.getKeepAliveSeconds() == null || config.getKeepAliveSeconds() < 0) {
            throw new IllegalArgumentException("Keep alive seconds must be >= 0");
        }
    }

    public ThreadPoolConfig getConfig(String instanceId, String poolName) {
        return repository.findConfig(instanceId, poolName);
    }

    public Page<ThreadPoolConfig> getConfigsPaged(String applicationName, int page, int size) {
        return repository.findConfigsPaged(applicationName, page, size);
    }

    public List<ThreadPoolConfig> getConfigsByInstance(String applicationName, String ip, Integer port) {
        return repository.findConfigsByApplication(applicationName).stream()
            .filter(c -> c.getIp().equals(ip) && c.getPort().equals(port))
            .collect(Collectors.toList());
    }

    public List<ThreadPoolConfig> findConfigListByInstanceId(String instanceId) {
        return repository.findConfigListByInstanceId(instanceId);
    }

    public ThreadPoolConfig findConfig(String instanceId, String poolName) {
        return repository.findConfig(instanceId, poolName);
    }

    public List<ThreadPoolConfig> getConfigsByPoolName(String applicationName, String poolName) {
        return repository.findConfigsByPoolName(applicationName, poolName);
    }

    public void reportStatus(ThreadPoolStatus status) {
        repository.saveStatus(status);
    }

    public List<ThreadPoolStatus> getAllStatuses(String applicationName) {
        return repository.findStatusesByApplication(applicationName);
    }

    public List<ThreadPoolStatus> getStatusesByInstance(String applicationName, String ip, Integer port) {
        return repository.findStatusesByApplication(applicationName).stream()
            .filter(s -> s.getIp().equals(ip) && s.getPort().equals(port))
            .collect(Collectors.toList());
    }

    public List<ApplicationInfo> getAllApplications() {
        return repository.findAllApplications().stream()
                .map(ApplicationInfo::new).collect(Collectors.toList());
    }

    public void registerClient(String instanceId, String applicationName, String ip, Integer port) {
        repository.registerClient(instanceId, applicationName, ip, port);
    }

    public void updateClientReportTime(String instanceId) {
        repository.updateClientReportTime(instanceId);
    }

    public void updateClientStatus(String instanceId, String status) {
        repository.updateClientStatus(instanceId, status);
    }

    public List<String> getAllClientInstanceIds() {
        return repository.findAllClientInstanceIds();
    }

    public String getClientStatus(String instanceId) {
        return repository.findClientStatus(instanceId);
    }

    public Long getClientReportTime(String instanceId) {
        return repository.findClientReportTime(instanceId);
    }

    public Page<ApplicationInfo> getApplicationsFromRegistryPaged(String applicationName, int page, int size) {
        Page<ApplicationInfo> pageResult = repository.findApplicationsFromRegistryPaged(applicationName, page, size);

        if (!pageResult.getRecords().isEmpty()) {
            pageResult.getRecords().get(0).addInstance(new ApplicationInfo.InstanceInfo("127.0.0.1", 8080, "ONLINE"));
            pageResult.getRecords().get(0).addInstance(new ApplicationInfo.InstanceInfo("127.0.0.1", 8081, "ONLINE"));
            pageResult.getRecords().get(0).addInstance(new ApplicationInfo.InstanceInfo("127.0.0.1", 8082, "ONLINE"));
            pageResult.getRecords().get(0).addInstance(new ApplicationInfo.InstanceInfo("127.0.0.1", 8083, "ONLINE"));
            pageResult.getRecords().get(0).addInstance(new ApplicationInfo.InstanceInfo("127.0.0.1", 8084, "ONLINE"));
        }
        return pageResult;
    }

    @Override
    public void destroy() throws Exception {
        this.mtpServerScheduler.shutdown();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.mtpServerScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("mtp-server-scheduler");
            return t;
        });
        int interval = mtpProperties.getClient().getHeartbeatInterval();
        this.mtpServerScheduler.scheduleAtFixedRate(()-> {
            try {
                List<String> instanceIds = getAllClientInstanceIds();
                long heartbeatInterval = interval * 1000L;
                long threshold = 2 * heartbeatInterval;
                long currentTime = System.currentTimeMillis();

                for (String instanceId : instanceIds) {
                    String status = getClientStatus(instanceId);
                    if (ClientStatus.ONLINE.toString().equals(status)) {
                        Long reportTime = getClientReportTime(instanceId);
                        if (reportTime != null && (currentTime - reportTime) > threshold) {
                            updateClientStatus(instanceId, ClientStatus.OFFLINE.toString());
                            log.info("Client {} marked as OFFLINE (report time: {}, current time: {}, threshold: {})",
                                    instanceId, reportTime, currentTime, threshold);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error updating client status", e);
            }
        }, interval, interval, TimeUnit.SECONDS);
    }
}