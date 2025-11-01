<template>
  <div class="create-review">
    <div class="container">
      <div class="review-header">
        <h2>评价服务</h2>
        <p>请对本次服务进行评价，您的意见对我们很重要</p>
      </div>

      <div v-loading="loading" class="review-content">
        <!-- 订单信息 -->
        <div class="order-info" v-if="order">
          <h3>订单信息</h3>
          <div class="order-card">
            <div class="order-item">
              <label>门店：</label>
              <span>{{ order.shopName }}</span>
            </div>
            <div class="order-item">
              <label>服务：</label>
              <span>{{ order.serviceName }}</span>
            </div>
            <div class="order-item">
              <label>服务时间：</label>
              <span>{{ order.appointmentTime }}</span>
            </div>
            <div class="order-item">
              <label>车辆：</label>
              <span>{{ order.vehicleBrand }} {{ order.vehicleModel }} ({{ order.licensePlate }})</span>
            </div>
          </div>
        </div>

        <!-- 评价表单 -->
        <div class="review-form">
          <h3>服务评价</h3>
          <el-form
            ref="reviewFormRef"
            :model="reviewForm"
            :rules="reviewRules"
            label-width="100px"
            @submit.prevent="submitReview"
          >
            <el-form-item label="整体评分" prop="overallRating">
              <el-rate
                v-model="reviewForm.overallRating"
                :max="5"
                show-text
                :texts="ratingTexts"
                :colors="ratingColors"
              />
            </el-form-item>

            <el-form-item label="服务专业" prop="technicianRating">
              <el-rate v-model="reviewForm.technicianRating" :max="5" />
            </el-form-item>

            <el-form-item label="服务态度" prop="attitudeRating">
              <el-rate v-model="reviewForm.serviceRating" :max="5" />
            </el-form-item>

            <el-form-item label="环境设施" prop="environmentRating">
              <el-rate v-model="reviewForm.environmentRating" :max="5" />
            </el-form-item>

            <el-form-item label="性价比" prop="valueRating">
              <el-rate v-model="reviewForm.priceRating" :max="5" />
            </el-form-item>

            <el-form-item label="服务标签" prop="tags">
              <div class="tag-selection">
                <el-tag
                  v-for="tag in availableTags"
                  :key="tag"
                  :type="selectedTags.includes(tag) ? 'success' : 'info'"
                  :effect="selectedTags.includes(tag) ? 'dark' : 'plain'"
                  class="tag-item"
                  @click="toggleTag(tag)"
                >
                  {{ tag }}
                </el-tag>
              </div>
            </el-form-item>

            <el-form-item label="评价内容" prop="content">
              <el-input
                v-model="reviewForm.content"
                type="textarea"
                :rows="4"
                placeholder="请分享您的服务体验..."
                maxlength="500"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="匿名评价">
              <el-switch v-model="reviewForm.isAnonymous" />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                class="submit-btn"
                :loading="submitting"
                @click="submitReview"
              >
                提交评价
              </el-button>
              <el-button size="large" @click="goBack">
                返回
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { orderAPI, reviewAPI } from '../api/index.js'

