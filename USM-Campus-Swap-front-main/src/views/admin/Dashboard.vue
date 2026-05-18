<template>
  <div class="admin-container">
    <div class="admin-sidebar">
      <div class="sidebar-brand">
        <el-icon class="brand-icon"><WarnTriangleFilled /></el-icon>
        <div class="brand-text">
          <h1>CS Marketplace</h1>
          <p>Higher Authority Portal</p>
        </div>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        class="admin-menu"
        background-color="#0f172a"
        text-color="#94a3b8"
        active-text-color="#ffffff"
        @select="handleMenuSelect"
      >
        <el-menu-item index="users">
          <el-icon><User /></el-icon>
          <span>User Master Control</span>
        </el-menu-item>
        <el-menu-item index="goods">
          <el-icon><Goods /></el-icon>
          <span>Global Goods Audit</span>
        </el-menu-item>
        <el-menu-item index="orders">
          <el-icon><List /></el-icon>
          <span>Transaction Auditing</span>
        </el-menu-item>
        <el-menu-item index="logout" class="logout-menu-item">
          <el-icon><SwitchButton /></el-icon>
          <span>Terminate Session</span>
        </el-menu-item>
      </el-menu>
    </div>

    <div class="admin-main-viewport">
      <div class="admin-viewport-header">
        <div class="header-title-area">
          <span class="system-badge">SYSTEM SECURE</span>
          <h2>
            <span v-if="activeMenu === 'users'">Authentication & User Registry</span>
            <span v-else-if="activeMenu === 'goods'">Centralized Marketplace Inventory</span>
            <span v-else>Global Ledger / Order History</span>
          </h2>
        </div>
        <div class="header-admin-profile">
          <div class="admin-meta">
            <span class="admin-role">ROOT_ADMINISTRATOR</span>
            <span class="admin-status-dot"></span>
          </div>
          <el-tag type="danger" effect="dark">LEVEL 1 AUTH</el-tag>
        </div>
      </div>

      <div class="admin-workspace-card">
        
        <el-table v-if="activeMenu === 'users'" :data="userList" v-loading="loading" style="width: 100%" stripe>
          <el-table-column prop="id" label="UID" width="80" />
          <el-table-column label="Profile" width="90">
            <template #default="scope">
              <el-avatar :size="38" :src="getSafeImg(scope.row) || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
            </template>
          </el-table-column>
          <el-table-column prop="username" label="Student Name" width="150" />
          <el-table-column prop="usmEmail" label="USM Registered Email" min-width="220" />
          
          <el-table-column label="Account Status" width="130">
            <template #default="scope">
              <el-tag :type="scope.row.isDelete == 1 ? 'danger' : 'success'" effect="plain">
                {{ scope.row.isDelete == 1 ? 'Banned' : 'Authorized' }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="Administrative Action" min-width="200" fixed="right">
            <template #default="scope">
              <el-button size="small" type="warning" plain @click="warnUser(scope.row)">
                Warn
              </el-button>
              
              <el-button v-if="scope.row.isDelete != 1" size="small" type="danger" @click="banUser(scope.row)">
                Restrict User
              </el-button>
              <el-button v-else size="small" type="success" plain @click="unbanUser(scope.row)">
                Lift Restriction
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-table v-if="activeMenu === 'goods'" :data="goodsList" v-loading="loading" style="width: 100%" stripe>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="Product Image" width="100">
            <template #default="scope">
              <el-image :src="getSafeImg(scope.row)" class="workspace-goods-img" fit="cover">
                <template #error>
                  <div class="image-slot-error"><el-icon><Picture /></el-icon></div>
                </template>
              </el-image>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="Product Title" min-width="180" show-overflow-tooltip />
          <el-table-column label="Market Price" width="130">
            <template #default="scope">
              <span class="workspace-currency">RM {{ Number(scope.row.price || scope.row.amount || 0).toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="sellerId" label="Seller UID" width="100" />
          
          <el-table-column label="Status" width="120">
            <template #default="scope">
              <el-tag :type="scope.row.status == 1 ? 'info' : 'success'" :effect="scope.row.status == 1 ? 'dark' : 'light'">
                {{ scope.row.status == 1 ? 'Off-market' : 'Active Listing' }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="Administrative Action" min-width="180" fixed="right">
            <template #default="scope">
              <el-button v-if="scope.row.status == 0" size="small" type="warning" plain @click="takeDownGoods(scope.row)">
                Takedown
              </el-button>
              <el-button v-else size="small" type="success" plain @click="restoreGoods(scope.row)">
                Re-list
              </el-button>
              
              <el-button size="small" type="danger" @click="deleteGoods(scope.row)">
                Delete
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-table v-if="activeMenu === 'orders'" :data="orderList" v-loading="loading" style="width: 100%" stripe>
          <el-table-column prop="id" label="System Order ID" width="160" />
          <el-table-column label="Target Item" min-width="220">
            <template #default="scope">
              <div class="order-goods-cell">
                <el-tag size="small" type="info" effect="plain" class="order-id-tag">Goods ID: {{ scope.row.goodsId }}</el-tag>
                <span class="order-goods-title" :title="scope.row._smartTitle">
                  {{ scope.row._smartTitle }}
                </span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="buyerId" label="Buyer UID" width="120" />
          <el-table-column prop="sellerId" label="Seller UID" width="120" />
          <el-table-column label="Total Value" width="160">
            <template #default="scope">
              <span class="workspace-currency">RM {{ Number(scope.row._smartPrice || 0).toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="Transaction Status">
            <template #default="scope">
              <el-tag v-if="scope.row.status == 0" type="warning" effect="light">Escrow / Awaiting Pay</el-tag>
              <el-tag v-else-if="scope.row.status == 1" type="primary" effect="light">Processing Delivery</el-tag>
              <el-tag v-else-if="scope.row.status == 2" type="success" effect="dark">Completed</el-tag>
              <el-tag v-else type="info" effect="plain">Cancelled</el-tag>
            </template>
          </el-table-column>
        </el-table>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { User, Goods, List, SwitchButton, WarnTriangleFilled, Picture } from '@element-plus/icons-vue'
import myAxios from "@/plugins/request.js"

const router = useRouter()
const activeMenu = ref('users')
const loading = ref(false)

const userList = ref([])
const goodsList = ref([])
const orderList = ref([])

// ==========================================
// 🛠️ 核心工具函数区
// ==========================================

// 通用数据提取器
const extractData = (res) => {
  if (!res) return [];
  if (res.data && res.status) res = res.data;
  if (res && res.code !== undefined && res.data !== undefined) res = res.data;
  if (res && res.records && Array.isArray(res.records)) return res.records;
  return Array.isArray(res) ? res : [];
}

// 图片解析与脱敏
const cleanImgUrl = (url) => {
  if (!url) return '';
  if (typeof url === 'string' && url.includes('[')) {
    try {
      const parsed = JSON.parse(url);
      return parsed[0] || ''; 
    } catch (e) {
      return url.replace(/[\[\]"']/g, '').split(',')[0].trim();
    }
  }
  return url;
}

// 终极安全图片雷达
const getSafeImg = (row) => {
  if (!row) return '';
  const possibleKeys = ['image', 'images', 'imgUrl', 'imageUrl', 'goodsImage', 'cover', 'coverUrl', 'picture', 'pic', 'avatar', 'avatarUrl', 'photo'];
  for (const key of possibleKeys) {
    if (row[key] && typeof row[key] === 'string' && row[key].length > 10) {
      return cleanImgUrl(row[key]);
    }
  }
  for (const key in row) {
    const val = row[key];
    if (val && typeof val === 'string') {
      if (val.includes('http') || val.includes('uploads') || val.includes('.jpg') || val.includes('.png')) {
        return cleanImgUrl(val);
      }
    }
  }
  return '';
}

// ==========================================
// 🚀 数据加载区
// ==========================================

const handleMenuSelect = (index) => {
  if (index === 'logout') {
    ElMessageBox.confirm('Are you sure you want to terminate this secure session?', 'System Alert', { 
      confirmButtonText: 'Exit', 
      cancelButtonText: 'Cancel', 
      type: 'error' 
    }).then(async () => { 
      
      try {
        // 🚀 1. 呼叫后端，彻底销毁后端的 Session（对应你 UserController 里的 /logout 接口）
        await myAxios.post('/user/logout');
      } catch (e) {
        console.log("Backend logout sync failed, forcing local wipe.");
      }

      // 🚀 2. 物理清空本地所有的缓存
      localStorage.removeItem('user');
      localStorage.clear();
      sessionStorage.clear();

      // 🚀 3. 终极拔线：放弃 router.push，直接使用 window.location 强制重载！
      // 这会瞬间清空所有的 Pinia 内存状态，任何路由守卫都拦不住你！
      window.location.href = '/login'; 
      
    }).catch(() => {
      // 捕获取消点击，防止控制台红字报错
    })
    return
  }
  
  activeMenu.value = index
  fetchData()
}

const fetchData = () => {
  if (activeMenu.value === 'users') fetchUsers()
  if (activeMenu.value === 'goods') fetchGoods()
  if (activeMenu.value === 'orders') fetchOrders()
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await myAxios.get('/user/list')
    userList.value = extractData(res)
  } catch (e) { ElMessage.error('Failed to synchronize user registry.') } 
  finally { loading.value = false }
}

const fetchGoods = async () => {
  loading.value = true
  try {
    const res = await myAxios.get('/goods/admin/list')
    goodsList.value = extractData(res)
  } catch (e) { ElMessage.error('Failed to synchronize goods audit.') } 
  finally { loading.value = false }
}

const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await myAxios.get('/order/admin/list')
    let rawOrders = extractData(res)

    // 前端强行连表查询：自动拉取商品池
    if (!goodsList.value || goodsList.value.length === 0) {
      try {
        const goodsRes = await myAxios.get('/goods/admin/list')
        goodsList.value = extractData(goodsRes)
      } catch(err) {}
    }

    // 智能缝合商品名称与价格
    orderList.value = rawOrders.map(order => {
      const targetGoods = goodsList.value.find(g => g.id === order.goodsId) || {};
      return {
        ...order,
        _smartTitle: order.goodsTitle || order.title || order.goodsName || targetGoods.title || targetGoods.goodsName || '[Item Info Missing]',
        _smartPrice: parseFloat(order.amount || order.price || order.totalPrice || targetGoods.price || targetGoods.amount || 0)
      }
    })
  } catch (e) { 
    ElMessage.error('Failed to synchronize transaction ledger.') 
  } finally { 
    loading.value = false 
  }
}

// ==========================================
// 🛡️ 管理员最高权限操作区 (Optimistic UI Updates)
// ==========================================

// 1. 发送严重警告
const warnUser = (user) => {
  ElMessageBox.prompt('Enter the warning message to send to this user:', 'Issue Official Warning', {
    confirmButtonText: 'Send Warning',
    cancelButtonText: 'Cancel',
    inputPattern: /.+/,
    inputErrorMessage: 'Warning message cannot be empty',
  }).then(async ({ value }) => {
    try {
      await myAxios.post(`/user/warn`, { id: user.id, message: value });
      ElNotification({ title: 'Warning Dispatched', message: `UID: ${user.id} has been warned.`, type: 'warning', duration: 3000 });
    } catch (e) {
      ElMessage.error('Failed to send warning.');
    }
  }).catch(() => {});
}

// 2. 封禁用户 (逻辑删除 isDelete = 1)
const banUser = async (user) => {
  try {
    user.isDelete = 1; // 乐观变红
    await myAxios.post(`/user/update/status`, { id: user.id, status: 1 });
    ElNotification({ title: 'Authority Executed', message: `UID: ${user.id} has been securely restricted.`, type: 'error', duration: 3000 });
  } catch (e) { 
    user.isDelete = 0; // 回滚
    ElMessage.error('Action intercepted by server.');
  }
}

// 3. 解封用户 (恢复 isDelete = 0)
const unbanUser = async (user) => {
  try {
    user.isDelete = 0; // 乐观变绿
    await myAxios.post(`/user/update/status`, { id: user.id, status: 0 });
    ElNotification({ title: 'Authority Executed', message: `Restriction lifted for UID: ${user.id}.`, type: 'success', duration: 3000 });
  } catch (e) { 
    user.isDelete = 1; // 回滚
    ElMessage.error('Action intercepted by server.');
  }
}

// 4. 下架违规商品 (status = 1)
const takeDownGoods = async (item) => {
  try {
    item.status = 1; // 乐观变灰
    await myAxios.post(`/goods/update`, { id: item.id, status: 1 });
    ElNotification({ title: 'Audit Takedown', message: `Goods ID: ${item.id} taken off-market.`, type: 'warning', duration: 3000 });
  } catch (e) { 
    item.status = 0;
    ElMessage.error('Action intercepted by server.');
  }
}

// 5. 恢复商品上架 (status = 0)
const restoreGoods = async (item) => {
  try {
    item.status = 0; // 乐观变绿
    await myAxios.post(`/goods/update`, { id: item.id, status: 0 });
    ElNotification({ title: 'Audit Re-list', message: `Goods ID: ${item.id} officially re-listed.`, type: 'success', duration: 3000 });
  } catch (e) { 
    item.status = 1;
    ElMessage.error('Action intercepted by server.');
  }
}

// 6. 物理抹除商品 (调用专属 admin/delete 接口)
const deleteGoods = (item) => {
  ElMessageBox.confirm(
    'Are you sure you want to permanently delete this non-compliant item? This action CANNOT be undone.',
    'CRITICAL SYSTEM WARNING',
    { confirmButtonText: 'Permanently Delete', cancelButtonText: 'Cancel', type: 'error' }
  ).then(async () => {
    try {
      await myAxios.post(`/goods/admin/delete`, { id: item.id });
      ElMessage.success(`Goods ID: ${item.id} has been permanently erased.`);
      fetchGoods(); // 删除后刷新列表
    } catch (e) {
      ElMessage.error('Deletion failed. System rejection.');
    }
  }).catch(() => {});
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.admin-container {
  display: grid;
  grid-template-columns: 280px 1fr;
  height: 100vh;
  background-color: #0b0f19 !important;
  color: #f1f5f9;
}
.admin-sidebar { background-color: #0f172a; border-right: 1px solid #1e293b; display: flex; flex-direction: column; }
.sidebar-brand { padding: 30px 24px; display: flex; align-items: center; gap: 12px; background-color: #020617; border-bottom: 1px solid #1e293b; }
.brand-icon { font-size: 24px; color: #f43f5e; }
.brand-text h1 { font-size: 16px; font-weight: 700; color: #ffffff; margin: 0; }
.brand-text p { font-size: 11px; color: #64748b; margin: 2px 0 0 0; }
.admin-menu { border-right: none; flex: 1; padding-top: 15px; }
:deep(.el-menu-item) { height: 54px; line-height: 54px; margin: 4px 12px; border-radius: 8px; }
:deep(.el-menu-item.is-active) { background: linear-gradient(90deg, #1e293b 0%, #0f172a 100%) !important; font-weight: 600; box-shadow: inset 4px 0 0 #f43f5e; }
.logout-menu-item { margin-top: auto !important; margin-bottom: 25px !important; color: #f43f5e !important; }
.admin-main-viewport { padding: 35px; display: flex; flex-direction: column; overflow-y: auto; background-color: #0f172a; }
.admin-viewport-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; border-bottom: 1px solid #1e293b; padding-bottom: 20px; }
.system-badge { font-size: 10px; font-weight: 700; color: #38bdf8; background: rgba(56, 189, 248, 0.1); padding: 3px 8px; border-radius: 4px; }
.header-title-area h2 { font-size: 24px; font-weight: 700; color: #ffffff; margin: 8px 0 0 0; }
.header-admin-profile { display: flex; align-items: center; gap: 15px; }
.admin-meta { display: flex; align-items: center; gap: 8px; }
.admin-role { font-size: 12px; color: #94a3b8; }
.admin-status-dot { width: 8px; height: 8px; background-color: #22c55e; border-radius: 50%; box-shadow: 0 0 8px #22c55e; }
.admin-workspace-card { background: #1e293b; padding: 24px; border-radius: 12px; border: 1px solid #334155; flex: 1; }

:deep(.el-table) { background-color: #1e293b !important; color: #cbd5e1 !important; --el-table-border-color: #334155 !important; }
:deep(.el-table th.el-table__cell) { background-color: #0f172a !important; color: #64748b !important; font-weight: 700; border-bottom: 1px solid #334155 !important; }
:deep(.el-table tr), :deep(.el-table td.el-table__cell) { background-color: #1e293b !important; border-bottom: 1px solid #334155 !important; }
:deep(.el-table__body tr:hover > td.el-table__cell),
:deep(.el-table__body tr.hover-row > td.el-table__cell),
:deep(.el-table__body tr.current-row > td.el-table__cell) { background-color: #243249 !important; color: #ffffff !important; }
:deep(.el-table--stripe .el-table__body tr.el-table__row--striped td.el-table__cell) { background-color: #161e2b !important; }

.workspace-goods-img { width: 52px; height: 52px; border-radius: 8px; border: 1px solid #475569; background: #0f172a; }
.image-slot-error { display: flex; justify-content: center; align-items: center; width: 52px; height: 52px; background: #334155; color: #64748b; border-radius: 8px; }
.workspace-currency { color: #f43f5e; font-weight: 700; font-family: monospace; }
.order-goods-cell { display: flex; flex-direction: column; gap: 6px; align-items: flex-start; justify-content: center; }
.order-id-tag { transform: scale(0.9); transform-origin: left center; }
.order-goods-title { font-size: 14px; font-weight: 500; color: #f8fafc; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 180px; }
</style>