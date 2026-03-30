<template>
  <div class="register-page">
    <div class="register-container">
      <!-- 左侧图片 -->
      <div class="register-left">
        <div class="welcome-content">
          <h1>Join USM Campus Swap</h1>
          <p>Start trading with your campus community today</p>
          <div class="features">
            <div class="feature-item">
              <el-icon><Check /></el-icon>
              <span>Buy and sell safely</span>
            </div>
            <div class="feature-item">
              <el-icon><Check /></el-icon>
              <span>Campus-only community</span>
            </div>
            <div class="feature-item">
              <el-icon><Check /></el-icon>
              <span>Instant communication</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧表单 -->
      <div class="register-right">
        <div class="form-container">
          <div class="form-header">
            <h2>Create Your Account</h2>
            <p>Already have an account? <router-link to="/login">Sign in</router-link></p>
          </div>

          <el-form
              :model="form"
              :rules="rules"
              ref="formRef"
              label-width="120px"
              label-position="top"
              class="register-form"
              size="large"
          >
            <!-- 基本信息 -->
            <div class="form-section">
              <h3 class="section-title">Account Information</h3>

              <div class="form-row">
                <el-form-item label="Account" prop="userAccount" class="form-item">
                  <el-input
                      v-model="form.userAccount"
                      placeholder="Enter your Account"
                      maxlength="20"
                  />
                  <p class="form-hint">4-20 characters, letters, numbers and underscores only</p>
                </el-form-item>

                <el-form-item label="User Name" prop="username" class="form-item">
                  <el-input
                      v-model="form.username"
                      placeholder="Enter your User name"
                      maxlength="20"
                      show-word-limit
                  />
                </el-form-item>
              </div>

              <el-form-item label="USM Email" prop="usmEmail" class="form-item">
                <el-input
                    v-model="form.usmEmail"
                    placeholder="your.email@student.usm.my"
                    type="email"
                />
                <p class="form-hint">Must be a valid USM email address ending with @usm.my</p>
              </el-form-item>
            </div>

            <!-- 密码设置 -->
            <div class="form-section">
              <h3 class="section-title">Security</h3>

              <div class="form-row">
                <el-form-item label="Password" prop="userPassword" class="form-item">
                  <el-input
                      v-model="form.userPassword"
                      placeholder="Enter your password"
                      type="password"
                      show-password
                  />
                  <p class="form-hint">At least 8 characters</p>
                </el-form-item>

                <el-form-item label="Confirm Password" prop="checkPassword" class="form-item">
                  <el-input
                      v-model="form.checkPassword"
                      placeholder="Confirm your password"
                      type="password"
                      show-password
                  />
                  <p class="form-hint">Re-enter your password</p>
                </el-form-item>
              </div>
            </div>

            <!-- 个人信息 -->
            <div class="form-section">
              <h3 class="section-title">Personal Information</h3>

              <div class="form-row">
                <el-form-item label="Campus" prop="campus" class="form-item">
                  <el-select
                      v-model="form.campus"
                      placeholder="Select your campus"
                      style="width: 100%"
                  >
                    <el-option label="Main Campus" value="Main Campus" />
                    <el-option label="Engineering Campus" value="Engineering Campus" />
                    <el-option label="Health Campus" value="Health Campus" />
                  </el-select>
                </el-form-item>

                <el-form-item label="School/Faculty" prop="school" class="form-item">
                  <el-input
                      v-model="form.school"
                      placeholder="e.g., School of Computer Sciences"
                  />
                </el-form-item>
              </div>

              <div class="form-row">
                <el-form-item label="Student ID" prop="studentId" class="form-item">
                  <el-input
                      v-model="form.studentId"
                      placeholder="Enter your student ID"
                      maxlength="20"
                  />
                  <p class="form-hint">Optional, but recommended for verification</p>
                </el-form-item>

                <el-form-item label="Phone Number" prop="phone" class="form-item">
                  <el-input
                      v-model="form.phone"
                      placeholder="123456789"
                      maxlength="10"
                  >
                    <template #prepend>+60</template>
                  </el-input>
                  <p class="form-hint">Malaysia number only (e.g., 123456789)</p>
                </el-form-item>
              </div>
            </div>

            <!-- 协议和注册按钮 -->
            <div class="form-actions">
              <el-checkbox v-model="agreeTerms" class="terms-checkbox">
                I agree to the <a href="#" class="link">Terms of Service</a> and <a href="#" class="link">Privacy Policy</a>
              </el-checkbox>

              <el-button
                  type="primary"
                  size="large"
                  @click="handleRegister"
                  :loading="loading"
                  class="register-btn"
                  :disabled="!agreeTerms"
              >
                <el-icon><User /></el-icon>
                Create Account
              </el-button>
            </div>
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
import { Check, User } from '@element-plus/icons-vue'
import { userRegister } from '@/api/user' // 1. 确保引入 API

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const agreeTerms = ref(false)

// 表单数据
const form = reactive({
  username: '',
  userAccount: '',
  userPassword: '',
  checkPassword: '',
  usmEmail: '',
  campus: '',
  studentId: '',
  school: '',
  phone: ''
})

