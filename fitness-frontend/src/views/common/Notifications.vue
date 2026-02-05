<template>
  <div class="notifications">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>消息通知</span>
          <el-button type="primary" size="small" @click="markAllRead">全部已读</el-button>
        </div>
      </template>

      <!-- 筛选 -->
      <el-radio-group v-model="filterType" @change="loadData" style="margin-bottom: 20px">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button :label="0">未读</el-radio-button>
        <el-radio-button :label="1">已读</el-radio-button>
      </el-radio-group>

      <!-- 通知列表 -->
      <el-timeline v-loading="loading">
        <el-timeline-item
          v-for="notification in notificationList"
          :key="notification.notificationId"
          :timestamp="formatTime(notification.createdAt)"
          placement="top"
        >
          <el-card :class="{ 'unread-notification': notification.isRead === 0 }">
            <div class="notification-header">
              <h4>{{ notification.title }}</h4>
              <el-button
                v-if="notification.isRead === 0"
                type="primary"
                size="small"
                text
                @click="markRead(notification.notificationId)"
              >
                标记已读
              </el-button>
              <el-button
                type="danger"
                size="small"
                text
                @click="deleteNotif(notification.notificationId)"
              >
                删除
              </el-button>
            </div>
            <p>{{ notification.content }}</p>
            <el-tag v-if="notification.isRead === 0" type="danger" size="small">未读</el-tag>
            <el-tag v-else type="info" size="small">已读</el-tag>
          </el-card>
        </el-timeline-item>
      </el-timeline>

      <el-empty v-if="!loading && notificationList.length === 0" description="暂无通知" />

      <!-- 分页 -->
      <el-pagination
        v-if="notificationList.length > 0"
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadData"
        @size-change="loadData"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyNotifications, markAsRead, markAllAsRead, deleteNotification } from '@/api/notification'

const loading = ref(false)
const notificationList = ref([])
const filterType = ref('')

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getMyNotifications({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      isRead: filterType.value === '' ? undefined : filterType.value
    })
    notificationList.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载通知失败')
  } finally {
    loading.value = false
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 19)
}

// 标记已读
const markRead = async (notificationId) => {
  try {
    await markAsRead(notificationId)
    ElMessage.success('已标记为已读')
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 全部已读
const markAllRead = async () => {
  try {
    await markAllAsRead()
    ElMessage.success('已全部标记为已读')
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 删除通知
const deleteNotif = async (notificationId) => {
  try {
    await ElMessageBox.confirm('确定删除此通知吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteNotification(notificationId)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.notifications {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.unread-notification {
  background: #f0f9ff;
  border-left: 3px solid #409eff;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.notification-header h4 {
  margin: 0;
  font-size: 16px;
}

.notification-header .el-button {
  margin-left: 10px;
}
</style>
