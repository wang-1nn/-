<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import QuestionEditor from './components/QuestionEditor.vue';
import QuestionDetail from './components/QuestionDetail.vue';
import {
  getTeacherCoursesAndClasses,
  getQuestionsForSelection,
  createExam,
  getExamDetail,
  getQuestionSubjects
} from '@/api/examManagement';
import { useAuthStore } from '@/stores/counter';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

// 加载考试数据
const loadExam = (examId) => {
  return new Promise((resolve, reject) => {
    if (examId === 'new') {
      resolve({
        id: '',
        title: '',
        description: '',
        courseId: '',
        classId: '',
        examType: '考试',
        duration: 90,
        startTime: '',
        endTime: '',
        questions: []
      });
      return;
    }

    const uid = authStore.user?.userId || '1';
    getExamDetail(
      examId,
      uid,
      (message, data) => {
        console.log('获取考试详情成功:', data);
        // 转换数据格式以适配前端
        const examData = {
          id: data.id,
          title: data.title,
          description: data.description || '',
          courseId: data.courseId,
          classIds: data.classId ? [data.classId] : [],
          examType: data.examType || '测验',
          duration: data.duration || 60,
          startTime: data.startTime ? new Date(data.startTime).toISOString().slice(0, 16) : '',
          endTime: data.endTime ? new Date(data.endTime).toISOString().slice(0, 16) : '',
          passingScore: 60,
          showResults: true,
          questions: data.questions || []
        };
        resolve(examData);
      },
      (message) => {
        console.error('获取考试详情失败:', message);
        reject(new Error(message));
      }
    );
  });
};


// 状态变量
const loading = ref(true);
const saving = ref(false);
const examId = ref(route.params.id || 'new');
const isNewExam = computed(() => examId.value === 'new');
const showPreview = ref(false);
const currentStep = ref(1);
const editingQuestion = ref(null);
const showQuestionEditor = ref(false);
const editingIndex = ref(-1);
const showQuestionSelector = ref(false);
// 使用Map来管理选择状态，key为questionId，value为题目数据
const selectedQuestionsMap = reactive(new Map());
const questionFilter = reactive({
  subject: ''
});

// 考试数据
const exam = reactive({
  id: '',
  title: '',
  description: '',
  courseId: null,
  classIds: [],
  examType: '测验',
  duration: 60,
  startTime: '',
  endTime: '',
  passingScore: 60,
  showResults: true,
  questions: []
});

// 课程班级数据
const coursesAndClasses = ref([]);
const availableQuestions = ref([]);
const questionSubjects = ref([]); // 题目学科列表
const questionsPagination = reactive({
  page: 1,
  size: 20,
  total: 0,
  loading: false
});

// 获取用户ID
const getUserId = () => {
  return authStore.user?.userId || '1'; // 从authStore获取用户ID
};

// 初始化表单验证
const errors = reactive({
  title: '',
  course: '',
  duration: '',
  passingScore: '',
  questions: ''
});

// 步骤定义
const steps = [
  { id: 1, name: '基本信息' },
  { id: 2, name: '题目编辑' },
  { id: 3, name: '设置与预览' }
];

// 加载课程班级数据
const loadCoursesAndClasses = () => {
  const uid = getUserId();
  getTeacherCoursesAndClasses(uid,
    (message, data) => {
      coursesAndClasses.value = data || [];
    },
    (message) => {
      console.error('加载课程班级失败:', message);
    }
  );
};

// 加载题目学科列表
const loadQuestionSubjects = () => {
  const uid = getUserId();
  getQuestionSubjects(uid,
    (message, data) => {
      questionSubjects.value = data || [];
    },
    (message) => {
      console.error('加载题目学科列表失败:', message);
    }
  );
};

// 加载题目数据
const loadQuestions = (subject = null) => {
  const subjectToSearch = subject || questionFilter.subject;

  // 如果没有科目，不加载题目
  if (!subjectToSearch || subjectToSearch.trim() === '') {
    availableQuestions.value = [];
    questionsPagination.loading = false;
    return;
  }

  questionsPagination.loading = true;
  const uid = getUserId();

  getQuestionsForSelection(
    uid,
    subjectToSearch.trim(),
    questionsPagination.page,
    questionsPagination.size,
    (message, data) => {
      availableQuestions.value = data || [];
      questionsPagination.loading = false;
    },
    (message) => {
      console.error('加载题目失败:', message);
      availableQuestions.value = [];
      questionsPagination.loading = false;
    }
  );
};

// 初始化考试数据的函数
const initializeExamData = async () => {
  try {
    loading.value = true;

    // 重置考试数据
    Object.assign(exam, {
      id: '',
      title: '',
      description: '',
      courseId: null,
      classIds: [],
      examType: '测验',
      duration: 60,
      startTime: '',
      endTime: '',
      passingScore: 60,
      showResults: true,
      questions: []
    });

    // 加载课程班级数据
    loadCoursesAndClasses();
    // 加载题目学科列表
    loadQuestionSubjects();

    if (isNewExam.value) {
      // 新建考试，不自动加载题目数据
      loading.value = false;
    } else {
      // 编辑现有考试
      try {
        const data = await loadExam(examId.value);
        Object.assign(exam, data);
        loading.value = false;
      } catch (error) {
        console.error('加载考试数据失败:', error);
        alert('加载考试数据失败: ' + error.message);
        router.push('/teacher/exam-assessment-module/exams');
      }
    }
  } catch (error) {
    console.error('加载考试失败', error);
    loading.value = false;
  }
};

