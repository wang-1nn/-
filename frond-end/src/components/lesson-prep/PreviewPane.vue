<template>
  <div class="flex flex-col h-full">
    <!-- 工具栏占位，可后续插入模板选择/生成按钮 -->
    <header class="flex items-center justify-between px-4 py-2 border-b bg-slate-50 sticky top-0 z-10">
      <slot name="toolbar" />
    </header>

    <!-- 内容布局区域 -->
    <div class="relative flex-1 flex overflow-hidden">
      <!-- 悬浮的布局选择器 -->
      <div v-if="layout === 'document' || layout === 'split'" class="absolute top-8 right-8 z-20 bg-white/80 backdrop-blur-sm p-1.5 rounded-xl shadow-lg border border-slate-200/50">
        <div class="flex items-center space-x-2 text-sm">
          <div class="flex border border-slate-200 rounded-md overflow-hidden">
            <div
                class="px-2 py-1 cursor-pointer"
                :class="[layout === 'document' ? 'bg-indigo-600 text-white shadow-inner' : 'bg-white text-slate-600 hover:bg-slate-50']"
                @click="setLayout('document')"
                title="文档视图"
            >
              <el-icon><Document /></el-icon>
            </div>
            <div
                class="px-2 py-1 cursor-pointer"
                :class="[layout === 'mindmap' ? 'bg-indigo-600 text-white shadow-inner' : 'bg-white text-slate-600 hover:bg-slate-50']"
                @click="setLayout('mindmap')"
                title="思维导图视图"
            >
              <el-icon><Connection /></el-icon>
            </div>
            <div
                class="px-2 py-1 cursor-pointer"
                :class="[layout === 'split' ? 'bg-indigo-600 text-white shadow-inner' : 'bg-white text-slate-600 hover:bg-slate-50']"
                @click="setLayout('split')"
                title="拆分视图"
            >
              <el-icon><CopyDocument /></el-icon>
            </div>
          </div>

          <el-button
              v-if="raw.length > 0"
              type="success"
              size="small"
              plain
              :loading="isEvaluating"
              @click="evaluateContent"
          >
            <el-icon class="mr-1"><CircleCheck /></el-icon>
            智能评估
          </el-button>
        </div>
      </div>

      <!-- 文档视图 -->
      <div
          v-if="layout === 'document' || layout === 'split'"
          ref="containerRef"
          @scroll="handleScroll"
          class="relative flex-1 my-4 rounded-2xl bg-white/95 border border-slate-200 shadow-lg backdrop-blur overflow-y-auto markdown-scroll"
          :class="{'w-1/2 pr-2': layout === 'split'}"
      >
        <div v-if="loading" class="absolute inset-0 flex items-center justify-center bg-white/60 z-10">
          <el-icon class="is-loading text-3xl text-indigo-500"><Loading /></el-icon>
        </div>
        <div v-html="renderedHtml" class="markdown-body p-4" />
      </div>

      <!-- 思维导图视图 -->
      <div
          v-if="layout === 'mindmap' || layout === 'split'"
          class="flex-1 overflow-hidden my-4 rounded-2xl bg-white/95 border border-slate-200 shadow-lg backdrop-blur"
          :class="{'w-1/2 pl-2': layout === 'split'}"
      >
        <MindMapView
            v-if="parsedOutline.length > 0"
            :data="parsedOutline"
            class="h-full"
        />
        <div v-else class="h-full flex items-center justify-center text-slate-400">
          <div class="text-center">
            <el-icon class="text-3xl mb-2"><Connection /></el-icon>
            <p>思维导图视图将在内容解析后显示</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 内容评估结果抽屉 -->
    <el-drawer
        v-model="showEvaluationDrawer"
        title="内容智能评估结果"
        size="30%"
        destroy-on-close
    >
      <div class="p-4">
        <div v-if="isEvaluating" class="flex flex-col items-center justify-center py-8">
          <el-progress type="circle" :percentage="evaluationProgress"></el-progress>
          <p class="mt-4 text-slate-500">正在分析内容，请稍候...</p>
        </div>

        <template v-else>
          <!-- 总体评分 -->
          <div class="mb-6">
            <h3 class="text-lg font-medium text-slate-700 mb-2">总体评分</h3>
            <div class="flex items-center">
              <div class="text-4xl font-bold text-indigo-600 mr-4">{{ evaluation.score }}/100</div>
              <el-rate v-model="evaluation.rating" disabled show-score></el-rate>
            </div>
          </div>

          <!-- 评估维度 -->
          <div class="mb-6">
            <h3 class="text-lg font-medium text-slate-700 mb-2">评估维度</h3>
            <div class="space-y-3">
              <div v-for="(item, index) in evaluation.dimensions" :key="index" class="bg-slate-50 p-3 rounded-lg">
                <div class="flex justify-between items-center mb-1">
                  <span class="font-medium">{{ item.name }}</span>
                  <span class="text-sm" :class="getScoreColor(item.score)">{{ item.score }}/10</span>
                </div>
                <el-progress
                    :percentage="item.score * 10"
                    :stroke-width="8"
                    :color="getScoreColor(item.score)"
                ></el-progress>
              </div>
            </div>
          </div>

          <!-- 改进建议 -->
          <div class="mb-6">
            <h3 class="text-lg font-medium text-slate-700 mb-2">改进建议</h3>
            <div class="bg-slate-50 p-4 rounded-lg">
              <ul class="list-disc pl-5 space-y-2">
                <li v-for="(suggestion, index) in evaluation.suggestions" :key="index" class="text-slate-700">
                  {{ suggestion }}
                </li>
              </ul>
            </div>
          </div>

          <!-- 亮点 -->
          <div>
            <h3 class="text-lg font-medium text-slate-700 mb-2">内容亮点</h3>
            <div class="bg-slate-50 p-4 rounded-lg">
              <ul class="list-disc pl-5 space-y-2">
                <li v-for="(highlight, index) in evaluation.highlights" :key="index" class="text-slate-700">
                  {{ highlight }}
                </li>
              </ul>
            </div>
          </div>
        </template>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onUnmounted, onMounted } from 'vue';
