<template>
  <div class="exam-taking-page">
    <!-- 顶部导航栏 -->
    <header class="exam-header sticky top-0 left-0 right-0 z-30 bg-white border-b border-gray-200 shadow-sm">
      <div class="container mx-auto px-4 py-3 flex justify-between items-center">
        <div class="flex items-center">
          <el-button icon="el-icon-back" circle size="small" class="mr-3" @click="goBack"></el-button>
          <h1 class="text-lg font-bold mr-4">{{ exam.title }}</h1>
          <el-tag size="small" type="info">{{ exam.subject }}</el-tag>
        </div>
        
        <div class="flex items-center gap-6">
          <!-- 计时器 -->
          <div class="timer flex items-center" :class="{'text-red-500': remainingTime < 300}">
            <i class="el-icon-time mr-1"></i>
            <span class="font-medium">{{ formatTime(remainingTime) }}</span>
          </div>
          
          <!-- 进度 -->
          <div class="progress-info text-sm">
            <span class="font-medium">{{ currentQuestionIndex + 1 }}</span> / {{ exam.questions.length }}
          </div>
          
          <!-- 提交按钮 -->
          <el-button type="primary" @click="confirmSubmit">提交试卷</el-button>
        </div>
      </div>
    </header>
    
    <div class="container mx-auto px-4 py-6">
      <div class="exam-container flex gap-6">
        <!-- 左侧：题目导航 -->
        <div class="question-nav w-64 bg-white p-4 rounded-xl shadow-sm sticky top-20 self-start">
          <h2 class="text-base font-medium mb-4">题目导航</h2>
          
          <div class="question-list grid grid-cols-5 gap-2">
            <button 
              v-for="(q, index) in exam.questions" 
              :key="index"
              @click="goToQuestion(index)"
              class="question-btn w-10 h-10 rounded-lg flex items-center justify-center text-sm font-medium transition-all"
              :class="{
                'bg-indigo-600 text-white': currentQuestionIndex === index,
                'bg-green-100 text-green-600 border border-green-200': isAnswered(index) && currentQuestionIndex !== index,
                'bg-gray-100 text-gray-600': !isAnswered(index) && currentQuestionIndex !== index
              }"
            >
              {{ index + 1 }}
            </button>
          </div>
          
          <div class="question-stats mt-6 space-y-2 text-sm">
            <div class="flex justify-between">
              <span class="text-gray-500">已答题</span>
              <span class="font-medium">{{ answeredCount }}/{{ exam.questions.length }}</span>
            </div>
            <div class="progress-bar h-2 bg-gray-200 rounded-full overflow-hidden">
              <div class="h-full bg-green-500 rounded-full" :style="`width: ${(answeredCount / exam.questions.length) * 100}%`"></div>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-500">未答题</span>
              <span class="font-medium">{{ exam.questions.length - answeredCount }}</span>
            </div>
          </div>
          
          <div class="mt-6 space-y-3">
            <el-button type="success" plain size="small" block @click="confirmSubmit" :disabled="answeredCount === 0">
              提交试卷
            </el-button>
            <el-button type="info" plain size="small" block @click="saveExam">
              保存进度
            </el-button>
          </div>
        </div>
        
        <!-- 右侧：答题区域 -->
        <div class="question-area flex-1 bg-white rounded-xl shadow-sm p-6">
          <!-- 考试信息 -->
          <div class="exam-info mb-6 p-4 bg-gray-50 rounded-lg">
            <div class="flex justify-between mb-2">
              <h2 class="text-lg font-semibold">{{ exam.title }}</h2>
              <span class="text-sm text-gray-500">总分: {{ exam.totalScore }}分</span>
            </div>
            <div class="flex flex-wrap gap-x-6 gap-y-2 text-sm text-gray-500">
              <div>科目: {{ exam.subject }}</div>
              <div>时长: {{ exam.duration }}分钟</div>
              <div>题目数: {{ exam.questions.length }}</div>
              <div>考试时间: {{ exam.startTime }} - {{ exam.endTime }}</div>
            </div>
          </div>
          
          <!-- 当前题目 -->
          <div class="current-question">
            <div class="question-header flex justify-between items-center mb-4">
              <h3 class="text-lg font-medium flex items-center">
                <span class="question-number bg-indigo-100 text-indigo-600 w-8 h-8 rounded-full flex items-center justify-center mr-3">
                  {{ currentQuestionIndex + 1 }}
                </span>
                <span>{{ getQuestionTypeName(currentQuestion.questionType) }}</span>
                <span class="ml-2 text-sm text-gray-500">({{ currentQuestion.score || '未设置' }}分)</span>
              </h3>
              <div class="flex gap-2">
                <el-button size="small" plain @click="markQuestion" :type="isMarked(currentQuestionIndex) ? 'warning' : 'info'">
                  {{ isMarked(currentQuestionIndex) ? '取消标记' : '标记题目' }}
                </el-button>
              </div>
            </div>
            
            <!-- 题目内容 -->
            <div class="question-content mb-6">
              <div class="question-text text-base mb-4">{{ currentQuestion.content }}</div>
              
              <!-- 选择题 -->
              <div v-if="currentQuestion.questionType === '选择题'" class="options space-y-3">
                <div 
                  v-for="(option, idx) in parseOptions(currentQuestion.options)" 
                  :key="idx"
                  @click="selectOption(option)"
                  class="option p-3 border rounded-lg cursor-pointer transition-all flex items-center"
                  :class="{
                    'border-indigo-500 bg-indigo-50': userAnswers[currentQuestionIndex] === option.charAt(0),
                    'hover:border-gray-300': userAnswers[currentQuestionIndex] !== option.charAt(0)
                  }"
                >
                  <div class="option-marker w-6 h-6 rounded-full border flex items-center justify-center mr-3"
                       :class="{
                         'border-indigo-500 bg-indigo-500 text-white': userAnswers[currentQuestionIndex] === option.charAt(0),
                         'border-gray-300': userAnswers[currentQuestionIndex] !== option.charAt(0)
                       }">
                    {{ option.charAt(0) }}
                  </div>
                  <div>{{ option.slice(2) }}</div>
                </div>
              </div>
              
              <!-- 判断题 -->
              <div v-else-if="currentQuestion.questionType === '判断题'" class="options space-y-3">
                <div 
                  v-for="(option, idx) in ['正确', '错误']" 
                  :key="idx"
                  @click="selectOption(option)"
                  class="option p-3 border rounded-lg cursor-pointer transition-all flex items-center"
                  :class="{
                    'border-indigo-500 bg-indigo-50': userAnswers[currentQuestionIndex] === option,
                    'hover:border-gray-300': userAnswers[currentQuestionIndex] !== option
                  }"
                >
                  <div class="option-marker w-6 h-6 rounded-full border flex items-center justify-center mr-3"
                       :class="{
                         'border-indigo-500 bg-indigo-500 text-white': userAnswers[currentQuestionIndex] === option,
                         'border-gray-300': userAnswers[currentQuestionIndex] !== option
                       }">
                    {{ idx === 0 ? '√' : '×' }}
                  </div>
                  <div>{{ option }}</div>
                </div>
              </div>
              
              <!-- 填空题 -->
              <div v-else-if="currentQuestion.questionType === '填空题'" class="text-answer">
                <el-input 
                  v-model="userAnswers[currentQuestionIndex]" 
                  type="text" 
                  placeholder="请在此输入你的答案..."
                  @input="updateAnswer"
                ></el-input>
              </div>
              
              <!-- 简答题 -->
              <div v-else-if="currentQuestion.questionType === '简答题'" class="text-answer">
                <el-input 
                  v-model="userAnswers[currentQuestionIndex]" 
                  type="textarea" 
                  :rows="6"
                  placeholder="请在此输入你的答案..."
                  @input="updateAnswer"
                ></el-input>
              </div>
            </div>
            
            <!-- 导航按钮 -->
            <div class="navigation-buttons flex justify-between mt-8">
              <el-button @click="prevQuestion" :disabled="currentQuestionIndex === 0">
                上一题
              </el-button>
              <el-button @click="nextQuestion" :disabled="currentQuestionIndex === exam.questions.length - 1">
                下一题
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 提交确认对话框 -->
    <el-dialog
      v-model="showSubmitDialog"
      title="确认提交"
      width="30%"
      :close-on-click-modal="false"
    >
      <div class="submit-confirm">
        <div class="mb-4">
          <p class="mb-2">你即将提交试卷，请确认以下信息：</p>
          <div class="stats p-3 bg-gray-50 rounded-lg">
            <div class="flex justify-between mb-2">
              <span>已答题数：</span>
              <span class="font-medium">{{ answeredCount }}/{{ exam.questions.length }}</span>
            </div>
            <div class="flex justify-between">
              <span>未答题数：</span>
              <span class="font-medium">{{ exam.questions.length - answeredCount }}</span>
            </div>
          </div>
        </div>
        
        <div v-if="exam.questions.length - answeredCount > 0" class="warning text-amber-600 mb-4">
          <i class="el-icon-warning mr-1"></i>
          你还有 {{ exam.questions.length - answeredCount }} 道题目未作答，确定要提交吗？
        </div>
        
        <div class="text-center">
          <el-checkbox v-model="confirmSubmission">我已检查完所有题目，确认提交</el-checkbox>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showSubmitDialog = false">取消</el-button>
          <el-button type="primary" @click="submitExam" :disabled="!confirmSubmission">
            确认提交
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, post } from '../../net'
import { useAuthStore } from '../../stores/counter'

