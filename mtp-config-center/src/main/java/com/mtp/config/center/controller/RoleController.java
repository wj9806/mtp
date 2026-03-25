package com.mtp.config.center.controller;

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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/role")
public class RoleController {

    @Autowired
    private MtpRoleMapper roleMapper;

    @Autowired
    private MtpMenuMapper menuMapper;

    @Autowired
    private MtpRoleMenuMapper roleMenuMapper;

    @GetMapping("/list")
    public R list(@RequestParam(defaultValue = "1") int page,
                  @RequestParam(defaultValue = "10") int size,
                  @RequestParam(required = false) String roleName) {
        LambdaQueryWrapper<MtpRoleEntity> wrapper = new LambdaQueryWrapper<>();
        if (roleName != null && !roleName.isEmpty()) {
            wrapper.like(MtpRoleEntity::getRoleName, roleName);
        }
        wrapper.orderByDesc(MtpRoleEntity::getCreateTime);
        Page<MtpRoleEntity> pageResult = roleMapper.selectPage(new Page<>(page, size), wrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("records", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        return R.ok(data);
    }

    @GetMapping("/menus/{roleId}")
    public R getRoleMenus(@PathVariable Long roleId) {
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

    @PostMapping("/menus/{roleId}")
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
    public R add(@RequestBody MtpRoleEntity role) {
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


    @PutMapping("/update")
    public R update(@RequestBody MtpRoleEntity role) {
        role.setUpdateTime(LocalDateTime.now());
        roleMapper.updateById(role);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R delete(@PathVariable Long id) {
        roleMenuMapper.delete(new LambdaQueryWrapper<MtpRoleMenuEntity>().eq(MtpRoleMenuEntity::getRoleId, id));
        roleMapper.deleteById(id);
        return R.ok();
    }
}