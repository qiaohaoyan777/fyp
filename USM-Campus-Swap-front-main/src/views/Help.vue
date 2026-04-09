<template>
  <div class="help-page">
    <div class="hero-section">
      <h1>Hi, how can we help you today?</h1>
      <p>Search for answers or browse our categories below.</p>
      <div class="search-box">
        <el-input
          v-model="searchQuery"
          placeholder="Search for articles (e.g., 'how to buy', 'wallet')..."
          size="large"
          class="search-input"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </div>

    <div class="category-grid">
      <el-card shadow="hover" class="category-card">
        <el-icon class="card-icon text-blue"><Lock /></el-icon>
        <h3>Account & Safety</h3>
        <p>Email verification, passwords, and security tips.</p>
      </el-card>

      <el-card shadow="hover" class="category-card">
        <el-icon class="card-icon text-green"><ShoppingCart /></el-icon>
        <h3>Buying Guide</h3>
        <p>How to search, negotiate, and purchase items safely.</p>
      </el-card>

      <el-card shadow="hover" class="category-card">
        <el-icon class="card-icon text-orange"><Shop /></el-icon>
        <h3>Selling Guide</h3>
        <p>Posting ads, managing inventory, and closing deals.</p>
      </el-card>

      <el-card shadow="hover" class="category-card">
        <el-icon class="card-icon text-purple"><Wallet /></el-icon>
        <h3>Payment & Delivery</h3>
        <p>Wallet balance, top-ups, and meetup arrangements.</p>
      </el-card>
    </div>

    <div class="faq-section">
      <div class="section-title">
        <h2>Frequently Asked Questions</h2>
        <p>Quick answers to the most common questions from USM students.</p>
      </div>

      <el-collapse v-model="activeNames" class="custom-collapse">
        <el-collapse-item 
          v-for="(faq, index) in filteredFaqs" 
          :key="index" 
          :title="faq.question" 
          :name="index"
        >
          <div class="faq-answer">{{ faq.answer }}</div>
        </el-collapse-item>
      </el-collapse>
      
      <div v-if="filteredFaqs.length === 0" class="no-results">
        <el-empty description="No matching questions found" :image-size="100" />
      </div>
    </div>

    <div class="contact-admin-section">
      <div class="contact-card">
        <div class="contact-info">
          <h3>Still need help?</h3>
          <p>Can't find what you're looking for? Reach out to our official support.</p>
        </div>
        <el-button type="primary" size="large" @click="contactAdmin" class="chat-btn">
          <el-icon class="el-icon--left"><ChatDotRound /></el-icon>
          Chat with Admin
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { 
  Search, Lock, ShoppingCart, Shop, Wallet, ChatDotRound 
} from '@element-plus/icons-vue'

const router = useRouter()
const searchQuery = ref('')
const activeNames = ref([0]) // 默认展开第一个问题

// 纯前端写死的 FAQ 数据，省去数据库建表的麻烦
const faqList = [
  {
    question: "How do I contact the seller?",
    answer: "Click the 'Chat' button on the product detail page to send a direct message to the seller to discuss the item or meetup details."
  },
  {
    question: "Is my money safe if I pay via Wallet Balance?",
    answer: "Yes! Your payment is temporarily held by the system. The seller won't receive the money until you click 'Confirm Receipt'."
  },
  {
    question: "Are there any fees for selling items?",
    answer: "No, posting and selling items on the USM Secondhand platform is 100% free for all students!"
  },
  {
    question: "What should I do after my item is purchased?",
    answer: "Check your 'Messages' for the System Notification. Contact the buyer to arrange a meetup (e.g., at the library or dorm) or delivery, then update the order status."
  },
  {
    question: "Why do I need to verify my USM Email?",
    answer: "To keep our community safe and scam-free, verifying your @student.usm.my email proves you are a legitimate USM student. Verified users get a special green badge!"
  },
  {
    question: "What if I receive a broken item?",
    answer: "Do NOT click 'Confirm Receipt'. Contact the seller first to negotiate a return. If unresolved, you can contact Admin for a dispute."
  }
]

