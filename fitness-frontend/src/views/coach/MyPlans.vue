<template>
  <div class="my-plans">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的计划</span>
          <el-radio-group v-model="filterStatus" @change="loadData">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="PENDING">待审核</el-radio-button>
            <el-radio-button label="APPROVED">已通过</el-radio-button>
            <el-radio-button label="REJECTED">已拒绝</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <!-- 计划列表 -->
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="planNo" label="计划编号" width="180" />
        <el-table-column prop="planName" label="计划名称" width="150" />
        <el-table-column prop="userName" label="学员" width="120" />
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
        <el-table-column prop="version" label="版本" width="80">
          <template #default="{ row }">
            V{{ row.version }}
          </template>
        </el-table-column>
        <el-table-column prop="auditStatus" label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getAuditType(row.auditStatus)">
              {{ getAuditText(row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userConfirmStatus" label="学员确认" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.userConfirmStatus" :type="getConfirmType(row.userConfirmStatus)">
              {{ getConfirmText(row.userConfirmStatus) }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="planStatus" label="计划状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.planStatus)">
              {{ getStatusText(row.planStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">
              查看详情
            </el-button>
            <el-button
              v-if="row.auditStatus === 'REJECTED'"
              type="danger"
              plain
              size="small"
              @click="handleView(row)"
            >
              拒绝原因
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
    <el-dialog v-model="detailVisible" title="计划详情" width="900px">
      <div
        v-if="currentPlan"
        style="display: flex; align-items: center; gap: 16px; margin-bottom: 16px; padding: 12px 16px; background: #f5f7fa; border-radius: 8px;"
      >
        <el-avatar :size="56" :src="currentPlan.coachAvatarUrl">
          {{ currentPlan.coachName?.charAt(0) }}
        </el-avatar>
        <div>
          <div style="font-size: 14px; color: #909399;">教练</div>
          <div style="font-size: 18px; font-weight: 600;">{{ currentPlan.coachName || '未分配' }}</div>
        </div>
      </div>
      <el-descriptions :column="2" border v-if="currentPlan">
        <el-descriptions-item label="计划编号" :span="2">
          {{ currentPlan.planNo }}
        </el-descriptions-item>
        <el-descriptions-item label="计划名称">
          {{ currentPlan.planName }}
        </el-descriptions-item>
        <el-descriptions-item label="版本">
          V{{ currentPlan.version }}
        </el-descriptions-item>
        <el-descriptions-item label="学员">
          {{ currentPlan.userName }}
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
        <el-descriptions-item label="开始时间">
          {{ formatDateTime(currentPlan.planStartTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="getAuditType(currentPlan.auditStatus)">
            {{ getAuditText(currentPlan.auditStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="学员确认" v-if="currentPlan.userConfirmStatus">
          <el-tag :type="getConfirmType(currentPlan.userConfirmStatus)">
            {{ getConfirmText(currentPlan.userConfirmStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="计划状态">
          <el-tag :type="getStatusType(currentPlan.planStatus)">
            {{ getStatusText(currentPlan.planStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDateTime(currentPlan.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item label="运动计划" :span="2">
          <div style="white-space: pre-wrap; max-height: 300px; overflow-y: auto; padding: 10px; background: #f5f7fa; border-radius: 4px">
            {{ currentPlan.exercisePlan || '暂无' }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="饮食计划" :span="2">
          <div style="white-space: pre-wrap; max-height: 300px; overflow-y: auto; padding: 10px; background: #f5f7fa; border-radius: 4px">
            {{ currentPlan.dietPlan || '暂无' }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="调整原因" :span="2" v-if="currentPlan.adjustmentReason">
          <div style="white-space: pre-wrap; padding: 10px; background: #fff3cd; border-radius: 4px">
            {{ currentPlan.adjustmentReason }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="审核意见" :span="2" v-if="currentPlan.auditRemark">
          <div style="white-space: pre-wrap; padding: 10px; background: #e7f5ff; border-radius: 4px">
            {{ currentPlan.auditRemark }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="学员拒绝理由" :span="2" v-if="currentPlan.userRejectReason">
          <div style="white-space: pre-wrap; padding: 10px; background: #ffe7e7; border-radius: 4px">
            {{ currentPlan.userRejectReason }}
          </div>
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
import { getCoachPlans } from '@/api/plan'
import { getImageUrl } from '@/utils/image'

const loading = ref(false)
const tableData = ref([])
const detailVisible = ref(false)
const currentPlan = ref(null)
const filterStatus = ref('')

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getCoachPlans({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      auditStatus: filterStatus.value || undefined
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
  currentPlan.value = {
    ...row,
    coachAvatarUrl: row.coachAvatar ? getImageUrl(row.coachAvatar) : ''
  }
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

// 获取确认状态文本
const getConfirmText = (status) => {
  const map = {
    'PENDING': '待确认',
    'APPROVED': '已同意',
    'REJECTED': '已拒绝'
  }
  return map[status] || status
}

// 获取确认状态类型
const getConfirmType = (status) => {
  const map = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  }
  return map[status] || ''
}

// 获取计划状态文本
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

// 获取计划状态类型
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
.my-plans {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}
</style>
