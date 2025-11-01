<template>
  <div class="member-management">
    <!-- 会员统计 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="8" v-for="stat in stats" :key="stat.label">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-label">{{ stat.label }}</div>
            <div class="stat-value">{{ stat.value }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 会员等级管理 -->
    <el-card class="level-card">
      <template #header>
        <div class="card-header">
          <span>会员等级配置</span>
          <el-button type="primary" @click="showCreateDialog">添加等级</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="memberLevels" stripe style="width: 100%">
        <el-table-column prop="id" label="等级ID" width="100" />
        <el-table-column prop="levelName" label="等级名称" width="150" />
        <el-table-column prop="minExperience" label="所需经验值" width="150" />
        <el-table-column prop="discountRate" label="折扣率" width="120">
          <template #default="scope">
            {{ (scope.row.discountRate * 100).toFixed(0) }}%
          </template>
        </el-table-column>
        <el-table-column prop="benefits" label="会员权益" min-width="200" />
        <el-table-column prop="icon" label="图标" width="100" />
        <el-table-column prop="color" label="颜色" width="100">
          <template #default="scope">
            <div class="color-preview" :style="{ backgroundColor: scope.row.color }"></div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingLevel ? '编辑会员等级' : '添加会员等级'"
      width="600px"
    >
      <el-form :model="levelForm" :rules="levelRules" ref="levelFormRef" label-width="120px">
        <el-form-item label="等级名称" prop="levelName">
          <el-input v-model="levelForm.levelName" placeholder="例如：青铜会员" />
        </el-form-item>
        <el-form-item label="所需经验值" prop="minExperience">
          <el-input-number v-model="levelForm.minExperience" :min="0" :step="100" />
        </el-form-item>
        <el-form-item label="折扣率" prop="discountRate">
          <el-input-number
            v-model="levelForm.discountRate"
            :min="0"
            :max="1"
            :step="0.01"
            :precision="2"
          />
          <span class="form-tip">0-1之间的小数，例如0.95表示95折</span>
        </el-form-item>
        <el-form-item label="会员权益" prop="benefits">
          <el-input
            v-model="levelForm.benefits"
            type="textarea"
            :rows="3"
            placeholder="例如：享受95折优惠、优先预约服务"
          />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="levelForm.icon" placeholder="图标名称" />
        </el-form-item>
        <el-form-item label="颜色" prop="color">
          <el-color-picker v-model="levelForm.color" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../api/index.js'

export default {
  name: 'MemberManagement',
  setup() {
    const loading = ref(false)
    const memberLevels = ref([])
    const stats = ref([
      { label: '总会员数', value: 0 },
      { label: '活跃会员', value: 0 },
      { label: '本月新增', value: 0 }
    ])
    const dialogVisible = ref(false)
    const editingLevel = ref(null)
    const levelFormRef = ref()
    const levelForm = ref({
      levelName: '',
      minExperience: 0,
      discountRate: 1.0,
      benefits: '',
      icon: '',
      color: '#409EFF'
    })

    const levelRules = {
      levelName: [
        { required: true, message: '请输入等级名称', trigger: 'blur' }
      ],
      minExperience: [
        { required: true, message: '请输入所需经验值', trigger: 'blur' }
      ],
      discountRate: [
        { required: true, message: '请输入折扣率', trigger: 'blur' }
      ]
    }

    const loadMemberLevels = async () => {
      loading.value = true
      try {
        const response = await request.get('/member-level/all')
        if (response.code === 200 && response.data) {
          memberLevels.value = response.data
        }
      } catch (error) {
        console.error('加载会员等级失败:', error)
        ElMessage.error('加载会员等级失败')
      } finally {
        loading.value = false
      }
    }

    const loadMemberStats = async () => {
      try {
        const response = await request.get('/member-level/stats')
        if (response.code === 200 && response.data) {
          const data = response.data
          stats.value = [
            { label: '总会员数', value: data.totalMembers || 0 },
            { label: '活跃会员', value: data.activeMembers || 0 },
            { label: '本月新增', value: data.newMembersThisMonth || 0 }
          ]
        }
      } catch (error) {
        console.error('加载会员统计失败:', error)
      }
    }

    const showCreateDialog = () => {
      editingLevel.value = null
      levelForm.value = {
        levelName: '',
        minExperience: 0,
        discountRate: 1.0,
        benefits: '',
        icon: '',
        color: '#409EFF'
      }
      dialogVisible.value = true
    }

    const handleEdit = (level) => {
      editingLevel.value = level
      levelForm.value = { ...level }
      dialogVisible.value = true
    }

    const handleSubmit = async () => {
      if (!levelFormRef.value) return

      try {
        await levelFormRef.value.validate()

        if (editingLevel.value && editingLevel.value.id) {
          // 更新
          await request.put(`/member-level/${editingLevel.value.id}`, levelForm.value)
          ElMessage.success('更新成功')
        } else {
          // 创建
          await request.post('/member-level', levelForm.value)
          ElMessage.success('创建成功')
        }

        dialogVisible.value = false
        loadMemberLevels()
        loadMemberStats()
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error('保存失败')
      }
    }

    const handleDelete = async (level) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除会员等级"${level.levelName}"吗？此操作不可恢复！`,
          '警告',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'error'
          }
        )

        await request.delete(`/member-level/${level.id}`)
        ElMessage.success('删除成功')
        loadMemberLevels()
        loadMemberStats()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败')
        }
      }
    }

    onMounted(() => {
      loadMemberLevels()
      loadMemberStats()
    })

    return {
      loading,
      memberLevels,
      stats,
      dialogVisible,
      editingLevel,
      levelFormRef,
      levelForm,
      levelRules,
      showCreateDialog,
      handleEdit,
      handleSubmit,
      handleDelete
    }
  }
}
</script>

<style scoped>
.member-management {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stats-row {
  margin-bottom: 0;
}

.stat-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
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
  font-size: 28px;
  font-weight: 700;
}

.level-card {
  flex: 1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.color-preview {
  width: 40px;
  height: 24px;
  border-radius: 4px;
  border: 1px solid #ddd;
}

.form-tip {
  margin-left: 10px;
  color: #999;
  font-size: 12px;
}
</style>
