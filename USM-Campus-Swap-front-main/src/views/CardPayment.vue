<template>
  <div class="payment-gateway">
    <div class="gateway-container">
      <div class="gateway-header">
        <h1>Secure Checkout</h1>
        <p>Please enter your credit card details to complete the payment.</p>
      </div>

      <div class="checkout-timer">
        <el-icon class="timer-icon"><Timer /></el-icon>
        <div class="timer-content">
          <span>Time remaining to complete payment: <strong class="timer-text">{{ formattedTime }}</strong></span>
        </div>
      </div>

      <div class="order-summary">
        <span class="label">Amount to Pay:</span>
        <span class="amount">RM {{ amount || '0.00' }}</span>
      </div>

      <el-card class="card-form-container" shadow="hover">
        <div class="card-icons">
          <img src="@/assets/Visa.png" alt="Visa" class="card-icon" />
          <img src="https://upload.wikimedia.org/wikipedia/commons/2/2a/Mastercard-logo.svg" alt="Mastercard" class="card-icon" />
        </div>

        <el-form 
          ref="cardFormRef" 
          :model="cardForm" 
          :rules="rules" 
          label-position="top"
        >
          <el-form-item label="Cardholder Name" prop="name">
            <el-input v-model="cardForm.name" placeholder="JOHN DOE" :prefix-icon="User" autocomplete="off" />
          </el-form-item>

          <el-form-item label="Card Number" prop="cardNumber">
            <el-input 
              v-model="cardForm.cardNumber" 
              placeholder="0000 0000 0000 0000" 
              maxlength="16"
              :prefix-icon="CreditCard"
              autocomplete="off"
            />
          </el-form-item>

          <div class="form-row">
            <el-form-item label="Expiry Date" prop="expiry" class="half-width">
              <el-input 
                v-model="cardForm.expiry" 
                placeholder="MM/YY" 
                maxlength="5" 
                autocomplete="off"
                name="cc-exp"
              />
            </el-form-item>

            <el-form-item label="CVV" prop="cvv" class="half-width">
              <el-input 
                v-model="cardForm.cvv" 
                placeholder="123" 
                maxlength="3" 
                type="password" 
                show-password 
                autocomplete="new-password"
                name="cvv"
              />
            </el-form-item>
          </div>

          <el-button 
            type="primary" 
            class="pay-btn" 
            @click="handlePayment" 
            :loading="processing"
            size="large"
          >
            {{ processing ? 'Processing Payment...' : `Confirm & Pay RM ${amount || '0.00'}` }}
          </el-button>
        </el-form>
      </el-card>

      <div class="security-badge">
        <el-icon><Lock /></el-icon> Payments are secure and encrypted.
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
// 🌟 引入 Timer 图标
import { User, CreditCard, Lock, Timer } from '@element-plus/icons-vue'
import myAxios from "@/plugins/request.js"

const route = useRoute()
const router = useRouter()
const cardFormRef = ref()

const orderId = ref(route.query.orderId)
const amount = ref(route.query.amount)

const processing = ref(false)

const cardForm = reactive({
  name: '',
  cardNumber: '',
  expiry: '',
  cvv: ''
})

// 🌟 ====== 倒计时功能核心逻辑 ====== 🌟
const timeLeft = ref(15 * 60) // 15分钟 = 900秒
let timerInterval = null

const formattedTime = computed(() => {
  const m = Math.floor(timeLeft.value / 60).toString().padStart(2, '0')
  const s = (timeLeft.value % 60).toString().padStart(2, '0')
  return `${m}:${s}`
})

const startTimer = () => {
  if (timerInterval) return
  timerInterval = setInterval(() => {
    if (timeLeft.value > 0) {
      timeLeft.value--
    } else {
      handleTimeout()
    }
  }, 1000)
}

const stopTimer = () => {
  if (timerInterval) {
    clearInterval(timerInterval)
    timerInterval = null
  }
}

const handleTimeout = () => {
  stopTimer()
  ElMessageBox.alert(
    'Payment session has expired. Please try purchasing again.',
    'Timeout',
    {
      confirmButtonText: 'Return to Home',
      type: 'warning',
      showClose: false,
      callback: () => {
        // 超时直接踢回首页
        router.replace('/')
      }
    }
  )
}
// ===================================

