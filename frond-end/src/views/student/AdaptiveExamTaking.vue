<template>
  <div class="adaptive-exam-taking">
    <!-- 顶部导航栏 -->
    <header class="exam-header sticky top-0 left-0 right-0 z-30 bg-white border-b border-gray-200 shadow-sm">
      <div class="container mx-auto px-4 py-3 flex justify-between items-center">
        <div class="flex items-center">
          <h1 class="text-lg font-bold mr-4">自适应智能测试</h1>
          <el-tag size="small" type="info">{{ examInfo.subject || '未知科目' }}</el-tag>
        </div>
        <div class="flex items-center">
          <div class="mr-6 text-sm">
            <span class="font-medium">题目：</span>
            <span class="text-indigo-600 font-medium">{{ currentQuestionNumber }}</span>
            <span class="text-gray-500">/{{ examInfo.questionCount || '?' }}</span>
          </div>
          <div class="countdown-timer flex items-center">
            <Icon icon="tabler:clock" class="mr-1 text-amber-500" />
            <span class="font-medium" :class="{'text-red-500': timeRemaining < 300}">
              {{ formatTime(timeRemaining) }}
            </span>
          </div>
        </div>
      </div>
    </header>
    
    <div class="container mx-auto px-4 py-6">
      <!-- 加载中状态 -->
      <div v-if="loading" class="flex flex-col items-center justify-center py-20">
        <el-spinner size="large"></el-spinner>
        <p class="mt-4 text-gray-500">{{ loadingMessage }}</p>
      </div>
      
      <!-- 测试完成状态 -->
      <div v-else-if="examCompleted" class="completion-screen bg-white rounded-xl shadow-md p-8 text-center">
        <div class="mb-6">
          <Icon icon="tabler:check-circle" class="text-6xl text-green-500" />
        </div>
        <h2 class="text-2xl font-bold mb-2">测试已完成！</h2>
        <p class="text-gray-500 mb-6">系统正在生成您的能力评估报告...</p>
        <div class="progress-bar mb-8">
          <el-progress :percentage="reportProgress" :stroke-width="20" :show-text="false"></el-progress>
          <div class="text-sm text-gray-500 mt-2">{{ reportProgressText }}</div>
        </div>
        <el-button type="primary" @click="viewReport" :disabled="reportProgress < 100">
          <Icon icon="tabler:file-report" class="mr-1" />
          查看评估报告
        </el-button>
      </div>
      
      <!-- 答题界面 -->
      <div v-else class="question-container">
        <!-- 题目卡片 -->
        <div class="question-card bg-white rounded-xl shadow-md p-6 mb-6">
          <!-- 题目头部信息 -->
          <div class="question-header flex justify-between items-center mb-4">
            <div class="flex items-center">
              <div class="question-type-tag px-3 py-1 rounded-full text-xs font-medium mr-2"
                   :class="getQuestionTypeClass(currentQuestion.questionType)">
                {{ getQuestionTypeText(currentQuestion.questionType) }}
              </div>
              <div class="difficulty-indicator flex items-center">
                <span class="text-xs text-gray-500 mr-1">难度：</span>
                <div class="flex">
                  <Icon v-for="i in 5" :key="i" icon="tabler:star-filled" 
                        :class="i <= currentQuestion.difficulty ? 'text-amber-400' : 'text-gray-300'"
                        class="w-3.5 h-3.5" />
                </div>
              </div>
            </div>
            <div class="question-score text-sm">
              <span class="text-gray-500">分值：</span>
              <span class="font-medium">{{ currentQuestion.score }}</span>
            </div>
          </div>
          
          <!-- 题目内容 -->
          <div class="question-content mb-6">
            <div class="question-text text-lg mb-4" v-html="currentQuestion.content"></div>
            
            <!-- 选择题选项 -->
            <div v-if="['SINGLE_CHOICE', 'MULTI_CHOICE'].includes(currentQuestion.questionType)" 
                 class="options-list space-y-3">
              <div v-for="option in currentQuestion.options" :key="option.id"
                   class="option-item p-3 border rounded-lg cursor-pointer transition-colors"
                   :class="{
                     'border-indigo-500 bg-indigo-50': isOptionSelected(option.id),
                     'hover:border-gray-300': !isOptionSelected(option.id) && !answerSubmitted,
                     'cursor-not-allowed': answerSubmitted
                   }"
                   @click="selectOption(option.id)">
                <div class="flex items-start">
                  <div class="option-marker w-6 h-6 rounded-full border flex items-center justify-center mr-3"
                       :class="{
                         'border-indigo-500 bg-indigo-500 text-white': isOptionSelected(option.id),
                         'border-gray-300': !isOptionSelected(option.id)
                       }">
                    {{ option.id }}
                  </div>
                  <div class="option-content" v-html="option.text"></div>
                </div>
              </div>
            </div>
            
            <!-- 判断题选项 -->
            <div v-else-if="currentQuestion.questionType === 'TRUE_FALSE'" class="true-false-options flex gap-4 mt-6">
              <el-button 
                size="large" 
                :type="userAnswer === '正确' ? 'primary' : 'default'" 
                @click="userAnswer = '正确'"
                :disabled="answerSubmitted"
                class="flex-1 h-16 text-lg"
              >
                <Icon icon="tabler:check" class="mr-2" />
                正确
              </el-button>
              <el-button 
                size="large" 
                :type="userAnswer === '错误' ? 'primary' : 'default'" 
                @click="userAnswer = '错误'"
                :disabled="answerSubmitted"
                class="flex-1 h-16 text-lg"
              >
                <Icon icon="tabler:x" class="mr-2" />
                错误
              </el-button>
            </div>
            
            <!-- 填空题输入框 -->
            <div v-else-if="currentQuestion.questionType === 'FILL_BLANK'" class="fill-blank-input mt-4">
              <div class="mb-2 text-sm text-gray-500">请填写答案：</div>
              <el-input 
                v-model="userAnswer" 
                placeholder="请输入答案..."
                :disabled="answerSubmitted"
                class="w-full"
              ></el-input>
            </div>
            
            <!-- 简答题输入框 -->
            <div v-else-if="currentQuestion.questionType === 'SHORT_ANSWER'" class="short-answer-input">
              <div class="mb-2 text-sm text-gray-500">请输入你的答案：</div>
              <el-input 
                type="textarea" 
                v-model="userAnswer" 
                :rows="6" 
                placeholder="请输入你的答案..."
                :disabled="answerSubmitted"
              ></el-input>
            </div>
            
            <!-- 默认情况 -->
            <div v-else class="unknown-question-type mt-4 p-4 bg-red-50 rounded-lg">
              <p class="text-red-500">
                <Icon icon="tabler:alert-triangle" class="mr-1" />
                未知题型：{{ currentQuestion.questionType }}
              </p>
              <p class="text-sm text-gray-500 mt-2">
                请联系管理员解决此问题
              </p>
            </div>
          </div>
          
          <!-- 提示信息 -->
          <div v-if="currentQuestion.showHint && !answerSubmitted" class="hint-section mb-4">
            <el-collapse>
              <el-collapse-item title="查看提示" name="1">
                <div class="hint-content p-3 bg-amber-50 rounded text-sm text-gray-700">
                  <p class="mb-2 font-medium text-amber-700">提示：</p>
                  <p>思考这个问题的关键是理解{{ currentQuestion.topic }}的基本概念，尝试从定义出发分析。</p>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>
          
          <!-- 答案反馈（提交后显示） -->
          <div v-if="answerSubmitted" class="answer-feedback mt-6 p-4 rounded-lg"
               :class="answerResult.isCorrect ? 'bg-green-50' : 'bg-red-50'">
            <div class="flex items-start">
              <div class="feedback-icon mr-3">
                <Icon v-if="answerResult.isCorrect" icon="tabler:circle-check" class="text-2xl text-green-500" />
                <Icon v-else icon="tabler:circle-x" class="text-2xl text-red-500" />
              </div>
              <div class="feedback-content">
                <div class="font-medium mb-1" 
                     :class="answerResult.isCorrect ? 'text-green-700' : 'text-red-700'">
                  {{ answerResult.isCorrect ? '回答正确！' : '回答错误' }}
                </div>
                <div class="text-sm text-gray-700 mb-2">
                  <span class="font-medium">正确答案：</span>
                  <span>{{ formattedCorrectAnswer }}</span>
                </div>
                <div class="explanation text-sm text-gray-600" v-html="answerResult.explanation"></div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 操作按钮 -->
        <div class="action-buttons flex justify-between">
          <el-button @click="confirmExit" plain>
            <Icon icon="tabler:door-exit" class="mr-1" />
            退出测试
          </el-button>
          
          <div>
            <el-button v-if="!answerSubmitted" type="primary" @click="submitAnswer" :disabled="!canSubmit || submittingAnswer">
              <Icon v-if="!submittingAnswer" icon="tabler:check" class="mr-1" />
              <el-spinner v-else class="mr-1" size="small" />
              {{ submittingAnswer ? '提交中...' : '提交答案' }}
            </el-button>
            <el-button v-else type="primary" @click="goToNextQuestion" :disabled="loadingNextQuestion">
              <Icon v-if="!loadingNextQuestion" icon="tabler:arrow-right" class="ml-1" />
              <el-spinner v-else class="mr-1" size="small" />
              {{ loadingNextQuestion ? '加载中...' : (answerResult.isLastQuestion ? '完成测试' : '下一题') }}
            </el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 退出确认对话框 -->
    <el-dialog v-model="showExitConfirm" title="确认退出" width="400px">
      <div class="text-center">
        <Icon icon="tabler:alert-triangle" class="text-4xl text-amber-500 mb-4" />
        <p class="mb-4">确定要退出当前测试吗？</p>
        <p class="text-sm text-gray-500 mb-6">退出后测试进度将会保存，但当前题目的作答将不会被记录。</p>
      </div>
      <template #footer>
        <div class="flex justify-center gap-4">
          <el-button @click="showExitConfirm = false">取消</el-button>
          <el-button type="danger" @click="exitExam">确认退出</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Icon } from '@iconify/vue'
