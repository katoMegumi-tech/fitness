<template>
  <div class="checkin-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>每日打卡</span>
        </div>
      </template>

      <!-- 打卡统计 -->
      <el-row :gutter="20" style="margin-bottom: 20px">
        <el-col :span="6">
          <el-statistic title="本月打卡天数" :value="stats.checkInDays" suffix="天" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="打卡率" :value="stats.checkInRate" suffix="%" :precision="1" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="平均运动完成度" :value="stats.avgExerciseCompletion" suffix="%" :precision="1" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="平均饮食完成度" :value="stats.avgDietCompletion" suffix="%" :precision="1" />
        </el-col>
      </el-row>

      <el-divider />

      <!-- 打卡权限提示 -->
      <el-alert
        v-if="!hasActivePlan"
        title="打卡功能暂不可用"
        type="warning"
        description="您需要先绑定教练并获得教练制定的健身计划（需管理员审核通过且您确认后）才能使用打卡功能"
        :closable="false"
        style="margin-bottom: 20px"
      />

      <!-- 今日打卡表单 -->
      <el-form :model="checkInForm" label-width="120px" style="max-width: 600px" :disabled="!hasActivePlan">
        <el-form-item label="打卡类型">
          <el-radio-group v-model="checkInForm.checkType">
            <el-radio label="NORMAL">普通打卡</el-radio>
            <el-radio label="TARGET">完成目标打卡</el-radio>
          </el-radio-group>
          <div style="color: #909399; font-size: 12px; margin-top: 5px">
            完成目标打卡需填写身体数据，教练将判断是否达标
          </div>
        </el-form-item>

        <el-form-item label="运动完成度">
          <el-slider v-model="checkInForm.exerciseCompletion" :min="0" :max="100" show-input />
        </el-form-item>

        <el-form-item label="饮食完成度">
          <el-radio-group v-model="checkInForm.dietCompletion">
            <el-radio label="COMPLETE">完全达标</el-radio>
            <el-radio label="PARTIAL">部分达标</el-radio>
            <el-radio label="INCOMPLETE">未达标</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item 
          label="当日体重(kg)" 
          :required="checkInForm.checkType === 'TARGET'">
          <el-input-number v-model="checkInForm.weight" :min="20" :max="300" :precision="1" />
        </el-form-item>

        <el-form-item 
          label="当日体脂率(%)" 
          :required="checkInForm.checkType === 'TARGET'">
          <el-input-number v-model="checkInForm.bodyFatRate" :min="5" :max="60" :precision="1" />
        </el-form-item>

        <el-form-item 
          label="肌肉量(kg)" 
          :required="checkInForm.checkType === 'TARGET'">
          <el-input-number v-model="checkInForm.muscleMass" :min="10" :max="100" :precision="1" />
        </el-form-item>

        <el-form-item label="打卡备注">
          <el-input
            v-model="checkInForm.userRemark"
            type="textarea"
            :rows="4"
            placeholder="记录今天的运动和饮食情况..."
          />
        </el-form-item>

        <el-form-item label="打卡图片">
          <el-upload
            v-model:file-list="imageList"
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :on-change="handleImageChange"
            :on-remove="handleImageRemove"
            :limit="3"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div style="color: #909399; font-size: 12px; margin-top: 5px">
            可上传运动或饮食照片（最多3张）
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitCheckIn" :loading="submitting" :disabled="!hasActivePlan">
            提交打卡
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 打卡历史 -->
    <el-card style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>打卡历史</span>
        </div>
      </template>

      <el-table :data="checkInHistory" stripe>
        <el-table-column prop="checkType" label="打卡类型" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.checkType === 'TARGET'" type="warning">完成目标</el-tag>
            <el-tag v-else>普通打卡</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="checkTime" label="打卡时间" width="180">
          <template #default="{ row }">
            {{ row.checkTime ? row.checkTime.replace('T', ' ').substring(0, 19) : '' }}
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
        <el-table-column label="是否达标" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.checkType === 'TARGET' && row.isQualified === 1" type="success">已达标</el-tag>
            <el-tag v-else-if="row.checkType === 'TARGET' && row.isQualified === 0" type="danger">未达标</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="userRemark" label="备注" show-overflow-tooltip />
        <el-table-column label="教练点评" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.coachComment" style="color: #67C23A">{{ row.coachComment }}</span>
            <span v-else style="color: #909399">暂无点评</span>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadCheckInHistory"
        @size-change="loadCheckInHistory"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>

    <!-- 数据趋势图表 -->
    <el-card style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>数据趋势</span>
        </div>
      </template>
      <div ref="chartRef" style="width: 100%; height: 400px"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { submitCheckIn as submitCheckInApi, getMyCheckInHistory, getCheckInStats, getBodyDataTrend } from '@/api/checkin'
import { checkActivePlan } from '@/api/plan'
import { uploadCheckinImage } from '@/api/upload'
import * as echarts from 'echarts'

