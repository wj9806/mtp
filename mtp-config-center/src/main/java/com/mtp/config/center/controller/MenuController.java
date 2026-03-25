package com.mtp.config.center.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mtp.config.center.entity.MtpMenuEntity;
import com.mtp.config.center.entity.MtpRoleMenuEntity;
import com.mtp.config.center.mapper.MtpMenuMapper;
import com.mtp.config.center.mapper.MtpRoleMenuMapper;
import com.mtp.config.center.model.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    @Autowired
    private MtpMenuMapper menuMapper;

    @Autowired
    private MtpRoleMenuMapper roleMenuMapper;

    @GetMapping("/list")
    public R list() {
        List<MtpMenuEntity> menus = menuMapper.selectList(
            new LambdaQueryWrapper<MtpMenuEntity>().orderByAsc(MtpMenuEntity::getOrderNum)
        );
        return R.ok(menus);
    }

    @GetMapping("/role/{roleId}")
    public R getMenusByRole(@PathVariable Long roleId) {
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

    @PostMapping("/role/{roleId}")
    public R assignMenus(@PathVariable Long roleId, @RequestBody List<Long> menuIds) {
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

    @PostMapping("/add")
    public R add(@RequestBody MtpMenuEntity menu) {
        menu.setCreateTime(LocalDateTime.now());
        menu.setUpdateTime(LocalDateTime.now());
        menuMapper.insert(menu);
        return R.ok();
    }

    @PutMapping("/update")
    public R update(@RequestBody MtpMenuEntity menu) {
        menu.setUpdateTime(LocalDateTime.now());
        menuMapper.updateById(menu);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R delete(@PathVariable Long id) {
        menuMapper.deleteById(id);
        return R.ok();
    }
}