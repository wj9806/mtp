package com.mtp.config.center.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mtp.core.model.ApplicationInfo;
import com.mtp.core.model.ThreadPoolConfig;
import com.mtp.core.model.ThreadPoolStatus;

import java.util.List;

public interface ConfigCenterRepository {

    void saveConfig(ThreadPoolConfig config);

    void updateConfig(ThreadPoolConfig config);

    void deleteConfig(String instanceId, String poolName);

    ThreadPoolConfig findConfig(String instanceId, String poolName);

    List<ThreadPoolConfig> findConfigListByInstanceId(String instanceId);

    List<ThreadPoolConfig> findAllConfigs();

    List<ThreadPoolConfig> findConfigsByApplication(String applicationName);

    List<ThreadPoolConfig> findConfigsByPoolName(String applicationName, String poolName);

    Page<ThreadPoolConfig> findConfigsPaged(String applicationName, int page, int size);

    void saveStatus(ThreadPoolStatus status);

    void deleteStatus(String instanceId, String poolName);

    List<ThreadPoolStatus> findAllStatuses();

    List<ThreadPoolStatus> findStatusesByApplication(String applicationName);

    List<String> findAllApplications();

    void registerClient(String instanceId, String applicationName, String ip, Integer port);

    void updateClientReportTime(String instanceId);

    void updateClientStatus(String instanceId, String status);

    List<String> findAllClientInstanceIds();

    String findClientStatus(String instanceId);

    Long findClientReportTime(String instanceId);

    Page<ApplicationInfo> findApplicationsFromRegistryPaged(String applicationName, int page, int size);

    int countApplicationsFromRegistry(String applicationName);
}