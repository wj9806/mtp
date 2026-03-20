package com.mtp.core.client;

import com.mtp.core.api.MessageBus;
import com.mtp.core.api.MessageListener;
import com.mtp.core.model.Message;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 消息总线实现类，基于主题发布-订阅模式
 * 使用ConcurrentHashMap存储主题与监听器的映射关系
 * 监听器列表使用CopyOnWriteArrayList保证并发安全
 */
public class MessageBusImpl implements MessageBus {

    private final Map<String, List<MessageListener<?>>> subscribers = new ConcurrentHashMap<>();

    @Override
    public void publish(String topic, String content) {
        publish(topic, new Message<>(content));
    }

    @Override
    public void publish(String topic, Message<?> message) {
        if (topic == null || topic.isEmpty()) {
            throw new IllegalArgumentException("Topic cannot be null or empty");
        }
        List<MessageListener<?>> listeners = subscribers.get(topic);
        if (listeners != null) {
            for (MessageListener<?> listener : listeners) {
                listener.onMessage((Message) message);
            }
        }
    }

    @Override
    public void subscribe(String topic, MessageListener<?> listener) {
        if (topic == null || topic.isEmpty()) {
            throw new IllegalArgumentException("Topic cannot be null or empty");
        }
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        subscribers.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    @Override
    public void unsubscribe(String topic, MessageListener<?> listener) {
        if (topic == null || topic.isEmpty() || listener == null) {
            return;
        }
        List<MessageListener<?>> listeners = subscribers.get(topic);
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                subscribers.remove(topic);
            }
        }
    }

    @Override
    public void unsubscribeAll(String topic) {
        if (topic == null || topic.isEmpty()) {
            return;
        }
        subscribers.remove(topic);
    }

}