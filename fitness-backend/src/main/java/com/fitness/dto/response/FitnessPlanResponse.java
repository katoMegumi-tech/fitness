package com.fitness.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 健身计划响应DTO
 */
@Data
public class FitnessPlanResponse {
    
    /**
     * 计划ID
     */
    private Long planId;
    
    /**
     * 计划编号
     */
    private String planNo;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户姓名
     */
    private String userName;
    
    /**
     * 教练ID
     */
    private Long coachUserId;
    
    /**
     * 教练姓名
     */
    private String coachName;
    
    /**
     * 计划名称
     */
    private String planName;
    
    /**
     * 健身目标
     */
    private String fitnessGoal;
    
    /**
     * 运动计划
     */
    private String exercisePlan;
    
    /**
     * 饮食计划
     */
    private String dietPlan;
    
    /**
     * 计划难度
     */
    private String planDifficulty;
    
    /**
     * 计划周期（天）
     */
    private Integer planCycle;
    
    /**
     * 计划开始时间
     */
    private LocalDateTime planStartTime;
    
    /**
     * 计划结束时间
     */
    private LocalDateTime planEndTime;
    
    /**
     * 计划状态
     */
    private String planStatus;
    
    /**
     * 版本号
     */
    private Integer version;
    
    /**
     * 父计划ID
     */
    private Long parentPlanId;
    
    /**
     * 调整原因
     */
    private String adjustmentReason;
    
    /**
     * 审核状态
     */
    private String auditStatus;
    
    /**
     * 审核备注
     */
    private String auditRemark;
    
    /**
     * 审核时间
     */
    private LocalDateTime auditTime;
    
    /**
     * 用户确认状态
     */
    private String userConfirmStatus;
    
    /**
     * 用户确认时间
     */
    private LocalDateTime userConfirmTime;
    
    /**
     * 用户拒绝理由
     */
    private String userRejectReason;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
