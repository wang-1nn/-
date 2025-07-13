<template>
  <div class="flex flex-col h-full">
    <!-- 工具栏占位，可后续插入模板选择/生成按钮 -->
    <header class="flex items-center justify-between px-4 py-2 border-b bg-slate-50 dark:bg-slate-700 sticky top-0 z-10">
      <slot name="toolbar" />
    </header>

    <!-- Markdown 渲染（普通滚动容器） -->
    <div
      v-bind="containerProps"
      class="relative flex-1 max-h-screen overflow-y-auto my-4 rounded-2xl bg-white/95 dark:bg-slate-800/90 border border-slate-200 dark:border-slate-700 shadow-lg backdrop-blur markdown-body markdown-scroll"
    >
      <div v-if="loading" class="absolute inset-0 flex items-center justify-center bg-white/60 dark:bg-slate-800/60 z-10">
        <el-icon class="is-loading text-3xl text-indigo-500"><Loading /></el-icon>
      </div>
      <div v-bind="wrapperProps" class="px-4 py-4">
        <div v-for="row in list" :key="row.index" v-html="renderHtml(row.data)" class="markdown-body" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onUnmounted, onMounted, onUpdated } from 'vue';
import { useVirtualList, useThrottleFn } from '@vueuse/core';
import { marked } from 'marked';
import DOMPurify from 'dompurify';
import hljs from 'highlight.js';
import { bus } from '@/utils/eventBus';

const raw = ref('');
const loading = ref(false);
let typingTimer = null;
let queue = '';

// === 滚动位置相关 ===
const containerRef = ref(null); // 绑定滚动容器
// 保存用户最新的滚动位置，默认 0（顶部）
const savedScrollTop = ref(0);
// 记录用户距离底部的距离（px）
const bottomOffset = ref(0);
// 是否允许自动滚动恢复（初始化和用户滚动后短暂禁用，避免互相干扰）
const allowScrollRestore = ref(true);
// 上次内容高度，用于判断是否有新增内容
const lastContentHeight = ref(0);
// 滚动锁定计时器
let scrollLockTimer = null;
// 是否正在触底
const isNearBottom = ref(false);

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

// 增量模式：收到 HTML 标签后追加到队列
bus.on('md-chunk', (chunk) => {
  if (!chunk) return;
  queue += chunk;

  const startTyping = () => {
    if (typingTimer) return;
    typingTimer = setInterval(() => {
      if (!queue.length) {
        clearInterval(typingTimer);
        typingTimer = null;
        return;
      }
      const ch = queue[0];
      queue = queue.slice(1);
      raw.value += ch;
    }, 6);
  };

  startTyping();

  // 高亮新代码块
  nextTick(() => {
    document.querySelectorAll('.markdown-body pre code').forEach(el => hljs.highlightElement(el));
  });
});

onUnmounted(() => {
  clearInterval(typingTimer);
  clearTimeout(scrollLockTimer);
});

// ===== 虚拟列表 =====
const paragraphs = computed(() => raw.value.split('\n\n'));

const { list, containerProps, wrapperProps } = useVirtualList(paragraphs, {
  itemHeight: 120,
  overscan: 10,
});

// ==== 将容器 ref 和滚动事件包装，确保内部滚动 & 我们自定义逻辑共存 ====
const originalContainerRef = containerProps.ref;
const originalOnScroll = containerProps.onScroll;

containerProps.ref = (el) => {
  containerRef.value = el;
  if (typeof originalContainerRef === 'function') {
    originalContainerRef(el);
  } else if (originalContainerRef && typeof originalContainerRef === 'object') {
    originalContainerRef.value = el;
  }
};

