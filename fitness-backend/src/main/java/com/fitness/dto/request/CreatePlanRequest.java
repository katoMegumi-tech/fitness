package com.fitness.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * 创建健身计划请求DTO
 */
@Data
public class CreatePlanRequest {
    
    /**
     * 计划所属用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    /**
     * 计划名称
     */
    @NotBlank(message = "计划名称不能为空")
    private String planName;
    
    /**
     * 健身目标
     */
    @NotBlank(message = "健身目标不能为空")
    private String fitnessGoal;
    
    /**
     * 运动计划（JSON格式）
     */
    @NotBlank(message = "运动计划不能为空")
    private String exercisePlan;
    
    /**
     * 饮食计划（JSON格式）
     */
    @NotBlank(message = "饮食计划不能为空")
    private String dietPlan;
    
    /**
     * 计划难度：BEGINNER/INTERMEDIATE/ADVANCED
     */
    @NotBlank(message = "计划难度不能为空")
    private String planDifficulty;
    
    /**
     * 计划周期（天）
     */
    @NotNull(message = "计划周期不能为空")
    @Min(value = 1, message = "计划周期至少1天")
    @Max(value = 365, message = "计划周期最多365天")
    private Integer planCycle;
    
    /**
     * 计划开始时间
     */
    @NotNull(message = "计划开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime planStartTime;
    
    /**
     * 计划结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime planEndTime;
    
    /**
     * 调整原因（调整计划时填写）
     */
    private String adjustmentReason;
}
