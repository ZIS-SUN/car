<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <div class="admin-logo">
        <div class="admin-logo-icon">🚗</div>
        <h2>管理后台</h2>
      </div>
      <nav class="admin-nav">
        <router-link to="/admin/dashboard" class="admin-nav-item">
          <el-icon><odometer /></el-icon>
          <span>数据概览</span>
        </router-link>
        <router-link to="/admin/users" class="admin-nav-item">
          <el-icon><user /></el-icon>
          <span>用户管理</span>
        </router-link>
        <router-link to="/admin/shops" class="admin-nav-item">
          <el-icon><shop /></el-icon>
          <span>门店管理</span>
        </router-link>
        <router-link to="/admin/orders" class="admin-nav-item">
          <el-icon><document /></el-icon>
          <span>订单管理</span>
        </router-link>
        <router-link to="/admin/reviews" class="admin-nav-item">
          <el-icon><star /></el-icon>
          <span>评价管理</span>
        </router-link>
        <router-link to="/admin/members" class="admin-nav-item">
          <el-icon><medal /></el-icon>
          <span>会员管理</span>
        </router-link>
        <router-link to="/admin/announcements" class="admin-nav-item">
          <el-icon><bell /></el-icon>
          <span>公告管理</span>
        </router-link>
        <router-link to="/admin/price-monitor" class="admin-nav-item">
          <el-icon><money /></el-icon>
          <span>价格监管</span>
        </router-link>
        <router-link to="/admin/data-export" class="admin-nav-item">
          <el-icon><download /></el-icon>
          <span>数据导出</span>
        </router-link>
        <router-link to="/admin/data-statistics" class="admin-nav-item">
          <el-icon><data-analysis /></el-icon>
          <span>数据统计</span>
        </router-link>
        <router-link to="/admin/settings" class="admin-nav-item">
          <el-icon><setting /></el-icon>
          <span>系统设置</span>
        </router-link>
        <a href="/" class="admin-nav-item admin-nav-exit">
          <el-icon><back /></el-icon>
          <span>返回前台</span>
        </a>
      </nav>
    </aside>
    <main class="admin-main">
      <div class="admin-header">
        <h1 class="admin-title">{{ pageTitle }}</h1>
        <div class="admin-user">
          <span>管理员：{{ username }}</span>
          <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
        </div>
      </div>
      <div class="admin-content">
        <router-view></router-view>
      </div>
    </main>
  </div>
</template>

<script>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Odometer,
  User,
  Shop,
  Document,
  Star,
  Medal,
  Bell,
  Money,
  Download,
  DataAnalysis,
  Setting,
  Back
} from '@element-plus/icons-vue'

export default {
  name: 'AdminLayout',
  components: {
    Odometer,
    User,
    Shop,
    Document,
    Star,
    Medal,
    Bell,
    Money,
    Download,
    DataAnalysis,
    Setting,
    Back
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const username = ref(localStorage.getItem('username') || '管理员')

    const pageTitle = computed(() => {
      const titles = {
        'AdminDashboard': '数据概览',
        'UserManagement': '用户管理',
        'ShopManagement': '门店管理',
        'OrderManagement': '订单管理',
        'ReviewManagement': '评价管理',
        'MemberManagement': '会员管理',
        'SystemSettings': '系统设置'
      }
      return titles[route.name] || '管理后台'
    })

    const handleLogout = () => {
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('userRole')
      router.push('/login')
    }

    return {
      pageTitle,
      username,
      handleLogout
    }
  }
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: #fafafa;
}

.admin-sidebar {
  width: 260px;
  min-width: 260px;
  background: linear-gradient(135deg, #2c2c2c 0%, #404040 100%);
  color: white;
  padding: 24px 16px;
  box-shadow: 2px 0 8px rgba(44, 44, 44, 0.15);
  overflow-y: auto;
}

.admin-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 32px;
  padding: 0 8px 20px 8px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.admin-logo-icon {
  font-size: 2rem;
  background: linear-gradient(135deg, #d4af37 0%, #ffd700 100%);
  border-radius: 12px;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #2c2c2c;
  box-shadow: 0 2px 8px rgba(212, 175, 55, 0.3);
}

.admin-logo h2 {
  font-size: 1.3rem;
  margin: 0;
}

.admin-nav {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.admin-nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 10px;
  color: white;
  text-decoration: none;
  transition: all 0.3s;
  font-weight: 500;
  font-size: 14px;
  white-space: nowrap;
  min-height: 48px;
}

.admin-nav-item:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateX(4px);
}

.admin-nav-item.router-link-active {
  background: linear-gradient(135deg, #d4af37 0%, #ffd700 100%);
  color: #2c2c2c;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(212, 175, 55, 0.3);
}

.admin-nav-item .el-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.admin-nav-exit {
  margin-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  padding-top: 20px;
}

.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.admin-header {
  background: white;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.admin-title {
  font-size: 1.8rem;
  margin: 0;
}

.admin-user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .admin-sidebar {
    width: 220px;
    min-width: 220px;
  }
  
  .admin-nav-item {
    padding: 12px 14px;
    font-size: 13px;
  }
  
  .admin-logo h2 {
    font-size: 1.2rem;
  }
}

@media (max-width: 768px) {
  .admin-layout {
    flex-direction: column;
  }
  
  .admin-sidebar {
    width: 100%;
    min-width: 100%;
    height: auto;
    padding: 16px;
    box-shadow: 0 2px 8px rgba(44, 44, 44, 0.15);
  }
  
  .admin-logo {
    margin-bottom: 16px;
    padding-bottom: 16px;
  }
  
  .admin-nav {
    flex-direction: row;
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .admin-nav-item {
    flex: 1;
    min-width: 120px;
    justify-content: center;
    padding: 12px 8px;
    font-size: 12px;
  }
  
  .admin-nav-exit {
    margin-top: 8px;
    padding-top: 8px;
    flex: 100%;
  }
  
  .admin-header {
    padding: 16px;
  }
  
  .admin-title {
    font-size: 1.5rem;
  }
  
  .admin-content {
    padding: 16px;
  }
}
</style>
