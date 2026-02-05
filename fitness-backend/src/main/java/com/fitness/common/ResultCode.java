package com.fitness.common;

import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 客户端错误
    ERROR(400, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    PARAM_MISSING(400, "缺少必填参数"),
    PARAM_INVALID(400, "参数格式不正确"),

    // 认证授权错误
    UNAUTHORIZED(401, "未登录或登录已过期"),
    TOKEN_INVALID(401, "Token无效"),
    TOKEN_EXPIRED(401, "Token已过期"),
    LOGIN_FAILED(401, "用户名或密码错误"),

    // 权限错误
    FORBIDDEN(403, "无权限访问"),
    ROLE_FORBIDDEN(403, "角色权限不足"),

    // 资源错误
    NOT_FOUND(404, "资源不存在"),
    USER_NOT_FOUND(404, "用户不存在"),
    COACH_NOT_FOUND(404, "教练不存在"),
    PLAN_NOT_FOUND(404, "健身计划不存在"),

    // 业务错误
    BUSINESS_ERROR(400, "业务处理失败"),
    ACCOUNT_EXISTS(400, "账号已存在"),
    ACCOUNT_DISABLED(400, "账号已被禁用"),
    COACH_NOT_CERTIFIED(400, "教练未认证"),
    BINDING_EXISTS(400, "已存在绑定关系"),
    BINDING_NOT_FOUND(400, "绑定关系不存在"),
    CHECKIN_EXISTS(400, "今日已打卡"),
    AUDIT_STATUS_ERROR(400, "审核状态错误"),

    // 服务器错误
    INTERNAL_ERROR(500, "服务器内部错误"),
    DATABASE_ERROR(500, "数据库操作失败"),
    FILE_UPLOAD_ERROR(500, "文件上传失败");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