import { marked } from 'marked';
import DOMPurify from 'dompurify';
import hljs from 'highlight.js';
import { bus } from '@/utils/eventBus';
import MindMapView from '@/components/lesson-prep/MindMapView.vue';
import { Document, Connection, CopyDocument, CircleCheck, Loading, Warning } from '@element-plus/icons-vue';

// --- 核心状态 ---
const raw = ref('');
const loading = ref(false);
const isGeneratingLesson = ref(false); // 标识是否正在生成教案
let typingTimer = null;
let queue = '';

// --- 滚动管理 ---
const containerRef = ref(null);
const isNearBottom = ref(true);

// --- 渲染逻辑 ---
const renderedHtml = computed(() => {
  const html = marked.parse(raw.value, {
    gfm: true,
    breaks: true,
    sanitize: false
  });
  return DOMPurify.sanitize(html, { ADD_TAGS: ['iframe'] });
});

// --- 监听与钩子 ---
watch(raw, () => {
  // 自动滚动
  if (isNearBottom.value) {
    nextTick(() => {
      if (containerRef.value) {
        containerRef.value.scrollTop = containerRef.value.scrollHeight;
      }
    });
  }
  // 更新代码高亮
  nextTick(() => {
    document.querySelectorAll('.markdown-body pre code').forEach(el => hljs.highlightElement(el));
  });
  bus.emit('outline-update', raw.value);
});

onMounted(() => {
  isNearBottom.value = true;
});

onUnmounted(() => {
  clearInterval(typingTimer);
});

// --- 方法 ---
const handleScroll = () => {
  const el = containerRef.value;
  if (!el) return;
  isNearBottom.value = el.scrollHeight - el.scrollTop - el.clientHeight < 30;
};

// --- 布局与UI ---
const layout = ref('document');
function setLayout(newLayout) {
  layout.value = newLayout;
}

