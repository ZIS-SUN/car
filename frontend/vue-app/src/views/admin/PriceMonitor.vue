<template>
  <div class="price-monitor">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card stat-card-blue">
          <div class="stat-content">
            <div class="stat-label">异常价格总数</div>
            <div class="stat-value">{{ stats.totalAbnormal || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-card-red">
          <div class="stat-content">
            <div class="stat-label">价格过高</div>
            <div class="stat-value">{{ stats.tooHighCount || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-card-orange">
          <div class="stat-content">
            <div class="stat-label">价格过低</div>
            <div class="stat-value">{{ stats.tooLowCount || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-card-green">
          <div class="stat-content">
            <div class="stat-label">平台指导价</div>
            <div class="stat-value">{{ guidePrices.length }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 平台指导价管理 -->
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <span>平台指导价管理</span>
          <el-button type="primary" @click="handleAddGuidePrice">
            <el-icon><plus /></el-icon>
            添加指导价
          </el-button>
        </div>
      </template>

      <el-table v-loading="guidePriceLoading" :data="guidePrices" stripe style="width: 100%">
        <el-table-column prop="serviceName" label="服务项目" width="180" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="guidePrice" label="参考价格" width="120">
          <template #default="scope">
            ¥{{ scope.row.guidePrice }}
          </template>
        </el-table-column>
        <el-table-column label="价格区间" width="200">
          <template #default="scope">
            ¥{{ scope.row.minPrice }} - ¥{{ scope.row.maxPrice }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="200" show-overflow-tooltip />
        <el-table-column prop="updateTime" label="更新时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEditGuidePrice(scope.row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDeleteGuidePrice(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 价格监控记录 -->
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <span>价格监控记录</span>
          <div class="filter-bar">
            <el-select v-model="filterStatus" placeholder="状态筛选" clearable @change="loadMonitorRecords">
              <el-option label="全部" value="" />
              <el-option label="正常" value="NORMAL" />
              <el-option label="过高" value="TOO_HIGH" />
              <el-option label="过低" value="TOO_LOW" />
            </el-select>
          </div>
        </div>
      </template>

      <el-table v-loading="recordLoading" :data="monitorRecords" stripe style="width: 100%">
        <el-table-column prop="shopId" label="门店ID" width="100" />
        <el-table-column prop="itemName" label="服务项目" width="150" />
        <el-table-column prop="shopPrice" label="门店价格" width="120">
          <template #default="scope">
            ¥{{ scope.row.shopPrice }}
          </template>
        </el-table-column>
        <el-table-column prop="guidePrice" label="指导价" width="120">
          <template #default="scope">
            ¥{{ scope.row.guidePrice }}
          </template>
        </el-table-column>
        <el-table-column prop="deviationRate" label="偏差率" width="120">
          <template #default="scope">
            <span :class="getDeviationClass(scope.row.deviationRate)">
              {{ scope.row.deviationRate }}%
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="recordTime" label="记录时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.recordTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="handleStatus" label="处理状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.handleStatus === 'PENDING' ? 'warning' : 'success'">
              {{ scope.row.handleStatus === 'PENDING' ? '待处理' : '已处理' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="recordPagination.current"
          v-model:page-size="recordPagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="recordPagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadMonitorRecords"
          @current-change="loadMonitorRecords"
        />
      </div>
    </el-card>

    <!-- 指导价编辑对话框 -->
    <el-dialog
      v-model="guidePriceDialogVisible"
      :title="isEdit ? '编辑指导价' : '添加指导价'"
      width="600px"
      @close="resetGuidePriceForm"
    >
      <el-form :model="guidePriceForm" :rules="guidePriceRules" ref="guidePriceFormRef" label-width="120px">
        <el-form-item label="服务项目" prop="serviceName">
          <el-input v-model="guidePriceForm.serviceName" placeholder="请输入服务项目名称" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="guidePriceForm.category" placeholder="请选择分类">
            <el-option label="保养" value="保养" />
            <el-option label="维修" value="维修" />
            <el-option label="美容" value="美容" />
            <el-option label="改装" value="改装" />
          </el-select>
        </el-form-item>
        <el-form-item label="参考价格" prop="guidePrice">
          <el-input-number v-model="guidePriceForm.guidePrice" :min="0" :precision="2" :step="10" />
          <span class="form-tip">元</span>
        </el-form-item>
        <el-form-item label="最低价格" prop="minPrice">
          <el-input-number v-model="guidePriceForm.minPrice" :min="0" :precision="2" :step="10" />
          <span class="form-tip">元</span>
        </el-form-item>
        <el-form-item label="最高价格" prop="maxPrice">
          <el-input-number v-model="guidePriceForm.maxPrice" :min="0" :precision="2" :step="10" />
          <span class="form-tip">元</span>
        </el-form-item>
        <el-form-item label="说明" prop="description">
          <el-input
            v-model="guidePriceForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入说明信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="guidePriceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitGuidePriceForm" :loading="submitLoading">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../api/index.js'

export default {
  name: 'PriceMonitor',
  components: {
    Plus
  },
  setup() {
    const guidePriceLoading = ref(false)
    const recordLoading = ref(false)
    const guidePrices = ref([])
    const monitorRecords = ref([])
    const stats = ref({})
    const filterStatus = ref('')
    
    const recordPagination = ref({
      current: 1,
      size: 10,
      total: 0
    })

    const guidePriceDialogVisible = ref(false)
    const isEdit = ref(false)
    const submitLoading = ref(false)
    const guidePriceFormRef = ref(null)
    
    const guidePriceForm = ref({
      serviceName: '',
      category: '',
      guidePrice: 0,
      minPrice: 0,
      maxPrice: 0,
      description: ''
    })

    const guidePriceRules = {
      serviceName: [{ required: true, message: '请输入服务项目名称', trigger: 'blur' }],
      category: [{ required: true, message: '请选择分类', trigger: 'change' }],
      guidePrice: [{ required: true, message: '请输入参考价格', trigger: 'blur' }],
      minPrice: [{ required: true, message: '请输入最低价格', trigger: 'blur' }],
      maxPrice: [{ required: true, message: '请输入最高价格', trigger: 'blur' }]
    }

    // 加载平台指导价
    const loadGuidePrices = async () => {
      guidePriceLoading.value = true
      try {
        const response = await request.get('/admin/price-monitor/guide-prices')
        if (response.code === 200) {
          guidePrices.value = response.data || []
        }
      } catch (error) {
        console.error('加载指导价失败:', error)
        ElMessage.error('加载指导价失败')
      } finally {
        guidePriceLoading.value = false
      }
    }

    // 加载监控记录
    const loadMonitorRecords = async () => {
      recordLoading.value = true
      try {
        const response = await request.get('/admin/price-monitor/records', {
          params: {
            current: recordPagination.value.current,
            size: recordPagination.value.size,
            status: filterStatus.value || undefined
          }
        })
        if (response.code === 200 && response.data) {
          monitorRecords.value = response.data.records || []
          recordPagination.value.total = response.data.total || 0
        }
      } catch (error) {
        console.error('加载监控记录失败:', error)
        ElMessage.error('加载监控记录失败')
      } finally {
        recordLoading.value = false
      }
    }

    // 加载统计数据
    const loadStats = async () => {
      try {
        const response = await request.get('/admin/price-monitor/abnormal-stats')
        if (response.code === 200) {
          stats.value = response.data || {}
        }
      } catch (error) {
        console.error('加载统计数据失败:', error)
      }
    }

    // 添加指导价
    const handleAddGuidePrice = () => {
      isEdit.value = false
      resetGuidePriceForm()
      guidePriceDialogVisible.value = true
    }

    // 编辑指导价
    const handleEditGuidePrice = (row) => {
      isEdit.value = true
      guidePriceForm.value = { ...row }
      guidePriceDialogVisible.value = true
    }

    // 删除指导价
    const handleDeleteGuidePrice = async (row) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除"${row.serviceName}"的指导价吗？`,
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )

        const response = await request.delete(`/admin/price-monitor/guide-prices/${row.id}`)
        if (response.code === 200) {
          ElMessage.success('删除成功')
          loadGuidePrices()
        } else {
          ElMessage.error(response.message || '删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败')
        }
      }
    }

    // 提交指导价表单
    const submitGuidePriceForm = async () => {
      if (!guidePriceFormRef.value) return

      await guidePriceFormRef.value.validate(async (valid) => {
        if (!valid) return

        submitLoading.value = true
        try {
          let response
          if (isEdit.value) {
            response = await request.put(
              `/admin/price-monitor/guide-prices/${guidePriceForm.value.id}`,
              guidePriceForm.value
            )
          } else {
            response = await request.post('/admin/price-monitor/guide-prices', guidePriceForm.value)
          }

          if (response.code === 200) {
            ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
            guidePriceDialogVisible.value = false
            loadGuidePrices()
          } else {
            ElMessage.error(response.message || '操作失败')
          }
        } catch (error) {
          console.error('提交失败:', error)
          ElMessage.error('操作失败')
        } finally {
          submitLoading.value = false
        }
      })
    }

    // 重置表单
    const resetGuidePriceForm = () => {
      guidePriceForm.value = {
        serviceName: '',
        category: '',
        guidePrice: 0,
        minPrice: 0,
        maxPrice: 0,
        description: ''
      }
      if (guidePriceFormRef.value) {
        guidePriceFormRef.value.resetFields()
      }
    }

    // 格式化日期时间
    const formatDateTime = (datetime) => {
      if (!datetime) return '-'
      return datetime.replace('T', ' ').substring(0, 19)
    }

    // 获取状态类型
    const getStatusType = (status) => {
      const typeMap = {
        'NORMAL': 'success',
        'TOO_HIGH': 'danger',
        'TOO_LOW': 'warning'
      }
      return typeMap[status] || 'info'
    }

    // 获取状态文本
    const getStatusText = (status) => {
      const textMap = {
        'NORMAL': '正常',
        'TOO_HIGH': '过高',
        'TOO_LOW': '过低'
      }
      return textMap[status] || '未知'
    }

    // 获取偏差率样式
    const getDeviationClass = (rate) => {
      if (!rate) return ''
      const absRate = Math.abs(rate)
      if (absRate > 30) return 'deviation-high'
      if (absRate > 10) return 'deviation-medium'
      return 'deviation-low'
    }

    onMounted(() => {
      loadGuidePrices()
      loadMonitorRecords()
      loadStats()
    })

    return {
      guidePriceLoading,
      recordLoading,
      guidePrices,
      monitorRecords,
      stats,
      filterStatus,
      recordPagination,
      guidePriceDialogVisible,
      isEdit,
      submitLoading,
      guidePriceFormRef,
      guidePriceForm,
      guidePriceRules,
      handleAddGuidePrice,
      handleEditGuidePrice,
      handleDeleteGuidePrice,
      submitGuidePriceForm,
      resetGuidePriceForm,
      loadMonitorRecords,
      formatDateTime,
      getStatusType,
      getStatusText,
      getDeviationClass
    }
  }
}
</script>

<style scoped>
.price-monitor {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stats-row {
  margin-bottom: 0;
}

.stat-card {
  color: white;
  border: none;
}

.stat-card-blue {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-card-red {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-card-orange {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stat-card-green {
  background: linear-gradient(135deg, #30cfd0 0%, #330867 100%);
}

.stat-card :deep(.el-card__body) {
  padding: 20px;
}

.stat-content {
  text-align: center;
}

.stat-label {
  font-size: 14px;
  margin-bottom: 8px;
  opacity: 0.9;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
}

.main-card {
  flex: 1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-bar {
  display: flex;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.form-tip {
  margin-left: 10px;
  color: #999;
  font-size: 12px;
}

.deviation-high {
  color: #f56c6c;
  font-weight: bold;
}

.deviation-medium {
  color: #e6a23c;
  font-weight: bold;
}

.deviation-low {
  color: #67c23a;
}
</style>

