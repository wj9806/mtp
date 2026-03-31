package com.mtp.starter;

import com.mtp.core.api.DynamicThreadPoolManager;
import com.mtp.core.util.ExecutorUtil;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MtpExecutorBeanPostProcessor implements InstantiationAwareBeanPostProcessor, BeanClassLoaderAware, ApplicationContextAware {

    private final Map<String, Object> mtpBeans = new ConcurrentHashMap<>();

    private final DynamicThreadPoolManager dynamicThreadPoolManager;

    private ClassLoader classLoader;

    private ApplicationContext applicationContext;

    public MtpExecutorBeanPostProcessor(DynamicThreadPoolManager dynamicThreadPoolManager) {
        this.dynamicThreadPoolManager = dynamicThreadPoolManager;
    }

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName)
            throws BeansException {
        // 处理字段注入
        ReflectionUtils.doWithFields(bean.getClass(), field -> {
            Mtp mtp = field.getAnnotation(Mtp.class);
            if (mtp != null &&
                    (ExecutorUtil.isExecutor(field.getType()))) {
                injectFieldValue(bean, field, mtp);
            }
        });

        // 处理方法注入
        ReflectionUtils.doWithMethods(bean.getClass(), method -> {
            Mtp annotation = method.getAnnotation(Mtp.class);
            if (annotation != null && method.getParameterCount() == 1) {
                injectMethodParameter(bean, method, annotation);
            }
        });

        return true;
    }

    private void injectMethodParameter(Object bean, Method method, Mtp mtp) {
        Class<?> paramType = method.getParameterTypes()[0];
        if (!ExecutorUtil.isExecutor(paramType)) return;

        try {
            Object value = getExecutorProxy(paramType, mtp);

            if (paramType.isAssignableFrom(value.getClass())) {
                method.setAccessible(true);
                method.invoke(bean, value);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject method: " + method.getName(), e);
        }
    }

    private void injectFieldValue(Object bean, Field field, Mtp mtp) {
        try {
            // 获取属性值
            Object value = mtpBeans.get(mtp.value());
            if (value != null) {
                setValue(bean, field, value);
                return;
            }

            value = getExecutorProxy(field.getType(), mtp);
            mtpBeans.put(mtp.value(), value);
            // 设置字段可访问
            setValue(bean, field, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to inject field: " + field.getName(), e);
        }
    }

    private Object getExecutorProxy(Class<?> type, Mtp mtp) {
        TargetSource targetSource = new MtpExecutorTargetSource(type, mtp.value(), dynamicThreadPoolManager, applicationContext);
        ProxyFactory pf = new ProxyFactory();
        pf.setTargetSource(targetSource);
        if (type.isInterface()) {
            pf.addInterface(type);
        }
        return pf.getProxy(classLoader);
    }

    private static void setValue(Object bean, Field field, Object value) throws IllegalAccessException {
        field.setAccessible(true);
        field.set(bean, value);
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
