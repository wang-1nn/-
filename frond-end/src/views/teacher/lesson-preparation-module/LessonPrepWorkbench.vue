
<template>
  <div class="flex flex-col h-full bg-slate-50">

    <!-- 模型状态提示条 - 添加平滑过渡动画 -->
    <div
        class="fixed top-0 left-0 right-0 z-50 p-2 text-center text-white bg-gradient-to-r from-blue-600 to-indigo-600 shadow-lg transition-all duration-500 transform"
        :class="toastVisible ? 'translate-y-0 opacity-100' : '-translate-y-full opacity-0'"
    >
      <p class="text-sm flex items-center justify-center gap-3">
        <span class="flex items-center font-medium"><el-icon><Monitor /></el-icon><span class="ml-1">模型: <span class="font-bold ml-1">{{ modelName }}</span></span></span>
        <span class="w-px h-4 bg-white/30"></span>
        <span class="flex items-center font-medium"><el-icon><Sort /></el-icon><span class="ml-1">队列: <span class="font-bold ml-1">{{ queueLength }}</span></span></span>
        <span class="w-px h-4 bg-white/30"></span>
        <span class="flex items-center font-medium"><el-icon><Cpu /></el-icon><span class="ml-1">GPU: <span class="font-bold ml-1">{{ gpuTemp }}°C</span></span></span>
      </p>
    </div>

    <!-- ======= Lesson Prep Wizard Steps ======= -->
    <el-steps :active="currentStep" align-center finish-status="success" class="mb-2 animate-in">
      <el-step title="导入大纲" :icon="UploadFilled"/>
      <el-step title="结构编辑" :icon="Document"/>
      <el-step title="结果预览" :icon="Reading"/>
    </el-steps>
    <!-- 滚动进度条（生成结果区域） -->
    <el-progress v-if="currentStep >= 1" :percentage="scrollProgress" :show-text="false" :stroke-width="4" class="mb-2 w-full" color="#6366f1"/>

    <!-- 新三栏布局 -->
    <div class="flex h-full gap-6 px-8 py-6 w-full">
      <!-- 左侧模板编辑区域 - 添加hover效果 -->
      <div
          class="template-panel transition-all duration-300 ease-in-out"
          :class="{'template-panel-expanded': isTemplatePanelHovered}"
          @mouseenter="isTemplatePanelHovered = true"
          @mouseleave="isTemplatePanelHovered = false"
      >
        <TemplatePane class="h-full" />
      </div>

      <!-- 中间渲染区域占主要空间 - 添加响应式压缩 -->
      <div class="preview-panel transition-all duration-300 ease-in-out">
        <PreviewPane class="h-full" >
          <template #toolbar>
            <div class="flex items-center gap-4 w-full justify-between">
              <div class="flex items-center gap-3">
                <!-- 上传按钮组 -->
                <div class="flex items-center gap-3">
                  <el-dropdown trigger="click" @command="handleImportCommand">
                    <el-button size="small" type="primary" plain>
                      <el-icon class="mr-1"><UploadFilled /></el-icon>导入
                      <el-icon class="ml-1"><ArrowDown /></el-icon>
                    </el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="upload-outline">上传教学大纲</el-dropdown-item>
                        <el-dropdown-item command="import-template">导入教案模板</el-dropdown-item>
                        <el-dropdown-item command="from-textbook">从教材自动提取</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                  <input ref="fileInput" type="file" class="hidden" accept=".pdf,.doc,.docx,.txt" @change="onFileChange" />
                </div>
              </div>

              <div class="flex items-center gap-3">
                <el-button
                    v-motion="btnMotion"
                    type="primary"
                    size="small"
                    @click="handleGenerate"
                >
                  <el-icon class="mr-1"><MagicStick /></el-icon>生成教案
                </el-button>
                <el-button
                    size="small"
                    :disabled="!contentState.currentContent"
                    @click="openEditor"
                >
                  <el-icon class="mr-1"><DocumentCopy /></el-icon>编辑内容
                </el-button>
                <el-button
                    size="small"
                    :disabled="!contentState.currentContent"
                    @click="exportToPDF"
                    type="success"
                    plain
                >
                  <el-icon class="mr-1"><Download /></el-icon>导出PDF
                </el-button>
              </div>
            </div>
          </template>
        </PreviewPane>
      </div>

      <!-- 右侧资源库区域 -->
      <div class="resource-panel transition-all duration-300 ease-in-out">
        <div class="bg-white p-4 rounded-xl shadow-sm h-full">
          <h3 class="text-lg font-medium text-indigo-700 mb-4 flex items-center">
            <el-icon class="mr-2"><Collection /></el-icon>
            教学资源库
          </h3>

          <!-- 搜索框 -->
          <el-input
              v-model="resourceSearch"
              placeholder="搜索资源"
              prefix-icon="Search"
              class="mb-4"
              clearable
          />

          <!-- 资源分类标签 -->
          <div class="flex flex-wrap gap-2 mb-4">
            <el-tag
                v-for="tag in resourceTags"
                :key="tag.id"
                :type="selectedResourceTag === tag.id ? '' : 'info'"
                effect="light"
                class="cursor-pointer"
                @click="selectedResourceTag = tag.id"
            >
              {{ tag.name }}
            </el-tag>
          </div>

          <!-- 资源列表 -->
          <div class="resource-list space-y-3 overflow-y-auto" style="max-height: 500px;">
            <div v-if="resourceLoading" class="text-center py-8 text-slate-400">
              <el-icon class="is-loading text-3xl mb-2"></el-icon>
              <p>加载中...</p>
            </div>
            <div v-else-if="resources.length > 0">
              <div v-for="resource in resources" :key="resource.id"
                   @click="handleResourceClick(resource)"
                   class="p-3 border border-slate-200 rounded-lg hover:bg-indigo-50 transition cursor-pointer hover:shadow-md hover:border-indigo-200">
                <div class="flex items-center justify-between">
                  <span class="text-sm font-medium text-slate-800">{{ resource.name }}</span>
                  <el-tag size="small" effect="plain">{{ resource.type }}</el-tag>
                </div>
                <p class="text-xs text-slate-500 mt-1">{{ resource.description }}</p>
              </div>
            </div>

            <div v-else class="text-center py-8 text-slate-400">
              <el-icon class="text-3xl mb-2"><DocumentDelete /></el-icon>
              <p>没有找到相关资源</p>
            </div>
          </div>

          <!-- 资源操作按钮 -->
          <div class="mt-4 flex justify-between">
            <el-button size="small" plain @click="fetchResources">
              <el-icon class="mr-1"><Refresh /></el-icon>刷新
            </el-button>
            <el-button size="small" type="primary" @click="showUploadResourceDialog = true">
              <el-icon class="mr-1"><Upload /></el-icon>上传资源
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- ======= 教案预览 Drawer ======= -->
    <el-drawer v-model="showPreviewDrawer" :with-header="false" size="40%" direction="rtl" @opened="() => { currentStep = 2 }" @closed="() => { currentStep = 1 }">
      <div class="p-6 h-full overflow-y-auto prose">
        <h2 class="mb-4">教案预览</h2>
        <div v-if="!contentState.currentContent" class="text-slate-500 text-center flex items-center justify-center h-full">
          <el-icon class="mr-1"><Document /></el-icon>暂无内容，请先导入大纲或生成教案。
        </div>
        <div v-else>
          <!-- 内容类型指示器 -->
          <div class="mb-4 p-3 bg-blue-50 border-l-4 border-blue-400 rounded">
            <div class="flex items-center">
              <el-icon class="mr-2 text-blue-600">
                <Document v-if="contentState.contentType === 'outline'" />
                <DocumentCopy v-else />
              </el-icon>
              <span class="text-blue-800 font-medium">
                {{ contentState.contentType === 'outline' ? '优化后的教学大纲' : '生成的教案内容' }}
              </span>
            </div>
          </div>
          <!-- 实际内容 -->
          <div v-dompurify-html="contentState.currentContent"></div>
        </div>
      </div>
    </el-drawer>

    <!-- ===== 教案编辑弹窗 ===== -->
    <LessonPlanEditor v-model:visible="showEditor" v-model="editorContent" @save="handleSaveLesson" />

    <!-- 上传资源对话框 -->
    <el-dialog
        v-model="showUploadResourceDialog"
        title="上传教学资源"
        width="500px"
    >
      <el-form label-position="top">
        <el-form-item label="资源名称">
          <el-input v-model="newResource.name" placeholder="输入资源名称" />
        </el-form-item>
        <el-form-item label="资源类型">
          <el-select v-model="newResource.type" placeholder="选择资源类型" class="w-full">
            <el-option
                v-for="tag in resourceTags"
                :key="tag.id"
                :label="tag.name"
                :value="tag.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="资源描述">
          <el-input
              v-model="newResource.description"
              type="textarea"
              placeholder="输入资源描述"
              :rows="3"
          />
        </el-form-item>
        <el-form-item label="上传文件">
          <el-upload
              class="upload-demo w-full"
              action="#"
              :auto-upload="false"
              :limit="1"
          >
            <template #trigger>
              <el-button type="primary" plain>选择文件</el-button>
            </template>
            <template #tip>
              <div class="text-xs text-slate-500 mt-2">支持PPT、Word、PDF、图片等格式，单个文件不超过20MB</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showUploadResourceDialog = false">取消</el-button>
          <el-button type="primary" @click="uploadResource">上传</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed, nextTick, watch } from 'vue';
