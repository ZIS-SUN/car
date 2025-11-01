<template>
  <div class="shop-layout">
    <!-- 侧边栏 -->
    <div class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="logo">
        <img src="/logo.png" alt="Logo" v-if="!sidebarCollapsed">
        <img src="/logo-mini.png" alt="Logo" v-else>
        <span v-if="!sidebarCollapsed">门店管理</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        :collapse="sidebarCollapsed"
        router
        unique-opened
      >
        <el-menu-item index="/shop/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>仪表板</span>
        </el-menu-item>

        <el-menu-item index="/shop/orders">
          <el-icon><Document /></el-icon>
          <span>订单管理</span>
        </el-menu-item>

        <el-menu-item index="/shop/services">
          <el-icon><Tools /></el-icon>
          <span>服务项目</span>
        </el-menu-item>

        <el-menu-item index="/shop/packages">
          <el-icon><Box /></el-icon>
          <span>套餐管理</span>
        </el-menu-item>

        <el-menu-item index="/shop/profile">
          <el-icon><User /></el-icon>
          <span>门店信息</span>
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 顶部导航 -->
      <div class="top-header">
        <div class="header-left">
          <el-button
            type="text"
            @click="toggleSidebar"
            class="collapse-btn"
          >
            <el-icon>
              <Expand v-if="sidebarCollapsed" />
              <Fold v-else />
            </el-icon>
          </el-button>

          <el-breadcrumb separator="/">
            <el-breadcrumb-item
              v-for="item in breadcrumbList"
              :key="item.path"
              :to="item.path === '' ? '' : { path: item.path }"
            >
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <!-- 通知 -->
          <el-badge :value="unreadNotifications" class="notification-badge">
            <el-button type="text" @click="showNotifications">
              <el-icon><Bell /></el-icon>
            </el-button>
          </el-badge>

          <!-- 用户信息 -->
          <el-dropdown @command="handleUserCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="shopInfo.avatar">
                {{ shopInfo.name?.charAt(0) || '店' }}
              </el-avatar>
              <span class="username">{{ shopInfo.name || '门店' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  门店信息
                </el-dropdown-item>
                <el-dropdown-item command="password">
                  <el-icon><Lock /></el-icon>
                  修改密码
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 页面内容 -->
      <div class="content-area">
        <router-view />
      </div>
    </div>

    <!-- 通知抽屉 -->
    <el-drawer
      v-model="notificationDrawerVisible"
      title="系统通知"
      direction="rtl"
      size="400px"
    >
      <div class="notification-list">
        <div
          v-for="notification in notifications"
          :key="notification.id"
          class="notification-item"
          :class="{ unread: !notification.read }"
        >
          <div class="notification-content">
            <h4>{{ notification.title }}</h4>
            <p>{{ notification.content }}</p>
            <span class="notification-time">{{ formatTime(notification.createdAt) }}</span>
          </div>
          <el-button
            v-if="!notification.read"
            size="small"
            type="text"
            @click="markAsRead(notification.id)"
          >
            标为已读
          </el-button>
        </div>
        <div v-if="notifications.length === 0" class="empty-notifications">
          <el-empty description="暂无通知" />
        </div>
      </div>
    </el-drawer>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="400px"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入当前密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请确认新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="passwordLoading" @click="submitPassword">
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { shopAPI } from '@/api/shop'

const route = useRoute()
const router = useRouter()

const sidebarCollapsed = ref(false)
const shopInfo = reactive({
  name: '',
  avatar: ''
})

const notificationDrawerVisible = ref(false)
const notifications = ref([])
const unreadNotifications = ref(0)

const passwordDialogVisible = ref(false)
const passwordLoading = ref(false)
const passwordFormRef = ref()

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 计算当前激活的菜单项
const activeMenu = computed(() => route.path)

// 面包屑导航
const breadcrumbList = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  const breadcrumbs = []

  matched.forEach((match, index) => {
    if (index === 0) {
      breadcrumbs.push({
        title: '首页',
        path: '/shop/dashboard'
      })
    }
    if (match.meta.title) {
      breadcrumbs.push({
        title: match.meta.title,
        path: match.path === route.path ? '' : match.path
      })
    }
  })

  return breadcrumbs
})

onMounted(() => {
  loadShopInfo()
  loadNotifications()
})

watch(() => route.path, () => {
  // 路由变化时可以执行一些操作
})

const loadShopInfo = () => {
  const savedShopInfo = localStorage.getItem('shopInfo')
  if (savedShopInfo) {
    const info = JSON.parse(savedShopInfo)
    Object.assign(shopInfo, info)
  }
}

