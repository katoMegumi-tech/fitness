<template>
  <div class="my-coach">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>我的教练</span>
        </div>
      </template>

      <!-- 未绑定教练 -->
      <div v-if="!currentCoach">
        <!-- 显示最近被拒绝的申请 -->
        <el-alert
          v-if="latestRejectedApply"
          type="warning"
          :closable="false"
          style="margin-bottom: 20px"
        >
          <template #title>
            <div style="font-size: 16px; font-weight: bold">申请被拒绝</div>
          </template>
          <div style="margin-top: 10px">
            <p style="margin: 5px 0">
              <strong>{{ latestRejectedApply.coachName }}</strong> 教练拒绝了您的申请
            </p>
            <p style="margin: 5px 0; color: #909399">
              拒绝时间：{{ formatTime(latestRejectedApply.handleTime) }}
            </p>
            <p v-if="latestRejectedApply.rejectReason" style="margin: 5px 0">
              拒绝理由：{{ latestRejectedApply.rejectReason }}
            </p>
          </div>
        </el-alert>

        <el-empty description="您还没有绑定教练">
          <el-button type="primary" @click="goToCoachList">选择教练</el-button>
        </el-empty>
      </div>

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
import { getCurrentBinding, unbind, getUserApplies } from '@/api/binding'
import { getCoachProfile } from '@/api/coach'
import { getImageUrl } from '@/utils/image'

const router = useRouter()
const loading = ref(false)
const currentCoach = ref(null)
const latestRejectedApply = ref(null)

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
        ...coachRes.data,
        avatar: coachRes.data?.avatar ? getImageUrl(coachRes.data.avatar) : ''
      }
    } else {
      // 如果没有绑定教练，查询最近被拒绝的申请
      await loadLatestRejectedApply()
    }
  } catch (error) {
    console.error('加载教练信息失败', error)
    // 如果没有绑定教练，查询最近被拒绝的申请
    await loadLatestRejectedApply()
  } finally {
    loading.value = false
  }
}

// 加载最近被拒绝的申请
const loadLatestRejectedApply = async () => {
  try {
    const res = await getUserApplies({
      pageNum: 1,
      pageSize: 10
    })
    if (res.data && res.data.records) {
      // 找到最近被拒绝的申请
      const rejectedApplies = res.data.records.filter(
        apply => apply.applyStatus === 'REJECTED'
      )
      if (rejectedApplies.length > 0) {
        // 按处理时间排序，取最新的
        rejectedApplies.sort((a, b) => {
          return new Date(b.handleTime) - new Date(a.handleTime)
        })
        latestRejectedApply.value = rejectedApplies[0]
      }
    }
  } catch (error) {
    console.error('加载申请记录失败', error)
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 19)
}

// 跳转到教练列表
const goToCoachList = () => {
  router.push('/user/coaches')
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
      // 解绑后重新加载，可能会显示被拒绝的申请
      await loadLatestRejectedApply()
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
