package com.fitness.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.common.ResultCode;
import com.fitness.dto.request.LoginRequest;
import com.fitness.dto.request.RegisterRequest;
import com.fitness.dto.response.LoginResponse;
import com.fitness.entity.User;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.UserMapper;
import com.fitness.util.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterRequest request) {
        // 检查账号是否已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getAccount, request.getAccount());
        User existUser = userMapper.selectOne(queryWrapper);
        if (existUser != null) {
            throw new BusinessException(ResultCode.ACCOUNT_EXISTS);
        }

        // 创建用户
        User user = new User();
        user.setAccount(request.getAccount());
        user.setPassword(PasswordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setAccountStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());

        userMapper.insert(user);
        log.info("用户注册成功: account={}, role={}", user.getAccount(), user.getRole());

        return user.getUserId();
    }

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getAccount, request.getAccount());
        User user = userMapper.selectOne(queryWrapper);

        // 验证用户是否存在
        if (user == null) {
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }

        // 验证密码
        if (!PasswordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }

        // 验证账号状态
        if (!"ACTIVE".equals(user.getAccountStatus())) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        // 登录成功，生成Token
        StpUtil.login(user.getUserId());
        String token = StpUtil.getTokenValue();

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户登录成功: account={}, role={}", user.getAccount(), user.getRole());

        // 构建响应
        return LoginResponse.builder()
                .token(token)
                .userId(user.getUserId())
                .account(user.getAccount())
                .name(user.getName())
                .role(user.getRole())
                .build();
    }

    /**
     * 用户登出
     */
    public void logout() {
        StpUtil.logout();
        log.info("用户登出成功: userId={}", StpUtil.getLoginIdDefaultNull());
    }
}
