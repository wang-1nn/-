<template>
  <div class="lesson-preview-container flex flex-col h-full">
    <!-- 预览工具栏 -->
    <div class="preview-toolbar flex items-center justify-between p-3 bg-white border-b">
      <div class="flex items-center space-x-4">
        <div class="format-indicator flex items-center text-sm text-slate-500">
          <el-icon class="mr-1" :class="{ 'text-green-500': outputFormat === 'markdown' }">
            <DocumentCopy v-if="outputFormat === 'markdown'" />
            <Document v-else />
          </el-icon>
          <span>{{ outputFormat === 'markdown' ? 'Markdown' : 'HTML' }} 格式</span>
        </div>
        
        <div class="word-count text-sm text-slate-500">
          <el-icon class="mr-1"><ChatLineRound /></el-icon>
          <span>{{ contentWordCount }} 字</span>
        </div>
      </div>
      
      <div class="flex items-center space-x-2">
        <el-tooltip content="复制内容" placement="top">
          <el-button 
            size="small" 
            circle 
            :disabled="!content" 
            @click="copyContent"
          >
            <el-icon><CopyDocument /></el-icon>
          </el-button>
        </el-tooltip>
        
        <el-tooltip content="下载教案" placement="top">
          <el-button 
            size="small" 
            circle 
            :disabled="!content" 
            @click="downloadContent"
          >
            <el-icon><Download /></el-icon>
          </el-button>
        </el-tooltip>
        
        <el-tooltip content="全屏预览" placement="top">
          <el-button 
            size="small" 
            circle 
            :disabled="!content" 
            @click="toggleFullscreen"
          >
            <el-icon><FullScreen v-if="!isFullscreen" /><Aim v-else /></el-icon>
          </el-button>
        </el-tooltip>
        
        <el-tooltip content="打开编辑器" placement="top">
          <el-button 
            size="small" 
            circle 
            :disabled="!content" 
            @click="openEditor"
          >
            <el-icon><Edit /></el-icon>
          </el-button>
        </el-tooltip>
      </div>
    </div>
    
    <!-- 预览内容区 -->
    <div class="preview-content flex-1 overflow-auto bg-white p-6">
      <div v-if="loading" class="h-full flex flex-col items-center justify-center">
        <div class="typing-animation mb-4">
          <span v-for="(dot, index) in 3" :key="index" :style="{ animationDelay: `${index * 0.3}s` }">.</span>
        </div>
        <p class="text-slate-500">{{ loadingMessage }}</p>
      </div>
      
      <div v-else-if="!content" class="h-full flex flex-col items-center justify-center">
        <el-icon class="text-5xl text-slate-300 mb-4"><Document /></el-icon>
        <p class="text-slate-500">点击"生成教案"按钮开始创建内容</p>
      </div>
      
      <div v-else ref="previewRef" class="prose prose-slate prose-headings:font-medium prose-img:rounded-md max-w-none" v-dompurify-html="renderedContent"></div>
    </div>
    
    <!-- 生成时间和元信息 -->
    <div v-if="content" class="preview-meta p-3 bg-white border-t text-xs text-slate-400">
      <div class="flex items-center justify-between">
        <span>生成时间: {{ generationTime }}</span>
        <span>AI 模型: {{ modelName }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, defineEmits, defineProps, onMounted, watch } from 'vue';
import { DocumentCopy, Document, ChatLineRound, CopyDocument, Download, FullScreen, Aim, Edit } from '@element-plus/icons-vue';
import { marked } from 'marked';
import DOMPurify from 'dompurify';
import { ElMessage } from 'element-plus';
import hljs from 'highlight.js';

const props = defineProps({
  content: {
    type: String,
    default: ''
  },
  outputFormat: {
    type: String,
    default: 'markdown'
  },
  loading: {
    type: Boolean,
    default: false
  },
  loadingMessage: {
    type: String,
    default: '正在生成教案...'
  },
  modelName: {
    type: String,
    default: 'AI模型'
  }
});

const emit = defineEmits(['open-editor']);

// 预览区引用
const previewRef = ref(null);

// 全屏状态
const isFullscreen = ref(false);

// 格式化后的内容
const renderedContent = computed(() => {
  if (!props.content) return '';
  
  // 如果是Markdown，则转换为HTML
  if (props.outputFormat === 'markdown') {
    return DOMPurify.sanitize(marked(props.content));
  }
  
  // 如果已经是HTML，直接显示
  return DOMPurify.sanitize(props.content);
});

// 内容字数统计
const contentWordCount = computed(() => {
  if (!props.content) return 0;
  
  // 移除HTML标签
  const plainText = props.content.replace(/<[^>]*>/g, '');
  
  // 简单的中英文字数统计
  return plainText.trim().replace(/\s+/g, ' ').length;
});

// 生成时间
const generationTime = ref(new Date().toLocaleString());

// 复制内容
const copyContent = () => {
  try {
    navigator.clipboard.writeText(props.content);
    ElMessage.success('内容已复制到剪贴板');
  } catch (err) {
    ElMessage.error('复制失败，请手动复制');
  }
};

// 下载内容
const downloadContent = () => {
  try {
    const fileName = `教案_${new Date().toISOString().slice(0, 10)}.${props.outputFormat === 'markdown' ? 'md' : 'html'}`;
    const fileContent = props.content;
    const blob = new Blob([fileContent], { type: 'text/plain;charset=utf-8' });
    
    // 创建下载链接
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = fileName;
    link.click();
    
    // 清理
    URL.revokeObjectURL(link.href);
    ElMessage.success('教案已下载');
  } catch (err) {
    ElMessage.error('下载失败');
  }
};

// 切换全屏预览
const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value;
  
  // 触发全屏事件
  emit('toggle-fullscreen', isFullscreen.value);
};

// 打开编辑器
const openEditor = () => {
  emit('open-editor', props.content);
};

// 监听内容变化，高亮代码
watch(() => renderedContent.value, () => {
  if (renderedContent.value) {
    // 更新生成时间
    generationTime.value = new Date().toLocaleString();
    
    // 应用代码高亮
    setTimeout(() => {
      if (previewRef.value) {
        previewRef.value.querySelectorAll('pre code').forEach(block => {
          hljs.highlightElement(block);
        });
      }
    }, 0);
  }
});

// 组件挂载后高亮代码
onMounted(() => {
  if (props.content && previewRef.value) {
    setTimeout(() => {
      previewRef.value.querySelectorAll('pre code').forEach(block => {
        hljs.highlightElement(block);
      });
    }, 0);
  }
});
</script>

<style scoped>
.lesson-preview-container {
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
}

/* 自定义滚动条 */
.preview-content::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.preview-content::-webkit-scrollbar-thumb {
  background-color: #a5b4fc;
  border-radius: 4px;
}

.preview-content::-webkit-scrollbar-track {
  background-color: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
}

/* 全屏模式 */
.lesson-preview-container.fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 1000;
  background: white;
}

/* 打字动画 */
.typing-animation {
  display: flex;
  justify-content: center;
  font-size: 2rem;
  color: #6366f1;
}

.typing-animation span {
  animation: typing 1s infinite;
  font-weight: bold;
}

@keyframes typing {
  0%, 100% { 
    opacity: 0;
    transform: translateY(0);
  }
  50% { 
    opacity: 1;
    transform: translateY(-10px);
  }
}
</style>