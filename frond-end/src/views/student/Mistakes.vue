<template>
  <div class="mistakes-wrapper">
    <div class="toolbar sticky top-0 z-20 flex flex-wrap gap-3 items-center bg-white/80 backdrop-blur-lg px-4 py-3 rounded-xl shadow-md mb-6">
      <div class="flex flex-wrap gap-2 items-center">
        <el-radio-group v-model="filter" size="small">
          <el-radio-button label="all">全部错题</el-radio-button>
          <el-radio-button label="important">重点错题</el-radio-button>
          <el-radio-button label="recent">最近错题</el-radio-button>
        </el-radio-group>
        
        <div class="ml-0 md:ml-4">
          <el-select v-model="subjectFilter" size="small" placeholder="科目" class="w-24">
            <el-option label="全部" value="all" />
            <el-option label="高数" value="math" />
            <el-option label="线代" value="linear" />
            <el-option label="物理" value="physics" />
          </el-select>
        </div>
      </div>
      
      <div class="ml-auto">
        <el-input
          v-model="search"
          placeholder="搜索错题…"
          size="small"
          clearable
          prefix-icon="el-icon-search"
          class="w-40 md:w-60"
        />
      </div>
    </div>
    
    <el-tabs v-model="activeTab" class="custom-tabs">
      <!-- 列表 -->
      <el-tab-pane label="错题列表" name="list">
        <div class="mistake-stats grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
          <div class="stat-card bg-white rounded-xl shadow-md p-4 flex items-center">
            <div class="stat-icon bg-red-100 p-3 rounded-lg">
              <i class="el-icon-warning-outline text-red-600 text-xl"></i>
            </div>
            <div class="ml-4">
              <div class="text-sm text-gray-500">错题总数</div>
              <div class="text-xl font-semibold">{{ mistakes.length }}</div>
            </div>
          </div>
          
          <div class="stat-card bg-white rounded-xl shadow-md p-4 flex items-center">
            <div class="stat-icon bg-amber-100 p-3 rounded-lg">
              <i class="el-icon-star-on text-amber-600 text-xl"></i>
            </div>
            <div class="ml-4">
              <div class="text-sm text-gray-500">重点错题</div>
              <div class="text-xl font-semibold">{{ importantCount }}</div>
            </div>
          </div>
          
          <div class="stat-card bg-white rounded-xl shadow-md p-4 flex items-center">
            <div class="stat-icon bg-green-100 p-3 rounded-lg">
              <i class="el-icon-check text-green-600 text-xl"></i>
            </div>
            <div class="ml-4">
              <div class="text-sm text-gray-500">已复习</div>
              <div class="text-xl font-semibold">{{ reviewedCount }}/{{ mistakes.length }}</div>
            </div>
          </div>
          
          <div class="stat-card bg-white rounded-xl shadow-md p-4 flex items-center">
            <div class="stat-icon bg-blue-100 p-3 rounded-lg">
              <i class="el-icon-refresh-right text-blue-600 text-xl"></i>
            </div>
            <div class="ml-4">
              <div class="text-sm text-gray-500">复习正确率</div>
              <div class="text-xl font-semibold">{{ reviewAccuracy }}%</div>
            </div>
          </div>
        </div>
        
        <transition-group name="flip" class="mistake-grid gap-4">
          <div v-for="m in filteredMistakes" :key="m.id" 
               class="mistake-item group" 
               :class="{'border-amber-300': m.important}" 
               @click="startRedo(m)">
            <div class="flex items-start justify-between">
              <div class="flex-1">
                <div class="flex items-center gap-2 mb-2">
                  <span class="subject-tag" :class="`bg-${getSubjectColor(m.subject)}-100 text-${getSubjectColor(m.subject)}-700`">
                    {{ m.subject }}
                  </span>
                  <span v-if="m.important" class="important-tag">重点</span>
                </div>
                <div class="q text-base">{{ m.question }}</div>
                <div class="text-xs text-gray-500 mt-2">
                  <span>错误次数: {{ m.errorCount }}</span>
                  <span class="mx-2">|</span>
                  <span>最近错误: {{ formatDate(m.lastErrorDate) }}</span>
                </div>
              </div>
              <div class="action-buttons opacity-0 group-hover:opacity-100 transition-opacity">
                <el-button type="primary" size="small" @click.stop="startRedo(m)" class="action-btn">
                  复习
                </el-button>
                <el-button type="text" size="small" @click.stop="toggleImportant(m)" class="ml-2">
                  <i :class="m.important ? 'el-icon-star-on text-amber-500' : 'el-icon-star-off text-gray-400'"></i>
                </el-button>
              </div>
            </div>
          </div>
        </transition-group>
        
        <div v-if="filteredMistakes.length === 0" class="empty-state flex flex-col items-center justify-center py-16">
          <div class="w-20 h-20 rounded-full bg-gray-100 flex items-center justify-center mb-4">
            <i class="el-icon-search text-4xl text-gray-400"></i>
          </div>
          <h3 class="text-lg font-medium text-gray-600">未找到错题</h3>
          <p class="text-sm text-gray-500 mt-2">尝试调整筛选条件或搜索其他内容</p>
        </div>
      </el-tab-pane>
      
      <!-- 重做 -->
      <el-tab-pane label="错题复习" name="redo">
        <div v-if="selected" class="redo-box bg-white rounded-xl shadow-md p-6">
          <div class="flex justify-between items-center mb-4">
            <div class="flex items-center gap-2">
              <span class="subject-tag" :class="`bg-${getSubjectColor(selected.subject)}-100 text-${getSubjectColor(selected.subject)}-700`">
                {{ selected.subject }}
              </span>
              <span v-if="selected.important" class="important-tag">重点</span>
            </div>
            <el-button type="text" @click="toggleImportant(selected)">
              <i :class="selected.important ? 'el-icon-star-on text-amber-500' : 'el-icon-star-off text-gray-400'"></i>
              {{ selected.important ? '取消标记' : '标记重点' }}
            </el-button>
          </div>
          
          <h3 class="text-lg font-medium mb-4">{{ selected.question }}</h3>
          
          <div class="mb-6">
            <div class="text-sm text-gray-500 mb-2">你的答案:</div>
            <el-input 
              v-model="userAnswer" 
              type="textarea" 
              :rows="3" 
              placeholder="输入你的答案..."
              class="w-full"
            />
          </div>
          
          <div class="flex justify-between mt-8">
            <el-button @click="activeTab = 'list'">返回列表</el-button>
            <div>
              <el-button @click="skipQuestion" plain>跳过</el-button>
              <el-button type="primary" @click="submitAnswer">提交答案</el-button>
            </div>
          </div>
          
          <transition name="fade">
            <div v-if="showAnswer" class="answer-section mt-6 pt-6 border-t border-gray-100">
              <div class="text-sm font-medium text-gray-700 mb-2">正确答案:</div>
              <div class="correct-answer p-3 bg-green-50 border border-green-100 rounded-lg text-green-800">
                {{ selected.correct }}
              </div>
              
              <div class="mt-6">
                <div class="text-sm font-medium text-gray-700 mb-2">解析:</div>
                <div class="explanation p-3 bg-blue-50 border border-blue-100 rounded-lg text-blue-800">
                  {{ selected.explanation || '暂无解析' }}
                </div>
              </div>
              
              <div class="flex justify-center mt-6">
                <el-button type="success" @click="markAsReviewed(true)">我答对了</el-button>
                <el-button type="danger" @click="markAsReviewed(false)" class="ml-4">我答错了</el-button>
              </div>
            </div>
          </transition>
        </div>
        
        <div v-else class="empty-state flex flex-col items-center justify-center py-16">
          <div class="w-20 h-20 rounded-full bg-gray-100 flex items-center justify-center mb-4">
            <i class="el-icon-document text-4xl text-gray-400"></i>
          </div>
          <h3 class="text-lg font-medium text-gray-600">请选择一道错题开始复习</h3>
          <p class="text-sm text-gray-500 mt-2 mb-4">从错题列表中选择需要复习的题目</p>
          <el-button type="primary" @click="activeTab = 'list'">查看错题列表</el-button>
        </div>
      </el-tab-pane>
      
      <!-- 统计分析 -->
      <el-tab-pane label="错题分析" name="analysis">
        <div class="analysis-wrapper bg-white rounded-xl shadow-md p-6">
          <h3 class="text-lg font-medium mb-6">错题分布</h3>
          
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div class="chart-container bg-gray-50 p-4 rounded-lg">
              <div class="text-center mb-4">科目分布</div>
              <div class="h-64 flex items-center justify-center text-gray-400">
                图表区域 - 科目分布饼图
              </div>
            </div>
            
            <div class="chart-container bg-gray-50 p-4 rounded-lg">
              <div class="text-center mb-4">错误类型分布</div>
              <div class="h-64 flex items-center justify-center text-gray-400">
                图表区域 - 错误类型柱状图
              </div>
            </div>
          </div>
          
          <div class="mt-6">
            <!-- 这里缺少内容，保留空的div结构 -->
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const activeTab = ref('list')
const filter = ref('all')
const subjectFilter = ref('all')
const search = ref('')
const userAnswer = ref('')
const showAnswer = ref(false)

