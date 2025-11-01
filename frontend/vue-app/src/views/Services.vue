<template>
  <div class="harmony-services">
    <div class="harmony-container">
      <!-- 页面标题 -->
      <div class="harmony-page-header">
        <h2 class="harmony-page-title">
          <span class="harmony-text-gradient">服务项目</span>
        </h2>
        <p class="harmony-page-subtitle">为您提供全方位的汽车保养维修服务</p>
      </div>

      <!-- 服务分类 -->
      <div class="harmony-services-grid">
        <div class="harmony-service-card" v-for="service in services" :key="service.id">
          <div class="harmony-service-icon">{{ service.icon }}</div>
          <h3 class="harmony-service-title">{{ service.title }}</h3>
          <p class="harmony-service-desc">{{ service.description }}</p>
          <div class="harmony-service-price">
            起步价：<span class="harmony-price">¥{{ service.startPrice }}</span>
          </div>
          <el-button type="primary" class="harmony-btn harmony-btn-primary" @click="goToShops">
            立即预约
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

export default {
  name: 'Services',
  setup() {
    const router = useRouter()

    const services = ref([
      {
        id: 1,
        icon: '🔧',
        title: '基础保养',
        description: '机油更换、机滤、空滤、汽滤更换及全车检查',
        startPrice: 299
      },
      {
        id: 2,
        icon: '🛞',
        title: '轮胎服务',
        description: '轮胎更换、平衡、四轮定位、补胎服务',
        startPrice: 80
      },
      {
        id: 3,
        icon: '🔩',
        title: '刹车系统',
        description: '刹车片更换、刹车油更换、刹车系统检测',
        startPrice: 450
      },
      {
        id: 4,
        icon: '❄️',
        title: '空调服务',
        description: '空调清洗、空调检修、制冷剂添加',
        startPrice: 180
      },
      {
        id: 5,
        icon: '🔋',
        title: '电瓶服务',
        description: '电瓶检测、电瓶更换、充电服务',
        startPrice: 380
      },
      {
        id: 6,
        icon: '✨',
        title: '美容清洗',
        description: '车身清洗、内饰清洁、打蜡抛光、镀晶服务',
        startPrice: 220
      }
    ])

    const goToShops = () => {
      router.push('/shops')
    }

    return {
      services,
      goToShops
    }
  }
}
</script>

<style scoped>
.harmony-services {
  min-height: calc(100vh - 200px);
  padding: var(--harmony-space-xl) 0;
}

.harmony-page-header {
  text-align: center;
  margin-bottom: var(--harmony-space-xl);
}

.harmony-page-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: var(--harmony-space-md);
}

.harmony-page-subtitle {
  font-size: 1.1rem;
  color: var(--harmony-text-secondary);
}

.harmony-services-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: var(--harmony-space-xl);
  margin-top: var(--harmony-space-xl);
}

.harmony-service-card {
  background: var(--harmony-glass-bg);
  backdrop-filter: blur(10px);
  border: 1px solid var(--harmony-border);
  border-radius: var(--harmony-radius-lg);
  padding: var(--harmony-space-xl);
  text-align: center;
  transition: all var(--harmony-transition-base);
}

.harmony-service-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--harmony-shadow-lg);
  border-color: var(--harmony-primary);
}

.harmony-service-icon {
  font-size: 4rem;
  margin-bottom: var(--harmony-space-lg);
}

.harmony-service-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--harmony-text-primary);
  margin-bottom: var(--harmony-space-md);
}

.harmony-service-desc {
  color: var(--harmony-text-secondary);
  line-height: 1.6;
  margin-bottom: var(--harmony-space-lg);
}

.harmony-service-price {
  font-size: 1.1rem;
  color: var(--harmony-text-secondary);
  margin-bottom: var(--harmony-space-lg);
}

.harmony-price {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--harmony-primary);
}

.harmony-btn {
  width: 100%;
  padding: 12px 24px;
  border-radius: var(--harmony-radius-full);
  font-weight: 500;
  transition: all var(--harmony-transition-base);
  border: none;
  cursor: pointer;
}

.harmony-btn-primary {
  background: var(--harmony-primary-gradient);
  color: white;
}

.harmony-btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 125, 255, 0.4);
}

@media (max-width: 768px) {
  .harmony-services-grid {
    grid-template-columns: 1fr;
  }

  .harmony-page-title {
    font-size: 2rem;
  }
}
</style>
