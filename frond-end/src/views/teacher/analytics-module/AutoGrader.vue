<script setup>
import { ref, reactive, computed, onMounted } from 'vue';

// 状态变量
const loading = ref(false);
const scoring = ref(false);
const selectedExam = ref('');
const selectedQuestionType = ref('all');
const selectedStatus = ref('pending');
const answers = ref([]);
const currentAnswerIndex = ref(0);

// 模拟的考试数据
const exams = ref([
  { id: '1', name: '期中考试：JavaScript基础' },
  { id: '2', name: '期末考试：Vue.js实战' },
  { id: '3', name: '单元测试：HTML与CSS' }
]);

// 题型过滤器
const questionTypes = [
  { id: 'all', name: '全部题型' },
  { id: 'short', name: '简答题' },
  { id: 'essay', name: '论述题' },
  { id: 'program', name: '编程题' }
];

// 状态过滤器
const statusFilters = [
  { id: 'pending', name: '待评分' },
  { id: 'graded', name: '已评分' },
  { id: 'all', name: '全部' }
];

// 分页配置
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
});

// 当前答案
const currentAnswer = computed(() => {
  return answers.value[currentAnswerIndex.value] || null;
});

// 是否有上一个答案
const hasPrevAnswer = computed(() => {
  return currentAnswerIndex.value > 0;
});

// 是否有下一个答案
const hasNextAnswer = computed(() => {
  return currentAnswerIndex.value < answers.value.length - 1;
});

// 初始化
onMounted(() => {
  // 默认选择第一个考试
  if (exams.value.length > 0) {
    selectedExam.value = exams.value[0].id;
    fetchAnswers();
  }
});

// 获取待评分答案
const fetchAnswers = async () => {
  loading.value = true;
  
  try {
    // 模拟API延迟
    await new Promise(resolve => setTimeout(resolve, 800));
    
    // 模拟数据
    const mockAnswers = [];
    
    // 生成20条模拟答案
    for (let i = 0; i < 20; i++) {
      const questionType = ['short', 'essay', 'program'][Math.floor(Math.random() * 3)];
      const status = i < 15 ? 'pending' : 'graded';
      
      mockAnswers.push({
        id: `ans_${i + 1}`,
        studentId: `S${10001 + i}`,
        studentName: `学生${10001 + i}`,
        questionId: `q_${Math.floor(i / 5) + 1}`,
        questionTitle: getQuestionTitle(questionType, Math.floor(i / 5) + 1),
        questionType,
        answer: getRandomAnswer(questionType),
        status,
        score: status === 'graded' ? Math.floor(Math.random() * 11) : null,
        maxScore: 10,
        feedback: status === 'graded' ? getRandomFeedback() : '',
        suggestedScore: Math.floor(Math.random() * 11),
        suggestedFeedback: getRandomFeedback(),
        similarity: Math.floor(Math.random() * 21) + 80 // 80% - 100%相似度
      });
    }
    
    // 应用过滤器
    const filteredAnswers = mockAnswers.filter(answer => {
      let match = true;
      
      if (selectedQuestionType.value !== 'all') {
        match = match && answer.questionType === selectedQuestionType.value;
      }
      
      if (selectedStatus.value !== 'all') {
        match = match && answer.status === selectedStatus.value;
      }
      
      return match;
    });
    
    answers.value = filteredAnswers;
    pagination.total = filteredAnswers.length;
    currentAnswerIndex.value = 0;
    
  } catch (error) {
    console.error('获取答案失败', error);
    // 错误处理
  } finally {
    loading.value = false;
  }
};

// 考试变更处理
const handleExamChange = () => {
  fetchAnswers();
};

// 题型变更处理
const handleQuestionTypeChange = () => {
  fetchAnswers();
};

// 状态变更处理
const handleStatusChange = () => {
  fetchAnswers();
};

// 上一个答案
const prevAnswer = () => {
  if (currentAnswerIndex.value > 0) {
    currentAnswerIndex.value--;
  }
};

// 下一个答案
const nextAnswer = () => {
  if (currentAnswerIndex.value < answers.value.length - 1) {
    currentAnswerIndex.value++;
  }
};

