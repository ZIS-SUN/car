<template>
  <div class="order-detail">
    <div class="container">
      <div class="page-header">
        <el-button @click="$router.back()" class="back-btn">
          <el-icon><arrow-left /></el-icon>
          返回
        </el-button>
        <h2>订单详情</h2>
      </div>

      <div v-loading="loading" class="detail-content">
        <el-card class="info-card" v-if="order">
          <template #header>
            <div class="card-header">
              <span>订单信息</span>
              <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
            </div>
          </template>

          <div class="info-section">
            <div class="info-row">
              <label>订单号：</label>
              <span>{{ order.orderNo }}</span>
            </div>
            <div class="info-row">
              <label>下单时间：</label>
              <span>{{ order.createTime }}</span>
            </div>
          </div>
        </el-card>

        <el-card class="info-card" v-if="order">
          <template #header>
            <span>门店信息</span>
          </template>

          <div class="info-section">
            <div class="info-row">
              <label>门店名称：</label>
              <span>{{ order.shopName || '-' }}</span>
            </div>
            <div class="info-row">
              <label>门店地址：</label>
              <span>{{ order.shopAddress || '-' }}</span>
            </div>
          </div>
        </el-card>

        <el-card class="info-card" v-if="order">
          <template #header>
            <span>服务信息</span>
          </template>

          <div class="info-section">
            <div class="info-row">
              <label>服务项目：</label>
              <span>{{ order.serviceItems || '-' }}</span>
            </div>
            <div class="info-row">
              <label>预约时间：</label>
              <span>{{ order.appointmentDate }} {{ order.appointmentTime }}</span>
            </div>
          </div>
        </el-card>

        <el-card class="info-card" v-if="order">
          <template #header>
            <span>车辆信息</span>
          </template>

          <div class="info-section">
            <div class="info-row">
              <label>车牌号：</label>
              <span>{{ order.licensePlate || '-' }}</span>
            </div>
          </div>
        </el-card>

        <el-card class="info-card" v-if="order">
          <template #header>
            <span>费用明细</span>
          </template>

          <div class="info-section">
            <div class="info-row">
              <label>服务费用：</label>
              <span>¥{{ order.totalAmount }}</span>
            </div>
            <div class="info-row">
              <label>优惠金额：</label>
              <span>-¥{{ order.discountAmount || 0 }}</span>
            </div>
            <div class="info-row total-row">
              <label>实付金额：</label>
              <span class="total-amount">¥{{ order.finalAmount }}</span>
            </div>
          </div>
        </el-card>

        <div class="action-buttons" v-if="order">
          <el-button v-if="order.status === 1" type="danger" @click="cancelOrder">取消订单</el-button>
          <el-button v-if="order.status === 3" type="primary" @click="createReview">评价订单</el-button>
        </div>
      </div>

      <el-empty v-if="!loading && !order" description="订单不存在" />
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { orderAPI } from '../api/index.js'

export default {
  name: 'OrderDetail',
  components: {
    ArrowLeft
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const loading = ref(false)
    const order = ref(null)

    const formatDateTime = (value) => {
      if (!value) return '-'
      const date = new Date(value)
      if (Number.isNaN(date.getTime())) {
        return value
      }
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
    }

    const loadOrderDetail = async () => {
      const orderId = route.query.orderId
      if (!orderId) {
        ElMessage.error('订单ID不存在')
        router.back()
        return
      }

      loading.value = true
      try {
        const response = await orderAPI.getOrderDetail(orderId)
        if (response.code === 200 && response.data) {
          const detail = response.data || {}
          const orderData = detail.order || {}
          const appointment = detail.appointment || {}
          const vehicle = detail.vehicle || {}
          const shop = detail.shop || {}
          const items = detail.items || []

          order.value = {
            ...orderData,
            orderNo: orderData.orderNo || `ORD-${orderData.id}`,
            createTime: formatDateTime(orderData.createTime),
            shopName: shop.shopName || '未知门店',
            shopAddress: shop.address || '-',
            serviceItems: items.length ? items.map(item => item.itemName).join('、') : '-',
            appointmentDate: appointment.appointmentDate || '-',
            appointmentTime: appointment.timeSlot || orderData.appointmentTime || '',
            licensePlate: vehicle.licensePlate || '-',
            vehicleBrand: vehicle.brand || '',
            vehicleModel: vehicle.model || '',
            totalAmount: orderData.totalAmount || 0,
            discountAmount: orderData.discountAmount || 0,
            finalAmount: orderData.finalAmount || orderData.totalAmount || 0
          }
        }
        loading.value = false
      } catch (error) {
        console.error('加载订单详情失败:', error)
        ElMessage.error('加载订单详情失败')
        loading.value = false
      }
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

    const cancelOrder = async () => {
      try {
        await ElMessageBox.confirm('确定要取消这个订单吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        await orderAPI.cancelOrder(order.value.id)
        ElMessage.success('订单已取消')
        loadOrderDetail()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('取消订单失败')
        }
      }
    }

    const createReview = () => {
      router.push({
        path: '/create-review',
        query: { orderId: order.value.id }
      })
    }

    onMounted(() => {
      loadOrderDetail()
    })

    return {
      loading,
      order,
      formatDateTime,
      getStatusType,
      getStatusText,
      cancelOrder,
      createReview
    }
  }
}
</script>

<style scoped>
.order-detail {
  padding: 40px 0;
  background: #f8f9fa;
  min-height: calc(100vh - 200px);
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
}

.info-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-row {
  display: flex;
  align-items: center;
}

.info-row label {
  min-width: 100px;
  color: #666;
  font-weight: 500;
}

.info-row span {
  color: #333;
}

.total-row {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #eee;
}

.total-row label {
  font-weight: 600;
  font-size: 16px;
}

.total-amount {
  font-size: 20px;
  font-weight: 700;
  color: #f56c6c;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 20px;
}
</style>