const route = useRoute()
const router = useRouter()
const examId = route.params.examId
const submissionId = route.query.submissionId
const authStore = useAuthStore()

// 考试信息
const exam = ref({
  id: examId,
  title: '',
  courseName: '',
  className: '',
  duration: 0,
  totalScore: 0,
  startTime: '',
  endTime: '',
  questions: [],
  submissionId: submissionId
})

// 用户答案
const userAnswers = ref({})

// 当前题目索引
const currentQuestionIndex = ref(0)

// 标记的题目
const markedQuestions = ref([])

// 提交确认
const showSubmitDialog = ref(false)
const confirmSubmission = ref(false)

// 计时器
const remainingTime = ref(0)
const timer = ref(null)

// 计算属性：当前题目
const currentQuestion = computed(() => {
  if (!exam.value.questions || exam.value.questions.length === 0) {
    return { content: '加载中...' }
  }
  return exam.value.questions[currentQuestionIndex.value] || { content: '题目不存在' }
})

// 计算属性：已答题数量
const answeredCount = computed(() => {
  return Object.keys(userAnswers.value).length
})

// 格式化时间
const formatTime = (seconds) => {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60
  
  return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
}

// 获取题型名称
const getQuestionTypeName = (type) => {
  if (!type) return '未知题型'
  return type
}