// 使用AI评分
const autoScore = async () => {
  if (!currentAnswer.value) return;
  
  scoring.value = true;
  
  try {
    // 模拟API延迟
    await new Promise(resolve => setTimeout(resolve, 1500));
    
    // 使用建议的评分和反馈
    currentAnswer.value.score = currentAnswer.value.suggestedScore;
    currentAnswer.value.feedback = currentAnswer.value.suggestedFeedback;
    currentAnswer.value.status = 'graded';
    
    // 自动进入下一题
    if (hasNextAnswer.value) {
      nextAnswer();
    }
  } catch (error) {
    console.error('自动评分失败', error);
    // 错误处理
  } finally {
    scoring.value = false;
  }
};

// 保存评分
const saveScore = async () => {
  if (!currentAnswer.value || !currentAnswer.value.score) return;
  
  const prevStatus = currentAnswer.value.status;
  currentAnswer.value.status = 'graded';
  
  try {
    // 模拟API延迟
    await new Promise(resolve => setTimeout(resolve, 500));
    
    // 保存成功后，如果是从待评分变为已评分，且选择的是待评分过滤器，则移除当前答案
    if (prevStatus === 'pending' && selectedStatus.value === 'pending') {
      const currentId = currentAnswer.value.id;
      const nextIndex = hasNextAnswer.value ? currentAnswerIndex.value : currentAnswerIndex.value - 1;
      
      answers.value = answers.value.filter(a => a.id !== currentId);
      pagination.total = answers.value.length;
      
      // 调整当前索引
      currentAnswerIndex.value = Math.max(0, Math.min(nextIndex, answers.value.length - 1));
    }
  } catch (error) {
    console.error('保存评分失败', error);
    // 错误处理
  }
};

// 生成随机答案内容
function getRandomAnswer(type) {
  switch (type) {
    case 'short':
      return '简答题答案：JavaScript是一种高级的、解释型的编程语言，它是Web的三大核心技术之一（HTML、CSS、JavaScript）。JavaScript可以使网页具有交互性，能够对用户的操作作出响应，并且可以动态更新网页内容。';
    
    case 'essay':
      return '论述题答案：Vue.js是一套用于构建用户界面的渐进式框架，与其它大型框架不同的是，Vue被设计为可以自底向上逐层应用。Vue的核心库只关注视图层，不仅易于上手，还便于与第三方库或既有项目整合。\n\n使用Vue.js的主要优势包括：\n1. 响应式数据绑定：当数据变化时，视图会自动更新。\n2. 组件化开发：可以构建可复用的UI组件。\n3. 虚拟DOM：提高渲染性能。\n4. 轻量级：相比Angular和React，Vue更小巧灵活。\n5. 丰富的生态系统：Vuex、Vue Router等官方库为复杂应用提供支持。\n\n总结来说，Vue.js适合各种规模的项目，从小型单页应用到复杂的企业级应用都能胜任。';
    
    case 'program':
      return '编程题答案：\n```javascript\nfunction bubbleSort(arr) {\n  const len = arr.length;\n  \n  for(let i = 0; i < len; i++) {\n    for(let j = 0; j < len - 1 - i; j++) {\n      // 如果前一个元素大于后一个元素，则交换它们\n      if(arr[j] > arr[j+1]) {\n        const temp = arr[j];\n        arr[j] = arr[j+1];\n        arr[j+1] = temp;\n      }\n    }\n  }\n  \n  return arr;\n}\n\n// 测试代码\nconst testArray = [5, 3, 8, 4, 2];\nconsole.log(bubbleSort(testArray));\n```';
    
    default:
      return '未知题型答案';
  }
}

// 生成题目标题
function getQuestionTitle(type, num) {
  const types = {
    'short': '简答题',
    'essay': '论述题',
    'program': '编程题'
  };
  
  const topics = {
    'short': ['JavaScript基础概念', 'HTML5新特性', 'CSS选择器优先级'],
    'essay': ['Vue.js的优势与应用场景', 'React与Vue的比较', '前端工程化实践'],
    'program': ['实现冒泡排序', '创建一个简单的计数器组件', '使用Promise处理异步操作']
  };
  
  return `${types[type]}${num}：${topics[type][(num - 1) % topics[type].length]}`;
}

