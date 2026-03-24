package com.mtp.core.api;

public interface MessageBusTopic {

    /**
     * 配置变更
     */
    String CONFIG_CHANGE = "config-change";

    /**
     * 重新注册
     */
    String RE_REGISTER = "re-register";

}
