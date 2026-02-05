package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户-教练绑定关系实体类
 */
@Data
@TableName("t_bind_record")
public class BindRecord {
    
    /**
     * 绑定ID
     */
    @TableId(value = "bind_id", type = IdType.AUTO)
    private Long bindId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 教练ID
     */
    private Long coachUserId;
    
    /**
     * 绑定时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime bindTime;
    
    /**
     * 解绑时间（NULL表示仍绑定）
     */
    private LocalDateTime unbindTime;
    
    /**
     * 解绑原因
     */
    private String unbindReason;
}
