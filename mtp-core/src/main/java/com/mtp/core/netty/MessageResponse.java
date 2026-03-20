package com.mtp.core.netty;

import com.mtp.core.model.ThreadPoolConfig;
import lombok.Data;

import java.util.List;

/**
 * 消息响应结构，服务端向客户端返回响应时使用
 */
@Data
public class MessageResponse {
    public String correlationId;
    public String type;
    public String error;
    public String data;
    public String applicationName;
    public String poolName;
    public List<ThreadPoolConfig> configs;
}