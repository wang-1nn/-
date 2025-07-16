<script setup>
import { computed } from 'vue';

const props = defineProps({
  // 卡片标题
  title: {
    type: String,
    default: ''
  },
  // 卡片类型/变体
  variant: {
    type: String,
    default: 'default',
    validator: (val) => ['default', 'filled', 'outline', 'flat'].includes(val)
  },
  // 内边距大小
  padding: {
    type: String,
    default: 'default',
    validator: (val) => ['default', 'small', 'large', 'none'].includes(val)
  },
  // 是否显示悬停效果
  hover: {
    type: Boolean,
    default: false
  },
  // 是否为容器卡片
  container: {
    type: Boolean,
    default: false
  },
  // 容器宽度类型：full(100%), wide(85%), narrow(65%)
  width: {
    type: String,
    default: 'full',
    validator: (val) => ['full', 'wide', 'narrow'].includes(val)
  }
});

// 计算主卡片样式类
const cardClass = computed(() => {
  const classes = ['ui-card'];

  // 变体样式
  classes.push(`ui-card-${props.variant}`);

  // 内边距样式
  classes.push(`ui-card-padding-${props.padding}`);

  // 悬停效果
  if (props.hover) {
    classes.push('ui-card-hover');
  }

  // 容器类型
  if (props.container) {
    classes.push('ui-content-container');
    classes.push(`ui-container-${props.width}`);
  }

  return classes;
});
</script>

<template>
  <div :class="cardClass">
    <div v-if="title" class="ui-card-header">
      <h3 class="ui-card-title">{{ title }}</h3>
      <div class="ui-card-actions">
        <slot name="actions"></slot>
      </div>
    </div>
    <div class="ui-card-content">
      <slot></slot>
    </div>
  </div>
</template>

<style scoped>
.ui-card {
  @apply bg-white rounded-xl shadow-sm border border-slate-200 transition-shadow duration-200 mb-4;
}

.ui-card-hover:hover {
  @apply shadow-md;
}

/* 卡片变体 */
.ui-card-filled {
  @apply bg-slate-50 border-slate-200;
}

.ui-card-outline {
  @apply bg-transparent border border-slate-200 shadow-none;
}

.ui-card-flat {
  @apply bg-transparent border-none shadow-none rounded-none;
}

/* 内边距变体 */
.ui-card-padding-default {
  @apply p-6;
}

.ui-card-padding-small {
  @apply p-4;
}

.ui-card-padding-large {
  @apply p-8;
}

.ui-card-padding-none {
  @apply p-0;
}

/* 卡片头部 */
.ui-card-header {
  @apply flex justify-between items-center mb-4;
}

.ui-card-title {
  @apply text-lg font-medium text-slate-800;
}

.ui-card-actions {
  @apply flex items-center;
}

/* 内容容器 */
.ui-content-container {
  @apply mx-auto;
}

.ui-container-full {
  width: 100%;
}

.ui-container-wide {
  width: 85%;
}

.ui-container-narrow {
  width: 65%;
}
</style> 