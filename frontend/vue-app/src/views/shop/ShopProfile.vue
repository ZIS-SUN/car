<template>
  <div class="shop-profile">
    <el-card class="profile-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>门店信息</span>
          <el-button type="primary" @click="editMode = !editMode">
            {{ editMode ? '取消编辑' : '编辑信息' }}
          </el-button>
        </div>
      </template>

      <el-form
        ref="profileFormRef"
        :model="profileForm"
        :rules="profileRules"
        label-width="120px"
        :disabled="!editMode"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="门店名称" prop="name">
              <el-input
                v-model="profileForm.name"
                placeholder="请输入门店名称"
                maxlength="50"
                show-word-limit
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input
                v-model="profileForm.phone"
                placeholder="请输入联系电话"
                maxlength="20"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="门店地址" prop="address">
              <el-input
                v-model="profileForm.address"
                placeholder="请输入门店地址"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="营业时间" prop="businessHours">
              <el-input
                v-model="profileForm.businessHours"
                placeholder="例如：8:00-18:00"
                maxlength="50"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="门店类型" prop="type">
              <el-select
                v-model="profileForm.type"
                placeholder="请选择门店类型"
                style="width: 100%"
              >
                <el-option label="4S店" value="4S" />
                <el-option label="连锁店" value="CHAIN" />
                <el-option label="独立店" value="INDEPENDENT" />
                <el-option label="快修店" value="QUICK" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="负责人" prop="manager">
              <el-input
                v-model="profileForm.manager"
                placeholder="请输入负责人姓名"
                maxlength="20"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人电话" prop="managerPhone">
              <el-input
                v-model="profileForm.managerPhone"
                placeholder="请输入负责人电话"
                maxlength="20"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="门店简介" prop="description">
          <el-input
            v-model="profileForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入门店简介"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="门店图片" prop="images">
          <el-upload
            v-model:file-list="imageList"
            class="shop-image-uploader"
            :action="uploadUrl"
            :headers="uploadHeaders"
            list-type="picture-card"
            :on-success="handleImageSuccess"
            :on-remove="handleImageRemove"
            :before-upload="beforeImageUpload"
            multiple
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">建议尺寸：800x600px，最多上传5张图片</div>
        </el-form-item>

        <el-form-item label="服务项目" prop="serviceTypes">
          <el-checkbox-group v-model="profileForm.serviceTypes">
            <el-checkbox label="保养">保养服务</el-checkbox>
            <el-checkbox label="维修">故障维修</el-checkbox>
            <el-checkbox label="清洗">汽车清洗</el-checkbox>
            <el-checkbox label="检测">车辆检测</el-checkbox>
            <el-checkbox label="钣金">钣金喷漆</el-checkbox>
            <el-checkbox label="轮胎">轮胎服务</el-checkbox>
            <el-checkbox label="空调">空调服务</el-checkbox>
            <el-checkbox label="电瓶">电瓶服务</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item v-if="editMode">
          <el-button type="primary" :loading="saveLoading" @click="saveProfile">
            保存信息
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计信息 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon orders">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <h3>{{ stats.totalOrders }}</h3>
              <p>总订单数</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon revenue">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-info">
              <h3>¥{{ stats.totalRevenue }}</h3>
              <p>总收入</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon customers">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <h3>{{ stats.totalCustomers }}</h3>
              <p>服务客户</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon rating">
              <el-icon><Star /></el-icon>
            </div>
            <div class="stat-info">
              <h3>{{ stats.averageRating }}</h3>
              <p>平均评分</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { shopAPI } from '@/api/shop'

const editMode = ref(false)
const saveLoading = ref(false)
const profileFormRef = ref()
const imageList = ref([])
const currentShopId = ref(null)

const uploadUrl = '/api/upload/shop/images'
const uploadHeaders = {
  'Authorization': `Bearer ${localStorage.getItem('shopToken')}`
}

const profileForm = reactive({
  name: '',
  phone: '',
  address: '',
  businessHours: '',
  type: '',
  manager: '',
  managerPhone: '',
  description: '',
  images: [],
  serviceTypes: []
})

const originalProfileForm = reactive({})

