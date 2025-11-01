<template>
  <div class="appointment">
    <div class="container">
      <div class="appointment-header">
        <h2>预约服务</h2>
        <p>选择您需要的保养服务</p>
      </div>

      <div class="appointment-content">
        <!-- 门店信息 -->
        <div class="shop-info" v-if="selectedShop">
          <h3>选择门店</h3>
          <div class="shop-card">
            <div class="shop-basic">
              <h4>{{ selectedShop.name }}</h4>
              <p>{{ selectedShop.address }}</p>
              <p>电话：{{ selectedShop.phone }}</p>
            </div>
            <el-button type="text" @click="changeShop">更换门店</el-button>
          </div>
        </div>

        <!-- 服务选择 -->
        <div class="service-selection">
          <h3>选择服务</h3>
          <div class="service-list">
            <div
              v-for="service in services"
              :key="service.id"
              class="service-item"
              :class="{ active: selectedServiceId === service.id }"
              @click="selectService(service)"
            >
              <div class="service-info">
                <h4>{{ service.name }}</h4>
                <p>{{ service.description }}</p>
              </div>
              <div class="service-price">
                <span class="price">¥{{ service.price }}</span>
                <span class="duration">{{ service.duration }}分钟</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 车辆选择 -->
        <div class="vehicle-selection">
          <h3>选择车辆</h3>
          <el-select v-model="selectedVehicleId" placeholder="请选择车辆" size="large" style="width: 100%;">
            <el-option
              v-for="vehicle in vehicles"
              :key="vehicle.id"
              :label="`${vehicle.brand} ${vehicle.model} (${vehicle.licensePlate})`"
              :value="vehicle.id"
            />
          </el-select>
          <el-button type="text" @click="showVehicleDialog" style="margin-top: 10px;">
            添加新车辆
          </el-button>
        </div>

        <!-- 车辆管理对话框 -->
        <el-dialog
          v-model="vehicleDialogVisible"
          :title="isEditVehicle ? '编辑车辆' : '添加车辆'"
          width="500px"
          @close="resetVehicleForm"
        >
          <el-form
            ref="vehicleFormRef"
            :model="vehicleForm"
            :rules="vehicleRules"
            label-width="80px"
          >
            <el-form-item label="品牌" prop="brand">
              <el-input v-model="vehicleForm.brand" placeholder="如：大众、丰田等" />
            </el-form-item>

            <el-form-item label="型号" prop="model">
              <el-input v-model="vehicleForm.model" placeholder="如：速腾、卡罗拉等" />
            </el-form-item>

            <el-form-item label="车牌号" prop="licensePlate">
              <el-input v-model="vehicleForm.licensePlate" placeholder="如：京A12345" />
            </el-form-item>

            <el-form-item label="颜色" prop="color">
              <el-select v-model="vehicleForm.color" placeholder="请选择颜色" style="width: 100%;">
                <el-option label="白色" value="白色"></el-option>
                <el-option label="黑色" value="黑色"></el-option>
                <el-option label="银色" value="银色"></el-option>
                <el-option label="灰色" value="灰色"></el-option>
                <el-option label="红色" value="红色"></el-option>
                <el-option label="蓝色" value="蓝色"></el-option>
                <el-option label="其他" value="其他"></el-option>
              </el-select>
            </el-form-item>

            <el-form-item label="年份" prop="year">
              <el-date-picker
                v-model="vehicleForm.yearDate"
                type="year"
                placeholder="选择年份"
                style="width: 100%;"
              />
            </el-form-item>
          </el-form>

          <template #footer>
            <span class="dialog-footer">
              <el-button @click="vehicleDialogVisible = false">取消</el-button>
              <el-button type="primary" :loading="vehicleSubmitting" @click="submitVehicle">
                {{ isEditVehicle ? '更新' : '添加' }}
              </el-button>
            </span>
          </template>
        </el-dialog>

        <!-- 时间选择 -->
        <div class="time-selection">
          <h3>选择时间</h3>
          <el-date-picker
            v-model="selectedDate"
            type="date"
            placeholder="选择日期"
            size="large"
            :disabled-date="disabledDate"
            style="width: 100%; margin-bottom: 15px;"
          />
          <el-select v-model="selectedTime" placeholder="选择时间段" size="large" style="width: 100%;">
            <el-option
              v-for="time in availableTimes"
              :key="time.value"
              :label="time.label"
              :value="time.value"
              :disabled="!time.available"
            />
          </el-select>
        </div>

        <!-- 备注 -->
        <div class="remark-section">
          <h3>备注信息</h3>
          <el-input
            v-model="remark"
            type="textarea"
            :rows="3"
            placeholder="请输入其他需要说明的信息（选填）"
          />
        </div>

        <!-- 提交按钮 -->
        <div class="submit-section">
          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :loading="submitting"
            @click="submitAppointment"
          >
            提交预约
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

