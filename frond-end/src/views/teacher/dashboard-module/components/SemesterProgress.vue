<script setup>
import { computed } from 'vue';

const props = defineProps({
  currentWeek: {
    type: Number,
    required: true
  },
  totalWeeks: {
    type: Number,
    required: true
  }
});

const progressPercentage = computed(() => {
  return (props.currentWeek / props.totalWeeks) * 100;
});
</script>

<template>
  <div class="semester-progress-card">
    <div class="card-header">
      <h3>当前学期进度</h3>
      <span class="week-indicator">第{{ currentWeek }}周 / 共{{ totalWeeks }}周</span>
    </div>
    
    <!-- 自定义进度条，取代ElementUI组件 -->
    <div class="progress-container">
      <div class="progress-bar" :style="{ width: `${progressPercentage}%` }"></div>
      <div class="progress-text">{{ currentWeek }}/{{ totalWeeks }}周</div>
    </div>
  </div>
</template>

<style scoped>
.semester-progress-card {
  @apply bg-white rounded-xl shadow-sm p-5 border border-gray-100 hover:shadow-md transition-all duration-300;
}

.card-header {
  @apply flex justify-between items-center mb-5;
}

.card-header h3 {
  @apply text-lg font-semibold text-gray-800;
}

.week-indicator {
  @apply text-sm text-gray-500 font-medium;
}

.progress-container {
  @apply relative h-3 bg-gray-100 rounded-full overflow-hidden;
}

.progress-bar {
  @apply absolute top-0 left-0 h-full bg-gradient-to-r from-blue-500 to-indigo-500 rounded-full transition-all duration-500;
}

.progress-text {
  @apply absolute top-0 left-0 w-full text-center text-xs text-white font-medium mt-5;
}
</style>