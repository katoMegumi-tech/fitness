import request from '@/utils/request'

/**
 * 用户发起绑定申请
 */
export function applyBinding(data) {
  return request({
    url: '/binding/apply',
    method: 'post',
    data
  })
}

/**
 * 教练处理绑定申请
 */
export function handleBinding(applyId, data) {
  return request({
    url: `/binding/handle/${applyId}`,
    method: 'post',
    data
  })
}

/**
 * 解绑
 */
export function unbind(params) {
  return request({
    url: '/binding/unbind',
    method: 'post',
    params
  })
}

/**
 * 查询用户的绑定申请列表
 */
export function getUserApplies(params) {
  return request({
    url: '/binding/user/applies',
    method: 'get',
    params
  })
}

/**
 * 查询教练收到的绑定申请列表
 */
export function getCoachApplies(params) {
  return request({
    url: '/binding/coach/applies',
    method: 'get',
    params
  })
}

/**
 * 查询用户当前的绑定关系
 */
export function getCurrentBinding() {
  return request({
    url: '/binding/current',
    method: 'get'
  })
}

/**
 * 查询教练的学员列表
 */
export function getCoachStudents(params) {
  return request({
    url: '/binding/coach/students',
    method: 'get',
    params
  })
}
