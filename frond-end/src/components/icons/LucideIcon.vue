<script setup>
import { defineProps, computed } from 'vue';
import * as LucideIcons from 'lucide-vue-next';

const props = defineProps({
  name: {
    type: String,
    required: true
  },
  size: {
    type: [Number, String],
    default: 24
  },
  color: {
    type: String,
    default: 'currentColor'
  },
  strokeWidth: {
    type: [Number, String],
    default: 2
  }
});

const Icon = computed(() => {
  // 将连字符名称转换为 PascalCase
  const pascalCase = props.name
    .split('-')
    .map(part => part.charAt(0).toUpperCase() + part.slice(1))
    .join('');
  
  return LucideIcons[pascalCase];
});
</script>

<template>
  <component 
    :is="Icon" 
    :size="size" 
    :color="color" 
    :stroke-width="strokeWidth"
    v-if="Icon"
  />
  <span v-else class="icon-not-found">图标未找到: {{ name }}</span>
</template>

<style scoped>
.icon-not-found {
  color: red;
  font-size: 0.8rem;
}
</style> 