// 简单的搜索过滤逻辑：根据搜索框内容动态过滤 FAQ
const filteredFaqs = computed(() => {
  if (!searchQuery.value) return faqList
  const query = searchQuery.value.toLowerCase()
  return faqList.filter(faq => 
    faq.question.toLowerCase().includes(query) || 
    faq.answer.toLowerCase().includes(query)
  )
})

// 跳转到消息页面与管理员聊天
const contactAdmin = () => {
  // 假设你想让用户直接跳去聊天页面找 Admin
  // 如果你的路由不同，可以修改这里的路径
  router.push('/messages') 
}
</script>

<style scoped>
.help-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 40px 20px;
}

/* 顶部搜索区 */
.hero-section {
  text-align: center;
  margin-bottom: 50px;
  padding: 40px 20px;
  background: linear-gradient(135deg, #f6f8fd 0%, #f1f5f9 100%);
  border-radius: 20px;
}

.hero-section h1 {
  font-size: 36px;
  color: #1e293b;
  margin-bottom: 10px;
  font-weight: 800;
}

.hero-section p {
  color: #64748b;
  font-size: 16px;
  margin-bottom: 30px;
}

.search-box {
  max-width: 600px;
  margin: 0 auto;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05);
  border-radius: 8px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  padding: 8px 15px;
}

/* 分类卡片区 */
.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
  margin-bottom: 60px;
}

.category-card {
  text-align: center;
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
  transition: transform 0.3s ease;
}

.category-card:hover {
  transform: translateY(-5px);
}

.card-icon {
  font-size: 40px;
  margin-bottom: 15px;
}

.text-blue { color: #3b82f6; }
.text-green { color: #10b981; }
.text-orange { color: #f59e0b; }
.text-purple { color: #8b5cf6; }

.category-card h3 {
  font-size: 18px;
  color: #1e293b;
  margin-bottom: 8px;
}

.category-card p {
  font-size: 14px;
  color: #64748b;
  line-height: 1.5;
}

/* FAQ 问答区 */
.faq-section {
  margin-bottom: 60px;
}

.section-title {
  text-align: center;
  margin-bottom: 30px;
}

.section-title h2 {
  font-size: 28px;
  color: #1e293b;
  margin-bottom: 8px;
}

.section-title p {
  color: #64748b;
}

.custom-collapse {
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
}

.custom-collapse :deep(.el-collapse-item__header) {
  font-size: 16px;
  font-weight: 600;
  color: #334155;
  padding: 0 20px;
  background-color: #ffffff;
}

.custom-collapse :deep(.el-collapse-item__header.is-active) {
  background-color: #f8fafc;
  color: #3b82f6;
}

.faq-answer {
  padding: 10px 20px 20px;
  color: #475569;
  font-size: 15px;
  line-height: 1.6;
  background-color: #f8fafc;
}

.no-results {
  padding: 40px 0;
}

/* 联系管理员 CTA */
.contact-admin-section {
  background: #3b82f6;
  border-radius: 16px;
  padding: 30px 40px;
  color: white;
  box-shadow: 0 10px 30px rgba(59, 130, 246, 0.3);
}

.contact-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.contact-info h3 {
  font-size: 24px;
  margin-bottom: 8px;
}

.contact-info p {
  opacity: 0.9;
  font-size: 15px;
}

.chat-btn {
  font-weight: bold;
  border-radius: 8px;
  background-color: white;
  color: #3b82f6;
  border: none;
}

.chat-btn:hover {
  background-color: #f1f5f9;
  color: #2563eb;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .hero-section {
    padding: 30px 15px;
  }
  .contact-card {
    flex-direction: column;
    text-align: center;
  }
  .chat-btn {
    width: 100%;
  }
}
</style>