<template>
  <div class="register-page">
    <div class="register-container">
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

              <div class="form-row">
                <el-form-item label="USM Email" prop="usmEmail" class="form-item">
                  <el-input
                      v-model="form.usmEmail"
                      placeholder="your.email@student.usm.my"
                      type="email"
                  />
                </el-form-item>

                <el-form-item label="Verification Code" prop="emailCode" class="form-item">
                  <div style="display: flex; gap: 10px;">
                    <el-input
                        v-model="form.emailCode"
                        placeholder="6-digit code"
                        maxlength="6"
                    />
                    <el-button 
                      type="primary" 
                      @click="sendVerificationCode" 
                      :disabled="countdown > 0"
                      :loading="sendingCode"
                      style="min-width: 120px;"
                    >
                      {{ countdown > 0 ? `${countdown}s` : 'Send Code' }}
                    </el-button>
                  </div>
                </el-form-item>
              </div>
              <p class="form-hint" style="margin-top: -10px; margin-bottom: 20px;">Must be a valid USM email address ending with @student.usm.my</p>
            </div>

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

                <el-form-item label="School" prop="school">
                  <el-select 
                    v-model="form.school" 
                    placeholder="Please select your school" 
                    style="width: 100%" 
                    clearable
                    filterable
                  >
                    <el-option
                      v-for="item in schoolList"
                      :key="item"
                      :label="item"
                      :value="item"
                    />
                  </el-select>
                </el-form-item>
              </div>

              <div class="form-row">
                <el-form-item label="Student ID" prop="studentId" class="form-item">
                  <el-input
                      v-model="form.studentId"
                      placeholder="Enter your student ID"
                      maxlength="20"
                  />
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

            <div class="form-actions">
              <el-checkbox v-model="agreeTerms" class="terms-checkbox">
                I agree to the <a href="#" class="link">Terms of Service</a> and <a href="#" class="link">Privacy Policy</a>
              </el-checkbox>

              <el-button
                  type="primary"
                  size="large"
                  @click="dialogVisible = true" 
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

  <el-dialog
      v-model="dialogVisible"
      title="Please Confirm Your Information"
      width="400px"
    >
      <div style="line-height: 1.8; font-size: 16px;">
        <p><strong>Account:</strong> {{ form.userAccount }}</p>
        <p><strong>School:</strong> {{ form.school }}</p>
        <p><strong>Email:</strong> {{ form.usmEmail }}</p>
        <p style="color: #E6A23C; margin-top: 15px; font-size: 14px;">
          * Please ensure all information is correct before proceeding.
        </p>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="handleRegister" :loading="loading">
            Confirm & Register
          </el-button>
        </span>
      </template>
    </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Check, User } from '@element-plus/icons-vue'
import { userRegister, sendEmailCode } from '@/api/user' // 👇 确保你的 api/user.js 导出了 sendEmailCode

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const agreeTerms = ref(false)
const dialogVisible = ref(false)

// 👇 验证码相关的状态
const sendingCode = ref(false)
const countdown = ref(0)

const form = reactive({
  username: '',
  userAccount: '',
  userPassword: '',
  checkPassword: '',
  usmEmail: '',
  emailCode: '', // 👇 新增验证码字段
  campus: '',
  studentId: '',
  school: '',
  phone: ''
})

// 验证规则
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
  usmEmail: [
    { required: true, message: 'Please enter USM email', trigger: 'blur' },
    { type: 'email', message: 'Please enter valid email address', trigger: 'blur' },
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
  // 👇 新增验证码的必填规则
  emailCode: [
    { required: true, message: 'Please enter the verification code', trigger: 'blur' },
    { len: 6, message: 'Code must be 6 digits', trigger: 'blur' }
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
  campus: [
    { required: true, message: 'Please select campus', trigger: 'change' }
  ],
  school: [
    { required: true, message: 'Please enter school/faculty', trigger: 'change' }
  ],
  studentId: [
    { required: true, message: 'Please enter your Student ID', trigger: 'blur' },
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

const schoolList = [
  'School of Computer Sciences',
  'School of Management',
  'School of Communication',
  'School of Biological Sciences',
  'School of Physics',
  'School of Chemical Sciences',
  'School of Mathematical Sciences',
  'School of Educational Studies',
  'School of Languages, Literacies and Translation',
  'School of Arts',
  'School of Humanities',
  'School of Social Sciences',
  'Other (Others)'
]

// 👇 新增方法：发送验证码逻辑
const sendVerificationCode = async () => {
  // 1. 先验证邮箱格式是否正确
  if (!form.usmEmail) {
    ElMessage.warning('Please enter your USM email first')
    return
  }
  if (!form.usmEmail.toLowerCase().endsWith('.usm.my')) {
    ElMessage.warning('Only USM emails ending with @usm.my are allowed')
    return
  }

  // 2. 发送请求
  try {
    sendingCode.value = true
    await sendEmailCode(form.usmEmail) // 调用后端接口
    ElMessage.success('Verification code sent! Please check your email inbox.')
    
    // 3. 开启 60 秒倒计时防刷
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)

  } catch (error) {
    ElMessage.error(error.message || 'Failed to send verification code. Please try again later.')
  } finally {
    sendingCode.value = false
  }
}

const handleRegister = async () => {
  try {
    loading.value = true
    await formRef.value.validate()
    
    const requestData = {
      ...form, 
      userName: form.username,
      phone: form.phone ? `+60${form.phone}` : null
    }

    await userRegister(requestData)

    dialogVisible.value = false
    ElMessage.success('Registration successful! Please sign in.')
    router.push('/login')

  } catch (error) {
    console.error('Register Error:', error)
    if (error.errorFields) {
      ElMessage.error('Please fill in all required fields correctly')
    } else {
      ElMessage.error(error.message || 'Registration failed. Please try again.')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 样式部分保持不变，可以直接保留你原有的 CSS */
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
  width: 100%;
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  min-height: 800px;
}

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