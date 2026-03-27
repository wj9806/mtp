package com.mtp.config.center.server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtp.config.center.server.MessageContext;
import com.mtp.core.model.ThreadPoolConfig;
import com.mtp.core.netty.MessageRequest;
import com.mtp.core.netty.MessageType;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;

/**
 * 更新配置处理器，处理客户端更新线程池配置的请求
 */
public class UpdateConfigHandler extends AbstractMessageHandler {

    public UpdateConfigHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public MessageType getType() {
        return MessageType.UPDATE_CONFIG;
    }

    @Override
    public String handle(ChannelHandlerContext ctx, MessageRequest request, MessageContext context) throws IOException {
        ThreadPoolConfig config = parsePayload(request.payload, ThreadPoolConfig.class);
        if (config != null) {
            context.getConfigCenterService().updateConfig(config);
            context.getMtpServer().notifyConfigChange(config.getApplicationName(), config.getPoolName());
            return buildResponse(request.correlationId, MessageType.UPDATE_CONFIG, true);
        }
        return buildErrorResponse(request.correlationId, "Invalid config payload");
    }

}