import { Splitpanes, Pane } from 'splitpanes';
import 'splitpanes/dist/splitpanes.css';
import { ElUpload, ElIcon, ElProgress, ElButton, ElSelect, ElOption, ElTooltip, ElSkeleton, ElLoading, ElSteps, ElStep, ElDrawer, ElDescriptions, ElDescriptionsItem, ElSwitch, ElDropdown, ElDropdownMenu, ElDropdownItem, ElTag, ElInput, ElDialog, ElForm, ElFormItem } from 'element-plus';
import {
  UploadFilled,
  Reading,
  Document,
  DocumentCopy,
  Files,
  MagicStick,
  Loading,
  CloseBold,
  ChatDotRound,
  Monitor,
  Sort,
  Cpu,
  ArrowDown,
  Collection,
  Search,
  DocumentDelete,
  Refresh,
  Upload,
  Download
} from '@element-plus/icons-vue';
import * as monaco from 'monaco-editor';
import { marked } from 'marked';
import DOMPurify from 'dompurify';
import hljs from 'highlight.js';
import { useResizeObserver } from '@vueuse/core';
import gsap from 'gsap';
import ScrollTrigger from 'gsap/ScrollTrigger';
gsap.registerPlugin(ScrollTrigger);
import { templates as snippetTemplates } from '@/utils/templates.js';
import { bus } from '@/utils/eventBus.js';
import TemplatePane from '@/components/lesson-prep/TemplatePane.vue';
import PreviewPane from '@/components/lesson-prep/PreviewPane.vue';
import { useAuthStore } from '@/stores/counter.js';
import LessonPlanEditor from '@/components/lesson-prep/LessonPlanEditor.vue';
import { ElMessage } from 'element-plus';
import { useDebounceFn } from '@vueuse/core';