// 生成随机反馈
function getRandomFeedback() {
  const feedbacks = [
    '答案基本正确，但缺乏具体例子说明。',
    '概念理解准确，论述清晰有条理。',
    '内容全面，但有些细节解释不够深入。',
    '代码实现正确，但可以进一步优化性能。',
    '回答不够全面，漏掉了一些重要点。'
  ];
  
  return feedbacks[Math.floor(Math.random() * feedbacks.length)];
}
</script>

<template>
  <div class="auto-grader">
    <div class="page-header">
      <h2 class="page-title">智能评分</h2>
    </div>
    
    <div class="filters-bar">
      <div class="filter-group">
        <label>选择考试</label>
        <select v-model="selectedExam" @change="handleExamChange">
          <option v-for="exam in exams" :key="exam.id" :value="exam.id">
            {{ exam.name }}
          </option>
        </select>
      </div>
      
      <div class="filter-group">
        <label>题型</label>
        <select v-model="selectedQuestionType" @change="handleQuestionTypeChange">
          <option v-for="type in questionTypes" :key="type.id" :value="type.id">
            {{ type.name }}
          </option>
        </select>
      </div>
      
      <div class="filter-group">
        <label>状态</label>
        <select v-model="selectedStatus" @change="handleStatusChange">
          <option v-for="status in statusFilters" :key="status.id" :value="status.id">
            {{ status.name }}
          </option>
        </select>
      </div>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>加载答案数据中...</p>
    </div>
    
    <!-- 答案列表为空 -->
    <div v-else-if="answers.length === 0" class="empty-state">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
      </svg>
      <h3>暂无答案需要评分</h3>
      <p>尝试更改过滤条件或选择其他考试</p>
    </div>
    
    <!-- 评分界面 -->
    <div v-else class="grading-layout">
      <!-- 左侧答案列表 -->
      <div class="answers-list">
        <div class="list-header">
          <h3>答案列表 ({{ answers.length }})</h3>
        </div>
        
        <div class="answer-items">
          <div 
            v-for="(answer, index) in answers" 
            :key="answer.id"
            class="answer-item"
            :class="{ 'active': index === currentAnswerIndex }"
            @click="currentAnswerIndex = index"
          >
            <div class="answer-info">
              <div class="student-name">{{ answer.studentName }}</div>
              <div class="question-title">{{ answer.questionTitle }}</div>
            </div>
            
            <div class="answer-status">
              <span 
                class="status-badge"
                :class="{ 'pending': answer.status === 'pending', 'graded': answer.status === 'graded' }"
              >
                {{ answer.status === 'pending' ? '待评分' : '已评分' }}
              </span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧评分区 -->
      <div class="grading-area" v-if="currentAnswer">
        <div class="answer-header">
          <h3 class="question-title">{{ currentAnswer.questionTitle }}</h3>
          <div class="answer-meta">
            <div class="meta-item">
              <span class="meta-label">学生：</span>
              <span class="meta-value">{{ currentAnswer.studentName }}</span>
            </div>
            
            <div class="meta-item">
              <span class="meta-label">题型：</span>
              <span class="meta-value">
                {{ 
                  currentAnswer.questionType === 'short' ? '简答题' :
                  currentAnswer.questionType === 'essay' ? '论述题' :
                  currentAnswer.questionType === 'program' ? '编程题' : '未知类型'
                }}
              </span>
            </div>
            
            <div class="meta-item">
              <span class="meta-label">状态：</span>
              <span 
                class="meta-value status"
                :class="{ 'pending': currentAnswer.status === 'pending', 'graded': currentAnswer.status === 'graded' }"
              >
                {{ currentAnswer.status === 'pending' ? '待评分' : '已评分' }}
              </span>
            </div>
          </div>
        </div>
        
        <div class="answer-content">
          <div class="content-header">学生答案</div>
          <div class="content-body" v-html="currentAnswer.answer.replace(/```([a-z]*)\n([\s\S]*?)\n```/g, '<pre class=\'code-block\'>$2</pre>')"></div>
        </div>
        
        <div class="ai-suggestion">
          <div class="suggestion-header">
            <span>AI评分建议</span>
            <div class="similarity">
              <span class="similarity-label">答案相似度：</span>
              <span class="similarity-value">{{ currentAnswer.similarity }}%</span>
            </div>
          </div>
          
          <div class="suggestion-content">
            <div class="suggestion-score">
              <span class="score-label">建议分数：</span>
              <span class="score-value">{{ currentAnswer.suggestedScore }}/{{ currentAnswer.maxScore }}</span>
            </div>
            
            <div class="suggestion-feedback">
              <div class="feedback-label">反馈意见：</div>
              <div class="feedback-value">{{ currentAnswer.suggestedFeedback }}</div>
            </div>
            
            <button 
              class="use-ai-btn" 
              @click="autoScore"
              :disabled="scoring || currentAnswer.status === 'graded'"
            >
              {{ scoring ? '评分中...' : '使用AI评分' }}
            </button>
          </div>
        </div>
        
        <div class="teacher-scoring">
          <div class="scoring-header">教师评分</div>
          
          <div class="scoring-content">
            <div class="score-input">
              <label>得分：</label>
              <div class="score-control">
                <input 
                  type="number" 
                  v-model.number="currentAnswer.score" 
                  min="0" 
                  :max="currentAnswer.maxScore"
                />
                <span class="max-score">/ {{ currentAnswer.maxScore }}</span>
              </div>
            </div>
            
            <div class="feedback-input">
              <label>评语：</label>
              <textarea 
                v-model="currentAnswer.feedback"
                rows="3"
                placeholder="输入对学生作答的评语..."
              ></textarea>
            </div>
            
            <button 
              class="save-btn" 
              @click="saveScore"
              :disabled="!currentAnswer.score"
            >
              保存评分
            </button>
          </div>
        </div>
        
        <div class="navigation-controls">
          <button 
            class="nav-btn prev-btn" 
            @click="prevAnswer"
            :disabled="!hasPrevAnswer"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
            上一个
          </button>
          
          <div class="pagination-info">
            {{ currentAnswerIndex + 1 }} / {{ answers.length }}
          </div>
          
          <button 
            class="nav-btn next-btn" 
            @click="nextAnswer"
            :disabled="!hasNextAnswer"
          >
            下一个
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auto-grader {
  @apply max-w-7xl mx-auto px-4;
}

