<template>
  <div class="admin-dashboard">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="220px" class="sidebar">
        <div class="logo">
          <el-icon :size="32" color="#fff"><TrophyBase /></el-icon>
          <h3>管理员端</h3>
        </div>
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          @select="handleMenuSelect"
          background-color="#2c3e50"
          text-color="#ecf0f1"
          active-text-color="#3498db"
        >
          <el-menu-item index="dashboard">
            <el-icon><HomeFilled /></el-icon>
            <span>工作台</span>
          </el-menu-item>
          <el-menu-item index="coach-audit">
            <el-icon><UserFilled /></el-icon>
            <span>教练审核</span>
          </el-menu-item>
          <el-menu-item index="plan-audit">
            <el-icon><Document /></el-icon>
            <span>计划审核</span>
          </el-menu-item>
          <el-menu-item index="article-audit">
            <el-icon><Reading /></el-icon>
            <span>文章审核</span>
          </el-menu-item>
          <el-menu-item index="notifications">
            <el-icon><Bell /></el-icon>
            <span>通知管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-container>
        <el-header class="header">
          <div class="header-content">
            <div class="header-left">
              <h2>健身指导系统</h2>
              <el-tag type="warning" size="small">管理员</el-tag>
            </div>
            <div class="header-right">
              <el-dropdown>
                <span class="user-dropdown">
                  <el-avatar :size="32" style="margin-right: 8px">
                    {{ userStore.userInfo.name?.charAt(0) }}
                  </el-avatar>
                  <span>{{ userStore.userInfo.name }}</span>
                  <el-icon><ArrowDown /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleLogout">
                      <el-icon><SwitchButton /></el-icon>
                      退出登录
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </el-header>

        <el-main class="main-content">
          <router-view v-if="$route.path !== '/admin/dashboard'" />
          <div v-else class="dashboard-home">
            <div class="welcome-card">
              <h2>👋 欢迎回来，{{ userStore.userInfo.name }}</h2>
              <p>今天也要认真工作哦！</p>
            </div>

            <el-row :gutter="20" style="margin-top: 20px">
              <el-col :span="8">
                <el-card shadow="hover" class="feature-card" @click="handleMenuSelect('coach-audit')">
                  <div class="card-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
                    <el-icon :size="40"><UserFilled /></el-icon>
                  </div>
                  <div class="card-content">
                    <h3>教练审核</h3>
                    <p>审核教练认证申请</p>
                  </div>
                  <el-icon class="card-arrow"><ArrowRight /></el-icon>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card shadow="hover" class="feature-card" @click="handleMenuSelect('plan-audit')">
                  <div class="card-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
                    <el-icon :size="40"><Document /></el-icon>
                  </div>
                  <div class="card-content">
                    <h3>计划审核</h3>
                    <p>审核健身计划</p>
                  </div>
                  <el-icon class="card-arrow"><ArrowRight /></el-icon>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card shadow="hover" class="feature-card" @click="handleMenuSelect('article-audit')">
                  <div class="card-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
                    <el-icon :size="40"><Reading /></el-icon>
                  </div>
                  <div class="card-content">
                    <h3>文章审核</h3>
                    <p>审核健身科普文章</p>
                  </div>
                  <el-icon class="card-arrow"><ArrowRight /></el-icon>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { HomeFilled, UserFilled, Document, Reading, Bell, ArrowDown, ArrowRight, SwitchButton, TrophyBase } from '@element-plus/icons-vue'
import { logout } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const activeMenu = ref('dashboard')

// 监听路由变化更新菜单激活状态
watch(() => route.path, (newPath) => {
  if (newPath === '/admin/dashboard') {
    activeMenu.value = 'dashboard'
  } else if (newPath === '/admin/coach-audit') {
    activeMenu.value = 'coach-audit'
  } else if (newPath === '/admin/plan-audit') {
    activeMenu.value = 'plan-audit'
  } else if (newPath === '/admin/article-audit') {
    activeMenu.value = 'article-audit'
  } else if (newPath === '/admin/notifications') {
    activeMenu.value = 'notifications'
  }
}, { immediate: true })

const handleMenuSelect = (index) => {
  activeMenu.value = index
  
  const routeMap = {
    'dashboard': '/admin/dashboard',
    'coach-audit': '/admin/coach-audit',
    'plan-audit': '/admin/plan-audit',
    'article-audit': '/admin/article-audit',
    'notifications': '/admin/notifications'
  }
  
  if (routeMap[index] && routeMap[index] !== router.currentRoute.value.path) {
    router.push(routeMap[index])
  }
}

const handleLogout = async () => {
  try {
    await logout()
    userStore.logout()
    ElMessage.success('退出成功')
    router.push('/login')
  } catch (error) {
    ElMessage.error('退出失败')
  }
}
</script>

<style scoped>
.admin-dashboard {
  height: 100vh;
  background: #f5f7fa;
}

.admin-dashboard .el-container {
  height: 100%;
}

.sidebar {
  background: #2c3e50;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  height: 100vh;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background: #34495e;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo h3 {
  color: #fff;
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.sidebar-menu {
  border-right: none;
  padding: 10px 0;
}

.sidebar-menu .el-menu-item {
  margin: 4px 12px;
  border-radius: 8px;
  transition: all 0.3s;
}

.sidebar-menu .el-menu-item:hover {
  background: rgba(52, 152, 219, 0.1) !important;
}

.sidebar-menu .el-menu-item.is-active {
  background: #3498db !important;
  color: #fff !important;
}

.header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  padding: 0 24px;
}

.header-content {
  height: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 8px;
  transition: all 0.3s;
}

.user-dropdown:hover {
  background: #f5f7fa;
}

.main-content {
  padding: 24px;
  background: #f5f7fa;
}

.dashboard-home {
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px;
  border-radius: 16px;
  color: #fff;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
}

.welcome-card h2 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
}

.welcome-card p {
  margin: 0;
  font-size: 16px;
  opacity: 0.9;
}

.feature-card {
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 12px;
  overflow: hidden;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.feature-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  padding: 24px;
  gap: 16px;
}

.card-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.card-content {
  flex: 1;
}

.card-content h3 {
  margin: 0 0 4px 0;
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.card-content p {
  margin: 0;
  font-size: 14px;
  color: #7f8c8d;
}

.card-arrow {
  font-size: 20px;
  color: #bdc3c7;
  transition: all 0.3s;
}

.feature-card:hover .card-arrow {
  color: #3498db;
  transform: translateX(4px);
}
</style>
