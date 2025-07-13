<template>
  <div class="adaptive-exam-review-page p-6">
    <div class="page-header mb-6 flex justify-between items-center">
      <div>
        <h1 class="text-2xl font-bold mb-2">自适应测试 - 错题复习</h1>
        <p class="text-gray-500">{{ reviewData.subject }} | {{ reviewData.date }}</p>
      </div>
      <div class="flex gap-2">
        <el-button @click="goToReport">查看完整报告</el-button>
        <el-button @click="goBack" icon="el-icon-back">返回</el-button>
      </div>
    </div>
    
    <!-- 复习进度 -->
    <div class="review-progress bg-white rounded-xl shadow-sm p-6 mb-6">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-lg font-semibold">复习进度</h2>
        <div class="text-sm">
          <span class="font-medium">{{ currentIndex + 1 }}</span> / {{ reviewData.wrongQuestions.length }}
        </div>
      </div>
      
      <el-progress :percentage="progressPercentage" :format="progressFormat" :stroke-width="10" />
      
      <div class="flex justify-between text-sm text-gray-500 mt-2">
        <div>已复习：{{ reviewedCount }}</div>
        <div>剩余：{{ reviewData.wrongQuestions.length - reviewedCount }}</div>
      </div>
    </div>
    
    <!-- 当前题目 -->
    <div class="current-question bg-white rounded-xl shadow-sm p-6 mb-6">
      <div class="question-header flex justify-between items-center mb-4">
        <h2 class="text-lg font-semibold">题目 {{ currentIndex + 1 }}</h2>
        <div class="flex items-center">
          <span class="difficulty-label text-sm text-gray-500 mr-2">难度：</span>
          <el-rate v-model="currentQuestion.difficulty" disabled :max="5" :colors="difficultyColors" />
        </div>
      </div>
      
      <div class="question-content mb-6">
        <div class="question-text text-base mb-4">{{ currentQuestion.content }}</div>
        
        <!-- 选择题 -->
        <div v-if="currentQuestion.type === 'choice'" class="options space-y-3">
          <div v-for="(option, index) in currentQuestion.options" :key="index" 
               class="option p-3 border rounded-md flex items-center cursor-pointer"
               :class="{
                 'border-green-500 bg-green-50': showAnswer && index === currentQuestion.answer,
                 'border-red-500 bg-red-50': showAnswer && index === currentQuestion.userAnswer && index !== currentQuestion.answer,
                 'border-blue-500 bg-blue-50': !showAnswer && index === userAnswer,
                 'hover:border-blue-300': !showAnswer
               }"
               @click="!showAnswer && selectOption(index)">
            <span class="option-marker w-6 h-6 rounded-full border flex items-center justify-center mr-3 text-sm"
                  :class="{
                    'border-green-500 bg-green-500 text-white': showAnswer && index === currentQuestion.answer,
                    'border-red-500 bg-red-500 text-white': showAnswer && index === currentQuestion.userAnswer && index !== currentQuestion.answer,
                    'border-blue-500 bg-blue-500 text-white': !showAnswer && index === userAnswer,
                    'border-gray-300': !showAnswer && index !== userAnswer
                  }">
              {{ ['A', 'B', 'C', 'D'][index] }}
            </span>
            <span>{{ option }}</span>
          </div>
        </div>
        
        <!-- 填空题 -->
        <div v-else-if="currentQuestion.type === 'text'" class="text-answer">
          <el-input v-if="!showAnswer" 
                    v-model="userTextAnswer" 
                    type="textarea" 
                    :rows="3" 
                    placeholder="请输入你的答案" 
                    class="mb-4" />
          
          <div v-if="showAnswer" class="answer-comparison space-y-3">
            <div class="user-answer p-3 rounded-md" :class="{'bg-green-50': isTextAnswerCorrect, 'bg-red-50': !isTextAnswerCorrect}">
              <div class="text-xs text-gray-500 mb-1">你的答案：</div>
              <div class="text-sm" :class="{'text-green-600': isTextAnswerCorrect, 'text-red-600': !isTextAnswerCorrect}">
                {{ userTextAnswer || '未作答' }}
              </div>
            </div>
            
            <div class="correct-answer p-3 bg-green-50 rounded-md">
              <div class="text-xs text-gray-500 mb-1">正确答案：</div>
              <div class="text-sm text-green-600">{{ currentQuestion.answer }}</div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 操作按钮 -->
      <div class="question-actions flex justify-center space-x-4">
        <el-button v-if="!showAnswer" type="primary" @click="checkAnswer">提交答案</el-button>
        <el-button v-else type="success" @click="nextQuestion">下一题</el-button>
      </div>
    </div>
    
    <!-- 解析区域 -->
    <div v-if="showAnswer" class="explanation bg-white rounded-xl shadow-sm p-6 mb-6">
      <h2 class="text-lg font-semibold mb-4">解析</h2>
      
      <div class="explanation-content">
        <div class="explanation-text text-sm mb-4">{{ currentQuestion.explanation }}</div>
        
        <div class="knowledge-points">
          <div class="text-sm text-gray-500 mb-2">相关知识点：</div>
          <div class="tags flex flex-wrap gap-2">
            <span v-for="(tag, index) in currentQuestion.tags" :key="index" 
                  class="tag px-3 py-1 bg-blue-100 text-blue-600 rounded-full text-xs">
              {{ tag }}
            </span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 知识点学习资源 -->
    <div v-if="showAnswer" class="learning-resources bg-white rounded-xl shadow-sm p-6">
      <h2 class="text-lg font-semibold mb-4">学习资源</h2>
      
      <div class="resources-list grid grid-cols-1 md:grid-cols-2 gap-4">
        <div v-for="(resource, index) in currentQuestion.resources" :key="index" 
             class="resource-card p-4 border rounded-lg hover:border-blue-300 cursor-pointer">
          <div class="resource-type text-xs text-gray-500 mb-1">{{ resource.type }}</div>
          <div class="resource-title text-base font-medium mb-2">{{ resource.title }}</div>
          <div class="resource-description text-sm text-gray-600 mb-3">{{ resource.description }}</div>
          <div class="resource-link text-sm text-blue-600 flex items-center">
            查看资源
            <span class="icon ml-1">→</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 底部导航 -->
    <div class="navigation-buttons flex justify-between mt-6">
      <el-button :disabled="currentIndex === 0" @click="prevQuestion" icon="el-icon-arrow-left">上一题</el-button>
      
      <div class="question-nav flex items-center">
        <el-popover placement="top" width="400" trigger="click">
          <template #reference>
            <el-button plain>题目导航</el-button>
          </template>
          <div class="question-nav-grid grid grid-cols-5 gap-2 p-2">
            <div v-for="(_, index) in reviewData.wrongQuestions" :key="index" 
                 class="question-nav-item w-10 h-10 rounded-md flex items-center justify-center cursor-pointer text-sm"
                 :class="{
                   'bg-blue-500 text-white': index === currentIndex,
                   'bg-green-100 text-green-600': questionStatus[index] === 'correct',
                   'bg-red-100 text-red-600': questionStatus[index] === 'wrong',
                   'bg-gray-100': questionStatus[index] === 'unanswered'
                 }"
                 @click="goToQuestion(index)">
              {{ index + 1 }}
            </div>
          </div>
        </el-popover>
      </div>
      
      <el-button :disabled="currentIndex === reviewData.wrongQuestions.length - 1" @click="nextQuestion" icon="el-icon-arrow-right" icon-position="right">
        下一题
      </el-button>
    </div>
    
    <!-- 复习完成对话框 -->
    <el-dialog v-model="showCompletionDialog" title="复习完成" width="30%" center>
      <div class="completion-content text-center">
        <div class="icon text-5xl mb-4">🎉</div>
        <h3 class="text-xl font-bold mb-2">恭喜你完成了错题复习！</h3>
        <p class="text-gray-500 mb-6">你已经复习了全部 {{ reviewData.wrongQuestions.length }} 道错题</p>
        
        <div class="stats grid grid-cols-2 gap-4 mb-6">
          <div class="stat p-3 bg-gray-50 rounded-lg">
            <div class="text-2xl font-bold text-blue-600">{{ correctCount }}</div>
            <div class="text-sm text-gray-500">本次复习正确</div>
          </div>
          <div class="stat p-3 bg-gray-50 rounded-lg">
            <div class="text-2xl font-bold text-green-600">{{ Math.round(correctCount / reviewData.wrongQuestions.length * 100) }}%</div>
            <div class="text-sm text-gray-500">正确率</div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer flex justify-center space-x-4">
          <el-button @click="restartReview">再次复习</el-button>
          <el-button type="primary" @click="finishReview">完成复习</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const reviewId = route.params.id

