<template>
  <div class="harmony-shop-list">
    <div class="harmony-container">
      <!-- 页面标题 -->
      <div class="harmony-page-header">
        <h2 class="harmony-page-title">
          <span class="harmony-text-gradient">选择门店</span>
        </h2>
        <p class="harmony-page-subtitle">为您精选优质门店，提供专业汽车保养服务</p>
      </div>

      <!-- 搜索筛选 -->
      <div class="harmony-filters">
        <div class="harmony-search-wrapper">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索门店名称或地址"
            class="harmony-input harmony-search-input"
            size="large"
            @input="searchShops"
          >
            <template #prefix>
              <el-icon class="harmony-search-icon"><search /></el-icon>
            </template>
          </el-input>
        </div>
        
        <div class="harmony-filter-wrapper">
          <el-select 
            v-model="selectedCity" 
            placeholder="选择城市" 
            class="harmony-select harmony-city-select"
            size="large"
            @change="filterShops"
          >
            <el-option label="全部城市" value=""></el-option>
            <el-option label="北京市" value="北京市"></el-option>
            <el-option label="上海市" value="上海市"></el-option>
            <el-option label="广州市" value="广州市"></el-option>
            <el-option label="深圳市" value="深圳市"></el-option>
          </el-select>
        </div>
      </div>

      <!-- 门店网格 -->
      <div v-loading="loading" class="harmony-shops-grid" element-loading-text="正在加载门店信息...">
        <div 
          v-for="shop in filteredShops" 
          :key="shop.id" 
          class="harmony-shop-card harmony-card-interactive" 
          @click="selectShop(shop)"
        >
          <div class="harmony-shop-image">
            <img :src="shop.imageUrl || '/default-shop.jpg'" :alt="shop.name">
            <div class="harmony-shop-badge">
              <span class="harmony-tag harmony-tag-primary">推荐</span>
            </div>
          </div>
          
          <div class="harmony-shop-info">
            <h3 class="harmony-shop-name">{{ shop.name }}</h3>
            
            <div class="harmony-shop-details">
              <p class="harmony-shop-address">
                <el-icon class="harmony-detail-icon"><location /></el-icon>
                {{ shop.address }}
              </p>
              <p class="harmony-shop-phone">
                <el-icon class="harmony-detail-icon"><phone /></el-icon>
                {{ shop.phone }}
              </p>
            </div>
            
            <div class="harmony-shop-rating">
              <el-rate 
                v-model="shop.rating" 
                disabled 
                show-score 
                class="harmony-rate"
              ></el-rate>
              <span class="harmony-rating-text">{{ shop.rating }}分</span>
            </div>
            
            <div class="harmony-shop-tags">
              <span class="harmony-tag">专业保养</span>
              <span class="harmony-tag">快速服务</span>
              <span class="harmony-tag">价格透明</span>
            </div>
            
            <div class="harmony-shop-actions">
              <el-button 
                type="primary" 
                class="harmony-btn harmony-btn-primary"
                @click.stop="makeAppointment(shop)"
              >
                <el-icon><calendar /></el-icon>
                立即预约
              </el-button>
              <el-button 
                class="harmony-btn harmony-btn-ghost"
                @click.stop="viewShopDetail(shop)"
              >
                <el-icon><view /></el-icon>
                查看详情
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="filteredShops.length === 0 && !loading" class="harmony-empty">
        <div class="harmony-empty-icon">🏪</div>
        <h3 class="harmony-empty-title">未找到符合条件的门店</h3>
        <p class="harmony-empty-desc">请尝试调整搜索条件或选择其他城市</p>
        <el-button 
          type="primary" 
          class="harmony-btn harmony-btn-primary"
          @click="resetFilters"
        >
          重置筛选
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  Search, 
  Location, 
  Phone, 
  Calendar, 
  View 
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { shopAPI } from '../api/index.js'

