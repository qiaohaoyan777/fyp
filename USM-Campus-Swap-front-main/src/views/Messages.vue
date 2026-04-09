<template>
  <div class="messages-page">
    <div class="conversations-sidebar">
      <div class="sidebar-header">
        <h2>Messages</h2>
      </div>

      <div class="search-box">
        <el-input
            v-model="searchKeyword"
            placeholder="Search conversations..."
            prefix-icon="Search"
            clearable
        />
      </div>

      <div class="conversations-list">
        <div
            v-for="conversation in filteredConversations"
            :key="conversation.id"
            class="conversation-item"
            :class="{ active: activeConversation?.id === conversation.id }"
            @click="selectConversation(conversation.id)"
        >
          <el-avatar :size="48" :src="conversation.avatar" />
          <div class="conversation-info">
            <div class="conversation-header">
              <h4 class="conversation-name">{{ conversation.name }}</h4>
              <span class="conversation-time">{{ formatTime(conversation.lastTime) }}</span>
            </div>
            <p class="conversation-preview">{{ conversation.lastMessage }}</p>
            <div class="conversation-meta">
              
              <el-tag v-if="conversation.isSystem" type="info" size="small" effect="dark">System</el-tag>
              
              <template v-else>
                <el-tag v-if="conversation.isOpponentSeller" type="success" size="small">Seller</el-tag>
                <el-tag v-else type="warning" size="small">Buyer</el-tag>
              </template>
              
              <el-badge v-if="conversation.unread > 0" :value="conversation.unread" :max="99" class="unread-badge" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="chat-area">
      <div v-if="!activeConversation" class="no-conversation">
        <div class="no-conversation-content">
          <el-icon><ChatDotRound /></el-icon>
          <h3>Select a conversation</h3>
          <p>Choose a conversation from the list to start messaging</p>
        </div>
      </div>

      <div v-else class="chat-container">
        <div class="chat-header">
          <div class="chat-user-info">
            <el-avatar :size="40" :src="activeConversation.avatar" />
            <div class="user-details">
              <h3>{{ activeConversation.name }}</h3>
              
              <p class="user-status" v-if="activeConversation.isSystem">
                <span class="status-dot online"></span>
                24/7 Active
              </p>
              
            </div>
          </div>
        </div>
        
        <div class="chat-product-banner" v-if="activeConversation?.itemId && !activeConversation.isSystem">
          <div class="banner-left">
            <el-icon><Box /></el-icon>
            <span class="item-name-text">Discussing: {{ activeConversation.itemName }}</span>
          </div>
          <el-button 
            size="small" 
            type="primary" 
            plain 
            @click="router.push(`/goods/${activeConversation.itemId}`)"
          >
            View Item
          </el-button>
        </div>

        <div class="messages-container" ref="messagesContainer">
          <template v-for="(message, index) in messages" :key="message.id">
            
            <div class="time-divider" v-if="showTimestamp(index)">
              {{ formatTime(message.timestamp) }}
            </div>

            <div :class="['message-wrapper', message.sender === 'me' ? 'sent' : 'received']">
              <el-avatar
                  v-if="message.sender !== 'me'"
                  :size="32"
                  :src="activeConversation.avatar"
                  class="message-avatar"
              />
              <div class="message-content">
                <p class="message-text">{{ message.content }}</p>
              </div>
            </div>
            
          </template>
        </div>

        <div class="message-input-container">
          <div class="input-actions">
            <el-button type="text" class="action-btn" @click="ElMessage.info('Feature coming soon!')">
              <el-icon><Picture /></el-icon>
            </el-button>
            <el-button type="text" class="action-btn">
              <el-icon><Files /></el-icon>
            </el-button>
          </div>
          <el-input
              v-model="newMessage"
              type="textarea"
              :rows="2"
              :placeholder="activeConversation.isSystem ? 'You cannot reply to system messages.' : 'Type a message...'"
              :disabled="activeConversation.isSystem"
              :maxlength="500"
              show-word-limit
              @keydown.enter.exact.prevent="sendMessage"
          />
          <el-button
              type="primary"
              @click="sendMessage"
              :disabled="!newMessage.trim() || activeConversation.isSystem"
              class="send-btn"
          >
            <el-icon><Promotion /></el-icon>
            Send
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Search, Box, Picture, Files, Promotion } from '@element-plus/icons-vue'
import request from '@/plugins/request.js'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const searchKeyword = ref('')
const activeConversation = ref(null)
const newMessage = ref('')
const messagesContainer = ref(null)
const conversations = ref([])
const messages = ref([])

