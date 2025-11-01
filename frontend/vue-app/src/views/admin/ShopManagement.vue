<template>
  <div class="shop-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>门店管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleCreate">
              <el-icon><plus /></el-icon>
              新建门店
            </el-button>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索门店..."
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

      <el-table v-loading="loading" :data="shopList" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="shopName" label="门店名称" width="200" />
        <el-table-column prop="city" label="城市" width="120" />
        <el-table-column prop="address" label="地址" min-width="200" />
        <el-table-column prop="phone" label="联系电话" width="150" />
        <el-table-column prop="rating" label="评分" width="100">
          <template #default="scope">
            <el-rate :model-value="scope.row.rating" disabled show-score />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 0"
              type="success"
              size="small"
              @click="handleApprove(scope.row)"
            >
              审核通过
            </el-button>
            <el-button
              v-if="scope.row.status === 0"
              type="warning"
              size="small"
              @click="handleReject(scope.row)"
            >
              拒绝
            </el-button>
            <el-button
              type="primary"
              size="small"
              @click="handleEdit(scope.row)"
            >
              编辑
            </el-button>
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
          @size-change="loadShopList"
          @current-change="loadShopList"
        />
      </div>
    </el-card>

    <!-- 拒绝原因对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝原因" width="500px">
      <el-input
        v-model="rejectReason"
        type="textarea"
        :rows="4"
        placeholder="请输入拒绝原因..."
      />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReject">确定</el-button>
      </template>
    </el-dialog>

    <!-- 门店编辑对话框 -->
    <el-dialog
      v-model="shopDialogVisible"
      :title="isEdit ? '编辑门店' : '新建门店'"
      width="600px"
      @close="resetForm"
    >
      <el-form :model="shopForm" :rules="shopRules" ref="shopFormRef" label-width="100px">
        <el-form-item label="门店名称" prop="shopName">
          <el-input v-model="shopForm.shopName" placeholder="请输入门店名称" />
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="shopForm.city" placeholder="请输入城市" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="shopForm.address" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="shopForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="营业时间" prop="businessHours">
          <el-input v-model="shopForm.businessHours" placeholder="例如: 09:00-18:00" />
        </el-form-item>
        <el-form-item label="门店描述" prop="description">
          <el-input
            v-model="shopForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入门店描述"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="shopForm.status" placeholder="请选择状态">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shopDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitShopForm" :loading="submitLoading">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminAPI } from '../../api/admin.js'

export default {
  name: 'ShopManagement',
  components: {
    Search,
    Plus
  },
  setup() {
    const loading = ref(false)
    const searchKeyword = ref('')
    const shopList = ref([])
    const pagination = ref({
      current: 1,
      size: 10,
      total: 0
    })
    const rejectDialogVisible = ref(false)
    const rejectReason = ref('')
    const currentShop = ref(null)

    // 门店编辑相关
    const shopDialogVisible = ref(false)
    const isEdit = ref(false)
    const submitLoading = ref(false)
    const shopFormRef = ref(null)
    const shopForm = ref({
      shopName: '',
      city: '',
      address: '',
      phone: '',
      businessHours: '',
      description: '',
      status: 0
    })
    const shopRules = {
      shopName: [{ required: true, message: '请输入门店名称', trigger: 'blur' }],
      city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
      address: [{ required: true, message: '请输入地址', trigger: 'blur' }],
      phone: [
        { required: true, message: '请输入联系电话', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
      ],
      businessHours: [{ required: true, message: '请输入营业时间', trigger: 'blur' }]
    }

    const loadShopList = async () => {
      loading.value = true
      try {
        const response = await adminAPI.getShopList({
          current: pagination.value.current,
          size: pagination.value.size,
          keyword: searchKeyword.value
        })
        if (response.code === 200 && response.data) {
          shopList.value = response.data.records || []
          pagination.value.total = response.data.total || 0
        }
      } catch (error) {
        console.error('加载门店列表失败:', error)
        ElMessage.error('加载门店列表失败')
      } finally {
        loading.value = false
      }
    }

    const handleSearch = () => {
      pagination.value.current = 1
      loadShopList()
    }

    const getStatusText = (status) => {
      const statusMap = {
        0: '待审核',
        1: '已通过',
        2: '已拒绝'
      }
      return statusMap[status] || '未知'
    }

    const getStatusTag = (status) => {
      const tagMap = {
        0: 'warning',
        1: 'success',
        2: 'danger'
      }
      return tagMap[status] || 'info'
    }

    const handleApprove = async (shop) => {
      try {
        await ElMessageBox.confirm(
          `确定要审核通过门店"${shop.shopName}"吗？`,
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )

        await adminAPI.approveShop(shop.id)
        ElMessage.success('审核通过')
        loadShopList()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('审核失败')
        }
      }
    }

    const handleReject = (shop) => {
      currentShop.value = shop
      rejectReason.value = ''
      rejectDialogVisible.value = true
    }

    const confirmReject = async () => {
      if (!rejectReason.value.trim()) {
        ElMessage.warning('请输入拒绝原因')
        return
      }

      try {
        await adminAPI.rejectShop(currentShop.value.id, rejectReason.value)
        ElMessage.success('已拒绝')
        rejectDialogVisible.value = false
        loadShopList()
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }

    // 新建门店
    const handleCreate = () => {
      isEdit.value = false
      resetForm()
      shopDialogVisible.value = true
    }

    // 编辑门店
    const handleEdit = (shop) => {
      isEdit.value = true
      shopForm.value = {
        id: shop.id,
        shopName: shop.shopName,
        city: shop.city,
        address: shop.address,
        phone: shop.phone,
        businessHours: shop.businessHours || '09:00-18:00',
        description: shop.description || '',
        status: shop.status
      }
      shopDialogVisible.value = true
    }

    // 删除门店
    const handleDelete = async (shop) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除门店"${shop.shopName}"吗？此操作不可恢复！`,
          '警告',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'error'
          }
        )

        await adminAPI.deleteShop(shop.id)
        ElMessage.success('删除成功')
        loadShopList()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败')
        }
      }
    }

    // 提交门店表单
    const submitShopForm = async () => {
      if (!shopFormRef.value) return

      await shopFormRef.value.validate(async (valid) => {
        if (!valid) return

        submitLoading.value = true
        try {
          if (isEdit.value) {
            await adminAPI.updateShop(shopForm.value.id, shopForm.value)
            ElMessage.success('更新成功')
          } else {
            await adminAPI.createShop(shopForm.value)
            ElMessage.success('创建成功')
          }
          shopDialogVisible.value = false
          loadShopList()
        } catch (error) {
          ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
        } finally {
          submitLoading.value = false
        }
      })
    }

    // 重置表单
    const resetForm = () => {
      shopForm.value = {
        shopName: '',
        city: '',
        address: '',
        phone: '',
        businessHours: '',
        description: '',
        status: 0
      }
      if (shopFormRef.value) {
        shopFormRef.value.resetFields()
      }
    }

    onMounted(() => {
      loadShopList()
    })

    return {
      loading,
      searchKeyword,
      shopList,
      pagination,
      rejectDialogVisible,
      rejectReason,
      shopDialogVisible,
      isEdit,
      submitLoading,
      shopFormRef,
      shopForm,
      shopRules,
      handleSearch,
      getStatusText,
      getStatusTag,
      handleApprove,
      handleReject,
      confirmReject,
      handleCreate,
      handleEdit,
      handleDelete,
      submitShopForm,
      resetForm
    }
  }
}
</script>

<style scoped>
.shop-management {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 16px;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
