<template>
  <div class="product-list">
    <div v-if="products.length === 0" class="empty-state">
      <p>No products found</p>
    </div>

    <div v-else class="products-grid">
      <div
          v-for="product in products"
          :key="product.id"
          class="product-card"
      >
        <div class="product-image">
          <img :src="product.coverImage" :alt="product.title" />
          <div class="product-status" :class="product.status">
            {{ product.status === 'active' ? 'Active' : product.status === 'sold' ? 'Sold' : 'Draft' }}
          </div>
        </div>

        <div class="product-info">
          <h3 class="product-title">{{ product.title }}</h3>
          <p class="product-price">RM{{ product.price }}</p>
          <div class="product-meta">
            <span class="campus">{{ product.campus }}</span>
            <span class="views">
              <el-icon><View /></el-icon>
              {{ product.viewCount }}
            </span>
          </div>

          <div class="product-stats">
            <span v-if="product.messages" class="stat">
              <el-icon><ChatDotRound /></el-icon>
              {{ product.messages }} messages
            </span>
            <span v-if="product.favorites" class="stat">
              <el-icon><Star /></el-icon>
              {{ product.favorites }} favorites
            </span>
          </div>

          <div v-if="product.soldAt" class="sold-info">
            Sold on {{ product.soldAt }} for RM{{ product.soldPrice }}
          </div>
        </div>

        <div class="product-actions">
          <el-button
              v-if="product.status === 'active'"
              type="success"
              @click="$emit('sold', product)"
              class="action-btn"
          >
            Mark as Sold
          </el-button>
          <el-button
              v-if="product.status === 'draft'"
              type="primary"
              @click="$emit('publish', product)"
              class="action-btn"
          >
            Publish
          </el-button>
          <el-button
              @click="$emit('edit', product)"
              class="action-btn"
          >
            Edit
          </el-button>
          <el-button
              v-if="product.status !== 'sold'"
              @click="$emit('view', product)"
              class="action-btn"
          >
            View
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { View, ChatDotRound, Star } from '@element-plus/icons-vue'

defineProps({
  products: {
    type: Array,
    default: () => []
  }
})

defineEmits(['edit', 'sold', 'publish', 'view'])
</script>

<style scoped>
.product-list {
  width: 100%;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #6b7280;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.product-card {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  gap: 16px;
  transition: all 0.3s ease;
}

.product-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.product-image {
  position: relative;
  width: 100px;
  height: 100px;
  flex-shrink: 0;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 6px;
}

.product-status {
  position: absolute;
  top: 4px;
  right: 4px;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 10px;
  color: white;
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
}

.product-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #1f2937;
  line-height: 1.4;
}

.product-price {
  font-size: 16px;
  font-weight: bold;
  color: #ef4444;
  margin-bottom: 8px;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 12px;
  color: #6b7280;
}

.product-meta .views {
  display: flex;
  align-items: center;
  gap: 4px;
}

.product-stats {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
}

.stat {
  font-size: 11px;
  color: #6b7280;
  display: flex;
  align-items: center;
  gap: 4px;
}

.sold-info {
  font-size: 12px;
  color: #10b981;
  font-weight: 500;
}

.product-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  justify-content: center;
}

.action-btn {
  min-width: 100px;
}

@media (max-width: 768px) {
  .products-grid {
    grid-template-columns: 1fr;
  }

  .product-card {
    flex-direction: column;
  }

  .product-image {
    width: 100%;
    height: 150px;
  }

  .product-actions {
    flex-direction: row;
    flex-wrap: wrap;
  }

  .action-btn {
    flex: 1;
    min-width: auto;
  }
}
</style>