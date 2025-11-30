<!-- src/components/ChatLayout.vue -->
<template>
  <div class="chat-layout">
    <!-- 侧边栏 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <h3>历史对话</h3>
        <div class="app-name">AI 脑筋急转弯</div>
      </div>

      <div class="history-list">
        <a-list
          :data-source="validRooms"
          :loading="loading"
          size="small"
          class="history-list-content"
        >
          <template #renderItem="{ item }">
            <a-list-item 
              class="history-item"
              :class="{ active: isCurrentRoom(item.id) }"
              @click="switchToRoom(item.id)"
            >
              <div class="item-content">
                <div class="room-header">
                  <span class="room-title">{{ item.title }}</span>
                  <a-tag v-if="isCurrentRoom(item.id)" color="blue" size="small">当前</a-tag>
                </div>
                <div class="room-info">
                  <span class="room-time">{{ formatTime(item.createdAt) }}</span>
                </div>
                <div class="room-preview" v-if="getLastMessage(item)">
                  {{ getLastMessage(item) }}
                </div>
              </div>
            </a-list-item>
          </template>

          <template #empty>
            <div class="empty-state">
              <p>暂无历史对话</p>
              <p class="empty-tip">开始新游戏后，对话将显示在这里</p>
              <a-button @click="startNewGame" type="primary">开始游戏</a-button>
            </div>
          </template>
        </a-list>
      </div>

      <div class="sidebar-footer">
        <a-button 
          type="primary" 
          block 
          @click="startNewGame"
          class="new-game-btn"
        >
          <template #icon><PlusOutlined /></template>
          开始新游戏
        </a-button>
      </div>
    </div>

    <!-- 主内容 -->
    <div class="main-content">
      <router-view v-if="currentRoomId" :key="currentRoomId" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { chatApi, type FrontendChatRoom } from '../services/api'

const route = useRoute()
const router = useRouter()

const currentRoomId = ref(route.params.roomId as string)
const historyRooms = ref<FrontendChatRoom[]>([])
const loading = ref(false)

// 过滤有效房间
const validRooms = computed(() => {
  return historyRooms.value.filter(room => 
    room.id && 
    room.id !== 'undefined' && 
    room.id !== 'null' &&
    !isNaN(Number(room.id)) // 确保是数字
  )
})

// 检查是否是当前房间
const isCurrentRoom = (roomId: string) => {
  return roomId === currentRoomId.value
}

// 加载历史记录
const loadHistory = async () => {
  try {
    console.log('🔄 开始加载历史记录...')
    loading.value = true
    
    const rooms = await chatApi.getRooms()
    console.log('📦 收到前端格式的房间数据:', rooms)
    
    // 安全处理数据并修改标题格式为 "对话-房间号"
    historyRooms.value = rooms
      .filter(room => room.id && !isNaN(Number(room.id))) // 过滤有效数字ID
      .map(room => ({
        ...room,
        title: `对话-${room.id}` // 修改标题格式
      }))
      .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    
    console.log('✅ 处理后的历史记录:', historyRooms.value)
    
    if (historyRooms.value.length === 0) {
      console.log('ℹ️ 没有历史记录，可能是首次使用或后端无数据')
    }
    
  } catch (error) {
    console.error('❌ 加载历史记录失败:', error)
    message.error('加载历史记录失败')
    historyRooms.value = []
  } finally {
    loading.value = false
  }
}

// 获取最后一条消息预览
const getLastMessage = (room: FrontendChatRoom) => {
  if (!room.messages || room.messages.length === 0) return '暂无消息'
  const lastMessage = room.messages[room.messages.length - 1]
  const content = lastMessage.content || ''
  return content.length > 25 ? content.substring(0, 25) + '...' : content
}

