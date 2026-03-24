package com.mtp.config.center.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("thread_pool_status")
public class ThreadPoolStatusEntity {

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField("instance_id")
    private String instanceId;

    @TableField("pool_name")
    private String poolName;

    @TableField("application_name")
    private String applicationName;

    @TableField("ip")
    private String ip;

    @TableField("port")
    private Integer port;

    @TableField("core_pool_size")
    private Integer corePoolSize;

    @TableField("max_pool_size")
    private Integer maxPoolSize;

    @TableField("active_count")
    private Integer activeCount;

    @TableField("pool_size")
    private Integer poolSize;

    @TableField("task_count")
    private Long taskCount;

    @TableField("completed_task_count")
    private Long completedTaskCount;

    @TableField("queue_size")
    private Integer queueSize;

    @TableField("queue_capacity")
    private Integer queueCapacity;

    @TableField("update_time")
    private Long updateTime;
}