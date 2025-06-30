<template>
  <div class="flex h-screen bg-gray-50 font-sans">
    <!-- 左侧边栏：大纲导入与展示 -->
    <aside class="w-1/4 h-full bg-white border-r border-gray-200 flex flex-col">
      <div class="p-4 border-b border-gray-200">
        <h2 class="text-lg font-semibold text-gray-800">课程大纲</h2>
      </div>
      
      <div class="flex-grow p-4 overflow-y-auto">
        <!-- 上传组件 -->
        <div v-if="!outline" class="h-full flex flex-col items-center justify-center text-center">
          <div 
            class="w-full border-2 border-dashed border-gray-300 rounded-xl p-8 hover:border-blue-500 hover:bg-blue-50 transition-all cursor-pointer"
            @click="triggerFileInput"
            @dragover.prevent="isDragging = true"
            @dragleave.prevent="isDragging = false"
            @drop.prevent="handleFileDrop"
            :class="{ 'border-blue-500 bg-blue-50 scale-105': isDragging }"
          >
            <input ref="fileInput" type="file" class="hidden" @change="handleFileChange" accept=".pdf,.doc,.docx" />
            <div v-if="!isUploading">
              <div v-if="!file">
                <svg class="mx-auto h-12 w-12 text-gray-400" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M12 16.5V9.75m0 0l-3.75 3.75M12 9.75l3.75 3.75M3 17.25V6.75A2.25 2.25 0 015.25 4.5h13.5A2.25 2.25 0 0121 6.75v10.5A2.25 2.25 0 0118.75 19.5H5.25A2.25 2.25 0 013 17.25z" />
                </svg>
                <p class="mt-2 font-semibold text-blue-600">选择文件</p>
                <p class="text-sm text-gray-500">或拖拽到此处</p>
              </div>
              <div v-else class="text-sm">
                <svg xmlns="http://www.w3.org/2000/svg" class="mx-auto h-12 w-12 text-green-500" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <p class="mt-2 font-semibold text-gray-700">{{ file.name }}</p>
                <p class="text-xs text-gray-500">{{ formatFileSize(file.size) }}</p>
              </div>
            </div>
            <div v-if="isUploading" class="flex flex-col items-center justify-center">
              <svg class="animate-spin h-8 w-8 text-blue-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              <p class="mt-2 text-sm font-semibold text-gray-600">正在解析大纲...</p>
            </div>
          </div>
          <button v-if="file && !isUploading" @click.stop="uploadFile" class="mt-6 w-full bg-blue-600 text-white py-2.5 rounded-lg hover:bg-blue-700 transition-all shadow-md hover:shadow-lg focus:ring-4 focus:ring-blue-300">
            开始解析
          </button>
        </div>

        <!-- 大纲树状图 -->
        <div v-else>
          <div class="mb-4 p-3 bg-blue-50 border border-blue-200 rounded-lg">
            <h3 class="font-semibold text-base text-blue-800">{{ outline.courseInfo.name }}</h3>
            <p class="text-sm text-blue-600">{{ outline.courseInfo.subject }} - {{ outline.courseInfo.grade }}</p>
          </div>
          <ul class="space-y-1">
            <li v-for="item in outline.outline" :key="item.title">
              <div class="flex items-start p-2 rounded-lg hover:bg-gray-100">
                <input type="checkbox" :id="item.title" :value="item" v-model="selectedNodes" class="mt-1 mr-3 h-4 w-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500 cursor-pointer">
                <label :for="item.title" class="text-sm font-medium text-gray-800 cursor-pointer">{{ item.title }}</label>
              </div>
              <ul v-if="item.children && item.children.length" class="pl-8 mt-1 space-y-1 border-l-2 border-gray-200 ml-2">
                <li v-for="child in item.children" :key="child.title">
                   <div class="flex items-start p-1.5 rounded-md hover:bg-gray-100">
                    <input type="checkbox" :id="child.title" :value="child" v-model="selectedNodes" class="mt-1 mr-3 h-4 w-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500 cursor-pointer">
                    <label :for="child.title" class="text-sm text-gray-600 cursor-pointer">{{ child.title }}</label>
                  </div>
                </li>
              </ul>
            </li>
          </ul>
        </div>
      </div>
       <div class="p-4 border-t border-gray-200">
          <button v-if="outline" @click="resetForm" class="w-full bg-gray-200 text-gray-700 py-2 rounded-lg hover:bg-gray-300 transition text-sm">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 inline-block -mt-0.5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M4 4v5h5M20 20v-5h-5" /><path stroke-linecap="round" stroke-linejoin="round" d="M4 9a9 9 0 0114.65-5.24L20 5M4 15a9 9 0 0014.65 5.24L20 19" /></svg>
              更换大纲
          </button>
      </div>
    </aside>

    <!-- 中间主面板：提示词编辑器 -->
    <main class="w-1/2 h-full flex flex-col bg-gray-50">
        <div class="flex-none flex justify-between items-center p-4">
            <h2 class="text-lg font-semibold text-gray-800">Prompt 编辑器</h2>
            <div class="flex items-center space-x-3">
                <el-select v-model="selectedTemplate" placeholder="选择模板" size="default">
                    <el-option v-for="item in templateOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
                <button @click="startGeneration" :disabled="isGenerating || !outline" class="px-5 py-2.5 bg-gradient-to-r from-green-500 to-teal-500 text-white rounded-lg hover:from-green-600 hover:to-teal-600 disabled:from-gray-300 disabled:to-gray-400 disabled:cursor-not-allowed transition-all font-semibold flex items-center shadow-md hover:shadow-lg focus:ring-4 focus:ring-green-300">
                    <svg v-if="isGenerating" class="animate-spin -ml-1 mr-2 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    <svg v-else xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 -ml-1 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" /></svg>
                    {{ isGenerating ? '生成中...' : '开始生成' }}
                </button>
            </div>
        </div>
        <div class="flex-grow p-4 pt-0">
          <div ref="editorContainer" class="w-full h-full border rounded-xl shadow-sm"></div>
        </div>
    </main>

    <!-- 右侧边栏：生成结果 -->
    <aside class="w-1/4 h-full bg-white border-l border-gray-200 flex flex-col">
        <div class="flex-none flex justify-between items-center p-4 border-b border-gray-200">
            <h2 class="text-lg font-semibold text-gray-800">生成结果</h2>
            <div class="flex space-x-3">
                <button @click="copyResult" class="text-gray-500 hover:text-blue-600 transition-colors" title="复制Markdown">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" /></svg>
                </button>
                <button @click="exportResult" class="text-gray-500 hover:text-green-600 transition-colors" title="导出为文件">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" /></svg>
                </button>
            </div>
        </div>
        
        <div class="flex-grow overflow-y-auto">
            <div v-if="isGenerating || generatedContent" ref="resultContainer" class="prose prose-sm max-w-none p-4" v-html="renderedMarkdown"></div>
            <!-- Empty State -->
            <div v-else class="h-full flex flex-col items-center justify-center text-center text-gray-500 p-4">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 text-gray-300" fill="none" viewBox="0 0 24 24" stroke-width="1" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M9.813 15.904L9 18.75l-.813-2.846a4.5 4.5 0 00-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 003.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 003.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 00-3.09 3.09zM18.259 8.715L18 9.75l-.259-1.035a3.375 3.375 0 00-2.455-2.456L14.25 6l1.036-.259a3.375 3.375 0 002.455-2.456L18 2.25l.259 1.035a3.375 3.375 0 002.456 2.456L21.75 6l-1.035.259a3.375 3.375 0 00-2.456 2.456zM16.898 20.572L16.5 21.75l-.398-1.178a3.375 3.375 0 00-2.456-2.456L12.5 18l1.178-.398a3.375 3.375 0 002.456-2.456L16.5 14.25l.398 1.178a3.375 3.375 0 002.456 2.456L20.5 18l-1.178.398a3.375 3.375 0 00-2.424 2.424z" /></svg>
                <h3 class="mt-4 text-sm font-semibold text-gray-800">准备就绪</h3>
                <p class="mt-1 text-xs text-gray-500">在左侧选择大纲节点，编辑Prompt后<br>点击"开始生成"即可在此处预览结果。</p>
            </div>
             <!-- Skeleton Loader -->
            <div v-if="isGenerating && !generatedContent" class="p-4 space-y-4">
                <div class="animate-pulse flex space-x-4">
                    <div class="flex-1 space-y-3 py-1">
                        <div class="h-2 bg-slate-200 rounded"></div>
                        <div class="space-y-2">
                            <div class="grid grid-cols-3 gap-4">
                                <div class="h-2 bg-slate-200 rounded col-span-2"></div>
                                <div class="h-2 bg-slate-200 rounded col-span-1"></div>
                            </div>
                            <div class="h-2 bg-slate-200 rounded"></div>
                        </div>
                    </div>
                </div>
                <div class="h-2 bg-slate-200 rounded"></div>
                <div class="h-2 bg-slate-200 rounded w-5/6"></div>
                <div class="h-2 bg-slate-200 rounded w-3/4"></div>
            </div>
        </div>
    </aside>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, nextTick } from 'vue';
