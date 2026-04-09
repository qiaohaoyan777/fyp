<template>
  <div class="app-wrapper">
    <div class="app-header">
      <Header />
    </div>
    <div class="app-main">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import Header from '@/components/Header.vue'
import { onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import request from '@/plugins/request.js' 

const userStore = useUserStore()
let pollingTimer = null // 🌟 记录定时器

const refreshUnreadCount = async () => {
  try {
    const res = await request.get('/conversation/my')
    if (res && Array.isArray(res)) {
      const total = res.reduce((sum, c) => sum + (c.unreadCount || 0), 0)
      // 把算出来的总未读数存进全局状态
      userStore.setTotalUnread(total)
    }
  } catch (error) {
    console.warn('Sync unread count failed:', error)
  }
}

onMounted(async () => {
  // 1. 获取用户信息
  await userStore.fetchCurrentUser()
  
  // 2. 如果已登录，开启实时轮询！
  if (userStore.isLoggedIn()) {
    refreshUnreadCount() // 先查一次
    
    // 🌟 新增：每隔 5 秒钟自动偷偷查一次未读消息！这就是“实时红点”的秘诀
    pollingTimer = setInterval(() => {
      refreshUnreadCount()
    }, 5000) 
  }
})

// 🌟 新增：页面销毁时关掉定时器，防止内存泄漏
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
</style>