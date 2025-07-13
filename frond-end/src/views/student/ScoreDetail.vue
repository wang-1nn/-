<template>
  <div class="score-detail-page p-6">
    <div class="page-header mb-6 flex justify-between items-center">
      <div>
        <h1 class="text-2xl font-bold mb-2">{{ examData.title }} - 成绩详情</h1>
        <p class="text-gray-500">{{ examData.subject }} | {{ examData.date }}</p>
      </div>
      <el-button @click="goBack" icon="el-icon-back">返回</el-button>
    </div>
    
    <!-- 成绩概览 -->
    <div class="score-overview bg-white rounded-xl shadow-sm p-6 mb-6">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div class="score-card text-center">
          <div class="text-4xl font-bold mb-2" :class="getScoreClass(examData.score)">
            {{ examData.score }}
          </div>
          <div class="text-sm text-gray-500">总分 / {{ examData.totalScore }}</div>
        </div>
        
        <div class="score-card text-center">
          <div class="text-4xl font-bold mb-2">{{ examData.rank }}</div>
          <div class="text-sm text-gray-500">排名 / {{ examData.totalStudents }}</div>
        </div>
        
        <div class="score-card text-center">
          <div class="text-4xl font-bold mb-2">{{ examData.correctCount }}</div>
          <div class="text-sm text-gray-500">正确题数 / {{ examData.totalQuestions }}</div>
        </div>
        
        <div class="score-card text-center">
          <div class="text-4xl font-bold mb-2">{{ examData.accuracy }}%</div>
          <div class="text-sm text-gray-500">正确率</div>
        </div>
      </div>
    </div>
    
    <!-- 成绩分析 -->
    <div class="score-analysis bg-white rounded-xl shadow-sm p-6 mb-6">
      <h2 class="text-lg font-semibold mb-4">成绩分析</h2>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- 左侧：得分分布 -->
        <div class="score-distribution">
          <h3 class="text-base font-medium mb-3">得分分布</h3>
          <div class="chart-container h-64 bg-gray-50 rounded-lg p-4 flex items-center justify-center">
            <div class="w-full h-full relative">
              <!-- 这里应该是图表，这里用简单的条形图示意 -->
              <div class="absolute bottom-0 left-0 w-full h-full flex items-end justify-around">
                <div v-for="(item, index) in scoreDistribution" :key="index" class="score-bar-container flex flex-col items-center">
                  <div class="score-value text-xs mb-1">{{ item.count }}</div>
                  <div class="score-bar rounded-t-sm" 
                       :style="`height: ${(item.count / maxCount) * 100}%; width: 30px; background-color: ${item.color}`"></div>
                  <div class="score-label text-xs mt-2">{{ item.label }}</div>
                </div>
              </div>
            </div>
          </div>
          <div class="text-sm text-gray-500 mt-2 text-center">
            班级平均分：{{ examData.classAverage }}，最高分：{{ examData.classHighest }}
          </div>
        </div>
        
        <!-- 右侧：知识点分析 -->
        <div class="knowledge-analysis">
          <h3 class="text-base font-medium mb-3">知识点掌握情况</h3>
          <div class="knowledge-points space-y-4">
            <div v-for="(point, index) in knowledgePoints" :key="index" class="knowledge-point">
              <div class="flex justify-between mb-1">
                <span class="text-sm">{{ point.name }}</span>
                <span class="text-sm">{{ point.mastery }}%</span>
              </div>
              <div class="h-2 bg-gray-100 rounded-full overflow-hidden">
                <div class="h-full rounded-full" 
                     :style="`width: ${point.mastery}%; background: ${getKnowledgePointColor(point.mastery)}`"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 答题详情 -->
    <div class="answer-details bg-white rounded-xl shadow-sm p-6">
      <h2 class="text-lg font-semibold mb-4">答题详情</h2>
      
      <el-tabs type="border-card">
        <el-tab-pane label="全部题目">
          <div class="questions-list space-y-6 mt-4">
            <div v-for="(question, index) in examData.questions" :key="index" class="question-item p-4 border rounded-lg"
                 :class="{'border-green-200 bg-green-50': question.isCorrect, 'border-red-200 bg-red-50': !question.isCorrect}">
              <div class="flex justify-between">
                <h4 class="text-base font-medium flex items-center">
                  <span class="question-number w-6 h-6 rounded-full flex items-center justify-center mr-2 text-sm"
                        :class="{'bg-green-100 text-green-700': question.isCorrect, 'bg-red-100 text-red-700': !question.isCorrect}">
                    {{ index + 1 }}
                  </span>
                  {{ question.type === 'choice' ? '选择题' : '填空题' }}
                  <span class="ml-2 text-sm text-gray-500">({{ question.score }}分)</span>
                </h4>
                <div class="flex items-center">
                  <span class="text-sm mr-2" :class="{'text-green-600': question.isCorrect, 'text-red-600': !question.isCorrect}">
                    {{ question.isCorrect ? '正确' : '错误' }}
                  </span>
                  <span class="badge px-2 py-0.5 text-xs rounded-full"
                        :class="{'bg-green-100 text-green-700': question.isCorrect, 'bg-red-100 text-red-700': !question.isCorrect}">
                    {{ question.isCorrect ? '得分：' + question.score : '得分：0' }}
                  </span>
                </div>
              </div>
              
              <div class="question-content mt-3">
                <p class="text-sm mb-3">{{ question.content }}</p>
                
                <!-- 选择题 -->
                <div v-if="question.type === 'choice'" class="options space-y-2">
                  <div v-for="(option, idx) in question.options" :key="idx" 
                       class="option p-2 rounded-md text-sm flex items-center"
                       :class="{
                         'bg-green-100': idx === question.answer,
                         'bg-red-100': idx === question.userAnswer && idx !== question.answer,
                         'bg-gray-50': idx !== question.answer && idx !== question.userAnswer
                       }">
                    <span class="option-marker w-5 h-5 rounded-full border flex items-center justify-center mr-2 text-xs"
                          :class="{
                            'border-green-500 bg-green-500 text-white': idx === question.answer,
                            'border-red-500 bg-red-500 text-white': idx === question.userAnswer && idx !== question.answer,
                            'border-gray-300': idx !== question.answer && idx !== question.userAnswer
                          }">
                      {{ ['A', 'B', 'C', 'D'][idx] }}
                    </span>
                    <span>{{ option }}</span>
                  </div>
                </div>
                
                <!-- 填空题 -->
                <div v-else-if="question.type === 'text'" class="text-answer">
                  <div class="mb-2">
                    <div class="text-xs text-gray-500 mb-1">你的答案：</div>
                    <div class="p-2 bg-gray-50 rounded-md text-sm" :class="{'text-red-600': !question.isCorrect}">
                      {{ question.userAnswer || '未作答' }}
                    </div>
                  </div>
                  <div>
                    <div class="text-xs text-gray-500 mb-1">正确答案：</div>
                    <div class="p-2 bg-green-50 rounded-md text-sm text-green-700">
                      {{ question.answer }}
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 解析 -->
              <div v-if="question.explanation" class="explanation mt-4 p-3 bg-blue-50 rounded-md">
                <div class="text-xs text-blue-500 mb-1">解析：</div>
                <div class="text-sm">{{ question.explanation }}</div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="错题">
          <div class="wrong-questions-list space-y-6 mt-4">
            <div v-for="(question, index) in wrongQuestions" :key="index" class="question-item p-4 border rounded-lg border-red-200 bg-red-50">
              <div class="flex justify-between">
                <h4 class="text-base font-medium flex items-center">
                  <span class="question-number w-6 h-6 rounded-full flex items-center justify-center mr-2 text-sm bg-red-100 text-red-700">
                    {{ question.index + 1 }}
                  </span>
                  {{ question.type === 'choice' ? '选择题' : '填空题' }}
                  <span class="ml-2 text-sm text-gray-500">({{ question.score }}分)</span>
                </h4>
                <div class="flex items-center">
                  <span class="text-sm mr-2 text-red-600">错误</span>
                  <span class="badge px-2 py-0.5 text-xs rounded-full bg-red-100 text-red-700">得分：0</span>
                </div>
              </div>
              
              <div class="question-content mt-3">
                <p class="text-sm mb-3">{{ question.content }}</p>
                
                <!-- 选择题 -->
                <div v-if="question.type === 'choice'" class="options space-y-2">
                  <div v-for="(option, idx) in question.options" :key="idx" 
                       class="option p-2 rounded-md text-sm flex items-center"
                       :class="{
                         'bg-green-100': idx === question.answer,
                         'bg-red-100': idx === question.userAnswer && idx !== question.answer,
                         'bg-gray-50': idx !== question.answer && idx !== question.userAnswer
                       }">
                    <span class="option-marker w-5 h-5 rounded-full border flex items-center justify-center mr-2 text-xs"
                          :class="{
                            'border-green-500 bg-green-500 text-white': idx === question.answer,
                            'border-red-500 bg-red-500 text-white': idx === question.userAnswer && idx !== question.answer,
                            'border-gray-300': idx !== question.answer && idx !== question.userAnswer
                          }">
                      {{ ['A', 'B', 'C', 'D'][idx] }}
                    </span>
                    <span>{{ option }}</span>
                  </div>
                </div>
                
                <!-- 填空题 -->
                <div v-else-if="question.type === 'text'" class="text-answer">
                  <div class="mb-2">
                    <div class="text-xs text-gray-500 mb-1">你的答案：</div>
                    <div class="p-2 bg-gray-50 rounded-md text-sm text-red-600">
                      {{ question.userAnswer || '未作答' }}
                    </div>
                  </div>
                  <div>
                    <div class="text-xs text-gray-500 mb-1">正确答案：</div>
                    <div class="p-2 bg-green-50 rounded-md text-sm text-green-700">
                      {{ question.answer }}
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 解析 -->
              <div v-if="question.explanation" class="explanation mt-4 p-3 bg-blue-50 rounded-md">
                <div class="text-xs text-blue-500 mb-1">解析：</div>
                <div class="text-sm">{{ question.explanation }}</div>
              </div>
            </div>
          </div>
          
          <div v-if="wrongQuestions.length === 0" class="text-center py-10 text-gray-500">
            恭喜你，本次考试没有错题！
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
    
    <!-- 底部操作按钮 -->
    <div class="action-buttons flex justify-center mt-6 space-x-4">
      <el-button type="primary" @click="reviewWrongQuestions" :disabled="wrongQuestions.length === 0">
        错题复习
      </el-button>
      <el-button type="success" @click="downloadReport">
        下载成绩单
      </el-button>
      <el-button @click="goBack">
        返回成绩列表
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const examId = route.params.id

