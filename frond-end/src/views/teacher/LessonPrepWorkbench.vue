<template>
  <div class="flex flex-col h-full bg-slate-50 dark:bg-slate-900">
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

    <!-- 新双栏布局 -->
    <div class="flex h-full gap-8 px-10 py-6 w-full">
      <!-- 固定宽度 420px 的模板编辑区域 -->
      <TemplatePane class="w-[420px] flex-shrink-0" />
      <!-- 右侧渲染区域占剩余所有空间 -->
      <PreviewPane class="flex-1 min-w-0" >
        <template #toolbar>
          <div class="flex items-center gap-4 w-full justify-between">
            <div class="flex items-center gap-3">
              <!-- 上传按钮 -->
              <div class="flex items-center gap-3">
                <el-button size="small" @click="fileInput.click()">
                  <el-icon class="mr-1"><UploadFilled /></el-icon>导入大纲
            </el-button>
                <input ref="fileInput" type="file" class="hidden" accept=".pdf,.doc,.docx" @change="onFileChange" />
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
              <el-button size="small" :disabled="!lessonBuffer" @click="openEditor">
                <el-icon class="mr-1"><DocumentCopy /></el-icon>编辑教案
                </el-button>
            </div>
          </div>
        </template>
      </PreviewPane>
            </div>

    <!-- ======= 教案预览 Drawer ======= -->
    <el-drawer v-model="showPreviewDrawer" :with-header="false" size="40%" direction="rtl" @opened="() => { currentStep = 2 }" @closed="() => { currentStep = 1 }">
      <div class="p-6 h-full overflow-y-auto prose dark:prose-invert">
        <h2 class="mb-4">教案预览</h2>
        <div v-if="!lessonBuffer" class="text-slate-500 text-center flex items-center justify-center h-full">
          <el-icon class="mr-1"><Document /></el-icon>暂无内容，请先生成教案。
        </div>
        <div v-else v-dompurify-html="lessonBuffer"></div>
      </div>
    </el-drawer>

    <!-- ===== 教案编辑弹窗 ===== -->
    <LessonPlanEditor v-model:visible="showEditor" v-model="editorContent" @save="handleSaveLesson" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, nextTick, watch } from 'vue';
import { Splitpanes, Pane } from 'splitpanes';
import 'splitpanes/dist/splitpanes.css';
import { ElUpload, ElIcon, ElProgress, ElButton, ElSelect, ElOption, ElTooltip, ElSkeleton, ElLoading, ElSteps, ElStep, ElDrawer, ElDescriptions, ElDescriptionsItem, ElSwitch } from 'element-plus';
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
  Cpu
} from '@element-plus/icons-vue';
import * as monaco from 'monaco-editor';
import { marked } from 'marked';
import DOMPurify from 'dompurify';
import hljs from 'highlight.js';
import { useResizeObserver } from '@vueuse/core';
import gsap from 'gsap';
import ScrollTrigger from 'gsap/ScrollTrigger';
gsap.registerPlugin(ScrollTrigger);
import { templates as snippetTemplates } from '@/utils/templates';
import { bus } from '@/utils/eventBus';
import TemplatePane from '@/components/lesson-prep/TemplatePane.vue';
import PreviewPane from '@/components/lesson-prep/PreviewPane.vue';
import { useAuthStore } from '@/stores/counter.js';
import LessonPlanEditor from '@/components/lesson-prep/LessonPlanEditor.vue';

// 获取用户信息
const user = useAuthStore().user;

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
const isDark = ref(document.documentElement.classList.contains('dark'));

// --- State from StreamOutput ---
const streamingText = ref('');
const isStreaming = ref(false);
let streamInterval = null;
const outputContainer = ref(null);
const htmlOutput = computed(() => DOMPurify.sanitize(marked(prompt.value, { gfm: true })));

// 根据容器大小实时调整 Monaco 编辑器
useResizeObserver(editorRef, () => {
  editor?.layout();
});

// ======= Wizard State =======
const currentStep = ref(0); // 0: 导入, 1: 编辑, 2: 预览
const showPreviewDrawer = ref(false);

const openPreview = () => {
  showPreviewDrawer.value = true;
};

