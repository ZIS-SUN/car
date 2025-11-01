<template>
  <div class="order-detail">
    <el-card class="detail-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>订单详情</span>
          <el-button size="small" @click="$emit('close')">关闭</el-button>
        </div>
      </template>

      <div v-if="order" class="order-content">
        <!-- 订单基本信息 -->
        <div class="section">
          <h4>订单信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单号">{{ order.id }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="getStatusType(order.status)">
                {{ getStatusText(order.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="下单时间">{{ formatDateTime(order.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="预约时间">{{ formatDateTime(order.appointmentTime) }}</el-descriptions-item>
            <el-descriptions-item label="总金额">¥{{ order.totalAmount }}</el-descriptions-item>
            <el-descriptions-item label="支付方式">{{ order.paymentMethod || '未支付' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 用户信息 -->
        <div class="section">
          <h4>用户信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="用户名">{{ order.customerName }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ order.customerPhone }}</el-descriptions-item>
            <el-descriptions-item label="车辆型号">{{ order.vehicleModel }}</el-descriptions-item>
            <el-descriptions-item label="车牌号">{{ order.vehiclePlate }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 服务项目 -->
        <div class="section">
          <h4>服务项目</h4>
          <el-table :data="order.items" border>
            <el-table-column prop="serviceName" label="服务名称" />
            <el-table-column prop="price" label="单价" width="120">
              <template #default="scope">¥{{ scope.row.price }}</template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column prop="subtotal" label="小计" width="120">
              <template #default="scope">¥{{ scope.row.subtotal }}</template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 服务评价 -->
        <div v-if="order.review" class="section">
          <h4>服务评价</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="总体评分">
              <el-rate v-model="order.review.rating" disabled />
            </el-descriptions-item>
            <el-descriptions-item label="评价内容">{{ order.review.comment }}</el-descriptions-item>
            <el-descriptions-item label="评价时间">{{ formatDateTime(order.review.createTime) }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 操作按钮 -->
        <div class="section">
          <div class="action-buttons">
            <el-button
              v-if="order.status === 'PENDING'"
              type="success"
              @click="$emit('confirm', order.id)"
            >
              确认订单
            </el-button>
            <el-button
              v-if="order.status === 'CONFIRMED'"
              type="primary"
              @click="$emit('start', order.id)"
            >
              开始服务
            </el-button>
            <el-button
              v-if="order.status === 'IN_PROGRESS'"
              type="success"
              @click="$emit('complete', order.id)"
            >
              完成服务
            </el-button>
            <el-button
              v-if="['PENDING', 'CONFIRMED'].includes(order.status)"
              type="danger"
              @click="$emit('cancel', order.id)"
            >
              取消订单
            </el-button>
          </div>
        </div>
      </div>

      <div v-else class="loading">
        <el-skeleton :rows="5" animated />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  order: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close', 'confirm', 'start', 'complete', 'cancel'])

// 状态映射
const statusMap = {
  PENDING: { text: '待确认', type: 'warning' },
  CONFIRMED: { text: '已确认', type: 'info' },
  IN_PROGRESS: { text: '进行中', type: 'primary' },
  COMPLETED: { text: '已完成', type: 'success' },
  CANCELLED: { text: '已取消', type: 'danger' }
}

const getStatusType = (status) => {
  return statusMap[status]?.type || 'info'
}

const getStatusText = (status) => {
  return statusMap[status]?.text || status
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}
</script>

<style scoped>
.order-detail {
  max-width: 800px;
  margin: 0 auto;
}

.detail-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section {
  margin-bottom: 24px;
}

.section h4 {
  margin-bottom: 12px;
  color: #303133;
  font-weight: 600;
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.loading {
  padding: 20px;
}
</style>