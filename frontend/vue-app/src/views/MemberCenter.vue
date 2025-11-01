<template>
<div class="member-center">
    <div class="container">
      <h2>会员中心</h2>
      <div class="member-content" v-loading="loading">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="个人信息" name="profile">
            <div class="profile-section" v-if="userInfo">
              <h3>基本信息</h3>
              <div class="info-grid">
                <div class="info-item">
                  <label>用户名：</label>
                  <span>{{ userInfo.username || '-' }}</span>
                </div>
                <div class="info-item">
                  <label>真实姓名：</label>
                  <span>{{ userInfo.realName || '-' }}</span>
                </div>
                <div class="info-item">
                  <label>邮箱：</label>
                  <span>{{ userInfo.email || '-' }}</span>
                </div>
                <div class="info-item">
                  <label>手机号：</label>
                  <span>{{ userInfo.phone || '-' }}</span>
                </div>
                <div class="info-item">
                  <label>会员等级：</label>
                  <el-tag :type="memberLevelType">
                    {{ currentLevelName }}
                  </el-tag>
                </div>
                <div class="info-item">
                  <label>注册时间：</label>
                  <span>{{ formatDate(userInfo.createTime) }}</span>
                </div>
                <div class="info-item" v-if="memberStats">
                  <label>累计经验：</label>
                  <span>{{ memberStats.totalExperience }}</span>
                </div>
                <div class="info-item" v-if="memberStats">
                  <label>可用经验：</label>
                  <span>{{ memberStats.availableExperience }}</span>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无用户信息" />
          </el-tab-pane>

          <el-tab-pane label="我的车辆" name="vehicles">
            <VehicleManagement />
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import VehicleManagement from '../components/VehicleManagement.vue'
import { userAPI, memberAPI } from '../api/index.js'

export default {
  name: 'MemberCenter',
  components: {
    VehicleManagement
  },
  setup() {
    const activeTab = ref('profile')
    const loading = ref(false)
    const userInfo = ref(null)
    const memberInfo = ref(null)

    const memberStats = computed(() => memberInfo.value?.userMember || null)
    const currentLevelName = computed(() => memberInfo.value?.currentLevel?.levelName || '普通会员')

    const memberLevelType = computed(() => {
      const levelName = currentLevelName.value
      if (!levelName) return ''
      if (levelName.includes('钻石') || levelName.includes('铂金')) return 'success'
      if (levelName.includes('黄金')) return 'warning'
      if (levelName.includes('白银')) return 'info'
      return ''
    })

    const formatDate = (value) => {
      if (!value) return '-'
      const date = new Date(value)
      if (Number.isNaN(date.getTime())) {
        return value
      }
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
    }

    const loadData = async () => {
      loading.value = true
      try {
        const [profileRes, memberRes] = await Promise.all([
          userAPI.getProfile(),
          memberAPI.getMyInfo()
        ])
        userInfo.value = profileRes.data || {}
        memberInfo.value = memberRes.data || {}
      } catch (error) {
        console.error('加载会员信息失败:', error)
        ElMessage.error(error?.response?.data?.message || error.message || '加载会员信息失败')
      } finally {
        loading.value = false
      }
    }

    onMounted(() => {
      loadData()
    })

    return {
      activeTab,
      loading,
      userInfo,
      memberInfo,
      memberStats,
      memberLevelType,
      currentLevelName,
      formatDate
    }
  }
}
</script>

<style scoped>
.member-center {
  padding: 40px 0;
  background: #f8f9fa;
  min-height: calc(100vh - 200px);
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px;
}

.member-center h2 {
  text-align: center;
  color: #2c3e50;
  margin-bottom: 40px;
  font-size: 2rem;
}

.member-content {
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.profile-section h3 {
  margin-bottom: 20px;
  color: #2c3e50;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.info-item {
  display: flex;
  padding: 15px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
}

.info-item label {
  font-weight: bold;
  color: #2c3e50;
  margin-right: 10px;
  min-width: 80px;
}

.vehicles-section h3 {
  margin-bottom: 20px;
  color: #2c3e50;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.vehicles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.vehicle-card {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.vehicle-info h4 {
  margin-bottom: 10px;
  color: #2c3e50;
}

.vehicle-info p {
  color: #7f8c8d;
  margin-bottom: 5px;
}
</style>
