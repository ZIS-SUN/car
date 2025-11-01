<template>
  <div class="harmony-home">
    <!-- 首页横幅 -->
    <section class="harmony-hero">
      <div class="harmony-container">
        <div class="harmony-hero-content">
          <div class="harmony-hero-text">
            <h2 class="harmony-hero-title">
              <span class="harmony-text-gradient">专业汽车保养</span>
              <br>在线轻松预约
            </h2>
            <p class="harmony-hero-subtitle">告别排队等待，享受便捷的汽车保养服务</p>
            <div class="harmony-hero-actions">
              <el-button 
                type="primary" 
                size="large" 
                @click="goToShops" 
                class="harmony-btn harmony-btn-primary harmony-btn-large"
              >
                <el-icon><calendar /></el-icon>
                立即预约
              </el-button>
              <el-button 
                size="large" 
                @click="learnMore"
                class="harmony-btn harmony-btn-ghost harmony-btn-large"
              >
                <el-icon><info-filled /></el-icon>
                了解更多
              </el-button>
            </div>
          </div>
          <div class="harmony-hero-image">
            <div class="harmony-hero-car">🚗</div>
            <div class="harmony-hero-decorations">
              <div class="harmony-decoration harmony-decoration-1">⚙️</div>
              <div class="harmony-decoration harmony-decoration-2">🔧</div>
              <div class="harmony-decoration harmony-decoration-3">🛠️</div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 服务特色 -->
    <section class="harmony-features">
      <div class="harmony-container">
        <h3 class="harmony-section-title">
          <span class="harmony-text-gradient">为什么选择我们</span>
        </h3>
        <div class="harmony-features-grid">
          <div class="harmony-feature-card" v-for="(feature, index) in features" :key="index">
            <div class="harmony-feature-icon">{{ feature.icon }}</div>
            <h4 class="harmony-feature-title">{{ feature.title }}</h4>
            <p class="harmony-feature-desc">{{ feature.description }}</p>
            <div class="harmony-feature-badge">{{ feature.badge }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 统计数据 -->
    <section class="harmony-stats">
      <div class="harmony-container">
        <div class="harmony-stats-grid">
          <div class="harmony-stat-item" v-for="(stat, index) in stats" :key="index">
            <div class="harmony-stat-number">{{ stat.number }}</div>
            <div class="harmony-stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 快速入口 -->
    <section class="harmony-quick-actions">
      <div class="harmony-container">
        <h3 class="harmony-section-title">快速入口</h3>
        <div class="harmony-actions-grid">
          <div 
            class="harmony-action-card" 
            v-for="(action, index) in quickActions" 
            :key="index"
            @click="handleQuickAction(action)"
          >
            <div class="harmony-action-icon">{{ action.icon }}</div>
            <h4 class="harmony-action-title">{{ action.title }}</h4>
            <p class="harmony-action-desc">{{ action.description }}</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Calendar, InfoFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

export default {
  name: 'Home',
  components: {
    Calendar,
    InfoFilled
  },
  setup() {
    const router = useRouter()

    // 特色功能数据
    const features = ref([
      {
        icon: '⏰',
        title: '在线预约',
        description: '随时随地预约，无需等待',
        badge: '便捷'
      },
      {
        icon: '🏪',
        title: '专业门店',
        description: '严选优质门店，服务有保障',
        badge: '专业'
      },
      {
        icon: '💰',
        title: '价格透明',
        description: '明码标价，无隐形消费',
        badge: '透明'
      },
      {
        icon: '⭐',
        title: '真实评价',
        description: '用户真实评价，选择更放心',
        badge: '可信'
      }
    ])

    // 统计数据
    const stats = ref([
      { number: '1000+', label: '合作门店' },
      { number: '50000+', label: '服务用户' },
      { number: '98%', label: '满意度' },
      { number: '24h', label: '在线服务' }
    ])

    // 快速入口
    const quickActions = ref([
      {
        icon: '🔍',
        title: '查找门店',
        description: '找到附近的保养门店',
        action: 'shops'
      },
      {
        icon: '📅',
        title: '我的预约',
        description: '查看预约记录',
        action: 'appointments'
      },
      {
        icon: '📋',
        title: '我的订单',
        description: '订单管理中心',
        action: 'orders'
      },
      {
        icon: '⭐',
        title: '我的评价',
        description: '查看服务评价',
        action: 'reviews'
      }
    ])

    const goToShops = () => {
      router.push('/shops')
    }

    const learnMore = () => {
      ElMessage.info('了解更多功能正在开发中...')
    }

    const handleQuickAction = (action) => {
      switch (action.action) {
        case 'shops':
          router.push('/shops')
          break
        case 'appointments':
          router.push('/my-appointments')
          break
        case 'orders':
          router.push('/my-orders')
          break
        case 'reviews':
          router.push('/my-reviews')
          break
        default:
          ElMessage.info(`${action.title}功能正在开发中...`)
      }
    }

    return {
      features,
      stats,
      quickActions,
      goToShops,
      learnMore,
      handleQuickAction
    }
  }
}
</script>

<style scoped>
.harmony-home {
  animation: harmony-fadeIn 0.8s ease-out;
}

/* 英雄区域 */
.harmony-hero {
  background: var(--harmony-gradient-1);
  color: white;
  padding: var(--harmony-space-2xl) 0;
  position: relative;
  overflow: hidden;
}

.harmony-hero::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  animation: harmony-float 20s ease-in-out infinite;
}

