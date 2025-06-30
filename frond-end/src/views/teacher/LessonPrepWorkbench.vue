<template>
  <div class="flex flex-col h-full bg-slate-50 dark:bg-slate-900">
    <!-- Part of ModelStatusToast.vue -->
    <div class="fixed top-0 left-0 right-0 z-50 p-2 text-center text-white bg-green-600 shadow-lg" v-if="toastVisible">
      <p class="text-sm">
        <span class="font-bold">模型状态:</span> {{ modelName }} |
        <span class="font-bold">队列:</span> {{ queueLength }} |
        <span class="font-bold">GPU:</span> {{ gpuTemp }}°C
      </p>
    </div>

    <Splitpanes class="flex-1 default-theme" :push-other-panes="false" style="padding-top: 40px;">
      <!-- === Left Pane: OutlineImport === -->
      <Pane min-size="20" size="25" class="flex flex-col bg-white dark:bg-slate-800">
        <div class="flex flex-col h-full p-4 border-r border-slate-200 dark:border-slate-700">
          <h3 class="mb-4 text-lg font-semibold text-slate-800 dark:text-slate-200">教学大纲</h3>
          <div class="mb-4">
            <el-upload drag action="#" :http-request="() => {}" :before-upload="handleBeforeUpload" :show-file-list="false" class="w-full">
              <div class="p-4 text-center">
                <el-icon class="el-icon--upload"><upload-filled /></el-icon>
                <div class="el-upload__text">
                  拖拽文件到此处或 <em>点击上传</em>
                  <div class="el-upload__tip" style="margin-top: 8px;">支持 PDF, DOCX 文件</div>
                </div>
              </div>
            </el-upload>
          </div>
          <div v-if="isParsing" class="flex flex-col items-center justify-center p-4 mb-4 rounded-md bg-slate-100 dark:bg-slate-700">
            <el-progress type="circle" :percentage="parsingProgress" class="mb-2" />
            <p class="text-sm text-slate-600 dark:text-slate-300">正在解析大纲...</p>
          </div>
          <div class="flex-1 overflow-y-auto">
            <el-tree v-if="!isParsing && outlineData.length" :data="outlineData" :props="{ children: 'children', label: 'label' }" @node-click="handleNodeClick" default-expand-all highlight-current class="bg-transparent" />
            <div v-else-if="!isParsing && !outlineData.length" class="flex items-center justify-center h-full">
              <p class="text-slate-500">上传文件后，此处将显示大纲结构。</p>
            </div>
          </div>
        </div>
      </Pane>

      <!-- === Middle Pane: Editor === -->
      <Pane min-size="40">
        <div class="flex flex-col h-full bg-white dark:bg-gray-800">
          <!-- Part of Toolbar.vue -->
          <div class="flex items-center justify-between p-2 border-b bg-slate-100 dark:bg-slate-800 border-slate-200 dark:border-slate-700">
            <div class="flex items-center gap-4">
              <label for="template-selector" class="text-sm font-medium text-slate-700 dark:text-slate-300">模板:</label>
              <select v-model="selectedTemplate" class="block w-48 px-3 py-1 text-sm bg-white border rounded-md shadow-sm border-slate-300 dark:bg-slate-700 dark:border-slate-600 dark:text-white focus:outline-none focus:ring-indigo-500 focus:border-indigo-500">
                <option v-for="template in templates" :key="template.id" :value="template.id">{{ template.name }}</option>
              </select>
            </div>
            <button @click="handleGenerate" class="inline-flex items-center px-6 py-2 text-sm font-semibold text-white transition-colors rounded-lg shadow-md bg-gradient-to-r from-sky-500 to-indigo-600 hover:from-sky-600 hover:to-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500">
              <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5 mr-2" viewBox="0 0 20 20" fill="currentColor"><path d="M10 3.5a1.5 1.5 0 013 0V4a1 1 0 001 1h3a1 1 0 011 1v3a1 1 0 01-1 1h-.5a1.5 1.5 0 000 3h.5a1 1 0 011 1v3a1 1 0 01-1 1h-3a1 1 0 00-1 1v.5a1.5 1.5 0 01-3 0V16a1 1 0 00-1-1H6a1 1 0 01-1-1v-3a1 1 0 011-1h.5a1.5 1.5 0 000-3H6a1 1 0 01-1-1V5a1 1 0 011-1h3a1 1 0 001-1v-.5z" /></svg>
              生成教案
            </button>
          </div>
          <!-- Part of PromptEditor.vue -->
          <div ref="editorRef" class="relative flex-1 w-full h-full"></div>
        </div>
      </Pane>

      <!-- === Right Pane: StreamOutput === -->
      <Pane min-size="20" size="35" class="flex flex-col">
        <div class="flex flex-col h-full p-4 bg-white dark:bg-slate-800">
          <div class="flex items-center justify-between pb-2 mb-2 border-b border-slate-200 dark:border-slate-700">
            <h3 class="text-lg font-semibold text-slate-800 dark:text-slate-200">生成结果</h3>
            <div>
              <button @click="handleCopy" title="复制" class="p-1 text-slate-500 hover:text-indigo-600 dark:hover:text-indigo-400"><svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" /></svg></button>
              <button @click="handleStopStream" title="停止生成" class="p-1 text-slate-500 hover:text-red-600 dark:hover:text-red-400"><svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 10h6v6H9z" /></svg></button>
            </div>
          </div>
          <div ref="outputContainer" class="flex-1 overflow-y-auto prose max-w-none dark:prose-invert">
            <p v-if="!isStreaming && !streamingText">点击"生成教案"开始。</p>
            <div v-html="formattedText"></div>
            <div v-if="isStreaming" class="flex items-center"><span class="typing-cursor"></span><span class="ml-2 text-sm text-slate-500">正在输入...</span></div>
          </div>
        </div>
      </Pane>
    </Splitpanes>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue';
