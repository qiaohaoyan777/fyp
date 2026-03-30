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
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 当 App 挂载时，尝试获取当前用户信息
onMounted(() => {
  userStore.fetchCurrentUser()
})
</script>

<style>
/* 全局重置 body 边距，防止页面出现白边 */
body {
  margin: 0;
  padding: 0;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', Arial, sans-serif;
}

/* Flex 布局容器 */
.app-wrapper {
  display: flex;
  flex-direction: column;
  min-height: 100vh; /* 占满全屏高度 */
}

/* 头部样式：固定在顶部 */
.app-header {
  position: sticky;
  top: 0;
  z-index: 999; /* 保证头部在最上层 */
  background-color: #fff;
}

/* 主体内容：自动撑开剩余高度 */
.app-main {
  flex: 1; /* 占据剩余空间 */
  background-color: #f5f5f5;
  padding: 20px;
  /* 解决路由切换时可能的溢出问题 */
  width: 100%;
  box-sizing: border-box;
}
</style>