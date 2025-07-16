<script setup>
import { computed, inject } from 'vue';

const props = defineProps({
  // 标签项
  tab: {
    type: Object,
    required: true
  },
  // 导航项类型 (default, card, underline, button)
  type: {
    type: String,
    default: 'default',
    validator: (value) => ['default', 'card', 'underline', 'button'].includes(value)
  },
  // 导航项大小
  size: {
    type: String,
    default: 'md',
    validator: (value) => ['sm', 'md', 'lg'].includes(value)
  },
  // 是否全宽度
  fullWidth: {
    type: Boolean,
    default: false
  }
});

// 获取父组件上下文
const tabs = inject('tabs', null);
if (!tabs) {
  console.error('TabNav 组件必须在 Tabs 组件内使用');
}

// 计算是否为活动标签
const isActive = computed(() => tabs.activeTab.value === props.tab.value);

// 计算样式类
const navItemClasses = computed(() => {
  return [
    'ui-tab-nav-item',
    `ui-tab-nav-item-${props.type}`,
    `ui-tab-nav-item-size-${props.size}`,
    isActive.value ? 'ui-tab-nav-item-active' : '',
    props.tab.disabled ? 'ui-tab-nav-item-disabled' : '',
    props.fullWidth ? 'ui-tab-nav-item-full-width' : ''
  ];
});

// 处理点击切换标签
const handleClick = () => {
  if (props.tab.disabled) return;
  tabs.switchTab(props.tab.value);
};
</script>

<template>
  <div 
    :class="navItemClasses" 
    @click="handleClick" 
    :aria-selected="isActive" 
    role="tab"
  >
    <div v-if="tab.icon" class="ui-tab-nav-item-icon">
      <component :is="tab.icon" />
    </div>
    <div class="ui-tab-nav-item-text">{{ tab.label }}</div>
  </div>
</template>

<style scoped>
/* 基础样式 */
.ui-tab-nav-item {
  @apply cursor-pointer transition-colors duration-200 flex items-center;
}

.ui-tab-nav-item-disabled {
  @apply opacity-50 cursor-not-allowed;
}

.ui-tab-nav-item-full-width {
  @apply flex-1 justify-center;
}

/* 图标 */
.ui-tab-nav-item-icon {
  @apply mr-2 flex-shrink-0;
}

.ui-tab-nav-item-icon svg {
  @apply w-4 h-4;
}

/* 类型样式 */
.ui-tab-nav-item-default {
  @apply px-4 py-2 font-medium text-slate-600 hover:text-primary-600;
}

.ui-tab-nav-item-default.ui-tab-nav-item-active {
  @apply text-primary-600 relative;
}

.ui-tab-nav-item-default.ui-tab-nav-item-active::after {
  content: "";
  @apply absolute bottom-0 left-0 w-full h-0.5 bg-primary-600 rounded-t;
}

.ui-tab-nav-item-card {
  @apply px-4 py-2 border-t border-l border-r border-transparent rounded-t-md text-slate-600 font-medium;
}

.ui-tab-nav-item-card.ui-tab-nav-item-active {
  @apply bg-white border-slate-200 border-b-white text-primary-600 relative -mb-px;
}

.ui-tab-nav-item-underline {
  @apply px-4 py-2 font-medium text-slate-600 hover:text-primary-600;
}

.ui-tab-nav-item-underline.ui-tab-nav-item-active {
  @apply text-primary-600 border-b-2 border-primary-600;
}

.ui-tab-nav-item-button {
  @apply px-4 py-2 rounded-md text-slate-600 font-medium hover:bg-slate-100;
}

.ui-tab-nav-item-button.ui-tab-nav-item-active {
  @apply bg-primary-100 text-primary-700;
}

/* 尺寸变体 */
.ui-tab-nav-item-size-sm {
  @apply text-sm py-1.5 px-3;
}

.ui-tab-nav-item-size-lg {
  @apply text-base py-2.5 px-5;
}
</style> 