containerProps.onScroll = (e) => {
  originalOnScroll && originalOnScroll(e);
  const el = e.target;
  // 记录当前滚动位置
  savedScrollTop.value = el.scrollTop;
  
  // 暂时禁用自动滚动恢复，避免用户滚动时被打断
  allowScrollRestore.value = false;
  clearTimeout(scrollLockTimer);
  setTimeout(() => {
    allowScrollRestore.value = true;
  }, 100);
  
  // 距离底部的距离（>0 表示离底部还有余量）
  bottomOffset.value = el.scrollHeight - el.scrollTop - el.clientHeight;
  
  // 检测是否接近底部（30px内）
  isNearBottom.value = bottomOffset.value < 30;
  
  // 如果用户正在触底，锁定滚动位置一段时间
  if (isNearBottom.value) {
    clearTimeout(scrollLockTimer);
    scrollLockTimer = setTimeout(() => {
      if (containerRef.value && isNearBottom.value) {
        containerRef.value.scrollTo({
          top: containerRef.value.scrollHeight,
          behavior: 'auto'
        });
      }
    }, 50);
  }
  
  // 更新内容高度记录
  lastContentHeight.value = el.scrollHeight;
};

// 节流函数，避免频繁 DOM 操作
const restorePos = useThrottleFn(() => {
  const el = containerRef.value;
  if (!el || !allowScrollRestore.value) return;

  // 内容高度变化检测
  const contentHeightChanged = el.scrollHeight !== lastContentHeight.value;
  lastContentHeight.value = el.scrollHeight;

  // 若用户几乎贴底（<30px），直接滚到底；否则保持与底部的偏移量
  if (isNearBottom.value && contentHeightChanged) {
    // 使用scrollTo API代替直接设置scrollTop，更加稳定
    requestAnimationFrame(() => {
      el.scrollTo({
        top: el.scrollHeight,
        behavior: 'auto'
      });
    });
  } else if (contentHeightChanged && !isNearBottom.value) {
    // 如果不在底部，保持当前滚动位置
    el.scrollTop = savedScrollTop.value;
  }
}, 100);

function renderHtml(text) {
  if (!text) return '';
  // 使用marked解析Markdown为HTML
  const html = marked.parse(text, { 
    gfm: true, // GitHub风格Markdown
    breaks: true, // 允许回车换行
    sanitize: false // 不进行HTML转义，交给DOMPurify处理
  });
  
  // 使用DOMPurify清理HTML，防止XSS攻击
  return DOMPurify.sanitize(html, { ADD_TAGS: ['iframe'] });
}

// 监听虚拟列表数据变化（段落级）
watch(list, () => {
  const highlightAndRestore = () => {
    document.querySelectorAll('.markdown-body pre code').forEach(el => hljs.highlightElement(el));
    
    // 使用多重 RAF 确保在浏览器完成所有渲染后再恢复位置
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        requestAnimationFrame(restorePos);
      });
    });
  };
  nextTick(highlightAndRestore);
  bus.emit('outline-update', raw.value);
});

// 监听原始 markdown 字符串变化（字符级）
watch(raw, () => {
  nextTick(() => {
    // 使用多重 RAF 确保在浏览器完成所有渲染后再恢复位置
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        requestAnimationFrame(restorePos);
      });
    });
  });
});

// 组件挂载后初始化滚动容器状态
onMounted(() => {
  if (containerRef.value) {
    lastContentHeight.value = containerRef.value.scrollHeight;
  }
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

/* 关闭浏览器弹性滚动和滚动链，防止回弹 */
.markdown-scroll {
  overscroll-behavior: contain;
  -webkit-overflow-scrolling: auto;
  scroll-behavior: auto;
  -ms-overflow-style: none; /* IE/Edge */
  scrollbar-width: thin; /* Firefox */
  
  /* 防止滚动链和浏览器弹性行为 */
  overscroll-behavior-y: none;
  overscroll-behavior-x: none;
  
  /* 阻止滚动传播 */
  isolation: isolate;
  
  /* 确保滚动容器有自己的堆叠上下文 */
  position: relative;
  z-index: 1;
  
  /* 禁用滚动动画 */
  scroll-behavior: auto !important;
  
  /* 防止滚动链传播 */
  scroll-snap-type: none;
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