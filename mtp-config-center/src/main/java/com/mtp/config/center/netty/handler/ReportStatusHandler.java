package com.mtp.config.center.netty.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtp.config.center.netty.MessageContext;
import com.mtp.core.model.ThreadPoolStatus;
import com.mtp.core.netty.MessageRequest;
import com.mtp.core.netty.MessageType;
import com.mtp.core.netty.MessageResponse;

import java.io.IOException;
import java.util.Map;

/**
 * 上报状态处理器，处理客户端上报线程池状态的请求
 */
public class ReportStatusHandler implements MessageHandler {
    private final ObjectMapper objectMapper;

    public ReportStatusHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public MessageType getType() {
        return MessageType.REPORT_STATUS;
    }

    @Override
    public String handle(MessageRequest request, MessageContext context) throws IOException {
        ThreadPoolStatus status = parsePayload(request.payload, ThreadPoolStatus.class);
        if (status != null) {
            context.getConfigCenterService().reportStatus(status);
            return buildResponse(request.correlationId, MessageType.REPORT_STATUS, true);
        }
        return buildErrorResponse(request.correlationId, "Invalid status payload");
    }

    private ThreadPoolStatus parsePayload(Object payload, Class<ThreadPoolStatus> clazz) {
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