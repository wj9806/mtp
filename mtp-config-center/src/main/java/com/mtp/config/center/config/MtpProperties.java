package com.mtp.config.center.config;

import com.mtp.core.model.ClientProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "mtp")
public class MtpProperties {

    private Repository repository;

    private Deploy deploy;

    private ClientProperties client;

    private Server server;

    @Data
    public static class Server {
        private String tcpPort;
    }

    @Data
    public static class Repository {
        //选择数据类型 h2 mysql
        // 单机部署支持h2和mysql
        // 集群部署只支持mysql
        private String type;
        //H2 内存数据库文件存储地址
        private String h2Path;
    }

    @Data
    public static class Deploy {
        // single 单机部署 cluster 集群
        private String type;
    }

}