import { ElMessage, ElSelect, ElOption } from 'element-plus';
import * as monaco from 'monaco-editor';
import { marked } from 'marked';
import DOMPurify from 'dompurify';

// Monaco Editor
const editorContainer = ref(null);
let editorInstance = null;
const editorContent = ref('');

// Result Display
const resultContainer = ref(null);
const generatedContent = ref('');
const renderedMarkdown = computed(() => {
    if (isGenerating.value && !generatedContent.value) return ''; // Don't render anything if loading skeleton is shown
    const rawHtml = marked.parse(generatedContent.value);
    return DOMPurify.sanitize(rawHtml);
});

// File Upload
const fileInput = ref(null);
const file = ref(null);
const isDragging = ref(false);
const isUploading = ref(false);

// Outline Data
const outline = ref(null);
const selectedNodes = ref([]);

// Generation State
const isGenerating = ref(false);

// Templates
const templateOptions = ref([
  { label: '标准教案模板', value: 'standard' },
  { label: '项目式学习模板', value: 'pbl' },
  { label: '翻转课堂模板', value: 'flipped' },
]);
const selectedTemplate = ref('standard');

const standardTemplate = `---
title: "课程教案：{{courseName}}"
subject: "{{subject}}"
grade: "{{grade}}"
duration: "90分钟"
---

# 1. 教学目标
- 知识与技能：
- 过程与方法：
- 情感态度与价值观：

# 2. 教学重难点
- 重点：
- 难点：

# 3. 教学准备
- 教师准备：
- 学生准备：

# 4. 教学过程
{{nodes}}

# 5. 板书设计

# 6. 课后反思
`;

