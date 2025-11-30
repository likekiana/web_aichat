// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '../views/HomePage.vue'
import ChatRoom from '../views/ChatRoom.vue'
import ChatLayout from '../components/ChatLayout.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: HomePage
  },
  {
    path: '/chat',
    component: ChatLayout,
    children: [
      {
        path: ':roomId',
        name: 'ChatRoom',
        component: ChatRoom,
        props: true
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router