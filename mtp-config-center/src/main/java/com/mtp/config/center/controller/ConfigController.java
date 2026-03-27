package com.mtp.config.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mtp.config.center.model.R;
import com.mtp.config.center.server.MtpServer;
import com.mtp.config.center.service.ConfigCenterService;
import com.mtp.core.model.ThreadPoolConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置控制器，提供线程池配置管理的API
 */
@Slf4j
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final ConfigCenterService configCenterService;
    private final MtpServer mtpServer;

    public ConfigController(ConfigCenterService configCenterService, MtpServer mtpServer) {
        this.configCenterService = configCenterService;
        this.mtpServer = mtpServer;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @PutMapping("/update")
    public R update(@RequestBody ThreadPoolConfig config) {
        String instanceId = config.getInstanceId();
        String poolName = config.getPoolName();
        configCenterService.updateConfigById(instanceId, poolName, config);
        notifyConfigChangeById(instanceId, poolName);
        return R.ok();
    }

    private void notifyConfigChangeById(String instanceId, String poolName) {
        try {
            ThreadPoolConfig config = configCenterService.getConfig(instanceId, poolName);
            if (config != null) {
                mtpServer.broadcastConfigChangeById(instanceId, poolName, config);
            }
        } catch (Exception e) {
            log.error("Failed to notify config change by id", e);
        }
    }

    @GetMapping("/list")
    public R list(@RequestParam(required = false) String applicationName,
                  @RequestParam(required = false) String ip,
                  @RequestParam(required = false) Integer port,
                  @RequestParam(defaultValue = "1") int page,
                  @RequestParam(defaultValue = "10") int size) {
        if (ip != null && port != null) {
            List<ThreadPoolConfig> configs = configCenterService.getConfigsByInstance(applicationName, ip, port);
            Map<String, Object> data = new HashMap<>();
            data.put("records", configs);
            data.put("total", configs.size());
            return R.ok(data);
        }
        Page<ThreadPoolConfig> pageResult = configCenterService.getConfigsPaged(applicationName, page, size);
        return R.ok(pageResult);
    }


    @PutMapping("/update-batch")
    public R updateBatch(@RequestParam String applicationName,
                         @RequestParam String poolName,
                         @RequestBody ThreadPoolConfig config) {
        int count = configCenterService.updateConfigsByAppAndPoolName(applicationName, poolName, config);
        if (count > 0) {
            notifyConfigChange(applicationName, poolName);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("count", count);
        return R.ok(data);
    }

    @GetMapping("/get-by-pool")
    public R getByPool(@RequestParam String applicationName,
                       @RequestParam String poolName) {
        List<ThreadPoolConfig> configs = configCenterService.getConfigsByPoolName(applicationName, poolName);
        return R.ok(configs);
    }

    private void notifyConfigChange(String applicationName, String poolName) {
        try {
            List<ThreadPoolConfig> configs = configCenterService.getConfigsByPoolName(applicationName, poolName);
            mtpServer.broadcastConfigChange(applicationName, poolName, configs);
        } catch (Exception e) {
            log.error("Failed to notify config change", e);
        }
    }
}