onMounted(() => {
  if (editorContainer.value) {
    editorInstance = monaco.editor.create(editorContainer.value, {
      value: "请先从左侧上传并解析课程大纲。",
      language: 'markdown',
      theme: 'vs',
      automaticLayout: true,
      wordWrap: 'on',
      fontSize: 14,
      lineNumbers: 'off',
      glyphMargin: false,
      folding: false,
      lineDecorationsWidth: 0,
      lineNumbersMinChars: 0,
      minimap: { enabled: false },
      padding: { top: 15, bottom: 15 },
      scrollBeyondLastLine: false,
    });
    editorInstance.onDidChangeModelContent(() => {
      editorContent.value = editorInstance.getValue();
    });
  }
});

watch([selectedNodes, selectedTemplate], () => {
    updateEditorWithTemplate();
}, { deep: true });

function updateEditorWithTemplate() {
    if (!outline.value) return;

    let template = standardTemplate; // Add logic for more templates later
    
    let finalContent = template
        .replace('{{courseName}}', outline.value.courseInfo.name || '未命名课程')
        .replace('{{subject}}', outline.value.courseInfo.subject || '待定')
        .replace('{{grade}}', outline.value.courseInfo.grade || '待定');
    
    const nodesContent = selectedNodes.value.length > 0 
        ? selectedNodes.value.map(node => `## ${node.title}\n\n### 教学活动\n\n- \n\n### 设计意图\n\n- \n`).join('\n')
        : '<!-- 请从左侧选择大纲节点以填充教学过程 -->';
    
    finalContent = finalContent.replace('{{nodes}}', nodesContent);

    if (editorInstance) {
        editorInstance.setValue(finalContent);
    }
}


