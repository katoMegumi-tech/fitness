package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户身体数据历史记录实体类
 */
@Data
@TableName("t_user_body_history")
public class UserBodyHistory {
    
    /**
     * 历史记录ID
     */
    @TableId(value = "history_id", type = IdType.AUTO)
    private Long historyId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 体重（kg）
     */
    private BigDecimal weight;
    
    /**
     * 体脂率（%）
     */
    private BigDecimal bodyFatRate;
    
    /**
     * 肌肉量（kg）
     */
    private BigDecimal muscleMass;
    
    /**
     * BMI指数
     */
    private BigDecimal bmi;
    
    /**
     * 腰围（cm）
     */
    private BigDecimal waistline;
    
    /**
     * 记录日期
     */
    private LocalDate recordDate;
    
    /**
     * 记录来源：MANUAL/CHECK_IN/DEVICE
     */
    private String recordSource;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
