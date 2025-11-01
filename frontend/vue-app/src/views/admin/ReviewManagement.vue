<template>
  <div class="review-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>评价管理</span>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索评价内容..."
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

      <el-table v-loading="loading" :data="reviewList" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userName" label="用户" width="120" />
        <el-table-column prop="shopName" label="门店" width="150" />
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="rating" label="评分" width="120">
          <template #default="scope">
            <el-rate :model-value="scope.row.rating" disabled show-score />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" min-width="200">
          <template #default="scope">
            <el-text line-clamp="2">{{ scope.row.content || '-' }}</el-text>
          </template>
        </el-table-column>
        <el-table-column prop="reply" label="回复内容" min-width="200">
          <template #default="scope">
            <el-text line-clamp="2">{{ scope.row.reply || '-' }}</el-text>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评价时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
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
          @size-change="loadReviewList"
          @current-change="loadReviewList"
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
  name: 'ReviewManagement',
  components: {
    Search
  },
  setup() {
    const loading = ref(false)
    const searchKeyword = ref('')
    const reviewList = ref([])
    const pagination = ref({
      current: 1,
      size: 10,
      total: 0
    })

    const loadReviewList = async () => {
      loading.value = true
      try {
        const response = await adminAPI.getReviewList({
          current: pagination.value.current,
          size: pagination.value.size,
          keyword: searchKeyword.value
        })
        if (response.code === 200 && response.data) {
          reviewList.value = response.data.records || []
          pagination.value.total = response.data.total || 0
        }
      } catch (error) {
        console.error('加载评价列表失败:', error)
        ElMessage.error('加载评价列表失败')
      } finally {
        loading.value = false
      }
    }

    const handleSearch = () => {
      pagination.value.current = 1
      loadReviewList()
    }

    const handleDelete = async (review) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除这条评价吗？此操作不可恢复！`,
          '警告',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'error'
          }
        )

        await adminAPI.deleteReview(review.id)
        ElMessage.success('删除成功')
        loadReviewList()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败')
        }
      }
    }

    onMounted(() => {
      loadReviewList()
    })

    return {
      loading,
      searchKeyword,
      reviewList,
      pagination,
      handleSearch,
      handleDelete
    }
  }
}
</script>

<style scoped>
.review-management {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
