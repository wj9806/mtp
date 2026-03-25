package com.mtp.config.center.model.request;

import lombok.Data;

import java.util.List;

@Data
public class UserRequest {

    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String phone;
    private String status;
    private List<Long> roleIds;

}
