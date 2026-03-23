package com.mtp.config.center.model;

import io.netty.channel.Channel;
import lombok.Data;

/**
 * 客户端实例信息
 */
@Data
public class ClientInstance {

    private Channel channel;

    private String instanceId;

    private String ip;

    private Integer port;

    private String applicationName;

}
