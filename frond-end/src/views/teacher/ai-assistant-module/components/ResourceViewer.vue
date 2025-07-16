<script setup>
import { ref, watch, computed } from 'vue';
import MarkdownIt from 'markdown-it';

const props = defineProps({
  resource: {
    type: Object,
    required: true
  },
  isOpen: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['close', 'edit', 'print']);

// Markdown 渲染器
const md = new MarkdownIt({
  html: true,
  breaks: true,
  linkify: true,
  typographer: true
});

// 将 Markdown 转换为 HTML
const renderedContent = computed(() => {
  return props.resource ? md.render(props.resource.content || '') : '';
});

// 资源类型名称
const resourceTypeName = computed(() => {
  return props.resource.type === 'lesson' ? '教案' : '习题集';
});

// 关闭预览
const closeViewer = () => {
  emit('close');
};

// 编辑资源
const editResource = () => {
  emit('edit', props.resource);
};

// 打印资源
const printResource = () => {
  emit('print', props.resource);
  
  // 简单的打印功能
  const printWindow = window.open('', '_blank');
  printWindow.document.write(`
    <html>
      <head>
        <title>${props.resource.title}</title>
        <style>
          body {
            font-family: Arial, sans-serif;
            line-height: 1.6;
            color: #333;
            margin: 2cm;
          }
          h1, h2, h3, h4 {
            color: #2563eb;
          }
          pre, code {
            background-color: #f1f5f9;
            padding: 0.2em 0.4em;
            border-radius: 3px;
            font-family: monospace;
          }
          table {
            border-collapse: collapse;
            margin: 1em 0;
            width: 100%;
          }
          table, th, td {
            border: 1px solid #ddd;
          }
          th, td {
            padding: 8px 12px;
            text-align: left;
          }
          th {
            background-color: #f1f5f9;
          }
          .meta-info {
            color: #666;
            border-bottom: 1px solid #ddd;
            padding-bottom: 1em;
            margin-bottom: 2em;
          }
          @media print {
            body {
              margin: 1cm;
            }
          }
        </style>
      </head>
      <body>
        <div class="meta-info">
          <h1>${props.resource.title}</h1>
          <p>类型: ${resourceTypeName.value} | 创建日期: ${new Date(props.resource.createdAt).toLocaleDateString('zh-CN')}</p>
        </div>
        ${renderedContent.value}
      </body>
    </html>
  `);
  printWindow.document.close();
  printWindow.print();
};
</script>

<template>
  <div
    v-if="isOpen" 
    class="resource-viewer"
  >
    <div class="overlay" @click="closeViewer"></div>
    
    <div class="viewer-container">
      <div class="viewer-header">
        <div class="resource-info">
          <div 
            class="resource-type-badge"
            :class="resource.type === 'lesson' ? 'lesson-badge' : 'questions-badge'"
          >
            {{ resourceTypeName }}
          </div>
          <h2 class="resource-title">{{ resource.title }}</h2>
          <p class="resource-date">创建于 {{ new Date(resource.createdAt).toLocaleDateString('zh-CN') }}</p>
        </div>
        
        <div class="viewer-actions">
          <button 
            class="action-btn edit-btn"
            title="编辑"
            @click="editResource"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
            </svg>
            编辑
          </button>
          
          <button 
            class="action-btn print-btn"
            title="打印"
            @click="printResource"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 17h2a2 2 0 002-2v-4a2 2 0 00-2-2H5a2 2 0 00-2 2v4a2 2 0 002 2h2m2 4h6a2 2 0 002-2v-4a2 2 0 00-2-2H9a2 2 0 00-2 2v4a2 2 0 002 2zm8-12V5a2 2 0 00-2-2H9a2 2 0 00-2 2v4h10z" />
            </svg>
            打印
          </button>
          
          <button 
            class="close-btn"
            @click="closeViewer"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </div>
      
      <div class="viewer-content">
        <div class="markdown-content" v-html="renderedContent"></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.resource-viewer {
  @apply fixed inset-0 z-50 flex items-center justify-center;
}

.overlay {
  @apply absolute inset-0 bg-black bg-opacity-50;
}

.viewer-container {
  @apply relative bg-white rounded-xl shadow-2xl max-w-5xl w-full h-[90vh] flex flex-col overflow-hidden z-10;
}

.viewer-header {
  @apply flex justify-between items-start p-6 border-b border-gray-200;
}

.resource-info {
  @apply flex-1;
}

.resource-type-badge {
  @apply inline-block text-sm px-3 py-1 rounded-full font-medium mb-2;
}

.lesson-badge {
  @apply bg-green-100 text-green-700;
}

.questions-badge {
  @apply bg-purple-100 text-purple-700;
}

.resource-title {
  @apply text-2xl font-bold text-gray-800 mb-1;
}

.resource-date {
  @apply text-sm text-gray-500;
}

.viewer-actions {
  @apply flex items-center gap-2;
}

.action-btn {
  @apply flex items-center px-4 py-2 rounded-lg text-sm font-medium transition-colors;
}

.edit-btn {
  @apply text-amber-700 bg-amber-50 hover:bg-amber-100;
}

.print-btn {
  @apply text-blue-700 bg-blue-50 hover:bg-blue-100;
}

.close-btn {
  @apply p-1.5 rounded-full hover:bg-gray-100 text-gray-500 hover:text-gray-700 ml-2;
}

.action-btn svg {
  @apply mr-2;
}

.viewer-content {
  @apply flex-1 overflow-y-auto p-6;
}

.markdown-content {
  @apply prose prose-blue max-w-none;
}

.markdown-content :deep(h1) {
  @apply text-2xl font-bold text-gray-800 mt-6 mb-4;
}

.markdown-content :deep(h2) {
  @apply text-xl font-bold text-gray-800 mt-5 mb-3;
}

.markdown-content :deep(h3) {
  @apply text-lg font-bold text-gray-800 mt-4 mb-2;
}

.markdown-content :deep(p) {
  @apply my-3;
}

.markdown-content :deep(ul), .markdown-content :deep(ol) {
  @apply my-3 ml-5;
}

.markdown-content :deep(li) {
  @apply my-1;
}

.markdown-content :deep(blockquote) {
  @apply pl-4 border-l-4 border-gray-300 text-gray-700 my-4;
}

.markdown-content :deep(table) {
  @apply w-full border-collapse my-4;
}

.markdown-content :deep(th), .markdown-content :deep(td) {
  @apply border border-gray-300 p-2;
}

.markdown-content :deep(th) {
  @apply bg-gray-100;
}

.markdown-content :deep(code) {
  @apply px-1.5 py-0.5 bg-gray-100 rounded text-sm;
}

.markdown-content :deep(pre) {
  @apply p-4 bg-gray-100 rounded-lg overflow-x-auto my-4;
}

.markdown-content :deep(pre code) {
  @apply bg-transparent p-0;
}

@media (max-width: 640px) {
  .viewer-header {
    @apply flex-col gap-4;
  }
  
  .viewer-actions {
    @apply w-full justify-end;
  }
  
  .resource-title {
    @apply text-xl;
  }
}
</style> 