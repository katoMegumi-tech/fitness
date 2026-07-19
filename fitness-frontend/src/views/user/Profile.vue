<template>
  <div class="user-profile">
    <el-row :gutter="20">
      <!-- 左侧：个人信息和头像 -->
      <el-col :xs="24" :sm="24" :md="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>个人信息</span>
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
          </div>

          <el-descriptions :column="1" border style="margin-top: 20px">
            <el-descriptions-item label="姓名">{{ userInfo.name }}</el-descriptions-item>
            <el-descriptions-item label="账号">{{ userInfo.account }}</el-descriptions-item>
            <el-descriptions-item label="角色">
              <el-tag v-if="userInfo.role === 'USER'" type="success">普通用户</el-tag>
              <el-tag v-else-if="userInfo.role === 'COACH'" type="warning">教练</el-tag>
              <el-tag v-else-if="userInfo.role === 'ADMIN'" type="danger">管理员</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="手机">{{ userInfo.phone }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ userInfo.email }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <!-- 中间：身体数据表单 -->
      <el-col :xs="24" :sm="24" :md="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>我的身体数据</span>
            </div>
          </template>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-width="120px"
          >
            <el-form-item label="身高" prop="height">
              <el-input-number
                v-model="form.height"
                :min="0.5"
                :max="2.5"
                :step="0.01"
                :precision="2"
                placeholder="请输入身高（米）"
              />
              <span style="margin-left: 10px">米</span>
            </el-form-item>

            <el-form-item label="当前体重" prop="currentWeight">
              <el-input-number
                v-model="form.currentWeight"
                :min="20"
                :max="300"
                :step="0.1"
                :precision="1"
                placeholder="请输入当前体重"
              />
              <span style="margin-left: 10px">kg</span>
            </el-form-item>

            <el-form-item label="目标体重" prop="targetWeight">
              <el-input-number
                v-model="form.targetWeight"
                :min="20"
                :max="300"
                :step="0.1"
                :precision="1"
                placeholder="请输入目标体重"
              />
              <span style="margin-left: 10px">kg</span>
            </el-form-item>

            <el-form-item label="当前体脂率" prop="currentBodyFatRate">
              <el-input-number
                v-model="form.currentBodyFatRate"
                :min="5"
                :max="60"
                :step="0.1"
                :precision="1"
                placeholder="请输入当前体脂率"
              />
              <span style="margin-left: 10px">%</span>
            </el-form-item>

            <el-form-item label="目标体脂率" prop="targetBodyFatRate">
              <el-input-number
                v-model="form.targetBodyFatRate"
                :min="5"
                :max="60"
                :step="0.1"
                :precision="1"
                placeholder="请输入目标体脂率"
              />
              <span style="margin-left: 10px">%</span>
            </el-form-item>

            <el-form-item label="肌肉量" prop="muscleMass">
              <el-input-number
                v-model="form.muscleMass"
                :min="0"
                :max="100"
                :step="0.1"
                :precision="1"
                placeholder="请输入肌肉量（选填）"
              />
              <span style="margin-left: 10px">kg</span>
            </el-form-item>

            <el-form-item label="腰围" prop="waistline">
              <el-input-number
                v-model="form.waistline"
                :min="0"
                :max="200"
                :step="0.1"
                :precision="1"
                placeholder="请输入腰围（选填）"
              />
              <span style="margin-left: 10px">cm</span>
            </el-form-item>

            <el-form-item label="健身目标" prop="fitnessGoal">
              <el-select v-model="form.fitnessGoal" placeholder="请选择健身目标">
                <el-option label="减脂" value="减脂" />
                <el-option label="增肌" value="增肌" />
                <el-option label="塑形" value="塑形" />
                <el-option label="保持" value="保持" />
              </el-select>
            </el-form-item>

            <el-form-item label="运动频率" prop="exerciseFrequency">
              <el-input-number
                v-model="form.exerciseFrequency"
                :min="1"
                :max="7"
                placeholder="每周运动次数"
              />
              <span style="margin-left: 10px">次/周</span>
            </el-form-item>

            <el-form-item label="运动时长" prop="exerciseDuration">
              <el-input-number
                v-model="form.exerciseDuration"
                :min="10"
                :max="300"
                :step="10"
                placeholder="每次运动时长"
              />
              <span style="margin-left: 10px">分钟</span>
            </el-form-item>

            <el-form-item label="饮食偏好" prop="foodPreference">
              <el-input
                v-model="form.foodPreference"
                type="textarea"
                :rows="2"
                placeholder="如：喜欢清淡、不吃辣等"
              />
            </el-form-item>

            <el-form-item label="过敏源" prop="allergens">
              <el-input
                v-model="form.allergens"
                placeholder="如：海鲜、花生等"
              />
            </el-form-item>

            <el-form-item label="健康状况" prop="healthConditions">
              <el-input
                v-model="form.healthConditions"
                type="textarea"
                :rows="3"
                placeholder="请说明您的健康状况、疾病史、运动禁忌等"
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="submitForm" :loading="loading">
                保存数据
              </el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右侧：数据趋势图表 -->
      <el-col :xs="24" :sm="24" :md="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>数据趋势</span>
              <el-radio-group v-model="chartDays" size="small" @change="loadHistory">
                <el-radio-button :label="7">7天</el-radio-button>
                <el-radio-button :label="30">30天</el-radio-button>
                <el-radio-button :label="90">90天</el-radio-button>
              </el-radio-group>
            </div>
          </template>

          <div ref="chartRef" style="width: 100%; height: 400px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { saveBodyData, getBodyData, getBodyHistory, getUserInfo, updateUserInfo } from '@/api/user'