// 监听路由参数变化，重新加载数据
watch(() => route.params.id, (newId, oldId) => {
  if (newId !== oldId) {
    examId.value = newId || 'new';
    initializeExamData();
  }
}, { immediate: false });

// 初始加载考试数据
onMounted(() => {
  initializeExamData();
});

// 计算总分
const totalScore = computed(() => {
  return exam.questions.reduce((sum, q) => sum + q.score, 0);
});

// 计算题目数量
const questionCount = computed(() => {
  return exam.questions.length;
});

// 获取选中课程的班级列表
const selectedCourseClasses = computed(() => {
  if (!exam.courseId) return [];
  const course = coursesAndClasses.value.find(c => c.courseId === exam.courseId);
  return course ? course.classes || [] : [];
});

// 计算选中题目数量
const selectedQuestionCount = computed(() => {
  return selectedQuestionsMap.size;
});

// 计算选中题目列表
const selectedQuestionsList = computed(() => {
  return Array.from(selectedQuestionsMap.values());
});

// 计算选中题目ID集合
const selectedQuestionIds = computed(() => {
  return new Set(selectedQuestionsMap.keys());
});

// 检查题目是否被选中
const isQuestionSelected = (question) => {
  return selectedQuestionIds.value.has(question.questionId);
};

// 课程变化处理
const onCourseChange = () => {
  // 清空已选班级
  exam.classIds = [];
};

// 校验表单
const validateForm = (step) => {
  let isValid = true;
  
  // 重置错误
  Object.keys(errors).forEach(key => errors[key] = '');
  
  if (step === 1) {
    if (!exam.title.trim()) {
      errors.title = '考试标题不能为空';
      isValid = false;
    }
    
    if (!exam.courseId) {
      errors.course = '请选择课程';
      isValid = false;
    }

    if (!exam.classIds || exam.classIds.length === 0) {
      errors.classes = '请选择至少一个班级';
      isValid = false;
    }
    
    if (!exam.duration || exam.duration <= 0) {
      errors.duration = '考试时长必须大于0';
      isValid = false;
    }
    
    if (!exam.passingScore || exam.passingScore < 0 || exam.passingScore > 100) {
      errors.passingScore = '及格分数必须在0-100之间';
      isValid = false;
    }
  }
  
  if (step === 2 || step === 3) {
    if (exam.questions.length === 0) {
      errors.questions = '考试至少需要一道题目';
      isValid = false;
    }
  }
  
  return isValid;
};

// 下一步
const nextStep = () => {
  if (validateForm(currentStep.value)) {
    currentStep.value++;
  }
};

// 上一步
const prevStep = () => {
  currentStep.value--;
};

// 打开题目编辑器
const openQuestionEditor = (question = null, index = -1) => {
  editingQuestion.value = question ? {...question} : null;
  editingIndex.value = index;
  showQuestionEditor.value = true;
};

// 打开题目选择器
const openQuestionSelector = () => {
  selectedQuestionsMap.clear();
  questionFilter.subject = '';
  questionsPagination.page = 1;
  showQuestionSelector.value = true;
  // 不自动加载题目数据，等待用户输入科目
};

// 关闭题目编辑器
const closeQuestionEditor = () => {
  editingQuestion.value = null;
  editingIndex.value = -1;
  showQuestionEditor.value = false;
};

// 关闭题目选择器
const closeQuestionSelector = () => {
  showQuestionSelector.value = false;
  selectedQuestionsMap.clear();
};

// 添加选中的题目
const addSelectedQuestions = () => {
  if (selectedQuestionsMap.size === 0) {
    alert('请先选择题目');
    return;
  }

  const selectedList = Array.from(selectedQuestionsMap.values());
  console.log('开始添加选中题目，数量:', selectedList.length);
  console.log('选中的题目:', selectedList);

  const addedCount = selectedList.length;

  selectedList.forEach(question => {
    const newQuestion = {
      id: question.questionId,
      questionId: question.questionId,
      type: question.questionType,
      content: question.content,
      options: question.options || [],
      answer: question.answer,
      analysis: question.analysis,
      score: 5, // 默认分数
      difficulty: question.difficulty,
      subject: question.subject
    };
    exam.questions.push(newQuestion);
    console.log('添加题目:', newQuestion);
  });

  console.log('考试题目总数:', exam.questions.length);

  // 显示成功提示
  alert(`成功添加 ${addedCount} 道题目到考试中！`);

  closeQuestionSelector();
};

// 切换题目选择状态
const toggleQuestionSelection = (question) => {
  console.log('=== 切换题目选择 ===');
  console.log('题目ID:', question.questionId);
  console.log('题目内容:', question.content);
  console.log('当前选中数量:', selectedQuestionsMap.size);

  if (selectedQuestionsMap.has(question.questionId)) {
    // 取消选择
    selectedQuestionsMap.delete(question.questionId);
    console.log('✅ 取消选择题目:', question.questionId);
  } else {
    // 添加选择
    const questionToAdd = {
      questionId: question.questionId,
      questionType: question.questionType,
      content: question.content,
      subject: question.subject,
      difficulty: question.difficulty,
      score: question.score || 5,
      analysis: question.analysis || '',
      options: question.options || '',
      answer: question.answer || '',
      isAiGenerated: question.isAiGenerated || false
    };
    selectedQuestionsMap.set(question.questionId, questionToAdd);
    console.log('✅ 选择题目:', question.questionId);
    console.log('添加的题目数据:', questionToAdd);
  }

  console.log('操作后选中数量:', selectedQuestionsMap.size);
  console.log('选中的题目ID列表:', Array.from(selectedQuestionsMap.keys()));
  console.log('=== 切换完成 ===');
};

