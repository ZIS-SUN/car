<template>
  <div class="announcement-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">公告管理</h1>
        <p class="page-subtitle">发布和管理系统公告信息</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showAddDialog">
          <el-icon><plus /></el-icon>
          发布公告
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon primary">
          <el-icon><bell /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ announcementStats.total }}</div>
          <div class="stat-label">总公告数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon success">
          <div class="stat-icon success">
            <el-icon><circle-check /></el-icon>
          </div>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ announcementStats.published }}</div>
          <div class="stat-label">已发布</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon warning">
          <el-icon><clock /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ announcementStats.draft }}</div>
          <div class="stat-label">草稿</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon danger">
          <el-icon><warning /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ announcementStats.important }}</div>
          <div class="stat-label">重要公告</div>
        </div>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="search-card">
      <div class="search-section">
        <div class="search-bar">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索公告标题或内容"
            class="search-input"
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><search /></el-icon>
            </template>
          </el-input>
        </div>
        <div class="filters">
          <el-select v-model="statusFilter" placeholder="发布状态" @change="handleFilter">
            <el-option label="全部状态" value=""></el-option>
            <el-option label="已发布" value="1"></el-option>
            <el-option label="草稿" value="0"></el-option>
          </el-select>
          <el-select v-model="importantFilter" placeholder="重要程度" @change="handleFilter">
            <el-option label="全部公告" value=""></el-option>
            <el-option label="重要公告" value="1"></el-option>
            <el-option label="普通公告" value="0"></el-option>
          </el-select>
        </div>
      </div>
    </el-card>

    <!-- 公告表格 -->
    <el-card class="table-card">
      <el-table 
        :data="filteredAnnouncements" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
        stripe
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="公告信息" min-width="300">
          <template #default="{ row }">
            <div class="announcement-info">
              <div class="announcement-title">
                <el-tag v-if="row.isImportant" type="danger" size="small">重要</el-tag>
                {{ row.title }}
              </div>
              <div class="announcement-summary">{{ row.summary || row.content.substring(0, 50) + '...' }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览量" width="100" />
        <el-table-column label="创建时间" width="150">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="150">
          <template #default="{ row }">
            {{ row.publishTime ? formatDate(row.publishTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button size="small" text type="primary" @click="viewAnnouncement(row)">
                查看
              </el-button>
              <el-button size="small" text type="primary" @click="editAnnouncement(row)">
                编辑
              </el-button>
              <el-button 
                v-if="row.status === 0"
                size="small" 
                text 
                type="success"
                @click="publishAnnouncement(row)"
              >
                发布
              </el-button>
              <el-button 
                v-else
                size="small" 
                text 
                type="warning"
                @click="unpublishAnnouncement(row)"
              >
                撤回
              </el-button>
              <el-button size="small" text type="danger" @click="deleteAnnouncement(row)">
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalAnnouncements"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑公告对话框 -->
    <el-dialog
      v-model="announcementDialogVisible"
      :title="isEditMode ? '编辑公告' : '发布公告'"
      width="800px"
      class="announcement-dialog"
    >
      <el-form :model="announcementForm" :rules="announcementRules" ref="announcementFormRef" label-width="80px">
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="announcementForm.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="公告摘要" prop="summary">
          <el-input 
            v-model="announcementForm.summary" 
            type="textarea" 
            :rows="2"
            placeholder="请输入公告摘要（可选）" 
          />
        </el-form-item>
        <el-form-item label="公告内容" prop="content">
          <el-input 
            v-model="announcementForm.content" 
            type="textarea" 
            :rows="6"
            placeholder="请输入公告详细内容" 
          />
        </el-form-item>
        <el-form-item label="重要程度">
          <el-switch
            v-model="announcementForm.isImportant"
            active-text="重要公告"
            inactive-text="普通公告"
          />
        </el-form-item>
        <el-form-item label="发布状态">
          <el-radio-group v-model="announcementForm.status">
            <el-radio :label="0">保存为草稿</el-radio>
            <el-radio :label="1">立即发布</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="announcementDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveAnnouncement" :loading="saving">
            {{ isEditMode ? '更新' : '发布' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 公告详情对话框 -->
    <el-dialog
      v-model="announcementDetailVisible"
      title="公告详情"
      width="700px"
      class="announcement-detail-dialog"
    >
      <div v-if="selectedAnnouncement" class="announcement-detail">
        <div class="detail-header">
          <h3 class="detail-title">
            <el-tag v-if="selectedAnnouncement.isImportant" type="danger" size="small">重要</el-tag>
            {{ selectedAnnouncement.title }}
          </h3>
          <div class="detail-meta">
            <span class="meta-item">
              <el-icon><user /></el-icon>
              创建者：{{ selectedAnnouncement.creatorName || '管理员' }}
            </span>
            <span class="meta-item">
              <el-icon><calendar /></el-icon>
              创建时间：{{ formatDate(selectedAnnouncement.createTime) }}
            </span>
            <span class="meta-item" v-if="selectedAnnouncement.publishTime">
              <el-icon><clock /></el-icon>
              发布时间：{{ formatDate(selectedAnnouncement.publishTime) }}
            </span>
            <span class="meta-item">
              <el-icon><view /></el-icon>
              浏览量：{{ selectedAnnouncement.viewCount || 0 }}
            </span>
          </div>
        </div>
        <div class="detail-content">
          <div v-if="selectedAnnouncement.summary" class="detail-summary">
            <h4>摘要</h4>
            <p>{{ selectedAnnouncement.summary }}</p>
          </div>
          <div class="detail-body">
            <h4>详细内容</h4>
            <div class="content-text">{{ selectedAnnouncement.content }}</div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { 
  Plus, 
  Search, 
  Bell, 
  CircleCheck, 
  Clock, 
  Warning,
  User,
  Calendar,
  View
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminAPI } from '../../api/admin.js'

export default {
  name: 'AnnouncementManagement',
  components: {
    Plus,
    Search,
    Bell,
    CircleCheck,
    Clock,
    Warning,
    User,
    Calendar,
    View
  },
  setup() {
    const loading = ref(false)
    const saving = ref(false)
    const searchKeyword = ref('')
    const statusFilter = ref('')
    const importantFilter = ref('')
    const announcementDialogVisible = ref(false)
    const announcementDetailVisible = ref(false)
    const isEditMode = ref(false)
    const selectedAnnouncement = ref(null)
    const selectedAnnouncements = ref([])
    
    // 分页
    const currentPage = ref(1)
    const pageSize = ref(20)
    const totalAnnouncements = ref(0)
    
    // 统计数据
    const announcementStats = ref({
      total: 0,
      published: 0,
      draft: 0,
      important: 0
    })
    
    // 公告数据
    const announcements = ref([])
    
    // 公告表单
    const announcementForm = ref({
      title: '',
      summary: '',
      content: '',
      isImportant: false,
      status: 1
    })
    
    const announcementFormRef = ref()
    
    const announcementRules = {
      title: [
        { required: true, message: '请输入公告标题', trigger: 'blur' },
        { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' }
      ],
      content: [
        { required: true, message: '请输入公告内容', trigger: 'blur' },
        { min: 10, max: 2000, message: '内容长度在 10 到 2000 个字符', trigger: 'blur' }
      ]
    }
    
    // 筛选后的公告列表
    const filteredAnnouncements = computed(() => {
      let result = announcements.value
      
      if (searchKeyword.value) {
        result = result.filter(announcement => 
          announcement.title.includes(searchKeyword.value) ||
          announcement.content.includes(searchKeyword.value)
        )
      }
      
      if (statusFilter.value !== '') {
        result = result.filter(announcement => announcement.status === parseInt(statusFilter.value))
      }
      
      if (importantFilter.value !== '') {
        result = result.filter(announcement => announcement.isImportant === Boolean(parseInt(importantFilter.value)))
      }
      
      totalAnnouncements.value = result.length
      
      // 分页
      const start = (currentPage.value - 1) * pageSize.value
      const end = start + pageSize.value
      return result.slice(start, end)
    })
    
    const loadAnnouncements = async () => {
      loading.value = true
      try {
        const response = await adminAPI.getAnnouncementList({
          current: currentPage.value,
          size: pageSize.value
        })

        if (response.code === 200 && response.data) {
          announcements.value = response.data.records || response.data || []

          // 更新统计数据
          announcementStats.value = {
            total: announcements.value.length,
            published: announcements.value.filter(a => a.status === 1).length,
            draft: announcements.value.filter(a => a.status === 0).length,
            important: announcements.value.filter(a => a.isImportant).length
          }
        }
      } catch (error) {
        console.error('加载公告列表失败:', error)
        ElMessage.error('加载公告列表失败')
      } finally {
        loading.value = false
      }
    }
    
    const showAddDialog = () => {
      isEditMode.value = false
      announcementForm.value = {
        title: '',
        summary: '',
        content: '',
        isImportant: false,
        status: 1
      }
      announcementDialogVisible.value = true
    }
    
    const editAnnouncement = (announcement) => {
      isEditMode.value = true
      announcementForm.value = { ...announcement }
      announcementDialogVisible.value = true
    }
    
    const viewAnnouncement = (announcement) => {
      selectedAnnouncement.value = announcement
      announcementDetailVisible.value = true
    }
    
    const saveAnnouncement = async () => {
      if (!announcementFormRef.value) return

      try {
        await announcementFormRef.value.validate()
        saving.value = true

        if (isEditMode.value) {
          await adminAPI.updateAnnouncement(announcementForm.value.id, announcementForm.value)
          ElMessage.success('公告更新成功')
        } else {
          await adminAPI.createAnnouncement(announcementForm.value)
          ElMessage.success('公告创建成功')
        }

        announcementDialogVisible.value = false
        loadAnnouncements()
      } catch (error) {
        console.error('保存公告失败:', error)
        ElMessage.error(isEditMode.value ? '更新失败' : '创建失败')
      } finally {
        saving.value = false
      }
    }
    
    const publishAnnouncement = async (announcement) => {
      try {
        await ElMessageBox.confirm(
          `确定要发布公告"${announcement.title}"吗？`,
          '确认发布',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )

        await adminAPI.publishAnnouncement(announcement.id)
        ElMessage.success('公告发布成功')
        loadAnnouncements()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('发布失败')
        }
      }
    }

    const unpublishAnnouncement = async (announcement) => {
      try {
        await ElMessageBox.confirm(
          `确定要撤回公告"${announcement.title}"吗？`,
          '确认撤回',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )

        await adminAPI.offlineAnnouncement(announcement.id)
        ElMessage.success('公告撤回成功')
        loadAnnouncements()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('撤回失败')
        }
      }
    }

    const deleteAnnouncement = async (announcement) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除公告"${announcement.title}"吗？此操作不可恢复！`,
          '确认删除',
          {
            confirmButtonText: '删除',
            cancelButtonText: '取消',
            type: 'error'
          }
        )

        await adminAPI.deleteAnnouncement(announcement.id)
        ElMessage.success('公告删除成功')
        loadAnnouncements()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败')
        }
      }
    }
    
    const handleSearch = () => {
      currentPage.value = 1
    }
    
    const handleFilter = () => {
      currentPage.value = 1
    }
    
    const handleSelectionChange = (selection) => {
      selectedAnnouncements.value = selection
    }
    
    const handleSizeChange = (size) => {
      pageSize.value = size
      currentPage.value = 1
    }
    
    const handleCurrentChange = (page) => {
      currentPage.value = page
    }
    
    const formatDate = (date) => {
      if (!date) return '-'
      return new Date(date).toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
    
    onMounted(() => {
      loadAnnouncements()
    })
    
    return {
      loading,
      saving,
      searchKeyword,
      statusFilter,
      importantFilter,
      announcementDialogVisible,
      announcementDetailVisible,
      isEditMode,
      selectedAnnouncement,
      selectedAnnouncements,
      currentPage,
      pageSize,
      totalAnnouncements,
      announcementStats,
      filteredAnnouncements,
      announcementForm,
      announcementFormRef,
      announcementRules,
      showAddDialog,
      editAnnouncement,
      viewAnnouncement,
      saveAnnouncement,
      publishAnnouncement,
      unpublishAnnouncement,
      deleteAnnouncement,
      handleSearch,
      handleFilter,
      handleSelectionChange,
      handleSizeChange,
      handleCurrentChange,
      formatDate
    }
  }
}
</script>

<style scoped>
.announcement-management {
  padding: 24px;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--harmony-space-xl);
  padding: var(--harmony-space-lg);
  background: var(--harmony-bg-primary);
  border-radius: var(--harmony-radius-lg);
  box-shadow: var(--harmony-shadow-sm);
}

.header-content h1 {
  font-size: 1.8rem;
  font-weight: 700;
  color: var(--harmony-text-primary);
  margin: 0 0 var(--harmony-space-sm);
}

.page-subtitle {
  color: var(--harmony-text-secondary);
  margin: 0;
}

.header-actions {
  display: flex;
  gap: var(--harmony-space-md);
}

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--harmony-space-lg);
  margin-bottom: var(--harmony-space-xl);
}

.stat-card {
  background: var(--harmony-bg-primary);
  border-radius: var(--harmony-radius-lg);
  padding: var(--harmony-space-lg);
  box-shadow: var(--harmony-shadow-sm);
  border: 1px solid var(--harmony-border);
  display: flex;
  align-items: center;
  gap: var(--harmony-space-md);
  transition: all var(--harmony-transition-base);
}

.stat-card:hover {
  box-shadow: var(--harmony-shadow-md);
  transform: translateY(-2px);
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: var(--harmony-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: white;
}

.stat-icon.primary {
  background: var(--harmony-primary-gradient);
}

.stat-icon.success {
  background: var(--harmony-gradient-4);
}

.stat-icon.warning {
  background: var(--harmony-gradient-2);
}

.stat-icon.danger {
  background: var(--harmony-gradient-3);
}

.stat-value {
  font-size: 1.8rem;
  font-weight: 700;
  color: var(--harmony-text-primary);
  margin-bottom: var(--harmony-space-xs);
}

.stat-label {
  color: var(--harmony-text-secondary);
  font-size: 0.9rem;
}

/* 搜索区域 */
.search-card {
  margin-bottom: var(--harmony-space-xl);
}

.search-section {
  display: flex;
  gap: var(--harmony-space-lg);
  align-items: center;
}

.search-bar {
  flex: 1;
}

.search-input {
  max-width: 400px;
}

.filters {
  display: flex;
  gap: var(--harmony-space-md);
}

.filters .el-select {
  min-width: 150px;
}

/* 表格区域 */
.table-card {
  box-shadow: var(--harmony-shadow-sm);
}

.announcement-info {
  padding: var(--harmony-space-sm) 0;
}

.announcement-title {
  font-weight: 600;
  color: var(--harmony-text-primary);
  margin-bottom: var(--harmony-space-xs);
  display: flex;
  align-items: center;
  gap: var(--harmony-space-sm);
}

.announcement-summary {
  color: var(--harmony-text-secondary);
  font-size: 0.9rem;
  line-height: 1.4;
}

.table-actions {
  display: flex;
  gap: var(--harmony-space-sm);
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
  padding: 16px 0;
  border-top: 1px solid var(--harmony-border);
}

/* 对话框样式 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--harmony-space-md);
}

/* 公告详情样式 */
.announcement-detail {
  padding: var(--harmony-space-md) 0;
}

.detail-header {
  margin-bottom: var(--harmony-space-xl);
  padding-bottom: var(--harmony-space-lg);
  border-bottom: 1px solid var(--harmony-border);
}

.detail-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--harmony-text-primary);
  margin-bottom: var(--harmony-space-md);
  display: flex;
  align-items: center;
  gap: var(--harmony-space-sm);
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--harmony-space-lg);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: var(--harmony-space-xs);
  color: var(--harmony-text-secondary);
  font-size: 0.9rem;
}

.detail-content h4 {
  color: var(--harmony-text-primary);
  margin-bottom: var(--harmony-space-md);
}

.detail-summary {
  margin-bottom: var(--harmony-space-lg);
}

.detail-summary p {
  color: var(--harmony-text-secondary);
  line-height: 1.6;
}

.content-text {
  color: var(--harmony-text-primary);
  line-height: 1.6;
  white-space: pre-wrap;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .announcement-management {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    gap: var(--harmony-space-lg);
    align-items: stretch;
  }
  
  .search-section {
    flex-direction: column;
    gap: var(--harmony-space-md);
  }
  
  .filters {
    flex-direction: column;
  }
  
  .filters .el-select {
    width: 100%;
  }
  
  .detail-meta {
    flex-direction: column;
    gap: var(--harmony-space-sm);
  }
}
</style>