import { get, post } from '@/net'

const route = useRoute()
const router = useRouter()
const examId = route.params.id

// 状态变量
const loading = ref(true)
const loadingMessage = ref('正在加载测试信息...')
const examInfo = ref({})
const questions = ref([]) // 存储所有题目
const currentQuestion = ref({})
const currentQuestionIndex = ref(0) // 当前题目索引
const currentQuestionNumber = ref(1)
const userAnswer = ref('')
const selectedOptions = ref([])
const answerSubmitted = ref(false)
const answerResult = ref({})
const examCompleted = ref(false)
const reportProgress = ref(0)
const timeRemaining = ref(0)
const showExitConfirm = ref(false)
const lastQuestionId = ref(null)
const submittingAnswer = ref(false) // 提交答案时的加载状态
const loadingNextQuestion = ref(false) // 加载下一题时的加载状态

// 计算属性
const canSubmit = computed(() => {
  if (currentQuestion.value.questionType === 'SHORT_ANSWER' || 
      currentQuestion.value.questionType === 'FILL_BLANK') {
    return userAnswer.value.trim().length > 0;
  } else if (currentQuestion.value.questionType === 'TRUE_FALSE') {
    return userAnswer.value === '正确' || userAnswer.value === '错误';
  } else {
    // 选择题
    return selectedOptions.value.length > 0;
  }
})