// 模拟考试数据
const examData = ref({
  id: examId,
  title: '高等数学期中考试',
  subject: '高等数学',
  date: '2023-10-25',
  score: 85,
  totalScore: 100,
  rank: 15,
  totalStudents: 120,
  correctCount: 17,
  totalQuestions: 20,
  accuracy: 85,
  classAverage: 78,
  classHighest: 98,
  questions: [
    {
      type: 'choice',
      content: '若函数f(x)=x²在点x=1处的导数为k，则k等于：',
      options: ['1', '2', '3', '4'],
      answer: 1, // 索引，正确答案是B
      userAnswer: 1,
      isCorrect: true,
      score: 5,
      explanation: '函数f(x)=x²的导数为f\'(x)=2x，在x=1处，f\'(1)=2×1=2，所以k=2。'
    },
    {
      type: 'choice',
      content: '下列哪个级数收敛？',
      options: ['Σ(1/n)', 'Σ(1/n²)', 'Σ(1/√n)', 'Σ(n/(n+1))'],
      answer: 1, // 索引，正确答案是B
      userAnswer: 1,
      isCorrect: true,
      score: 5,
      explanation: 'Σ(1/n)是调和级数，发散；Σ(1/n²)是p级数，p=2>1，收敛；Σ(1/√n)是p级数，p=1/2<1，发散；Σ(n/(n+1))的极限不为0，发散。'
    },
    {
      type: 'choice',
      content: '函数f(x)=sin(x)的泰勒展开式的前三项是：',
      options: ['x', 'x-x³/3!', 'x-x³/3!+x⁵/5!', 'x-x²/2!+x³/3!'],
      answer: 1, // 索引，正确答案是B
      userAnswer: 2,
      isCorrect: false,
      score: 5,
      explanation: 'sin(x)的泰勒展开式为：x-x³/3!+x⁵/5!-x⁷/7!+...，所以前三项是x-x³/3!+x⁵/5!。'
    },
    {
      type: 'text',
      content: '计算极限：lim(x→0) (sin(x)/x)',
      answer: '1',
      userAnswer: '1',
      isCorrect: true,
      score: 10,
      explanation: '这是一个重要的基本极限，lim(x→0) (sin(x)/x) = 1。'
    },
    {
      type: 'choice',
      content: '矩阵A=[[1,2],[3,4]]的行列式等于：',
      options: ['-2', '-1', '1', '2'],
      answer: 0, // 索引，正确答案是A
      userAnswer: 3,
      isCorrect: false,
      score: 5,
      explanation: '矩阵A的行列式为|A|=1×4-2×3=4-6=-2。'
    }
  ]
})

