<script setup>
import { ref, reactive, computed, onMounted } from 'vue';

const props = defineProps({
  teacherInfo: {
    type: Object,
    default: () => ({})
  },
  themeColor: {
    type: String,
    default: 'blue'
  }
});

const emit = defineEmits(['save-feedback', 'close']);

// 状态变量
const loading = ref(false);
const isGrading = ref(false);
const gradingProgress = ref(0);
const assignments = ref([]);
const selectedAssignment = ref(null);
const gradeResults = ref(null);
const assignmentContent = ref('');
const rubric = reactive({
  criteria: [
    { id: 'understanding', name: '概念理解', weight: 30, score: 0, comment: '' },
    { id: 'application', name: '应用能力', weight: 25, score: 0, comment: '' },
    { id: 'presentation', name: '表达呈现', weight: 20, score: 0, comment: '' },
    { id: 'creativity', name: '创新思维', weight: 15, score: 0, comment: '' },
    { id: 'effort', name: '完成度', weight: 10, score: 0, comment: '' },
  ],
  maxScore: 100
});

// 反馈模板
const feedbackTemplates = [
  { id: 'positive', name: '优秀表现', templates: [
    '概念理解透彻，解释清晰准确。',
    '能够灵活运用所学知识解决问题。',
    '论述有理有据，逻辑性强。',
    '表达清晰流畅，结构合理。',
    '有独特的见解和创新思路。'
  ]},
  { id: 'improvement', name: '待改进', templates: [
    '概念理解不够深入，建议回顾相关章节。',
    '解题思路正确，但计算过程有错误。',
    '部分论述缺乏足够的支持论据。',
    '表达不够清晰，结构需要调整。',
    '作业不够完整，有遗漏的部分。'
  ]},
  { id: 'encouragement', name: '鼓励', templates: [
    '看得出你在这部分内容上付出了很多努力！',
    '你的思路很有创意，继续保持这种探索精神。',
    '比起上次作业有明显进步，继续加油！',
    '你的分析角度很独特，展示了深入思考。',
    '整体表现良好，相信下次会更出色！'
  ]}
];

// 当前编辑的评语
const currentFeedback = ref('');

// 模拟作业数据
const mockAssignments = [
  {
    id: 'assign1',
    title: '函数与导数应用题',
    student: '张明',
    submitDate: '2023-10-20',
    type: 'homework',
    content: `# 函数与导数应用题
    
1. 求函数 f(x) = 2x³ - 3x² + 4x - 1 的导数，并求出其在 x = 2 处的切线方程。

解答：
f'(x) = 6x² - 6x + 4
当 x = 2 时，f'(2) = 6×4 - 6×2 + 4 = 24 - 12 + 4 = 16
f(2) = 2×8 - 3×4 + 4×2 - 1 = 16 - 12 + 8 - 1 = 11
所以切线方程为：y - 11 = 16(x - 2)，即 y = 16x - 21

2. 一个长方体容器，底面积为 x²，高为 h，容积为 V。已知 V = 1000 cm³，求当表面积最小时的 x 和 h 值。

解答：
已知 V = x²h = 1000，所以 h = 1000/x²
表面积 S = 2x² + 4xh = 2x² + 4x(1000/x²) = 2x² + 4000/x
求导：S' = 4x - 4000/x²
当 S' = 0 时，4x = 4000/x²，即 x³ = 1000，所以 x = 10 cm
此时 h = 1000/x² = 1000/100 = 10 cm
检验 S'' = 4 + 8000/x³ > 0，确实是最小值
所以当 x = 10 cm，h = 10 cm 时表面积最小`
  },
  {
    id: 'assign2',
    title: '二次函数分析',
    student: '李华',
    submitDate: '2023-10-18',
    type: 'quiz',
    content: `# 二次函数分析
    
1. 对于二次函数 f(x) = ax² + bx + c (a ≠ 0)，请回答下列问题：
   a) 函数的对称轴是什么？
   b) 顶点坐标如何计算？
   c) 当 a > 0 和 a < 0 时，函数的图像有什么不同？

解答：
a) 对称轴是 x = -b/(2a)
b) 顶点坐标是 (-b/(2a), f(-b/(2a)))，也可以写成 (-b/(2a), c - b²/(4a))
c) 当 a > 0 时，函数图像是开口向上的抛物线，有最小值；
   当 a < 0 时，函数图像是开口向下的抛物线，有最大值。

2. 已知二次函数 f(x) = x² - 4x + 3，求：
   a) 函数的顶点
   b) 函数的对称轴
   c) 函数的最小值
   d) 函数的零点

解答：
a) 对称轴 x = -b/(2a) = -(-4)/(2×1) = 2
   顶点坐标 (2, f(2)) = (2, 4 - 8 + 3) = (2, -1)
b) 函数的对称轴是 x = 2
c) 函数的最小值是 -1
d) 令 f(x) = 0，得 x² - 4x + 3 = 0
   使用求根公式，x = (4 ± √(16-12))/2 = (4 ± 2)/2
   所以 x₁ = 3, x₂ = 1
   函数的零点是 x = 1 或 x = 3`
  },
  {
    id: 'assign3',
    title: '数列与数学归纳法',
    student: '王芳',
    submitDate: '2023-10-15',
    type: 'homework',
    content: `# 数列与数学归纳法
    
1. 使用数学归纳法证明：对于任意自然数 n，有 1 + 2 + 3 + ... + n = n(n+1)/2

证明：
(1) 当 n = 1 时，左边为 1，右边为 1(1+1)/2 = 1，等式成立。
(2) 假设当 n = k 时，等式成立，即 1 + 2 + ... + k = k(k+1)/2
(3) 当 n = k+1 时，左边为 1 + 2 + ... + k + (k+1)
    = k(k+1)/2 + (k+1)
    = (k+1)(k/2 + 1)
    = (k+1)(k+2)/2
    = (k+1)((k+1)+1)/2
    右边为 (k+1)(k+2)/2，两边相等，等式成立。
所以，对于任意自然数 n，等式 1 + 2 + ... + n = n(n+1)/2 成立。

2. 已知等比数列 {an}，其中 a1 = 3，q = 2，求：
a) a5 的值
b) 前 5 项的和 S5

解答：
a) a5 = a1 × q^(5-1) = 3 × 2^4 = 3 × 16 = 48
b) S5 = a1(1-q^5)/(1-q) = 3(1-2^5)/(1-2) = 3(1-32)/(-1) = 3×(-31)/(-1) = 93`
  }
];

