<script setup>
import { ref, reactive, watch } from 'vue';

const props = defineProps({
  initialQuestion: {
    type: Object,
    default: () => ({
      type: 'single',
      content: '',
      options: [
        { id: 1, content: '' },
        { id: 2, content: '' },
        { id: 3, content: '' },
        { id: 4, content: '' }
      ],
      answer: [],
      analysis: '',
      score: 5,
      difficulty: 3
    })
  },
  isNew: {
    type: Boolean,
    default: true
  }
});

const emit = defineEmits(['save', 'cancel']);

// 题目类型选项
const questionTypes = [
  { value: 'single', label: '单选题' },
  { value: 'multiple', label: '多选题' },
  { value: 'true_false', label: '判断题' },
  { value: 'fill', label: '填空题' },
  { value: 'short', label: '简答题' }
];

// 克隆初始题目数据，避免直接修改props
const question = reactive({
  type: props.initialQuestion.type || 'single',
  content: props.initialQuestion.content || '',
  options: [...(props.initialQuestion.options || [])],
  answer: [...(props.initialQuestion.answer || [])],
  analysis: props.initialQuestion.analysis || '',
  score: props.initialQuestion.score || 5,
  difficulty: props.initialQuestion.difficulty || 3
});

// 表单校验
const errors = reactive({
  content: '',
  options: '',
  answer: ''
});

// 处理题目类型变化
watch(() => question.type, (newType) => {
  // 根据题目类型调整选项和答案
  if (newType === 'true_false') {
    question.options = [
      { id: 1, content: '正确' },
      { id: 2, content: '错误' }
    ];
  } else if (newType === 'fill' || newType === 'short') {
    question.options = [];
    question.answer = [''];
  } else if (newType === 'single') {
    if (question.options.length < 2) {
      question.options = [
        { id: 1, content: '' },
        { id: 2, content: '' },
        { id: 3, content: '' },
        { id: 4, content: '' }
      ];
    }
    question.answer = question.answer.slice(0, 1);
  } else if (newType === 'multiple') {
    if (question.options.length < 2) {
      question.options = [
        { id: 1, content: '' },
        { id: 2, content: '' },
        { id: 3, content: '' },
        { id: 4, content: '' }
      ];
    }
  }
});

// 添加选项
const addOption = () => {
  const maxId = Math.max(0, ...question.options.map(o => o.id));
  question.options.push({
    id: maxId + 1,
    content: ''
  });
};

// 删除选项
const removeOption = (index) => {
  question.options.splice(index, 1);
  // 如果删除的是已选择的答案，更新答案
  const optionId = question.options[index]?.id;
  if (optionId && question.answer.includes(optionId)) {
    question.answer = question.answer.filter(a => a !== optionId);
  }
};

// 切换选择题答案
const toggleAnswer = (optionId) => {
  if (question.type === 'single') {
    question.answer = [optionId];
  } else if (question.type === 'multiple') {
    const index = question.answer.indexOf(optionId);
    if (index >= 0) {
      question.answer.splice(index, 1);
    } else {
      question.answer.push(optionId);
    }
  }
};

// 判断选项是否被选中
const isOptionSelected = (optionId) => {
  return question.answer.includes(optionId);
};

// 检查表单是否有效
const validateForm = () => {
  let isValid = true;
  
  // 重置错误信息
  Object.keys(errors).forEach(key => errors[key] = '');
  
  // 校验题目内容
  if (!question.content.trim()) {
    errors.content = '题目内容不能为空';
    isValid = false;
  }
  
  // 校验选项（针对选择题和判断题）
  if (['single', 'multiple', 'true_false'].includes(question.type)) {
    if (question.options.length < 2) {
      errors.options = '至少需要两个选项';
      isValid = false;
    } else if (question.options.some(opt => !opt.content.trim())) {
      errors.options = '选项内容不能为空';
      isValid = false;
    }
    
    // 校验答案
    if (question.answer.length === 0) {
      errors.answer = '请选择正确答案';
      isValid = false;
    }
  }
  
  // 针对填空题和简答题的校验
  if (['fill', 'short'].includes(question.type)) {
    if (question.answer.length === 0 || !question.answer[0]) {
      errors.answer = '请填写答案';
      isValid = false;
    }
  }
  
  return isValid;
};

// 保存题目
const saveQuestion = () => {
  if (validateForm()) {
    emit('save', {...question});
  }
};

// 取消编辑
const cancelEdit = () => {
  emit('cancel');
};
</script>

