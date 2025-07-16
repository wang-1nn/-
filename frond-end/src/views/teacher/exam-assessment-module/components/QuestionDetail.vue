<script setup>
import { defineProps } from 'vue';

const props = defineProps({
  question: {
    type: Object,
    required: true
  },
  showAnswer: {
    type: Boolean,
    default: false
  },
  showAnalysis: {
    type: Boolean,
    default: false
  }
});

// 获取题目类型文本
const getQuestionTypeText = (type) => {
  const types = {
    single: '单选题',
    multiple: '多选题',
    true_false: '判断题',
    fill: '填空题',
    short: '简答题'
  };
  return types[type] || '未知类型';
};

// 获取难度文本
const getDifficultyText = (difficulty) => {
  const levels = ['', '简单', '较简单', '中等', '较难', '困难'];
  return levels[difficulty] || '中等';
};

// 检查选项是否是答案
const isAnswer = (optionId) => {
  return props.question.answer.includes(optionId);
};

// 获取选项标签 (A, B, C...)
const getOptionLabel = (index) => {
  return String.fromCharCode(65 + index);
};
</script>

<template>
  <div class="question-detail">
    <div class="question-header">
      <div class="question-type">{{ getQuestionTypeText(question.type) }}</div>
      <div class="question-meta">
        <span class="score">{{ question.score }}分</span>
        <span class="difficulty-badge" :class="`level-${question.difficulty}`">
          {{ getDifficultyText(question.difficulty) }}
        </span>
      </div>
    </div>
    
    <div class="question-content" v-html="question.content"></div>
    
    <!-- 选择题选项 -->
    <div v-if="['single', 'multiple', 'true_false'].includes(question.type)" class="question-options">
      <div 
        v-for="(option, index) in question.options" 
        :key="option.id"
        class="option-item"
        :class="{ 
          'is-answer': showAnswer && isAnswer(option.id),
          'not-answer': showAnswer && !isAnswer(option.id)
        }"
      >
        <div class="option-label">{{ getOptionLabel(index) }}</div>
        <div class="option-content" v-html="option.content"></div>
        <div v-if="showAnswer && isAnswer(option.id)" class="answer-icon">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
          </svg>
        </div>
      </div>
    </div>
    
    <!-- 填空题和简答题答案 -->
    <div v-if="showAnswer && ['fill', 'short'].includes(question.type)" class="answer-section">
      <div class="section-title">标准答案</div>
      <div class="answer-content" v-html="question.answer[0]"></div>
    </div>
    
    <!-- 解析 -->
    <div v-if="showAnalysis && question.analysis" class="analysis-section">
      <div class="section-title">解析</div>
      <div class="analysis-content" v-html="question.analysis"></div>
    </div>
  </div>
</template>

<style scoped>
.question-detail {
  @apply bg-white rounded-lg border border-gray-100 p-4 mb-4;
}

.question-header {
  @apply flex justify-between items-center mb-3;
}

.question-type {
  @apply px-3 py-1 bg-blue-100 text-blue-700 text-sm font-medium rounded-full;
}

.question-meta {
  @apply flex items-center gap-3;
}

.score {
  @apply text-sm font-medium text-gray-700;
}

.difficulty-badge {
  @apply px-2 py-0.5 text-xs font-medium rounded-full;
}

.level-1 {
  @apply bg-green-100 text-green-700;
}

.level-2 {
  @apply bg-green-50 text-green-600;
}

.level-3 {
  @apply bg-yellow-100 text-yellow-700;
}

.level-4 {
  @apply bg-orange-100 text-orange-700;
}

.level-5 {
  @apply bg-red-100 text-red-700;
}

.question-content {
  @apply text-gray-800 mb-4;
}

.question-options {
  @apply space-y-2 mb-4;
}

.option-item {
  @apply flex items-center p-2 rounded-lg border border-transparent hover:bg-gray-50 transition-colors;
}

.option-item.is-answer {
  @apply bg-green-50 border-green-200 text-green-800;
}

.option-item.not-answer {
  @apply text-gray-500;
}

.option-label {
  @apply h-7 w-7 flex items-center justify-center bg-gray-100 rounded-full mr-3 text-sm font-medium text-gray-700;
}

.option-item.is-answer .option-label {
  @apply bg-green-200 text-green-800;
}

.option-content {
  @apply flex-1;
}

.answer-icon {
  @apply text-green-600;
}

.section-title {
  @apply text-sm font-medium text-gray-700 mb-2;
}

.answer-section, .analysis-section {
  @apply mt-4 pt-4 border-t border-gray-100;
}

.answer-content {
  @apply bg-green-50 p-3 rounded-lg text-green-800;
}

.analysis-content {
  @apply bg-blue-50 p-3 rounded-lg text-blue-800;
}
</style>