const mistakes = ref([
  { 
    id: 1, 
    question: '∫x^2 dx = ?', 
    correct: '1/3 x^3 + C', 
    subject: 'math',
    important: false,
    errorCount: 2,
    lastErrorDate: new Date(2023, 9, 15),
    explanation: '幂函数求积分，指数加1，除以新指数'
  },
  { 
    id: 2, 
    question: '矩阵A可逆条件是？', 
    correct: 'det(A) ≠ 0', 
    subject: 'linear',
    important: true,
    errorCount: 1,
    lastErrorDate: new Date(2023, 10, 5),
    explanation: '矩阵可逆当且仅当其行列式不为零'
  },
])

const selected = ref(null)
const importantCount = computed(() => mistakes.value.filter(m => m.important).length)
const reviewedCount = ref(0)
const reviewAccuracy = ref(85)

const filteredMistakes = computed(() => {
  let result = [...mistakes.value]
  
  // 筛选重要性
  if (filter.value === 'important') {
    result = result.filter(m => m.important)
  } else if (filter.value === 'recent') {
    result = result.sort((a, b) => new Date(b.lastErrorDate) - new Date(a.lastErrorDate))
  }
  
  // 筛选科目
  if (subjectFilter.value !== 'all') {
    result = result.filter(m => m.subject === subjectFilter.value)
  }
  
  // 搜索
  if (search.value) {
    const searchLower = search.value.toLowerCase()
    result = result.filter(m => 
      m.question.toLowerCase().includes(searchLower) || 
      m.correct.toLowerCase().includes(searchLower)
    )
  }
  
  return result
})

