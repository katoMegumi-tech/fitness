package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户身体基础数据实体类
 */
@Data
@TableName("t_user_body_base_data")
public class UserBodyBaseData {
    
    /**
     * 数据ID
     */
    @TableId(value = "data_id", type = IdType.AUTO)
    private Long dataId;
    
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
     * 健身目标：减脂/增肌/塑形/保持
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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
