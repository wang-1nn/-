<script setup>
import { computed } from 'vue';

const props = defineProps({
  // 按钮类型: primary, secondary, success, warning, danger, ghost, link
  variant: {
    type: String,
    default: 'primary',
    validator: (value) => ['primary', 'secondary', 'success', 'warning', 'danger', 'ghost', 'link'].includes(value)
  },
  // 按钮大小: sm, md, lg
  size: {
    type: String,
    default: 'md',
    validator: (value) => ['sm', 'md', 'lg'].includes(value)
  },
  // 是否为轮廓按钮
  outline: {
    type: Boolean,
    default: false
  },
  // 是否为圆角按钮
  rounded: {
    type: Boolean,
    default: false
  },
  // 是否为块级按钮
  block: {
    type: Boolean,
    default: false
  },
  // 图标组件
  icon: {
    type: [Object, Function],
    default: null
  },
  // 图标位置: left, right
  iconPosition: {
    type: String,
    default: 'left',
    validator: (value) => ['left', 'right'].includes(value)
  },
  // 是否禁用
  disabled: {
    type: Boolean,
    default: false
  },
  // 加载状态
  loading: {
    type: Boolean,
    default: false
  },
  // 原生类型: button, submit, reset
  type: {
    type: String,
    default: 'button',
    validator: (value) => ['button', 'submit', 'reset'].includes(value)
  }
});

const emit = defineEmits(['click']);

// 处理点击事件
const handleClick = (event) => {
  if (props.loading || props.disabled) return;
  emit('click', event);
};

// 计算按钮基础类
const buttonClasses = computed(() => {
  return [
    'ui-button',
    `ui-button-${props.variant}`,
    `ui-button-size-${props.size}`,
    props.outline ? 'ui-button-outline' : '',
    props.rounded ? 'ui-button-rounded' : '',
    props.block ? 'ui-button-block' : '',
    props.disabled ? 'ui-button-disabled' : '',
    props.loading ? 'ui-button-loading' : '',
    props.icon && !slots.default ? 'ui-button-icon-only' : ''
  ];
});

// 计算按钮样式
const buttonStyles = computed(() => {
  return {};
});

// 检查是否有默认插槽
const slots = defineSlots();
const hasDefaultSlot = computed(() => {
  return slots.default !== undefined;
});

// 检查是否只有图标
const iconOnly = computed(() => {
  return props.icon && !hasDefaultSlot.value;
});
</script>

<template>
  <button
      :class="buttonClasses"
      :style="buttonStyles"
      :type="type"
      :disabled="disabled || loading"
      @click="handleClick"
  >
    <!-- 加载图标 -->
    <span v-if="loading" class="ui-button-spinner">
      <svg class="ui-spinner" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <circle class="ui-spinner-track" cx="12" cy="12" r="10" stroke="currentColor" stroke-opacity="0.25" stroke-width="4" />
        <path
            class="ui-spinner-head"
            d="M12 2C6.47715 2 2 6.47715 2 12C2 14.5361 2.98398 16.8429 4.58397 18.5"
            stroke="currentColor"
            stroke-width="4"
            stroke-linecap="round"
        />
      </svg>
    </span>

    <!-- 左侧图标 -->
    <component
        :is="icon"
        v-else-if="icon && iconPosition === 'left'"
        class="ui-button-icon ui-button-icon-left"
    />

    <!-- 文本内容 -->
    <span
        v-if="$slots.default"
        :class="{
        'ui-button-text': true,
        'ui-button-text-with-icon-left': icon && iconPosition === 'left' && !loading,
        'ui-button-text-with-icon-right': icon && iconPosition === 'right' && !loading,
        'ui-button-text-with-spinner': loading
      }"
    >
      <slot></slot>
    </span>

    <!-- 右侧图标 -->
    <component
        :is="icon"
        v-if="icon && iconPosition === 'right' && !loading"
        class="ui-button-icon ui-button-icon-right"
    />
  </button>
</template>

<style scoped>
.ui-button {
  @apply inline-flex items-center justify-center font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 focus-visible:ring-offset-2;
  @apply select-none cursor-pointer;
}

/* 尺寸变体 */
.ui-button-size-sm {
  @apply h-8 px-3 text-sm rounded;
  min-width: 2rem;
}

.ui-button-size-md {
  @apply h-10 px-4 text-sm rounded-md;
  min-width: 2.5rem;
}

.ui-button-size-lg {
  @apply h-12 px-6 text-base rounded-md;
  min-width: 3rem;
}

/* 类型变体 */
.ui-button-primary {
  @apply bg-primary-600 text-white hover:bg-primary-700 active:bg-primary-800;
}

.ui-button-secondary {
  @apply bg-slate-100 text-slate-900 hover:bg-slate-200 active:bg-slate-300;
}

.ui-button-success {
  @apply bg-success-color text-white hover:bg-success-600 active:bg-success-700;
}

.ui-button-warning {
  @apply bg-warning-color text-white hover:bg-warning-600 active:bg-warning-700;
}

.ui-button-danger {
  @apply bg-error-color text-white hover:bg-error-600 active:bg-error-700;
}

.ui-button-ghost {
  @apply bg-transparent text-slate-700 hover:bg-slate-100 active:bg-slate-200;
}

.ui-button-link {
  @apply bg-transparent text-primary-600 hover:text-primary-700 hover:underline active:text-primary-800 p-0;
}

/* 轮廓按钮 */
.ui-button-outline.ui-button-primary {
  @apply border border-primary-600 bg-transparent text-primary-600 hover:bg-primary-50;
}

.ui-button-outline.ui-button-secondary {
  @apply border border-slate-300 bg-transparent text-slate-700 hover:bg-slate-50;
}

.ui-button-outline.ui-button-success {
  @apply border border-success-500 bg-transparent text-success-600 hover:bg-success-50;
}

.ui-button-outline.ui-button-warning {
  @apply border border-warning-500 bg-transparent text-warning-600 hover:bg-warning-50;
}

.ui-button-outline.ui-button-danger {
  @apply border border-error-500 bg-transparent text-error-600 hover:bg-error-50;
}

/* 圆角按钮 */
.ui-button-rounded {
  @apply rounded-full;
}

/* 块级按钮 */
.ui-button-block {
  @apply w-full;
}

/* 禁用状态 */
.ui-button-disabled {
  @apply opacity-50 cursor-not-allowed pointer-events-none;
}

/* 加载状态 */
.ui-button-loading {
  @apply relative cursor-wait;
}

.ui-button-spinner {
  @apply absolute inset-0 flex items-center justify-center;
}

.ui-spinner {
  @apply h-5 w-5 animate-spin;
}

.ui-spinner-head {
  @apply animate-spin;
}

/* 图标样式 */
.ui-button-icon {
  @apply h-5 w-5 flex-shrink-0;
}

.ui-button-icon-left {
  @apply mr-2;
}

.ui-button-icon-right {
  @apply ml-2;
}

.ui-button-icon-only {
  @apply p-2;
  min-width: unset;
  width: 2.5rem;
}

.ui-button-size-sm.ui-button-icon-only {
  width: 2rem;
}

.ui-button-size-lg.ui-button-icon-only {
  width: 3rem;
}

/* 文本和图标间距 */
.ui-button-text-with-icon-left {
  @apply ml-2;
}

.ui-button-text-with-icon-right {
  @apply mr-2;
}

.ui-button-text-with-spinner {
  @apply invisible;
}
</style>