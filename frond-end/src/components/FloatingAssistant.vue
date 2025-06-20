<script setup>
import { ref } from 'vue'
import { Teleport } from 'vue'
import LucideIcon from '@/components/icons/LucideIcon.vue'
import AiAssistant from '@/views/student/AiAssistant.vue'

const open = ref(false)
</script>
<template>
  <!-- Floating Ball -->
  <div>
    <button @click="open = !open" class="fixed bottom-6 right-6 z-40 h-14 w-14 rounded-full bg-gradient-to-r from-indigo-500 to-purple-600 shadow-xl flex items-center justify-center text-white hover:scale-110 active:scale-95 transition-transform">
      <LucideIcon name="message-circle" size="24" />
    </button>

    <Teleport to="body">
      <transition name="fade">
        <div v-if="open" class="fixed inset-0 z-50 flex items-end md:items-center justify-center">
          <!-- 半透明遮罩 -->
          <div class="absolute inset-0 bg-black/40" @click="open=false"></div>
          <!-- 助手面板（复用原页面组件） -->
          <div class="relative w-full h-[70vh] md:h-[80vh] md:max-w-3xl">
            <AiAssistant />
          </div>
        </div>
      </transition>
    </Teleport>
  </div>
</template>
<style scoped>
.fade-enter-active,.fade-leave-active{transition:opacity .2s ease;}
.fade-enter-from,.fade-leave-to{opacity:0;}
</style> 