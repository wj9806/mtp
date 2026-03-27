package com.mtp.config.center.controller;

import com.mtp.config.center.model.R;
import com.mtp.config.center.server.MtpServer;
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
    private final MtpServer mtpServer;

    public StatusController(ConfigCenterService configCenterService, MtpServer mtpServer) {
        this.configCenterService = configCenterService;
        this.mtpServer = mtpServer;
    }

    @GetMapping("/list")
    public R list(@RequestParam(required = false) String applicationName,
                  @RequestParam(required = false) String ip,
                  @RequestParam(required = false) Integer port) {
        List<ThreadPoolStatus> statuses;
        if (ip != null && port != null) {
            statuses = configCenterService.getStatusesByInstance(applicationName, ip, port);
        } else {
            statuses = configCenterService.getAllStatuses(applicationName);
        }
        return R.ok(statuses);
    }

    @PostMapping("/refresh/{instanceId}/{poolName}")
    public R refreshStatus(@PathVariable String instanceId, @PathVariable String poolName) {
        mtpServer.requestClientStatus(instanceId, poolName);
        return R.ok();
    }
}