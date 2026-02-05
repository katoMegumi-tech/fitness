<template>
  <div class="plan-editor">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '调整健身计划' : '制定健身计划' }}</span>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-form-item label="选择学员" prop="userId" v-if="!isEdit">
          <el-select v-model="form.userId" placeholder="请选择学员" filterable>
            <el-option
              v-for="student in students"
              :key="student.userId"
              :label="student.userName"
              :value="student.userId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="计划名称" prop="planName">
          <el-input v-model="form.planName" placeholder="请输入计划名称" />
        </el-form-item>

        <el-form-item label="健身目标" prop="fitnessGoal">
          <el-select v-model="form.fitnessGoal" placeholder="请选择健身目标">
            <el-option label="减脂" value="减脂" />
            <el-option label="增肌" value="增肌" />
            <el-option label="塑形" value="塑形" />
            <el-option label="保持" value="保持" />
          </el-select>
        </el-form-item>

        <el-form-item label="计划难度" prop="planDifficulty">
          <el-radio-group v-model="form.planDifficulty">
            <el-radio label="BEGINNER">初级</el-radio>
            <el-radio label="INTERMEDIATE">中级</el-radio>
            <el-radio label="ADVANCED">高级</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="计划周期" prop="planCycle">
          <el-input-number
            v-model="form.planCycle"
            :min="1"
            :max="365"
            placeholder="请输入计划周期"
          />
          <span style="margin-left: 10px">天</span>
        </el-form-item>

        <el-form-item label="开始时间" prop="planStartTime">
          <el-date-picker
            v-model="form.planStartTime"
            type="datetime"
            placeholder="选择开始时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>

        <el-form-item label="结束时间" prop="planEndTime">
          <el-date-picker
            v-model="form.planEndTime"
            type="datetime"
            placeholder="选择结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>

        <el-form-item label="运动计划" prop="exercisePlan">
          <el-input
            v-model="form.exercisePlan"
            type="textarea"
            :rows="8"
            placeholder="请详细描述运动计划，包括运动项目、强度、频率等"
          />
        </el-form-item>

        <el-form-item label="饮食计划" prop="dietPlan">
          <el-input
            v-model="form.dietPlan"
            type="textarea"
            :rows="8"
            placeholder="请详细描述饮食计划，包括饮食结构、热量摄入、营养搭配等"
          />
        </el-form-item>

        <el-form-item label="调整原因" prop="adjustmentReason" v-if="isEdit">
          <el-input
            v-model="form.adjustmentReason"
            type="textarea"
            :rows="3"
            placeholder="请说明调整原因"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="loading">
            提交计划
          </el-button>
          <el-button @click="resetForm">重置</el-button>
          <el-button @click="$router.back()">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createPlan, adjustPlan, getPlanById } from '@/api/plan'
import { getCoachStudents } from '@/api/binding'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const isEdit = ref(false)
const students = ref([])

const form = reactive({
  userId: null,
  planName: '',
  fitnessGoal: '',
  planDifficulty: 'BEGINNER',
  planCycle: 7,
  planStartTime: '',
  planEndTime: '',
  exercisePlan: '',
  dietPlan: '',
  adjustmentReason: ''
})

const rules = {
  userId: [
    { required: true, message: '请选择学员', trigger: 'change' }
  ],
  planName: [
    { required: true, message: '请输入计划名称', trigger: 'blur' }
  ],
  fitnessGoal: [
    { required: true, message: '请选择健身目标', trigger: 'change' }
  ],
  planDifficulty: [
    { required: true, message: '请选择计划难度', trigger: 'change' }
  ],
  planCycle: [
    { required: true, message: '请输入计划周期', trigger: 'blur' }
  ],
  planStartTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  planEndTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ],
  exercisePlan: [
    { required: true, message: '请输入运动计划', trigger: 'blur' },
    { min: 50, message: '运动计划至少50个字符', trigger: 'blur' }
  ],
  dietPlan: [
    { required: true, message: '请输入饮食计划', trigger: 'blur' },
    { min: 50, message: '饮食计划至少50个字符', trigger: 'blur' }
  ]
}

// 加载学员列表
const loadStudents = async () => {
  try {
    const res = await getCoachStudents({ pageNum: 1, pageSize: 100 })
    students.value = res.data.records
  } catch (error) {
    console.error('加载学员列表失败', error)
  }
}

// 加载计划详情（调整时）
const loadPlanDetail = async (planId) => {
  try {
    const res = await getPlanById(planId)
    Object.assign(form, res.data)
    isEdit.value = true
  } catch (error) {
    ElMessage.error('加载计划详情失败')
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        if (isEdit.value) {
          await adjustPlan(route.query.planId, form)
          ElMessage.success('计划已调整，等待管理员审核')
        } else {
          await createPlan(form)
          ElMessage.success('计划已创建，等待管理员审核')
        }
        router.back()
      } catch (error) {
        ElMessage.error(error.message || '提交失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  if (!formRef.value) return
  formRef.value.resetFields()
}

onMounted(async () => {
  await loadStudents()
  
  // 如果有planId参数，说明是调整计划
  if (route.query.planId) {
    await loadPlanDetail(route.query.planId)
  }
})
</script>

<style scoped>
.plan-editor {
  padding: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}
</style>
