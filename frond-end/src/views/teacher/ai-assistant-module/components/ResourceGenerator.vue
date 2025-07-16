<script setup>
import { ref, reactive } from 'vue';

const props = defineProps({
  type: {
    type: String,
    required: true,  // 'lesson' 或 'questions'
    validator: (value) => ['lesson', 'questions'].includes(value)
  },
  initialContext: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['close', 'save', 'back']);

// 状态变量
const loading = ref(false);
const step = ref(1); // 1: 表单 2: 预览
const generatedContent = ref('');

// 教案表单
const lessonForm = reactive({
  subject: '',
  topic: '',
  grade: '',
  duration: 45,
  objectives: '',
  resources: '',
  notes: ''
});

// 习题表单
const questionForm = reactive({
  subject: '',
  topic: '',
  grade: '',
  quantity: 5,
  types: ['single', 'multiple', 'true_false', 'short'],
  difficulty: 'medium',
  includeAnswers: true
});

// 题型选项
const questionTypes = [
  { id: 'single', label: '单选题' },
  { id: 'multiple', label: '多选题' },
  { id: 'true_false', label: '判断题' },
  { id: 'short', label: '简答题' },
  { id: 'essay', label: '论述题' },
  { id: 'calculation', label: '计算题' }
];

// 难度选项
const difficultyLevels = [
  { id: 'easy', label: '简单' },
  { id: 'medium', label: '中等' },
  { id: 'hard', label: '困难' },
  { id: 'mixed', label: '混合难度' }
];

// 年级选项
const grades = [
  '小学一年级', '小学二年级', '小学三年级', '小学四年级', '小学五年级', '小学六年级',
  '初中一年级', '初中二年级', '初中三年级',
  '高中一年级', '高中二年级', '高中三年级'
];

// 常见学科
const subjects = [
  '语文', '数学', '英语', '物理', '化学', '生物', '历史', '地理', '政治', '信息技术', '音乐', '美术', '体育'
];

// 生成资源
const generateResource = () => {
  loading.value = true;
  
  // 根据类型选择表单
  const form = props.type === 'lesson' ? lessonForm : questionForm;
  
  // 模拟API延迟
  setTimeout(() => {
    // 生成内容
    if (props.type === 'lesson') {
      generatedContent.value = generateLessonPlan(form);
    } else {
      generatedContent.value = generateQuestions(form);
    }
    
    // 进入预览步骤
    step.value = 2;
    loading.value = false;
  }, 2000);
};

// 生成教案
const generateLessonPlan = (form) => {
  return `# ${form.topic} - 教案

## 基本信息
- **学科**: ${form.subject}
- **年级**: ${form.grade}
- **课时**: ${form.duration}分钟
- **教学主题**: ${form.topic}

## 教学目标
${form.objectives || `
- 知识目标：学生将能够理解和解释核心概念
- 技能目标：学生将能够应用所学知识解决问题
- 情感目标：培养学生的学习兴趣和探究精神
`}

## 教学准备
${form.resources || `
- 课件
- 教学视频
- 教学挂图
- 学生活动材料
`}

## 教学过程

### 一、导入（5分钟）
- 通过提问引入今天的主题
- 展示与主题相关的图片或视频激发学生兴趣
- 简要说明本节课的学习目标和内容

### 二、新课讲解（20分钟）
- 讲解主要概念和原理
  - 关键点1：...
  - 关键点2：...
  - 关键点3：...
- 结合实例进行深入说明
- 通过提问检查学生理解程度

### 三、活动实践（15分钟）
- 学生分组活动：...
- 完成相关练习：...
- 小组讨论和分享：...

### 四、总结提升（5分钟）
- 与学生一起回顾本节课的主要内容
- 强调重点和难点
- 布置课后作业和预习任务

## 板书设计
\`\`\`
【主题】
├── 核心概念1
│   ├── 要点1
│   └── 要点2
├── 核心概念2
│   ├── 要点1
│   └── 要点2
└── 小结
\`\`\`

## 教学反思
${form.notes || '(课后填写，根据教学实际情况进行反思)'}

## 拓展资源
- 推荐阅读材料：...
- 相关视频资源：...
- 实践活动建议：...`;
};

// 生成习题
const generateQuestions = (form) => {
  const questions = [];
  
  // 单选题示例
  if (form.types.includes('single')) {
    questions.push(`## 一、单选题

1. 以下关于${form.topic}的说法，正确的是：
   A. 选项一
   B. 选项二
   C. 选项三
   D. 选项四
   
   ${form.includeAnswers ? '*答案：B，解析：选项B正确，因为...*' : ''}

2. ${form.topic}的主要特点是：
   A. 特点一
   B. 特点二
   C. 特点三
   D. 特点四
   
   ${form.includeAnswers ? '*答案：A，解析：特点一是最主要的特征，因为...*' : ''}`);
  }
  
  // 多选题示例
  if (form.types.includes('multiple')) {
    questions.push(`## 二、多选题

1. 以下关于${form.topic}的说法，正确的有：
   A. 说法一
   B. 说法二
   C. 说法三
   D. 说法四
   
   ${form.includeAnswers ? '*答案：ABC，解析：说法一、二、三是正确的，因为...*' : ''}

2. ${form.topic}的应用包括：
   A. 应用场景一
   B. 应用场景二
   C. 应用场景三
   D. 应用场景四
   
   ${form.includeAnswers ? '*答案：BD，解析：应用场景二和四是典型应用，因为...*' : ''}`);
  }
  
  // 判断题示例
  if (form.types.includes('true_false')) {
    questions.push(`## 三、判断题

1. ${form.topic}的概念最早出现于20世纪。（ ）
   
   ${form.includeAnswers ? '*答案：√，解析：这一概念确实起源于20世纪早期...*' : ''}

2. ${form.topic}在实际应用中没有局限性。（ ）
   
   ${form.includeAnswers ? '*答案：×，解析：任何理论或方法都有其适用范围和局限性...*' : ''}`);
  }
  
  // 简答题示例
  if (form.types.includes('short')) {
    questions.push(`## 四、简答题

1. 简述${form.topic}的基本概念及其特点。
   
   ${form.includeAnswers ? '*参考答案：${form.topic}是指...，其主要特点包括...*' : ''}

2. ${form.topic}在实际中有哪些应用？举例说明。
   
   ${form.includeAnswers ? '*参考答案：${form.topic}的应用主要有以下几个方面：首先...*' : ''}`);
  }
  
  // 论述题示例
  if (form.types.includes('essay')) {
    questions.push(`## 五、论述题

1. 论述${form.topic}的发展历程及其重要意义。
   
   ${form.includeAnswers ? '*参考答案：${form.topic}的发展经历了以下几个阶段：...*' : ''}

2. 分析${form.topic}面临的挑战及未来发展趋势。
   
   ${form.includeAnswers ? '*参考答案：当前${form.topic}面临的主要挑战包括：...，未来发展趋势可能是...*' : ''}`);
  }
  
  // 计算题示例
  if (form.types.includes('calculation')) {
    questions.push(`## 六、计算题

1. 计算问题示例...
   
   ${form.includeAnswers ? '*解答步骤：步骤1... 步骤2... 最终答案是...*' : ''}

2. 应用${form.topic}的原理解决以下问题...
   
   ${form.includeAnswers ? '*解答思路：首先分析问题... 然后应用公式... 最后得到结果...*' : ''}`);
  }
  
  return `# ${form.topic} - ${form.grade}习题集

## 基本信息
- **学科**: ${form.subject}
- **主题**: ${form.topic}
- **适用年级**: ${form.grade}
- **难度**: ${difficultyLevels.find(d => d.id === form.difficulty).label}
- **题目数量**: ${form.types.reduce((count, type) => count + (type === 'calculation' ? 2 : 2), 0)}题

${questions.join('\n\n')}

## 附录
- 本习题集适用于${form.grade}${form.subject}${form.topic}单元的课堂练习或课后作业
- 建议完成时间：约30-45分钟
- ${form.includeAnswers ? '答案和解析仅供教师参考' : '教师版中包含详细答案和解析'}`;
};

// 返回表单
const backToForm = () => {
  step.value = 1;
};

// 保存生成的资源
const saveResource = () => {
  emit('save', {
    type: props.type,
    title: props.type === 'lesson' ? `${lessonForm.topic} - 教案` : `${questionForm.topic} - 习题集`,
    content: generatedContent.value
  });
};

// 检查表单是否有效
const isFormValid = computed(() => {
  if (props.type === 'lesson') {
    return lessonForm.subject && lessonForm.topic && lessonForm.grade;
  } else {
    return questionForm.subject && questionForm.topic && questionForm.grade && 
           questionForm.types.length > 0;
  }
});

// 切换题型选择
const toggleQuestionType = (typeId) => {
  const index = questionForm.types.indexOf(typeId);
  if (index >= 0) {
    questionForm.types.splice(index, 1);
  } else {
    questionForm.types.push(typeId);
  }
};
</script>

<template>
  <div class="resource-generator">
    <div class="generator-header">
      <button class="back-btn" @click="emit('back')">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
        </svg>
        返回对话
      </button>
      
      <h2 class="generator-title">
        {{ props.type === 'lesson' ? '教案生成器' : '习题生成器' }}
      </h2>
      
      <button class="close-btn" @click="emit('close')">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
    </div>
    
    <!-- 步骤指示器 -->
    <div class="steps">
      <div 
        class="step"
        :class="{ 'active': step === 1, 'completed': step > 1 }"
      >
        <div class="step-number">1</div>
        <div class="step-name">填写信息</div>
      </div>
      
      <div class="step-line"></div>
      
      <div 
        class="step"
        :class="{ 'active': step === 2 }"
      >
        <div class="step-number">2</div>
        <div class="step-name">预览与保存</div>
      </div>
    </div>
    
    <!-- 表单步骤 -->
    <div v-if="step === 1" class="form-step">
      <!-- 教案表单 -->
      <div v-if="props.type === 'lesson'" class="lesson-form">
        <div class="form-row">
          <div class="form-group">
            <label>学科</label>
            <select v-model="lessonForm.subject">
              <option value="">请选择学科</option>
              <option v-for="subject in subjects" :key="subject" :value="subject">
                {{ subject }}
              </option>
              <option value="other">其他</option>
            </select>
          </div>
          
          <div class="form-group">
            <label>适用年级</label>
            <select v-model="lessonForm.grade">
              <option value="">请选择年级</option>
              <option v-for="grade in grades" :key="grade" :value="grade">
                {{ grade }}
              </option>
            </select>
          </div>
        </div>
        
        <div class="form-group">
          <label>教学主题</label>
          <input 
            type="text"
            v-model="lessonForm.topic"
            placeholder="例如：古代文学、物质的三态、分数加减法..."
          />
        </div>
        
        <div class="form-group">
          <label>课时时长（分钟）</label>
          <input type="number" v-model.number="lessonForm.duration" min="5" max="180" />
        </div>
        
        <div class="form-group">
          <label>教学目标（可选）</label>
          <textarea 
            v-model="lessonForm.objectives"
            rows="3"
            placeholder="描述本节课的知识目标、能力目标和情感目标..."
          ></textarea>
        </div>
        
        <div class="form-group">
          <label>教学准备（可选）</label>
          <textarea 
            v-model="lessonForm.resources"
            rows="3"
            placeholder="列出教学所需的设备、材料和资源..."
          ></textarea>
        </div>
        
        <div class="form-group">
          <label>教学反思（可选）</label>
          <textarea 
            v-model="lessonForm.notes"
            rows="3"
            placeholder="添加额外的教学建议或注意事项..."
          ></textarea>
        </div>
      </div>
      
      <!-- 习题表单 -->
      <div v-else class="question-form">
        <div class="form-row">
          <div class="form-group">
            <label>学科</label>
            <select v-model="questionForm.subject">
              <option value="">请选择学科</option>
              <option v-for="subject in subjects" :key="subject" :value="subject">
                {{ subject }}
              </option>
              <option value="other">其他</option>
            </select>
          </div>
          
          <div class="form-group">
            <label>适用年级</label>
            <select v-model="questionForm.grade">
              <option value="">请选择年级</option>
              <option v-for="grade in grades" :key="grade" :value="grade">
                {{ grade }}
              </option>
            </select>
          </div>
        </div>
        
        <div class="form-group">
          <label>主题</label>
          <input 
            type="text"
            v-model="questionForm.topic"
            placeholder="例如：几何证明、化学方程式、现代文阅读..."
          />
        </div>
        
        <div class="form-row">
          <div class="form-group">
            <label>题目数量（每类）</label>
            <input type="number" v-model.number="questionForm.quantity" min="1" max="20" />
          </div>
          
          <div class="form-group">
            <label>难度</label>
            <select v-model="questionForm.difficulty">
              <option v-for="level in difficultyLevels" :key="level.id" :value="level.id">
                {{ level.label }}
              </option>
            </select>
          </div>
        </div>
        
        <div class="form-group">
          <label>题目类型（至少选择一种）</label>
          <div class="question-types">
            <div 
              v-for="type in questionTypes" 
              :key="type.id"
              class="type-checkbox"
              :class="{ 'selected': questionForm.types.includes(type.id) }"
              @click="toggleQuestionType(type.id)"
            >
              <div class="checkbox">
                <svg v-if="questionForm.types.includes(type.id)" class="h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
                </svg>
              </div>
              {{ type.label }}
            </div>
          </div>
        </div>
        
        <div class="form-group">
          <div class="include-answers">
            <input 
              type="checkbox"
              id="includeAnswers"
              v-model="questionForm.includeAnswers"
            />
            <label for="includeAnswers">包含答案和解析</label>
          </div>
        </div>
      </div>
      
      <div class="form-actions">
        <button 
          class="generate-btn"
          :disabled="!isFormValid"
          @click="generateResource"
        >
          {{ loading ? '生成中...' : '生成内容' }}
          <svg v-if="loading" class="animate-spin h-4 w-4 ml-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
        </button>
      </div>
    </div>
    
    <!-- 预览步骤 -->
    <div v-else-if="step === 2" class="preview-step">
      <div class="preview-container">
        <div class="preview-content">
          <pre>{{ generatedContent }}</pre>
        </div>
      </div>
      
      <div class="preview-actions">
        <button class="back-form-btn" @click="backToForm">
          返回修改
        </button>
        
        <button class="save-btn" @click="saveResource">
          保存内容
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.resource-generator {
  @apply bg-white rounded-xl shadow-lg max-w-4xl w-full max-h-[90vh] flex flex-col;
}

.generator-header {
  @apply flex items-center justify-between p-4 border-b border-gray-200;
}

.back-btn {
  @apply flex items-center px-3 py-1 text-gray-600 hover:text-gray-800 text-sm;
}

.generator-title {
  @apply text-lg font-medium text-gray-800;
}

.close-btn {
  @apply p-1.5 rounded-full hover:bg-gray-100 text-gray-500 hover:text-gray-700;
}

.steps {
  @apply flex items-center justify-center px-6 py-4;
}

.step {
  @apply flex flex-col items-center;
}

.step-number {
  @apply h-8 w-8 rounded-full flex items-center justify-center text-gray-600 bg-white border-2 border-gray-300 font-medium;
}

.step.active .step-number {
  @apply bg-blue-600 text-white border-blue-600;
}

.step.completed .step-number {
  @apply bg-green-600 text-white border-green-600;
}

.step-line {
  @apply w-24 h-1 bg-gray-200 mx-2;
}

.step.active ~ .step-line {
  @apply bg-blue-200;
}

.step.completed ~ .step-line {
  @apply bg-green-200;
}

.step-name {
  @apply text-xs text-gray-500 mt-1;
}

.form-step, .preview-step {
  @apply flex-1 p-6 overflow-y-auto;
}

.form-row {
  @apply flex flex-col sm:flex-row gap-4 mb-4;
}

.form-group {
  @apply mb-4 flex-1;
}

.form-group label {
  @apply block text-sm font-medium text-gray-700 mb-1;
}

.form-group select, .form-group input[type="text"], .form-group input[type="number"], .form-group textarea {
  @apply w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all duration-200;
}

.question-types {
  @apply grid grid-cols-2 sm:grid-cols-3 gap-3 mt-2;
}

.type-checkbox {
  @apply flex items-center px-3 py-2 border border-gray-300 rounded-lg cursor-pointer hover:bg-gray-50 transition-colors;
}

.type-checkbox.selected {
  @apply bg-blue-50 border-blue-500;
}

.checkbox {
  @apply h-5 w-5 border border-gray-400 rounded mr-2 flex items-center justify-center text-blue-600;
}

.type-checkbox.selected .checkbox {
  @apply bg-blue-600 border-blue-600;
}

.include-answers {
  @apply flex items-center;
}

.include-answers input[type="checkbox"] {
  @apply h-4 w-4 text-blue-600 rounded mr-2;
}

.form-actions {
  @apply flex justify-end mt-6;
}

.generate-btn {
  @apply flex items-center px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed;
}

.preview-container {
  @apply bg-gray-50 rounded-lg p-4 mb-4 max-h-[calc(90vh-200px)] overflow-y-auto;
}

.preview-content {
  @apply font-mono text-sm whitespace-pre-wrap;
}

.preview-actions {
  @apply flex justify-between;
}

.back-form-btn {
  @apply px-6 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors;
}

.save-btn {
  @apply px-6 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors;
}

@media (max-width: 640px) {
  .form-row {
    @apply flex-col gap-2;
  }
  
  .question-types {
    @apply grid-cols-1;
  }
}
</style>