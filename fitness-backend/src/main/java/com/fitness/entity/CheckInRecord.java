package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 打卡记录实体类
 */
@Data
@TableName("t_check_in_record")
public class CheckInRecord {
    
    /**
     * 主键ID
     */
    @TableId(value = "check_id", type = IdType.AUTO)
    private Long checkId;
    
    /**
     * 打卡编号
     */
    private String checkNo;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 计划ID
     */
    private Long planId;
    
    /**
     * 打卡类型：NORMAL/TARGET
     */
    private String checkType;
    
    /**
     * 是否达标：1=达标, 0=未达标
     */
    private Integer isQualified;
    
    /**
     * 运动完成度（0-100）
     */
    private Integer exerciseCompletion;
    
    /**
     * 饮食完成度：COMPLETE/PARTIAL/INCOMPLETE
     */
    private String dietCompletion;
    
    /**
     * 打卡时间
     */
    private LocalDateTime checkTime;
    
    /**
     * 当日体重（kg）
     */
    private BigDecimal weight;
    
    /**
     * 当日体脂率（%）
     */
    @TableField("body_fat_rate")
    private BigDecimal bodyFatRate;
    
    /**
     * 肌肉量（kg）
     */
    private BigDecimal muscleMass;
    
    /**
     * 打卡图片路径（JSON数组）
     */
    private String images;
    
    /**
     * 打卡视频路径（JSON数组）
     */
    private String videos;
    
    /**
     * 用户备注
     */
    @TableField("user_remark")
    private String userRemark;
    
    /**
     * 教练点评
     */
    private String coachComment;
    
    /**
     * 教练点评时间
     */
    @TableField("coach_comment_time")
    private LocalDateTime coachCommentTime;
}
