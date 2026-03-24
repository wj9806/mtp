package com.mtp.core.netty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtp.core.api.MessageBus;
import com.mtp.core.api.MessageBusTopic;
import com.mtp.core.model.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Netty客户端处理器，负责处理与服务端的网络通信
 * 处理消息响应、配置变更通知和连接状态管理
 */
@Slf4j
public class ClientHandler extends ChannelInboundHandlerAdapter {

    private final ObjectMapper objectMapper;
    private final Map<String, PendingRequest> pendingRequests;

    public ClientHandler(ObjectMapper objectMapper,
                         Map<String, PendingRequest> pendingRequests) {
        this.objectMapper = objectMapper;
        this.pendingRequests = pendingRequests;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("Connected to config center server");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        pendingRequests.values().forEach(r -> r.responseFuture.completeExceptionally(new RuntimeException("Connection lost")));
        pendingRequests.clear();
        log.warn("Disconnected from config center server, will attempt to reconnect");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            String message = (String) msg;
            log.debug("Received message from server: {}", message);

            if (message.startsWith("{")) {
                MessageResponse response = objectMapper.readValue(message, MessageResponse.class);

                if (response.correlationId != null && pendingRequests.containsKey(response.correlationId)) {
                    PendingRequest pendingRequest = pendingRequests.remove(response.correlationId);
                    if (response.error != null) {
                        pendingRequest.responseFuture.completeExceptionally(new RuntimeException(response.error));
                    } else {
                        pendingRequest.responseFuture.complete(response.data);
                    }
                } else if (MessageType.CONFIG_CHANGE.getType().equals(response.type)) {
                    ConfigChangeEvent event = new ConfigChangeEvent();
                    event.setApplicationName(response.applicationName);
                    event.setPoolName(response.poolName);
                    if (response.configs != null) {
                        event.setConfigs(response.configs);
                    }
                    MessageBus.bus.publish(MessageBusTopic.CONFIG_CHANGE, new Message<>(event));
                } else if (MessageType.GET_ALL_STATUSES.getType().equals(response.type)) {
                    MessageBus.bus.publish(MessageBusTopic.GET_THREAD_POOL_STATUS, new Message<>(response.poolName));
                }
            }
        } catch (Exception e) {
            log.error("Error processing server message", e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof java.net.SocketException && "Connection reset".equals(cause.getMessage())) {
            log.warn("Server connection reset");
        } else {
            log.error("Exception in client handler", cause);
        }
        ctx.close();
    }

    /**
     * 待处理请求，用于异步响应管理
     */
    public static class PendingRequest {
        public final CompletableFuture<String> responseFuture = new CompletableFuture<>();
    }

}