export default {
  name: 'CreateReview',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const reviewFormRef = ref()
    const loading = ref(false)
    const submitting = ref(false)
    const order = ref(null)
    const selectedTags = ref([])
    const isEdit = ref(false)

    const reviewForm = reactive({
      orderId: '',
      overallRating: 5,
      technicianRating: 5,
      serviceRating: 5,
      environmentRating: 5,
      priceRating: 5,
      tags: [],
      content: '',
      isAnonymous: false
    })

    const ratingTexts = ['很不满意', '不满意', '一般', '满意', '非常满意']
    const ratingColors = ['#F56C6C', '#E6A23C', '#909399', '#67C23A', '#409EFF']

    const availableTags = [
      '服务专业',
      '态度友好',
      '环境整洁',
      '设备先进',
      '价格合理',
      '效率高',
      '无推销',
      '透明收费',
      '守时守信',
      '技术精湛'
    ]

    const reviewRules = {
      overallRating: [
        { required: true, message: '请选择整体评分', trigger: 'change' }
      ],
      content: [
        { min: 10, message: '评价内容至少10个字符', trigger: 'blur' }
      ]
    }

    watch(
      () => reviewForm.overallRating,
      (value) => {
        if (!value) return
        reviewForm.technicianRating = value
        reviewForm.serviceRating = value
        reviewForm.environmentRating = value
        reviewForm.priceRating = value
      }
    )

    const formatDateTime = (value) => {
      if (!value) return '-'
      const date = new Date(value)
      if (Number.isNaN(date.getTime())) {
        return value
      }
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
    }

    const hydrateOrder = (detailData) => {
      const orderData = detailData.order || {}
      const appointment = detailData.appointment || {}
      const vehicle = detailData.vehicle || {}
      const shop = detailData.shop || {}

      order.value = {
        id: orderData.id,
        shopName: shop.shopName || '未知门店',
        serviceName: detailData.items?.map(item => item.itemName).join('、') || '预约服务',
        appointmentTime: appointment.appointmentDate
          ? `${appointment.appointmentDate} ${appointment.timeSlot || ''}`
          : formatDateTime(orderData.createTime),
        vehicleBrand: vehicle.brand || '车辆',
        vehicleModel: vehicle.model || '',
        licensePlate: vehicle.licensePlate || '未知'
      }

      reviewForm.orderId = orderData.id
    }

    const loadOrderDetail = async () => {
      loading.value = true
      try {
        const orderId = route.query.orderId
        if (!orderId) {
          ElMessage.error('缺少订单信息')
          router.push('/my-orders')
          return
        }
        const orderRes = await orderAPI.getOrderDetail(orderId)
        hydrateOrder(orderRes.data || {})

        const reviewId = route.query.reviewId
        if (reviewId) {
          const reviewRes = await reviewAPI.getReviewDetail(reviewId)
          const reviewData = reviewRes.data || {}
          reviewForm.orderId = reviewData.orderId
          reviewForm.overallRating = Number(reviewData.overallRating) || 5
          reviewForm.technicianRating = Number(reviewData.technicianRating) || reviewForm.overallRating
          reviewForm.serviceRating = Number(reviewData.serviceRating) || reviewForm.overallRating
          reviewForm.environmentRating = Number(reviewData.environmentRating) || reviewForm.overallRating
          reviewForm.priceRating = Number(reviewData.priceRating) || reviewForm.overallRating
          reviewForm.content = reviewData.comment || ''
          reviewForm.isAnonymous = reviewData.isAnonymous === 1
          selectedTags.value = []
          isEdit.value = true
        } else {
          const checkRes = await reviewAPI.checkReviewed(orderId)
          if (checkRes.data) {
            ElMessage.warning('该订单已评价，正在跳转...')
            router.push('/my-reviews')
            return
          }
        }
      } catch (error) {
        console.error('加载订单信息失败:', error)
        ElMessage.error(error?.response?.data?.message || error.message || '加载订单信息失败')
        router.push('/my-orders')
      } finally {
        loading.value = false
      }
    }

    const toggleTag = (tag) => {
      const index = selectedTags.value.indexOf(tag)
      if (index > -1) {
        selectedTags.value.splice(index, 1)
      } else {
        if (selectedTags.value.length < 5) {
          selectedTags.value.push(tag)
        } else {
          ElMessage.warning('最多选择5个标签')
        }
      }
    }

    const submitReview = async () => {
      if (!reviewFormRef.value) return

      try {
        await reviewFormRef.value.validate()

        submitting.value = true

        const payload = {
          orderId: reviewForm.orderId,
          technicianRating: reviewForm.technicianRating,
          serviceRating: reviewForm.serviceRating,
          priceRating: reviewForm.priceRating,
          environmentRating: reviewForm.environmentRating,
          comment: buildComment(),
          isAnonymous: reviewForm.isAnonymous ? 1 : 0
        }

        if (isEdit.value && route.query.reviewId) {
          await reviewAPI.updateReview(route.query.reviewId, payload)
          ElMessage.success('评价更新成功！')
        } else {
          await reviewAPI.createReview(payload)
          ElMessage.success('评价提交成功！')
        }

        router.push('/my-reviews')

      } catch (error) {
        if (error === 'cancel') {
          submitting.value = false
          return
        }
        console.error('提交评价失败:', error)
        ElMessage.error(error?.response?.data?.message || error.message || '提交评价失败')
        submitting.value = false
      }
    }

    const buildComment = () => {
      const tagText = selectedTags.value.length ? ` 标签：${selectedTags.value.join('、')}` : ''
      return `${reviewForm.content || ''}${tagText}`
    }

    const goBack = () => {
      router.push('/my-orders')
    }

    onMounted(() => {
      loadOrderDetail()
    })

    return {
      reviewFormRef,
      loading,
      submitting,
      order,
      reviewForm,
      reviewRules,
      ratingTexts,
      ratingColors,
      availableTags,
      selectedTags,
      toggleTag,
      submitReview,
      formatDateTime,
      goBack
    }
  }
}
</script>

<style scoped>
.create-review {
  padding: 40px 0;
  background: #f8f9fa;
  min-height: calc(100vh - 200px);
}

.review-header {
  text-align: center;
  margin-bottom: 40px;
}

.review-header h2 {
  color: #2c3e50;
  margin-bottom: 10px;
  font-size: 2rem;
}

.review-header p {
  color: #7f8c8d;
  font-size: 1.1rem;
}

.review-content {
  max-width: 800px;
  margin: 0 auto;
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.order-info, .review-form {
  margin-bottom: 30px;
}

.order-info h3, .review-form h3 {
  margin-bottom: 20px;
  color: #2c3e50;
  font-size: 1.3rem;
}

.order-card {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
}

.order-item {
  display: flex;
  margin-bottom: 10px;
}

.order-item:last-child {
  margin-bottom: 0;
}

.order-item label {
  font-weight: 500;
  color: #2c3e50;
  min-width: 80px;
}

.order-item span {
  color: #5a6c7d;
}

.tag-selection {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-item {
  cursor: pointer;
  margin: 0;
  transition: all 0.3s ease;
}

.tag-item:hover {
  transform: translateY(-2px);
}

.submit-btn {
  margin-right: 20px;
  padding: 12px 40px;
}

:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
  line-height: 100px;
}

@media (max-width: 768px) {
  .review-content {
    margin: 0 20px;
    padding: 20px;
  }

  .order-item {
    flex-direction: column;
  }

  .order-item label {
    margin-bottom: 5px;
  }

  .submit-btn {
    width: 100%;
    margin-right: 0;
    margin-bottom: 10px;
  }
}
</style>
