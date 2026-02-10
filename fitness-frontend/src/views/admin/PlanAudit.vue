<template>
  <div class="plan-audit">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>健身计划审核</span>
          <el-radio-group v-model="filterStatus" @change="loadData">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="PENDING">待审核</el-radio-button>
            <el-radio-button label="APPROVED">已通过</el-radio-button>
            <el-radio-button label="REJECTED">已拒绝</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <!-- 待审核列表 -->
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="planNo" label="计划编号" width="180" />
        <el-table-column prop="planName" label="计划名称" width="150" />
        <el-table-column prop="userName" label="学员" width="120" />
        <el-table-column prop="coachName" label="教练" width="120" />
        <el-table-column prop="fitnessGoal" label="健身目标" width="100" />
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
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">
              查看详情
            </el-button>
            <el-button 
              v-if="row.auditStatus === 'PENDING'"
              type="success" 
              size="small" 
              @click="handleAudit(row, 'APPROVED')">
              通过
            </el-button>
            <el-button 
              v-if="row.auditStatus === 'PENDING'"
              type="danger" 
              size="small" 
              @click="handleAudit(row, 'REJECTED')">
              拒绝
            </el-button>
            <el-button 
              v-if="row.auditStatus === 'APPROVED' || row.auditStatus === 'REJECTED'"
              type="warning" 
              size="small" 
              @click="handleRevoke(row)">
              撤销
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
        <el-descriptions-item label="学员">
          {{ currentPlan.userName }}
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
        <el-button type="success" @click="handleAudit(currentPlan, 'APPROVED')">
          通过审核
        </el-button>
        <el-button type="danger" @click="handleAudit(currentPlan, 'REJECTED')">
          拒绝审核
        </el-button>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog v-model="auditVisible" :title="auditTitle" width="500px">
      <el-form :model="auditForm" label-width="100px">
        <el-form-item label="审核意见">
          <el-input
            v-model="auditForm.auditOpinion"
            type="textarea"
            :rows="4"
            placeholder="请输入审核意见（选填）"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAudit" :loading="auditLoading">
          确认
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPendingPlans, auditPlan } from '@/api/plan'

const loading = ref(false)
const auditLoading = ref(false)
const tableData = ref([])
const detailVisible = ref(false)
const auditVisible = ref(false)
const currentPlan = ref(null)
const auditTitle = ref('')
const filterStatus = ref('')

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const auditForm = reactive({
  auditStatus: '',
  auditOpinion: ''
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getPendingPlans({
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
  currentPlan.value = row
  detailVisible.value = true
}

// 审核操作
const handleAudit = (row, status) => {
  currentPlan.value = row
  auditForm.auditStatus = status
  auditForm.auditOpinion = ''
  auditTitle.value = status === 'APPROVED' ? '通过审核' : '拒绝审核'
  auditVisible.value = true
  detailVisible.value = false
}

// 确认审核
const confirmAudit = async () => {
  auditLoading.value = true
  try {
    await auditPlan(currentPlan.value.planId, auditForm)
    ElMessage.success('审核完成')
    auditVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '审核失败')
  } finally {
    auditLoading.value = false
  }
}

// 撤销审核
const handleRevoke = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要撤销该计划的审核吗？撤销后状态将变为待审核。',
      '撤销确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用撤销接口（需要后端支持）
    await auditPlan(row.planId, { auditStatus: 'PENDING', auditOpinion: '撤销审核' })
    ElMessage.success('审核已撤销')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '撤销失败')
    }
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
.plan-audit {
  padding: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}
</style>
