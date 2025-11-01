<template>
  <div class="harmony-shop-dashboard">
    <div class="harmony-dashboard-header">
      <h1 class="harmony-page-title">
        <span class="harmony-text-gradient">门店管理面板</span>
      </h1>
      <div class="harmony-header-actions">
        <el-button type="primary" class="harmony-btn harmony-btn-primary" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          退出登录
        </el-button>
      </div>
    </div>

    <!-- 门店信息卡片 -->
    <div class="harmony-card harmony-shop-info-card">
      <div class="harmony-card-header">
        <h3 class="harmony-card-title">
          <el-icon class="harmony-card-icon"><Shop /></el-icon>
          门店信息
        </h3>
        <el-button size="small" class="harmony-btn harmony-btn-ghost" @click="editShopInfo">
          <el-icon><Edit /></el-icon>
          编辑
        </el-button>
      </div>
      <div class="harmony-shop-info">
        <div class="harmony-info-item">
          <label>门店名称：</label>
          <span>{{ shopInfo.name || '未设置' }}</span>
        </div>
        <div class="harmony-info-item">
          <label>门店地址：</label>
          <span>{{ shopInfo.address || '未设置' }}</span>
        </div>
        <div class="harmony-info-item">
          <label>联系电话：</label>
          <span>{{ shopInfo.phone || '未设置' }}</span>
        </div>
        <div class="harmony-info-item">
          <label>营业时间：</label>
          <span>{{ shopInfo.businessHours || '未设置' }}</span>
        </div>
        <div class="harmony-info-item">
          <label>门店状态：</label>
          <el-tag :type="getStatusTagType(shopInfo.status)" class="harmony-status-tag">
            {{ getStatusText(shopInfo.status) }}
          </el-tag>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="harmony-stats-grid">
      <div class="harmony-stat-card harmony-card-interactive">
        <div class="harmony-stat-content">
          <div class="harmony-stat-icon harmony-stat-pending">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="harmony-stat-info">
            <h3 class="harmony-stat-number">{{ stats.pendingOrders }}</h3>
            <p class="harmony-stat-label">待处理订单</p>
          </div>
        </div>
      </div>

      <div class="harmony-stat-card harmony-card-interactive">
        <div class="harmony-stat-content">
          <div class="harmony-stat-icon harmony-stat-processing">
            <el-icon><Loading /></el-icon>
          </div>
          <div class="harmony-stat-info">
            <h3 class="harmony-stat-number">{{ stats.processingOrders }}</h3>
            <p class="harmony-stat-label">进行中订单</p>
          </div>
        </div>
      </div>

      <div class="harmony-stat-card harmony-card-interactive">
        <div class="harmony-stat-content">
          <div class="harmony-stat-icon harmony-stat-completed">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="harmony-stat-info">
            <h3 class="harmony-stat-number">{{ stats.completedOrders }}</h3>
            <p class="harmony-stat-label">已完成订单</p>
          </div>
        </div>
      </div>

      <div class="harmony-stat-card harmony-card-interactive">
        <div class="harmony-stat-content">
          <div class="harmony-stat-icon harmony-stat-revenue">
            <el-icon><Money /></el-icon>
          </div>
          <div class="harmony-stat-info">
            <h3 class="harmony-stat-number">¥{{ stats.monthlyRevenue }}</h3>
            <p class="harmony-stat-label">本月收入</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 今日预约 -->
    <div class="harmony-card harmony-appointments-card">
      <div class="harmony-card-header">
        <h3 class="harmony-card-title">
          <el-icon class="harmony-card-icon"><Calendar /></el-icon>
          今日预约
        </h3>
        <router-link to="/shop/orders">
          <el-button size="small" class="harmony-btn harmony-btn-ghost">
            <el-icon><View /></el-icon>
            查看全部
          </el-button>
        </router-link>
      </div>

      <el-table :data="todayAppointments" style="width: 100%">
        <el-table-column prop="time" label="预约时间" width="150" />
        <el-table-column prop="customerName" label="客户姓名" width="120" />
        <el-table-column prop="vehicleInfo" label="车辆信息" />
        <el-table-column prop="services" label="服务项目" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              size="small"
              type="primary"
              @click="confirmAppointment(row)"
            >
              确认
            </el-button>
            <el-button
              size="small"
              @click="viewAppointmentDetail(row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div v-if="todayAppointments.length === 0" class="harmony-empty-state">
        <el-empty description="今日暂无预约" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Shop, 
  Edit, 
  Calendar, 
  View, 
  Clock, 
  Loading, 
  CircleCheck, 
  Money,
  SwitchButton 
} from '@element-plus/icons-vue'
import { shopAPI } from '@/api/shop'

const router = useRouter()

const shopInfo = reactive({
  name: '',
  address: '',
  phone: '',
  businessHours: '',
  status: 0
})

