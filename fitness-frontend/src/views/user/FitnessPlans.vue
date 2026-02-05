<template>
  <div class="fitness-plans">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的健身计划</span>
        </div>
      </template>

      <!-- 计划列表 -->
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="planNo" label="计划编号" width="180" />
        <el-table-column prop="planName" label="计划名称" width="150" />
        <el-table-column prop="coachName" label="教练" width="120" />
        <el-table-column prop="fitnessGoal" label="健身目标" width="100" />
        <el-table-column prop="planDifficulty" label="难度" width="100">
          <template #default="{ row }">
            {{ getDifficultyText(row.planDifficulty) }}
          </template>
        </el-table-column>
        <el-table-column prop="planCycle" label="周期" width="80">
          <template #default="{ row }">
            {{ row.planCycle }}天
          </template>
        </el-table-column>
        <el-table-column prop="planStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.planStatus)">
              {{ getStatusText(row.planStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditStatus" label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getAuditType(row.auditStatus)">
              {{ getAuditText(row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">
              查看详情
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

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="计划详情" width="800px">
      <el-descriptions :column="2" border v-if="currentPlan">
        <el-descriptions-item label="计划编号" :span="2">
          {{ currentPlan.planNo }}
        </el-descriptions-item>
        <el-descriptions-item label="计划名称">
          {{ currentPlan.planName }}
        </el-descriptions-item>
        <el-descriptions-item label="教练">
          {{ currentPlan.coachName }}
        </el-descriptions-item>
        <el-descriptions-item label="健身目标">
          {{ currentPlan.fitnessGoal }}
        </el-descriptions-item>
        <el-descriptions-item label="难度">
          {{ getDifficultyText(currentPlan.planDifficulty) }}
        </el-descriptions-item>
        <el-descriptions-item label="周期">
          {{ currentPlan.planCycle }}天
        </el-descriptions-item>
        <el-descriptions-item label="版本">
          V{{ currentPlan.version }}
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">
          {{ formatDateTime(currentPlan.planStartTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="结束时间">
          {{ formatDateTime(currentPlan.planEndTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="运动计划" :span="2">
          <div style="white-space: pre-wrap">{{ currentPlan.exercisePlan }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="饮食计划" :span="2">
          <div style="white-space: pre-wrap">{{ currentPlan.dietPlan }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="调整原因" :span="2" v-if="currentPlan.adjustmentReason">
          {{ currentPlan.adjustmentReason }}
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserPlans } from '@/api/plan'

const loading = ref(false)
const tableData = ref([])
const detailVisible = ref(false)
const currentPlan = ref(null)

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserPlans({
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

// 查看详情
const handleView = (row) => {
  currentPlan.value = row
  detailVisible.value = true
}

// 获取难度文本
const getDifficultyText = (difficulty) => {
  const map = {
    'BEGINNER': '初级',
    'INTERMEDIATE': '中级',
    'ADVANCED': '高级'
  }
  return map[difficulty] || difficulty
}

// 获取状态文本
const getStatusText = (status) => {
  const map = {
    'DRAFT': '草稿',
    'PENDING': '待审核',
    'ACTIVE': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return map[status] || status
}

// 获取状态类型
const getStatusType = (status) => {
  const map = {
    'DRAFT': 'info',
    'PENDING': 'warning',
    'ACTIVE': 'success',
    'COMPLETED': '',
    'CANCELLED': 'danger'
  }
  return map[status] || ''
}

// 获取审核状态文本
const getAuditText = (status) => {
  const map = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  }
  return map[status] || status
}

// 获取审核状态类型
const getAuditType = (status) => {
  const map = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  }
  return map[status] || ''
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
.fitness-plans {
  padding: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}
</style>
