<template>
  <div class="my-reviews">
    <div class="container">
      <h2>我的评价</h2>

      <div v-loading="loading" class="reviews-list">
        <div v-for="review in reviews" :key="review.id" class="review-card">
          <div class="review-header">
            <div class="order-info">
              <h3>{{ review.shopName || `门店 #${review.shopId}` }}</h3>
              <p>订单号：{{ review.orderNo || '--' }}</p>
            </div>
            <div class="review-time">
              <p>{{ formatDate(review.createTime) }}</p>
            </div>
          </div>

          <div class="review-content">
            <div class="rating-section">
              <span class="label">服务评分：</span>
              <el-rate :model-value="Number(review.overallRating) || 0" disabled show-score />
            </div>

            <div class="review-text" v-if="review.comment">
              <p>{{ review.comment }}</p>
            </div>

            <div class="service-tags" v-if="review.tags && review.tags.length">
              <el-tag
                v-for="tag in review.tags"
                :key="tag"
                size="small"
                type="success"
                class="tag"
              >
                {{ tag }}
              </el-tag>
            </div>
          </div>

          <div class="review-actions">
            <el-button type="text" @click="editReview(review)">编辑</el-button>
            <el-button type="text" style="color: #f56c6c;" @click="deleteReview(review)">删除</el-button>
          </div>
        </div>
      </div>

      <div v-if="reviews.length === 0 && !loading" class="no-reviews">
        <el-empty description="暂无评价" />
      </div>

      <div v-if="reviews.length" class="pagination">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { reviewAPI, shopAPI, orderAPI } from '../api/index.js'

export default {
  name: 'MyReviews',
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const reviews = ref([])
    const total = ref(0)
    const currentPage = ref(1)
    const pageSize = ref(10)
    const shopCache = new Map()

    const formatDate = (value) => {
      if (!value) return '-'
      const date = new Date(value)
      if (Number.isNaN(date.getTime())) {
        return value
      }
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
    }

    const hydrateReview = async (review) => {
      const enriched = { ...review }

      // 订单信息
      if (!enriched.orderNo && review.orderId) {
        try {
          const orderDetail = await orderAPI.getOrderDetail(review.orderId)
          const detailData = orderDetail.data || {}
          enriched.orderNo = detailData.order?.orderNo
          enriched.shopName = detailData.shop?.shopName
        } catch (error) {
          console.warn('加载订单信息失败:', error)
        }
      }

      if (!enriched.shopName && review.shopId) {
        if (shopCache.has(review.shopId)) {
          enriched.shopName = shopCache.get(review.shopId)
        } else {
          try {
            const shopRes = await shopAPI.getShopDetail(review.shopId)
            const shopName = shopRes.data?.shopName
            if (shopName) {
              shopCache.set(review.shopId, shopName)
              enriched.shopName = shopName
            }
          } catch (error) {
            console.warn('加载门店信息失败:', error)
          }
        }
      }

      return enriched
    }

    const loadReviews = async () => {
      loading.value = true
      try {
        const response = await reviewAPI.getMyReviews({
          current: currentPage.value,
          size: pageSize.value
        })

        const pageData = response.data || {}
        total.value = pageData.total || 0

        const rawRecords = pageData.records || []
        const hydrated = await Promise.all(rawRecords.map(hydrateReview))
        reviews.value = hydrated
      } catch (error) {
        console.error('加载评价失败:', error)
        ElMessage.error(error?.response?.data?.message || error.message || '加载评价失败')
      } finally {
        loading.value = false
      }
    }

    const editReview = (review) => {
      router.push({
        path: '/create-review',
        query: {
          orderId: review.orderId,
          reviewId: review.id
        }
      })
    }

    const deleteReview = async (review) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除对 ${review.shopName} 的评价吗？`,
          '删除评价',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )

        await reviewAPI.deleteReview(review.id)
        ElMessage.success('评价已删除')
        loadReviews()
      } catch (error) {
        if (error === 'cancel') {
          return
        }
        console.error('删除评价失败:', error)
        ElMessage.error(error?.response?.data?.message || error.message || '删除评价失败')
      }
    }

    const handlePageChange = (page) => {
      currentPage.value = page
      loadReviews()
    }

    onMounted(() => {
      loadReviews()
    })

    return {
      loading,
      reviews,
      total,
      currentPage,
      pageSize,
      formatDate,
      editReview,
      deleteReview,
      handlePageChange
    }
  }
}
</script>

<style scoped>
.my-reviews {
  padding: 40px 0;
  background: #f8f9fa;
  min-height: calc(100vh - 200px);
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

.my-reviews h2 {
  text-align: center;
  color: #2c3e50;
  margin-bottom: 30px;
  font-size: 2rem;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-card {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.review-header {
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

.review-time p {
  color: #7f8c8d;
}

.review-content {
  margin-bottom: 20px;
}

.rating-section {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.label {
  margin-right: 10px;
  color: #2c3e50;
  font-weight: 500;
}

.review-text {
  margin-bottom: 15px;
}

.review-text p {
  color: #2c3e50;
  line-height: 1.6;
}

.service-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  margin: 0;
}

.review-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.no-reviews {
  text-align: center;
  margin-top: 50px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
