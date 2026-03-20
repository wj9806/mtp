package com.mtp.core.api;

import com.mtp.core.model.Message;

/**
 * 消息监听器接口，用于订阅消息总线上的消息
 * @param <T> 消息内容的类型
 */
public interface MessageListener<T> {

    /**
     * 收到消息时的回调方法
     * @param message 收到的消息对象
     */
    void onMessage(Message<T> message);
}