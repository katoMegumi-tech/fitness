package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文章互动实体类
 */
@Data
@TableName("t_article_interaction")
public class ArticleInteraction {
    
    /**
     * 互动ID
     */
    @TableId(value = "interaction_id", type = IdType.AUTO)
    private Long interactionId;
    
    /**
     * 文章ID
     */
    private Long articleId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 互动类型：LIKE/COLLECT/VIEW
     */
    private String interactionType;
    
    /**
     * 互动时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime interactionTime;
}