// --- 内容评估 ---
const isEvaluating = ref(false);
const evaluationProgress = ref(0);
const showEvaluationDrawer = ref(false);
const evaluation = ref({
  score: 0,
  rating: 0,
  dimensions: [
    { name: '结构完整性', score: 0 },
    { name: '内容丰富度', score: 0 },
    { name: '教学逻辑性', score: 0 },
    { name: '目标明确性', score: 0 },
    { name: '资源丰富性', score: 0 }
  ],
  suggestions: [],
  highlights: []
});

function evaluateContent() {
  if (isEvaluating.value || !raw.value) return;
  isEvaluating.value = true;
  evaluationProgress.value = 0;
  showEvaluationDrawer.value = true;
  const progressInterval = setInterval(() => {
    evaluationProgress.value += 5;
    if (evaluationProgress.value >= 100) {
      clearInterval(progressInterval);
      completeEvaluation();
    }
  }, 100);
}

function completeEvaluation() {
  const content = raw.value.toLowerCase();
  const totalScore = calculateContentScore(content);
  evaluation.value = {
    score: totalScore,
    rating: Math.round(totalScore / 20),
    dimensions: [
      { name: '结构完整性', score: calculateStructureScore(content) },
      { name: '内容丰富度', score: calculateContentRichness(content) },
      { name: '教学逻辑性', score: calculateLogicScore(content) },
      { name: '目标明确性', score: calculateGoalScore(content) },
      { name: '资源丰富性', score: calculateResourceScore(content) }
    ],
    suggestions: generateSuggestions(content),
    highlights: generateHighlights(content)
  };
  isEvaluating.value = false;
}

function getScoreColor(score) {
  if (score >= 9) return 'text-green-500';
  if (score >= 7) return 'text-blue-500';
  if (score >= 5) return 'text-orange-500';
  return 'text-red-500';
}

// (此处省略了所有计分和建议的纯函数，它们不与核心逻辑交互)
function calculateContentScore(content) { let score = 70; if (content.includes('教学目标') && content.includes('教学重点') && content.includes('教学难点')) { score += 5; } if (content.includes('导入') && content.includes('讲授') && content.includes('巩固')) { score += 5; } const contentLength = content.length; if (contentLength > 2000) score += 5; if (contentLength > 4000) score += 5; if (content.includes('视频') || content.includes('图片') || content.includes('音频')) { score += 3; } if (content.includes('讨论') || content.includes('互动') || content.includes('小组')) { score += 4; } if (content.includes('评价') || content.includes('评估') || content.includes('测验')) { score += 3; } return Math.min(Math.max(score, 0), 100); }
function calculateStructureScore(content) { let score = 5; if (content.includes('教学目标')) score += 1; if (content.includes('教学重点')) score += 1; if (content.includes('教学难点')) score += 1; if (content.includes('教学过程')) score += 1; if (content.includes('总结') || content.includes('作业')) score += 1; return score; }
function calculateContentRichness(content) { const contentLength = content.length; let score = 3; if (contentLength > 1000) score += 1; if (contentLength > 2000) score += 1; if (contentLength > 3000) score += 1; if (contentLength > 4000) score += 1; if (content.includes('案例') || content.includes('示例') || content.includes('实例')) score += 3; return Math.min(score, 10); }
function calculateLogicScore(content) { let score = 6; if (content.includes('导入') && content.includes('讲授') && content.includes('巩固')) { score += 2; } if (content.includes('首先') && content.includes('其次') && content.includes('最后')) { score += 1; } if (content.includes('分析') && content.includes('综合')) { score += 1; } return Math.min(score, 10); }
function calculateGoalScore(content) { let score = 5; if (content.includes('知识目标')) score += 1; if (content.includes('能力目标')) score += 1; if (content.includes('情感目标')) score += 1; if (content.includes('掌握') || content.includes('理解') || content.includes('应用')) score += 2; return Math.min(score, 10); }
function calculateResourceScore(content) { let score = 4; if (content.includes('教材') || content.includes('课本')) score += 1; if (content.includes('视频') || content.includes('音频')) score += 1; if (content.includes('图片') || content.includes('图像')) score += 1; if (content.includes('幻灯片') || content.includes('ppt')) score += 1; if (content.includes('网络资源') || content.includes('在线')) score += 1; if (content.includes('实验') || content.includes('实践')) score += 1; return Math.min(score, 10); }
function generateSuggestions(content) { const suggestions = []; if (!content.includes('教学目标')) { suggestions.push('建议明确设置教学目标，包括知识目标、能力目标和情感目标'); } if (!content.includes('小组') && !content.includes('讨论') && !content.includes('互动')) { suggestions.push('可增加互动环节，如小组讨论、学生参与等，提高课堂参与度'); } if (!content.includes('评价') && !content.includes('评估')) { suggestions.push('建议添加多元化的评价方式，包括形成性评价和终结性评价'); } if (!content.includes('视频') && !content.includes('图片') && !content.includes('音频')) { suggestions.push('可丰富多媒体教学资源，如视频、图片等，增强教学效果'); } if (!content.includes('作业') && !content.includes('练习')) { suggestions.push('建议设计巩固练习或课后作业，帮助学生消化所学知识'); } return suggestions.length > 0 ? suggestions : ['内容已经很完善，可以尝试增加更多的实例和案例分析']; }
function generateHighlights(content) { const highlights = []; if (content.includes('教学目标') && content.includes('知识目标') && content.includes('能力目标')) { highlights.push('教学目标设置全面，包含知识、能力和情感维度'); } if (content.includes('小组') || content.includes('讨论') || content.includes('互动')) { highlights.push('设计了丰富的互动环节，有助于提高学生参与度和学习兴趣'); } if (content.includes('多媒体') || content.includes('视频') || content.includes('图片')) { highlights.push('教学资源丰富多样，融合多种媒体形式'); } if (content.includes('评价') || content.includes('评估')) { highlights.push('包含完整的评价体系，有助于全面了解学生学习情况'); } if (content.includes('导入') && content.includes('讲授') && content.includes('巩固')) { highlights.push('教学流程设计合理，环节衔接自然'); } return highlights.length > 0 ? highlights : ['内容结构清晰，教学设计完整']; }


