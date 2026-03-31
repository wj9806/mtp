package com.mtp.example.config;

import com.mtp.core.api.DynamicThreadPoolManager;
import com.mtp.core.model.ThreadPoolConfig;
import com.mtp.starter.Mtp;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池自动配置类，用于初始化示例应用的线程池
 */
@Configuration
@ConditionalOnProperty(prefix = "mtp", name = "enabled", havingValue = "true")
public class ThreadPoolAutoConfig {

    @Bean
    @Mtp("test-spring-pool")
    public ThreadPoolTaskExecutor testSpringPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean
    @Mtp("test-pool")
    public ThreadPoolExecutor threadPoolExecutor() {
        return new ThreadPoolExecutor(10, 20, 60L,
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(100),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

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
            businessConfig.setKeepAliveSeconds(60L);
            businessConfig.setRejectedPolicy("abort");
            threadPoolManager.registerPool("business-pool", businessConfig);

            ThreadPoolConfig asyncConfig = new ThreadPoolConfig();
            asyncConfig.setPoolName("async-pool");
            asyncConfig.setCorePoolSize(5);
            asyncConfig.setMaxPoolSize(10);
            asyncConfig.setQueueCapacity(50);
            asyncConfig.setKeepAliveSeconds(30L);
            asyncConfig.setRejectedPolicy("caller-runs");
            threadPoolManager.registerPool("async-pool", asyncConfig);
        }
    }
}