// 计算属性
// 总分
const totalScore = computed(() => {
  if (!rubric.criteria) return 0;
  
  return rubric.criteria.reduce((sum, criterion) => {
    return sum + (criterion.score || 0) * criterion.weight / 100;
  }, 0);
});

// 字数统计
const wordCount = computed(() => {
  if (!assignmentContent.value) return 0;
  return assignmentContent.value.replace(/\s+/g, ' ').split(' ').filter(Boolean).length;
});

// 初始化
onMounted(() => {
  loadAssignments();
});

// 加载作业列表
const loadAssignments = () => {
  loading.value = true;
  
  // 模拟API请求延迟
  setTimeout(() => {
    assignments.value = mockAssignments;
    loading.value = false;
  }, 500);
};

// 选择作业
const selectAssignment = (assignment) => {
  selectedAssignment.value = assignment;
  assignmentContent.value = assignment.content;
  resetRubric();
  gradeResults.value = null;
};

// 重置评分标准
const resetRubric = () => {
  rubric.criteria.forEach(criterion => {
    criterion.score = 0;
    criterion.comment = '';
  });
};

// 开始自动批改
const startGrading = () => {
  if (!selectedAssignment.value) return;
  
  isGrading.value = true;
  gradingProgress.value = 0;
  
  // 模拟分析进度
  const interval = setInterval(() => {
    gradingProgress.value += 5;
    
    if (gradingProgress.value >= 100) {
      clearInterval(interval);
      completeGrading();
    }
  }, 100);
};

// 完成批改
const completeGrading = () => {
  // 模拟AI评分结果
  const results = {
    understanding: {
      score: 85,
      comment: '概念理解较为透彻，但在二次函数的性质解释部分有小错误。'
    },
    application: {
      score: 90,
      comment: '能够很好地运用公式解决问题，计算步骤清晰。'
    },
    presentation: {
      score: 80,
      comment: '表达基本清晰，但格式和符号使用可以更规范。'
    },
    creativity: {
      score: 75,
      comment: '解题思路较为常规，可以尝试更多角度分析问题。'
    },
    effort: {
      score: 95,
      comment: '作业完成度很高，细节处理认真。'
    },
    overallComment: '整体表现良好，对核心概念掌握牢固，在表达和创新方面可以进一步提高。建议复习二次函数性质的精确表述，并尝试用多种方法解决同一个问题。',
    suggestedScore: 85
  };
  
  // 更新评分标准
  rubric.criteria.forEach(criterion => {
    if (results[criterion.id]) {
      criterion.score = results[criterion.id].score;
      criterion.comment = results[criterion.id].comment;
    }
  });
  
  // 更新总体评语
  currentFeedback.value = results.overallComment;
  
  // 保存结果
  gradeResults.value = results;
  
  // 完成批改
  isGrading.value = false;
};

