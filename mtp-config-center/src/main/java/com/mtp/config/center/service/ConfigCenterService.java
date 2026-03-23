package com.mtp.config.center.service;

import com.mtp.core.model.ApplicationInfo;
import com.mtp.core.model.ApplicationInfo.InstanceInfo;
import com.mtp.core.model.ThreadPoolConfig;
import com.mtp.core.model.ThreadPoolStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

/**
 * 配置中心服务，负责管理线程池配置和状态的存储与查询
 */
@Slf4j
@Service
public class ConfigCenterService {

    private final Map<String, ThreadPoolConfig> configStore;
    private final Map<String, ThreadPoolStatus> statusStore;
    private final Map<String, Set<InstanceInfo>> applicationInstances;
    private final Set<String> applications;

    public static final String TOPIC_CONFIG_CHANGE = "config_change";

    public ConfigCenterService() {
        this.configStore = new ConcurrentHashMap<>();
        this.statusStore = new ConcurrentHashMap<>();
        this.applicationInstances = new ConcurrentHashMap<>();
        this.applications = new ConcurrentSkipListSet<>();
    }

    @PostConstruct
    public void init() {
    }

    public void registerConfig(ThreadPoolConfig config) {
        validateConfig(config);
        String key = buildConfigKey(config.getInstanceId(), config.getPoolName());
        config.setRegisterTime(System.currentTimeMillis());
        configStore.put(key, config);

        String appKey = config.getApplicationName();
        applications.add(appKey);

        InstanceInfo instance = new InstanceInfo(config.getIp(), config.getPort());
        Set<InstanceInfo> instances = applicationInstances.computeIfAbsent(appKey, k -> ConcurrentHashMap.newKeySet());
        instances.add(instance);

        log.info("Registered config: [{}]", key);
    }

    public void unregisterConfig(String instanceId, String poolName) {
        String key = buildConfigKey(instanceId, poolName);
        configStore.remove(key);
        log.info("Unregistered config: [{}]", key);
    }

    public void updateConfig(ThreadPoolConfig config) {
        validateConfig(config);
        String key = buildConfigKey(config.getInstanceId(), config.getPoolName());
        if (configStore.containsKey(key)) {
            configStore.put(key, config);
            log.info("Updated config: [{}]", key);
        }
    }

    public void updateConfigById(String instanceId, String poolName, ThreadPoolConfig newConfig) {
        validateConfig(newConfig);
        String key = buildConfigKey(instanceId, poolName);
        if (configStore.containsKey(key)) {
            ThreadPoolConfig existing = configStore.get(key);
            existing.setCorePoolSize(newConfig.getCorePoolSize());
            existing.setMaxPoolSize(newConfig.getMaxPoolSize());
            existing.setQueueCapacity(newConfig.getQueueCapacity());
            existing.setKeepAliveSeconds(newConfig.getKeepAliveSeconds());
            existing.setRejectedPolicy(newConfig.getRejectedPolicy());
            configStore.put(key, existing);
            log.info("Updated config by id: [{}]", key);
        }
    }

    public int updateConfigsByAppAndPoolName(String applicationName, String poolName, ThreadPoolConfig newConfig) {
        validateConfig(newConfig);
        List<String> keysToUpdate = configStore.keySet().stream()
            .filter(key -> {
                ThreadPoolConfig config = configStore.get(key);
                return config != null
                    && applicationName.equals(config.getApplicationName())
                    && poolName.equals(config.getPoolName());
            })
            .collect(Collectors.toList());

        for (String key : keysToUpdate) {
            ThreadPoolConfig existing = configStore.get(key);
            existing.setCorePoolSize(newConfig.getCorePoolSize());
            existing.setMaxPoolSize(newConfig.getMaxPoolSize());
            existing.setQueueCapacity(newConfig.getQueueCapacity());
            existing.setKeepAliveSeconds(newConfig.getKeepAliveSeconds());
            existing.setRejectedPolicy(newConfig.getRejectedPolicy());
            configStore.put(key, existing);
            log.info("Updated config for all instances: [{}]", key);
        }
        return keysToUpdate.size();
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
        String key = buildConfigKey(instanceId, poolName);
        return configStore.get(key);
    }

