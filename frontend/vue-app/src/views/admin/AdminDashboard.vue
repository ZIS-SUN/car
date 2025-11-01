<template>
  <div class="admin-dashboard">
    <div v-loading="loading" class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #2c2c2c 0%, #404040 100%);">
          <el-icon :size="32"><user /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-label">总用户数</div>
          <div class="stat-value">{{ stats.totalUsers || 0 }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #d4af37 0%, #ffd700 100%);">
          <el-icon :size="32"><shop /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-label">合作门店</div>
          <div class="stat-value">{{ stats.totalShops || 0 }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #27ae60 0%, #2ecc71 100%);">
          <el-icon :size="32"><document /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-label">总订单数</div>
          <div class="stat-value">{{ stats.totalOrders || 0 }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #c0392b 0%, #e74c3c 100%);">
          <el-icon :size="32"><money /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-label">总收入</div>
          <div class="stat-value">¥{{ formatMoney(stats.totalRevenue) }}</div>
        </div>
      </div>
    </div>

    <div class="dashboard-content">
      <el-card class="chart-card">
        <template #header>
          <span>最近订单</span>
        </template>
        <el-table v-loading="ordersLoading" :data="recentOrders" stripe style="width: 100%">
          <el-table-column prop="orderNo" label="订单号" width="180" />
          <el-table-column prop="userName" label="用户" width="120" />
          <el-table-column prop="shopName" label="门店" width="150" />
          <el-table-column prop="serviceItems" label="服务项目" />
          <el-table-column prop="totalAmount" label="金额" width="100">
            <template #default="scope">
              ¥{{ scope.row.totalAmount }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180" />
        </el-table>
        <el-empty v-if="!ordersLoading && recentOrders.length === 0" description="暂无订单数据" />
      </el-card>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { User, Shop, Document, Money } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { adminAPI } from '../../api/admin.js'

export default {
  name: 'AdminDashboard',
  components: {
    User,
    Shop,
    Document,
    Money
  },
  setup() {
    const loading = ref(false)
    const ordersLoading = ref(false)
    const stats = ref({
      totalUsers: 0,
      totalShops: 0,
      totalOrders: 0,
      totalRevenue: 0
    })
    const recentOrders = ref([])

    const loadStatistics = async () => {
      loading.value = true
      try {
        const response = await adminAPI.getSystemStats()
        if (response.code === 200 && response.data) {
          stats.value = response.data
        }
      } catch (error) {
        console.error('加载统计数据失败:', error)
        ElMessage.error('加载统计数据失败')
      } finally {
        loading.value = false
      }
    }

    const loadRecentOrders = async () => {
      ordersLoading.value = true
      try {
        const response = await adminAPI.getRecentOrders({ current: 1, size: 10 })
        if (response.code === 200 && response.data) {
          recentOrders.value = response.data.records || []
        }
      } catch (error) {
        console.error('加载订单数据失败:', error)
        ElMessage.error('加载订单数据失败')
      } finally {
        ordersLoading.value = false
      }
    }

    const formatMoney = (value) => {
      return value ? Number(value).toLocaleString() : '0'
    }

    const getStatusType = (status) => {
      const statusMap = {
        1: 'warning',  // 待支付
        2: 'primary',  // 已支付
        3: 'success',  // 已完成
        4: 'danger'    // 已取消
      }
      return statusMap[status] || 'info'
    }

    const getStatusText = (status) => {
      const statusMap = {
        1: '待支付',
        2: '已支付',
        3: '已完成',
        4: '已取消'
      }
      return statusMap[status] || '未知'
    }

    onMounted(() => {
      loadStatistics()
      loadRecentOrders()
    })

    return {
      loading,
      ordersLoading,
      stats,
      recentOrders,
      formatMoney,
      getStatusType,
      getStatusText
    }
  }
}
</script>

<style scoped>
.admin-dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 1.8rem;
  font-weight: 700;
  color: #333;
}

.dashboard-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.chart-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
</style>
