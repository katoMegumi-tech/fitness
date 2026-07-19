<template>
  <div class="fitness-plans">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的健身计划</span>
          <el-radio-group v-model="filterStatus" @change="loadData">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="PENDING">待确认</el-radio-button>
            <el-radio-button label="ACTIVE">进行中</el-radio-button>
            <el-radio-button label="COMPLETED">已完成</el-radio-button>
          </el-radio-group>
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
        <el-table-column prop="userConfirmStatus" label="确认状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.userConfirmStatus" :type="getConfirmType(row.userConfirmStatus)">
              {{ getConfirmText(row.userConfirmStatus) }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">
              查看详情
            </el-button>
            <el-button 
              v-if="row.auditStatus === 'APPROVED' && row.userConfirmStatus === 'PENDING'"
              type="success" 
              size="small" 
              @click="handleConfirm(row)">
              开始跟练
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
        <el-descriptions-item label="开始时间">
          {{ formatDateTime(currentPlan.planStartTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="getAuditType(currentPlan.auditStatus)">
            {{ getAuditText(currentPlan.auditStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="确认状态" v-if="currentPlan.userConfirmStatus">
          <el-tag :type="getConfirmType(currentPlan.userConfirmStatus)">
            {{ getConfirmText(currentPlan.userConfirmStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="计划状态">
          <el-tag :type="getStatusType(currentPlan.planStatus)">
            {{ getStatusText(currentPlan.planStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="运动计划" :span="2">
          <div style="white-space: pre-wrap; word-break: break-all; overflow-wrap: anywhere; max-height: 300px; overflow-y: auto; padding: 10px; background: #f5f7fa; border-radius: 4px">
            {{ currentPlan.exercisePlan || '暂无' }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="饮食计划" :span="2">
          <div style="white-space: pre-wrap; word-break: break-all; overflow-wrap: anywhere; max-height: 300px; overflow-y: auto; padding: 10px; background: #f5f7fa; border-radius: 4px">
            {{ currentPlan.dietPlan || '暂无' }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item :label="currentPlan.parentPlanId ? '教练点评' : '计划说明'" :span="2" v-if="currentPlan.adjustmentReason">
          <div style="white-space: pre-wrap; word-break: break-all; overflow-wrap: anywhere; padding: 10px; background: #fff3cd; border-radius: 4px">
            {{ currentPlan.adjustmentReason }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="审核意见" :span="2" v-if="currentPlan.auditRemark">
          <div style="white-space: pre-wrap; word-break: break-all; overflow-wrap: anywhere; padding: 10px; background: #e7f5ff; border-radius: 4px">
            {{ currentPlan.auditRemark }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="拒绝理由" :span="2" v-if="currentPlan.userRejectReason">
          <div style="white-space: pre-wrap; word-break: break-all; overflow-wrap: anywhere; padding: 10px; background: #ffe7e7; border-radius: 4px">
            {{ currentPlan.userRejectReason }}
          </div>
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <div style="display: flex; justify-content: space-between">
          <el-button @click="detailVisible = false">关闭</el-button>
          <div>
            <el-button 
              v-if="currentPlan && currentPlan.planStatus === 'ACTIVE'"
              type="primary" 
              @click="goToCheckIn">
              打卡
            </el-button>
            <el-button 
              v-if="currentPlan && currentPlan.auditStatus === 'APPROVED' && currentPlan.userConfirmStatus === 'PENDING'"
              type="success" 
              @click="handleConfirm(currentPlan)">
              开始跟练
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- 确认对话框 -->
    <el-dialog v-model="confirmVisible" title="开始跟练" width="500px">
      <el-form :model="confirmForm" label-width="100px">
        <el-form-item label="确认结果">
          <el-radio-group v-model="confirmForm.confirmStatus">
            <el-radio label="APPROVED">开始跟练</el-radio>
            <el-radio label="REJECTED">暂不跟练</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="拒绝理由" v-if="confirmForm.confirmStatus === 'REJECTED'">
          <el-input 
            v-model="confirmForm.rejectReason" 
            type="textarea" 
            :rows="3"
            placeholder="请输入拒绝理由"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="confirmVisible = false">取消</el-button>
        <el-button type="primary" @click="submitConfirm">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserPlans, confirmPlan } from '@/api/plan'
import { getImageUrl } from '@/utils/image'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const detailVisible = ref(false)
const confirmVisible = ref(false)
const currentPlan = ref(null)
const filterStatus = ref('')

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const confirmForm = reactive({
  planId: null,
  confirmStatus: 'APPROVED',
  rejectReason: ''
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserPlans({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      planStatus: filterStatus.value || undefined
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

// 跳转到打卡页面
const goToCheckIn = () => {
  detailVisible.value = false
  router.push('/user/checkin')
}

// 确认计划
const handleConfirm = (row) => {
  confirmForm.planId = row.planId
  confirmForm.confirmStatus = 'APPROVED'
  confirmForm.rejectReason = ''
  confirmVisible.value = true
  detailVisible.value = false
}

// 提交确认
const submitConfirm = async () => {
  if (confirmForm.confirmStatus === 'REJECTED' && !confirmForm.rejectReason) {
    ElMessage.warning('请输入拒绝理由')
    return
  }

  try {
    await confirmPlan(confirmForm.planId, {
      confirmStatus: confirmForm.confirmStatus,
      rejectReason: confirmForm.rejectReason
    })
    ElMessage.success('确认成功')
    confirmVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '确认失败')
  }
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}
</style>
