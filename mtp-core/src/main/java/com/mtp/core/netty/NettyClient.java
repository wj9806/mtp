package com.mtp.core.netty;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtp.core.api.MessageBus;
import com.mtp.core.api.MessageBusTopic;
import com.mtp.core.model.Message;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Netty客户端，负责与配置中心服务端建立连接并通信
 * 支持自动重连机制，当连接断开时会自动尝试重连
 */
@Slf4j
public class NettyClient {

    private static final int RECONNECT_DELAY_SECONDS = 5;
    private static final int REQUEST_TIMEOUT_SECONDS = 30;

    private final String host;
    private final int port;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MessageBus messageBus = MessageBus.bus;
    private final Map<String, ClientHandler.PendingRequest> pendingRequests = new ConcurrentHashMap<>();

    private EventLoopGroup group;
    private Channel channel;
    private volatile boolean connected = false;
    private final AtomicBoolean reconnecting = new AtomicBoolean(false);
    private Bootstrap bootstrap;
    private volatile boolean stopped = false;

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private void notifyConfigChange(ConfigChangeEvent event) {
        messageBus.publish(MessageBusTopic.CONFIG_CHANGE, new Message<>(event));
    }

    public void start() {
        stopped = false;
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                    pipeline.addLast(new LengthFieldPrepender(4));
                    pipeline.addLast(new StringDecoder(StandardCharsets.UTF_8));
                    pipeline.addLast(new StringEncoder(StandardCharsets.UTF_8));
                    pipeline.addLast(new ClientHandler(objectMapper, pendingRequests, NettyClient.this::notifyConfigChange));
                }
            });

        doConnect();
    }

    private synchronized void doConnect() {
        if (stopped || connected) {
            return;
        }

        if (!reconnecting.compareAndSet(false, true)) {
            log.debug("Reconnection already in progress, skipping");
            return;
        }

        try {
            log.info("Attempting to connect to Netty server at {}:{}", host, port);
            ChannelFuture connectFuture = bootstrap.connect(host, port);
            connectFuture.addListener((ChannelFutureListener) f -> {
                try {
                    if (f.isSuccess()) {
                        channel = f.channel();
                        connected = true;
                        reconnecting.set(false);
                        log.info("Connected to Netty server at {}:{}", host, port);
                        channel.closeFuture().addListener((ChannelFutureListener) cf -> {
                            connected = false;
                            log.warn("Connection closed, will attempt to reconnect");
                            scheduleReconnect();
                        });
                    } else {
                        log.warn("Failed to connect to Netty server at {}:{}, will retry in {} seconds", host, port, RECONNECT_DELAY_SECONDS);
                        scheduleReconnect();
                    }
                } finally {
                    reconnecting.set(false);
                }
            });
        } catch (Exception e) {
            reconnecting.set(false);
            log.warn("Exception while connecting to Netty server at {}:{}, will retry in {} seconds", host, port, RECONNECT_DELAY_SECONDS);
            scheduleReconnect();
        }
    }

    private void scheduleReconnect() {
        if (stopped) {
            log.info("Client stopped, skipping reconnection");
            return;
        }
        group.schedule(() -> {
            if (!stopped && !connected) {
                doConnect();
            }
        }, RECONNECT_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    public synchronized void stop() {
        stopped = true;
        connected = false;
        reconnecting.set(false);
        pendingRequests.values().forEach(r -> r.responseFuture.completeExceptionally(new RuntimeException("Client stopped")));
        pendingRequests.clear();
        if (channel != null) {
            channel.close();
        }
        if (group != null) {
            group.shutdownGracefully();
        }
        log.info("Netty client stopped");
    }

    public void awaitConnect() {
        while (!connected) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for connection");
            }
        }
    }

    public String sendRequest(MessageType type, Object payload) throws Exception {
        return sendRequest(type, payload, new TypeReference<String>() {});
    }

    public <T> T sendRequest(MessageType type, Object payload, TypeReference<T> typeReference) throws Exception {
        if (channel == null || !channel.isActive()) {
            throw new RuntimeException("Not connected to server");
        }

        String correlationId = UUID.randomUUID().toString();
        MessageRequest request = new MessageRequest();
        request.correlationId = correlationId;
        request.type = type.getType();
        request.payload = payload;

        ClientHandler.PendingRequest pendingRequest = new ClientHandler.PendingRequest();
        pendingRequests.put(correlationId, pendingRequest);

        try {
            String json = objectMapper.writeValueAsString(request);
            channel.writeAndFlush(json + "\n");
            String responseJson = pendingRequest.responseFuture.get(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            // 根据 TypeReference 反序列化返回值
            return objectMapper.readValue(responseJson, typeReference);
        } finally {
            pendingRequests.remove(correlationId);
        }
    }

    public void sendNotification(MessageType type, Object payload) {
        if (channel == null || !channel.isActive()) {
            log.warn("Cannot send notification, not connected");
            return;
        }

        MessageRequest request = new MessageRequest();
        request.correlationId = UUID.randomUUID().toString();
        request.type = type.getType();
        request.payload = payload;

        try {
            String json = objectMapper.writeValueAsString(request);
            channel.writeAndFlush(json + "\n");
        } catch (Exception e) {
            log.error("Failed to send notification", e);
        }
    }
}