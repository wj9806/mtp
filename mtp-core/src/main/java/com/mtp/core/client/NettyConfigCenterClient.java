package com.mtp.core.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mtp.core.api.ConfigCenterClient;
import com.mtp.core.model.ApplicationInfo;
import com.mtp.core.model.ThreadPoolConfig;
import com.mtp.core.model.ThreadPoolStatus;
import com.mtp.core.netty.MessageType;
import com.mtp.core.netty.NettyClient;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class NettyConfigCenterClient implements ConfigCenterClient {

    private final NettyClient nettyClient;
    private final Map<String, ThreadPoolConfig> configCache;

    public NettyConfigCenterClient(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
        this.configCache = new ConcurrentHashMap<>();
    }

    @Override
    public void register(ThreadPoolConfig config) {
        String key = buildKey(config.getInstanceId(), config.getPoolName());
        configCache.put(key, config);
        try {
            nettyClient.sendNotification(MessageType.REGISTER, config);
        } catch (Exception e) {
            log.error("Failed to register config", e);
        }
    }

    @Override
    public void unregister(String instanceId, String poolName) {
        String key = buildKey(instanceId, poolName);
        configCache.remove(key);
        try {
            Map<String, Object> params = new ConcurrentHashMap<>();
            params.put("instanceId", instanceId);
            params.put("poolName", poolName);
            nettyClient.sendNotification(MessageType.UNREGISTER, params);
        } catch (Exception e) {
            log.error("Failed to unregister", e);
        }
    }

    @Override
    public void updateConfig(ThreadPoolConfig config) {
        String key = buildKey(config.getInstanceId(), config.getPoolName());
        configCache.put(key, config);
        try {
            nettyClient.sendNotification(MessageType.UPDATE_CONFIG, config);
        } catch (Exception e) {
            log.error("Failed to update config", e);
        }
    }

    @Override
    public int updateConfigsByAppAndPoolName(String applicationName, String poolName, ThreadPoolConfig config) {
        try {
            Map<String, Object> params = new ConcurrentHashMap<>();
            params.put("applicationName", applicationName);
            params.put("poolName", poolName);
            params.put("config", config);
            String response = nettyClient.sendRequest(MessageType.UPDATE_CONFIGS_BY_APP_AND_POOL, params);
            return Integer.parseInt(response);
        } catch (Exception e) {
            log.error("Failed to update configs by app and pool", e);
            return 0;
        }
    }

    @Override
    public ThreadPoolConfig getConfig(String instanceId, String poolName) {
        String key = buildKey(instanceId, poolName);
        if (configCache.containsKey(key)) {
            return configCache.get(key);
        }
        try {
            Map<String, Object> params = new ConcurrentHashMap<>();
            params.put("instanceId", instanceId);
            params.put("poolName", poolName);
            ThreadPoolConfig config = nettyClient.sendRequest(MessageType.GET_CONFIG, params, new TypeReference<ThreadPoolConfig>() {});
            if (config != null) {
                configCache.put(key, config);
            }
            return config;
        } catch (Exception e) {
            log.error("Failed to get config", e);
            return null;
        }
    }

    @Override
    public List<ThreadPoolConfig> getAllConfigs(String applicationName) {
        try {
            Map<String, Object> params = new ConcurrentHashMap<>();
            params.put("applicationName", applicationName);
            return nettyClient.sendRequest(MessageType.GET_ALL_CONFIGS, params, new TypeReference<List<ThreadPoolConfig>>() {});
        } catch (Exception e) {
            log.error("Failed to get all configs", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<ThreadPoolConfig> getConfigsByPoolName(String applicationName, String poolName) {
        try {
            Map<String, Object> params = new ConcurrentHashMap<>();
            params.put("applicationName", applicationName);
            params.put("poolName", poolName);
            return nettyClient.sendRequest(MessageType.GET_CONFIGS_BY_POOL, params, new TypeReference<List<ThreadPoolConfig>>() {});
        } catch (Exception e) {
            log.error("Failed to get configs by pool", e);
            return Collections.emptyList();
        }
    }

    @Override
    public void reportStatus(ThreadPoolStatus status) {
        try {
            nettyClient.sendNotification(MessageType.REPORT_STATUS, status);
        } catch (Exception e) {
            log.error("Failed to report status", e);
        }
    }

    @Override
    public List<ThreadPoolStatus> getAllStatuses(String applicationName) {
        try {
            Map<String, Object> params = new ConcurrentHashMap<>();
            params.put("applicationName", applicationName);
            return nettyClient.sendRequest(MessageType.GET_ALL_STATUSES, params, new TypeReference<List<ThreadPoolStatus>>() {});
        } catch (Exception e) {
            log.error("Failed to get all statuses", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<ApplicationInfo> getAllApplications() {
        try {
            return nettyClient.sendRequest(MessageType.GET_ALL_APPLICATIONS, null, new TypeReference<List<ApplicationInfo>>() {});
        } catch (Exception e) {
            log.error("Failed to get all applications", e);
            return Collections.emptyList();
        }
    }

    @Override
    public String getConfigCenterUrl() {
        return "netty://" + nettyClient;
    }

    private String buildKey(String instanceId, String poolName) {
        return instanceId + ":" + poolName;
    }
}