// ===== 教案编辑弹窗 =====
const showEditor = ref(false);
const editorContent = ref('');
const lessonBuffer = ref('');

// 监听流式内容更新
bus.on('md-ready', (md)=>{ lessonBuffer.value = md || ''; });
bus.on('md-chunk', (chunk)=>{ lessonBuffer.value += chunk || ''; });

function openEditor() {
  editorContent.value = lessonBuffer.value;
  showEditor.value = true;
}

function handleSaveLesson(content) {
  // 保存到本地文件
  const blob = new Blob([content], { type: 'text/html;charset=utf-8' });
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = `lesson-plan-${Date.now()}.html`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}

// 监听 bus 触发打开编辑器事件
bus.on('open-editor', (full) => {
  editorContent.value = full;
  showEditor.value = true;
});

// --- Logic & Methods ---

onMounted(() => {
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
      theme: isDark.value ? 'edu-dark' : 'vs',
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

// Method from OutlineImport - 使用原生 fetch + SSE 处理流式返回
const handleBeforeUpload = (file) => {
  // 调用后端解析并优化教学大纲
  isParsing.value = true;
  bus.emit('preview-loading', true);

  const formData = new FormData();
  formData.append('message', '请解析并优化下列教学大纲');
  formData.append('conversationId', user.id);
  formData.append('files', file);

  // 构造带有鉴权的请求头（如果本地存储有 token）
  const headers = {
    'Accept': 'text/event-stream'
  };
  const token = localStorage.getItem('authToken');
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  fetch('/api/teacher/Ai/OptimizateOutline', {
    method: 'POST',
    headers,
    body: formData
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const reader = response.body.getReader();
      const decoder = new TextDecoder('utf-8');

      let htmlBuffer = '';
      let sseBuffer = '';
      let typingQueue = '';
      let typingTimer = null;

      const enqueue = (str) => {
        if (!str) return;
        typingQueue += str;
        bus.emit('md-chunk', str);
        startTyping();
      };

      const startTyping = () => {
        if (typingTimer) return;
        typingTimer = setInterval(() => {
          if (!typingQueue.length) {
            clearInterval(typingTimer);
            typingTimer = null;
            return;
          }
          const ch = typingQueue[0];
          typingQueue = typingQueue.slice(1);
          prompt.value += ch;
        }, 6);
      };

      const flushHtmlBuffer = () => {
        let idx;
        // 以标签闭合作为切分点
        while ((idx = htmlBuffer.indexOf('>')) !== -1) {
          const segment = htmlBuffer.slice(0, idx + 1);
          htmlBuffer = htmlBuffer.slice(idx + 1);
          enqueue(segment);
        }
        // 处理纯文本
        if (htmlBuffer && !htmlBuffer.includes('<')) {
          enqueue(htmlBuffer);
          htmlBuffer = '';
        }
      };

      const processSSEBuffer = () => {
        let sep;
        // 寻找完整事件（以空行分隔）
        while ((sep = sseBuffer.indexOf('\n\n')) !== -1) {
          const eventBlock = sseBuffer.slice(0, sep);
          sseBuffer = sseBuffer.slice(sep + 2);

          // 处理 eventBlock，抽取 data 行
          const dataLines = eventBlock
            .split(/\n/)
            .filter(l => l.startsWith('data:'))
            .map(l => l.slice(5));

          const dataStr = dataLines.join('\n');
          htmlBuffer += dataStr;
          flushHtmlBuffer();
    }
  };

      const pump = () => {
        reader.read().then(({ done, value }) => {
          if (done) {
            // 处理剩余数据
            processSSEBuffer();
            flushHtmlBuffer();
            bus.emit('preview-loading', false);
            return;
          }

          const chunk = decoder.decode(value, { stream: true });
          sseBuffer += chunk.replace(/\r/g, ''); // 统一换行符
          processSSEBuffer();
          pump();
        });
      };
      pump();
    })
    .catch(err => {
    console.error(err);
    isParsing.value = false;
      bus.emit('preview-loading', false);
  });

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

function flattenMeta(obj, prefix = '') {
  const res = {};
  for (const [k, v] of Object.entries(obj || {})) {
    const key = prefix ? `${prefix}.${k}` : k;
    if (typeof v === 'object' && v !== null) {
      Object.assign(res, flattenMeta(v, key));
    } else {
      res[key] = v;
    }
  }
  return res;
}

// reactive to store current template and outline
const templateContent = ref('');
const outlineContent = ref('');

bus.on('template-update', (t)=>{ templateContent.value = t; });
bus.on('outline-update', (o)=>{ outlineContent.value = o; });

// Method from Toolbar/Main component
const handleGenerate = () => {
  // 构造 message
  const message = `请按照模板:\n${templateContent.value}\n以及教学大纲:\n${outlineContent.value}\n为我生成教案`;

  bus.emit('preview-loading', true);

  // 创建FormData
  const formData = new FormData();
  formData.append('message', message);
  
  // 构造带有鉴权的请求头（如果本地存储有 token）
  const headers = {
    'Accept': 'text/event-stream'
  };
  const token = localStorage.getItem('authToken');
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  // 创建SSE连接
  fetch(`/api/teacher/Ai/CreateLesson?id=${user.id}`, {
    method: 'POST',
    headers,
    body: formData
  })
  .then(response => {
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder('utf-8');

    let sseBuffer = '';
    
    // 清空之前的内容
    bus.emit('md-ready', '');

    const processSSEBuffer = () => {
      let sep;
      // 寻找完整事件（以空行分隔）
      while ((sep = sseBuffer.indexOf('\n\n')) !== -1) {
        const eventBlock = sseBuffer.slice(0, sep);
        sseBuffer = sseBuffer.slice(sep + 2);

        // 处理 eventBlock，抽取 data 行
        const dataLines = eventBlock
          .split(/\n/)
          .filter(l => l.startsWith('data:'))
          .map(l => l.slice(5));

        const dataStr = dataLines.join('\n');
        if (dataStr) {
          // 处理接收到的Markdown块
          const chunk = preprocessMdChunk(dataStr);
          bus.emit('md-chunk', chunk);
        }
      }
    };

    const pump = () => {
      reader.read().then(({ done, value }) => {
        if (done) {
          // 处理剩余数据
          processSSEBuffer();
          bus.emit('preview-loading', false);
          return;
        }

        const chunk = decoder.decode(value, { stream: true });
        sseBuffer += chunk.replace(/\r/g, ''); // 统一换行符
        processSSEBuffer();
        pump();
      });
    };
    pump();
  })
  .catch(err => {
    console.error('生成教案出错:', err);
    bus.emit('preview-loading', false);
  });
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
  
  // Loading overlay
  if (outputContainer.value) {
    loadingInstance = ElLoading.service({
      target: outputContainer.value,
      lock: true,
      text: 'AI 正在生成...',
      background: 'rgba(255,255,255,0.6)'
    });
  }
  
  const fullResponse = `### 智能教学设计：第一章 课程介绍\n\n**教学目标:**\n1.  **知识与技能:** 学生能够清晰定义"人工智能"，并列举至少三个应用实例。\n2.  **过程与方法:** 通过小组讨论，培养学生归纳总结和口头表达能力。\n3.  **情感态度价值观:** 激发学生对前沿科技的兴趣，树立科学探索精神。\n\n**教学重点:** 人工智能的核心概念。\n**教学难点:** 理解"智能"的多维度含义。`;
  let i = 0;
  streamInterval = setInterval(() => {
    if (i < fullResponse.length) {
      streamingText.value += fullResponse[i];
      i++;
      scrollToBottom();
    } else {
      handleStopStream();
      // 添加生成完成动画
      const el = outputContainer.value;
      if (el) {
        el.classList.add('generation-complete');
        setTimeout(() => {
          el.classList.remove('generation-complete');
        }, 1000);
      }
    }
  }, 20);
};

const handleStopStream = () => {
  isStreaming.value = false;
  clearInterval(streamInterval);
  loadingInstance?.close();
};

const handleCopy = () => {
  navigator.clipboard.writeText(streamingText.value);
  
  // 添加复制成功提示动画
  const copyBtn = document.querySelector('.action-btn');
  if (copyBtn) {
    copyBtn.classList.add('copy-success');
    setTimeout(() => {
      copyBtn.classList.remove('copy-success');
    }, 1000);
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

function outlineToMarkdown(nodes, depth = 1) {
  if (!nodes || !nodes.length) return ''
  return nodes.map(n => {
    const prefix = '#'.repeat(depth);
    const metaPart = n.meta && Object.keys(n.meta).length ? `\n*${JSON.stringify(n.meta, null, 2)}*\n` : '';
    const childrenMd = n.children?.length ? outlineToMarkdown(n.children, depth + 1) : '';
    return `${prefix} ${n.label}\n${metaPart}${childrenMd}`;
  }).join('\n');
}

// --- New reactive declarations ---
const livePreview = ref(false);
const cachedHtml = ref('');

const renderHtml = computed(() => cachedHtml.value);

watch([prompt, livePreview], ([md, live])=>{
  if(live){
    cachedHtml.value = DOMPurify.sanitize(marked(md));
  }
});

const manualRefresh = () => {
  cachedHtml.value = DOMPurify.sanitize(marked(prompt.value));
};

watch(htmlOutput, () => {
  nextTick(() => {
    document.querySelectorAll('.markdown-body pre code').forEach(el => hljs.highlightElement(el));
  });
});

let loadingInstance = null;

const fileInput = ref(null);
const onFileChange = (e) => {
  const f = e.target.files[0];
  if (f) {
    handleBeforeUpload(f);
    e.target.value = '';
  }
};
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@300..800&display=swap');
/* 可变字体 & 编辑器 UI 优化：必须置于其他规则之前 */
/* --- General Styles for Splitpanes --- */
.splitpanes.default-theme .splitpanes__pane { 
  background-color: transparent; 
  transition: all 0.3s ease;
}

.splitpanes.default-theme .splitpanes__splitter {
  background-color: #f1f5f9; /* bg-slate-100 */
  border-left: 1px solid #e2e8f0; /* border-slate-200 */
  position: relative;
  width: 8px;
  z-index: 10;
  transition: all 0.2s ease;
}

.dark .splitpanes.default-theme .splitpanes__splitter {
  background-color: #1e293b; /* dark:bg-slate-800 */
  border-left: 1px solid #334155; /* dark:border-slate-700 */
}

.splitpanes.default-theme .splitpanes__splitter:hover { 
  border-left: 1px solid #4f46e5;
  box-shadow: 0 0 8px rgba(79, 70, 229, 0.3);
}

.splitpanes.default-theme .splitpanes__splitter:before {
  content: '';
  position: absolute;
  left: 50%; top: 50%;
  transform: translate(-50%,-50%);
  width: 2px; height: 40px;
  background-color: #cbd5e1; /* slate-300 */
  border-radius: 5px;
  transition: all 0.3s ease;
}

.dark .splitpanes.default-theme .splitpanes__splitter:before { 
  background-color: #475569; 
}

.splitpanes.default-theme .splitpanes__splitter:hover:before { 
  background-color: #4f46e5;
  box-shadow: 0 0 8px rgba(79, 70, 229, 0.5);
  height: 60px;
}

/* --- 上传区域样式 --- */
.upload-area .el-upload-dragger {
  width: 100% !important;
  border: 2px dashed #cbd5e1;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.upload-area .el-upload-dragger:hover {
  border-color: #6366f1;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.15);
}

.dark .upload-area .el-upload-dragger {
  border-color: #475569;
  background-color: #1e293b;
}

.dark .upload-area .el-upload-dragger:hover {
  border-color: #818cf8;
  box-shadow: 0 4px 12px rgba(129, 140, 248, 0.2);
}

.el-icon--upload {
  font-size: 32px !important;
  margin-bottom: 8px;
  transition: transform 0.3s ease;
}

.el-upload-dragger:hover .el-icon--upload {
  transform: scale(1.1);
}

/* --- 树形控件样式 --- */
.custom-tree.el-tree {
  --el-tree-node-hover-bg-color: #f0f4ff;
  background-color: transparent;
  font-size: 0.95rem;
}

.dark .custom-tree.el-tree {
  --el-tree-text-color: #cbd5e1; /* slate-300 */
  --el-tree-node-hover-bg-color: #334155; /* slate-700 */
}

.custom-tree .el-tree-node__content {
  height: 36px;
  border-radius: 4px;
  margin: 2px 0;
  transition: all 0.2s ease;
}

.custom-tree .el-tree-node__content:hover {
  background-color: var(--el-tree-node-hover-bg-color);
  transform: translateX(2px);
}

.custom-tree .el-tree-node.is-current > .el-tree-node__content {
  background-color: #818cf8;
  color: white;
}

.dark .custom-tree .el-tree-node.is-current > .el-tree-node__content {
  background-color: #4f46e5;
}

.outline-container {
  scrollbar-width: thin;
  scrollbar-color: #cbd5e1 transparent;
}

.outline-container::-webkit-scrollbar {
  width: 6px;
}

.outline-container::-webkit-scrollbar-track {
  background: transparent;
}

.outline-container::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 3px;
}

.dark .outline-container::-webkit-scrollbar-thumb {
  background-color: #475569;
}

/* --- 下拉菜单样式 --- */
.template-selector .el-input__wrapper {
  box-shadow: 0 0 0 1px #e2e8f0 inset !important;
  border-radius: 6px !important;
  transition: all 0.3s ease;
}

.template-selector .el-input__wrapper:hover {
  box-shadow: 0 0 0 1px #6366f1 inset !important;
}

.dark .template-selector .el-input__wrapper {
  box-shadow: 0 0 0 1px #475569 inset !important;
  background-color: #1e293b;
}

.dark .template-selector .el-input__wrapper:hover {
  box-shadow: 0 0 0 1px #818cf8 inset !important;
}

.template-dropdown .el-select-dropdown__item {
  padding: 8px 12px !important;
}

.template-dropdown .el-select-dropdown__item.selected {
  color: #6366f1 !important;
  font-weight: 600;
}

/* --- 按钮样式 --- */
.generate-btn {
  background: linear-gradient(135deg, var(--color-primary) 0%, #4f46e5 100%) !important;
  border: none !important;
  border-radius: 6px !important;
  padding: 8px 18px !important;
  transition: all 0.3s ease !important;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3) !important;
  position: relative;
  overflow: hidden;
}

.generate-btn:hover {
  transform: translateY(-1px) !important;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.4) !important;
}

.generate-btn:active {
  transform: translateY(1px) !important;
}

.generate-btn::after {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    to bottom right,
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 0.1) 50%,
    rgba(255, 255, 255, 0) 100%
  );
  transform: rotate(45deg);
  transition: all 0.3s ease;
  opacity: 0;
}

.generate-btn:hover::after {
  animation: shine 1.5s infinite;
}

@keyframes shine {
  0% {
    opacity: 0;
    transform: translateX(-100%) rotate(45deg);
  }
  50% {
    opacity: 0.7;
  }
  100% {
    opacity: 0;
    transform: translateX(100%) rotate(45deg);
  }
}

.action-btn {
  transition: all 0.3s ease !important;
}

.action-btn:hover:not(:disabled) {
  transform: translateY(-1px) !important;
}

.action-btn:active:not(:disabled) {
  transform: translateY(1px) !important;
}

/* --- 编辑器容器样式 --- */
.editor-container {
  box-shadow: inset 0 0 8px rgba(0, 0, 0, 0.05);
  border-radius: 0 0 4px 4px;
  transition: all 0.3s ease;
}

.editor-container.editor-focused {
  box-shadow: inset 0 0 0 2px rgba(99, 102, 241, 0.3), 0 0 12px rgba(99, 102, 241, 0.2);
}

.dark .editor-container {
  box-shadow: inset 0 0 8px rgba(0, 0, 0, 0.2);
}

.dark .editor-container.editor-focused {
  box-shadow: inset 0 0 0 2px rgba(129, 140, 248, 0.3), 0 0 12px rgba(129, 140, 248, 0.2);
}

/* --- 输出容器样式 --- */
.output-container {
  scrollbar-width: thin;
  scrollbar-color: #cbd5e1 transparent;
  transition: all 0.3s ease;
}

.output-container.generating {
  animation: pulse 0.3s ease;
}

.output-container.generation-complete {
  animation: success-pulse 0.5s ease;
}

@keyframes pulse {
  0% { background-color: transparent; }
  50% { background-color: rgba(99, 102, 241, 0.05); }
  100% { background-color: transparent; }
}

@keyframes success-pulse {
  0% { background-color: transparent; }
  30% { background-color: rgba(52, 211, 153, 0.1); }
  100% { background-color: transparent; }
}

.output-container::-webkit-scrollbar {
  width: 6px;
}

.output-container::-webkit-scrollbar-track {
  background: transparent;
}

.output-container::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 3px;
}