function triggerFileInput() {
  fileInput.value.click();
}

function handleFileChange(event) {
  const files = event.target.files;
  if (files.length > 0) {
    file.value = files[0];
  }
}

function handleFileDrop(event) {
  isDragging.value = false;
  const files = event.dataTransfer.files;
  if (files.length > 0) {
    file.value = files[0];
  }
}

function formatFileSize(bytes) {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

function resetForm() {
    file.value = null;
    outline.value = null;
    selectedNodes.value = [];
    generatedContent.value = '';
    if (editorInstance) editorInstance.setValue('请先从左侧上传并解析课程大纲。');
}

async function uploadFile() {
  if (!file.value) return;
  isUploading.value = true;
  await new Promise(resolve => setTimeout(resolve, 1500));

  outline.value = {
    courseInfo: {
      name: '高级编程技术',
      grade: '大学二年级',
      totalHours: 64,
      subject: '计算机科学',
    },
    outline: [
      {
        title: '第一单元：算法基础',
        children: [{ title: '1.1 复杂度分析' }, { title: '1.2 递归与分治' }],
      },
      {
        title: '第二单元：数据结构',
        children: [{ title: '2.1 树与二叉树' }, { title: '2.2 图的应用' }],
      },
    ],
  };
  
  isUploading.value = false;
  ElMessage.success('大纲解析成功！');
  if (outline.value) {
      selectedNodes.value = outline.value.outline.flatMap(o => [o, ...(o.children || [])]);
  }
}

async function startGeneration() {
    if (isGenerating.value || !outline.value || selectedNodes.value.length === 0) {
        if(!outline.value) ElMessage.warning('请先导入课程大纲');
        else if(selectedNodes.value.length === 0) ElMessage.warning('请至少选择一个大纲节点');
        return;
    }

    isGenerating.value = true;
    generatedContent.value = '';
    
    // Mock streaming generation
    const fullText = `# ${outline.value.courseInfo.name} 教案\n\n${editorContent.value}`;
    const words = fullText.split(/(\s+)/);
    
    await new Promise(resolve => setTimeout(resolve, 500)); // Initial delay for skeleton

    for (let i = 0; i < words.length; i++) {
        await new Promise(resolve => setTimeout(resolve, 30));
        generatedContent.value += words[i];
        await nextTick();
        if(resultContainer.value) resultContainer.value.scrollTop = resultContainer.value.scrollHeight;
    }

    isGenerating.value = false;
    ElMessage.success('教案生成完毕！');
}

function copyResult() {
    if(!generatedContent.value) return ElMessage.info('没有内容可以复制');
    navigator.clipboard.writeText(generatedContent.value);
    ElMessage.success('已复制到剪贴板！');
}

function exportResult() {
    if(!generatedContent.value) return ElMessage.info('没有内容可以导出');
    const blob = new Blob([generatedContent.value], { type: 'text/markdown;charset=utf-8' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `${outline.value.courseInfo.name || '教案'}.md`;
    link.click();
    URL.revokeObjectURL(link.href);
    ElMessage.success('Markdown文件已开始下载');
}
</script>

<style>
.prose h1, .prose h2, .prose h3 {
  font-weight: 600;
}
.prose pre {
    background-color: #f3f4f6; /* bg-gray-100 */
    color: #1f2937; /* text-gray-800 */
    padding: 1em;
    border-radius: 0.5rem; /* rounded-lg */
    overflow-x: auto;
}
.prose code {
    background-color: #e5e7eb; /* bg-gray-200 */
    padding: 0.2em 0.4em;
    margin: 0;
    font-size: 85%;
    border-radius: 0.25rem; /* rounded-md */
}
.prose blockquote {
  border-left-color: #3b82f6; /* border-blue-500 */
}
</style> 