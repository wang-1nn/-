<template>
  <div class="fixed top-0 left-0 right-0 z-50 p-2 text-center text-white bg-green-600 shadow-lg" v-if="visible">
    <p class="text-sm">
      <span class="font-bold">模型状态:</span> {{ modelName }} |
      <span class="font-bold">队列:</span> {{ queueLength }} |
      <span class="font-bold">GPU:</span> {{ gpuTemp }}°C
    </p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';

// Mock data
const modelName = ref('gemma-7b-it');
const queueLength = ref(2);
const gpuTemp = ref(68);
const visible = ref(true);

// Hide after a few seconds for demonstration
onMounted(() => {
  setTimeout(() => {
    visible.value = false;
  }, 5000);
});
</script>

<style scoped>
/* Scoped styles if needed */
</style> 