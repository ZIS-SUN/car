<template>
  <div class="shop-detail">
    <div class="container">
      <!-- 返回按钮 -->
      <div class="back-button">
        <el-button @click="goBack">
          <el-icon><arrow-left /></el-icon>
          返回门店列表
        </el-button>
      </div>

      <div v-loading="loading" class="shop-content">
        <!-- 门店基本信息 -->
        <div class="shop-header">
          <div class="shop-image">
            <img :src="shop.imageUrl || '/default-shop.jpg'" :alt="shop.name">
          </div>
          <div class="shop-info">
            <h1>{{ shop.name }}</h1>
            <div class="rating">
              <el-rate :model-value="shop.rating" disabled show-score></el-rate>
              <span class="review-count">({{ shop.reviewCount }}条评价)</span>
            </div>
            <div class="shop-address">
              <el-icon><location /></el-icon>
              <span>{{ shop.address }}</span>
            </div>
            <div class="shop-phone">
              <el-icon><phone /></el-icon>
              <span>{{ shop.phone }}</span>
            </div>
            <div class="shop-hours">
              <el-icon><clock /></el-icon>
              <span>营业时间：{{ shop.businessHours }}</span>
            </div>
          </div>
          <div class="shop-actions">
            <el-button type="primary" size="large" @click="makeAppointment">
              立即预约
            </el-button>
            <el-button size="large" @click="collectShop">
              <el-icon><star /></el-icon>
              {{ isCollected ? '已收藏' : '收藏' }}
            </el-button>
          </div>
        </div>

        <!-- 门店描述 -->
        <div class="shop-description">
          <h3>门店介绍</h3>
          <p>{{ shop.description }}</p>
        </div>

        <!-- 服务项目 -->
        <div class="shop-services">
          <h3>服务项目</h3>
          <div v-if="services.length" class="services-grid">
            <div v-for="service in services" :key="service.id" class="service-card">
              <h4>{{ service.name }}</h4>
              <p>{{ service.description }}</p>
              <div class="service-price">
                <span class="price">¥{{ service.price }}</span>
                <span class="duration">{{ service.duration }}分钟</span>
              </div>
            </div>
          </div>
          <div v-else class="empty-block">
            <el-empty description="暂无服务项目" />
          </div>
        </div>

        <!-- 评价列表 -->
        <div class="shop-reviews">
          <div class="reviews-header">
            <h3>用户评价</h3>
            <el-button type="text" @click="showAllReviews">
              查看全部评价
            </el-button>
          </div>
          <div v-if="recentReviews.length" class="reviews-list">
            <div v-for="review in recentReviews" :key="review.id" class="review-item">
              <div class="review-header">
                <div class="user-info">
                  <span class="username">{{ review.username }}</span>
                  <el-rate :model-value="review.rating" disabled size="small"></el-rate>
                </div>
                <span class="review-time">{{ review.createTime }}</span>
              </div>
              <div class="review-content">
                <p>{{ review.content }}</p>
                <div class="review-tags" v-if="review.tags && review.tags.length">
                  <el-tag
                    v-for="tag in review.tags"
                    :key="tag"
                    size="small"
                    type="success"
                  >
                    {{ tag }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="empty-block">
            <el-empty description="暂无评价" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, Location, Phone, Clock, Star } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