const validateExpiry = (rule, value, callback) => {
  if (!value) {
    return callback(new Error('Expiry date is required'))
  }
  const dateStr = value.replace(/\s+/g, '').replace('/', '')
  if (!/^\d{4}$/.test(dateStr)) {
    return callback(new Error('Please enter a valid format (e.g., 12/26)'))
  }
  const month = parseInt(dateStr.substring(0, 2), 10)
  const year = parseInt(dateStr.substring(2, 4), 10)

  if (month < 1 || month > 12) {
    return callback(new Error('Please enter a valid month.')) 
  }
  if (year < 26) {
    return callback(new Error('Card has expired. Please check the year.')) 
  }
  callback()
}

const rules = {
  name: [
    { required: true, message: 'Cardholder name is required', trigger: 'blur' },
    { pattern: /^[A-Za-z\s]+$/, message: 'Name can only contain letters and spaces', trigger: 'blur' }
  ],
  cardNumber: [
    { required: true, message: 'Card number is required', trigger: 'blur' },
    { pattern: /^\d{16}$/, message: 'Card number must be exactly 16 digits', trigger: 'blur' }
  ],
  expiry: [
    { required: true, validator: validateExpiry, trigger: 'blur' }
  ],
  cvv: [
    { required: true, message: 'CVV is required', trigger: 'blur' },
    { pattern: /^\d{3}$/, message: 'CVV must be exactly 3 digits', trigger: 'blur' }
  ]
}

const handlePayment = async () => {
  // 如果点击时已经超时，直接拦截
  if (timeLeft.value <= 0) {
    handleTimeout()
    return
  }

  if (!cardFormRef.value) return
  
  await cardFormRef.value.validate(async (valid) => {
    if (valid) {
      processing.value = true
      
      try {
        await new Promise(resolve => setTimeout(resolve, 2000))

        if (orderId.value) {
           const idsArray = orderId.value.toString().split(',').map(id => Number(id.trim()));
           
           await myAxios.post('/order/pay', { 
             itemIds: idsArray,
             payMethod: 2, 
             amount: Number(amount.value)
           });
        }

        // 🌟 支付成功，立刻停止定时器
        stopTimer()

        ElMessage.success('Payment Successful! Thank you for your purchase.')
        router.push('/orders')

      } catch (error) {
        ElMessage.error(error.message || 'Payment failed. Please try again.')
        console.error('Payment Error:', error)
      } finally {
        processing.value = false
      }
    }
  })
}

onMounted(() => {
  if (!orderId.value) {
    ElMessage.warning('Order information missing.')
  } else {
    // 🌟 有订单信息则开启倒计时
    startTimer()
  }
})

// 🌟 组件销毁时必须清除定时器
onUnmounted(() => {
  stopTimer()
})
</script>

<style scoped>
.payment-gateway {
  min-height: 100vh;
  background-color: #f4f7fb;
  display: flex;
  justify-content: center;
  padding-top: 60px;
}

.gateway-container {
  width: 100%;
  max-width: 500px;
  padding: 0 20px;
}

.gateway-header {
  text-align: center;
  margin-bottom: 25px;
}

.gateway-header h1 {
  font-size: 28px;
  color: #1e293b;
  margin-bottom: 10px;
}

.gateway-header p {
  color: #64748b;
}

/* 🌟 倒计时横幅样式 */
.checkout-timer {
  background-color: #fff7e6;
  border: 1px solid #ffd591;
  padding: 12px 16px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

.timer-icon {
  font-size: 20px;
  color: #fa8c16;
}

.timer-content {
  font-size: 14px;
  color: #d4380d;
}

.timer-text {
  font-family: 'Courier New', Courier, monospace;
  font-size: 16px;
  font-weight: 800;
  letter-spacing: 1px;
  margin-left: 4px;
  color: #f5222d;
}

/* 原有样式保留 */
.order-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.02);
  border: 1px solid #e2e8f0;
}

.order-summary .label {
  font-size: 16px;
  color: #475569;
  font-weight: 500;
}

.order-summary .amount {
  font-size: 24px;
  font-weight: 800;
  color: #1e293b;
}

.card-form-container {
  border-radius: 16px;
  padding: 10px;
  border: none;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05);
}

.card-icons {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  justify-content: flex-end;
}

.card-icon {
  height: 24px;
  width: auto;
  opacity: 0.8;
}

.form-row {
  display: flex;
  gap: 20px;
}

.half-width {
  flex: 1;
}

.pay-btn {
  width: 100%;
  margin-top: 10px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  padding: 24px 0;
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  border: none;
}

.pay-btn:hover {
  background: linear-gradient(135deg, #334155 0%, #1e293b 100%);
  box-shadow: 0 8px 15px rgba(15, 23, 42, 0.2);
}

.security-badge {
  text-align: center;
  margin-top: 20px;
  color: #94a3b8;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}
</style>