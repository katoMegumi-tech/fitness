<template>
  <div class="certification-apply">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>教练认证申请</span>
        </div>
      </template>

      <!-- 认证状态显示 -->
      <el-alert
        v-if="certStatus"
        :title="getStatusText()"
        :type="getStatusType()"
        :closable="false"
        style="margin-bottom: 20px"
      />

      <el-alert
        v-if="auditOpinion"
        title="管理员审核意见"
        type="info"
        :description="auditOpinion"
        :closable="false"
        style="margin-bottom: 20px"
      />

      <!-- 认证表单 -->
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        :disabled="certStatus === 'PENDING'"
      >
        <el-form-item label="个人简介" prop="introduction">
          <el-input
            v-model="form.introduction"
            type="textarea"
            :rows="4"
            placeholder="请介绍您的教学经验、专业背景等"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="擅长领域" prop="specialties">
          <el-input
            v-model="form.specialties"
            placeholder="如：减脂、增肌、康复训练等"
            maxlength="200"
          />
        </el-form-item>

        <el-form-item label="从业年限" prop="yearsOfExperience">
          <el-input-number
            v-model="form.yearsOfExperience"
            :min="0"
            :max="50"
            placeholder="请输入从业年限"
          />
        </el-form-item>

        <el-form-item label="资格证书" prop="certImages">
          <div class="cert-upload">
            <el-upload
              v-model:file-list="fileList"
              action="#"
              list-type="picture-card"
              :auto-upload="false"
              :on-change="handleFileChange"
              :on-remove="handleFileRemove"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">
              请上传您的健身教练资格证书、相关培训证书等（支持jpg、png格式）
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            @click="submitForm"
            :loading="loading"
            :disabled="certStatus === 'PENDING'"
          >
            {{ certStatus === 'APPROVED' ? '更新认证信息' : '提交认证申请' }}
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { submitCertification, getCertificationStatus } from '@/api/coach'
import { uploadCertificate } from '@/api/upload'

const formRef = ref(null)
const loading = ref(false)
const certStatus = ref(null)
const fileList = ref([])
const auditOpinion = ref('')

const form = reactive({
  introduction: '',
  specialties: '',
  yearsOfExperience: 0,
  certImages: []
})

const rules = {
  introduction: [
    { required: true, message: '请输入个人简介', trigger: 'blur' },
    { min: 20, message: '个人简介至少20个字符', trigger: 'blur' }
  ],
  specialties: [
    { required: true, message: '请输入擅长领域', trigger: 'blur' }
  ],
  yearsOfExperience: [
    { required: true, message: '请输入从业年限', trigger: 'blur' }
  ],
  certImages: [
    { required: true, message: '请上传至少一张资格证书', trigger: 'change' }
  ]
}

// 获取认证状态
const loadCertStatus = async () => {
  try {
    const res = await getCertificationStatus()
    certStatus.value = res.data.certificationStatus
    auditOpinion.value = res.data.auditOpinion || ''
    
    // 如果已有认证信息，回显数据
    if (res.data.certificationStatus !== 'NOT_APPLIED') {
      form.introduction = res.data.introduction || ''
      form.specialties = res.data.specialties || ''
      form.yearsOfExperience = res.data.yearsOfExperience || 0
      
      if (res.data.certImages && res.data.certImages.length > 0) {
        fileList.value = res.data.certImages.map((cert, index) => ({
          uid: index,
          name: cert.name,
          url: getImageUrl(cert.path)
        }))
        form.certImages = res.data.certImages
      }
    }
  } catch (error) {
    console.error('获取认证状态失败', error)
  }
}

// 获取图片完整URL
const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path
  }
  return 'http://localhost:8080/api' + path
}

// 文件变化处理
const handleFileChange = async (file) => {
  try {
    // 调用上传接口
    const res = await uploadCertificate(file.raw)
    const certImage = {
      name: file.name,
      path: res.data
    }
    form.certImages.push(certImage)
    ElMessage.success('证书上传成功')
  } catch (error) {
    ElMessage.error('证书上传失败：' + (error.message || '未知错误'))
    // 移除上传失败的文件
    const index = fileList.value.findIndex(f => f.uid === file.uid)
    if (index > -1) {
      fileList.value.splice(index, 1)
    }
  }
}

// 文件移除处理
const handleFileRemove = (file) => {
  const index = form.certImages.findIndex(cert => cert.name === file.name)
  if (index > -1) {
    form.certImages.splice(index, 1)
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (form.certImages.length === 0) {
        ElMessage.warning('请至少上传一张资格证书')
        return
      }
      
      loading.value = true
      try {
        await submitCertification(form)
        if (certStatus.value === 'APPROVED') {
          ElMessage.success('认证信息已更新，请等待管理员重新审核')
        } else {
          ElMessage.success('认证申请已提交，请等待管理员审核')
        }
        certStatus.value = 'PENDING'
        loadCertStatus()
      } catch (error) {
        ElMessage.error(error.message || '提交失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  if (!formRef.value) return
  formRef.value.resetFields()
  fileList.value = []
  form.certImages = []
}

// 获取状态文本
const getStatusText = () => {
  const statusMap = {
    'PENDING': '您的认证申请正在审核中，请耐心等待',
    'APPROVED': '恭喜！您的认证申请已通过。如需更新认证信息，可以修改后重新提交',
    'REJECTED': '很抱歉，您的认证申请未通过，请修改后重新提交'
  }
  return statusMap[certStatus.value] || ''
}

// 获取状态类型
const getStatusType = () => {
  const typeMap = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'error'
  }
  return typeMap[certStatus.value] || 'info'
}

onMounted(() => {
  loadCertStatus()
})
</script>

<style scoped>
.certification-apply {
  padding: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.cert-upload {
  width: 100%;
}

.upload-tip {
  margin-top: 10px;
  color: #909399;
  font-size: 12px;
}
</style>
