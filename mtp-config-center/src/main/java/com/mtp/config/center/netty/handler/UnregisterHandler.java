package com.mtp.config.center.netty.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtp.config.center.netty.MessageContext;
import com.mtp.core.netty.MessageRequest;
import com.mtp.core.netty.MessageType;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.util.Map;

public class UnregisterHandler extends AbstractMessageHandler {

    public UnregisterHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public MessageType getType() {
        return MessageType.UNREGISTER;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String handle(ChannelHandlerContext ctx, MessageRequest request, MessageContext context) throws IOException {
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

}