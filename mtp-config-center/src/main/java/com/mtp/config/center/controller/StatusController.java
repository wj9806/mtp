package com.mtp.config.center.controller;

import com.mtp.config.center.netty.NettyServer;
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
    private final NettyServer nettyServer;

    public StatusController(ConfigCenterService configCenterService, NettyServer nettyServer) {
        this.configCenterService = configCenterService;
        this.nettyServer = nettyServer;
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

    @PostMapping("/refresh/{instanceId}/{poolName}")
    public void refreshStatus(@PathVariable String instanceId, @PathVariable String poolName) {
        nettyServer.requestClientStatus(instanceId, poolName);
    }
}