import { Splitpanes, Pane } from 'splitpanes';
import 'splitpanes/dist/splitpanes.css';
import { ElUpload, ElIcon, ElTree, ElProgress } from 'element-plus';
import { UploadFilled } from '@element-plus/icons-vue';
import * as monaco from 'monaco-editor';
import { marked } from 'marked';

// --- State from ModelStatusToast ---
const modelName = ref('gemma-7b-it');
const queueLength = ref(2);
const gpuTemp = ref(68);
const toastVisible = ref(true);

// --- State from OutlineImport ---
const isParsing = ref(false);
const parsingProgress = ref(0);
const outlineData = ref([]);
const prompt = ref(''); // Shared state, moved from main component

// --- State from Toolbar ---
const templates = ref([
  { id: 'standard', name: '标准教学教案' },
  { id: 'pbl', name: '项目式学习 (PBL)' },
  { id: 'flipped', name: '翻转课堂' },
]);
const selectedTemplate = ref('standard');

// --- State from PromptEditor ---
const editorRef = ref(null);
let editor = null;
const isDark = ref(document.documentElement.classList.contains('dark'));

// --- State from StreamOutput ---
const streamingText = ref('');
const isStreaming = ref(false);
let streamInterval = null;
const outputContainer = ref(null);
const formattedText = computed(() => marked(streamingText.value));

// --- Logic & Methods ---

onMounted(() => {
  // ModelStatusToast logic
  setTimeout(() => { toastVisible.value = false; }, 5000);

  // PromptEditor logic
  if (editorRef.value) {
    editor = monaco.editor.create(editorRef.value, {
      value: prompt.value,
      language: 'markdown',
      theme: isDark.value ? 'vs-dark' : 'vs',
      wordWrap: 'on',
      minimap: { enabled: false },
      automaticLayout: true,
      fontSize: 14,
      scrollBeyondLastLine: false,
      padding: { top: 16, bottom: 16 },
      lineNumbers: 'off',
    });
    editor.onDidChangeModelContent(() => {
      prompt.value = editor.getValue();
    });
  }
});

onUnmounted(() => {
  editor?.dispose();
  clearInterval(streamInterval);
});

// Method from PromptEditor to insert text
const insertInEditor = (text) => {
  if (!editor) return;
  const selection = editor.getSelection();
  const op = { range: selection, text: text, forceMoveMarkers: true };
  editor.executeEdits('insert-text', [op]);
  editor.focus();
};