const startRedo = (m) => { 
  selected.value = m
  activeTab.value = 'redo'
  userAnswer.value = ''
  showAnswer.value = false
}

const submitAnswer = () => {
  showAnswer.value = true
}

const skipQuestion = () => {
  showAnswer.value = true
}

const markAsReviewed = (isCorrect) => {
  // 这里应该添加逻辑来记录复习结果
  selected.value = null
  activeTab.value = 'list'
}

const toggleImportant = (mistake) => {
  mistake.important = !mistake.important
}

const getSubjectColor = (subject) => {
  const colors = {
    'math': 'blue',
    'linear': 'green',
    'physics': 'purple'
  }
  return colors[subject] || 'gray'
}

const formatDate = (date) => {
  if (!date) return '未知'
  if (typeof date === 'string') date = new Date(date)
  return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`
}
</script>

<style scoped>
.mistakes-wrapper { 
  @apply p-6; 
}

.mistake-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 1rem;
}

.mistake-item { 
  @apply bg-white rounded-xl shadow-md px-4 py-3 transition-transform duration-300 cursor-pointer; 
  border: 1px solid #f1f5f9; 
}

.mistake-item:hover {
  box-shadow: 0 12px 28px rgba(99, 102, 241, .18);
  transform: translateY(-6px) scale(1.02);
} 

.subject-tag {
  @apply text-xs px-2 py-0.5 rounded-full;
}

.important-tag {
  @apply bg-amber-100 text-amber-700 text-xs px-2 py-0.5 rounded-full;
}

.flip-enter-from {
  opacity: 0;
  transform: rotateX(-90deg);
} 

.flip-enter-active {
  transition: all .4s cubic-bezier(.175, .885, .32, 1.1);
} 

.flip-enter-to {
  opacity: 1;
  transform: rotateX(0);
} 

.flip-leave-active {
  display: none;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}

.redo-box { 
  animation: fade .3s ease; 
}

@keyframes fade {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  } 
}
</style> 