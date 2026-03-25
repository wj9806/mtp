package com.mtp.config.center.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mtp.config.center.entity.MtpRoleEntity;
import com.mtp.config.center.entity.MtpUserEntity;
import com.mtp.config.center.entity.MtpUserRoleEntity;
import com.mtp.config.center.mapper.MtpRoleMapper;
import com.mtp.config.center.mapper.MtpUserMapper;
import com.mtp.config.center.mapper.MtpUserRoleMapper;
import com.mtp.config.center.model.R;
import com.mtp.config.center.model.vo.UserWithRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private MtpUserMapper userMapper;

    @Autowired
    private MtpRoleMapper roleMapper;

    @Autowired
    private MtpUserRoleMapper userRoleMapper;

    public R list(int page, int size, String username) {

        int offset = (page - 1) * size;
        List<UserWithRoles> users = userMapper.selectUserListWithRoles(username, offset, size);
        long total = userMapper.countUserListWithRoles(username);

        List<Map<String, Object>> records = users.stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("nickname", user.getNickname());
            map.put("email", user.getEmail());
            map.put("phone", user.getPhone());
            map.put("status", user.getStatus());
            map.put("createTime", user.getCreateTime());

            List<Long> roleIds = new ArrayList<>();
            List<String> roleNames = new ArrayList<>();
            if (user.getRoleIds() != null && !user.getRoleIds().isEmpty()) {
                String[] ids = user.getRoleIds().split(",");
                for (String id : ids) {
                    if (!id.trim().isEmpty()) {
                        roleIds.add(Long.parseLong(id.trim()));
                    }
                }
            }
            if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                roleNames.addAll(Arrays.asList(user.getRoles().split(",")));
            }
            map.put("roleIds", roleIds);
            map.put("roles", roleNames);
            return map;
        }).collect(Collectors.toList());

        Page<Map<String, Object>> pageResult = new Page<>(page, size, total);
        pageResult.setRecords(records);
        return R.ok(pageResult);
    }

    public List<MtpRoleEntity> getAllRoles() {
        return roleMapper.selectList(null);
    }

    public List<Long> getUserRoles(Long userId) {
        return userRoleMapper.selectList(
                new LambdaQueryWrapper<MtpUserRoleEntity>().eq(MtpUserRoleEntity::getUserId, userId)
        ).stream().map(MtpUserRoleEntity::getRoleId).collect(Collectors.toList());
    }

    @Transactional
    public R addUser(MtpUserEntity user, List<Long> roleIds) {
        Map<String, Object> result = new HashMap<>();

        LambdaQueryWrapper<MtpUserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MtpUserEntity::getUsername, user.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            return R.error("用户名已存在");
        }

        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);

        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                MtpUserRoleEntity ur = new MtpUserRoleEntity();
                ur.setUserId(user.getId());
                ur.setRoleId(roleId);
                ur.setCreateTime(LocalDateTime.now());
                userRoleMapper.insert(ur);
            }
        }

        return R.ok();
    }

    @Transactional
    public R updateUser(MtpUserEntity user, List<Long> roleIds) {
        Map<String, Object> result = new HashMap<>();

        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        userRoleMapper.delete(new LambdaQueryWrapper<MtpUserRoleEntity>().eq(MtpUserRoleEntity::getUserId, user.getId()));
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                MtpUserRoleEntity ur = new MtpUserRoleEntity();
                ur.setUserId(user.getId());
                ur.setRoleId(roleId);
                ur.setCreateTime(LocalDateTime.now());
                userRoleMapper.insert(ur);
            }
        }

        return R.ok();
    }

    @Transactional
    public R deleteUser(Long id) {
        Map<String, Object> result = new HashMap<>();

        MtpUserEntity user = userMapper.selectById(id);
        if ("mtp".equals(user.getUsername())) {
            return R.error("超级管理员用户不能删除");
        }

        userRoleMapper.delete(new LambdaQueryWrapper<MtpUserRoleEntity>().eq(MtpUserRoleEntity::getUserId, id));
        userMapper.deleteById(id);

        return R.ok();
    }
}