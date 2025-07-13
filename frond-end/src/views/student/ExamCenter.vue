<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import ExamsList from './Exams.vue'
import AdaptiveExam from './AdaptiveExam.vue'
import Scores from './Scores.vue'
import { ElMessage } from 'element-plus'
import { BookOpen, Clock, Award, Target, Calendar, ChartBar } from 'lucide-vue-next'
import { get } from '../../net'
import { useAuthStore } from '../../stores/counter'

const router = useRouter()
const activeTab = ref('list')
const loading = ref(true)
const authStore = useAuthStore()

// 各部分加载状态
const loadingStats = ref(true)
const loadingTrend = ref(true)
const loadingUpcoming = ref(true)

// 考试统计数据
const stats = ref({
  totalExams: 0,
  completedExams: 0,
  upcomingExams: 0,
  ongoingExams: 0,
  averageScore: 0,
  highestScore: 0,
  lowestScore: 0,
  passRate: 0
})

// 最近考试记录
const recentExams = ref([])

// 即将到来的考试
const upcomingExams = ref([])

// 考试成绩趋势
const scoresTrend = ref([])
const trendLabels = ref([])

// 切换标签
const handleTabChange = (tab) => {
  // 可以在这里添加标签切换的逻辑
}

// 开始考试
const startExam = (id) => {
  router.push(`/student/exam/${id}`)
}

// 查看成绩
const viewScore = (id) => {
  router.push(`/student/scores?examId=${id}`)
}

// 开始自适应测试
const startAdaptiveExam = () => {
  ElMessage.success('正在准备自适应测试...')
  setTimeout(() => {
    router.push('/student/adaptive-exam/start')
  }, 1000)
}

// 获取考试统计数据
const fetchExamStats = () => {
  loadingStats.value = true;
  return new Promise((resolve, reject) => {
    get('/api/student/exams/stats', { studentId: authStore.user.userId },
      (message, data) => {
        if (data) {
          stats.value = data;
          loadingStats.value = false;
          resolve(data);
        } else {
          loadingStats.value = false;
          resolve(null);
        }
      },
      (message) => {
        ElMessage.error(message || '获取考试统计数据失败');
        loadingStats.value = false;
        reject(message);
      }
    );
  });
}

// 获取成绩趋势数据
const fetchScoreTrend = () => {
  loadingTrend.value = true;
  return new Promise((resolve, reject) => {
    get('/api/student/exams/score-trend', { studentId: authStore.user.userId, limit: 7 },
      (message, data) => {
        if (data && data.length > 0) {
          // 提取分数数据
          scoresTrend.value = data.map(item => item.score).reverse();
          
          // 提取日期标签
          trendLabels.value = data.map(item => {
            const date = new Date(item.submittedAt);
            return `${date.getMonth() + 1}/${date.getDate()}`;
          }).reverse();
          
          // 更新最近考试记录
          recentExams.value = data.slice(0, 3).map(item => {
            const date = new Date(item.submittedAt);
            return {
              id: item.examId,
              submissionId: item.submissionId,
              title: item.examTitle,
              date: `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`,
              score: item.score,
              status: 'finished'
            };
          });
        }
        loadingTrend.value = false;
        resolve(data);
      },
      (message) => {
        console.warn('获取成绩趋势数据失败:', message);
        loadingTrend.value = false;
        reject(message);
      }
    );
  });
}

// 获取即将到来的考试
const fetchUpcomingExams = () => {
  loadingUpcoming.value = true;
  return new Promise((resolve, reject) => {
    get('/api/student/exams/list', { studentId: authStore.user.userId },
      (message, data) => {
        if (data) {
          console.log('获取到的考试列表:', data);
          // 筛选未开始的考试
          const upcoming = data.filter(exam => {
            console.log('考试状态:', exam.status, '考试开始时间:', exam.startTime);
            return exam.status === '未开始';
          })
            .slice(0, 3)
            .map(exam => {
              const startDate = new Date(exam.startTime);
              console.log('解析后的日期对象:', startDate, '是否有效:', !isNaN(startDate.getTime()));
              return {
                id: exam.id,
                title: exam.title,
                date: `${startDate.getFullYear()}-${String(startDate.getMonth() + 1).padStart(2, '0')}-${String(startDate.getDate()).padStart(2, '0')}`,
                duration: exam.duration,
                subject: exam.courseName
              };
            });
          
          upcomingExams.value = upcoming;
          console.log('即将到来的考试:', upcomingExams.value);
        }
        loadingUpcoming.value = false;
        resolve(data);
      },
      (message) => {
        console.warn('获取即将到来的考试失败:', message);
        loadingUpcoming.value = false;
        reject(message);
      }
    );
  });
}