// Method from OutlineImport
const handleBeforeUpload = (file) => {
  isParsing.value = true;
  parsingProgress.value = 0;
  const interval = setInterval(() => {
    parsingProgress.value += 10;
    if (parsingProgress.value >= 100) {
      clearInterval(interval);
      isParsing.value = false;
      outlineData.value = [
        { id: 1, label: '第一章：课程介绍', children: [{ id: 2, label: '1.1 什么是人工智能' }, { id: 3, label: '1.2 发展历史' }] },
        { id: 4, label: '第二章：搜索算法', children: [{ id: 5, label: '2.1 深度优先搜索' }, { id: 6, label: '2.2 广度优先搜索' }] },
      ];
    }
  }, 200);
  return false;
};

const handleNodeClick = (data) => {
  if (!data.children || data.children.length === 0) {
    insertInEditor(`\n### ${data.label}\n`);
  }
};

// Method from Toolbar/Main component
const handleGenerate = () => {
  startStream();
};

// Methods from StreamOutput
const scrollToBottom = () => {
  nextTick(() => {
    if (outputContainer.value) {
      outputContainer.value.scrollTop = outputContainer.value.scrollHeight;
    }
  });
};

const startStream = () => {
  isStreaming.value = true;
  streamingText.value = '';
  const fullResponse = `### 智能教学设计：第一章 课程介绍\n\n**教学目标:**\n1.  **知识与技能:** 学生能够清晰定义"人工智能"，并列举至少三个应用实例。\n2.  **过程与方法:** 通过小组讨论，培养学生归纳总结和口头表达能力。\n3.  **情感态度价值观:** 激发学生对前沿科技的兴趣，树立科学探索精神。\n\n**教学重点:** 人工智能的核心概念。\n**教学难点:** 理解"智能"的多维度含义。`;
  let i = 0;
  streamInterval = setInterval(() => {
    if (i < fullResponse.length) {
      streamingText.value += fullResponse[i];
      i++;
      scrollToBottom();
    } else {
      handleStopStream();
    }
  }, 20);
};

const handleStopStream = () => {
  isStreaming.value = false;
  clearInterval(streamInterval);
};

const handleCopy = () => {
  navigator.clipboard.writeText(streamingText.value);
};
</script>

<style>
/* --- General Styles for Splitpanes --- */
.splitpanes.default-theme .splitpanes__pane { background-color: transparent; }
.splitpanes.default-theme .splitpanes__splitter {
  background-color: #f1f5f9; /* bg-slate-100 */
  border-left: 1px solid #e2e8f0; /* border-slate-200 */
  position: relative;
  width: 8px;
  z-index: 10;
}
.dark .splitpanes.default-theme .splitpanes__splitter {
  background-color: #1e293b; /* dark:bg-slate-800 */
  border-left: 1px solid #334155; /* dark:border-slate-700 */
}
.splitpanes.default-theme .splitpanes__splitter:hover { border-left: 1px solid #4f46e5; }
.splitpanes.default-theme .splitpanes__splitter:before {
  content: '';
  position: absolute;
  left: 50%; top: 50%;
  transform: translate(-50%,-50%);
  width: 2px; height: 40px;
  background-color: #cbd5e1; /* slate-300 */
  border-radius: 5px;
  transition: background-color 0.2s;
}
.dark .splitpanes.default-theme .splitpanes__splitter:before { background-color: #475569; }
.splitpanes.default-theme .splitpanes__splitter:hover:before { background-color: #4f46e5; }

/* --- Styles from OutlineImport --- */
.el-tree { --el-tree-node-hover-bg-color: #f0f4ff; }
.dark .el-tree {
  --el-tree-text-color: #cbd5e1; /* slate-300 */
  --el-tree-node-hover-bg-color: #334155; /* slate-700 */
}
.dark .el-tree-node__content:hover { background-color: var(--el-tree-node-hover-bg-color); }
.dark .el-tree-node.is-current > .el-tree-node__content { background-color: #4f46e5; }
.el-upload-dragger { width: 100% !important; }

/* --- Styles from StreamOutput --- */
.prose :where(pre):not(:where([class~="not-prose"] *)) {
    background-color: #f3f4f6; color: #1f2937;
    border-radius: 0.375rem; padding: 1em;
}
.dark .prose :where(pre):not(:where([class~="not-prose"] *)) {
    background-color: #1f2937; color: #e5e7eb;
}
.typing-cursor {
  display: inline-block;
  width: 8px; height: 1.2em;
  background-color: #4f46e5;
  animation: blink 1s step-end infinite;
}
@keyframes blink {
  from, to { opacity: 1; }
  50% { opacity: 0; }
}
</style> 