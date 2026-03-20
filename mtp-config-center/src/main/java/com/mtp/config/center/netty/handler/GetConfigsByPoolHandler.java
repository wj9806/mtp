package com.mtp.config.center.netty.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtp.config.center.netty.MessageContext;
import com.mtp.core.model.ThreadPoolConfig;
import com.mtp.core.netty.MessageRequest;
import com.mtp.core.netty.MessageType;
import com.mtp.core.netty.MessageResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 获取线程池配置处理器，处理客户端查询线程池配置的请求
 */
public class GetConfigsByPoolHandler implements MessageHandler {
    private final ObjectMapper objectMapper;

    public GetConfigsByPoolHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public MessageType getType() {
        return MessageType.GET_CONFIGS_BY_POOL;
    }

    @Override
    public String handle(MessageRequest request, MessageContext context) throws IOException {
        Map<String, Object> params = toParams(request.payload);
        if (params == null) {
            return buildErrorResponse(request.correlationId, "Invalid payload");
        }

        String applicationName = (String) params.get("applicationName");
        String poolName = (String) params.get("poolName");

        if (applicationName == null || poolName == null) {
            return buildErrorResponse(request.correlationId, "Missing required parameters");
        }

        List<ThreadPoolConfig> configs = context.getConfigCenterService().getConfigsByPoolName(applicationName, poolName);
        return buildResponse(request.correlationId, MessageType.GET_CONFIGS_BY_POOL, configs);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toParams(Object payload) {
        if (payload instanceof Map) {
            return (Map<String, Object>) payload;
        }
        return null;
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