const localPrevUnread = ref(-1)

const sendSound = new Audio('https://cdn.jsdelivr.net/npm/botpress@10.29.0/lib/web/audio/notification.mp3')
const receiveSound = new Audio('https://cdn.jsdelivr.net/npm/whatsapp-notification-sound@1.0.0/notification.mp3')

const getUserId = () => {
  const storedUser = localStorage.getItem('user')
  return storedUser ? JSON.parse(storedUser).id : 0
}
const userId = ref(getUserId())
let pollingTimer = null

const showTimestamp = (index) => {
  if (index === 0) return true;
  const prevTime = new Date(messages.value[index - 1].timestamp).getTime();
  const currTime = new Date(messages.value[index].timestamp).getTime();
  return (currTime - prevTime) > 5 * 60 * 1000;
}

const filteredConversations = computed(() => {
  if (!searchKeyword.value) return conversations.value
  return conversations.value.filter(conv =>
      conv.name.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      (conv.lastMessage && conv.lastMessage.toLowerCase().includes(searchKeyword.value.toLowerCase()))
  )
})

const loadConversations = async () => {
  try {
    const res = await request.get('/conversation/my')
    let totalUnreadCount = 0;

    conversations.value = res.map(c => {
      const currentUnread = c.unreadCount || 0;
      totalUnreadCount += currentUnread;

      // 🌟 核心判断：如果没有真实名字，或者是系统发送者（比如ID为0），判定为系统消息
      const isSystemMsg = !c.targetName || c.targetName === 'Unknown User' || Number(c.targetId) === 0;
      
      const iAmSeller = Number(userId.value) === Number(c.sellerId);

      return {
        id: Number(c.id),
        name: isSystemMsg ? 'System Notification' : c.targetName,
        // 系统消息使用官方铃铛头像
        avatar: isSystemMsg ? 'https://img.icons8.com/color/48/appointment-reminders--v1.png' : (c.targetAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'),
        lastMessage: c.lastMessage,
        lastTime: c.lastTime,
        unread: currentUnread, 
        isOpponentSeller: !iAmSeller, 
        isSystem: isSystemMsg, // 记录身份
        itemId: c.goodsId,
        itemName: c.itemName || c.goodsName || c.title || 'Product',
      }
    })

    if (localPrevUnread.value !== -1 && totalUnreadCount > localPrevUnread.value) {
      try {
        receiveSound.currentTime = 0; 
        receiveSound.volume = 0.6;
        receiveSound.play();
      } catch(e) { 
        console.warn('浏览器防打扰机制拦截了声音'); 
      }
    }
    
    localPrevUnread.value = totalUnreadCount;

    if (userStore?.setTotalUnread) {
      userStore.setTotalUnread(totalUnreadCount);
    }
  } catch (error) {
    console.error('Failed to load conversations', error)
  }
}

const selectConversation = async (conversationId) => {
  conversationId = Number(conversationId)
  activeConversation.value = conversations.value.find(conv => conv.id === conversationId)
  if (!activeConversation.value) return

  const unreadCount = activeConversation.value.unread
  if (unreadCount > 0) {
    activeConversation.value.unread = 0;
    
    if (userStore?.setTotalUnread) {
      const newTotal = Math.max(0, userStore.totalUnread - unreadCount);
      userStore.setTotalUnread(newTotal);
      localPrevUnread.value = newTotal; 
    }
    
    try {
      await request.post(`/message/clearUnread?conversationId=${conversationId}`)
    } catch (e) {
      console.error(e)
    }
  }
  await loadMessages(conversationId, false)
}

const loadMessages = async (conversationId, isPolling = false) => {
  try {
    const res = await request.get('/message/list', { params: { conversationId } })
    if (!res) return
    
    const oldLength = messages.value.length 
    messages.value = res.map(m => ({
      id: m.id,
      content: m.content,
      sender: m.senderId === userId.value ? 'me' : 'them',
      timestamp: m.createTime
    }))

    if (messages.value.length > oldLength) {
      scrollToBottom()
      
      if (isPolling && activeConversation.value?.id === conversationId) {
        try {
          receiveSound.currentTime = 0; 
          receiveSound.volume = 0.6; 
          receiveSound.play();
        } catch(e) {}
      }
    }
  } catch (error) {
    if (!isPolling) console.error(error)
  }
}

const sendMessage = async () => {
  if (!newMessage.value.trim() || !activeConversation.value || activeConversation.value.isSystem) return
  const msg = {
    conversationId: activeConversation.value.id,
    content: newMessage.value.trim(),
    type: 1
  }
  try {
    await request.post('/message/send', msg)
    newMessage.value = ''
    
    try {
      sendSound.currentTime = 0;
      sendSound.volume = 0.5;
      sendSound.play();
    } catch(e) { }

    await loadMessages(activeConversation.value.id, false)
    loadConversations()
  } catch (error) {
    ElMessage.error('Failed to send')
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      setTimeout(() => {
        if (messagesContainer.value) {
          messagesContainer.value.scrollTo({
            top: messagesContainer.value.scrollHeight,
            behavior: 'smooth'
          });
        }
      }, 50); 
    }
  });
}

