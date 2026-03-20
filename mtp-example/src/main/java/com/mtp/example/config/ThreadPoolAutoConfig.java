package com.mtp.example.config;

import com.mtp.core.api.DynamicThreadPoolManager;
import com.mtp.core.model.ThreadPoolConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 线程池自动配置类，用于初始化示例应用的线程池
 */
@Configuration
public class ThreadPoolAutoConfig {

    @Bean
    public ThreadPoolInitializer threadPoolInitializer(DynamicThreadPoolManager threadPoolManager) {
        return new ThreadPoolInitializer(threadPoolManager);
    }

    /**
     * 线程池初始化器，在应用启动时自动注册线程池
     */
    public static class ThreadPoolInitializer {
        private final DynamicThreadPoolManager threadPoolManager;

        public ThreadPoolInitializer(DynamicThreadPoolManager threadPoolManager) {
            this.threadPoolManager = threadPoolManager;
            init();
        }

        private void init() {
            ThreadPoolConfig businessConfig = new ThreadPoolConfig();
            businessConfig.setPoolName("business-pool");
            businessConfig.setCorePoolSize(10);
            businessConfig.setMaxPoolSize(20);
            businessConfig.setQueueCapacity(100);
            businessConfig.setKeepAliveSeconds(60);
            businessConfig.setRejectedPolicy("abort");
            threadPoolManager.registerPool("business-pool", businessConfig);

            ThreadPoolConfig asyncConfig = new ThreadPoolConfig();
            asyncConfig.setPoolName("async-pool");
            asyncConfig.setCorePoolSize(5);
            asyncConfig.setMaxPoolSize(10);
            asyncConfig.setQueueCapacity(50);
            asyncConfig.setKeepAliveSeconds(30);
            asyncConfig.setRejectedPolicy("caller-runs");
            threadPoolManager.registerPool("async-pool", asyncConfig);
        }
    }
}