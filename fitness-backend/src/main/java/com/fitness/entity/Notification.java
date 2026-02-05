package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 消息通知实体类
 */
@Data
@TableName("t_notification")
public class Notification {
    
    /**
     * 通知ID
     */
    @TableId(value = "notification_id", type = IdType.AUTO)
    private Long notificationId;
    
    /**
     * 接收者ID
     */
    private Long receiverId;
    
    /**
     * 发送者ID（系统通知为NULL）
     */
    private Long senderId;
    
    /**
     * 通知类型
     */
    private String notificationType;
    
    /**
     * 通知标题
     */
    private String title;
    
    /**
     * 通知内容
     */
    private String content;
    
    /**
     * 关联业务ID
     */
    private Long relatedId;
    
    /**
     * 是否已读：0=未读, 1=已读
     */
    private Integer isRead;
    
    /**
     * 阅读时间
     */
    private LocalDateTime readTime;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
