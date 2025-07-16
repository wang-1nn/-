<script setup>
import { computed } from 'vue';

const props = defineProps({
  // 卡片标题
  title: {
    type: String,
    default: ''
  },
  // 卡片描述
  description: {
    type: String,
    default: ''
  },
  // 卡片变体: default, flat, elevated, border
  variant: {
    type: String,
    default: 'default',
    validator: (value) => ['default', 'flat', 'elevated', 'border'].includes(value)
  },
  // 卡片圆角大小
  radius: {
    type: String,
    default: 'default',
    validator: (value) => ['none', 'small', 'default', 'large', 'full'].includes(value)
  },
  // 卡片内边距
  padding: {
    type: String,
    default: 'default',
    validator: (value) => ['none', 'small', 'default', 'large'].includes(value)
  },
  // 是否启用悬停效果
  hover: {
    type: Boolean,
    default: false
  },
  // 加载状态
  loading: {
    type: Boolean,
    default: false
  },
  // 卡片宽度 (可以是 'full', 'auto' 或特定像素值)
  width: {
    type: String,
    default: 'auto'
  },
  // 卡片高度 (可以是 'full', 'auto' 或特定像素值)
  height: {
    type: String,
    default: 'auto'
  },
  // 禁止内容溢出
  noOverflow: {
    type: Boolean,
    default: false
  }
});

// 计算卡片样式类
const cardClasses = computed(() => {
  return [
    'ui-card',
    `ui-card-${props.variant}`,
    `ui-card-radius-${props.radius}`,
    `ui-card-padding-${props.padding}`,
    props.hover ? 'ui-card-hover' : '',
    props.loading ? 'ui-card-loading' : '',
    props.noOverflow ? 'ui-card-no-overflow' : ''
  ];
});

// 计算卡片样式
const cardStyles = computed(() => {
  const styles = {};
  
  if (props.width !== 'auto') {
    styles.width = props.width === 'full' ? '100%' : props.width;
  }
  
  if (props.height !== 'auto') {
    styles.height = props.height === 'full' ? '100%' : props.height;
  }
  
  return styles;
});

// 检查插槽
const slots = defineSlots();
</script>

<template>
  <div :class="cardClasses" :style="cardStyles">
    <!-- 加载状态覆盖层 -->
    <div v-if="loading" class="ui-card-loading-overlay">
      <div class="ui-card-spinner">
        <svg class="ui-spinner" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle class="ui-spinner-track" cx="12" cy="12" r="10" stroke="currentColor" stroke-opacity="0.25" stroke-width="4" />
          <path
            class="ui-spinner-head"
            d="M12 2C6.47715 2 2 6.47715 2 12C2 14.5361 2.98398 16.8429 4.58397 18.5"
            stroke="currentColor"
            stroke-width="4"
            stroke-linecap="round"
          />
        </svg>
      </div>
    </div>

    <!-- 卡片内容 -->
    <div v-if="$slots.image" class="ui-card-image">
      <slot name="image"></slot>
    </div>
    
    <div v-if="title || description || $slots.header" class="ui-card-header">
      <div v-if="!$slots.header" class="ui-card-header-content">
        <h3 v-if="title" class="ui-card-title">{{ title }}</h3>
        <p v-if="description" class="ui-card-description">{{ description }}</p>
      </div>
      <slot name="header"></slot>
      
      <div v-if="$slots.headerActions" class="ui-card-header-actions">
        <slot name="headerActions"></slot>
      </div>
    </div>
    
    <div class="ui-card-body">
      <slot></slot>
    </div>
    
    <div v-if="$slots.footer" class="ui-card-footer">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<style scoped>
.ui-card {
  @apply bg-white border border-slate-200 relative flex flex-col;
}

/* 变体样式 */
.ui-card-default {
  @apply shadow-sm;
}

.ui-card-flat {
  @apply shadow-none border-0;
}

.ui-card-elevated {
  @apply shadow-md;
}

.ui-card-border {
  @apply border shadow-none;
}

/* 圆角变体 */
.ui-card-radius-none {
  @apply rounded-none;
}

.ui-card-radius-small {
  @apply rounded-sm;
}

.ui-card-radius-default {
  @apply rounded-lg;
}

.ui-card-radius-large {
  @apply rounded-xl;
}

.ui-card-radius-full {
  @apply rounded-3xl;
}

/* 内边距变体 */
.ui-card-padding-none .ui-card-body {
  @apply p-0;
}

.ui-card-padding-small .ui-card-body {
  @apply p-3;
}

.ui-card-padding-default .ui-card-body {
  @apply p-6;
}

.ui-card-padding-large .ui-card-body {
  @apply p-8;
}

/* 悬停效果 */
.ui-card-hover {
  @apply transition-shadow duration-200;
}

.ui-card-hover:hover {
  @apply shadow-md;
}

/* 禁止内容溢出 */
.ui-card-no-overflow {
  @apply overflow-hidden;
}

/* 加载状态 */
.ui-card-loading {
  @apply pointer-events-none;
}

.ui-card-loading-overlay {
  @apply absolute inset-0 bg-white bg-opacity-70 flex items-center justify-center z-10;
}

.ui-card-spinner {
  @apply h-10 w-10;
}

.ui-spinner {
  @apply animate-spin;
}

.ui-spinner-head {
  @apply stroke-primary-600;
}

.ui-spinner-track {
  @apply stroke-slate-200;
}

/* 卡片组件 */
.ui-card-image {
  @apply w-full overflow-hidden;
}

.ui-card-image img {
  @apply w-full h-full object-cover;
}

.ui-card-header {
  @apply flex items-center justify-between p-6 border-b border-slate-100;
}

.ui-card-padding-small .ui-card-header {
  @apply p-3;
}

.ui-card-padding-large .ui-card-header {
  @apply p-8;
}

.ui-card-header-content {
  @apply flex-1;
}

.ui-card-header-actions {
  @apply flex items-center ml-4 space-x-2;
}

.ui-card-title {
  @apply text-lg font-semibold text-slate-900;
}

.ui-card-description {
  @apply text-sm text-slate-500 mt-1;
}

.ui-card-footer {
  @apply p-6 bg-slate-50 border-t border-slate-100 mt-auto;
}

.ui-card-padding-small .ui-card-footer {
  @apply p-3;
}

.ui-card-padding-large .ui-card-footer {
  @apply p-8;
}
</style> 