import { getImageUrl } from '@/utils/image'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const formRef = ref(null)
const chartRef = ref(null)
const loading = ref(false)
const chartDays = ref(30)
let chartInstance = null

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
  height: null,
  currentWeight: null,
  targetWeight: null,
  currentBodyFatRate: null,
  targetBodyFatRate: null,
  muscleMass: null,
  waistline: null,
  fitnessGoal: '',
  exerciseFrequency: null,
  exerciseDuration: null,
  foodPreference: '',
  allergens: '',
  healthConditions: ''
})

const rules = {
  height: [
    { required: true, message: '请输入身高', trigger: 'blur' }
  ],
  currentWeight: [
    { required: true, message: '请输入当前体重', trigger: 'blur' }
  ],
  targetWeight: [
    { required: true, message: '请输入目标体重', trigger: 'blur' }
  ],
  fitnessGoal: [
    { required: true, message: '请选择健身目标', trigger: 'change' }
  ]
}

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const res = await getUserInfo()
    userInfo.value = res.data
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

// 加载身体数据
const loadData = async () => {
  try {
    const res = await getBodyData()
    if (res.data) {
      Object.assign(form, res.data)
    }
  } catch (error) {
    console.error('加载数据失败', error)
  }
}

// 加载历史数据
const loadHistory = async () => {
  try {
    const res = await getBodyHistory({ days: chartDays.value })
    renderChart(res.data)
  } catch (error) {
    console.error('加载历史数据失败', error)
  }
}

// 渲染图表
const renderChart = (data) => {
  if (!chartRef.value) return

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  const dates = data.map(item => item.recordDate).reverse()
  const weights = data.map(item => item.weight).reverse()
  const bodyFatRates = data.map(item => item.bodyFatRate).reverse()

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['体重', '体脂率']
    },
    xAxis: {
      type: 'category',
      data: dates
    },
    yAxis: [
      {
        type: 'value',
        name: '体重(kg)',
        position: 'left'
      },
      {
        type: 'value',
        name: '体脂率(%)',
        position: 'right'
      }
    ],
    series: [
      {
        name: '体重',
        type: 'line',
        data: weights,
        smooth: true,
        yAxisIndex: 0
      },
      {
        name: '体脂率',
        type: 'line',
        data: bodyFatRates,
        smooth: true,
        yAxisIndex: 1
      }
    ]
  }

  chartInstance.setOption(option)
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await saveBodyData(form)
        ElMessage.success('保存成功')
        loadHistory()
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
  if (!formRef.value) return
  formRef.value.resetFields()
}

onMounted(async () => {
  await loadUserInfo()
  await loadData()
  await nextTick()
  await loadHistory()
})
</script>

<style scoped>
.user-profile {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
