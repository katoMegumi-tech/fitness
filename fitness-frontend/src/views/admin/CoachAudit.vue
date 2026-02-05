<template>
  <div class="coach-audit">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>教练认证审核</span>
        </div>
      </template>

      <!-- 待审核列表 -->
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="userId" label="教练ID" width="80" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="specialties" label="擅长领域" width="150" />
        <el-table-column prop="yearsOfExperience" label="从业年限" width="100">
          <template #default="{ row }">
            {{ row.yearsOfExperience }}年
          </template>
        </el-table-column>
        <el-table-column prop="introduction" label="个人简介" show-overflow-tooltip />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">
              查看详情
            </el-button>
            <el-button 
              v-if="row.certificationStatus === 'PENDING'"
              type="success" 
              size="small" 
              @click="handleAudit(row, 'APPROVED')"
            >
              通过
            </el-button>
            <el-button 
              v-if="row.certificationStatus === 'PENDING'"
              type="danger" 
              size="small" 
              @click="handleAudit(row, 'REJECTED')"
            >
              拒绝
            </el-button>
            <el-button 
              v-if="row.certificationStatus === 'APPROVED' || row.certificationStatus === 'REJECTED'"
              type="warning" 
              size="small" 
              @click="handleRevoke(row)"
            >
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
    <el-dialog v-model="detailVisible" title="教练认证详情" width="800px">
      <el-descriptions :column="2" border v-if="currentCoach">
        <el-descriptions-item label="姓名">{{ currentCoach.name }}</el-descriptions-item>
        <el-descriptions-item label="账号">{{ currentCoach.account }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentCoach.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentCoach.email }}</el-descriptions-item>
        <el-descriptions-item label="擅长领域" :span="2">
          {{ currentCoach.specialties }}
        </el-descriptions-item>
        <el-descriptions-item label="从业年限">
          {{ currentCoach.yearsOfExperience }}年
        </el-descriptions-item>
        <el-descriptions-item label="个人简介" :span="2">
          {{ currentCoach.introduction }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 证书图片 -->
      <div style="margin-top: 20px">
        <h4>资格证书</h4>
        <el-image
          v-for="(cert, index) in currentCoach.certImages"
          :key="index"
          :src="getImageUrl(cert.path)"
          :preview-src-list="currentCoach.certImages.map(c => getImageUrl(c.path))"
          :initial-index="index"
          fit="cover"
          style="width: 150px; height: 150px; margin-right: 10px; cursor: pointer"
        />
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="success" @click="handleAudit(currentCoach, 'APPROVED')">
          通过认证
        </el-button>
        <el-button type="danger" @click="handleAudit(currentCoach, 'REJECTED')">
          拒绝认证
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPendingCoaches, auditCertification, revokeCoachAudit } from '@/api/coach'

const loading = ref(false)
const tableData = ref([])
const detailVisible = ref(false)
const auditVisible = ref(false)
const auditLoading = ref(false)
const currentCoach = ref(null)
const auditTitle = ref('')

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
    const res = await getPendingCoaches({
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
  console.log('查看教练详情:', row)
  console.log('证书图片:', row.certImages)
  currentCoach.value = row
  detailVisible.value = true
}

// 审核操作
const handleAudit = (row, status) => {
  currentCoach.value = row
  auditForm.auditStatus = status
  auditForm.auditOpinion = ''
  auditTitle.value = status === 'APPROVED' ? '通过认证' : '拒绝认证'
  auditVisible.value = true
  detailVisible.value = false
}

// 确认审核
const confirmAudit = async () => {
  auditLoading.value = true
  try {
    await auditCertification(currentCoach.value.userId, auditForm)
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
      '确定要撤销该教练的审核吗？撤销后状态将变为待审核。',
      '撤销确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await revokeCoachAudit(row.userId)
    ElMessage.success('审核已撤销')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '撤销失败')
    }
  }
}

// 获取图片完整URL
const getImageUrl = (path) => {
  if (!path) return ''
  // 如果路径已经是完整URL，直接返回
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path
  }
  // 图片需要使用完整的后端地址，不能使用相对路径
  return 'http://localhost:8080/api' + path
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.coach-audit {
  padding: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}
</style>