// 插入模板评语
const insertFeedbackTemplate = (template) => {
  currentFeedback.value += (currentFeedback.value ? '\n' : '') + template;
};

// 保存评分反馈
const saveFeedback = () => {
  if (!selectedAssignment.value) return;
  
  const feedback = {
    assignmentId: selectedAssignment.value.id,
    studentName: selectedAssignment.value.student,
    score: totalScore.value,
    criteria: rubric.criteria.map(c => ({
      id: c.id,
      name: c.name,
      score: c.score,
      weight: c.weight,
      comment: c.comment
    })),
    overallComment: currentFeedback.value,
    gradedDate: new Date()
  };
  
  // 发送保存事件
  emit('save-feedback', feedback);
  
  // 模拟保存成功
  alert('评分已保存');
  
  // 重置选择
  selectedAssignment.value = null;
  assignmentContent.value = '';
  resetRubric();
  gradeResults.value = null;
  currentFeedback.value = '';
};

// 关闭批改器
const closeGrader = () => {
  emit('close');
};

// 获取成绩级别
const getScoreLevel = (score) => {
  if (score >= 90) return { level: 'A', class: 'score-excellent' };
  if (score >= 80) return { level: 'B', class: 'score-good' };
  if (score >= 70) return { level: 'C', class: 'score-fair' };
  if (score >= 60) return { level: 'D', class: 'score-pass' };
  return { level: 'F', class: 'score-fail' };
};

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  });
};
</script>