// --- 思维导图 ---
const parsedOutline = ref([]);
function parseContentToOutline() {
  try {
    const lines = raw.value.split('\n');
    const result = [];
    const stack = [{ children: result }];
    lines.forEach(line => {
      const match = line.match(/^(#{1,6})\s+(.+)$/);
      if (match) {
        const level = match[1].length;
        const text = match[2].trim();
        const node = { label: text, children: [] };
        while (stack.length > level) {
          stack.pop();
        }
        while (stack.length < level) {
          const parent = stack[stack.length - 1];
          const newParent = { children: [] };
          parent.children.push(newParent);
          stack.push(newParent);
        }
        stack[level - 1].children.push(node);
        stack[level] = node;
      }
    });
    parsedOutline.value = transformToMindMap(result);
  } catch (error) {
    console.error('解析大纲失败:', error);
    parsedOutline.value = [];
  }
}

function transformToMindMap(nodes) {
  if (!nodes || nodes.length === 0) return [];
  const rootNode = { id: '0', label: '教案结构', children: [] };
  function processChildren(children, parentId = '0') {
    return children.map((node, index) => {
      const id = `${parentId}-${index}`;
      return {
        id,
        label: node.label || '未命名节点',
        children: node.children && node.children.length > 0 ? processChildren(node.children, id) : []
      };
    });
  }
  rootNode.children = processChildren(nodes);
  return [rootNode];
}

// --- 事件总线处理 ---
bus.on('md-ready', (md) => {
  if (!md) return;
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
      parseContentToOutline();
    }
  }, 6);
});

bus.on('preview-loading', (v) => {
  loading.value = v;
});

// 处理优化大纲的事件
bus.on('outline-ready', (content) => {
  if (!content) return;
  raw.value = '';
  const full = content;
  let idx = 0;
  clearInterval(typingTimer);
  typingTimer = setInterval(() => {
    raw.value += full[idx] || '';
    idx++;
    if (idx >= full.length) {
      clearInterval(typingTimer);
      typingTimer = null;
      parseContentToOutline();
    }
  }, 6);
});