// 加载上一页
const loadPreviousPage = () => {
  if (questionsPagination.page > 1) {
    questionsPagination.page--;
    loadQuestions();
  }
};

// 加载下一页
const loadNextPage = () => {
  questionsPagination.page++;
  loadQuestions();
};

// 筛选条件变化处理
const onFilterChange = () => {
  questionsPagination.page = 1; // 重置到第一页
  loadQuestions();
};

// 清空筛选条件
const clearFilters = () => {
  questionFilter.subject = '';
  questionsPagination.page = 1;
  availableQuestions.value = [];
};

// 获取题目类型标签
const getQuestionTypeLabel = (type) => {
  const typeMap = {
    'single': '单选题',
    'multiple': '多选题',
    'true_false': '判断题',
    'fill': '填空题',
    'short': '简答题',
    'essay': '论述题',
    'programming': '编程题'
  };
  return typeMap[type] || type;
};

// 预览题目
const previewQuestion = (question) => {
  // 可以实现题目预览功能
  console.log('预览题目:', question);
};

// 快速选择科目
const selectSubject = (subject) => {
  questionFilter.subject = subject;
  questionsPagination.page = 1;
  loadQuestions(subject);
};

// 保存题目
const saveQuestion = (question) => {
  if (editingIndex.value >= 0) {
    // 编辑现有题目
    exam.questions[editingIndex.value] = {
      ...exam.questions[editingIndex.value],
      ...question
    };
  } else {
    // 添加新题目
    exam.questions.push({
      id: `temp_${Date.now()}`, // 临时ID，实际应由后端生成
      ...question
    });
  }
  
  closeQuestionEditor();
};

// 删除题目
const deleteQuestion = (index) => {
  if (confirm('确定要删除这道题目吗？')) {
    exam.questions.splice(index, 1);
  }
};

// 上移题目
const moveQuestionUp = (index) => {
  if (index > 0) {
    [exam.questions[index], exam.questions[index - 1]] = [exam.questions[index - 1], exam.questions[index]];
  }
};

// 下移题目
const moveQuestionDown = (index) => {
  if (index < exam.questions.length - 1) {
    [exam.questions[index], exam.questions[index + 1]] = [exam.questions[index + 1], exam.questions[index]];
  }
};

// 复制题目
const duplicateQuestion = (index) => {
  const question = {...exam.questions[index]};
  question.id = `temp_${Date.now()}`;
  exam.questions.splice(index + 1, 0, question);
};

// 切换预览模式
const togglePreview = () => {
  showPreview.value = !showPreview.value;
};

// 保存考试
const saveExam = async () => {
  if (!validateForm(currentStep.value)) {
    return;
  }

  saving.value = true;

  try {
    const uid = getUserId();

    // 构建考试数据
    const examData = {
      title: exam.title,
      description: exam.description,
      courseId: exam.courseId,
      classIds: exam.classIds,
      examType: exam.examType,
      startTime: exam.startTime || null,
      endTime: exam.endTime || null,
      duration: exam.duration,
      passingScore: exam.passingScore,
      showResults: exam.showResults,
      questions: exam.questions.map((q, index) => ({
        questionId: q.questionId || q.id,
        score: q.score || 5,
        orderNum: index + 1
      }))
    };

    console.log('保存考试数据:', examData);

    createExam(uid, examData,
      (message, data) => {
        console.log('考试创建成功:', message, data);
        // 成功后跳转
        router.push('/teacher/exam-assessment-module/exams');
      },
      (message) => {
        console.error('保存失败:', message);
        alert('保存失败: ' + message);
      }
    );
  } catch (error) {
    console.error('保存失败', error);
    alert('保存失败: ' + error.message);
  } finally {
    saving.value = false;
  }
};

// 取消编辑
const cancelEdit = () => {
  if (confirm('确定要取消编辑吗？未保存的更改将丢失。')) {
    router.push('/teacher/exam-assessment-module/exams');
  }
};
</script>

