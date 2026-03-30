<template>
  <div class="page-wrapper">
    <div class="breadcrumb-container">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">Home</el-breadcrumb-item>
        <el-breadcrumb-item>Marketplace</el-breadcrumb-item>
        <el-breadcrumb-item>{{ goods.category }}</el-breadcrumb-item>
        <el-breadcrumb-item>{{ goods.title }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="main-content">
      <div class="product-card">
        <div class="product-grid">
          <!-- 图片区域 -->
          <div class="gallery-section">
            <div class="carousel-wrapper">
              <el-carousel trigger="click" height="450px" indicator-position="outside" arrow="hover">
                <el-carousel-item v-for="img in goods.images" :key="img">
                  <div class="image-placeholder">
                    <img :src="img" :alt="goods.title" class="product-img" />
                  </div>
                </el-carousel-item>
              </el-carousel>
            </div>
          </div>

          <!-- 信息区域 -->
          <div class="info-section">
            <div class="header-group">
              <h1 class="product-title">{{ goods.title }}</h1>
              <div class="product-meta-header">
                <span class="view-count"><el-icon><View /></el-icon> {{ goods.viewCount }} views</span>
                <span class="post-date"><el-icon><Clock /></el-icon> {{ goods.postedDate }}</span>
              </div>
            </div>

            <div class="price-tag">
              <span class="currency">RM</span>
              <span class="amount">{{ goods.price.toFixed(2) }}</span>
            </div>

            <el-divider style="margin: 20px 0;" />

            <div class="specs-grid">
              <div class="spec-item">
                <span class="spec-label">Condition</span>
                <el-tag :type="conditionType" effect="dark" round>{{ goods.condition }}</el-tag>
              </div>
              <div class="spec-item">
                <span class="spec-label">Category</span>
                <div class="spec-value">{{ goods.category }}</div>
              </div>
              <div class="spec-item">
                <span class="spec-label">Campus</span>
                <div class="spec-value location-value">
                  <el-icon><Location /></el-icon> {{ goods.campus }}
                </div>
              </div>
            </div>

            <div class="description-box">
              <h3>Description</h3>
              <p>{{ goods.description }}</p>
            </div>

            <div class="action-area">
              <div class="user-actions">
                <el-button
                    type="primary"
                    size="large"
                    class="buy-btn"
                    @click="purchaseItem"
                >
                  Purchase Now
                </el-button>
                <el-button
                    size="large"
                    class="wish-btn"
                    :class="{ 'is-active': isInWishlist }"
                    @click="toggleWishlist"
                    circle
                >
                  <el-icon v-if="isInWishlist" color="#f56c6c"><StarFilled /></el-icon>
                  <el-icon v-else><Star /></el-icon>
                </el-button>
              </div>

              <div class="inline-seller">
                <el-avatar :size="40" :src="goods.userAvatar" />
                <div class="inline-seller-info">
                  <span class="name">Sold by {{ goods.userName }}</span>
                  <div class="rating-mini">
                    <el-icon color="#E6A23C"><StarFilled /></el-icon>
                    {{ goods.seller.rating }}
                  </div>
                </div>
                <!-- 合并 Chat / Contact Seller 按钮 -->
                <el-button type="primary" plain @click="openChatWithSeller">
                  <el-icon style="margin-right:5px"><Message /></el-icon> Contact Seller
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="bottom-layout">
        <div class="seller-sidebar">
          <div class="section-title">Seller Profile</div>
          <div class="seller-card">
            <div class="seller-top">
              <el-avatar :size="70" :src="goods.userAvatar" class="seller-avatar-lg" />
              <div class="seller-identity">
                <h3>{{ goods.userName }}</h3>
                <span class="member-since">Joined {{ goods.seller.joinDate }}</span>
              </div>
            </div>

            <div class="seller-metrics">
              <div class="metric">
                <span class="num">{{ goods.seller.rating }}</span>
                <span class="txt">Rating</span>
              </div>
              <div class="metric">
                <span class="num">{{ goods.seller.reviewCount }}</span>
                <span class="txt">Reviews</span>
              </div>
              <div class="metric">
                <span class="num">{{ goods.seller.itemCount }}</span>
                <span class="txt">Items</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Star, StarFilled, View, Clock, Location, Message } from '@element-plus/icons-vue'
import myAxios from "@/plugins/request";
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

const goods = ref({
  id: 0,
  title: '',
  price: 0,
  images: [],
  userAvatar: '',
  userName: '',
  category: '',
  campus: '',
  condition: '',
  description: '',
  viewCount: 0,
  postedDate: '',
  seller: {
    rating: 5.0,
    reviewCount: 0,
    itemCount: 0,
    joinDate: '',
  }
})

const relatedGoods = ref([])
const isInWishlist = ref(false)