bus.on('outline-chunk', (chunk) => {
  if (!chunk) return;

  // 确保chunk是字符串
  const content = typeof chunk === 'string' ? chunk : (chunk.content || '');
  if (!content) return;

  queue += content;
  const startTyping = () => {
    if (typingTimer) return;
    typingTimer = setInterval(() => {
      if (!queue.length) {
        clearInterval(typingTimer);
        typingTimer = null;
        parseContentToOutline();
        return;
      }

      // 检查是否在HTML标签中间，避免分割标签
      let nextChar = queue[0];

      // 如果当前字符是 '<'，尝试读取完整的标签
      if (nextChar === '<') {
        const tagEndIndex = queue.indexOf('>');
        if (tagEndIndex > 0) {
          // 一次性添加完整的标签
          const fullTag = queue.slice(0, tagEndIndex + 1);
          queue = queue.slice(tagEndIndex + 1);
          raw.value += fullTag;
          return;
        }
      }

      // 如果在标签内部，等待标签完成
      if (raw.value.lastIndexOf('<') > raw.value.lastIndexOf('>')) {
        const tagEndIndex = queue.indexOf('>');
        if (tagEndIndex >= 0) {
          const tagRemainder = queue.slice(0, tagEndIndex + 1);
          queue = queue.slice(tagEndIndex + 1);
          raw.value += tagRemainder;
          return;
        } else {
          // 等待更多内容
          return;
        }
      }

      // 正常添加单个字符
      queue = queue.slice(1);
      raw.value += nextChar;
    }, 6);
  };
  startTyping();
});

// 处理生成教案的事件
bus.on('lesson-ready', (content) => {
  if (!content) return;
  raw.value = '';
  const full = content;
  let idx = 0;
  clearInterval(typingTimer);
  typingTimer = setInterval(() => {
    raw.value += full[idx] || '';
    idx++;
    if (idx >= full.length) {
      clearInterval(typingTimer);
      typingTimer = null;
      parseContentToOutline();
    }
  }, 6);
});

bus.on('lesson-chunk', (chunk) => {
  if (!chunk) return;

  // 确保chunk是字符串
  const content = typeof chunk === 'string' ? chunk : (chunk.content || '');
  if (!content) return;

  // 如果是第一个教案块，清空之前的大纲内容
  if (!isGeneratingLesson.value) {
    isGeneratingLesson.value = true;
    raw.value = ''; // 清空大纲内容
    queue = ''; // 清空队列
  }

  queue += content;
  const startTyping = () => {
    if (typingTimer) return;
    typingTimer = setInterval(() => {
      if (!queue.length) {
        clearInterval(typingTimer);
        typingTimer = null;
        parseContentToOutline();
        return;
      }
      // 检查是否在HTML标签中间，避免分割标签
      let nextChar = queue[0];

      // 如果当前字符是 '<'，尝试读取完整的标签
      if (nextChar === '<') {
        const tagEndIndex = queue.indexOf('>');
        if (tagEndIndex > 0) {
          // 一次性添加完整的标签
          const fullTag = queue.slice(0, tagEndIndex + 1);
          queue = queue.slice(tagEndIndex + 1);
          raw.value += fullTag;
          return;
        }
      }

      // 如果在标签内部，等待标签完成
      if (raw.value.lastIndexOf('<') > raw.value.lastIndexOf('>')) {
        const tagEndIndex = queue.indexOf('>');
        if (tagEndIndex >= 0) {
          const tagRemainder = queue.slice(0, tagEndIndex + 1);
          queue = queue.slice(tagEndIndex + 1);
          raw.value += tagRemainder;
          return;
        } else {
          // 等待更多内容
          return;
        }
      }

      // 正常添加单个字符
      queue = queue.slice(1);
      raw.value += nextChar;
    }, 6);
  };
  startTyping();
});

// 处理大纲完成事件
bus.on('outline-complete', () => {
  if (typingTimer) {
    clearInterval(typingTimer);
    typingTimer = null;
  }
  parseContentToOutline();
});

