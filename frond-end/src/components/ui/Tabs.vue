<script setup>
import { computed, ref, provide, onMounted, watch } from 'vue';

// 组件属性
const props = defineProps({
  // 当前激活的标签页值
  modelValue: {
    type: [String, Number],
    default: ''
  },
  // 标签页类型: card, default, underline, button
  type: {
    type: String,
    default: 'default',
    validator: (value) => ['default', 'card', 'underline', 'button'].includes(value)
  },
  // 标签页大小: sm, md, lg
  size: {
    type: String,
    default: 'md',
    validator: (value) => ['sm', 'md', 'lg'].includes(value)
  },
  // 标签页位置: top, left, right, bottom
  position: {
    type: String,
    default: 'top',
    validator: (value) => ['top', 'left', 'right', 'bottom'].includes(value)
  },
  // 是否展示内容边框
  bordered: {
    type: Boolean,
    default: false
  },
  // 是否全宽度显示标签页
  fullWidth: {
    type: Boolean,
    default: false
  },
  // 居中对齐标签
  centered: {
    type: Boolean,
    default: false
  },
  // 禁用状态
  disabled: {
    type: Boolean,
    default: false
  }
});

// 事件
const emit = defineEmits(['update:modelValue', 'change']);

// 标签页项列表
const tabItems = ref([]);
// 当前激活标签页索引
const activeTab = ref(props.modelValue);

// 监听 modelValue 变化
watch(() => props.modelValue, (newValue) => {
  activeTab.value = newValue;
});

// 计算组件样式类
const tabsClasses = computed(() => {
  return [
    'ui-tabs',
    `ui-tabs-${props.type}`,
    `ui-tabs-${props.size}`,
    `ui-tabs-position-${props.position}`,
    props.bordered ? 'ui-tabs-bordered' : '',
    props.fullWidth ? 'ui-tabs-full-width' : '',
    props.centered ? 'ui-tabs-centered' : '',
    props.disabled ? 'ui-tabs-disabled' : '',
  ];
});

// 注册标签页
const registerTab = (tab) => {
  tabItems.value.push(tab);
};

// 移除标签页
const unregisterTab = (tab) => {
  const index = tabItems.value.findIndex(t => t.value === tab.value);
  if (index !== -1) {
    tabItems.value.splice(index, 1);
  }
};

// 切换标签页
const switchTab = (value) => {
  if (props.disabled) return;
  
  activeTab.value = value;
  emit('update:modelValue', value);
  emit('change', value);
};

// 默认选择第一个标签页(如果没有指定激活值)
onMounted(() => {
  if (!props.modelValue && tabItems.value.length > 0) {
    switchTab(tabItems.value[0].value);
  }
});

// 向子组件提供上下文
provide('tabs', {
  activeTab,
  registerTab,
  unregisterTab,
  switchTab
});
</script>

<template>
  <div :class="tabsClasses">
    <!-- 标签头部 -->
    <div class="ui-tabs-nav">
      <slot name="nav"></slot>
    </div>
    
    <!-- 标签内容 -->
    <div class="ui-tabs-content">
      <slot></slot>
    </div>
  </div>
</template>

<style scoped>
.ui-tabs {
  @apply w-full;
}

/* 水平或垂直布局 */
.ui-tabs-position-top,
.ui-tabs-position-bottom {
  @apply flex flex-col;
}

.ui-tabs-position-left,
.ui-tabs-position-right {
  @apply flex flex-row;
}

/* 标签头导航样式 */
.ui-tabs-nav {
  @apply flex relative;
}

/* 位置变体 */
.ui-tabs-position-top .ui-tabs-nav {
  @apply border-b border-slate-200 mb-4;
}

.ui-tabs-position-bottom .ui-tabs-nav {
  @apply border-t border-slate-200 mt-4 order-1;
}

.ui-tabs-position-left .ui-tabs-nav {
  @apply border-r border-slate-200 mr-4 flex-col;
}

.ui-tabs-position-right .ui-tabs-nav {
  @apply border-l border-slate-200 ml-4 flex-col order-1;
}

/* 全宽度标签 */
.ui-tabs-full-width .ui-tabs-nav {
  @apply w-full;
}

/* 居中对齐标签 */
.ui-tabs-centered .ui-tabs-nav {
  @apply justify-center;
}

/* 标签内容区域 */
.ui-tabs-content {
  @apply flex-1 min-w-0;
}

/* 带边框的内容区 */
.ui-tabs-bordered .ui-tabs-content {
  @apply border border-slate-200 rounded-md p-4;
}

.ui-tabs-position-top.ui-tabs-bordered .ui-tabs-content {
  @apply rounded-t-none border-t-0;
}

.ui-tabs-position-bottom.ui-tabs-bordered .ui-tabs-content {
  @apply rounded-b-none border-b-0;
}

.ui-tabs-position-left.ui-tabs-bordered .ui-tabs-content {
  @apply rounded-l-none border-l-0;
}

.ui-tabs-position-right.ui-tabs-bordered .ui-tabs-content {
  @apply rounded-r-none border-r-0;
}

/* 标签尺寸 */
.ui-tabs-sm .ui-tabs-nav {
  @apply text-sm;
}

.ui-tabs-lg .ui-tabs-nav {
  @apply text-base;
}

/* 禁用状态 */
.ui-tabs-disabled {
  @apply opacity-50 pointer-events-none;
}
</style> 