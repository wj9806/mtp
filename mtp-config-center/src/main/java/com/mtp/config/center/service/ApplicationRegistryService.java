package com.mtp.config.center.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mtp.config.center.entity.ApplicationRegistryEntity;
import com.mtp.config.center.mapper.ApplicationRegistryMapper;
import com.mtp.config.center.model.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ApplicationRegistryService {

    @Autowired
    private ApplicationRegistryMapper applicationRegistryMapper;

    public R list(int page, int size, String applicationName) {
        LambdaQueryWrapper<ApplicationRegistryEntity> wrapper = new LambdaQueryWrapper<>();
        if (applicationName != null && !applicationName.isEmpty()) {
            wrapper.like(ApplicationRegistryEntity::getApplicationName, applicationName);
        }
        wrapper.orderByDesc(ApplicationRegistryEntity::getCreateTime);
        Page<ApplicationRegistryEntity> pageResult = applicationRegistryMapper.selectPage(new Page<>(page, size), wrapper);
        return R.ok(pageResult);
    }

    @Transactional
    public R add(ApplicationRegistryEntity entity) {
        LambdaQueryWrapper<ApplicationRegistryEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApplicationRegistryEntity::getApplicationName, entity.getApplicationName());
        if (applicationRegistryMapper.selectCount(wrapper) > 0) {
            return R.error("应用名称已存在");
        }
        entity.setAccessToken(UUID.randomUUID().toString().replace("-", ""));
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        applicationRegistryMapper.insert(entity);
        return R.ok();
    }

    public R update(ApplicationRegistryEntity entity) {
        LambdaQueryWrapper<ApplicationRegistryEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApplicationRegistryEntity::getApplicationName, entity.getApplicationName())
                .ne(ApplicationRegistryEntity::getId, entity.getId());
        if (applicationRegistryMapper.selectCount(wrapper) > 0) {
            return R.error("应用名称已存在");
        }
        entity.setUpdateTime(LocalDateTime.now());
        applicationRegistryMapper.updateById(entity);
        return R.ok();
    }

    @Transactional
    public R delete(Long id) {
        applicationRegistryMapper.deleteById(id);
        return R.ok();
    }

    public R getById(Long id) {
        ApplicationRegistryEntity entity = applicationRegistryMapper.selectById(id);
        return R.ok(entity);
    }

    public ApplicationRegistryEntity findByName(String applicationName) {
        LambdaQueryWrapper<ApplicationRegistryEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApplicationRegistryEntity::getApplicationName, applicationName);
        return applicationRegistryMapper.selectOne(wrapper);
    }
}