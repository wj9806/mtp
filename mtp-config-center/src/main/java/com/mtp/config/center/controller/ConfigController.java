package com.mtp.config.center.controller;

import com.mtp.config.center.netty.NettyServer;
import com.mtp.config.center.service.ConfigCenterService;
import com.mtp.config.center.service.PagedResult;
import com.mtp.core.model.ThreadPoolConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * 配置控制器，提供线程池配置管理的API
 */
@Slf4j
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final ConfigCenterService configCenterService;
    private final NettyServer nettyServer;

    public ConfigController(ConfigCenterService configCenterService, NettyServer nettyServer) {
        this.configCenterService = configCenterService;
        this.nettyServer = nettyServer;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @PutMapping("/update")
    public void update(@RequestBody ThreadPoolConfig config) {
        String instanceId = config.getInstanceId();
        String poolName = config.getPoolName();
        configCenterService.updateConfigById(instanceId, poolName, config);
        notifyConfigChangeById(instanceId, poolName);
    }

    private void notifyConfigChangeById(String instanceId, String poolName) {
        try {
            ThreadPoolConfig config = configCenterService.getConfig(instanceId, poolName);
            if (config != null) {
                nettyServer.broadcastConfigChangeById(instanceId, poolName, config);
            }
        } catch (Exception e) {
            log.error("Failed to notify config change by id", e);
        }
    }

    @GetMapping("/list")
    public PagedResult<ThreadPoolConfig> list(@RequestParam(required = false) String applicationName,
                                               @RequestParam(required = false) String ip,
                                               @RequestParam(required = false) Integer port,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        if (ip != null && port != null) {
            List<ThreadPoolConfig> configs = configCenterService.getConfigsByInstance(applicationName, ip, port);
            return new PagedResult<>(configs, configs.size(), page, size);
        }
        return configCenterService.getConfigsPaged(applicationName, page, size);
    }

    @PutMapping("/update-batch")
    public int updateBatch(@RequestParam String applicationName,
                          @RequestParam String poolName,
                          @RequestBody ThreadPoolConfig config) {
        int count = configCenterService.updateConfigsByAppAndPoolName(applicationName, poolName, config);
        if (count > 0) {
            notifyConfigChange(applicationName, poolName);
        }
        return count;
    }

    @GetMapping("/get-by-pool")
    public List<ThreadPoolConfig> getByPool(@RequestParam String applicationName,
                                           @RequestParam String poolName) {
        return configCenterService.getConfigsByPoolName(applicationName, poolName);
    }

    private void notifyConfigChange(String applicationName, String poolName) {
        try {
            List<ThreadPoolConfig> configs = configCenterService.getConfigsByPoolName(applicationName, poolName);
            nettyServer.broadcastConfigChange(applicationName, poolName, configs);
        } catch (Exception e) {
            log.error("Failed to notify config change", e);
        }
    }
}