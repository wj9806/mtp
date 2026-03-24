package com.mtp.config.center;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * 配置中心应用启动类
 */
@SpringBootApplication
@ConfigurationPropertiesScan("com.mtp.config.center.config")
public class MtpConfigCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(MtpConfigCenterApplication.class, args);
    }
}