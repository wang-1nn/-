<script setup>
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';

// 题库列表
const questionList = ref([]);

// 筛选条件
const filters = reactive({
  subject: '',
  chapter: '',
  difficulty: '',
  type: ''
});

// 生成表单
const generationForm = reactive({
  subject: '计算机科学',
  chapter: '算法设计',
  count: 5,
  difficulty: 'medium',
  type: 'multiple_choice',
  topics: '',
  requirements: ''
});

// 题目类型选项
const questionTypes = [
  { label: '单选题', value: 'single_choice' },
  { label: '多选题', value: 'multiple_choice' },
  { label: '判断题', value: 'true_false' },
  { label: '填空题', value: 'fill_blank' },
  { label: '简答题', value: 'short_answer' },
  { label: '编程题', value: 'coding' }
];

// 难度选项
const difficultyOptions = [
  { label: '简单', value: 'easy' },
  { label: '中等', value: 'medium' },
  { label: '困难', value: 'hard' }
];

// 正在生成状态
const generating = ref(false);

// 生成进度
const generationProgress = ref(0);

// 抽屉可见状态
const drawerVisible = ref(false);
const currentQuestion = ref(null);

// 是否显示生成表单
const showGenerationForm = ref(false);

// 批次列表
const batchList = ref([
  {
    id: 1,
    name: '面向对象程序设计练习',
    createTime: '2023-06-15 10:30:22',
    count: 15,
    subject: '计算机科学'
  },
  {
    id: 2,
    name: '数据结构基础测试',
    createTime: '2023-06-12 14:20:45',
    count: 20,
    subject: '计算机科学'
  },
  {
    id: 3,
    name: '网络协议综合练习',
    createTime: '2023-05-28 09:15:30',
    count: 10,
    subject: '计算机网络'
  }
]);

// 监听页面加载
onMounted(() => {
  loadQuestionList();
});

// 加载题库列表
const loadQuestionList = () => {
  // 实际项目中应该发起API请求获取题目列表
  // 这里使用模拟数据
  setTimeout(() => {
    questionList.value = generateMockQuestions();
  }, 500);
};

// 生成模拟题目数据
const generateMockQuestions = () => {
  const types = ['single_choice', 'multiple_choice', 'true_false', 'fill_blank', 'short_answer', 'coding'];
  const difficulties = ['easy', 'medium', 'hard'];
  const subjects = ['计算机科学', '计算机网络', '数据库系统'];
  
  return Array.from({ length: 20 }, (_, i) => {
    const type = types[Math.floor(Math.random() * types.length)];
    const typeLabel = questionTypes.find(t => t.value === type)?.label || '未知类型';
    
    return {
      id: i + 1,
      title: `示例题目 ${i + 1}: ${getQuestionTitleByType(type)}`,
      type,
      typeLabel,
      difficulty: difficulties[Math.floor(Math.random() * difficulties.length)],
      subject: subjects[Math.floor(Math.random() * subjects.length)],
      chapter: `第${Math.floor(Math.random() * 10) + 1}章`,
      createTime: new Date(Date.now() - Math.random() * 10000000000).toLocaleString(),
      options: type.includes('choice') ? 
        ['选项A', '选项B', '选项C', '选项D'].map((opt, j) => ({ label: opt, value: String.fromCharCode(65 + j) })) : 
        [],
      answer: type === 'single_choice' ? 'A' : 
              type === 'multiple_choice' ? ['A', 'C'] : 
              type === 'true_false' ? '正确' : 
              type === 'fill_blank' ? '答案内容' :
              type === 'coding' ? 'function example() {\n  return "示例代码";\n}' :
              '这是一个简答题的答案示例。详细解释了相关概念和原理。'
    };
  });
};

// 根据题型生成标题
const getQuestionTitleByType = (type) => {
  switch (type) {
    case 'single_choice':
      return '下列关于算法复杂度的描述中，哪一项是正确的？';
    case 'multiple_choice':
      return '以下哪些排序算法的平均时间复杂度为O(nlogn)？';
    case 'true_false':
      return '快速排序的最坏时间复杂度是O(n²)。';
    case 'fill_blank':
      return '归并排序的空间复杂度为____。';
    case 'short_answer':
      return '简述动态规划的基本原理和适用条件。';
    case 'coding':
      return '实现一个函数，判断一个字符串是否是另一个字符串的子序列。';
    default:
      return '示例题目';
  }
};

