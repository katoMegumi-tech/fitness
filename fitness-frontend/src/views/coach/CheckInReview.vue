<template>
  <div class="checkin-review">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学员打卡审核</span>
          <el-radio-group v-model="filterType" @change="loadPendingCheckIns">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="TARGET">待审核</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <!-- 打卡列表 -->
      <el-empty v-if="!loading && tableData.length === 0" description="暂无待审核的打卡记录">
        <template #extra>
          <el-text type="info">
            提示：只有学员提交"完成目标打卡"时才需要审核
          </el-text>
        </template>
      </el-empty>
      
      <el-table v-else :data="tableData" v-loading="loading" border>
        <el-table-column prop="checkNo" label="打卡编号" width="180" />
        <el-table-column prop="userName" label="学员" width="120" />
        <el-table-column prop="checkType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.checkType === 'TARGET'" type="warning">完成目标</el-tag>
            <el-tag v-else>普通打卡</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="checkTime" label="打卡时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.checkTime) }}
          </template>
        </el-table-column>
        <el-table-column label="运动完成度" width="120">
          <template #default="{ row }">
            <el-progress :percentage="row.exerciseCompletion" :color="getProgressColor(row.exerciseCompletion)" />
          </template>
        </el-table-column>
        <el-table-column label="饮食完成度" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.dietCompletion === 'COMPLETE'" type="success">完全达标</el-tag>
            <el-tag v-else-if="row.dietCompletion === 'PARTIAL'" type="warning">部分达标</el-tag>
            <el-tag v-else type="danger">未达标</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="weight" label="体重(kg)" width="100" />
        <el-table-column prop="bodyFatRate" label="体脂率(%)" width="100" />
        <el-table-column prop="muscleMass" label="肌肉量(kg)" width="100" />
        <el-table-column label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.checkStatus === 'PENDING'" type="warning">待审核</el-tag>
            <el-tag v-else-if="row.checkStatus === 'COMPLETED' && row.isQualified === 1" type="success">已达标</el-tag>
            <el-tag v-else-if="row.checkStatus === 'COMPLETED' && row.isQualified === 0" type="danger">未达标</el-tag>
            <el-tag v-else-if="row.checkStatus === 'COMPLETED'" type="info">已完结</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">
              查看详情
            </el-button>
            <el-button 
              v-if="row.checkType === 'TARGET' && row.checkStatus === 'PENDING'"
              type="success" 
              size="small" 
              @click="handleReview(row)">
              审核
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
        @size-change="loadPendingCheckIns"
        @current-change="loadPendingCheckIns"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="打卡详情" width="800px">
      <el-descriptions :column="2" border v-if="currentCheckIn">
        <el-descriptions-item label="打卡编号" :span="2">
          {{ currentCheckIn.checkNo }}
        </el-descriptions-item>
        <el-descriptions-item label="学员姓名">
          {{ currentCheckIn.userName }}
        </el-descriptions-item>
        <el-descriptions-item label="打卡时间">
          {{ formatDateTime(currentCheckIn.checkTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="打卡类型">
          <el-tag v-if="currentCheckIn.checkType === 'TARGET'" type="warning">完成目标打卡</el-tag>
          <el-tag v-else>普通打卡</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="运动完成度">
          {{ currentCheckIn.exerciseCompletion }}%
        </el-descriptions-item>
        <el-descriptions-item label="饮食完成度" :span="2">
          <el-tag v-if="currentCheckIn.dietCompletion === 'COMPLETE'" type="success">完全达标</el-tag>
          <el-tag v-else-if="currentCheckIn.dietCompletion === 'PARTIAL'" type="warning">部分达标</el-tag>
          <el-tag v-else type="danger">未达标</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="体重" v-if="currentCheckIn.weight">
          {{ currentCheckIn.weight }}kg
        </el-descriptions-item>
        <el-descriptions-item label="体脂率" v-if="currentCheckIn.bodyFatRate">
          {{ currentCheckIn.bodyFatRate }}%
        </el-descriptions-item>
        <el-descriptions-item label="肌肉量" v-if="currentCheckIn.muscleMass">
          {{ currentCheckIn.muscleMass }}kg
        </el-descriptions-item>
        <el-descriptions-item label="学员备注" :span="2" v-if="currentCheckIn.userRemark">
          {{ currentCheckIn.userRemark }}
        </el-descriptions-item>
        <el-descriptions-item label="教练点评" :span="2" v-if="currentCheckIn.coachComment">
          {{ currentCheckIn.coachComment }}
        </el-descriptions-item>
        <el-descriptions-item label="审核状态" v-if="currentCheckIn.checkType === 'TARGET'">
          <el-tag v-if="currentCheckIn.isQualified === 1" type="success">已达标</el-tag>
          <el-tag v-else-if="currentCheckIn.isQualified === 0" type="danger">未达标</el-tag>
          <el-tag v-else type="warning">待审核</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 打卡图片 -->
      <div style="margin-top: 20px" v-if="currentCheckIn && currentCheckIn.images">
        <h4>打卡图片</h4>
        <el-image
          v-for="(img, index) in getImageList(currentCheckIn.images)"
          :key="index"
          :src="getImageUrl(img)"
          :preview-src-list="getImageList(currentCheckIn.images).map(i => getImageUrl(i))"
          :initial-index="index"
          fit="cover"
          style="width: 150px; height: 150px; margin-right: 10px; cursor: pointer"
        />
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button 
          v-if="currentCheckIn && currentCheckIn.checkType === 'TARGET' && currentCheckIn.checkStatus === 'PENDING'"
          type="success" 
          @click="handleReview(currentCheckIn)">
          审核
        </el-button>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog v-model="reviewVisible" title="审核打卡" width="600px">
      <el-form :model="reviewForm" label-width="100px">
        <el-form-item label="是否达标">
          <el-radio-group v-model="reviewForm.isQualified">
            <el-radio :label="1">已达标</el-radio>
            <el-radio :label="0">未达标</el-radio>
          </el-radio-group>
          <div style="margin-top: 8px; line-height: 1.7">
            <div style="color: #67C23A; font-size: 12px">已达标：自动解除绑定关系，学员完成训练</div>
            <div style="color: #E6A23C; font-size: 12px">未达标：保持绑定关系，学员继续训练；如有需要可重新制定计划</div>
          </div>
        </el-form-item>
        <el-form-item label="教练点评">
          <el-input
            v-model="reviewForm.comment"
            type="textarea"
            :rows="4"
            placeholder="请输入点评内容..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPendingCheckIns, reviewCheckIn } from '@/api/checkin'
import { getImageUrl } from '@/utils/image'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const detailVisible = ref(false)
const reviewVisible = ref(false)
const submitting = ref(false)
const currentCheckIn = ref(null)
const filterType = ref('TARGET')

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const reviewForm = reactive({
  isQualified: 1,
  comment: ''
})

// 加载待审核打卡列表
const loadPendingCheckIns = async () => {
  loading.value = true
  try {
    const res = await getPendingCheckIns({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      checkType: filterType.value || undefined
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
    
    // 调试信息：查看数据
    console.log('打卡列表数据:', res.data.records)
    if (res.data.records.length > 0) {
      console.log('第一条数据:', res.data.records[0])
      console.log('checkType:', res.data.records[0].checkType)
      console.log('isQualified:', res.data.records[0].isQualified)
      console.log('isQualified类型:', typeof res.data.records[0].isQualified)
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 查看详情
const handleView = (row) => {
  currentCheckIn.value = row
  detailVisible.value = true
}

// 审核打卡
const handleReview = (row) => {
  currentCheckIn.value = row
  reviewForm.isQualified = 1
  reviewForm.comment = ''
  reviewVisible.value = true
  detailVisible.value = false
}

// 提交审核
const submitReview = async () => {
  if (!reviewForm.comment) {
    ElMessage.warning('请输入点评内容')
    return
  }

  // 如果选择未达标，询问是否重新制定计划
  if (reviewForm.isQualified === 0) {
    try {
      await ElMessageBox.confirm(
        '学员未达标，是否需要重新制定锻炼计划？',
        '重新制定计划',
        {
          confirmButtonText: '是，重新制定',
          cancelButtonText: '否，继续原计划',
          type: 'warning',
          distinguishCancelAndClose: true
        }
      )
      
      // 用户选择"是"，先提交审核，然后跳转到制定计划页面
      submitting.value = true
      try {
        await reviewCheckIn(currentCheckIn.value.checkInId, reviewForm)
        ElMessage.success('审核完成，即将跳转到制定计划页面')
        reviewVisible.value = false
        
        // 跳转到制定计划页面，并传递学员ID
        setTimeout(() => {
          router.push({
            path: '/coach/plan-editor',
            query: {
              userId: currentCheckIn.value.userId,
              userName: currentCheckIn.value.userName,
              fromReview: 'true'
            }
          })
        }, 1000)
      } catch (error) {
        ElMessage.error(error.message || '审核失败')
      } finally {
        submitting.value = false
      }
    } catch (action) {
      if (action === 'cancel') {
        // 用户选择"否"，只提交审核，不跳转
        submitting.value = true
        try {
          await reviewCheckIn(currentCheckIn.value.checkInId, reviewForm)
          ElMessage.success('审核完成，学员将继续按原计划训练')
          reviewVisible.value = false
          loadPendingCheckIns()
        } catch (error) {
          ElMessage.error(error.message || '审核失败')
        } finally {
          submitting.value = false
        }
      }
      // 用户点击关闭按钮，不做任何操作
    }
  } else {
    // 选择已达标，直接提交审核
    submitting.value = true
    try {
      await reviewCheckIn(currentCheckIn.value.checkInId, reviewForm)
      ElMessage.success('审核完成，学员已达标，自动解除绑定')
      reviewVisible.value = false
      loadPendingCheckIns()
    } catch (error) {
      ElMessage.error(error.message || '审核失败')
    } finally {
      submitting.value = false
    }
  }
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

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

onMounted(() => {
  loadPendingCheckIns()
})
</script>

<style scoped>
.checkin-review {
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