const reportProgressText = computed(() => {
  if (reportProgress.value < 30) return '分析答题数据...'
  if (reportProgress.value < 60) return '评估能力水平...'
  if (reportProgress.value < 90) return '生成学习建议...'
  return '报告准备完成！'
})

// 格式化答案显示
const formattedCorrectAnswer = computed(() => {
  if (!answerResult.value || !answerResult.value.correctAnswer) {
    return '';
  }
  
  // 根据题目类型格式化答案
  if (currentQuestion.value.questionType === 'TRUE_FALSE') {
    // 判断题答案格式化
    try {
      const answer = String(answerResult.value.correctAnswer).toUpperCase();
      if (answer === 'TRUE' || answer === 'T' || answer === '1' || answer === 'YES' || answer === 'Y' || answer === '正确') {
        return '正确';
      } else if (answer === 'FALSE' || answer === 'F' || answer === '0' || answer === 'NO' || answer === 'N' || answer === '错误') {
        return '错误';
      }
      // 如果不匹配任何标准格式，返回原始答案
      return String(answerResult.value.correctAnswer);
    } catch (e) {
      console.error('判断题答案格式化错误:', e);
      return String(answerResult.value.correctAnswer);
    }
  } else if (currentQuestion.value.questionType === 'SINGLE_CHOICE' || currentQuestion.value.questionType === 'MULTI_CHOICE') {
    // 选择题答案格式化
    try {
      const answer = String(answerResult.value.correctAnswer);
      
      // 如果答案是选项ID (A,B,C,D)，直接返回
      if (answer.length <= 4) {
        return answer;
      }
      
      // 否则尝试查找对应的选项文本
      if (currentQuestion.value.options && Array.isArray(currentQuestion.value.options)) {
        const optionIds = answer.split(',');
        const optionTexts = optionIds.map(id => {
          const option = currentQuestion.value.options.find(opt => opt.id === id);
          return option ? `${option.id}. ${option.text}` : id;
        });
        return optionTexts.join('; ');
      }
    } catch (e) {
      console.error('选择题答案格式化错误:', e);
    }
  }
  
  // 默认返回原始答案
  return String(answerResult.value.correctAnswer);
})