.harmony-hero-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--harmony-space-2xl);
  align-items: center;
  position: relative;
  z-index: 1;
}

.harmony-hero-title {
  font-size: 3.5rem;
  font-weight: 700;
  margin-bottom: var(--harmony-space-lg);
  line-height: 1.2;
}

.harmony-hero-subtitle {
  font-size: 1.3rem;
  margin-bottom: var(--harmony-space-xl);
  opacity: 0.9;
  line-height: 1.6;
}

.harmony-hero-actions {
  display: flex;
  gap: var(--harmony-space-lg);
  flex-wrap: wrap;
}

.harmony-hero-image {
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
}

.harmony-hero-car {
  font-size: 8rem;
  animation: harmony-float 6s ease-in-out infinite;
  filter: drop-shadow(0 10px 20px rgba(0, 0, 0, 0.3));
}

.harmony-hero-decorations {
  position: absolute;
  width: 100%;
  height: 100%;
}

.harmony-decoration {
  position: absolute;
  font-size: 2rem;
  opacity: 0.7;
}

.harmony-decoration-1 {
  top: 20%;
  left: 10%;
  animation: harmony-float 8s ease-in-out infinite;
}

.harmony-decoration-2 {
  top: 60%;
  right: 20%;
  animation: harmony-float 10s ease-in-out infinite reverse;
}

.harmony-decoration-3 {
  bottom: 20%;
  left: 30%;
  animation: harmony-float 12s ease-in-out infinite;
}

/* 特色功能 */
.harmony-features {
  padding: var(--harmony-space-2xl) 0;
  background: var(--harmony-bg-secondary);
}

.harmony-section-title {
  text-align: center;
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: var(--harmony-space-2xl);
  color: var(--harmony-text-primary);
}

.harmony-features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: var(--harmony-space-xl);
}

.harmony-feature-card {
  background: var(--harmony-bg-primary);
  border-radius: var(--harmony-radius-lg);
  padding: var(--harmony-space-xl);
  text-align: center;
  box-shadow: var(--harmony-shadow-sm);
  transition: all var(--harmony-transition-base);
  border: 1px solid var(--harmony-border);
  position: relative;
  overflow: hidden;
}

.harmony-feature-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--harmony-primary-gradient);
  transform: scaleX(0);
  transition: transform var(--harmony-transition-base);
}

.harmony-feature-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--harmony-shadow-lg);
}

.harmony-feature-card:hover::before {
  transform: scaleX(1);
}

