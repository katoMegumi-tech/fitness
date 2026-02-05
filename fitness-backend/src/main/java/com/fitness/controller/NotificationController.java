package com.fitness.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.Result;
import com.fitness.entity.Notification;
import com.fitness.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 通知控制器
 */
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    
    /**
     * 查询我的通知列表
     */
    @GetMapping("/my-list")
    @SaCheckLogin
    public Result<Page<Notification>> getMyNotifications(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer isRead) {
        Page<Notification> page = notificationService.getMyNotifications(pageNum, pageSize, isRead);
        return Result.success(page);
    }
    
    /**
     * 标记为已读
     */
    @PostMapping("/read/{notificationId}")
    @SaCheckLogin
    public Result<String> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return Result.success("已标记为已读");
    }
    
    /**
     * 全部标记为已读
     */
    @PostMapping("/read-all")
    @SaCheckLogin
    public Result<String> markAllAsRead() {
        notificationService.markAllAsRead();
        return Result.success("已全部标记为已读");
    }
    
    /**
     * 获取未读数量
     */
    @GetMapping("/unread-count")
    @SaCheckLogin
    public Result<Map<String, Object>> getUnreadCount() {
        Map<String, Object> result = notificationService.getUnreadCount();
        return Result.success(result);
    }
    
    /**
     * 删除通知
     */
    @DeleteMapping("/delete/{notificationId}")
    @SaCheckLogin
    public Result<String> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return Result.success("删除成功");
    }
    
    /**
     * 发送通知（管理员）
     */
    @PostMapping("/send")
    @SaCheckRole("ADMIN")
    public Result<String> sendNotification(@RequestBody Map<String, Object> request) {
        Long receiverId = Long.valueOf(request.get("receiverId").toString());
        String type = (String) request.get("notificationType");
        String title = (String) request.get("title");
        String content = (String) request.get("content");
        Long relatedId = request.get("relatedId") != null ? Long.valueOf(request.get("relatedId").toString()) : null;
        
        notificationService.sendNotification(receiverId, null, type, title, content, relatedId);
        return Result.success("通知发送成功");
    }
    
    /**
     * 批量发送通知（管理员）
     */
    @PostMapping("/send-batch")
    @SaCheckRole("ADMIN")
    public Result<String> sendBatchNotification(@RequestBody Map<String, Object> request) {
        String type = (String) request.get("notificationType");
        String title = (String) request.get("title");
        String content = (String) request.get("content");
        String targetRole = (String) request.get("targetRole"); // USER/COACH/ALL
        
        notificationService.sendBatchNotification(type, title, content, targetRole);
        return Result.success("批量通知发送成功");
    }
}
