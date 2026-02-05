package com.fitness.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.entity.Notification;
import com.fitness.entity.User;
import com.fitness.mapper.NotificationMapper;
import com.fitness.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知服务类
 */
@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    
    /**
     * 发送通知
     */
    @Transactional(rollbackFor = Exception.class)
    public void sendNotification(Long receiverId, Long senderId, String type, 
                                 String title, String content, Long relatedId) {
        Notification notification = new Notification();
        notification.setReceiverId(receiverId);
        notification.setSenderId(senderId);
        notification.setNotificationType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setIsRead(0);
        
        notificationMapper.insert(notification);
    }
    
    /**
     * 查询我的通知列表
     */
    public Page<Notification> getMyNotifications(int pageNum, int pageSize, Integer isRead) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        Page<Notification> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getReceiverId, userId);
        
        if (isRead != null) {
            wrapper.eq(Notification::getIsRead, isRead);
        }
        
        wrapper.orderByDesc(Notification::getCreatedAt);
        
        return notificationMapper.selectPage(page, wrapper);
    }
    
    /**
     * 标记为已读
     */
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long notificationId) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification != null && notification.getReceiverId().equals(userId)) {
            notification.setIsRead(1);
            notification.setReadTime(LocalDateTime.now());
            notificationMapper.updateById(notification);
        }
    }
    
    /**
     * 全部标记为已读
     */
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead() {
        Long userId = StpUtil.getLoginIdAsLong();
        
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getReceiverId, userId)
               .eq(Notification::getIsRead, 0);
        
        Notification update = new Notification();
        update.setIsRead(1);
        update.setReadTime(LocalDateTime.now());
        
        notificationMapper.update(update, wrapper);
    }
    
    /**
     * 获取未读数量
     */
    public Map<String, Object> getUnreadCount() {
        Long userId = StpUtil.getLoginIdAsLong();
        
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getReceiverId, userId)
               .eq(Notification::getIsRead, 0);
        
        Long count = notificationMapper.selectCount(wrapper);
        
        Map<String, Object> result = new HashMap<>();
        result.put("unreadCount", count);
        return result;
    }
    
    /**
     * 删除通知
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotification(Long notificationId) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification != null && notification.getReceiverId().equals(userId)) {
            notificationMapper.deleteById(notificationId);
        }
    }
    
    /**
     * 批量发送通知（管理员）
     */
    @Transactional(rollbackFor = Exception.class)
    public void sendBatchNotification(String type, String title, String content, String targetRole) {
        // 查询目标用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if ("ALL".equals(targetRole)) {
            // 发送给所有用户
            wrapper.ne(User::getRole, "ADMIN");
        } else {
            // 发送给指定角色
            wrapper.eq(User::getRole, targetRole);
        }
        
        List<User> users = userMapper.selectList(wrapper);
        
        // 批量发送通知
        for (User user : users) {
            sendNotification(user.getUserId(), null, type, title, content, null);
        }
    }
}