<template>
  <div class="question-editor">
    <h3 class="editor-title">{{ isNew ? '添加新题目' : '编辑题目' }}</h3>
    
    <div class="form-group">
      <label>题目类型</label>
      <select v-model="question.type" class="form-control">
        <option v-for="type in questionTypes" :key="type.value" :value="type.value">
          {{ type.label }}
        </option>
      </select>
    </div>
    
    <div class="form-group">
      <label>题目内容</label>
      <textarea 
        v-model="question.content" 
        class="form-control" 
        :class="{ 'is-invalid': errors.content }"
        rows="3"
        placeholder="请输入题目内容"
      ></textarea>
      <div v-if="errors.content" class="error-message">{{ errors.content }}</div>
    </div>
    
    <!-- 选择题选项 -->
    <div v-if="['single', 'multiple', 'true_false'].includes(question.type)" class="options-section">
      <div class="section-header">
        <label>选项</label>
        <button 
          v-if="!['true_false'].includes(question.type)" 
          type="button" 
          class="add-option-btn"
          @click="addOption"
        >
          添加选项
        </button>
      </div>
      
      <div v-if="errors.options" class="error-message mb-2">{{ errors.options }}</div>
      
      <div 
        v-for="(option, index) in question.options" 
        :key="option.id" 
        class="option-item"
      >
        <div class="option-input">
          <div class="option-label">
            {{ String.fromCharCode(65 + index) }}
          </div>
          
          <input 
            v-model="option.content" 
            type="text" 
            class="form-control"
            :disabled="question.type === 'true_false'"
            placeholder="输入选项内容"
          />
          
          <button 
            v-if="!['true_false'].includes(question.type) && question.options.length > 2" 
            type="button"
            class="remove-option-btn"
            @click="removeOption(index)"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
        
        <div 
          class="answer-selector" 
          :class="{ 
            'selected': isOptionSelected(option.id),
            'single': question.type === 'single',
            'multiple': question.type === 'multiple'
          }"
          @click="toggleAnswer(option.id)"
        >
          <svg v-if="question.type === 'single'" xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
            <circle cx="10" cy="10" r="10" :fill="isOptionSelected(option.id) ? 'currentColor' : 'none'" />
          </svg>
          <svg v-else xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <rect x="4" y="4" width="16" height="16" rx="2" :stroke="isOptionSelected(option.id) ? 'none' : 'currentColor'" :fill="isOptionSelected(option.id) ? 'currentColor' : 'none'" />
            <path v-if="isOptionSelected(option.id)" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 11l3 3L16 9" stroke="white" />
          </svg>
        </div>
      </div>
      
      <div v-if="errors.answer" class="error-message mt-2">{{ errors.answer }}</div>
    </div>
    
    <!-- 填空题/简答题答案 -->
    <div v-if="['fill', 'short'].includes(question.type)" class="form-group">
      <label>标准答案</label>
      <textarea 
        v-model="question.answer[0]" 
        class="form-control" 
        :class="{ 'is-invalid': errors.answer }"
        rows="3"
        placeholder="请输入标准答案"
      ></textarea>
      <div v-if="errors.answer" class="error-message">{{ errors.answer }}</div>
    </div>
    
    <!-- 分析 -->
    <div class="form-group">
      <label>解析（可选）</label>
      <textarea 
        v-model="question.analysis" 
        class="form-control" 
        rows="3"
        placeholder="请输入题目解析（可选）"
      ></textarea>
    </div>
    
    <!-- 分数和难度 -->
    <div class="form-row">
      <div class="form-group half">
        <label>分数</label>
        <input 
          v-model.number="question.score" 
          type="number" 
          min="0" 
          step="0.5"
          class="form-control"
        />
      </div>
      
      <div class="form-group half">
        <label>难度</label>
        <div class="difficulty-slider">
          <input 
            v-model.number="question.difficulty" 
            type="range" 
            min="1" 
            max="5" 
            step="1"
          />
          <div class="difficulty-labels">
            <span>简单</span>
            <span>中等</span>
            <span>困难</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 操作按钮 -->
    <div class="actions">
      <button type="button" class="cancel-btn" @click="cancelEdit">取消</button>
      <button type="button" class="save-btn" @click="saveQuestion">保存</button>
    </div>
  </div>
</template>

<style scoped>
.question-editor {
  @apply bg-white rounded-xl p-6 shadow-sm max-w-3xl mx-auto;
}

.editor-title {
  @apply text-lg font-medium text-gray-800 mb-6;
}

.form-group {
  @apply mb-6;
}

.form-row {
  @apply flex flex-wrap gap-6;
}

.form-group.half {
  @apply flex-1 min-w-[200px];
}

label {
  @apply block text-sm font-medium text-gray-700 mb-1;
}

.form-control {
  @apply w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all duration-200;
}

.form-control.is-invalid {
  @apply border-red-500 focus:ring-red-500;
}

.error-message {
  @apply text-sm text-red-600 mt-1;
}

.section-header {
  @apply flex justify-between items-center mb-2;
}

.add-option-btn {
  @apply text-sm text-blue-600 hover:text-blue-800;
}

.options-section {
  @apply mb-6;
}

.option-item {
  @apply flex items-center mb-2 gap-2;
}

.option-input {
  @apply flex-1 flex items-center;
}

.option-label {
  @apply bg-gray-100 text-gray-700 h-9 w-9 flex items-center justify-center rounded-lg font-medium mr-2;
}

.remove-option-btn {
  @apply text-gray-400 hover:text-red-500 p-1.5;
}

.answer-selector {
  @apply flex items-center justify-center h-8 w-8 cursor-pointer text-gray-300 hover:text-gray-500;
}

.answer-selector.selected {
  @apply text-blue-600;
}

.difficulty-slider {
  @apply w-full;
}

.difficulty-slider input {
  @apply w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer;
}

.difficulty-slider input::-webkit-slider-thumb {
  @apply appearance-none w-5 h-5 bg-blue-600 rounded-full;
}

.difficulty-labels {
  @apply flex justify-between text-xs text-gray-500 mt-1;
}

.actions {
  @apply flex justify-end space-x-4 mt-8;
}

.cancel-btn {
  @apply px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors;
}

.save-btn {
  @apply px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors;
}
</style>