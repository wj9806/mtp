package com.mtp.config.center.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserWithRoles {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String status;
    private LocalDateTime createTime;
    private String roleIds;
    private String roles;
}