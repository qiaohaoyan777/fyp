<template>
  <div class="payment-container">
    <div class="payment-card">
      <h2>Checkout</h2>

      <div class="order-info">
        <span class="order-id-tag">
          {{ isBatch ? `Items Count: ${itemIds.length}` : `Order # ${orderId}` }}
        </span>
      </div>

      <div class="amount-section">
        <p class="label">Total Amount Due</p>
        <p class="total-price">$ {{ totalAmount.toFixed(2) }}</p>
      </div>

      <div class="method-selector">
        <div
            class="method-btn"
            :class="{ active: paymentType === 'balance' }"
            @click="paymentType = 'balance'"
        >
          <div class="icon">💰</div>
          <div class="text">
            <span>Wallet Balance</span>
            <small>Available: $ {{ userBalance.toFixed(2) }}</small>
          </div>
          <div class="check-mark" v-if="paymentType === 'balance'">✓</div>
        </div>

        <div
            class="method-btn"
            :class="{ active: paymentType === 'cash' }"
            @click="paymentType = 'cash'"
        >
          <div class="icon">💵</div>
          <div class="text">
            <span>Cash on Delivery</span>
            <small>Pay on arrival/at counter</small>
          </div>
          <div class="check-mark" v-if="paymentType === 'cash'">✓</div>
        </div>
      </div>

      <div v-if="isBalanceInsufficient" class="warning-box">
        ⚠️ Insufficient balance. Please use Cash or top up your wallet.
      </div>

      <div class="guide-text">
        <p v-if="paymentType === 'balance'">
          Funds will be deducted immediately from your account.
        </p>
        <p v-else>
          Please ensure you have the exact amount ready upon delivery.
        </p>
      </div>

      <button
          class="submit-btn"
          :class="paymentType"
          :disabled="isProcessing || (paymentType === 'balance' && isBalanceInsufficient)"
          @click="handlePaymentSubmit"
      >
        <span v-if="isProcessing">Processing...</span>
        <span v-else>{{ paymentType === 'balance' ? 'Pay Now' : 'Confirm Order' }}</span>
      </button>

      <p class="back-link" @click="goBack">Cancel and Return</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { ElMessage } from 'element-plus';
import myAxios from "@/plugins/request.js";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

// --- 状态定义 ---
const orderId = ref('');
const itemIds = ref([]); // 存储所有待支付的商品 ID
const totalAmount = ref(0.00);
const paymentType = ref('balance');
const isProcessing = ref(false);
const isBatch = ref(false); // 是否为批量购买

const userBalance = computed(() => userStore.userInfo?.balance || 0);

const isBalanceInsufficient = computed(() => {
  return paymentType.value === 'balance' && userBalance.value < totalAmount.value;
});

// 支付页面的 onMounted
onMounted(async () => {
  const orderIdParam = route.params.orderId;
  const itemIdQuery = route.query.itemId;
  const itemIdsQuery = route.query.itemIds;

  let idsToFetch = [];

  // 1. 确定我们要查哪些 ID
  if (itemIdsQuery) {
    isBatch.value = true;
    idsToFetch = itemIdsQuery.split(',').map(id => Number(id.trim()));
  } else if (orderIdParam || itemIdQuery) {
    isBatch.value = false;
    idsToFetch = [Number(orderIdParam || itemIdQuery)];
  }

  if (idsToFetch.length === 0) {
    ElMessage.warning("No items found to pay.");
    router.back();
    return;
  }

  itemIds.value = idsToFetch;

  try {
    // 2. 批量或单个获取商品信息并计算价格
    let tempTotal = 0;

    // 这里我们用 Promise.all 并发请求所有商品信息
    const requests = idsToFetch.map(id => myAxios.get(`/goods/${id}`));
    const responses = await Promise.all(requests);

    responses.forEach((res, index) => {
      if (res) {
        tempTotal += res.price;
        if (!isBatch.value) {
          orderId.value = res.id;
        }
      }
    });

    totalAmount.value = tempTotal;

  } catch (error) {
    console.error("Load payment info error:", error);
  }
});