// 复习数据
const reviewData = ref({
  id: reviewId,
  subject: '高等数学',
  date: '2023-11-05',
  wrongQuestions: [
    {
      id: 1,
      type: 'choice',
      content: '函数f(x)=sin(x)的泰勒展开式的前三项是：',
      options: ['x', 'x-x³/3!', 'x-x³/3!+x⁵/5!', 'x-x²/2!+x³/3!'],
      answer: 2, // 索引，正确答案是C
      userAnswer: 1,
      difficulty: 3,
      explanation: 'sin(x)的泰勒展开式为：x-x³/3!+x⁵/5!-x⁷/7!+...，所以前三项是x-x³/3!+x⁵/5!。',
      tags: ['泰勒展开', '三角函数', '级数'],
      resources: [
        {
          type: '视频',
          title: '泰勒级数详解',
          description: '详细讲解泰勒级数的概念和常见函数的泰勒展开',
          link: '#'
        },
        {
          type: '练习题',
          title: '泰勒展开练习题集',
          description: '包含20道泰勒展开相关练习题，带详细解析',
          link: '#'
        }
      ]
    },
    {
      id: 2,
      type: 'choice',
      content: '矩阵A=[[1,2],[3,4]]的行列式等于：',
      options: ['-2', '-1', '1', '2'],
      answer: 0, // 索引，正确答案是A
      userAnswer: 3,
      difficulty: 2,
      explanation: '矩阵A的行列式为|A|=1×4-2×3=4-6=-2。',
      tags: ['行列式', '矩阵'],
      resources: [
        {
          type: '文章',
          title: '行列式计算方法总结',
          description: '详细介绍二阶和三阶行列式的计算方法和技巧',
          link: '#'
        },
        {
          type: '练习题',
          title: '矩阵行列式练习题',
          description: '包含15道矩阵行列式计算练习题，难度递增',
          link: '#'
        }
      ]
    },
    {
      id: 3,
      type: 'text',
      content: '计算极限：lim(x→∞) (1+1/x)^x',
      answer: 'e',
      userAnswer: '1',
      difficulty: 4,
      explanation: '这是一个重要的极限，lim(x→∞) (1+1/x)^x = e，是自然对数的底数。',
      tags: ['极限', '自然对数'],
      resources: [
        {
          type: '视频',
          title: '重要极限详解',
          description: '详细讲解常见的重要极限及其应用',
          link: '#'
        },
        {
          type: '文章',
          title: '自然对数e的由来',
          description: '介绍自然对数e的数学意义和在实际中的应用',
          link: '#'
        }
      ]
    },
    {
      id: 4,
      type: 'choice',
      content: '下列哪个级数收敛？',
      options: ['Σ(1/n)', 'Σ(1/n²)', 'Σ(1/√n)', 'Σ(n/(n+1))'],
      answer: 1, // 索引，正确答案是B
      userAnswer: 2,
      difficulty: 3,
      explanation: 'Σ(1/n)是调和级数，发散；Σ(1/n²)是p级数，p=2>1，收敛；Σ(1/√n)是p级数，p=1/2<1，发散；Σ(n/(n+1))的极限不为0，发散。',
      tags: ['级数收敛性', 'p级数'],
      resources: [
        {
          type: '视频',
          title: '级数收敛性判断',
          description: '详细讲解各种级数的收敛性判断方法',
          link: '#'
        },
        {
          type: '练习题',
          title: '级数收敛性练习题',
          description: '包含多种类型的级数收敛性判断题',
          link: '#'
        }
      ]
    },
    {
      id: 5,
      type: 'text',
      content: '求函数f(x)=x³-3x²+2x在区间[0,2]上的最大值和最小值。',
      answer: '最大值为0，最小值为-1',
      userAnswer: '最大值为2，最小值为0',
      difficulty: 4,
      explanation: '求导得f\'(x)=3x²-6x+2，令f\'(x)=0，解得x=1±√(1/3)。检验可知x=1-√(1/3)处取到最大值0，x=1+√(1/3)处取到最小值-1。',
      tags: ['导数应用', '最值问题'],
      resources: [
        {
          type: '文章',
          title: '导数在最值问题中的应用',
          description: '详细讲解如何利用导数求函数的最大值和最小值',
          link: '#'
        },
        {
          type: '练习题',
          title: '最值问题练习题',
          description: '包含20道使用导数求最值的练习题',
          link: '#'
        }
      ]
    }
  ]
})

