<template>
  <div class="bind-applies">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>绑定申请</span>
          <el-radio-group v-model="statusFilter" size="small" @change="loadData">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="PENDING">待处理</el-radio-button>
            <el-radio-button label="APPROVED">已通过</el-radio-button>
            <el-radio-button label="REJECTED">已拒绝</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <!-- 申请列表 -->
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="applyId" label="申请ID" width="80" />
        <el-table-column prop="userName" label="申请人" width="120" />
        <el-table-column prop="applyReason" label="申请理由" show-overflow-tooltip />
        <el-table-column prop="applyStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.applyStatus)">
              {{ getStatusText(row.applyStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.applyTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="拒绝理由" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">
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

    <!-- 查看申请详情对话框 -->
    <el-dialog v-model="detailVisible" title="申请详情" width="700px">
      <div v-if="currentApply">
        <!-- 申请信息 -->
        <el-descriptions title="申请信息" :column="2" border>
          <el-descriptions-item label="申请人">{{ currentApply.userName }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">
            {{ formatDateTime(currentApply.applyTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="申请状态" :span="2">
            <el-tag :type="getStatusType(currentApply.applyStatus)">
              {{ getStatusText(currentApply.applyStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请理由" :span="2">
            {{ currentApply.applyReason || '无' }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentApply.rejectReason" label="拒绝理由" :span="2">
            {{ currentApply.rejectReason }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 申请人身体数据 -->
        <el-divider content-position="left">申请人身体数据</el-divider>
        <div v-loading="bodyDataLoading">
          <el-descriptions v-if="bodyData" :column="2" border>
            <el-descriptions-item label="身高">
              {{ bodyData.height ? bodyData.height + ' 米' : '未填写' }}
            </el-descriptions-item>
            <el-descriptions-item label="当前体重">
              {{ bodyData.currentWeight ? bodyData.currentWeight + ' kg' : '未填写' }}
            </el-descriptions-item>
            <el-descriptions-item label="目标体重">
              {{ bodyData.targetWeight ? bodyData.targetWeight + ' kg' : '未填写' }}
            </el-descriptions-item>
            <el-descriptions-item label="当前体脂率">
              {{ bodyData.currentBodyFatRate ? bodyData.currentBodyFatRate + ' %' : '未填写' }}
            </el-descriptions-item>
            <el-descriptions-item label="目标体脂率">
              {{ bodyData.targetBodyFatRate ? bodyData.targetBodyFatRate + ' %' : '未填写' }}
            </el-descriptions-item>
            <el-descriptions-item label="肌肉量">
              {{ bodyData.muscleMass ? bodyData.muscleMass + ' kg' : '未填写' }}
            </el-descriptions-item>
            <el-descriptions-item label="腰围">
              {{ bodyData.waistline ? bodyData.waistline + ' cm' : '未填写' }}
            </el-descriptions-item>
            <el-descriptions-item label="健身目标">
              {{ bodyData.fitnessGoal || '未填写' }}
            </el-descriptions-item>
            <el-descriptions-item label="运动频率">
              {{ bodyData.exerciseFrequency ? bodyData.exerciseFrequency + ' 次/周' : '未填写' }}
            </el-descriptions-item>
            <el-descriptions-item label="运动时长">
              {{ bodyData.exerciseDuration ? bodyData.exerciseDuration + ' 分钟' : '未填写' }}
            </el-descriptions-item>
            <el-descriptions-item label="饮食偏好" :span="2">
              {{ bodyData.foodPreference || '未填写' }}
            </el-descriptions-item>
            <el-descriptions-item label="过敏源" :span="2">
              {{ bodyData.allergens || '未填写' }}
            </el-descriptions-item>
            <el-descriptions-item label="健康状况" :span="2">
              {{ bodyData.healthConditions || '未填写' }}
            </el-descriptions-item>
          </el-descriptions>
          <el-empty v-else description="该用户暂未填写身体数据" />
        </div>
      </div>

      <template #footer>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <el-button @click="detailVisible = false">关闭</el-button>
          <div v-if="currentApply && currentApply.applyStatus === 'PENDING'">
            <el-button type="danger" @click="handleRejectInDetail" :loading="handleLoading">
              拒绝
            </el-button>
            <el-button type="success" @click="handleApproveInDetail" :loading="handleLoading">
              通过
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- 拒绝对话框 -->
    <el-dialog v-model="rejectVisible" title="拒绝申请" width="500px">
      <el-form :model="rejectForm" label-width="100px">
        <el-form-item label="拒绝理由">
          <el-input
            v-model="rejectForm.rejectReason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝理由"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject" :loading="handleLoading">
          确认拒绝
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCoachApplies, handleBinding } from '@/api/binding'
import { getUserBodyData } from '@/api/user'

const loading = ref(false)
const handleLoading = ref(false)
const bodyDataLoading = ref(false)
const tableData = ref([])
const statusFilter = ref('')
const detailVisible = ref(false)
const rejectVisible = ref(false)
const currentApply = ref(null)
const bodyData = ref(null)

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const rejectForm = reactive({
  rejectReason: ''
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getCoachApplies({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      status: statusFilter.value
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 查看申请详情
const handleView = async (row) => {
  currentApply.value = row
  bodyData.value = null
  detailVisible.value = true
  
  // 加载申请人的身体数据
  bodyDataLoading.value = true
  try {
    const res = await getUserBodyData(row.userId)
    bodyData.value = res.data
  } catch (error) {
    console.error('加载身体数据失败', error)
    bodyData.value = null
  } finally {
    bodyDataLoading.value = false
  }
}

// 在详情页面中通过申请
const handleApproveInDetail = () => {
  ElMessageBox.confirm('确认通过该绑定申请？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    handleLoading.value = true
    try {
      await handleBinding(currentApply.value.applyId, {
        applyStatus: 'APPROVED'
      })
      ElMessage.success('已通过申请')
      detailVisible.value = false
      loadData()
    } catch (error) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      handleLoading.value = false
    }
  }).catch(() => {})
}

// 在详情页面中拒绝申请
const handleRejectInDetail = () => {
  detailVisible.value = false
  rejectForm.rejectReason = ''
  rejectVisible.value = true
}

// 确认拒绝
const confirmReject = async () => {
  handleLoading.value = true
  try {
    await handleBinding(currentApply.value.applyId, {
      applyStatus: 'REJECTED',
      rejectReason: rejectForm.rejectReason
    })
    ElMessage.success('已拒绝申请')
    rejectVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    handleLoading.value = false
  }
}

// 获取状态文本
const getStatusText = (status) => {
  const map = {
    'PENDING': '待处理',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝',
    'CANCELLED': '已取消'
  }
  return map[status] || status
}

// 获取状态类型
const getStatusType = (status) => {
  const map = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'CANCELLED': 'info'
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
.bind-applies {
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
