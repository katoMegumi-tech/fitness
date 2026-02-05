package com.fitness.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.Result;
import com.fitness.dto.request.BindApplyRequest;
import com.fitness.dto.request.HandleBindRequest;
import com.fitness.dto.response.BindApplyResponse;
import com.fitness.dto.response.BindRecordResponse;
import com.fitness.service.BindingService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 绑定管理控制器
 */
@RestController
@RequestMapping("/binding")
@RequiredArgsConstructor
public class BindingController {
    
    private final BindingService bindingService;
    
    /**
     * 用户发起绑定申请
     */
    @PostMapping("/apply")
    @SaCheckRole("USER")
    public Result<String> applyBinding(@Validated @RequestBody BindApplyRequest request) {
        bindingService.applyBinding(request);
        return Result.success("申请已提交，请等待教练处理");
    }
    
    /**
     * 教练处理绑定申请
     */
    @PostMapping("/handle/{applyId}")
    @SaCheckRole("COACH")
    public Result<String> handleBinding(
            @PathVariable Long applyId,
            @Validated @RequestBody HandleBindRequest request) {
        bindingService.handleBinding(applyId, request);
        return Result.success("处理完成");
    }
    
    /**
     * 解绑
     */
    @PostMapping("/unbind")
    @SaCheckRole("USER")
    public Result<String> unbind(@RequestParam(required = false) String unbindReason) {
        bindingService.unbind(unbindReason);
        return Result.success("解绑成功");
    }
    
    /**
     * 查询用户的绑定申请列表
     */
    @GetMapping("/user/applies")
    @SaCheckRole("USER")
    public Result<Page<BindApplyResponse>> getUserApplies(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<BindApplyResponse> page = bindingService.getUserApplies(pageNum, pageSize);
        return Result.success(page);
    }
    
    /**
     * 查询教练收到的绑定申请列表
     */
    @GetMapping("/coach/applies")
    @SaCheckRole("COACH")
    public Result<Page<BindApplyResponse>> getCoachApplies(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status) {
        Page<BindApplyResponse> page = bindingService.getCoachApplies(pageNum, pageSize, status);
        return Result.success(page);
    }
    
    /**
     * 查询用户当前的绑定关系
     */
    @GetMapping("/current")
    @SaCheckRole("USER")
    public Result<BindRecordResponse> getCurrentBinding() {
        BindRecordResponse response = bindingService.getCurrentBinding();
        return Result.success(response);
    }
    
    /**
     * 查询教练的学员列表
     */
    @GetMapping("/coach/students")
    @SaCheckRole("COACH")
    public Result<Page<BindRecordResponse>> getCoachStudents(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<BindRecordResponse> page = bindingService.getCoachStudents(pageNum, pageSize);
        return Result.success(page);
    }
}