// 获取用户信息
const user = useAuthStore().user || { id: 'default-user' }; // 添加默认值，防止user为undefined

// API配置常量
const API_CONFIG = {
  timeout: 30000,  // 30秒超时
  maxRetries: 3,
  retryDelay: 1000
};

// 统一的错误处理函数
const handleApiError = (error, operation) => {
  console.error(`${operation}出错:`, error);
  ElMessage.error(`${operation}失败，请稍后重试`);
  isParsing.value = false;
  bus.emit('preview-loading', false);
};

// 显示进度指示器
const showProgressIndicator = (operation) => {
  ElMessage({
    message: `正在${operation}，请稍候...`,
    type: 'info',
    duration: 0,  // 不自动关闭
    showClose: true
  });
};

// 当前请求控制器
let currentOptimizeController = null;
let currentLessonController = null;

// 带重试机制的fetch包装函数
const fetchWithRetry = async (url, options, retries = API_CONFIG.maxRetries) => {
  try {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), API_CONFIG.timeout);

    // 确保使用正确的baseURL - 直接使用配置的后端地址
    const fullUrl = url.startsWith('http') ? url : `http://localhost:8080${url}`;

    const response = await fetch(fullUrl, {
      ...options,
      signal: controller.signal,
      credentials: 'include' // 等同于 withCredentials: true
    });

    clearTimeout(timeoutId);

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return response;
  } catch (error) {
    if (retries > 0 && error.name !== 'AbortError') {
      await new Promise(resolve => setTimeout(resolve, API_CONFIG.retryDelay));
      return fetchWithRetry(url, options, retries - 1);
    }
    throw error;
  }
};

