<template>
  <div class="harmony-announcements-page">
    <div class="harmony-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1 class="page-title">
          <el-icon class="title-icon"><bell /></el-icon>
          <span class="harmony-text-gradient">系统公告</span>
        </h1>
        <p class="page-subtitle">查看最新的系统通知和活动信息</p>
      </div>

      <!-- 公告列表 -->
      <div class="announcements-container">
        <div v-if="loading" class="loading-state">
          <el-icon class="is-loading"><loading /></el-icon>
          <span>加载中...</span>
        </div>

        <div v-else-if="announcements.length === 0" class="empty-state">
          <el-empty description="暂无公告" />
        </div>

        <div v-else class="announcements-list">
          <div
            v-for="announcement in announcements"
            :key="announcement.id"
            class="announcement-card"
            @click="viewAnnouncementDetail(announcement)"
          >
            <div class="card-header">
              <div class="card-title-section">
                <el-tag
                  v-if="announcement.isImportant"
                  type="danger"
                  size="small"
                  class="important-tag"
                >
                  <el-icon><warning-filled /></el-icon>
                  重要
                </el-tag>
                <el-tag
                  :type="getTypeTagType(announcement.type)"
                  size="small"
                >
                  {{ getTypeText(announcement.type) }}
                </el-tag>
                <h3 class="card-title">{{ announcement.title }}</h3>
              </div>
              <span class="card-time">{{ formatTime(announcement.createTime) }}</span>
            </div>

            <p class="card-summary">
              {{ announcement.summary || announcement.content.substring(0, 150) + '...' }}
            </p>

            <div class="card-footer">
              <el-button text type="primary">
                查看详情 <el-icon><arrow-right /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 公告详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="selectedAnnouncement?.title"
      width="700px"
      class="announcement-detail-dialog"
    >
      <div v-if="selectedAnnouncement" class="announcement-detail">
        <div class="detail-meta">
          <el-tag
            v-if="selectedAnnouncement.isImportant"
            type="danger"
            size="small"
          >
            <el-icon><warning-filled /></el-icon>
            重要公告
          </el-tag>
          <el-tag
            :type="getTypeTagType(selectedAnnouncement.type)"
            size="small"
          >
            {{ getTypeText(selectedAnnouncement.type) }}
          </el-tag>
          <span class="detail-date">{{ formatDate(selectedAnnouncement.createTime) }}</span>
        </div>
        <div class="detail-content" v-html="selectedAnnouncement.content"></div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { Bell, ArrowRight, Loading, WarningFilled } from '@element-plus/icons-vue'
import request from '../api/index.js'

