<script setup>
import { computed } from 'vue';

const props = defineProps({
  chartType: {
    type: String,
    default: 'activity' // 'activity' or 'resources'
  },
  studentActivityData: {
    type: Object,
    default: () => ({
      labels: [],
      values: []
    })
  },
  resourceAccessData: {
    type: Object,
    default: () => ({
      labels: [],
      values: []
    })
  }
});

// 根据当前选择的图表类型选择正确的数据
const currentData = computed(() => {
  return props.chartType === 'activity' ? props.studentActivityData : props.resourceAccessData;
});

// 计算最大值，用于绘制图表高度
const maxValue = computed(() => {
  if (!currentData.value.values.length) return 1;
  return Math.max(...currentData.value.values, 1);
});
</script>

<template>
  <div class="charts-card">
    <div class="card-header">
      <h3>数据统计</h3>
      <div class="chart-toggle">
        <button 
          :class="['chart-toggle-btn', chartType === 'activity' ? 'active' : '']"
          @click="$emit('update:chartType', 'activity')"
        >
          学生活跃度
        </button>
        <button 
          :class="['chart-toggle-btn', chartType === 'resources' ? 'active' : '']"
          @click="$emit('update:chartType', 'resources')"
        >
          资源访问
        </button>
      </div>
    </div>
    
    <div class="chart-container">
      <div class="chart-placeholder">
        <div class="chart-mock">
          <div 
            v-for="(value, index) in currentData.values" 
            :key="index" 
            :style="{height: `${(value / maxValue) * 160}px`}" 
            :class="['chart-bar', chartType === 'resources' ? 'resource-bar' : 'activity-bar']"
          >
            <div class="chart-bar-tooltip">{{ value }}</div>
          </div>
        </div>
        <div class="chart-labels">
          <span v-for="(label, index) in currentData.labels" :key="index">{{ label }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.charts-card {
  @apply bg-white rounded-xl shadow-sm p-5 border border-gray-100 hover:shadow-md transition-all duration-300;
}

.card-header {
  @apply flex justify-between items-center mb-5;
}

.card-header h3 {
  @apply text-lg font-semibold text-gray-800;
}

.chart-toggle {
  @apply flex items-center bg-gray-100 rounded-lg overflow-hidden p-1;
}

.chart-toggle-btn {
  @apply px-4 py-1.5 text-sm transition-all duration-200;
}

.chart-toggle-btn.active {
  @apply bg-white text-blue-600 font-medium shadow-sm rounded-md;
}

.chart-container {
  @apply h-64;
}

.chart-placeholder {
  @apply h-full flex flex-col;
}

.chart-mock {
  @apply flex justify-around items-end h-48 border-b border-gray-200 px-4;
}

.chart-bar {
  @apply w-10 rounded-t-md transition-all duration-500 relative;
  min-height: 4px;
}

.activity-bar {
  @apply bg-gradient-to-t from-blue-500 to-blue-400 hover:from-blue-600 hover:to-blue-500;
}

.resource-bar {
  @apply bg-gradient-to-t from-purple-500 to-purple-400 hover:from-purple-600 hover:to-purple-500;
}

.chart-bar-tooltip {
  @apply absolute -top-7 left-1/2 transform -translate-x-1/2 bg-gray-800 text-white text-xs py-1 px-2 rounded opacity-0 transition-opacity duration-200;
}

.chart-bar:hover .chart-bar-tooltip {
  @apply opacity-100;
}

.chart-labels {
  @apply flex justify-around pt-3;
}

.chart-labels span {
  @apply text-xs text-gray-500 font-medium;
}
</style>