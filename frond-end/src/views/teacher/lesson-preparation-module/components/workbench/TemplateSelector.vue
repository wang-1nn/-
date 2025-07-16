<template>
  <div class="template-selector">
    <!-- 模板选择网格 -->
    <div class="grid grid-cols-1 gap-4">
      <div
          v-for="template in templates"
          :key="template.id"
          class="template-card"
          :class="{ 'selected': modelValue?.id === template.id }"
          @click="selectTemplate(template)"
      >
        <div class="flex items-start space-x-3">
          <!-- 模板图标 -->
          <div class="template-icon">
            <el-icon class="text-2xl" :class="getTemplateIconClass(template.style)">
              <component :is="getTemplateIcon(template.style)" />
            </el-icon>
          </div>

          <!-- 模板信息 -->
          <div class="flex-1">
            <h4 class="template-title">{{ template.name }}</h4>
            <p class="template-description">{{ template.description }}</p>

            <!-- 模板特点标签 -->
            <div class="template-tags">
              <el-tag
                  size="small"
                  :type="getTemplateTagType(template.style)"
                  effect="light"
              >
                {{ getTemplateStyleName(template.style) }}
              </el-tag>
            </div>
          </div>

          <!-- 选择状态 -->
          <div class="selection-indicator">
            <el-icon v-if="modelValue?.id === template.id" class="text-blue-600">
              <CircleCheck />
            </el-icon>
            <el-icon v-else class="text-gray-300">
              <Circle />
            </el-icon>
          </div>
        </div>
      </div>
    </div>

    <!-- 选中模板的详细信息 -->
    <div v-if="modelValue" class="selected-template-info">
      <h5 class="info-title">已选择模板</h5>
      <div class="info-content">
        <div class="flex items-center space-x-2">
          <el-icon class="text-blue-600">
            <component :is="getTemplateIcon(modelValue.style)" />
          </el-icon>
          <span class="font-medium">{{ modelValue.name }}</span>
        </div>
        <p class="text-sm text-gray-600 mt-1">{{ modelValue.description }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';
import {
  CircleCheck,
  Circle,
  ChatDotRound,
  Search,
  Folder,
  Document
} from '@element-plus/icons-vue';

const props = defineProps({
  modelValue: {
    type: Object,
    default: null
  },
  templates: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['update:modelValue', 'template-selected']);

// 选择模板
const selectTemplate = (template) => {
  emit('update:modelValue', template);
  emit('template-selected', template);
};

// 获取模板图标
const getTemplateIcon = (style) => {
  const iconMap = {
    interactive: ChatDotRound,
    inquiry: Search,
    project: Folder,
    traditional: Document
  };
  return iconMap[style] || Document;
};

// 获取模板图标样式类
const getTemplateIconClass = (style) => {
  const classMap = {
    interactive: 'text-blue-600',
    inquiry: 'text-green-600',
    project: 'text-purple-600',
    traditional: 'text-orange-600'
  };
  return classMap[style] || 'text-gray-600';
};

// 获取模板标签类型
const getTemplateTagType = (style) => {
  const typeMap = {
    interactive: 'primary',
    inquiry: 'success',
    project: 'warning',
    traditional: 'info'
  };
  return typeMap[style] || 'info';
};

// 获取模板风格名称
const getTemplateStyleName = (style) => {
  const nameMap = {
    interactive: '互动式',
    inquiry: '探究式',
    project: '项目式',
    traditional: '传统式'
  };
  return nameMap[style] || '未知';
};
</script>

<style scoped>
.template-selector {
  @apply space-y-4;
}

.template-card {
  @apply p-4 border border-gray-200 rounded-lg cursor-pointer transition-all duration-200;
  @apply hover:border-blue-300 hover:shadow-md;
}

.template-card.selected {
  @apply border-blue-500 bg-blue-50 shadow-md;
}

.template-icon {
  @apply w-12 h-12 flex items-center justify-center bg-gray-50 rounded-lg;
}

.template-card.selected .template-icon {
  @apply bg-blue-100;
}

.template-title {
  @apply text-lg font-medium text-gray-900 mb-1;
}

.template-description {
  @apply text-sm text-gray-600 mb-2;
}

.template-tags {
  @apply flex flex-wrap gap-1;
}

.selection-indicator {
  @apply flex-shrink-0;
}

.selected-template-info {
  @apply mt-6 p-4 bg-blue-50 border border-blue-200 rounded-lg;
}

.info-title {
  @apply text-sm font-medium text-blue-800 mb-2;
}

.info-content {
  @apply space-y-1;
}
</style>
