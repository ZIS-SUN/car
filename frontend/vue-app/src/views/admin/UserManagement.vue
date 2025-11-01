<template>
  <div class="user-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户列表</span>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索用户..."
            style="width: 300px"
            clearable
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><search /></el-icon>
            </template>
          </el-input>
        </div>
      </template>

      <el-table v-loading="loading" :data="userList" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="userType" label="用户类型" width="120">
          <template #default="scope">
            <el-tag :type="getUserTypeTag(scope.row.userType)">
              {{ getUserTypeText(scope.row.userType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 1"
              type="danger"
              size="small"
              @click="handleStatusChange(scope.row, 0)"
            >
              禁用
            </el-button>
            <el-button
              v-else
              type="success"
              size="small"
              @click="handleStatusChange(scope.row, 1)"
            >
              启用
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadUserList"
          @current-change="loadUserList"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminAPI } from '../../api/admin.js'

export default {
  name: 'UserManagement',
  components: {
    Search
  },
  setup() {
    const loading = ref(false)
    const searchKeyword = ref('')
    const userList = ref([])
    const pagination = ref({
      current: 1,
      size: 10,
      total: 0
    })

    const loadUserList = async () => {
      loading.value = true
      try {
        const response = await adminAPI.getUserList({
          current: pagination.value.current,
          size: pagination.value.size,
          keyword: searchKeyword.value
        })
        if (response.code === 200 && response.data) {
          userList.value = response.data.records || []
          pagination.value.total = response.data.total || 0
        }
      } catch (error) {
        console.error('加载用户列表失败:', error)
        ElMessage.error('加载用户列表失败')
      } finally {
        loading.value = false
      }
    }

    const handleSearch = () => {
      pagination.value.current = 1
      loadUserList()
    }

    const getUserTypeText = (userType) => {
      const typeMap = {
        1: '普通用户',
        2: '门店',
        3: '管理员'
      }
      return typeMap[userType] || '未知'
    }

    const getUserTypeTag = (userType) => {
      const tagMap = {
        1: '',
        2: 'warning',
        3: 'danger'
      }
      return tagMap[userType] || 'info'
    }

    const handleStatusChange = async (user, status) => {
      try {
        await ElMessageBox.confirm(
          `确定要${status === 1 ? '启用' : '禁用'}用户"${user.username}"吗？`,
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )

        await adminAPI.updateUserStatus(user.id, status)
        ElMessage.success(`${status === 1 ? '启用' : '禁用'}成功`)
        loadUserList()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error(`${status === 1 ? '启用' : '禁用'}失败`)
        }
      }
    }

    const handleDelete = async (user) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除用户"${user.username}"吗？此操作不可恢复！`,
          '警告',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'error'
          }
        )

        await adminAPI.deleteUser(user.id)
        ElMessage.success('删除成功')
        loadUserList()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败')
        }
      }
    }

    onMounted(() => {
      loadUserList()
    })

    return {
      loading,
      searchKeyword,
      userList,
      pagination,
      handleSearch,
      getUserTypeText,
      getUserTypeTag,
      handleStatusChange,
      handleDelete
    }
  }
}
</script>

<style scoped>
.user-management {
  height: 100%;
  padding: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
}

.card-header span {
  color: var(--harmony-text-primary);
}

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: center;
  padding: 16px 0;
  border-top: 1px solid var(--harmony-border);
}

/* 表格样式优化 */
.el-table {
  border-radius: var(--harmony-radius-lg);
  overflow: hidden;
  box-shadow: var(--harmony-shadow-sm);
}

.el-table .el-table__header-wrapper {
  background: var(--harmony-bg-tertiary);
}

.el-table .el-table__header-wrapper th {
  background: var(--harmony-bg-tertiary);
  color: var(--harmony-text-secondary);
  font-weight: 600;
  border-color: var(--harmony-border);
}

.el-table .el-table__cell {
  border-color: var(--harmony-border);
}

.el-table .el-table__row:hover {
  background: var(--harmony-primary-light) !important;
}

/* 卡片样式 */
.el-card {
  border-radius: var(--harmony-radius-lg);
  box-shadow: var(--harmony-shadow-sm);
  border: 1px solid var(--harmony-border);
}

.el-card:hover {
  box-shadow: var(--harmony-shadow-md);
}

/* 按钮样式 */
.el-button {
  border-radius: var(--harmony-radius-md);
  font-weight: 500;
}

.el-button--primary {
  background: var(--harmony-primary-gradient);
  border: none;
  box-shadow: 0 2px 8px rgba(44, 44, 44, 0.2);
}

.el-button--primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(44, 44, 44, 0.3);
}

/* 输入框样式 */
.el-input__wrapper {
  border-radius: var(--harmony-radius-md);
  box-shadow: var(--harmony-shadow-sm);
}

.el-input__wrapper:hover {
  box-shadow: var(--harmony-shadow-md);
}

/* 标签样式 */
.el-tag {
  border-radius: var(--harmony-radius-full);
  font-weight: 500;
}

/* 分页样式 */
.el-pagination {
  --el-pagination-button-color: var(--harmony-text-secondary);
  --el-pagination-hover-color: var(--harmony-primary);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-management {
    padding: 16px;
  }
  
  .card-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .el-table .el-table-column--selection {
    display: none;
  }
}
</style>
