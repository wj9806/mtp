package com.mtp.config.center.netty.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtp.config.center.netty.MessageContext;
import com.mtp.core.model.ThreadPoolConfig;
import com.mtp.core.netty.MessageRequest;
import com.mtp.core.netty.MessageType;
import com.mtp.core.netty.MessageResponse;

import java.io.IOException;
import java.util.Map;

/**
 * 注册处理器，处理客户端注册线程池配置的请求
 */
public class RegisterHandler implements MessageHandler {
    private final ObjectMapper objectMapper;

    public RegisterHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public MessageType getType() {
        return MessageType.REGISTER;
    }

    @Override
    public String handle(MessageRequest request, MessageContext context) throws IOException {
        ThreadPoolConfig config = parsePayload(request.payload, ThreadPoolConfig.class);
        if (config != null) {
            context.getConfigCenterService().registerConfig(config);
            return buildResponse(request.correlationId, MessageType.REGISTER, true);
        }
        return buildErrorResponse(request.correlationId, "Invalid config payload");
    }

    private ThreadPoolConfig parsePayload(Object payload, Class<ThreadPoolConfig> clazz) {
        if (payload == null) {
            return null;
        }
        if (payload instanceof Map) {
            return objectMapper.convertValue(payload, clazz);
        }
        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(payload), clazz);
        } catch (Exception e) {
            return null;
        }
    }

    private String buildResponse(String correlationId, MessageType type, Object result) throws IOException {
        MessageResponse response = new MessageResponse();
        response.correlationId = correlationId;
        response.type = type.getType();
        response.data = result != null ? objectMapper.writeValueAsString(result) : null;
        return objectMapper.writeValueAsString(response);
    }

    private String buildErrorResponse(String correlationId, String error) throws IOException {
        MessageResponse response = new MessageResponse();
        response.correlationId = correlationId;
        response.error = error;
        return objectMapper.writeValueAsString(response);
    }
}