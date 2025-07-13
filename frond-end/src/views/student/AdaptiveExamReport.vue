<template>
  <div class="adaptive-exam-report">
    <!-- 顶部导航栏 -->
    <header class="report-header sticky top-0 left-0 right-0 z-30 bg-white border-b border-gray-200 shadow-sm">
      <div class="container mx-auto px-4 py-3 flex justify-between items-center">
        <div class="flex items-center">
          <el-button icon="el-icon-back" circle size="small" class="mr-3" @click="goBack"></el-button>
          <h1 class="text-lg font-bold mr-4">测试报告</h1>
          <el-tag size="small" type="info">{{ examInfo.subject || '未知科目' }}</el-tag>
        </div>
        <div class="flex items-center">
          <el-button size="small" @click="printReport">
            <Icon icon="tabler:printer" class="mr-1" />
            打印报告
          </el-button>
      </div>
      </div>
    </header>
    
    <div class="container mx-auto px-4 py-6">
      <!-- 加载中状态 -->
      <div v-if="loading" class="flex flex-col items-center justify-center py-20">
        <div class="loading-spinner mb-4">
          <svg class="animate-spin h-10 w-10 text-indigo-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
        </div>
        <p class="text-lg font-medium text-gray-700 mb-2">正在加载测试报告...</p>
        <p class="text-sm text-gray-500">请稍候，系统正在处理您的测试数据</p>
      </div>
      
      <!-- 报告内容 -->
      <div v-else class="report-content">
        <!-- 报告头部 -->
        <div class="report-header-section bg-white rounded-xl shadow-md p-6 mb-6">
          <div class="flex flex-col md:flex-row justify-between items-start md:items-center">
            <div>
              <h2 class="text-2xl font-bold mb-2">{{ examInfo.subject }} 能力评估报告</h2>
              <p class="text-gray-500">完成时间：{{ formatDate(examInfo.endTime) }}</p>
            </div>
            <div class="mt-4 md:mt-0">
              <div class="score-display bg-gradient-to-r from-indigo-500 to-purple-600 text-white rounded-lg p-4 text-center">
                <div class="text-sm mb-1">总分</div>
                <div class="text-3xl font-bold">{{ Math.round(reportData.score || 0) }}</div>
                <div class="text-xs mt-1">满分100</div>
              </div>
            </div>
          </div>
          
          <div class="summary-stats grid grid-cols-2 md:grid-cols-4 gap-4 mt-6">
            <div class="stat-item bg-gray-50 rounded-lg p-3 text-center">
              <div class="text-sm text-gray-500 mb-1">题目总数</div>
              <div class="text-xl font-medium">{{ reportData.totalQuestions || 0 }}</div>
            </div>
            <div class="stat-item bg-gray-50 rounded-lg p-3 text-center">
              <div class="text-sm text-gray-500 mb-1">答对题数</div>
              <div class="text-xl font-medium text-green-600">{{ reportData.correctCount || 0 }}</div>
            </div>
            <div class="stat-item bg-gray-50 rounded-lg p-3 text-center">
              <div class="text-sm text-gray-500 mb-1">能力水平</div>
              <div class="text-xl font-medium text-indigo-600">{{ reportData.abilityLevel || '未评定' }}</div>
            </div>
            <div class="stat-item bg-gray-50 rounded-lg p-3 text-center">
              <div class="text-sm text-gray-500 mb-1">用时</div>
              <div class="text-xl font-medium">{{ formatDuration(examInfo.startTime, examInfo.endTime) }}</div>
            </div>
      </div>
    </div>
    
        <!-- 能力水平评估 -->
        <div class="ability-section bg-white rounded-xl shadow-md p-6 mb-6">
          <h3 class="text-xl font-semibold mb-4 flex items-center">
            <Icon icon="tabler:brain" class="mr-2 text-indigo-500" />
            能力水平评估
          </h3>
          
          <div class="ability-meter mb-6">
            <div class="flex justify-between mb-1 text-sm">
              <span>入门</span>
              <span>基础</span>
              <span>中级</span>
              <span>进阶</span>
              <span>专家</span>
                </div>
            <div class="h-3 bg-gray-200 rounded-full overflow-hidden">
              <div class="h-full bg-gradient-to-r from-green-500 via-blue-500 to-purple-500"
                   :style="`width: ${reportData.abilityLevel ? reportData.abilityLevel : 0}%`"></div>
              </div>
            <div class="mt-2 text-center">
              <span class="px-3 py-1 rounded-full text-sm font-medium"
                    :class="getAbilityLevelClass(reportData.abilityLevel)">
                {{ getAbilityLevelText(reportData.abilityLevel) }}
              </span>
            </div>
          </div>
          
          <div class="ability-analysis">
            <p class="text-gray-700 mb-4" v-html="reportData.analysis"></p>
          </div>
        </div>
        
        <!-- 知识点掌握情况 -->
        <div class="topics-section bg-white rounded-xl shadow-md p-6 mb-6">
          <h3 class="text-xl font-semibold mb-4 flex items-center">
            <Icon icon="tabler:chart-bar" class="mr-2 text-indigo-500" />
            知识点掌握情况
          </h3>
          
          <div class="topic-scores mb-6">
            <div v-for="(score, topic) in reportData.topicScores" :key="topic" class="topic-score-item mb-3">
              <div class="flex justify-between mb-1">
                <span class="text-sm font-medium">{{ topic }}</span>
                <span class="text-sm" :class="getScoreTextClass(score)">{{ Math.round(score) }}%</span>
              </div>
              <div class="h-2 bg-gray-200 rounded-full overflow-hidden">
                <div class="h-full rounded-full" :style="`width: ${score}%; ${getScoreBarStyle(score)}`"></div>
        </div>
      </div>
    </div>
    
          <div class="strengths-weaknesses grid grid-cols-1 md:grid-cols-2 gap-6">
            <!-- 优势知识点 -->
            <div class="strengths">
              <h4 class="font-medium mb-3 flex items-center">
                <Icon icon="tabler:star" class="mr-1 text-amber-500" />
                优势知识点
              </h4>
              <ul class="list-disc pl-5 space-y-2">
                <li v-for="(strength, index) in reportData.strengths" :key="index" class="text-gray-700">
                  {{ strength }}
                </li>
              </ul>
            </div>
            
            <!-- 弱势知识点 -->
            <div class="weaknesses">
              <h4 class="font-medium mb-3 flex items-center">
                <Icon icon="tabler:alert-triangle" class="mr-1 text-red-500" />
                需要提升的知识点
              </h4>
              <ul class="list-disc pl-5 space-y-2">
                <li v-for="(weakness, index) in reportData.weaknesses" :key="index" class="text-gray-700">
                  {{ weakness }}
                </li>
              </ul>
            </div>
          </div>
        </div>
        
        <!-- 学习建议 -->
        <div class="recommendations-section bg-white rounded-xl shadow-md p-6 mb-6">
          <h3 class="text-xl font-semibold mb-4 flex items-center">
            <Icon icon="tabler:bulb" class="mr-2 text-indigo-500" />
            学习建议
          </h3>
          
          <div class="recommendations-content">
            <p class="text-gray-700 mb-4" v-html="reportData.recommendations"></p>
            
            <div class="next-steps mt-6 p-4 bg-indigo-50 rounded-lg">
              <h4 class="font-medium mb-2 text-indigo-700">后续学习计划建议</h4>
              <ul class="list-disc pl-5 space-y-2 text-gray-700">
                <li>针对弱势知识点进行专项练习</li>
                <li>定期进行自适应测试，追踪学习进度</li>
                <li>结合学习资源库中的相关内容进行学习</li>
                <li>参与相关讨论和学习小组，加深理解</li>
              </ul>
            </div>
          </div>
        </div>
        
        <!-- 操作按钮 -->
        <div class="action-buttons flex justify-between">
          <el-button @click="goBack" plain>
            <Icon icon="tabler:arrow-left" class="mr-1" />
            返回
          </el-button>
          
          <div class="flex gap-3">
            <el-button @click="startNewTest" type="primary">
              <Icon icon="tabler:player-play" class="mr-1" />
              开始新测试
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Icon } from '@iconify/vue'
import { get, post } from '@/net'

