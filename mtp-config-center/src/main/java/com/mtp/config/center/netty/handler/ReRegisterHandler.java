package com.mtp.config.center.netty.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtp.config.center.netty.MessageContext;
import com.mtp.core.model.ThreadPoolConfig;
import com.mtp.core.netty.MessageRequest;
import com.mtp.core.netty.MessageType;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.util.Map;

/**
 * 注册处理器，处理客户端重新注册
 */
public class ReRegisterHandler extends AbstractMessageHandler {

    public ReRegisterHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public MessageType getType() {
        return MessageType.RE_REGISTER;
    }

    @Override
    public String handle(ChannelHandlerContext ctx, MessageRequest request, MessageContext context) throws IOException {
        if (request.payload instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) request.payload;
            context.getNettyServer().registerInstance(ctx.channel(), payload);
        }

        return buildResponse(request.correlationId, MessageType.REGISTER, true);
    }

}