// 格式化日期
const formatDate = (dateTime) => {
  if (!dateTime) return '';
  const date = new Date(dateTime);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}

// 计算剩余时间
const getRemainingTime = (dateStr) => {
  const examDate = new Date(dateStr);
  const now = new Date();
  const diffTime = examDate - now;
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  
  if (diffDays <= 0) return '今天';
  if (diffDays === 1) return '明天';
  if (diffDays < 7) return `${diffDays}天后`;
  if (diffDays < 30) return `${Math.floor(diffDays / 7)}周后`;
  return `${Math.floor(diffDays / 30)}个月后`;
}

onMounted(() => {
  // 获取数据
  Promise.all([
    fetchExamStats(),
    fetchScoreTrend(),
    fetchUpcomingExams()
  ]).finally(() => {
    loading.value = false;
  });
})
</script>

<template>
  <section class="exam-center-wrapper p-4 md:p-6" v-loading="loading">
    <!-- 顶部统计卡片 -->
    <div class="stats-section grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
      <div class="stat-card bg-gradient-to-br from-blue-50 to-indigo-100 rounded-xl p-5 shadow-md flex items-center">
        <div class="stat-icon bg-blue-500/10 p-3 rounded-lg">
          <BookOpen :size="24" class="text-blue-600" />
        </div>
        <div class="ml-4">
          <div class="text-sm text-gray-500">考试总数</div>
          <div class="text-xl font-semibold">{{ stats.totalExams }}</div>
          <div class="text-xs text-gray-500 mt-1">
            已完成 {{ stats.completedExams }}/{{ stats.totalExams }}
          </div>
        </div>
      </div>
      
      <div class="stat-card bg-gradient-to-br from-green-50 to-green-100 rounded-xl p-5 shadow-md flex items-center">
        <div class="stat-icon bg-green-500/10 p-3 rounded-lg">
          <Clock :size="24" class="text-green-600" />
        </div>
        <div class="ml-4">
          <div class="text-sm text-gray-500">即将到来</div>
          <div class="text-xl font-semibold">{{ stats.upcomingExams }}</div>
          <div class="text-xs text-gray-500 mt-1">
            进行中 {{ stats.ongoingExams }}
          </div>
        </div>
      </div>
      
      <div class="stat-card bg-gradient-to-br from-amber-50 to-amber-100 rounded-xl p-5 shadow-md flex items-center">
        <div class="stat-icon bg-amber-500/10 p-3 rounded-lg">
          <Award :size="24" class="text-amber-600" />
        </div>
        <div class="ml-4">
          <div class="text-sm text-gray-500">平均分数</div>
          <div class="text-xl font-semibold">{{ stats.averageScore }}</div>
          <div class="text-xs text-gray-500 mt-1">
            最高分 {{ stats.highestScore }}
          </div>
        </div>
      </div>
      
      <div class="stat-card bg-gradient-to-br from-purple-50 to-purple-100 rounded-xl p-5 shadow-md flex items-center">
        <div class="stat-icon bg-purple-500/10 p-3 rounded-lg">
          <Target :size="24" class="text-purple-600" />
        </div>
        <div class="ml-4">
          <div class="text-sm text-gray-500">达标率</div>
          <div class="text-xl font-semibold">{{ stats.passRate }}%</div>
          <div class="text-xs text-gray-500 mt-1">
            及格率 {{ stats.passRate }}%
          </div>
        </div>
      </div>
    </div>
    
    <!-- 主要内容区 -->
    <div class="main-content grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6">
      <!-- 左侧：即将到来的考试 -->
      <div class="upcoming-exams bg-white rounded-xl shadow-md p-5">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-lg font-medium flex items-center">
            <Calendar :size="20" class="text-blue-600 mr-2" />
            即将到来的考试
          </h3>
          <el-button type="text" size="small" @click="activeTab = 'list'">查看全部</el-button>
        </div>
        
        <div v-loading="loadingUpcoming" class="min-h-[150px]">
          <div v-if="!loadingUpcoming" class="space-y-4">
            <div v-for="exam in upcomingExams" :key="exam.id" 
                 class="upcoming-exam p-4 border border-gray-100 rounded-lg hover:bg-gray-50 transition-colors cursor-pointer"
                 @click="startExam(exam.id)">
              <div class="flex justify-between items-start">
                <div>
                  <div class="font-medium">{{ exam.title }}</div>
                  <div class="text-xs text-gray-500 mt-1">{{ exam.subject }} · {{ exam.duration }}分钟</div>
                </div>
                <el-tag size="small" type="warning">{{ exam.date }}</el-tag>
              </div>
              <div class="mt-3 flex justify-end">
                <el-button type="primary" size="small" @click.stop="startExam(exam.id)">
                  进入考试
                </el-button>
              </div>
            </div>
          </div>
          
          <div v-if="!loadingUpcoming && upcomingExams.length === 0" class="text-center text-gray-400 py-8">
            <div class="flex flex-col items-center">
              <Calendar :size="32" class="text-gray-300 mb-2" />
              <p>暂无即将到来的考试</p>
              <p class="text-xs mt-1">您可以在"考试列表"标签页查看所有考试</p>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 中间：成绩趋势 -->
      <div class="scores-trend bg-white rounded-xl shadow-md p-5">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-lg font-medium flex items-center">
            <ChartBar :size="20" class="text-green-600 mr-2" />
            成绩趋势
          </h3>
          <el-button type="text" size="small" @click="activeTab = 'scores'">详细分析</el-button>
        </div>
        
        <div v-loading="loadingTrend" class="min-h-[150px]">
          <div v-if="!loadingTrend && scoresTrend.length > 0" class="chart-container h-40 flex items-end justify-between gap-1">
            <div v-for="(score, index) in scoresTrend" :key="index" class="score-bar-container h-full flex flex-col items-center justify-end">
              <div class="score-value text-xs mb-1">{{ score }}</div>
              <div class="score-bar bg-blue-500 rounded-t-sm w-8" :style="`height: ${score}%`"></div>
              <div class="text-xs mt-1 text-gray-500">{{ trendLabels[index] || '-' }}</div>
            </div>
          </div>
          
          <div v-if="!loadingTrend && scoresTrend.length === 0" class="text-center text-gray-400 py-8">
            <div class="flex flex-col items-center">
              <ChartBar :size="32" class="text-gray-300 mb-2" />
              <p>暂无成绩数据</p>
              <p class="text-xs mt-1">完成考试后可查看成绩趋势</p>
            </div>
          </div>
          
          <div v-if="!loadingTrend && scoresTrend.length > 0" class="mt-4 text-center">
            <div class="text-sm text-gray-500">最近{{ scoresTrend.length }}次考试平均分</div>
            <div class="text-xl font-semibold text-blue-600">
              {{ Math.round(scoresTrend.reduce((a, b) => a + b, 0) / scoresTrend.length) }}
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧：自适应测试 -->
      <div class="adaptive-test bg-gradient-to-br from-indigo-500 to-purple-600 text-white rounded-xl shadow-lg p-5">
        <h3 class="text-lg font-medium mb-3">自适应智能测试</h3>
        <p class="text-sm text-indigo-100 mb-4">
          系统根据你的实时表现动态调整题目难度，为你提供精准评估。
        </p>
        
        <div class="flex flex-wrap gap-3 mb-4">
          <div class="feature-item bg-white/10 backdrop-blur-sm p-3 rounded-lg flex-1">
            <div class="text-xl font-bold">🎯</div>
            <div class="text-xs mt-1">智能选题</div>
          </div>
          <div class="feature-item bg-white/10 backdrop-blur-sm p-3 rounded-lg flex-1">
            <div class="text-xl font-bold">⚡</div>
            <div class="text-xs mt-1">即刻反馈</div>
          </div>
          <div class="feature-item bg-white/10 backdrop-blur-sm p-3 rounded-lg flex-1">
            <div class="text-xl font-bold">📊</div>
            <div class="text-xs mt-1">精准评估</div>
          </div>
        </div>
        
        <el-button type="primary" @click="startAdaptiveExam" class="w-full bg-white text-indigo-600 border-none hover:bg-indigo-50">
          开始智能测试
        </el-button>
      </div>
    </div>
    
    <!-- 最近考试记录 -->
    <div class="recent-exams bg-white rounded-xl shadow-md p-5 mb-6">
      <div class="flex justify-between items-center mb-4">
        <h3 class="text-lg font-medium">最近考试记录</h3>
      </div>
      
      <el-table :data="recentExams" style="width: 100%" :border="false">
        <el-table-column prop="title" label="考试名称" min-width="180" />
        <el-table-column prop="date" label="考试日期" width="120" />
        <el-table-column prop="score" label="分数" width="100">
          <template #default="scope">
            <span :class="{
              'text-green-600': scope.row.score >= 90,
              'text-blue-600': scope.row.score >= 80 && scope.row.score < 90,
              'text-amber-600': scope.row.score >= 60 && scope.row.score < 80,
              'text-red-600': scope.row.score < 60
            }">{{ scope.row.score }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button type="primary" size="small" plain @click="viewScore(scope.row.submissionId)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div v-if="recentExams.length === 0" class="text-center text-gray-400 py-8">
        暂无考试记录
      </div>
    </div>

    <!-- 内容标签页 -->
    <div class="content-tabs bg-white rounded-xl shadow-md overflow-hidden">
      <el-tabs 
        v-model="activeTab" 
        class="exam-tabs custom-tabs" 
        @tab-change="handleTabChange"
        tab-position="top"
      >
        <el-tab-pane label="考试列表" name="list">
          <ExamsList />
        </el-tab-pane>
        <el-tab-pane label="自适应测验" name="adaptive">
          <AdaptiveExam />
        </el-tab-pane>
        <el-tab-pane label="历史成绩" name="scores">
          <Scores />
        </el-tab-pane>
      </el-tabs>
    </div>
  </section>
