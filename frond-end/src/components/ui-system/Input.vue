<script setup>
import { computed, ref } from 'vue';

const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: ''
  },
  type: {
    type: String,
    default: 'text'
  },
  placeholder: {
    type: String,
    default: ''
  },
  label: {
    type: String,
    default: ''
  },
  error: {
    type: String,
    default: ''
  },
  hint: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  },
  readonly: {
    type: Boolean,
    default: false
  },
  required: {
    type: Boolean,
    default: false
  },
  id: {
    type: String,
    default: () => `input-${Math.random().toString(36).substring(2, 10)}`
  },
  prefix: {
    type: String,
    default: ''
  },
  suffix: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['update:modelValue', 'focus', 'blur', 'input']);

const inputRef = ref(null);
const focused = ref(false);

// 计算输入框包装器类名
const wrapperClasses = computed(() => {
  const classes = ['input-wrapper'];
  
  if (props.error) {
    classes.push('has-error');
  }
  
  if (focused.value) {
    classes.push('is-focused');
  }
  
  if (props.disabled) {
    classes.push('is-disabled');
  }
  
  return classes.join(' ');
});

// 计算是否有前缀或后缀
const hasPrefix = computed(() => Boolean(props.prefix || $slots.prefix));
const hasSuffix = computed(() => Boolean(props.suffix || $slots.suffix));

// 处理输入事件
const handleInput = (event) => {
  emit('update:modelValue', event.target.value);
  emit('input', event);
};

// 处理聚焦和失焦事件
const handleFocus = (event) => {
  focused.value = true;
  emit('focus', event);
};

const handleBlur = (event) => {
  focused.value = false;
  emit('blur', event);
};

// 聚焦输入框
const focus = () => {
  inputRef.value?.focus();
};

// 向外部暴露方法
defineExpose({ focus });
</script>

<template>
  <div class="input-container">
    <!-- 标签 -->
    <label v-if="label" :for="id" class="input-label">
      {{ label }}
      <span v-if="required" class="input-required">*</span>
    </label>
    
    <div :class="wrapperClasses">
      <!-- 前缀 -->
      <div v-if="hasPrefix" class="input-prefix">
        <slot name="prefix">{{ prefix }}</slot>
      </div>
      
      <!-- 输入框 -->
      <input
        ref="inputRef"
        :id="id"
        :value="modelValue"
        :type="type"
        :placeholder="placeholder"
        :disabled="disabled"
        :readonly="readonly"
        :required="required"
        class="input-field"
        @input="handleInput"
        @focus="handleFocus"
        @blur="handleBlur"
      />
      
      <!-- 后缀 -->
      <div v-if="hasSuffix" class="input-suffix">
        <slot name="suffix">{{ suffix }}</slot>
      </div>
      
      <!-- 清除按钮 -->
      <button
        v-if="modelValue && !disabled && !readonly && type !== 'password'"
        type="button"
        class="input-clear"
        @click="$emit('update:modelValue', '')"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
    </div>
    
    <!-- 错误消息 -->
    <div v-if="error" class="input-error">{{ error }}</div>
    
    <!-- 提示信息 -->
    <div v-else-if="hint" class="input-hint">{{ hint }}</div>
  </div>
</template>

<style scoped>
.input-container {
  @apply space-y-1.5;
}

.input-label {
  @apply block text-sm font-medium text-gray-700;
}

.input-required {
  @apply text-red-500;
}

.input-wrapper {
  @apply flex items-center border border-gray-300 bg-white rounded-lg overflow-hidden transition-all duration-200;
}

.input-wrapper.is-focused {
  @apply ring-2 ring-blue-500 border-blue-500;
}

.input-wrapper.has-error {
  @apply ring-2 ring-red-500 border-red-500;
}

.input-wrapper.is-disabled {
  @apply bg-gray-100 opacity-75 cursor-not-allowed;
}

.input-prefix, .input-suffix {
  @apply flex items-center justify-center px-3 text-gray-500;
}

.input-field {
  @apply flex-1 w-full px-3 py-2 bg-transparent outline-none text-gray-800;
}

.input-field::placeholder {
  @apply text-gray-400;
}

.input-field:disabled {
  @apply cursor-not-allowed;
}

.input-clear {
  @apply flex items-center justify-center w-8 h-8 text-gray-400 hover:text-gray-600 transition-colors duration-200;
}

.input-error {
  @apply text-red-500 text-xs mt-1;
}

.input-hint {
  @apply text-gray-500 text-xs mt-1;
}
</style>