    public List<ThreadPoolConfig> getAllConfigs(String applicationName) {
        if (applicationName == null || applicationName.isEmpty()) {
            return new ArrayList<>(configStore.values());
        }
        return configStore.values().stream()
            .filter(c -> applicationName.equals(c.getApplicationName()))
            .collect(Collectors.toList());
    }

    public PagedResult<ThreadPoolConfig> getConfigsPaged(String applicationName, int page, int size) {
        List<ThreadPoolConfig> allConfigs = getAllConfigs(applicationName);
        int total = allConfigs.size();
        int fromIndex = (page - 1) * size;
        if (fromIndex >= total) {
            return new PagedResult<>(new ArrayList<>(), total, page, size);
        }
        int toIndex = Math.min(fromIndex + size, total);
        List<ThreadPoolConfig> pagedConfigs = allConfigs.subList(fromIndex, toIndex);
        return new PagedResult<>(new ArrayList<>(pagedConfigs), total, page, size);
    }

    public List<ThreadPoolConfig> getConfigsByInstance(String applicationName, String ip, Integer port) {
        return configStore.values().stream()
            .filter(c -> applicationName.equals(c.getApplicationName()))
            .filter(c -> Objects.equals(c.getIp(), ip) && Objects.equals(c.getPort(), port))
            .collect(Collectors.toList());
    }

    public List<ThreadPoolConfig> getConfigsByPoolName(String applicationName, String poolName) {
        return configStore.values().stream()
            .filter(c -> applicationName.equals(c.getApplicationName()))
            .filter(c -> poolName.equals(c.getPoolName()))
            .collect(Collectors.toList());
    }

    public void reportStatus(ThreadPoolStatus status) {
        String key = buildStatusKey(status.getInstanceId(), status.getPoolName());
        statusStore.put(key, status);
        applications.add(status.getApplicationName());
    }

    public List<ThreadPoolStatus> getAllStatuses(String applicationName) {
        if (applicationName == null || applicationName.isEmpty()) {
            return new ArrayList<>(statusStore.values());
        }
        return statusStore.values().stream()
            .filter(s -> applicationName.equals(s.getApplicationName()))
            .collect(Collectors.toList());
    }

    public List<ThreadPoolStatus> getStatusesByInstance(String applicationName, String ip, Integer port) {
        return statusStore.values().stream()
            .filter(s -> applicationName.equals(s.getApplicationName()))
            .filter(s -> Objects.equals(s.getIp(), ip) && Objects.equals(s.getPort(), port))
            .collect(Collectors.toList());
    }

    public List<ApplicationInfo> getAllApplicationsWithInstances() {
        List<ApplicationInfo> result = new ArrayList<>();
        for (String appName : applications) {
            ApplicationInfo appInfo = new ApplicationInfo(appName);
            Set<InstanceInfo> instances = applicationInstances.get(appName);
            if (instances != null) {
                appInfo.setInstances(new HashSet<>(instances));
            }
            result.add(appInfo);
        }
        return result;
    }

    public List<String> getAllApplications() {
        return new ArrayList<>(applications);
    }

    public Map<String, ThreadPoolConfig> getAllConfigsMap() {
        return new HashMap<>(configStore);
    }

    public Map<String, ThreadPoolStatus> getAllStatusesMap() {
        return new HashMap<>(statusStore);
    }

    private String buildConfigKey(String instanceId, String poolName) {
        return instanceId + ":" + poolName;
    }

    private String buildStatusKey(String instanceId, String poolName) {
        return instanceId + ":" + poolName;
    }
}