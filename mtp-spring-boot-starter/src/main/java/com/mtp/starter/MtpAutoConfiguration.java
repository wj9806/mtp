package com.mtp.starter;

import com.mtp.core.api.DynamicThreadPoolManager;
import com.mtp.core.client.DynamicThreadPoolManagerImpl;
import com.mtp.core.util.NetworkUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MTP自动配置类，用于Spring Boot自动装配
 */
@Configuration
@EnableConfigurationProperties(MtpProperties.class)
public class MtpAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "mtp", name = "enabled", havingValue = "true")
    public DynamicThreadPoolManager dynamicThreadPoolManager(
            MtpProperties properties,
            ServerProperties serverProperties) {
        return new DynamicThreadPoolManagerImpl(
            properties.getApplicationName(),
            properties.getAccessToken(),
            properties.getMtpServerHost(),
            properties.getMtpServerPort(),
            NetworkUtil.getLocalIp(),
            serverProperties.getPort()
        );
    }
}