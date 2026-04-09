<template>
  <div class="my-products-page">
    <div class="page-header">
      <h1>My Products</h1>
      <p>Manage your listed items and track their status</p>
    </div>

    <div class="page-actions">
      <el-button type="primary" @click="router.push('/sell')" class="add-product-btn">
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
      </div>
    </div>

    <el-tabs v-model="activeTab" class="products-tabs" v-loading="loading">
      
      <el-tab-pane label="Active" name="active">
        <div class="products-list">
          <div v-for="product in activeProducts" :key="product.id" class="product-card clickable-card" @click="handleView(product)">
            <div class="product-image">
              <img :src="product.coverImage" :alt="product.title" />
              <div class="product-status active">Active</div>
            </div>

            <div class="product-info">
              <h3 class="product-title">{{ product.title }}</h3>
              <p class="product-price">RM{{ product.price?.toFixed(2) }}</p>
              
              <div class="delivery-badge">
                <span class="delivery" v-if="product.deliveryMethod === 2">
                  <el-icon><Van /></el-icon> Delivery (Postage)
                </span>
                <span class="meetup" v-else>
                  <el-icon><Location /></el-icon> 
                  {{ product.campus }} 
                  <span class="sub-location" v-if="product.address && product.address.trim() !== ''">
                    - {{ product.address }}
                  </span>
                </span>
              </div>

              <div class="product-meta-grid">
                <div class="meta-item"><el-icon><View /></el-icon><span>{{ product.viewCount }} views</span></div>
              </div>
            </div>

            <div class="product-actions">
              <el-button type="success" plain @click.stop="handleMarkAsSold(product)">
                <el-icon><Check /></el-icon> Mark Sold
              </el-button>
              <el-button type="warning" plain @click.stop="handleDeactivate(product)">
                <el-icon><CircleClose /></el-icon> Deactivate
              </el-button>
              <el-button type="primary" @click.stop="handleEdit(product)">
                <el-icon><Edit /></el-icon> Edit
              </el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="Sold" name="sold">
        <div class="products-list">
          <div v-for="product in soldProducts" :key="product.id" class="product-card clickable-card" @click="handleView(product)">
            <div class="product-image">
              <img :src="product.coverImage" :alt="product.title" />
              <div class="product-status sold">Sold</div>
            </div>
            
            <div class="product-info">
              <h3 class="product-title">{{ product.title }}</h3>
              <p class="product-price">RM{{ product.price?.toFixed(2) }}</p>
              
              <div class="delivery-badge">
                <span class="delivery" v-if="product.deliveryMethod === 2"><el-icon><Van /></el-icon> Delivery</span>
                <span class="meetup" v-else><el-icon><Location /></el-icon> {{ product.campus }} <span class="sub-location" v-if="product.address">- {{ product.address }}</span></span>
              </div>

              <div class="sold-info"><el-icon><Check /></el-icon> Sold Out</div>
            </div>

            <div class="product-actions">
              <el-button @click.stop="handleView(product)">View Detail</el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="Inactive" name="inactive">
        <div class="products-list">
          <div v-for="product in inactiveProducts" :key="product.id" class="product-card clickable-card" @click="handleView(product)">
            <div class="product-image">
              <img :src="product.coverImage" :alt="product.title" />
              <div class="product-status draft">Inactive</div>
            </div>

            <div class="product-info">
              <h3 class="product-title">{{ product.title }}</h3>
              <p class="product-price">RM{{ product.price?.toFixed(2) }}</p>
              
              <div class="delivery-badge">
                <span class="delivery" v-if="product.deliveryMethod === 2"><el-icon><Van /></el-icon> Delivery</span>
                <span class="meetup" v-else><el-icon><Location /></el-icon> {{ product.campus }} <span class="sub-location" v-if="product.address">- {{ product.address }}</span></span>
              </div>
            </div>

            <div class="product-actions">
              <el-button type="primary" plain @click.stop="handleReactivate(product)">Reactivate</el-button>
              <el-button type="primary" @click.stop="handleEdit(product)">Edit</el-button>
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
import { ElMessage } from 'element-plus'
import { Plus, Search, Location, View, Check, CircleClose, Edit, Van } from '@element-plus/icons-vue'
import myAxios from "@/plugins/request.js";

const router = useRouter()
const activeTab = ref('active')
const searchKeyword = ref('')
const loading = ref(false)
const products = ref([])

const STATUS_MAP = { 1: 'active', 2: 'sold', 3: 'inactive' }

const processImage = (imgData) => {
  if (!imgData) return 'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png';
  let url = '';
  try {
    if (typeof imgData === 'string' && imgData.startsWith('[')) {
      const parsed = JSON.parse(imgData);
      url = Array.isArray(parsed) ? parsed[0] : parsed;
    } else if (Array.isArray(imgData)) {
      url = imgData[0];
    } else {
      url = imgData;
    }
  } catch (e) { url = imgData; }
  return (url && url.startsWith('http')) ? url : (url ? '/' + url : '');
}

