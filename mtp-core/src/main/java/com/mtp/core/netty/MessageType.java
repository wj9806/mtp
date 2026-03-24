package com.mtp.core.netty;

/**
 * 消息类型枚举，定义客户端与服务端之间的通信消息类型
 */
public enum MessageType {
    REGISTER("REGISTER"),
    RE_REGISTER("RE_REGISTER"),
    UNREGISTER("UNREGISTER"),
    UPDATE_CONFIG("UPDATE_CONFIG"),
    UPDATE_CONFIGS_BY_APP_AND_POOL("UPDATE_CONFIGS_BY_APP_AND_POOL"),
    GET_CONFIG("GET_CONFIG"),
    GET_ALL_CONFIGS("GET_ALL_CONFIGS"),
    GET_CONFIGS_BY_POOL("GET_CONFIGS_BY_POOL"),
    REPORT_STATUS("REPORT_STATUS"),
    GET_ALL_STATUSES("GET_ALL_STATUSES"),
    CONFIG_CHANGE("CONFIG_CHANGE"),
    GET_CLIENT_SERVER_CONFIG("GET_CLIENT_SERVER_CONFIG"),
    REFRESH_STATUS("REFRESH_STATUS");

    private final String type;

    MessageType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static MessageType fromString(String type) {
        for (MessageType mt : values()) {
            if (mt.type.equals(type)) {
                return mt;
            }
        }
        return null;
    }
}