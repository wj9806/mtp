package com.mtp.config.center.netty.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtp.config.center.netty.MessageContext;
import com.mtp.core.model.ApplicationInfo;
import com.mtp.core.netty.MessageRequest;
import com.mtp.core.netty.MessageType;
import com.mtp.core.netty.MessageResponse;

import java.io.IOException;
import java.util.List;

/**
 * 获取所有应用处理器，处理客户端查询所有应用的请求
 */
public class GetAllApplicationsHandler implements MessageHandler {
    private final ObjectMapper objectMapper;

    public GetAllApplicationsHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public MessageType getType() {
        return MessageType.GET_ALL_APPLICATIONS;
    }

    @Override
    public String handle(MessageRequest request, MessageContext context) throws IOException {
        List<ApplicationInfo> applications = context.getConfigCenterService().getAllApplicationsWithInstances();
        return buildResponse(request.correlationId, MessageType.GET_ALL_APPLICATIONS, applications);
    }

    private String buildResponse(String correlationId, MessageType type, Object result) throws IOException {
        MessageResponse response = new MessageResponse();
        response.correlationId = correlationId;
        response.type = type.getType();
        response.data = result != null ? objectMapper.writeValueAsString(result) : null;
        return objectMapper.writeValueAsString(response);
    }
}