// 改进的SSE数据处理函数
const processSSEBuffer = (sseBuffer, onData) => {
  let buffer = sseBuffer;
  let sep;

  while ((sep = buffer.indexOf('\n\n')) !== -1) {
    const eventBlock = buffer.slice(0, sep);
    buffer = buffer.slice(sep + 2);

    if (eventBlock.trim()) {
      const dataLines = eventBlock
          .split(/\n/)
          .filter(l => l.startsWith('data:'))
          .map(l => l.slice(5));

      const dataStr = dataLines.join('\n');
      if (dataStr && dataStr !== '[DONE]') {
        const chunk = preprocessMdChunk(dataStr);
        if (chunk) {
          onData(chunk);
        }
      }
    }
  }

  return buffer;
};

// --- 模板面板悬停状态 ---
const isTemplatePanelHovered = ref(false);

// --- State from ModelStatusToast ---
const modelName = ref('gemma-7b-it');
const queueLength = ref(2);
const gpuTemp = ref(68);
const toastVisible = ref(true);
// === Scroll progress ===
const scrollProgress = ref(0);

// ======= Motion 动画配置 =======
const btnMotion = {
  initial: { scale: 1 },
  hover: { scale: 1.05 },
  tap: { scale: 0.95 },
};

// --- State from OutlineImport ---
const isParsing = ref(false);
const parsingProgress = ref(0);
const outlineData = ref([]);
const selectedMeta = ref(null);
const prompt = ref(''); // Shared state, moved from main component

// --- State from Toolbar ---
const templates = ref([
  { id: 'standard', name: '标准教学教案' },
  { id: 'pbl', name: '项目式学习 (PBL)' },
  { id: 'flipped', name: '翻转课堂' },
  { id: 'gamified', name: '游戏化教学' },
  { id: 'inquiry', name: '探究式教学' },
]);
const selectedTemplate = ref('standard');

// --- State from PromptEditor ---
const editorRef = ref(null);
let editor = null;

// --- State from StreamOutput ---
const outputContainer = ref(null);
const htmlOutput = computed(() => DOMPurify.sanitize(marked(prompt.value, { gfm: true })));

// 根据容器大小实时调整 Monaco 编辑器
useResizeObserver(editorRef, () => {
  editor?.layout();
});

// ======= Wizard State =======
const currentStep = ref(0); // 0: 导入, 1: 编辑, 2: 预览
const showPreviewDrawer = ref(false);

// 删除未使用的 openPreview 函数

// ===== 教案内容状态管理 =====
const showEditor = ref(false);
const editorContent = ref('');

// 统一的内容状态管理
const contentState = reactive({
  // 原始大纲内容
  originalOutline: '',
  // 优化后的大纲内容
  optimizedOutline: '',
  // 最终生成的教案内容
  finalLesson: '',
  // 当前显示的内容（用于预览区域）
  currentContent: '',
  // 内容类型标识
  contentType: 'empty' // 'empty' | 'outline' | 'lesson'
});

// 监听流式内容更新 - 优化大纲
bus.on('outline-ready', (content) => {
  contentState.optimizedOutline = content || '';
  contentState.currentContent = content || '';
  contentState.contentType = 'outline';
});

bus.on('outline-chunk', (chunk) => {
  contentState.optimizedOutline += chunk || '';
  contentState.currentContent = contentState.optimizedOutline;
});

// 监听流式内容更新 - 生成教案
bus.on('lesson-ready', (content) => {
  contentState.finalLesson = content || '';
  contentState.currentContent = content || '';
  contentState.contentType = 'lesson';
});

bus.on('lesson-chunk', (chunk) => {
  contentState.finalLesson += chunk || '';
  contentState.currentContent = contentState.finalLesson;
});

// 监听插入文本事件
bus.on('insert-text', (text) => {
  // 这是一个占位逻辑，实际的编辑器插入需要在 TemplatePane.vue 中监听并实现
  console.log('请求插入文本:', text);
  // 理想情况下，TemplatePane.vue 中的 Monaco 编辑器会监听此事件
  // bus.emit('execute-insert', text);
});

