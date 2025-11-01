<template>
  <div class="vehicle-management">
    <div class="section-header">
      <h3>我的车辆</h3>
      <el-button type="primary" @click="showAddDialog">
        <el-icon><plus /></el-icon>
        添加车辆
      </el-button>
    </div>

    <div v-loading="loading" class="vehicles-list">
      <div v-if="vehicles.length === 0" class="empty-state">
        <el-empty description="暂无车辆信息">
          <el-button type="primary" @click="showAddDialog">添加第一辆车</el-button>
        </el-empty>
      </div>

      <div v-else class="vehicles-grid">
        <div v-for="vehicle in vehicles" :key="vehicle.id" class="vehicle-card">
          <div class="vehicle-header">
            <div class="vehicle-icon">🚗</div>
            <div class="vehicle-badge" v-if="vehicle.isDefault">
              <el-tag size="small" type="success">默认车辆</el-tag>
            </div>
          </div>

          <div class="vehicle-info">
            <h4>{{ vehicle.brand }} {{ vehicle.model }}</h4>
            <p class="license-plate">{{ vehicle.licensePlate }}</p>
            <div class="vehicle-details">
              <p><span>颜色：</span>{{ vehicle.color }}</p>
              <p><span>年份：</span>{{ vehicle.year }}</p>
              <p><span>排量：</span>{{ vehicle.displacement }}</p>
              <p><span>变速箱：</span>{{ vehicle.transmission }}</p>
            </div>
          </div>

          <div class="vehicle-actions">
            <el-button
              v-if="!vehicle.isDefault"
              type="text"
              @click="setDefaultVehicle(vehicle)"
            >
              设为默认
            </el-button>
            <el-button type="text" @click="editVehicle(vehicle)">编辑</el-button>
            <el-button
              type="text"
              style="color: #f56c6c;"
              @click="deleteVehicle(vehicle)"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加/编辑车辆对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑车辆' : '添加车辆'"
      width="500px"
      @close="resetForm"
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
            v-model="vehicleForm.year"
            type="year"
            placeholder="选择年份"
            style="width: 100%;"
          />
        </el-form-item>

        <el-form-item label="排量" prop="displacement">
          <el-select v-model="vehicleForm.displacement" placeholder="请选择排量" style="width: 100%;">
            <el-option label="1.0L" value="1.0L"></el-option>
            <el-option label="1.2L" value="1.2L"></el-option>
            <el-option label="1.4L" value="1.4L"></el-option>
            <el-option label="1.5L" value="1.5L"></el-option>
            <el-option label="1.6L" value="1.6L"></el-option>
            <el-option label="1.8L" value="1.8L"></el-option>
            <el-option label="2.0L" value="2.0L"></el-option>
            <el-option label="2.4L" value="2.4L"></el-option>
            <el-option label="3.0L" value="3.0L"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="变速箱" prop="transmission">
          <el-select v-model="vehicleForm.transmission" placeholder="请选择变速箱" style="width: 100%;">
            <el-option label="手动" value="手动"></el-option>
            <el-option label="自动" value="自动"></el-option>
            <el-option label="双离合" value="双离合"></el-option>
            <el-option label="CVT" value="CVT"></el-option>
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitVehicle">
            {{ isEdit ? '更新' : '添加' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/index'

export default {
  name: 'VehicleManagement',
  components: {
    Plus
  },
  emits: ['vehicle-selected'],
  setup(props, { emit }) {
    const loading = ref(false)
    const dialogVisible = ref(false)
    const isEdit = ref(false)
    const submitting = ref(false)
    const vehicleFormRef = ref()
    const vehicles = ref([])

    const vehicleForm = reactive({
      id: null,
      brand: '',
      model: '',
      licensePlate: '',
      color: '',
      year: '',
      displacement: '',
      transmission: ''
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
      ],
      year: [
        { required: true, message: '请选择年份', trigger: 'change' }
      ],
      displacement: [
        { required: true, message: '请选择排量', trigger: 'change' }
      ],
      transmission: [
        { required: true, message: '请选择变速箱', trigger: 'change' }
      ]
    }

    const loadVehicles = async () => {
      loading.value = true
      try {
        const response = await request.get('/vehicle')
        if (response.code === 200) {
          vehicles.value = response.data.map(vehicle => ({
            ...vehicle,
            year: vehicle.year ? vehicle.year.toString() : '',
            displacement: vehicle.displacement || '未知',
            transmission: vehicle.transmission || '未知',
            isDefault: false
          }))
        } else {
          ElMessage.error(response.message || '加载车辆列表失败')
        }
      } catch (error) {
        console.error('加载车辆列表失败:', error)
        ElMessage.error('加载车辆列表失败')
      } finally {
        loading.value = false
      }
    }

    const showAddDialog = () => {
      isEdit.value = false
      dialogVisible.value = true
    }

    const editVehicle = (vehicle) => {
      isEdit.value = true
      Object.assign(vehicleForm, vehicle)
      dialogVisible.value = true
    }

    const resetForm = () => {
      if (vehicleFormRef.value) {
        vehicleFormRef.value.resetFields()
      }
      Object.assign(vehicleForm, {
        id: null,
        brand: '',
        model: '',
        licensePlate: '',
        color: '',
        year: '',
        displacement: '',
        transmission: ''
      })
    }

    const submitVehicle = async () => {
      if (!vehicleFormRef.value) return

      try {
        await vehicleFormRef.value.validate()
        submitting.value = true

        const vehicleData = {
          brand: vehicleForm.brand,
          model: vehicleForm.model,
          licensePlate: vehicleForm.licensePlate,
          color: vehicleForm.color,
          year: vehicleForm.year ? new Date(vehicleForm.year).getFullYear() : null,
          displacement: vehicleForm.displacement,
          transmission: vehicleForm.transmission
        }

        let response
        if (isEdit.value) {
          response = await request.put(`/vehicle/${vehicleForm.id}`, vehicleData)
          if (response.code === 200) {
            const index = vehicles.value.findIndex(v => v.id === vehicleForm.id)
            if (index > -1) {
              vehicles.value[index] = { ...vehicleData, id: vehicleForm.id }
            }
            ElMessage.success('车辆信息更新成功')
          } else {
            ElMessage.error(response.message || '车辆信息更新失败')
          }
        } else {
          response = await request.post('/vehicle', vehicleData)
          if (response.code === 200) {
            const newVehicle = {
              ...vehicleData,
              id: response.data || Date.now(),
              isDefault: vehicles.value.length === 0
            }
            vehicles.value.push(newVehicle)
            ElMessage.success('车辆添加成功')
          } else {
            ElMessage.error(response.message || '车辆添加失败')
          }
        }

        dialogVisible.value = false
        resetForm()

      } catch (error) {
        console.error('提交车辆信息失败:', error)
        ElMessage.error(isEdit.value ? '车辆信息更新失败' : '车辆添加失败')
      } finally {
        submitting.value = false
      }
    }

    const setDefaultVehicle = (vehicle) => {
      vehicles.value.forEach(v => v.isDefault = false)
      vehicle.isDefault = true
      ElMessage.success('已设置为默认车辆')
    }

    const deleteVehicle = async (vehicle) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除 ${vehicle.brand} ${vehicle.model} 吗？`,
          '删除车辆',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )

        const response = await request.delete(`/vehicle/${vehicle.id}`)
        if (response.code === 200) {
          const index = vehicles.value.findIndex(v => v.id === vehicle.id)
          if (index > -1) {
            vehicles.value.splice(index, 1)
            ElMessage.success('车辆删除成功')
          }
        } else {
          ElMessage.error(response.message || '车辆删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除车辆失败:', error)
          ElMessage.error('车辆删除失败')
        }
      }
    }

    onMounted(() => {
      loadVehicles()
    })

    return {
      loading,
      dialogVisible,
      isEdit,
      submitting,
      vehicleFormRef,
      vehicleForm,
      vehicleRules,
      vehicles,
      showAddDialog,
      editVehicle,
      resetForm,
      submitVehicle,
      setDefaultVehicle,
      deleteVehicle
    }
  }
}
</script>

<style scoped>
.vehicle-management {
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.section-header h3 {
  color: #2c3e50;
  font-size: 1.3rem;
  margin: 0;
}

.vehicles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.vehicle-card {
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  padding: 25px;
  transition: all 0.3s ease;
  position: relative;
}

.vehicle-card:hover {
  border-color: #409eff;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
}

.vehicle-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.vehicle-icon {
  font-size: 2rem;
}

.vehicle-info h4 {
  margin-bottom: 8px;
  color: #2c3e50;
  font-size: 1.2rem;
}

.license-plate {
  background: #f0f8ff;
  color: #409eff;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: 500;
  font-size: 0.9rem;
  margin-bottom: 15px;
  display: inline-block;
}

.vehicle-details p {
  color: #7f8c8d;
  margin-bottom: 5px;
  font-size: 0.9rem;
}

.vehicle-details span {
  color: #2c3e50;
  font-weight: 500;
  margin-right: 5px;
}

.vehicle-actions {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  gap: 15px;
}

.empty-state {
  text-align: center;
  margin-top: 50px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 768px) {
  .vehicles-grid {
    grid-template-columns: 1fr;
  }

  .section-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
}
</style>