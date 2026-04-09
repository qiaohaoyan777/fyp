<template>
  <div class="home-page">
    
    <transition name="fade-slide">
      <div class="hero-section" v-if="showSplash">
        <div class="blob blob-1"></div>
        <div class="blob blob-2"></div>
        <div class="blob blob-3"></div>
        
        <div class="hero-glass-card">
          <div class="hero-badge">Welcome to USM Secondhand</div>
          <h1>Discover Campus <br/> <span class="highlight-text">Best Deals</span></h1>
          <p>Buy, sell, and trade safely within your university community. Sustainable living starts here.</p>
          <el-button class="explore-btn" type="primary" size="large" @click="handleStartExploring">
            Start Exploring
            <el-icon class="el-icon--right"><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>
    </transition>

    <div class="main-content" v-if="!showSplash">
      <div class="category-wrapper">
        <div class="category-filter">
          <div
              class="filter-item"
              :class="{ active: activeCategory === 'all' }"
              @click="handleCategorySelect('all')"
          >
            <el-icon v-if="activeCategory === 'all'"><Check /></el-icon>
            All Items
          </div>
          <div
              v-for="cat in categories"
              :key="cat.id"
              class="filter-item"
              :class="{ active: activeCategory === cat.id }"
              @click="handleCategorySelect(cat.id)"
          >
            {{ cat.name }}
          </div>
        </div>
      </div>

      <div class="products-container" id="products-anchor">
        
        <div v-if="loading" class="products-grid">
          <div v-for="i in 8" :key="i" class="product-card skeleton-card">
            <el-skeleton animated>
              <template #template>
                <el-skeleton-item variant="image" style="height: 220px; width: 100%; border-radius: 16px 16px 0 0;" />
                <div style="padding: 20px;">
                  <el-skeleton-item variant="h3" style="width: 50%; margin-bottom: 15px;" />
                  <el-skeleton-item variant="text" style="width: 100%; margin-bottom: 10px;" />
                  <el-skeleton-item variant="text" style="width: 80%; margin-bottom: 20px;" />
                  <div style="display: flex; align-items: center; gap: 10px;">
                    <el-skeleton-item variant="circle" style="width: 24px; height: 24px;" />
                    <el-skeleton-item variant="text" style="width: 30%;" />
                  </div>
                </div>
              </template>
            </el-skeleton>
          </div>
        </div>

        <div v-else-if="products.length === 0" class="empty-state">
          <el-empty description="No items found in this category" :image-size="200" />
        </div>

        <div v-else class="products-grid fade-in">
          <div
              v-for="item in products"
              :key="item.id"
              class="product-card"
              @click="goToDetail(item.id)"
          >
            <div class="card-image-wrapper">
              <img :src="item.coverImage" :alt="item.title" class="product-img" />
              <div class="glass-tags">
                <span class="condition-tag" :class="`cond-${item.condition}`">{{ item.conditionText }}</span>
              </div>
              <span class="campus-tag">
                <el-icon><Location /></el-icon> {{ item.campus }}
              </span>
            </div>

            <div class="card-info">
              <div class="price-row">
                <span class="price"><span class="currency">RM</span> {{ item.price?.toFixed(2) }}</span>
                
                <span class="likes">
                   <el-icon><StarFilled v-if="item.wishlistCount > 0" /><Star v-else /></el-icon> 
                   {{ item.wishlistCount || 0 }}
                </span>
                
              </div>
              <h3 class="title" :title="item.title">{{ item.title }}</h3>
              
              <div class="seller-row">
                <el-avatar :size="24" :src="item.userAvatar" class="seller-avatar" />
                <span class="seller-name">{{ item.userName }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="pagination-container" v-if="total > 0 && !loading">
          <el-pagination
              background
              layout="prev, pager, next"
              :total="total"
              :page-size="queryParams.pageSize"
              :current-page="queryParams.pageNum"
              @current-change="handlePageChange"
              class="custom-pagination"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Star, StarFilled, Location, ArrowRight, Check } from '@element-plus/icons-vue'
import myAxios from '@/plugins/request'
import { listCategories } from '@/api/category'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore() 

const loading = ref(false)
const products = ref([])
const categories = ref([])
const activeCategory = ref('all')
const total = ref(0)
const showSplash = ref(true)

const handleStartExploring = () => {
  if (!userStore.userInfo) {
    ElMessage.warning('Please login first to access this feature.')
    router.push({ path: '/login', query: { redirect: '/' } })
    return
  }
  showSplash.value = false 
}

