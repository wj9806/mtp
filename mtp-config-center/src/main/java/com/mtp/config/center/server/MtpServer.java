package com.mtp.config.center.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtp.config.center.config.MtpProperties;
import com.mtp.config.center.model.ClientInstance;
import com.mtp.config.center.server.handler.*;
import com.mtp.config.center.server.interceptor.MessageInterceptor;
import com.mtp.config.center.service.ConfigCenterService;
import com.mtp.core.model.ThreadPoolConfig;
import com.mtp.core.netty.MessageResponse;
import com.mtp.core.netty.MessageType;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Netty服务端，负责接收客户端连接并处理消息
 * 使用Spring Component注解自动注入，服务器端口为9090
 */
@Slf4j
@Component
public class MtpServer implements DisposableBean {

    private static final int PORT = 9090;

    private final MtpProperties mtpProperties;
    private final ConfigCenterService configCenterService;
    private final ObjectMapper objectMapper;
    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private final List<ClientInstance> allChannels = new CopyOnWriteArrayList<>();
    private final MessageHandlerRegistry registry;
    private final MessageContext messageContext;
    private final ObjectProvider<MessageInterceptor> messageInterceptors;

    public MtpServer(ConfigCenterService configCenterService, MtpProperties mtpProperties,
                     ObjectProvider<MessageInterceptor> messageInterceptors) {
        this.configCenterService = configCenterService;
        this.objectMapper = new ObjectMapper();
        this.registry = new MessageHandlerRegistry(this.objectMapper);
        this.messageContext = new MessageContext(configCenterService, this);
        this.mtpProperties = mtpProperties;
        this.registerHandlers();
        this.messageInterceptors = messageInterceptors;
    }

    private void registerHandlers() {
        registry.register(new RegisterHandler(objectMapper))
            .register(new ReRegisterHandler(objectMapper))
            .register(new UnregisterHandler(objectMapper))
            .register(new UpdateConfigHandler(objectMapper))
            .register(new UpdateBatchHandler(objectMapper))
            .register(new ReportStatusHandler(objectMapper))
            .register(new ReportStatusesHandler(objectMapper))
            .register(new GetConfigsByPoolHandler(objectMapper))
            .register(new GetClientServerConfigHandler(objectMapper, mtpProperties));
    }

    @PostConstruct
    public void start() {
        new Thread(() -> {
            String tcpPort = mtpProperties.getServer().getTcpPort();
            int port = StringUtils.hasLength(tcpPort) ? Integer.parseInt(tcpPort) : PORT;
            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<io.netty.channel.socket.SocketChannel>() {
                        @Override
                        protected void initChannel(io.netty.channel.socket.SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                            pipeline.addLast(new LengthFieldPrepender(4));
                            pipeline.addLast(new StringDecoder(StandardCharsets.UTF_8));
                            pipeline.addLast(new StringEncoder(StandardCharsets.UTF_8));
                            pipeline.addLast(new ServerHandler(registry, messageContext, objectMapper, messageInterceptors));
                        }
                    });
                ChannelFuture future = bootstrap.bind(port).sync();
                log.info("mtp server started on port {}", port);
                future.channel().closeFuture().sync();
            } catch (Exception e) {
                log.error("Failed to start mtp server on port {}", port, e);
            }
        }, "mtp-server").start();
    }

    public void broadcastConfigChange(String applicationName, String poolName, List<ThreadPoolConfig> configs) {
        MessageResponse message = new MessageResponse();
        message.type = MessageType.CONFIG_CHANGE.getType();
        message.applicationName = applicationName;
        message.poolName = poolName;
        message.configs = configs;

        try {
            String json = objectMapper.writeValueAsString(message);
            for (ClientInstance clientInstance : findClientInstanceByAppName(applicationName)) {
                Channel channel = clientInstance.getChannel();
                if (channel.isActive()) {
                    channel.writeAndFlush(json + "\n");
                }
            }
            log.info("Broadcasted config change for {}/{} to {} clients", applicationName, poolName, allChannels.size());
        } catch (Exception e) {
            log.error("Failed to broadcast config change", e);
        }
    }

    public void broadcastConfigChangeById(String instanceId, String poolName, ThreadPoolConfig config) {
        MessageResponse message = new MessageResponse();
        message.type = MessageType.CONFIG_CHANGE.getType();
        message.instanceId = instanceId;
        message.applicationName = config.getApplicationName();
        message.poolName = poolName;
        message.configs = Collections.singletonList(config);

        try {
            String json = objectMapper.writeValueAsString(message);
            Channel targetChannel = findClientInstanceById(instanceId).getChannel();
            if (targetChannel != null && targetChannel.isActive()) {
                targetChannel.writeAndFlush(json + "\n");
                log.info("Sent config change to instance {} for pool {}", instanceId, poolName);
            } else {
                log.warn("Instance {} not found or inactive", instanceId);
            }
        } catch (Exception e) {
            log.error("Failed to send config change by id", e);
        }
    }

    public void notifyConfigChange(String applicationName, String poolName) {
        try {
            List<ThreadPoolConfig> configs = configCenterService.getConfigsByPoolName(applicationName, poolName);
            broadcastConfigChange(applicationName, poolName, configs);
        } catch (Exception e) {
            log.error("Failed to notify config change", e);
        }
    }

    /**
     * 注册实例
     */
    public void registerInstance(Channel channel, Map<String, Object> payload) {
        String instanceId = (String) payload.get("instanceId");
        if (findClientInstanceById(instanceId) == null) {
            ClientInstance clientInstance = new ClientInstance();
            clientInstance.setChannel(channel);
            clientInstance.setInstanceId(instanceId);
            clientInstance.setIp((String) payload.get("ip"));
            clientInstance.setPort((Integer) payload.get("port"));
            clientInstance.setApplicationName((String) payload.get("applicationName"));
            allChannels.add(clientInstance);
            log.info("Instance registered: {}", instanceId);
        }
    }

    public List<ClientInstance> findClientInstanceByAppName(String applicationName) {
        return allChannels.stream()
            .filter(c -> c.getApplicationName().equals(applicationName))
            .collect(Collectors.toList());
    }

    private ClientInstance findClientInstanceById(String instanceId) {
        return allChannels.stream()
            .filter(c -> c.getInstanceId().equals(instanceId))
            .findFirst()
            .orElse(null);
    }

    public void unregisterInstance(Channel channel) {
        if (allChannels.removeIf(c -> c.getChannel().equals(channel)))
            log.info("Instance unregistered: {}", channel);
    }

    public void requestClientStatus(String instanceId, String poolName) {
        ClientInstance client = findClientInstanceById(instanceId);
        if (client != null && client.getChannel().isActive()) {
            MessageResponse message = new MessageResponse();
            message.type = MessageType.GET_ALL_STATUSES.getType();
            message.instanceId = instanceId;
            message.poolName = poolName;
            try {
                String json = objectMapper.writeValueAsString(message);
                client.getChannel().writeAndFlush(json + "\n");
                log.debug("Requested status from instance {}", instanceId);
            } catch (Exception e) {
                log.error("Failed to request status from instance {}", instanceId, e);
            }
        } else {
            log.warn("Instance {} not found or inactive", instanceId);
        }
    }

    @Override
    public void destroy() throws Exception {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        log.info("mtp server stopped");
    }
}