const loadMyProducts = async () => {
  loading.value = true;
  try {
    const res = await myAxios.get('/goods/my/list');
    if (res && Array.isArray(res)) {
      products.value = res.map(item => ({
        ...item,
        coverImage: processImage(item.images || item.coverImage),
        status: STATUS_MAP[item.status] || 'inactive',
        deliveryMethod: item.deliveryMethod || 1,
        campus: item.campus || 'Main Campus',
        address: item.address || ''
      }));
    }
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
}

const handleEdit = (product) => { router.push({ path: '/sell', query: { id: product.id } }); }
const handleMarkAsSold = async (product) => { await myAxios.post('/goods/status', { id: product.id, status: 2 }); ElMessage.success('Marked as sold'); loadMyProducts(); }
const handleDeactivate = async (product) => { await myAxios.post('/goods/status', { id: product.id, status: 3 }); ElMessage.success('Product deactivated'); loadMyProducts(); }
const handleReactivate = async (product) => { await myAxios.post('/goods/status', { id: product.id, status: 1 }); ElMessage.success('Product reactivated'); loadMyProducts(); }
const handleView = (product) => { router.push(`/goods/${product.id}`); }

const filterProducts = (status) => {
  return products.value.filter(p => {
    return p.status === status && 
           (!searchKeyword.value || p.title.toLowerCase().includes(searchKeyword.value.toLowerCase()));
  })
}

const activeProducts = computed(() => filterProducts('active'))
const soldProducts = computed(() => filterProducts('sold'))
const inactiveProducts = computed(() => filterProducts('inactive'))

onMounted(loadMyProducts)
</script>

<style scoped>
.my-products-page { max-width: 1000px; margin: 0 auto; padding: 20px; } /* 稍微收窄，让长条形更好看 */
.page-header { text-align: center; margin-bottom: 30px; }
.page-actions { display: flex; justify-content: space-between; margin-bottom: 20px; }

/* 🌟 核心修改 1：取消网格，改成垂直排列的列表 */
.products-list { display: flex; flex-direction: column; gap: 16px; }

/* 🌟 核心修改 2：卡片内部的水平排版 */
.product-card { 
  border: 1px solid #ebeef5; 
  border-radius: 12px; 
  padding: 20px; 
  display: flex; 
  gap: 24px; 
  background: #fff; 
  transition: all 0.3s ease; 
  align-items: center; /* 垂直居中对齐 */
}
.clickable-card { cursor: pointer; }
.product-card:hover { 
  box-shadow: 0 4px 16px rgba(0,0,0,0.08); 
  transform: translateY(-2px); 
  border-color: #dcdfe6;
}

/* 图片略微放大，并固定大小防止被挤压 */
.product-image { width: 120px; height: 120px; border-radius: 8px; overflow: hidden; position: relative; flex-shrink: 0;}
.product-image img { width: 100%; height: 100%; object-fit: cover; }
.product-status { position: absolute; top: 0; left: 0; font-size: 11px; color: #fff; padding: 4px 8px; border-bottom-right-radius: 8px; font-weight: bold;}
.product-status.active { background: #67c23a; }
.product-status.sold { background: #909399; }
.product-status.draft { background: #e6a23c; }

/* 🌟 核心修改 3：文字区占满剩余空间，解除截断封印 */
.product-info { flex: 1; display: flex; flex-direction: column; justify-content: center; min-width: 0;}
.product-title { 
  font-size: 18px; 
  margin: 0 0 8px; 
  font-weight: 600; 
  color: #303133; 
  white-space: nowrap; 
  overflow: hidden; 
  text-overflow: ellipsis; 
}
.product-price { color: #f56c6c; font-weight: bold; font-size: 20px; margin: 0 0 10px; }

/* 地点栏：再也不用担心名字太长被盖住了 */
.delivery-badge { font-size: 14px; margin-bottom: 8px; }
.delivery-badge .delivery { color: #409EFF; display: flex; align-items: center; gap: 6px; font-weight: 500;}
.delivery-badge .meetup { color: #67c23a; display: flex; align-items: center; gap: 6px; font-weight: 500;}
.sub-location { color: #909399; font-weight: normal; font-size: 13px; margin-left: 6px;}

.meta-item { font-size: 13px; color: #999; display: flex; align-items: center; gap: 4px; }
.sold-info { color: #67c23a; font-size: 14px; margin-top: 10px; font-weight: bold;}

/* 🌟 核心修改 4：右侧按钮区变成整齐的竖排阵列 */
.product-actions { 
  display: flex; 
  flex-direction: column; 
  gap: 12px; 
  justify-content: center; 
  min-width: 140px; 
}
.product-actions .el-button { margin-left: 0; width: 100%;} /* 强制按钮等宽且对齐 */
.product-actions .el-button + .el-button { margin-left: 0 !important; } /* 覆盖 Element UI 默认的兄弟按钮间距 */
</style>