// 格式化时间
const formatTime = (dateString: string) => {
  try {
    if (!dateString) return '未知时间'
    
    const date = new Date(dateString)
    if (isNaN(date.getTime())) {
      return '时间错误'
    }
    
    const now = new Date()
    const diff = now.getTime() - date.getTime()
    const minutes = Math.floor(diff / (1000 * 60))
    const hours = Math.floor(diff / (1000 * 60 * 60))
    const days = Math.floor(diff / (1000 * 60 * 60 * 24))
    
    // 如果是今天，显示具体时间
    if (days === 0) {
      if (minutes < 1) return '刚刚'
      if (minutes < 60) return `${minutes}分钟前`
      if (hours < 24) return `${hours}小时前`
    }
    
    // 如果是今年，显示月日时分
    if (date.getFullYear() === now.getFullYear()) {
      return date.toLocaleDateString('zh-CN', {
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      }).replace(/\//g, '-')
    }
    
    // 其他情况显示完整年月日时分
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    }).replace(/\//g, '-')
  } catch (error) {
    return '时间错误'
  }
}

// 切换房间
const switchToRoom = (roomId: string) => {
  console.log('🔄 尝试切换房间:', roomId)
  
  if (!roomId || roomId === 'undefined') {
    message.error('房间ID无效')
    return
  }
  
  if (roomId !== currentRoomId.value) {
    console.log('✅ 切换到房间:', roomId)
    router.push(`/chat/${roomId}`)
  } else {
    console.log('ℹ️ 已经是当前房间')
  }
}

// 开始新游戏
const startNewGame = () => {
  const newRoomId = Math.floor(Math.random() * 900000) + 100000
  console.log('🎮 开始新游戏，房间号:', newRoomId)
  router.push(`/chat/${newRoomId}`)
}

// 监听路由变化
watch(
  () => route.params.roomId,
  (newRoomId) => {
    if (newRoomId && newRoomId !== 'undefined') {
      console.log('📍 路由变化，新房间号:', newRoomId)
      currentRoomId.value = newRoomId as string
      
      // 检查是否已经存在这个房间
      const existingRoom = historyRooms.value.find(room => room.id === newRoomId)
      if (!existingRoom) {
        console.log('➕ 添加新房间到历史记录:', newRoomId)
        // 创建新房间并添加到列表开头，标题格式为 "对话-房间号"
        const newRoom: FrontendChatRoom = {
          id: newRoomId as string,
          createdAt: new Date().toISOString(),
          title: `对话-${newRoomId}`,
          messages: []
        }
        historyRooms.value.unshift(newRoom)
      }
    }
  },
  { immediate: true }
)

onMounted(() => {
  console.log('🏁 ChatLayout 组件挂载完成，当前房间:', currentRoomId.value)
  loadHistory()
})
</script>

<style scoped>
.chat-layout {
  display: flex;
  height: 100vh;
  background: #f5f5f5;
}

.sidebar {
  width: 320px;
  background: white;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  padding: 20px;
  background: linear-gradient(135deg, #1890ff, #36cfc9);
  color: white;
}

.sidebar-header h3 {
  margin: 0 0 8px 0;
  font-size: 1.2rem;
}

.app-name {
  font-size: 0.9rem;
  opacity: 0.9;
}

.history-list {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.history-list-content {
  flex: 1;
  overflow-y: auto;
}

.history-list-content :deep(.ant-list-item) {
  padding: 0;
  border-bottom: 1px solid #f0f0f0;
}

.history-item {
  padding: 16px 20px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.history-item:hover {
  background: #f0f7ff;
}

.history-item.active {
  background: #e6f7ff;
  border-right: 3px solid #1890ff;
}

.item-content {
  width: 100%;
}

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 6px;
}

.room-title {
  font-weight: 500;
  color: #333;
  flex: 1;
}

.room-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}

.room-time {
  font-size: 0.75rem;
  color: #666;
}

.room-preview {
  font-size: 0.8rem;
  color: #999;
  line-height: 1.3;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.empty-tip {
  font-size: 0.8rem;
  margin: 8px 0 16px 0;
}

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid #e8e8e8;
  flex-shrink: 0;
}

.new-game-btn {
  height: 40px;
}

.main-content {
  flex: 1;
  overflow: hidden;
  min-width: 0;
}
</style>