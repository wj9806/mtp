package com.mtp.config.center.netty;

import com.mtp.config.center.service.ConfigCenterService;

/**
 * 消息上下文，包含处理消息所需的服务和服务器引用
 */
public class MessageContext {
    private final ConfigCenterService configCenterService;
    private final NettyServer nettyServer;

    public MessageContext(ConfigCenterService configCenterService, NettyServer nettyServer) {
        this.configCenterService = configCenterService;
        this.nettyServer = nettyServer;
    }

    public ConfigCenterService getConfigCenterService() {
        return configCenterService;
    }

    public NettyServer getNettyServer() {
        return nettyServer;
    }
}