<template>
  <div class="help-page">
    <div class="page-header">
      <h1>Help Center</h1>
      <p>Find answers to common questions</p>
    </div>

    <div class="coming-soon">
      <el-result
          icon="info"
          title="Help Page Coming Soon"
          sub-title="This page is under development"
      >
        <template #extra>
          <el-button type="primary" @click="$router.push('/')">Back to Home</el-button>
        </template>
      </el-result>
    </div>
  </div>
</template>

<script setup>
// 页面逻辑将在后续添加
</script>

<style scoped>
.help-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header h1 {
  font-size: 32px;
  margin-bottom: 8px;
}

.coming-soon {
  margin-top: 60px;
}
</style>