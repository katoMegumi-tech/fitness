<template>
  <div class="my-coach">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>我的教练</span>
        </div>
      </template>

      <!-- 未绑定教练 -->
      <el-empty v-if="!currentCoach" description="您还没有绑定教练">
        <el-button type="primary" @click="goToCoachList">选择教练</el-button>
      </el-empty>

      <!-- 已绑定教练 -->
      <div v-else class="coach-info-container">
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="coach-avatar-section">
              <el-avatar :size="120" :src="currentCoach.avatar">
                {{ currentCoach.coachName?.charAt(0) }}
              </el-avatar>
              <h2>{{ currentCoach.coachName }}</h2>
              <div class="coach-rating">
                <el-rate
                  :model-value="Number(currentCoach.rating) || 5"
                  disabled
                  show-score
                  text-color="#ff9900"
                />
              </div>
            </div>
          </el-col>

          <el-col :span="16">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="擅长领域" :span="2">
                <el-tag>{{ currentCoach.specialties }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="从业年限">
                {{ currentCoach.yearsOfExperience }}年
              </el-descriptions-item>
              <el-descriptions-item label="服务学员">
                {{ currentCoach.totalStudents }}人
              </el-descriptions-item>
              <el-descriptions-item label="联系电话">
                {{ currentCoach.coachPhone || '未提供' }}
              </el-descriptions-item>
              <el-descriptions-item label="邮箱">
                {{ currentCoach.coachEmail || '未提供' }}
              </el-descriptions-item>
              <el-descriptions-item label="绑定时间" :span="2">
                {{ formatTime(currentCoach.bindTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="个人简介" :span="2">
                {{ currentCoach.introduction || '暂无简介' }}
              </el-descriptions-item>
            </el-descriptions>

            <div style="margin-top: 20px">
              <el-button type="danger" @click="handleUnbind">解除绑定</el-button>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCurrentBinding, unbind } from '@/api/binding'
import { getCoachProfile } from '@/api/coach'

const router = useRouter()
const loading = ref(false)
const currentCoach = ref(null)

// 加载当前绑定的教练
const loadCurrentCoach = async () => {
  loading.value = true
  try {
    const res = await getCurrentBinding()
    if (res.data) {
      // 获取教练详细信息
      const coachRes = await getCoachProfile(res.data.coachUserId)
      currentCoach.value = {
        ...res.data,
        ...coachRes.data
      }
    }
  } catch (error) {
    console.error('加载教练信息失败', error)
  } finally {
    loading.value = false
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 19)
}

// 跳转到教练列表
const goToCoachList = () => {
  router.push('/user/coach-list')
}

// 解除绑定
const handleUnbind = () => {
  ElMessageBox.prompt('请输入解绑理由（选填）', '解除绑定', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputType: 'textarea',
    confirmButtonClass: 'el-button--danger'
  }).then(async ({ value }) => {
    try {
      await unbind({ unbindReason: value || '' })
      ElMessage.success('已解除绑定')
      currentCoach.value = null
    } catch (error) {
      ElMessage.error(error.message || '解绑失败')
    }
  }).catch(() => {
    // 取消操作
  })
}

onMounted(() => {
  loadCurrentCoach()
})
</script>

<style scoped>
.my-coach {
  padding: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.coach-info-container {
  padding: 20px;
}

.coach-avatar-section {
  text-align: center;
}

.coach-avatar-section h2 {
  margin: 15px 0 10px;
  font-size: 24px;
}

.coach-rating {
  margin: 10px 0;
}
</style>
