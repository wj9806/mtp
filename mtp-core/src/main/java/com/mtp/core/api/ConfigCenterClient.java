package com.mtp.core.api;

import com.mtp.core.model.ApplicationInfo;
import com.mtp.core.model.ThreadPoolConfig;
import com.mtp.core.model.ThreadPoolStatus;

import java.util.List;

public interface ConfigCenterClient {

    void register(ThreadPoolConfig config);

    void unregister(String instanceId, String poolName);

    void updateConfig(ThreadPoolConfig config);

    int updateConfigsByAppAndPoolName(String applicationName, String poolName, ThreadPoolConfig config);

    ThreadPoolConfig getConfig(String instanceId, String poolName);

    List<ThreadPoolConfig> getAllConfigs(String applicationName);

    List<ThreadPoolConfig> getConfigsByInstanceId(String instanceId, String poolName);

    void reportStatus(ThreadPoolStatus status);

    List<ThreadPoolStatus> getAllStatuses(String applicationName);

    List<ApplicationInfo> getAllApplications();

    String getConfigCenterUrl();
}