// 开始批量生成题目
const startGenerateQuestions = async () => {
  if (generating.value) return;
  
  generating.value = true;
  generationProgress.value = 0;
  
  try {
    // 模拟生成进度
    const totalSteps = 10;
    for(let i = 1; i <= totalSteps; i++) {
      await new Promise(resolve => setTimeout(resolve, 500));
      generationProgress.value = (i / totalSteps) * 100;
    }
    
    // 实际项目中应该发起API请求生成题目
    /*
    const response = await post('/api/question/generate-batch', {
      ...generationForm
    }, null, null, null, true);
    */
    
    // 模拟新增题目
    const newQuestions = Array.from({ length: generationForm.count }, (_, i) => {
      return {
        id: questionList.value.length + i + 1,
        title: `新生成题目 ${i + 1}: ${getQuestionTitleByType(generationForm.type)}`,
        type: generationForm.type,
        typeLabel: questionTypes.find(t => t.value === generationForm.type)?.label || '未知类型',
        difficulty: generationForm.difficulty,
        subject: generationForm.subject,
        chapter: generationForm.chapter,
        createTime: new Date().toLocaleString(),
        options: generationForm.type.includes('choice') ? 
          ['选项A', '选项B', '选项C', '选项D'].map((opt, j) => ({ label: opt, value: String.fromCharCode(65 + j) })) : 
          [],
        answer: generationForm.type === 'single_choice' ? 'A' : 
                generationForm.type === 'multiple_choice' ? ['A', 'C'] : 
                generationForm.type === 'true_false' ? '正确' : 
                generationForm.type === 'fill_blank' ? '答案内容' :
                generationForm.type === 'coding' ? 'function example() {\n  return "示例代码";\n}' :
                '这是一个简答题的答案示例。详细解释了相关概念和原理。'
      };
    });
    
    // 添加到列表开头
    questionList.value = [...newQuestions, ...questionList.value];
    
    // 添加批次记录
    batchList.value = [{
      id: batchList.value.length + 1,
      name: `${generationForm.subject} - ${generationForm.chapter}`,
      createTime: new Date().toLocaleString(),
      count: generationForm.count,
      subject: generationForm.subject
    }, ...batchList.value];
    
    ElMessage.success(`成功生成 ${generationForm.count} 道题目`);
    showGenerationForm.value = false;
  } catch (error) {
    console.error('生成失败:', error);
    ElMessage.error('生成失败，请稍后重试');
  } finally {
    generating.value = false;
  }
};

// 查看题目详情
const viewQuestionDetail = (question) => {
  currentQuestion.value = question;
  drawerVisible.value = true;
};

// 删除题目
const deleteQuestion = (question) => {
  ElMessageBox.confirm('确定要删除这道题目吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 实际项目中应该发起API请求删除题目
    questionList.value = questionList.value.filter(item => item.id !== question.id);
    ElMessage.success('删除成功');
  }).catch(() => {});
};

// 编辑题目
const editQuestion = (question) => {
  // 实际项目中应该跳转到题目编辑页面
  ElMessage.info('功能开发中...');
};

// 添加到试卷
const addToExam = (question) => {
  // 实际项目中应该打开试卷选择对话框
  ElMessage.success('已添加到试卷');
};

// 重新生成题目
const regenerateQuestion = (question) => {
  ElMessageBox.confirm('确定要重新生成这道题目吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    // 实际项目中应该发起API请求重新生成
    ElMessage.success('重新生成请求已提交');
  }).catch(() => {});
};
</script>

