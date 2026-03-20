package com.mtp.config.center.controller;

import com.mtp.config.center.service.ConfigCenterService;
import com.mtp.core.model.ApplicationInfo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 应用控制器，提供应用相关信息的API
 */
@RestController
@RequestMapping("/api")
public class ApplicationController {

    private final ConfigCenterService configCenterService;

    public ApplicationController(ConfigCenterService configCenterService) {
        this.configCenterService = configCenterService;
    }

    @GetMapping("/applications")
    public List<ApplicationInfo> getAllApplications() {
        return configCenterService.getAllApplicationsWithInstances();
    }
}