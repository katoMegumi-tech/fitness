package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 健身科普文章实体类
 */
@Data
@TableName("t_fitness_popular_science")
public class FitnessArticle {
    
    /**
     * 文章ID
     */
    @TableId(value = "article_id", type = IdType.AUTO)
    private Long articleId;
    
    /**
     * 文章编号
     */
    private String articleNo;
    
    /**
     * 文章标题
     */
    private String title;
    
    /**
     * 文章分类
     */
    private String category;
    
    /**
     * 文章内容
     */
    private String content;
    
    /**
     * 封面图片路径
     */
    private String coverImage;
    
    /**
     * 作者ID（教练）
     */
    private Long authorId;
    
    /**
     * 标签（逗号分隔）
     */
    private String tags;
    
    /**
     * 发布状态：DRAFT/PUBLISHED/OFFLINE
     */
    private String publishStatus;
    
    /**
     * 是否置顶：0=否, 1=是
     */
    private Integer isTop;
    
    /**
     * 置顶时间
     */
    private LocalDateTime topTime;
    
    /**
     * 发布时间
     */
    private LocalDateTime publishTime;
    
    /**
     * 审核状态：PENDING/APPROVED/REJECTED
     */
    private String auditStatus;
    
    /**
     * 审核管理员ID
     */
    private Long auditAdminId;
    
    /**
     * 审核时间
     */
    private LocalDateTime auditTime;
    
    /**
     * 审核备注
     */
    private String auditRemark;
    
    /**
     * 浏览量
     */
    private Integer viewCount;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 收藏数
     */
    private Integer collectCount;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;
}