// 解析选项
const parseOptions = (options) => {
  if (!options) return []
  try {
    return JSON.parse(options)
  } catch (e) {
    console.error('解析选项失败:', e)
    return []
  }
}

// 检查题目是否已答
const isAnswered = (index) => {
  return userAnswers.value[index] !== undefined
}

// 检查题目是否被标记
const isMarked = (index) => {
  return markedQuestions.value.includes(index)
}

// 标记题目
const markQuestion = () => {
  const index = currentQuestionIndex.value
  if (isMarked(index)) {
    markedQuestions.value = markedQuestions.value.filter(i => i !== index)
  } else {
    markedQuestions.value.push(index)
  }
}

// 选择选项
const selectOption = (option) => {
  // 对于选择题，保存选项的首字母（A、B、C、D）
  if (currentQuestion.value.questionType === '选择题') {
    userAnswers.value[currentQuestionIndex.value] = option.charAt(0)
  } else {
    userAnswers.value[currentQuestionIndex.value] = option
  }
  saveAnswer()
}

// 更新填空题答案
const updateAnswer = () => {
  saveAnswer()
}

// 保存答案到后端
const saveAnswer = () => {
  const questionId = currentQuestion.value.id || currentQuestion.value.questionId
  const answer = userAnswers.value[currentQuestionIndex.value]
  
  if (questionId && answer !== undefined && submissionId) {
    post(`/api/student/exams/submissions/${submissionId}/answers?questionId=${questionId}&answer=${encodeURIComponent(answer.toString())}`, {}, 
      (message) => {
        // 保存成功，静默处理
      }, 
      (message) => {
        ElMessage.error(message || '保存答案失败')
      }
    )
  }
}

// 跳转到指定题目
const goToQuestion = (index) => {
  if (index >= 0 && index < exam.value.questions.length) {
    currentQuestionIndex.value = index
  }
}

// 下一题
const nextQuestion = () => {
  if (currentQuestionIndex.value < exam.value.questions.length - 1) {
    currentQuestionIndex.value++
  }
}

// 上一题
const prevQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--
  }
}

// 确认提交对话框
const confirmSubmit = () => {
  showSubmitDialog.value = true
  confirmSubmission.value = false
}

