import request from '@/utils/request'

/**
 * 教练创建健身计划
 */
export function createPlan(data) {
  return request({
    url: '/plan',
    method: 'post',
    data
  })
}

/**
 * 教练调整健身计划
 */
export function adjustPlan(planId, data) {
  return request({
    url: `/plan/${planId}/adjust`,
    method: 'post',
    data
  })
}

/**
 * 管理员审核健身计划
 */
export function auditPlan(planId, data) {
  return request({
    url: `/plan/${planId}/audit`,
    method: 'post',
    data
  })
}

/**
 * 查询用户的健身计划列表
 */
export function getUserPlans(params) {
  return request({
    url: '/plan/user',
    method: 'get',
    params
  })
}

/**
 * 查询教练创建的计划列表
 */
export function getCoachPlans(params) {
  return request({
    url: '/plan/coach',
    method: 'get',
    params
  })
}

/**
 * 查询待审核的计划列表（管理员）
 */
export function getPendingPlans(params) {
  return request({
    url: '/plan/pending',
    method: 'get',
    params
  })
}

/**
 * 根据ID查询计划详情
 */
export function getPlanById(planId) {
  return request({
    url: `/plan/${planId}`,
    method: 'get'
  })
}

/**
 * 用户确认健身计划
 */
export function confirmPlan(planId, data) {
  return request({
    url: `/plan/${planId}/confirm`,
    method: 'post',
    params: data
  })
}

/**
 * 检查用户是否有激活的计划
 */
export function checkActivePlan() {
  return request({
    url: '/plan/check-active',
    method: 'get'
  })
}
