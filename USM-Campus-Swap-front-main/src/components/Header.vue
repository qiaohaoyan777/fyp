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
          size="large"
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
        
        <el-badge 
          :value="userStore.totalUnread" 
          :max="99" 
          :hidden="!userStore.totalUnread || userStore.totalUnread === 0"
          class="message-badge"
        >
          <el-button @click="goMessages" circle size="large">
            <el-icon :size="20"><ChatDotRound /></el-icon>
          </el-button>
        </el-badge>
        
        <router-link to="/sell" style="text-decoration: none;">
          <el-button class="sell-button" size="large">
            <el-icon :size="18"><Plus /></el-icon>
            <span style="font-size: 16px; margin-left: 6px; font-weight: bold;">Sell</span>
          </el-button>
        </router-link>
      </div>

      <div class="auth-section">
        <template v-if="!isLogin">
          <el-button link @click="router.push('/login')" style="font-size: 16px;">Login</el-button>
        </template>
        
        <template v-else>
          <el-dropdown trigger="click">
            <span class="user-avatar-wrapper">
              <el-avatar :size="45" :src="userAvatar" />
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
import { ref, computed, watch } from 'vue'
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

const handleSearch = () => {
  const keyword = searchKeyword.value.trim()
  router.push({
    path: '/',
    query: {
      keyword: keyword || undefined 
    }
  })
}

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

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
  padding: 0 20px;
  min-width: 200px;
}

.search-input {
  width: 100%;
  max-width: 400px; 
}

.header-right {
  display: flex;
  align-items: center;
  gap: 30px; 
  flex-shrink: 0;
}

.action-buttons { 
  display: flex; 
  align-items: center; 
  gap: 15px; 
}

/* 🌟 防止红点错位 */
.message-badge {
  display: flex;
  align-items: center;
  justify-content: center;
}

.sell-button {
  background-color: #ff5722;
  border-color: #ff5722;
  color: #fff;
}

.user-avatar-wrapper { 
  display: flex; 
  align-items: center; 
  gap: 5px; 
  cursor: pointer; 
}

@media (max-width: 1100px) {
  .logo-text { display: none; }
}
</style>