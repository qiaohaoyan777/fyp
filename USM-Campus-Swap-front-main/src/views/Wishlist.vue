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
            <p class="item-price">RM{{ item.price }}</p>
            <div class="item-meta">
              <span class="campus">{{ item.campus }}</span>
              <span class="views">
                <el-icon><View /></el-icon>
                {{ item.viewCount }}
              </span>
            </div>
            <div class="seller-info">
              <img :src="item.userAvatar" :alt="item.userName" class="seller-avatar" />
              <span class="seller-name">{{ item.userName }}</span>
            </div>
          </div>

          <div class="item-actions-bottom">
            <el-button type="primary" @click="contactSeller(item)" class="contact-btn">
              <el-icon><ChatDotRound /></el-icon>
              Contact Seller
            </el-button>
            <el-button @click="$router.push(`/goods/${item.id}`)" class="view-btn">
              View Details
            </el-button>
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
import { Star, ShoppingCart, Close, ChatDotRound, View } from '@element-plus/icons-vue'
import myAxios from "@/plugins/request.js"; // 1. 引入 Axios

const router = useRouter()

// 2. 将初始值设为空数组
const wishlistItems = ref([])
const recommendedItems = ref([])

// 3. 加载收藏列表 (连接后端)
const loadWishlist = async () => {
  try {
    // 1. res 现在直接就是后端返回的收藏商品数组（脱壳后）
    const res = await myAxios.get('/wishlist/list')

    // 2. 拦截器已处理 code !== 0 的情况，只需判断 res 是否为数组
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
    // 3. 拦截器会自动通过 ElMessage 报错，此处仅需处理逻辑
    console.error('Failed to load wishlist:', error)
  }
}

// 4. 加载推荐商品 (可选)
const loadRecommendations = async () => {
  try {
    const res = await myAxios.get('/goods/recommend')
    if (res.code === 0) {
      recommendedItems.value = res.data
    }
  } catch (e) {
    console.error(e)
  }
}

// 5. 初始化加载
onMounted(() => {
  loadWishlist()
  loadRecommendations()
})

// 6. 从收藏移除 (连接后端)
const removeFromWishlist = async (itemId) => {
  try {
    await ElMessageBox.confirm(
        'Are you sure you want to remove this item?',
        'Remove from Wishlist',
        { confirmButtonText: 'Remove', cancelButtonText: 'Cancel', type: 'warning' }
    )

    // 4. 调用后端删除接口（拦截器处理成功/失败）
    await myAxios.post('/wishlist/remove', { goodsId: itemId })

    // 5. 只要没有抛出错误，就代表成功
    wishlistItems.value = wishlistItems.value.filter(item => item.id !== itemId)
    ElMessage.success('Item removed from wishlist')
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

// 7. 联系卖家 (修复跳转逻辑)
const contactSeller = (item) => {
  // 必须传递 targetUserId，否则聊天页面不知道跟谁聊
  if (!item.sellerId) {
    ElMessage.warning('Cannot contact this seller currently.')
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

.page-header p {
  color: #6b7280;
  font-size: 16px;
  margin: 0;
}

/* 空状态 */
.empty-wishlist {
  text-align: center;
  padding: 80px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.empty-content .el-icon {
  font-size: 64px;
  color: #e5e7eb;
  margin-bottom: 20px;
}

.empty-content h3 {
  font-size: 20px;
  color: #374151;
  margin-bottom: 8px;
}

.empty-content p {
  color: #6b7280;
  margin-bottom: 24px;
}

/* 收藏商品网格 */
.wishlist-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
  margin-bottom: 60px;
}

.wishlist-item {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
}

.wishlist-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.item-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-actions {
  position: absolute;
  top: 12px;
  right: 12px;
}

.remove-btn {
  background: rgba(239, 68, 68, 0.9);
  border: none;
}

.remove-btn:hover {
  background: #dc2626;
}

.item-status {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: white;
  background: #10b981;
}

.item-info {
  padding: 16px;
  flex: 1;
}

.item-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #1f2937;
  cursor: pointer;
  line-height: 1.4;
}

.item-title:hover {
  color: #409eff;
}

.item-price {
  font-size: 18px;
  font-weight: bold;
  color: #ef4444;
  margin-bottom: 12px;
}

.item-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 13px;
  color: #6b7280;
}

.item-meta .views {
  display: flex;
  align-items: center;
  gap: 4px;
}

.seller-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.seller-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
}

.seller-name {
  font-size: 13px;
  color: #6b7280;
}

.item-actions-bottom {
  padding: 0 16px 16px;
  display: flex;
  gap: 8px;
}

.contact-btn, .view-btn {
  flex: 1;
}

/* 推荐商品 */
.recommended-section {
  border-top: 1px solid #e5e7eb;
  padding-top: 40px;
}

.recommended-section h2 {
  font-size: 24px;
  margin-bottom: 20px;
  color: #1f2937;
  text-align: center;
}

.recommended-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.recommended-item {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
}

.recommended-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.recommended-image {
  width: 100%;
  height: 120px;
  object-fit: cover;
  border-radius: 6px;
  margin-bottom: 12px;
}

.recommended-info h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #1f2937;
  line-height: 1.4;
}

.recommended-price {
  font-size: 16px;
  font-weight: 600;
  color: #ef4444;
  margin: 0 0 4px 0;
}

.recommended-campus {
  font-size: 12px;
  color: #6b7280;
  margin: 0;
}

@media (max-width: 768px) {
  .wishlist-page {
    padding: 15px;
  }

  .wishlist-grid {
    grid-template-columns: 1fr;
  }

  .item-actions-bottom {
    flex-direction: column;
  }

  .recommended-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>