package com.fitness.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.dto.request.ArticleRequest;
import com.fitness.dto.response.ArticleResponse;
import com.fitness.entity.ArticleInteraction;
import com.fitness.entity.FitnessArticle;
import com.fitness.entity.User;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.ArticleInteractionMapper;
import com.fitness.mapper.FitnessArticleMapper;
import com.fitness.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文章服务类
 */
@Service
@RequiredArgsConstructor
public class ArticleService {
    
    private final FitnessArticleMapper articleMapper;
    private final ArticleInteractionMapper interactionMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    
    /**
     * 创建文章（教练）
     */
    @Transactional(rollbackFor = Exception.class)
    public void createArticle(ArticleRequest request) {
        Long coachId = StpUtil.getLoginIdAsLong();
        
        FitnessArticle article = new FitnessArticle();
        BeanUtils.copyProperties(request, article);
        
        // 生成文章编号
        String articleNo = "ART" + System.currentTimeMillis();
        article.setArticleNo(articleNo);
        article.setAuthorId(coachId);
        article.setPublishStatus("DRAFT");
        article.setAuditStatus("PENDING");
        article.setIsTop(0);
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCollectCount(0);
        
        articleMapper.insert(article);
    }
    
    /**
     * 发布文章（提交审核）
     */
    @Transactional(rollbackFor = Exception.class)
    public void publishArticle(Long articleId) {
        Long coachId = StpUtil.getLoginIdAsLong();
        
        FitnessArticle article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        
        if (!article.getAuthorId().equals(coachId)) {
            throw new BusinessException("无权操作此文章");
        }
        
        if (!"DRAFT".equals(article.getPublishStatus())) {
            throw new BusinessException("只能提交草稿状态的文章");
        }
        
        // 提交审核，等待管理员审核通过后才能发布
        article.setAuditStatus("PENDING");
        articleMapper.updateById(article);
    }
    
    /**
     * 更新文章
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateArticle(Long articleId, ArticleRequest request) {
        Long coachId = StpUtil.getLoginIdAsLong();
        
        FitnessArticle article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        
        if (!article.getAuthorId().equals(coachId)) {
            throw new BusinessException("无权操作此文章");
        }
        
        // 如果是已发布的文章，不允许编辑
        if ("PUBLISHED".equals(article.getPublishStatus())) {
            throw new BusinessException("已发布的文章不能编辑");
        }
        
        BeanUtils.copyProperties(request, article);
        
        // 如果是被拒绝的文章，编辑后重新设置为待审核
        if ("REJECTED".equals(article.getAuditStatus())) {
            article.setAuditStatus("PENDING");
            article.setAuditRemark(null);
            article.setAuditAdminId(null);
            article.setAuditTime(null);
        }
        
        articleMapper.updateById(article);
    }
    
    /**
     * 删除文章
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticle(Long articleId) {
        Long coachId = StpUtil.getLoginIdAsLong();
        
        FitnessArticle article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        
        if (!article.getAuthorId().equals(coachId)) {
            throw new BusinessException("无权操作此文章");
        }
        
        articleMapper.deleteById(articleId);
    }
    
    /**
     * 查询文章列表（分页）
     */
    public Page<ArticleResponse> getArticleList(int pageNum, int pageSize, String category, String keyword) {
        Page<FitnessArticle> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<FitnessArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FitnessArticle::getPublishStatus, "PUBLISHED")
               .eq(FitnessArticle::getAuditStatus, "APPROVED");
        
