package com.mtp.starter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MTP配置属性类，用于从application.yml中读取mtp相关配置
 */
@Data
@ConfigurationProperties(prefix = "mtp")
public class MtpProperties {

    private String applicationName;
    private String accessToken;
    private String mtpServerHost = "localhost";
    private int mtpServerPort = 9090;
    private boolean enabled = true;

}