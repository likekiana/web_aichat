<!-- src/views/ChatRoom.vue -->
<template>
  <div class="chat-room">
    <!-- 页面标题 -->
    <div class="page-title">
      <h1>AI脑筋急转弯</h1>
    </div>

    <!-- 房间头部 -->
    <div class="room-header">
      <div class="room-info">
        <span class="label">房间号:</span>
        <span class="room-id">{{ roomId }}</span>
        <a-button size="small" @click="loadRoomHistory" :loading="loadingHistory">
          <template #icon><ReloadOutlined /></template>
          刷新历史
        </a-button>
      </div>
    </div>

    <!-- 聊天区域 -->
    <div class="chat-area">
      <div class="messages" ref="messagesRef">
        <div 
          v-for="(message, index) in messages" 
          :key="index"
          :class="['message', message.type]"
        >
          <div class="avatar">
            <img :src="message.type === 'ai' ? aiAvatar : userAvatar" />
          </div>
          <div class="message-content">
            <div class="bubble">
              {{ message.content }}
            </div>
            <div class="timestamp">
              {{ formatMessageTime(message.timestamp) }}
            </div>
          </div>
        </div>
        
        <div v-if="messages.length === 0 && !loadingHistory" class="empty-state">
          <p>点击"开始"按钮开始游戏</p>
        </div>
        
        <div v-if="loadingHistory" class="loading-state">
          <a-spin tip="加载历史消息..." />
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="input-area">
      <!-- 操作按钮区域 -->
      <div class="action-buttons">
        <a-button 
          type="primary" 
          :disabled="gameStarted"
          @click="handleStart"
          class="start-btn"
        >
          开始
        </a-button>
        <a-button 
          danger
          :disabled="gameEnded"
          @click="handleEnd"
          class="end-btn"
        >
          结束
        </a-button>
      </div>

      <!-- 输入框区域 -->
      <div class="input-container">
        <a-textarea
          v-model:value="userInput"
          placeholder="输入消息..."
          :rows="3"
          :disabled="gameEnded"
          @pressEnter="handleSend"
          class="input-textarea"
        />
        <a-button 
          type="primary" 
          @click="handleSend"
          :disabled="!userInput.trim() || gameEnded"
          class="send-btn"
        >
          发送
        </a-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import { chatApi } from '../services/api'

interface ChatMessage {
  type: 'ai' | 'user'
  content: string
  timestamp: string
}

const route = useRoute()
const messagesRef = ref<HTMLDivElement>()

const roomId = ref(route.params.roomId as string)
const userInput = ref('')
const messages = ref<ChatMessage[]>([])
const gameStarted = ref(false)
const gameEnded = ref(false)
const loadingHistory = ref(false)

const aiAvatar = 'https://api.dicebear.com/7.x/bottts/svg?seed=AI'
const userAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=User'

// 格式化消息时间 - 显示月日时分
const formatMessageTime = (timestamp: string) => {
  try {
    const date = new Date(timestamp)
    if (isNaN(date.getTime())) return ''
    
    const now = new Date()
    const isToday = date.toDateString() === now.toDateString()
    
    if (isToday) {
      // 今天显示时分
      return date.toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
      })
    } else {
      // 非今天显示月日时分
      return date.toLocaleDateString('zh-CN', {
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
      }).replace(/\//g, '/')
    }
  } catch (error) {
    return ''
  }
}

// 自动滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

watch(messages, scrollToBottom, { deep: true })

// 加载房间历史消息
const loadRoomHistory = async () => {
  try {
    console.log('🔄 加载房间历史消息，房间ID:', roomId.value)
    loadingHistory.value = true
    
    const rooms = await chatApi.getRooms()
    console.log('📦 所有房间数据:', rooms)
    
    // 查找当前房间
    const currentRoom = rooms.find(room => room.id === roomId.value)
    console.log('🎯 找到当前房间:', currentRoom)
    
    if (currentRoom) {
      console.log('📊 当前房间详情:', {
        id: currentRoom.id,
        title: currentRoom.title,
        messageCount: currentRoom.messages?.length,
        messages: currentRoom.messages
      })
      
      if (currentRoom.messages && currentRoom.messages.length > 0) {
        messages.value = currentRoom.messages
        console.log('✅ 加载历史消息成功，消息数:', messages.value.length)
        
        // 打印每条消息的详细信息
        messages.value.forEach((msg, index) => {
          console.log(`  [${index}] ${msg.type}: ${msg.content.substring(0, 50)}...`)
        })
      } else {
        console.log('ℹ️ 当前房间没有消息')
        messages.value = []
      }
      
      // 检查游戏状态
      const lastMessage = messages.value[messages.value.length - 1]
      if (lastMessage && lastMessage.content.includes('游戏结束')) {
        gameEnded.value = true
        message.success('游戏已结束')
      }
      
      // 如果已经有消息，标记游戏已开始
      if (messages.value.length > 0) {
        gameStarted.value = true
      }
    } else {
      console.log('❌ 没有找到该房间的历史消息')
      messages.value = []
    }
  } catch (error) {
    console.error('❌ 加载历史消息失败:', error)
    message.error('加载历史消息失败')
  } finally {
    loadingHistory.value = false
  }
}

