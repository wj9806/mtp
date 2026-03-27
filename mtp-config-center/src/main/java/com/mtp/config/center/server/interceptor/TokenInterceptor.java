package com.mtp.config.center.server.interceptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtp.config.center.entity.ApplicationRegistryEntity;
import com.mtp.config.center.service.ApplicationRegistryService;
import com.mtp.core.api.MessageBus;
import com.mtp.core.api.MessageBusTopic;
import com.mtp.core.api.MessageListener;
import com.mtp.core.model.Message;
import com.mtp.core.netty.MessageRequest;
import com.mtp.core.netty.MessageResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class TokenInterceptor implements MessageInterceptor {

    @Autowired
    private ApplicationRegistryService applicationRegistryService;

    @Autowired
    private ObjectMapper objectMapper;
    
    private static final List<Channel> VALID_CHANNELS = new CopyOnWriteArrayList<>();

    public TokenInterceptor() {
        MessageBus.bus.subscribe(MessageBusTopic.CLIENT_CHANNEL_INACTIVE, new MessageListener<ChannelHandlerContext>() {
            @Override
            public void onMessage(Message<ChannelHandlerContext> message) {
                VALID_CHANNELS.remove(message.getContent().channel());
            }
        });
    }

    @Override
    public boolean preHandle(MessageRequest request, ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        if (VALID_CHANNELS.contains(channel)) {
            return true;
        }

        Map<String, String> headers = request.getHeaders();
        if (headers != null && headers.containsKey("accessToken") && headers.containsKey("applicationName")) {
            String accessToken = headers.get("accessToken");
            String applicationName = headers.get("applicationName");

            ApplicationRegistryEntity applicationRegistry = applicationRegistryService.findByName(applicationName);
            if (applicationRegistry != null && applicationRegistry.getAccessToken().equals(accessToken)) {
                VALID_CHANNELS.add(channel);
                return true;
            }
        }

        MessageResponse response = new MessageResponse();
        response.correlationId = request.correlationId;
        response.error = "token invalid OR token is null";

        try {
            ctx.writeAndFlush(objectMapper.writeValueAsString(response));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
