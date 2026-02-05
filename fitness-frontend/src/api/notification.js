import request from '@/utils/request'

/**
 * 查询我的通知列表
 */
export function getMyNotifications(params) {
  return request({
    url: '/notification/my-list',
    method: 'get',
    params
  })
}

/**
 * 标记为已读
 */
export function markAsRead(notificationId) {
  return request({
    url: `/notification/read/${notificationId}`,
    method: 'post'
  })
}

/**
 * 全部标记为已读
 */
export function markAllAsRead() {
  return request({
    url: '/notification/read-all',
    method: 'post'
  })
}

/**
 * 获取未读数量
 */
export function getUnreadCount() {
  return request({
    url: '/notification/unread-count',
    method: 'get'
  })
}

/**
 * 删除通知
 */
export function deleteNotification(notificationId) {
  return request({
    url: `/notification/delete/${notificationId}`,
    method: 'delete'
  })
}

/**
 * 发送通知（管理员）
 */
export function sendNotification(data) {
  return request({
    url: '/notification/send',
    method: 'post',
    data
  })
}

/**
 * 批量发送通知（管理员）
 */
export function sendBatchNotification(data) {
  return request({
    url: '/notification/send-batch',
    method: 'post',
    data
  })
}
