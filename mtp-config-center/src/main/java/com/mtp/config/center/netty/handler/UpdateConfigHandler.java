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
 * 更新配置处理器，处理客户端更新线程池配置的请求
 */
public class UpdateConfigHandler implements MessageHandler {
    private final ObjectMapper objectMapper;

    public UpdateConfigHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public MessageType getType() {
        return MessageType.UPDATE_CONFIG;
    }

    @Override
    public String handle(MessageRequest request, MessageContext context) throws IOException {
        ThreadPoolConfig config = parsePayload(request.payload, ThreadPoolConfig.class);
        if (config != null) {
            context.getConfigCenterService().updateConfig(config);
            context.getNettyServer().notifyConfigChange(config.getApplicationName(), config.getPoolName());
            return buildResponse(request.correlationId, MessageType.UPDATE_CONFIG, true);
        }
        return buildErrorResponse(request.correlationId, "Invalid config payload");
    }

    private ThreadPoolConfig parsePayload(Object payload, Class<ThreadPoolConfig> clazz) {
        if (payload == null) return null;
        if (payload instanceof Map) return objectMapper.convertValue(payload, clazz);
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