export default {
  name: 'Announcements',
  components: {
    Bell,
    ArrowRight,
    Loading,
    WarningFilled
  },
  setup() {
    const announcements = ref([])
    const loading = ref(true)
    const detailDialogVisible = ref(false)
    const selectedAnnouncement = ref(null)

    // 加载公告列表
    const loadAnnouncements = async () => {
      loading.value = true
      try {
        const response = await request.get('/announcement/active')
        if (response.code === 200 && response.data) {
          announcements.value = response.data
        }
      } catch (error) {
        console.error('加载公告失败:', error)
        // 使用模拟数据作为后备
        announcements.value = getMockAnnouncements()
      } finally {
        loading.value = false
      }
    }

    // 模拟数据
    const getMockAnnouncements = () => {
      return [
        {
          id: 1,
          title: '系统维护通知：今晚23:00-01:00进行系统升级',
          content: '<p>尊敬的用户：</p><p>为了提供更好的服务体验，我们将于<strong>今晚23:00-01:00</strong>进行系统维护升级。</p><p>维护期间可能影响的功能：</p><ul><li>在线预约服务</li><li>订单查询</li><li>会员中心</li></ul><p>给您带来的不便，我们深表歉意。感谢您的理解与支持！</p>',
          summary: '系统维护升级通知，今晚23:00-01:00进行，期间部分功能受影响',
          type: 3, // 维护公告
          isImportant: true,
          createTime: new Date(Date.now() - 2 * 60 * 60 * 1000)
        },
        {
          id: 2,
          title: '新增门店：深圳南山汽车服务中心正式营业',
          content: '<p>我们很高兴地宣布，<strong>深圳南山汽车服务中心</strong>已正式开业！</p><p>门店地址：深圳市南山区科技园南区</p><p>营业时间：周一至周日 08:00-20:00</p><p>提供服务：</p><ul><li>专业汽车保养</li><li>汽车维修</li><li>汽车美容</li><li>轮胎更换</li></ul><p>现在预约享受<strong>开业8折优惠</strong>！</p>',
          summary: '深圳南山汽车服务中心正式开业，提供专业保养维修服务，开业8折优惠',
          type: 1, // 系统公告
          isImportant: false,
          createTime: new Date(Date.now() - 24 * 60 * 60 * 1000)
        },
        {
          id: 3,
          title: '春季保养优惠活动开始啦！',
          content: '<p>春季汽车保养套餐<strong>限时优惠</strong>！</p><p>原价：<del>399元</del></p><p>现价：<strong style="color: #e74c3c; font-size: 24px;">299元</strong></p><p>套餐包含：</p><ul><li>机油更换（4L全合成机油）</li><li>机滤更换</li><li>空调滤芯更换</li><li>全车60项免费检测</li></ul><p>活动时间：2025年3月1日-3月31日</p><p><a href="/shops">点击预约</a></p>',
          summary: '春季保养套餐优惠，原价399元现价299元，包含机油更换、滤芯更换等服务',
          type: 2, // 活动公告
          isImportant: false,
          createTime: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000)
        },
        {
          id: 4,
          title: '会员积分规则更新通知',
          content: '<p>尊敬的会员用户：</p><p>为了回馈广大会员的支持，我们对会员积分规则进行了优化升级：</p><p><strong>新规则：</strong></p><ol><li>每消费1元可获得2积分（原为1积分）</li><li>积分永久有效，不再设置过期时间</li><li>新增积分兑换礼品功能</li><li>生日月可获得双倍积分</li></ol><p>新规则将于2025年3月1日起生效。</p>',
          summary: '会员积分规则优化升级，消费积分翻倍，积分永久有效，新增兑换礼品功能',
          type: 1,
          isImportant: false,
          createTime: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000)
        }
      ]
    }

    // 查看公告详情
    const viewAnnouncementDetail = (announcement) => {
      selectedAnnouncement.value = announcement
      detailDialogVisible.value = true
    }

    // 获取公告类型文字
    const getTypeText = (type) => {
      const typeMap = {
        1: '系统公告',
        2: '活动公告',
        3: '维护公告'
      }
      return typeMap[type] || '公告'
    }

    // 获取类型标签颜色
    const getTypeTagType = (type) => {
      const typeMap = {
        1: 'primary',
        2: 'success',
        3: 'warning'
      }
      return typeMap[type] || 'info'
    }

    // 格式化时间
    const formatTime = (time) => {
      const now = new Date()
      const date = new Date(time)
      const diff = now - date
      const hours = Math.floor(diff / (1000 * 60 * 60))
      const days = Math.floor(diff / (1000 * 60 * 60 * 24))

      if (hours < 1) return '刚刚发布'
      if (hours < 24) return `${hours}小时前`
      if (days < 7) return `${days}天前`
      return formatDate(time)
    }

    // 格式化日期
    const formatDate = (time) => {
      return new Date(time).toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    // 页面加载时获取公告
    onMounted(() => {
      loadAnnouncements()
    })

    return {
      announcements,
      loading,
      detailDialogVisible,
      selectedAnnouncement,
      viewAnnouncementDetail,
      getTypeText,
      getTypeTagType,
      formatTime,
      formatDate
    }
  }
}
</script>

<style scoped>
.harmony-announcements-page {
  min-height: calc(100vh - 140px);
  padding: var(--harmony-space-2xl) 0;
  background: var(--harmony-bg-secondary);
}

/* 页面头部 */
.page-header {
  text-align: center;
  margin-bottom: var(--harmony-space-2xl);
  animation: harmony-fadeIn 0.6s ease-out;
}

.page-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--harmony-space-md);
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: var(--harmony-space-md);
  color: var(--harmony-text-primary);
}

.title-icon {
  font-size: 2.5rem;
  color: var(--harmony-primary);
}

.page-subtitle {
  font-size: 1.1rem;
  color: var(--harmony-text-secondary);
  margin: 0;
}

/* 公告列表容器 */
.announcements-container {
  max-width: 1000px;
  margin: 0 auto;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--harmony-space-md);
  padding: var(--harmony-space-2xl);
  color: var(--harmony-text-secondary);
}

