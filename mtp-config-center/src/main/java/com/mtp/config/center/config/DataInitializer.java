package com.mtp.config.center.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mtp.config.center.entity.MtpUserEntity;
import com.mtp.config.center.mapper.MtpUserMapper;
import com.mtp.config.center.util.PasswordEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    private final MtpUserMapper userMapper;

    public DataInitializer(MtpUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public void run(String... args) {
        initAdminUser();
    }

    private void initAdminUser() {
        LambdaQueryWrapper<MtpUserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MtpUserEntity::getUsername, "mtp");
        if (userMapper.selectCount(wrapper) == 0) {
            MtpUserEntity admin = new MtpUserEntity();
            admin.setUsername("mtp");
            admin.setPassword(PasswordEncoder.encode("mtp"));
            admin.setNickname("管理员");
            admin.setStatus("ACTIVE");
            admin.setCreateTime(LocalDateTime.now());
            admin.setUpdateTime(LocalDateTime.now());
            userMapper.insert(admin);
            log.info("Created default admin user: mtp");
        }
    }
}