const route = useRoute()
const router = useRouter()
const examId = route.params.id

// 状态变量
const loading = ref(true)
const examInfo = ref({})
const reportData = ref({
  score: 0,
  totalQuestions: 0,
  correctCount: 0,
  abilityLevel: 0,
  topicScores: {},
  strengths: [],
  weaknesses: [],
  analysis: '',
  recommendations: ''
})

// 加载报告数据
const loadReportData = async () => {
  loading.value = true
  
  try {
    // 获取测试基本信息和结果报告并行请求
    const [examInfoPromise, examResultPromise] = [
      new Promise((resolve, reject) => {
        get(`/api/adaptive-exams/${examId}`, null,
          (message, data) => resolve(data),
          (message) => reject(new Error(message || '获取测试信息失败')),
          () => reject(new Error('获取测试信息出错'))
        )
      }),
      new Promise((resolve, reject) => {
        get(`/api/adaptive-exams/${examId}/result`, null,
          (message, data) => resolve(data),
          (message) => reject(new Error(message || '获取测试报告失败')),
          () => reject(new Error('获取测试报告出错'))
        )
      })
    ]
    
    // 并行获取数据
    const [examInfoData, examResultData] = await Promise.allSettled([examInfoPromise, examResultPromise])
    
    // 处理测试基本信息
    if (examInfoData.status === 'fulfilled') {
      examInfo.value = examInfoData.value
      
      // 如果测试尚未完成，先完成测试
      if (examInfo.value.status !== 'COMPLETED') {
        console.log('测试尚未完成，尝试完成测试')
        await completeExamAndLoadResult()
        return
      }
    } else {
      console.error('获取测试信息失败:', examInfoData.reason)
      ElMessage.warning(examInfoData.reason?.message || '获取测试信息失败')
    }
    
    // 处理测试结果报告
    if (examResultData.status === 'fulfilled') {
      reportData.value = examResultData.value
    } else {
      console.error('获取测试报告失败:', examResultData.reason)
      ElMessage.warning(examResultData.reason?.message || '获取测试报告失败')
      // 如果报告不存在，显示默认报告
      showDefaultReport()
    }
  } catch (error) {
    console.error('加载报告数据出错:', error)
    ElMessage.error('加载报告数据出错')
    showDefaultReport()
  } finally {
    loading.value = false
  }
}

