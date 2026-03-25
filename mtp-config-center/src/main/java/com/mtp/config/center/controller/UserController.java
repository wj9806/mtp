package com.mtp.config.center.controller;

import com.mtp.config.center.entity.MtpUserEntity;
import com.mtp.config.center.model.R;
import com.mtp.config.center.model.request.UserRequest;
import com.mtp.config.center.service.UserService;
import com.mtp.config.center.util.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public R list(@RequestParam(defaultValue = "1") int page,
                  @RequestParam(defaultValue = "10") int size,
                  @RequestParam(required = false) String username) {
        return userService.list(page, size, username);
    }

    @GetMapping("/roles")
    public R getAllRoles() {
        return R.ok(userService.getAllRoles());
    }

    @GetMapping("/{userId}/roles")
    public R getUserRoles(@PathVariable Long userId) {
        return R.ok(userService.getUserRoles(userId));
    }

    @PostMapping("/add")
    public R add(@RequestBody UserRequest request) {
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return R.error("密码不能为空");
        }
        MtpUserEntity user = new MtpUserEntity();
        user.setUsername(request.getUsername());
        user.setPassword(PasswordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(request.getStatus());
        return userService.addUser(user, request.getRoleIds());
    }


    @PutMapping("/update")
    public R update(@RequestBody UserRequest request) {
        MtpUserEntity user = new MtpUserEntity();
        user.setId(request.getId());
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(request.getStatus());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(PasswordEncoder.encode(request.getPassword()));
        }
        return userService.updateUser(user, request.getRoleIds());
    }

    @DeleteMapping("/{id}")
    public R delete(@PathVariable Long id) {
        return userService.deleteUser(id);
    }

}