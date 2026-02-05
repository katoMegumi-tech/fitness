import request from '@/utils/request'

/**
 * 提交教练认证申请
 */
export function submitCertification(data) {
  return request({
    url: '/coach/certification',
    method: 'post',
    data
  })
}

/**
 * 查询当前教练的认证状态
 */
export function getCertificationStatus() {
  return request({
    url: '/coach/certification/status',
    method: 'get'
  })
}

/**
 * 根据ID查询教练资料
 */
export function getCoachProfile(coachId) {
  return request({
    url: `/coach/${coachId}`,
    method: 'get'
  })
}

/**
 * 查询已认证的教练列表
 */
export function getApprovedCoaches(params) {
  return request({
    url: '/coach/approved',
    method: 'get',
    params
  })
}

/**
 * 查询可选择的教练列表（排除已绑定的教练）
 */
export function getAvailableCoaches(params) {
  return request({
    url: '/coach/list/available',
    method: 'get',
    params
  })
}

/**
 * 查询待审核的教练列表（管理员）
 */
export function getPendingCoaches(params) {
  return request({
    url: '/coach/pending',
    method: 'get',
    params
  })
}

/**
 * 审核教练资质（管理员）
 */
export function auditCertification(coachUserId, data) {
  return request({
    url: `/coach/${coachUserId}/audit`,
    method: 'post',
    data
  })
}

/**
 * 查询教练的审核历史（管理员）
 */
export function getAuditHistory(coachUserId) {
  return request({
    url: `/coach/${coachUserId}/audit/history`,
    method: 'get'
  })
}

/**
 * 撤销教练审核（管理员）
 */
export function revokeCoachAudit(coachUserId) {
  return request({
    url: `/coach/${coachUserId}/audit/revoke`,
    method: 'post'
  })
}
