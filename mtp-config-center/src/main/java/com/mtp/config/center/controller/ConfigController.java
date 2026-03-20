package com.mtp.config.center.controller;

import com.mtp.config.center.netty.NettyServer;
import com.mtp.config.center.service.ConfigCenterService;
import com.mtp.core.model.ThreadPoolConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * 配置控制器，提供线程池配置管理的API
 */
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
        configCenterService.updateConfig(config);
        notifyConfigChange(config.getApplicationName(), config.getPoolName());
    }

    @GetMapping("/list")
    public List<ThreadPoolConfig> list(@RequestParam(required = false) String applicationName,
                                      @RequestParam(required = false) String ip,
                                      @RequestParam(required = false) Integer port) {
        if (ip != null && port != null) {
            return configCenterService.getConfigsByInstance(applicationName, ip, port);
        }
        return configCenterService.getAllConfigs(applicationName);
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
            e.printStackTrace();
        }
    }
}