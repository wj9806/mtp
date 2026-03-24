package com.mtp.config.center.netty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtp.config.center.netty.handler.MessageHandler;
import com.mtp.core.netty.MessageRequest;
import com.mtp.core.netty.MessageType;
import com.mtp.core.netty.MessageResponse;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息处理器注册表，用于将消息类型路由到对应的处理器
 */
public class MessageHandlerRegistry {
    private final ObjectMapper objectMapper;
    private final Map<MessageType, MessageHandler> handlers = new HashMap<>();

    public MessageHandlerRegistry(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public MessageHandlerRegistry register(MessageHandler handler) {
        handlers.put(handler.getType(), handler);
        return this;
    }

    public String route(ChannelHandlerContext ctx, MessageRequest request, MessageContext context) throws IOException {
        MessageType type = MessageType.fromString(request.type);
        if (type != null) {
            MessageHandler handler = handlers.get(type);
            if (handler != null) {
                return handler.handle(ctx, request, context);
            }
        }
        return buildErrorResponse(request.correlationId, "Unknown request type: " + request.type);
    }

    private String buildErrorResponse(String correlationId, String error) throws IOException {
        MessageResponse response = new MessageResponse();
        response.correlationId = correlationId;
        response.error = error;
        return objectMapper.writeValueAsString(response);
    }
}