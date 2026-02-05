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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewData(row)">
              查看数据
            </el-button>
            <el-button type="success" size="small" @click="handleMakePlan(row)">
              制定计划
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCoachStudents } from '@/api/binding'

const router = useRouter()

const loading = ref(false)
const tableData = ref([])

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
  ElMessage.info('查看学员数据功能将在后续实现')
  // TODO: 跳转到学员数据详情页
}

// 制定计划
const handleMakePlan = (row) => {
  router.push({
    name: 'PlanEditor',
    query: { userId: row.userId }
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