const profileRules = {
  name: [
    { required: true, message: '请输入门店名称', trigger: 'blur' },
    { min: 2, max: 50, message: '门店名称长度在2到50个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入门店地址', trigger: 'blur' },
    { min: 5, max: 200, message: '地址长度在5到200个字符', trigger: 'blur' }
  ],
  businessHours: [
    { required: true, message: '请输入营业时间', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择门店类型', trigger: 'change' }
  ],
  manager: [
    { required: true, message: '请输入负责人姓名', trigger: 'blur' }
  ],
  managerPhone: [
    { required: true, message: '请输入负责人电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

const parseImages = (images) => {
  if (!images) return []
  if (Array.isArray(images)) return images
  if (typeof images === 'string') {
    try {
      const parsed = JSON.parse(images)
      if (Array.isArray(parsed)) {
        return parsed
      }
    } catch (error) {
      // ignore parse error, fallback to splitting string
    }
    return images.split(',').map(item => item.trim()).filter(Boolean)
  }
  return []
}

const formatCurrency = (value) => {
  if (value === null || value === undefined) {
    return '0.00'
  }
  const numberValue = Number(value)
  return Number.isNaN(numberValue) ? '0.00' : numberValue.toFixed(2)
}

const stats = reactive({
  totalOrders: 0,
  totalRevenue: '0.00',
  totalCustomers: 0,
  averageRating: '0.0'
})

onMounted(async () => {
  await loadShopProfile()
  await loadShopStats()
})

const loadShopProfile = async () => {
  try {
    const response = await shopAPI.getShopProfile()
    if (response.code === 200 && response.data) {
      const data = response.data
      currentShopId.value = data.id

      const images = parseImages(data.images)

      Object.assign(profileForm, {
        name: data.shopName || data.name || '',
        phone: data.phone || '',
        address: data.address || '',
        businessHours: data.businessHours || '',
        description: data.description || '',
        images,
        serviceTypes: profileForm.serviceTypes.length ? profileForm.serviceTypes : []
      })

      Object.assign(originalProfileForm, profileForm)

      imageList.value = images.map((url, index) => ({
        uid: index,
        name: `image${index}`,
        status: 'success',
        url
      }))
      return
    }
  } catch (error) {
    console.error('获取门店信息失败:', error)
    ElMessage.error('获取门店信息失败')
  }

  const savedShopInfo = localStorage.getItem('shopInfo')
  if (savedShopInfo) {
    Object.assign(profileForm, JSON.parse(savedShopInfo))
    Object.assign(originalProfileForm, profileForm)
    imageList.value = (profileForm.images || []).map((url, index) => ({
      uid: index,
      name: `image${index}`,
      status: 'success',
      url
    }))
  }
}

const loadShopStats = async () => {
  try {
    const [orderStatsResp, reviewStatsResp] = await Promise.all([
      shopAPI.getOrderStats(),
      currentShopId.value ? shopAPI.getReviewStats(currentShopId.value) : Promise.resolve(null)
    ])

    if (orderStatsResp?.code === 200 && orderStatsResp.data) {
      const data = orderStatsResp.data
      stats.totalOrders = data.totalCount || 0
      stats.totalRevenue = formatCurrency(data.revenue?.totalRevenue)
      stats.totalCustomers = data.totalCount || 0
    }

    if (reviewStatsResp?.code === 200 && reviewStatsResp.data) {
      const reviewData = reviewStatsResp.data
      stats.averageRating = Number(reviewData.averageRating || 0).toFixed(1)
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const saveProfile = async () => {
  if (!profileFormRef.value) return

  try {
    await profileFormRef.value.validate()
    saveLoading.value = true

    const images = imageList.value
      .filter(file => file.status === 'success' && file.url)
      .map(file => file.url)

    const payload = {
      shopName: profileForm.name.trim(),
      phone: profileForm.phone,
      address: profileForm.address,
      businessHours: profileForm.businessHours,
      description: profileForm.description,
      images: images.length ? JSON.stringify(images) : null
    }

    const response = await shopAPI.updateShopProfile(payload)
    if (response.code === 200) {
      ElMessage.success(response.message || '门店信息更新成功')
      editMode.value = false
      await loadShopProfile()
    } else {
      ElMessage.error(response.message || '更新失败')
    }
  } catch (error) {
    console.error('保存门店信息失败:', error)
    ElMessage.error('保存失败，请稍后重试')
  } finally {
    saveLoading.value = false
  }
}

const resetForm = () => {
  Object.assign(profileForm, originalProfileForm)
  if (profileFormRef.value) {
    profileFormRef.value.clearValidate()
  }
  imageList.value = (profileForm.images || []).map((url, index) => ({
    uid: index,
    name: `image${index}`,
    status: 'success',
    url
  }))
}

const handleImageSuccess = (response, file) => {
  if (response.code === 200) {
    file.url = response.data.url
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

const handleImageRemove = (file) => {
  const index = imageList.value.findIndex(item => item.uid === file.uid)
  if (index > -1) {
    imageList.value.splice(index, 1)
  }
}

const beforeImageUpload = (file) => {
  const isJPGOrPNG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isJPGOrPNG) {
    ElMessage.error('图片只能是 JPG/PNG 格式!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  if (imageList.value.length >= 5) {
    ElMessage.error('最多只能上传5张图片!')
    return false
  }
  return true
}
</script>

<style scoped>
.shop-profile {
  max-width: 1200px;
  margin: 0 auto;
}

.profile-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.shop-image-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: 0.2s;
}

.shop-image-uploader .el-upload:hover {
  border-color: #409eff;
}

.shop-image-uploader .el-icon {
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

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 20px;
  color: white;
}

.stat-icon.orders {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.revenue {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.customers {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.rating {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info h3 {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 5px 0;
}

.stat-info p {
  color: #6c757d;
  font-size: 14px;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stats-row .el-col {
    margin-bottom: 16px;
  }
}
</style>