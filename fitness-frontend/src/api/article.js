import request from '@/utils/request'

/**
 * 创建文章
 */
export function createArticle(data) {
  return request({
    url: '/article/create',
    method: 'post',
    data
  })
}

/**
 * 发布文章
 */
export function publishArticle(articleId) {
  return request({
    url: `/article/publish/${articleId}`,
    method: 'post'
  })
}

/**
 * 更新文章
 */
export function updateArticle(articleId, data) {
  return request({
    url: `/article/update/${articleId}`,
    method: 'put',
    data
  })
}

/**
 * 删除文章
 */
export function deleteArticle(articleId) {
  return request({
    url: `/article/delete/${articleId}`,
    method: 'delete'
  })
}

/**
 * 查询文章列表
 */
export function getArticleList(params) {
  return request({
    url: '/article/list',
    method: 'get',
    params
  })
}

/**
 * 查询文章详情
 */
export function getArticleDetail(articleId) {
  return request({
    url: `/article/detail/${articleId}`,
    method: 'get'
  })
}

/**
 * 点赞文章
 */
export function likeArticle(articleId) {
  return request({
    url: `/article/like/${articleId}`,
    method: 'post'
  })
}

/**
 * 取消点赞
 */
export function unlikeArticle(articleId) {
  return request({
    url: `/article/unlike/${articleId}`,
    method: 'post'
  })
}

/**
 * 收藏文章
 */
export function collectArticle(articleId) {
  return request({
    url: `/article/collect/${articleId}`,
    method: 'post'
  })
}

/**
 * 取消收藏
 */
export function uncollectArticle(articleId) {
  return request({
    url: `/article/uncollect/${articleId}`,
    method: 'post'
  })
}

/**
 * 查询我的收藏
 */
export function getMyCollections(params) {
  return request({
    url: '/article/my-collections',
    method: 'get',
    params
  })
}

/**
 * 查询我的文章（教练）
 */
export function getMyArticles(params) {
  return request({
    url: '/article/my-articles',
    method: 'get',
    params
  })
}

/**
 * 查询待审核文章列表（管理员）
 */
export function getPendingArticles(params) {
  return request({
    url: '/article/admin/pending',
    method: 'get',
    params
  })
}

/**
 * 查询所有文章列表（管理员）
 */
export function getAllArticles(params) {
  return request({
    url: '/article/admin/all',
    method: 'get',
    params
  })
}

/**
 * 审核文章（管理员）
 */
export function auditArticle(articleId, auditStatus, auditRemark) {
  return request({
    url: `/article/admin/audit/${articleId}`,
    method: 'post',
    params: {
      auditStatus,
      auditRemark
    }
  })
}
