package com.fitness.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.fitness.common.Result;
import com.fitness.dto.request.UserBodyDataRequest;
import com.fitness.dto.response.UserBodyDataResponse;
import com.fitness.entity.User;
import com.fitness.entity.UserBodyHistory;
import com.fitness.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户信息管理控制器
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<User> getUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userService.getUserById(userId);
        // 清除敏感信息
        user.setPassword(null);
        return Result.success(user);
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    public Result<String> updateUserInfo(@RequestBody Map<String, Object> updates) {
        Long userId = StpUtil.getLoginIdAsLong();
        userService.updateUserInfo(userId, updates);
        return Result.success("更新成功");
    }
    
    /**
     * 保存或更新用户身体数据
     */
    @PostMapping("/body-data")
    @SaCheckRole("USER")
    public Result<String> saveBodyData(@Validated @RequestBody UserBodyDataRequest request) {
        userService.saveOrUpdateBodyData(request);
        return Result.success("保存成功");
    }
    
    /**
     * 查询当前用户的身体基础数据
     */
    @GetMapping("/body-data")
    @SaCheckRole("USER")
    public Result<UserBodyDataResponse> getBodyData() {
        UserBodyDataResponse response = userService.getBodyData();
        return Result.success(response);
    }
    
    /**
     * 查询用户身体数据历史记录
     */
    @GetMapping("/body-history")
    @SaCheckRole("USER")
    public Result<List<UserBodyHistory>> getBodyHistory(
            @RequestParam(required = false, defaultValue = "30") Integer days) {
        List<UserBodyHistory> history = userService.getBodyHistory(days);
        return Result.success(history);
    }
    
    /**
     * 根据用户ID查询身体数据（教练查看学员数据）
     */
    @GetMapping("/{userId}/body-data")
    @SaCheckRole("COACH")
    public Result<UserBodyDataResponse> getUserBodyData(@PathVariable Long userId) {
        UserBodyDataResponse response = userService.getBodyDataByUserId(userId);
        return Result.success(response);
    }
    
    /**
     * 根据用户ID查询身体数据历史（教练查看学员数据）
     */
    @GetMapping("/{userId}/body-history")
    @SaCheckRole("COACH")
    public Result<List<UserBodyHistory>> getUserBodyHistory(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "30") Integer days) {
        List<UserBodyHistory> history = userService.getBodyHistoryByUserId(userId, days);
        return Result.success(history);
    }
}
