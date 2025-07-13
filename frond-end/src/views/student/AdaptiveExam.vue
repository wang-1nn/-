<template>
  <section class="adaptive-exam-wrapper">
    <!-- 顶部介绍区域 -->
    <div class="banner relative overflow-hidden">
      <div class="banner-content relative z-10 p-8 md:p-12">
        <h1 class="text-3xl md:text-4xl font-bold mb-3 text-white">自适应智能测验</h1>
        <p class="text-sm md:text-base text-gray-200 mb-6 max-w-2xl">
          系统根据你的实时表现动态调整题目难度，为你提供精准评估。每次测验结束后，你将获得详细的能力分析报告。
        </p>
        <div class="flex gap-3">
          <el-button type="primary" size="large" @click="startCat" class="px-8">
            <Icon icon="tabler:brain" class="mr-2" />
            开始测试
          </el-button>
          <el-button plain size="large" @click="openHistory" class="history-btn border-indigo-300 text-indigo-600 hover:bg-indigo-50">
            <Icon icon="tabler:history" class="mr-2" />
            查看历史记录
          </el-button>
        </div>
      </div>
      
      <!-- 背景装饰 -->
      <div class="absolute top-0 right-0 w-1/2 h-full opacity-20">
        <div class="absolute top-10 right-10 w-32 h-32 rounded-full bg-white/10"></div>
        <div class="absolute top-40 right-40 w-48 h-48 rounded-full bg-white/10"></div>
        <div class="absolute top-20 right-60 w-16 h-16 rounded-full bg-white/10"></div>
      </div>
    </div>

    <!-- 特性说明 -->
    <div class="features grid md:grid-cols-3 gap-6 mt-12">
      <div v-for="s in steps" :key="s.title" 
           class="step-card relative overflow-hidden group">
        <div class="p-6 relative z-10">
          <div class="icon-wrapper mb-4 flex justify-center">
            <Icon :icon="s.icon" :style="{color: s.iconColor}" class="text-4xl" />
          </div>
          <h3 class="font-medium text-lg mb-2">{{ s.title }}</h3>
          <p class="text-gray-500 text-sm leading-relaxed">{{ s.desc }}</p>
        </div>
        <div class="absolute inset-0 bg-gradient-to-br opacity-0 group-hover:opacity-5 transition-opacity duration-300"
             :class="s.gradient"></div>
      </div>
    </div>
    
    <!-- 测试流程说明 -->
    <div class="test-process bg-white rounded-xl shadow-md p-6 mt-8">
      <h2 class="text-xl font-semibold mb-6 flex items-center">
        <Icon icon="tabler:route" class="mr-2 text-indigo-500" />
        测试流程
      </h2>
      
      <div class="process-steps flex flex-col md:flex-row gap-4">
        <div v-for="(step, index) in testProcess" :key="index" 
             class="process-step flex-1 p-4 rounded-lg border border-gray-100 bg-gray-50 relative">
          <div class="step-number absolute -top-3 -left-3 w-6 h-6 rounded-full bg-indigo-500 text-white flex items-center justify-center text-xs font-bold">
            {{ index + 1 }}
          </div>
          <div class="flex items-center mb-2">
            <Icon :icon="step.icon" class="mr-2 text-indigo-500" />
            <h4 class="font-medium">{{ step.title }}</h4>
          </div>
          <p class="text-sm text-gray-500">{{ step.desc }}</p>
        </div>
      </div>
    </div>
    
    <!-- 难度级别说明 -->
    <div class="difficulty-levels bg-white rounded-xl shadow-md p-6 mt-8">
      <h2 class="text-xl font-semibold mb-6 flex items-center">
        <Icon icon="tabler:stairs-up" class="mr-2 text-indigo-500" />
        难度级别说明
      </h2>
      
      <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-5 gap-4">
        <div v-for="(level, index) in difficultyLevels" :key="index"
             class="difficulty-card p-4 rounded-lg border transition-all hover:shadow-md"
             :class="level.borderClass">
          <div class="flex items-center mb-2">
            <div class="w-8 h-8 rounded-full flex items-center justify-center mr-2"
                 :class="level.bgClass">
              <Icon :icon="level.icon" :class="level.iconClass" />
            </div>
            <h4 class="font-medium" :class="level.textClass">{{ level.name }}</h4>
          </div>
          <div class="text-xs text-gray-500">{{ level.desc }}</div>
        </div>
      </div>
    </div>
    
    <!-- 历史记录对话框 -->
    <el-dialog v-model="showHistory" title="测试历史记录" width="80%" destroy-on-close>
      <div class="history-records">
        <el-table :data="historyRecords" style="width: 100%" v-loading="loading">
          <el-table-column prop="date" label="测试日期" width="180">
            <template #default="scope">
              <span class="font-medium text-gray-800">{{ scope.row.date }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="duration" label="用时" width="100">
            <template #default="scope">
              <span class="font-medium text-indigo-600">{{ scope.row.duration }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="questions" label="题目数" width="100">
            <template #default="scope">
              <span class="font-medium text-gray-700">{{ scope.row.questions }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="score" label="得分">
            <template #default="scope">
              <span class="font-semibold" :class="{
                'text-green-600': scope.row.score >= 90,
                'text-blue-600': scope.row.score >= 80 && scope.row.score < 90,
                'text-amber-600': scope.row.score >= 60 && scope.row.score < 80,
                'text-red-600': scope.row.score < 60
              }">{{ scope.row.score }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="level" label="评估等级" width="120">
            <template #default="scope">
              <el-tag :type="scope.row.levelType" effect="light">{{ scope.row.level }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <div class="flex gap-2">
                <el-button type="primary" size="small" @click="viewReport(scope.row.id)">
                  <Icon icon="tabler:file-analytics" class="mr-1" />
                  查看报告
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        
        <div v-if="historyRecords.length === 0" class="text-center py-8 text-gray-500">
          暂无测试记录，开始你的第一次测试吧！
        </div>
      </div>
    </el-dialog>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Icon } from '@iconify/vue'
import { get } from '@/net'

const router = useRouter()
const showHistory = ref(false)
const loading = ref(false)

// 开始自适应测试
const startCat = () => {
  ElMessage.success('正在准备自适应测试环境...')
  setTimeout(() => {
    router.push('/student/adaptive-exam/start')
  }, 1000)
}

// 查看测试报告
const viewReport = (id) => {
  // 显示加载提示
  ElMessage({
    message: '正在准备报告数据...',
    type: 'info',
    duration: 1000
  })
  
  // 预加载报告数据
  preloadReport(id)
  
  // 导航到报告页面
  setTimeout(() => {
    router.push(`/student/adaptive-exam/report/${id}`)
  }, 200)
}

// 预加载报告数据
const preloadReport = (id) => {
  // 并行预加载测试信息和报告数据
  Promise.all([
    new Promise((resolve) => {
      get(`/api/adaptive-exams/${id}`, null, 
        (message, data) => resolve(data),
        () => resolve(null),
        () => resolve(null)
      )
    }),
    new Promise((resolve) => {
      get(`/api/adaptive-exams/${id}/result`, null,
        (message, data) => resolve(data),
        () => resolve(null),
        () => resolve(null)
      )
    })
  ]).then(([examInfo, reportData]) => {
    console.log('预加载报告数据完成')
    // 数据已预加载到浏览器缓存中
  }).catch(() => {
    // 忽略错误，不影响用户体验
  })
}

// 特性介绍
const steps = [
  { 
    title: '智能选题', 
    desc: '系统根据你的答题表现，实时调整题目难度，精确定位你的能力水平，不浪费时间在过简单或过难的题目上。', 
    icon: 'tabler:target',
    iconColor: '#f97316',
    gradient: 'from-red-500 to-orange-500'
  },
  { 
    title: '即刻反馈', 
    desc: '每道题完成后立即给出评分与详细解析，帮助你理解错误原因，及时纠正知识盲点，加深对知识点的理解。', 
    icon: 'tabler:bolt',
    iconColor: '#3b82f6',
    gradient: 'from-blue-500 to-indigo-500'
  },
  { 
    title: '精准评估', 
    desc: '测试结束后生成全面的能力分析报告，包括知识点掌握程度、薄弱环节识别、学习建议和进步轨迹。', 
    icon: 'tabler:chart-pie',
    iconColor: '#10b981',
    gradient: 'from-green-500 to-emerald-500'
  },
]

// 测试流程
const testProcess = [
  {
    title: '选择科目',
    desc: '选择你想要测试的学科和知识范围',
    icon: 'tabler:book'
  },
  {
    title: '答题过程',
    desc: '系统会根据你的回答自动调整题目难度',
    icon: 'tabler:writing'
  },
  {
    title: '完成测试',
    desc: '完成所有题目后提交测试',
    icon: 'tabler:check'
  },
  {
    title: '查看报告',
    desc: '获取详细的能力评估报告和学习建议',
    icon: 'tabler:report'
  }
]

// 难度级别
const difficultyLevels = [
  {
    name: '入门级',
    desc: '基础概念和简单应用',
    icon: 'tabler:circle-1',
    bgClass: 'bg-green-100',
    iconClass: 'text-green-600',
    textClass: 'text-green-700',
    borderClass: 'border-green-200'
  },
  {
    name: '基础级',
    desc: '基本原理和标准应用',
    icon: 'tabler:circle-2',
    bgClass: 'bg-cyan-100',
    iconClass: 'text-cyan-600',
    textClass: 'text-cyan-700',
    borderClass: 'border-cyan-200'
  },
  {
    name: '中级',
    desc: '综合应用和分析能力',
    icon: 'tabler:circle-3',
    bgClass: 'bg-blue-100',
    iconClass: 'text-blue-600',
    textClass: 'text-blue-700',
    borderClass: 'border-blue-200'
  },
  {
    name: '进阶级',
    desc: '复杂问题和深度思考',
    icon: 'tabler:circle-4',
    bgClass: 'bg-purple-100',
    iconClass: 'text-purple-600',
    textClass: 'text-purple-700',
    borderClass: 'border-purple-200'
  },
  {
    name: '专家级',
    desc: '创新思维和挑战性问题',
    icon: 'tabler:circle-5',
    bgClass: 'bg-pink-100',
    iconClass: 'text-pink-600',
    textClass: 'text-pink-700',
    borderClass: 'border-pink-200'
  }
]

// 历史记录
const historyRecords = ref([])
const historyLoaded = ref(false) // 跟踪历史记录是否已加载

// 加载历史记录
const loadHistory = () => {
  if (historyLoaded.value && historyRecords.value.length > 0) {
    // 如果已经加载过历史记录且有数据，则不重复加载
    return
  }
  
  loading.value = true
  get('/api/adaptive-exams/history', null,
    (message, data) => {
      console.log('获取历史记录成功:', data)
      // 处理历史数据
      historyRecords.value = data.map(item => {
        // 计算测试用时
        const startTime = new Date(item.startTime)
        const endTime = new Date(item.endTime || startTime)
        const durationMs = endTime - startTime
        const minutes = Math.floor(durationMs / 60000)
        const seconds = Math.floor((durationMs % 60000) / 1000)
        
        // 格式化日期
        const date = new Date(item.startTime)
        const formattedDate = date.toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        })
        
        // 确定等级
        let level = '未评定'
        let levelType = 'info'
        
        if (item.score >= 90) {
          level = '优秀'
          levelType = 'success'
        } else if (item.score >= 80) {
          level = '良好'
          levelType = 'primary'
        } else if (item.score >= 70) {
          level = '中等'
          levelType = 'warning'
        } else if (item.score >= 60) {
          level = '及格'
          levelType = 'warning'
        } else if (item.score > 0) {
          level = '不及格'
          levelType = 'danger'
        }
        
        return {
          id: item.id,
          date: formattedDate,
          duration: `${minutes}分${seconds}秒`,
          questions: item.questionCount || 0,
          score: Math.round(item.score || 0),
          level,
          levelType,
          subject: item.subject || '未知科目',
          status: item.status
        }
      })
      historyLoaded.value = true
      loading.value = false
    },
    (message) => {
      console.error('获取历史记录失败:', message)
      ElMessage.warning(message || '获取历史记录失败')
      loading.value = false
    },
    () => {
      console.error('获取历史记录出错')
      ElMessage.error('获取历史记录出错')
      loading.value = false
    }
  )
}