// 当前题目索引
const currentIndex = ref(0)

// 计算当前题目
const currentQuestion = computed(() => {
  return reviewData.value.wrongQuestions[currentIndex.value] || {}
})

// 用户选择的答案（选择题）
const userAnswer = ref(null)

// 用户输入的答案（填空题）
const userTextAnswer = ref('')

// 是否显示答案解析
const showAnswer = ref(false)

// 题目状态记录
const questionStatus = ref({})

// 已复习题目数量
const reviewedCount = computed(() => {
  return Object.values(questionStatus.value).filter(status => status === 'correct' || status === 'wrong').length
})

// 正确题目数量
const correctCount = computed(() => {
  return Object.values(questionStatus.value).filter(status => status === 'correct').length
})

// 进度百分比
const progressPercentage = computed(() => {
  return Math.round((reviewedCount.value / reviewData.value.wrongQuestions.length) * 100)
})

// 进度格式化
const progressFormat = (percentage) => {
  return `${percentage}%`
}

// 填空题答案是否正确
const isTextAnswerCorrect = computed(() => {
  if (currentQuestion.value.type !== 'text') return false
  return userTextAnswer.value.trim().toLowerCase() === currentQuestion.value.answer.trim().toLowerCase()
})

// 难度颜色
const difficultyColors = ['#67C23A', '#E6A23C', '#F56C6C']