// 完成测试并加载结果
const completeExamAndLoadResult = async () => {
  try {
    const result = await new Promise((resolve, reject) => {
      post(`/api/adaptive-exams/${examId}/complete`, {},
        (message, data) => resolve(data),
        (message) => reject(new Error(message || '测试完成失败')),
        () => reject(new Error('测试完成出错'))
      )
    })
    
    console.log('测试完成成功:', result)
    // 更新测试信息
    if (result) {
      Object.assign(examInfo.value, result)
    }
    
    // 加载测试结果
    await loadExamResult()
  } catch (error) {
    console.error('测试完成失败:', error)
    ElMessage.warning(error?.message || '测试完成失败，尝试直接获取结果')
    // 尝试直接获取结果
    await loadExamResult()
  }
}

// 加载测试结果
const loadExamResult = async () => {
  try {
    const result = await new Promise((resolve, reject) => {
      get(`/api/adaptive-exams/${examId}/result`, null,
        (message, data) => resolve(data),
        (message) => reject(new Error(message || '获取测试报告失败')),
        () => reject(new Error('获取测试报告出错'))
      )
    })
    
    reportData.value = result
  } catch (error) {
    console.error('获取测试报告失败:', error)
    ElMessage.warning(error?.message || '获取测试报告失败')
    // 如果报告不存在，显示默认报告
    showDefaultReport()
  } finally {
    loading.value = false
  }
}

