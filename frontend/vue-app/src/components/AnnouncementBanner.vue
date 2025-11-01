<template>
  <div v-if="announcements.length > 0 && bannerVisible" class="announcement-banner">
    <div class="harmony-container">
      <div class="announcement-content">
        <div class="announcement-icon">
          <el-icon><bell /></el-icon>
        </div>
        <div class="announcement-carousel">
          <el-carousel
            :interval="5000"
            :show-indicators="false"
            arrow="never"
            height="auto"
            class="announcement-slider"
          >
            <el-carousel-item v-for="announcement in announcements" :key="announcement.id">
              <div class="announcement-item" @click="viewAnnouncement(announcement)">
                <span class="announcement-badge" v-if="announcement.isImportant">重要</span>
                <span class="announcement-title">{{ announcement.title }}</span>
                <span class="announcement-time">{{ formatTime(announcement.createTime) }}</span>
              </div>
            </el-carousel-item>
          </el-carousel>
        </div>
        <div class="announcement-actions">
          <el-button text @click="showAllAnnouncements">
            查看全部
            <el-icon><arrow-right /></el-icon>
          </el-button>
          <el-button text @click="closeAnnouncement" class="close-btn">
            <el-icon><close /></el-icon>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 公告详情弹窗 -->
    <el-dialog
      v-model="announcementDialogVisible"
      :title="selectedAnnouncement?.title"
      width="600px"
      class="announcement-dialog"
    >
      <div v-if="selectedAnnouncement" class="announcement-detail">
        <div class="announcement-meta">
          <el-tag v-if="selectedAnnouncement.isImportant" type="danger" size="small">重要公告</el-tag>
          <span class="announcement-date">{{ formatDate(selectedAnnouncement.createTime) }}</span>
        </div>
        <div class="announcement-body" v-html="selectedAnnouncement.content"></div>
      </div>
    </el-dialog>

    <!-- 全部公告弹窗 -->
    <el-dialog
      v-model="allAnnouncementsVisible"
      title="系统公告"
      width="800px"
      class="announcements-list-dialog"
    >
      <div class="announcements-list">
        <div
          v-for="announcement in allAnnouncements"
          :key="announcement.id"
          class="announcement-list-item"
          @click="viewAnnouncement(announcement)"
        >
          <div class="list-item-header">
            <h4 class="list-item-title">
              <el-tag v-if="announcement.isImportant" type="danger" size="small">重要</el-tag>
              {{ announcement.title }}
            </h4>
            <span class="list-item-time">{{ formatDate(announcement.createTime) }}</span>
          </div>
          <p class="list-item-summary">{{ announcement.summary || announcement.content.substring(0, 100) + '...' }}</p>
        </div>
      </div>
      <div v-if="allAnnouncements.length === 0" class="empty-announcements">
        <el-empty description="暂无公告" />
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { Bell, ArrowRight, Close } from '@element-plus/icons-vue'

export default {
  name: 'AnnouncementBanner',
  components: {
    Bell,
    ArrowRight,
    Close
  },
  setup() {
    const announcements = ref([])
    const allAnnouncements = ref([])
    const announcementDialogVisible = ref(false)
    const allAnnouncementsVisible = ref(false)
    const selectedAnnouncement = ref(null)
    const bannerVisible = ref(true)

    const loadAnnouncements = async () => {
      try {
        // 使用公共API获取活跃公告数据
        const { default: request } = await import('../api/index.js')
        const response = await request.get('/announcement/active')

        if (response.code === 200 && response.data) {
          const allAnnouncementData = response.data || []

          // 只显示最新的2条重要公告在轮播中
          announcements.value = allAnnouncementData.filter(a => a.isImportant).slice(0, 2)
          allAnnouncements.value = allAnnouncementData
        }
      } catch (error) {
        console.error('加载公告失败:', error)
      }
    }

    const viewAnnouncement = (announcement) => {
      selectedAnnouncement.value = announcement
      announcementDialogVisible.value = true
      allAnnouncementsVisible.value = false
    }

    const showAllAnnouncements = () => {
      allAnnouncementsVisible.value = true
    }

    const closeAnnouncement = () => {
      bannerVisible.value = false
    }

    const formatTime = (time) => {
      const now = new Date()
      const diff = now - time
      const hours = Math.floor(diff / (1000 * 60 * 60))
      const days = Math.floor(diff / (1000 * 60 * 60 * 24))
      
      if (hours < 1) return '刚刚发布'
      if (hours < 24) return `${hours}小时前`
      if (days < 7) return `${days}天前`
      return formatDate(time)
    }

    const formatDate = (time) => {
      return new Date(time).toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
      })
    }

    onMounted(() => {
      loadAnnouncements()
    })

    return {
      announcements,
      allAnnouncements,
      announcementDialogVisible,
      allAnnouncementsVisible,
      selectedAnnouncement,
      bannerVisible,
      viewAnnouncement,
      showAllAnnouncements,
      closeAnnouncement,
      formatTime,
      formatDate
    }
  }
}
</script>

