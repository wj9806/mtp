package com.mtp.config.center.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("application_registry")
public class ApplicationRegistryEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("application_name")
    private String applicationName;

    @TableField("application_info")
    private String applicationInfo;

    @TableField("access_token")
    private String accessToken;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}