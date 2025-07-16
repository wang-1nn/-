<script setup>
import { computed } from 'vue';

const props = defineProps({
  // 徽章内容
  content: {
    type: [String, Number],
    default: ''
  },
  // 徽章类型: primary, secondary, success, warning, error
  type: {
    type: String,
    default: 'primary',
    validator: (value) => ['primary', 'secondary', 'success', 'warning', 'error'].includes(value)
  },
  // 徽章大小: sm, md, lg
  size: {
    type: String,
    default: 'md',
    validator: (value) => ['sm', 'md', 'lg'].includes(value)
  },
  // 是否为圆点
  dot: {
    type: Boolean,
    default: false
  },
  // 最大值(超过最大值显示为 {max}+)
  max: {
    type: Number,
    default: 99
  },
  // 是否显示为数字0
  showZero: {
    type: Boolean,
    default: false
  },
  // 是否为轮廓样式
  outline: {
    type: Boolean,
    default: false
  },
  // 自定义类名
  customClass: {
    type: String,
    default: ''
  },
  // 处理徽章位置
  position: {
    type: String,
    default: 'top-right',
    validator: (value) => ['top-right', 'top-left', 'bottom-right', 'bottom-left'].includes(value)
  }
});

// 计算最终显示内容
const displayContent = computed(() => {
  // 如果是圆点，则不显示内容
  if (props.dot) {
    return '';
  }
  
  // 如果内容为数字0且不显示0，则不显示内容
  if (props.content === 0 && !props.showZero) {
    return '';
  }
  
  // 如果内容是数字且超过最大值，则显示最大值+
  if (typeof props.content === 'number' && props.content > props.max) {
    return `${props.max}+`;
  }
  
  return props.content;
});

// 是否显示徽章
const isVisible = computed(() => {
  if (props.dot) {
    return true;
  }
  
  if (props.content === 0) {
    return props.showZero;
  }
  
  return !!props.content;
});

// 计算徽章样式类
const badgeClasses = computed(() => {
  return [
    'ui-badge',
    `ui-badge-${props.type}`,
    `ui-badge-size-${props.size}`,
    `ui-badge-position-${props.position}`,
    props.dot ? 'ui-badge-dot' : '',
    props.outline ? 'ui-badge-outline' : '',
    props.customClass
  ];
});

// 计算容器样式类
const containerClasses = computed(() => {
  return [
    'ui-badge-container',
    isVisible.value ? 'ui-badge-has-content' : ''
  ];
});

// 判断是否独立使用徽章
const isStandalone = computed(() => {
  return !slots.default;
});
</script>

<template>
  <div :class="containerClasses">
    <!-- 默认内容插槽 -->
    <slot></slot>
    
    <!-- 徽章 -->
    <span 
      v-if="isVisible" 
      :class="badgeClasses"
      :data-content="displayContent"
    >
      <!-- 独立使用时显示内容 -->
      <template v-if="isStandalone">{{ displayContent }}</template>
    </span>
  </div>
</template>

<style scoped>
.ui-badge-container {
  @apply relative inline-flex items-center;
}

/* 徽章基础样式 */
.ui-badge {
  @apply inline-flex items-center justify-center font-medium;
}

/* 非独立使用时的定位 */
.ui-badge-container:not(:empty) .ui-badge {
  @apply absolute;
}

/* 位置变体 */
.ui-badge-position-top-right {
  @apply top-0 right-0 -translate-y-1/2 translate-x-1/2;
}

.ui-badge-position-top-left {
  @apply top-0 left-0 -translate-y-1/2 -translate-x-1/2;
}

.ui-badge-position-bottom-right {
  @apply bottom-0 right-0 translate-y-1/2 translate-x-1/2;
}

.ui-badge-position-bottom-left {
  @apply bottom-0 left-0 translate-y-1/2 -translate-x-1/2;
}

/* 大小变体 */
.ui-badge-size-sm {
  @apply text-xs min-w-5 h-5 px-1 rounded-full;
}

.ui-badge-size-md {
  @apply text-xs min-w-6 h-6 px-1.5 rounded-full;
}

.ui-badge-size-lg {
  @apply text-sm min-w-7 h-7 px-2 rounded-full;
}

/* 圆点样式 */
.ui-badge-dot {
  @apply min-w-0 min-h-0 p-0 rounded-full;
}

.ui-badge-dot.ui-badge-size-sm {
  @apply w-2 h-2;
}

.ui-badge-dot.ui-badge-size-md {
  @apply w-2.5 h-2.5;
}

.ui-badge-dot.ui-badge-size-lg {
  @apply w-3 h-3;
}

/* 类型变体 */
.ui-badge-primary {
  @apply bg-primary-500 text-white;
}

.ui-badge-secondary {
  @apply bg-slate-500 text-white;
}

.ui-badge-success {
  @apply bg-success-color text-white;
}

.ui-badge-warning {
  @apply bg-warning-color text-white;
}

.ui-badge-error {
  @apply bg-error-color text-white;
}

/* 轮廓样式 */
.ui-badge-outline.ui-badge-primary {
  @apply bg-transparent border border-primary-500 text-primary-500;
}

.ui-badge-outline.ui-badge-secondary {
  @apply bg-transparent border border-slate-500 text-slate-500;
}

.ui-badge-outline.ui-badge-success {
  @apply bg-transparent border border-success-color text-success-color;
}

.ui-badge-outline.ui-badge-warning {
  @apply bg-transparent border border-warning-color text-warning-color;
}

.ui-badge-outline.ui-badge-error {
  @apply bg-transparent border border-error-color text-error-color;
}

/* 当独立使用徽章时 */
.ui-badge-container:empty .ui-badge {
  @apply static transform-none;
}

/* 为空标记添加属性内容 */
.ui-badge:not(:empty)::after {
  content: attr(data-content);
}
</style> 