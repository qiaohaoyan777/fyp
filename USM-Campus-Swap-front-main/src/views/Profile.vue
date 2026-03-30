<template>
  <div class="profile-page">
    <div class="page-header">
      <h1>Profile Settings</h1>
      <p>Manage your account information and preferences</p>
    </div>

    <div class="profile-container">
      <div class="left-column">
        <div class="user-card">
          <div class="avatar-section">
            <div class="avatar-wrapper">
              <el-avatar :size="120" :src="userInfo.avatarUrl" class="main-avatar">
                {{ userInfo.username?.charAt(0) || 'U' }}
              </el-avatar>

              <el-upload
                  class="avatar-uploader"
                  action="#"
                  :show-file-list="false"
                  :http-request="uploadAvatar"
                  accept="image/jpeg,image/png,image/webp"
              >
                <el-button class="upload-btn" circle :loading="uploading">
                  <el-icon><Camera /></el-icon>
                </el-button>
              </el-upload>
            </div>
            <h2 class="user-name">{{ userInfo.username || 'User' }}</h2>
            <p class="user-email">{{ userInfo.usmEmail || userInfo.userAccount || 'user@usm.my' }}</p>
            <div class="member-since">
              <el-icon><Calendar /></el-icon>
              Member since {{ formatDate(userInfo.createTime) || '2024' }}
            </div>
          </div>

          <div class="quick-actions">
            <el-button
                type="primary"
                @click="activeTab = 'profile'"
                :class="{ active: activeTab === 'profile' }"
                class="action-btn"
            >
              <el-icon><User /></el-icon>
              Personal Info
            </el-button>
            <el-button
                @click="activeTab = 'security'"
                :class="{ active: activeTab === 'security' }"
                class="action-btn"
            >
              <el-icon><Lock /></el-icon>
              Security
            </el-button>
          </div>
        </div>

        <div class="wallet-card">
          <div class="wallet-header">
            <h3><el-icon><Wallet /></el-icon> Wallet Balance</h3>
            <el-tag v-if="userInfo.emailVerified === 1" type="success" size="small">
              USM Verified
            </el-tag>
            <el-tag v-else type="danger" size="small">
              Unverified Account
            </el-tag>
          </div>
          <div class="wallet-balance">
            <div class="balance-amount">RM {{ (userInfo.balance || 0).toFixed(2) }}</div>
            <div class="balance-label">Available Balance</div>
          </div>
          <div class="wallet-actions">
            <el-button type="primary" @click="showTopUpDialog = true">
              <el-icon><Plus /></el-icon>
              Top Up
            </el-button>
            <el-button @click="$router.push('/orders')">
              <el-icon><List /></el-icon>
              Transaction History
            </el-button>
          </div>
        </div>

        <div class="status-card">
          <h3>Account Status</h3>
          <div class="status-items">
            <div class="status-item">
              <span class="label">Account Status:</span>
              <el-tag :type="getStatusType(userInfo.userStatus)" size="small">
                {{ getUserStatus(userInfo.userStatus) }}
              </el-tag>
            </div>
            <div class="status-item">
              <span class="label">User Role:</span>
              <span class="value">{{ getUserRole(userInfo.userRole) }}</span>
            </div>
            <div class="status-item">
              <span class="label">Student ID:</span>
              <span class="value">{{ userInfo.studentId || 'Not set' }}</span>
            </div>
            <div class="status-item">
              <span class="label">School:</span>
              <span class="value">{{ userInfo.school || 'Not set' }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="right-column">
        <div v-show="activeTab === 'profile'" class="tab-content">
          <div class="section-title">
            <h2>Personal Information</h2>
            <p>Update your personal details</p>
          </div>

          <el-form
              ref="profileFormRef"
              :model="profileForm"
              :rules="profileRules"
              label-width="140px"
              class="profile-form"
          >
            <el-form-item label="Username" prop="username">
              <el-input
                  v-model="profileForm.username"
                  placeholder="Enter your username"
                  :prefix-icon="User"
              />
              <div class="form-tip">This is your display name</div>
            </el-form-item>

            <el-form-item label="Email" prop="usmEmail">
              <el-input
                  v-model="profileForm.usmEmail"
                  placeholder="Enter your USM email"
                  :prefix-icon="Message"
                  :disabled="true"
              >
                <template #append>
                  <el-tag v-if="profileForm.emailVerified === 1" type="success" size="small">
                    USM Verified
                  </el-tag>
                  <el-tag v-else type="danger" size="small">
                    Not a USM Account
                  </el-tag>
                </template>
              </el-input>
              <div class="form-tip">Email cannot be changed</div>
            </el-form-item>

            <el-form-item label="Phone" prop="phone">
              <el-input
                  v-model="profileForm.phone"
                  placeholder="Enter your phone number"
                  :prefix-icon="Phone"
              />
            </el-form-item>

            <el-form-item label="Gender" prop="gender">
              <el-radio-group v-model="profileForm.gender">
                <el-radio :label="0">Unknown</el-radio>
                <el-radio :label="1">Male</el-radio>
                <el-radio :label="2">Female</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="Campus" prop="campus">
              <el-select
                  v-model="profileForm.campus"
                  placeholder="Select campus"
                  style="width: 100%"
              >
                <el-option label="Main Campus" value="Main Campus" />
                <el-option label="Engineering Campus" value="Engineering Campus" />
                <el-option label="Health Campus" value="Health Campus" />
                <el-option label="Other" value="Other" />
              </el-select>
            </el-form-item>

            <el-form-item label="Student ID" prop="studentId">
              <el-input
                  v-model="profileForm.studentId"
                  placeholder="Enter your student ID"
                  :prefix-icon="CreditCard"
              />
            </el-form-item>

            <el-form-item label="School" prop="school">
              <el-input
                  v-model="profileForm.school"
                  placeholder="Enter your school/faculty"
                  :prefix-icon="School"
              />
            </el-form-item>

            <el-form-item label="Delivery Address" prop="address">
              <el-input
                  v-model="profileForm.address"
                  type="textarea"
                  :rows="3"
                  placeholder="Enter your default delivery address"
                  :prefix-icon="Location"
                  maxlength="200"
                  show-word-limit
              />
            </el-form-item>

            <div class="form-actions" style="text-align: center;">
              <el-button type="primary" @click="saveProfile" :loading="saving">
                Save Changes
              </el-button>
              <el-button @click="resetProfile">Reset</el-button>
            </div>
          </el-form>
        </div>

        <div v-show="activeTab === 'security'" class="tab-content">
          <div class="section-title">
            <h2>Security Settings</h2>
            <p>Manage your password and security</p>
          </div>

          <div class="security-section">
            <h3>Change Password</h3>
            <el-form
                ref="passwordFormRef"
                :model="passwordForm"
                :rules="passwordRules"
                label-width="160px"
                class="password-form"
            >
              <el-form-item label="Current Password" prop="currentPassword">
                <el-input
                    v-model="passwordForm.currentPassword"
                    type="password"
                    placeholder="Enter current password"
                    show-password
                />
              </el-form-item>

              <el-form-item label="New Password" prop="newPassword">
                <el-input
                    v-model="passwordForm.newPassword"
                    type="password"
                    placeholder="Enter new password"
                    show-password
                />
                <div class="form-tip">Password must be at least 6 characters</div>
              </el-form-item>

              <el-form-item label="Confirm Password" prop="confirmPassword">
                <el-input
                    v-model="passwordForm.confirmPassword"
                    type="password"
                    placeholder="Confirm new password"
                    show-password
                />
              </el-form-item>

              <div class="form-actions" style="text-align: center;">
                <el-button type="primary" @click="changePassword" :loading="changingPassword">
                  Update Password
                </el-button>
              </div>
            </el-form>

            <h3 style="margin-top: 40px;">Account Security</h3>
            <div class="security-preferences">
              <div class="preference-item">
                <div class="preference-info">
                  <h4>University Verification</h4>
                  <p>
                    This account is
                    <strong>
                      {{ profileForm.emailVerified === 1 ? 'verified by USM email' : 'not verified' }}
                    </strong>
                  </p>
                </div>
              </div>

            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog
        v-model="showTopUpDialog"
        title="Top Up Wallet"
        width="400px"
    >
      <div class="top-up-dialog">
        <div class="amount-selection">
          <div
              v-for="amount in topUpAmounts"
              :key="amount"
              class="amount-option"
              :class="{ active: selectedAmount === amount }"
              @click="selectedAmount = amount"
          >
            RM {{ amount }}
          </div>
        </div>

        <div class="custom-amount">
          <el-input-number
              v-model="customAmount"
              :min="10"
              :max="1000"
              :step="10"
              placeholder="Custom amount"
              style="width: 100%"
          />
        </div>

        <div class="payment-methods">
          <h4>Payment Method</h4>
          <el-select v-model="paymentMethod" placeholder="Select payment method" style="width: 100%">
            <el-option label="Credit/Debit Card" value="card" />
            <el-option label="Online Banking" value="banking" />
            <el-option label="Touch 'n Go eWallet" value="tng" />
            <el-option label="GrabPay" value="grabpay" />
          </el-select>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showTopUpDialog = false">Cancel</el-button>
          <el-button type="primary" @click="processTopUp" :loading="toppingUp">
            Proceed to Pay RM {{ (customAmount || selectedAmount).toFixed(2) }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User, Lock, Setting, Camera, Calendar,
  Wallet, Plus, List, Message, Phone,
  Location, CreditCard, School
} from '@element-plus/icons-vue'
import myAxios from "@/plugins/request.js"

const router = useRouter()
const userStore = useUserStore()

// 激活的标签页
const activeTab = ref('profile')

// 用户信息
const userInfo = ref({
  id: null,
  username: '',
  userAccount: '',
  avatarUrl: '',
  gender: 0,
  phone: '',
  usmEmail: '',
  emailVerified: 0,
  campus: '',
  studentId: '',
  school: '',
  balance: 0,
  userStatus: 1,
  userRole: 0,
  address: '',
  createTime: null
})

// 加载用户数据
const loadUserData = async () => {
  try {
    // 1. res 现在直接就是后端返回的 User 对象（脱壳后）
    const res = await myAxios.get('/user/current')

    // 2. 拦截器已处理 code !== 0 的情况，只需判断数据是否存在
    if (res) {
      userInfo.value = { ...userInfo.value, ...res }
      if (userStore.userInfo) {
        userStore.userInfo = { ...userStore.userInfo, ...res }
      }
      initForms()
    }
  } catch (error) {
    // 3. 拦截器会自动弹出错误，此处仅需处理组件状态
    console.error('Failed to load user data:', error)
  }
}

// 个人信息表单
const profileForm = reactive({
  username: '',
  userAccount: '',
  usmEmail: '',
  emailVerified: 0,
  phone: '',
  gender: 0,
  campus: '',
  studentId: '',
  school: '',
  address: ''
})

const profileFormRef = ref()
const saving = ref(false)

const profileRules = {
  username: [
    { required: true, message: 'Please enter your username', trigger: 'blur' },
    { max: 20, message: 'Username cannot exceed 20 characters', trigger: 'blur' }
  ],
  usmEmail: [
    { required: true, message: 'Please enter your USM email', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('Email is required'))
        } else if (!value.toLowerCase().includes('usm.my')) {
          callback(new Error('Must be USM email'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  phone: [
    {
      pattern: /^(\+?6?01)[0-46-9]-*[0-9]{7,8}$/,
      message: 'Please enter a valid Malaysian phone number',
      trigger: 'blur'
    }
  ],
  studentId: [
    {
      pattern: /^[0-9]{6}$/,
      message: 'Student ID must be exactly 6 digits',
      trigger: 'blur'
    }
  ],
}

// 密码表单
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordFormRef = ref()
const changingPassword = ref(false)

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('Please confirm the password'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error("Passwords don't match!"))
  } else {
    callback()
  }
}

const passwordRules = {
  currentPassword: [
    { required: true, message: 'Please enter current password', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: 'Please enter new password', trigger: 'blur' },
    { min: 6, message: 'Password must be at least 6 characters', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
  ]
}

// 充值相关
const showTopUpDialog = ref(false)
const selectedAmount = ref(50)
const customAmount = ref(null)
const paymentMethod = ref('card')
const toppingUp = ref(false)
const topUpAmounts = [10, 20, 50, 100, 200]

// 上传头像状态
const uploading = ref(false)

// 辅助函数
const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('en-US', {
    year: 'numeric', month: 'long', day: 'numeric'
  })
}

const getUserStatus = (status) => {
  const statusMap = { 0: 'Not Verified', 1: 'Normal', 2: 'Disabled' }
  return statusMap[status] || 'Unknown'
}

const getStatusType = (status) => {
  const typeMap = { 0: 'warning', 1: 'success', 2: 'danger' }
  return typeMap[status] || 'info'
}

const getUserRole = (role) => {
  return role === 1 ? 'Administrator' : 'Normal User'
}

// 初始化表单数据
const initForms = () => {
  Object.assign(profileForm, {
    username: userInfo.value.username,
    userAccount: userInfo.value.userAccount,
    usmEmail: userInfo.value.usmEmail,
    emailVerified: userInfo.value.emailVerified,
    phone: userInfo.value.phone || '',
    gender: userInfo.value.gender || 0,
    campus: userInfo.value.campus || '',
    studentId: userInfo.value.studentId || '',
    school: userInfo.value.school || '',
    address: userInfo.value.address || ''
  })
}

// 保存个人信息
const saveProfile = async () => {
  try {
    const fieldsToValidate = ['username', 'phone', 'studentId']
    for (const field of fieldsToValidate) {
      await profileFormRef.value.validateField(field)
    }

    saving.value = true

    const updateData = {
      id: userInfo.value.id,
      username: profileForm.username,
      phone: profileForm.phone || '',
      gender: profileForm.gender || 0,
      campus: profileForm.campus || '',
      studentId: profileForm.studentId || '',
      school: profileForm.school || '',
      address: profileForm.address || ''
    }

    // 拦截器成功后直接返回结果，失败则跳入 catch
    await myAxios.post('/user/update', updateData)

    ElMessage.success('Profile updated successfully!')
    await loadUserData()
  } catch (error) {
    // 校验失败由前端规则触发，业务失败由拦截器触发
    console.error(error)
  } finally {
    saving.value = false
  }
}

// 重置表单
const resetProfile = () => {
  initForms()
}

// 修改密码
const changePassword = async () => {
  try {
    await passwordFormRef.value.validate()
    changingPassword.value = true

    const res = await myAxios.post('/user/change-password', {
      oldPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword
    })

    if (res.code === 0) {
      ElMessage.success('Password changed successfully!')
      passwordForm.currentPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
      passwordFormRef.value.resetFields()
    } else {
      const errorMsg = res.description || res.message || 'Password change failed'
      ElMessage.error(errorMsg)
    }
  } catch (error) {
    ElMessage.error('Failed to change password')
  } finally {
    changingPassword.value = false
  }
}

/**
 * 处理头像上传 (修改后)
 * 流程：上传文件 -> 获取URL -> 更新User表 -> 更新视图
 */
const uploadAvatar = async (options) => {
  const { file } = options

  // 1. 验证文件
  const isValidFormat = ['image/jpeg', 'image/png', 'image/webp'].includes(file.type)
  const isLt2M = file.size / 1024 / 1024 < 5

  if (!isValidFormat) {
    ElMessage.error('Avatar must be JPG, PNG or WebP format!')
    return
  }
  if (!isLt2M) {
    ElMessage.error('Avatar size can not exceed 5MB!')
    return
  }

  uploading.value = true

  try {
    // 2. 上传文件到 FileController
    const formData = new FormData()
    formData.append('file', file)

    const fileRes = await myAxios.post('/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })

    if (fileRes.code !== 0) {
      throw new Error(fileRes.message || 'File upload failed')
    }

    // 获取到图片 URL
    const newAvatarUrl = fileRes.data

    // 3. 将 URL 更新到 User 表
    const updateRes = await myAxios.post('/user/update', {
      id: userInfo.value.id,
      avatarUrl: newAvatarUrl
    })

    if (updateRes.code === 0) {
      // 4. 更新成功，修改本地视图
      userInfo.value.avatarUrl = newAvatarUrl
      if (userStore.userInfo) {
        userStore.userInfo.avatarUrl = newAvatarUrl
      }
      ElMessage.success('Avatar updated successfully!')
    } else {
      throw new Error(updateRes.message || 'Failed to update user profile')
    }

  } catch (error) {
    console.error('上传头像失败:', error)
    ElMessage.error(error.message || 'Failed to upload avatar')
  } finally {
    uploading.value = false
  }
}

// 处理充值
const processTopUp = async () => {
  const amount = customAmount.value || selectedAmount.value
  toppingUp.value = true

  try {
    await new Promise(resolve => setTimeout(resolve, 1000))

    ElMessageBox.alert(
        'This feature is currently under development. Wallet top-up functionality will be available soon!',
        'Feature Coming Soon',
        {
          confirmButtonText: 'OK',
          type: 'info',
          callback: () => {
            showTopUpDialog.value = false
            selectedAmount.value = 50
            customAmount.value = null
          }
        }
    )
  } catch (error) {
    ElMessage.error('Operation failed')
  } finally {
    toppingUp.value = false
  }
}

onMounted(async () => {
  await loadUserData()
})
</script>

<style scoped>
.profile-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-header h1 {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 8px;
  color: #1f2937;
}

.page-header p {
  color: #6b7280;
  font-size: 16px;
  margin: 0;
}

.profile-container {
  display: grid;
  grid-template-columns: 350px 1fr;
  gap: 30px;
}

/* 左侧样式 */
.left-column {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.user-card {
  background: white;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  text-align: center;
}

.avatar-section {
  margin-bottom: 25px;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
  margin-bottom: 15px;
}

.main-avatar {
  border: 4px solid #f0f2f5;
}

.upload-btn {
  position: absolute;
  bottom: 5px;
  right: 5px;
  background: white;
  border: 2px solid #f0f2f5;
}

.user-name {
  font-size: 24px;
  font-weight: 700;
  margin: 10px 0 5px;
  color: #1f2937;
}

.user-email {
  color: #6b7280;
  margin-bottom: 10px;
  font-size: 14px;
}

.member-since {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #9ca3af;
  font-size: 13px;
}

.stats-section {
  display: flex;
  justify-content: space-around;
  margin: 25px 0;
  padding: 20px 0;
  border-top: 1px solid #f3f4f6;
  border-bottom: 1px solid #f3f4f6;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #3b82f6;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.action-btn {
  justify-content: flex-start;
  padding: 12px 20px;
  border: none;
  background: #f9fafb;
  color: #6b7280;
}

.action-btn:hover {
  background: #f3f4f6;
  color: #4b5563;
}

.action-btn.active {
  background: #3b82f6;
  color: white;
}

.wallet-card {
  background: white;
  border-radius: 16px;
  padding: 25px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.wallet-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.wallet-header h3 {
  margin: 0;
  font-size: 18px;
  color: #1f2937;
  display: flex;
  align-items: center;
  gap: 8px;
}

.wallet-balance {
  text-align: center;
  margin-bottom: 25px;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.balance-amount {
  font-size: 36px;
  font-weight: 800;
  margin-bottom: 5px;
}

.balance-label {
  font-size: 14px;
  opacity: 0.9;
}

.wallet-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.status-card {
  background: white;
  border-radius: 16px;
  padding: 25px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.status-card h3 {
  margin: 0 0 20px;
  font-size: 18px;
  color: #1f2937;
}

.status-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f3f4f6;
}

.status-item:last-child {
  border-bottom: none;
}

.status-item .label {
  color: #6b7280;
  font-size: 14px;
}

.status-item .value {
  color: #1f2937;
  font-weight: 500;
}

/* 右侧样式 */
.right-column {
  background: white;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.tab-content {
  min-height: 500px;
}

.section-title h2 {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 8px;
  color: #1f2937;
}

.section-title p {
  color: #6b7280;
  margin-bottom: 30px;
}

.profile-form,
.password-form {
  max-width: 600px;
}

.form-tip {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 4px;
}

.form-actions {
  margin-top: 30px;
}

.security-section h3 {
  font-size: 20px;
  margin-bottom: 20px;
  color: #1f2937;
}

.security-preferences {
  background: #f9fafb;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
}

.preference-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #e5e7eb;
}

.preference-item:last-child {
  border-bottom: none;
}

.preference-info h4 {
  margin: 0 0 5px;
  font-size: 16px;
  color: #1f2937;
}

.preference-info p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

/* 充值对话框 */
.top-up-dialog {
  padding: 10px 0;
}

.amount-selection {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 20px;
}

.amount-option {
  padding: 15px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  text-align: center;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.amount-option:hover {
  border-color: #3b82f6;
}

.amount-option.active {
  background: #3b82f6;
  color: white;
  border-color: #3b82f6;
}

.custom-amount {
  margin-bottom: 20px;
}

.payment-methods h4 {
  margin-bottom: 10px;
  color: #4b5563;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .profile-container {
    grid-template-columns: 1fr;
  }

  .left-column {
    order: 2;
  }

  .right-column {
    order: 1;
  }
}

@media (max-width: 768px) {
  .profile-page {
    padding: 15px;
  }

  .page-header h1 {
    font-size: 24px;
  }

  .user-card,
  .wallet-card,
  .status-card,
  .right-column {
    padding: 20px;
  }

  .stats-section {
    padding: 15px 0;
  }

  .stat-value {
    font-size: 20px;
  }

  .amount-selection {
    grid-template-columns: repeat(2, 1fr);
  }

  .wallet-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .profile-form :deep(.el-form-item__label) {
    width: 120px !important;
  }

  .profile-form :deep(.el-form-item__content) {
    margin-left: 120px !important;
  }
}
</style>