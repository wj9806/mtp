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

/**
 * 基于Netty的配置中心客户端实现
 * 通过Netty客户端与服务端通信，实现配置的注册、更新、查询等功能
 */
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
        String key = buildKey(config.getPoolName(), config.getApplicationName(), config.getIp(), config.getPort());
        configCache.put(key, config);
        try {
            nettyClient.sendNotification(MessageType.REGISTER, config);
        } catch (Exception e) {
            log.error("Failed to register config", e);
        }
    }

    @Override
    public void unregister(String poolName, String applicationName, String ip, Integer port) {
        String key = buildKey(poolName, applicationName, ip, port);
        configCache.remove(key);
        try {
            Map<String, Object> params = new ConcurrentHashMap<>();
            params.put("poolName", poolName);
            params.put("applicationName", applicationName);
            params.put("ip", ip);
            params.put("port", port);
            nettyClient.sendNotification(MessageType.UNREGISTER, params);
        } catch (Exception e) {
            log.error("Failed to unregister", e);
        }
    }

    @Override
    public void updateConfig(ThreadPoolConfig config) {
        String key = buildKey(config.getPoolName(), config.getApplicationName(), config.getIp(), config.getPort());
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
    public ThreadPoolConfig getConfig(String poolName, String applicationName, String ip, Integer port) {
        String key = buildKey(poolName, applicationName, ip, port);
        if (configCache.containsKey(key)) {
            return configCache.get(key);
        }
        try {
            Map<String, Object> params = new ConcurrentHashMap<>();
            params.put("poolName", poolName);
            params.put("applicationName", applicationName);
            params.put("ip", ip);
            params.put("port", port);
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

    private String buildKey(String poolName, String applicationName, String ip, Integer port) {
        return poolName + ":" + applicationName + ":" + ip + ":" + port;
    }
}