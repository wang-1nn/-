<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue';

const props = defineProps({
  teacherInfo: {
    type: Object,
    default: () => ({})
  },
  aiContext: {
    type: String,
    default: ''
  },
  themeColor: {
    type: String,
    default: 'blue'
  }
});

const emit = defineEmits(['generate-resource', 'view-resource', 'close']);

// 状态变量
const loading = ref(false);
const analysisCompleted = ref(false);
const currentStep = ref(1);  // 1: 分析, 2: 推荐, 3: 定制
const analysisProgress = ref(0);
const recommendations = ref([]);
const teachingGoals = ref([]);
const selectedRecommendations = ref([]);
const customizationOptions = reactive({
  difficulty: 'medium',  // easy, medium, hard
  focusArea: 'balanced', // theoretical, practical, balanced
  duration: 45,          // 分钟数
  interactivity: 'medium' // low, medium, high
});

// 推荐的资源类型
const resourceCategories = [
  { id: 'lesson-plan', name: '教案', icon: '📝' },
  { id: 'practice', name: '练习题', icon: '✏️' },
  { id: 'presentation', name: '课件', icon: '🖼️' },
  { id: 'activity', name: '课堂活动', icon: '🎮' },
  { id: 'assessment', name: '评估工具', icon: '📊' }
];

// 模拟教学目标
const sampleTeachingGoals = [
  '理解函数的概念和性质',
  '掌握函数图像的绘制方法',
  '能够应用函数知识解决实际问题',
  '培养数学思维和逻辑推理能力'
];

// 模拟推荐资源
const sampleRecommendations = [
  {
    id: 'rec1',
    title: '函数与导数基础 - 教案',
    type: 'lesson-plan',
    description: '包含函数基本概念、性质和导数入门的完整教案',
    matchScore: 95,
    tags: ['函数', '导数', '高一数学']
  },
  {
    id: 'rec2',
    title: '函数图像绘制 - 练习题集',
    type: 'practice',
    description: '20道函数图像绘制相关练习题，包含详细解答',
    matchScore: 87,
    tags: ['函数图像', '作图', '练习']
  },
  {
    id: 'rec3',
    title: '函数在实际生活中的应用 - 课件',
    type: 'presentation',
    description: '生动展示函数在日常生活和科学中的应用场景',
    matchScore: 82,
    tags: ['实际应用', '函数', '多媒体']
  },
  {
    id: 'rec4',
    title: '函数大富翁 - 课堂活动',
    type: 'activity',
    description: '让学生通过游戏方式巩固函数知识的小组活动',
    matchScore: 78,
    tags: ['互动', '游戏化', '小组活动']
  },
  {
    id: 'rec5',
    title: '函数与导数单元测试',
    type: 'assessment',
    description: '全面评估学生对函数与导数理解程度的单元测试',
    matchScore: 90,
    tags: ['测试', '评估', '导数']
  }
];

// 初始化
onMounted(() => {
  startAnalysis();
});

// 开始分析
const startAnalysis = () => {
  loading.value = true;
  analysisProgress.value = 0;
  
  // 模拟分析进度
  const interval = setInterval(() => {
    analysisProgress.value += 5;
    
    if (analysisProgress.value >= 100) {
      clearInterval(interval);
      completeAnalysis();
    }
  }, 200);
};

// 完成分析
const completeAnalysis = () => {
  setTimeout(() => {
    // 设置模拟数据
    teachingGoals.value = sampleTeachingGoals;
    recommendations.value = sampleRecommendations;
    
    loading.value = false;
    analysisCompleted.value = true;
  }, 500);
};

// 前往下一步
const nextStep = () => {
  if (currentStep.value < 3) {
    currentStep.value++;
  } else {
    // 完成流程，生成选中资源
    generateSelectedResources();
  }
};

// 返回上一步
const prevStep = () => {
  if (currentStep.value > 1) {
    currentStep.value--;
  }
};

// 切换选择状态
const toggleSelection = (recommendation) => {
  const index = selectedRecommendations.value.findIndex(r => r.id === recommendation.id);
  
  if (index === -1) {
    selectedRecommendations.value.push(recommendation);
  } else {
    selectedRecommendations.value.splice(index, 1);
  }
};

// 检查是否已选中
const isSelected = (recommendation) => {
  return selectedRecommendations.value.some(r => r.id === recommendation.id);
};