// 打开历史记录对话框
const openHistory = () => {
  showHistory.value = true
  loadHistory() // 每次打开对话框时刷新历史记录
}

onMounted(() => {
  // 初始加载历史记录
  loadHistory()
})
</script>

<style scoped>
.adaptive-exam-wrapper { 
  @apply p-6; 
}

.banner { 
  @apply bg-gradient-to-br from-indigo-500 to-purple-600 rounded-2xl shadow-xl; 
  min-height: 240px; 
}

.history-btn {
  background-color: rgba(255, 255, 255, 0.2);
  color: white !important;
  border-color: rgba(255, 255, 255, 0.3) !important;
}

.history-btn:hover {
  background-color: rgba(255, 255, 255, 0.3) !important;
  border-color: rgba(255, 255, 255, 0.4) !important;
}

.step-card { 
  @apply bg-white rounded-xl p-6 text-center shadow-md transition transform duration-300; 
}

.step-card:hover { 
  @apply shadow-lg -translate-y-2; 
}

.icon-wrapper {
  transition: transform 0.3s ease;
}

.step-card:hover .icon-wrapper {
  transform: scale(1.2);
}

/* 历史记录样式 */
.history-records {
  @apply rounded-lg overflow-hidden;
}

.history-records :deep(.el-table) {
  @apply rounded-lg overflow-hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.history-records :deep(.el-table__header) {
  @apply bg-gray-50;
}

.history-records :deep(.el-table__header-wrapper th) {
  @apply bg-gray-50 text-gray-700 font-medium;
}

.history-records :deep(.el-table__row) {
  @apply transition-colors duration-200;
}

.history-records :deep(.el-table__row:hover) {
  @apply bg-blue-50;
}

/* 历史记录对话框样式 */
:deep(.history-dialog) {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.history-dialog .el-dialog__header) {
  background-color: #f9fafb;
  padding: 16px 20px;
  margin: 0;
  border-bottom: 1px solid #e5e7eb;
}

:deep(.history-dialog .el-dialog__body) {
  padding: 20px;
}

:deep(.history-dialog .el-dialog__headerbtn) {
  top: 16px;
  right: 16px;
}

:deep(.history-dialog .el-dialog__headerbtn .el-dialog__close) {
  color: #6b7280;
}

.dialog-header {
  width: 100%;
  padding-right: 40px;
}

/* 动画效果 */
.banner, .step-card, .test-process, .difficulty-levels {
  animation: fadeInUp 0.6s ease-out;
  animation-fill-mode: both;
}

.features {
  animation-delay: 0.2s;
}

.test-process {
  animation-delay: 0.6s;
}

.difficulty-levels {
  animation-delay: 0.8s;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>