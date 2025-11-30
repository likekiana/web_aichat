<!-- src/views/HomePage.vue -->
<template>
  <div class="home-page">
    <a-card class="game-card" :bordered="false">
      <div class="title-section">
        <h1 class="game-title">AI 急转弯</h1>
        <p class="game-subtitle">准备好挑战你的思维极限了吗？</p>
      </div>
      <div class="button-section">
        <a-button 
          type="primary" 
          size="large" 
          class="start-button"
          @click="startGame"
        >
          点击开始
        </a-button>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'

const router = useRouter()

const startGame = async () => {
  try {
    // 生成随机房间号（1-9999）
    const roomId = Math.floor(Math.random() * 9999) + 1
    router.push(`/chat/${roomId}`)
  } catch (error) {
    message.error('开始游戏失败，请重试')
  }
}
</script>

<style scoped>
.home-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.game-card {
  width: 100%;
  max-width: 500px;
  text-align: center;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.title-section {
  margin-bottom: 40px;
}

.game-title {
  font-size: 3rem;
  font-weight: bold;
  color: #1890ff;
  margin-bottom: 16px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.game-subtitle {
  font-size: 1.2rem;
  color: #666;
}

.button-section {
  margin-top: 20px;
}

.start-button {
  height: 60px;
  font-size: 1.5rem;
  padding: 0 40px;
  border-radius: 30px;
  background: linear-gradient(45deg, #1890ff, #36cfc9);
  border: none;
  box-shadow: 0 4px 15px rgba(24, 144, 255, 0.4);
  transition: all 0.3s ease;
}

.start-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(24, 144, 255, 0.6);
}
</style>