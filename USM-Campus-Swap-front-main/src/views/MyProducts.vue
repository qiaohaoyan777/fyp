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
              <div class="tags-row">
                 <span class="category-badge">{{ product.categoryName }}</span>
                 <span v-if="product.specifics" class="specific-badge">{{ product.specifics }}</span>
              </div>
              
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
              <div class="tags-row">
                 <span class="category-badge">{{ product.categoryName }}</span>
                 <span v-if="product.specifics" class="specific-badge">{{ product.specifics }}</span>
              </div>

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
              <div class="tags-row">
                 <span class="category-badge">{{ product.categoryName }}</span>
                 <span v-if="product.specifics" class="specific-badge">{{ product.specifics }}</span>
              </div>

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
const categories = ref([])

const STATUS_MAP = { 1: 'active', 2: 'sold', 3: 'inactive' }

// 🌟 新增：先获取所有商品类别
const loadCategories = async () => {
  try {
    const res = await myAxios.get('/category/list');
    categories.value = res || [];
  } catch (error) {
    console.error('Failed to load categories', error);
  }
}

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
      products.value = res.map(item => {
        // 🌟 动态匹配出这个商品的种类名称
        const matchedCategory = categories.value.find(c => String(c.id) === String(item.categoryId));
        const catName = matchedCategory ? matchedCategory.name : 'General';

        return {
          ...item,
          categoryName: catName, // 保存种类名称
          specifics: item.specifics || '', // 保存属性细节
          coverImage: processImage(item.images || item.coverImage),
          status: STATUS_MAP[item.status] || 'inactive',
          deliveryMethod: item.deliveryMethod || 1,
          campus: item.campus || 'Main Campus',
          address: item.address || ''
        };
      });
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

// 🌟 确保先加载分类，再去请求商品数据
onMounted(async () => {
  await loadCategories();
  loadMyProducts();
})
</script>

<style scoped>
.my-products-page { max-width: 1000px; margin: 0 auto; padding: 20px; } 
.page-header { text-align: center; margin-bottom: 30px; }
.page-actions { display: flex; justify-content: space-between; margin-bottom: 20px; }

.products-list { display: flex; flex-direction: column; gap: 16px; }

.product-card { 
  border: 1px solid #ebeef5; 
  border-radius: 12px; 
  padding: 20px; 
  display: flex; 
  gap: 24px; 
  background: #fff; 
  transition: all 0.3s ease; 
  align-items: center; 
}
.clickable-card { cursor: pointer; }
.product-card:hover { 
  box-shadow: 0 4px 16px rgba(0,0,0,0.08); 
  transform: translateY(-2px); 
  border-color: #dcdfe6;
}

/* 🌟 图片：改成 contain 并加上底色防裁切 */
.product-image { 
  width: 120px; 
  height: 120px; 
  border-radius: 8px; 
  overflow: hidden; 
  position: relative; 
  flex-shrink: 0;
  background-color: #f8fafc;
  border: 1px solid #f1f5f9;
}
.product-image img { 
  width: 100%; 
  height: 100%; 
  object-fit: contain; /* 防止长图被裁切 */
  padding: 8px; 
  box-sizing: border-box;
}

.product-status { position: absolute; top: 0; left: 0; font-size: 11px; color: #fff; padding: 4px 8px; border-bottom-right-radius: 8px; font-weight: bold; z-index: 2;}
.product-status.active { background: #67c23a; }
.product-status.sold { background: #909399; }
.product-status.draft { background: #e6a23c; }

.product-info { flex: 1; display: flex; flex-direction: column; justify-content: center; min-width: 0;}

/* 🌟 新增：标签区的样式 */
.tags-row { display: flex; gap: 8px; align-items: center; margin-bottom: 8px; flex-wrap: wrap; }
.category-badge { font-size: 11px; font-weight: 700; color: #4f46e5; background: #e0e7ff; padding: 3px 8px; border-radius: 6px; text-transform: uppercase; letter-spacing: 0.5px; }
.specific-badge { font-size: 11px; font-weight: 700; color: white; background: rgba(79, 70, 229, 0.9); padding: 3px 8px; border-radius: 6px; }

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

.delivery-badge { font-size: 14px; margin-bottom: 8px; }
.delivery-badge .delivery { color: #409EFF; display: flex; align-items: center; gap: 6px; font-weight: 500;}
.delivery-badge .meetup { color: #67c23a; display: flex; align-items: center; gap: 6px; font-weight: 500;}
.sub-location { color: #909399; font-weight: normal; font-size: 13px; margin-left: 6px;}

.meta-item { font-size: 13px; color: #999; display: flex; align-items: center; gap: 4px; }
.sold-info { color: #67c23a; font-size: 14px; margin-top: 10px; font-weight: bold;}

.product-actions { 
  display: flex; 
  flex-direction: column; 
  gap: 12px; 
  justify-content: center; 
  min-width: 140px; 
}
.product-actions .el-button { margin-left: 0; width: 100%;}
.product-actions .el-button + .el-button { margin-left: 0 !important; }
</style>