const mapCondition = val => ({ 1:'New',2:'Like New',3:'Gently Used',4:'Used' }[val] || 'Good')

const checkWishlistStatus = async (goodsId) => {
  if (!userStore.userInfo) {
    isInWishlist.value = false;
    return;
  }
  try {

    const res = await myAxios.get('/wishlist/is-collected', { params: { goodsId } })

    // 如果请求成功且有返回结果，直接赋值
    if (res !== undefined) {
      isInWishlist.value = res
    }
  } catch (e) {
    console.error('Check wishlist status error:', e)
  }
}

const loadGoodsDetail = async () => {
  const id = route.params.id
  if(!id) return
  try {
    // 这里的 res 已经是后端返回的 data 对象了
    const res = await myAxios.get(`/goods/${id}`)

    // 拦截器已经处理了 code !== 0 的情况并抛出错误
    // 所以这里直接判断 res 是否存在即可
    if(res){
      goods.value = {
        id: res.id,
        title: res.title,
        price: res.price,
        description: res.description,
        campus: res.campus || 'Main Campus',
        category: res.categoryName || 'General',
        condition: mapCondition(res.condition),
        viewCount: res.viewCount || 0,
        postedDate: res.createTime ? new Date(res.createTime).toLocaleDateString() : 'Recently',
        images: processImages(res.images || res.coverImage),
        userName: res.user ? res.user.username : 'Unknown User',
        userAvatar: res.user ? res.user.avatarUrl : 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
        seller: {
          rating: 4.5,
          reviewCount: 10,
          itemCount: 5,
          joinDate: res.user && res.user.createTime ? new Date(res.user.createTime).toLocaleDateString() : '2023'
        }
      }
      checkWishlistStatus(res.id)
      loadRelatedGoods()
    }
  } catch(e){
    // 拦截器会自动通过 ElMessage 报错，此处通常只需处理后续逻辑
    console.error(e)
  }
}

const processImages = imgData => {
  if(!imgData) return ['https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png']
  let urls=[]
  try{
    urls=Array.isArray(imgData)?imgData:JSON.parse(imgData)
  }catch(e){ urls=[imgData] }
  return urls.map(url=>{
    if(url.startsWith('data:image')) return url
    if(url.startsWith('http')||url.startsWith('https')) return url
    return url.startsWith('/') ? url : '/' + url
  })
}

const loadRelatedGoods=async()=>{/* 保持原逻辑 */}

onMounted(()=>{ loadGoodsDetail() })
watch(()=>route.params.id,(newId)=>{ if(newId){ loadGoodsDetail(); window.scrollTo(0,0) }})

const conditionType = computed(()=>({
  'New':'success',
  'Like New':'primary',
  'Gently Used':'warning',
  'Used':'info',
  'Good':'warning'
}[goods.value.condition]||'info'))

const toggleWishlist=async()=>{
  if(!userStore.userInfo){ ElMessage.warning('Please login first'); router.push('/login'); return; }
  if(!goods.value.id) return
  try{
    if(isInWishlist.value){
      const res=await myAxios.post('/wishlist/remove',{goodsId:goods.value.id})
      if(res.code===0){ isInWishlist.value=false; ElMessage.success('Removed from wishlist') }
    } else {
      const res=await myAxios.post('/wishlist/add',{goodsId:goods.value.id})
      if(res.code===0){ isInWishlist.value=true; ElMessage.success('Added to wishlist') }
    }
  }catch(e){ console.error(e); ElMessage.error('Operation failed') }
}

const purchaseItem = () => {
  // 1. 检查是否登录（建议加上）
  if (!userStore.userInfo) {
    ElMessage.warning('Please login first');
    router.push('/login');
    return;
  }

  // 2. 检查商品ID是否存在
  if (!goods.value.id) {
    ElMessage.error('Invalid goods');
    return;
  }

  // 3. 跳转到支付页，直接传当前商品的 ID
  router.push({
    name: 'Payment', // 确保你的路由配置里支付页面的 name 是 'Payment'
    query: {
      itemIds: goods.value.id // 直接使用当前商品的ID
    }
  })
}

const viewGoods=id=>router.push(`/goods/${id}`)


const openChatWithSeller = async () => {
  if (!userStore.userInfo) {
    ElMessage.warning('Please login first');
    router.push('/login');
    return;
  }
  try {
    // 同样，这里的 conversationId 直接就是后端返回的长整型 ID
    const conversationId = await myAxios.post(`/conversation/open?goodsId=${goods.value.id}`);

    if (conversationId) {
      router.push({ path: '/messages', query: { conversationId } });
    }
  } catch (error) {
    // 这里的 errorMsg 会自动显示“Session expired”或具体业务错误
    console.error(error);
  }
}

</script>


