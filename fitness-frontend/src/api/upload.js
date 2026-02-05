import request from '@/utils/request'

/**
 * 上传单个文件
 */
export function uploadSingle(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/upload/single',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传多个文件
 */
export function uploadMultiple(files) {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  
  return request({
    url: '/upload/multiple',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传教练证书
 */
export function uploadCertificate(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/upload/coach/certificate',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传打卡图片
 */
export function uploadCheckinImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/upload/checkin/image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