// 处理教案完成事件
bus.on('lesson-complete', () => {
  if (typingTimer) {
    clearInterval(typingTimer);
    typingTimer = null;
  }
  isGeneratingLesson.value = false;
  parseContentToOutline();
});

bus.on('md-chunk', (chunk) => {
  if (!chunk) return;
  queue += chunk;
  const startTyping = () => {
    if (typingTimer) return;
    typingTimer = setInterval(() => {
      if (!queue.length) {
        clearInterval(typingTimer);
        typingTimer = null;
        parseContentToOutline();
        return;
      }
      const ch = queue[0];
      queue = queue.slice(1);
      raw.value += ch;
    }, 6);
  };
  startTyping();
  nextTick(() => {
    document.querySelectorAll('.markdown-body pre code').forEach(el => hljs.highlightElement(el));
  });
});

</script>

<style scoped>
/* 预览区域样式 */
.markdown-scroll {
  scrollbar-width: thin;
  scrollbar-color: #cbd5e1 #f1f5f9;
}

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

/* 优化后的 Markdown 样式 */
:deep(.markdown-body) {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, 'Noto Sans', sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol', 'Noto Color Emoji';
  font-size: 16px;
  line-height: 1.7;
  color: #333;
  background-color: #fff;
  padding: 2rem;
  word-wrap: break-word;
}

:deep(.markdown-body h1),
:deep(.markdown-body h2),
:deep(.markdown-body h3),
:deep(.markdown-body h4),
:deep(.markdown-body h5),
:deep(.markdown-body h6) {
  margin-top: 1.5rem;
  margin-bottom: 1rem;
  font-weight: 600;
  line-height: 1.25;
  color: #111;
  border-bottom: 1px solid #eaecef;
  padding-bottom: 0.3em;
}

:deep(.markdown-body h1) { font-size: 2em; }
:deep(.markdown-body h2) { font-size: 1.5em; }
:deep(.markdown-body h3) { font-size: 1.25em; }
:deep(.markdown-body h4) { font-size: 1em; }

:deep(.markdown-body p) {
  margin-bottom: 1rem;
}

:deep(.markdown-body a) {
  color: #4f46e5;
  text-decoration: none;
  transition: color 0.2s ease-in-out;
}

:deep(.markdown-body a:hover) {
  color: #3730a3;
  text-decoration: underline;
}

:deep(.markdown-body ul),
:deep(.markdown-body ol) {
  margin-bottom: 1rem;
  padding-left: 2em;
}

:deep(.markdown-body li > p) {
  margin-top: 0.25rem;
}

:deep(.markdown-body blockquote) {
  margin: 0 0 1rem;
  padding: 0.5rem 1rem;
  color: #666;
  background-color: #f8f9fa;
  border-left: 0.25em solid #4f46e5;
}

:deep(.markdown-body pre) {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, Courier, monospace;
  padding: 1rem;
  margin-bottom: 1rem;
  overflow: auto;
  line-height: 1.45;
  background-color: #f8f9fa;
  border-radius: 6px;
  color: #333;
}

:deep(.markdown-body code) {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, Courier, monospace;
  font-size: 85%;
  padding: 0.2em 0.4em;
  margin: 0;
  background-color: rgba(27, 31, 35, 0.05);
  border-radius: 3px;
}

:deep(.markdown-body pre > code) {
  padding: 0;
  margin: 0;
  font-size: 100%;
  word-break: normal;
  white-space: pre;
  background: transparent;
  border: 0;
}

:deep(.markdown-body hr) {
  height: 0.25em;
  padding: 0;
  margin: 24px 0;
  background-color: #e1e4e8;
  border: 0;
}

:deep(.markdown-body table) {
  width: 100%;
  margin-bottom: 1rem;
  border-collapse: collapse;
}

:deep(.markdown-body th),
:deep(.markdown-body td) {
  padding: 0.75rem;
  border: 1px solid #dfe2e5;
}

:deep(.markdown-body th) {
  font-weight: 600;
  background-color: #f8f9fa;
}

/* 平滑过渡效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>