// 监听当前题目变化，重置答题状态
watch(() => currentQuestionIndex.value, () => {
  if (questions.value.length > 0) {
    // 更新当前题目
    currentQuestion.value = questions.value[currentQuestionIndex.value];
    currentQuestionNumber.value = currentQuestionIndex.value + 1;
    
    // 重置用户答案
    userAnswer.value = '';
    selectedOptions.value = [];
    answerSubmitted.value = false;
    
    console.log('当前题目:', currentQuestion.value);
  }
}, { immediate: true });

// 监听题目数据变化
watch(() => questions.value, () => {
  if (questions.value.length > 0) {
    // 设置第一题为当前题目
    currentQuestion.value = questions.value[0];
    currentQuestionIndex.value = 0;
    currentQuestionNumber.value = 1;
    
    // 重置答题状态
    userAnswer.value = '';
    selectedOptions.value = [];
    answerSubmitted.value = false;
    
    console.log('题目数据已加载:', questions.value.length);
  }
}, { deep: true });

// 计时器
let timer = null

// 格式化时间
const formatTime = (seconds) => {
  if (!seconds && seconds !== 0) return '00:00'
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`
}

// 初始化测试
const initExam = async () => {
  loading.value = true;
  loadingMessage.value = '正在加载测试信息...';
  
  // 获取测试信息
  get(`/api/adaptive-exams/${examId}`, null,
    (message, data) => {
      console.log('获取测试信息成功:', data);
      examInfo.value = data;
      
      // 如果测试已完成，则跳转到报告页面
      if (data.status === 'COMPLETED') {
        router.replace(`/student/adaptive-exam/report/${examId}`);
        return;
      }
      
      // 如果测试尚未开始，则启动测试
      if (data.status === 'CREATED') {
        startExam();
        return;
      }
      
      // 处理题目数据
      if (data.questions && data.questions.length > 0) {
        console.log('题目数据:', data.questions);
        // 直接使用API返回的题目数据
        questions.value = data.questions;
        // currentQuestion.value 和 currentQuestionIndex.value 会通过 watch 自动设置
        loading.value = false;
      } else {
        // 如果没有题目数据，则重新加载
        console.log('未找到题目数据，正在重新加载...');
        ElMessage.warning('未找到题目数据，正在重新加载...');
        reloadExam();
      }
      
      // 设置计时器
      if (data.timeLimit) {
        // 如果有剩余时间信息，则使用剩余时间，否则使用完整时间限制
        if (data.remainingTime) {
          timeRemaining.value = data.remainingTime;
        } else {
          timeRemaining.value = data.timeLimit * 60; // 转换为秒
        }
        startTimer();
      }
    },
    (message) => {
      ElMessage.warning(message || '获取测试信息失败');
      loading.value = false;
    },
    () => {
      ElMessage.error('获取测试信息出错');
      loading.value = false;
    }
  );
};

// 启动测试
const startExam = () => {
  loadingMessage.value = '正在启动测试...'
  
  post(`/api/adaptive-exams/${examId}/start`, {},
    () => {
      // 获取题目数据
      reloadExam()
    },
    (message) => {
      ElMessage.warning(message || '启动测试失败')
      loading.value = false
    },
    () => {
      ElMessage.error('启动测试出错')
      loading.value = false
    }
  )
}

// 重新加载测试数据
const reloadExam = () => {
  // 重新请求完整的测试数据，包括题目
  get(`/api/adaptive-exams/${examId}`, null,
    (message, data) => {
      examInfo.value = data
      
      // 如果返回了题目数据，直接使用
      if (data.questions && data.questions.length > 0) {
        questions.value = data.questions
        currentQuestion.value = questions.value[0]
        currentQuestionIndex.value = 0
        currentQuestionNumber.value = 1
        loading.value = false
      } else {
        // 如果仍然没有题目数据，显示错误
        ElMessage.error('无法获取题目数据')
        loading.value = false
      }
    },
    (message) => {
      ElMessage.warning(message || '获取测试信息失败')
      loading.value = false
    },
    () => {
      ElMessage.error('获取测试信息出错')
      loading.value = false
    }
  )
}

// 下一题
const goToNextQuestion = () => {
  if (answerResult.value.isLastQuestion || currentQuestionIndex.value >= questions.value.length - 1) {
    completeExam()
  } else {
    loadingNextQuestion.value = true // 设置加载状态
    
    // 切换到下一题
    currentQuestionIndex.value++
    currentQuestion.value = questions.value[currentQuestionIndex.value]
    currentQuestionNumber.value = currentQuestionIndex.value + 1
    
    // 重置答题状态
    userAnswer.value = ''
    selectedOptions.value = []
    answerSubmitted.value = false
    
    // 注意：这里不重新启动计时器，保持原有计时器继续运行
    
    loadingNextQuestion.value = false
  }
}

// 准备完成测试（模拟进度条）
const prepareCompletion = () => {
  reportProgress.value = 0
  let completionInterval = null
  
  // 开始报告生成进度条
  completionInterval = setInterval(() => {
    reportProgress.value += 1
    
    // 进度到达100%时，清除定时器
    if (reportProgress.value >= 100) {
      clearInterval(completionInterval)
    }
  }, 50)
}

// 完成测试
const completeExam = () => {
  examCompleted.value = true
  
  // 停止计时器
  if (timer) {
    clearInterval(timer)
    timer = null
  }
  
  // 先调用完成测试API
  post(`/api/adaptive-exams/${examId}/complete`, {},
    (message, data) => {
      console.log('测试完成成功:', data)
      
      // 然后获取测试结果
      get(`/api/adaptive-exams/${examId}/result`, null,
        (message, data) => {
          // 结果已准备好，等待用户查看
          console.log('获取测试结果成功:', data)
          // 确保进度条完成
          if (reportProgress.value < 100) {
            reportProgress.value = 100
          }
        },
        (message) => {
          console.error('获取测试结果失败:', message)
          ElMessage.warning(message || '获取测试结果失败，但您可以稍后查看报告')
          // 确保进度条完成，即使有错误
          reportProgress.value = 100
        },
        () => {
          console.error('获取测试结果出错')
          ElMessage.error('获取测试结果出错，但您可以稍后查看报告')
          // 确保进度条完成，即使有错误
          reportProgress.value = 100
        }
      )
    },
    (message) => {
      console.error('测试完成失败:', message)
      ElMessage.warning(message || '测试完成失败，但您可以稍后查看报告')
      // 确保进度条完成，即使有错误
      reportProgress.value = 100
    },
    () => {
      console.error('测试完成出错')
      ElMessage.error('测试完成出错，但您可以稍后查看报告')
      // 确保进度条完成，即使有错误
      reportProgress.value = 100
    }
  )
}

// 查看报告
const viewReport = () => {
  router.push(`/student/adaptive-exam/report/${examId}`)
}

// 确认退出
const confirmExit = () => {
  showExitConfirm.value = true
}

// 退出测试
const exitExam = () => {
  router.push('/student/adaptive-exam')
}

// 开始计时器
const startTimer = () => {
  if (timer) {
    clearInterval(timer)
  }
  
  timer = setInterval(() => {
    if (timeRemaining.value > 0) {
      timeRemaining.value--
    } else {
      // 时间到，自动提交
      clearInterval(timer)
      timer = null
      
      if (!answerSubmitted.value) {
        ElMessage.warning('时间已到，系统将自动提交当前答案')
        submitAnswer()
      }
      
      if (!examCompleted.value) {
        completeExam()
      }
    }
  }, 1000)
}

// 获取题目类型文本
const getQuestionTypeText = (type) => {
  const typeMap = {
    'SINGLE_CHOICE': '单选题',
    'MULTI_CHOICE': '多选题',
    'SHORT_ANSWER': '简答题',
    'FILL_BLANK': '填空题',
    'TRUE_FALSE': '判断题'
  }
  return typeMap[type] || '未知类型';
}

// 获取题目类型样式类
const getQuestionTypeClass = (type) => {
  const classMap = {
    'SINGLE_CHOICE': 'bg-blue-100 text-blue-700',
    'MULTI_CHOICE': 'bg-purple-100 text-purple-700',
    'SHORT_ANSWER': 'bg-green-100 text-green-700',
    'FILL_BLANK': 'bg-amber-100 text-amber-700',
    'TRUE_FALSE': 'bg-indigo-100 text-indigo-700'
  }
  return classMap[type] || 'bg-gray-100 text-gray-700';
}

// 选择选项（单选或多选）
const selectOption = (optionId) => {
  if (answerSubmitted.value) return
  
  if (currentQuestion.value.questionType === 'SINGLE_CHOICE') {
    selectedOptions.value = [optionId]
  } else if (currentQuestion.value.questionType === 'MULTI_CHOICE') {
    const index = selectedOptions.value.indexOf(optionId)
    if (index === -1) {
      selectedOptions.value.push(optionId)
    } else {
      selectedOptions.value.splice(index, 1)
    }
  }
}

// 检查选项是否被选中
const isOptionSelected = (optionId) => {
  return selectedOptions.value.includes(optionId)
}

// 提交答案
const submitAnswer = () => {
  if (!canSubmit.value) {
    ElMessage.warning('请先回答问题');
    return;
  }
  
  // 设置提交中状态
  submittingAnswer.value = true;
  
  // 准备答案数据
  let answer;
  
  switch (currentQuestion.value.questionType) {
    case 'SINGLE_CHOICE':
    case 'MULTI_CHOICE':
      answer = selectedOptions.value.join(',');
      break;
    case 'TRUE_FALSE':
      // 标准化判断题答案
      answer = userAnswer.value === '正确' ? 'TRUE' : 'FALSE';
      break;
    case 'FILL_BLANK':
    case 'SHORT_ANSWER':
      answer = userAnswer.value;
      break;
    default:
      answer = userAnswer.value || selectedOptions.value.join(',');
  }
  
  const answerData = { answer };
  
  console.log('提交答案:', {
    questionId: currentQuestion.value.id,
    questionType: currentQuestion.value.questionType,
    answer: answer
  });
  
  post(`/api/adaptive-exams/${examId}/questions/${currentQuestion.value.id}/submit`, answerData,
    (message, data) => {
      console.log('答案评估结果:', data);
      answerSubmitted.value = true;
      answerResult.value = data;
      lastQuestionId.value = currentQuestion.value.id;
      
      // 判断是否是最后一题
      const isLastQuestion = currentQuestionIndex.value >= questions.value.length - 1;
      answerResult.value.isLastQuestion = isLastQuestion;
      
      // 如果是最后一题，则准备完成测试
      if (isLastQuestion) {
        prepareCompletion();
      }
      submittingAnswer.value = false; // 完成提交
    },
    (message) => {
      // 提交失败但仍然允许继续
      console.error('提交答案失败:', message);
      ElMessage.warning(message || '提交答案失败，但您可以继续测试');
      
      // 创建一个默认的答案结果
      answerSubmitted.value = true;
      answerResult.value = {
        isCorrect: false,
        score: 0,
        correctAnswer: '系统无法评估答案',
        explanation: '由于系统错误，无法评估您的答案。请继续完成测试，或联系管理员。',
        isLastQuestion: currentQuestionIndex.value >= questions.value.length - 1
      };
      
      lastQuestionId.value = currentQuestion.value.id;
      submittingAnswer.value = false;
    },
    () => {
      // 提交出错但仍然允许继续
      console.error('提交答案出错');
      ElMessage.error('提交答案出错，但您可以继续测试');
      
      // 创建一个默认的答案结果
      answerSubmitted.value = true;
      answerResult.value = {
        isCorrect: false,
        score: 0,
        correctAnswer: '系统出错',
        explanation: '由于系统错误，无法评估您的答案。请继续完成测试，或联系管理员。',
        isLastQuestion: currentQuestionIndex.value >= questions.value.length - 1
      };
      
      lastQuestionId.value = currentQuestion.value.id;
      submittingAnswer.value = false;
    }
  );
}

// 重置当前题目状态
const resetQuestion = () => {
  // 此函数已不再需要，保留空实现以防其他地方调用
}

// 组件挂载时初始化测试
onMounted(() => {
  initExam()
})

// 组件卸载前清理计时器
onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})
</script>

<style scoped>
.adaptive-exam-taking {
  min-height: 100vh;
  background-color: #f8fafc;
}

.exam-header {
  backdrop-filter: blur(8px);
  background-color: rgba(255, 255, 255, 0.9);
}

.question-card {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 选项悬停效果 */
.option-item:not(.cursor-not-allowed):hover {
  background-color: #f9fafb;
}

/* 倒计时警告效果 */
@keyframes pulse {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
  100% {
    opacity: 1;
  }
}

.countdown-timer .text-red-500 {
  animation: pulse 1s infinite;
}
</style> 