// 计算属性：错题列表
const wrongQuestions = computed(() => {
  return examData.value.questions
    .map((q, idx) => ({ ...q, index: idx }))
    .filter(q => !q.isCorrect)
})

// 得分分布数据
const scoreDistribution = [
  { label: '0-60', count: 5, color: '#ef4444' },
  { label: '60-70', count: 15, color: '#f59e0b' },
  { label: '70-80', count: 35, color: '#3b82f6' },
  { label: '80-90', count: 40, color: '#10b981' },
  { label: '90-100', count: 25, color: '#8b5cf6' }
]

// 找出最大计数值，用于计算条形图高度
const maxCount = Math.max(...scoreDistribution.map(item => item.count))

// 知识点掌握情况
const knowledgePoints = [
  { name: '微分方程', mastery: 90 },
  { name: '多元函数微分', mastery: 85 },
  { name: '级数', mastery: 65 },
  { name: '空间解析几何', mastery: 95 },
  { name: '极限', mastery: 80 }
]

// 获取成绩颜色类名
const getScoreClass = (score) => {
  if (score >= 90) return 'text-green-600'
  if (score >= 80) return 'text-blue-600'
  if (score >= 60) return 'text-amber-600'
  return 'text-red-600'
}

// 获取知识点掌握程度颜色
const getKnowledgePointColor = (mastery) => {
  if (mastery >= 90) return '#10b981'
  if (mastery >= 80) return '#3b82f6'
  if (mastery >= 60) return '#f59e0b'
  return '#ef4444'
}

// 返回上一页
const goBack = () => {
  router.push('/student/scores')
}

// 错题复习
const reviewWrongQuestions = () => {
  ElMessage.success('即将进入错题复习模式')
  // 这里应该跳转到错题复习页面
  router.push(`/student/mistakes?examId=${examId}`)
}

// 下载成绩单
const downloadReport = () => {
  ElMessage.success('成绩单下载中...')
  // 这里应该实现下载功能
}

onMounted(() => {
  // 这里应该从后端获取考试详情数据
  // 模拟加载数据
  setTimeout(() => {
    // examData.value = ...
  }, 500)
})
</script>

<style scoped>
.score-detail-page {
  min-height: calc(100vh - 64px);
  background-color: #f8fafc;
}

.score-overview, .score-analysis, .answer-details {
  animation: fadeIn 0.6s ease-out;
}

.score-analysis {
  animation-delay: 0.2s;
}

.answer-details {
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

/* 自定义标签页样式 */
:deep(.el-tabs__item) {
  height: 40px;
  line-height: 40px;
}

:deep(.el-tabs__content) {
  padding: 20px;
}
</style> 