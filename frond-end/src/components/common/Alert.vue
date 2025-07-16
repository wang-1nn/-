<script setup>
import { computed, ref, onMounted } from 'vue';

const props = defineProps({
  // 提示类型: info, success, warning, error
  type: {
    type: String,
    default: 'info',
    validator: (value) => ['info', 'success', 'warning', 'error'].includes(value)
  },
  // 提示标题
  title: {
    type: String,
    default: ''
  },
  // 是否显示图标
  showIcon: {
    type: Boolean,
    default: true
  },
  // 是否可关闭
  closable: {
    type: Boolean,
    default: false
  },
  // 是否显示边框
  bordered: {
    type: Boolean,
    default: true
  },
  // 是否自动关闭
  autoClose: {
    type: Boolean,
    default: false
  },
  // 自动关闭延迟时间(ms)
  duration: {
    type: Number,
    default: 3000
  },
  // 描述文本
  description: {
    type: String,
    default: ''
  },
  // 自定义图标组件
  icon: {
    type: [Object, Function],
    default: null
  },
  // 自定义类名
  customClass: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['close']);

// 控制显示状态
const isVisible = ref(true);
// 自动关闭计时器
let timer = null;

// 计算不同类型的默认图标
const defaultIcon = computed(() => {
  switch (props.type) {
    case 'info':
      return `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>`;
    case 'success':
      return `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>`;
    case 'warning':
      return `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>`;
    case 'error':
      return `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="15" y1="9" x2="9" y2="15"></line><line x1="9" y1="9" x2="15" y2="15"></line></svg>`;
    default:
      return `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>`;
  }
});

// 计算样式类
const alertClasses = computed(() => {
  return [
    'ui-alert',
    `ui-alert-${props.type}`,
    props.bordered ? 'ui-alert-bordered' : '',
    props.description ? 'ui-alert-with-description' : '',
    props.customClass
  ];
});

// 处理关闭
const handleClose = () => {
  isVisible.value = false;
  clearTimeout(timer);
  emit('close');
};

// 设置自动关闭
onMounted(() => {
  if (props.autoClose && props.duration > 0) {
    timer = setTimeout(() => {
      handleClose();
    }, props.duration);
  }

  return () => {
    if (timer) {
      clearTimeout(timer);
    }
  };
});
</script>

<template>
  <transition name="ui-alert-fade">
    <div v-if="isVisible" :class="alertClasses" role="alert">
      <!-- 图标 -->
      <div v-if="showIcon" class="ui-alert-icon">
        <component v-if="icon" :is="icon" class="ui-icon" />
        <span v-else v-html="defaultIcon"></span>
      </div>

      <!-- 内容 -->
      <div class="ui-alert-content">
        <div v-if="title" class="ui-alert-title">{{ title }}</div>
        <div class="ui-alert-message">
          <slot>{{ description }}</slot>
        </div>
      </div>

      <!-- 关闭按钮 -->
      <button v-if="closable" @click="handleClose" class="ui-alert-close">
        <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="18" y1="6" x2="6" y2="18"></line>
          <line x1="6" y1="6" x2="18" y2="18"></line>
        </svg>
      </button>
    </div>
  </transition>
</template>

<style scoped>
.ui-alert {
  @apply flex items-start p-4 rounded-md w-full mb-4 relative;
}

/* 类型变体 */
.ui-alert-info {
  @apply bg-blue-50 text-blue-800;
}

.ui-alert-success {
  @apply bg-green-50 text-green-800;
}

.ui-alert-warning {
  @apply bg-yellow-50 text-yellow-800;
}

.ui-alert-error {
  @apply bg-red-50 text-red-800;
}

/* 边框样式 */
.ui-alert-bordered.ui-alert-info {
  @apply border border-blue-200;
}

.ui-alert-bordered.ui-alert-success {
  @apply border border-green-200;
}

.ui-alert-bordered.ui-alert-warning {
  @apply border border-yellow-200;
}

.ui-alert-bordered.ui-alert-error {
  @apply border border-red-200;
}

/* 图标 */
.ui-alert-icon {
  @apply flex-shrink-0 mr-3 mt-0.5;
}

.ui-alert-info .ui-alert-icon {
  @apply text-blue-500;
}

.ui-alert-success .ui-alert-icon {
  @apply text-green-500;
}

.ui-alert-warning .ui-alert-icon {
  @apply text-yellow-500;
}

.ui-alert-error .ui-alert-icon {
  @apply text-red-500;
}

/* 内容 */
.ui-alert-content {
  @apply flex-1;
}

.ui-alert-title {
  @apply font-medium mb-1;
}

.ui-alert-message {
  @apply text-sm;
}

.ui-alert-with-description .ui-alert-title {
  @apply text-base;
}

/* 关闭按钮 */
.ui-alert-close {
  @apply flex-shrink-0 ml-4 p-1 rounded-md hover:bg-black hover:bg-opacity-10 transition-colors duration-150;
}

/* 淡入淡出动画 */
.ui-alert-fade-enter-active,
.ui-alert-fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}

.ui-alert-fade-enter-from,
.ui-alert-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>