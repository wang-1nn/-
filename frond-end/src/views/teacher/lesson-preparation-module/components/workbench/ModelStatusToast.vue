<template>
  <div 
    class="fixed top-0 left-0 right-0 z-50 p-2 text-center text-white bg-gradient-to-r from-blue-600 to-indigo-600 shadow-lg transition-all duration-500 transform" 
    :class="toastVisible ? 'translate-y-0 opacity-100' : '-translate-y-full opacity-0'"
  >
    <p class="text-sm flex items-center justify-center gap-3">
      <span class="flex items-center font-medium"><el-icon><Monitor /></el-icon><span class="ml-1">模型: <span class="font-bold ml-1">{{ modelName }}</span></span></span>
      <span class="w-px h-4 bg-white/30"></span>
      <span class="flex items-center font-medium"><el-icon><Sort /></el-icon><span class="ml-1">队列: <span class="font-bold ml-1">{{ queueLength }}</span></span></span>
      <span class="w-px h-4 bg-white/30"></span>
      <span class="flex items-center font-medium"><el-icon><Cpu /></el-icon><span class="ml-1">GPU: <span class="font-bold ml-1">{{ gpuTemp }}°C</span></span></span>
    </p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { Monitor, Sort, Cpu } from '@element-plus/icons-vue';

// 状态数据，实际应用中可从API获取
const props = defineProps({
  autoHide: {
    type: Boolean,
    default: true
  },
  hideDelay: {
    type: Number,
    default: 5000
  }
});

const modelName = ref('gemma-7b-it');
const queueLength = ref(2);
const gpuTemp = ref(68);
const toastVisible = ref(true);

// 模拟数据更新
const updateModelStatus = () => {
  // 实际项目中，这里可以通过WebSocket或轮询获取实时状态
  modelName.value = ['gemma-7b-it', 'llama-2-13b', 'gpt-3.5-turbo'][Math.floor(Math.random() * 3)];
  queueLength.value = Math.floor(Math.random() * 5);
  gpuTemp.value = 60 + Math.floor(Math.random() * 15);
};

onMounted(() => {
  // 自动隐藏
  if (props.autoHide) {
    setTimeout(() => { 
      toastVisible.value = false; 
    }, props.hideDelay);
  }
  
  // 启动模拟数据更新
  setInterval(updateModelStatus, 10000);
});

// 提供方法给父组件控制显示/隐藏
defineExpose({
  show: () => { toastVisible.value = true; },
  hide: () => { toastVisible.value = false; }
});
</script>

<style scoped>
/* 微交互：悬停时略微提高亮度 */
div:hover {
  filter: brightness(1.1);
}
</style>