export default {
  name: 'ShopList',
  components: {
    Search,
    Location,
    Phone,
    Calendar,
    View
  },
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const shops = ref([])
    const searchKeyword = ref('')
    const selectedCity = ref('')
    const searchTimer = ref(null)

    const filteredShops = computed(() => shops.value)

    const loadShops = async () => {
      loading.value = true
      try {
        const params = {
          current: 1,
          size: 50,
          status: 1
        }

        if (searchKeyword.value) {
          params.keyword = searchKeyword.value
        }

        if (selectedCity.value) {
          params.city = selectedCity.value
        }

        const response = await shopAPI.getShopList(params)

        if (response.code === 200 && response.data) {
          const records = response.data.records || []
          shops.value = records.map(shop => {
            const images = Array.isArray(shop.images)
              ? shop.images
              : (typeof shop.images === 'string' && shop.images.length > 0
                ? shop.images.split(',').map(item => item.trim()).filter(Boolean)
                : [])

            return {
              id: shop.id,
              name: shop.shopName || '未命名门店',
              address: shop.address || '地址待完善',
              phone: shop.phone || '联系电话暂无',
              city: shop.city || '',
              rating: shop.rating || 4.5,
              imageUrl: images[0] || '/default-shop.jpg',
              businessHours: shop.businessHours
            }
          })
        } else {
          shops.value = []
        }
      } catch (error) {
        console.error('加载门店列表失败:', error)
        const message = error.response?.data?.message || '加载门店列表失败'
        ElMessage.error(message)
        shops.value = []
      } finally {
        loading.value = false
      }
    }

    const searchShops = () => {
      if (searchTimer.value) {
        clearTimeout(searchTimer.value)
      }
      searchTimer.value = setTimeout(() => {
        loadShops()
      }, 300)
    }

    const filterShops = () => {
      loadShops()
    }

    const resetFilters = () => {
      searchKeyword.value = ''
      selectedCity.value = ''
      ElMessage.success('筛选条件已重置')
      loadShops()
    }

    const selectShop = (shop) => {
      router.push(`/appointment?shopId=${shop.id}`)
    }

    const makeAppointment = (shop) => {
      router.push(`/appointment?shopId=${shop.id}`)
    }

    const viewShopDetail = (shop) => {
      ElMessage.info(`查看门店详情: ${shop.name}`)
      // 可以跳转到详情页或显示弹窗
    }

    onMounted(() => {
      loadShops()
    })

    onUnmounted(() => {
      if (searchTimer.value) {
        clearTimeout(searchTimer.value)
      }
    })

    return {
      loading,
      shops,
      searchKeyword,
      selectedCity,
      filteredShops,
      searchShops,
      filterShops,
      resetFilters,
      selectShop,
      makeAppointment,
      viewShopDetail
    }
  }
}
</script>

<style scoped>
.harmony-shop-list {
  padding: var(--harmony-space-2xl) 0;
  background: var(--harmony-bg-secondary);
  min-height: calc(100vh - 140px);
  animation: harmony-fadeIn 0.8s ease-out;
}

/* 页面头部 */
.harmony-page-header {
  text-align: center;
  margin-bottom: var(--harmony-space-2xl);
}

.harmony-page-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: var(--harmony-space-md);
  color: var(--harmony-text-primary);
}

.harmony-page-subtitle {
  font-size: 1.1rem;
  color: var(--harmony-text-secondary);
  max-width: 600px;
  margin: 0 auto;
  line-height: 1.6;
}

/* 筛选区域 */
.harmony-filters {
  display: flex;
  gap: var(--harmony-space-lg);
  margin-bottom: var(--harmony-space-2xl);
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
  align-items: center;
}

.harmony-search-wrapper {
  flex: 1;
}

.harmony-filter-wrapper {
  min-width: 180px;
}

.harmony-search-input .el-input__wrapper {
  border-radius: var(--harmony-radius-full) !important;
  box-shadow: var(--harmony-shadow-sm) !important;
  transition: all var(--harmony-transition-base) !important;
}

.harmony-search-input .el-input__wrapper:hover {
  box-shadow: var(--harmony-shadow-md) !important;
}

.harmony-search-input .el-input__wrapper.is-focus {
  box-shadow: 0 0 0 4px var(--harmony-primary-light) !important;
}

.harmony-search-icon {
  color: var(--harmony-primary) !important;
}

.harmony-city-select .el-select__wrapper {
  border-radius: var(--harmony-radius-md) !important;
  box-shadow: var(--harmony-shadow-sm) !important;
}

/* 门店网格 */
.harmony-shops-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: var(--harmony-space-xl);
  margin-bottom: var(--harmony-space-2xl);
}

.harmony-shop-card {
  background: var(--harmony-bg-primary);
  border-radius: var(--harmony-radius-lg);
  overflow: hidden;
  box-shadow: var(--harmony-shadow-sm);
  transition: all var(--harmony-transition-base);
  cursor: pointer;
  position: relative;
  border: 1px solid var(--harmony-border);
}

.harmony-shop-card::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, transparent 0%, rgba(0, 125, 255, 0.05) 100%);
  opacity: 0;
  transition: opacity var(--harmony-transition-base);
}

.harmony-shop-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--harmony-shadow-xl);
}

.harmony-shop-card:hover::after {
  opacity: 1;
}

/* 门店图片 */
.harmony-shop-image {
  height: 220px;
  overflow: hidden;
  position: relative;
}

.harmony-shop-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--harmony-transition-slow);
}

