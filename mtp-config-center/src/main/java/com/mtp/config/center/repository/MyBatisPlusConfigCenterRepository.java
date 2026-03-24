package com.mtp.config.center.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mtp.config.center.entity.ClientRegistryEntity;
import com.mtp.config.center.entity.ThreadPoolConfigEntity;
import com.mtp.config.center.entity.ThreadPoolStatusEntity;
import com.mtp.config.center.mapper.ClientRegistryMapper;
import com.mtp.config.center.mapper.ThreadPoolConfigMapper;
import com.mtp.config.center.mapper.ThreadPoolStatusMapper;
import com.mtp.config.center.model.ClientStatus;
import com.mtp.core.model.ApplicationInfo;
import com.mtp.core.model.ApplicationInfo.InstanceInfo;
import com.mtp.core.model.ThreadPoolConfig;
import com.mtp.core.model.ThreadPoolStatus;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Transactional
public class MyBatisPlusConfigCenterRepository implements ConfigCenterRepository {

    private final ThreadPoolConfigMapper configMapper;
    private final ThreadPoolStatusMapper statusMapper;
    private final ClientRegistryMapper clientRegistryMapper;

    public MyBatisPlusConfigCenterRepository(ThreadPoolConfigMapper configMapper,
                                              ThreadPoolStatusMapper statusMapper,
                                              ClientRegistryMapper clientRegistryMapper) {
        this.configMapper = configMapper;
        this.statusMapper = statusMapper;
        this.clientRegistryMapper = clientRegistryMapper;
    }

