package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 教练扩展信息实体类
 */
@Data
@TableName("coach_profile")
public class CoachProfile {
    
    /**
     * 教练用户ID（主键，关联user表）
     */
    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;
    
    /**
     * 证书附件路径（JSON数组）
     */
    private String certsPath;
    
    /**
     * 个人简介
     */
    private String introduction;
    
    /**
     * 擅长领域（如：减脂/增肌/康复训练）
     */
    private String specialties;
    
    /**
     * 从业年限
     */
    private Integer yearsOfExperience;
    
    /**
     * 认证状态：PENDING/APPROVED/REJECTED
     */
    private String certificationStatus;
    
    /**
     * 教练评分（1-5分）
     */
    private BigDecimal rating;
    
    /**
     * 累计服务学员数
     */
    private Integer totalStudents;
    
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
