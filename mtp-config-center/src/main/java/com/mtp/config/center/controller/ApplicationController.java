package com.mtp.config.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mtp.config.center.model.R;
import com.mtp.config.center.service.ConfigCenterService;
import com.mtp.core.model.ApplicationInfo;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public R getApplications(
            @RequestParam(required = false) String applicationName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "4") int size) {
        Page<ApplicationInfo> pageResult = configCenterService.getApplicationsFromRegistryPaged(applicationName, page, size);
        return R.ok(pageResult);
    }

    @GetMapping("/applicationList")
    public R getApplicationList() {
        List<ApplicationInfo> applications = configCenterService.getAllApplications();
        return R.ok(applications);
    }
}