package com.fitness.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.Result;
import com.fitness.dto.request.AuditRequest;
import com.fitness.dto.request.CreatePlanRequest;
import com.fitness.dto.response.FitnessPlanResponse;
import com.fitness.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 健身计划管理控制器
 */
@RestController
@RequestMapping("/plan")
@RequiredArgsConstructor
public class PlanController {
    
    private final PlanService planService;
    
    /**
     * 教练创建健身计划
     */
    @PostMapping
    @SaCheckRole("COACH")
    public Result<String> createPlan(@Validated @RequestBody CreatePlanRequest request) {
        planService.createPlan(request);
        return Result.success("计划已创建，等待管理员审核");
    }
    
    /**
     * 教练调整健身计划
     */
    @PostMapping("/{planId}/adjust")
    @SaCheckRole("COACH")
    public Result<String> adjustPlan(
            @PathVariable Long planId,
            @Validated @RequestBody CreatePlanRequest request) {
        planService.adjustPlan(planId, request);
        return Result.success("计划已调整，等待管理员审核");
    }
    
    /**
     * 管理员审核健身计划
     */
    @PostMapping("/{planId}/audit")
    @SaCheckRole("ADMIN")
    public Result<String> auditPlan(
            @PathVariable Long planId,
            @Validated @RequestBody AuditRequest request) {
        planService.auditPlan(planId, request);
        return Result.success("审核完成");
    }
    
    /**
     * 用户确认健身计划
     */
    @PostMapping("/{planId}/confirm")
    @SaCheckRole("USER")
    public Result<String> confirmPlan(
            @PathVariable Long planId,
            @RequestParam String confirmStatus,
            @RequestParam(required = false) String rejectReason) {
        planService.confirmPlan(planId, confirmStatus, rejectReason);
        return Result.success("确认完成");
    }
    
    /**
     * 查询用户的健身计划列表
     */
    @GetMapping("/user")
    @SaCheckRole("USER")
    public Result<Page<FitnessPlanResponse>> getUserPlans(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String planStatus) {
        Page<FitnessPlanResponse> page = planService.getUserPlans(pageNum, pageSize, planStatus);
        return Result.success(page);
    }
    
    /**
     * 查询教练创建的计划列表
     */
    @GetMapping("/coach")
    @SaCheckRole("COACH")
    public Result<Page<FitnessPlanResponse>> getCoachPlans(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String auditStatus) {
        Page<FitnessPlanResponse> page = planService.getCoachPlans(pageNum, pageSize, auditStatus);
        return Result.success(page);
    }
    
    /**
     * 查询待审核的计划列表（管理员）
     */
    @GetMapping("/pending")
    @SaCheckRole("ADMIN")
    public Result<Page<FitnessPlanResponse>> getPendingPlans(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String auditStatus) {
        Page<FitnessPlanResponse> page = planService.getPendingPlans(pageNum, pageSize, auditStatus);
        return Result.success(page);
    }
    
    /**
     * 根据ID查询计划详情
     */
    @GetMapping("/{planId}")
    public Result<FitnessPlanResponse> getPlanById(@PathVariable Long planId) {
        FitnessPlanResponse response = planService.getPlanById(planId);
        return Result.success(response);
    }
    
    /**
     * 检查用户是否有激活的计划
     */
    @GetMapping("/check-active")
    @SaCheckRole("USER")
    public Result<Boolean> checkActivePlan() {
        Long userId = cn.dev33.satoken.stp.StpUtil.getLoginIdAsLong();
        boolean hasActive = planService.hasActivePlan(userId);
        return Result.success(hasActive);
    }
}