<style scoped>
/* 全局容器与背景 */
.page-wrapper {
  background-color: #f5f7fa; /* 浅灰背景，突出白色卡片 */
  min-height: 100vh;
  padding-bottom: 60px;
}

.breadcrumb-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 20px;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 商品主卡片 */
.product-card {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  padding: 30px;
  margin-bottom: 30px;
}

.product-grid {
  display: grid;
  grid-template-columns: 1.2fr 1fr; /* 图片区域稍大 */
  gap: 40px;
}

/* 图片区域 */
.carousel-wrapper {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}

.image-placeholder {
  width: 100%;
  height: 100%;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-img {
  width: 100%;
  height: 100%;
  object-fit: contain; /* 保证图片完整显示 */
  background-color: #fff;
}

/* 信息区域 */
.product-title {
  font-size: 26px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 10px 0;
  line-height: 1.3;
}

.product-meta-header {
  display: flex;
  gap: 15px;
  color: #909399;
  font-size: 13px;
  margin-bottom: 15px;
}

.product-meta-header span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.price-tag {
  display: inline-flex;
  align-items: baseline;
  color: #ff5000; /* 更有活力的橙色 */
}

.currency {
  font-size: 18px;
  font-weight: 600;
  margin-right: 4px;
}

.amount {
  font-size: 36px;
  font-weight: 800;
  letter-spacing: -1px;
}

/* 规格网格 */
.specs-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 25px;
}

.spec-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.spec-label {
  font-size: 12px;
  text-transform: uppercase;
  color: #909399;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.spec-value {
  font-size: 15px;
  color: #303133;
  font-weight: 500;
}

.location-value {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #409EFF; /* 使用品牌色强调地点 */
}

.description-box h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 10px;
  color: #303133;
}

.description-box p {
  color: #606266;
  line-height: 1.7;
  font-size: 14px;
  background: #f9fafc;
  padding: 15px;
  border-radius: 8px;
  margin: 0 0 25px 0;
}

/* 购买操作区 */
.action-area {
  background: #fff;
  border-top: 1px solid #eee;
  padding-top: 20px;
}

.user-actions {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.buy-btn {
  flex: 1;
  font-weight: 600;
  font-size: 16px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: transform 0.2s;
}

.buy-btn:hover {
  transform: translateY(-2px);
}

.wish-btn {
  width: 50px; /* 圆形按钮 */
  border-color: #e4e7ed;
}

.wish-btn.is-active {
  border-color: #f56c6c;
  background-color: #fef0f0;
}

/* 嵌入式卖家信息 */
.inline-seller {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background-color: #fcfcfc;
}

.inline-seller-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.inline-seller-info .name {
  font-size: 13px;
  font-weight: 500;
  color: #606266;
}

.rating-mini {
  display: flex;
  align-items: center;
  font-size: 12px;
  font-weight: 700;
  color: #303133;
  gap: 2px;
}

/* 底部双栏布局 */
.bottom-layout {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 30px;
  align-items: start;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 15px;
  padding-left: 10px;
  border-left: 4px solid #409EFF;
}

/* 卖家侧边栏 */
.seller-card {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.04);
}

.seller-top {
  text-align: center;
  margin-bottom: 20px;
}

.seller-avatar-lg {
  border: 4px solid #f0f2f5;
  margin-bottom: 10px;
}

.seller-identity h3 {
  margin: 5px 0;
  font-size: 18px;
}

.member-since {
  font-size: 12px;
  color: #909399;
}

.seller-metrics {
  display: flex;
  justify-content: space-around;
  margin-bottom: 25px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.metric {
  text-align: center;
  display: flex;
  flex-direction: column;
}

.metric .num {
  font-weight: 700;
  font-size: 16px;
  color: #303133;
}

.metric .txt {
  font-size: 12px;
  color: #909399;
}

/* 相关商品网格 */
.related-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.related-card {
  background: white;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
}

.related-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.08);
}

.related-img-wrapper {
  height: 140px;
  position: relative;
  overflow: hidden;
}

.related-img-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hover-overlay {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  opacity: 0;
  transition: opacity 0.3s;
}

.related-card:hover .hover-overlay {
  opacity: 1;
}

.related-details {
  padding: 12px;
}

.truncate {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #303133;
}

.related-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.r-price {
  color: #ff5000;
  font-weight: 700;
}

.r-campus {
  font-size: 11px;
  color: #909399;
  background: #f4f4f5;
  padding: 2px 6px;
  border-radius: 4px;
}

/* 响应式适配 */
@media (max-width: 992px) {
  .product-grid {
    grid-template-columns: 1fr;
    gap: 30px;
  }

  .bottom-layout {
    grid-template-columns: 1fr;
  }

  .seller-sidebar {
    order: 2; /* 在移动端将卖家信息放到最下面 */
  }

  .carousel-wrapper {
    height: auto;
  }
}
</style>