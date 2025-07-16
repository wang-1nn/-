<script setup>
import { ref, computed, inject, onMounted, onBeforeUnmount, getCurrentInstance } from 'vue';

const props = defineProps({
  // 标签页标题
  label: {
    type: String,
    required: true
  },
  // 标签页唯一标识
  value: {
    type: [String, Number],
    default: null
  },
  // 是否禁用
  disabled: {
    type: Boolean,
    default: false
  },
  // 标签页图标
  icon: {
    type: [Object, Function],
    default: null
  },
  // 是否延迟渲染内容
  lazy: {
    type: Boolean,
    default: false
  }
});

// 获取父组件上下文
const tabs = inject('tabs', null);
if (!tabs) {
  console.error('TabPane 组件必须在 Tabs 组件内使用');
}

// 实例uid作为备选value
const instance = getCurrentInstance();
const uid = ref(`tab-${instance ? instance.uid : Math.random().toString(36).substr(2, 9)}`);

// 计算value值，如果没有提供value属性，则使用uid
const internalValue = computed(() => props.value !== null ? props.value : uid.value);

// 计算是否为活动标签
const isActive = computed(() => tabs.activeTab.value === internalValue.value);

// 是否曾经激活过（用于延迟渲染）
const hasBeenActivated = ref(!props.lazy || isActive.value);

// 监听激活状态
if (props.lazy) {
  // 如果标签变为激活状态，更新hasBeenActivated
  watch(isActive, (newVal) => {
    if (newVal) {
      hasBeenActivated.value = true;
    }
  });
}

// 计算标签页样式
const paneClasses = computed(() => {
  return [
    'ui-tab-pane',
    isActive.value ? 'ui-tab-pane-active' : ''
  ];
});

// 注册和注销标签页
onMounted(() => {
  tabs.registerTab({
    label: props.label,
    value: internalValue.value,
    disabled: props.disabled,
    icon: props.icon
  });
});

onBeforeUnmount(() => {
  tabs.unregisterTab({
    value: internalValue.value
  });
});

// 提供给NavItem组件使用的数据
defineExpose({
  value: internalValue,
  label: props.label,
  disabled: props.disabled,
  icon: props.icon,
  isActive
});
</script>

<template>
  <div v-if="isActive" :class="paneClasses">
    <!-- 只有在标签激活且不是延迟渲染，或者已经激活过的情况下才渲染内容 -->
    <template v-if="!lazy || hasBeenActivated">
      <slot></slot>
    </template>
  </div>
</template>

<style scoped>
.ui-tab-pane {
  @apply w-full;
}
</style> 