const goBack = () => {
  router.back();
};

// --- 支付提交逻辑 ---
const handlePaymentSubmit = async () => {
  // ... 前置校验保持不变
  isProcessing.value = true;
  try {
    await myAxios.post('/order/pay', {
      itemIds: itemIds.value,
      payMethod: paymentType.value === 'balance' ? 1 : 2,
      amount: totalAmount.value
    });
    ElMessage.success('Payment successful!');
    await userStore.fetchCurrentUser();
    router.push('/orders');
  } catch (err) {
    // 拦截器 Promise.reject(new Error(errorMsg)) 会跑到这里
    // 我们需要把这个 errorMsg 弹出来
    ElMessage.error(err.message || 'Payment failed');
    console.error('Payment failed:', err);
  } finally {
    isProcessing.value = false;
  }
};
</script>

<style scoped>
/* 保持原有样式不变 */
.payment-container {
  background: #f8fafc;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  font-family: 'Inter', -apple-system, sans-serif;
}

.payment-card {
  background: #ffffff;
  width: 100%;
  max-width: 440px;
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

h2 {
  text-align: center;
  margin: 0 0 10px 0;
  color: #1e293b;
  font-size: 24px;
}

.order-info {
  text-align: center;
  margin-bottom: 30px;
}

.order-id-tag {
  background: #f1f5f9;
  color: #64748b;
  padding: 4px 12px;
  border-radius: 100px;
  font-size: 13px;
  font-weight: 500;
}

.amount-section {
  text-align: center;
  margin-bottom: 32px;
  padding: 24px;
  background: #fdfdfd;
  border: 1px dashed #e2e8f0;
  border-radius: 20px;
}

.amount-section .label {
  color: #94a3b8;
  font-size: 14px;
  margin: 0 0 4px 0;
}

.amount-section .total-price {
  font-size: 40px;
  font-weight: 800;
  color: #0f172a;
  margin: 0;
}

.method-selector {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}

.method-btn {
  display: flex;
  align-items: center;
  padding: 18px;
  border: 2px solid #f1f5f9;
  border-radius: 16px;
  cursor: pointer;
  position: relative;
  transition: all 0.2s ease;
}

.method-btn:hover {
  border-color: #cbd5e1;
}

.method-btn.active {
  border-color: #3b82f6;
  background: #eff6ff;
}

.method-btn .icon {
  font-size: 28px;
  margin-right: 16px;
}

.method-btn .text span {
  display: block;
  font-weight: 600;
  color: #1e293b;
  font-size: 16px;
}

.method-btn .text small {
  color: #64748b;
  font-size: 12px;
}

.check-mark {
  position: absolute;
  right: 20px;
  color: #3b82f6;
  font-size: 18px;
}

.warning-box {
  background: #fff1f2;
  border: 1px solid #fecdd3;
  color: #e11d48;
  padding: 14px;
  border-radius: 12px;
  font-size: 13px;
  margin-bottom: 20px;
  line-height: 1.5;
}

.guide-text {
  text-align: center;
  font-size: 13px;
  color: #94a3b8;
  margin-bottom: 30px;
  min-height: 40px;
}

.submit-btn {
  width: 100%;
  padding: 18px;
  border-radius: 16px;
  border: none;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  color: #ffffff;
  transition: all 0.2s ease;
}

.submit-btn.balance {
  background: #3b82f6;
}

.submit-btn.cash {
  background: #10b981;
}

.submit-btn:disabled {
  background: #e2e8f0;
  color: #94a3b8;
  cursor: not-allowed;
}

.submit-btn:active:not(:disabled) {
  transform: scale(0.98);
}

.back-link {
  text-align: center;
  font-size: 14px;
  color: #64748b;
  margin-top: 24px;
  cursor: pointer;
  font-weight: 500;
}

.back-link:hover {
  color: #1e293b;
  text-decoration: underline;
}
</style>