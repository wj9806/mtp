package com.mtp.starter;

import java.lang.annotation.*;

/**
 * 线程池注解
 * 1. 注册线程池
 * 2. 获取线程池
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Mtp {

    /**
     * 线程池名称
     */
    String value();

}
