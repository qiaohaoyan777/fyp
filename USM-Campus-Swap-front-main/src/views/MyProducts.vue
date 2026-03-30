<template>
  <div class="my-products-page">
    <div class="page-header">
      <h1>My Products</h1>
      <p>Manage your listed items and track their status</p>
    </div>

    <div class="page-actions">
      <el-button type="primary" @click="$router.push('/sell')" class="add-product-btn">
        <el-icon><Plus /></el-icon>
        Add New Product
      </el-button>

      <div class="filter-actions">
        <el-input
            v-model="searchKeyword"
            placeholder="Search your products..."
            prefix-icon="Search"
            clearable
            style="width: 300px;"
        />
        <el-select v-model="statusFilter" placeholder="Filter by status" clearable>
          <el-option label="Active" value="active" />
          <el-option label="Sold" value="sold" />
          <el-option label="Inactive" value="inactive" />
        </el-select>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="products-tabs">
      <el-tab-pane label="Active" name="active">
        <!-- 直接在这里渲染商品，避免组件导入问题 -->
        <div class="products-grid">
          <div
              v-for="product in activeProducts"
              :key="product.id"
              class="product-card"
          >
            <div class="product-image">
              <img :src="product.coverImage" :alt="product.title" />
              <div class="product-status active">Active</div>
            </div>

            <div class="product-info">
              <h3 class="product-title">{{ product.title }}</h3>
              <p class="product-price">RM{{ product.price }}</p>

              <div class="product-meta-grid">
                <div class="meta-item">
                  <el-icon><Location /></el-icon>
                  <span>{{ product.campus }}</span>
                </div>
                <div class="meta-item">
                  <el-icon><View /></el-icon>
                  <span>{{ product.viewCount }} views</span>
                </div>
              </div>

              <div class="product-stats-grid">
                <div class="stat-item">
                  <el-icon><ChatDotRound /></el-icon>
                  <span>{{ product.messages }} messages</span>
                </div>
                <div class="stat-item">
                  <el-icon><Star /></el-icon>
                  <span>{{ product.favorites }} favorites</span>
                </div>
              </div>
            </div>

            <div class="product-actions">
              <el-button
                  type="success"
                  @click="handleMarkAsSold(product)"
                  class="action-btn"
              >
                <el-icon><Check /></el-icon>
                Mark Sold
              </el-button>
              <el-button
                  @click="handleDeactivate(product)"
                  class="action-btn"
              >
                <el-icon><CircleClose /></el-icon>
                Deactivate
              </el-button>
              <el-button
                  @click="handleEdit(product)"
                  class="action-btn"
              >
                <el-icon><Edit /></el-icon>
                Edit
              </el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="Sold" name="sold">
        <div class="products-grid">
          <div
              v-for="product in soldProducts"
              :key="product.id"
              class="product-card"
          >
            <div class="product-image">
              <img :src="product.coverImage" :alt="product.title" />
              <div class="product-status sold">Sold</div>
            </div>

            <div class="product-info">
              <h3 class="product-title">{{ product.title }}</h3>
              <p class="product-price">RM{{ product.price }}</p>

              <div class="product-meta-grid">
                <div class="meta-item">
                  <el-icon><Location /></el-icon>
                  <span>{{ product.campus }}</span>
                </div>
                <div class="meta-item">
                  <el-icon><View /></el-icon>
                  <span>{{ product.viewCount }} views</span>
                </div>
              </div>

              <div class="sold-info">
                <el-icon><Check /></el-icon>
                Sold on {{ product.soldAt }}
              </div>
            </div>

            <div class="product-actions">
              <el-button
                  @click="handleView(product)"
                  class="action-btn"
              >
                <el-icon><View /></el-icon>
                View
              </el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="Inactive" name="inactive">
        <div class="products-grid">
          <div
              v-for="product in inactiveProducts"
              :key="product.id"
              class="product-card"
          >
            <div class="product-image">
              <img :src="product.coverImage" :alt="product.title" />
              <div class="product-status draft">Draft</div>
            </div>

            <div class="product-info">
              <h3 class="product-title">{{ product.title }}</h3>
              <p class="product-price">RM{{ product.price }}</p>

              <div class="product-meta-grid">
                <div class="meta-item">
                  <el-icon><Location /></el-icon>
                  <span>{{ product.campus }}</span>
                </div>
                <div class="meta-item">
                  <el-icon><View /></el-icon>
                  <span>{{ product.viewCount }} views</span>
                </div>
              </div>
            </div>

            <div class="product-actions">
              <el-button
                  type="primary"
                  @click="handleReactivate(product)"
              class="action-btn"
              >
              <el-icon><Refresh /></el-icon>
              Reactivate
              </el-button>
              <el-button
                  @click="handleEdit(product)"
                  class="action-btn"
              >
                <el-icon><Edit /></el-icon>
                Edit
              </el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Search, Location, View, ChatDotRound, Star, Check, Promotion, Edit,Refresh
} from '@element-plus/icons-vue'
import myAxios from "@/plugins/request.js";