.page-header {
  @apply mb-6;
}

.page-title {
  @apply text-2xl font-bold text-gray-800;
}

.filters-bar {
  @apply flex flex-wrap gap-4 mb-6;
}

.filter-group {
  @apply min-w-[180px];
}

.filter-group label {
  @apply block text-sm font-medium text-gray-700 mb-1;
}

.filter-group select {
  @apply w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all duration-200;
}

.loading-state {
  @apply flex flex-col items-center justify-center py-12;
}

.loading-spinner {
  @apply w-10 h-10 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin mb-4;
}

.empty-state {
  @apply flex flex-col items-center justify-center text-center py-16;
}

.empty-state svg {
  @apply text-gray-300 mb-4;
}

.empty-state h3 {
  @apply text-lg font-medium text-gray-700 mb-2;
}

.empty-state p {
  @apply text-gray-500;
}

.grading-layout {
  @apply flex flex-col md:flex-row gap-6;
}

.answers-list {
  @apply w-full md:w-1/3 lg:w-1/4 bg-white rounded-xl shadow-sm overflow-hidden;
}

.list-header {
  @apply bg-gray-50 p-4 border-b border-gray-200;
}

.list-header h3 {
  @apply text-base font-medium text-gray-800;
}

.answer-items {
  @apply max-h-[calc(100vh-250px)] overflow-y-auto;
}

.answer-item {
  @apply flex justify-between items-center p-4 border-b border-gray-100 cursor-pointer hover:bg-gray-50 transition-colors;
}