// 验证规则 (保持不变)
const rules = reactive({
  username: [
    { required: true, message: 'Please enter your User name', trigger: 'blur' },
    { min: 1, max: 20, message: 'User name cannot exceed 20 characters', trigger: 'blur' }
  ],
  userAccount: [
    { required: true, message: 'Please enter account', trigger: 'blur' },
    { min: 4, message: 'Account must be at least 4 characters', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9_]+$/,
      message: 'Account can only contain letters, numbers and underscores',
      trigger: 'blur'
    }
  ],
  userPassword: [
    { required: true, message: 'Please enter password', trigger: 'blur' },
    { min: 8, message: 'Password must be at least 8 characters', trigger: 'blur' }
  ],
  checkPassword: [
    { required: true, message: 'Please confirm password', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.userPassword) {
          callback(new Error('Passwords do not match'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  usmEmail: [
    { required: true, message: 'Please enter USM email', trigger: 'blur' },
    {
      type: 'email',
      message: 'Please enter valid email address',
      trigger: 'blur'
    },
    {
      validator: (rule, value, callback) => {
        if (value && !value.toLowerCase().endsWith('.usm.my')) {
          callback(new Error('Please use USM email (@usm.my) to register'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  campus: [
    { required: true, message: 'Please select campus', trigger: 'change' }
  ],
  school: [
    { required: true, message: 'Please enter school/faculty', trigger: 'blur' }
  ],
  studentId: [
    {
      validator: (rule, value, callback) => {
        if (value && value.length < 6) {
          callback(new Error('Student ID must be at least 6 characters'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  phone: [
    {
      validator: (rule, value, callback) => {
        if (value) {
          if (value.length < 9 || value.length > 10) {
            callback(new Error('Phone number must be 9-10 digits'))
          } else if (!/^\d+$/.test(value)) {
            callback(new Error('Phone number can only contain numbers'))
          } else {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

const handleRegister = async () => {
  if (!formRef.value) return

  try {
    loading.value = true
    // 1. 校验表单
    await formRef.value.validate()

    // 2. 构造数据 (处理字段名对应和手机号前缀)
    // 这里的 form 是 reactive 对象，直接访问属性，不需要 .value
    const requestData = {
      ...form,
      userName: form.username,
      phone: form.phone ? `+60${form.phone}` : ''
    }

    // 3. 发送真实请求
    await userRegister(requestData)

    // 4. 成功处理
    ElMessage.success('Registration successful! Please sign in.')
    router.push('/login')

  } catch (error) {
    console.error('Register Error:', error)
    // 5. 错误处理
    if (error.errorFields) {
      ElMessage.error('Please fill in all required fields correctly')
    } else {
      // 显示后端返回的具体错误 (比如 "账号已存在")
      ElMessage.error(error.message || 'Registration failed. Please try again.')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.register-container {
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  max-width: 1200px;
  width: '100%';
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  min-height: 800px;
}

/* 左侧样式 */
.register-left {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 60px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.welcome-content h1 {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 16px;
  line-height: 1.2;
}

.welcome-content p {
  font-size: 18px;
  margin-bottom: 40px;
  opacity: 0.9;
  line-height: 1.5;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
}

.feature-item .el-icon {
  color: #10b981;
}

/* 右侧样式 */
.register-right {
  padding: 60px 50px;
  display: flex;
  align-items: center;
}

.form-container {
  width: 100%;
  max-width: 500px;
  margin: 0 auto;
}

.form-header {
  text-align: center;
  margin-bottom: 40px;
}

.form-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 8px;
}

.form-header p {
  color: #6b7280;
  font-size: 16px;
}

.form-header a {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
}

.form-header a:hover {
  text-decoration: underline;
}

.register-form {
  width: 100%;
}

.form-section {
  margin-bottom: 40px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f3f4f6;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.form-item {
  margin-bottom: 24px;
}

.form-item:last-child {
  margin-bottom: 0;
}

.form-hint {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 6px;
  margin-bottom: 0;
  line-height: 1.4;
}

/* 协议和按钮 */
.form-actions {
  margin-top: 40px;
  text-align: center;
}

.terms-checkbox {
  margin-bottom: 24px;
  display: block;
}

.link {
  color: #667eea;
  text-decoration: none;
}

.link:hover {
  text-decoration: underline;
}

.register-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.register-btn:hover {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.register-btn:disabled {
  background: #9ca3af;
  transform: none;
  box-shadow: none;
  cursor: not-allowed;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .register-container {
    grid-template-columns: 1fr;
    max-width: 600px;
  }

  .register-left {
    padding: 40px 30px;
    min-height: 300px;
  }

  .welcome-content h1 {
    font-size: 28px;
  }

  .welcome-content p {
    font-size: 16px;
  }
}

@media (max-width: 768px) {
  .register-page {
    padding: 10px;
  }

  .register-right {
    padding: 40px 30px;
  }

  .form-row {
    grid-template-columns: 1fr;
    gap: 0;
  }

  .form-header h2 {
    font-size: 24px;
  }

  .section-title {
    font-size: 16px;
  }
}

/* 自定义表单样式 */
:deep(.el-form-item__label) {
  font-weight: 600;
  color: #374151;
  margin-bottom: 6px;
  font-size: 14px;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid #d1d5db;
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  border-color: #667eea;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

:deep(.el-select .el-input__wrapper) {
  box-shadow: none;
}
</style>