package com.mtp.config.center.server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtp.config.center.server.MessageContext;
import com.mtp.core.model.ThreadPoolConfig;
import com.mtp.core.netty.MessageRequest;
import com.mtp.core.netty.MessageType;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.util.Map;

/**
 * 注册处理器，处理客户端注册线程池配置的请求
 */
public class RegisterHandler extends AbstractMessageHandler {

    public RegisterHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public MessageType getType() {
        return MessageType.REGISTER;
    }

    @Override
    public String handle(ChannelHandlerContext ctx, MessageRequest request, MessageContext context) throws IOException {
        if (request.payload instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) request.payload;
            context.getMtpServer().registerInstance(ctx.channel(), payload);
        }

        ThreadPoolConfig config = parsePayload(request.payload, ThreadPoolConfig.class);
        if (config != null) {
            context.getConfigCenterService().registerConfig(config);
            context.getConfigCenterService().registerClient(
                config.getInstanceId(),
                config.getApplicationName(),
                config.getIp(),
                config.getPort()
            );
            return buildResponse(request.correlationId, MessageType.REGISTER, true);
        }
        return buildErrorResponse(request.correlationId, "Invalid config payload");
    }

}