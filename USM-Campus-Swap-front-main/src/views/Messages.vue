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
              <el-tag v-if="conversation.isSeller" type="success" size="small">Seller</el-tag>
              <el-tag v-if="conversation.isBuyer" type="warning" size="small">Buyer</el-tag>
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
              <p class="user-status">
                <span class="status-dot" :class="activeConversation.isOnline ? 'online' : 'offline'"></span>
                {{ activeConversation.isOnline ? 'Online' : 'Offline' }}
              </p>
            </div>
          </div>
        </div>

        <div class="messages-container" ref="messagesContainer">
          <div
              v-for="message in messages"
              :key="message.id"
              :class="['message-wrapper', message.sender === 'me' ? 'sent' : 'received']"
          >
            <el-avatar
                v-if="message.sender !== 'me'"
                :size="32"
                :src="activeConversation.avatar"
                class="message-avatar"
            />
            <div class="message-content">
              <p class="message-text">{{ message.content }}</p>
              <span class="message-time">{{ formatTime(message.timestamp) }}</span>
            </div>
          </div>
        </div>

        <div class="message-input-container">
          <div class="input-actions">
            <el-button type="text" class="action-btn">
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
              placeholder="Type a message..."
              :maxlength="500"
              show-word-limit
              @keydown.enter.exact.prevent="sendMessage"
          />
          <el-button
              type="primary"
              @click="sendMessage"
              :disabled="!newMessage.trim()"
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
// 1. ✅ 引入 onUnmounted
import { ref, computed, onMounted, nextTick, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ChatDotRound,
  Search,
  User,
  Box,
  Picture,
  Files,
  Promotion
} from '@element-plus/icons-vue'

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

const getUserId = () => {
  const storedUser = localStorage.getItem('user')
  if (storedUser) {
    try {
      return JSON.parse(storedUser).id || 0
    } catch (e) {
      return 0
    }
  }
  return 0
}
const userId = ref(getUserId())
let pollingTimer = null

// 过滤对话列表
const filteredConversations = computed(() => {
  if (!searchKeyword.value) return conversations.value
  return conversations.value.filter(conv =>
      conv.name.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      (conv.lastMessage && conv.lastMessage.toLowerCase().includes(searchKeyword.value.toLowerCase()))
  )
})

// 加载会话列表
const loadConversations = async () => {
  try {
    const res = await request.get('/conversation/my')
    conversations.value = res.map(c => ({
      id: Number(c.id),
      name: c.targetName || 'Unknown User',
      avatar: c.targetAvatar || '',
      lastMessage: c.lastMessage,
      lastTime: c.lastTime,
      unread: 0,
      isSeller: c.sellerId === userId.value,
      isBuyer: c.buyerId === userId.value,
      userId: c.sellerId === userId.value ? c.buyerId : c.sellerId,
      itemId: c.goodsId,
      isOnline: c.isOnline || false
    }))
  } catch (error) {
    console.error(error)
    ElMessage.error('Failed to load conversations')
  }
}

// 选择会话
const selectConversation = async (conversationId) => {
  conversationId = Number(conversationId)
  activeConversation.value = conversations.value.find(conv => conv.id === conversationId)
  if (!activeConversation.value) return
  // 手动点击时，isPolling 为 false
  await loadMessages(conversationId, false)
  scrollToBottom()
}

// 加载消息 (增加 isPolling 参数)
const loadMessages = async (conversationId, isPolling = false) => {
  try {
    const res = await request.get('/message/list', { params: { conversationId } })

    // 如果没有消息，或者数据格式不对，做个保护
    if (!res) return

    messages.value = res.map(m => ({
      id: m.id,
      content: m.content,
      sender: m.senderId === userId.value ? 'me' : 'them',
      timestamp: m.createTime
    }))

    // 只有在非轮询状态下（手动点击），或者用户已经在底部时，才强制滚动
    // 这里简单处理：如果是手动点击，必滚；如果是轮询，暂时不强制滚，防止用户在看历史消息时跳动
    if (!isPolling) {
      scrollToBottom()
    }
  } catch (error) {
    // 轮询时如果出错，只打印日志，不弹窗打扰用户
    if (!isPolling) {
      console.error(error)
      ElMessage.error('Failed to load messages')
    } else {
      console.warn('Polling silent fail:', error)
    }
  }
}