<template>
  <div class="assignment-grader">
    <!-- 没有选择作业时显示列表 -->
    <div v-if="!selectedAssignment" class="assignments-list">
      <div class="list-header">
        <h2 class="list-title">待批改作业</h2>
        <span class="assignment-count">{{ assignments.length }} 项待批改</span>
      </div>
      
      <div class="assignments-container" v-if="!loading">
        <div 
          v-for="assignment in assignments" 
          :key="assignment.id"
          class="assignment-item"
          @click="selectAssignment(assignment)"
        >
          <div class="assignment-icon">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
          </div>
          
          <div class="assignment-details">
            <h3 class="assignment-title">{{ assignment.title }}</h3>
            <div class="assignment-meta">
              <span class="student-name">{{ assignment.student }}</span>
              <span class="submit-date">提交于 {{ formatDate(assignment.submitDate) }}</span>
            </div>
          </div>
          
          <div class="assignment-type" :class="assignment.type">
            {{ assignment.type === 'homework' ? '作业' : '测验' }}
          </div>
        </div>
      </div>
      
      <div v-else class="loading-container">
        <div class="loading-spinner"></div>
        <span>加载作业中...</span>
      </div>
    </div>
    
    <!-- 选择作业后显示批改界面 -->
    <div v-else class="grading-interface">
      <div class="grading-header">
        <button class="back-button" @click="selectedAssignment = null">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
          返回列表
        </button>
        
        <div class="assignment-info">
          <h3>{{ selectedAssignment.title }}</h3>
          <div class="info-meta">
            <span class="info-student">学生: {{ selectedAssignment.student }}</span>
            <span class="info-date">提交日期: {{ formatDate(selectedAssignment.submitDate) }}</span>
          </div>
        </div>
      </div>
      
      <div class="grading-content">
        <!-- 左侧：作业内容 -->
        <div class="content-panel">
          <div class="panel-header">
            <h4>作业内容</h4>
            <span class="word-count">{{ wordCount }} 字</span>
          </div>
          <div class="content-display">
            <pre>{{ assignmentContent }}</pre>
          </div>
        </div>
        
        <!-- 右侧：评分工具 -->
        <div class="grading-panel">
          <div class="panel-header with-actions">
            <h4>评分工具</h4>
            
            <button 
              v-if="!gradeResults"
              class="grade-button" 
              @click="startGrading"
              :disabled="isGrading"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
              </svg>
              智能评分
            </button>
          </div>
          
          <!-- 评分进度条 -->
          <div v-if="isGrading" class="grading-progress">
            <div class="progress-container">
              <div class="progress-bar" :style="{ width: `${gradingProgress}%` }"></div>
            </div>
            <div class="progress-text">正在分析作业内容... {{ gradingProgress }}%</div>
          </div>
          
          <!-- 评分标准 -->
          <div v-else class="rubric-container">
            <div class="total-score">
              <div class="score-display">
                <div 
                  class="score-value" 
                  :class="getScoreLevel(totalScore).class"
                >
                  {{ Math.round(totalScore) }}
                </div>
                <div class="score-level">{{ getScoreLevel(totalScore).level }}</div>
              </div>
              <div class="score-label">总分</div>
            </div>
            
            <div class="criteria-list">
              <div 
                v-for="criterion in rubric.criteria" 
                :key="criterion.id"
                class="criterion-item"
              >
                <div class="criterion-header">
                  <div class="criterion-name">{{ criterion.name }}</div>
                  <div class="criterion-weight">{{ criterion.weight }}%</div>
                </div>
                
                <div class="criterion-score">
                  <input 
                    type="range" 
                    v-model.number="criterion.score" 
                    min="0" 
                    max="100" 
                    step="5"
                    class="score-slider"
                  />
                  <div class="score-value">{{ criterion.score }}</div>
                </div>
                
                <textarea 
                  v-model="criterion.comment"
                  class="criterion-comment"
                  :placeholder="`${criterion.name}评语...`"
                  rows="2"
                ></textarea>
              </div>
            </div>
            
            <div class="overall-feedback">
              <h4>总体评语</h4>
              <textarea 
                v-model="currentFeedback"
                class="feedback-textarea"
                placeholder="输入总体评语..."
                rows="4"
              ></textarea>
              
              <div class="feedback-templates">
                <div class="templates-header">快速评语模板</div>
                
                <div class="template-categories">
                  <div 
                    v-for="category in feedbackTemplates" 
                    :key="category.id" 
                    class="template-category"
                  >
                    <h5 class="category-name">{{ category.name }}</h5>
                    <div class="template-list">
                      <button 
                        v-for="(template, index) in category.templates"
                        :key="index"
                        class="template-button"
                        @click="insertFeedbackTemplate(template)"
                      >
                        {{ template }}
                      </button>
                    </div>
                  </div>
                </div>
              </div>
              
              <div class="action-buttons">
                <button class="save-button" @click="saveFeedback">保存评分</button>
                <button class="cancel-button" @click="selectedAssignment = null">取消</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.assignment-grader {
  @apply flex flex-col h-full bg-white;
}

/* 作业列表样式 */
.assignments-list {
  @apply p-6 h-full;
}

.list-header {
  @apply flex justify-between items-center mb-6;
}

.list-title {
  @apply text-xl font-semibold text-gray-800;
}

.assignment-count {
  @apply text-sm font-medium text-gray-600 bg-gray-100 px-3 py-1 rounded-full;
}

.assignments-container {
  @apply space-y-4;
}

.assignment-item {
  @apply flex items-center p-4 bg-white border border-gray-200 rounded-lg shadow-sm hover:shadow-md transition-shadow cursor-pointer;
}

.assignment-icon {
  @apply mr-4 text-gray-500;
}

.assignment-details {
  @apply flex-1;
}

.assignment-title {
  @apply font-medium text-gray-800 mb-1;
}

.assignment-meta {
  @apply flex text-sm text-gray-600;
}

.student-name {
  @apply mr-4;
}

.submit-date {
  @apply text-gray-500;
}

.assignment-type {
  @apply px-3 py-1 rounded-full text-xs font-medium;
}

.assignment-type.homework {
  @apply bg-blue-100 text-blue-800;
}

.assignment-type.quiz {
  @apply bg-purple-100 text-purple-800;
}

.loading-container {
  @apply flex flex-col items-center justify-center h-64 text-gray-500;
}

.loading-spinner {
  @apply w-8 h-8 border-4 border-gray-300 border-t-blue-600 rounded-full animate-spin mb-3;
}

/* 批改界面样式 */
.grading-interface {
  @apply flex flex-col h-full;
}

.grading-header {
  @apply p-4 border-b border-gray-200 flex items-center;
}

.back-button {
  @apply flex items-center text-gray-600 hover:text-gray-900 mr-4;
}

.assignment-info {
  @apply flex-1;
}

