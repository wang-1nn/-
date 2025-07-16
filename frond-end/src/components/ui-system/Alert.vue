<script setup>
import { computed } from 'vue';

const props = defineProps({
  variant: {
    type: String,
    default: 'info',
    validator: (value) => ['info', 'success', 'warning', 'error'].includes(value)
  },
  title: {
    type: String,
    default: ''
  },
  icon: {
    type: Boolean,
    default: true
  },
  dismissible: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['close']);

// 计算警告框类名
const alertClasses = computed(() => {
  return [`alert-${props.variant}`];
});

// 根据变体获取图标路径
const iconPath = computed(() => {
  switch (props.variant) {
    case 'info':
      return 'M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z';
    case 'success':
      return 'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z';
    case 'warning':
      return 'M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z';
    case 'error':
      return 'M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z';
    default:
      return '';
  }
});

// 处理关闭警告框
const handleClose = () => {
  emit('close');
};
</script>

<template>
  <div :class="['alert', alertClasses]" role="alert">
    <div class="alert-content">
      <!-- 图标 -->
      <div v-if="icon" class="alert-icon">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="iconPath" />
        </svg>
      </div>
      
      <!-- 文本内容 -->
      <div class="alert-message">
        <!-- 标题 -->
        <div v-if="title" class="alert-title">{{ title }}</div>
        
        <!-- 描述 -->
        <div class="alert-description">
          <slot></slot>
        </div>
      </div>
      
      <!-- 关闭按钮 -->
      <button v-if="dismissible" class="alert-close-btn" @click="handleClose">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
    </div>
  </div>
</template>

<style scoped>
.alert {
  @apply rounded-lg p-4;
}

.alert-content {
  @apply flex items-start;
}

.alert-icon {
  @apply flex-shrink-0 mr-3;
}

.alert-message {
  @apply flex-1;
}

.alert-title {
  @apply text-sm font-medium mb-1;
}

.alert-description {
  @apply text-sm;
}

.alert-close-btn {
  @apply flex-shrink-0 ml-3 p-1 rounded-full transition-colors duration-200;
}

/* 变体样式 */
.alert-info {
  @apply bg-blue-50 text-blue-800;
}

.alert-info .alert-close-btn {
  @apply hover:bg-blue-100;
}

.alert-success {
  @apply bg-green-50 text-green-800;
}

.alert-success .alert-close-btn {
  @apply hover:bg-green-100;
}

.alert-warning {
  @apply bg-amber-50 text-amber-800;
}

.alert-warning .alert-close-btn {
  @apply hover:bg-amber-100;
}

.alert-error {
  @apply bg-red-50 text-red-800;
}

.alert-error .alert-close-btn {
  @apply hover:bg-red-100;
}
</style>