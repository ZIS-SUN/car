<template>
  <div class="data-statistics">
    <!-- 报表类型选择 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="报表类型">
          <el-select v-model="filterForm.reportType" @change="loadReportData">
            <el-option label="日报" value="daily" />
            <el-option label="周报" value="weekly" />
            <el-option label="月报" value="monthly" />
          </el-select>
        </el-form-item>
        
        <el-form-item v-if="filterForm.reportType === 'daily'" label="日期">
          <el-date-picker
            v-model="filterForm.date"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            @change="loadReportData"
          />
        </el-form-item>

        <el-form-item v-if="filterForm.reportType === 'weekly'" label="开始日期">
          <el-date-picker
            v-model="filterForm.startDate"
            type="date"
            placeholder="选择开始日期"
            value-format="YYYY-MM-DD"
            @change="loadReportData"
          />
        </el-form-item>

        <el-form-item v-if="filterForm.reportType === 'monthly'" label="年月">
          <el-date-picker
            v-model="filterForm.yearMonth"
            type="month"
            placeholder="选择年月"
            value-format="YYYY-MM"
            @change="loadReportData"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="loadReportData">
            <el-icon><refresh /></el-icon>
            刷新数据
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row" v-loading="loading">
      <el-col :span="6">
        <el-card class="stat-card stat-card-blue">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="36"><document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">总订单数</div>
              <div class="stat-value">{{ reportData.totalOrders || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-card-green">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="36"><money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">总营业额</div>
              <div class="stat-value">¥{{ reportData.totalRevenue || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-card-orange">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="36"><user /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">新增用户</div>
              <div class="stat-value">{{ reportData.newUsers || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-card-purple">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="36"><shop /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">活跃门店</div>
              <div class="stat-value">{{ reportData.activeShops || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细数据表格 -->
    <el-card class="data-card">
      <template #header>
        <div class="card-header">
          <span>详细数据</span>
          <el-button type="primary" size="small" @click="exportReport">
            <el-icon><download /></el-icon>
            导出报表
          </el-button>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="报表类型">
          {{ reportTypeText }}
        </el-descriptions-item>
        <el-descriptions-item label="统计周期">
          {{ getStatsPeriod() }}
        </el-descriptions-item>
        <el-descriptions-item label="总订单数">
          {{ reportData.totalOrders || 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="已完成订单">
          {{ reportData.completedOrders || 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="总营业额">
          ¥{{ reportData.totalRevenue || 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="平均订单金额">
          ¥{{ reportData.avgOrderAmount || 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="新增用户">
          {{ reportData.newUsers || 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="活跃门店">
          {{ reportData.activeShops || reportData.newShops || 0 }}
        </el-descriptions-item>
        <el-descriptions-item v-if="reportData.dailyAvgRevenue" label="日均营业额">
          ¥{{ reportData.dailyAvgRevenue || 0 }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 周报趋势图 -->
      <div v-if="filterForm.reportType === 'weekly' && reportData.dailyOrderTrend" class="trend-section">
        <h3>订单趋势（每日）</h3>
        <el-table :data="getTrendTableData()" stripe style="width: 100%">
          <el-table-column prop="date" label="日期" width="150" />
          <el-table-column prop="orders" label="订单数" width="120">
            <template #default="scope">
              <el-tag>{{ scope.row.orders }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="趋势">
            <template #default="scope">
              <el-progress
                :percentage="scope.row.percentage"
                :color="scope.row.orders > 5 ? '#67c23a' : '#e6a23c'"
              />
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 月报门店排行 -->
      <div v-if="filterForm.reportType === 'monthly' && reportData.topShops" class="ranking-section">
        <h3>门店排行榜</h3>
        <el-table :data="reportData.topShops" stripe style="width: 100%">
          <el-table-column type="index" label="排名" width="80" />
          <el-table-column prop="shopName" label="门店名称" width="200" />
          <el-table-column prop="orderCount" label="订单数" width="120" />
          <el-table-column prop="revenue" label="营业额" width="150">
            <template #default="scope">
              ¥{{ scope.row.revenue }}
            </template>
          </el-table-column>
          <el-table-column prop="avgRating" label="平均评分" width="120">
            <template #default="scope">
              <el-rate :model-value="scope.row.avgRating || 5" disabled />
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, computed } from 'vue'
import { Document, Money, User, Shop, Download, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../../api/index.js'

export default {
  name: 'DataStatistics',
  components: {
    Document,
    Money,
    User,
    Shop,
    Download,
    Refresh
  },
  setup() {
    const loading = ref(false)
    const reportData = ref({})
    
    const filterForm = reactive({
      reportType: 'daily',
      date: new Date().toISOString().slice(0, 10),
      startDate: null,
      yearMonth: new Date().toISOString().slice(0, 7)
    })

    const reportTypeText = computed(() => {
      const typeMap = {
        'daily': '日报',
        'weekly': '周报',
        'monthly': '月报'
      }
      return typeMap[filterForm.reportType] || '日报'
    })

    // 加载报表数据
    const loadReportData = async () => {
      loading.value = true
      try {
        let url = ''
        let params = {}

        if (filterForm.reportType === 'daily') {
          url = '/admin/reports/daily'
          params.date = filterForm.date
        } else if (filterForm.reportType === 'weekly') {
          url = '/admin/reports/weekly'
          params.startDate = filterForm.startDate || getMonday()
        } else if (filterForm.reportType === 'monthly') {
          url = '/admin/reports/monthly'
          const [year, month] = filterForm.yearMonth.split('-')
          params.year = parseInt(year)
          params.month = parseInt(month)
        }

        const response = await request.get(url, { params })
        if (response.code === 200) {
          reportData.value = response.data || {}
        } else {
          ElMessage.error(response.message || '加载失败')
        }
      } catch (error) {
        console.error('加载报表数据失败:', error)
        ElMessage.error('加载报表数据失败')
      } finally {
        loading.value = false
      }
    }

    // 获取本周一的日期
    const getMonday = () => {
      const now = new Date()
      const day = now.getDay()
      const diff = now.getDate() - day + (day === 0 ? -6 : 1)
      const monday = new Date(now.setDate(diff))
      return monday.toISOString().slice(0, 10)
    }

    // 获取统计周期文本
    const getStatsPeriod = () => {
      if (filterForm.reportType === 'daily') {
        return filterForm.date
      } else if (filterForm.reportType === 'weekly') {
        return `${reportData.value.startDate || ''} 至 ${reportData.value.endDate || ''}`
      } else if (filterForm.reportType === 'monthly') {
        return `${reportData.value.year || ''}年${reportData.value.month || ''}月`
      }
      return '-'
    }

    // 获取趋势表格数据
    const getTrendTableData = () => {
      if (!reportData.value.dailyOrderTrend) return []
      
      const trend = reportData.value.dailyOrderTrend
      const maxOrders = Math.max(...Object.values(trend), 1)
      
      return Object.entries(trend).map(([date, orders]) => ({
        date,
        orders,
        percentage: Math.round((orders / maxOrders) * 100)
      })).sort((a, b) => a.date.localeCompare(b.date))
    }

    // 导出报表
    const exportReport = () => {
      const jsonData = JSON.stringify(reportData.value, null, 2)
      const blob = new Blob([jsonData], { type: 'application/json' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `${reportTypeText.value}_${new Date().toISOString().slice(0, 10)}.json`
      link.click()
      window.URL.revokeObjectURL(url)
      ElMessage.success('导出成功')
    }

    // 初始化加载数据
    loadReportData()

    return {
      loading,
      reportData,
      filterForm,
      reportTypeText,
      loadReportData,
      getStatsPeriod,
      getTrendTableData,
      exportReport
    }
  }
}
</script>

<style scoped>
.data-statistics {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.filter-card {
  margin-bottom: 0;
}

.filter-form {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stats-row {
  margin-bottom: 0;
}

.stat-card {
  height: 120px;
  color: white;
  border: none;
}

.stat-card-blue {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-card-green {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-card-orange {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stat-card-purple {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
}

.stat-card :deep(.el-card__body) {
  padding: 20px;
  height: 100%;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
  height: 100%;
}

.stat-icon {
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  margin-bottom: 8px;
  opacity: 0.9;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
}

.data-card {
  flex: 1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.trend-section,
.ranking-section {
  margin-top: 30px;
}

.trend-section h3,
.ranking-section h3 {
  margin-bottom: 15px;
  color: #333;
  font-size: 16px;
}
</style>





