package com.mtp.core.api;

import com.mtp.core.model.ApplicationInfo;
import com.mtp.core.model.ThreadPoolConfig;
import com.mtp.core.model.ThreadPoolStatus;

import java.util.List;

/**
 * 配置中心客户端接口，定义与配置中心交互的基本操作
 */
public interface ConfigCenterClient {

    void register(ThreadPoolConfig config);

    void unregister(String poolName, String applicationName, String ip, Integer port);

    void updateConfig(ThreadPoolConfig config);

    int updateConfigsByAppAndPoolName(String applicationName, String poolName, ThreadPoolConfig config);

    ThreadPoolConfig getConfig(String poolName, String applicationName, String ip, Integer port);

    List<ThreadPoolConfig> getAllConfigs(String applicationName);

    List<ThreadPoolConfig> getConfigsByPoolName(String applicationName, String poolName);

    void reportStatus(ThreadPoolStatus status);

    List<ThreadPoolStatus> getAllStatuses(String applicationName);

    List<ApplicationInfo> getAllApplications();

    String getConfigCenterUrl();
}