<template>
  <div class="question-bank-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-title">
        <h2 class="text-xl font-bold">智能题库管理</h2>
        <span class="text-sm text-gray-500">利用人工智能高效生成、管理和组织题目</span>
      </div>
      <div class="page-actions">
        <el-button type="primary" @click="showGenerationForm = true">
          <i class="el-icon-plus mr-1"></i>批量生成题目
        </el-button>
      </div>
    </div>
    
    <!-- 内容区域 -->
    <el-card class="main-content">
      <div class="content-layout">
        <!-- 左侧批次列表 -->
        <div class="batch-list-panel">
          <div class="panel-header">
            <h3 class="text-base font-medium">生成批次</h3>
            <el-button type="text" size="small">查看全部</el-button>
          </div>
          <div class="batch-list">
            <div 
              v-for="batch in batchList" 
              :key="batch.id"
              class="batch-item"
            >
              <div class="item-header">
                <span class="item-title">{{ batch.name }}</span>
                <span class="item-badge">{{ batch.count }}题</span>
              </div>
              <div class="item-info">
                <span class="text-gray-500">{{ batch.subject }}</span>
                <span class="text-gray-400 text-xs">{{ batch.createTime }}</span>
              </div>
              <div class="item-actions">
                <el-button type="text" size="small">查看</el-button>
                <el-button type="text" size="small">编辑</el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧题库内容 -->
        <div class="question-list-panel">
          <!-- 筛选工具栏 -->
          <div class="filter-toolbar">
            <el-form :inline="true" :model="filters" size="small">
              <el-form-item>
                <el-input
                  v-model="filters.keyword"
                  placeholder="搜索题目"
                  prefix-icon="el-icon-search"
                />
              </el-form-item>
              <el-form-item>
                <el-select v-model="filters.subject" placeholder="学科">
                  <el-option label="全部" value=""></el-option>
                  <el-option label="计算机科学" value="计算机科学"></el-option>
                  <el-option label="计算机网络" value="计算机网络"></el-option>
                  <el-option label="数据库系统" value="数据库系统"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-select v-model="filters.type" placeholder="题型">
                  <el-option label="全部" value=""></el-option>
                  <el-option 
                    v-for="type in questionTypes" 
                    :key="type.value" 
                    :label="type.label" 
                    :value="type.value"
                  ></el-option>
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-select v-model="filters.difficulty" placeholder="难度">
                  <el-option label="全部" value=""></el-option>
                  <el-option 
                    v-for="diff in difficultyOptions" 
                    :key="diff.value" 
                    :label="diff.label" 
                    :value="diff.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-form>
          </div>
          
          <!-- 题目列表 -->
          <div class="question-list">
            <div 
              v-for="question in questionList" 
              :key="question.id"
              class="question-card"
              @click="viewQuestionDetail(question)"
            >
              <div class="question-header">
                <div class="question-type">
                  {{ question.typeLabel }}
                </div>
                <div class="question-difficulty" :class="{
                  'bg-green-100 text-green-800': question.difficulty === 'easy',
                  'bg-blue-100 text-blue-800': question.difficulty === 'medium',
                  'bg-red-100 text-red-800': question.difficulty === 'hard',
                }">
                  {{ 
                    question.difficulty === 'easy' ? '简单' : 
                    question.difficulty === 'medium' ? '中等' : 
                    '困难'
                  }}
                </div>
              </div>
              
              <div class="question-title">
                {{ question.title }}
              </div>
              
              <!-- 选择题选项 -->
              <div v-if="question.type.includes('choice')" class="question-options">
                <div v-for="option in question.options" :key="option.value" class="option-item">
                  <span class="option-label">{{ option.value }}.</span>
                  <span class="option-content">{{ option.label }}</span>
                </div>
              </div>
              
              <div class="question-footer">
                <div class="question-meta">
                  <span class="meta-item">{{ question.subject }}</span>
                  <span class="meta-item">{{ question.chapter }}</span>
                </div>
                
                <div class="question-actions">
                  <el-button type="text" size="small" @click.stop="editQuestion(question)">编辑</el-button>
                  <el-button type="text" size="small" @click.stop="addToExam(question)">加入试卷</el-button>
                  <el-button type="text" size="small" @click.stop="deleteQuestion(question)">删除</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
    
    <!-- 题目详情抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      title="题目详情"
      size="50%"
      :destroy-on-close="true"
    >
      <div v-if="currentQuestion" class="question-detail">
        <div class="detail-header">
          <div class="detail-meta">
            <el-tag size="small" effect="plain">{{ currentQuestion.typeLabel }}</el-tag>
            <el-tag 
              size="small" 
              effect="plain" 
              :type="
                currentQuestion.difficulty === 'easy' ? 'success' : 
                currentQuestion.difficulty === 'medium' ? 'primary' : 
                'danger'
              "
            >
              {{ 
                currentQuestion.difficulty === 'easy' ? '简单' : 
                currentQuestion.difficulty === 'medium' ? '中等' : 
                '困难'
              }}
            </el-tag>
            <span class="text-gray-500 text-sm">{{ currentQuestion.subject }} / {{ currentQuestion.chapter }}</span>
          </div>
          <div class="detail-actions">
            <el-button size="small" @click="editQuestion(currentQuestion)">编辑</el-button>
            <el-button size="small" @click="regenerateQuestion(currentQuestion)">重新生成</el-button>
            <el-button type="primary" size="small" @click="addToExam(currentQuestion)">加入试卷</el-button>
          </div>
        </div>
        
        <div class="detail-content">
          <div class="detail-title">
            <div class="title-number">题目ID: {{ currentQuestion.id }}</div>
            <div class="title-content">{{ currentQuestion.title }}</div>
          </div>
          
          <!-- 选择题选项 -->
          <div v-if="currentQuestion.type.includes('choice')" class="detail-options">
            <div v-for="option in currentQuestion.options" :key="option.value" class="detail-option-item">
              <div class="option-label" :class="{ 
                'option-correct': 
                  (currentQuestion.type === 'single_choice' && currentQuestion.answer === option.value) ||
                  (currentQuestion.type === 'multiple_choice' && currentQuestion.answer.includes(option.value))
              }">
                {{ option.value }}
              </div>
              <div class="option-content">{{ option.label }}</div>
            </div>
          </div>
          
          <!-- 答案 -->
          <div class="detail-answer">
            <div class="answer-label">参考答案</div>
            <div class="answer-content">
              <template v-if="currentQuestion.type === 'single_choice'">
                {{ currentQuestion.answer }}
              </template>
              <template v-else-if="currentQuestion.type === 'multiple_choice'">
                {{ currentQuestion.answer.join(', ') }}
              </template>
              <template v-else-if="currentQuestion.type === 'true_false'">
                {{ currentQuestion.answer }}
              </template>
              <template v-else-if="currentQuestion.type === 'fill_blank'">
                {{ currentQuestion.answer }}
              </template>
              <template v-else-if="currentQuestion.type === 'coding'">
                <pre class="code-block">{{ currentQuestion.answer }}</pre>
              </template>
              <template v-else>
                {{ currentQuestion.answer }}
              </template>
            </div>
          </div>
          
          <!-- 解析 -->
          <div class="detail-analysis">
            <div class="analysis-label">解析</div>
            <div class="analysis-content">
              这里是题目的详细解析，包含解题思路、知识点讲解等。
              <br><br>
              本题涉及的知识点：算法复杂度分析、排序算法特性、最优解法选择等。
            </div>
          </div>
        </div>
      </div>
    </el-drawer>
    
    <!-- 生成题目对话框 -->
    <el-dialog
      v-model="showGenerationForm"
      title="批量生成题目"
      width="500px"
    >
      <el-form 
        :model="generationForm" 
        label-width="100px"
        label-position="top"
      >
        <el-form-item label="学科">
          <el-input v-model="generationForm.subject" placeholder="请输入学科"></el-input>
        </el-form-item>
        <el-form-item label="章节">
          <el-input v-model="generationForm.chapter" placeholder="请输入章节"></el-input>
        </el-form-item>
        <el-form-item label="题目数量">
          <el-input-number v-model="generationForm.count" :min="1" :max="20"></el-input-number>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="generationForm.difficulty" placeholder="请选择难度" style="width: 100%">
            <el-option 
              v-for="item in difficultyOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="题目类型">
          <el-select v-model="generationForm.type" placeholder="请选择题目类型" style="width: 100%">
            <el-option 
              v-for="item in questionTypes"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="知识点/主题">
          <el-input 
            v-model="generationForm.topics" 
            type="textarea" 
            :rows="2"
            placeholder="请输入知识点或主题，多个用逗号分隔"
          ></el-input>
        </el-form-item>
        <el-form-item label="特殊要求">
          <el-input 
            v-model="generationForm.requirements" 
            type="textarea" 
            :rows="2"
            placeholder="有特殊要求可以在这里说明"
          ></el-input>
        </el-form-item>
      </el-form>
      
      <div v-if="generating" class="generation-progress">
        <el-progress :percentage="generationProgress" :format="format => `${format}%`"></el-progress>
        <div class="text-center text-sm text-gray-500 mt-2">
          正在生成题目，请稍候...
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showGenerationForm = false">取消</el-button>
          <el-button 
            type="primary" 
            :loading="generating"
            @click="startGenerateQuestions"
          >开始生成</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.question-bank-container {
  padding: 0 0 20px 0;
}

