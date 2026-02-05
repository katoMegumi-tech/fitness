<template>
  <div class="coach-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>选择教练</span>
        </div>
      </template>

      <!-- 教练列表 -->
      <el-row :gutter="20" v-loading="loading">
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
              <el-avatar :size="80" :src="coach.avatar">
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
              <p class="coach-intro">{{ coach.introduction }}</p>
            </div>
            <div class="coach-actions">
              <el-button type="primary" size="small" @click="handleSelect(coach)">
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
    <el-dialog v-model="detailVisible" title="教练详情" width="600px">
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

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleSelect(currentCoach)">
          选择该教练
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAvailableCoaches } from '@/api/coach'
import { applyBinding } from '@/api/binding'

const loading = ref(false)
const coachList = ref([])
const detailVisible = ref(false)
const currentCoach = ref(null)

const pagination = reactive({
  pageNum: 1,
  pageSize: 8,
  total: 0
})

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
      rating: Number(coach.rating) || 5
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

// 选择教练
const handleSelect = (coach) => {
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
      // 刷新列表，因为申请后可能会影响可选教练
      loadData()
    } catch (error) {
      ElMessage.error(error.message || '申请失败')
    }
  }).catch(() => {
    // 取消操作
  })
}

onMounted(() => {
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
</style>
