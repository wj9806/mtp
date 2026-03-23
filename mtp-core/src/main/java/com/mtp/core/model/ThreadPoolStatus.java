package com.mtp.core.model;

import lombok.Data;
import java.io.Serializable;

/**
 * 线程池状态实体类，用于存储和上报线程池的运行时状态
 */
@Data
public class ThreadPoolStatus implements Serializable {
    private static final long serialVersionUID = 1L;

    private String poolName;
    private String applicationName;
    private String instanceId;
    private String ip;
    private Integer port;
    private Integer corePoolSize;
    private Integer maxPoolSize;
    private Integer activeCount;
    private Integer poolSize;
    private Long taskCount;
    private Long completedTaskCount;
    private Integer queueSize;
    private Long heartbeatTime;

    public ThreadPoolStatus() {
    }

    public ThreadPoolStatus(String poolName, String applicationName) {
        this.poolName = poolName;
        this.applicationName = applicationName;
        this.heartbeatTime = System.currentTimeMillis();
    }
}