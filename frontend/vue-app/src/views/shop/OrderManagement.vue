<template>
  <div class="order-management">
    <div class="page-header">
      <h1>订单管理</h1>
      <div class="header-actions">
        <el-button type="primary" @click="refreshOrders">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="订单号">
          <el-input
            v-model="searchForm.orderNo"
            placeholder="请输入订单号"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="客户姓名">
          <el-input
            v-model="searchForm.customerName"
            placeholder="请输入客户姓名"
            clearable
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option label="待服务" value="1" />
            <el-option label="进行中" value="2" />
            <el-option label="已完成" value="3" />
            <el-option label="已取消" value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="预约日期">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 订单列表 -->
    <el-card class="order-list-card" shadow="hover">
      <el-table
        v-loading="loading"
        :data="orders"
        style="width: 100%"
        stripe
      >
        <el-table-column prop="orderNo" label="订单号" width="150" />
        <el-table-column prop="customerName" label="客户姓名" width="100" />
        <el-table-column prop="customerPhone" label="联系电话" width="120" />
        <el-table-column prop="vehicleInfo" label="车辆信息" min-width="180">
          <template #default="{ row }">
            <div class="vehicle-info">
              <div>{{ row.vehicleInfo.brand }} {{ row.vehicleInfo.model }}</div>
              <small class="text-muted">{{ row.vehicleInfo.licensePlate }}</small>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="appointmentTime" label="预约时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.appointmentTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="services" label="服务项目" min-width="200">
          <template #default="{ row }">
            <el-tag
              v-for="service in row.services"
              :key="service.id"
              size="small"
              class="service-tag"
            >
              {{ service.name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="总金额" width="100">
          <template #default="{ row }">
            <span class="amount">¥{{ row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              link
              @click="viewOrderDetail(row)"
            >
              详情
            </el-button>
            <el-button
              v-if="Number(row.status) === 1"
              size="small"
              type="warning"
              link
              @click="startOrder(row)"
            >
              开始服务
            </el-button>
            <el-button
              v-if="Number(row.status) === 2"
              size="small"
              type="success"
              link
              @click="completeOrder(row)"
            >
              完成服务
            </el-button>
            <el-button
              v-if="Number(row.status) !== 3 && Number(row.status) !== 4"
              size="small"
              type="danger"
              link
              @click="cancelOrder(row)"
            >
              取消订单
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="订单详情"
      width="800px"
      destroy-on-close
    >
      <OrderDetail
        v-if="selectedOrder"
        :order="selectedOrder"
        :mode="'shop'"
        @order-updated="onOrderUpdated"
      />
    </el-dialog>

    <!-- 调整订单对话框 -->
    <el-dialog
      v-model="adjustDialogVisible"
      title="调整订单"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="adjustFormRef"
        :model="adjustForm"
        :rules="adjustRules"
        label-width="100px"
      >
        <el-form-item label="预约时间" prop="appointmentTime">
          <el-date-picker
            v-model="adjustForm.appointmentTime"
            type="datetime"
            placeholder="选择预约时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="调整原因" prop="reason">
          <el-input
            v-model="adjustForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入调整原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="adjustLoading" @click="submitAdjustOrder">
          确认调整
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { shopAPI } from '@/api/shop'
import OrderDetail from '@/components/OrderDetail.vue'

const loading = ref(false)
const adjustLoading = ref(false)
const orders = ref([])
const allOrders = ref([])
const selectedOrder = ref(null)
const detailDialogVisible = ref(false)
const adjustDialogVisible = ref(false)
const adjustFormRef = ref()

const searchForm = reactive({
  orderNo: '',
  customerName: '',
  status: '',
  dateRange: []
})

const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

const adjustForm = reactive({
  orderId: '',
  appointmentTime: '',
  reason: ''
})

const adjustRules = {
  appointmentTime: [
    { required: true, message: '请选择预约时间', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请输入调整原因', trigger: 'blur' }
  ]
}

const ORDER_STATUS_MAP = {
  1: { text: '待服务', tag: 'warning' },
  2: { text: '进行中', tag: 'primary' },
  3: { text: '已完成', tag: 'success' },
  4: { text: '已取消', tag: 'danger' }
}

const parseServiceItems = (items) => {
  if (!items) return []
  if (Array.isArray(items)) {
    return items
  }
  if (typeof items === 'string') {
    try {
      const parsed = JSON.parse(items)
      if (Array.isArray(parsed)) {
        return parsed
      }
    } catch (error) {
      // ignore parse error
    }
  }
  return []
}

const transformOrder = (order) => {
  const services = parseServiceItems(order.serviceItems).map((item, index) => ({
    id: item.id || index,
    name: item.itemName || item.name || '服务项目',
    price: item.price || 0,
    quantity: item.quantity || 1
  }))

  return {
    id: order.id,
    orderNo: order.orderNo,
    customerName: order.userId ? `用户 ${order.userId}` : '客户未知',
    customerPhone: order.userPhone || '',
    vehicleInfo: {
      brand: order.vehicleBrand || '',
      model: order.vehicleModel || '',
      licensePlate: order.licensePlate || '未登记'
    },
    appointmentTime: order.appointmentDate && order.appointmentTime
      ? `${order.appointmentDate} ${order.appointmentTime}`
      : order.appointmentDate || '-/-',
    services,
    totalAmount: Number(order.finalAmount || order.totalAmount || 0).toFixed(2),
    status: order.status || 1,
    raw: order
  }
}

const applyFilters = () => {
  let filtered = [...allOrders.value]

  if (searchForm.orderNo) {
    filtered = filtered.filter(order => order.orderNo && order.orderNo.includes(searchForm.orderNo.trim()))
  }

  if (searchForm.customerName) {
    filtered = filtered.filter(order => order.customerName && order.customerName.includes(searchForm.customerName.trim()))
  }

  if (searchForm.status) {
    const statusCode = Number(searchForm.status)
    filtered = filtered.filter(order => Number(order.status) === statusCode)
  }

  if (searchForm.dateRange && searchForm.dateRange.length === 2) {
    const [start, end] = searchForm.dateRange
    filtered = filtered.filter(order => {
      const date = order.raw?.appointmentDate
      if (!date) return false
      return date >= start && date <= end
    })
  }

  pagination.total = filtered.length
  const start = (pagination.current - 1) * pagination.size
  orders.value = filtered.slice(start, start + pagination.size)
}

onMounted(() => {
  loadOrders()
})

const loadOrders = async () => {
  loading.value = true
  try {
    const params = {}
    if (searchForm.status) {
      params.status = Number(searchForm.status)
    }

    const response = await shopAPI.getOrderList(params)
    if (response.code === 200) {
      const list = Array.isArray(response.data) ? response.data : []
      allOrders.value = list.map(transformOrder)
      pagination.current = 1
      applyFilters()
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadOrders()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    orderNo: '',
    customerName: '',
    status: '',
    dateRange: []
  })
  pagination.current = 1
  loadOrders()
}

const handleSizeChange = (size) => {
  pagination.size = size
  applyFilters()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  applyFilters()
}

const refreshOrders = () => {
  loadOrders()
}

const viewOrderDetail = (order) => {
  selectedOrder.value = order
  detailDialogVisible.value = true
}

const startOrder = async (order) => {
  try {
    await ElMessageBox.confirm('确认开始服务吗？', '开始服务', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await shopAPI.startOrder(order.id)
    if (response.code === 200) {
      ElMessage.success('服务已开始')
      loadOrders()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('开始服务失败:', error)
      ElMessage.error('操作失败，请稍后重试')
    }
  }
}

const completeOrder = async (order) => {
  try {
    await ElMessageBox.confirm('确认完成服务吗？', '完成服务', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'success'
    })

    const response = await shopAPI.completeOrder(order.id)
    if (response.code === 200) {
      ElMessage.success('服务已完成')
      loadOrders()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('完成服务失败:', error)
      ElMessage.error('操作失败，请稍后重试')
    }
  }
}

const adjustOrder = () => {
  ElMessage.info('订单调整功能开发中，敬请期待')
}

const submitAdjustOrder = async () => {
  ElMessage.info('订单调整功能暂未开放')
  adjustDialogVisible.value = false
}

const cancelOrder = async (order) => {
  try {
    await ElMessageBox.confirm('确认取消这个订单吗？', '取消订单', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await shopAPI.updateOrderStatus(order.id, 4)
    if (response.code === 200) {
      ElMessage.success('订单已取消')
      loadOrders()
    } else {
      ElMessage.error(response.message || '取消失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
      ElMessage.error('取消失败，请稍后重试')
    }
  }
}

const onOrderUpdated = () => {
  detailDialogVisible.value = false
  loadOrders()
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  if (typeof dateTime === 'string') {
    return dateTime
  }
  const d = new Date(dateTime)
  if (Number.isNaN(d.getTime())) {
    return '-'
  }
  return d.toLocaleString('zh-CN')
}

const getStatusText = (status) => {
  const key = Number(status)
  return ORDER_STATUS_MAP[key]?.text || '未知状态'
}

const getStatusTagType = (status) => {
  const key = Number(status)
  return ORDER_STATUS_MAP[key]?.tag || 'info'
}
</script>

<style scoped>
.order-management {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  color: #2c3e50;
  font-size: 28px;
  font-weight: 600;
}

.filter-card {
  margin-bottom: 20px;
}

.order-list-card {
  margin-bottom: 20px;
}

.vehicle-info {
  line-height: 1.4;
}

.text-muted {
  color: #6c757d;
}

.service-tag {
  margin-right: 8px;
  margin-bottom: 4px;
}

.amount {
  font-weight: 600;
  color: #e74c3c;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .order-management {
    padding: 15px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .page-header h1 {
    font-size: 24px;
  }
}
</style>