<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/counter.js'

const router = useRouter()
const auth = useAuthStore()

// 深色模式控制
const darkMode = ref(false)

// 检查用户偏好和本地存储
onMounted(() => {
  // 检查浏览器偏好
  const prefersDarkMode = window.matchMedia('(prefers-color-scheme: dark)').matches
  // 检查本地存储
  const storedDarkMode = localStorage.getItem('darkMode')

  if (storedDarkMode !== null) {
    darkMode.value = storedDarkMode === 'true'
  } else {
    darkMode.value = prefersDarkMode
  }

  // 应用主题
  applyTheme()
})

// 切换深色模式
const toggleDarkMode = () => {
  darkMode.value = !darkMode.value
  localStorage.setItem('darkMode', darkMode.value.toString())
}

// 应用主题
const applyTheme = () => {
  if (darkMode.value) {
    document.documentElement.classList.add('dark-mode')
  } else {
    document.documentElement.classList.remove('dark-mode')
  }
}

// 监听状态变化
watch(darkMode, () => {
  applyTheme()
})

// 导出函数供全局使用
defineExpose({
  toggleDarkMode
})
</script>

<template>
  <RouterView v-slot="{ Component }">
    <transition name="fade" mode="out-in">
      <component :is="Component" />
    </transition>
  </RouterView>
</template>

<style>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&family=Fira+Code:wght@400;500&display=swap');

html, body {
  height: 100%;
  margin: 0;
  padding: 0;
}

body {
  font-family: var(--font-family-sans);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s ease;
}
</style>
