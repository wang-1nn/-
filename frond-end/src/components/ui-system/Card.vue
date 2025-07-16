<script setup>
import { computed } from 'vue';

const props = defineProps({
  variant: {
    type: String,
    default: 'default',
    validator: (value) => ['default', 'bordered', 'flat'].includes(value)
  },
  padding: {
    type: String,
    default: 'default',
    validator: (value) => ['none', 'sm', 'default', 'lg'].includes(value)
  },
  hover: {
    type: Boolean,
    default: false
  }
});

// 计算卡片类名
const cardClasses = computed(() => {
  const classes = ['card'];
  
  // 变体样式
  classes.push(`card-${props.variant}`);
  
  // 内边距
  classes.push(`card-padding-${props.padding}`);
  
  // 悬浮效果
  if (props.hover) {
    classes.push('card-hover');
  }
  
  return classes.join(' ');
});
</script>

<template>
  <div :class="cardClasses">
    <!-- 卡片头部 -->
    <div v-if="$slots.header" class="card-header">
      <slot name="header"></slot>
    </div>
    
    <!-- 卡片内容 -->
    <div class="card-body">
      <slot></slot>
    </div>
    
    <!-- 卡片底部 -->
    <div v-if="$slots.footer" class="card-footer">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<style scoped>
.card {
  @apply bg-white rounded-xl overflow-hidden transition-all duration-300;
}

/* 变体样式 */
.card-default {
  @apply shadow-sm border border-gray-100;
}

.card-bordered {
  @apply border border-gray-200;
}

.card-flat {
  @apply shadow-none;
}

/* 内边距 */
.card-padding-none .card-body {
  @apply p-0;
}

.card-padding-sm .card-body {
  @apply p-3;
}

.card-padding-default .card-body {
  @apply p-5;
}

.card-padding-lg .card-body {
  @apply p-6;
}

/* 卡片头部和底部 */
.card-header {
  @apply p-5 border-b border-gray-100;
}

.card-footer {
  @apply p-5 border-t border-gray-100;
}

/* 悬浮效果 */
.card-hover {
  @apply hover:shadow-md transition-shadow duration-300;
}

.card-hover.card-default {
  @apply hover:border-gray-200;
}

.card-hover.card-bordered {
  @apply hover:border-gray-300;
}
</style>