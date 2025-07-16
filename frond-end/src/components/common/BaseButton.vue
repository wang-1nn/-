<script setup>
import { computed, useSlots } from 'vue';

const props = defineProps({
  // 按钮类型：primary, success, warning, danger, info, default
  type: {
    type: String,
    default: 'default',
    validator: (value) => ['primary', 'success', 'warning', 'danger', 'info', 'default'].includes(value)
  },
  // 按钮大小：large, default, small, mini
  size: {
    type: String,
    default: 'default',
    validator: (value) => ['large', 'default', 'small', 'mini'].includes(value)
  },
  // 是否为朴素按钮
  plain: {
    type: Boolean,
    default: false
  },
  // 是否为圆角按钮
  round: {
    type: Boolean,
    default: false
  },
  // 是否为圆形按钮
  circle: {
    type: Boolean,
    default: false
  },
  // 是否为链接按钮
  link: {
    type: Boolean,
    default: false
  },
  // 是否为文字按钮
  text: {
    type: Boolean,
    default: false
  },
  // 是否为块级按钮
  block: {
    type: Boolean,
    default: false
  },
  // 图标类名
  icon: {
    type: String,
    default: ''
  },
  // 是否禁用
  disabled: {
    type: Boolean,
    default: false
  },
  // 是否加载中
  loading: {
    type: Boolean,
    default: false
  },
  // 原生类型：button, submit, reset
  nativeType: {
    type: String,
    default: 'button',
    validator: (value) => ['button', 'submit', 'reset'].includes(value)
  },
  // 自定义类名
  customClass: {
    type: String,
    default: ''
  }
});

const slots = useSlots();

// 发出点击事件
const emit = defineEmits(['click']);

// 处理点击事件
const handleClick = (event) => {
  if (props.loading || props.disabled) return;
  emit('click', event);
};

// 计算按钮样式类
const buttonClass = computed(() => {
  const classes = [`btn-${props.type}`];

  // 大小
  if (props.size !== 'default') {
    classes.push(`btn-${props.size}`);
  }

  // 形状和样式变体
  if (props.plain) classes.push('btn-plain');
  if (props.round) classes.push('btn-round');
  if (props.circle) classes.push('btn-circle');
  if (props.link) classes.push('btn-link');
  if (props.text) classes.push('btn-text');
  if (props.block) classes.push('btn-block');
  if (props.disabled) classes.push('btn-disabled');
  if (props.loading) classes.push('btn-loading');
  if (props.icon && !props.loading && !slots.default) classes.push('btn-icon-only');

  // 添加自定义类名
  if (props.customClass) classes.push(props.customClass);

  return classes.join(' ');
});

// 判断是否只有图标无文字
const isIconOnly = computed(() => {
  return Boolean(props.icon && !slots.default);
});
</script>

<template>
  <button
      :class="['base-button', buttonClass]"
      :type="nativeType"
      :disabled="disabled || loading"
      @click="handleClick"
  >
    <!-- 加载图标 -->
    <span v-if="loading" class="btn-loading-icon">
      <svg viewBox="25 25 50 50" class="loading-spinner">
        <circle cx="50" cy="50" r="20" fill="none" />
      </svg>
    </span>

    <!-- 图标 -->
    <i v-else-if="icon" :class="icon"></i>

    <!-- 内容插槽 -->
    <span v-if="$slots.default" :class="{'ml-2': (loading || icon) && $slots.default}">
      <slot></slot>
    </span>
  </button>
</template>

<style scoped>
.base-button {
  @apply inline-flex items-center justify-center font-medium cursor-pointer transition-all duration-200 outline-none;
  @apply px-4 py-2 text-sm rounded-md;
  line-height: 1.5;
  margin: 0;
  border: 1px solid transparent;
}

/* 按钮类型 */
.btn-default {
  @apply bg-white text-gray-700 border-gray-300 hover:text-blue-600 hover:border-blue-400;
}

.btn-primary {
  @apply bg-blue-500 text-white hover:bg-blue-600 border-transparent;
}

.btn-success {
  @apply bg-green-500 text-white hover:bg-green-600 border-transparent;
}

.btn-warning {
  @apply bg-yellow-500 text-white hover:bg-yellow-600 border-transparent;
}

.btn-danger {
  @apply bg-red-500 text-white hover:bg-red-600 border-transparent;
}

.btn-info {
  @apply bg-gray-500 text-white hover:bg-gray-600 border-transparent;
}

/* 朴素按钮 */
.btn-plain.btn-primary {
  @apply bg-white text-blue-500 border-blue-500 hover:bg-blue-50;
}

.btn-plain.btn-success {
  @apply bg-white text-green-500 border-green-500 hover:bg-green-50;
}

.btn-plain.btn-warning {
  @apply bg-white text-yellow-500 border-yellow-500 hover:bg-yellow-50;
}

.btn-plain.btn-danger {
  @apply bg-white text-red-500 border-red-500 hover:bg-red-50;
}

.btn-plain.btn-info {
  @apply bg-white text-gray-500 border-gray-500 hover:bg-gray-50;
}

/* 大小变体 */
.btn-large {
  @apply px-6 py-3 text-base;
}

.btn-small {
  @apply px-3 py-1 text-xs;
}

.btn-mini {
  @apply px-2 py-0.5 text-xs;
}

/* 形状变体 */
.btn-round {
  @apply rounded-full;
}

.btn-circle {
  @apply rounded-full p-0;
  width: 2.5rem;
  height: 2.5rem;
}

.btn-circle.btn-large {
  width: 3.5rem;
  height: 3.5rem;
}

.btn-circle.btn-small {
  width: 2rem;
  height: 2rem;
}

.btn-circle.btn-mini {
  width: 1.5rem;
  height: 1.5rem;
}

/* 链接按钮 */
.btn-link {
  @apply bg-transparent border-transparent text-blue-500 hover:text-blue-700 hover:underline p-0;
}

/* 文字按钮 */
.btn-text {
  @apply bg-transparent border-transparent text-gray-700 hover:bg-gray-100 hover:text-gray-900;
}

/* 块级按钮 */
.btn-block {
  @apply w-full flex;
}

/* 禁用状态 */
.btn-disabled {
  @apply opacity-60 cursor-not-allowed pointer-events-none;
}

/* 加载状态 */
.btn-loading {
  @apply opacity-80 cursor-wait;
}

.btn-loading-icon {
  display: inline-block;
  width: 1em;
  height: 1em;
  animation: btn-loading-rotate 1s linear infinite;
}

.loading-spinner {
  animation: loading-dash 1.5s ease-in-out infinite;
  stroke-dasharray: 90, 150;
  stroke-dashoffset: 0;
  stroke-width: 4;
  stroke-linecap: round;
  stroke: currentColor;
}

@keyframes btn-loading-rotate {
  100% {
    transform: rotate(360deg);
  }
}

@keyframes loading-dash {
  0% {
    stroke-dasharray: 1, 200;
    stroke-dashoffset: 0;
  }
  50% {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: -40px;
  }
  100% {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: -120px;
  }
}
</style>