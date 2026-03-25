package com.mtp.config.center.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mtp.config.center.entity.MtpMenuEntity;
import com.mtp.config.center.entity.MtpRoleMenuEntity;
import com.mtp.config.center.mapper.MtpMenuMapper;
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
public class MenuService {

    @Autowired
    private MtpMenuMapper menuMapper;

    @Autowired
    private MtpRoleMenuMapper roleMenuMapper;

    public R list() {
        List<MtpMenuEntity> menus = menuMapper.selectList(
            new LambdaQueryWrapper<MtpMenuEntity>().orderByAsc(MtpMenuEntity::getOrderNum)
        );
        return R.ok(menus);
    }

    public R getMenusByRole(Long roleId) {
        List<MtpMenuEntity> allMenus = menuMapper.selectList(
            new LambdaQueryWrapper<MtpMenuEntity>().orderByAsc(MtpMenuEntity::getOrderNum)
        );
        List<Long> roleMenuIds = roleMenuMapper.selectList(
            new LambdaQueryWrapper<MtpRoleMenuEntity>().eq(MtpRoleMenuEntity::getRoleId, roleId)
        ).stream().map(MtpRoleMenuEntity::getMenuId).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("allMenus", allMenus);
        data.put("roleMenuIds", roleMenuIds);
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

    public R add(MtpMenuEntity menu) {
        menu.setCreateTime(LocalDateTime.now());
        menu.setUpdateTime(LocalDateTime.now());
        menuMapper.insert(menu);
        return R.ok();
    }

    public R update(MtpMenuEntity menu) {
        menu.setUpdateTime(LocalDateTime.now());
        menuMapper.updateById(menu);
        return R.ok();
    }

    public R delete(Long id) {
        menuMapper.deleteById(id);
        return R.ok();
    }
}