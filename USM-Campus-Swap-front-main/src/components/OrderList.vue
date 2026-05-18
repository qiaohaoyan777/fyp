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

          <div class="delivery-badge" v-if="order.deliveryMethod">
            <span class="delivery" v-if="order.deliveryMethod === 2">
              <el-icon><Van /></el-icon> Delivery (Postage)
            </span>
            <span class="meetup" v-else>
              <el-icon><Location /></el-icon> 
              {{ order.campus || 'Main Campus' }} 
              <span class="sub-location" v-if="order.address && order.address.trim() !== ''">
                - {{ order.address }}
              </span>
            </span>
          </div>

          <div class="counterparty">
            <el-avatar :size="20" :src="order.counterpartyAvatar" />
            <span class="name">
              {{ role === 'buyer' ? 'Seller' : 'Buyer' }}: {{ order.counterpartyName || 'User' }}
            </span>
          </div>
        </div>
        
        <div class="price-section">
          <span class="currency">RM</span>
          <span class="amount">{{ order.totalAmount?.toFixed(2) }}</span>
        </div>
      </div>

      <div class="card-footer">
        
        <el-button 
          v-if="role === 'buyer' && order.orderStatus === 1 && !order.hasReview" 
          type="warning" 
          size="small" 
          plain 
          @click.stop="$emit('open-review', order)"
        >
          Rate Seller
        </el-button>

        <el-button 
          v-if="role === 'buyer' && order.orderStatus === 1 && order.hasReview" 
          type="success" 
          size="small" 
          plain 
          @click.stop="$emit('view-review', order)"
        >
          View Review
        </el-button>

        <el-button size="small" @click.stop="contactUser(order)">
          Contact {{ role === 'buyer' ? 'Seller' : 'Buyer' }}
        </el-button>
        <el-button type="primary" size="small" plain @click.stop="viewDetails(order.id)">
          View Details
        </el-button>
        
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { Location, Van } from '@element-plus/icons-vue' 

const router = useRouter()

const props = defineProps({
  orders: Array,
  loading: Boolean,
  role: String // 'buyer' or 'seller'
})

// 🌟 1. 补上 contact-seller 事件，声明 emit
const emit = defineEmits(['open-review', 'view-review', 'contact-seller'])

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

// 🌟 2. 彻底修改 contactUser 的逻辑
const contactUser = (order) => {
  // 不再自己跳转了，直接把带着所有信息的 order 丢给父组件（Orders.vue）去处理
  emit('contact-seller', order)
}

const viewDetails = (orderId) => {
  // 预留订单详情页入口
}
</script>

<style scoped>
.order-card { border: 1px solid #e5e7eb; border-radius: 8px; margin-bottom: 20px; transition: all 0.3s; background: #fff; }
.order-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.08); transform: translateY(-1px); }
.card-header { background: #f9fafb; padding: 10px 15px; display: flex; justify-content: space-between; border-bottom: 1px solid #e5e7eb; font-size: 13px; color: #6b7280; border-top-left-radius: 8px; border-top-right-radius: 8px; }
.header-right { display: flex; align-items: center; gap: 10px; }
.card-body { padding: 15px; display: flex; gap: 15px; cursor: pointer; }
.goods-thumb { width: 80px; height: 80px; object-fit: cover; border-radius: 6px; background: #f3f4f6; }
.goods-info { flex: 1; display: flex; flex-direction: column; justify-content: center; }
.goods-title { font-size: 16px; margin: 0 0 8px 0; color: #1f2937; font-weight: 600; }
.delivery-badge { font-size: 13px; margin-bottom: 8px; }
.delivery-badge .delivery { color: #409EFF; display: flex; align-items: center; gap: 4px; font-weight: 500;}
.delivery-badge .meetup { color: #67c23a; display: flex; align-items: center; gap: 4px; font-weight: 500;}
.sub-location { color: #909399; font-weight: normal; font-size: 12px; }
.counterparty { display: flex; align-items: center; gap: 6px; font-size: 13px; color: #6b7280; background: #f9fafb; padding: 4px 8px; border-radius: 4px; width: fit-content; }
.price-section { text-align: right; font-weight: 600; color: #ef4444; display: flex; align-items: center; gap: 2px; }
.currency { font-size: 14px; }
.amount { font-size: 20px; }
.card-footer { padding: 12px 15px; border-top: 1px solid #f3f4f6; text-align: right; display: flex; justify-content: flex-end; gap: 10px; }
</style>