.dark .output-container::-webkit-scrollbar-thumb {
  background-color: #475569;
}

.prose :where(pre):not(:where([class~="not-prose"] *)) {
  background-color: #f3f4f6; 
  color: #1f2937;
  border-radius: 8px; 
  padding: 1em;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  margin: 1.5em 0;
}

.dark .prose :where(pre):not(:where([class~="not-prose"] *)) {
  background-color: #1f2937; 
  color: #e5e7eb;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.prose h3 {
  color: #4f46e5;
  border-bottom: 1px solid #e2e8f0;
  padding-bottom: 0.5rem;
  margin-top: 1.5rem;
}

.dark .prose h3 {
  color: #818cf8;
  border-bottom: 1px solid #334155;
}

.typing-cursor {
  display: inline-block;
  width: 8px; 
  height: 1.2em;
  background-color: #4f46e5;
  animation: blink 1s step-end infinite;
  border-radius: 2px;
}

.dark .typing-cursor {
  background-color: #818cf8;
}

@keyframes blink {
  from, to { opacity: 1; }
  50% { opacity: 0; }
}

/* 淡入动画 */
.fade-in {
  animation: fadeIn 0.5s ease-in-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* 添加响应式设计 */
@media (max-width: 768px) {
  .splitpanes.default-theme .splitpanes__pane {
    min-width: 100% !important;
  }
}

/* --- 动画效果 --- */
.animate-in {
  opacity: 0;
  transform: translateY(10px);
  transition: opacity 0.5s ease, transform 0.5s ease;
}

.animate-in.animated {
  opacity: 1;
  transform: translateY(0);
}

/* 复制按钮动画 */
.action-btn.copy-success {
  background-color: rgba(52, 211, 153, 0.2) !important;
  border-color: #34d399 !important;
  color: #10b981 !important;
  transform: scale(1.1) !important;
}

.dark .action-btn.copy-success {
  background-color: rgba(52, 211, 153, 0.3) !important;
  color: #34d399 !important;
}

:root {
  --color-primary: #6366f1;
  --color-primary-dark: #818cf8;
  --ease-primary: cubic-bezier(0.4, 0, 0.2, 1);
  --border-color-light: #e2e8f0;
  --border-color-dark: #334155;
}

/* Motion safe */
@media (prefers-reduced-motion: reduce) {
  * {
    animation: none !important;
    transition: none !important;
  }
}

/* 统一过渡 */
*, *::before, *::after {
  transition-timing-function: var(--ease-primary);
}

/* --- 工具栏过渡效果 --- */
.toolbar-area {
  transition: background 0.4s var(--ease-primary), border-color 0.4s var(--ease-primary);
}

.toolbar-area:hover {
  background: linear-gradient(90deg, rgba(99, 102, 241, 0.05) 0%, rgba(99, 102, 241, 0.12) 100%);
  border-color: var(--color-primary);
}

.dark .toolbar-area:hover {
  background: linear-gradient(90deg, rgba(67, 56, 202, 0.3) 0%, rgba(79, 70, 229, 0.35) 100%);
}

/* === 可变字体 & 编辑器 UI 优化 === */
.monaco-editor, .monaco-editor * {
  font-family: 'JetBrains Mono', SFMono-Regular, Consolas, 'Liberation Mono', Menlo, monospace !important;
  font-variant-ligatures: contextual;
}

.editor-container {
  background: radial-gradient(circle at 50% 0, rgba(99,102,241,0.04) 0%, transparent 70%) !important;
}

/* ---------- 预览与结果面板横向滚动优化 ---------- */
.prose {
  overflow-x: auto;
  scrollbar-width: thin;
}
.prose pre {
  overflow-x: auto;
}
</style> 