// ===== 编辑器和导出功能 =====
function openEditor() {
  editorContent.value = contentState.currentContent;
  showEditor.value = true;
}

function handleSaveLesson(content) {
  // 更新内容状态
  if (contentState.contentType === 'lesson') {
    contentState.finalLesson = content;
  } else {
    contentState.optimizedOutline = content;
  }
  contentState.currentContent = content;

  // 保存到本地文件
  const blob = new Blob([content], { type: 'text/html;charset=utf-8' });
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = `lesson-plan-${Date.now()}.html`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);

  ElMessage.success('教案已保存');
}

// ===== PDF导出功能 =====
const exportToPDF = async () => {
  if (!contentState.currentContent) {
    ElMessage.warning('没有内容可以导出');
    return;
  }

  try {
    // 动态导入html2pdf库
    const html2pdf = (await import('html2pdf.js')).default;

    // 创建临时容器
    const tempDiv = document.createElement('div');
    tempDiv.innerHTML = contentState.currentContent;
    tempDiv.style.padding = '20px';
    tempDiv.style.fontFamily = 'Arial, sans-serif';
    tempDiv.style.lineHeight = '1.6';
    tempDiv.style.color = '#333';

    // PDF配置
    const options = {
      margin: 1,
      filename: `教案-${new Date().toLocaleDateString()}.pdf`,
      image: { type: 'jpeg', quality: 0.98 },
      html2canvas: { scale: 2 },
      jsPDF: { unit: 'in', format: 'a4', orientation: 'portrait' }
    };

    // 生成PDF
    await html2pdf().set(options).from(tempDiv).save();
    ElMessage.success('PDF导出成功');

  } catch (error) {
    console.error('PDF导出失败:', error);
    ElMessage.error('PDF导出失败，请稍后重试');
  }
};

// 监听 bus 触发打开编辑器事件
bus.on('open-editor', (full) => {
  editorContent.value = full;
  showEditor.value = true;
});

// ===== 资源库逻辑重构 =====
const resourceSearch = ref('');
const selectedResourceTag = ref('all');
const showUploadResourceDialog = ref(false);
const resources = ref([]); // 初始为空，将从API获取
const resourceLoading = ref(false);

const newResource = ref({
  name: '',
  type: 'document',
  description: ''
});

// 资源标签 (保持不变)
const resourceTags = ref([
  { id: 'all', name: '全部' },
  { id: 'document', name: '文档' },
  { id: 'image', name: '图片' },
  { id: 'video', name: '视频' },
  { id: 'audio', name: '音频' },
  { id: 'ppt', name: '演示文稿' },
  { id: 'exercise', name: '习题' }
]);

// 模拟的后端数据源
const allResources = [
  { id: 1, name: '教学大纲模板.docx', type: 'document', description: '标准教学大纲文档模板...', url: '/resources/1/教学大纲模板.docx' },
  { id: 2, name: '课程PPT模板.pptx', type: 'ppt', description: '精美课程PPT模板...', url: '/resources/2/课程PPT模板.pptx' },
  { id: 3, name: '知识点图解.png', type: 'image', description: '核心知识点图解...', url: '/resources/3/知识点图解.png' },
  { id: 4, name: '教学视频示例.mp4', type: 'video', description: '示范课教学视频...', url: '/resources/4/教学视频示例.mp4' },
  { id: 5, name: '课堂测验题库.zip', type: 'exercise', description: '包含多种题型...', url: '/resources/5/课堂测验题库.zip' },
  { id: 6, name: '函数思想音频讲解.mp3', type: 'audio', description: '专家讲解函数核心思想', url: '/resources/6/函数思想音频讲解.mp3' }
];

// 模拟API: 获取资源
const fetchResources = async () => {
  resourceLoading.value = true;
  console.log(`正在获取资源: 标签='${selectedResourceTag.value}', 关键词='${resourceSearch.value}'`);

  // 模拟网络延迟
  await new Promise(resolve => setTimeout(resolve, 500));

  let result = allResources;

  // 模拟后端筛选
  if (selectedResourceTag.value !== 'all') {
    result = result.filter(r => r.type === selectedResourceTag.value);
  }
  if (resourceSearch.value) {
    const keyword = resourceSearch.value.toLowerCase();
    result = result.filter(r =>
        r.name.toLowerCase().includes(keyword) ||
        r.description.toLowerCase().includes(keyword)
    );
  }

  resources.value = result;
  resourceLoading.value = false;
};