<template>
  <div class="exam-editor">
    <div class="editor-header">
      <h2 class="editor-title">{{ isNewExam ? '创建新考试' : '编辑考试' }}</h2>
      <div class="editor-actions" v-if="!loading">
        <button 
          type="button" 
          class="cancel-btn"
          @click="cancelEdit"
          :disabled="saving"
        >
          取消
        </button>
        
        <button 
          v-if="currentStep === steps.length" 
          type="button" 
          class="save-btn"
          @click="saveExam"
          :disabled="saving"
        >
          {{ saving ? '保存中...' : '保存考试' }}
        </button>
      </div>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>加载考试数据中...</p>
    </div>
    
    <div v-else>
      <!-- 步骤指示器 -->
      <div class="steps">
        <div 
          v-for="step in steps" 
          :key="step.id"
          class="step"
          :class="{ 
            'active': currentStep === step.id,
            'completed': currentStep > step.id 
          }"
          @click="currentStep = step.id"
        >
          <div class="step-number">{{ step.id }}</div>
          <div class="step-name">{{ step.name }}</div>
        </div>
      </div>
      
      <!-- 步骤1：基本信息 -->
      <div v-if="currentStep === 1" class="step-content">
        <div class="form-section">
          <div class="form-group">
            <label>考试标题</label>
            <input 
              v-model="exam.title" 
              type="text" 
              class="form-control"
              :class="{ 'is-invalid': errors.title }"
              placeholder="例如：期中测试、JavaScript基础考试..."
            />
            <div v-if="errors.title" class="error-message">{{ errors.title }}</div>
          </div>
          
          <div class="form-group">
            <label>课程</label>
            <select
              v-model="exam.courseId"
              class="form-control"
              :class="{ 'is-invalid': errors.course }"
              @change="onCourseChange"
            >
              <option value="">请选择课程</option>
              <option
                v-for="course in coursesAndClasses"
                :key="course.courseId"
                :value="course.courseId"
              >
                {{ course.courseName }} ({{ course.subject }})
              </option>
            </select>
            <div v-if="errors.course" class="error-message">{{ errors.course }}</div>
          </div>

          <div class="form-group" v-if="exam.courseId">
            <label>班级</label>
            <div class="checkbox-group">
              <div
                v-for="classInfo in selectedCourseClasses"
                :key="classInfo.classId"
                class="checkbox-item"
              >
                <input
                  type="checkbox"
                  :id="`class-${classInfo.classId}`"
                  :value="classInfo.classId"
                  v-model="exam.classIds"
                />
                <label :for="`class-${classInfo.classId}`">
                  {{ classInfo.className }} ({{ classInfo.grade }} - {{ classInfo.major }})
                </label>
              </div>
            </div>
            <div v-if="errors.classes" class="error-message">{{ errors.classes }}</div>
          </div>

          <div class="form-group">
            <label>考试类型</label>
            <select
              v-model="exam.examType"
              class="form-control"
            >
              <option value="作业">作业</option>
              <option value="测验">测验</option>
              <option value="考试">考试</option>
            </select>
          </div>
          
          <div class="form-group">
            <label>考试说明（可选）</label>
            <textarea 
              v-model="exam.description" 
              class="form-control"
              rows="3"
              placeholder="输入考试说明、要求或注意事项..."
            ></textarea>
          </div>
          
          <div class="form-row">
            <div class="form-group half">
              <label>考试时长（分钟）</label>
              <input 
                v-model.number="exam.duration" 
                type="number" 
                class="form-control"
                :class="{ 'is-invalid': errors.duration }"
                min="1"
              />
              <div v-if="errors.duration" class="error-message">{{ errors.duration }}</div>
            </div>
            
            <div class="form-group half">
              <label>及格分数</label>
              <input 
                v-model.number="exam.passingScore" 
                type="number" 
                class="form-control"
                :class="{ 'is-invalid': errors.passingScore }"
                min="0"
                max="100"
              />
              <div v-if="errors.passingScore" class="error-message">{{ errors.passingScore }}</div>
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group half">
              <label>开始时间</label>
              <input 
                v-model="exam.startTime" 
                type="datetime-local" 
                class="form-control"
              />
            </div>
            
            <div class="form-group half">
              <label>结束时间</label>
              <input 
                v-model="exam.endTime" 
                type="datetime-local" 
                class="form-control"
              />
            </div>
          </div>
        </div>
        
        <div class="step-actions">
          <button type="button" class="next-btn" @click="nextStep">下一步：编辑题目</button>
        </div>
      </div>
      
      <!-- 步骤2：题目编辑 -->
      <div v-if="currentStep === 2" class="step-content">
        <div class="questions-header">
          <div class="questions-summary">
            <span>共 {{ questionCount }} 道题目</span>
            <span class="mx-2">|</span>
            <span>总分 {{ totalScore }} 分</span>
          </div>
          
          <div class="questions-actions">
            <button type="button" class="add-question-btn" @click="openQuestionEditor()">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
              </svg>
              新建题目
            </button>
            <button type="button" class="select-question-btn" @click="openQuestionSelector()">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
              </svg>
              从题库选择
            </button>
          </div>
        </div>
        
        <div v-if="errors.questions" class="error-message mb-4">{{ errors.questions }}</div>
        
        <!-- 题目列表 -->
        <div v-if="exam.questions.length > 0" class="questions-list">
          <div 
            v-for="(question, index) in exam.questions" 
            :key="question.id"
            class="question-item"
          >
            <div class="question-number">{{ index + 1 }}</div>
            
            <div class="question-content">
              <QuestionDetail 
                :question="question" 
                :showAnswer="true"
                :showAnalysis="true"
              />
            </div>
            
            <div class="question-actions">
              <button 
                type="button" 
                class="action-btn" 
                title="编辑"
                @click="openQuestionEditor(question, index)"
              >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                </svg>
              </button>
              
              <button 
                type="button" 
                class="action-btn" 
                title="复制"
                @click="duplicateQuestion(index)"
              >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7v8a2 2 0 002 2h6M8 7V5a2 2 0 012-2h4.586a1 1 0 01.707.293l4.414 4.414a1 1 0 01.293.707V15a2 2 0 01-2 2h-2M8 7H6a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2v-2" />
                </svg>
              </button>
              
              <button 
                type="button" 
                class="action-btn" 
                title="上移"
                :disabled="index === 0"
                @click="moveQuestionUp(index)"
              >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 15l7-7 7 7" />
                </svg>
              </button>
              
              <button 
                type="button" 
                class="action-btn" 
                title="下移"
                :disabled="index === exam.questions.length - 1"
                @click="moveQuestionDown(index)"
              >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
                </svg>
              </button>
              
              <button 
                type="button" 
                class="action-btn delete" 
                title="删除"
                @click="deleteQuestion(index)"
              >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                </svg>
              </button>
            </div>
          </div>
        </div>
        
        <!-- 空题目提示 -->
        <div v-else class="empty-questions">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
          </svg>
          <p>当前考试没有题目。点击上方"添加题目"按钮开始创建。</p>
        </div>
        
        <div class="step-actions">
          <button type="button" class="back-btn" @click="prevStep">返回</button>
          <button type="button" class="next-btn" @click="nextStep">下一步：设置与预览</button>
        </div>
      </div>
      
      <!-- 步骤3：设置与预览 -->
      <div v-if="currentStep === 3" class="step-content">
        <div class="settings-section">
          <h3 class="section-title">考试设置</h3>
          
          <div class="form-group">
            <div class="checkbox">
              <input type="checkbox" id="showResults" v-model="exam.showResults" />
              <label for="showResults">考试结束后立即显示成绩</label>
            </div>
          </div>
          
          <div class="summary-section">
            <h3 class="section-title">考试摘要</h3>
            
            <div class="summary-item">
              <div class="label">考试标题</div>
              <div class="value">{{ exam.title }}</div>
            </div>
            
            <div class="summary-item">
              <div class="label">课程</div>
              <div class="value">{{ exam.course }}</div>
            </div>
            
            <div class="summary-item">
              <div class="label">考试时长</div>
              <div class="value">{{ exam.duration }}分钟</div>
            </div>
            
            <div class="summary-item">
              <div class="label">题目数量</div>
              <div class="value">{{ questionCount }}题</div>
            </div>
            
            <div class="summary-item">
              <div class="label">总分</div>
              <div class="value">{{ totalScore }}分</div>
            </div>
            
            <div class="summary-item">
              <div class="label">及格分数</div>
              <div class="value">{{ exam.passingScore }}分</div>
            </div>
            
            <div v-if="exam.startTime" class="summary-item">
              <div class="label">开始时间</div>
              <div class="value">{{ new Date(exam.startTime).toLocaleString() }}</div>
            </div>
            
            <div v-if="exam.endTime" class="summary-item">
              <div class="label">结束时间</div>
              <div class="value">{{ new Date(exam.endTime).toLocaleString() }}</div>
            </div>
          </div>
        </div>
        
        <div class="preview-section">
          <h3 class="section-title">
            考试预览
            <button type="button" class="toggle-preview" @click="togglePreview">
              {{ showPreview ? '隐藏答案' : '显示答案' }}
            </button>
          </h3>
          
          <div class="preview-exam">
            <div class="exam-header">
              <h2>{{ exam.title }}</h2>
              <p v-if="exam.description">{{ exam.description }}</p>
              <div class="exam-meta">
                <div class="meta-item">
                  <span class="meta-label">时长：</span>
                  <span>{{ exam.duration }}分钟</span>
                </div>
                
                <div class="meta-item">
                  <span class="meta-label">总分：</span>
                  <span>{{ totalScore }}分</span>
                </div>
                
                <div class="meta-item">
                  <span class="meta-label">及格分数：</span>
                  <span>{{ exam.passingScore }}分</span>
                </div>
              </div>
            </div>
            
            <div class="preview-questions">
              <div v-for="(question, index) in exam.questions" :key="question.id" class="preview-question">
                <div class="question-header">
                  <div class="question-index">题目 {{ index + 1 }}</div>
                  <div class="question-score">{{ question.score }}分</div>
                </div>
                
                <QuestionDetail 
                  :question="question" 
                  :showAnswer="showPreview"
                  :showAnalysis="showPreview"
                />
              </div>
            </div>
          </div>
        </div>
        
        <div class="step-actions">
          <button type="button" class="back-btn" @click="prevStep">返回</button>
          <button type="button" class="save-btn" @click="saveExam" :disabled="saving">
            {{ saving ? '保存中...' : '保存考试' }}
          </button>
        </div>
      </div>
      
      <!-- 题目编辑器弹窗 -->
      <div v-if="showQuestionEditor" class="question-editor-modal">
        <div class="modal-overlay" @click="closeQuestionEditor"></div>
        <div class="modal-container">
          <div class="modal-header">
            <h3>{{ editingQuestion ? '编辑题目' : '添加题目' }}</h3>
            <button type="button" class="close-btn" @click="closeQuestionEditor">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
          <div class="modal-body">
            <QuestionEditor 
              :initialQuestion="editingQuestion"
              :isNew="editingIndex < 0"
              @save="saveQuestion"
              @cancel="closeQuestionEditor"
            />
          </div>
        </div>
      </div>

      <!-- 题目选择器弹窗 -->
      <div v-if="showQuestionSelector" class="question-selector-modal">
        <div class="modal-overlay" @click="closeQuestionSelector"></div>
        <div class="modal-container large">
          <div class="modal-header">
            <h3>从题库选择题目</h3>
            <button type="button" class="close-btn" @click="closeQuestionSelector">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
          <div class="modal-body">
            <!-- 科目搜索 -->
            <div class="filter-section">
              <div class="filter-header">
                <h4>科目搜索</h4>
                <button
                  type="button"
                  class="clear-filters-btn"
                  @click="clearFilters"
                  v-if="questionFilter.subject"
                >
                  清空搜索
                </button>
              </div>

              <div class="search-controls">
                <div class="search-group">
                  <label>
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                    </svg>
                    请输入科目名称
                  </label>
                  <input
                    v-model="questionFilter.subject"
                    type="text"
                    placeholder="例如：数学、语文、英语..."
                    @input="onFilterChange"
                    class="subject-input"
                  />
                  <div class="search-hint">
                    <p>💡 提示：输入科目名称后自动搜索相关题目</p>
                  </div>
                </div>

                <!-- 快速选择学科 -->
                <div class="quick-subjects" v-if="questionSubjects.length > 0">
                  <label>快速选择：</label>
                  <div class="subject-tags">
                    <button
                      v-for="subject in questionSubjects"
                      :key="subject"
                      type="button"
                      class="subject-tag-btn"
                      :class="{ 'active': questionFilter.subject === subject }"
                      @click="selectSubject(subject)"
                    >
                      {{ subject }}
                    </button>
                  </div>
                </div>
              </div>

              <!-- 搜索结果统计 -->
              <div class="search-stats" v-if="questionFilter.subject && !questionsPagination.loading">
                <span class="stats-text">
                  找到 <strong>{{ availableQuestions.length }}</strong> 道"{{ questionFilter.subject }}"相关题目
                </span>
              </div>

              <!-- 选择状态提示 -->
              <div class="selection-status" v-if="availableQuestions.length > 0">
                <div class="status-info">
                  <span v-if="selectedQuestionCount === 0" class="no-selection">
                    💡 点击题目卡片进行选择
                  </span>
                  <span v-else class="has-selection">
                    ✅ 已选择 <strong>{{ selectedQuestionCount }}</strong> 道题目
                  </span>
                </div>
              </div>
            </div>

            <!-- 题目列表 -->
            <div v-if="questionsPagination.loading" class="loading-questions">
              <div class="loading-spinner"></div>
              <p>加载题目中...</p>
            </div>

            <div v-else-if="availableQuestions.length === 0" class="empty-questions">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
              </svg>
              <p>没有找到符合条件的题目</p>
              <p class="text-sm text-gray-500">
                <span v-if="questionFilter.batchNameKeyword || questionFilter.subject">
                  尝试调整筛选条件或
                </span>
                <button type="button" class="link-btn" @click="clearFilters">清空筛选</button>
              </p>
            </div>

            <div v-else class="questions-grid">
              <div
                v-for="question in availableQuestions"
                :key="question.questionId"
                class="question-card"
                :class="{ 'selected': isQuestionSelected(question) }"
              >
                <!-- 选择状态指示器 -->
                <div class="selection-indicator" @click.stop="toggleQuestionSelection(question)">
                  <div class="checkbox-wrapper">
                    <input
                      type="checkbox"
                      :checked="isQuestionSelected(question)"
                      @click.stop
                      readonly
                    />
                  </div>
                </div>

                <div class="question-header">
                  <div class="question-type-badge" :class="`type-${question.questionType}`">
                    {{ getQuestionTypeLabel(question.questionType) }}
                  </div>
                  <div class="question-difficulty">
                    <span class="difficulty-label">难度</span>
                    <div class="difficulty-stars">
                      <span
                        v-for="i in 5"
                        :key="i"
                        class="star"
                        :class="{ 'filled': i <= (question.difficulty || 1) }"
                      >
                        ★
                      </span>
                    </div>
                  </div>
                </div>

                <div class="question-content">
                  {{ question.content }}
                </div>

                <div class="question-meta">
                  <div class="meta-tags">
                    <span class="subject-tag">{{ question.subject }}</span>
                    <span class="ai-tag" v-if="question.isAiGenerated">
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
                      </svg>
                      AI生成
                    </span>
                  </div>
                  <div class="question-actions">
                    <button
                      type="button"
                      class="preview-btn"
                      @click.stop="previewQuestion(question)"
                      title="预览题目"
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                      </svg>
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- 分页 -->
            <div class="pagination" v-if="availableQuestions.length > 0">
              <button
                @click="loadPreviousPage"
                :disabled="questionsPagination.page <= 1"
                class="page-btn"
              >
                上一页
              </button>
              <span class="page-info">第 {{ questionsPagination.page }} 页</span>
              <button
                @click="loadNextPage"
                :disabled="availableQuestions.length < questionsPagination.size"
                class="page-btn"
              >
                下一页
              </button>
            </div>

            <!-- 操作按钮 -->
            <div class="modal-actions">
              <button type="button" class="cancel-btn" @click="closeQuestionSelector">
                取消
              </button>
              <button
                type="button"
                class="confirm-btn"
                @click="addSelectedQuestions"
                :disabled="selectedQuestionCount === 0"
                :title="selectedQuestionCount === 0 ? '请先选择题目' : `添加 ${selectedQuestionCount} 道题目`"
              >
                <span v-if="selectedQuestionCount === 0">请先选择题目</span>
                <span v-else>添加选中题目 ({{ selectedQuestionCount }})</span>
              </button>
              <!-- 调试信息 -->
              <div class="debug-info" style="margin-top: 10px; font-size: 12px; color: #666; border: 1px solid #ddd; padding: 8px; border-radius: 4px; background: #f9f9f9;">
                <div><strong>选择状态调试:</strong></div>
                <div>已选择数量: {{ selectedQuestionCount }}</div>
                <div>已选择ID: {{ Array.from(selectedQuestionIds).join(', ') || '无' }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.exam-editor {
  @apply max-w-7xl mx-auto px-4;
}

.editor-header {
  @apply flex justify-between items-center mb-8;
}

.editor-title {
  @apply text-2xl font-bold text-gray-800;
}

.editor-actions {
  @apply flex gap-4;
}

.cancel-btn {
  @apply px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors;
}

.save-btn {
  @apply px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors;
}

.loading-state {
  @apply flex flex-col items-center justify-center py-12;
}

.loading-spinner {
  @apply w-10 h-10 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin mb-4;
}

.steps {
  @apply flex justify-between mb-8 relative;
}

.steps::before {
  content: '';
  @apply absolute top-5 left-0 w-full h-1 bg-gray-200;
  z-index: 0;
}

.step {
  @apply flex flex-col items-center relative z-10 cursor-pointer flex-1;
}

.step-number {
  @apply h-10 w-10 rounded-full flex items-center justify-center text-gray-600 bg-white border-2 border-gray-300 font-medium mb-2;
}

.step.active .step-number {
  @apply bg-blue-600 text-white border-blue-600;
}

.step.completed .step-number {
  @apply bg-green-600 text-white border-green-600;
}

.step-name {
  @apply text-sm text-gray-600;
}

.step.active .step-name {
  @apply font-medium text-blue-600;
}

.step.completed .step-name {
  @apply font-medium text-green-600;
}

.step-content {
  @apply bg-white rounded-xl shadow-sm p-6 mb-8;
}

.form-section {
  @apply mb-6;
}

.form-group {
  @apply mb-4;
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

.checkbox {
  @apply flex items-center;
}

.checkbox input {
  @apply h-4 w-4 text-blue-600 mr-2;
}

.step-actions {
  @apply flex justify-between mt-6;
}

.back-btn {
  @apply px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors;
}

.next-btn {
  @apply px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors;
}

.questions-header {
  @apply flex justify-between items-center mb-6;
}

.questions-summary {
  @apply text-gray-600;
}

.add-question-btn {
  @apply flex items-center px-3 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors;
}

.questions-list {
  @apply space-y-6 mb-6;
}

.question-item {
  @apply flex gap-4;
}

.question-number {
  @apply h-8 w-8 rounded-full bg-blue-100 text-blue-800 flex items-center justify-center font-medium flex-shrink-0 mt-4;
}

.question-content {
  @apply flex-1;
}

.question-actions {
  @apply flex flex-col gap-1 mt-4;
}

.action-btn {
  @apply p-2 rounded-lg text-gray-500 hover:bg-gray-100 transition-colors;
}

.action-btn.delete:hover {
  @apply text-red-600 bg-red-50;
}

.action-btn:disabled {
  @apply opacity-30 cursor-not-allowed;
}

.empty-questions {
  @apply flex flex-col items-center justify-center text-center py-16 bg-gray-50 rounded-lg;
}

.empty-questions svg {
  @apply text-gray-300 mb-4;
}

.empty-questions p {
  @apply text-gray-500;
}

.section-title {
  @apply text-lg font-medium text-gray-800 mb-4 flex items-center justify-between;
}

.settings-section {
  @apply mb-8;
}

.summary-section {
  @apply bg-gray-50 p-4 rounded-lg mt-6;
}

.summary-item {
  @apply flex justify-between py-2 border-b border-gray-200 last:border-0;
}

.summary-item .label {
  @apply text-gray-600 font-medium;
}

.summary-item .value {
  @apply text-gray-800;
}

.preview-section {
  @apply mb-8;
}

.toggle-preview {
  @apply text-sm text-blue-600 hover:text-blue-800 font-normal ml-2;
}

.preview-exam {
  @apply bg-gray-50 p-6 rounded-lg;
}

.exam-header {
  @apply mb-6;
}

.exam-header h2 {
  @apply text-xl font-medium mb-2;
}

.exam-meta {
  @apply flex flex-wrap gap-x-6 gap-y-2 mt-3 text-sm;
}

.meta-item {
  @apply flex items-center;
}

.meta-label {
  @apply text-gray-500 mr-1;
}

.preview-questions {
  @apply space-y-6;
}

.preview-question {
  @apply border-b border-gray-200 pb-6 last:border-0 last:pb-0;
}

.question-header {
  @apply flex justify-between items-center mb-2;
}

.question-index {
  @apply text-sm font-medium text-gray-700;
}

.question-score {
  @apply text-sm text-gray-600;
}

.question-editor-modal {
  @apply fixed inset-0 z-50 flex items-center justify-center;
}

.modal-overlay {
  @apply absolute inset-0 bg-black bg-opacity-50;
}

.modal-container {
  @apply relative bg-white rounded-xl shadow-lg max-w-3xl w-full max-h-[90vh] flex flex-col overflow-hidden;
}

.modal-header {
  @apply flex justify-between items-center p-4 border-b border-gray-200;
}

.close-btn {
  @apply p-1.5 rounded-full hover:bg-gray-100 text-gray-500 hover:text-gray-700 transition-colors;
}

.modal-body {
  @apply overflow-y-auto p-4;
}

/* 题目选择器样式 */
.question-selector-modal {
  @apply fixed inset-0 z-50 flex items-center justify-center p-4;
}

.question-selector-modal .modal-container.large {
  max-width: 1200px;
  width: 95vw;
  max-height: 85vh;
  margin: auto;
}

.filter-section {
  background: #f8fafc;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 24px;
  border: 1px solid #e2e8f0;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.filter-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.clear-filters-btn {
  background: #ef4444;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.clear-filters-btn:hover {
  background: #dc2626;
}

.search-controls {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 16px;
}

.search-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.subject-input {
  padding: 12px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  background: white;
  font-size: 16px;
  transition: all 0.2s;
}

.subject-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.search-hint {
  padding: 8px 12px;
  background: #f0f9ff;
  border-radius: 6px;
  border-left: 3px solid #0ea5e9;
}

.search-hint p {
  margin: 0;
  color: #0c4a6e;
  font-size: 13px;
}

.quick-subjects {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.quick-subjects label {
  font-weight: 500;
  color: #374151;
  font-size: 14px;
}

.subject-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.subject-tag-btn {
  padding: 6px 12px;
  background: #f1f5f9;
  border: 1px solid #cbd5e1;
  border-radius: 16px;
  color: #475569;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.subject-tag-btn:hover {
  background: #e2e8f0;
  border-color: #94a3b8;
}

.subject-tag-btn.active {
  background: #3b82f6;
  border-color: #3b82f6;
  color: white;
}

.search-stats {
  padding: 12px 16px;
  background: #ecfdf5;
  border-radius: 6px;
  border-left: 4px solid #10b981;
}

.selection-status {
  margin-top: 12px;
  padding: 12px 16px;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
}

.status-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.no-selection {
  color: #64748b;
  font-size: 14px;
}

.has-selection {
  color: #059669;
  font-size: 14px;
  font-weight: 500;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-group label {
  font-weight: 500;
  color: #374151;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.filter-stats {
  padding: 12px 16px;
  background: #e0f2fe;
  border-radius: 6px;
  border-left: 4px solid #0284c7;
}

.stats-text {
  color: #0c4a6e;
  font-size: 14px;
}

.filter-active {
  color: #059669;
  font-weight: 500;
}

.filter-input,
.filter-select {
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: white;
  font-size: 14px;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.filter-input:focus,
.filter-select:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.loading-questions,
.empty-questions {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #6b7280;
  text-align: center;
}

.empty-questions svg {
  color: #9ca3af;
  margin-bottom: 16px;
}

.link-btn {
  background: none;
  border: none;
  color: #3b82f6;
  text-decoration: underline;
  cursor: pointer;
  font-size: inherit;
}

.link-btn:hover {
  color: #2563eb;
}

.questions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.question-card {
  position: relative;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
  overflow: hidden;
  user-select: none;
}

.question-card:active {
  transform: scale(0.98);
}

.question-card:hover {
  border-color: #3b82f6;
  box-shadow: 0 4px 20px rgba(59, 130, 246, 0.15);
  transform: translateY(-2px);
}

.question-card.selected {
  border-color: #10b981;
  background: linear-gradient(135deg, #ecfdf5 0%, #f0fdf4 100%);
  box-shadow: 0 4px 20px rgba(16, 185, 129, 0.2);
  position: relative;
}

.question-card.selected::before {
  content: '✓';
  position: absolute;
  top: 8px;
  left: 8px;
  background: #10b981;
  color: white;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
  z-index: 10;
}

.selection-indicator {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 10;
  cursor: pointer;
  padding: 4px;
  border-radius: 8px;
  transition: background-color 0.2s ease;
}

.selection-indicator:hover {
  background-color: rgba(59, 130, 246, 0.1);
}

.checkbox-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: 2px solid #d1d5db;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.checkbox-wrapper:hover {
  border-color: #3b82f6;
  background: #f3f4f6;
  transform: scale(1.05);
}

.question-card.selected .checkbox-wrapper {
  border-color: #10b981;
  background: #10b981;
}

.checkbox-wrapper input[type="checkbox"] {
  width: 18px;
  height: 18px;
  margin: 0;
  cursor: pointer;
  accent-color: #10b981;
}

.question-card.selected .checkbox-wrapper input[type="checkbox"] {
  accent-color: white;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  padding-right: 30px; /* 为复选框留空间 */
}

.question-type-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.type-single { background: #dbeafe; color: #1e40af; }
.type-multiple { background: #fef3c7; color: #92400e; }
.type-true_false { background: #d1fae5; color: #065f46; }
.type-fill { background: #e0e7ff; color: #3730a3; }
.type-short { background: #fce7f3; color: #be185d; }
.type-essay { background: #f3e8ff; color: #7c2d12; }
.type-programming { background: #ecfdf5; color: #047857; }

.question-difficulty {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.difficulty-label {
  font-size: 11px;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.difficulty-stars {
  display: flex;
  gap: 2px;
}

.star {
  color: #d1d5db;
  font-size: 14px;
  transition: color 0.2s;
}

.star.filled {
  color: #fbbf24;
}

.question-content {
  margin-bottom: 16px;
  color: #1f2937;
  line-height: 1.6;
  font-size: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.question-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.meta-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  flex: 1;
}

.batch-tag {
  background: #f1f5f9;
  color: #475569;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
  border: 1px solid #e2e8f0;
}

.subject-tag {
  background: #eff6ff;
  color: #1e40af;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
  border: 1px solid #bfdbfe;
}

.ai-tag {
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
  box-shadow: 0 2px 4px rgba(16, 185, 129, 0.2);
}

.question-actions {
  display: flex;
  gap: 8px;
}

.preview-btn {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 6px;
  cursor: pointer;
  transition: all 0.2s;
  color: #64748b;
}

.preview-btn:hover {
  background: #e2e8f0;
  color: #475569;
  transform: scale(1.05);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: white;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  background: #f9fafb;
  border-color: #9ca3af;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  color: #6b7280;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

.confirm-btn {
  padding: 8px 16px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}

.confirm-btn:hover:not(:disabled) {
  background: #2563eb;
}

.confirm-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.select-question-btn {
  padding: 8px 16px;
  background: #10b981;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
  display: flex;
  align-items: center;
}

.select-question-btn:hover {
  background: #059669;
}

.questions-actions {
  display: flex;
  gap: 12px;
}

.checkbox-group {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 8px;
  margin-top: 8px;
}

.checkbox-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.checkbox-item input[type="checkbox"] {
  width: 16px;
  height: 16px;
}

.checkbox-item label {
  cursor: pointer;
  color: #374151;
}
</style>