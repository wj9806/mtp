package com.mtp.config.center.controller;

import com.mtp.config.center.auth.WebUtil;
import com.mtp.config.center.entity.MtpMenuEntity;
import com.mtp.config.center.entity.MtpUserEntity;
import com.mtp.config.center.model.R;
import com.mtp.config.center.model.request.LoginRequest;
import com.mtp.config.center.service.AuthService;
import com.mtp.config.center.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public R login(@RequestBody LoginRequest request) {
        MtpUserEntity user = authService.findByUsername(request.getUsername());

        if (user == null || !Objects.equals(request.getPassword(), user.getPassword())) {
            return R.error("用户名或密码错误");
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            return R.error("用户已被禁用");
        }

        List<MtpMenuEntity> menus = authService.getMenusByUserId(user.getId());

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", Collections.emptyList());
        String token = JwtUtil.generateToken(user.getUsername(), claims);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("menus", menus);
        return R.ok(data);
    }

    @PostMapping("/logout")
    public R logout() {
        return R.ok();
    }

    @GetMapping("/menus")
    public R getMenus() {
        String username = WebUtil.currentUsername();
        MtpUserEntity user = authService.findByUsername(username);
        if (user == null) {
            return R.error("用户不存在");
        }
        List<MtpMenuEntity> menus = authService.getMenusByUserId(user.getId());
        return R.ok(menus);
    }

    @GetMapping("/currentUser")
    public R currentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String username = JwtUtil.getUsernameFromToken(token);
            MtpUserEntity user = authService.findByUsername(username);

            if (user == null) {
                return R.error("用户不存在");
            }

            Map<String, Object> data = new HashMap<>();
            data.put("username", user.getUsername());
            data.put("nickname", user.getNickname());
            return R.ok(data);
        } catch (Exception e) {
            return R.error("token无效");
        }
    }
}