const goToDetail = (id) => {
  if (!userStore.userInfo) {
    ElMessage.warning('Please login first to access this feature.')
    router.push({ path: '/login', query: { redirect: `/goods/${id}` } })
    return
  }
  router.push(`/goods/${id}`)
}

const queryParams = reactive({
  pageNum: 1,
  pageSize: 12,
  keyword: '',
  categoryId: null
})

const loadCategories = async () => {
  try {
    const res = await listCategories()
    categories.value = res
  } catch (error) {
    console.error('Failed to load categories', error)
  }
}

const loadProducts = async () => {
  loading.value = true
  try {
    queryParams.keyword = route.query.keyword || ''

    const params = {
      current: queryParams.pageNum,
      size: queryParams.pageSize,
      title: queryParams.keyword, 
      status: 1 
    }

    if (queryParams.categoryId) {
      params.categoryId = queryParams.categoryId
    }

    await new Promise(resolve => setTimeout(resolve, 600)) 

    const res = await myAxios.get('/goods/list/page/vo', { params })

    if (res) {
      const rawList = res.records || []
      total.value = res.total || 0

      products.value = rawList.map(item => {
        let cover = 'https://placehold.co/300x300?text=No+Image'
        if (item.images) {
          try {
            const imgArray = JSON.parse(item.images)
            if (Array.isArray(imgArray) && imgArray.length > 0) {
              cover = imgArray[0]
            }
          } catch (e) {
            if (item.images.startsWith('http')) {
              cover = item.images
            }
          }
        }
        if (item.coverImage) {
          cover = item.coverImage
        }

        const conditionMap = { 1: 'New', 2: 'Like New', 3: 'Good', 4: 'Fair' }

        return {
          ...item,
          coverImage: cover,
          conditionText: conditionMap[item.condition] || 'Good',
          userAvatar: item.userAvatar || 'https://api.dicebear.com/7.x/micah/svg?seed=' + (item.userName || 'User'),
          userName: item.userName || 'User',
          // 🌟 确保有默认值
          wishlistCount: item.wishlistCount || 0 
        }
      })
    }
  } catch (error) {
    console.error('Failed to load products', error)
  } finally {
    loading.value = false
  }
}

const handleCategorySelect = (id) => {
  activeCategory.value = id
  queryParams.categoryId = id === 'all' ? null : id
  queryParams.pageNum = 1
  loadProducts()
}

const handlePageChange = (val) => {
  queryParams.pageNum = val
  loadProducts()
  scrollToProducts()
}

const scrollToProducts = () => {
  document.getElementById('products-anchor')?.scrollIntoView({ behavior: 'smooth' })
}

watch(
    () => route.query.keyword,
    (newVal) => {
      queryParams.keyword = newVal || ''
      queryParams.pageNum = 1 
      loadProducts()
    }
)

onMounted(() => {
  loadCategories()
  if (route.query.keyword) {
    queryParams.keyword = route.query.keyword
  }
  loadProducts()
})
</script>

