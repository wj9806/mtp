package com.mtp.config.center.controller;

import com.mtp.config.center.entity.MtpMenuEntity;
import com.mtp.config.center.model.R;
import com.mtp.config.center.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    @Autowired
    private MenuService menuService;

    @GetMapping("/list")
    public R list() {
        return menuService.list();
    }

    @GetMapping("/role/{roleId}")
    public R getMenusByRole(@PathVariable Long roleId) {
        return menuService.getMenusByRole(roleId);
    }

    @PostMapping("/role/{roleId}")
    public R assignMenus(@PathVariable Long roleId, @RequestBody List<Long> menuIds) {
        return menuService.assignMenus(roleId, menuIds);
    }

    @PostMapping("/add")
    public R add(@RequestBody MtpMenuEntity menu) {
        return menuService.add(menu);
    }

    @PutMapping("/update")
    public R update(@RequestBody MtpMenuEntity menu) {
        return menuService.update(menu);
    }

    @DeleteMapping("/{id}")
    public R delete(@PathVariable Long id) {
        return menuService.delete(id);
    }
}