package com.mtp.config.center.server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtp.config.center.config.MtpProperties;
import com.mtp.config.center.server.MessageContext;
import com.mtp.core.netty.MessageRequest;
import com.mtp.core.netty.MessageType;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;

import static com.mtp.core.netty.MessageType.GET_CLIENT_SERVER_CONFIG;

public class GetClientServerConfigHandler extends AbstractMessageHandler {

    private final MtpProperties mtpProperties;

    public GetClientServerConfigHandler(ObjectMapper objectMapper, MtpProperties mtpProperties) {
        super(objectMapper);
        this.mtpProperties = mtpProperties;
    }

    @Override
    public MessageType getType() {
        return GET_CLIENT_SERVER_CONFIG;
    }

    @Override
    public String handle(ChannelHandlerContext ctx, MessageRequest request, MessageContext context) throws IOException {
        return buildResponse(request.correlationId, MessageType.GET_CONFIGS_BY_POOL, mtpProperties.getClient());
    }
}
