<template>
  <header class="harmony-header harmony-glass">
    <div class="harmony-container">
      <div class="harmony-header-content">
        <div class="harmony-logo">
          <div class="harmony-logo-icon">🚗</div>
          <h1 class="harmony-logo-text">
            <span class="harmony-text-gradient">汽车保养</span>预约系统
          </h1>
        </div>
        <nav class="harmony-nav">
          <ul class="harmony-nav-list">
            <li><router-link to="/" class="harmony-nav-link">首页</router-link></li>
            <li><router-link to="/shops" class="harmony-nav-link">门店</router-link></li>
            <li><router-link to="/services" class="harmony-nav-link">服务</router-link></li>
            <li><router-link to="/announcements" class="harmony-nav-link">公告</router-link></li>
            <li><router-link to="/about" class="harmony-nav-link">关于</router-link></li>
            <li v-if="isAdmin"><router-link to="/admin" class="harmony-nav-link harmony-admin-link">管理后台</router-link></li>
          </ul>
        </nav>
        <div class="harmony-user-actions">
          <template v-if="!isLoggedIn">
            <router-link to="/login" class="harmony-btn harmony-btn-ghost">登录</router-link>
            <router-link to="/register" class="harmony-btn harmony-btn-primary">注册</router-link>
          </template>
          <template v-else>
            <div class="harmony-user-welcome">
              <div class="harmony-avatar">{{ displayName.charAt(0).toUpperCase() }}</div>
              <span class="harmony-username">{{ displayName }}</span>
            </div>
            <el-dropdown class="harmony-dropdown">
              <el-button type="primary" class="harmony-btn harmony-btn-primary">
                会员中心<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu class="harmony-dropdown-menu">
                  <el-dropdown-item @click="handleMemberCenter">
                    <el-icon><user /></el-icon>个人中心
                  </el-dropdown-item>
                  <el-dropdown-item v-if="!isAdmin" @click="handleMyOrders">
                    <el-icon><document /></el-icon>我的订单
                  </el-dropdown-item>
                  <el-dropdown-item v-if="!isAdmin" @click="handleMyReviews">
                    <el-icon><star /></el-icon>我的评价
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">
                    <el-icon><switch-button /></el-icon>退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </div>
      </div>
    </div>
  </header>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  ArrowDown, 
  User, 
  Document, 
  Star, 
  SwitchButton 
} from '@element-plus/icons-vue'

export default {
  name: 'AppHeader',
  components: {
    ArrowDown,
    User,
    Document,
    Star,
    SwitchButton
  },
  setup() {
    const router = useRouter()
    const username = ref('用户')
    const isLoggedIn = ref(false)
    const isAdmin = ref(false)

    // 检查登录状态的函数
    const checkLoginStatus = () => {
      isLoggedIn.value = localStorage.getItem('token') !== null
      isAdmin.value = localStorage.getItem('userRole') === 'admin'
      const savedUsername = localStorage.getItem('username')
      if (savedUsername) {
        username.value = savedUsername
      }
    }

    const displayName = computed(() => {
      // 清理用户名，去掉多余的字符
      let name = username.value.trim()
      // 如果用户名包含多个单词，只取最后一个（通常是实际用户名）
      if (name.includes(' ')) {
        name = name.split(' ').pop()
      }
      return name || '用户'
    })

    const handleMemberCenter = () => {
      router.push('/member-center')
    }

    const handleMyOrders = () => {
      router.push('/my-orders')
    }

    const handleMyReviews = () => {
      router.push('/my-reviews')
    }

    const handleLogout = () => {
      console.log('退出登录')
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('userRole')
      localStorage.removeItem('userInfo')
      checkLoginStatus()
      router.push('/')
    }

    onMounted(() => {
      checkLoginStatus()

      // 监听自定义登录事件
      window.addEventListener('userLoggedIn', checkLoginStatus)
      window.addEventListener('userLoggedOut', checkLoginStatus)
    })

    return {
      isLoggedIn,
      isAdmin,
      username,
      displayName,
      handleMemberCenter,
      handleMyOrders,
      handleMyReviews,
      handleLogout
    }
  }
}
</script>

<style scoped>
.harmony-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  padding: var(--harmony-space-md) 0;
  border-bottom: 1px solid var(--harmony-border);
  background: var(--harmony-bg-primary);
}

.harmony-header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--harmony-space-lg);
}

.harmony-logo {
  display: flex;
  align-items: center;
  gap: var(--harmony-space-md);
}

