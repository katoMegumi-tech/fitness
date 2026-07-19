<template>
  <div class="checkin-stats">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学员打卡统计</span>
        </div>
      </template>

      <!-- 学员列表 -->
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="userName" label="学员姓名" width="120" />
        <el-table-column prop="planName" label="当前计划" width="150" />
        <el-table-column prop="totalDays" label="打卡天数" width="100">
          <template #default="{ row }">
            <el-tag type="success">{{ row.totalDays }}天</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalCount" label="打卡次数" width="100">
          <template #default="{ row }">
            <el-tag type="primary">{{ row.totalCount }}次</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastCheckInTime" label="最近打卡" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.lastCheckInTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="planStartTime" label="计划开始" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.planStartTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewDetails(row)">
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

    <!-- 打卡详情对话框 -->
    <el-dialog v-model="detailVisible" title="打卡详情" width="1000px">
      <div v-if="currentStudent">
        <!-- 统计信息 -->
        <el-descriptions :column="4" border style="margin-bottom: 20px">
          <el-descriptions-item label="学员姓名">{{ currentStudent.userName }}</el-descriptions-item>
          <el-descriptions-item label="当前计划">{{ currentStudent.planName }}</el-descriptions-item>
          <el-descriptions-item label="打卡天数">
            <el-tag type="success">{{ currentStudent.totalDays }}天</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="打卡次数">
            <el-tag type="primary">{{ currentStudent.totalCount }}次</el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 打卡记录列表 -->
        <el-table :data="checkInRecords" v-loading="recordsLoading" border max-height="500">
          <el-table-column prop="checkTime" label="打卡时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.checkTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="checkType" label="打卡类型" width="120">
            <template #default="{ row }">
              <el-tag :type="row.checkType === 'NORMAL' ? 'success' : 'warning'">
                {{ row.checkType === 'NORMAL' ? '普通打卡' : '完成目标' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="checkStatus" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.checkStatus === 'COMPLETED' ? 'success' : 'warning'">
                {{ row.checkStatus === 'COMPLETED' ? '已完成' : '待审核' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="exerciseCompletion" label="运动完成度" width="120">
            <template #default="{ row }">
              <el-progress :percentage="row.exerciseCompletion" :color="getProgressColor(row.exerciseCompletion)" />
            </template>
          </el-table-column>
          <el-table-column prop="dietCompletion" label="饮食完成度" width="120">
            <template #default="{ row }">
              <el-tag v-if="row.dietCompletion === 'COMPLETE'" type="success">完全达标</el-tag>
              <el-tag v-else-if="row.dietCompletion === 'PARTIAL'" type="warning">部分达标</el-tag>
              <el-tag v-else type="danger">未达标</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="weight" label="体重(kg)" width="100" />
          <el-table-column prop="bodyFatRate" label="体脂率(%)" width="100" />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button type="text" size="small" @click="handleViewRecord(row)">
                查看
              </el-button>
              <el-button type="text" size="small" @click="handleCommentRecord(row)">
                批注
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 打卡记录详情对话框 -->
    <el-dialog v-model="recordDetailVisible" title="打卡记录详情" width="700px">
      <el-descriptions :column="2" border v-if="currentRecord">
        <el-descriptions-item label="打卡编号" :span="2">{{ currentRecord.checkNo }}</el-descriptions-item>
        <el-descriptions-item label="打卡时间" :span="2">
          {{ formatDateTime(currentRecord.checkTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="打卡类型">
          <el-tag :type="currentRecord.checkType === 'NORMAL' ? 'success' : 'warning'">
            {{ currentRecord.checkType === 'NORMAL' ? '普通打卡' : '完成目标' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentRecord.checkStatus === 'COMPLETED' ? 'success' : 'warning'">
            {{ currentRecord.checkStatus === 'COMPLETED' ? '已完成' : '待审核' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="运动完成度">
          <el-progress :percentage="currentRecord.exerciseCompletion" :color="getProgressColor(currentRecord.exerciseCompletion)" />
        </el-descriptions-item>
        <el-descriptions-item label="饮食完成度">
          <el-tag v-if="currentRecord.dietCompletion === 'COMPLETE'" type="success">完全达标</el-tag>
          <el-tag v-else-if="currentRecord.dietCompletion === 'PARTIAL'" type="warning">部分达标</el-tag>
          <el-tag v-else type="danger">未达标</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="体重" v-if="currentRecord.weight">
          {{ currentRecord.weight }}kg
        </el-descriptions-item>
        <el-descriptions-item label="体脂率" v-if="currentRecord.bodyFatRate">
          {{ currentRecord.bodyFatRate }}%
        </el-descriptions-item>
        <el-descriptions-item label="肌肉量" v-if="currentRecord.muscleMass">
          {{ currentRecord.muscleMass }}kg
        </el-descriptions-item>
        <el-descriptions-item label="学员备注" :span="2" v-if="currentRecord.userRemark">
          <div style="white-space: pre-wrap; padding: 10px; background: #f5f7fa; border-radius: 4px">
            {{ currentRecord.userRemark }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="打卡图片" :span="2" v-if="currentRecord.images">
          <el-image
            v-for="(img, index) in getImageList(currentRecord.images)"
            :key="index"
            :src="getImageUrl(img)"
            :preview-src-list="getImageList(currentRecord.images).map(i => getImageUrl(i))"
            :initial-index="index"
            style="width: 100px; height: 100px; margin-right: 10px; cursor: pointer"
            fit="cover"
          />
        </el-descriptions-item>
        <el-descriptions-item label="教练评价" :span="2" v-if="currentRecord.coachComment">
          <div style="white-space: pre-wrap; padding: 10px; background: #e7f5ff; border-radius: 4px">
            {{ currentRecord.coachComment }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="是否达标" v-if="currentRecord.checkType === 'TARGET'">
          <el-tag v-if="currentRecord.isQualified === 1" type="success">已达标</el-tag>
          <el-tag v-else-if="currentRecord.isQualified === 0" type="danger">未达标</el-tag>
          <el-tag v-else type="warning">待审核</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="recordDetailVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleCommentRecord(currentRecord)" v-if="currentRecord">
          批注
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="commentVisible" title="教练批注" width="500px">
      <el-form :model="commentForm" label-width="80px">
        <el-form-item label="批注内容">
          <el-input
            v-model="commentForm.comment"
            type="textarea"
            :rows="5"
            maxlength="500"
            show-word-limit
            placeholder="请输入学员可见的批注内容"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="commentVisible = false">取消</el-button>
        <el-button type="primary" @click="submitComment" :loading="commentSubmitting">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getStudentCheckInStats, getStudentCheckInRecords, addCoachComment } from '@/api/checkin'
import { getImageUrl } from '@/utils/image'

const loading = ref(false)
const recordsLoading = ref(false)
const tableData = ref([])
const detailVisible = ref(false)
const recordDetailVisible = ref(false)
const commentVisible = ref(false)
const commentSubmitting = ref(false)
const currentStudent = ref(null)
const currentRecord = ref(null)
const checkInRecords = ref([])

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const commentForm = reactive({
  checkInId: null,
  comment: ''
})

// 加载学员打卡统计数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getStudentCheckInStats({
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

// 查看学员打卡详情
const loadStudentRecords = async (userId) => {
  recordsLoading.value = true
  try {
    const res = await getStudentCheckInRecords({
      userId,
      pageNum: 1,
      pageSize: 100
    })
    checkInRecords.value = res.data.records
  } catch (error) {
    ElMessage.error('加载打卡记录失败')
  } finally {
    recordsLoading.value = false
  }
}

const handleViewDetails = async (row) => {
  currentStudent.value = row
  detailVisible.value = true
  await loadStudentRecords(row.userId)
}

// 查看单条打卡记录详情
const handleViewRecord = (row) => {
  currentRecord.value = row
  recordDetailVisible.value = true
}

const handleCommentRecord = (row) => {
  if (!row) return
  currentRecord.value = row
  commentForm.checkInId = row.checkInId
  commentForm.comment = row.coachComment || ''
  commentVisible.value = true
}

const submitComment = async () => {
  const comment = commentForm.comment.trim()
  if (!comment) {
    ElMessage.warning('请输入批注内容')
    return
  }

  if (currentRecord.value?.checkType === 'TARGET' && currentRecord.value?.isQualified == null) {
    ElMessage.warning('目标打卡请先在打卡审核中完成达标判定')
    return
  }

  commentSubmitting.value = true
  try {
    await addCoachComment(
      commentForm.checkInId,
      comment,
      currentRecord.value?.checkType === 'TARGET' ? currentRecord.value?.isQualified : undefined
    )
    ElMessage.success('批注已保存')
    commentVisible.value = false

    if (currentStudent.value?.userId) {
      await loadStudentRecords(currentStudent.value.userId)
    }

    if (currentRecord.value?.checkInId === commentForm.checkInId) {
      currentRecord.value = {
        ...currentRecord.value,
        coachComment: comment
      }
    }
  } catch (error) {
    ElMessage.error(error.message || '批注保存失败')
  } finally {
    commentSubmitting.value = false
  }
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 获取图片列表
const getImageList = (images) => {
  if (!images) return []
  if (Array.isArray(images)) return images.filter(Boolean)
  if (typeof images !== 'string') return []
  const trimmed = images.trim()
  if (!trimmed) return []
  try {
    const parsed = JSON.parse(trimmed)
    if (Array.isArray(parsed)) {
      return parsed.filter(Boolean)
    }
  } catch (error) {
    // fall through to comma-separated parsing
  }
  return trimmed
    .replace(/^\[/, '')
    .replace(/\]$/, '')
    .split(',')
    .map(img => img.replace(/^["'\s]+|["'\s]+$/g, ''))
    .filter(Boolean)
}

// 进度条颜色
const getProgressColor = (percentage) => {
  if (percentage >= 80) return '#67C23A'
  if (percentage >= 60) return '#E6A23C'
  return '#F56C6C'
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.checkin-stats {
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
