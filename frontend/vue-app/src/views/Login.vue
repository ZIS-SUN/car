<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>用户登录</h2>
        <p>欢迎回到汽车保养预约系统</p>
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
            placeholder="用户名/邮箱"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            size="large"
            :prefix-icon="Lock"
            show-password
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
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <p>还没有账号？<router-link to="/register" class="register-link">立即注册</router-link></p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { authAPI } from '../api/index.js'

export default {
  name: 'Login',
  components: {
    User,
    Lock
  },
  setup() {
    const router = useRouter()
    const loginFormRef = ref()
    const loading = ref(false)

    const loginForm = reactive({
      username: '',
      password: ''
    })

    const loginRules = {
      username: [
        { required: true, message: '请输入用户名或邮箱', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
      ]
    }

    // 解析JWT token获取用户信息
    const parseJWT = (token) => {
      try {
        const base64Url = token.split('.')[1]
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => {
          return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
        }).join(''))
        return JSON.parse(jsonPayload)
      } catch (e) {
        console.error('Failed to parse JWT:', e)
        return null
      }
    }

    const handleLogin = async () => {
      if (!loginFormRef.value) return

      try {
        await loginFormRef.value.validate()
        loading.value = true

        // 调用真实的登录API
        const response = await authAPI.login(loginForm)

        if (response.code === 200 && response.data.token) {
          const token = response.data.token

          // 解析JWT获取用户信息
          const payload = parseJWT(token)

          if (payload) {
            // 清除门店端token，避免冲突
            localStorage.removeItem('shopToken')
            localStorage.removeItem('shopInfo')

            // 保存token和用户信息
            localStorage.setItem('token', token)
            localStorage.setItem('username', payload.sub || loginForm.username)

            // 根据userType设置userRole
            let userRole = 'customer'
            if (payload.userType === 3) {
              userRole = 'admin'
            } else if (payload.userType === 2) {
              userRole = 'shop'
              // 如果是门店用户错误登录到用户端，提示并跳转
              ElMessage.warning('门店账号请使用门店端登录')
              setTimeout(() => {
                window.location.href = '/shop/login'
              }, 1500)
              return
            }
            localStorage.setItem('userRole', userRole)

            ElMessage.success('登录成功')

            // 触发登录事件，通知其他组件更新状态
            window.dispatchEvent(new Event('userLoggedIn'))

            // 根据角色跳转
            if (userRole === 'admin') {
              router.push('/admin')
            } else {
              router.push('/')
            }
          }
        }

        loading.value = false

      } catch (error) {
        console.error('登录失败:', error)
        loading.value = false
        ElMessage.error('登录失败，请检查用户名和密码')
      }
    }

    return {
      loginFormRef,
      loginForm,
      loginRules,
      loading,
      handleLogin,
      User,
      Lock
    }
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-box {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
  width: 100%;
  max-width: 400px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  color: #2c3e50;
  margin-bottom: 10px;
  font-size: 1.8rem;
}

.login-header p {
  color: #7f8c8d;
  font-size: 0.95rem;
}

.login-form {
  margin-bottom: 20px;
}

.login-btn {
  width: 100%;
  padding: 12px 0;
  font-size: 1rem;
}

.login-footer {
  text-align: center;
}

.register-link {
  color: #409eff;
  text-decoration: none;
}

.register-link:hover {
  text-decoration: underline;
}
</style>