.harmony-logo-icon {
  font-size: 2rem;
  background: var(--harmony-primary-gradient);
  border-radius: 50%;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--harmony-shadow-md);
}

.harmony-logo-text {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--harmony-text-primary);
  margin: 0;
}

.harmony-nav-list {
  display: flex;
  list-style: none;
  gap: var(--harmony-space-lg);
  margin: 0;
  padding: 0;
}

.harmony-nav-link {
  text-decoration: none;
  color: var(--harmony-text-secondary);
  font-weight: 500;
  padding: var(--harmony-space-sm) var(--harmony-space-md);
  border-radius: var(--harmony-radius-md);
  transition: all var(--harmony-transition-base);
  position: relative;
}

.harmony-nav-link:hover {
  color: var(--harmony-primary);
  background: var(--harmony-primary-light);
}

.harmony-nav-link.router-link-active {
  color: var(--harmony-primary);
  background: var(--harmony-primary-light);
}

.harmony-nav-link.router-link-active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 3px;
  background: var(--harmony-primary-gradient);
  border-radius: var(--harmony-radius-full);
}

.harmony-admin-link {
  background: var(--harmony-gradient-2) !important;
  color: white !important;
  border-radius: var(--harmony-radius-md) !important;
  padding: var(--harmony-space-sm) var(--harmony-space-md) !important;
  font-weight: 600;
  box-shadow: var(--harmony-shadow-sm);
}

.harmony-admin-link:hover {
  background: var(--harmony-gradient-2) !important;
  transform: translateY(-2px);
  box-shadow: var(--harmony-shadow-md) !important;
}

.harmony-user-actions {
  display: flex;
  align-items: center;
  gap: var(--harmony-space-md);
}

.harmony-user-welcome {
  display: flex;
  align-items: center;
  gap: var(--harmony-space-sm);
  margin-right: var(--harmony-space-md);
}

.harmony-username {
  color: var(--harmony-text-primary);
  font-weight: 500;
}

.harmony-btn {
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 10px 20px;
  border-radius: var(--harmony-radius-full);
  font-weight: 500;
  font-size: 14px;
  transition: all var(--harmony-transition-base);
  border: none;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.harmony-btn::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width var(--harmony-transition-base), height var(--harmony-transition-base);
}

.harmony-btn:hover::before {
  width: 200px;
  height: 200px;
}

.harmony-btn > * {
  position: relative;
  z-index: 1;
}

.harmony-btn-ghost {
  background: transparent;
  color: var(--harmony-primary);
  border: 2px solid var(--harmony-primary);
}

.harmony-btn-ghost:hover {
  background: var(--harmony-primary-light);
  transform: translateY(-2px);
}

.harmony-btn-primary {
  background: var(--harmony-primary-gradient);
  color: white;
  box-shadow: 0 4px 12px rgba(0, 125, 255, 0.3);
}

.harmony-btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 125, 255, 0.4);
}

.harmony-dropdown-menu {
  border-radius: var(--harmony-radius-md) !important;
  box-shadow: var(--harmony-shadow-lg) !important;
  border: 1px solid var(--harmony-border) !important;
}

.harmony-dropdown-menu .el-dropdown-menu__item {
  display: flex;
  align-items: center;
  gap: var(--harmony-space-sm);
  padding: var(--harmony-space-md) var(--harmony-space-lg);
  transition: all var(--harmony-transition-base);
}

.harmony-dropdown-menu .el-dropdown-menu__item:hover {
  background: var(--harmony-primary-light) !important;
  color: var(--harmony-primary) !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .harmony-header-content {
    flex-wrap: wrap;
    gap: var(--harmony-space-md);
  }
  
  .harmony-nav {
    order: 3;
    width: 100%;
  }
  
  .harmony-nav-list {
    justify-content: center;
    flex-wrap: wrap;
    gap: var(--harmony-space-md);
  }
  
  .harmony-logo-text {
    font-size: 1.2rem;
  }
  
  .harmony-user-actions {
    gap: var(--harmony-space-sm);
  }
  
  .harmony-btn {
    padding: 8px 16px;
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .harmony-logo-text {
    display: none;
  }
  
  .harmony-nav-list {
    gap: var(--harmony-space-sm);
  }
  
  .harmony-nav-link {
    padding: var(--harmony-space-xs) var(--harmony-space-sm);
    font-size: 14px;
  }
}
</style>