// 【核心功能】处理资源点击事件
const handleResourceClick = (resource) => {
  if (!resource.url) {
    ElMessage.warning('该资源没有有效的链接');
    return;
  }
  // 生成Markdown链接
  const markdownLink = `[${resource.name}](${resource.url})`;
  // 通过事件总线通知编辑器插入文本
  bus.emit('insert-text', markdownLink);
  ElMessage.success(`已复制资源链接到教案: ${markdownLink}`);
};

// 监听筛选条件变化，并使用防抖函数避免频繁请求
watch([selectedResourceTag, resourceSearch], useDebounceFn(() => {
  fetchResources();
}, 300));


// 上传资源
const uploadResource = async () => {
  console.log('模拟上传资源:', newResource.value);
  // 模拟API调用
  await new Promise(resolve => setTimeout(resolve, 1000));

  const resource = {
    id: allResources.length + 1,
    url: `/resources/${allResources.length + 1}/${newResource.value.name}`,
    ...newResource.value
  };
  allResources.push(resource);

  showUploadResourceDialog.value = false;
  ElMessage.success('资源上传成功!');

  // 重置表单并刷新列表
  newResource.value = { name: '', type: 'document', description: '' };
  fetchResources();
};

// 处理导入命令
const handleImportCommand = (command) => {
  switch (command) {
    case 'upload-outline':
      fileInput.value.click();
      break;
    case 'import-template':
      // 模拟导入模板
      ElMessage.success('模板导入功能将在后续版本提供');
      break;
    case 'from-textbook':
      // 模拟从教材提取
      ElMessage.success('教材提取功能将在后续版本提供');
      break;
  }
};

// --- Logic & Methods ---

onMounted(() => {
  // 组件挂载时获取初始资源
  fetchResources();

  // ModelStatusToast logic
  setTimeout(() => { toastVisible.value = false; }, 5000);

  // PromptEditor logic
  if (editorRef.value) {
    // 自定义 Monaco 主题
    monaco.editor.defineTheme('edu-dark', {
      base: 'vs-dark',
      inherit: true,
      rules: [
        { token: 'keyword', foreground: 'C792EA' },
        { token: 'strong', foreground: 'FFCB6B', fontStyle: 'bold' },
        { token: 'heading-1', foreground: '82AAFF', fontStyle: 'bold' },
      ],
      colors: { 'editor.background': '#0F172A' },
    });

    editor = monaco.editor.create(editorRef.value, {
      value: prompt.value,
      language: 'markdown',
      theme: 'vs',
      fontFamily: '"Inter", "JetBrains Mono", SFMono-Regular, monospace',
      fontLigatures: true,
      lineHeight: 22,
      wordWrap: 'on',
      minimap: { enabled: false },
      automaticLayout: true,
      fontSize: 15,
      scrollBeyondLastLine: false,
      padding: { top: 16, bottom: 16 },
      lineNumbers: 'off',
    });
    editor.onDidChangeModelContent(() => {
      prompt.value = editor.getValue();
    });

    // 添加编辑器动画效果
    const editorElement = editorRef.value;
    editorElement.style.opacity = '0';
    editorElement.style.transform = 'translateY(10px)';

    setTimeout(() => {
      editorElement.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
      editorElement.style.opacity = '1';
      editorElement.style.transform = 'translateY(0)';
    }, 100);

    // 添加编辑器焦点效果
    editor.onDidFocusEditorWidget(() => {
      editorElement.classList.add('editor-focused');
    });

    editor.onDidBlurEditorWidget(() => {
      editorElement.classList.remove('editor-focused');
    });
  }

  // 添加初始动画效果
  animateElements();

  // ScrollTrigger 进度条
  nextTick(() => {
    if (outputContainer.value) {
      gsap.context(()=>{
        ScrollTrigger.create({
          trigger: outputContainer.value,
          start: 'top top',
          end: 'bottom bottom',
          onUpdate: self => {
            scrollProgress.value = Math.round(self.progress * 100);
          },
        });
      });
    }
  });

  // 监听 bus 事件，实现跨组件联动
  bus.on('node-click', handleNodeClick);
});