.page-header {
  @apply flex justify-between items-center mb-6;
}

.page-title {
  @apply flex flex-col;
}

.main-content {
  min-height: calc(100vh - 200px);
}

.content-layout {
  @apply flex gap-6;
  height: calc(100vh - 230px);
}

/* 批次列表面板 */
.batch-list-panel {
  @apply w-64 border-r border-gray-200 pr-4;
}

.panel-header {
  @apply flex justify-between items-center mb-4;
}

.batch-list {
  @apply space-y-4 overflow-auto;
  max-height: calc(100vh - 280px);
}

.batch-item {
  @apply border border-gray-200 rounded-md p-3 cursor-pointer hover:border-blue-300 hover:bg-blue-50;
}

.item-header {
  @apply flex justify-between items-center mb-2;
}

.item-title {
  @apply font-medium;
}

.item-badge {
  @apply bg-blue-100 text-blue-800 text-xs px-2 py-1 rounded-full;
}

.item-info {
  @apply flex justify-between items-center mb-2;
}

.item-actions {
  @apply flex justify-end space-x-2;
}

/* 题库列表面板 */
.question-list-panel {
  @apply flex-1 overflow-hidden flex flex-col;
}

.filter-toolbar {
  @apply mb-4 pb-4 border-b border-gray-200;
}

