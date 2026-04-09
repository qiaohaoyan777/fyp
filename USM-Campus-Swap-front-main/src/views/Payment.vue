<template>
  <div class="payment-container">
    <div class="payment-card">
      <h2 class="page-title">Checkout</h2>

      <div class="amount-card">
        <p class="label">Total Amount Due</p>
        <p class="total-price">RM {{ totalAmount.toFixed(2) }}</p>
        
        <div class="delivery-badge">
          <el-tag 
            :type="deliveryMethod === 1 ? 'info' : 'warning'" 
            effect="dark" 
            round
          >
            Method: {{ deliveryMethod === 1 ? '🤝 Self Pickup' : '🚚 Delivery' }}
          </el-tag>
        </div>
      </div>

      <div class="method-selector">
        <div
            class="method-btn"
            :class="{ active: paymentType === 'card' }"
            @click="paymentType = 'card'"
        >
          <div class="icon-wrapper card-icon">
            <el-icon><CreditCard /></el-icon>
          </div>
          <div class="text">
            <span class="title">Credit / Debit Card</span>
            <span class="subtitle">Pay securely with Visa or Mastercard</span>
          </div>
          <div class="check-mark" v-if="paymentType === 'card'">✔</div>
        </div>

        <div
            class="method-btn"
            :class="{ active: paymentType === 'cash' }"
            @click="paymentType = 'cash'"
        >
          <div class="icon-wrapper cash-icon">
            <el-icon><Money /></el-icon>
          </div>
          <div class="text">
            <span class="title">Cash on Delivery</span>
            <span class="subtitle">Pay on arrival / meetup</span>
          </div>
          <div class="check-mark" v-if="paymentType === 'cash'">✔</div>
        </div>
      </div>

      <div class="guide-text">
        <el-alert
          v-if="deliveryMethod === 1 && paymentType === 'card'"
          title="Recommend 'Cash' for Self Pickup to verify item before paying."
          type="info"
          show-icon
          :closable="false"
        />
        <el-alert
          v-else-if="deliveryMethod === 2 && paymentType === 'cash'"
          title="Attention: Sellers may prioritize prepaid 'Card' orders for delivery."
          type="warning"
          show-icon
          :closable="false"
        />
        <p class="fade-text" v-else-if="paymentType === 'card'">
          Secure payment gateway will be used for this transaction.
        </p>
        <p class="fade-text" v-else>
          Please prepare exact change for the transaction.
        </p>
      </div>

      <button
          class="submit-btn"
          :class="paymentType"
          :disabled="isProcessing"
          @click="handlePaymentSubmit"
      >
        <span v-if="isProcessing">Processing...</span>
        <span v-else>{{ paymentType === 'card' ? 'Proceed to Pay' : 'Confirm Order' }}</span>
      </button>

      <p class="back-link" @click="goBack">Cancel and Return</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
// 🌟 修复点 3：引入 Element Plus 的官方图标
import { CreditCard, Money } from '@element-plus/icons-vue';
import myAxios from "@/plugins/request.js";

const route = useRoute();
const router = useRouter();

const orderId = ref('');
const itemIds = ref([]); 
const totalAmount = ref(0.00);
const paymentType = ref('card'); 
const deliveryMethod = ref(1); 
const isProcessing = ref(false);
const isBatch = ref(false); 

onMounted(async () => {
  const orderIdParam = route.params.orderId;
  const itemIdQuery = route.query.itemId;
  const itemIdsQuery = route.query.itemIds;

  let idsToFetch = [];

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
    let tempTotal = 0;
    const requests = idsToFetch.map(id => myAxios.get(`/goods/${id}`));
    const responses = await Promise.all(requests);

    responses.forEach((res, index) => {
      if (res) {
        tempTotal += res.price;
        if (!isBatch.value) {
          orderId.value = res.id;
        }
        if (index === 0) {
          deliveryMethod.value = res.deliveryMethod;
        }
      }
    });

    totalAmount.value = tempTotal;

    if (deliveryMethod.value === 1) {
      paymentType.value = 'cash'; 
    } else {
      paymentType.value = 'card'; 
    }

  } catch (error) {
    console.error("Load payment info error:", error);
  }
});

const goBack = () => {
  router.back();
};