export default {
  name: 'Appointment',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const submitting = ref(false)

    const selectedShop = ref(null)
    const selectedServiceId = ref(null)
    const selectedVehicleId = ref('')
    const selectedDate = ref('')
    const selectedTime = ref('')
    const remark = ref('')

    const services = ref([])

    const vehicles = ref([])

    const availableTimes = ref([])

    const disabledDate = (time) => {
      return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
    }

    const selectService = (service) => {
      selectedServiceId.value = service.id
    }

    const vehicleDialogVisible = ref(false)
    const isEditVehicle = ref(false)
    const vehicleSubmitting = ref(false)
    const vehicleFormRef = ref()

    const vehicleForm = ref({
      id: null,
      brand: '',
      model: '',
      licensePlate: '',
      color: '',
      yearDate: null
    })

    const vehicleRules = {
      brand: [
        { required: true, message: '请输入品牌', trigger: 'blur' }
      ],
      model: [
        { required: true, message: '请输入型号', trigger: 'blur' }
      ],
      licensePlate: [
        { required: true, message: '请输入车牌号', trigger: 'blur' },
        { pattern: /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[A-Z0-9]{4}[A-Z0-9挂学警港澳]{1}$/, message: '请输入正确的车牌号格式', trigger: 'blur' }
      ],
      color: [
        { required: true, message: '请选择颜色', trigger: 'change' }
      ]
    }

    const changeShop = () => {
      router.push('/shops')
    }

    const showVehicleDialog = () => {
      isEditVehicle.value = false
      resetVehicleForm()
      vehicleDialogVisible.value = true
    }

    const resetVehicleForm = () => {
      if (vehicleFormRef.value) {
        vehicleFormRef.value.resetFields()
      }
      vehicleForm.value = {
        id: null,
        brand: '',
        model: '',
        licensePlate: '',
        color: '',
        yearDate: null
      }
    }

    const submitVehicle = async () => {
      if (!vehicleFormRef.value) return

      try {
        await vehicleFormRef.value.validate()
        vehicleSubmitting.value = true

        const { default: request } = await import('../api/index.js')
        const vehicleData = {
          brand: vehicleForm.value.brand.trim(),
          model: vehicleForm.value.model.trim(),
          licensePlate: vehicleForm.value.licensePlate.trim(),
          color: vehicleForm.value.color,
          year: vehicleForm.value.yearDate ? new Date(vehicleForm.value.yearDate).getFullYear() : new Date().getFullYear()
        }

        let response
        if (isEditVehicle.value && vehicleForm.value.id) {
          response = await request.put(`/vehicle/${vehicleForm.value.id}`, vehicleData)
        } else {
          response = await request.post('/vehicle', vehicleData)
        }

        if (response.code === 200) {
          ElMessage.success(isEditVehicle.value ? '车辆更新成功' : '车辆添加成功')
          vehicleDialogVisible.value = false
          await loadVehicles()
        } else {
          ElMessage.error(response.message || '操作失败')
        }
      } catch (error) {
        console.error('提交车辆失败:', error)
        let errorMessage = '操作失败，请稍后重试'
        if (error.response?.data?.message) {
          errorMessage = error.response.data.message
        } else if (error.message) {
          errorMessage = error.message
        }
        ElMessage.error(errorMessage)
      } finally {
        vehicleSubmitting.value = false
      }
    }

    const submitAppointment = async () => {
      // 验证表单
      if (!selectedShop.value) {
        ElMessage.error('请选择门店')
        return
      }
      if (!selectedServiceId.value) {
        ElMessage.error('请选择服务')
        return
      }
      if (!selectedVehicleId.value) {
        ElMessage.error('请选择车辆')
        return
      }
      if (!selectedDate.value) {
        ElMessage.error('请选择日期')
        return
      }
      if (!selectedTime.value) {
        ElMessage.error('请选择时间')
        return
      }

      submitting.value = true

      try {
        // 调用真实的预约API
        const { default: request } = await import('../api/index.js')

        // 格式化日期
        const appointmentDate = new Date(selectedDate.value).toISOString().split('T')[0]

        // 找到选中的服务项目
        const selectedService = services.value.find(s => s.id === selectedServiceId.value)

        const appointmentData = {
          shopId: parseInt(selectedShop.value.id),
          vehicleId: selectedVehicleId.value,
          appointmentDate: appointmentDate,
          timeSlot: selectedTime.value,
          items: [{
            itemId: selectedService.id,
            itemName: selectedService.name,
            price: selectedService.price,
            quantity: 1
          }],
          remark: remark.value || ''
        }

        const response = await request.post('/appointment', appointmentData)

        if (response.code === 200) {
          ElMessage.success('预约提交成功！')
          router.push('/my-orders')
        } else {
          ElMessage.error(response.message || '预约失败，请重试')
        }

        submitting.value = false
      } catch (error) {
        console.error('预约失败:', error)
        ElMessage.error(error.response?.data?.message || '预约失败，请重试')
        submitting.value = false
      }
    }

    const loadShopInfo = async (shopId) => {
      try {
        const { shopAPI } = await import('../api/index.js')
        const response = await shopAPI.getShopDetail(shopId)
        if (response.code === 200 && response.data) {
          selectedShop.value = {
            id: response.data.id,
            name: response.data.shopName,
            address: response.data.address,
            phone: response.data.phone || '联系电话未提供',
            businessHours: response.data.businessHours
          }
        } else {
          ElMessage.error('门店信息不存在，请重新选择门店')
          router.push('/shops')
        }
      } catch (error) {
        console.error('加载门店信息失败:', error)
        const message = error.response?.data?.message || '加载门店信息失败'
        ElMessage.error(message)
        router.push('/shops')
      }
    }

    const loadServices = async (shopId) => {
      try {
        const { shopAPI } = await import('../api/index.js')
        const response = await shopAPI.getServiceItems(shopId)
        if (response.code === 200 && response.data) {
          services.value = response.data.map(item => ({
            id: item.id,
            name: item.itemName,
            description: item.description || '',
            price: item.price,
            duration: item.duration
          }))
          if (services.value.length > 0) {
            selectedServiceId.value = services.value[0].id
          }
        } else {
          services.value = []
          ElMessage.warning('当前门店暂无可预约的服务项目')
        }
      } catch (error) {
        console.error('加载服务列表失败:', error)
        services.value = []
        ElMessage.error(error.response?.data?.message || '加载服务列表失败')
      }
    }

    const loadVehicles = async () => {
      try {
        const userToken = localStorage.getItem('token')
        if (!userToken) {
          ElMessage.warning('请先登录后再预约服务')
          router.push('/login')
          return
        }

        const { default: request } = await import('../api/index.js')
        const response = await request.get('/vehicle')
        if (response.code === 200 && response.data) {
          vehicles.value = response.data.map(v => ({
            id: v.id,
            brand: v.brand,
            model: v.model,
            licensePlate: v.licensePlate
          }))
          if (vehicles.value.length > 0) {
            selectedVehicleId.value = vehicles.value[0].id
          }
        }
      } catch (error) {
        console.error('加载车辆列表失败:', error)
        vehicles.value = []
        if (error.response?.status !== 401) {
          ElMessage.error(error.response?.data?.message || '加载车辆列表失败')
        }
      }
    }

    const loadAvailableTimeSlots = async () => {
      if (!selectedShop.value?.id || !selectedDate.value) {
        availableTimes.value = []
        return
      }

      try {
        const { shopAPI } = await import('../api/index.js')
        const dateObj = new Date(selectedDate.value)
        const appointmentDate = `${dateObj.getFullYear()}-${String(dateObj.getMonth() + 1).padStart(2, '0')}-${String(dateObj.getDate()).padStart(2, '0')}`
        selectedTime.value = ''
        const response = await shopAPI.getAvailableTimes(selectedShop.value.id, appointmentDate)
        if (response.code === 200 && Array.isArray(response.data)) {
          availableTimes.value = response.data.map(slot => ({
            label: slot,
            value: slot,
            available: true
          }))
        } else {
          availableTimes.value = []
        }
      } catch (error) {
        console.error('加载可用时间段失败:', error)
        availableTimes.value = []
        ElMessage.error(error.response?.data?.message || '加载可用时间段失败')
      }
    }

    onMounted(async () => {
      // 从路由参数获取门店ID
      const shopId = route.query.shopId
      if (shopId) {
        await Promise.all([
          loadShopInfo(shopId),
          loadServices(shopId),
          loadVehicles()
        ])
        if (selectedDate.value) {
          loadAvailableTimeSlots()
        }
      } else {
        ElMessage.error('请先选择门店')
        router.push('/shops')
      }
    })

    watch(selectedDate, () => {
      loadAvailableTimeSlots()
    })

    return {
      selectedShop,
      selectedServiceId,
      selectedVehicleId,
      selectedDate,
      selectedTime,
      remark,
      services,
      vehicles,
      availableTimes,
      submitting,
      vehicleDialogVisible,
      isEditVehicle,
      vehicleSubmitting,
      vehicleFormRef,
      vehicleForm,
      vehicleRules,
      disabledDate,
      selectService,
      changeShop,
      showVehicleDialog,
      resetVehicleForm,
      submitVehicle,
      submitAppointment
    }
  }
}
</script>