onUnmounted(() => {
  bus.off('node-click', handleNodeClick);
  editor?.dispose();
  // 取消正在进行的请求
  if (currentOptimizeController) {
    currentOptimizeController.abort();
  }
  if (currentLessonController) {
    currentLessonController.abort();
  }
});

// 删除未使用的 insertInEditor 函数

// Method from OutlineImport - 使用原生 fetch + SSE 处理流式返回
const handleBeforeUpload = async (file) => {
  // 取消之前的请求
  if (currentOptimizeController) {
    currentOptimizeController.abort();
  }

  // 调用后端解析并优化教学大纲
  isParsing.value = true;
  bus.emit('preview-loading', true);
  showProgressIndicator('优化教学大纲');

  // 重置内容状态，准备接收优化大纲
  contentState.optimizedOutline = '';
  contentState.currentContent = '';
  contentState.contentType = 'outline';
  prompt.value = ''; // 同时重置prompt

  const formData = new FormData();
  formData.append('message', '请解析并优化下列教学大纲');
  formData.append('conversationId', String(user.id || user.userId)); // 确保转换为字符串
  formData.append('files', file);

  // 构造带有鉴权的请求头
  const headers = {
    'Accept': 'text/event-stream'
  };
  const token = localStorage.getItem('authToken');
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  try {
    currentOptimizeController = new AbortController();
    const response = await fetchWithRetry('/api/teacher/Ai/OptimizateOutline', {
      method: 'POST',
      headers,
      body: formData,
      signal: currentOptimizeController.signal
    });

    const reader = response.body.getReader();
    const decoder = new TextDecoder('utf-8');
    let sseBuffer = '';

    const pump = async () => {
      try {
        while (true) {
          const { done, value } = await reader.read();

          if (done) {
            // 处理剩余数据
            sseBuffer = processSSEBuffer(sseBuffer, (chunk) => {
              bus.emit('outline-chunk', chunk);
              prompt.value += chunk;
              // 同步更新contentState
              contentState.optimizedOutline += chunk;
              contentState.currentContent = contentState.optimizedOutline;
            });
            break;
          }

          const chunk = decoder.decode(value, { stream: true });
          sseBuffer += chunk.replace(/\r/g, ''); // 统一换行符
          sseBuffer = processSSEBuffer(sseBuffer, (chunk) => {
            bus.emit('outline-chunk', chunk);
            prompt.value += chunk;
            // 同步更新contentState
            contentState.optimizedOutline += chunk;
            contentState.currentContent = contentState.optimizedOutline;
          });
        }
      } catch (error) {
        if (error.name !== 'AbortError') {
          throw error;
        }
      } finally {
        isParsing.value = false;
        bus.emit('preview-loading', false);
        ElMessage.closeAll(); // 关闭进度提示
      }
    };

    await pump();

  } catch (err) {
    handleApiError(err, '优化教学大纲');
  }

  // 阻止 el-upload 默认上传行为
  return false;
};

