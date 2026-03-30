<template>
  <div class="order-list-container" v-loading="loading">
    <el-empty v-if="!loading && orders.length === 0" description="No orders found" />

    <div v-else class="order-card" v-for="order in orders" :key="order.id">
      <div class="card-header">
        <span class="order-id">Order #{{ order.id }}</span>
        <div class="header-right">
          <span class="order-time">{{ formatDate(order.createTime) }}</span>
          <el-tag :type="getStatusType(order.orderStatus)" effect="dark" size="small">
            {{ getStatusText(order.orderStatus) }}
          </el-tag>
        </div>
      </div>

      <div class="card-body" @click="goToGoods(order.goodsId)">
        <img :src="order.goodsImage" class="goods-thumb" />
        <div class="goods-info">
          <h3 class="goods-title">{{ order.goodsTitle || 'Unknown Item' }}</h3>

          <div class="counterparty">
            <el-avatar :size="20" :src="order.counterpartyAvatar" />
            <span class="name">
              {{ role === 'buyer' ? 'Seller' : 'Buyer' }}: {{ order.counterpartyName || 'User' }}
            </span>
          </div>
        </div>
        <div class="price-section">
          <span class="currency">RM</span>
          <span class="amount">{{ order.totalAmount }}</span>
        </div>
      </div>

      <div class="card-footer">
        <el-button size="small" @click="contactUser(order)">Contact {{ role === 'buyer' ? 'Seller' : 'Buyer' }}</el-button>
        <el-button type="primary" size="small" plain @click="viewDetails(order.id)">View Details</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()

const props = defineProps({
  orders: Array,
  loading: Boolean,
  role: String // 'buyer' or 'seller'
})

// 状态映射 (根据你的数据库定义调整)
// 假设: 0-Pending, 1-Completed, 2-Cancelled
const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'info' }
  return map[status] || ''
}

const getStatusText = (status) => {
  const map = { 0: 'Pending', 1: 'Completed', 2: 'Cancelled' }
  return map[status] || 'Unknown'
}

const formatDate = (dateStr) => {
  if(!dateStr) return '';
  return new Date(dateStr).toLocaleString();
}

const goToGoods = (id) => {
  router.push(`/goods/${id}`)
}

const contactUser = (order) => {
  // 跳转到消息页
  router.push('/messages')
}

const viewDetails = (orderId) => {
  // 可以做一个订单详情页，或者这就够了
}
</script>

<style scoped>
.order-card {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  margin-bottom: 20px;
  transition: all 0.3s;
}
.order-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.card-header {
  background: #f9fafb;
  padding: 10px 15px;
  display: flex;
  justify-content: space-between;
  border-bottom: 1px solid #e5e7eb;
  font-size: 13px;
  color: #6b7280;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-body {
  padding: 15px;
  display: flex;
  gap: 15px;
  cursor: pointer;
}

.goods-thumb {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 6px;
  background: #f3f4f6;
}

.goods-info {
  flex: 1;
}

.goods-title {
  font-size: 16px;
  margin: 0 0 10px 0;
  color: #1f2937;
}

.counterparty {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6b7280;
}

.price-section {
  text-align: right;
  font-weight: 600;
  color: #1f2937;
}

.amount {
  font-size: 18px;
}

.card-footer {
  padding: 10px 15px;
  border-top: 1px solid #f3f4f6;
  text-align: right;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>