// 是否显示完成对话框
const showCompletionDialog = ref(false)

// 选择选项
const selectOption = (index) => {
  userAnswer.value = index
}

// 检查答案
const checkAnswer = () => {
  if (currentQuestion.value.type === 'choice' && userAnswer.value === null) {
    ElMessage.warning('请选择一个选项')
    return
  }
  
  if (currentQuestion.value.type === 'text' && !userTextAnswer.value.trim()) {
    ElMessage.warning('请输入你的答案')
    return
  }
  
  showAnswer.value = true
  
  // 记录答题状态
  if (currentQuestion.value.type === 'choice') {
    questionStatus.value[currentIndex.value] = userAnswer.value === currentQuestion.value.answer ? 'correct' : 'wrong'
  } else {
    questionStatus.value[currentIndex.value] = isTextAnswerCorrect.value ? 'correct' : 'wrong'
  }
  
  // 检查是否全部完成
  if (reviewedCount.value === reviewData.value.wrongQuestions.length) {
    setTimeout(() => {
      showCompletionDialog.value = true
    }, 1000)
  }
}

// 下一题
const nextQuestion = () => {
  if (currentIndex.value < reviewData.value.wrongQuestions.length - 1) {
    currentIndex.value++
    resetQuestion()
  }
}

// 上一题
const prevQuestion = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--
    resetQuestion()
  }
}

// 跳转到指定题目
const goToQuestion = (index) => {
  currentIndex.value = index
  resetQuestion()
}

// 重置当前题目状态
const resetQuestion = () => {
  showAnswer.value = questionStatus.value[currentIndex.value] ? true : false
  userAnswer.value = null
  userTextAnswer.value = ''
  
  // 如果是已答过的填空题，恢复用户答案
  if (currentQuestion.value.type === 'text' && showAnswer.value) {
    userTextAnswer.value = currentQuestion.value.userAnswer || ''
  }
}

// 再次复习
const restartReview = () => {
  showCompletionDialog.value = false
  currentIndex.value = 0
  questionStatus.value = {}
  resetQuestion()
}

// 完成复习
const finishReview = () => {
  showCompletionDialog.value = false
  router.push(`/student/adaptive-exam/report/${reviewId}`)
}

// 查看完整报告
const goToReport = () => {
  router.push(`/student/adaptive-exam/report/${reviewId}`)
}

// 返回
const goBack = () => {
  router.push('/student/exams')
}

// 监听当前题目变化，更新用户答案
watch(currentIndex, () => {
  if (currentQuestion.value.type === 'choice') {
    userAnswer.value = showAnswer.value ? currentQuestion.value.userAnswer : null
  } else if (currentQuestion.value.type === 'text') {
    userTextAnswer.value = showAnswer.value ? currentQuestion.value.userAnswer : ''
  }
})

onMounted(() => {
  // 初始化题目状态
  reviewData.value.wrongQuestions.forEach((_, index) => {
    questionStatus.value[index] = 'unanswered'
  })
  
  // 这里应该从后端获取复习数据
  // 模拟加载数据
  setTimeout(() => {
    // reviewData.value = ...
    resetQuestion()
  }, 500)
})
</script>

<style scoped>
.adaptive-exam-review-page {
  min-height: calc(100vh - 64px);
  background-color: #f8fafc;
}

.review-progress, .current-question, .explanation, .learning-resources {
  animation: fadeIn 0.6s ease-out;
}

.explanation {
  animation-delay: 0.2s;
}

.learning-resources {
  animation-delay: 0.4s;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 选项悬停效果 */
.option:hover:not(.border-green-500):not(.border-red-500):not(.border-blue-500) {
  border-color: #e5e7eb;
  background-color: #f9fafb;
}
</style> 