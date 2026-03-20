package com.mtp.core.api;

import com.mtp.core.client.MessageBusImpl;
import com.mtp.core.model.Message;

/**
 * 消息总线接口，提供消息的发布与订阅功能
 * 支持主题订阅模式，订阅者可以接收发布到特定主题的消息
 */
public interface MessageBus {

    MessageBus bus = new MessageBusImpl();

    /**
     * 发布纯文本消息到指定主题
     * @param topic 主题名称
     * @param content 消息内容
     */
    void publish(String topic, String content);

    /**
     * 发布消息对象到指定主题
     * @param topic 主题名称
     * @param message 消息对象，包含内容和元数据
     */
    void publish(String topic, Message<?> message);

    /**
     * 订阅指定主题的消息
     * @param topic 主题名称
     * @param listener 消息监听器，收到消息时触发onMessage回调
     */
    void subscribe(String topic, MessageListener<?> listener);

    /**
     * 取消订阅指定主题的某个监听器
     * @param topic 主题名称
     * @param listener 要移除的监听器
     */
    void unsubscribe(String topic, MessageListener<?> listener);

    /**
     * 取消订阅指定主题的所有监听器
     * @param topic 主题名称
     */
    void unsubscribeAll(String topic);

}