const router = useRouter()

const activeTab = ref('active')
const searchKeyword = ref('')
const statusFilter = ref('')
const loading = ref(false)

// 真实商品列表
const products = ref([])

// ★ 核心：后端状态码 (Integer) 与 前端 Tab (String) 的映射
// 假设后端: 1-Active, 2-Sold, 3-Draft/Inactive
const STATUS_MAP = {
  1: 'active',
  2: 'sold',
  3: 'inactive'
}

const REVERSE_STATUS_MAP = {
  'active': 1,
  'sold': 2,
  'inactive': 3
}

// 图片处理函数 (与详情页保持一致)
const processImage = (imgData) => {
  if (!imgData) return 'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png';
  let url = '';
  try {
    if (Array.isArray(imgData)) {
      url = imgData[0];
    } else {
      // 尝试解析 JSON，取第一张作为封面
      const parsed = JSON.parse(imgData);
      url = Array.isArray(parsed) ? parsed[0] : parsed;
    }
  } catch (e) {
    url = imgData; // 普通字符串
  }

  if (url.startsWith('data:image') || url.startsWith('http')) return url;
  return url.startsWith('/') ? url : '/' + url;
}

// 加载我的商品
// 修改后的加载函数
const loadMyProducts = async () => {
  loading.value = true;
  try {
    // 1. 这里的 res 已经是后端返回的 data 数组了（脱壳后）
    const res = await myAxios.get('/goods/my/list');

    // 2. 拦截器已处理 code !== 0 的报错，所以此处直接判断是否存在数据
    if (res && Array.isArray(res)) {
      products.value = res.map(item => ({
        id: item.id,
        title: item.title,
        price: item.price,
        coverImage: processImage(item.images || item.coverImage),
        campus: item.campus || 'Main Campus',
        viewCount: item.viewCount || 0,
        status: STATUS_MAP[item.status] || 'inactive',
        createdAt: item.createTime ? new Date(item.createTime).toLocaleDateString() : '',
        messages: 0,
        favorites: item.likeCount || 0
      }));
    }
  } catch (error) {
    // 拦截器会自动通过 ElMessage.error 弹出后端返回的 description
    console.error('Failed to load products:', error);
  } finally {
    loading.value = false;
  }
}

// 计算属性：前端过滤 (也可以改为点击 Tab 时重新请求后端)
const filterProducts = (status) => {
  return products.value.filter(p => {
    // 1. 状态匹配
    if (p.status !== status) return false;
    // 2. 搜索关键词匹配
    if (searchKeyword.value && !p.title.toLowerCase().includes(searchKeyword.value.toLowerCase())) {
      return false;
    }
    return true;
  })
}

const activeProducts = computed(() => filterProducts('active'))
const soldProducts = computed(() => filterProducts('sold'))
const inactiveProducts = computed(() => filterProducts('inactive'))

// --- 操作逻辑 ---

