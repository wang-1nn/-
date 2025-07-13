<template>
  <div class="learning-timeline-page p-6">
    <div class="page-header mb-6">
      <h1 class="text-2xl font-bold mb-2">我的学习轨迹</h1>
      <p class="text-gray-500">查看您的学习进度、里程碑和未来计划</p>
    </div>

    <!-- 进度概览卡片 -->
    <div class="progress-overview grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
      <div class="stat-card bg-white rounded-xl shadow-sm p-4 flex items-center">
        <div class="stat-icon bg-blue-100 p-3 rounded-lg">
          <i class="el-icon-time text-blue-600 text-xl"></i>
        </div>
        <div class="ml-4">
          <div class="text-sm text-gray-500">总学习时长</div>
          <div class="text-xl font-semibold">{{ formatTime(totalStudyTime) }}</div>
        </div>
      </div>
      
      <div class="stat-card bg-white rounded-xl shadow-sm p-4 flex items-center">
        <div class="stat-icon bg-green-100 p-3 rounded-lg">
          <i class="el-icon-finished text-green-600 text-xl"></i>
        </div>
        <div class="ml-4">
          <div class="text-sm text-gray-500">完成任务</div>
          <div class="text-xl font-semibold">{{ completedTasks }}/{{ totalTasks }}</div>
        </div>
      </div>
      
      <div class="stat-card bg-white rounded-xl shadow-sm p-4 flex items-center">
        <div class="stat-icon bg-purple-100 p-3 rounded-lg">
          <i class="el-icon-medal text-purple-600 text-xl"></i>
        </div>
        <div class="ml-4">
          <div class="text-sm text-gray-500">获得成就</div>
          <div class="text-xl font-semibold">{{ achievements.length }}</div>
        </div>
      </div>
      
      <div class="stat-card bg-white rounded-xl shadow-sm p-4 flex items-center">
        <div class="stat-icon bg-amber-100 p-3 rounded-lg">
          <i class="el-icon-star-on text-amber-600 text-xl"></i>
        </div>
        <div class="ml-4">
          <div class="text-sm text-gray-500">当前等级</div>
          <div class="text-xl font-semibold">Lv.{{ userLevel }}</div>
        </div>
      </div>
    </div>

    <div class="flex flex-col lg:flex-row gap-6">
      <!-- 左侧：学习路径 -->
      <div class="learning-path-container flex-1">
        <div class="bg-white rounded-xl shadow-sm p-6 mb-6">
          <div class="flex justify-between items-center mb-4">
            <h2 class="text-lg font-semibold">我的学习路径</h2>
            <el-select v-model="selectedSubject" size="small" placeholder="选择科目" class="w-32">
              <el-option v-for="subject in subjects" :key="subject.value" :label="subject.label" :value="subject.value" />
            </el-select>
          </div>
          
          <div class="learning-path-timeline relative">
            <!-- 时间轴线 -->
            <div class="timeline-line absolute left-4 top-0 bottom-0 w-0.5 bg-gray-200"></div>
            
            <!-- 时间轴节点 -->
            <div v-for="(milestone, index) in filteredMilestones" :key="milestone.id" 
                 class="milestone-item relative pl-12 pb-8"
                 :class="{'completed': milestone.completed}">
              <!-- 节点标记 -->
              <div class="milestone-marker absolute left-0 top-1 w-8 h-8 rounded-full border-4 flex items-center justify-center z-10"
                   :class="milestone.completed ? 'border-green-500 bg-green-100' : 'border-gray-300 bg-white'">
                <i v-if="milestone.completed" class="el-icon-check text-green-500"></i>
                <span v-else class="text-xs font-medium text-gray-500">{{ index + 1 }}</span>
              </div>
              
              <!-- 节点内容 -->
              <div class="milestone-content bg-white rounded-lg border p-4"
                   :class="milestone.completed ? 'border-green-200' : 'border-gray-200'">
                <div class="flex justify-between items-start">
                  <h3 class="text-base font-medium mb-2">{{ milestone.title }}</h3>
                  <span class="text-xs px-2 py-0.5 rounded-full"
                        :class="milestone.completed ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-700'">
                    {{ milestone.completed ? '已完成' : '进行中' }}
                  </span>
                </div>
                
                <p class="text-sm text-gray-600 mb-3">{{ milestone.description }}</p>
                
                <div class="flex justify-between items-center">
                  <div class="text-xs text-gray-500">
                    <i class="el-icon-date mr-1"></i>
                    {{ milestone.date }}
                  </div>
                  
                  <div v-if="milestone.completed" class="flex items-center text-xs text-green-600">
                    <i class="el-icon-circle-check mr-1"></i>
                    {{ milestone.completionDate }}
                  </div>
                  <el-button v-else size="mini" type="primary" plain>继续学习</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 学习建议 -->
        <div class="bg-white rounded-xl shadow-sm p-6">
          <h2 class="text-lg font-semibold mb-4">学习建议</h2>
          
          <div class="suggestions space-y-4">
            <div v-for="suggestion in learningRecommendations" :key="suggestion.id" 
                 class="suggestion-item p-4 border rounded-lg hover:border-blue-300 transition-colors">
              <div class="flex items-start">
                <div class="suggestion-icon p-2 rounded-lg mr-3" :class="`bg-${suggestion.color}-100`">
                  <i :class="`el-icon-${suggestion.icon} text-${suggestion.color}-600 text-lg`"></i>
                </div>
                <div class="flex-1">
                  <h3 class="text-base font-medium mb-1">{{ suggestion.title }}</h3>
                  <p class="text-sm text-gray-600 mb-3">{{ suggestion.description }}</p>
                  <div class="flex justify-between items-center">
                    <div class="text-xs text-gray-500">
                      <i class="el-icon-timer mr-1"></i>
                      预计用时: {{ suggestion.estimatedTime }}
                    </div>
                    <el-button size="mini" type="primary">开始学习</el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧：成就和统计 -->
      <div class="side-panel w-full lg:w-80">
        <!-- 学习统计 -->
        <div class="bg-white rounded-xl shadow-sm p-6 mb-6">
          <h2 class="text-lg font-semibold mb-4">学习统计</h2>
          
          <div class="weekly-stats mb-4">
            <div class="flex justify-between items-center mb-2">
              <div class="text-sm font-medium">本周学习时长</div>
              <div class="text-sm text-gray-500">{{ formatTime(weeklyStudyTime) }}</div>
            </div>
            
            <div class="weekly-chart h-20 bg-gray-50 rounded-lg p-2 flex items-end justify-between">
              <div v-for="(day, index) in weeklyData" :key="index" 
                   class="day-bar w-8 bg-blue-400 rounded-t-sm transition-all duration-300"
                   :style="`height: ${day.percentage}%`"
                   :class="{'bg-blue-600': day.isToday}">
                <div class="text-xs text-center text-gray-500 mt-2">{{ day.label }}</div>
              </div>
            </div>
          </div>
          
          <div class="subject-distribution">
            <div class="text-sm font-medium mb-2">学科分布</div>
            
            <div class="subject-bars space-y-3">
              <div v-for="subject in subjectDistribution" :key="subject.name" class="subject-bar">
                <div class="flex justify-between items-center mb-1">
                  <div class="text-xs">{{ subject.name }}</div>
                  <div class="text-xs text-gray-500">{{ subject.percentage }}%</div>
                </div>
                <div class="h-2 bg-gray-100 rounded-full overflow-hidden">
                  <div class="h-full rounded-full" 
                       :style="`width: ${subject.percentage}%; background-color: ${subject.color}`"></div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 最近成就 -->
        <div class="bg-white rounded-xl shadow-sm p-6">
          <div class="flex justify-between items-center mb-4">
            <h2 class="text-lg font-semibold">最近成就</h2>
            <el-button type="text" size="small" @click="$router.push('/student/achievements')">
              查看全部
              <i class="el-icon-arrow-right ml-1"></i>
            </el-button>
          </div>
          
          <div class="achievements space-y-3">
            <div v-for="achievement in recentAchievements" :key="achievement.id" 
                 class="achievement-item flex items-center p-3 border rounded-lg">
              <div class="achievement-icon w-10 h-10 rounded-full flex items-center justify-center mr-3"
                   :class="`bg-${achievement.color}-100`">
                <i :class="`el-icon-${achievement.icon} text-${achievement.color}-600`"></i>
              </div>
              <div class="flex-1">
                <div class="text-sm font-medium">{{ achievement.title }}</div>
                <div class="text-xs text-gray-500">{{ achievement.date }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

// 用户数据
const totalStudyTime = ref(125 * 60 * 60) // 125小时（秒）
const completedTasks = ref(24)
const totalTasks = ref(36)
const userLevel = ref(8)

// 科目选择
const subjects = [
  { label: '全部科目', value: 'all' },
  { label: '高等数学', value: 'math' },
  { label: '线性代数', value: 'linear' },
  { label: '概率统计', value: 'probability' },
  { label: '大学物理', value: 'physics' }
]
const selectedSubject = ref('all')

// 学习里程碑
const milestones = ref([
  {
    id: 1,
    title: '微积分基础',
    description: '掌握微积分的基本概念和计算方法，包括导数、积分和微分方程的基本应用',
    date: '2023-09-01',
    completionDate: '2023-09-20',
    completed: true,
    subject: 'math'
  },
  {
    id: 2,
    title: '多元函数微积分',
    description: '学习多元函数的偏导数、全微分、多重积分等概念和计算方法',
    date: '2023-09-25',
    completionDate: '2023-10-15',
    completed: true,
    subject: 'math'
  },
  {
    id: 3,
    title: '级数与级数展开',
    description: '掌握数项级数、幂级数的收敛性判断和函数展开为幂级数的方法',
    date: '2023-10-20',
    completionDate: null,
    completed: false,
    subject: 'math'
  },
  {
    id: 4,
    title: '矩阵与线性变换',
    description: '理解矩阵的概念、运算和线性变换的关系，掌握特征值和特征向量的计算',
    date: '2023-11-01',
    completionDate: null,
    completed: false,
    subject: 'linear'
  },
  {
    id: 5,
    title: '概率论基础',
    description: '学习随机事件、概率分布、数字特征等概率论基础知识',
    date: '2023-11-15',
    completionDate: null,
    completed: false,
    subject: 'probability'
  }
])

// 根据选择的科目过滤里程碑
const filteredMilestones = computed(() => {
  if (selectedSubject.value === 'all') {
    return milestones.value
  }
  return milestones.value.filter(m => m.subject === selectedSubject.value)
})

// 学习建议
const learningRecommendations = ref([
  {
    id: 1,
    title: '复习微分方程',
    description: '根据你最近的测验结果，建议复习一阶微分方程的求解方法，特别是变量分离法',
    estimatedTime: '2小时',
    icon: 'notebook-1',
    color: 'blue'
  },
  {
    id: 2,
    title: '完成线性代数作业',
    description: '你有一个线性代数作业即将截止，建议尽快完成',
    estimatedTime: '1.5小时',
    icon: 'document',
    color: 'amber'
  },
  {
    id: 3,
    title: '准备概率论小测',
    description: '下周二有概率论小测，建议提前准备，重点复习离散型随机变量',
    estimatedTime: '3小时',
    icon: 'reading',
    color: 'green'
  }
])

// 成就
const achievements = ref([
  {
    id: 1,
    title: '学习达人',
    description: '连续30天完成学习任务',
    date: '2023-10-15',
    icon: 'medal',
    color: 'purple'
  },
  {
    id: 2,
    title: '微积分精通',
    description: '微积分测验获得满分',
    date: '2023-10-10',
    icon: 'trophy',
    color: 'amber'
  },
  {
    id: 3,
    title: '勤学好问',
    description: '在讨论区发布10个有价值的问题',
    date: '2023-10-05',
    icon: 'question',
    color: 'blue'
  },
  {
    id: 4,
    title: '知识分享者',
    description: '帮助5位同学解答问题',
    date: '2023-09-28',
    icon: 'share',
    color: 'green'
  }
])

// 最近成就（取前3个）
const recentAchievements = computed(() => {
  return achievements.value.slice(0, 3)
})

// 每周学习时间（秒）
const weeklyStudyTime = ref(18 * 60 * 60) // 18小时

// 每周学习数据
const weeklyData = ref([
  { label: '一', hours: 2.5, percentage: 50, isToday: false },
  { label: '二', hours: 3, percentage: 60, isToday: false },
  { label: '三', hours: 1.5, percentage: 30, isToday: false },
  { label: '四', hours: 4, percentage: 80, isToday: false },
  { label: '五', hours: 2, percentage: 40, isToday: false },
  { label: '六', hours: 3.5, percentage: 70, isToday: false },
  { label: '日', hours: 1.5, percentage: 30, isToday: true }
])

// 学科分布
const subjectDistribution = ref([
  { name: '高等数学', percentage: 40, color: '#3b82f6' },
  { name: '线性代数', percentage: 25, color: '#10b981' },
  { name: '概率统计', percentage: 20, color: '#8b5cf6' },
  { name: '大学物理', percentage: 15, color: '#f59e0b' }
])

// 格式化时间（秒转为小时和分钟）
const formatTime = (seconds) => {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  
  if (hours > 0) {
    return `${hours}小时${minutes > 0 ? ` ${minutes}分钟` : ''}`
  }
  return `${minutes}分钟`
}
</script>

<style scoped>
.learning-timeline-page {
  min-height: calc(100vh - 64px);
  background-color: #f8fafc;
}

/* 时间轴样式 */
.milestone-item {
  animation: fadeIn 0.6s ease-out;
  animation-fill-mode: both;
}

.milestone-item:nth-child(1) { animation-delay: 0.1s; }
.milestone-item:nth-child(2) { animation-delay: 0.2s; }
.milestone-item:nth-child(3) { animation-delay: 0.3s; }
.milestone-item:nth-child(4) { animation-delay: 0.4s; }
.milestone-item:nth-child(5) { animation-delay: 0.5s; }

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

/* 学习建议悬停效果 */
.suggestion-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

/* 成就项目悬停效果 */
.achievement-item {
  transition: all 0.2s ease;
}

.achievement-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.05);
}
</style> 