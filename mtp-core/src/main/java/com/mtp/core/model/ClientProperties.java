package com.mtp.core.model;

import lombok.Data;

@Data
public class ClientProperties {

    // 心跳间隔秒数
    private int heartbeatInterval = 30;

}
