package com.mtp.starter;

import com.mtp.core.api.DynamicThreadPoolManager;
import org.springframework.aop.TargetSource;
import org.springframework.context.ApplicationContext;

public class MtpExecutorTargetSource implements TargetSource {

    private final Class<?> type;

    private final String poolName;

    private final DynamicThreadPoolManager dynamicThreadPoolManager;

    private final ApplicationContext applicationContext;

    private volatile Object target;

    public MtpExecutorTargetSource(Class<?> type, String poolName, DynamicThreadPoolManager dynamicThreadPoolManager,
                                   ApplicationContext applicationContext) {
        this.type = type;
        this.poolName = poolName;
        this.dynamicThreadPoolManager = dynamicThreadPoolManager;
        this.applicationContext = applicationContext;
    }

    @Override
    public Class<?> getTargetClass() {
        return type;
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public Object getTarget() throws Exception {
        if (target == null) {
            synchronized (dynamicThreadPoolManager) {
                if (target == null) {
                    String beanName = MtpRegisterExecutorBeanPostProcessor.getBeanName(poolName);
                    if (beanName != null)
                        target = applicationContext.getBean(beanName);
                    else
                        target = dynamicThreadPoolManager.getExecutor(poolName);
                }
            }
        }
        return target;
    }

    @Override
    public void releaseTarget(Object target) throws Exception {

    }
}
