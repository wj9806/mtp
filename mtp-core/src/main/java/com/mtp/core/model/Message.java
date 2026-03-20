package com.mtp.core.model;

import lombok.Data;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息对象，用于消息总线传输
 * @param <T> 消息内容的类型
 */
@Data
public class Message<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private T content;
    private Map<String, Object> headers;
    private long timestamp;

    public Message() {
        this.timestamp = System.currentTimeMillis();
        this.headers = new HashMap<>();
    }

    public Message(T content) {
        this();
        this.content = content;
    }

    public Message(T content, Map<String, Object> headers) {
        this(content);
        this.headers = headers != null ? headers : new HashMap<>();
    }

    public void putHeader(String key, Object value) {
        this.headers.put(key, value);
    }

    public Object getHeader(String key) {
        return this.headers.get(key);
    }

}