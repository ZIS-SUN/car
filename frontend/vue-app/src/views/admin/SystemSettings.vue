<template>
  <div class="system-settings">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>系统配置</span>
          <el-button type="primary" @click="handleSave">保存设置</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" type="border-card">
        <!-- 基础设置 -->
        <el-tab-pane label="基础设置" name="basic">
          <el-form :model="configs" label-width="150px" class="settings-form">
            <el-form-item label="系统名称">
              <el-input v-model="configs.systemName" placeholder="请输入系统名称" />
            </el-form-item>
            <el-form-item label="系统版本">
              <el-input v-model="configs.systemVersion" placeholder="请输入系统版本" />
            </el-form-item>
            <el-form-item label="公司名称">
              <el-input v-model="configs.companyName" placeholder="请输入公司名称" />
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="configs.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
            <el-form-item label="联系邮箱">
              <el-input v-model="configs.contactEmail" placeholder="请输入联系邮箱" />
            </el-form-item>
            <el-form-item label="公司地址">
              <el-input v-model="configs.companyAddress" type="textarea" :rows="2" placeholder="请输入公司地址" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 业务设置 -->
        <el-tab-pane label="业务设置" name="business">
          <el-form :model="configs" label-width="180px" class="settings-form">
            <el-form-item label="默认预约时长（分钟）">
              <el-input-number v-model.number="configs.defaultAppointmentDuration" :min="30" :step="30" />
            </el-form-item>
            <el-form-item label="预约提前天数">
              <el-input-number v-model.number="configs.appointmentAdvanceDays" :min="1" :max="30" />
            </el-form-item>
            <el-form-item label="取消订单时限（小时）">
              <el-input-number v-model.number="configs.cancelOrderTimeLimit" :min="1" :max="72" />
            </el-form-item>
            <el-form-item label="订单超时时间（小时）">
              <el-input-number v-model.number="configs.orderTimeoutHours" :min="1" :max="168" />
            </el-form-item>
            <el-form-item label="自动完成订单（天）">
              <el-input-number v-model.number="configs.autoCompleteOrderDays" :min="1" :max="30" />
              <span class="form-tip">订单服务完成后，自动设置为已完成的天数</span>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 会员设置 -->
        <el-tab-pane label="会员设置" name="member">
          <el-form :model="configs" label-width="180px" class="settings-form">
            <el-form-item label="会员经验获取比例">
              <el-input-number v-model.number="configs.experienceRatio" :min="0.1" :max="10" :step="0.1" :precision="1" />
              <span class="form-tip">每消费1元获得的经验值</span>
            </el-form-item>
            <el-form-item label="启用会员系统">
              <el-switch v-model="configs.enableMemberSystem" />
            </el-form-item>
            <el-form-item label="启用积分系统">
              <el-switch v-model="configs.enablePointsSystem" />
            </el-form-item>
            <el-form-item label="积分抵扣比例">
              <el-input-number v-model.number="configs.pointsDeductionRatio" :min="0" :max="1" :step="0.01" :precision="2" />
              <span class="form-tip">0-1之间，表示积分可抵扣金额的比例</span>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 通知设置 -->
        <el-tab-pane label="通知设置" name="notification">
          <el-form :model="configs" label-width="180px" class="settings-form">
            <el-form-item label="启用邮件通知">
              <el-switch v-model="configs.enableEmailNotification" />
            </el-form-item>
            <el-form-item label="启用短信通知">
              <el-switch v-model="configs.enableSmsNotification" />
            </el-form-item>
            <el-form-item label="SMTP服务器">
              <el-input v-model="configs.smtpServer" placeholder="例如：smtp.example.com" />
            </el-form-item>
            <el-form-item label="SMTP端口">
              <el-input-number v-model.number="configs.smtpPort" :min="1" :max="65535" />
            </el-form-item>
            <el-form-item label="发件人邮箱">
              <el-input v-model="configs.senderEmail" placeholder="请输入发件人邮箱" />
            </el-form-item>
            <el-form-item label="短信API Key">
              <el-input v-model="configs.smsApiKey" type="password" show-password placeholder="请输入短信API Key" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 支付设置 -->
        <el-tab-pane label="支付设置" name="payment">
          <el-form :model="configs" label-width="180px" class="settings-form">
            <el-form-item label="启用微信支付">
              <el-switch v-model="configs.enableWechatPay" />
            </el-form-item>
            <el-form-item label="启用支付宝支付">
              <el-switch v-model="configs.enableAlipay" />
            </el-form-item>
            <el-form-item label="微信支付商户号">
              <el-input v-model="configs.wechatMerchantId" placeholder="请输入微信支付商户号" />
            </el-form-item>
            <el-form-item label="微信支付API密钥">
              <el-input v-model="configs.wechatApiKey" type="password" show-password placeholder="请输入微信支付API密钥" />
            </el-form-item>
            <el-form-item label="支付宝应用ID">
              <el-input v-model="configs.alipayAppId" placeholder="请输入支付宝应用ID" />
            </el-form-item>
            <el-form-item label="支付宝私钥">
              <el-input v-model="configs.alipayPrivateKey" type="textarea" :rows="3" placeholder="请输入支付宝私钥" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/index.js'

