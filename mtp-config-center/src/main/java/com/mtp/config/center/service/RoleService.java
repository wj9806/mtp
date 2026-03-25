package com.mtp.config.center.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mtp.config.center.entity.MtpMenuEntity;
import com.mtp.config.center.entity.MtpRoleEntity;
import com.mtp.config.center.entity.MtpRoleMenuEntity;
import com.mtp.config.center.mapper.MtpMenuMapper;
import com.mtp.config.center.mapper.MtpRoleMapper;
import com.mtp.config.center.mapper.MtpRoleMenuMapper;
import com.mtp.config.center.model.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RoleService {

    @Autowired
    private MtpRoleMapper roleMapper;

    @Autowired
    private MtpMenuMapper menuMapper;

    @Autowired
    private MtpRoleMenuMapper roleMenuMapper;

    public R list(int page, int size, String roleName) {
        LambdaQueryWrapper<MtpRoleEntity> wrapper = new LambdaQueryWrapper<>();
        if (roleName != null && !roleName.isEmpty()) {
            wrapper.like(MtpRoleEntity::getRoleName, roleName);
        }
        wrapper.orderByDesc(MtpRoleEntity::getCreateTime);
        Page<MtpRoleEntity> pageResult = roleMapper.selectPage(new Page<>(page, size), wrapper);
        return R.ok(pageResult);
    }

    public R getRoleMenus(Long roleId) {
        List<MtpMenuEntity> allMenus = menuMapper.selectList(
                new LambdaQueryWrapper<MtpMenuEntity>()
                        .eq(MtpMenuEntity::getStatus, "ACTIVE")
                        .orderByAsc(MtpMenuEntity::getOrderNum)
        );
        List<Long> assignedMenuIds = roleMenuMapper.selectList(
                new LambdaQueryWrapper<MtpRoleMenuEntity>().eq(MtpRoleMenuEntity::getRoleId, roleId)
        ).stream().map(MtpRoleMenuEntity::getMenuId).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("allMenus", allMenus);
        data.put("assignedMenuIds", assignedMenuIds);
        return R.ok(data);
    }

    @Transactional
    public R assignMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.delete(new LambdaQueryWrapper<MtpRoleMenuEntity>().eq(MtpRoleMenuEntity::getRoleId, roleId));
        for (Long menuId : menuIds) {
            MtpRoleMenuEntity rm = new MtpRoleMenuEntity();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            rm.setCreateTime(LocalDateTime.now());
            roleMenuMapper.insert(rm);
        }
        return R.ok();
    }

    public R add(MtpRoleEntity role) {
        LambdaQueryWrapper<MtpRoleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MtpRoleEntity::getRoleCode, role.getRoleCode());
        if (roleMapper.selectCount(wrapper) > 0) {
            return R.error("角色代码已存在");
        }
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        roleMapper.insert(role);
        return R.ok();
    }

    public R update(MtpRoleEntity role) {
        role.setUpdateTime(LocalDateTime.now());
        roleMapper.updateById(role);
        return R.ok();
    }

    @Transactional
    public R delete(Long id) {
        roleMenuMapper.delete(new LambdaQueryWrapper<MtpRoleMenuEntity>().eq(MtpRoleMenuEntity::getRoleId, id));
        roleMapper.deleteById(id);
        return R.ok();
    }
}