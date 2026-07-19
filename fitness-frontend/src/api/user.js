import request from '@/utils/request'

/**
 * 获取当前用户信息
 */
export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

/**
 * 更新用户信息
 */
export function updateUserInfo(data) {
  return request({
    url: '/user/info',
    method: 'put',
    data
  })
}

/**
 * 保存或更新用户身体数据
 */
export function saveBodyData(data) {
  return request({
    url: '/user/body-data',
    method: 'post',
    data
  })
}

/**
 * 查询当前用户的身体基础数据
 */
export function getBodyData() {
  return request({
    url: '/user/body-data',
    method: 'get'
  })
}

/**
 * 查询用户身体数据历史记录
 */
export function getBodyHistory(params) {
  return request({
    url: '/user/body-history',
    method: 'get',
    params
  })
}

/**
 * 根据用户ID查询身体数据（教练查看学员数据）
 */
export function getUserBodyData(userId) {
  return request({
    url: `/user/${userId}/body-data`,
    method: 'get'
  })
}

/**
 * 根据用户ID查询身体数据历史（教练查看学员数据）
 */
export function getUserBodyHistory(userId, params) {
  return request({
    url: `/user/${userId}/body-history`,
    method: 'get',
    params
  })
}
