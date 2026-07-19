<template>
  <div class="students-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的学员</span>
        </div>
      </template>

      <!-- 学员列表 -->
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="userId" label="学员ID" width="80" />
        <el-table-column prop="userName" label="姓名" width="120" />
        <el-table-column prop="userPhone" label="联系电话" width="130" />
        <el-table-column prop="userEmail" label="邮箱" width="180" />
        <el-table-column prop="bindTime" label="绑定时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.bindTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewData(row)">
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>

    <!-- 学员数据对话框 -->
    <el-dialog v-model="dataVisible" title="学员身体数据" width="900px">
      <div v-if="studentBodyData">
        <h3>基础数据</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="身高">{{ studentBodyData.height }}m</el-descriptions-item>
          <el-descriptions-item label="目标体重">{{ studentBodyData.targetWeight }}kg</el-descriptions-item>
          <el-descriptions-item label="目标体脂率">{{ studentBodyData.targetBodyFatRate }}%</el-descriptions-item>
          <el-descriptions-item label="健身目标">{{ studentBodyData.fitnessGoal }}</el-descriptions-item>
          <el-descriptions-item label="运动频率">{{ studentBodyData.exerciseFrequency }}次/周</el-descriptions-item>
          <el-descriptions-item label="运动时长">{{ studentBodyData.exerciseDuration }}分钟/次</el-descriptions-item>
          <el-descriptions-item label="饮食偏好" :span="2">{{ studentBodyData.foodPreference || '-' }}</el-descriptions-item>
          <el-descriptions-item label="过敏源" :span="2">{{ studentBodyData.allergens || '-' }}</el-descriptions-item>
          <el-descriptions-item label="健康状况" :span="2">{{ studentBodyData.healthConditions || '-' }}</el-descriptions-item>
        </el-descriptions>

        <h3 style="margin-top: 20px">历史数据（最近30天）</h3>
        <el-table :data="studentBodyHistory" border max-height="300">
          <el-table-column prop="recordDate" label="日期" width="120" />
          <el-table-column prop="weight" label="体重(kg)" width="100" />
          <el-table-column prop="bodyFatRate" label="体脂率(%)" width="100" />
          <el-table-column prop="muscleMass" label="肌肉量(kg)" width="100" />
          <el-table-column prop="bmi" label="BMI" width="80" />
          <el-table-column prop="waistline" label="腰围(cm)" width="100" />
          <el-table-column prop="recordSource" label="来源" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.recordSource === 'MANUAL'" size="small">手动录入</el-tag>
              <el-tag v-else-if="row.recordSource === 'CHECK_IN'" type="success" size="small">打卡记录</el-tag>
              <el-tag v-else type="info" size="small">{{ row.recordSource }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div v-else>
        <el-empty description="该学员暂未录入身体数据" />
      </div>
      <template #footer>
        <el-button @click="dataVisible = false">关闭</el-button>
        <el-button type="success" @click="handleMakePlan(currentStudent)" v-if="currentStudent">
          <el-icon style="margin-right: 4px"><EditPen /></el-icon>
          制定计划
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { EditPen } from '@element-plus/icons-vue'
import { getCoachStudents } from '@/api/binding'
import { getUserBodyData, getUserBodyHistory } from '@/api/user'

const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const dataVisible = ref(false)
const currentStudent = ref(null)
const studentBodyData = ref(null)
const studentBodyHistory = ref([])

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getCoachStudents({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 查看学员数据
const handleViewData = (row) => {
  currentStudent.value = row
  loadStudentData(row.userId)
  dataVisible.value = true
}

// 加载学员数据
const loadStudentData = async (userId) => {
  try {
    const [baseRes, historyRes] = await Promise.all([
      getUserBodyData(userId),
      getUserBodyHistory(userId, { days: 30 })
    ])
    studentBodyData.value = baseRes.data
    studentBodyHistory.value = historyRes.data
  } catch (error) {
    ElMessage.error('加载学员数据失败')
  }
}

// 制定计划
const handleMakePlan = (row) => {
  dataVisible.value = false
  router.push({
    path: '/coach/plan-editor',
    query: { 
      userId: row.userId,
      userName: row.userName,
      fromStudents: 'true'
    }
  })
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.students-management {
  padding: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}
</style>