.answer-item.active {
  @apply bg-blue-50;
}

.answer-info {
  @apply flex-1 min-w-0;
}

.student-name {
  @apply font-medium text-gray-800 truncate mb-1;
}

.question-title {
  @apply text-sm text-gray-500 truncate;
}

.answer-status {
  @apply ml-3 flex-shrink-0;
}

.status-badge {
  @apply px-2 py-0.5 text-xs font-medium rounded-full;
}

.status-badge.pending {
  @apply bg-yellow-100 text-yellow-800;
}

.status-badge.graded {
  @apply bg-green-100 text-green-800;
}

.grading-area {
  @apply w-full md:w-2/3 lg:w-3/4 flex flex-col gap-6;
}

.answer-header {
  @apply bg-white rounded-xl p-6 shadow-sm;
}

.question-title {
  @apply text-lg font-medium text-gray-800 mb-3;
}

.answer-meta {
  @apply flex flex-wrap gap-x-6 gap-y-2;
}

.meta-item {
  @apply flex items-center;
}

.meta-label {
  @apply text-sm text-gray-500;
}

.meta-value {
  @apply text-sm font-medium text-gray-700;
}

.meta-value.status.pending {
  @apply text-yellow-600;
}

.meta-value.status.graded {
  @apply text-green-600;
}

.answer-content {
  @apply bg-white rounded-xl p-6 shadow-sm;
}

.content-header {
  @apply text-base font-medium text-gray-800 mb-4;
}

.content-body {
  @apply text-gray-700 whitespace-pre-line;
}

.ai-suggestion {
  @apply bg-white rounded-xl p-6 shadow-sm;
}

.suggestion-header {
  @apply flex justify-between items-center mb-4;
}

.suggestion-header span {
  @apply text-base font-medium text-gray-800;
}

.similarity {
  @apply flex items-center;
}

.similarity-label {
  @apply text-sm text-gray-500 mr-1;
}

.similarity-value {
  @apply text-sm font-medium text-green-600;
}

.suggestion-content {
  @apply bg-blue-50 rounded-lg p-4;
}

.suggestion-score {
  @apply mb-3;
}

.score-label {
  @apply text-sm text-gray-600 mr-1;
}

.score-value {
  @apply font-medium text-blue-700;
}

.suggestion-feedback {
  @apply mb-4;
}

.feedback-label {
  @apply text-sm text-gray-600 mb-1;
}

.feedback-value {
  @apply text-gray-700 bg-white p-3 rounded-lg;
}

.use-ai-btn {
  @apply w-full py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed;
}

.teacher-scoring {
  @apply bg-white rounded-xl p-6 shadow-sm;
}

.scoring-header {
  @apply text-base font-medium text-gray-800 mb-4;
}

.scoring-content {
  @apply space-y-4;
}

.score-input {
  @apply flex items-center gap-3;
}

.score-input label {
  @apply text-sm text-gray-600 w-12;
}

.score-control {
  @apply flex items-center;
}

.score-control input {
  @apply px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all duration-200 w-20;
}

.max-score {
  @apply ml-1 text-gray-500;
}

.feedback-input {
  @apply flex flex-col sm:flex-row gap-3;
}

.feedback-input label {
  @apply text-sm text-gray-600 w-12;
}

.feedback-input textarea {
  @apply flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all duration-200;
}

.save-btn {
  @apply w-full py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed;
}

.navigation-controls {
  @apply flex items-center justify-between mt-4;
}

.nav-btn {
  @apply flex items-center px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed;
}

.pagination-info {
  @apply text-sm text-gray-500;
}

.next-btn {
  @apply bg-blue-600 text-white border-blue-600 hover:bg-blue-700;
}

:deep(.code-block) {
  @apply bg-gray-800 text-white p-4 rounded-lg overflow-x-auto font-mono text-sm my-2;
  white-space: pre;
}

@media (max-width: 768px) {
  .answers-list {
    @apply max-h-[300px] overflow-y-auto;
  }
  
  .feedback-input {
    @apply flex-col;
  }
  
  .feedback-input label {
    @apply mb-1;
  }
}
</style>