    @Override
    public void saveConfig(ThreadPoolConfig config) {
        ThreadPoolConfigEntity entity = toConfigEntity(config);
        entity.setUpdateTime(System.currentTimeMillis());
        LambdaQueryWrapper<ThreadPoolConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThreadPoolConfigEntity::getInstanceId, entity.getInstanceId())
               .eq(ThreadPoolConfigEntity::getPoolName, entity.getPoolName());
        ThreadPoolConfigEntity existing = configMapper.selectOne(wrapper);
        if (existing != null) {
            configMapper.updateById(entity);
        } else {
            configMapper.insert(entity);
        }
    }

    @Override
    public void updateConfig(ThreadPoolConfig config) {
        ThreadPoolConfigEntity entity = toConfigEntity(config);
        entity.setUpdateTime(System.currentTimeMillis());
        configMapper.updateById(entity);
    }

    @Override
    public void deleteConfig(String instanceId, String poolName) {
        LambdaQueryWrapper<ThreadPoolConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThreadPoolConfigEntity::getInstanceId, instanceId)
               .eq(ThreadPoolConfigEntity::getPoolName, poolName);
        configMapper.delete(wrapper);
    }

    @Override
    public ThreadPoolConfig findConfigById(String instanceId, String poolName) {
        LambdaQueryWrapper<ThreadPoolConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThreadPoolConfigEntity::getInstanceId, instanceId)
               .eq(ThreadPoolConfigEntity::getPoolName, poolName);
        ThreadPoolConfigEntity entity = configMapper.selectOne(wrapper);
        return entity == null ? null : toConfigModel(entity);
    }

    @Override
    public List<ThreadPoolConfig> findAllConfigs() {
        return configMapper.selectList(null).stream()
            .map(this::toConfigModel)
            .collect(Collectors.toList());
    }

    @Override
    public List<ThreadPoolConfig> findConfigsByApplication(String applicationName) {
        if (applicationName == null || applicationName.isEmpty()) {
            return findAllConfigs();
        }
        LambdaQueryWrapper<ThreadPoolConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThreadPoolConfigEntity::getApplicationName, applicationName);
        return configMapper.selectList(wrapper).stream()
            .map(this::toConfigModel)
            .collect(Collectors.toList());
    }

    @Override
    public List<ThreadPoolConfig> findConfigsByPoolName(String applicationName, String poolName) {
        LambdaQueryWrapper<ThreadPoolConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThreadPoolConfigEntity::getApplicationName, applicationName)
               .eq(ThreadPoolConfigEntity::getPoolName, poolName);
        return configMapper.selectList(wrapper).stream()
            .map(this::toConfigModel)
            .collect(Collectors.toList());
    }

    @Override
    public List<ThreadPoolConfig> findConfigsPaged(String applicationName, int page, int size) {
        Page<ThreadPoolConfigEntity> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ThreadPoolConfigEntity> wrapper = new LambdaQueryWrapper<>();
        if (applicationName != null && !applicationName.isEmpty()) {
            wrapper.eq(ThreadPoolConfigEntity::getApplicationName, applicationName);
        }
        IPage<ThreadPoolConfigEntity> result = configMapper.selectPage(pageParam, wrapper);
        return result.getRecords().stream()
            .map(this::toConfigModel)
            .collect(Collectors.toList());
    }

    @Override
    public int countConfigs(String applicationName) {
        LambdaQueryWrapper<ThreadPoolConfigEntity> wrapper = new LambdaQueryWrapper<>();
        if (applicationName != null && !applicationName.isEmpty()) {
            wrapper.eq(ThreadPoolConfigEntity::getApplicationName, applicationName);
        }
        return configMapper.selectCount(wrapper).intValue();
    }

    @Override
    public void saveStatus(ThreadPoolStatus status) {
        ThreadPoolStatusEntity entity = toStatusEntity(status);
        entity.setUpdateTime(System.currentTimeMillis());
        LambdaQueryWrapper<ThreadPoolStatusEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThreadPoolStatusEntity::getInstanceId, entity.getInstanceId())
               .eq(ThreadPoolStatusEntity::getPoolName, entity.getPoolName());
        ThreadPoolStatusEntity existing = statusMapper.selectOne(wrapper);
        if (existing != null) {
            statusMapper.updateById(entity);
        } else {
            statusMapper.insert(entity);
        }
    }

    @Override
    public void deleteStatus(String instanceId, String poolName) {
        LambdaQueryWrapper<ThreadPoolStatusEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThreadPoolStatusEntity::getInstanceId, instanceId)
               .eq(ThreadPoolStatusEntity::getPoolName, poolName);
        statusMapper.delete(wrapper);
    }

    @Override
    public ThreadPoolStatus findStatusById(String instanceId, String poolName) {
        LambdaQueryWrapper<ThreadPoolStatusEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThreadPoolStatusEntity::getInstanceId, instanceId)
               .eq(ThreadPoolStatusEntity::getPoolName, poolName);
        ThreadPoolStatusEntity entity = statusMapper.selectOne(wrapper);
        return entity == null ? null : toStatusModel(entity);
    }

    @Override
    public List<ThreadPoolStatus> findAllStatuses() {
        return statusMapper.selectList(null).stream()
            .map(this::toStatusModel)
            .collect(Collectors.toList());
    }

    @Override
    public List<ThreadPoolStatus> findStatusesByApplication(String applicationName) {
        if (applicationName == null || applicationName.isEmpty()) {
            return findAllStatuses();
        }
        LambdaQueryWrapper<ThreadPoolStatusEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThreadPoolStatusEntity::getApplicationName, applicationName);
        return statusMapper.selectList(wrapper).stream()
            .map(this::toStatusModel)
            .collect(Collectors.toList());
    }

    @Override
    public List<String> findAllApplications() {
        //todo
        return clientRegistryMapper.selectList(null).stream().map(ClientRegistryEntity::getApplicationName)
                .distinct().collect(Collectors.toList());
    }

    @Override
    public void registerClient(String instanceId, String applicationName, String ip, Integer port) {
        ClientRegistryEntity entity = new ClientRegistryEntity();
        entity.setId(instanceId);
        entity.setInstanceId(instanceId);
        entity.setApplicationName(applicationName);
        entity.setIp(ip);
        entity.setPort(port);
        entity.setStatus(ClientStatus.ONLINE.toString());
        entity.setCreateTime(System.currentTimeMillis());
        entity.setReportTime(System.currentTimeMillis());

        ClientRegistryEntity existing = clientRegistryMapper.selectById(instanceId);
        if (existing != null) {
            entity.setCreateTime(existing.getCreateTime());
            clientRegistryMapper.updateById(entity);
        } else {
            clientRegistryMapper.insert(entity);
        }
    }

    @Override
    public void updateClientReportTime(String instanceId) {
        ClientRegistryEntity entity = clientRegistryMapper.selectById(instanceId);
        if (entity != null) {
            entity.setReportTime(System.currentTimeMillis());
            entity.setStatus(ClientStatus.ONLINE.toString());
            clientRegistryMapper.updateById(entity);
        }
    }

    @Override
    public void updateClientStatus(String instanceId, String status) {
        ClientRegistryEntity entity = clientRegistryMapper.selectById(instanceId);
        if (entity != null) {
            entity.setStatus(status);
            clientRegistryMapper.updateById(entity);
        }
    }

    @Override
    public List<String> findAllClientInstanceIds() {
        return clientRegistryMapper.selectList(null).stream()
            .map(ClientRegistryEntity::getInstanceId)
            .collect(Collectors.toList());
    }

    @Override
    public String findClientStatus(String instanceId) {
        ClientRegistryEntity entity = clientRegistryMapper.selectById(instanceId);
        return entity != null ? entity.getStatus() : null;
    }

    @Override
    public Long findClientReportTime(String instanceId) {
        ClientRegistryEntity entity = clientRegistryMapper.selectById(instanceId);
        return entity != null ? entity.getReportTime() : null;
    }

    @Override
    public List<ApplicationInfo> findApplicationsFromRegistryPaged(String applicationName, int page, int size) {
        LambdaQueryWrapper<ClientRegistryEntity> wrapper = new LambdaQueryWrapper<>();
        if (applicationName != null && !applicationName.isEmpty()) {
            wrapper.like(ClientRegistryEntity::getApplicationName, applicationName);
        }
        List<ClientRegistryEntity> allClients = clientRegistryMapper.selectList(wrapper);

        Map<String, Set<InstanceInfo>> appInstances = new LinkedHashMap<>();
        for (ClientRegistryEntity client : allClients) {
            String appName = client.getApplicationName();
            Set<InstanceInfo> instanceInfos = appInstances.computeIfAbsent(appName, k -> new HashSet<>());
            instanceInfos.add(new InstanceInfo(client.getIp(), client.getPort(), client.getStatus()));
        }

        List<String> appList = new ArrayList<>(appInstances.keySet());
        int total = appList.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);

        if (start >= total) {
            return new ArrayList<>();
        }

        List<String> pagedApps = appList.subList(start, end);
        return pagedApps.stream().map(appName -> {
            ApplicationInfo info = new ApplicationInfo(appName);
            info.setInstances(appInstances.get(appName));
            return info;
        }).collect(Collectors.toList());
    }

    @Override
    public int countApplicationsFromRegistry(String applicationName) {
        LambdaQueryWrapper<ClientRegistryEntity> wrapper = new LambdaQueryWrapper<>();
        if (applicationName != null && !applicationName.isEmpty()) {
            wrapper.like(ClientRegistryEntity::getApplicationName, applicationName);
        }
        List<ClientRegistryEntity> clients = clientRegistryMapper.selectList(wrapper);
        Set<String> uniqueApps = new HashSet<>();
        for (ClientRegistryEntity client : clients) {
            uniqueApps.add(client.getApplicationName());
        }
        return uniqueApps.size();
    }

    private ThreadPoolConfigEntity toConfigEntity(ThreadPoolConfig config) {
        ThreadPoolConfigEntity entity = new ThreadPoolConfigEntity();
        entity.setId(config.getInstanceId() + ":" + config.getPoolName());
        BeanUtils.copyProperties(config, entity);
        return entity;
    }

    private ThreadPoolConfig toConfigModel(ThreadPoolConfigEntity entity) {
        ThreadPoolConfig config = new ThreadPoolConfig();
        BeanUtils.copyProperties(entity, config);
        return config;
    }

    private ThreadPoolStatusEntity toStatusEntity(ThreadPoolStatus status) {
        ThreadPoolStatusEntity entity = new ThreadPoolStatusEntity();
        entity.setId(status.getInstanceId() + ":" + status.getPoolName());
        BeanUtils.copyProperties(status, entity);
        return entity;
    }

    private ThreadPoolStatus toStatusModel(ThreadPoolStatusEntity entity) {
        ThreadPoolStatus status = new ThreadPoolStatus();
        BeanUtils.copyProperties(entity, status);
        return status;
    }
}