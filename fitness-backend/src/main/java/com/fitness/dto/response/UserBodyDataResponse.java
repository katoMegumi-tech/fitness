package com.fitness.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户身体数据响应DTO
 */
@Data
public class UserBodyDataResponse {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 身高（米）
     */
    private BigDecimal height;
    
    /**
     * 目标体重（kg）
     */
    private BigDecimal targetWeight;
    
    /**
     * 目标体脂率（%）
     */
    private BigDecimal targetBodyFatRate;
    
    /**
     * 饮食偏好
     */
    private String foodPreference;
    
    /**
     * 过敏源
     */
    private String allergens;
    
    /**
     * 健身目标
     */
    private String fitnessGoal;
    
    /**
     * 每周运动频率（次）
     */
    private Integer exerciseFrequency;
    
    /**
     * 每次运动时长（分钟）
     */
    private Integer exerciseDuration;
    
    /**
     * 健康状况说明
     */
    private String healthConditions;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