.harmony-feature-icon {
  font-size: 4rem;
  margin-bottom: var(--harmony-space-lg);
  background: var(--harmony-primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.harmony-feature-title {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: var(--harmony-space-md);
  color: var(--harmony-text-primary);
}

.harmony-feature-desc {
  color: var(--harmony-text-secondary);
  line-height: 1.6;
  margin-bottom: var(--harmony-space-lg);
}

.harmony-feature-badge {
  display: inline-block;
  padding: 6px 16px;
  background: var(--harmony-primary-light);
  color: var(--harmony-primary);
  border-radius: var(--harmony-radius-full);
  font-size: 0.9rem;
  font-weight: 500;
}

/* 统计数据 */
.harmony-stats {
  padding: var(--harmony-space-2xl) 0;
  background: var(--harmony-bg-primary);
}

.harmony-stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--harmony-space-xl);
}

.harmony-stat-item {
  text-align: center;
  padding: var(--harmony-space-lg);
}

.harmony-stat-number {
  font-size: 3rem;
  font-weight: 700;
  background: var(--harmony-primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: var(--harmony-space-sm);
}

.harmony-stat-label {
  color: var(--harmony-text-secondary);
  font-size: 1.1rem;
  font-weight: 500;
}

/* 快速入口 */
.harmony-quick-actions {
  padding: var(--harmony-space-2xl) 0;
  background: var(--harmony-bg-secondary);
}

.harmony-actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: var(--harmony-space-lg);
}

.harmony-action-card {
  background: var(--harmony-bg-primary);
  border-radius: var(--harmony-radius-lg);
  padding: var(--harmony-space-xl);
  text-align: center;
  box-shadow: var(--harmony-shadow-sm);
  transition: all var(--harmony-transition-base);
  cursor: pointer;
  border: 1px solid var(--harmony-border);
  position: relative;
  overflow: hidden;
}

.harmony-action-card::after {
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

.harmony-action-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--harmony-shadow-xl);
}

.harmony-action-card:hover::after {
  opacity: 1;
}

.harmony-action-icon {
  font-size: 3rem;
  margin-bottom: var(--harmony-space-lg);
  position: relative;
  z-index: 1;
}

.harmony-action-title {
  font-size: 1.3rem;
  font-weight: 600;
  margin-bottom: var(--harmony-space-sm);
  color: var(--harmony-text-primary);
  position: relative;
  z-index: 1;
}

.harmony-action-desc {
  color: var(--harmony-text-secondary);
  line-height: 1.6;
  position: relative;
  z-index: 1;
}

/* 按钮增强 */
.harmony-btn-large {
  padding: 16px 32px !important;
  font-size: 1.1rem !important;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .harmony-hero-content {
    grid-template-columns: 1fr;
    text-align: center;
    gap: var(--harmony-space-xl);
  }
  
  .harmony-hero-title {
    font-size: 2.5rem;
  }
  
  .harmony-hero-subtitle {
    font-size: 1.1rem;
  }
  
  .harmony-hero-actions {
    justify-content: center;
  }
  
  .harmony-hero-car {
    font-size: 6rem;
  }
  
  .harmony-section-title {
    font-size: 2rem;
  }
  
  .harmony-features-grid,
  .harmony-actions-grid {
    grid-template-columns: 1fr;
  }
  
  .harmony-stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .harmony-hero {
    padding: var(--harmony-space-xl) 0;
  }
  
  .harmony-hero-title {
    font-size: 2rem;
  }
  
  .harmony-hero-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .harmony-btn-large {
    width: 100%;
    max-width: 280px;
  }
  
  .harmony-stats-grid {
    grid-template-columns: 1fr;
  }
  
  .harmony-stat-number {
    font-size: 2.5rem;
  }
}

/* 动画增强 */
.harmony-feature-card,
.harmony-action-card,
.harmony-stat-item {
  animation: harmony-slideUp 0.6s ease-out;
}

.harmony-feature-card:nth-child(1) { animation-delay: 0.1s; }
.harmony-feature-card:nth-child(2) { animation-delay: 0.2s; }
.harmony-feature-card:nth-child(3) { animation-delay: 0.3s; }
.harmony-feature-card:nth-child(4) { animation-delay: 0.4s; }

.harmony-action-card:nth-child(1) { animation-delay: 0.1s; }
.harmony-action-card:nth-child(2) { animation-delay: 0.2s; }
.harmony-action-card:nth-child(3) { animation-delay: 0.3s; }
.harmony-action-card:nth-child(4) { animation-delay: 0.4s; }
</style>