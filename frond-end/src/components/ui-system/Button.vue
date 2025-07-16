<script setup>
import { computed } from 'vue';

const props = defineProps({
  variant: {
    type: String,
    default: 'primary',
    validator: (value) => ['primary', 'secondary', 'outline', 'ghost', 'link', 'danger'].includes(value)
  },
  size: {
    type: String,
    default: 'md',
    validator: (value) => ['sm', 'md', 'lg'].includes(value)
  },
  disabled: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  },
  fullWidth: {
    type: Boolean,
    default: false
  },
  type: {
    type: String,
    default: 'button'
  }
});

const emit = defineEmits(['click']);

// 计算按钮类名
const buttonClasses = computed(() => {
  const classes = ['btn'];
  
  // 变体样式
  classes.push(`btn-${props.variant}`);
  
  // 尺寸
  classes.push(`btn-${props.size}`);
  
  // 禁用状态
  if (props.disabled) {
    classes.push('btn-disabled');
  }
  
  // 加载状态
  if (props.loading) {
    classes.push('btn-loading');
  }
  
  // 宽度
  if (props.fullWidth) {
    classes.push('btn-full-width');
  }
  
  return classes.join(' ');
});

// 处理按钮点击
const handleClick = (event) => {
  if (!props.disabled && !props.loading) {
    emit('click', event);
  }
};
</script>

<template>
  <button 
    :class="buttonClasses"
    :disabled="disabled || loading"
    :type="type"
    @click="handleClick"
  >
    <!-- 加载状态显示 spinner -->
    <span v-if="loading" class="btn-spinner"></span>
    
    <!-- 默认插槽 -->
    <span :class="{ 'opacity-0': loading, 'btn-content': true }">
      <slot></slot>
    </span>
  </button>
</template>

<style scoped>
.btn {
  @apply inline-flex items-center justify-center rounded-lg font-medium transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2;
}

/* 变体样式 */
.btn-primary {
  @apply bg-blue-600 text-white hover:bg-blue-700 focus:ring-blue-500;
}

.btn-secondary {
  @apply bg-gray-200 text-gray-800 hover:bg-gray-300 focus:ring-gray-500;
}

.btn-outline {
  @apply border border-gray-300 bg-white text-gray-700 hover:bg-gray-50 focus:ring-gray-500;
}

.btn-ghost {
  @apply text-gray-700 hover:bg-gray-100 focus:ring-gray-500;
}

.btn-link {
  @apply text-blue-600 hover:text-blue-700 hover:underline focus:ring-blue-500;
}

.btn-danger {
  @apply bg-red-600 text-white hover:bg-red-700 focus:ring-red-500;
}

/* 尺寸 */
.btn-sm {
  @apply px-3 py-1.5 text-sm;
}

.btn-md {
  @apply px-4 py-2 text-sm;
}

.btn-lg {
  @apply px-5 py-2.5 text-base;
}

/* 禁用状态 */
.btn-disabled {
  @apply opacity-50 cursor-not-allowed;
}

/* 加载状态 */
.btn-loading {
  @apply relative cursor-wait;
}

.btn-spinner {
  @apply absolute w-4 h-4 border-2 border-white/20 border-t-white rounded-full animate-spin;
}

.btn-primary .btn-spinner {
  @apply border-blue-300/20 border-t-white;
}

.btn-secondary .btn-spinner,
.btn-outline .btn-spinner,
.btn-ghost .btn-spinner {
  @apply border-gray-300/20 border-t-gray-600;
}

.btn-danger .btn-spinner {
  @apply border-red-300/20 border-t-white;
}

/* 宽度 */
.btn-full-width {
  @apply w-full;
}

/* 内容区域 */
.btn-content {
  @apply flex items-center gap-2;
}
</style>