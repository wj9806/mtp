package com.mtp.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置实体类，存储线程池的各种配置参数
 */
@Data
public class ThreadPoolConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private String poolName;
    private Integer corePoolSize;
    private Integer maxPoolSize;
    private Integer queueCapacity;
    private Integer keepAliveSeconds;
    private String rejectedPolicy;
    private String applicationName;
    private Long registerTime;
    private String instanceId;
    private String ip;
    private Integer port;

    public ThreadPoolConfig() {
    }

    public ThreadPoolConfig(String poolName, Integer corePoolSize, Integer maxPoolSize,
                           Integer queueCapacity, Integer keepAliveSeconds, String rejectedPolicy) {
        this.poolName = poolName;
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.queueCapacity = queueCapacity;
        this.keepAliveSeconds = keepAliveSeconds;
        this.rejectedPolicy = rejectedPolicy;
    }

    @JsonIgnore
    public RejectedExecutionHandler getRejectedExecutionHandler() {
        if (rejectedPolicy == null) {
            return new ThreadPoolExecutor.AbortPolicy();
        }
        switch (rejectedPolicy.toLowerCase()) {
            case "abort":
                return new ThreadPoolExecutor.AbortPolicy();
            case "discard":
                return new ThreadPoolExecutor.DiscardPolicy();
            case "discard-oldest":
                return new ThreadPoolExecutor.DiscardOldestPolicy();
            case "caller-runs":
            default:
                return new ThreadPoolExecutor.CallerRunsPolicy();
        }
    }
}