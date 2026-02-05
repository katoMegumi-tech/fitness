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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="row.applyStatus === 'PENDING'">
              <el-button type="success" size="small" @click="handleApprove(row)">
                通过
              </el-button>
              <el-button type="danger" size="small" @click="handleReject(row)">
                拒绝
              </el-button>
            </template>
            <span v-else style="color: #909399">已处理</span>
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

const loading = ref(false)
const handleLoading = ref(false)
const tableData = ref([])
const statusFilter = ref('')
const rejectVisible = ref(false)
const currentApply = ref(null)

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

// 通过申请
const handleApprove = (row) => {
  ElMessageBox.confirm('确认通过该绑定申请？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    try {
      await handleBinding(row.applyId, {
        applyStatus: 'APPROVED'
      })
      ElMessage.success('已通过申请')
      loadData()
    } catch (error) {
      ElMessage.error(error.message || '操作失败')
    }
  }).catch(() => {})
}

// 拒绝申请
const handleReject = (row) => {
  currentApply.value = row
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
