<template>
  <div class="app-wrapper">
    <div class="app-header" v-if="!route.path.startsWith('/admin')">
      <Header />
    </div>

    <div class="app-main" :class="{ 'is-admin-layout': route.path.startsWith('/admin') }">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import Header from '@/components/Header.vue'
import { onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRoute } from 'vue-router' // 🌟 核心新增：引入路由对象
import request from '@/plugins/request.js' 

const userStore = useUserStore()
const route = useRoute() // 🌟 获取当前网页的网址路径
let pollingTimer = null 

const refreshUnreadCount = async () => {
  try {
    const res = await request.get('/conversation/my')
    if (res && Array.isArray(res)) {
      const total = res.reduce((sum, c) => sum + (c.unreadCount || 0), 0)
      userStore.setTotalUnread(total)
    }
  } catch (error) {
    console.warn('Sync unread count failed:', error)
  }
}

onMounted(async () => {
  await userStore.fetchCurrentUser()
  
  if (userStore.isLoggedIn()) {
    refreshUnreadCount() 
    
    pollingTimer = setInterval(() => {
      refreshUnreadCount()
    }, 5000) 
  }
})

onUnmounted(() => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
  }
})
</script>

<style>
body {
  margin: 0;
  padding: 0;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', Arial, sans-serif;
}
.app-wrapper {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}
.app-header {
  position: sticky;
  top: 0;
  z-index: 999;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.app-main {
  flex: 1;
  background-color: #f5f5f5;
  padding: 20px;
  width: 100%;
  box-sizing: border-box;
}

/* 🌟 核心新增：当进入管理员界面时，强制干掉边距，实现无缝全屏暗黑后台！ */
.app-main.is-admin-layout {
  padding: 0 !important;
  background-color: transparent !important;
  display: flex;
  flex-direction: column;
}
</style>