// 跳转编辑页
const handleEdit = (product) => {
  router.push(`/sell?id=${product.id}`);
}

// 标记为已售出
const handleMarkAsSold = async (product) => {
  try {
    await ElMessageBox.confirm(
        `Are you sure you want to mark "${product.title}" as sold?`,
        'Mark as Sold',
        { confirmButtonText: 'Yes, Mark Sold', cancelButtonText: 'Cancel', type: 'warning' }
    )

    // 调用后端接口
    // 注意：新的拦截器下，如果成功，返回的就是 res.data
    const res = await myAxios.post('/goods/status', {
      id: product.id,
      status: 2
    });

    // 只要没有抛出错误，就代表业务成功（code 0 或 200）
    ElMessage.success('Product marked as sold!');
    product.status = 'sold';
  } catch (e) {
    if (e !== 'cancel') console.error(e);
  }
}

const handleDeactivate = async (product) => {
  await myAxios.post('/goods/status', { id: product.id, status: 3 });
  product.status = 'inactive';
  ElMessage.success('Product deactivated!');
}


const handleReactivate = async (product) => {
  const res = await myAxios.post('/goods/status', {
    id: product.id,
    status: 1  // 重新上架
  });

  if (res.code === 0) {
    product.status = 'active';
    ElMessage.success('Product reactivated!');
  }
}

const handleView = (product) => {
  router.push(`/goods/${product.id}`);
}

onMounted(() => {
  loadMyProducts();
})
</script>

<style scoped>
.my-products-page {
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

.page-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  gap: 20px;
}

.filter-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.add-product-btn {
  height: 44px;
  font-weight: 600;
}

.products-tabs {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

/* 商品网格样式 */
.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 24px;
}

.product-card {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  gap: 16px;
  transition: all 0.3s ease;
  position: relative;
}

.product-card:hover {
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.product-image {
  position: relative;
  width: 100px;
  height: 100px;
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-status {
  position: absolute;
  top: 6px;
  right: 6px;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
  color: white;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.product-status.active {
  background: #10b981;
}

.product-status.sold {
  background: #6b7280;
}

.product-status.draft {
  background: #f59e0b;
}

.product-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.product-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  color: #1f2937;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-price {
  font-size: 18px;
  font-weight: 700;
  color: #ef4444;
  margin: 0;
}

.product-meta-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin: 4px 0;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6b7280;
}

.meta-item .el-icon {
  font-size: 14px;
  color: #9ca3af;
}

.product-stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin: 4px 0;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6b7280;
  padding: 4px 8px;
  background: #f8f9fa;
  border-radius: 6px;
}

.stat-item .el-icon {
  font-size: 14px;
  color: #667eea;
}

.sold-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #10b981;
  font-weight: 500;
  padding: 6px 10px;
  background: #f0fdf4;
  border-radius: 6px;
  border: 1px solid #dcfce7;
  margin-top: 4px;
}

.sold-info .el-icon {
  font-size: 14px;
}

.product-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  justify-content: center;
  min-width: 120px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  justify-content: center;
  font-size: 13px;
  padding: 8px 12px;
}

@media (max-width: 768px) {
  .my-products-page {
    padding: 15px;
  }

  .page-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-actions {
    flex-direction: column;
  }

  .filter-actions .el-input,
  .filter-actions .el-select {
    width: 100% !important;
  }

  .products-grid {
    grid-template-columns: 1fr;
  }

  .product-card {
    flex-direction: column;
    text-align: center;
  }

  .product-image {
    width: 100%;
    height: 160px;
  }

  .product-meta-grid,
  .product-stats-grid {
    grid-template-columns: 1fr;
    gap: 6px;
  }

  .product-actions {
    flex-direction: row;
    flex-wrap: wrap;
    min-width: auto;
  }

  .action-btn {
    flex: 1;
    min-width: 100px;
  }
  .product-status.inactive {
    background: #6b7280;
  }

  .product-status.draft {
    background: #f59e0b;
  }
}
</style>