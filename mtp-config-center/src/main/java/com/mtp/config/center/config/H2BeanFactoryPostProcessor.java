package com.mtp.config.center.config;

import com.mtp.core.tp.MtpException;
import lombok.extern.slf4j.Slf4j;
import org.h2.tools.Server;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Slf4j
@Component()
@ConditionalOnProperty(name = "mtp.repository.type", havingValue = "h2")
public class H2BeanFactoryPostProcessor implements BeanFactoryPostProcessor, DisposableBean {

    @Value("${mtp.repository.h2-path}")
    private String h2Path;

    private Server tcpServer;
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        try {
            tcpServer = Server.createTcpServer(
                    "-tcp",
                    "-tcpPort", "9092",
                    "-baseDir", h2Path,
                    "-ifNotExists"
            );
            tcpServer.start();
            log.info("H2 TCP Server start on port 9092");
        } catch (SQLException e) {
            throw new MtpException(e);
        }
    }

    @Override
    public void destroy() throws Exception {
        tcpServer.stop();
        log.info("H2 TCP Server stop");
    }
}
