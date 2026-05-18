<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h2>Campus Swap</h2>
        <p>Centralized Second-Hand Marketplace for USM Community</p>
      </div>

      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-position="top">
        <el-form-item label="Student ID / Email" prop="userEmail">
          <el-input 
            v-model="loginForm.userEmail" 
            placeholder="Please enter your student ID or email"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item label="Password" prop="userPassword">
          <el-input 
            v-model="loginForm.userPassword" 
            type="password" 
            placeholder="Please enter password"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <div class="form-options">
          <el-checkbox v-model="rememberMe">Remember me</el-checkbox>
          <el-link type="primary" :underlined="false" @click="handleForgotPassword">Forgot password?</el-link>
        </div>

        <el-form-item class="submit-item">
          <el-button 
            type="primary" 
            class="w-full login-btn" 
            :loading="loading" 
            @click="handleLogin"
          >
            Sign In
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <span>Don't have an account? </span>
        <el-link type="primary" :underlined="false" @click="router.push('/register')">Create one now</el-link>
      </div>

      <hr class="divider-line" />
      
      <div class="admin-portal-wrapper">
        <button class="admin-portal-btn" @click="openAdminVerify">
          <el-icon><Avatar /></el-icon> Admin Portal Access
        </button>
      </div>
    </div>

    <el-dialog
      v-model="adminDialogVisible"
      title="Admin Security Authentication"
      width="400px"
      append-to-body
      destroy-on-close
    >
      <el-form :model="adminForm" :rules="adminRules" ref="adminFormRef" label-position="top">
        <el-form-item label="Admin Account (USM Email)" prop="adminEmail">
          <el-input v-model="adminForm.adminEmail" placeholder="Enter admin email" prefix-icon="User" />
        </el-form-item>
        <el-form-item label="Security Password" prop="adminPassword">
          <el-input 
            v-model="adminForm.adminPassword" 
            type="password" 
            placeholder="Enter security token" 
            prefix-icon="Lock" 
            show-password
            @keyup.enter="handleAdminLogin"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="adminDialogVisible = false">Cancel</el-button>
          <el-button type="info" :loading="adminLoading" @click="handleAdminLogin">Verify & Enter</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Avatar } from '@element-plus/icons-vue'
import myAxios from "@/plugins/request.js"
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)
const loginForm = ref({ userEmail: '', userPassword: '' })

const adminDialogVisible = ref(false)
const adminLoading = ref(false)
const adminFormRef = ref(null)
const adminForm = ref({ adminEmail: '', adminPassword: '' })

const rules = {
  userEmail: [{ required: true, message: 'Account cannot be empty', trigger: 'blur' }],
  userPassword: [{ required: true, message: 'Password cannot be empty', trigger: 'blur' }]
}

const adminRules = {
  adminEmail: [{ required: true, message: 'Admin email required', trigger: 'blur' }],
  adminPassword: [{ required: true, message: 'Security password required', trigger: 'blur' }]
}

// 🎓 1. 普通学生登录逻辑
const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await myAxios.post('/user/login', {
        usmEmail: loginForm.value.userEmail,
        userPassword: loginForm.value.userPassword
      })
      const userInfo = res.data || res
      if (userInfo) {
        // 🚀 拦截：防止管理员走错门。如果用普通登录框登了管理员号，直接送去后台！
        if (userInfo.userRole === 1) {
          ElMessage.success('Welcome back, Administrator!')
          localStorage.setItem('user', JSON.stringify(userInfo))
          if (userStore?.setUserInfo) userStore.setUserInfo(userInfo)
          router.push('/admin/dashboard') // 直飞后台！
          return
        }

        ElMessage.success('Login Successful!')
        localStorage.setItem('user', JSON.stringify(userInfo))
        if (userStore?.setUserInfo) userStore.setUserInfo(userInfo)
        router.push('/') // 普通学生去商城首页
      }
    } catch (error) {
      ElMessage.error(error.message || 'Authentication failed!')
    } finally {
      loading.value = false
    }
  })
}

// 打开管理员验证弹窗
const openAdminVerify = () => {
  adminForm.value = { adminEmail: '', adminPassword: '' }
  adminDialogVisible.value = true
}

// 🛡️ 2. 管理员专属登录逻辑（100% 独立跳级）
const handleAdminLogin = async () => {
  if (!adminFormRef.value) return
  await adminFormRef.value.validate(async (valid) => {
    if (!valid) return
    adminLoading.value = true
    try {
      const res = await myAxios.post('/user/login', {
        usmEmail: adminForm.value.adminEmail,
        userPassword: adminForm.value.adminPassword
      })
      const adminInfo = res.data || res
      
      if (adminInfo) {
        // 严格查验身份：只有 userRole == 1 才是真管理员
        if (adminInfo.userRole === 1 || adminInfo.role === 'admin' || adminInfo.role === 1) {
          ElMessage.success('Admin Authentication Passed! Redirecting to Core Portal...')
          localStorage.setItem('user', JSON.stringify(adminInfo))
          if (userStore?.setUserInfo) userStore.setUserInfo(adminInfo)
          
          adminDialogVisible.value = false
          // 🚀 核心修改：绝对不碰主页，瞬间空降去纯净黑色后台！
          router.push('/admin/dashboard') 
        } else {
          ElMessage.error('Access Denied: Your account does not have admin privileges!')
        }
      }
    } catch (error) {
      console.error(error)
      ElMessage.error(error.message || 'Invalid Admin Credentials!')
    } finally {
      adminLoading.value = false
    }
  })
}

const handleForgotPassword = () => {
  ElMessage.info('Please contact campus IT support to reset your password.')
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%); 
}

.login-card {
  width: 420px;
  background: white;
  padding: 40px;
  border-radius: 16px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  font-size: 26px;
  color: #111827;
  margin: 0 0 10px 0;
  font-weight: 700;
}

.login-header p {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
  line-height: 1.4;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.w-full {
  width: 100%;
}

.login-btn {
  padding: 12px 0;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
}

.submit-item {
  margin-top: 10px;
  margin-bottom: 20px;
}

.login-footer {
  text-align: center;
  font-size: 14px;
  color: #4b5563;
  margin-bottom: 20px;
}

.divider-line {
  border: none;
  border-top: 1px dashed #e5e7eb;
  margin: 20px 0;
}

.admin-portal-wrapper {
  display: flex;
  justify-content: center;
}

.admin-portal-btn {
  width: 100%;
  padding: 10px;
  background-color: #374151; 
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: background-color 0.3s;
}

.admin-portal-btn:hover {
  background-color: #1f2937; 
}
</style>