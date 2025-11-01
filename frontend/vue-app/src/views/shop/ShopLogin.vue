<template>
  <div class="shop-login">
    <div class="login-container">
      <div class="login-header">
        <h2>门店登录</h2>
        <p class="subtitle">汽车保养服务管理系统</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入门店账号"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <p>© 2024 汽车保养服务系统</p>
        <router-link to="/login" class="back-link">返回用户登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { shopAPI } from '@/api/shop'

const router = useRouter()
const loading = ref(false)
const loginFormRef = ref()

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入门店账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()
    loading.value = true

    const response = await shopAPI.login(loginForm)

    if (response.code === 200) {
      // 清除用户端token，避免冲突
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('userRole')
      localStorage.removeItem('userInfo')

      // 保存门店登录信息
      localStorage.setItem('shopToken', response.data.token)
      localStorage.setItem('shopInfo', JSON.stringify(response.data.shop))

      ElMessage.success('登录成功')
      // 强制刷新页面，确保App.vue重新计算hidePublicLayout
      window.location.href = '/shop/dashboard'
    } else {
      ElMessage.error(response.message || '登录失败')
    }
  } catch (error) {
    console.error('登录错误:', error)
    ElMessage.error(error.response?.data?.message || '登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.shop-login {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-container {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  color: #2c3e50;
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 8px;
}

.subtitle {
  color: #6c757d;
  font-size: 14px;
  margin: 0;
}

.login-form {
  margin-bottom: 20px;
}

.login-form .el-form-item {
  margin-bottom: 20px;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
}

.login-footer {
  text-align: center;
  border-top: 1px solid #eee;
  padding-top: 20px;
}

.login-footer p {
  color: #6c757d;
  font-size: 12px;
  margin: 0 0 10px 0;
}

.back-link {
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
}

.back-link:hover {
  color: #337ecc;
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .shop-login {
    padding: 10px;
  }

  .login-container {
    padding: 30px 20px;
  }

  .login-header h2 {
    font-size: 24px;
  }
}
</style>