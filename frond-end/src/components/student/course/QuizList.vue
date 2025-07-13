

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/counter'
import { get, post } from '@/net'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const courseId = route.params.courseId
const loading = ref(false)
const quizzes = ref([])

// 获取课程测验列表
const fetchQuizzes = () => {
  loading.value = true
  
  get(`/api/student/courses/${courseId}/quizzes`, 
    { userId: authStore.user?.userId },
    (message, data) => {
      quizzes.value = data || []
      loading.value = false
    },
    (message) => {
      ElMessage.error(message || '获取测验列表失败')
      loading.value = false
    }
  )
}

const filterType = ref('all')
const searchQuery = ref('')

// 过滤测验
const filteredQuizzes = computed(() => {
  if (!quizzes.value || quizzes.value.length === 0) return [];
  
  return quizzes.value.filter(quiz => {
    if (!quiz) return false;
    const matchType = filterType.value === 'all' || quiz.quizType?.toLowerCase() === filterType.value
    const matchSearch = quiz.title?.toLowerCase().includes(searchQuery.value.toLowerCase())
    return matchType && matchSearch
  })
})

// 状态颜色映射
const statusColorMap = {
  0: 'primary',  // 未开始
  1: 'warning',  // 进行中
  2: 'info',     // 已结束但未提交
  3: 'success'   // 已提交
}

// 状态文本映射
const statusTextMap = {
  0: '未开始',
  1: '进行中',
  2: '待提交',
  3: '已完成'
}

// 类型图标映射
const typeIconMap = {
  quiz: 'fa-clipboard-check',
  practice: 'fa-dumbbell',
  exam: 'fa-file-alt'
}

// 类型文本映射
const typeTextMap = {
  quiz: '测验',
  practice: '练习',
  exam: '考试'
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 跳转到测验
const goToQuiz = (quiz) => {
  if (quiz.status === 3) { // 已完成
    // 查看结果
    router.push(`/student/exams/result/${quiz.id}`)
  } else if (quiz.status === 1) { // 进行中
    // 开始测验
    router.push(`/student/exams/take/${quiz.id}`)
  } else {
    ElMessage.info('该测验暂时无法参与')
  }
}

onMounted(() => {
  fetchQuizzes()
})
</script>

<template>
  <div class="quiz-list-container p-6" v-loading="loading">
    <!-- 工具栏 -->
    <div class="flex flex-wrap gap-3 justify-between items-center mb-6">
      <h3 class="text-lg font-medium">练习 & 测验</h3>
      <div class="flex gap-3 items-center">
        <el-input 
          v-model="searchQuery" 
          placeholder="搜索..." 
          prefix-icon="el-icon-search"
          size="small" 
          clearable 
          class="w-48"
        />
        <el-select v-model="filterType" size="small" class="w-32">
          <el-option label="全部" value="all" />
          <el-option label="测验" value="quiz" />
          <el-option label="练习" value="practice" />
          <el-option label="考试" value="exam" />
        </el-select>
      </div>
    </div>
    
    <!-- 测验列表 -->
    <div class="grid gap-4">
      <div 
        v-for="quiz in filteredQuizzes" 
        :key="quiz.id" 
        class="quiz-card bg-white rounded-xl shadow-md overflow-hidden hover:shadow-lg transition-shadow duration-200"
        @click="goToQuiz(quiz)"
      >
        <div class="flex items-stretch">
          <!-- 左侧类型标识 -->
          <div class="w-16 flex items-center justify-center" :class="`bg-${quiz.quizType?.toLowerCase() || 'quiz'}-color`">
            <i :class="['fas', typeIconMap[quiz.quizType?.toLowerCase() || 'quiz'], 'text-white text-xl']"></i>
          </div>
          
          <!-- 主要内容 -->
          <div class="flex-1 p-4">
            <div class="flex justify-between items-start">
              <div>
                <h4 class="font-medium text-lg mb-1">{{ quiz.title || '未命名测验' }}</h4>
                <div class="flex items-center gap-2 text-sm text-gray-500">
                  <span class="badge" :class="`badge-${quiz.quizType?.toLowerCase() || 'quiz'}`">
                    {{ typeTextMap[quiz.quizType?.toLowerCase() || 'quiz'] }}
                  </span>
                  <span>{{ quiz.questionCount || 0 }} 题</span>
                  <span v-if="quiz.duration">• {{ quiz.duration }} 分钟</span>
                </div>
              </div>
              
              <el-tag size="small" :type="statusColorMap[quiz.status || 0]">
                {{ statusTextMap[quiz.status || 0] }}
              </el-tag>
            </div>
            
            <!-- 进度或得分 -->
            <div class="mt-3">
              <template v-if="quiz.status === 3">
                <div class="flex items-center">
                  <div class="text-sm text-gray-500 mr-2">得分：</div>
                  <div class="font-bold" :class="(quiz.score || 0) >= 60 ? 'text-green-500' : 'text-red-500'">
                    {{ quiz.score || 0 }}/{{ quiz.totalScore || 100 }}
                  </div>
                </div>
              </template>
              
              <template v-else-if="quiz.status === 1 && quiz.completedCount > 0">
                <div class="text-sm text-gray-500 mb-1">
                  已完成：{{ quiz.completedCount || 0 }}/{{ quiz.questionCount || 0 }}
                </div>
                <div class="w-full bg-gray-200 rounded-full h-1.5">
                  <div 
                    class="bg-blue-600 h-1.5 rounded-full" 
                    :style="{width: Math.round(((quiz.completedCount || 0) / (quiz.questionCount || 1)) * 100) + '%'}"
                  ></div>
                </div>
              </template>
              
              <template v-else>
                <div class="text-sm text-gray-500" v-if="quiz.endTime">
                  截止日期：{{ formatDate(quiz.endTime) }}
                </div>
              </template>
            </div>
          </div>
          
          <!-- 右侧操作按钮 -->
          <div class="flex items-center pr-4">
            <el-button 
              :type="quiz.status === 3 ? 'info' : 'primary'" 
              size="small" 
              :icon="quiz.status === 3 ? 'el-icon-view' : 'el-icon-right'"
              plain
            >
              {{ quiz.status === 3 ? '查看结果' : '开始' }}
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-if="filteredQuizzes.length === 0" class="text-center py-12 text-gray-400">
        <i class="fas fa-clipboard-list text-4xl mb-3"></i>
        <p>暂无{{ filterType === 'all' ? '' : typeTextMap[filterType] }}内容</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.quiz-card {
  cursor: pointer;
}

.bg-quiz-color {
  background-color: #409EFF;
}

.bg-practice-color {
  background-color: #67C23A;
}

.bg-exam-color {
  background-color: #E6A23C;
}

.badge {
  display: inline-block;
  padding: 0.15rem 0.5rem;
  border-radius: 1rem;
  font-size: 0.75rem;
}

.badge-quiz {
  background-color: #ecf5ff;
  color: #409EFF;
}

.badge-practice {
  background-color: #f0f9eb;
  color: #67C23A;
}

.badge-exam {
  background-color: #fdf6ec;
  color: #E6A23C;
}
</style>