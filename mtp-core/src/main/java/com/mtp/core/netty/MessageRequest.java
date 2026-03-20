package com.mtp.core.netty;

import lombok.Data;

/**
 * 消息请求结构，客户端向服务端发送请求时使用
 */
@Data
public class MessageRequest {
    public String correlationId;
    public String type;
    public Object payload;
}