const stats = reactive({
  pendingOrders: 0,
  processingOrders: 0,
  completedOrders: 0,
  monthlyRevenue: '0.00'
})

const todayAppointments = ref([])

const shopStatusMap = {
  0: { text: '待审核', tag: 'warning' },
  1: { text: '营业中', tag: 'success' },
  2: { text: '已禁用', tag: 'danger' },
  3: { text: '已驳回', tag: 'danger' }
}

const appointmentStatusMap = {
  1: { text: '待服务', tag: 'warning' },
  2: { text: '进行中', tag: 'primary' },
  3: { text: '已完成', tag: 'success' },
  4: { text: '已取消', tag: 'danger' },
  5: { text: '已违约', tag: 'danger' }
}

const formatCurrency = (value) => {
  if (value === null || value === undefined) {
    return '0.00'
  }
  const numberValue = Number(value)
  return Number.isNaN(numberValue) ? '0.00' : numberValue.toFixed(2)
}

const formatDate = (value) => {
  const date = value instanceof Date ? value : new Date(value)
  if (Number.isNaN(date.getTime())) {
    return ''
  }
  return date.toISOString().split('T')[0]
}

const loadShopInfo = async () => {
  try {
    const response = await shopAPI.getShopInfo()
    if (response.code === 200 && response.data) {
      const data = response.data
      Object.assign(shopInfo, {
        name: data.shopName || '未命名门店',
        address: data.address || '地址待完善',
        phone: data.phone || '联系电话暂无',
        businessHours: data.businessHours || '营业时间待设置',
        status: data.status ?? 0
      })
      localStorage.setItem('shopInfo', JSON.stringify(shopInfo))
      return
    }
  } catch (error) {
    console.error('获取门店信息失败:', error)
  }

  const savedShopInfo = localStorage.getItem('shopInfo')
  if (savedShopInfo) {
    Object.assign(shopInfo, JSON.parse(savedShopInfo))
  }
}

const loadOrderStats = async () => {
  try {
    const response = await shopAPI.getOrderStats()
    if (response.code === 200 && response.data) {
      const data = response.data
      stats.pendingOrders = data.pendingCount || 0
      stats.processingOrders = data.inProgressCount || 0
      stats.completedOrders = data.completedCount || 0
      stats.monthlyRevenue = formatCurrency(data.revenue?.totalRevenue)
    }
  } catch (error) {
    console.error('获取订单统计失败:', error)
  }
}

const loadTodayAppointments = async () => {
  try {
    const today = formatDate(new Date())
    const response = await shopAPI.getShopAppointments({ current: 1, size: 8, date: today })
    if (response.code === 200 && response.data) {
      const records = response.data.records || []
      todayAppointments.value = await Promise.all(records.map(async (record) => {
        let appointmentDetail
        try {
          const detailResp = await shopAPI.getAppointmentDetail(record.id)
          if (detailResp.code === 200) {
            appointmentDetail = detailResp.data
          }
        } catch (error) {
          console.error('获取预约详情失败:', error)
        }

        const appointment = appointmentDetail?.appointment || record
        const vehicle = appointmentDetail?.vehicle
        const items = appointmentDetail?.items || []

        return {
          id: record.id,
          time: record.timeSlot ? `${record.appointmentDate} ${record.timeSlot}` : `${record.appointmentDate}`,
          customerName: appointment?.userId ? `用户 ${appointment.userId}` : '客户信息待完善',
          vehicleInfo: vehicle ? `${vehicle.brand || ''} ${vehicle.model || ''} (${vehicle.licensePlate || '未登记'})` : `车辆ID: ${record.vehicleId || '-'}`,
          services: items.length ? items.map(item => item.itemName || item.name || '').filter(Boolean).join('、') : '项目待确认',
          status: record.status
        }
      }))
    }
  } catch (error) {
    console.error('获取今日预约失败:', error)
  }
}

const mapStatus = (status) => {
  if (typeof status === 'number') {
    return shopStatusMap[status] || { text: '未知状态', tag: 'info' }
  }
  if (appointmentStatusMap[status]) {
    return appointmentStatusMap[status]
  }
  const parsed = Number(status)
  if (!Number.isNaN(parsed)) {
    return shopStatusMap[parsed] || { text: '未知状态', tag: 'info' }
  }
  return { text: status || '未知状态', tag: 'info' }
}

const getStatusText = (status) => {
  if (appointmentStatusMap[status]) {
    return appointmentStatusMap[status].text
  }
  const parsed = Number(status)
  return mapStatus(Number.isNaN(parsed) ? status : parsed).text
}

const getStatusTagType = (status) => {
  if (appointmentStatusMap[status]) {
    return appointmentStatusMap[status].tag
  }
  const parsed = Number(status)
  return mapStatus(Number.isNaN(parsed) ? status : parsed).tag
}

const editShopInfo = () => {
  router.push('/shop/profile')
}

