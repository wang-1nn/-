<script setup>
import { computed, ref, watch } from 'vue';

const props = defineProps({
  // 输入框类型
  type: {
    type: String,
    default: 'text',
    validator: (value) => [
      'text', 'password', 'email', 'number', 'tel', 'url', 'search',
      'date', 'time', 'datetime-local', 'month', 'week'
    ].includes(value)
  },
  // 输入框值
  modelValue: {
    type: [String, Number],
    default: ''
  },
  // 输入框标签
  label: {
    type: String,
    default: ''
  },
  // 占位符
  placeholder: {
    type: String,
    default: ''
  },
  // 是否禁用
  disabled: {
    type: Boolean,
    default: false
  },
  // 是否只读
  readonly: {
    type: Boolean,
    default: false
  },
  // 是否必填
  required: {
    type: Boolean,
    default: false
  },
  // 输入框大小: sm, md, lg
  size: {
    type: String,
    default: 'md',
    validator: (value) => ['sm', 'md', 'lg'].includes(value)
  },
  // 输入框变体: default, outlined, underlined, filled
  variant: {
    type: String,
    default: 'default',
    validator: (value) => ['default', 'outlined', 'underlined', 'filled'].includes(value)
  },
  // 输入框前图标组件
  prefixIcon: {
    type: [Object, Function],
    default: null
  },
  // 输入框后图标组件
  suffixIcon: {
    type: [Object, Function],
    default: null
  },
  // 错误消息
  error: {
    type: String,
    default: ''
  },
  // 提示消息
  hint: {
    type: String,
    default: ''
  },
  // 最大长度
  maxlength: {
    type: [String, Number],
    default: null
  },
  // 自动完成
  autocomplete: {
    type: String,
    default: 'off'
  },
  // 自动获取焦点
  autofocus: {
    type: Boolean,
    default: false
  },
  // 名称
  name: {
    type: String,
    default: ''
  },
  // 输入框样式类
  inputClass: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['update:modelValue', 'focus', 'blur', 'input', 'change']);

// 生成唯一ID
const uid = ref(`input-${Math.random().toString(36).substring(2, 9)}`);

// 内部输入值，用于同步和事件处理
const inputValue = ref(props.modelValue);

// 是否聚焦
const isFocused = ref(false);

// 同步内部值和传入值
watch(() => props.modelValue, (newValue) => {
  inputValue.value = newValue;
});

// 处理输入事件
const handleInput = (event) => {
  const value = event.target.value;
  inputValue.value = value;
  emit('update:modelValue', value);
  emit('input', event);
};

// 处理变化事件
const handleChange = (event) => {
  emit('change', event);
};

// 处理焦点事件
const handleFocus = (event) => {
  isFocused.value = true;
  emit('focus', event);
};

// 处理失焦事件
const handleBlur = (event) => {
  isFocused.value = false;
  emit('blur', event);
};

// 计算容器类
const containerClasses = computed(() => {
  return [
    'ui-input-container',
    `ui-input-${props.variant}`,
    `ui-input-size-${props.size}`,
    props.disabled ? 'ui-input-disabled' : '',
    props.readonly ? 'ui-input-readonly' : '',
    props.error ? 'ui-input-error' : '',
    isFocused.value ? 'ui-input-focused' : '',
    props.prefixIcon ? 'ui-input-has-prefix-icon' : '',
    props.suffixIcon ? 'ui-input-has-suffix-icon' : '',
  ];
});

// 计算输入框类
const inputClasses = computed(() => {
  return [
    'ui-input',
    props.inputClass
  ];
});

// 展示字数统计
const showCharCount = computed(() => {
  return props.maxlength !== null && 
         typeof props.modelValue === 'string' && 
         (props.type === 'text' || props.type === 'textarea');
});

// 当前字符长度
const charCount = computed(() => {
  return typeof props.modelValue === 'string' ? props.modelValue.length : 0;
});
</script>

<template>
  <div class="ui-input-wrapper">
    <label v-if="label" :for="uid" class="ui-input-label">
      {{ label }}
      <span v-if="required" class="ui-input-required">*</span>
    </label>
    
    <div :class="containerClasses">
      <!-- 前置图标 -->
      <div v-if="prefixIcon" class="ui-input-prefix">
        <component :is="prefixIcon" class="ui-input-icon" />
      </div>
      
      <!-- 输入框 -->
      <input
        :id="uid"
        :type="type"
        :value="modelValue"
        :placeholder="placeholder"
        :disabled="disabled"
        :readonly="readonly"
        :required="required"
        :maxlength="maxlength"
        :autocomplete="autocomplete"
        :autofocus="autofocus"
        :name="name"
        :class="inputClasses"
        @input="handleInput"
        @change="handleChange"
        @focus="handleFocus"
        @blur="handleBlur"
      />
      
      <!-- 后置图标 -->
      <div v-if="suffixIcon" class="ui-input-suffix">
        <component :is="suffixIcon" class="ui-input-icon" />
      </div>
      
      <!-- 字数统计 -->
      <div v-if="showCharCount" class="ui-input-char-count">
        {{ charCount }}/{{ maxlength }}
      </div>
    </div>
    
    <!-- 错误信息或提示 -->
    <div v-if="error" class="ui-input-error-message">
      {{ error }}
    </div>
    <div v-else-if="hint" class="ui-input-hint">
      {{ hint }}
    </div>
  </div>
</template>

<style scoped>
.ui-input-wrapper {
  @apply flex flex-col w-full;
}

.ui-input-label {
  @apply block text-sm font-medium text-slate-700 mb-1;
}

.ui-input-required {
  @apply text-error-color ml-0.5;
}

.ui-input-container {
  @apply flex items-center w-full relative bg-white border border-slate-300 transition-all duration-200;
}

/* 尺寸变体 */
.ui-input-size-sm {
  @apply h-8 text-sm rounded;
}

.ui-input-size-md {
  @apply h-10 text-sm rounded-md;
}

.ui-input-size-lg {
  @apply h-12 text-base rounded-md;
}

/* 类型变体 */
.ui-input-default {
  @apply bg-white border-slate-300 shadow-sm;
}

.ui-input-outlined {
  @apply bg-transparent;
}

.ui-input-underlined {
  @apply border-x-0 border-t-0 border-b-2 rounded-none;
}

.ui-input-filled {
  @apply bg-slate-100 border-transparent;
}

/* 输入框 */
.ui-input {
  @apply block w-full h-full px-3 bg-transparent border-none outline-none appearance-none;
}

/* 尺寸调整内边距 */
.ui-input-size-sm .ui-input {
  @apply px-2;
}

.ui-input-size-lg .ui-input {
  @apply px-4;
}

/* 状态 */
.ui-input-focused {
  @apply border-primary-500 ring-2 ring-primary-100;
}

.ui-input-disabled {
  @apply bg-slate-100 border-slate-200 cursor-not-allowed opacity-75;
}

.ui-input-readonly {
  @apply bg-slate-50;
}

.ui-input-error {
  @apply border-error-color;
}

.ui-input-error.ui-input-focused {
  @apply ring-2 ring-error-color/20;
}

/* 图标 */
.ui-input-prefix,
.ui-input-suffix {
  @apply flex items-center justify-center h-full px-3 text-slate-400;
}

.ui-input-has-prefix-icon .ui-input {
  @apply pl-0;
}

.ui-input-has-suffix-icon .ui-input {
  @apply pr-0;
}

.ui-input-icon {
  @apply w-5 h-5 text-slate-400;
}

/* 字数统计 */
.ui-input-char-count {
  @apply absolute right-3 text-xs text-slate-400;
}

/* 错误消息和提示 */
.ui-input-error-message {
  @apply mt-1 text-xs text-error-color;
}

.ui-input-hint {
  @apply mt-1 text-xs text-slate-500;
}
</style> 