// 发送消息
const sendMessage = async (content: string) => {
  try {
    console.log('📤 发送消息:', content)
    const response = await chatApi.sendMessage(roomId.value, content)
    console.log('📥 AI回复:', response)
    
    messages.value.push({
      type: 'ai',
      content: response,
      timestamp: new Date().toISOString()
    })

    // 检查游戏是否结束
    if (response.includes('游戏结束')) {
      gameEnded.value = true
      message.success('游戏已结束')
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    message.error('发送消息失败')
  }
}

// 开始游戏
const handleStart = async () => {
  if (gameStarted.value) return
  
  console.log('🎮 开始游戏')
  gameStarted.value = true
  
  messages.value.push({
    type: 'user',
    content: '开始',
    timestamp: new Date().toISOString()
  })

  await sendMessage('开始')
}

// 结束游戏
const handleEnd = async () => {
  if (gameEnded.value) return
  
  console.log('⏹️ 结束游戏')
  gameEnded.value = true
  
  messages.value.push({
    type: 'user',
    content: '结束游戏',
    timestamp: new Date().toISOString()
  })
  
  await sendMessage('结束游戏')
}

// 发送消息
const handleSend = async () => {
  if (!userInput.value.trim() || gameEnded.value) return

  const content = userInput.value.trim()
  console.log('💬 发送用户消息:', content)
  
  // 添加用户消息
  messages.value.push({
    type: 'user',
    content: content,
    timestamp: new Date().toISOString()
  })

  userInput.value = ''

  // 如果是"开始"，触发开始逻辑
  if (content === '开始' && !gameStarted.value) {
    gameStarted.value = true
    await sendMessage('开始')
    return
  }

  // 发送普通消息
  await sendMessage(content)
}

// 监听路由变化，当房间切换时重新加载历史
watch(
  () => route.params.roomId,
  (newRoomId) => {
    if (newRoomId && newRoomId !== roomId.value) {
      console.log('🔄 房间切换:', roomId.value, '->', newRoomId)
      roomId.value = newRoomId as string
      messages.value = []
      gameStarted.value = false
      gameEnded.value = false
      loadRoomHistory()
    }
  }
)

onMounted(() => {
  console.log('🏁 ChatRoom 组件挂载，房间ID:', roomId.value)
  loadRoomHistory()
  scrollToBottom()
})
</script>

<style scoped>
.chat-room {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

/* 页面标题样式 */
.page-title {
  background: linear-gradient(135deg, #1890ff, #36cfc9);
  color: white;
  padding: 16px 24px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.page-title h1 {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 房间头部样式 - 居中显示 */
.room-header {
  background: white;
  padding: 16px 24px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.room-info {
  display: flex;
  align-items: center;
  gap: 16px;
  justify-content: center;
}

.label {
  color: #666;
  font-weight: 500;
  font-size: 1rem;
}

.room-id {
  font-size: 1.4rem;
  font-weight: bold;
  color: #1890ff;
  background: #f0f7ff;
  padding: 4px 12px;
  border-radius: 20px;
  border: 2px solid #e6f7ff;
}

.chat-area {
  flex: 1;
  padding: 20px;
  overflow: hidden;
  background: #fafafa;
}

.messages {
  height: 100%;
  overflow-y: auto;
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.message {
  display: flex;
  margin-bottom: 20px;
  align-items: flex-start;
  animation: fadeIn 0.3s ease-in;
}

.message.ai {
  justify-content: flex-start;
}

.message.user {
  justify-content: flex-end;
}

.message-content {
  display: flex;
  flex-direction: column;
  max-width: 70%;
}

.message.ai .message-content {
  align-items: flex-start;
  margin-left: 12px;
}

.message.user .message-content {
  align-items: flex-end;
  margin-right: 12px;
}

.bubble {
  padding: 12px 16px;
  border-radius: 18px;
  word-wrap: break-word;
  line-height: 1.4;
  max-width: 100%;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.message.ai .bubble {
  background: linear-gradient(135deg, #f0f7ff, #e6f7ff);
  border: 1px solid #d0e3ff;
}

.message.user .bubble {
  background: linear-gradient(135deg, #1890ff, #096dd9);
  color: white;
  border: 1px solid #1890ff;
}

.timestamp {
  font-size: 0.75rem;
  color: #999;
  margin-top: 4px;
  padding: 0 8px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  border: 2px solid #f0f0f0;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.empty-state {
  text-align: center;
  color: #999;
  padding: 80px 20px;
  font-size: 1.1rem;
}

.loading-state {
  text-align: center;
  padding: 60px 20px;
}

/* 输入区域样式 */
.input-area {
  padding: 20px 24px;
  background: white;
  border-top: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
  gap: 16px;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
}

.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: flex-start;
  align-items: center;
}

.start-btn {
  min-width: 80px;
  height: 36px;
  font-weight: 500;
}

.end-btn {
  min-width: 80px;
  height: 36px;
  font-weight: 500;
}

.input-container {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.input-textarea {
  flex: 1;
  border-radius: 8px;
}

.input-textarea:deep(.ant-input) {
  border-radius: 8px;
  resize: none;
}

.send-btn {
  height: auto;
  padding: 8px 24px;
  border-radius: 6px;
  font-weight: 500;
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 滚动条样式优化 */
.messages::-webkit-scrollbar {
  width: 6px;
}

.messages::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.messages::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.messages::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-title {
    padding: 12px 16px;
  }
  
  .page-title h1 {
    font-size: 1.5rem;
  }
  
  .room-header {
    padding: 12px 16px;
  }
  
  .room-info {
    gap: 12px;
    flex-wrap: wrap;
  }
  
  .chat-area {
    padding: 16px;
  }
  
  .input-area {
    padding: 16px;
  }
  
  .message-content {
    max-width: 85%;
  }
}
</style>