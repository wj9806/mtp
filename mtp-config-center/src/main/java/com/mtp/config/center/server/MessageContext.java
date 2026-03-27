package com.mtp.config.center.server;

import com.mtp.config.center.service.ConfigCenterService;

/**
 * 消息上下文，包含处理消息所需的服务和服务器引用
 */
public class MessageContext {
    private final ConfigCenterService configCenterService;
    private final MtpServer mtpServer;

    public MessageContext(ConfigCenterService configCenterService, MtpServer mtpServer) {
        this.configCenterService = configCenterService;
        this.mtpServer = mtpServer;
    }

    public ConfigCenterService getConfigCenterService() {
        return configCenterService;
    }

    public MtpServer getMtpServer() {
        return mtpServer;
    }
}