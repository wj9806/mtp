package com.mtp.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MTP配置属性类，用于从application.yml中读取mtp相关配置
 */
@ConfigurationProperties(prefix = "mtp")
public class MtpProperties {

    private String applicationName;
    private String configCenterUrl = "http://localhost:8080";
    private String nettyServerHost = "localhost";
    private int nettyServerPort = 9090;
    private boolean enabled = true;

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getConfigCenterUrl() {
        return configCenterUrl;
    }

    public void setConfigCenterUrl(String configCenterUrl) {
        this.configCenterUrl = configCenterUrl;
    }

    public String getNettyServerHost() {
        return nettyServerHost;
    }

    public void setNettyServerHost(String nettyServerHost) {
        this.nettyServerHost = nettyServerHost;
    }

    public int getNettyServerPort() {
        return nettyServerPort;
    }

    public void setNettyServerPort(int nettyServerPort) {
        this.nettyServerPort = nettyServerPort;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


}