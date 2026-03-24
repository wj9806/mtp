package com.mtp.example2;

import com.mtp.core.api.DynamicThreadPoolManager;
import com.mtp.core.client.DynamicThreadPoolManagerImpl;
import com.mtp.core.client.NettyConfigCenterClient;
import com.mtp.core.model.ThreadPoolConfig;
import com.mtp.core.netty.NettyClient;
import com.mtp.core.util.NetworkUtil;

public class MtpExample2Application {

    public static void main(String[] args) {
        NettyClient nettyClient = new NettyClient("127.0.0.1", 9090);
        NettyConfigCenterClient centerClient = new NettyConfigCenterClient(nettyClient);
        DynamicThreadPoolManager threadPoolManager = new DynamicThreadPoolManagerImpl(centerClient, nettyClient,
                "mtp-example2", NetworkUtil.getLocalIp(), 12138);

        ThreadPoolConfig businessConfig = new ThreadPoolConfig();
        businessConfig.setPoolName("business-pool");
        businessConfig.setCorePoolSize(10);
        businessConfig.setMaxPoolSize(20);
        businessConfig.setQueueCapacity(100);
        businessConfig.setKeepAliveSeconds(60);
        businessConfig.setRejectedPolicy("abort");
        threadPoolManager.registerPool("business-pool", businessConfig);

    }
}
