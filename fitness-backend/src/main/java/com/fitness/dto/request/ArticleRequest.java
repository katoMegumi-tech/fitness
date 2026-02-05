package com.fitness.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 文章请求DTO
 */
@Data
public class ArticleRequest {
    
    @NotBlank(message = "文章标题不能为空")
    private String title;
    
    @NotBlank(message = "文章分类不能为空")
    private String category;
    
    @NotBlank(message = "文章内容不能为空")
    private String content;
    
    private String coverImage;
    
    private String tags;
}
