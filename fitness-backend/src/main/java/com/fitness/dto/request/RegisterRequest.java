package com.fitness.dto.request;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 用户注册请求
 */
@Data
public class RegisterRequest {

    /**
     * 账号
     */
    @NotBlank(message = "账号不能为空")
    @Size(min = 3, max = 20, message = "账号长度在3-20个字符")
    private String account;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6位")
    private String password;

    /**
     * 姓名
     */
    @NotBlank(message = "姓名不能为空")
    private String name;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 角色：USER/COACH
     */
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "^(USER|COACH)$", message = "角色只能是USER或COACH")
    private String role;
}
