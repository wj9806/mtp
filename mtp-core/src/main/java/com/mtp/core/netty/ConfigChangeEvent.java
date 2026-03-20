package com.mtp.core.netty;

import com.mtp.core.model.ThreadPoolConfig;
import lombok.Data;

import java.util.List;

/**
 * 配置变更事件，当服务端推送配置变更时使用
 */
@Data
public class ConfigChangeEvent {

    private String applicationName;
    private String poolName;
    private List<ThreadPoolConfig> configs;

}