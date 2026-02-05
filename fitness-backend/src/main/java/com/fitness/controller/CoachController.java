package com.fitness.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.Result;
import com.fitness.dto.request.AuditRequest;
import com.fitness.dto.request.CoachCertRequest;
import com.fitness.dto.response.CoachProfileResponse;
import com.fitness.entity.CertAuditRecord;
import com.fitness.service.AuditService;
import com.fitness.service.CoachService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教练管理控制器
 */
@RestController
@RequestMapping("/coach")
@RequiredArgsConstructor
public class CoachController {
    
    private final CoachService coachService;
    private final AuditService auditService;
    
    /**
     * 提交教练认证申请
     */
    @PostMapping("/certification")
    @SaCheckRole("COACH")
    public Result<String> submitCertification(@Validated @RequestBody CoachCertRequest request) {
        coachService.submitCertification(request);
        return Result.success("认证申请已提交，请等待管理员审核");
    }
    
    /**
     * 查询当前教练的认证状态
     */
    @GetMapping("/certification/status")
    @SaCheckRole("COACH")
    public Result<CoachProfileResponse> getCertificationStatus() {
        CoachProfileResponse response = coachService.getCertificationStatus();
        return Result.success(response);
    }
    
    /**
     * 根据ID查询教练资料（公开接口）
     */
    @GetMapping("/{coachId}")
    public Result<CoachProfileResponse> getCoachProfile(@PathVariable Long coachId) {
        CoachProfileResponse response = coachService.getCoachProfile(coachId);
        return Result.success(response);
    }
    
    /**
     * 查询已认证的教练列表（用户选择教练）
     */
    @GetMapping("/approved")
    public Result<Page<CoachProfileResponse>> getApprovedCoaches(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<CoachProfileResponse> page = coachService.getApprovedCoaches(pageNum, pageSize);
        return Result.success(page);
    }
    
    /**
     * 查询可选择的教练列表（排除已绑定的教练）
     */
    @GetMapping("/list/available")
    @SaCheckRole("USER")
    public Result<Page<CoachProfileResponse>> getAvailableCoaches(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<CoachProfileResponse> page = coachService.getAvailableCoaches(pageNum, pageSize);
        return Result.success(page);
    }
    
    /**
     * 查询待审核的教练列表（管理员）
     */
    @GetMapping("/pending")
    @SaCheckRole("ADMIN")
    public Result<Page<CoachProfileResponse>> getPendingCoaches(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<CoachProfileResponse> page = coachService.getPendingCoaches(pageNum, pageSize);
        return Result.success(page);
    }
    
    /**
     * 审核教练资质（管理员）
     */
    @PostMapping("/{coachUserId}/audit")
    @SaCheckRole("ADMIN")
    public Result<String> auditCertification(
            @PathVariable Long coachUserId,
            @Validated @RequestBody AuditRequest request) {
        auditService.auditCoachCertification(coachUserId, request);
        return Result.success("审核完成");
    }
    
    /**
     * 查询教练的审核历史（管理员）
     */
    @GetMapping("/{coachUserId}/audit/history")
    @SaCheckRole("ADMIN")
    public Result<List<CertAuditRecord>> getAuditHistory(@PathVariable Long coachUserId) {
        List<CertAuditRecord> history = auditService.getAuditHistory(coachUserId);
        return Result.success(history);
    }
    
    /**
     * 撤销教练审核（管理员）
     */
    @PostMapping("/{coachUserId}/audit/revoke")
    @SaCheckRole("ADMIN")
    public Result<String> revokeAudit(@PathVariable Long coachUserId) {
        auditService.revokeCoachAudit(coachUserId);
        return Result.success("审核已撤销");
    }
}
