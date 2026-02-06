package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 健身计划实体类
 */
@Data
@TableName("t_fitness_plan")
public class FitnessPlan {
    
    /**
     * 计划ID
     */
    @TableId(value = "plan_id", type = IdType.AUTO)
    private Long planId;
    
    /**
     * 计划编号
     */
    private String planNo;
    
    /**
     * 计划所属用户ID
     */
    private Long userId;
    
    /**
     * 制定教练ID
     */
    private Long coachUserId;
    
    /**
     * 计划名称
     */
    private String planName;
    
    /**
     * 健身目标
     */
    private String fitnessGoal;
    
    /**
     * 运动计划（JSON格式）
     */
    private String exercisePlan;
    
    /**
     * 饮食计划（JSON格式）
     */
    private String dietPlan;
    
    /**
     * 计划难度：BEGINNER/INTERMEDIATE/ADVANCED
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
     * 计划状态：DRAFT/PENDING/ACTIVE/COMPLETED/CANCELLED
     */
    private String planStatus;
    
    /**
     * 计划版本号
     */
    private Integer version;
    
    /**
     * 父计划ID（调整后的计划关联原计划）
     */
    private Long parentPlanId;
    
    /**
     * 调整原因
     */
    private String adjustmentReason;
    
    /**
     * 审核状态：PENDING/APPROVED/REJECTED
     */
    private String auditStatus;
    
    /**
     * 审核管理员ID
     */
    private Long auditAdminId;
    
    /**
     * 审核时间
     */
    private LocalDateTime auditTime;
    
    /**
     * 审核备注
     */
    private String auditRemark;
    
    /**
     * 用户确认状态：PENDING/APPROVED/REJECTED
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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