const hasActivePlan = ref(false)
const checkInForm = reactive({
  checkType: 'NORMAL',
  exerciseCompletion: 80,
  dietCompletion: 'COMPLETE',
  weight: null,
  bodyFatRate: null,
  muscleMass: null,
  userRemark: '',
  images: []
})

const submitting = ref(false)
const checkInHistory = ref([])
const imageList = ref([])
const stats = ref({
  checkInDays: 0,
  checkInRate: 0,
  avgExerciseCompletion: 0,
  avgDietCompletion: 0
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const chartRef = ref(null)
let chartInstance = null

// 图片上传处理
const handleImageChange = async (file) => {
  try {
    const res = await uploadCheckinImage(file.raw)
    checkInForm.images.push(res.data)
    ElMessage.success('图片上传成功')
  } catch (error) {
    ElMessage.error('图片上传失败：' + (error.message || '未知错误'))
    const index = imageList.value.findIndex(f => f.uid === file.uid)
    if (index > -1) {
      imageList.value.splice(index, 1)
    }
  }
}

// 图片移除处理
const handleImageRemove = (file) => {
  const index = checkInForm.images.findIndex(img => img.includes(file.name))
  if (index > -1) {
    checkInForm.images.splice(index, 1)
  }
}

// 提交打卡
const submitCheckIn = async () => {
  // 验证完成目标打卡必填项
  if (checkInForm.checkType === 'TARGET') {
    if (!checkInForm.weight || !checkInForm.bodyFatRate || !checkInForm.muscleMass) {
      ElMessage.warning('完成目标打卡必须填写体重、体脂率和肌肉量')
      return
    }
  }

  try {
    submitting.value = true
    // 将图片数组转换为逗号分隔的字符串
    const submitData = {
      ...checkInForm,
      images: checkInForm.images.join(',')
    }
    await submitCheckInApi(submitData)
    ElMessage.success('打卡成功！')
    
    // 重置表单
    checkInForm.checkType = 'NORMAL'
    checkInForm.exerciseCompletion = 80
    checkInForm.dietCompletion = 'COMPLETE'
    checkInForm.weight = null
    checkInForm.bodyFatRate = null
    checkInForm.muscleMass = null
    checkInForm.userRemark = ''
    checkInForm.images = []
    imageList.value = []
    
    // 刷新数据
    loadCheckInHistory()
    loadStats()
    loadTrendChart()
  } catch (error) {
    ElMessage.error(error.message || '打卡失败')
  } finally {
    submitting.value = false
  }
}

// 加载打卡历史
const loadCheckInHistory = async () => {
  try {
    const res = await getMyCheckInHistory({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    checkInHistory.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载打卡历史失败')
  }
}

// 加载统计数据
const loadStats = async () => {
  try {
    const res = await getCheckInStats(30)
    stats.value = res.data
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

// 加载趋势图表
const loadTrendChart = async () => {
  try {
    const res = await getBodyDataTrend(30)
    const trendData = res.data
    
    await nextTick()
    
    if (!chartInstance && chartRef.value) {
      chartInstance = echarts.init(chartRef.value)
    }
    
    if (chartInstance) {
      const option = {
        title: {
          text: '最近30天数据趋势'
        },
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['体重', '体脂率', '运动完成度', '饮食完成度']
        },
        xAxis: {
          type: 'category',
          data: trendData.dates
        },
        yAxis: [
          {
            type: 'value',
            name: '体重/体脂',
            position: 'left'
          },
          {
            type: 'value',
            name: '完成度(%)',
            position: 'right',
            max: 100
          }
        ],
        series: [
          {
            name: '体重',
            type: 'line',
            data: trendData.weights,
            yAxisIndex: 0
          },
          {
            name: '体脂率',
            type: 'line',
            data: trendData.bodyFats,
            yAxisIndex: 0
          },
          {
            name: '运动完成度',
            type: 'line',
            data: trendData.exerciseCompletions,
            yAxisIndex: 1
          },
          {
            name: '饮食完成度',
            type: 'line',
            data: trendData.dietCompletions,
            yAxisIndex: 1
          }
        ]
      }
      
      chartInstance.setOption(option)
    }
  } catch (error) {
    console.error('加载趋势图表失败', error)
  }
}

// 进度条颜色
const getProgressColor = (percentage) => {
  if (percentage >= 80) return '#67C23A'
  if (percentage >= 60) return '#E6A23C'
  return '#F56C6C'
}

// 检查是否有激活的计划
const checkPlanStatus = async () => {
  try {
    const res = await checkActivePlan()
    hasActivePlan.value = res.data
  } catch (error) {
    console.error('检查计划状态失败', error)
  }
}

onMounted(() => {
  checkPlanStatus()
  loadCheckInHistory()
  loadStats()
  loadTrendChart()
})
</script>

<style scoped>
.checkin-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
}
</style>