const formatTime = (timestamp) => {
  if (!timestamp) return ''
  return new Date(timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

const startPolling = () => {
  pollingTimer = setInterval(() => {
    loadConversations()
    if (activeConversation.value) {
      loadMessages(activeConversation.value.id, true)
    }
  }, 3000) 
}

onMounted(async () => {
  await loadConversations()
  if (route.query.conversationId) {
    selectConversation(route.query.conversationId)
  }
  startPolling()
})

onUnmounted(() => {
  if (pollingTimer) clearInterval(pollingTimer)
})
</script>

<style scoped>
/* -----------------------------------------------------------
   CSS 样式保持不变
   ----------------------------------------------------------- */
.messages-page {
  display: grid;
  grid-template-columns: 320px 1fr;
  height: 100vh;
  background: #f3f4f6;
  overflow: hidden;
}

.conversations-sidebar {
  background: white;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #e5e7eb;
}

.sidebar-header {
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-header h2 {
  font-size: 20px;
  font-weight: 700;
  margin: 0;
  color: #111827;
}

.search-box {
  padding: 15px 20px;
  border-bottom: 1px solid #f3f4f6;
}

.conversations-list {
  flex: 1;
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  padding: 15px 20px;
  gap: 15px;
  cursor: pointer;
  border-bottom: 1px solid #f9fafb;
  transition: all 0.2s;
  align-items: center;
}

.conversation-item:hover {
  background-color: #f9fafb;
}

.conversation-item.active {
  background-color: #eff6ff;
  border-left: 4px solid #3b82f6;
}

.conversation-info {
  flex: 1;
  min-width: 0;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.conversation-name {
  font-weight: 600;
  font-size: 15px;
  color: #111827;
  margin: 0;
}

.conversation-time {
  font-size: 12px;
  color: #9ca3af;
}

.conversation-preview {
  font-size: 13px;
  color: #6b7280;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin: 0;
}

.conversation-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 5px;
}

.chat-area {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f8f9fa;
}

.no-conversation {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
}

.no-conversation-content {
  text-align: center;
}

.no-conversation-content .el-icon {
  font-size: 50px;
  margin-bottom: 15px;
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: white;
}

.chat-header {
  padding: 15px 25px;
  border-bottom: 1px solid #e5e7eb;
  background: #fff;
}

.chat-user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-details h3 {
  margin: 0;
  font-size: 17px;
  font-weight: 600;
}

.user-status {
  margin: 0;
  font-size: 12px;
  color: #6b7280;
  display: flex;
  align-items: center;
  gap: 5px;
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
}

.status-dot.online { background: #10b981; }
.status-dot.offline { background: #9ca3af; }

.chat-product-banner {
  padding: 10px 25px;
  background: #f0f7ff;
  border-bottom: 1px solid #e0e7ff;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-name-text {
  font-weight: 600;
  color: #2563eb;
  font-size: 14px;
}

.messages-container {
  flex: 1;
  padding: 25px;
  overflow-y: auto;
  background: #f3f4f6;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.message-wrapper {
  display: flex;
  max-width: 75%;
  gap: 10px;
}

.message-wrapper.sent {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-content {
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
}

.sent .message-content {
  background: #3b82f6;
  color: white;
}

.received .message-content {
  background: white;
  color: #1f2937;
}

.time-divider {
  text-align: center;
  font-size: 12px;
  color: #9ca3af;
  margin: 10px 0;
}

.message-input-container {
  padding: 20px 25px;
  background: #fff;
  border-top: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.input-actions {
  display: flex;
  gap: 15px;
}

.send-btn {
  align-self: flex-end;
  border-radius: 10px;
}

.messages-container::-webkit-scrollbar { width: 5px; }
.messages-container::-webkit-scrollbar-thumb { background: #d1d5db; border-radius: 10px; }
</style>