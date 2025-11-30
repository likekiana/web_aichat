// src/services/api.ts
import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    console.log('🚀 发起请求:', {
      method: config.method?.toUpperCase(),
      url: config.url,
      params: config.params
    })
    return config
  },
  (error) => {
    console.error('❌ 请求配置错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    console.log('✅ 请求成功:', {
      url: response.config.url,
      status: response.status,
      data: response.data
    })
    return response.data
  },
  (error) => {
    console.error('❌ API请求错误:', {
      url: error.config?.url,
      method: error.config?.method?.toUpperCase(),
      status: error.response?.status,
      message: error.message
    })
    return Promise.reject(error)
  }
)

// 后端数据结构 - 添加 createTime 字段
export interface BackendChatRoom {
  roomId: number
  chatMessageList: BackendChatMessage[]
  createTime?: number // 添加这个字段
}

export interface BackendChatMessage {
  role: string
  content: string
}

// 前端数据结构
export interface FrontendChatRoom {
  id: string
  createdAt: string
  title: string
  messages: FrontendChatMessage[]
}

export interface FrontendChatMessage {
  type: 'ai' | 'user'
  content: string
  timestamp: string
}

// 转换后端数据到前端格式 - 修复版本
function convertBackendToFrontendRoom(backendRoom: BackendChatRoom): FrontendChatRoom {
  console.log('🔄 开始转换后端房间数据:', backendRoom)
  
  const messages: FrontendChatMessage[] = []
  
  if (backendRoom.chatMessageList && Array.isArray(backendRoom.chatMessageList)) {
    console.log('📦 后端消息列表长度:', backendRoom.chatMessageList.length)
    
    const userAssistantMessages = backendRoom.chatMessageList.filter((msg: any) => {
        const role = String(msg.role).toLowerCase().trim()
        const shouldInclude = role === 'user' || role === 'assistant'
        console.log(`  过滤检查: 原始role="${msg.role}", 标准化role="${role}", 包含=${shouldInclude}`)
        return shouldInclude
      })
    
    console.log('✅ 过滤后的消息数量:', userAssistantMessages.length)
    
    // 使用房间创建时间作为基准
    const baseTime = backendRoom.createTime || Date.now()
    
    userAssistantMessages.forEach((msg, index) => {
      // 模拟时间递增
      const timestamp = new Date(baseTime + index * 1000).toISOString()
      
      const messageType = msg.role === 'user' ? 'user' : 'ai'
      console.log(`  添加消息 [${index}]: ${messageType} - ${msg.content.substring(0, 50)}...`)
      
      messages.push({
        type: messageType,
        content: msg.content,
        timestamp: timestamp
      })
    })
  } else {
    console.warn('❌ 后端消息列表为空或不是数组')
  }
  
  // 生成标题
  let title = '脑筋急转弯对话'
  const firstAiMessage = messages.find(msg => msg.type === 'ai')
  if (firstAiMessage) {
    const content = firstAiMessage.content
    // 提取第一行或前几个字作为标题
    const firstLine = content.split('\n')[0].replace(/[#*`]/g, '').trim()
    title = firstLine.length > 25 ? firstLine.substring(0, 25) + '...' : firstLine
    console.log('📝 生成标题:', title)
  }
  
  const result = {
    id: backendRoom.roomId.toString(),
    createdAt: new Date(backendRoom.createTime || Date.now()).toISOString(),
    title: title,
    messages: messages
  }
  
  console.log('🎯 转换完成，结果:', {
    id: result.id,
    title: result.title,
    messageCount: result.messages.length,
    messages: result.messages.map(m => ({ type: m.type, content: m.content.substring(0, 30) + '...' }))
  })
  
  return result
}

export const chatApi = {
  /**
   * 发送消息到指定房间
   * 对应后端: POST /api/chat/{roomId}
   */
  async sendMessage(roomId: string, userPrompt: string): Promise<string> {
    try {
      console.log(`📤 发送消息到房间 ${roomId}:`, userPrompt)
      const response = await api.post(`/api/chat/${roomId}`, null, {
        params: { userPrompt }
      })
      console.log(`📥 收到AI回复:`, response)
      return response
    } catch (error: any) {
      console.error('发送消息失败:', error)
      throw new Error(`发送消息失败: ${error.message}`)
    }
  },

  /**
   * 开始新的脑筋急转弯游戏
   * 对应后端: POST /api/chat/start
   */
  async startGame(roomId: string): Promise<string> {
    try {
      console.log(`🎮 开始游戏，房间 ${roomId}`)
      const response = await api.post('/api/chat/start', null, {
        params: { roomId }
      })
      return response
    } catch (error: any) {
      console.error('开始游戏失败:', error)
      throw new Error(`开始游戏失败: ${error.message}`)
    }
  },

  /**
   * 获取所有聊天室列表
   * 对应后端: GET /api/chat/rooms
   */
  async getRooms(): Promise<FrontendChatRoom[]> {
    try {
      console.log('🔄 开始获取房间列表...')
      const backendRooms: BackendChatRoom[] = await api.get('/api/chat/rooms')
      console.log('📦 收到后端房间数据:', backendRooms)
      
      if (!Array.isArray(backendRooms)) {
        console.warn('返回数据不是数组:', backendRooms)
        return []
      }
      
      console.log(`📊 收到 ${backendRooms.length} 个房间`)
      
      // 转换数据格式
      const frontendRooms = backendRooms.map(convertBackendToFrontendRoom)
      console.log('🔄 转换完成，前端房间数据:', frontendRooms)
      
      // 统计总消息数
      const totalMessages = frontendRooms.reduce((sum, room) => sum + room.messages.length, 0)
      console.log(`📈 总共转换了 ${totalMessages} 条消息`)
      
      return frontendRooms
      
    } catch (error: any) {
      console.error('获取房间列表失败:', error)
      throw new Error(`获取历史记录失败: ${error.response?.status === 404 ? '接口不存在' : error.message}`)
    }
  }
}

// 健康检查
export const healthApi = {
  async testConnection() {
    try {
      await api.get('/api/chat/rooms')
      return { connected: true, message: '后端连接正常' }
    } catch (error: any) {
      return { 
        connected: false, 
        message: `后端连接失败: ${error.response?.status === 404 ? '接口不存在' : error.message}`
      }
    }
  }
}

export default api