<style scoped>
.announcement-banner {
  background: linear-gradient(135deg, #d4af37 0%, #ffd700 100%);
  color: #2c2c2c;
  padding: 12px 0;
  box-shadow: 0 2px 8px rgba(212, 175, 55, 0.3);
  position: relative;
  z-index: 50;
}

.announcement-content {
  display: flex;
  align-items: center;
  gap: var(--harmony-space-md);
}

.announcement-icon {
  font-size: 1.2rem;
  color: #2c2c2c;
  animation: bell-shake 2s ease-in-out infinite;
}

@keyframes bell-shake {
  0%, 50%, 100% { transform: rotate(0deg); }
  10%, 30% { transform: rotate(-10deg); }
  20%, 40% { transform: rotate(10deg); }
}

.announcement-carousel {
  flex: 1;
  min-height: 40px;
}

.announcement-slider {
  height: 40px !important;
}

.announcement-item {
  display: flex;
  align-items: center;
  gap: var(--harmony-space-md);
  cursor: pointer;
  padding: 8px 0;
  transition: all var(--harmony-transition-base);
}

.announcement-item:hover {
  opacity: 0.8;
}

.announcement-badge {
  background: #e74c3c;
  color: white;
  padding: 2px 8px;
  border-radius: var(--harmony-radius-full);
  font-size: 12px;
  font-weight: 600;
}

.announcement-title {
  flex: 1;
  font-weight: 500;
  font-size: 14px;
}

.announcement-time {
  font-size: 12px;
  opacity: 0.8;
}

.announcement-actions {
  display: flex;
  align-items: center;
  gap: var(--harmony-space-sm);
}

.announcement-actions .el-button {
  color: #2c2c2c;
  font-weight: 500;
}

.close-btn {
  opacity: 0.7;
}

.close-btn:hover {
  opacity: 1;
}

/* 公告详情样式 */
.announcement-detail {
  padding: var(--harmony-space-md) 0;
}

.announcement-meta {
  display: flex;
  align-items: center;
  gap: var(--harmony-space-md);
  margin-bottom: var(--harmony-space-lg);
  padding-bottom: var(--harmony-space-md);
  border-bottom: 1px solid var(--harmony-border);
}

.announcement-date {
  color: var(--harmony-text-secondary);
  font-size: 14px;
}

.announcement-body {
  line-height: 1.6;
  color: var(--harmony-text-primary);
}

/* 公告列表样式 */
.announcements-list {
  max-height: 500px;
  overflow-y: auto;
}

.announcement-list-item {
  padding: var(--harmony-space-lg);
  border-bottom: 1px solid var(--harmony-border);
  cursor: pointer;
  transition: all var(--harmony-transition-base);
}

.announcement-list-item:hover {
  background: var(--harmony-bg-secondary);
}

.announcement-list-item:last-child {
  border-bottom: none;
}

.list-item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--harmony-space-sm);
}

.list-item-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--harmony-text-primary);
  margin: 0;
  display: flex;
  align-items: center;
  gap: var(--harmony-space-sm);
}

.list-item-time {
  color: var(--harmony-text-tertiary);
  font-size: 12px;
  white-space: nowrap;
}

.list-item-summary {
  color: var(--harmony-text-secondary);
  font-size: 14px;
  line-height: 1.5;
  margin: 0;
}

.empty-announcements {
  padding: var(--harmony-space-2xl) 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .announcement-content {
    gap: var(--harmony-space-sm);
  }
  
  .announcement-title {
    font-size: 13px;
  }
  
  .announcement-time {
    display: none;
  }
  
  .list-item-header {
    flex-direction: column;
    gap: var(--harmony-space-xs);
  }
  
  .list-item-time {
    align-self: flex-start;
  }
}
</style>
