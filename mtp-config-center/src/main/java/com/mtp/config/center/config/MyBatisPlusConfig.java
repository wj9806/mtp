package com.mtp.config.center.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.mtp.config.center.mapper.ClientRegistryMapper;
import com.mtp.config.center.mapper.ThreadPoolConfigMapper;
import com.mtp.config.center.mapper.ThreadPoolStatusMapper;
import com.mtp.config.center.repository.ConfigCenterRepository;
import com.mtp.config.center.repository.MyBatisPlusConfigCenterRepository;
import com.mtp.core.tp.MtpException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Slf4j
@Configuration
@EnableTransactionManagement
public class MyBatisPlusConfig {

    private final MtpProperties mtpProperties;

    public MyBatisPlusConfig(MtpProperties mtpProperties) {
        this.mtpProperties = mtpProperties;

        if ("cluster".equals(mtpProperties.getDeploy().getType())) {
            if ("h2".equals(mtpProperties.getRepository().getType())) {
                throw new MtpException("集群部署不支持H2数据库");
            }
        }

        log.info("mtp-server using {} deployment", mtpProperties.getDeploy().getType());
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        DbType dbType = "mysql".equalsIgnoreCase(mtpProperties.getRepository().getType()) ? DbType.MYSQL : DbType.H2;
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(dbType));
        return interceptor;
    }

    @Bean
    @Primary
    public ConfigCenterRepository configCenterRepository(
            ThreadPoolConfigMapper configMapper,
            ThreadPoolStatusMapper statusMapper,
            ClientRegistryMapper clientRegistryMapper) {
        return new MyBatisPlusConfigCenterRepository(configMapper, statusMapper, clientRegistryMapper);
    }
}