// 显示默认报告
const showDefaultReport = () => {
  // 使用测试基本信息生成简单报告
  if (examInfo.value) {
    reportData.value = {
      score: examInfo.value.score || 0,
      totalQuestions: examInfo.value.questionCount || 0,
      correctCount: 0,
      abilityLevel: '未评定',
      topicScores: {},
      strengths: ['数据不可用'],
      weaknesses: ['数据不可用'],
      analysis: '很抱歉，无法获取详细的测试报告数据。',
      recommendations: '建议您重新参加测试，或联系管理员解决问题。'
    }
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '未知时间'
  
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 计算测试用时
const formatDuration = (startTime, endTime) => {
  if (!startTime || !endTime) return '未知'
  
  const start = new Date(startTime)
  const end = new Date(endTime)
  const durationMs = end - start
  
  const minutes = Math.floor(durationMs / 60000)
  const seconds = Math.floor((durationMs % 60000) / 1000)
  
  return `${minutes}分${seconds}秒`
}

// 获取能力水平文本
const getAbilityLevelText = (level) => {
  if (!level) return '未评定'
  
  if (level >= 90) return '专家级'
  if (level >= 80) return '进阶级'
  if (level >= 70) return '中级'
  if (level >= 60) return '基础级'
  return '入门级'
}

// 获取能力水平样式类
const getAbilityLevelClass = (level) => {
  if (!level) return 'bg-gray-100 text-gray-700'
  
  if (level >= 90) return 'bg-purple-100 text-purple-700'
  if (level >= 80) return 'bg-blue-100 text-blue-700'
  if (level >= 70) return 'bg-green-100 text-green-700'
  if (level >= 60) return 'bg-amber-100 text-amber-700'
  return 'bg-red-100 text-red-700'
}

// 获取分数文本样式类
const getScoreTextClass = (score) => {
  if (score >= 90) return 'text-green-600 font-medium'
  if (score >= 70) return 'text-blue-600 font-medium'
  if (score >= 60) return 'text-amber-600 font-medium'
  return 'text-red-600 font-medium'
}

// 获取分数条样式
const getScoreBarStyle = (score) => {
  if (score >= 90) return 'background: linear-gradient(to right, #10b981, #34d399)'
  if (score >= 70) return 'background: linear-gradient(to right, #3b82f6, #60a5fa)'
  if (score >= 60) return 'background: linear-gradient(to right, #f59e0b, #fbbf24)'
  return 'background: linear-gradient(to right, #ef4444, #f87171)'
}

// 返回上一页
const goBack = () => {
  router.push('/student/adaptive-exam')
}

// 打印报告
const printReport = () => {
  window.print()
}

// 开始新测试
const startNewTest = () => {
  router.push('/student/adaptive-exam/start')
}

// 组件挂载时加载报告数据
onMounted(() => {
  loadReportData()
})
</script>

<style scoped>
.adaptive-exam-report {
  min-height: 100vh;
  background-color: #f8fafc;
  contain: content; /* 提高渲染性能 */
}

.report-header {
  backdrop-filter: blur(8px);
  background-color: rgba(255, 255, 255, 0.9);
  will-change: transform; /* 优化固定元素的性能 */
  transform: translateZ(0); /* 启用GPU加速 */
}

.report-content {
  animation: fadeIn 0.6s ease-out;
  contain: layout style; /* 减少布局变化的影响 */
}

.report-content > div {
  contain: content; /* 隔离内容，提高渲染性能 */
}

.summary-stats, .ability-section, .topics-section, .recommendations-section {
  contain: content; /* 隔离内容，提高渲染性能 */
  height: auto; /* 防止高度抖动 */
  min-height: 100px; /* 设置最小高度，减少布局变化 */
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

/* 打印样式 */
@media print {
  .report-header, .action-buttons {
    display: none;
  }
  
  .container {
    width: 100%;
    max-width: 100%;
    padding: 0;
  }
  
  .report-content > div {
    break-inside: avoid;
    margin-bottom: 20px;
  }
}
</style> 