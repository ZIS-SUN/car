<template>
  <div class="my-orders">
    <div class="container">
      <h2>我的订单</h2>

      <div class="order-filters">
        <el-select v-model="statusFilter" placeholder="订单状态" @change="filterOrders">
          <el-option label="全部" value=""></el-option>
          <el-option label="待确认" value="pending"></el-option>
          <el-option label="已确认" value="confirmed"></el-option>
          <el-option label="进行中" value="in_progress"></el-option>
          <el-option label="已完成" value="completed"></el-option>
          <el-option label="已取消" value="cancelled"></el-option>
        </el-select>
      </div>

      <div v-loading="loading" class="orders-list">
        <div v-for="order in filteredOrders" :key="order.id" class="order-card">
          <div class="order-header">
            <div class="order-info">
              <h3>订单号：{{ order.orderNo }}</h3>
              <p>下单时间：{{ order.createTime }}</p>
            </div>
            <div class="order-status">
              <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
            </div>
          </div>

          <div class="order-content">
            <div class="shop-info">
              <h4>{{ order.shopName }}</h4>
              <p>{{ order.shopAddress }}</p>
            </div>

            <div class="service-info">
              <h4>服务项目</h4>
              <p>{{ order.serviceName }}</p>
              <p>预约时间：{{ order.appointmentTime }}</p>
            </div>

            <div class="vehicle-info">
              <h4>车辆信息</h4>
              <p>{{ order.vehicleBrand }} {{ order.vehicleModel }} ({{ order.licensePlate }})</p>
            </div>

            <div class="price-info">
              <h4>费用</h4>
              <p class="price">¥{{ order.price }}</p>
            </div>
          </div>

          <div class="order-actions">
            <el-button v-if="order.status === 'pending'" type="primary" @click="payOrder(order)">
              立即支付
            </el-button>
            <el-button v-if="order.status === 'pending'" type="danger" @click="cancelOrder(order)">
              取消订单
            </el-button>
            <el-button v-if="order.status === 'completed'" type="primary" @click="createReview(order)">
              评价
            </el-button>
            <el-button type="text" @click="viewOrderDetail(order)">查看详情</el-button>
          </div>
        </div>
      </div>

      <div v-if="filteredOrders.length === 0 && !loading" class="no-orders">
        <el-empty description="暂无订单" />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderAPI } from '../api/index.js'

export default {
  name: 'MyOrders',
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const statusFilter = ref('')
    const orders = ref([])

    const filteredOrders = computed(() => {
      if (!statusFilter.value) {
        return orders.value
      }
      return orders.value.filter(order => order.status === statusFilter.value)
    })

    const loadOrders = async () => {
      loading.value = true
      try {
        const response = await orderAPI.getOrderList()

        if (response.code === 200 && response.data) {
          // 处理订单数据，使用完整的OrderVO数据
          orders.value = (response.data || []).map(order => ({
            id: order.id,
            orderNo: order.orderNo || `ORD-${order.id}`,
            shopName: order.shopName || '未知门店',
            shopAddress: order.shopAddress || '地址未知',
            serviceName: order.serviceItems || '服务项目未知',
            appointmentTime: order.appointmentDate && order.appointmentTime
              ? `${order.appointmentDate} ${order.appointmentTime}`
              : order.createTime || '',
            vehicleBrand: order.vehicleBrand || '未知',
            vehicleModel: order.vehicleModel || '未知',
            licensePlate: order.licensePlate || '未知',
            price: order.finalAmount || order.totalAmount || 0,
            status: getStatusKey(order.status),
            createTime: order.createTime || ''
          }))
        }
        loading.value = false
      } catch (error) {
        console.error('加载订单失败:', error)
        ElMessage.error('加载订单失败')
        loading.value = false
      }
    }

    const getStatusKey = (status) => {
      // 将后端状态码映射为前端状态key
      const statusMap = {
        1: 'pending',      // 待支付
        2: 'confirmed',    // 已支付/已确认
        3: 'completed',    // 已完成
        4: 'cancelled'     // 已取消
      }
      return statusMap[status] || 'pending'
    }

    const filterOrders = () => {
      // 筛选逻辑已在computed中处理
    }

    const getStatusType = (status) => {
      const statusMap = {
        pending: 'warning',
        confirmed: 'primary',
        in_progress: 'primary',
        completed: 'success',
        cancelled: 'danger'
      }
      return statusMap[status] || 'info'
    }

    const getStatusText = (status) => {
      const statusMap = {
        pending: '待确认',
        confirmed: '已确认',
        in_progress: '进行中',
        completed: '已完成',
        cancelled: '已取消'
      }
      return statusMap[status] || '未知'
    }

    const cancelOrder = async (order) => {
      try {
        await ElMessageBox.confirm('确定要取消这个订单吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        await orderAPI.cancelOrder(order.id)
        ElMessage.success('订单已取消')
        order.status = 'cancelled'
        loadOrders() // 重新加载订单列表
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('取消订单失败')
        }
      }
    }

    const createReview = (order) => {
      router.push({
        path: '/create-review',
        query: { orderId: order.id }
      })
    }

    const payOrder = async (order) => {
      try {
        const { value: paymentMethod } = await ElMessageBox.prompt('请选择支付方式', '订单支付', {
          confirmButtonText: '确认支付',
          cancelButtonText: '取消',
          inputType: 'select',
          inputOptions: {
            '2': '微信支付',
            '3': '支付宝',
            '4': '银行卡',
            '1': '现金'
          },
          inputValue: '2'
        })

        const response = await orderAPI.payOrder(order.id, parseInt(paymentMethod))
        if (response.code === 200) {
          ElMessage.success('支付成功！')
          loadOrders() // 重新加载订单列表
        } else {
          ElMessage.error(response.message || '支付失败')
        }
      } catch (error) {
        if (error === 'cancel') {
          return
        }
        console.error('支付失败:', error)
        const message = error?.response?.data?.message || error.message || '支付失败，请稍后重试'
        ElMessage.error(message)
      }
    }

    const viewOrderDetail = (order) => {
      router.push({
        path: '/order-detail',
        query: { orderId: order.id }
      })
    }

    onMounted(() => {
      // 检查用户角色，管理员不能访问客户订单页面
      const userRole = localStorage.getItem('userRole')
      if (userRole === 'admin') {
        ElMessage.warning('管理员请使用管理后台查看订单')
        router.push('/admin')
        return
      }

      loadOrders()
    })

    return {
      loading,
      statusFilter,
      filteredOrders,
      filterOrders,
      getStatusType,
      getStatusText,
      cancelOrder,
      payOrder,
      createReview,
      viewOrderDetail
    }
  }
}
</script>

<style scoped>
.my-orders {
  padding: 40px 0;
  background: #f8f9fa;
  min-height: calc(100vh - 200px);
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px;
}

.my-orders h2 {
  text-align: center;
  color: #2c3e50;
  margin-bottom: 30px;
  font-size: 2rem;
}

.order-filters {
  margin-bottom: 30px;
  text-align: center;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-card {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e0e0e0;
}

.order-info h3 {
  margin-bottom: 5px;
  color: #2c3e50;
}

.order-info p {
  color: #7f8c8d;
}

.order-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.order-content h4 {
  margin-bottom: 8px;
  color: #2c3e50;
  font-size: 0.95rem;
}

.order-content p {
  color: #7f8c8d;
  margin-bottom: 5px;
}

.price {
  font-size: 1.2rem;
  color: #e74c3c;
  font-weight: bold;
}

.order-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.no-orders {
  text-align: center;
  margin-top: 50px;
}
</style>