export default {
  name: 'SystemSettings',
  setup() {
    const loading = ref(false)
    const activeTab = ref('basic')
    const configs = ref({
      // 基础设置
      systemName: '汽车保养预约系统',
      systemVersion: '1.0.0',
      companyName: '',
      contactPhone: '',
      contactEmail: '',
      companyAddress: '',

      // 业务设置
      defaultAppointmentDuration: 60,
      appointmentAdvanceDays: 7,
      cancelOrderTimeLimit: 24,
      orderTimeoutHours: 24,
      autoCompleteOrderDays: 7,

      // 会员设置
      experienceRatio: 1.0,
      enableMemberSystem: true,
      enablePointsSystem: true,
      pointsDeductionRatio: 0.01,

      // 通知设置
      enableEmailNotification: false,
      enableSmsNotification: false,
      smtpServer: '',
      smtpPort: 587,
      senderEmail: '',
      smsApiKey: '',

      // 支付设置
      enableWechatPay: false,
      enableAlipay: false,
      wechatMerchantId: '',
      wechatApiKey: '',
      alipayAppId: '',
      alipayPrivateKey: ''
    })

    const loadConfigs = async () => {
      loading.value = true
      try {
        const response = await request.get('/admin/configs')
        if (response.code === 200 && response.data) {
          // 合并配置，将字符串转换为对应的类型
          Object.keys(response.data).forEach(key => {
            const value = response.data[key]
            // 处理布尔值
            if (value === 'true' || value === 'false') {
              configs.value[key] = value === 'true'
            }
            // 处理数字
            else if (!isNaN(value) && value !== '') {
              configs.value[key] = Number(value)
            }
            // 字符串
            else {
              configs.value[key] = value
            }
          })
        }
      } catch (error) {
        console.error('加载系统配置失败:', error)
        ElMessage.error('加载系统配置失败')
      } finally {
        loading.value = false
      }
    }

    const handleSave = async () => {
      loading.value = true
      try {
        // 逐个保存配置项
        const savePromises = Object.keys(configs.value).map(key => {
          const value = String(configs.value[key])
          return request.put('/admin/configs', null, {
            params: {
              configKey: key,
              configValue: value
            }
          })
        })

        await Promise.all(savePromises)
        ElMessage.success('保存成功')
      } catch (error) {
        console.error('保存配置失败:', error)
        ElMessage.error('保存配置失败')
      } finally {
        loading.value = false
      }
    }

    onMounted(() => {
      loadConfigs()
    })

    return {
      loading,
      activeTab,
      configs,
      handleSave
    }
  }
}
</script>

<style scoped>
.system-settings {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.settings-form {
  max-width: 800px;
  padding: 20px;
}

.form-tip {
  margin-left: 10px;
  color: #999;
  font-size: 12px;
}

:deep(.el-tabs--border-card) {
  box-shadow: none;
  border: none;
}

:deep(.el-tabs__content) {
  padding: 20px;
}
</style>
