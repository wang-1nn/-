<script setup>
import { ref, inject, onBeforeUnmount, onMounted } from 'vue';

const props = defineProps({
  id: {
    type: [String, Number],
    required: true
  },
  label: {
    type: String,
    required: true
  },
  icon: {
    type: [Object, Function],
    default: null
  }
});

// 获取从Tabs组件提供的上下文
const { registerTab, unregisterTab, isTabActive } = inject('tabs');

// 在组件挂载时注册标签页
onMounted(() => {
  registerTab({
    id: props.id,
    label: props.label,
    icon: props.icon
  });
});

// 在组件销毁前注销标签页
onBeforeUnmount(() => {
  unregisterTab(props.id);
});

// 计算当前标签页是否处于活动状态
const active = computed(() => isTabActive(props.id));
</script>

<template>
  <!-- 只有当标签页处于活动状态时，才渲染其内容 -->
  <div v-if="active" class="tab-panel">
    <slot></slot>
  </div>
</template>

<style scoped>
.tab-panel {
  @apply w-full;
}
</style>