<template>
  <div class="flex items-center justify-between p-2 bg-slate-100 dark:bg-slate-800">
    <div class="flex items-center gap-4">
      <label for="template-selector" class="text-sm font-medium text-slate-700 dark:text-slate-300">模板:</label>
      <select
          id="template-selector"
          v-model="selectedTemplate"
          @change="onTemplateChange"
          class="block w-48 px-3 py-1 text-sm bg-white border rounded-md shadow-sm border-slate-300 dark:bg-slate-700 dark:border-slate-600 dark:text-white focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
      >
        <option disabled value="">请选择一个模板</option>
        <option v-for="template in templates" :key="template.id" :value="template.id">
          {{ template.name }}
        </option>
      </select>
    </div>
    <button
        @click="$emit('generate')"
        class="inline-flex items-center px-6 py-2 text-sm font-semibold text-white transition-colors rounded-lg shadow-md bg-gradient-to-r from-sky-500 to-indigo-600 hover:from-sky-600 hover:to-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
    >
      <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
        <path d="M10 3.5a1.5 1.5 0 013 0V4a1 1 0 001 1h3a1 1 0 011 1v3a1 1 0 01-1 1h-.5a1.5 1.5 0 000 3h.5a1 1 0 011 1v3a1 1 0 01-1 1h-3a1 1 0 00-1 1v.5a1.5 1.5 0 01-3 0V16a1 1 0 00-1-1H6a1 1 0 01-1-1v-3a1 1 0 011-1h.5a1.5 1.5 0 000-3H6a1 1 0 01-1-1V5a1 1 0 011-1h3a1 1 0 001-1v-.5z" />
      </svg>
      生成教案
    </button>
  </div>
</template>

<script setup>
import { ref } from 'vue';

defineEmits(['generate', 'templateChange']);

const templates = ref([
  { id: 'standard', name: '标准教学教案' },
  { id: 'pbl', name: '项目式学习 (PBL)' },
  { id: 'flipped', name: '翻转课堂' },
]);

const selectedTemplate = ref('standard');

const onTemplateChange = () => {
  // In a real app, this would emit the whole template object or its content
  // emit('templateChange', selectedTemplate.value);
};
</script> 