export default {
  name: 'ShopDetail',
  components: {
    ArrowLeft,
    Location,
    Phone,
    Clock,
    Star
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const loading = ref(false)
    const shop = ref({
      id: null,
      name: '',
      address: '',
      phone: '',
      businessHours: '',
      rating: 0,
      reviewCount: 0,
      description: '',
      imageUrl: ''
    })
    const services = ref([])
    const isCollected = ref(false)
    const recentReviews = ref([])

    const extractPrimaryImage = (images) => {
      if (!images) return ''
      if (Array.isArray(images) && images.length) {
        return images[0]
      }
      if (typeof images === 'string') {
        try {
          const parsed = JSON.parse(images)
          if (Array.isArray(parsed) && parsed.length) {
            return parsed[0]
          }
        } catch (error) {
          const segments = images.split(',').map(item => item.trim()).filter(Boolean)
          if (segments.length) {
            return segments[0]
          }
        }
      }
      if (images && typeof images === 'object') {
        const values = Object.values(images).flat()
        if (values.length && typeof values[0] === 'string') {
          return values[0]
        }
      }
      return ''
    }

    const formatDate = (value) => {
      if (!value) return ''
      const date = new Date(value)
      if (Number.isNaN(date.getTime())) {
        return value
      }
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    }

    const loadShopDetail = async () => {
      const shopId = Number(route.params.id)
      if (!shopId) {
        ElMessage.error('无效的门店编号')
        router.push('/shops')
        return
      }

      loading.value = true
      try {
        const { shopAPI } = await import('../api/index.js')
        const [
          shopResponse,
          servicesResponse,
          reviewStatsResponse,
          latestReviewsResponse
        ] = await Promise.all([
          shopAPI.getShopDetail(shopId),
          shopAPI.getServiceItems(shopId),
          shopAPI.getReviewStats(shopId),
          shopAPI.getLatestReviews(shopId, 4)
        ])

        if (!shopResponse || shopResponse.code !== 200 || !shopResponse.data) {
          throw new Error('门店信息不存在')
        }

        const shopData = shopResponse.data
        const primaryImage = extractPrimaryImage(shopData.images)

        shop.value = {
          id: shopData.id,
          name: shopData.shopName || '未命名门店',
          address: shopData.address || '地址待完善',
          phone: shopData.phone || '联系电话暂无',
          businessHours: shopData.businessHours || '营业时间待更新',
          rating: shopData.rating ? Number(shopData.rating) : 0,
          reviewCount: 0,
          description: shopData.description || '门店简介待完善',
          imageUrl: primaryImage || '/default-shop.jpg'
        }

        if (servicesResponse?.code === 200 && Array.isArray(servicesResponse.data)) {
          services.value = servicesResponse.data
            .filter(item => item.status !== 0)
            .map(item => ({
              id: item.id,
              name: item.itemName || item.name,
              description: item.description || '暂无描述',
              price: item.price || 0,
              duration: item.duration || item.durationMinutes || 60
            }))
        } else {
          services.value = []
        }

        if (reviewStatsResponse?.code === 200 && reviewStatsResponse.data) {
          const stats = reviewStatsResponse.data
          const avg = stats.averageRating ? Number(stats.averageRating) : 0
          shop.value.rating = avg || shop.value.rating
          shop.value.reviewCount = stats.totalCount || 0
        }

        if (latestReviewsResponse?.code === 200 && Array.isArray(latestReviewsResponse.data)) {
          recentReviews.value = latestReviewsResponse.data.map(review => ({
            id: review.id,
            username: review.isAnonymous ? '匿名用户' : `车主 ${review.userId}`,
            rating: review.overallRating ? Number(review.overallRating) : (review.rating || 0),
            content: review.comment || '用户暂未填写评价内容',
            createTime: formatDate(review.createTime)
          }))
        } else {
          recentReviews.value = []
        }
      } catch (error) {
        console.error('加载门店详情失败:', error)
        const message = error.response?.data?.message || error.message || '加载门店详情失败'
        ElMessage.error(message)
        router.push('/shops')
      } finally {
        loading.value = false
      }
    }

    const goBack = () => {
      router.push('/shops')
    }

    const makeAppointment = () => {
      if (!shop.value.id) {
        ElMessage.warning('门店信息加载中，请稍后重试')
        return
      }
      router.push(`/appointment?shopId=${shop.value.id}`)
    }

    const collectShop = () => {
      isCollected.value = !isCollected.value
      ElMessage.success(isCollected.value ? '收藏成功' : '已取消收藏')
    }

    const showAllReviews = () => {
      ElMessage.info('评价列表即将上线，敬请期待~')
    }

    onMounted(() => {
      loadShopDetail()
    })

    watch(
      () => route.params.id,
      (newId, oldId) => {
        if (newId && newId !== oldId) {
          loadShopDetail()
        }
      }
    )

    return {
      loading,
      shop,
      services,
      recentReviews,
      isCollected,
      goBack,
      makeAppointment,
      collectShop,
      showAllReviews
    }
  }
}
</script>

<style scoped>
.shop-detail {
  padding: 20px 0;
  background: #f8f9fa;
  min-height: calc(100vh - 200px);
}

.back-button {
  margin-bottom: 20px;
}

.shop-content {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.shop-header {
  display: flex;
  gap: 30px;
  padding: 30px;
  border-bottom: 1px solid #e0e0e0;
}

.shop-image {
  width: 300px;
  height: 200px;
  border-radius: 8px;
  overflow: hidden;
}

.shop-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.shop-info {
  flex: 1;
}

.shop-info h1 {
  margin-bottom: 15px;
  color: #2c3e50;
  font-size: 1.8rem;
}

.rating {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.review-count {
  color: #7f8c8d;
  font-size: 0.9rem;
}

.shop-address, .shop-phone, .shop-hours {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  color: #2c3e50;
}

.shop-actions {
  display: flex;
  flex-direction: column;
  gap: 15px;
  justify-content: center;
}

.shop-description, .shop-services, .shop-reviews {
  padding: 30px;
}

.shop-description h3, .shop-services h3, .shop-reviews h3 {
  margin-bottom: 20px;
  color: #2c3e50;
  font-size: 1.5rem;
}

.shop-description p {
  line-height: 1.8;
  color: #5a6c7d;
}

.services-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.service-card {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  transition: all 0.3s ease;
}

.service-card:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.service-card h4 {
  margin-bottom: 10px;
  color: #2c3e50;
}

.service-card p {
  color: #7f8c8d;
  margin-bottom: 15px;
  line-height: 1.6;
}

.service-price {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price {
  font-size: 1.2rem;
  color: #e74c3c;
  font-weight: bold;
}

.duration {
  color: #7f8c8d;
  font-size: 0.9rem;
}

.reviews-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.review-item {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.username {
  font-weight: 500;
  color: #2c3e50;
}

.review-time {
  color: #7f8c8d;
  font-size: 0.9rem;
}

.review-content p {
  color: #5a6c7d;
  line-height: 1.6;
  margin-bottom: 10px;
}

.review-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.empty-block {
  padding: 20px 0;
}

@media (max-width: 768px) {
  .shop-header {
    flex-direction: column;
  }

  .shop-image {
    width: 100%;
  }

  .shop-actions {
    flex-direction: row;
  }

  .services-grid {
    grid-template-columns: 1fr;
  }
}
</style>