package com.mtp.config.center.netty.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtp.config.center.netty.MessageContext;
import com.mtp.core.netty.MessageRequest;
import com.mtp.core.netty.MessageType;
import com.mtp.core.netty.MessageResponse;

import java.io.IOException;
import java.util.Map;

public class UnregisterHandler implements MessageHandler {
    private final ObjectMapper objectMapper;

    public UnregisterHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public MessageType getType() {
        return MessageType.UNREGISTER;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String handle(MessageRequest request, MessageContext context) throws IOException {
        Map<String, Object> payload = (Map<String, Object>) request.payload;
        if (payload == null) {
            return buildErrorResponse(request.correlationId, "Invalid payload");
        }

        String instanceId = (String) payload.get("instanceId");
        String poolName = (String) payload.get("poolName");

        if (instanceId == null || poolName == null) {
            return buildErrorResponse(request.correlationId, "instanceId and poolName are required");
        }

        context.getConfigCenterService().unregisterConfig(instanceId, poolName);
        return buildResponse(request.correlationId, MessageType.UNREGISTER, true);
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