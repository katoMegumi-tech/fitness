/**
 * 图片URL处理工具
 */

// 后端服务器地址（包含 context-path）
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

/**
 * 获取图片完整URL
 * @param {string} path - 图片路径
 * @returns {string} 完整的图片URL
 */
export function getImageUrl(path) {
  if (!path) return ''
  
  // 如果已经是完整URL，直接返回
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path
  }
  
  // 确保路径以 / 开头
  const normalizedPath = path.startsWith('/') ? path : '/' + path
  
  // 返回后端服务器地址 + 路径
  return API_BASE_URL + normalizedPath
}

/**
 * 获取多个图片URL
 * @param {string|Array} paths - 图片路径或路径数组（可以是逗号分隔的字符串）
 * @returns {Array} 图片URL数组
 */
export function getImageUrls(paths) {
  if (!paths) return []
  
  // 如果是字符串，按逗号分割
  const pathArray = typeof paths === 'string' ? paths.split(',') : paths
  
  // 转换每个路径
  return pathArray.map(path => getImageUrl(path.trim())).filter(url => url)
}
