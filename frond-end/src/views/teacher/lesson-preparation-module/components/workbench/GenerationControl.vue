<template>
  <div class="generation-control">
    <!-- 生成状态显示 -->
    <div v-if="isGenerating" class="mb-4 p-4 bg-blue-50 border border-blue-200 rounded-lg">
      <div class="flex items-center mb-2">
        <el-icon class="mr-2 text-blue-600 animate-spin"><Loading /></el-icon>
        <span class="font-medium text-blue-800">正在生成教案...</span>
      </div>

      <!-- 进度条 -->
      <el-progress
          :percentage="generationProgress"
          :format="progressFormat"
          :stroke-width="8"
          class="mb-2"
      />

      <p class="text-sm text-blue-600">{{ generationStatus }}</p>
    </div>

    <!-- 生成进度和状态 -->
    <div v-if="isGenerating" class="mb-4 p-4 bg-white rounded-lg shadow space-y-3">
      <div class="flex items-center justify-between">
        <span class="text-sm font-medium text-slate-700">正在生成教案...</span>
        <span class="text-xs text-slate-500">预计剩余时间: {{ remainingTime }}</span>
      </div>

      <el-progress
          :percentage="generationProgress"
          :stroke-width="10"
          :format="progressFormat"
          class="generation-progress"
      />

      <div class="flex items-center text-xs text-slate-500">
        <el-icon class="mr-1"><Loading /></el-icon>
        <span>{{ generationStatus }}</span>
      </div>
    </div>

    <!-- 生成教案按钮 -->
    <div class="mt-4">
      <el-button
          type="primary"
          size="large"
          :loading="isGenerating"
          :disabled="!selectedTemplate"
          @click="startGeneration"
          class="w-full"
      >
        <el-icon class="mr-2" v-if="!isGenerating"><Document /></el-icon>
        {{ isGenerating ? '生成中...' : '开始生成教案' }}
      </el-button>

      <div v-if="!selectedTemplate" class="text-center text-sm text-gray-500 mt-2">
        请先选择教案模板
      </div>

      <div v-else class="text-center text-sm text-gray-600 mt-2">
        将基于"{{ selectedTemplate.name }}"生成教案
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, defineEmits, defineProps } from 'vue';
import { Loading, Document } from '@element-plus/icons-vue';

const props = defineProps({
  isGenerating: {
    type: Boolean,
    default: false
  },
  generationProgress: {
    type: Number,
    default: 0
  },
  selectedTemplate: {
    type: Object,
    default: null
  }
});

const emit = defineEmits(['start-generation']);

// 生成状态信息
const generationStatus = ref('准备生成教案...');
const remainingTime = ref('约1分钟');

// 进度格式化
const progressFormat = (percentage) => {
  return `${percentage}%`;
};

// 开始生成
const startGeneration = () => {
  if (!props.selectedTemplate) {
    return;
  }

  const params = {
    template: props.selectedTemplate
  };

  emit('start-generation', params);
};
</script>

<style scoped>
.custom-collapse :deep(.el-collapse-item__header) {
  background-color: white;
  border: none;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  margin-bottom: 4px;
}

.custom-collapse :deep(.el-collapse-item__content) {
  padding: 0;
}

/* 创造性滑块样式 */
.creativity-slider :deep(.el-slider__runway) {
  height: 6px;
}

.creativity-slider :deep(.el-slider__bar) {
  background-color: #818cf8; /* indigo-400 */
  height: 6px;
}

.creativity-slider :deep(.el-slider__button) {
  width: 16px;
  height: 16px;
  border: 2px solid #6366f1; /* indigo-500 */
  background-color: white;
}

/* 教学风格标签样式 */
.style-tag {
  transition: all 0.2s ease;
  cursor: pointer;
}

.style-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 生成进度条样式 */
.generation-progress :deep(.el-progress-bar__outer) {
  background-color: rgba(99, 102, 241, 0.1);
  border-radius: 8px;
}

.generation-progress :deep(.el-progress-bar__inner) {
  background-color: #6366f1; /* indigo-500 */
  border-radius: 8px;
}
</style>