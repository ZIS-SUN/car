<template>
  <div class="service-management">
    <div class="page-header">
      <h1>服务项目管理</h1>
      <div class="header-actions">
        <el-button type="primary" @click="showAddServiceDialog">
          <el-icon><Plus /></el-icon>
          添加服务项目
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="服务名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入服务名称"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="服务类别">
          <el-select
            v-model="searchForm.category"
            placeholder="请选择类别"
            clearable
            style="width: 120px"
          >
            <el-option label="保养" value="保养" />
            <el-option label="维修" value="维修" />
            <el-option label="清洗" value="清洗" />
            <el-option label="检测" value="检测" />
            <el-option label="其他" value="其他" />
          </el-select>
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

    <!-- 服务项目列表 -->
    <el-card class="service-list-card" shadow="hover">
      <el-table
        v-loading="loading"
        :data="services"
        style="width: 100%"
        stripe
      >
        <el-table-column prop="name" label="服务名称" min-width="150" />
        <el-table-column prop="category" label="服务类别" width="100">
          <template #default="{ row }">
            <el-tag :type="getCategoryTagType(row.category)" size="small">
              {{ row.category }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="服务描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="standardHours" label="标准工时" width="100">
          <template #default="{ row }">
            {{ row.standardHours }}小时
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              active-value="ACTIVE"
              inactive-value="INACTIVE"
              @change="toggleServiceStatus(row)"
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
              @click="editService(row)"
            >
              编辑
            </el-button>
            <el-button
              size="small"
              type="danger"
              link
              @click="deleteService(row)"
            >
              删除
            </el-button>
            <el-button
              size="small"
              type="info"
              link
              @click="viewServiceDetail(row)"
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

    <!-- 添加/编辑服务对话框 -->
    <el-dialog
      v-model="serviceDialogVisible"
      :title="isEdit ? '编辑服务项目' : '添加服务项目'"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="serviceFormRef"
        :model="serviceForm"
        :rules="serviceRules"
        label-width="100px"
      >
        <el-form-item label="服务名称" prop="name">
          <el-input
            v-model="serviceForm.name"
            placeholder="请输入服务名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="服务类别" prop="category">
          <el-select
            v-model="serviceForm.category"
            placeholder="请选择服务类别"
            style="width: 100%"
          >
            <el-option label="保养" value="保养" />
            <el-option label="维修" value="维修" />
            <el-option label="清洗" value="清洗" />
            <el-option label="检测" value="检测" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务描述" prop="description">
          <el-input
            v-model="serviceForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入服务描述"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="标准工时" prop="standardHours">
          <el-input-number
            v-model="serviceForm.standardHours"
            :min="0.5"
            :max="24"
            :step="0.5"
            :precision="1"
            style="width: 100%"
          />
          <span style="margin-left: 10px; color: #666;">小时</span>
        </el-form-item>
        <el-form-item label="服务价格" prop="price">
          <el-input-number
            v-model="serviceForm.price"
            :min="0"
            :max="9999"
            :precision="2"
            style="width: 100%"
          />
          <span style="margin-left: 10px; color: #666;">元</span>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="serviceForm.status">
            <el-radio label="ACTIVE">上架</el-radio>
            <el-radio label="INACTIVE">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="服务图片" prop="image">
          <el-upload
            class="service-image-uploader"
            :action="uploadUrl"
            :show-file-list="false"
            :on-success="handleImageSuccess"
            :before-upload="beforeImageUpload"
          >
            <img v-if="serviceForm.image" :src="serviceForm.image" class="service-image" />
            <el-icon v-else class="service-image-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">建议尺寸：300x300px，支持jpg、png格式</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="serviceDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitService">
          {{ isEdit ? '更新' : '添加' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 服务详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="服务详情"
      width="500px"
      destroy-on-close
    >
      <div v-if="selectedService" class="service-detail">
        <div class="detail-item">
          <label>服务名称：</label>
          <span>{{ selectedService.name }}</span>
        </div>
        <div class="detail-item">
          <label>服务类别：</label>
          <el-tag :type="getCategoryTagType(selectedService.category)" size="small">
            {{ selectedService.category }}
          </el-tag>
        </div>
        <div class="detail-item">
          <label>服务描述：</label>
          <p>{{ selectedService.description }}</p>
        </div>
        <div class="detail-item">
          <label>标准工时：</label>
          <span>{{ selectedService.standardHours }}小时</span>
        </div>
        <div class="detail-item">
          <label>服务价格：</label>
          <span class="price">¥{{ selectedService.price }}</span>
        </div>
        <div class="detail-item">
          <label>状态：</label>
          <el-tag :type="selectedService.status === 'ACTIVE' ? 'success' : 'danger'" size="small">
            {{ selectedService.status === 'ACTIVE' ? '上架' : '下架' }}
          </el-tag>
        </div>
        <div v-if="selectedService.image" class="detail-item">
          <label>服务图片：</label>
          <div class="service-image-preview">
            <img :src="selectedService.image" alt="服务图片" />
          </div>
        </div>
        <div class="detail-item">
          <label>创建时间：</label>
          <span>{{ formatDateTime(selectedService.createdAt) }}</span>
        </div>
        <div class="detail-item">
          <label>更新时间：</label>
          <span>{{ formatDateTime(selectedService.updatedAt) }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { shopAPI } from '@/api/shop'

const loading = ref(false)
const submitLoading = ref(false)
const services = ref([])
const allServices = ref([])
const selectedService = ref(null)
const serviceDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const isEdit = ref(false)
const serviceFormRef = ref()

const uploadUrl = '/api/shop/upload/service-image'

const searchForm = reactive({
  name: '',
  category: '',
  status: ''
})

const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

const serviceForm = reactive({
  id: '',
  name: '',
  category: '',
  description: '',
  standardHours: 1,
  price: 0,
  status: 'ACTIVE',
  image: ''
})

const serviceRules = {
  name: [
    { required: true, message: '请输入服务名称', trigger: 'blur' },
    { min: 2, max: 50, message: '服务名称长度在2到50个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择服务类别', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入服务描述', trigger: 'blur' },
    { min: 10, max: 200, message: '服务描述长度在10到200个字符', trigger: 'blur' }
  ],
  standardHours: [
    { required: true, message: '请输入标准工时', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入服务价格', trigger: 'blur' }
  ]
}

const transformServiceItem = (item) => {
  const duration = item.duration ?? item.durationMinutes ?? 60
  return {
    id: item.id,
    name: item.itemName || item.name || '',
    category: item.category || '其他',
    description: item.description || '',
    standardHours: Number((duration / 60).toFixed(1)) || 1,
    price: Number(item.price ?? item.packagePrice ?? 0),
    status: (item.status === 1 || item.status === 'ACTIVE') ? 'ACTIVE' : 'INACTIVE',
    image: item.iconUrl || item.imageUrl || '',
    createdAt: item.createTime,
    updatedAt: item.updateTime
  }
}

const applyFilters = () => {
  let filtered = [...allServices.value]

  if (searchForm.name) {
    filtered = filtered.filter(service => service.name.includes(searchForm.name.trim()))
  }

  if (searchForm.category) {
    filtered = filtered.filter(service => service.category === searchForm.category)
  }

  if (searchForm.status) {
    filtered = filtered.filter(service => service.status === searchForm.status)
  }

  pagination.total = filtered.length

  const start = (pagination.current - 1) * pagination.size
  services.value = filtered.slice(start, start + pagination.size)
}

onMounted(() => {
  loadServices()
})

const loadServices = async () => {
  loading.value = true
  try {
    const response = await shopAPI.getServiceList()
    if (response.code === 200) {
      const list = Array.isArray(response.data) ? response.data : []
      allServices.value = list.map(transformServiceItem)
      pagination.current = 1
      applyFilters()
    }
  } catch (error) {
    console.error('获取服务列表失败:', error)
    ElMessage.error('获取服务列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  applyFilters()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    name: '',
    category: '',
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

const showAddServiceDialog = () => {
  isEdit.value = false
  resetServiceForm()
  serviceDialogVisible.value = true
}

const editService = (service) => {
  isEdit.value = true
  Object.assign(serviceForm, {
    id: service.id,
    name: service.name,
    category: service.category,
    description: service.description,
    standardHours: service.standardHours,
    price: Number(service.price || 0),
    status: service.status,
    image: service.image || ''
  })
  serviceDialogVisible.value = true
}

const resetServiceForm = () => {
  Object.assign(serviceForm, {
    id: '',
    name: '',
    category: '',
    description: '',
    standardHours: 1,
    price: 0,
    status: 'ACTIVE',
    image: ''
  })
}

const submitService = async () => {
  if (!serviceFormRef.value) return

  try {
    await serviceFormRef.value.validate()
    submitLoading.value = true

    const payload = {
      itemName: serviceForm.name.trim(),
      category: serviceForm.category,
      description: serviceForm.description,
      price: Number(serviceForm.price || 0),
      duration: Math.max(1, Math.round(Number(serviceForm.standardHours || 1) * 60)),
      iconUrl: serviceForm.image || '',
      status: serviceForm.status === 'ACTIVE' ? 1 : 0
    }

    let response
    if (isEdit.value) {
      response = await shopAPI.updateService(serviceForm.id, { ...payload, id: serviceForm.id })
    } else {
      response = await shopAPI.createService(payload)
    }

    if (response.code === 200) {
      ElMessage.success(isEdit.value ? '服务更新成功' : '服务添加成功')
      serviceDialogVisible.value = false
      await loadServices()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('提交服务失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    submitLoading.value = false
  }
}

const toggleServiceStatus = async (service) => {
  const desiredStatus = service.status
  const originalStatus = desiredStatus === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  try {
    const response = await shopAPI.updateServiceStatus([service.id], desiredStatus === 'ACTIVE' ? 1 : 0)
    if (response.code === 200) {
      ElMessage.success(`服务已${desiredStatus === 'ACTIVE' ? '上架' : '下架'}`)
      await loadServices()
    } else {
      service.status = originalStatus
      ElMessage.error(response.message || '状态更新失败')
    }
  } catch (error) {
    service.status = originalStatus
    console.error('更新服务状态失败:', error)
    ElMessage.error('状态更新失败，请稍后重试')
  }
}

const deleteService = async (service) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除服务"${service.name}"吗？删除后不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await shopAPI.deleteService(service.id)
    if (response.code === 200) {
      ElMessage.success('服务删除成功')
      loadServices()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除服务失败:', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }
}

const viewServiceDetail = (service) => {
  selectedService.value = service
  detailDialogVisible.value = true
}

const handleImageSuccess = (response) => {
  if (response.code === 200) {
    serviceForm.image = response.data.url
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
.service-management {
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

.service-list-card {
  margin-bottom: 20px;
}

.price {
  font-weight: 600;
  color: #e74c3c;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.service-image-uploader .service-image {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
}

.service-image-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: 0.2s;
}

.service-image-uploader .el-upload:hover {
  border-color: #409eff;
}

.service-image-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
  line-height: 100px;
}

.upload-tip {
  font-size: 12px;
  color: #666;
  margin-top: 5px;
}

.service-detail {
  padding: 20px;
}

.detail-item {
  margin-bottom: 15px;
  display: flex;
  align-items: flex-start;
}

.detail-item label {
  font-weight: 500;
  color: #606266;
  min-width: 80px;
  margin-right: 10px;
}

.detail-item span {
  color: #2c3e50;
  flex: 1;
}

.detail-item p {
  color: #2c3e50;
  margin: 0;
  flex: 1;
  line-height: 1.5;
}

.service-image-preview {
  margin-top: 5px;
}

.service-image-preview img {
  max-width: 200px;
  max-height: 200px;
  border-radius: 4px;
  border: 1px solid #ddd;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .service-management {
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