const confirmAppointment = async (appointment) => {
  try {
    await ElMessageBox.confirm('确认接受这个预约吗？', '确认预约', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    })

    const response = await shopAPI.updateAppointmentStatus(appointment.id, 2)
    if (response.code === 200) {
      ElMessage.success('预约确认成功')
      loadTodayAppointments()
      loadOrderStats()
    } else {
      ElMessage.error(response.message || '确认失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认预约失败:', error)
      ElMessage.error('确认失败，请稍后重试')
    }
  }
}

const viewAppointmentDetail = (appointment) => {
  router.push({ path: '/shop/orders', query: { appointmentId: appointment.id } })
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('shopToken')
    localStorage.removeItem('shopInfo')
    router.push('/shop/login')
    ElMessage.success('已退出登录')
  }).catch(() => {})
}

onMounted(() => {
  loadShopInfo()
  loadOrderStats()
  loadTodayAppointments()
})
</script>

<style scoped>
.harmony-shop-dashboard {
  padding: var(--harmony-space-xl);
  max-width: 1400px;
  margin: 0 auto;
  animation: harmony-fadeIn 0.8s ease-out;
}

.harmony-dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--harmony-space-2xl);
}

.harmony-page-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin: 0;
  color: var(--harmony-text-primary);
}

.harmony-text-gradient {
  background: var(--harmony-primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 门店信息卡片 */
.harmony-shop-info-card {
  margin-bottom: var(--harmony-space-xl);
}

.harmony-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--harmony-space-lg);
  padding-bottom: var(--harmony-space-md);
  border-bottom: 1px solid var(--harmony-border);
}

.harmony-card-title {
  display: flex;
  align-items: center;
  gap: var(--harmony-space-sm);
  font-size: 1.3rem;
  font-weight: 600;
  color: var(--harmony-text-primary);
  margin: 0;
}

.harmony-card-icon {
  color: var(--harmony-primary);
  font-size: 1.2rem;
}

.harmony-shop-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: var(--harmony-space-lg);
}

.harmony-info-item {
  display: flex;
  align-items: center;
  padding: var(--harmony-space-sm) 0;
}

.harmony-info-item label {
  font-weight: 500;
  color: var(--harmony-text-secondary);
  min-width: 100px;
  margin-right: var(--harmony-space-md);
}

.harmony-info-item span {
  color: var(--harmony-text-primary);
  font-weight: 500;
}

.harmony-status-tag {
  border-radius: var(--harmony-radius-full) !important;
}

/* 统计卡片 */
.harmony-stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: var(--harmony-space-xl);
  margin-bottom: var(--harmony-space-2xl);
}

.harmony-stat-card {
  background: var(--harmony-bg-primary);
  border-radius: var(--harmony-radius-lg);
  padding: var(--harmony-space-xl);
  box-shadow: var(--harmony-shadow-sm);
  transition: all var(--harmony-transition-base);
  border: 1px solid var(--harmony-border);
  position: relative;
  overflow: hidden;
}

.harmony-stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--harmony-primary-gradient);
  opacity: 0;
  transition: opacity var(--harmony-transition-base);
}

.harmony-stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--harmony-shadow-xl);
}

.harmony-stat-card:hover::before {
  opacity: 1;
}

.harmony-stat-content {
  display: flex;
  align-items: center;
  gap: var(--harmony-space-lg);
}

.harmony-stat-icon {
  width: 64px;
  height: 64px;
  border-radius: var(--harmony-radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
  box-shadow: var(--harmony-shadow-md);
}

.harmony-stat-pending {
  background: var(--harmony-gradient-3);
}

.harmony-stat-processing {
  background: var(--harmony-gradient-4);
}

.harmony-stat-completed {
  background: var(--harmony-gradient-4);
}

.harmony-stat-revenue {
  background: var(--harmony-gradient-2);
}

.harmony-stat-info {
  flex: 1;
}

.harmony-stat-number {
  font-size: 2rem;
  font-weight: 700;
  color: var(--harmony-text-primary);
  margin: 0 0 var(--harmony-space-xs) 0;
  line-height: 1;
}

.harmony-stat-label {
  color: var(--harmony-text-secondary);
  font-size: 0.95rem;
  font-weight: 500;
  margin: 0;
}

/* 预约卡片 */
.harmony-appointments-card {
  margin-bottom: var(--harmony-space-xl);
}

.harmony-empty-state {
  padding: var(--harmony-space-2xl) 0;
  text-align: center;
}

/* 动画 */
@keyframes harmony-fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  font-weight: 600;
  color: #2c3e50;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .shop-dashboard {
    padding: 15px;
  }

  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .dashboard-header h1 {
    font-size: 24px;
  }

  .stats-cards {
    grid-template-columns: 1fr;
  }

  .shop-info {
    grid-template-columns: 1fr;
  }
}
</style>