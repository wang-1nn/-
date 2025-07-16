<script setup>
import { computed } from 'vue';

const props = defineProps({
  variant: {
    type: String,
    default: 'default',
    validator: (value) => ['default', 'primary', 'success', 'warning', 'danger', 'info'].includes(value)
  },
  size: {
    type: String,
    default: 'md',
    validator: (value) => ['sm', 'md', 'lg'].includes(value)
  },
  dot: {
    type: Boolean,
    default: false
  },
  rounded: {
    type: Boolean,
    default: false
  }
});

// 计算徽章类名
const badgeClasses = computed(() => {
  const classes = ['badge'];
  
  // 变体样式
  classes.push(`badge-${props.variant}`);
  
  // 尺寸
  classes.push(`badge-${props.size}`);
  
  // 圆角风格
  if (props.rounded) {
    classes.push('badge-rounded');
  }
  
  return classes.join(' ');
});
</script>

<template>
  <!-- 点样式徽章 -->
  <span v-if="dot" class="badge-dot" :class="`badge-dot-${variant}`"></span>
  
  <!-- 常规徽章 -->
  <span v-else :class="badgeClasses">
    <slot></slot>
  </span>
</template>

<style scoped>
/* 基础徽章样式 */
.badge {
  @apply inline-flex items-center justify-center font-medium whitespace-nowrap;
}

/* 尺寸 */
.badge-sm {
  @apply text-xs px-2 py-0.5;
}

.badge-md {
  @apply text-xs px-2.5 py-1;
}

.badge-lg {
  @apply text-sm px-3 py-1;
}

/* 圆角风格 */
.badge-rounded {
  @apply rounded-full;
}

.badge:not(.badge-rounded) {
  @apply rounded-md;
}

/* 变体样式 */
.badge-default {
  @apply bg-gray-100 text-gray-800;
}

.badge-primary {
  @apply bg-blue-100 text-blue-800;
}

.badge-success {
  @apply bg-green-100 text-green-800;
}

.badge-warning {
  @apply bg-amber-100 text-amber-800;
}

.badge-danger {
  @apply bg-red-100 text-red-800;
}

.badge-info {
  @apply bg-sky-100 text-sky-800;
}

/* 点样式徽章 */
.badge-dot {
  @apply inline-block w-2 h-2 rounded-full;
}

.badge-dot-default {
  @apply bg-gray-500;
}

.badge-dot-primary {
  @apply bg-blue-500;
}

.badge-dot-success {
  @apply bg-green-500;
}

.badge-dot-warning {
  @apply bg-amber-500;
}

.badge-dot-danger {
  @apply bg-red-500;
}

.badge-dot-info {
  @apply bg-sky-500;
}
</style>