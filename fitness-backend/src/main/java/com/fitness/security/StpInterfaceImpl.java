package com.fitness.security;

import cn.dev33.satoken.stp.StpInterface;
import com.fitness.entity.User;
import com.fitness.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token权限接口实现
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final UserMapper userMapper;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 本系统暂不使用细粒度权限，返回空列表
        return new ArrayList<>();
    }

    /**
     * 返回一个账号所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> roles = new ArrayList<>();
        
        // 根据用户ID查询角色
        Long userId = Long.parseLong(loginId.toString());
        User user = userMapper.selectById(userId);
        
        if (user != null && user.getRole() != null) {
            roles.add(user.getRole());
        }
        
        return roles;
    }
}
