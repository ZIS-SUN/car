<template>
  <div class="package-management">
    <div class="page-header">
      <h1>套餐管理</h1>
      <div class="header-actions">
        <el-button type="primary" @click="showAddPackageDialog">
          <el-icon><Plus /></el-icon>
          创建套餐
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="套餐名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入套餐名称"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 100px"
          >
            <el-option label="上架" value="ACTIVE" />
            <el-option label="下架" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 套餐列表 -->
    <el-card class="package-list-card" shadow="hover">
      <el-table
        v-loading="loading"
        :data="packages"
        style="width: 100%"
        stripe
      >
        <el-table-column prop="name" label="套餐名称" min-width="150" />
        <el-table-column prop="description" label="套餐描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="services" label="包含服务" min-width="250">
          <template #default="{ row }">
            <div class="package-services">
              <el-tag
                v-for="service in row.services"
                :key="service.id"
                size="small"
                class="service-tag"
                :type="getCategoryTagType(service.category)"
              >
                {{ service.name }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="originalPrice" label="原价" width="100">
          <template #default="{ row }">
            <span class="original-price">¥{{ row.originalPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="packagePrice" label="套餐价" width="100">
          <template #default="{ row }">
            <span class="package-price">¥{{ row.packagePrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="discount" label="折扣" width="80">
          <template #default="{ row }">
            <span class="discount">{{ calculateDiscount(row) }}折</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              active-value="ACTIVE"
              inactive-value="INACTIVE"
              @change="togglePackageStatus(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              link
              @click="editPackage(row)"
            >
              编辑
            </el-button>
            <el-button
              size="small"
              type="danger"
              link
              @click="deletePackage(row)"
            >
              删除
            </el-button>
            <el-button
              size="small"
              type="info"
              link
              @click="viewPackageDetail(row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑套餐对话框 -->
    <el-dialog
      v-model="packageDialogVisible"
      :title="isEdit ? '编辑套餐' : '创建套餐'"
      width="700px"
      destroy-on-close
    >
      <el-form
        ref="packageFormRef"
        :model="packageForm"
        :rules="packageRules"
        label-width="100px"
      >
        <el-form-item label="套餐名称" prop="name">
          <el-input
            v-model="packageForm.name"
            placeholder="请输入套餐名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="套餐描述" prop="description">
          <el-input
            v-model="packageForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入套餐描述"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="包含服务" prop="serviceIds">
          <el-select
            v-model="packageForm.serviceIds"
            multiple
            placeholder="请选择包含的服务"
            style="width: 100%"
            @change="onServiceChange"
          >
            <el-option
              v-for="service in availableServices"
              :key="service.id"
              :label="`${service.name} (¥${service.price})`"
              :value="service.id"
            />
          </el-select>
          <div class="selected-services">
            <div
              v-for="serviceId in packageForm.serviceIds"
              :key="serviceId"
              class="selected-service-item"
            >
              <span>{{ getServiceName(serviceId) }}</span>
              <span class="service-price">¥{{ getServicePrice(serviceId) }}</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="原价">
          <span class="calculated-price">¥{{ calculatedOriginalPrice }}</span>
        </el-form-item>
        <el-form-item label="套餐价格" prop="packagePrice">
          <el-input-number
            v-model="packageForm.packagePrice"
            :min="0"
            :max="9999"
            :precision="2"
            style="width: 200px"
          />
          <span style="margin-left: 10px; color: #666;">元</span>
        </el-form-item>
        <el-form-item label="预计节省">
          <span class="savings">¥{{ calculatedSavings }} ({{ calculateSavingsPercentage }}%)</span>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="packageForm.status">
            <el-radio label="ACTIVE">上架</el-radio>
            <el-radio label="INACTIVE">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="套餐图片" prop="image">
          <el-upload
            class="package-image-uploader"
            :action="uploadUrl"
            :show-file-list="false"
            :on-success="handleImageSuccess"
            :before-upload="beforeImageUpload"
          >
            <img v-if="packageForm.image" :src="packageForm.image" class="package-image" />
            <el-icon v-else class="package-image-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">建议尺寸：400x300px，支持jpg、png格式</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="packageDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitPackage">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 套餐详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="套餐详情"
      width="600px"
      destroy-on-close
    >
      <div v-if="selectedPackage" class="package-detail">
        <div class="detail-header">
          <h3>{{ selectedPackage.name }}</h3>
          <el-tag :type="selectedPackage.status === 'ACTIVE' ? 'success' : 'danger'" size="large">
            {{ selectedPackage.status === 'ACTIVE' ? '上架' : '下架' }}
          </el-tag>
        </div>

        <div class="detail-description">
          <p>{{ selectedPackage.description }}</p>
        </div>

        <div class="detail-services">
          <h4>包含服务项目：</h4>
          <div class="service-list">
            <div
              v-for="service in selectedPackage.services"
              :key="service.id"
              class="service-item"
            >
              <el-tag :type="getCategoryTagType(service.category)" size="small">
                {{ service.name }}
              </el-tag>
              <span class="service-price">¥{{ service.price }}</span>
            </div>
          </div>
        </div>

        <div class="detail-pricing">
          <div class="price-item">
            <label>原价：</label>
            <span class="original-price">¥{{ selectedPackage.originalPrice }}</span>
          </div>
          <div class="price-item">
            <label>套餐价：</label>
            <span class="package-price">¥{{ selectedPackage.packagePrice }}</span>
          </div>
          <div class="price-item">
            <label>折扣：</label>
            <span class="discount">{{ calculateDiscount(selectedPackage) }}折</span>
          </div>
          <div class="price-item">
            <label>节省：</label>
            <span class="savings">¥{{ selectedPackage.originalPrice - selectedPackage.packagePrice }}</span>
          </div>
        </div>

        <div v-if="selectedPackage.image" class="detail-image">
          <h4>套餐图片：</h4>
          <img :src="selectedPackage.image" alt="套餐图片" />
        </div>

        <div class="detail-footer">
          <div class="time-item">
            <label>创建时间：</label>
            <span>{{ formatDateTime(selectedPackage.createdAt) }}</span>
          </div>
          <div class="time-item">
            <label>更新时间：</label>
            <span>{{ formatDateTime(selectedPackage.updatedAt) }}</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { shopAPI } from '@/api/shop'

const loading = ref(false)
const submitLoading = ref(false)
const packages = ref([])
const allPackages = ref([])
const availableServices = ref([])
const selectedPackage = ref(null)
const packageDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const isEdit = ref(false)
const packageFormRef = ref()

const uploadUrl = '/api/shop/upload/package-image'

const searchForm = reactive({
  name: '',
  status: ''
})

const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

const packageForm = reactive({
  id: '',
  name: '',
  description: '',
  serviceIds: [],
  packagePrice: 0,
  status: 'ACTIVE',
  image: ''
})

const packageRules = {
  name: [
    { required: true, message: '请输入套餐名称', trigger: 'blur' },
    { min: 2, max: 50, message: '套餐名称长度在2到50个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入套餐描述', trigger: 'blur' },
    { min: 10, max: 200, message: '套餐描述长度在10到200个字符', trigger: 'blur' }
  ],
  serviceIds: [
    { required: true, type: 'array', min: 2, message: '请至少选择2个服务项目', trigger: 'change' }
  ],
  packagePrice: [
    { required: true, message: '请输入套餐价格', trigger: 'blur' }
  ]
}

const parseServiceItems = (items) => {
  if (!items) return []
  if (Array.isArray(items)) return items
  if (typeof items === 'string') {
    try {
      const parsed = JSON.parse(items)
      if (Array.isArray(parsed)) {
        return parsed
      }
    } catch (error) {
      // ignore
    }
  }
  return []
}

const transformPackage = (pkg) => {
  const services = parseServiceItems(pkg.items || pkg.serviceItems || [])
  const originalPrice = services.reduce((sum, item) => sum + Number(item.price || 0), 0)

  return {
    id: pkg.id,
    name: pkg.packageName || pkg.name || '',
    description: pkg.description || '',
    services,
    originalPrice: Number(originalPrice || pkg.originalPrice || 0).toFixed(2),
    packagePrice: Number(pkg.packagePrice || pkg.price || 0).toFixed(2),
    status: (pkg.status === 1 || pkg.status === 'ACTIVE') ? 'ACTIVE' : 'INACTIVE',
    image: pkg.coverUrl || pkg.imageUrl || '',
    createdAt: pkg.createTime,
    updatedAt: pkg.updateTime
  }
}

const applyFilters = () => {
  let filtered = [...allPackages.value]

  if (searchForm.name) {
    filtered = filtered.filter(pkg => pkg.name.includes(searchForm.name.trim()))
  }

  if (searchForm.status) {
    filtered = filtered.filter(pkg => pkg.status === searchForm.status)
  }

  pagination.total = filtered.length
  const start = (pagination.current - 1) * pagination.size
  packages.value = filtered.slice(start, start + pagination.size)
}

// 计算属性
const calculatedOriginalPrice = computed(() => {
  return packageForm.serviceIds.reduce((total, serviceId) => {
    return total + getServicePrice(serviceId)
  }, 0)
})

const calculatedSavings = computed(() => {
  return calculatedOriginalPrice.value - packageForm.packagePrice
})

const calculateSavingsPercentage = computed(() => {
  if (calculatedOriginalPrice.value === 0) return 0
  return Math.round((calculatedSavings.value / calculatedOriginalPrice.value) * 100)
})

onMounted(() => {
  loadPackages()
  loadAvailableServices()
})

const loadPackages = async () => {
  loading.value = true
  try {
    const response = await shopAPI.getPackageList()
    if (response.code === 200) {
      const list = Array.isArray(response.data) ? response.data : []
      
      const enrichedPackages = await Promise.all(list.map(async (pkg) => {
        try {
          const detailResp = await shopAPI.getPackageDetail(pkg.id)
          if (detailResp.code === 200 && detailResp.data) {
            const detail = detailResp.data
            return transformPackage({ ...pkg, ...(detail.package || detail), items: detail.items || [] })
          }
        } catch (error) {
          console.error(`获取套餐${pkg.id}详情失败:`, error)
        }
        return transformPackage(pkg)
      }))

      allPackages.value = enrichedPackages
      pagination.current = 1
      applyFilters()
    }
  } catch (error) {
    console.error('获取套餐列表失败:', error)
    ElMessage.error('获取套餐列表失败')
  } finally {
    loading.value = false
  }
}

const loadAvailableServices = async () => {
  try {
    const response = await shopAPI.getServiceList()
    if (response.code === 200) {
      const list = Array.isArray(response.data) ? response.data : []
      availableServices.value = list
        .filter(item => item.status === 1)
        .map(item => ({
          id: item.id,
          name: item.itemName || item.name,
          price: Number(item.price || 0),
          category: item.category || '其他',
          duration: item.duration || item.durationMinutes || 60
        }))
    }
  } catch (error) {
    console.error('获取可用服务失败:', error)
  }
}

const handleSearch = () => {
  pagination.current = 1
  applyFilters()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    name: '',
    status: ''
  })
  pagination.current = 1
  applyFilters()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  applyFilters()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  applyFilters()
}

const showAddPackageDialog = () => {
  isEdit.value = false
  resetPackageForm()
  packageDialogVisible.value = true
}

const editPackage = (pkg) => {
  isEdit.value = true
  Object.assign(packageForm, {
    id: pkg.id,
    name: pkg.name,
    description: pkg.description,
    serviceIds: pkg.services.map(s => s.id || s.itemId),
    packagePrice: Number(pkg.packagePrice || 0),
    status: pkg.status,
    image: pkg.image || ''
  })
  packageDialogVisible.value = true
}

const resetPackageForm = () => {
  Object.assign(packageForm, {
    id: '',
    name: '',
    description: '',
    serviceIds: [],
    packagePrice: 0,
    status: 'ACTIVE',
    image: ''
  })
}

const submitPackage = async () => {
  if (!packageFormRef.value) return

  try {
    await packageFormRef.value.validate()
    submitLoading.value = true

    const payload = {
      packageName: packageForm.name.trim(),
      description: packageForm.description,
      packagePrice: Number(packageForm.packagePrice || 0),
      originalPrice: Number(calculatedOriginalPrice.value || 0),
      itemIds: packageForm.serviceIds,
      status: packageForm.status === 'ACTIVE' ? 1 : 0,
      coverUrl: packageForm.image || ''
    }

    let response
    if (isEdit.value) {
      response = await shopAPI.updatePackage(packageForm.id, { ...payload, id: packageForm.id })
    } else {
      response = await shopAPI.createPackage(payload)
    }

    if (response.code === 200) {
      ElMessage.success(isEdit.value ? '套餐更新成功' : '套餐创建成功')
      packageDialogVisible.value = false
      await loadPackages()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('提交套餐失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    submitLoading.value = false
  }
}

const togglePackageStatus = async (pkg) => {
  const desiredStatus = pkg.status
  const originalStatus = desiredStatus === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  try {
    const response = await shopAPI.updatePackageStatus([pkg.id], desiredStatus === 'ACTIVE' ? 1 : 0)
    if (response.code === 200) {
      ElMessage.success(`套餐已${desiredStatus === 'ACTIVE' ? '上架' : '下架'}`)
      await loadPackages()
    } else {
      pkg.status = originalStatus
      ElMessage.error(response.message || '状态更新失败')
    }
  } catch (error) {
    pkg.status = originalStatus
    console.error('更新套餐状态失败:', error)
    ElMessage.error('状态更新失败，请稍后重试')
  }
}

const deletePackage = async (pkg) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除套餐"${pkg.name}"吗？删除后不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await shopAPI.deletePackage(pkg.id)
    if (response.code === 200) {
      ElMessage.success('套餐删除成功')
      loadPackages()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除套餐失败:', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }
}

const viewPackageDetail = (pkg) => {
  selectedPackage.value = pkg
  detailDialogVisible.value = true
}

const onServiceChange = () => {
  // 当选择的服务改变时，自动计算套餐价格
  if (calculatedOriginalPrice.value > 0) {
    packageForm.packagePrice = Math.round(calculatedOriginalPrice.value * 0.8 * 100) / 100
  }
}

const getServiceName = (serviceId) => {
  const service = availableServices.value.find(s => s.id === serviceId)
  return service ? service.name : ''
}

const getServicePrice = (serviceId) => {
  const service = availableServices.value.find(s => s.id === serviceId)
  return service ? service.price : 0
}

const calculateDiscount = (pkg) => {
  if (!pkg.originalPrice || pkg.originalPrice === 0) return '10.0'
  const discount = (pkg.packagePrice / pkg.originalPrice) * 10
  return discount.toFixed(1)
}

const handleImageSuccess = (response) => {
  if (response.code === 200) {
    packageForm.image = response.data.url
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

const beforeImageUpload = (file) => {
  const isJPGOrPNG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPGOrPNG) {
    ElMessage.error('图片只能是 JPG/PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const getCategoryTagType = (category) => {
  const typeMap = {
    '保养': 'primary',
    '维修': 'danger',
    '清洗': 'info',
    '检测': 'warning',
    '其他': ''
  }
  return typeMap[category] || ''
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}
</script>

<style scoped>
.package-management {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  color: #2c3e50;
  font-size: 28px;
  font-weight: 600;
}

.filter-card {
  margin-bottom: 20px;
}

.package-list-card {
  margin-bottom: 20px;
}

.package-services {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.service-tag {
  margin: 0;
}

.original-price {
  text-decoration: line-through;
  color: #999;
  font-size: 12px;
}

.package-price {
  font-weight: 600;
  color: #e74c3c;
  font-size: 14px;
}

.discount {
  font-weight: 600;
  color: #27ae60;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.selected-services {
  margin-top: 10px;
  max-height: 100px;
  overflow-y: auto;
}

.selected-service-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 8px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 4px;
}

.service-price {
  color: #e74c3c;
  font-weight: 500;
}

.calculated-price {
  font-weight: 600;
  color: #2c3e50;
}

.savings {
  font-weight: 600;
  color: #27ae60;
}

.package-image-uploader .package-image {
  width: 120px;
  height: 90px;
  display: block;
  object-fit: cover;
}

.package-image-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: 0.2s;
}

.package-image-uploader .el-upload:hover {
  border-color: #409eff;
}

.package-image-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 90px;
  text-align: center;
  line-height: 90px;
}

.upload-tip {
  font-size: 12px;
  color: #666;
  margin-top: 5px;
}

.package-detail {
  padding: 20px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.detail-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 20px;
}

.detail-description {
  margin-bottom: 20px;
}

.detail-description p {
  color: #666;
  line-height: 1.6;
  margin: 0;
}

.detail-services {
  margin-bottom: 20px;
}

.detail-services h4 {
  margin: 0 0 10px 0;
  color: #2c3e50;
  font-size: 16px;
}

.service-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.service-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 4px;
}

.detail-pricing {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 6px;
  margin-bottom: 20px;
}

.price-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.price-item:last-child {
  margin-bottom: 0;
}

.price-item label {
  font-weight: 500;
  color: #606266;
}

.detail-image {
  margin-bottom: 20px;
}

.detail-image h4 {
  margin: 0 0 10px 0;
  color: #2c3e50;
  font-size: 16px;
}

.detail-image img {
  max-width: 100%;
  max-height: 200px;
  border-radius: 6px;
  border: 1px solid #ddd;
}

.detail-footer {
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.time-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.time-item:last-child {
  margin-bottom: 0;
}

.time-item label {
  font-weight: 500;
  color: #606266;
  min-width: 80px;
  margin-right: 10px;
}

.time-item span {
  color: #666;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .package-management {
    padding: 15px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .page-header h1 {
    font-size: 24px;
  }
}
</style>