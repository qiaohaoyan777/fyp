<template>
  <div class="header">
    <div class="header-left">
      <router-link to="/" class="logo">
        <el-icon><Shop /></el-icon>
        <span class="logo-text">Campus Swap</span>
      </router-link>

      <el-menu
          mode="horizontal"
          :default-active="activeIndex"
          router
          class="main-nav"
          :ellipsis="false"
      >
        <el-menu-item index="/">Home</el-menu-item>
        <el-menu-item index="/my-products">My Products</el-menu-item>
        <el-menu-item index="/my-wishlist">Wishlist</el-menu-item>
        <el-menu-item index="/orders">Orders</el-menu-item>

        <el-sub-menu index="admin" v-if="userStore.userInfo?.userRole === 1">
          <template #title>
            <el-icon><Management /></el-icon>
            <span>Admin</span>
          </template>
          <el-menu-item index="/admin/goods">Manage Goods</el-menu-item>
          <el-menu-item index="/admin/orders">Manage Orders</el-menu-item>
          <el-menu-item index="/admin/users">Manage Users</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/help">Help</el-menu-item>
      </el-menu>
    </div>

    <div class="header-center">
      <el-input
          v-model="searchKeyword"
          placeholder="Search products..."
          class="search-input"
          @keyup.enter="handleSearch"
          clearable
      >
        <template #append>
          <el-button icon="Search" @click="handleSearch"></el-button>
        </template>
      </el-input>
    </div>

    <div class="header-right">
      <div class="action-buttons">
        <el-button icon="ChatDotRound" @click="goMessages" circle></el-button>
        <router-link to="/sell">
          <el-button class="sell-button" icon="Plus">Sell</el-button>
        </router-link>
      </div>

      <div class="auth-section">
        <template v-if="!isLogin">
          <el-button link @click="router.push('/login')">Login</el-button>
        </template>
        <template v-else>
          <el-dropdown trigger="click">
            <span class="user-avatar-wrapper">
              <el-avatar :size="32" :src="userAvatar" />
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/profile')">Profile</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">Logout</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue' // ★ 1. 引入 watch
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  Shop, House, Star, Collection, Document,
  ChatDotRound, Plus, Search, ArrowDown,
  User, SwitchButton, Management
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const searchKeyword = ref('')
const activeIndex = computed(() => route.path)

const isLogin = computed(() => !!userStore.userInfo)
const userAvatar = computed(() => userStore.userInfo?.avatarUrl || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png')
const userName = computed(() => userStore.userInfo?.username || 'User')

// ★ 2. 修复后的搜索逻辑
const handleSearch = () => {
  const keyword = searchKeyword.value.trim()

  // 跳转到首页，并带上查询参数
  router.push({
    path: '/',
    query: {
      keyword: keyword || undefined // 如果为空就不传
    }
  })
}

// ★ 3. 监听路由变化，保持搜索框内容同步
// (例如：用户点了浏览器的后退按钮，搜索框里的字也应该变回去)
watch(
    () => route.query.keyword,
    (newVal) => {
      searchKeyword.value = newVal || ''
    },
    { immediate: true }
)

const handleLogout = async () => {
  await userStore.logout()
  router.push('/login')
}

const goMessages = () => router.push('/messages')
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  height: 64px;
  padding: 0 20px;
  background: #fff;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  z-index: 1000;
}

.header-left {
  display: flex;
  align-items: center;
  flex: 2;
  min-width: 0;
}

.logo {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: #409eff;
  font-weight: bold;
  margin-right: 20px;
  flex-shrink: 0;
}

.logo-text { font-size: 18px; margin-left: 8px; }

.main-nav {
  border: none !important;
  height: 64px;
  flex: 1;
}

/* 搜索栏样式优化 */
.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
  padding: 0 20px;
  min-width: 200px;
}

/* ★ 4. 确保这里没有多余的嵌套，宽度才能生效 */
.search-input {
  width: 100%;
  max-width: 400px; /* 稍微加宽一点，视觉更舒适 */
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
  flex-shrink: 0;
}

.action-buttons { display: flex; align-items: center; gap: 10px; }

.sell-button {
  background-color: #ff5722;
  border-color: #ff5722;
  color: #fff;
}

.user-avatar-wrapper { display: flex; align-items: center; gap: 5px; cursor: pointer; }

@media (max-width: 1100px) {
  .logo-text { display: none; }
}
</style>