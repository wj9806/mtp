package com.mtp.config.center.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("client_registry")
public class ClientRegistryEntity {

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField("instance_id")
    private String instanceId;

    @TableField("ip")
    private String ip;

    @TableField("port")
    private Integer port;

    @TableField("application_name")
    private String applicationName;

    @TableField("status")
    private String status;

    @TableField("create_time")
    private Long createTime;

    @TableField("report_time")
    private Long reportTime;
}