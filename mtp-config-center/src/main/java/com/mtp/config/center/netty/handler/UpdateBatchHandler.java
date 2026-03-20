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
 * 批量更新配置处理器，处理批量更新线程池配置的请求
 */
public class UpdateBatchHandler implements MessageHandler {
    private final ObjectMapper objectMapper;

    public UpdateBatchHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public MessageType getType() {
        return MessageType.UPDATE_CONFIGS_BY_APP_AND_POOL;
    }

    @Override
    public String handle(MessageRequest request, MessageContext context) throws IOException {
        Map<String, Object> params = toParams(request.payload);
        if (params == null) {
            return buildErrorResponse(request.correlationId, "Invalid payload");
        }

        ThreadPoolConfig config = parsePayload(params.get("config"), ThreadPoolConfig.class);
        String applicationName = (String) params.get("applicationName");
        String poolName = (String) params.get("poolName");

        if (config == null || applicationName == null || poolName == null) {
            return buildErrorResponse(request.correlationId, "Missing required parameters");
        }

        int count = context.getConfigCenterService().updateConfigsByAppAndPoolName(applicationName, poolName, config);
        context.getNettyServer().notifyConfigChange(applicationName, poolName);
        return buildResponse(request.correlationId, MessageType.UPDATE_CONFIGS_BY_APP_AND_POOL, count);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toParams(Object payload) {
        if (payload instanceof Map) {
            return (Map<String, Object>) payload;
        }
        return null;
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