.harmony-shop-card:hover .harmony-shop-image img {
  transform: scale(1.05);
}

.harmony-shop-badge {
  position: absolute;
  top: var(--harmony-space-md);
  right: var(--harmony-space-md);
}

/* 门店信息 */
.harmony-shop-info {
  padding: var(--harmony-space-lg);
  position: relative;
  z-index: 1;
}

.harmony-shop-name {
  font-size: 1.4rem;
  font-weight: 600;
  margin-bottom: var(--harmony-space-md);
  color: var(--harmony-text-primary);
}

.harmony-shop-details {
  margin-bottom: var(--harmony-space-md);
}

.harmony-shop-address,
.harmony-shop-phone {
  display: flex;
  align-items: center;
  gap: var(--harmony-space-sm);
  margin-bottom: var(--harmony-space-sm);
  color: var(--harmony-text-secondary);
  font-size: 0.95rem;
}

.harmony-detail-icon {
  color: var(--harmony-primary);
  font-size: 1.1rem;
}

/* 评分 */
.harmony-shop-rating {
  display: flex;
  align-items: center;
  gap: var(--harmony-space-sm);
  margin-bottom: var(--harmony-space-md);
}

.harmony-rating-text {
  color: var(--harmony-text-secondary);
  font-size: 0.9rem;
  font-weight: 500;
}

/* 标签 */
.harmony-shop-tags {
  display: flex;
  gap: var(--harmony-space-sm);
  margin-bottom: var(--harmony-space-lg);
  flex-wrap: wrap;
}

.harmony-shop-tags .harmony-tag {
  font-size: 0.8rem;
  padding: 4px 12px;
  background: var(--harmony-bg-tertiary);
  color: var(--harmony-text-secondary);
  border-radius: var(--harmony-radius-full);
}

/* 操作按钮 */
.harmony-shop-actions {
  display: flex;
  gap: var(--harmony-space-md);
  margin-top: var(--harmony-space-lg);
}

.harmony-shop-actions .el-button {
  flex: 1;
  border-radius: var(--harmony-radius-full) !important;
  font-weight: 500;
}

/* 空状态 */
.harmony-empty {
  text-align: center;
  padding: var(--harmony-space-2xl);
  background: var(--harmony-bg-primary);
  border-radius: var(--harmony-radius-lg);
  box-shadow: var(--harmony-shadow-sm);
  max-width: 500px;
  margin: 0 auto;
}

.harmony-empty-icon {
  font-size: 4rem;
  margin-bottom: var(--harmony-space-lg);
  opacity: 0.6;
}

.harmony-empty-title {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: var(--harmony-space-md);
  color: var(--harmony-text-primary);
}

.harmony-empty-desc {
  color: var(--harmony-text-secondary);
  margin-bottom: var(--harmony-space-xl);
  line-height: 1.6;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .harmony-filters {
    flex-direction: column;
    gap: var(--harmony-space-md);
  }
  
  .harmony-filter-wrapper {
    width: 100%;
  }
  
  .harmony-shops-grid {
    grid-template-columns: 1fr;
    gap: var(--harmony-space-lg);
  }
  
  .harmony-page-title {
    font-size: 2rem;
  }
  
  .harmony-shop-actions {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .harmony-shop-list {
    padding: var(--harmony-space-xl) 0;
  }
  
  .harmony-page-title {
    font-size: 1.8rem;
  }
  
  .harmony-shop-info {
    padding: var(--harmony-space-md);
  }
  
  .harmony-shop-image {
    height: 180px;
  }
}

/* 加载状态增强 */
.el-loading-mask {
  border-radius: var(--harmony-radius-lg);
}

.el-loading-text {
  color: var(--harmony-primary) !important;
  font-weight: 500;
}

/* 动画增强 */
.harmony-shop-card {
  animation: harmony-slideUp 0.6s ease-out;
}

.harmony-shop-card:nth-child(1) { animation-delay: 0.1s; }
.harmony-shop-card:nth-child(2) { animation-delay: 0.2s; }
.harmony-shop-card:nth-child(3) { animation-delay: 0.3s; }
.harmony-shop-card:nth-child(4) { animation-delay: 0.4s; }
.harmony-shop-card:nth-child(5) { animation-delay: 0.5s; }
.harmony-shop-card:nth-child(6) { animation-delay: 0.6s; }

/* Element Plus 组件样式覆盖 */
.harmony-rate .el-rate__icon {
  color: #FFD700 !important;
  margin-right: 2px !important;
}

.harmony-tag-primary {
  background: var(--harmony-primary-light) !important;
  color: var(--harmony-primary) !important;
  border: 1px solid var(--harmony-primary) !important;
}
</style>