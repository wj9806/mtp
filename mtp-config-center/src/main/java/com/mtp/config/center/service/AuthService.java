package com.mtp.config.center.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mtp.config.center.entity.*;
import com.mtp.config.center.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private MtpUserMapper userMapper;

    @Autowired
    private MtpMenuMapper menuMapper;

    @Autowired
    private MtpRoleMenuMapper roleMenuMapper;

    @Autowired
    private MtpUserRoleMapper userRoleMapper;

    public MtpUserEntity findByUsername(String username) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<MtpUserEntity>().eq(MtpUserEntity::getUsername, username)
        );
    }

    public MtpUserEntity findById(Long id) {
        return userMapper.selectById(id);
    }

    public List<MtpMenuEntity> getMenusByUserId(Long userId) {
        List<Long> roleIds = userRoleMapper.selectList(
                new LambdaQueryWrapper<MtpUserRoleEntity>().eq(MtpUserRoleEntity::getUserId, userId)
        ).stream().map(MtpUserRoleEntity::getRoleId).collect(Collectors.toList());

        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> menuIds = roleMenuMapper.selectList(
                new LambdaQueryWrapper<MtpRoleMenuEntity>().in(MtpRoleMenuEntity::getRoleId, roleIds)
        ).stream().map(MtpRoleMenuEntity::getMenuId).collect(Collectors.toList());

        if (menuIds.isEmpty()) {
            return Collections.emptyList();
        }

        return menuMapper.selectList(
                new LambdaQueryWrapper<MtpMenuEntity>()
                        .in(MtpMenuEntity::getId, menuIds)
                        .eq(MtpMenuEntity::getStatus, "ACTIVE")
                        .orderByAsc(MtpMenuEntity::getOrderNum)
        );
    }
}