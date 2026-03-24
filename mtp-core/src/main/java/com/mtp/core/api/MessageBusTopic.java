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

    /**
     * 获取线程池当前状态
     */
    String GET_THREAD_POOL_STATUS = "get-thread-pool-status";
}
