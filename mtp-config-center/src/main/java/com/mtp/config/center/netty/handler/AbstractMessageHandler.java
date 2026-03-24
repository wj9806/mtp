package com.mtp.config.center.netty.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtp.core.netty.MessageResponse;
import com.mtp.core.netty.MessageType;

import java.io.IOException;
import java.util.Map;

public abstract class AbstractMessageHandler implements MessageHandler {

    protected final ObjectMapper objectMapper;

    public AbstractMessageHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    protected String buildResponse(String correlationId, MessageType type, Object result) throws IOException {
        MessageResponse response = new MessageResponse();
        response.correlationId = correlationId;
        response.type = type.getType();
        response.data = result != null ? objectMapper.writeValueAsString(result) : null;
        return objectMapper.writeValueAsString(response);
    }

    protected String buildErrorResponse(String correlationId, String error) throws IOException {
        MessageResponse response = new MessageResponse();
        response.correlationId = correlationId;
        response.error = error;
        return objectMapper.writeValueAsString(response);
    }

    protected <T> T parsePayload(Object payload, Class<T> clazz) {
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
}
