package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户-教练绑定申请实体类
 */
@Data
@TableName("t_bind_apply")
public class BindApply {
    
    /**
     * 申请ID
     */
    @TableId(value = "apply_id", type = IdType.AUTO)
    private Long applyId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 教练ID
     */
    private Long coachUserId;
    
    /**
     * 申请理由
     */
    private String applyReason;
    
    /**
     * 申请状态：PENDING/APPROVED/REJECTED/CANCELLED
     */
    private String applyStatus;
    
    /**
     * 拒绝理由
     */
    private String rejectReason;
    
    /**
     * 申请时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime applyTime;
    
    /**
     * 处理时间
     */
    private LocalDateTime handleTime;
}
