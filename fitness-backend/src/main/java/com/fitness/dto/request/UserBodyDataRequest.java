package com.fitness.dto.request;

import lombok.Data;
import javax.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 用户身体数据请求DTO
 */
@Data
public class UserBodyDataRequest {
    
    /**
     * 身高（米）
     */
    @NotNull(message = "身高不能为空")
    @DecimalMin(value = "0.5", message = "身高必须在0.5-2.5米之间")
    @DecimalMax(value = "2.5", message = "身高必须在0.5-2.5米之间")
    private BigDecimal height;
    
    /**
     * 当前体重（kg）
     */
    @NotNull(message = "当前体重不能为空")
    @DecimalMin(value = "20", message = "体重必须在20-300kg之间")
    @DecimalMax(value = "300", message = "体重必须在20-300kg之间")
    private BigDecimal currentWeight;
    
    /**
     * 目标体重（kg）
     */
    @NotNull(message = "目标体重不能为空")
    @DecimalMin(value = "20", message = "目标体重必须在20-300kg之间")
    @DecimalMax(value = "300", message = "目标体重必须在20-300kg之间")
    private BigDecimal targetWeight;
    
    /**
     * 当前体脂率（%）
     */
    @DecimalMin(value = "5", message = "体脂率必须在5-60%之间")
    @DecimalMax(value = "60", message = "体脂率必须在5-60%之间")
    private BigDecimal currentBodyFatRate;
    
    /**
     * 目标体脂率（%）
     */
    @DecimalMin(value = "5", message = "目标体脂率必须在5-60%之间")
    @DecimalMax(value = "60", message = "目标体脂率必须在5-60%之间")
    private BigDecimal targetBodyFatRate;
    
    /**
     * 肌肉量（kg）
     */
    private BigDecimal muscleMass;
    
    /**
     * 腰围（cm）
     */
    private BigDecimal waistline;
    
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
    @NotBlank(message = "健身目标不能为空")
    private String fitnessGoal;
    
    /**
     * 每周运动频率（次）
     */
    @Min(value = 1, message = "每周运动频率至少1次")
    @Max(value = 7, message = "每周运动频率最多7次")
    private Integer exerciseFrequency;
    
    /**
     * 每次运动时长（分钟）
     */
    @Min(value = 10, message = "每次运动时长至少10分钟")
    @Max(value = 300, message = "每次运动时长最多300分钟")
    private Integer exerciseDuration;
    
    /**
     * 健康状况说明
     */
    private String healthConditions;
}