        if (category != null && !category.isEmpty()) {
            wrapper.eq(FitnessArticle::getCategory, category);
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(FitnessArticle::getTitle, keyword)
                             .or()
                             .like(FitnessArticle::getContent, keyword));
        }
        
        wrapper.orderByDesc(FitnessArticle::getIsTop)
               .orderByDesc(FitnessArticle::getPublishTime);
        
        Page<FitnessArticle> articlePage = articleMapper.selectPage(page, wrapper);
        
        Page<ArticleResponse> responsePage = new Page<>(pageNum, pageSize);
        responsePage.setTotal(articlePage.getTotal());
        
        List<ArticleResponse> records = articlePage.getRecords().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
        
        responsePage.setRecords(records);
        return responsePage;
    }
    
    /**
     * 查询文章详情
     */
    @Transactional(rollbackFor = Exception.class)
    public ArticleResponse getArticleDetail(Long articleId) {
        FitnessArticle article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        
        // 增加浏览量
        article.setViewCount(article.getViewCount() + 1);
        articleMapper.updateById(article);
        
        // 记录浏览记录
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            recordInteraction(articleId, userId, "VIEW");
        } catch (Exception e) {
            // 未登录用户不记录
        }
        
        return convertToResponse(article);
    }
    
    /**
     * 点赞文章
     */
    @Transactional(rollbackFor = Exception.class)
    public void likeArticle(Long articleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 检查是否已点赞
        LambdaQueryWrapper<ArticleInteraction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleInteraction::getArticleId, articleId)
               .eq(ArticleInteraction::getUserId, userId)
               .eq(ArticleInteraction::getInteractionType, "LIKE");
        
        Long count = interactionMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("已经点赞过了");
        }
        
        // 记录点赞
        recordInteraction(articleId, userId, "LIKE");
        
        // 更新点赞数
        FitnessArticle article = articleMapper.selectById(articleId);
        article.setLikeCount(article.getLikeCount() + 1);
        articleMapper.updateById(article);
    }
    
    /**
     * 取消点赞
     */
    @Transactional(rollbackFor = Exception.class)
    public void unlikeArticle(Long articleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        LambdaQueryWrapper<ArticleInteraction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleInteraction::getArticleId, articleId)
               .eq(ArticleInteraction::getUserId, userId)
               .eq(ArticleInteraction::getInteractionType, "LIKE");
        
        interactionMapper.delete(wrapper);
        
        // 更新点赞数
        FitnessArticle article = articleMapper.selectById(articleId);
        if (article.getLikeCount() > 0) {
            article.setLikeCount(article.getLikeCount() - 1);
            articleMapper.updateById(article);
        }
    }
    
    /**
     * 收藏文章
     */
    @Transactional(rollbackFor = Exception.class)
    public void collectArticle(Long articleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 检查是否已收藏
        LambdaQueryWrapper<ArticleInteraction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleInteraction::getArticleId, articleId)
               .eq(ArticleInteraction::getUserId, userId)
               .eq(ArticleInteraction::getInteractionType, "COLLECT");
        
        Long count = interactionMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("已经收藏过了");
        }
        
        // 记录收藏
        recordInteraction(articleId, userId, "COLLECT");
        
        // 更新收藏数
        FitnessArticle article = articleMapper.selectById(articleId);
        article.setCollectCount(article.getCollectCount() + 1);
        articleMapper.updateById(article);
    }
    
    /**
     * 取消收藏
     */
    @Transactional(rollbackFor = Exception.class)
    public void uncollectArticle(Long articleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        LambdaQueryWrapper<ArticleInteraction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleInteraction::getArticleId, articleId)
               .eq(ArticleInteraction::getUserId, userId)
               .eq(ArticleInteraction::getInteractionType, "COLLECT");
        
        interactionMapper.delete(wrapper);
        
        // 更新收藏数
        FitnessArticle article = articleMapper.selectById(articleId);
        if (article.getCollectCount() > 0) {
            article.setCollectCount(article.getCollectCount() - 1);
            articleMapper.updateById(article);
        }
    }
    
    /**
     * 查询我的收藏
     */
    public Page<ArticleResponse> getMyCollections(int pageNum, int pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 查询收藏的文章ID列表
        LambdaQueryWrapper<ArticleInteraction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleInteraction::getUserId, userId)
               .eq(ArticleInteraction::getInteractionType, "COLLECT");
        
        List<Long> articleIds = interactionMapper.selectList(wrapper).stream()
            .map(ArticleInteraction::getArticleId)
            .collect(Collectors.toList());
        
        if (articleIds.isEmpty()) {
            return new Page<>(pageNum, pageSize);
        }
        
        Page<FitnessArticle> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FitnessArticle> articleWrapper = new LambdaQueryWrapper<>();
        articleWrapper.in(FitnessArticle::getArticleId, articleIds)
                     .orderByDesc(FitnessArticle::getPublishTime);
        
        Page<FitnessArticle> articlePage = articleMapper.selectPage(page, articleWrapper);
        
        Page<ArticleResponse> responsePage = new Page<>(pageNum, pageSize);
        responsePage.setTotal(articlePage.getTotal());
        
        List<ArticleResponse> records = articlePage.getRecords().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
        
        responsePage.setRecords(records);
        return responsePage;
    }
    
    /**
     * 查询我的文章（教练）
     */
    public Page<ArticleResponse> getMyArticles(int pageNum, int pageSize) {
        Long coachId = StpUtil.getLoginIdAsLong();
        
        Page<FitnessArticle> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FitnessArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FitnessArticle::getAuthorId, coachId)
               .orderByDesc(FitnessArticle::getCreatedAt);
        
        Page<FitnessArticle> articlePage = articleMapper.selectPage(page, wrapper);
        
        Page<ArticleResponse> responsePage = new Page<>(pageNum, pageSize);
        responsePage.setTotal(articlePage.getTotal());
        
        List<ArticleResponse> records = articlePage.getRecords().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
        
        responsePage.setRecords(records);
        return responsePage;
    }
    
    /**
     * 查询待审核文章列表（管理员）
     */
    public Page<ArticleResponse> getPendingArticles(int pageNum, int pageSize) {
        Page<FitnessArticle> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FitnessArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FitnessArticle::getAuditStatus, "PENDING")
               .eq(FitnessArticle::getPublishStatus, "PUBLISHED")
               .orderByAsc(FitnessArticle::getPublishTime);
        
        Page<FitnessArticle> articlePage = articleMapper.selectPage(page, wrapper);
        
        Page<ArticleResponse> responsePage = new Page<>(pageNum, pageSize);
        responsePage.setTotal(articlePage.getTotal());
        
        List<ArticleResponse> records = articlePage.getRecords().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
        
        responsePage.setRecords(records);
        return responsePage;
    }
    
    /**
     * 查询所有文章列表（管理员）
     */
    public Page<ArticleResponse> getAllArticles(int pageNum, int pageSize, String auditStatus) {
        Page<FitnessArticle> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FitnessArticle> wrapper = new LambdaQueryWrapper<>();
        
        if (auditStatus != null && !auditStatus.isEmpty()) {
            wrapper.eq(FitnessArticle::getAuditStatus, auditStatus);
        }
        
        wrapper.orderByDesc(FitnessArticle::getCreatedAt);
        
        Page<FitnessArticle> articlePage = articleMapper.selectPage(page, wrapper);
        
        Page<ArticleResponse> responsePage = new Page<>(pageNum, pageSize);
        responsePage.setTotal(articlePage.getTotal());
        
        List<ArticleResponse> records = articlePage.getRecords().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
        
        responsePage.setRecords(records);
        return responsePage;
    }
    
    /**
     * 审核文章（管理员）
     */
    @Transactional(rollbackFor = Exception.class)
    public void auditArticle(Long articleId, String auditStatus, String auditRemark) {
        Long adminId = StpUtil.getLoginIdAsLong();
        
        FitnessArticle article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        
        if (!"PENDING".equals(article.getAuditStatus())) {
            throw new BusinessException("只能审核待审核状态的文章");
        }
        
        article.setAuditStatus(auditStatus);
        article.setAuditAdminId(adminId);
        article.setAuditTime(LocalDateTime.now());
        article.setAuditRemark(auditRemark);
        
        // 如果审核通过，发布文章
        if ("APPROVED".equals(auditStatus)) {
            article.setPublishStatus("PUBLISHED");
            article.setPublishTime(LocalDateTime.now());
        } else {
            // 如果审核拒绝，保持草稿状态，让教练可以重新编辑
            article.setPublishStatus("DRAFT");
        }
        
        articleMapper.updateById(article);
        
        // 发送通知给教练
        String notificationContent = "APPROVED".equals(auditStatus) 
            ? "您的文章《" + article.getTitle() + "》已通过审核并发布" 
            : "您的文章《" + article.getTitle() + "》审核未通过，原因：" + auditRemark + "。请修改后重新提交";
        
        notificationService.sendNotification(
            article.getAuthorId(),
            adminId,
            "SYSTEM",
            "文章审核通知",
            notificationContent,
            articleId
        );
    }
    
    /**
     * 记录互动
     */
    private void recordInteraction(Long articleId, Long userId, String type) {
        ArticleInteraction interaction = new ArticleInteraction();
        interaction.setArticleId(articleId);
        interaction.setUserId(userId);
        interaction.setInteractionType(type);
        interactionMapper.insert(interaction);
    }
    
    /**
     * 转换为响应DTO
     */
    private ArticleResponse convertToResponse(FitnessArticle article) {
        ArticleResponse response = new ArticleResponse();
        BeanUtils.copyProperties(article, response);
        
        // 查询作者信息
        User author = userMapper.selectById(article.getAuthorId());
        if (author != null) {
            response.setAuthorName(author.getName());
        }
        
        // 查询当前用户的互动状态
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            LambdaQueryWrapper<ArticleInteraction> likeWrapper = new LambdaQueryWrapper<>();
            likeWrapper.eq(ArticleInteraction::getArticleId, article.getArticleId())
                      .eq(ArticleInteraction::getUserId, userId)
                      .eq(ArticleInteraction::getInteractionType, "LIKE");
            response.setIsLiked(interactionMapper.selectCount(likeWrapper) > 0);
            
            LambdaQueryWrapper<ArticleInteraction> collectWrapper = new LambdaQueryWrapper<>();
            collectWrapper.eq(ArticleInteraction::getArticleId, article.getArticleId())
                         .eq(ArticleInteraction::getUserId, userId)
                         .eq(ArticleInteraction::getInteractionType, "COLLECT");
            response.setIsCollected(interactionMapper.selectCount(collectWrapper) > 0);
        } catch (Exception e) {
            response.setIsLiked(false);
            response.setIsCollected(false);
        }
        
        return response;
    }
}
