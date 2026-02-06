import request from '@/utils/request'

/**
 * 提交打卡
 */
export function submitCheckIn(data) {
  return request({
    url: '/api/checkin/submit',
    method: 'post',
    data
  })
}

/**
 * 查询我的打卡历史
 */
export function getMyCheckInHistory(params) {
  return request({
    url: '/api/checkin/my-history',
    method: 'get',
    params
  })
}

/**
 * 查询学员的打卡记录（教练端）
 */
export function getStudentCheckIns(studentId, params) {
  return request({
    url: `/api/checkin/student/${studentId}`,
    method: 'get',
    params
  })
}

/**
 * 教练点评打卡记录
 */
export function addCoachComment(checkInId, comment) {
  return request({
    url: `/api/checkin/comment/${checkInId}`,
    method: 'post',
    data: { comment }
  })
}

/**
 * 获取身体数据趋势
 */
export function getBodyDataTrend(days = 30) {
  return request({
    url: '/api/checkin/trend',
    method: 'get',
    params: { days }
  })
}

/**
 * 获取打卡统计数据
 */
export function getCheckInStats(days = 30) {
  return request({
    url: '/api/checkin/stats',
    method: 'get',
    params: { days }
  })
}

/**
 * 查询待审核的打卡列表（教练端）
 */
export function getPendingCheckIns(params) {
  return request({
    url: '/api/checkin/pending',
    method: 'get',
    params
  })
}

/**
 * 审核打卡（教练端）
 */
export function reviewCheckIn(checkInId, data) {
  return request({
    url: `/api/checkin/comment/${checkInId}`,
    method: 'post',
    data
  })
}