// 添加preprocessMdChunk函数，用于处理Markdown块
function preprocessMdChunk(chunk) {
  if (!chunk) return '';

  // 清理特殊字符和格式问题
  return chunk
      .replace(/\\n/g, '\n')  // 替换转义的换行符
      .replace(/\\\\/g, '\\') // 替换转义的反斜杠
      .replace(/\\\"/g, '"')  // 替换转义的引号
      .trim();
}

// 将 meta 对象递归转换为 Markdown 列表
const metaToMarkdown = (meta, indent = 0) => {
  if (!meta) return '';
  const prefix = '  '.repeat(indent);
  let md = '';
  if (Array.isArray(meta)) {
    meta.forEach((item) => {
      if (typeof item === 'object') {
        md += `${prefix}-` + '\n' + metaToMarkdown(item, indent + 1);
      } else {
        md += `${prefix}- ${item}` + '\n';
      }
    });
  } else if (typeof meta === 'object') {
    Object.entries(meta).forEach(([key, val]) => {
      if (typeof val === 'object') {
        md += `${prefix}- **${key}:**` + '\n' + metaToMarkdown(val, indent + 1);
      } else {
        md += `${prefix}- **${key}:** ${val}` + '\n';
      }
    });
  } else {
    md += `${prefix}- ${meta}` + '\n';
  }
  return md;
};

const handleNodeClick = async (data) => {
  if (data.children && data.children.length) return; // 仅处理叶子节点
  const templateId = selectedTemplate.value || 'standard';
  const generator = snippetTemplates[templateId];
  if (!generator) return;

  const snippet = generator(data);
  const controller = editor?.getContribution('snippetController2');
  controller?.insert(snippet);

  selectedMeta.value = data.meta || {};
};

// 删除未使用的 flattenMeta 函数

// reactive to store current template and outline
const templateContent = ref('');
const outlineContent = ref('');

bus.on('template-update', (t)=>{ templateContent.value = t; });
bus.on('outline-update', (o)=>{ outlineContent.value = o; });

// Method from Toolbar/Main component
const handleGenerate = async () => {
  // 取消之前的请求
  if (currentLessonController) {
    currentLessonController.abort();
  }

  // 构造 message
  const message = `请按照模板:\n${templateContent.value}\n以及教学大纲:\n${outlineContent.value}\n为我生成教案`;

  bus.emit('preview-loading', true);
  showProgressIndicator('生成教案');

  // 创建FormData
  const formData = new FormData();
  formData.append('message', message);

  // 构造带有鉴权的请求头
  const headers = {
    'Accept': 'text/event-stream'
  };
  const token = localStorage.getItem('authToken');
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  try {
    currentLessonController = new AbortController();
    const response = await fetchWithRetry(`/api/teacher/Ai/CreateLesson?id=${user.id || user.userId}`, {
      method: 'POST',
      headers,
      body: formData,
      signal: currentLessonController.signal
    });

    const reader = response.body.getReader();
    const decoder = new TextDecoder('utf-8');
    let sseBuffer = '';

    // 清空之前的内容，开始生成教案
    bus.emit('lesson-ready', '');

    const pump = async () => {
      try {
        while (true) {
          const { done, value } = await reader.read();

          if (done) {
            // 处理剩余数据
            sseBuffer = processSSEBuffer(sseBuffer, (chunk) => {
              bus.emit('md-chunk', chunk);
            });
            break;
          }

          const chunk = decoder.decode(value, { stream: true });
          sseBuffer += chunk.replace(/\r/g, ''); // 统一换行符
          sseBuffer = processSSEBuffer(sseBuffer, (chunk) => {
            bus.emit('lesson-chunk', chunk);
          });
        }
      } catch (error) {
        if (error.name !== 'AbortError') {
          throw error;
        }
      } finally {
        bus.emit('preview-loading', false);
        ElMessage.closeAll(); // 关闭进度提示
      }
    };

    await pump();

  } catch (err) {
    handleApiError(err, '生成教案');
  }
};

// 添加元素动画效果函数
const animateElements = () => {
  const elements = document.querySelectorAll('.animate-in');
  elements.forEach((el, index) => {
    setTimeout(() => {
      el.classList.add('animated');
    }, 100 * index);
  });
};

watch(htmlOutput, () => {
  nextTick(() => {
    document.querySelectorAll('.markdown-body pre code').forEach(el => hljs.highlightElement(el));
  });
});

// 删除未使用的 loadingInstance 变量

const fileInput = ref(null);
const onFileChange = (e) => {
  const f = e.target.files[0];
  if (f) {
    handleBeforeUpload(f);
    e.target.value = '';
  }
};
</script>

<style scoped>
/* 三栏布局样式 */
.template-panel {
  width: 320px;
  flex-shrink: 0;
  z-index: 10;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.template-panel-expanded {
  width: 550px;
  box-shadow: 0 10px 25px -5px rgba(99, 102, 241, 0.2), 0 8px 10px -6px rgba(99, 102, 241, 0.15);
  transform: translateZ(0);
}

.preview-panel {
  flex: 1;
  min-width: 0;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.resource-panel {
  width: 280px;
  flex-shrink: 0;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

/* 滚动条美化 */
.markdown-scroll::-webkit-scrollbar {
  width: 6px;
}

.markdown-scroll::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 10px;
}

.markdown-scroll::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 10px;
}

.markdown-scroll::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* 平滑过渡动画 */
.animate-in {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>





