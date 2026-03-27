package com.mtp.example2;

import com.mtp.core.api.DynamicThreadPoolManager;
import com.mtp.core.client.DynamicThreadPoolManagerImpl;
import com.mtp.core.model.ThreadPoolConfig;
import com.mtp.core.util.NetworkUtil;

import java.util.concurrent.Executor;

public class MtpExample2Application {

    public static void main(String[] args) {
        DynamicThreadPoolManager threadPoolManager = new DynamicThreadPoolManagerImpl("mtp-example2",
                "", "localhost", 9090,
                NetworkUtil.getLocalIp(), -1);

        ThreadPoolConfig businessConfig = new ThreadPoolConfig();
        businessConfig.setPoolName("business-pool");
        businessConfig.setCorePoolSize(10);
        businessConfig.setMaxPoolSize(20);
        businessConfig.setQueueCapacity(100);
        businessConfig.setKeepAliveSeconds(60);
        businessConfig.setRejectedPolicy("abort");
        threadPoolManager.registerPool("business-pool", businessConfig);

        Executor executor = threadPoolManager.getExecutor("business-pool");

        executor.execute(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("hello world");
            }
        });
    }
}
