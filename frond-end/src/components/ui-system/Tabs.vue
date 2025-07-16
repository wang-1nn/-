<script setup>
import { computed, ref, provide, onMounted } from 'vue';

const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: ''
  },
  variant: {
    type: String,
    default: 'underline',
    validator: (value) => ['underline', 'pills', 'enclosed'].includes(value)
  }
});

const emit = defineEmits(['update:modelValue']);

// 存储所有标签页及其内容
const tabs = ref([]);
const activeTab = ref(props.modelValue);

// 计算标签栏的类名
const tabsListClasses = computed(() => {
  return [`tabs-list-${props.variant}`];
});

// 当活动标签改变时，更新模型值
const setActiveTab = (id) => {
  activeTab.value = id;
  emit('update:modelValue', id);
};

// 注册标签页
const registerTab = (tab) => {
  tabs.value.push(tab);
};

// 注销标签页
const unregisterTab = (tabId) => {
  const index = tabs.value.findIndex(tab => tab.id === tabId);
  if (index !== -1) {
    tabs.value.splice(index, 1);
  }
};

// 检查是否是活动标签页
const isTabActive = (tabId) => {
  return activeTab.value === tabId;
};

// 为子组件提供上下文
provide('tabs', {
  activeTab,
  registerTab,
  unregisterTab,
  setActiveTab,
  isTabActive
});

// 组件挂载时，如果没有设置默认标签，则选择第一个
onMounted(() => {
  if (!activeTab.value && tabs.value.length) {
    setActiveTab(tabs.value[0].id);
  }
});
</script>

<template>
  <div class="tabs">
    <!-- 标签页标题列表 -->
    <div :class="tabsListClasses">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        class="tab-trigger"
        :class="{
          'tab-active': isTabActive(tab.id),
          [`tab-trigger-${variant}`]: true
        }"
        @click="setActiveTab(tab.id)"
      >
        <!-- 标签图标 -->
        <span v-if="tab.icon" class="tab-icon">
          <component :is="tab.icon" />
        </span>
        
        <!-- 标签文本 -->
        {{ tab.label }}
      </button>
    </div>
    
    <!-- 标签页内容 -->
    <div class="tab-content">
      <slot></slot>
    </div>
  </div>
</template>

<style scoped>
.tabs {
  @apply flex flex-col;
}

/* 标签列表样式 - 下划线风格 */
.tabs-list-underline {
  @apply flex space-x-2 border-b border-gray-200;
}

.tab-trigger-underline {
  @apply px-4 py-2 border-b-2 border-transparent text-gray-600 hover:text-gray-800 transition-colors duration-200;
}

.tab-trigger-underline.tab-active {
  @apply border-blue-600 text-blue-600 font-medium;
}

/* 标签列表样式 - 药丸风格 */
.tabs-list-pills {
  @apply flex space-x-2;
}

.tab-trigger-pills {
  @apply px-4 py-2 rounded-lg text-gray-600 hover:text-gray-800 transition-colors duration-200;
}

.tab-trigger-pills.tab-active {
  @apply bg-blue-600 text-white font-medium;
}

/* 标签列表样式 - 封闭风格 */
.tabs-list-enclosed {
  @apply flex;
}

.tab-trigger-enclosed {
  @apply px-4 py-2 border-t border-l border-r border-transparent bg-gray-50 text-gray-600 hover:text-gray-800 transition-colors duration-200;
}

.tab-trigger-enclosed:first-child {
  @apply rounded-tl-lg;
}

.tab-trigger-enclosed:last-child {
  @apply rounded-tr-lg;
}

.tab-trigger-enclosed.tab-active {
  @apply bg-white border-gray-200 border-b-white text-blue-600 font-medium;
}

/* 通用触发器样式 */
.tab-trigger {
  @apply flex items-center whitespace-nowrap;
}

.tab-icon {
  @apply mr-2;
}

/* 内容样式 */
.tab-content {
  @apply py-4;
}
</style>