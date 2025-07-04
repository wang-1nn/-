<template>
  <div class="flex flex-col h-full p-4 bg-white/95 dark:bg-slate-800/90 rounded-2xl shadow-lg border border-indigo-200 dark:border-indigo-700">
    <!-- 选择模板 -->
    <el-select v-model="selectedId" class="mb-3" size="small" @change="apply">
      <el-option v-for="tpl in presets" :key="tpl.id" :label="tpl.name" :value="tpl.id" />
    </el-select>

    <!-- 可编辑 Markdown -->
    <el-input v-model="mdText" type="textarea" :rows="20" class="flex-1" />
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { marked } from 'marked';
import DOMPurify from 'dompurify';
import hljs from 'highlight.js';
import { bus } from '@/utils/eventBus';

const presets = [
  { id: 'standard', name: '标准教学教案', md: '# 标准教学教案\n\n- 章节一\n- 章节二' },
  { id: 'pbl', name: '项目式学习', md: '# PBL 模板\n\n> 项目背景\n\n- 任务一\n- 任务二' },
];

const selectedId = ref(presets[0].id);
const mdText = ref(presets[0].md);
const html = ref('');

function render() {
  html.value = DOMPurify.sanitize(marked.parse(mdText.value));
  // 代码高亮
  setTimeout(() => {
    document.querySelectorAll('.markdown-body pre code').forEach(el => hljs.highlightElement(el));
  });
  bus.emit('template-update', mdText.value);
}

function apply() {
  const tpl = presets.find(t => t.id === selectedId.value);
  if (tpl) mdText.value = tpl.md;
  render();
}

render();

// 监听 textarea input
watch(mdText, (v)=>{
  bus.emit('template-update', v);
});
</script> 