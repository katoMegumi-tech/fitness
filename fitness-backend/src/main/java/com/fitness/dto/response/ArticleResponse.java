package com.fitness.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文章响应DTO
 */
@Data
public class ArticleResponse {
    
    private Long articleId;
    private String articleNo;
    private String title;
    private String category;
    private String content;
    private String coverImage;
    private Long authorId;
    private String authorName;
    private String tags;
    private String publishStatus;
    private Integer isTop;
    private LocalDateTime publishTime;
    private String auditStatus;
    private String auditRemark;
    private Integer viewCount;
    private Integer likeCount;
    private Integer collectCount;
    private LocalDateTime createdAt;
    
    // 用户互动状态
    private Boolean isLiked;
    private Boolean isCollected;
}
