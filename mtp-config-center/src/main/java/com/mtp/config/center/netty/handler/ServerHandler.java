package com.mtp.config.center.netty.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtp.config.center.netty.MessageContext;
import com.mtp.config.center.netty.MessageHandlerRegistry;
import com.mtp.core.netty.MessageRequest;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;


/**
 * Netty服务端处理器，负责处理客户端连接和消息
 */
@Slf4j
public class ServerHandler extends ChannelInboundHandlerAdapter {

    private final MessageHandlerRegistry registry;
    private final MessageContext context;
    private final ObjectMapper objectMapper;

    public ServerHandler(MessageHandlerRegistry registry, MessageContext context,
                         ObjectMapper objectMapper) {
        this.registry = registry;
        this.context = context;
        this.objectMapper = objectMapper;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        //String clientId = ctx.channel().remoteAddress().toString();
        //context.getNettyServer().registerClient(clientId, ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        context.getNettyServer().removeClient(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            String message = (String) msg;
            log.debug("Received message from client: {}", message);

            if (message.startsWith("{")) {
                MessageRequest request = objectMapper.readValue(message, MessageRequest.class);

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