<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-left">
        <div class="welcome-content">
          <h1>Welcome Back</h1>
          <p>Sign in to continue trading with USM Campus Swap</p>
          <div class="features">
            <div class="feature-item">
              <el-icon><Check /></el-icon>
              <span>Access your products</span>
            </div>
            <div class="feature-item">
              <el-icon><Check /></el-icon>
              <span>Manage your orders</span>
            </div>
            <div class="feature-item">
              <el-icon><Check /></el-icon>
              <span>Connect with buyers</span>
            </div>
          </div>
        </div>
      </div>

      <div class="login-right">
        <div class="form-container">
          <div class="form-header">
            <h2>Sign In</h2>
            <p>Don't have an account? <router-link to="/register">Create one</router-link></p>
          </div>

          <el-form
              :model="form"
              :rules="rules"
              ref="formRef"
              label-width="0"
              class="login-form"
              size="large"
          >
            <el-form-item prop="usmEmail" class="form-item">
              <el-input
                  v-model="form.usmEmail"
                  placeholder="your.email@student.usm.my"
                  :prefix-icon="Message"
                  @keyup.enter="handleLogin"
              />
            </el-form-item>

            <el-form-item prop="userPassword" class="form-item">
              <el-input
                  v-model="form.userPassword"
                  placeholder="Enter your password"
                  type="password"
                  show-password
                  :prefix-icon="Lock"
                  @keyup.enter="handleLogin"
              />
            </el-form-item>

            <el-button
                type="primary"
                size="large"
                @click="handleLogin"
                :loading="loading"
                class="login-btn"
            >
              Sign In
            </el-button>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Check, User, Lock, Message } from '@element-plus/icons-vue' // 引入了 Message 图标
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  //userAccount: '',
  usmEmail: '',
  userPassword: ''
})

const rules = reactive({
  // 👇 把 userAccount 的校验换成了 usmEmail 专属校验
  usmEmail: [
    { required: true, message: 'Please enter your USM email', trigger: 'blur' },
    { type: 'email', message: 'Please enter a valid email address', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value && !value.toLowerCase().endsWith('.usm.my')) {
          callback(new Error('Please use USM email (@usm.my) to login'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  userPassword: [
    { required: true, message: 'Please enter your password', trigger: 'blur' },
    { min: 8, message: 'Password must be at least 8 characters', trigger: 'blur' }
  ]
})

const handleLogin = async () => {
  if (!formRef.value) return

  try {
    loading.value = true

    // 表单校验
    await formRef.value.validate()

    // 调用 store 登录
    const userData = await userStore.login(form)

    // 成功 (把备用的 userAccount 换成了 usmEmail)
    const username = userData.username || userData.usmEmail || userData.name || form.usmEmail
    ElMessage.success(`Welcome back, ${username}!`)

    // 跳转首页
    router.push('/')
  } catch (error) {
    // 显示后端返回消息或网络错误
    ElMessage.error(error.message || 'Login failed. Please check your account and password.')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-container {
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  max-width: 1000px;
  width: 100%;
  display: grid;
  grid-template-columns: 1fr 1fr;
  min-height: 600px;
}

.login-left {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 50px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.welcome-content h1 {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 16px;
  line-height: 1.2;
}

.welcome-content p {
  font-size: 16px;
  margin-bottom: 30px;
  opacity: 0.9;
  line-height: 1.5;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}

.feature-item .el-icon {
  color: #10b981;
}

.login-right {
  padding: 50px 40px;
  display: flex;
  align-items: center;
}

.form-container {
  width: 100%;
  max-width: 350px;
  margin: 0 auto;
}

.form-header {
  text-align: center;
  margin-bottom: 30px;
}

.form-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 8px;
}

.form-header p {
  color: #6b7280;
  font-size: 14px;
}

.form-header a {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
}

.form-header a:hover {
  text-decoration: underline;
}

.login-form {
  width: 100%;
}

.form-item {
  margin-bottom: 20px;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  margin-bottom: 25px;
}

.login-btn:hover {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

:deep(.el-input__wrapper) {
  border-radius: 12px;
  border: 1px solid #d1d5db;
  transition: all 0.3s ease;
  padding: 0 15px;
}

:deep(.el-input__wrapper:hover) {
  border-color: #667eea;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

:deep(.el-form-item__error) {
  font-size: 12px;
}
</style>