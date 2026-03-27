package com.mtp.starter;

import com.mtp.core.api.DynamicThreadPoolManager;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class MtpBeanPostProcessor implements BeanPostProcessor {

    private final DynamicThreadPoolManager dynamicThreadPoolManager;

    public MtpBeanPostProcessor(DynamicThreadPoolManager dynamicThreadPoolManager) {
        this.dynamicThreadPoolManager = dynamicThreadPoolManager;
    }

}
