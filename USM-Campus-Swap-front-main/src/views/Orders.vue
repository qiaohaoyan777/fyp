<template>
  <div class="orders-page">
    <div class="page-header">
      <h1>Orders Management</h1>
      <p>Track your purchases and sales history</p>
    </div>

    <el-tabs v-model="activeTab" class="orders-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="My Purchases" name="buyer">
        <template #label>
          <span class="custom-tab-label">
            <el-icon><ShoppingBag /></el-icon>
            <span>My Purchases</span>
          </span>
        </template>
        <OrderList :orders="orderList" :loading="loading" role="buyer" />
      </el-tab-pane>

      <el-tab-pane label="My Sales" name="seller">
        <template #label>
          <span class="custom-tab-label">
            <el-icon><SoldOut /></el-icon>
            <span>My Sales</span>
          </span>
        </template>
        <OrderList :orders="orderList" :loading="loading" role="seller" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingBag, SoldOut } from '@element-plus/icons-vue'
import myAxios from "@/plugins/request.js";
import OrderList from '@/components/OrderList.vue'

const router = useRouter()
const activeTab = ref('buyer') // 默认看“我买的”
const loading = ref(false)
const orderList = ref([])

// 加载订单数据
const fetchOrders = async () => {
  loading.value = true
  try {
    // 1. 这里的 res 直接就是后端返回的 List<OrdersVO> 数组
    const res = await myAxios.get('/order/list', {
      params: {
        role: activeTab.value
      }
    })

    // 2. 拦截器已处理业务错误。如果代码走到这里，说明请求成功且 res 有数据
    if (res && Array.isArray(res)) {
      orderList.value = res.map(item => ({
        ...item,
        goodsImage: processImage(item.goodsImage)
      }))
    } else {
      orderList.value = []
    }
  } catch (error) {
    // 3. 拦截器会自动通过 ElMessage 弹出错误（如 Session 过期或网络错误）
    // 这里只需记录日志，无需重复 ElMessage.error
    console.error('Fetch orders error:', error)
    orderList.value = []
  } finally {
    loading.value = false
  }
}

// 辅助：处理图片
const processImage = (imgData) => {
  if (!imgData) return 'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png';

  // 处理可能残余的 JSON 字符串包裹（如 ["url"]）
  let url = imgData;
  if (typeof imgData === 'string' && imgData.startsWith('[')) {
    try {
      const arr = JSON.parse(imgData);
      url = Array.isArray(arr) ? arr[0] : imgData;
    } catch (e) {
      url = imgData;
    }
  }

  if (url.startsWith('data:image') || url.startsWith('http')) return url;
  return url.startsWith('/') ? url : '/' + url;
}

const handleTabChange = () => {
  // 切换标签时重新加载数据
  orderList.value = []
  fetchOrders()
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.orders-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  min-height: 80vh;
  background-color: #f9fafb;
}

.page-header {
  margin-bottom: 30px;
  text-align: center;
}

.page-header h1 {
  font-size: 28px;
  color: #1f2937;
  margin-bottom: 8px;
}

.page-header p {
  color: #6b7280;
}

.orders-tabs {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}

.custom-tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}
</style>