const handlePaymentSubmit = async () => {
  if (paymentType.value === 'card') {
    const targetOrderId = isBatch.value ? itemIds.value.join(',') : orderId.value;
    router.push({
      path: '/card-payment',
      query: {
        orderId: targetOrderId,
        amount: totalAmount.value.toFixed(2)
      }
    });
    return; 
  }

  isProcessing.value = true;
  try {
    await myAxios.post('/order/pay', {
      itemIds: itemIds.value,
      payMethod: 2, 
      amount: totalAmount.value
    });
    ElMessage.success('Order confirmed successfully!');
    router.push('/orders');
  } catch (err) {
    ElMessage.error(err.message || 'Order failed');
  } finally {
    isProcessing.value = false;
  }
};
</script>

<style scoped>
.payment-container {
  background-color: #f4f7fb;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

.payment-card {
  background: #ffffff;
  width: 100%;
  max-width: 460px;
  border-radius: 24px;
  padding: 40px 32px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.06);
}

.page-title {
  text-align: center;
  margin: 0 0 25px 0;
  color: #1e293b;
  font-size: 26px;
  font-weight: 700;
  letter-spacing: -0.5px;
}

.amount-card {
  position: relative;
  text-align: center;
  margin-bottom: 40px;
  padding: 35px 20px;
  background: #ffffff;
  border: 1px dashed #cbd5e1;
  border-radius: 20px;
}

.amount-card .label {
  color: #64748b;
  font-size: 15px;
  margin: 0 0 8px 0;
  font-weight: 500;
}

.amount-card .total-price {
  font-size: 42px;
  font-weight: 800;
  color: #0f172a;
  margin: 0;
  letter-spacing: -1px;
}

.delivery-badge {
  position: absolute;
  bottom: -14px;
  left: 50%;
  transform: translateX(-50%);
  background: #ffffff;
  padding: 0 12px;
}

.method-selector {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.method-btn {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border: 2px solid #e2e8f0;
  border-radius: 16px;
  cursor: pointer;
  background: #ffffff;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.method-btn:hover {
  border-color: #cbd5e1;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
}

.method-btn.active {
  border-color: #3b82f6;
  background-color: #f0f7ff;
}

.icon-wrapper {
  width: 46px;
  height: 46px;
  background: #ffffff;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px; /* 统一的 SVG 图标大小 */
  margin-right: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  transition: all 0.3s;
}

/* 🌟 给不同图标赋予专属颜色，UI 质感飙升 */
.card-icon {
  color: #3b82f6; /* 科技蓝 */
}
.cash-icon {
  color: #10b981; /* 钞票绿 */
}

.method-btn.active .icon-wrapper {
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.15);
  transform: scale(1.05); /* 选中的时候图标稍微放大一点点 */
}

.method-btn .text {
  display: flex;
  flex-direction: column;
}

.method-btn .text .title {
  font-weight: 600;
  color: #1e293b;
  font-size: 15px;
  margin-bottom: 2px;
}

.method-btn .text .subtitle {
  color: #64748b;
  font-size: 13px;
}

.check-mark {
  margin-left: auto;
  color: #3b82f6;
  font-size: 18px;
  font-weight: bold;
}

.guide-text {
  margin-bottom: 25px;
  min-height: 20px;
  text-align: center;
}

.fade-text {
  font-size: 13.5px;
  color: #64748b;
  margin: 0;
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
  transition: all 0.3s;
}

.submit-btn.card { 
  background: #1e293b; 
  box-shadow: 0 4px 12px rgba(30, 41, 59, 0.2);
}
.submit-btn.card:hover { 
  background: #0f172a; 
  box-shadow: 0 6px 16px rgba(15, 23, 42, 0.3);
}

.submit-btn.cash { 
  background: #10b981; 
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.2);
}
.submit-btn.cash:hover {
  background: #059669;
}

.submit-btn:disabled {
  background: #cbd5e1;
  color: #ffffff;
  box-shadow: none;
  cursor: not-allowed;
}

.back-link {
  text-align: center;
  font-size: 14px;
  color: #64748b;
  margin-top: 24px;
  cursor: pointer;
  transition: color 0.2s;
}

.back-link:hover {
  color: #1e293b;
}
</style>