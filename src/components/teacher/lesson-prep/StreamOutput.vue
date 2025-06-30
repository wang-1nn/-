<template>
  <div class="flex flex-col h-full p-4 bg-white dark:bg-slate-800">
    <div class="flex items-center justify-between pb-2 mb-2 border-b border-slate-200 dark:border-slate-700">
      <h3 class="text-lg font-semibold text-slate-800 dark:text-slate-200">生成结果</h3>
      <div>
        <button @click="handleCopy" title="复制" class="p-1 text-slate-500 hover:text-indigo-600 dark:hover:text-indigo-400">
          <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" /></svg>
        </button>
        <button @click="handleStop" title="停止生成" class="p-1 text-slate-500 hover:text-red-600 dark:hover:text-red-400">
          <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 10h6v6H9z" /></svg>
        </button>
      </div>
    </div>
    <div ref="outputContainer" class="flex-1 overflow-y-auto prose max-w-none dark:prose-invert">
      <p v-if="!isStreaming && !streamingText">点击"生成教案"开始。</p>
      <div v-html="formattedText"></div>
      <div v-if="isStreaming" class="flex items-center">
        <span class="typing-cursor"></span>
        <span class="ml-2 text-sm text-slate-500">正在输入...</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue';
import { marked } from 'marked';

const streamingText = ref('');
const isStreaming = ref(false);
let streamInterval = null;
const outputContainer = ref(null);

const formattedText = computed(() => marked(streamingText.value));

const start = (messages) => {
  console.log("Starting stream with messages:", messages);
  isStreaming.value = true;
  streamingText.value = '';
  const fullResponse = `### 智能教学设计：第一章 课程介绍

**教学目标:**
1.  **知识与技能:** 学生能够清晰定义"人工智能"，并列举至少三个应用实例。
2.  **过程与方法:** 通过小组讨论，培养学生归纳总结和口头表达能力。
3.  **情感态度价值观:** 激发学生对前沿科技的兴趣，树立科学探索精神。

**教学重点:** 人工智能的核心概念（模拟、延伸、扩展人的智能）。
**教学难点:** 理解"智能"的多维度含义。`;
  
  let i = 0;
  streamInterval = setInterval(() => {
    if (i < fullResponse.length) {
      streamingText.value += fullResponse[i];
      i++;
      scrollToBottom();
    } else {
      stop();
    }
  }, 20);
};

const stop = () => {
  isStreaming.value = false;
  clearInterval(streamInterval);
};

const handleCopy = () => {
  navigator.clipboard.writeText(streamingText.value);
};

const handleStop = () => {
  stop();
};

const scrollToBottom = () => {
    nextTick(() => {
        if(outputContainer.value) {
            outputContainer.value.scrollTop = outputContainer.value.scrollHeight;
        }
    });
};

defineExpose({ start, stop });
</script>

<style scoped>
.prose :where(pre):not(:where([class~="not-prose"] *)) {
    background-color: #f3f4f6;
    color: #1f2937;
    border-radius: 0.375rem;
    padding: 1em;
}
.dark .prose :where(pre):not(:where([class~="not-prose"] *)) {
    background-color: #1f2937;
    color: #e5e7eb;
}
.typing-cursor {
  display: inline-block;
  width: 8px;
  height: 1.2em;
  background-color: #4f46e5;
  animation: blink 1s step-end infinite;
}

@keyframes blink {
  from, to {
    opacity: 1;
  }
  50% {
    opacity: 0;
  }
}
</style> 