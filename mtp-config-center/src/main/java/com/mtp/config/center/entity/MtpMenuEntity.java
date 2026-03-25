package com.mtp.config.center.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("mtp_menu")
public class MtpMenuEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("menu_name")
    private String menuName;

    @TableField("menu_code")
    private String menuCode;

    @TableField("parent_id")
    private Long parentId;

    @TableField("path")
    private String path;

    @TableField("order_num")
    private Integer orderNum;

    @TableField("status")
    private String status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime  updateTime;
}