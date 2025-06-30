<script setup>
import { computed } from 'vue';

const props = defineProps({
  // 卡片标题
  title: {
    type: String,
    default: ''
  },
  // 卡片副标题
  subtitle: {
    type: String,
    default: ''
  },
  // 是否显示边框
  bordered: {
    type: Boolean,
    default: true
  },
  // 卡片阴影效果: always, hover, never
  shadow: {
    type: String,
    default: 'hover',
    validator: (value) => ['always', 'hover', 'never'].includes(value)
  },
  // 卡片大小: default, small, large
  size: {
    type: String,
    default: 'default',
    validator: (value) => ['small', 'default', 'large'].includes(value)
  },
  // 卡片内边距: default, none, small, large
  padding: {
    type: String,
    default: 'default',
    validator: (value) => ['none', 'small', 'default', 'large'].includes(value)
  },
  // 内容高度，可以传入像素值或css变量
  bodyHeight: {
    type: String,
    default: ''
  },
  // 卡片加载状态
  loading: {
    type: Boolean,
    default: false
  }
});

// 计算卡片尺寸样式
const sizeClass = computed(() => {
  switch (props.size) {
    case 'small':
      return 'card-small';
    case 'large':
      return 'card-large';
    default:
      return '';
  }
});

// 计算内边距样式
const paddingClass = computed(() => {
  switch (props.padding) {
    case 'none':
      return 'card-no-padding';
    case 'small':
      return 'card-padding-small';
    case 'large':
      return 'card-padding-large';
    default:
      return '';
  }
});

// 计算阴影样式
const shadowClass = computed(() => {
  switch (props.shadow) {
    case 'always':
      return 'card-shadow-always';
    case 'never':
      return 'card-shadow-none';
    default:
      return 'card-shadow-hover';
  }
});

// 计算容器样式
const cardClass = computed(() => [
  'base-card',
  sizeClass.value,
  paddingClass.value,
  shadowClass.value,
  {
    'base-card-bordered': props.bordered
  }
]);

// 计算卡片内容样式
const bodyStyle = computed(() => {
  return props.bodyHeight ? { height: props.bodyHeight } : {};
});
</script>

<template>
  <div :class="cardClass">
    <div v-if="$slots.cover" class="card-cover">
      <slot name="cover" />
    </div>
    
    <div v-if="title || subtitle || $slots.extra" class="card-header">
      <div class="card-header-title">
        <div v-if="title" class="card-title">{{ title }}</div>
        <div v-if="subtitle" class="card-subtitle">{{ subtitle }}</div>
      </div>
      <div v-if="$slots.extra" class="card-extra">
        <slot name="extra" />
      </div>
    </div>
    
    <div class="card-divider" v-if="(title || subtitle || $slots.extra) && $slots.default"></div>
    
    <div class="card-loading-mask" v-if="loading">
      <div class="card-loading-spinner">
        <svg viewBox="25 25 50 50" class="loading-icon">
          <circle cx="50" cy="50" r="20" fill="none" class="loading-circle" />
        </svg>
      </div>
    </div>
    
    <div class="card-body" :style="bodyStyle">
      <slot />
    </div>
    
    <div class="card-divider" v-if="$slots.footer"></div>
    
    <div v-if="$slots.footer" class="card-footer">
      <slot name="footer" />
    </div>
  </div>
</template>

<style scoped>
.base-card {
  @apply bg-white rounded-md overflow-hidden transition-all duration-200;
}

.base-card-bordered {
  @apply border border-gray-200;
}

.card-header {
  @apply flex items-center justify-between py-4 px-6;
}

.card-title {
  @apply text-base font-medium text-gray-800;
}

.card-subtitle {
  @apply text-sm text-gray-500 mt-1;
}

.card-divider {
  @apply border-t border-gray-100;
}

.card-body {
  @apply py-4 px-6 overflow-auto;
}

.card-footer {
  @apply py-3 px-6 bg-gray-50;
}

.card-cover {
  @apply w-full overflow-hidden;
}

.card-cover :deep(img) {
  @apply w-full h-full object-cover;
}

/* 尺寸变体 */
.card-small .card-header {
  @apply py-2 px-4;
}

.card-small .card-body {
  @apply py-2 px-4;
}

.card-small .card-footer {
  @apply py-2 px-4;
}

.card-large .card-header {
  @apply py-5 px-8;
}

.card-large .card-body {
  @apply py-5 px-8;
}

.card-large .card-footer {
  @apply py-4 px-8;
}

/* 内边距变体 */
.card-no-padding .card-body {
  @apply p-0;
}

.card-padding-small .card-body {
  @apply py-2 px-3;
}

.card-padding-large .card-body {
  @apply py-6 px-8;
}

/* 阴影变体 */
.card-shadow-always {
  @apply shadow;
}

.card-shadow-none {
  @apply shadow-none;
}

.card-shadow-hover {
  @apply shadow-sm hover:shadow;
}

/* 加载状态 */
.card-loading-mask {
  @apply absolute inset-0 bg-white bg-opacity-70 flex items-center justify-center z-10;
}

.card-loading-spinner {
  @apply w-10 h-10;
}

.loading-icon {
  @apply animate-spin;
  animation-duration: 2s;
}

.loading-circle {
  @apply stroke-blue-500;
  stroke-width: 4;
  stroke-dasharray: 80, 200;
  stroke-dashoffset: 0;
  stroke-linecap: round;
  animation: loading-circle 1.5s ease-in-out infinite;
}

@keyframes loading-circle {
  0% {
    stroke-dasharray: 1, 200;
    stroke-dashoffset: 0;
  }
  50% {
    stroke-dasharray: 90, 200;
    stroke-dashoffset: -35px;
  }
  100% {
    stroke-dasharray: 90, 200;
    stroke-dashoffset: -125px;
  }
}
</style> 