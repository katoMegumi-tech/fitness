<template>
  <div class="notification-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>通知管理</span>
          <el-button type="primary" @click="showSendDialog">发送通知</el-button>
        </div>
      </template>

      <el-alert
        title="通知说明"
        type="info"
        :closable="false"
        style="margin-bottom: 20px">
        <p>管理员可以向用户和教练发送系统通知，支持单个发送和批量发送。</p>
      </el-alert>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="发送单个通知" name="single">
          <el-form :model="singleForm" label-width="120px" style="max-width: 600px">
            <el-form-item label="接收者ID" required>
              <el-input v-model="singleForm.receiverId" placeholder="请输入用户ID" type="number" />
              <div style="color: #909399; font-size: 12px; margin-top: 5px">
                可以在用户管理或教练管理中查看用户ID
              </div>
            </el-form-item>
            <el-form-item label="通知类型" required>
              <el-select v-model="singleForm.notificationType" placeholder="请选择通知类型">
                <el-option label="系统通知" value="SYSTEM" />
                <el-option label="绑定申请" value="BIND_APPLY" />
                <el-option label="绑定结果" value="BIND_RESULT" />
                <el-option label="计划审核" value="PLAN_AUDIT" />
                <el-option label="计划推送" value="PLAN_PUSH" />
                <el-option label="打卡点评" value="CHECK_COMMENT" />
                <el-option label="认证审核" value="CERT_AUDIT" />
              </el-select>
            </el-form-item>
            <el-form-item label="通知标题" required>
              <el-input v-model="singleForm.title" placeholder="请输入通知标题" />
            </el-form-item>
            <el-form-item label="通知内容" required>
              <el-input 
                v-model="singleForm.content" 
                type="textarea" 
                :rows="5"
                placeholder="请输入通知内容" />
            </el-form-item>
            <el-form-item label="关联业务ID">
              <el-input v-model="singleForm.relatedId" placeholder="选填，如计划ID、申请ID等" type="number" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="sendSingle" :loading="sending">发送</el-button>
              <el-button @click="resetSingleForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="批量发送通知" name="batch">
          <el-form :model="batchForm" label-width="120px" style="max-width: 600px">
            <el-form-item label="发送对象" required>
              <el-radio-group v-model="batchForm.targetRole">
                <el-radio label="USER">所有用户</el-radio>
                <el-radio label="COACH">所有教练</el-radio>
                <el-radio label="ALL">所有人（用户+教练）</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="通知类型" required>
              <el-select v-model="batchForm.notificationType" placeholder="请选择通知类型">
                <el-option label="系统通知" value="SYSTEM" />
                <el-option label="系统维护" value="SYSTEM_MAINTENANCE" />
                <el-option label="功能更新" value="SYSTEM_UPDATE" />
                <el-option label="活动通知" value="ACTIVITY" />
              </el-select>
            </el-form-item>
            <el-form-item label="通知标题" required>
              <el-input v-model="batchForm.title" placeholder="请输入通知标题" />
            </el-form-item>
            <el-form-item label="通知内容" required>
              <el-input 
                v-model="batchForm.content" 
                type="textarea" 
                :rows="5"
                placeholder="请输入通知内容" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="sendBatch" :loading="sending">批量发送</el-button>
              <el-button @click="resetBatchForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { sendNotification, sendBatchNotification } from '@/api/notification'

const activeTab = ref('single')
const sending = ref(false)

const singleForm = reactive({
  receiverId: '',
  notificationType: 'SYSTEM',
  title: '',
  content: '',
  relatedId: ''
})

const batchForm = reactive({
  targetRole: 'USER',
  notificationType: 'SYSTEM',
  title: '',
  content: ''
})

// 显示发送对话框
const showSendDialog = () => {
  activeTab.value = 'single'
}

// 发送单个通知
const sendSingle = async () => {
  if (!singleForm.receiverId || !singleForm.title || !singleForm.content) {
    ElMessage.warning('请填写必填项')
    return
  }

  try {
    await ElMessageBox.confirm('确定发送此通知吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    sending.value = true
    await sendNotification({
      receiverId: parseInt(singleForm.receiverId),
      notificationType: singleForm.notificationType,
      title: singleForm.title,
      content: singleForm.content,
      relatedId: singleForm.relatedId ? parseInt(singleForm.relatedId) : null
    })
    ElMessage.success('通知发送成功')
    resetSingleForm()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '发送失败')
    }
  } finally {
    sending.value = false
  }
}

// 批量发送通知
const sendBatch = async () => {
  if (!batchForm.title || !batchForm.content) {
    ElMessage.warning('请填写必填项')
    return
  }

  try {
    const targetText = batchForm.targetRole === 'USER' ? '所有用户' : 
                      batchForm.targetRole === 'COACH' ? '所有教练' : '所有人'
    await ElMessageBox.confirm(`确定向${targetText}发送通知吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    sending.value = true
    await sendBatchNotification({
      targetRole: batchForm.targetRole,
      notificationType: batchForm.notificationType,
      title: batchForm.title,
      content: batchForm.content
    })
    ElMessage.success('批量通知发送成功')
    resetBatchForm()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '发送失败')
    }
  } finally {
    sending.value = false
  }
}

// 重置表单
const resetSingleForm = () => {
  singleForm.receiverId = ''
  singleForm.notificationType = 'SYSTEM'
  singleForm.title = ''
  singleForm.content = ''
  singleForm.relatedId = ''
}

const resetBatchForm = () => {
  batchForm.targetRole = 'USER'
  batchForm.notificationType = 'SYSTEM'
  batchForm.title = ''
  batchForm.content = ''
}
</script>

<style scoped>
.notification-manage {
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
