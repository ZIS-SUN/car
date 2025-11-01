<template>
  <div class="data-export">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据导出</span>
        </div>
      </template>

      <el-row :gutter="20">
        <!-- 用户数据导出 -->
        <el-col :span="8">
          <el-card class="export-card" shadow="hover">
            <div class="export-icon">
              <el-icon :size="48" color="#409EFF"><user /></el-icon>
            </div>
            <h3>用户数据导出</h3>
            <p>导出系统中的用户信息，包括车主、门店等用户类型</p>
            
            <el-form :model="userExportForm" label-width="100px" class="export-form">
              <el-form-item label="用户类型">
                <el-select v-model="userExportForm.userType" placeholder="选择用户类型" clearable>
                  <el-option label="全部" :value="null" />
                  <el-option label="车主" :value="1" />
                  <el-option label="门店" :value="2" />
                  <el-option label="管理员" :value="3" />
                </el-select>
              </el-form-item>
            </el-form>

            <el-button
              type="primary"
              @click="exportUsers"
              :loading="exporting.users"
              style="width: 100%"
            >
              <el-icon><download /></el-icon>
              导出Excel
            </el-button>
          </el-card>
        </el-col>

        <!-- 订单数据导出 -->
        <el-col :span="8">
          <el-card class="export-card" shadow="hover">
            <div class="export-icon">
              <el-icon :size="48" color="#67C23A"><document /></el-icon>
            </div>
            <h3>订单数据导出</h3>
            <p>导出订单信息，支持按日期范围筛选</p>
            
            <el-form :model="orderExportForm" label-width="100px" class="export-form">
              <el-form-item label="开始日期">
                <el-date-picker
                  v-model="orderExportForm.startDate"
                  type="date"
                  placeholder="选择开始日期"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>
              <el-form-item label="结束日期">
                <el-date-picker
                  v-model="orderExportForm.endDate"
                  type="date"
                  placeholder="选择结束日期"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>
            </el-form>

            <el-button
              type="success"
              @click="exportOrders"
              :loading="exporting.orders"
              style="width: 100%"
            >
              <el-icon><download /></el-icon>
              导出Excel
            </el-button>
          </el-card>
        </el-col>

        <!-- 财务数据导出 -->
        <el-col :span="8">
          <el-card class="export-card" shadow="hover">
            <div class="export-icon">
              <el-icon :size="48" color="#E6A23C"><money /></el-icon>
            </div>
            <h3>财务数据导出</h3>
            <p>导出财务统计数据，按日期汇总</p>
            
            <el-form :model="financialExportForm" label-width="100px" class="export-form">
              <el-form-item label="开始日期">
                <el-date-picker
                  v-model="financialExportForm.startDate"
                  type="date"
                  placeholder="选择开始日期"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>
              <el-form-item label="结束日期">
                <el-date-picker
                  v-model="financialExportForm.endDate"
                  type="date"
                  placeholder="选择结束日期"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>
            </el-form>

            <el-button
              type="warning"
              @click="exportFinancial"
              :loading="exporting.financial"
              style="width: 100%"
            >
              <el-icon><download /></el-icon>
              导出Excel
            </el-button>
          </el-card>
        </el-col>
      </el-row>

      <!-- 导出历史记录 -->
      <el-divider />
      <h3>导出历史</h3>
      <el-table :data="exportHistory" stripe style="width: 100%">
        <el-table-column prop="type" label="导出类型" width="150" />
        <el-table-column prop="count" label="记录数" width="100" />
        <el-table-column prop="time" label="导出时间" width="180" />
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'success' ? 'success' : 'danger'">
              {{ scope.row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { User, Document, Money, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { adminAPI } from '../../api/admin.js'

export default {
  name: 'DataExport',
  components: {
    User,
    Document,
    Money,
    Download
  },
  setup() {
    const exporting = reactive({
      users: false,
      orders: false,
      financial: false
    })

    const userExportForm = reactive({
      userType: null
    })

    const orderExportForm = reactive({
      startDate: null,
      endDate: null
    })

    const financialExportForm = reactive({
      startDate: null,
      endDate: null
    })

    const exportHistory = ref([])

    // 导出用户数据
    const exportUsers = async () => {
      exporting.users = true
      try {
        const response = await adminAPI.exportUsers(userExportForm.userType)
        
        // 创建下载链接
        const blob = new Blob([response], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `用户数据_${getCurrentDateTime()}.xlsx`
        link.click()
        window.URL.revokeObjectURL(url)

        ElMessage.success('导出成功')
        addExportHistory('用户数据', '-')
      } catch (error) {
        console.error('导出失败:', error)
        ElMessage.error('导出失败')
      } finally {
        exporting.users = false
      }
    }

    // 导出订单数据
    const exportOrders = async () => {
      exporting.orders = true
      try {
        const params = {}
        if (orderExportForm.startDate) params.startDate = orderExportForm.startDate
        if (orderExportForm.endDate) params.endDate = orderExportForm.endDate

        const response = await adminAPI.exportOrders(params)
        
        const blob = new Blob([response], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `订单数据_${getCurrentDateTime()}.xlsx`
        link.click()
        window.URL.revokeObjectURL(url)

        ElMessage.success('导出成功')
        addExportHistory('订单数据', '-')
      } catch (error) {
        console.error('导出失败:', error)
        ElMessage.error('导出失败')
      } finally {
        exporting.orders = false
      }
    }

    // 导出财务数据
    const exportFinancial = async () => {
      exporting.financial = true
      try {
        const params = {}
        if (financialExportForm.startDate) params.startDate = financialExportForm.startDate
        if (financialExportForm.endDate) params.endDate = financialExportForm.endDate

        const response = await adminAPI.exportFinancialData(params)
        
        const blob = new Blob([response], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `财务数据_${getCurrentDateTime()}.xlsx`
        link.click()
        window.URL.revokeObjectURL(url)

        ElMessage.success('导出成功')
        addExportHistory('财务数据', '-')
      } catch (error) {
        console.error('导出失败:', error)
        ElMessage.error('导出失败')
      } finally {
        exporting.financial = false
      }
    }

    // 获取当前日期时间
    const getCurrentDateTime = () => {
      const now = new Date()
      return now.toISOString().slice(0, 19).replace('T', '_').replace(/:/g, '-')
    }

    // 添加导出历史
    const addExportHistory = (type, count) => {
      const username = localStorage.getItem('username') || '管理员'
      exportHistory.value.unshift({
        type,
        count,
        time: new Date().toLocaleString('zh-CN'),
        operator: username,
        status: 'success'
      })
      
      // 只保留最近10条
      if (exportHistory.value.length > 10) {
        exportHistory.value.length = 10
      }
    }

    return {
      exporting,
      userExportForm,
      orderExportForm,
      financialExportForm,
      exportHistory,
      exportUsers,
      exportOrders,
      exportFinancial
    }
  }
}
</script>

<style scoped>
.data-export {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.export-card {
  text-align: center;
  padding: 20px;
  transition: all 0.3s;
}

.export-card:hover {
  transform: translateY(-5px);
}

.export-icon {
  margin: 20px 0;
}

.export-card h3 {
  margin: 15px 0 10px;
  color: #333;
}

.export-card p {
  color: #666;
  font-size: 14px;
  margin-bottom: 20px;
  min-height: 40px;
}

.export-form {
  margin: 20px 0;
  text-align: left;
}

.export-form :deep(.el-form-item) {
  margin-bottom: 15px;
}

.export-form :deep(.el-form-item__label) {
  font-size: 12px;
}
</style>





