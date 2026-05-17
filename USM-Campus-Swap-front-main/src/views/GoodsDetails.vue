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
              
              <div class="spec-item" v-if="goods.specifics">
                <span class="spec-label">Item Spec / Size</span>
                <el-tag effect="plain" type="info" size="large">{{ goods.specifics }}</el-tag>
              </div>
              
              <div class="spec-item">
                <span class="spec-label">{{ goods.deliveryMethod === 2 ? 'Delivery Method' : 'Meetup Location' }}</span>
                
                <div class="spec-value location-value clickable-location" v-if="goods.deliveryMethod === 1" @click="showMapDialog" title="Click to view on map">
                  <el-icon><Location /></el-icon> 
                  {{ goods.campus }} 
                  <span v-if="goods.address && goods.address.trim() !== '' && goods.address !== 'Other'" class="detailed-address">
                    - {{ goods.address }}
                  </span>
                  <el-icon class="map-hint-icon"><MapLocation /></el-icon>
                </div>
                
                <div class="spec-value location-value" style="color: #E6A23C;" v-else>
                  <el-icon><Van /></el-icon> Delivery
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

              <div class="admin-actions" v-if="isAdmin" style="margin-bottom: 20px;">
                <el-button type="danger" size="large" style="width: 100%; box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);" @click="takeDownDialogVisible = true">
                  <el-icon style="margin-right: 8px;"><Warning /></el-icon> Take Down Product (Admin)
                </el-button>
              </div>

              <div class="inline-seller" @click="sellerDialogVisible = true" style="cursor: pointer;">
                <el-avatar :size="40" :src="goods.userAvatar" />
                <div class="inline-seller-info">
                  <span class="name">Sold by {{ goods.userName }}</span>
                  <div class="rating-mini">
                    <el-icon color="#E6A23C"><StarFilled /></el-icon>
                    {{ goods.seller.rating }}
                  </div>
                </div>
                <el-button type="primary" plain @click.stop="openChatWithSeller">
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
          <div class="seller-card clickable-card" @click="sellerDialogVisible = true" title="Click to view seller details">
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

    <el-dialog v-model="sellerDialogVisible" title="About the Seller" width="450px" center destroy-on-close>
      <div class="seller-dialog-content">
        <el-avatar :size="80" :src="goods.userAvatar" style="box-shadow: 0 4px 12px rgba(0,0,0,0.1);" />
        <h2>{{ goods.userName }}</h2>
        <p class="join-date">Member since {{ goods.seller.joinDate }}</p>

        <div class="seller-detail-list">
          <div class="detail-item">
            <span class="d-label">Student ID:</span>
            <span class="d-val">{{ goods.seller.studentId || 'N/A' }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">USM Email:</span>
            <span class="d-val">{{ goods.seller.usmEmail || 'N/A' }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">Campus:</span>
            <span class="d-val">{{ goods.seller.campus || 'Main Campus' }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">School:</span>
            <span class="d-val">{{ goods.seller.school || 'General' }}</span>
          </div>
        </div>

        <div class="dialog-metrics">
          <div class="d-metric">
            <span class="d-num"><el-icon color="#E6A23C"><StarFilled /></el-icon> {{ goods.seller.rating }}</span>
            <span class="d-txt">Rating</span>
          </div>
          <div class="d-metric">
            <span class="d-num">{{ goods.seller.reviewCount }}</span>
            <span class="d-txt">Reviews</span>
          </div>
          <div class="d-metric">
            <span class="d-num">{{ goods.seller.itemCount }}</span>
            <span class="d-txt">Items for Sale</span>
          </div>
        </div>

        <el-button type="primary" size="large" class="dialog-contact-btn" @click="openChatWithSeller">
          <el-icon style="margin-right:8px"><Message /></el-icon> Send Message
        </el-button>
      </div>
    </el-dialog>

    <el-dialog v-model="takeDownDialogVisible" title="Admin Violation Takedown" width="400px" destroy-on-close>
      <div style="margin-bottom: 15px; color: #606266;">
        Please select the reason for taking down <strong>{{ goods.title }}</strong>:
      </div>
      
      <el-select v-model="takeDownReason" placeholder="Select violation reason" style="width: 100%; margin-bottom: 15px;">
        <el-option label="Prohibited/Restricted Item" value="Prohibited or Restricted Item" />
        <el-option label="Counterfeit/Fake Product" value="Counterfeit or Fake Product" />
        <el-option label="False or Misleading Information" value="False or Misleading Information" />
        <el-option label="Inappropriate Content/Images" value="Inappropriate Content or Images" />
        <el-option label="Other (Please specify)" value="Other" />
      </el-select>

      <el-input 
        v-if="takeDownReason === 'Other'" 
        v-model="takeDownCustomReason" 
        type="textarea"
        :rows="3"
        placeholder="Please type the specific reason here..." 
      />

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="takeDownDialogVisible = false">Cancel</el-button>
          <el-button type="danger" @click="submitTakeDown" :disabled="!takeDownReason || (takeDownReason === 'Other' && !takeDownCustomReason.trim())">
            Confirm Take Down
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="mapVisible" title="Location Map" width="600px" destroy-on-close align-center>
      <div style="width: 100%; height: 400px; border-radius: 8px; overflow: hidden; background: #f0f2f5;">
        <iframe
          width="100%"
          height="100%"
          frameborder="0"
          style="border:0"
          :src="mapIframeUrl"
          allowfullscreen>
        </iframe>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" plain @click="openInGoogleMapsApp">
            <el-icon style="margin-right: 6px;"><Location /></el-icon> Open in Google Maps
          </el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, StarFilled, View, Clock, Location, Message, Van, Warning, MapLocation } from '@element-plus/icons-vue' // 引入 MapLocation
import myAxios from "@/plugins/request";
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

const sellerDialogVisible = ref(false)

const isAdmin = computed(() => userStore.userInfo?.userRole === 1)
const takeDownDialogVisible = ref(false)
const takeDownReason = ref('')
const takeDownCustomReason = ref('')

const goods = ref({
  id: 0,
  title: '',
  price: 0,
  images: [],
  userAvatar: '',
  userName: '',
  category: '',
  campus: '',
  address: '', 
  specifics: '', 
  condition: '',
  description: '',
  viewCount: 0,
  postedDate: '',
  deliveryMethod: 1, 
  seller: {
    rating: 5.0,
    reviewCount: 0,
    itemCount: 0,
    joinDate: '',
    studentId: '',
    usmEmail: '',
    campus: '',
    school: ''
  }
})

const isInWishlist = ref(false)

const mapCondition = val => ({ 1:'New',2:'Like New',3:'Gently Used',4:'Used' }[val] || 'Good')

const checkWishlistStatus = async (goodsId) => {
  if (!userStore.userInfo) {
    isInWishlist.value = false;
    return;
  }
  try {
    const res = await myAxios.get('/wishlist/is-collected', { params: { goodsId } })
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
    const res = await myAxios.get(`/goods/${id}`)

    if(res){
      // 🌟 强效排错检查：可以在控制台看到后端到底返回了什么字段
      console.log("Backend response:", res); 

      goods.value = {
        id: res.id,
        title: res.title,
        price: res.price,
        description: res.description,
        campus: res.campus || (res.user ? res.user.campus : 'Main Campus'),
        
        // 🌟 修复点 1：扩大了读取地点的字段范围，应对前后端字段名不一致
        address: res.address || res.location || res.meetupLocation || '', 
        
        // 🌟 修复点 2：确保 specifics 被正确读取
        specifics: res.specifics || res.itemSpec || '', 
        
        deliveryMethod: Number(res.deliveryMethod) || 1, 
        category: res.categoryName || 'General',
        condition: mapCondition(res.condition),
        viewCount: res.viewCount || 0,
        postedDate: res.createTime ? new Date(res.createTime).toLocaleDateString() : 'Recently',
        images: processImages(res.images || res.coverImage),
        userName: res.user ? res.user.username : 'Unknown User',
        userAvatar: res.user ? res.user.avatarUrl : 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
        userId: res.userId, 
        
        seller: {
          rating: res.user?.rating || 5.0,
          reviewCount: res.user?.reviewCount || 0,
          itemCount: res.user?.itemCount || 0,
          joinDate: res.user?.createTime ? new Date(res.user.createTime).toLocaleDateString() : 'Unknown',
          studentId: res.user?.studentId, 
          usmEmail: res.user?.usmEmail,  
          campus: res.user?.campus,      
          school: res.user?.school        
        }
      }
      checkWishlistStatus(res.id)
    }
  } catch(e){
    console.error('Load goods detail failed:', e)
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

onMounted(()=>{ loadGoodsDetail() })
watch(()=>route.params.id,(newId)=>{ if(newId){ loadGoodsDetail(); window.scrollTo(0,0) }})

const conditionType = computed(()=>({
  'New':'success',
  'Like New':'primary',
  'Gently Used':'warning',
  'Used':'info',
  'Good':'warning'
}[goods.value.condition]||'info'))

const toggleWishlist = async () => {
  if (!userStore.userInfo) { 
    ElMessage.warning('Please login first'); 
    router.push('/login'); 
    return; 
  }
  const sellerId = goods.value.userId || (goods.value.user && goods.value.user.id);
  if (sellerId == userStore.userInfo.id) {
    ElMessage.warning('You cannot add your own item to the wishlist!');
    return;
  }
  if (!goods.value.id) return;
  try {
    if (isInWishlist.value) {
      await myAxios.post('/wishlist/remove', { goodsId: goods.value.id });
      isInWishlist.value = false; 
      ElMessage.success('Removed from wishlist');
    } else {
      await myAxios.post('/wishlist/add', { goodsId: goods.value.id });
      isInWishlist.value = true; 
      ElMessage.success('Added to wishlist');
    }
  } catch (e) { 
    console.error('Wishlist toggle error:', e); 
  }
}

const purchaseItem = () => {
  if (!userStore.userInfo) {
    ElMessage.warning('Please login first');
    router.push('/login');
    return;
  }
  if (!goods.value.id) {
    ElMessage.error('Invalid goods');
    return;
  }
  router.push({
    name: 'Payment',
    query: {
      itemIds: goods.value.id 
    }
  })
}

const openChatWithSeller = async () => {
  if (!userStore.userInfo) {
    ElMessage.warning('Please login first');
    router.push('/login');
    return;
  }
  sellerDialogVisible.value = false;
  try {
    const conversationId = await myAxios.post(`/conversation/open?goodsId=${goods.value.id}`);
    if (conversationId) {
      router.push({ path: '/messages', query: { conversationId } });
    }
  } catch (error) {
    console.error(error);
  }
}

const submitTakeDown = async () => {
  const finalReason = takeDownReason.value === 'Other' ? takeDownCustomReason.value : takeDownReason.value;
  
  try {
    await myAxios.post('/goods/admin/takedown', {
      goodsId: goods.value.id,
      sellerId: goods.value.userId,
      goodsTitle: goods.value.title,
      reason: finalReason
    });
    
    ElMessage.success('Item has been successfully taken down.');
    takeDownDialogVisible.value = false;
    
    setTimeout(() => {
      router.push('/');
    }, 1500);

  } catch (error) {
    ElMessage.error('Failed to take down the item.');
    console.error(error);
  }
}

// 🌟 ====== 地图功能核心逻辑 ====== 🌟
const mapVisible = ref(false)
const mapIframeUrl = ref('')
const mapExternalUrl = ref('')

const showMapDialog = () => {
  if (goods.value.deliveryMethod !== 1) return;

  // 🌟 1. 构造“精准定位”搜索词
  // 逻辑：具体建筑名 + 校区 + 大学名称 + 城市
  // 例如："Student Center, Main Campus, Universiti Sains Malaysia, Penang"
  let parts = [];
  
  // 第一优先级：具体的交易地点（如 Student Center）
  if (goods.value.address && goods.value.address !== 'Other') {
    parts.push(goods.value.address);
  }
  
  // 第二优先级：校区名（如 Main Campus）
  if (goods.value.campus) {
    parts.push(goods.value.campus);
  }
  
  // 第三优先级：大学全称（确保不会搜到校外去）
  parts.push("Universiti Sains Malaysia");

  // 第四优先级：城市（进一步增加权重）
  if (goods.value.campus === 'Main Campus') parts.push("Penang");
  if (goods.value.campus === 'Engineering Campus') parts.push("Nibong Tebal");
  if (goods.value.campus === 'Health Campus') parts.push("Kelantan");

  const searchQuery = parts.join(", ");
  const encodedQuery = encodeURIComponent(searchQuery);

  // 🌟 2. 修复语法：注意前面的 $ 符号，并使用更稳定的嵌入链接
  // z=18 是街道/建筑级缩放，能直接看清哪栋楼
  mapIframeUrl.value = `https://maps.google.com/maps?q=${encodedQuery}&t=&z=18&ie=UTF8&iwloc=&output=embed`;
  
  // 外部链接也同步修复
  mapExternalUrl.value = `https://www.google.com/maps/search/${encodedQuery}`;

  mapVisible.value = true;
}

const openInGoogleMapsApp = () => {
  window.open(mapExternalUrl.value, '_blank');
}
// ===================================
</script>

<style scoped>
.page-wrapper { background-color: #f5f7fa; min-height: 100vh; padding-bottom: 60px; }
.breadcrumb-container { max-width: 1200px; margin: 0 auto; padding: 20px 20px; }
.main-content { max-width: 1200px; margin: 0 auto; padding: 0 20px; }

.product-card { background: #ffffff; border-radius: 16px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05); padding: 30px; margin-bottom: 30px; }
.product-grid { display: grid; grid-template-columns: 1.2fr 1fr; gap: 40px; }

.carousel-wrapper { border-radius: 12px; overflow: hidden; box-shadow: 0 2px 12px rgba(0,0,0,0.04); }
.image-placeholder { width: 100%; height: 100%; background: #f0f2f5; display: flex; align-items: center; justify-content: center; }
.product-img { width: 100%; height: 100%; object-fit: contain; background-color: #fff; }

.product-title { font-size: 26px; font-weight: 700; color: #1a1a1a; margin: 0 0 10px 0; line-height: 1.3; }
.product-meta-header { display: flex; gap: 15px; color: #909399; font-size: 13px; margin-bottom: 15px; }
.product-meta-header span { display: flex; align-items: center; gap: 4px; }
.price-tag { display: inline-flex; align-items: baseline; color: #ff5000; }
.currency { font-size: 18px; font-weight: 600; margin-right: 4px; }
.amount { font-size: 36px; font-weight: 800; letter-spacing: -1px; }

.specs-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; margin-bottom: 25px; }
.spec-item { display: flex; flex-direction: column; gap: 6px; }
.spec-label { font-size: 12px; text-transform: uppercase; color: #909399; font-weight: 600; letter-spacing: 0.5px; }
.spec-value { font-size: 15px; color: #303133; font-weight: 500; }

.location-value { display: flex; align-items: center; gap: 4px; color: #409EFF; }
.detailed-address { color: #64748b; font-size: 14px; margin-left: 5px; font-weight: normal; }

/* 🌟 地图可点击区域的特效 */
.clickable-location {
  cursor: pointer;
  padding: 6px 10px;
  margin-left: -10px;
  border-radius: 8px;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
}
.clickable-location:hover {
  background-color: #f0f7ff;
  color: #409EFF;
}
.map-hint-icon {
  margin-left: 8px;
  font-size: 16px;
  color: #409EFF;
  opacity: 0.7;
  transition: opacity 0.3s;
}
.clickable-location:hover .map-hint-icon {
  opacity: 1;
  transform: scale(1.1);
}

.description-box h3 { font-size: 16px; font-weight: 600; margin-bottom: 10px; color: #303133; }
.description-box p { color: #606266; line-height: 1.7; font-size: 14px; background: #f9fafc; padding: 15px; border-radius: 8px; margin: 0 0 25px 0; }

.action-area { background: #fff; border-top: 1px solid #eee; padding-top: 20px; }
.user-actions { display: flex; gap: 15px; margin-bottom: 20px; }
.buy-btn { flex: 1; font-weight: 600; font-size: 16px; box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3); transition: transform 0.2s; }
.wish-btn { width: 50px; border-color: #e4e7ed; }
.wish-btn.is-active { border-color: #f56c6c; background-color: #fef0f0; }

.inline-seller { display: flex; align-items: center; gap: 12px; padding: 12px; border: 1px solid #ebeef5; border-radius: 8px; background-color: #fcfcfc; transition: border-color 0.3s; }
.inline-seller:hover { border-color: #409EFF; }
.inline-seller-info { flex: 1; display: flex; flex-direction: column; }
.inline-seller-info .name { font-size: 13px; font-weight: 500; color: #606266; }
.rating-mini { display: flex; align-items: center; font-size: 12px; font-weight: 700; color: #303133; gap: 2px; }

.bottom-layout { display: grid; grid-template-columns: 300px 1fr; gap: 30px; align-items: start; }
.section-title { font-size: 18px; font-weight: 700; color: #303133; margin-bottom: 15px; padding-left: 10px; border-left: 4px solid #409EFF; }

.seller-card { background: white; padding: 25px; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.04); }
.clickable-card { cursor: pointer; transition: transform 0.2s ease, box-shadow 0.2s ease; }
.clickable-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,0.08); }

.seller-top { text-align: center; margin-bottom: 20px; }
.seller-avatar-lg { border: 4px solid #f0f2f5; margin-bottom: 10px; }
.seller-identity h3 { margin: 5px 0; font-size: 18px; }
.member-since { font-size: 12px; color: #909399; }
.seller-metrics { display: flex; justify-content: space-around; margin-bottom: 5px; padding-bottom: 20px; border-bottom: 1px solid #ebeef5; }
.metric { text-align: center; display: flex; flex-direction: column; }
.metric .num { font-weight: 700; font-size: 16px; color: #303133; }
.metric .txt { font-size: 12px; color: #909399; }

.seller-detail-list {
  width: 100%;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 20px;
}
.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px dashed #cbd5e1;
}
.detail-item:last-child {
  border-bottom: none;
}
.d-label {
  color: #64748b;
  font-size: 13px;
  font-weight: 600;
}
.d-val {
  color: #1e293b;
  font-size: 13px;
  font-weight: 500;
}

.seller-dialog-content { display: flex; flex-direction: column; align-items: center; padding: 10px 20px 20px; }
.seller-dialog-content h2 { margin: 15px 0 5px; font-size: 22px; color: #303133; font-weight: 700; }
.join-date { font-size: 14px; color: #909399; margin-bottom: 25px; }

.dialog-metrics { display: flex; width: 100%; justify-content: space-around; background: #f8fafc; padding: 20px; border-radius: 12px; margin-bottom: 25px; }
.d-metric { text-align: center; display: flex; flex-direction: column; gap: 5px; }
.d-num { font-weight: 800; font-size: 18px; color: #1f2937; display: flex; align-items: center; justify-content: center; gap: 4px; }
.d-txt { font-size: 13px; color: #64748b; font-weight: 500;}
.dialog-contact-btn { width: 100%; font-size: 16px; border-radius: 8px; font-weight: 600; }

@media (max-width: 992px) {
  .product-grid { grid-template-columns: 1fr; gap: 30px; }
  .bottom-layout { grid-template-columns: 1fr; }
  .seller-sidebar { order: 2; }
  .carousel-wrapper { height: auto; }
}
</style>