const loadNotifications = async () => {
  try {
    // 模拟通知数据，实际应该从API获取
    notifications.value = [
      {
        id: 1,
        title: '新订单提醒',
        content: '您有一个新的预约订单待确认',
        read: false,
        createdAt: new Date(Date.now() - 30 * 60 * 1000)
      },
      {
        id: 2,
        title: '系统维护通知',
        content: '系统将于今晚22:00-23:00进行维护',
        read: true,
        createdAt: new Date(Date.now() - 2 * 60 * 60 * 1000)
      }
    ]
    unreadNotifications.value = notifications.value.filter(n => !n.read).length
  } catch (error) {
    console.error('获取通知失败:', error)
  }
}

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const showNotifications = () => {
  notificationDrawerVisible.value = true
}

const markAsRead = async (notificationId) => {
  try {
    // 调用API标记为已读
    const notification = notifications.value.find(n => n.id === notificationId)
    if (notification) {
      notification.read = true
      unreadNotifications.value = notifications.value.filter(n => !n.read).length
      ElMessage.success('已标记为已读')
    }
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

const handleUserCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/shop/profile')
      break
    case 'password':
      passwordDialogVisible.value = true
      break
    case 'logout':
      handleLogout()
      break
  }
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await shopAPI.logout()
    } catch (error) {
      console.error('退出登录失败:', error)
    } finally {
      localStorage.removeItem('shopToken')
      localStorage.removeItem('shopInfo')
      router.push('/shop/login')
      ElMessage.success('已退出登录')
    }
  }).catch(() => {})
}

const submitPassword = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()
    passwordLoading.value = true

    const response = await shopAPI.changePassword(passwordForm)
    if (response.code === 200) {
      ElMessage.success('密码修改成功')
      passwordDialogVisible.value = false
      resetPasswordForm()
    } else {
      ElMessage.error(response.message || '密码修改失败')
    }
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error('密码修改失败，请稍后重试')
  } finally {
    passwordLoading.value = false
  }
}

const resetPasswordForm = () => {
  Object.assign(passwordForm, {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  })
}

const formatTime = (time) => {
  const now = new Date()
  const diff = now - time
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return time.toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.shop-layout {
  display: flex;
  height: 100vh;
  background: var(--harmony-bg-secondary);
}

.sidebar {
  width: 250px;
  background: var(--harmony-primary-gradient);
  transition: width 0.3s;
  overflow: hidden;
  box-shadow: var(--harmony-shadow-lg);
}

.sidebar.collapsed {
  width: 64px;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--harmony-primary-dark);
  color: white;
  font-size: 18px;
  font-weight: 600;
  gap: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.logo img {
  width: 32px;
  height: 32px;
}

.sidebar-menu {
  border: none;
  background: transparent;
}

.sidebar-menu .el-menu-item {
  color: rgba(255, 255, 255, 0.8);
  border: none;
  margin: 4px 8px;
  border-radius: var(--harmony-radius-md);
  transition: all var(--harmony-transition-base);
}

.sidebar-menu .el-menu-item:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
  transform: translateX(4px);
}

.sidebar-menu .el-menu-item.is-active {
  color: #fff;
  background: var(--harmony-gradient-2);
  box-shadow: var(--harmony-shadow-md);
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.top-header {
  height: 64px;
  background: var(--harmony-bg-primary);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: var(--harmony-shadow-sm);
  z-index: 10;
  border-bottom: 1px solid var(--harmony-border);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 16px;
  color: #666;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.notification-badge {
  margin-right: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.3s;
}

.user-info:hover {
  background: #f5f5f5;
}

.username {
  color: #666;
  font-size: 14px;
}

.content-area {
  flex: 1;
  padding: var(--harmony-space-xl);
  overflow-y: auto;
  background: var(--harmony-bg-secondary);
}

.notification-list {
  padding: 16px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.3s;
}

.notification-item:hover {
  background: #fafafa;
}

.notification-item.unread {
  background: #f6ffed;
  border-left: 3px solid #52c41a;
}

.notification-content h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 500;
  color: #262626;
}

.notification-content p {
  margin: 0 0 8px 0;
  font-size: 12px;
  color: #666;
  line-height: 1.5;
}

.notification-time {
  font-size: 11px;
  color: #999;
}

.empty-notifications {
  padding: 40px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: -250px;
    top: 0;
    bottom: 0;
    z-index: 1000;
    transition: left 0.3s;
  }

  .sidebar.show {
    left: 0;
  }

  .sidebar.collapsed {
    width: 250px;
  }

  .main-content {
    margin-left: 0;
  }

  .content-area {
    padding: 16px;
  }

  .top-header {
    padding: 0 16px;
  }

  .username {
    display: none;
  }
}
</style>