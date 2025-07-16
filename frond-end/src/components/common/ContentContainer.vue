<script setup>
import { computed } from 'vue';

const props = defineProps({
  // 容器宽度类型：full(100%), wide(85%), narrow(65%), fixed(固定像素)
  width: {
    type: String,
    default: 'wide',
    validator: (val) => ['full', 'wide', 'narrow', 'fixed'].includes(val)
  },
  // 固定宽度值（当width为'fixed'时使用）
  fixedWidth: {
    type: [String, Number],
    default: '1200px'
  },
  // 内边距
  padding: {
    type: String,
    default: 'default',
    validator: (val) => ['none', 'small', 'default', 'large'].includes(val)
  },
  // 是否居中显示
  center: {
    type: Boolean,
    default: true
  }
});

// 计算容器样式
const containerStyle = computed(() => {
  const styles = {};

  // 固定宽度
  if (props.width === 'fixed') {
    styles.maxWidth = typeof props.fixedWidth === 'number'
        ? `${props.fixedWidth}px`
        : props.fixedWidth;
  }

  return styles;
});

// 计算容器类名
const containerClass = computed(() => {
  const classes = ['ui-content-container'];

  // 宽度类型
  classes.push(`ui-container-${props.width}`);

  // 内边距
  classes.push(`ui-container-padding-${props.padding}`);

  // 居中
  if (props.center) {
    classes.push('ui-container-center');
  }

  return classes;
});
</script>

<template>
  <div :class="containerClass" :style="containerStyle">
    <slot></slot>
  </div>
</template>

<style scoped>
.ui-content-container {
  @apply w-full;
}

.ui-container-center {
  @apply mx-auto;
}

.ui-container-full {
  width: 100%;
  max-width: 100%;
}

.ui-container-wide {
  width: 85%;
  max-width: 1440px;
}

.ui-container-narrow {
  width: 65%;
  max-width: 1200px;
}

.ui-container-padding-none {
  @apply p-0;
}

.ui-container-padding-small {
  @apply px-4 py-3;
}

.ui-container-padding-default {
  @apply px-6 py-4;
}

.ui-container-padding-large {
  @apply px-8 py-6;
}
</style>