.loading-state .el-icon {
  font-size: 2rem;
}

/* 空状态 */
.empty-state {
  background: var(--harmony-bg-primary);
  border-radius: var(--harmony-radius-lg);
  padding: var(--harmony-space-2xl);
  box-shadow: var(--harmony-shadow-sm);
}

/* 公告列表 */
.announcements-list {
  display: flex;
  flex-direction: column;
  gap: var(--harmony-space-lg);
}

/* 公告卡片 */
.announcement-card {
  background: var(--harmony-bg-primary);
  border-radius: var(--harmony-radius-lg);
  padding: var(--harmony-space-xl);
  box-shadow: var(--harmony-shadow-sm);
  border: 1px solid var(--harmony-border);
  cursor: pointer;
  transition: all var(--harmony-transition-base);
  animation: harmony-slideUp 0.5s ease-out;
  position: relative;
  overflow: hidden;
}

.announcement-card::before {
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

.announcement-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--harmony-shadow-lg);
  border-color: var(--harmony-primary);
}

.announcement-card:hover::before {
  transform: scaleX(1);
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--harmony-space-md);
  gap: var(--harmony-space-lg);
}

.card-title-section {
  flex: 1;
  display: flex;
  align-items: center;
  gap: var(--harmony-space-sm);
  flex-wrap: wrap;
}

.important-tag {
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-title {
  font-size: 1.3rem;
  font-weight: 600;
  color: var(--harmony-text-primary);
  margin: 0;
  flex: 1 1 100%;
  margin-top: var(--harmony-space-sm);
}

.card-time {
  color: var(--harmony-text-tertiary);
  font-size: 0.9rem;
  white-space: nowrap;
}

/* 卡片摘要 */
.card-summary {
  color: var(--harmony-text-secondary);
  line-height: 1.6;
  margin: 0 0 var(--harmony-space-lg) 0;
  font-size: 1rem;
}

/* 卡片底部 */
.card-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: var(--harmony-space-md);
  border-top: 1px solid var(--harmony-border);
}

/* 公告详情弹窗 */
.announcement-detail {
  padding: var(--harmony-space-md) 0;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: var(--harmony-space-md);
  margin-bottom: var(--harmony-space-lg);
  padding-bottom: var(--harmony-space-md);
  border-bottom: 1px solid var(--harmony-border);
  flex-wrap: wrap;
}

.detail-date {
  color: var(--harmony-text-secondary);
  font-size: 0.95rem;
}

.detail-content {
  line-height: 1.8;
  color: var(--harmony-text-primary);
  font-size: 1rem;
}

.detail-content :deep(p) {
  margin: var(--harmony-space-md) 0;
}

.detail-content :deep(ul),
.detail-content :deep(ol) {
  padding-left: var(--harmony-space-xl);
  margin: var(--harmony-space-md) 0;
}

.detail-content :deep(li) {
  margin: var(--harmony-space-sm) 0;
}

.detail-content :deep(strong) {
  color: var(--harmony-primary);
  font-weight: 600;
}

.detail-content :deep(a) {
  color: var(--harmony-primary);
  text-decoration: underline;
}

.detail-content :deep(a:hover) {
  color: var(--harmony-primary-dark);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-title {
    font-size: 2rem;
  }

  .page-subtitle {
    font-size: 1rem;
  }

  .announcement-card {
    padding: var(--harmony-space-lg);
  }

  .card-header {
    flex-direction: column;
    gap: var(--harmony-space-sm);
  }

  .card-time {
    align-self: flex-start;
  }

  .card-title {
    font-size: 1.1rem;
  }

  .card-summary {
    font-size: 0.95rem;
  }
}

@media (max-width: 480px) {
  .harmony-announcements-page {
    padding: var(--harmony-space-lg) 0;
  }

  .page-title {
    font-size: 1.8rem;
  }

  .title-icon {
    font-size: 2rem;
  }

  .announcement-card {
    padding: var(--harmony-space-md);
  }
}

/* 动画效果 */
.announcement-card:nth-child(1) { animation-delay: 0.1s; }
.announcement-card:nth-child(2) { animation-delay: 0.2s; }
.announcement-card:nth-child(3) { animation-delay: 0.3s; }
.announcement-card:nth-child(4) { animation-delay: 0.4s; }
.announcement-card:nth-child(5) { animation-delay: 0.5s; }
</style>
