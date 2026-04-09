<template>
  <div class="wishlist-page">
    <div class="page-header">
      <h1>My Wishlist</h1>
      <p>Items you've saved for later</p>
    </div>

    <div class="wishlist-content">
      <div v-if="wishlistItems.length === 0" class="empty-wishlist">
        <div class="empty-content">
          <el-icon><Star /></el-icon>
          <h3>Your wishlist is empty</h3>
          <p>Start exploring and add items you're interested in!</p>
          <el-button type="primary" @click="$router.push('/')">
            <el-icon><ShoppingCart /></el-icon>
            Start Shopping
          </el-button>
        </div>
      </div>

      <div v-else class="wishlist-grid">
        <div
            v-for="item in wishlistItems"
            :key="item.id"
            class="wishlist-item"
        >
          <div class="item-image">
            <img :src="item.coverImage" :alt="item.title" />
            <div class="item-actions">
              <el-button
                  type="danger"
                  size="small"
                  circle
                  @click="removeFromWishlist(item.id)"
                  class="remove-btn"
              >
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
            <div class="item-status available">Available</div>
          </div>

          <div class="item-info">
            <h3 class="item-title" @click="$router.push(`/goods/${item.id}`)">{{ item.title }}</h3>
            <p class="item-price">RM {{ item.price?.toFixed(2) }}</p>
            <div class="item-meta">
              <span class="campus">
                <el-icon><Location /></el-icon> {{ item.campus }}
              </span>
              <span class="views">
                <el-icon><View /></el-icon> {{ item.viewCount }}
              </span>
            </div>
            <div class="seller-info">
              <img :src="item.userAvatar" :alt="item.userName" class="seller-avatar" />
              <span class="seller-name">{{ item.userName }}</span>
            </div>
          </div>

          <div class="item-actions-bottom">
            <el-button type="primary" @click="contactSeller(item)" class="contact-btn">
              <el-icon style="margin-right: 5px;"><ChatDotRound /></el-icon>
              Contact
            </el-button>
            <el-button @click="$router.push(`/goods/${item.id}`)" class="view-btn">
              Details
            </el-button>
          </div>
        </div>
      </div>

      <div class="recommended-section" v-if="recommendedItems.length > 0">
        <h2>Recommended for You</h2>
        <div class="recommended-grid">
          <div 
            v-for="rec in recommendedItems" 
            :key="rec.id" 
            class="recommended-item"
            @click="$router.push(`/goods/${rec.id}`)"
          >
            <img :src="rec.coverImage" class="recommended-image" />
            <div class="recommended-info">
              <h4 class="truncate">{{ rec.title }}</h4>
              <p class="recommended-price">RM {{ rec.price?.toFixed(2) }}</p>
              <p class="recommended-campus">{{ rec.campus }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, ShoppingCart, Close, ChatDotRound, View, Location } from '@element-plus/icons-vue'
import myAxios from "@/plugins/request.js"

const router = useRouter()
const wishlistItems = ref([])
const recommendedItems = ref([])

// 加载收藏列表
const loadWishlist = async () => {
  try {
    const res = await myAxios.get('/wishlist/list')
    if (res && Array.isArray(res)) {
      wishlistItems.value = res.map(item => ({
        id: item.goodsId,
        title: item.goodsName,
        price: item.price,
        coverImage: item.coverImage,
        campus: item.campus,
        viewCount: item.viewCount || 0,
        userName: item.sellerName,
        userAvatar: item.sellerAvatar,
        sellerId: item.sellerId
      }))
    }
  } catch (error) {
    console.error('Failed to load wishlist:', error)
  }
}

// 🌟 修复：加载推荐商品 (对接后端新写的 /recommend 接口)
const loadRecommendations = async () => {
  try {
    // 路径与 GoodsController 中的新接口保持一致
    const res = await myAxios.get('/goods/recommend')
    
    // 同样需要进行数据脱壳（如果拦截器已经脱壳，res 就是数组）
    const data = res.records || res 
    if (Array.isArray(data)) {
      recommendedItems.value = data.slice(0, 4) // 只取前4个推荐，避免太乱
    }
  } catch (e) {
    // 如果后端没写好推荐算法，这里会静默失败，不弹出红色报错
    console.warn('Recommended goods not available:', e)
  }
}

// 初始化加载
onMounted(() => {
  loadWishlist()
  loadRecommendations()
})

// 从收藏移除
const removeFromWishlist = async (itemId) => {
  try {
    await ElMessageBox.confirm(
        'Are you sure you want to remove this item?',
        'Remove from Wishlist',
        { confirmButtonText: 'Remove', cancelButtonText: 'Cancel', type: 'warning' }
    )

    await myAxios.post('/wishlist/remove', { goodsId: itemId })
    wishlistItems.value = wishlistItems.value.filter(item => item.id !== itemId)
    ElMessage.success('Item removed')
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

// 联系卖家
const contactSeller = (item) => {
  if (!item.sellerId) {
    ElMessage.warning('Seller information not found.')
    return
  }
  router.push({
    path: '/messages',
    query: { targetUserId: item.sellerId }
  })
}
</script>

<style scoped>
.wishlist-page {
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

/* 空状态 */
.empty-wishlist {
  text-align: center;
  padding: 80px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.empty-content .el-icon {
  font-size: 64px;
  color: #e5e7eb;
  margin-bottom: 20px;
}

/* 收藏商品网格 */
.wishlist-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 60px;
}

.wishlist-item {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
}

.wishlist-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.item-image {
  position: relative;
  height: 180px;
  background: #f9fafb;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.item-status {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 11px;
  color: white;
  background: #10b981;
}

.item-info {
  padding: 16px;
  flex: 1;
}

.item-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 6px;
  color: #111827;
  cursor: pointer;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-price {
  font-size: 18px;
  font-weight: 700;
  color: #ef4444;
  margin-bottom: 12px;
}

.item-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 12px;
  color: #6b7280;
}

.seller-info {
  display: flex;
  align-items: center;
  gap: 8px;
  border-top: 1px dashed #f3f4f6;
  padding-top: 12px;
}

.seller-avatar {
  width: 22px;
  height: 22px;
  border-radius: 50%;
}

.item-actions-bottom {
  padding: 0 16px 16px;
  display: flex;
  gap: 10px;
}

.contact-btn { flex: 1.5; }
.view-btn { flex: 1; }

/* 推荐区域 */
.recommended-section {
  margin-top: 60px;
  border-top: 2px solid #f3f4f6;
  padding-top: 40px;
}

.recommended-section h2 {
  font-size: 22px;
  margin-bottom: 24px;
  color: #111827;
  text-align: center;
}

.recommended-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.recommended-item {
  background: white;
  border-radius: 10px;
  padding: 12px;
  cursor: pointer;
  border: 1px solid #f3f4f6;
  transition: transform 0.2s;
}

.recommended-item:hover {
  transform: scale(1.02);
}

.recommended-image {
  width: 100%;
  height: 140px;
  object-fit: contain;
  margin-bottom: 10px;
}

.truncate {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: 500;
}

.recommended-price {
  color: #ef4444;
  font-weight: bold;
}

.recommended-campus {
  font-size: 11px;
  color: #9ca3af;
}
</style>