<style scoped>
/* 样式部分保持不变... */
.home-page { min-height: 100vh; background-color: #f4f7fb; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; }
.hero-section { position: relative; height: calc(100vh - 60px); background-color: #f8fafc; overflow: hidden; display: flex; justify-content: center; align-items: center; }
.blob { position: absolute; border-radius: 50%; filter: blur(80px); opacity: 0.6; z-index: 1; animation: float 10s infinite ease-in-out alternate; }
.blob-1 { width: 400px; height: 400px; background: #60a5fa; top: -10%; left: -10%; }
.blob-2 { width: 500px; height: 500px; background: #c084fc; bottom: -20%; right: -10%; animation-delay: -5s; }
.blob-3 { width: 300px; height: 300px; background: #f472b6; top: 40%; left: 50%; animation-delay: -2s; }
@keyframes float { 0% { transform: translate(0, 0) scale(1); } 100% { transform: translate(50px, 50px) scale(1.1); } }
.hero-glass-card { position: relative; z-index: 2; background: rgba(255, 255, 255, 0.4); backdrop-filter: blur(20px); -webkit-backdrop-filter: blur(20px); border: 1px solid rgba(255, 255, 255, 0.6); border-radius: 30px; padding: 60px 50px; max-width: 700px; text-align: center; box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.1); }
.hero-badge { display: inline-block; padding: 6px 16px; background: rgba(99, 102, 241, 0.1); color: #4f46e5; border-radius: 20px; font-weight: 600; font-size: 14px; margin-bottom: 20px; letter-spacing: 0.5px; }
.hero-glass-card h1 { font-size: 4rem; font-weight: 800; color: #1e293b; line-height: 1.1; margin-bottom: 20px; }
.highlight-text { background: linear-gradient(135deg, #4f46e5 0%, #ec4899 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.hero-glass-card p { font-size: 1.2rem; color: #475569; margin-bottom: 40px; line-height: 1.6; }
.explore-btn { font-size: 1.1rem; padding: 15px 40px; border-radius: 50px; height: auto; background: linear-gradient(135deg, #4f46e5 0%, #3b82f6 100%); border: none; box-shadow: 0 10px 20px rgba(79, 70, 229, 0.3); transition: all 0.3s; }
.explore-btn:hover { transform: translateY(-3px); box-shadow: 0 15px 25px rgba(79, 70, 229, 0.4); }
.main-content { padding-top: 40px; }
.category-wrapper { background: white; padding: 20px 0; position: sticky; top: 60px; z-index: 10; box-shadow: 0 4px 20px rgba(0,0,0,0.03); margin-bottom: 40px; }
.category-filter { display: flex; justify-content: center; gap: 12px; flex-wrap: wrap; padding: 0 20px; max-width: 1200px; margin: 0 auto; }
.filter-item { display: flex; align-items: center; gap: 6px; padding: 10px 24px; background: #f1f5f9; border-radius: 30px; cursor: pointer; font-weight: 600; font-size: 14px; color: #64748b; transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); border: 1px solid transparent; }
.filter-item:hover { background: #e2e8f0; color: #0f172a; }
.filter-item.active { background: #1e293b; color: white; box-shadow: 0 8px 16px rgba(30, 41, 59, 0.2); transform: translateY(-2px); }
.products-container { max-width: 1250px; margin: 0 auto; padding: 0 20px 80px; }
.products-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 30px; }
.product-card { background: white; border-radius: 20px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.04); cursor: pointer; transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1); display: flex; flex-direction: column; border: 1px solid rgba(226, 232, 240, 0.6); }
.product-card:hover { transform: translateY(-10px); box-shadow: 0 20px 30px rgba(0,0,0,0.1); }
.card-image-wrapper { position: relative; height: 240px; width: 100%; overflow: hidden; background: #f8fafc; }
.product-img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.6s ease; }
.product-card:hover .product-img { transform: scale(1.08); }
.glass-tags { position: absolute; top: 15px; right: 15px; z-index: 2; }
.condition-tag { background: rgba(255, 255, 255, 0.85); backdrop-filter: blur(8px); color: #0f172a; padding: 6px 12px; border-radius: 12px; font-size: 12px; font-weight: 700; box-shadow: 0 4px 6px rgba(0,0,0,0.05); }
.cond-1 { color: #10b981; } .cond-4 { color: #f59e0b; }
.campus-tag { position: absolute; bottom: 15px; left: 15px; background: rgba(15, 23, 42, 0.7); backdrop-filter: blur(8px); color: white; padding: 6px 14px; border-radius: 20px; font-size: 12px; font-weight: 600; display: flex; align-items: center; gap: 4px; z-index: 2; }
.card-info { padding: 24px; flex: 1; display: flex; flex-direction: column; }
.price-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.price { color: #ef4444; font-size: 24px; font-weight: 800; letter-spacing: -0.5px; }
.currency { font-size: 14px; opacity: 0.8; }
.likes { color: #64748b; font-size: 14px; display: flex; align-items: center; gap: 6px; background: #f1f5f9; padding: 4px 10px; border-radius: 12px; font-weight: 600; }
.likes .el-icon { color: #f59e0b; }
.title { font-size: 17px; color: #0f172a; margin-bottom: 20px; line-height: 1.5; font-weight: 600; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; flex: 1; }
.seller-row { display: flex; align-items: center; gap: 12px; padding-top: 16px; border-top: 1px dashed #e2e8f0; }
.seller-avatar { border: 2px solid white; box-shadow: 0 2px 6px rgba(0,0,0,0.1); }
.seller-name { font-size: 14px; color: #475569; font-weight: 500; }
.fade-in { animation: fadeIn 0.8s ease forwards; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
.skeleton-card { box-shadow: none; border: 1px solid #f1f5f9; }
.empty-state { margin-top: 60px; background: white; border-radius: 20px; padding: 60px; box-shadow: 0 4px 20px rgba(0,0,0,0.02); }
.pagination-container { margin-top: 60px; display: flex; justify-content: center; }
.custom-pagination :deep(.el-pager li) { background: white; border-radius: 8px; box-shadow: 0 2px 6px rgba(0,0,0,0.04); }
.custom-pagination :deep(.el-pager li.is-active) { background: #1e293b !important; color: white; }
</style>