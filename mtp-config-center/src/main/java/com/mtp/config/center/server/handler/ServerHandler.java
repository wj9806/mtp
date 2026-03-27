package com.mtp.config.center.server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtp.config.center.server.MessageContext;
import com.mtp.config.center.server.MessageHandlerRegistry;
import com.mtp.config.center.server.interceptor.MessageInterceptor;
import com.mtp.core.api.MessageBus;
import com.mtp.core.api.MessageBusTopic;
import com.mtp.core.api.MessageListener;
import com.mtp.core.model.Message;
import com.mtp.core.netty.MessageRequest;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Netty服务端处理器，负责处理客户端连接和消息
 */
@Slf4j
public class ServerHandler extends ChannelInboundHandlerAdapter {

    private final MessageHandlerRegistry registry;
    private final MessageContext context;
    private final ObjectMapper objectMapper;
    private final List<MessageInterceptor> messageInterceptors;

    public ServerHandler(MessageHandlerRegistry registry, MessageContext context,
                         ObjectMapper objectMapper, ObjectProvider<MessageInterceptor> messageInterceptors) {
        this.registry = registry;
        this.context = context;
        this.objectMapper = objectMapper;
        this.messageInterceptors = messageInterceptors.orderedStream().collect(Collectors.toList());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        MessageBus.bus.publish(MessageBusTopic.CLIENT_CHANNEL_INACTIVE, new Message<>(ctx));
        context.getMtpServer().unregisterInstance(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            String message = (String) msg;
            log.debug("Received message from client: {}", message);

            if (message.startsWith("{")) {
                MessageRequest request = objectMapper.readValue(message, MessageRequest.class);

                if (messageInterceptors != null) {
                    for (MessageInterceptor interceptor : messageInterceptors) {
                        if (!interceptor.preHandle(request, ctx)) {
                            log.warn("{} Message intercepted: {}", ctx.channel().remoteAddress(), request.getType());
                            return;
                        }
                    }
                }
                String response = registry.route(ctx, request, context);
                if (response != null) {
                    ctx.writeAndFlush(response + "\n");
                }
            }
        } catch (Exception e) {
            log.error("Error processing client message", e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof java.net.SocketException && "Connection reset".equals(cause.getMessage())) {
            log.warn("Client connection reset");
        } else {
            log.error("Exception in server handler", cause);
        }
        ctx.close();
    }
}