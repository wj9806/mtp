package com.mtp.config.center.netty.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtp.config.center.config.MtpProperties;
import com.mtp.config.center.netty.MessageContext;
import com.mtp.core.netty.MessageRequest;
import com.mtp.core.netty.MessageType;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;

import static com.mtp.core.netty.MessageType.GET_CLIENT_SERVER_CONFDIG;

public class GetClientServerConfigHandler extends AbstractMessageHandler {

    private final MtpProperties mtpProperties;

    public GetClientServerConfigHandler(ObjectMapper objectMapper, MtpProperties mtpProperties) {
        super(objectMapper);
        this.mtpProperties = mtpProperties;
    }

    @Override
    public MessageType getType() {
        return GET_CLIENT_SERVER_CONFDIG;
    }

    @Override
    public String handle(ChannelHandlerContext ctx, MessageRequest request, MessageContext context) throws IOException {
        return buildResponse(request.correlationId, MessageType.GET_CONFIGS_BY_POOL, mtpProperties.getClient());
    }
}
