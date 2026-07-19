<template>
  <div class="admin-profile">
    <el-row :gutter="20" justify="center">
      <el-col :xs="24" :sm="20" :md="16" :lg="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>个人资料</span>
            </div>
          </template>

          <div class="avatar-section">
            <el-avatar :size="120" :src="avatarUrl">
              {{ userInfo.name?.charAt(0) }}
            </el-avatar>
            <el-upload
              class="avatar-uploader"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :on-error="handleAvatarError"
              :before-upload="beforeAvatarUpload"
            >
              <el-button type="primary" size="small" style="margin-top: 10px">
                上传头像
              </el-button>
            </el-upload>
            <el-text type="info" size="small" style="margin-top: 8px">
              支持 JPG、PNG 格式，文件大小不超过 2MB
            </el-text>
          </div>

          <el-descriptions :column="1" border style="margin-top: 30px">
            <el-descriptions-item label="姓名">{{ userInfo.name }}</el-descriptions-item>
            <el-descriptions-item label="账号">{{ userInfo.account }}</el-descriptions-item>
            <el-descriptions-item label="角色">
              <el-tag type="danger">管理员</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="手机">{{ userInfo.phone || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ userInfo.email || '未设置' }}</el-descriptions-item>
          </el-descriptions>

          <el-divider />

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-width="100px"
          >
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="submitForm" :loading="loading">
                保存修改
              </el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserInfo, updateUserInfo } from '@/api/user'
import { getImageUrl } from '@/utils/image'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const userInfo = ref({
  name: '',
  account: '',
  role: '',
  phone: '',
  email: '',
  avatar: ''
})

const uploadUrl = computed(() => {
  return '/api/upload/single'
})

const uploadHeaders = computed(() => {
  return {
    'satoken': userStore.token
  }
})

const avatarUrl = computed(() => {
  return userInfo.value.avatar ? getImageUrl(userInfo.value.avatar) : ''
})

const form = reactive({
  phone: '',
  email: ''
})

const rules = {
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const res = await getUserInfo()
    userInfo.value = res.data
    form.phone = res.data.phone || ''
    form.email = res.data.email || ''
  } catch (error) {
    console.error('加载用户信息失败', error)
  }
}

// 头像上传前验证
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 头像上传成功
const handleAvatarSuccess = async (response) => {
  if (response.code === 200) {
    try {
      await updateUserInfo({ avatar: response.data })
      userInfo.value.avatar = response.data
      ElMessage.success('头像上传成功')
      // 更新store中的用户信息
      userStore.setUserInfo({ ...userStore.userInfo, avatar: response.data })
    } catch (error) {
      ElMessage.error('更新头像失败')
    }
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleAvatarError = () => {
  ElMessage.error('头像上传失败')
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await updateUserInfo(form)
        ElMessage.success('保存成功')
        await loadUserInfo()
      } catch (error) {
        ElMessage.error(error.message || '保存失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  form.phone = userInfo.value.phone || ''
  form.email = userInfo.value.email || ''
}

onMounted(async () => {
  await loadUserInfo()
})
</script>

<style scoped>
.admin-profile {
  padding: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.avatar-uploader {
  margin-top: 10px;
}
</style>
