package com.fitness.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 用户登录请求
 */
@Data
public class LoginRequest {

    /**
     * 账号
     */
    @NotBlank(message = "账号不能为空")
    private String account;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