</template>

<style scoped>
.exam-center-wrapper {
  min-height: calc(100vh - 64px);
  background-color: #f8fafc;
}

.stat-card {
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.08);
}

/* 自定义标签页样式 */
.custom-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
  padding: 0 1rem;
  border-bottom: 1px solid #ebeef5;
  background-color: #f8f9fa;
}

.custom-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.custom-tabs :deep(.el-tabs__item) {
  height: 50px;
  line-height: 50px;
  font-weight: 500;
  transition: all 0.3s;
}

.custom-tabs :deep(.el-tabs__item.is-active) {
  color: #4f46e5;
  font-weight: 600;
}

.custom-tabs :deep(.el-tabs__active-bar) {
  background-color: #4f46e5;
  height: 3px;
  border-radius: 3px;
}

.custom-tabs :deep(.el-tabs__content) {
  padding: 0;
}

/* 成绩趋势图表 */
.score-bar {
  min-height: 4px;
  transition: height 1s ease;
}

.score-bar-container:hover .score-bar {
  background-color: #6366f1;
}

/* 动画效果 */
.stat-card, .upcoming-exams, .scores-trend, .adaptive-test, .recent-exams, .content-tabs {
  animation: fadeInUp 0.6s ease-out;
  animation-fill-mode: both;
}

.upcoming-exams {
  animation-delay: 0.1s;
}

.scores-trend {
  animation-delay: 0.2s;
}

.adaptive-test {
  animation-delay: 0.3s;
}

.recent-exams {
  animation-delay: 0.4s;
}

.content-tabs {
  animation-delay: 0.5s;
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

/* 表格样式优化 */
.recent-exams :deep(.el-table) {
  --el-table-border-color: transparent;
  --el-table-header-bg-color: #f8fafc;
  --el-table-row-hover-bg-color: #f1f5f9;
}

.recent-exams :deep(.el-table__header) {
  font-weight: 600;
  color: #4b5563;
}

.recent-exams :deep(.el-table__row) {
  cursor: pointer;
}

/* 自适应测试卡片 */
.feature-item {
  text-align: center;
  transition: all 0.3s ease;
}

.feature-item:hover {
  transform: translateY(-3px);
  background-color: rgba(255, 255, 255, 0.2);
}
</style> 