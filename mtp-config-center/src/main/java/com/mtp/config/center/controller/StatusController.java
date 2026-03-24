package com.mtp.config.center.controller;

import com.mtp.config.center.service.ConfigCenterService;
import com.mtp.core.model.ThreadPoolStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 状态控制器，提供线程池状态查询的API
 */
@RestController
@RequestMapping("/api/status")
public class StatusController {

    private final ConfigCenterService configCenterService;

    public StatusController(ConfigCenterService configCenterService) {
        this.configCenterService = configCenterService;
    }

    @GetMapping("/list")
    public List<ThreadPoolStatus> list(@RequestParam(required = false) String applicationName,
                                      @RequestParam(required = false) String ip,
                                      @RequestParam(required = false) Integer port) {
        if (ip != null && port != null) {
            return configCenterService.getStatusesByInstance(applicationName, ip, port);
        }
        return configCenterService.getAllStatuses(applicationName);
    }
}