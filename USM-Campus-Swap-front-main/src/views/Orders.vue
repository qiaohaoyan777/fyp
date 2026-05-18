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
        <OrderList 
          :orders="orderList" 
          :loading="loading" 
          role="buyer" 
          @open-review="handleOpenReview" 
          @view-review="handleViewReview" 
          @contact-seller="handleContact"
        />
      </el-tab-pane>

      <el-tab-pane label="My Sales" name="seller">
        <template #label>
          <span class="custom-tab-label">
            <el-icon><SoldOut /></el-icon>
            <span>My Sales</span>
          </span>
        </template>
        <OrderList 
          :orders="orderList" 
          :loading="loading" 
          role="seller" 
          @contact-seller="handleContact"
        />
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="reviewDialogVisible" title="Rate Seller" width="500px" destroy-on-close>
      <div class="review-form">
        <div class="rating-item">
          <span class="label">Overall Rating:</span>
          <el-rate 
            v-model="reviewForm.rating" 
            :colors="['#99A9BF', '#F7BA2A', '#FF9900']" 
            show-text 
            :texts="['Terrible', 'Bad', 'Average', 'Good', 'Excellent']"
          />
        </div>
        
        <div class="content-item">
          <span class="label">Leave a review (Optional):</span>
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            :rows="4"
            placeholder="Share your experience with this seller (e.g., fast response, friendly, item as described)..."
          />
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="reviewDialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="submitReview" :loading="submittingReview">
            Submit Review
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="viewReviewDialogVisible" title="My Review" width="500px">
      <div class="review-form">
        <div class="rating-item">
          <span class="label">My Rating:</span>
          <el-rate 
            v-model="currentViewReview.rating" 
            :colors="['#99A9BF', '#F7BA2A', '#FF9900']" 
            disabled 
            show-score
            score-template="{value} Stars"
          />
        </div>
        <div class="content-item" v-if="currentViewReview.content">
          <span class="label">My Comment:</span>
          <div class="review-content-box">{{ currentViewReview.content }}</div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="viewReviewDialogVisible = false">Close</el-button>
        </span>
      </template>
    </el-dialog>

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
const activeTab = ref('buyer') 
const loading = ref(false)
const orderList = ref([])

const reviewDialogVisible = ref(false)
const submittingReview = ref(false)
const reviewForm = ref({
  orderId: null,
  sellerId: null,
  rating: 5,
  content: ''
})

const viewReviewDialogVisible = ref(false)
const currentViewReview = ref({ rating: 5, content: '' })

const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await myAxios.get('/order/list', {
      params: { role: activeTab.value }
    })
    if (res && Array.isArray(res)) {
      orderList.value = res.map(item => ({
        ...item,
        goodsImage: processImage(item.goodsImage)
      }))
    } else {
      orderList.value = []
    }
  } catch (error) {
    console.error('Fetch orders error:', error)
    orderList.value = []
  } finally {
    loading.value = false
  }
}

const processImage = (imgData) => {
  if (!imgData) return 'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png';
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
  orderList.value = []
  fetchOrders()
}

const handleOpenReview = (order) => {
  reviewForm.value = {
    orderId: order.id,
    sellerId: order.sellerId,
    rating: 5,
    content: ''
  }
  reviewDialogVisible.value = true
}

const handleViewReview = (order) => {
  currentViewReview.value = {
    rating: order.reviewRating || 5,
    content: order.reviewContent || ''
  }
  viewReviewDialogVisible.value = true
}

const submitReview = async () => {
  if (!reviewForm.value.rating) {
    ElMessage.warning('Please select a star rating')
    return
  }
  submittingReview.value = true
  try {
    await myAxios.post('/review/add', reviewForm.value)
    ElMessage.success('Thank you! Review submitted successfully.')
    reviewDialogVisible.value = false
    fetchOrders() 
  } catch (error) {
    console.error('Submit review error:', error)
  } finally {
    submittingReview.value = false
  }
}

// 🌟 终极修复：配合你高强度的后端接口，只传 goodsId
const handleContact = async (order) => {
  try {
    // 找出当前订单对应的商品 ID
    const goodsId = order.goodsId || order.id; 
    
    if (!goodsId) {
      ElMessage.error('Item information is missing!');
      return;
    }

    console.log("准备打开聊天，传入的商品ID是:", goodsId);

    // 🌟 最关键的一行：把路径改成 /open，并且用 ?goodsId= 的形式满足后端的 @RequestParam
    const res = await myAxios.post(`/conversation/open?goodsId=${goodsId}`);
    
    // 此时后端返回的是你的 conversationId。这里兼容 Axios 拦截器的解包结构
    const conversationId = res.data || res; 
    
    if (conversationId) {
      router.push(`/messages?id=${conversationId}`);
    } else {
      ElMessage.error('Failed to get conversation ID');
    }
    
  } catch (error) {
    console.error('Failed to start conversation:', error);
    ElMessage.error('Cannot connect right now. Please try again.');
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.orders-page { max-width: 1000px; margin: 0 auto; padding: 20px; min-height: 80vh; background-color: #f9fafb; }
.page-header { margin-bottom: 30px; text-align: center; }
.page-header h1 { font-size: 28px; color: #1f2937; margin-bottom: 8px; }
.page-header p { color: #6b7280; }
.orders-tabs { background: white; padding: 20px; border-radius: 12px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); }
.custom-tab-label { display: flex; align-items: center; gap: 6px; }

.review-form { padding: 10px; }
.rating-item { display: flex; align-items: center; gap: 15px; margin-bottom: 25px; font-size: 16px; }
.content-item { display: flex; flex-direction: column; gap: 10px; }
.label { font-weight: bold; color: #4b5563; }

.review-content-box {
  background: #f3f4f6;
  padding: 12px;
  border-radius: 8px;
  color: #374151;
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap;
}
</style>