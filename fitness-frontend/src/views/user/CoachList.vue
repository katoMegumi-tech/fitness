<template>
  <div class="coach-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>选择教练</span>
        </div>
      </template>

      <!-- 已绑定提示 -->
      <el-alert
        v-if="hasBinding"
        title="您已绑定教练"
        type="info"
        :description="`您当前已绑定教练：${currentBinding?.coachName}，如需更换教练，请先在【我的教练】页面解绑。`"
        :closable="false"
        style="margin-bottom: 20px"
      />

      <!-- 教练列表 -->
      <el-row :gutter="20" v-loading="loading" :class="{ 'disabled-list': hasBinding }">
        <el-col
          v-for="coach in coachList"
          :key="coach.userId"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <el-card class="coach-card" shadow="hover">
            <div class="coach-avatar">
              <el-avatar :size="80" :src="coach.avatarUrl">
                {{ coach.name.charAt(0) }}
              </el-avatar>
            </div>
            <div class="coach-info">
              <h3>{{ coach.name }}</h3>
              <div class="coach-rating">
                <el-rate
                  v-model="coach.rating"
                  disabled
                  show-score
                  text-color="#ff9900"
                />
              </div>
              <p class="coach-specialties">
                <el-tag size="small">{{ coach.specialties }}</el-tag>
              </p>
              <p class="coach-experience">
                从业{{ coach.yearsOfExperience }}年 | 服务{{ coach.totalStudents }}人
              </p>
              <p class="coach-certs" v-if="coach.certImages && coach.certImages.length > 0">
                <el-icon><Medal /></el-icon>
                <span>{{ coach.certImages.length }}个资质证书</span>
              </p>
              <p class="coach-intro">{{ coach.introduction }}</p>
            </div>
            <div class="coach-actions">
              <el-button 
                type="primary" 
                size="small" 
                @click="handleSelect(coach)"
                :disabled="hasBinding"
              >
                选择教练
              </el-button>
              <el-button size="small" @click="handleViewDetail(coach)">
                查看详情
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[8, 16, 24]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>

    <!-- 教练详情对话框 -->
    <el-dialog v-model="detailVisible" title="教练详情" width="700px">
      <el-descriptions :column="2" border v-if="currentCoach">
        <el-descriptions-item label="姓名">{{ currentCoach.name }}</el-descriptions-item>
        <el-descriptions-item label="评分">
          <el-rate v-model="currentCoach.rating" disabled />
        </el-descriptions-item>
        <el-descriptions-item label="擅长领域" :span="2">
          {{ currentCoach.specialties }}
        </el-descriptions-item>
        <el-descriptions-item label="从业年限">
          {{ currentCoach.yearsOfExperience }}年
        </el-descriptions-item>
        <el-descriptions-item label="服务学员">
          {{ currentCoach.totalStudents }}人
        </el-descriptions-item>
        <el-descriptions-item label="个人简介" :span="2">
          {{ currentCoach.introduction }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 资质证书展示 -->
      <div v-if="currentCoach && currentCoach.certImages && currentCoach.certImages.length > 0" style="margin-top: 20px">
        <el-divider content-position="left">资质证书</el-divider>
        <el-row :gutter="10">
          <el-col 
            v-for="(cert, index) in currentCoach.certImages" 
            :key="index"
            :xs="24" 
            :sm="12" 
            :md="8"
          >
            <el-card shadow="hover" :body-style="{ padding: '10px' }" style="margin-bottom: 10px">
              <el-image
                :src="getCertImageUrl(cert.path)"
                :preview-src-list="currentCoach.certImages.map(c => getCertImageUrl(c.path))"
                :initial-index="index"
                fit="cover"
                style="width: 100%; height: 150px; cursor: pointer"
              >
                <template #error>
                  <div class="image-slot">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div style="padding: 5px 0; text-align: center; font-size: 12px; color: #909399">
                {{ cert.name }}
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button 
          type="primary" 
          @click="handleSelect(currentCoach)"
          :disabled="hasBinding"
        >
          选择该教练
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, Medal } from '@element-plus/icons-vue'
import { getAvailableCoaches } from '@/api/coach'
import { applyBinding, getCurrentBinding } from '@/api/binding'
import { getImageUrl } from '@/utils/image'

const loading = ref(false)
const coachList = ref([])
const detailVisible = ref(false)
const currentCoach = ref(null)
const hasBinding = ref(false)
const currentBinding = ref(null)

const pagination = reactive({
  pageNum: 1,
  pageSize: 8,
  total: 0
})

// 获取教练头像URL
const getCoachAvatar = (avatar) => {
  return avatar ? getImageUrl(avatar) : ''
}

// 获取证书图片URL
const getCertImageUrl = (path) => {
  return path ? getImageUrl(path) : ''
}

// 加载教练列表
const loadData = async () => {
  loading.value = true
  try {
    const res = await getAvailableCoaches({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    coachList.value = res.data.records.map(coach => ({
      ...coach,
      rating: Number(coach.rating) || 5,
      avatarUrl: getCoachAvatar(coach.avatar)
    }))
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载教练列表失败')
  } finally {
    loading.value = false
  }
}

// 查看详情
const handleViewDetail = (coach) => {
  currentCoach.value = coach
  detailVisible.value = true
}

// 检查当前绑定状态
const checkBindingStatus = async () => {
  try {
    const res = await getCurrentBinding()
    if (res.data) {
      hasBinding.value = true
      currentBinding.value = res.data
    } else {
      hasBinding.value = false
      currentBinding.value = null
    }
  } catch (error) {
    // 如果没有绑定，接口可能返回错误，这是正常的
    hasBinding.value = false
    currentBinding.value = null
  }
}

// 选择教练
const handleSelect = (coach) => {
  if (hasBinding.value) {
    ElMessage.warning('您已绑定教练，请先解绑后再申请')
    return
  }
  
  ElMessageBox.prompt('请输入申请理由（选填）', '申请绑定教练', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputType: 'textarea'
  }).then(async ({ value }) => {
    try {
      await applyBinding({
        coachUserId: coach.userId,
        applyReason: value || ''
      })
      ElMessage.success('申请已提交，请等待教练处理')
      detailVisible.value = false
      // 刷新绑定状态
      await checkBindingStatus()
    } catch (error) {
      ElMessage.error(error.message || '申请失败')
    }
  }).catch(() => {
    // 取消操作
  })
}

onMounted(async () => {
  await checkBindingStatus()
  loadData()
})
</script>

<style scoped>
.coach-list {
  padding: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.coach-card {
  margin-bottom: 20px;
  text-align: center;
}

.coach-avatar {
  margin-bottom: 15px;
}

.coach-info h3 {
  margin: 10px 0;
  font-size: 18px;
}

.coach-rating {
  margin: 10px 0;
}

.coach-specialties {
  margin: 10px 0;
}

.coach-experience {
  color: #909399;
  font-size: 12px;
  margin: 5px 0;
}

.coach-certs {
  color: #67c23a;
  font-size: 12px;
  margin: 5px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}

.coach-intro {
  color: #606266;
  font-size: 14px;
  margin: 10px 0;
  text-align: left;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.coach-actions {
  margin-top: 15px;
}

.disabled-list {
  opacity: 0.6;
  pointer-events: none;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 30px;
}
</style>