.info-meta {
  @apply flex text-sm text-gray-600 mt-1;
}

.info-student {
  @apply mr-4;
}

.grading-content {
  @apply flex flex-1 overflow-hidden;
}

.content-panel, .grading-panel {
  @apply flex flex-col h-full;
}

.content-panel {
  @apply w-3/5 border-r border-gray-200;
}

.grading-panel {
  @apply w-2/5;
}

.panel-header {
  @apply p-4 border-b border-gray-200 flex items-center;
}

.panel-header h4 {
  @apply font-medium text-gray-700;
}

.panel-header.with-actions {
  @apply flex justify-between;
}

.word-count {
  @apply text-xs text-gray-500 ml-2;
}

.grade-button {
  @apply flex items-center px-3 py-1.5 rounded-md text-sm font-medium text-white transition-colors;
  background-color: var(--primary-color);
}

.grade-button:hover:not(:disabled) {
  background-color: var(--primary-dark);
}

.grade-button:disabled {
  @apply opacity-50 cursor-not-allowed;
}

.content-display {
  @apply p-4 overflow-y-auto flex-1;
  max-height: calc(100vh - 200px);
}

.content-display pre {
  @apply whitespace-pre-wrap font-sans text-gray-800;
}

.grading-progress {
  @apply p-4;
}

.progress-container {
  @apply w-full h-2 bg-gray-200 rounded-full overflow-hidden mb-2;
}

.progress-bar {
  @apply h-full transition-all duration-200 ease-out;
  background-color: var(--primary-color);
}

.progress-text {
  @apply text-sm text-gray-600 text-center;
}

.rubric-container {
  @apply p-4 overflow-y-auto;
  max-height: calc(100vh - 200px);
}

.total-score {
  @apply flex flex-col items-center mb-6;
}

.score-display {
  @apply flex items-center;
}

.score-value {
  @apply text-4xl font-bold mr-2 p-4 rounded-full w-16 h-16 flex items-center justify-center;
}

.score-level {
  @apply text-xl font-medium;
}

.score-label {
  @apply text-sm text-gray-600 mt-1;
}

.score-excellent {
  @apply bg-green-100 text-green-800;
}

.score-good {
  @apply bg-blue-100 text-blue-800;
}

.score-fair {
  @apply bg-yellow-100 text-yellow-800;
}

.score-pass {
  @apply bg-orange-100 text-orange-800;
}

.score-fail {
  @apply bg-red-100 text-red-800;
}

.criteria-list {
  @apply space-y-6 mb-8;
}

.criterion-item {
  @apply border border-gray-200 rounded-lg p-3;
}

.criterion-header {
  @apply flex justify-between mb-2;
}

.criterion-name {
  @apply font-medium text-gray-700;
}

.criterion-weight {
  @apply text-sm text-gray-600;
}

.criterion-score {
  @apply flex items-center mb-3;
}

.score-slider {
  @apply flex-1 mr-3;
}

.score-slider::-webkit-slider-thumb {
  @apply appearance-none w-4 h-4 rounded-full cursor-pointer;
  background-color: var(--primary-color);
}

.criterion-comment {
  @apply w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 transition-colors;
  focus-ring-color: var(--primary-color);
}

.overall-feedback {
  @apply space-y-4;
}

.overall-feedback h4 {
  @apply font-medium text-gray-700;
}

.feedback-textarea {
  @apply w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 transition-colors;
  focus-ring-color: var(--primary-color);
}

.feedback-templates {
  @apply border border-gray-200 rounded-lg p-3;
}

.templates-header {
  @apply text-sm font-medium mb-3 pb-2 border-b border-gray-100;
}

.template-categories {
  @apply space-y-4;
}

.category-name {
  @apply text-xs uppercase tracking-wide text-gray-500 mb-2;
}

.template-list {
  @apply flex flex-wrap gap-1;
}

.template-button {
  @apply text-xs px-2 py-1 border border-gray-300 rounded-md hover:bg-gray-50 transition-colors;
}

.action-buttons {
  @apply flex justify-end space-x-3;
}

.save-button, .cancel-button {
  @apply px-4 py-2 rounded-md text-sm font-medium transition-colors;
}

.save-button {
  @apply text-white;
  background-color: var(--primary-color);
}

.save-button:hover {
  background-color: var(--primary-dark);
}

.cancel-button {
  @apply border border-gray-300 text-gray-700 hover:bg-gray-50;
}
</style>