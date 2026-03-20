package com.mtp.config.center.netty.handler;

import com.mtp.config.center.netty.MessageContext;
import com.mtp.core.netty.MessageRequest;
import com.mtp.core.netty.MessageType;

import java.io.IOException;

/**
 * 消息处理器接口，用于处理不同类型的消息
 */
public interface MessageHandler {
    MessageType getType();
    String handle(MessageRequest request, MessageContext context) throws IOException;
}