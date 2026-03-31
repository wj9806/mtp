package com.mtp.starter;

import com.mtp.core.api.DynamicThreadPoolManager;
import com.mtp.core.mtp.MtpThreadPoolExecutorProxy;
import com.mtp.core.util.ExecutorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MtpRegisterExecutorBeanPostProcessor implements MergedBeanDefinitionPostProcessor,
        SmartInitializingSingleton, ApplicationContextAware {

    private final DynamicThreadPoolManager dynamicThreadPoolManager;

    private ApplicationContext applicationContext;

    /**
     * key: beanName
     * value: poolName
     */
    private static final Map<String, String> executorNames = new ConcurrentHashMap<>();

    /**
     * 根据poolName获取beanName
     */
    public static String getBeanName(String poolName) {
        for (Map.Entry<String, String> entry : executorNames.entrySet()) {
            if (entry.getValue().equals(poolName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public MtpRegisterExecutorBeanPostProcessor(DynamicThreadPoolManager dynamicThreadPoolManager) {
        this.dynamicThreadPoolManager = dynamicThreadPoolManager;
    }

    @Override
    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
        if (!ExecutorUtil.isExecutor(beanType)) return;

        if (beanDefinition instanceof AnnotatedBeanDefinition) {

            AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) beanDefinition;
            AnnotationMetadata metadata = annotatedBeanDefinition.getMetadata();
            MergedAnnotations annotations = metadata.getAnnotations();

            if (annotations.isPresent(Mtp.class)) {
                String value = annotations.get(Mtp.class).getString("value");
                executorNames.put(annotatedBeanDefinition.getFactoryMethodName(), value);
            } else {
                MethodMetadata methodMetadata = annotatedBeanDefinition.getFactoryMethodMetadata();
                if (methodMetadata != null && methodMetadata.isAnnotated(Mtp.class.getName())) {
                    String value = methodMetadata.getAnnotations().get(Mtp.class).getString("value");
                    executorNames.put(annotatedBeanDefinition.getFactoryMethodName(), value);
                }
            }
        } else {
            Mtp mtp = AnnotationUtils.findAnnotation(beanType, Mtp.class);
            if (mtp != null)
                executorNames.put(beanName, mtp.value());
        }
    }

    @Override
    public void afterSingletonsInstantiated() {
        executorNames.forEach((beanName, poolName) -> {
            Object bean = applicationContext.getBean(beanName);
            if (bean instanceof ThreadPoolExecutor) {
                dynamicThreadPoolManager.registerPool(poolName, (ThreadPoolExecutor) bean);
            } else if (bean instanceof ThreadPoolTaskExecutor) {
                ThreadPoolTaskExecutor taskExecutor = ((ThreadPoolTaskExecutor) bean);
                ThreadPoolExecutor threadPoolExecutor = taskExecutor.getThreadPoolExecutor();
                MtpThreadPoolExecutorProxy proxy = new MtpThreadPoolExecutorProxy(threadPoolExecutor);

                //反射设置ThreadPoolTaskExecutor内部线程池
                setFieldValue(taskExecutor, "threadPoolExecutor", proxy);

                dynamicThreadPoolManager.registerPool(poolName, proxy);
                ExecutorUtil.gracefulShutdown(threadPoolExecutor, 10, TimeUnit.SECONDS);
            }
        });
        
    }

    private void setFieldValue(ThreadPoolTaskExecutor taskExecutor, String threadPoolExecutor, MtpThreadPoolExecutorProxy proxy) {
        try {
            Class<?> clazz = taskExecutor.getClass();
            java.lang.reflect.Field field = clazz.getDeclaredField(threadPoolExecutor);
            field.setAccessible(true);
            field.set(taskExecutor, proxy);
        } catch (Exception e) {
            log.error("setFieldValue error", e);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