// 根据匹配度获取颜色类
const getMatchScoreClass = (score) => {
  if (score >= 90) return 'match-excellent';
  if (score >= 80) return 'match-good';
  if (score >= 70) return 'match-moderate';
  return 'match-low';
};

// 生成选中的资源
const generateSelectedResources = () => {
  if (selectedRecommendations.value.length === 0) {
    alert('请至少选择一个推荐资源');
    return;
  }
  
  loading.value = true;
  
  // 模拟生成延迟
  setTimeout(() => {
    emit('generate-resource', {
      recommendations: selectedRecommendations.value,
      customizationOptions: customizationOptions
    });
    loading.value = false;
  }, 1500);
};

// 关闭推荐器
const closeRecommender = () => {
  emit('close');
};

// 获取资源类型名称
const getResourceTypeName = (type) => {
  const category = resourceCategories.find(c => c.id === type);
  return category ? category.name : type;
};

// 获取资源类型图标
const getResourceTypeIcon = (type) => {
  const category = resourceCategories.find(c => c.id === type);
  return category ? category.icon : '📄';
};
</script>

<template>
  <div class="resource-recommender">
    <div class="recommender-header">
      <h2 class="recommender-title">
        <span v-if="currentStep === 1">智能教学资源分析</span>
        <span v-else-if="currentStep === 2">推荐资源</span>
        <span v-else>资源定制</span>
      </h2>
      
      <button class="close-button" @click="closeRecommender">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
    </div>
    
    <div class="steps-progress">
      <div class="step" :class="{ active: currentStep >= 1, complete: currentStep > 1 }">
        <div class="step-number">1</div>
        <div class="step-label">课程分析</div>
      </div>
      <div class="step-line" :class="{ active: currentStep > 1 }"></div>
      <div class="step" :class="{ active: currentStep >= 2, complete: currentStep > 2 }">
        <div class="step-number">2</div>
        <div class="step-label">资源推荐</div>
      </div>
      <div class="step-line" :class="{ active: currentStep > 2 }"></div>
      <div class="step" :class="{ active: currentStep >= 3 }">
        <div class="step-number">3</div>
        <div class="step-label">定制生成</div>
      </div>
    </div>
    
    <div class="recommender-content">
      <!-- 步骤1: 分析课程 -->
      <div v-if="currentStep === 1" class="analysis-container">
        <div v-if="loading && !analysisCompleted" class="analysis-progress">
          <div class="progress-container">
            <div class="progress-bar" :style="{ width: `${analysisProgress}%` }"></div>
          </div>
          <div class="progress-status">
            <span class="progress-percentage">{{ analysisProgress }}%</span>
            <span class="progress-action">{{ 
              analysisProgress < 30 ? '分析课程内容...' : 
              analysisProgress < 60 ? '识别教学目标...' : 
              analysisProgress < 90 ? '匹配教学资源...' : 
              '生成推荐方案...'
            }}</span>
          </div>
          <div class="analysis-info">
            <div class="info-item">
              <span class="info-label">学科：</span>
              <span class="info-value">{{ teacherInfo.subject || '未指定' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">年级：</span>
              <span class="info-value">{{ teacherInfo.grade || '未指定' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">单元：</span>
              <span class="info-value">{{ teacherInfo.currentUnit || '未指定' }}</span>
            </div>
          </div>
        </div>
        
        <div v-else class="analysis-results">
          <h3 class="results-title">分析结果</h3>
          
          <div class="goals-container">
            <h4 class="section-title">识别到的教学目标</h4>
            <ul class="goals-list">
              <li v-for="(goal, index) in teachingGoals" :key="index" class="goal-item">
                {{ goal }}
              </li>
            </ul>
          </div>
          
          <div class="summary-container">
            <h4 class="section-title">资源推荐总结</h4>
            <div class="summary-content">
              <p>根据您的教学内容和目标，我们为您匹配了 {{ recommendations.length }} 种教学资源，其中包括教案、练习题、课件等多种类型。这些资源将帮助您更高效地完成教学任务，并提升学生的学习体验。</p>
              <p>点击"下一步"查看详细的资源推荐。</p>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 步骤2: 资源推荐 -->
      <div v-else-if="currentStep === 2" class="recommendations-container">
        <div class="recommendations-header">
          <h3 class="recommendations-title">为您推荐的资源</h3>
          <p class="recommendations-subtitle">
            根据您的课程内容，我们推荐了以下 {{ recommendations.length }} 项资源，请选择您感兴趣的内容
          </p>
        </div>
        
        <div class="recommendation-cards">
          <div 
            v-for="recommendation in recommendations" 
            :key="recommendation.id" 
            class="recommendation-card"
            :class="{ selected: isSelected(recommendation) }"
            @click="toggleSelection(recommendation)"
          >
            <div class="card-header">
              <span class="resource-type">
                <span class="resource-icon">{{ getResourceTypeIcon(recommendation.type) }}</span>
                {{ getResourceTypeName(recommendation.type) }}
              </span>
              <span 
                class="match-score" 
                :class="getMatchScoreClass(recommendation.matchScore)"
              >
                匹配度 {{ recommendation.matchScore }}%
              </span>
            </div>
            
            <div class="card-content">
              <h4 class="card-title">{{ recommendation.title }}</h4>
              <p class="card-description">{{ recommendation.description }}</p>
            </div>
            
            <div class="card-tags">
              <span 
                v-for="(tag, index) in recommendation.tags" 
                :key="index" 
                class="tag"
              >
                {{ tag }}
              </span>
            </div>
            
            <div class="card-selection">
              <div class="check-icon" :class="{ checked: isSelected(recommendation) }">
                <svg v-if="isSelected(recommendation)" xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
                </svg>
              </div>
              <span class="selection-text">{{ isSelected(recommendation) ? '已选择' : '点击选择' }}</span>
            </div>
          </div>
        </div>
        
        <div class="selected-summary">
          已选择 {{ selectedRecommendations.length }} / {{ recommendations.length }} 个资源
        </div>
      </div>
      
      <!-- 步骤3: 资源定制 -->
      <div v-else class="customization-container">
        <h3 class="customization-title">资源定制</h3>
        <p class="customization-subtitle">调整以下选项，使生成的资源更符合您的需求</p>
        
        <div class="customization-options">
          <div class="option-group">
            <label class="option-label">难度级别</label>
            <div class="option-choices">
              <button 
                class="option-choice"
                :class="{ active: customizationOptions.difficulty === 'easy' }"
                @click="customizationOptions.difficulty = 'easy'"
              >简单</button>
              <button 
                class="option-choice"
                :class="{ active: customizationOptions.difficulty === 'medium' }"
                @click="customizationOptions.difficulty = 'medium'"
              >中等</button>
              <button 
                class="option-choice"
                :class="{ active: customizationOptions.difficulty === 'hard' }"
                @click="customizationOptions.difficulty = 'hard'"
              >困难</button>
            </div>
          </div>
          
          <div class="option-group">
            <label class="option-label">关注重点</label>
            <div class="option-choices">
              <button 
                class="option-choice"
                :class="{ active: customizationOptions.focusArea === 'theoretical' }"
                @click="customizationOptions.focusArea = 'theoretical'"
              >理论讲解</button>
              <button 
                class="option-choice"
                :class="{ active: customizationOptions.focusArea === 'balanced' }"
                @click="customizationOptions.focusArea = 'balanced'"
              >平衡配比</button>
              <button 
                class="option-choice"
                :class="{ active: customizationOptions.focusArea === 'practical' }"
                @click="customizationOptions.focusArea = 'practical'"
              >实践应用</button>
            </div>
          </div>
          
          <div class="option-group">
            <label class="option-label">课时时长 (分钟)</label>
            <div class="range-slider">
              <input 
                type="range" 
                min="30" 
                max="90" 
                step="5"
                v-model.number="customizationOptions.duration"
              >
              <span class="range-value">{{ customizationOptions.duration }} 分钟</span>
            </div>
          </div>
          
          <div class="option-group">
            <label class="option-label">互动程度</label>
            <div class="option-choices">
              <button 
                class="option-choice"
                :class="{ active: customizationOptions.interactivity === 'low' }"
                @click="customizationOptions.interactivity = 'low'"
              >低</button>
              <button 
                class="option-choice"
                :class="{ active: customizationOptions.interactivity === 'medium' }"
                @click="customizationOptions.interactivity = 'medium'"
              >中</button>
              <button 
                class="option-choice"
                :class="{ active: customizationOptions.interactivity === 'high' }"
                @click="customizationOptions.interactivity = 'high'"
              >高</button>
            </div>
          </div>
        </div>
        
        <div class="resource-preview">
          <h4 class="preview-title">已选资源 ({{ selectedRecommendations.length }})</h4>
          <div class="preview-list">
            <div 
              v-for="recommendation in selectedRecommendations"
              :key="recommendation.id"
              class="preview-item"
            >
              <span class="preview-icon">{{ getResourceTypeIcon(recommendation.type) }}</span>
              <span class="preview-name">{{ recommendation.title }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="recommender-footer">
      <button 
        v-if="currentStep > 1"
        class="btn-secondary" 
        @click="prevStep"
      >
        上一步
      </button>
      
      <button
        class="btn-primary"
        :disabled="(currentStep === 1 && loading) || (currentStep === 2 && selectedRecommendations.length === 0)"
        @click="nextStep"
      >
        {{ currentStep < 3 ? '下一步' : '生成资源' }}
      </button>
    </div>
    
    <!-- 加载蒙版 -->
    <div v-if="loading && currentStep === 3" class="loading-overlay">
      <div class="loading-spinner"></div>
      <div class="loading-text">正在生成您的自定义资源...</div>
    </div>
  </div>
</template>

<style scoped>
.resource-recommender {
  @apply flex flex-col h-full bg-white rounded-lg overflow-hidden relative;
}

.recommender-header {
  @apply px-6 py-4 border-b border-gray-200 flex justify-between items-center;
}

.recommender-title {
  @apply text-xl font-semibold text-gray-800;
}

.close-button {
  @apply p-1 rounded-full text-gray-500 hover:bg-gray-100 hover:text-gray-700 transition-colors;
}

.steps-progress {
  @apply flex items-center justify-center py-4 bg-gray-50 border-b border-gray-200;
}

.step {
  @apply flex flex-col items-center relative;
}

.step-number {
  @apply w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium border-2 bg-white;
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.step.active .step-number {
  background-color: var(--primary-color);
  color: white;
}

.step.complete .step-number {
  @apply bg-green-500 border-green-500 text-white;
}

.step-label {
  @apply text-xs mt-1 font-medium;
  color: var(--primary-color);
}

.step-line {
  @apply w-20 h-0.5 mx-2 bg-gray-300;
}

.step-line.active {
  background-color: var(--primary-color);
}

.recommender-content {
  @apply flex-1 p-6 overflow-y-auto;
}

/* 分析步骤样式 */
.analysis-container {
  @apply h-full flex flex-col;
}

.analysis-progress {
  @apply flex flex-col items-center justify-center h-full;
}

.progress-container {
  @apply w-full h-2 bg-gray-200 rounded-full overflow-hidden mb-2;
}

.progress-bar {
  @apply h-full transition-all duration-300 ease-out;
  background-color: var(--primary-color);
}

.progress-status {
  @apply flex items-center justify-center space-x-2 mb-8;
}

.progress-percentage {
  @apply text-xl font-bold;
  color: var(--primary-color);
}

.progress-action {
  @apply text-sm text-gray-600;
}

.analysis-info {
  @apply flex flex-col items-start bg-gray-50 p-4 rounded-lg border border-gray-200 mt-4 w-80;
}

.info-item {
  @apply flex items-center py-1;
}

.info-label {
  @apply text-gray-500 w-16;
}

.info-value {
  @apply font-medium;
}

.analysis-results {
  @apply h-full;
}

.results-title {
  @apply text-lg font-medium mb-4 text-center;
}

.goals-container, .summary-container {
  @apply bg-white border border-gray-200 rounded-lg p-4 mb-6 shadow-sm;
}

.section-title {
  @apply text-base font-medium mb-3 text-gray-700 pb-2 border-b border-gray-100;
}

.goals-list {
  @apply space-y-2;
}

.goal-item {
  @apply flex items-start;
}

.goal-item:before {
  content: "•";
  @apply mr-2 text-green-500;
}

.summary-content {
  @apply space-y-3 text-gray-700;
}

/* 推荐步骤样式 */
.recommendations-container {
  @apply h-full flex flex-col;
}

.recommendations-header {
  @apply mb-6;
}

.recommendations-title {
  @apply text-lg font-medium mb-2;
}

.recommendations-subtitle {
  @apply text-gray-600 text-sm;
}

.recommendation-cards {
  @apply grid grid-cols-1 md:grid-cols-2 gap-4 mb-4;
  max-height: calc(100vh - 320px);
  overflow-y: auto;
}

.recommendation-card {
  @apply border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow cursor-pointer relative;
}

.recommendation-card.selected {
  @apply border-2;
  border-color: var(--primary-color);
  background-color: var(--primary-bg);
}

.card-header {
  @apply flex justify-between items-center mb-3;
}

.resource-type {
  @apply text-sm flex items-center;
}

.resource-icon {
  @apply mr-1 text-lg;
}

.match-score {
  @apply text-xs font-medium px-2 py-1 rounded-full;
}

.match-excellent {
  @apply bg-green-100 text-green-800;
}

.match-good {
  @apply bg-blue-100 text-blue-800;
}

.match-moderate {
  @apply bg-yellow-100 text-yellow-800;
}

.match-low {
  @apply bg-gray-100 text-gray-800;
}

.card-content {
  @apply mb-3;
}

.card-title {
  @apply font-medium mb-1;
}

.card-description {
  @apply text-sm text-gray-600;
}

.card-tags {
  @apply flex flex-wrap gap-1 mb-3;
}

.tag {
  @apply text-xs px-2 py-0.5 rounded-full bg-gray-100 text-gray-700;
}

.card-selection {
  @apply flex items-center justify-end mt-2;
}

.check-icon {
  @apply w-5 h-5 rounded-full border flex items-center justify-center mr-2;
  border-color: var(--primary-color);
}

.check-icon.checked {
  background-color: var(--primary-color);
  color: white;
}

.selection-text {
  @apply text-xs;
}

.selected-summary {
  @apply text-center text-sm font-medium mt-4;
  color: var(--primary-color);
}

/* 定制步骤样式 */
.customization-container {
  @apply h-full;
}

.customization-title {
  @apply text-lg font-medium mb-2;
}

.customization-subtitle {
  @apply text-gray-600 text-sm mb-6;
}

.customization-options {
  @apply grid grid-cols-1 md:grid-cols-2 gap-6 mb-8;
}

.option-group {
  @apply flex flex-col;
}

.option-label {
  @apply font-medium mb-2 text-gray-700;
}

.option-choices {
  @apply flex space-x-2;
}

.option-choice {
  @apply px-3 py-1.5 rounded-md border border-gray-300 text-sm flex-1 transition-all;
}

.option-choice.active {
  @apply font-medium border-2;
  border-color: var(--primary-color);
  background-color: var(--primary-bg);
  color: var(--primary-color);
}

.range-slider {
  @apply flex items-center;
}

.range-slider input {
  @apply w-full mr-4;
}

.range-value {
  @apply text-sm font-medium whitespace-nowrap;
}

.resource-preview {
  @apply border border-gray-200 rounded-lg p-4;
}

.preview-title {
  @apply font-medium mb-3 text-gray-700 pb-2 border-b border-gray-100;
}

.preview-list {
  @apply space-y-2 max-h-40 overflow-y-auto;
}

.preview-item {
  @apply flex items-center py-1.5 px-2 hover:bg-gray-50 rounded-md;
}

.preview-icon {
  @apply mr-2 text-lg;
}

.preview-name {
  @apply text-sm;
}

.recommender-footer {
  @apply px-6 py-4 border-t border-gray-200 flex justify-between;
}

.btn-primary, .btn-secondary {
  @apply px-4 py-2 rounded-md text-sm font-medium transition-colors;
}

.btn-primary {
  color: white;
  background-color: var(--primary-color);
}

.btn-primary:hover:not(:disabled) {
  background-color: var(--primary-dark);
}

.btn-primary:disabled {
  @apply opacity-50 cursor-not-allowed;
}

.btn-secondary {
  @apply border border-gray-300 text-gray-700 hover:bg-gray-50;
}

.loading-overlay {
  @apply absolute inset-0 bg-white bg-opacity-80 flex flex-col items-center justify-center z-10;
}

.loading-spinner {
  @apply w-12 h-12 border-4 rounded-full animate-spin mb-4;
  border-color: var(--primary-color) transparent transparent;
}

.loading-text {
  @apply text-gray-700 font-medium;
}
</style>