// 提交考试
const submitExam = () => {
  // 先保存所有未保存的答案
  const savePromises = []
  
  // 遍历所有题目，确保每道题的答案都已保存
  exam.value.questions.forEach((question, index) => {
    const answer = userAnswers.value[index]
    if (answer !== undefined) {
      const questionId = question.id || question.questionId
      if (questionId) {
        // 创建保存答案的Promise
        const savePromise = new Promise((resolve, reject) => {
          post(`/api/student/exams/submissions/${submissionId}/answers?questionId=${questionId}&answer=${encodeURIComponent(answer.toString())}`, {}, 
            () => resolve(),
            (error) => reject(error)
          )
        })
        savePromises.push(savePromise)
      }
    }
  })
  
  // 等待所有答案保存完成后再提交试卷
  Promise.all(savePromises)
    .then(() => {
      // 所有答案保存成功后，提交试卷
      post(`/api/student/exams/submissions/${submissionId}/submit`, {}, 
        (message) => {
          ElMessage.success(message || '提交成功')
          showSubmitDialog.value = false
          router.push('/student/exams')
        },
        (message) => {
          ElMessage.error(message || '提交失败')
        }
      )
    })
    .catch(() => {
      ElMessage.error('部分答案保存失败，请重试')
    })
}

// 保存考试进度
const saveExam = () => {
  ElMessage.success('进度已保存')
}

// 初始化计时器
const initTimer = () => {
  if (exam.value.duration) {
    // 转换为秒
    remainingTime.value = exam.value.duration * 60
    
    timer.value = setInterval(() => {
      if (remainingTime.value > 0) {
        remainingTime.value--
      } else {
        // 时间到，自动提交
        clearInterval(timer.value)
        ElMessage.warning('考试时间已结束，系统将自动提交')
        submitExam()
      }
    }, 1000)
  }
}

// 加载考试详情
const loadExamDetail = () => {
  console.log('开始加载考试详情:', examId, '学生ID:', authStore.user.userId);
  get(`/api/student/exams/${examId}`, { studentId: authStore.user.userId },
    (message, data) => {
      console.log('考试详情加载成功:', data);
      if (data) {
        exam.value = data;
        console.log('题目列表:', data.questions);
        
        // 初始化计时器
        initTimer()
        
        // 加载已保存的答案
        loadSavedAnswers()
      }
    },
    (message) => {
      console.error('获取考试详情失败:', message);
      ElMessage.error(message || '获取考试详情失败')
      router.push('/student/exams')
    }
  )
}

// 加载已保存的答案
const loadSavedAnswers = () => {
  if (submissionId) {
    get(`/api/student/exams/submissions/${submissionId}/answers`, {}, 
      (message, data) => {
        if (data && Array.isArray(data)) {
          data.forEach(answer => {
            const questionIndex = exam.value.questions.findIndex(q => q.id === answer.questionId || q.questionId === answer.questionId)
            if (questionIndex >= 0) {
              userAnswers.value[questionIndex] = answer.userAnswer
            }
          })
        }
      },
      (message) => {
        console.warn('无法加载已保存的答案:', message)
      }
    )
  }
}

// 离开页面前提示
const beforeUnload = (e) => {
  e.preventDefault()
  e.returnValue = '考试尚未提交，确定要离开吗？'
}

// 返回上一页
const goBack = () => {
  ElMessageBox.confirm('确定要离开考试页面吗？未提交的答案可能会丢失。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    router.push('/student/exams')
  }).catch(() => {})
}

onMounted(() => {
  loadExamDetail()
  
  // 添加页面离开提示
  window.addEventListener('beforeunload', beforeUnload)
})

onBeforeUnmount(() => {
  // 清除计时器
  if (timer.value) {
    clearInterval(timer.value)
  }
  
  // 移除页面离开提示
  window.removeEventListener('beforeunload', beforeUnload)
})

// 监听路由变化
watch(() => route.params.examId, (newId) => {
  if (newId !== examId) {
    location.reload()
  }
})
</script>

<style scoped>
.exam-taking-page {
  min-height: 100vh;
  background-color: #f8fafc;
}

.exam-header {
  backdrop-filter: blur(8px);
  background-color: rgba(255, 255, 255, 0.9);
}

.question-btn {
  transition: all 0.2s ease;
}

.question-btn:hover {
  transform: scale(1.05);
}

.option {
  transition: all 0.2s ease;
}

.option:hover {
  background-color: #f9fafb;
}

/* 动画效果 */
.question-area, .question-nav {
  animation: fadeIn 0.6s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* 响应式调整 */
@media (max-width: 768px) {
  .exam-container {
    flex-direction: column;
  }
  
  .question-nav {
    width: 100%;
    position: static;
    margin-bottom: 1rem;
  }
  
  .question-list {
    grid-template-columns: repeat(10, 1fr);
  }
}
</style> 