.question-list {
  @apply grid gap-4 overflow-y-auto;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  max-height: calc(100vh - 350px);
}

.question-card {
  @apply border border-gray-200 rounded-lg p-4 cursor-pointer hover:border-blue-300 hover:shadow-sm transition-all;
}

.question-header {
  @apply flex justify-between items-center mb-3;
}

.question-type {
  @apply bg-gray-100 text-gray-700 px-2 py-1 rounded text-xs;
}

.question-difficulty {
  @apply px-2 py-1 rounded text-xs;
}

.question-title {
  @apply text-base mb-3;
}

.question-options {
  @apply space-y-2 mb-3;
}

.option-item {
  @apply flex text-sm;
}

.option-label {
  @apply mr-2 font-medium;
}

.question-footer {
  @apply flex justify-between items-center mt-3 pt-3 border-t border-gray-200;
}

.question-meta {
  @apply flex items-center;
}

.meta-item {
  @apply text-xs text-gray-500 mr-2;
}

.question-actions {
  @apply opacity-0 transition-opacity;
}

.question-card:hover .question-actions {
  @apply opacity-100;
}

/* 详情抽屉 */
.question-detail {
  @apply p-6;
}

.detail-header {
  @apply flex justify-between items-center mb-6 pb-4 border-b border-gray-200;
}

.detail-meta {
  @apply space-x-2 flex items-center;
}

.detail-content {
  @apply space-y-6;
}

.detail-title {
  @apply space-y-2;
}

.title-number {
  @apply text-sm text-gray-500;
}

.title-content {
  @apply text-lg font-medium;
}

.detail-options {
  @apply space-y-3 mt-4;
}

.detail-option-item {
  @apply flex;
}

.option-label {
  @apply w-8 h-8 rounded-full flex items-center justify-center text-gray-700 border border-gray-300 mr-3;
}

.option-label.option-correct {
  @apply bg-green-500 text-white border-green-500;
}

.detail-answer {
  @apply bg-blue-50 p-4 rounded-md;
}

.answer-label {
  @apply text-blue-800 font-medium mb-2;
}

.detail-analysis {
  @apply bg-gray-50 p-4 rounded-md;
}

.analysis-label {
  @apply text-gray-700 font-medium mb-2;
}

.code-block {
  @apply bg-gray-100 p-3 rounded font-mono text-sm whitespace-pre-wrap;
}

/* 生成进度 */
.generation-progress {
  @apply mt-6;
}
</style>