package com.mtp.starter;

import com.mtp.core.api.ConfigCenterClient;
import com.mtp.core.api.DynamicThreadPoolManager;
import com.mtp.core.client.DynamicThreadPoolManagerImpl;
import com.mtp.core.client.NettyConfigCenterClient;
import com.mtp.core.netty.NettyClient;
import com.mtp.core.util.NetworkUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MTP自动配置类，用于Spring Boot自动装配
 * 当缺少相关Bean时自动创建NettyClient、ConfigCenterClient和DynamicThreadPoolManager
 */
@Configuration
@EnableConfigurationProperties(MtpProperties.class)
public class MtpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public NettyClient nettyClient(MtpProperties properties) {
        return new NettyClient(properties.getNettyServerHost(), properties.getNettyServerPort());
    }

    @Bean
    @ConditionalOnMissingBean
    public ConfigCenterClient configCenterClient(NettyClient nettyClient) {
        return new NettyConfigCenterClient(nettyClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicThreadPoolManager dynamicThreadPoolManager(
            ConfigCenterClient configCenterClient,
            NettyClient nettyClient,
            MtpProperties properties,
            ServerProperties serverProperties) {
        return new DynamicThreadPoolManagerImpl(
            configCenterClient,
            nettyClient,
            properties.getApplicationName(),
            NetworkUtil.getLocalIp(),
            serverProperties.getPort(),
            properties.getStatusReportInterval()
        );
    }
}