// 发送消息
const sendMessage = async () => {
  if (!newMessage.value.trim() || !activeConversation.value) return
  const msg = {
    conversationId: activeConversation.value.id,
    content: newMessage.value.trim(),
    type: 1
  }
  try {
    await request.post('/message/send', msg)
    newMessage.value = ''
    await loadMessages(activeConversation.value.id, false)
    scrollToBottom()
  } catch (error) {
    console.error(error)
    ElMessage.error('Failed to send message')
  }
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// 格式化时间
const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

// 停止轮询
const stopPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}

// 开启轮询
const startPolling = () => {
  // 先清除之前的，防止重复
  stopPolling()

  pollingTimer = setInterval(() => {
    // 只有当前选中了会话，才请求
    if (activeConversation.value) {
      loadMessages(activeConversation.value.id, true)
    }
  }, 3000)
}

// 生命周期：挂载
onMounted(async () => {
  await loadConversations()
  if (route.query.conversationId) {
    selectConversation(route.query.conversationId)
  }
  startPolling() // 开始轮询
})

// 生命周期：卸载 (离开页面时)
onUnmounted(() => {
  stopPolling() // 停止轮询，防止后台一直请求
})
</script>

<style scoped>
.messages-page {
  display: grid;
  grid-template-columns: 320px 1fr;
  height: 100vh; /* 确保这里填满父容器 */
  background: #f3f4f6;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

/* 左侧对话列表 */
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
  font-weight: 600;
  font-size: 18px;
}

.new-chat-btn {
  color: #409eff;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}

.search-box {
  padding: 12px 20px;
  border-bottom: 1px solid #e5e7eb;
}

.conversations-list {
  flex: 1;
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #f1f5f9;
  transition: background-color 0.2s ease;
  align-items: center;
}

.conversation-item:hover {
  background-color: #f9fafb;
}

.conversation-item.active {
  background-color: #e0f0ff;
  border-left: 4px solid #409eff;
}

.conversation-info {
  flex: 1;
  min-width: 0;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.conversation-name {
  font-weight: 600;
  font-size: 14px;
  color: #1f2937;
}

.conversation-time {
  font-size: 12px;
  color: #9ca3af;
}

.conversation-preview {
  font-size: 13px;
  color: #6b7280;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-meta {
  display: flex;
  gap: 6px;
  align-items: center;
  margin-top: 4px;
}

/* 右侧聊天区域 */
.chat-area {
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

.no-conversation {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
}

.no-conversation-content {
  text-align: center;
}

.no-conversation-content .el-icon {
  font-size: 64px;
  color: #cbd5e1;
  margin-bottom: 12px;
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: white;
  box-shadow: inset 0 0 0 1px #e5e7eb;
}

.chat-header {
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e5e7eb;
  background: #f9fafb;
}

.chat-user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-details h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.user-status {
  margin: 0;
  font-size: 13px;
  color: #6b7280;
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}

.status-dot.online {
  background-color: #10b981;
}

.status-dot.offline {
  background-color: #9ca3af;
}

.chat-actions {
  display: flex;
  gap: 8px;
}

/* 消息列表 */
.messages-container {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: #f3f4f6;
}

.message-wrapper {
  display: flex;
  max-width: 70%;
  gap: 8px;
}

.message-wrapper.sent {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-wrapper.received {
  align-self: flex-start;
  flex-direction: row;
}

.message-content {
  padding: 12px 16px;
  border-radius: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
  word-break: break-word;
  max-width: 100%;
}

.message-wrapper.sent .message-content {
  background-color: #409eff;
  color: white;
}

.message-wrapper.received .message-content {
  background-color: #f1f5f9;
  color: #1f2937;
}

.message-avatar {
  align-self: flex-end;
}

/* 消息输入 */
.message-input-container {
  padding: 16px 20px;
  border-top: 1px solid #e5e7eb;
  display: flex;
  gap: 12px;
  align-items: flex-end;
  background: #f9fafb;
}

.input-actions {
  display: flex;
  gap: 6px;
}

.action-btn {
  color: #6b7280;
}

.action-btn:hover {
  color: #409eff;
}

.message-input-container :deep(.el-textarea) {
  flex: 1;
}

.message-input-container :deep(.el-textarea__inner) {
  border-radius: 12px;
  resize: none;
}

.send-btn {
  padding: 10px 18px;
  border-radius: 12px;
}

/* 滚动条 */
.conversations-list::-webkit-scrollbar,
.messages-container::-webkit-scrollbar {
  width: 6px;
}

.conversations-list::-webkit-scrollbar-thumb,
.messages-container::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}
</style>