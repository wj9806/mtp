package com.mtp.config.center.controller;

import com.mtp.config.center.entity.MtpRoleEntity;
import com.mtp.config.center.model.R;
import com.mtp.config.center.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping("/list")
    public R list(@RequestParam(defaultValue = "1") int page,
                  @RequestParam(defaultValue = "10") int size,
                  @RequestParam(required = false) String roleName) {
        return roleService.list(page, size, roleName);
    }

    @GetMapping("/menus/{roleId}")
    public R getRoleMenus(@PathVariable Long roleId) {
        return roleService.getRoleMenus(roleId);
    }

    @PostMapping("/menus/{roleId}")
    public R assignMenus(@PathVariable Long roleId, @RequestBody List<Long> menuIds) {
        return roleService.assignMenus(roleId, menuIds);
    }

    @PostMapping("/add")
    public R add(@RequestBody MtpRoleEntity role) {
        return roleService.add(role);
    }

    @PutMapping("/update")
    public R update(@RequestBody MtpRoleEntity role) {
        return roleService.update(role);
    }

    @DeleteMapping("/{id}")
    public R delete(@PathVariable Long id) {
        return roleService.delete(id);
    }
}