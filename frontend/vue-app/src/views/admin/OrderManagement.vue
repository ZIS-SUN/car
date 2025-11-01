<template>
  <div class="order-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单管理</span>
          <div class="header-actions">
            <el-select v-model="statusFilter" placeholder="订单状态" style="width: 150px; margin-right: 10px" @change="handleSearch">
              <el-option label="全部" :value="null" />
              <el-option label="待支付" :value="1" />
              <el-option label="已支付" :value="2" />
              <el-option label="已完成" :value="3" />
              <el-option label="已取消" :value="4" />
            </el-select>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索订单号/用户..."
              style="width: 300px"
              clearable
              @input="handleSearch"
            >
              <template #prefix>
                <el-icon><search /></el-icon>
              </template>
            </el-input>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="orderList" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="userName" label="用户" width="120" />
        <el-table-column prop="shopName" label="门店" width="150" />
        <el-table-column prop="serviceItems" label="服务项目" min-width="150" />
        <el-table-column prop="appointmentDate" label="预约日期" width="120" />
        <el-table-column prop="appointmentTime" label="预约时间" width="100" />
        <el-table-column prop="totalAmount" label="总金额" width="100">
          <template #default="scope">
            ¥{{ scope.row.totalAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="finalAmount" label="实付金额" width="100">
          <template #default="scope">
            ¥{{ scope.row.finalAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadOrderList"
          @current-change="loadOrderList"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { adminAPI } from '../../api/admin.js'

export default {
  name: 'OrderManagement',
  components: {
    Search
  },
  setup() {
    const loading = ref(false)
    const searchKeyword = ref('')
    const statusFilter = ref(null)
    const orderList = ref([])
    const pagination = ref({
      current: 1,
      size: 10,
      total: 0
    })

    const loadOrderList = async () => {
      loading.value = true
      try {
        const params = {
          current: pagination.value.current,
          size: pagination.value.size,
          keyword: searchKeyword.value
        }
        if (statusFilter.value !== null) {
          params.status = statusFilter.value
        }

        const response = await adminAPI.getOrderList(params)
        if (response.code === 200 && response.data) {
          orderList.value = response.data.records || []
          pagination.value.total = response.data.total || 0
        }
      } catch (error) {
        console.error('加载订单列表失败:', error)
        ElMessage.error('加载订单列表失败')
      } finally {
        loading.value = false
      }
    }

    const handleSearch = () => {
      pagination.value.current = 1
      loadOrderList()
    }

    const getStatusType = (status) => {
      const statusMap = {
        1: 'warning',  // 待支付
        2: 'primary',  // 已支付
        3: 'success',  // 已完成
        4: 'danger'    // 已取消
      }
      return statusMap[status] || 'info'
    }

    const getStatusText = (status) => {
      const statusMap = {
        1: '待支付',
        2: '已支付',
        3: '已完成',
        4: '已取消'
      }
      return statusMap[status] || '未知'
    }

    onMounted(() => {
      loadOrderList()
    })

    return {
      loading,
      searchKeyword,
      statusFilter,
      orderList,
      pagination,
      handleSearch,
      getStatusType,
      getStatusText
    }
  }
}
</script>

<style scoped>
.order-management {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