<style scoped>
.appointment {
  padding: 40px 0;
  background: #f8f9fa;
  min-height: calc(100vh - 200px);
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

.appointment-header {
  text-align: center;
  margin-bottom: 40px;
}

.appointment-header h2 {
  color: #2c3e50;
  margin-bottom: 10px;
  font-size: 2rem;
}

.appointment-header p {
  color: #7f8c8d;
  font-size: 1.1rem;
}

.appointment-content > div {
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.appointment-content h3 {
  margin-bottom: 20px;
  color: #2c3e50;
  font-size: 1.3rem;
}

.shop-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
}

.shop-basic h4 {
  margin-bottom: 10px;
  color: #2c3e50;
}

.shop-basic p {
  color: #7f8c8d;
  margin-bottom: 5px;
}

.service-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.service-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.service-item:hover {
  border-color: #409eff;
}

.service-item.active {
  border-color: #409eff;
  background: #f0f8ff;
}

.service-info h4 {
  margin-bottom: 8px;
  color: #2c3e50;
}

.service-info p {
  color: #7f8c8d;
}

.service-price {
  text-align: right;
}

.price {
  display: block;
  font-size: 1.3rem;
  color: #e74c3c;
  font-weight: bold;
}

.duration {
  color: #7f8c8d;
  font-size: 0.9rem;
}

.submit-section {
  text-align: center;
  padding: 20px 0;
}

.submit-btn {
  padding: 15px 60px;
  font-size: 1.1rem;
}
</style>