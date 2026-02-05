import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/common/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/common/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/user',
    component: () => import('@/views/user/Dashboard.vue'),
    meta: { requiresAuth: true, role: 'USER' },
    children: [
      {
        path: 'dashboard',
        name: 'UserDashboard',
        component: { template: '<div></div>' },
        meta: { title: '用户首页' }
      },
      {
        path: 'coaches',
        name: 'CoachList',
        component: () => import('@/views/user/CoachList.vue'),
        meta: { title: '选择教练' }
      },
      {
        path: 'my-coach',
        name: 'MyCoach',
        component: () => import('@/views/user/MyCoach.vue'),
        meta: { title: '我的教练' }
      },
      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('@/views/user/Profile.vue'),
        meta: { title: '我的资料' }
      },
      {
        path: 'checkin',
        name: 'CheckIn',
        component: () => import('@/views/user/CheckIn.vue'),
        meta: { title: '每日打卡' }
      },
      {
        path: 'plans',
        name: 'FitnessPlans',
        component: () => import('@/views/user/FitnessPlans.vue'),
        meta: { title: '我的计划' }
      },
      {
        path: 'articles',
        name: 'ArticleList',
        component: () => import('@/views/user/ArticleList.vue'),
        meta: { title: '健身科普' }
      },
      {
        path: 'article/:id',
        name: 'ArticleDetail',
        component: () => import('@/views/user/ArticleDetail.vue'),
        meta: { title: '文章详情' }
      },
      {
        path: 'notifications',
        name: 'UserNotifications',
        component: () => import('@/views/common/Notifications.vue'),
        meta: { title: '消息通知' }
      }
    ]
  },
  {
    path: '/coach',
    component: () => import('@/views/coach/Dashboard.vue'),
    meta: { requiresAuth: true, role: 'COACH' },
    children: [
      {
        path: 'dashboard',
        name: 'CoachDashboard',
        component: { template: '<div></div>' },
        meta: { title: '教练首页' }
      },
      {
        path: 'certification',
        name: 'CertificationApply',
        component: () => import('@/views/coach/CertificationApply.vue'),
        meta: { title: '教练认证' }
      },
      {
        path: 'bind-applies',
        name: 'BindApplies',
        component: () => import('@/views/coach/BindApplies.vue'),
        meta: { title: '绑定申请' }
      },
      {
        path: 'students',
        name: 'Students',
        component: () => import('@/views/coach/Students.vue'),
        meta: { title: '我的学员' }
      },
      {
        path: 'plan-editor',
        name: 'PlanEditor',
        component: () => import('@/views/coach/PlanEditor.vue'),
        meta: { title: '制定计划' }
      },
      {
        path: 'articles',
        name: 'CoachArticles',
        component: () => import('@/views/coach/ArticleManage.vue'),
        meta: { title: '文章管理' }
      },
      {
        path: 'notifications',
        name: 'CoachNotifications',
        component: () => import('@/views/common/Notifications.vue'),
        meta: { title: '消息通知' }
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/views/admin/Dashboard.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: { template: '<div></div>' },
        meta: { title: '管理员首页' }
      },
      {
        path: 'coach-audit',
        name: 'CoachAudit',
        component: () => import('@/views/admin/CoachAudit.vue'),
        meta: { title: '教练审核' }
      },
      {
        path: 'plan-audit',
        name: 'PlanAudit',
        component: () => import('@/views/admin/PlanAudit.vue'),
        meta: { title: '计划审核' }
      },
      {
        path: 'article-audit',
        name: 'ArticleAudit',
        component: () => import('@/views/admin/ArticleAudit.vue'),
        meta: { title: '文章审核' }
      },
      {
        path: 'notifications',
        name: 'NotificationManage',
        component: () => import('@/views/admin/NotificationManage.vue'),
        meta: { title: '通知管理' }
      }
    ]
  },
  {
    path: '/',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const token = userStore.token
  
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 健身指导系统` : '健身指导系统'
  
  // 白名单路由
  const whiteList = ['/login', '/register']
  
  if (token) {
    // 已登录
    if (to.path === '/login') {
      // 根据角色跳转到对应首页
      const role = userStore.role
      if (role === 'ADMIN') {
        next('/admin/dashboard')
      } else if (role === 'COACH') {
        next('/coach/dashboard')
      } else {
        next('/user/dashboard')
      }
    } else {
      // 检查角色权限
      if (to.meta.role && to.meta.role !== userStore.role) {
        next('/login')
      } else {
        next()
      }
    }
  } else {
    // 未登录
    if (whiteList.includes(to.path)) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router
