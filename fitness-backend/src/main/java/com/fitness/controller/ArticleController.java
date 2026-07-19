package com.fitness.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.Result;
import com.fitness.dto.request.ArticleRequest;
import com.fitness.dto.response.ArticleResponse;
import com.fitness.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 文章控制器
 */
@RestController
@RequestMapping("/article")
@RequiredArgsConstructor
public class ArticleController {
    
    private final ArticleService articleService;
    
    /**
     * 创建文章（教练）
     */
    @PostMapping("/create")
    @SaCheckRole("COACH")
    public Result<String> createArticle(@Validated @RequestBody ArticleRequest request) {
        articleService.createArticle(request);
        return Result.success("文章创建成功");
    }
    
    /**
     * 发布文章（教练）
     */
    @PostMapping("/publish/{articleId}")
    @SaCheckRole("COACH")
    public Result<String> publishArticle(@PathVariable Long articleId) {
        articleService.publishArticle(articleId);
        return Result.success("文章发布成功");
    }
    
    /**
     * 更新文章（教练）
     */
    @PutMapping("/update/{articleId}")
    @SaCheckRole("COACH")
    public Result<String> updateArticle(
            @PathVariable Long articleId,
            @Validated @RequestBody ArticleRequest request) {
        articleService.updateArticle(articleId, request);
        return Result.success("文章更新成功");
    }
    
    /**
     * 删除文章（教练）
     */
    @DeleteMapping("/delete/{articleId}")
    @SaCheckRole("COACH")
    public Result<String> deleteArticle(@PathVariable Long articleId) {
        articleService.deleteArticle(articleId);
        return Result.success("文章删除成功");
    }
    
    /**
     * 查询文章列表
     */
    @GetMapping("/list")
    public Result<Page<ArticleResponse>> getArticleList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        Page<ArticleResponse> page = articleService.getArticleList(pageNum, pageSize, category, keyword);
        return Result.success(page);
    }
    
    /**
     * 查询文章详情
     */
    @GetMapping("/detail/{articleId}")
    public Result<ArticleResponse> getArticleDetail(@PathVariable Long articleId) {
        ArticleResponse response = articleService.getArticleDetail(articleId);
        return Result.success(response);
    }
    
    /**
     * 点赞文章
     */
    @PostMapping("/like/{articleId}")
    @SaCheckRole({"USER", "COACH"})
    public Result<String> likeArticle(@PathVariable Long articleId) {
        articleService.likeArticle(articleId);
        return Result.success("点赞成功");
    }
    
    /**
     * 取消点赞
     */
    @PostMapping("/unlike/{articleId}")
    @SaCheckRole({"USER", "COACH"})
    public Result<String> unlikeArticle(@PathVariable Long articleId) {
        articleService.unlikeArticle(articleId);
        return Result.success("取消点赞成功");
    }
    
    /**
     * 收藏文章
     */
    @PostMapping("/collect/{articleId}")
    @SaCheckRole({"USER", "COACH"})
    public Result<String> collectArticle(@PathVariable Long articleId) {
        articleService.collectArticle(articleId);
        return Result.success("收藏成功");
    }
    
    /**
     * 取消收藏
     */
    @PostMapping("/uncollect/{articleId}")
    @SaCheckRole({"USER", "COACH"})
    public Result<String> uncollectArticle(@PathVariable Long articleId) {
        articleService.uncollectArticle(articleId);
        return Result.success("取消收藏成功");
    }
    
    /**
     * 查询我的收藏
     */
    @GetMapping("/my-collections")
    @SaCheckRole({"USER", "COACH"})
    public Result<Page<ArticleResponse>> getMyCollections(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<ArticleResponse> page = articleService.getMyCollections(pageNum, pageSize);
        return Result.success(page);
    }
    
    /**
     * 查询我的文章（教练）
     */
    @GetMapping("/my-articles")
    @SaCheckRole("COACH")
    public Result<Page<ArticleResponse>> getMyArticles(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String auditStatus) {
        Page<ArticleResponse> page = articleService.getMyArticles(pageNum, pageSize, auditStatus);
        return Result.success(page);
    }
    
    /**
     * 查询待审核文章列表（管理员）
     */
    @GetMapping("/admin/pending")
    @SaCheckRole("ADMIN")
    public Result<Page<ArticleResponse>> getPendingArticles(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<ArticleResponse> page = articleService.getPendingArticles(pageNum, pageSize);
        return Result.success(page);
    }
    
    /**
     * 查询所有文章列表（管理员）
     */
    @GetMapping("/admin/all")
    @SaCheckRole("ADMIN")
    public Result<Page<ArticleResponse>> getAllArticles(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String auditStatus) {
        Page<ArticleResponse> page = articleService.getAllArticles(pageNum, pageSize, auditStatus);
        return Result.success(page);
    }
    
    /**
     * 审核文章（管理员）
     */
    @PostMapping("/admin/audit/{articleId}")
    @SaCheckRole("ADMIN")
    public Result<String> auditArticle(
            @PathVariable Long articleId,
            @RequestParam String auditStatus,
            @RequestParam(required = false) String auditRemark) {
        articleService.auditArticle(articleId, auditStatus, auditRemark);
        return Result.success("审核完成");
    }
}
