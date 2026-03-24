package com.mtp.config.center.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("thread_pool_config")
public class ThreadPoolConfigEntity {

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField("instance_id")
    private String instanceId;

    @TableField("pool_name")
    private String poolName;

    @TableField("application_name")
    private String applicationName;

    @TableField("core_pool_size")
    private Integer corePoolSize;

    @TableField("max_pool_size")
    private Integer maxPoolSize;

    @TableField("queue_capacity")
    private Integer queueCapacity;

    @TableField("keep_alive_seconds")
    private Integer keepAliveSeconds;

    @TableField("rejected_policy")
    private String rejectedPolicy;

    @TableField("ip")
    private String ip;

    @TableField("port")
    private Integer port;

    @TableField("register_time")
    private Long registerTime;

    @TableField("update_time")
    private Long updateTime;
}