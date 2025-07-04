<template>
  <div class="flex flex-col h-full">
    <!-- 工具栏占位，可后续插入模板选择/生成按钮 -->
    <header class="flex items-center justify-between px-4 py-2 border-b bg-slate-50 dark:bg-slate-700 sticky top-0 z-10">
      <slot name="toolbar" />
    </header>

    <!-- 虚拟滚动 Markdown 渲染 -->
    <div v-bind="containerProps" class="relative flex-1 overflow-y-auto my-4 rounded-2xl bg-white/95 dark:bg-slate-800/90 border border-slate-200 dark:border-slate-700 shadow-lg backdrop-blur">
      <div v-if="loading" class="absolute inset-0 flex items-center justify-center bg-white/60 dark:bg-slate-800/60 z-10">
        <el-icon class="is-loading text-3xl text-indigo-500"><Loading /></el-icon>
      </div>
      <div v-bind="wrapperProps" class="px-4 py-4">
        <div
          v-for="row in list"
          :key="row.index"
          v-html="renderHtml(row.data)"
          class="markdown-body"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onUnmounted } from 'vue';
import { useVirtualList } from '@vueuse/core';
import { marked } from 'marked';
import DOMPurify from 'dompurify';
import hljs from 'highlight.js';
import { bus } from '@/utils/eventBus';

const raw = ref('');
const loading = ref(false);
let typingTimer = null;

bus.on('md-ready', (md) => {
  if (!md) return;
  // 清空现有文本
  raw.value = '';
  const full = md;
  let idx = 0;

  clearInterval(typingTimer);
  typingTimer = setInterval(() => {
    raw.value += full[idx] || '';
    idx++;
    if (idx >= full.length) {
      clearInterval(typingTimer);
      typingTimer = null;
    }
  }, 6); // 速度可调整
});

bus.on('preview-loading', (v) => {
  loading.value = v;
});

onUnmounted(() => {
  clearInterval(typingTimer);
});

const paragraphs = computed(() => raw.value.split('\n\n'));

const { list, containerProps, wrapperProps } = useVirtualList(paragraphs, {
  itemHeight: 120,
  overscan: 10,
});

function renderHtml(text) {
  return DOMPurify.sanitize(marked.parse(text));
}

watch(list, () => {
  nextTick(() => {
    document.querySelectorAll('.markdown-body pre code').forEach(el => hljs.highlightElement(el));
  });
  bus.emit('outline-update', raw.value);
});
</script>

<style scoped>
.markdown-body::-webkit-scrollbar {
  width: 8px;
}
.markdown-body::-webkit-scrollbar-thumb {
  background-color: #a5b4fc; /* indigo-300 */
  border-radius: 4px;
}
.markdown-body {
  scrollbar-width: thin;
  scrollbar-color: #a5b4fc transparent;
}

/* --- Markdown Color Theme ---- */
.markdown-body h1,
.markdown-body h2 {
  color: #4f46e5; /* indigo-600 */
}

.markdown-body h3,
.markdown-body h4 {
  color: #6366f1; /* indigo-500 */
}

.markdown-body a {
  color: #3b82f6; /* blue-500 */
  text-decoration: underline;
}

.markdown-body blockquote {
  border-left: 4px solid #a78bfa; /* violet-400 */
  background: #f5f3ff; /* violet-50 */
  color: #6d28d9; /* violet-700 */
  padding-left: 1rem;
}

.markdown-body code {
  background: #fef2f2; /* rose-50 */
  color: #e11d48; /* rose-600 */
  padding: 2px 4px;
  border-radius: 4px;
  font-family: "JetBrains Mono", monospace;
  font-size: 0.875em;
}

.dark .markdown-body blockquote {
  background: rgba(88,28,135,0.2);
  border-color: #c4b5fd;
  color: #d8b4fe;
}

/* --- Lists ---- */
.markdown-body ul,
.markdown-body ol {
  margin-left: 1.5rem;
  padding-left: 0.5rem;
}

.markdown-body ul li {
  list-style-type: disc;
}

.markdown-body ol li {
  list-style-type: decimal;
}

/* --- Tables ---- */
.markdown-body table {
  width: 100%;
  border-collapse: collapse;
  margin: 1rem 0;
  font-size: 0.925rem;
}

.markdown-body thead th {
  background: #eef2ff; /* indigo-100 */
  font-weight: 600;
}

.markdown-body th,
.markdown-body td {
  padding: 0.5rem 0.75rem;
  border: 1px solid #e5e7eb; /* gray-200 */
}

.dark .markdown-body thead th {
  background: rgba(99,102,241,0.15);
  border-color: rgba(165,180,252,0.4);
}

.dark .markdown-body th,
.dark .markdown-body td {
  border-color: #374151;
}

/* --- Horizontal Rule --- */
.markdown-body hr {
  border: none;
  border-top: 1px solid #e5e7eb;
  margin: 1.5rem 0;
}

.dark .markdown-body hr {
  border-top-color: #4b5563;
}
</style> 