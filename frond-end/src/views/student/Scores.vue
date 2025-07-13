<template>
  <div class="scores-page p-6">
    <div class="page-header mb-6">
      <h1 class="text-2xl font-bold mb-2">学习成绩分析</h1>
      <p class="text-gray-500">查看你的考试成绩、学习进度和能力分析</p>
    </div>
    
    <!-- 成绩概览卡片 -->
    <div class="overview-cards grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
      <div class="overview-card bg-white rounded-xl shadow-sm p-5 flex items-center">
        <div class="icon-container w-12 h-12 rounded-xl bg-blue-50 flex items-center justify-center mr-4">
          <div class="text-blue-500 text-xl">📊</div>
        </div>
        <div>
          <div class="text-sm text-gray-500">平均分</div>
          <div class="text-xl font-semibold">{{ averageScore }}</div>
          <div class="text-xs text-gray-400">总体表现</div>
        </div>
      </div>
      
      <div class="overview-card bg-white rounded-xl shadow-sm p-5 flex items-center">
        <div class="icon-container w-12 h-12 rounded-xl bg-green-50 flex items-center justify-center mr-4">
          <div class="text-green-500 text-xl">🏆</div>
        </div>
        <div>
          <div class="text-sm text-gray-500">最高分</div>
          <div class="text-xl font-semibold">{{ highestScore.score }}</div>
          <div class="text-xs text-gray-400">{{ highestScore.subject }}</div>
        </div>
      </div>
      
      <div class="overview-card bg-white rounded-xl shadow-sm p-5 flex items-center">
        <div class="icon-container w-12 h-12 rounded-xl bg-amber-50 flex items-center justify-center mr-4">
          <div class="text-amber-500 text-xl">📝</div>
        </div>
        <div>
          <div class="text-sm text-gray-500">考试总数</div>
          <div class="text-xl font-semibold">{{ totalExams }}</div>
          <div class="text-xs text-gray-400">已完成 {{ completedExams }}</div>
        </div>
      </div>
      
      <div class="overview-card bg-white rounded-xl shadow-sm p-5 flex items-center">
        <div class="icon-container w-12 h-12 rounded-xl bg-purple-50 flex items-center justify-center mr-4">
          <div class="text-purple-500 text-xl">📈</div>
        </div>
        <div>
          <div class="text-sm text-gray-500">及格率</div>
          <div class="text-xl font-semibold">{{ passRate }}%</div>
          <div class="text-xs text-gray-400">优秀率 {{ excellentRate }}%</div>
        </div>
      </div>
    </div>
    
    <!-- 主要内容区 -->
    <div class="main-content grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- 左侧：成绩趋势 -->
      <div class="score-trend bg-white rounded-xl shadow-sm p-5 lg:col-span-2">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-lg font-semibold">成绩趋势</h2>
          <div class="flex gap-2">
            <el-select v-model="trendTimeRange" size="small" placeholder="时间范围" class="w-32">
              <el-option v-for="opt in timeRangeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
            <el-select v-model="trendSubject" size="small" placeholder="科目" class="w-32">
              <el-option v-for="opt in subjectOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </div>
        </div>
        
        <div class="trend-chart h-64 flex items-end justify-between">
          <div v-for="(point, index) in trendData" :key="index" class="trend-point flex flex-col items-center">
            <div class="score-value text-xs mb-1">{{ point.score }}</div>
            <div class="score-bar rounded-t-sm" 
                 :style="`height: ${point.score}%; width: 12px; background-color: ${getScoreColor(point.score)}`"></div>
            <div class="date text-xs mt-2 text-gray-500">{{ point.date }}</div>
          </div>
        </div>
        
        <div class="trend-analysis mt-4 p-3 bg-gray-50 rounded-lg">
          <div class="text-sm">
            <span class="font-medium">分析：</span>
            {{ trendAnalysis }}
          </div>
        </div>
      </div>
      
      <!-- 右侧：科目分布 -->
      <div class="subject-distribution bg-white rounded-xl shadow-sm p-5">
        <h2 class="text-lg font-semibold mb-4">科目分布</h2>
        
        <div class="subject-chart space-y-4">
          <div v-for="subject in subjectDistribution" :key="subject.name" class="subject-item">
            <div class="flex justify-between mb-1">
              <span class="text-sm">{{ subject.name }}</span>
              <span class="text-sm font-medium">{{ subject.average }}</span>
            </div>
            <div class="h-2 bg-gray-100 rounded-full overflow-hidden">
              <div class="h-full rounded-full" 
                   :style="`width: ${subject.average}%; background: ${subject.color}`"></div>
            </div>
          </div>
        </div>
        
        <div class="mt-4 text-center">
          <el-button type="primary" size="small" plain @click="showSubjectDetails = true">
            查看详细分析
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 成绩详情表格 -->
    <div class="score-details bg-white rounded-xl shadow-sm p-5 mt-6">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-lg font-semibold">成绩详情</h2>
        <div class="flex gap-2">
          <el-input v-model="searchQuery" placeholder="搜索考试..." size="small" clearable class="w-48">
            <template #prefix>
              <i class="el-icon-search"></i>
            </template>
          </el-input>
          <el-select v-model="filterSubject" size="small" placeholder="科目" class="w-32">
            <el-option v-for="opt in subjectOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
          <el-select v-model="filterStatus" size="small" placeholder="状态" class="w-32">
            <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </div>
      </div>
      
      <el-table :data="filteredExams" style="width: 100%" :border="false" class="custom-table">
        <el-table-column prop="examTitle" label="考试名称" min-width="180">
          <template #default="scope">
            <div class="font-medium">{{ scope.row.examTitle }}</div>
            <div class="text-xs text-gray-500">{{ scope.row.courseName }}</div>
          </template>
        </el-table-column>
        
        <el-table-column prop="submittedAt" label="考试日期" width="120" />
        
        <el-table-column prop="totalScore" label="分数" width="100">
          <template #default="scope">
            <div class="flex items-center">
              <span :class="{
                'text-green-600 font-medium': scope.row.totalScore >= 90,
                'text-blue-600 font-medium': scope.row.totalScore >= 80 && scope.row.totalScore < 90,
                'text-amber-600 font-medium': scope.row.totalScore >= 60 && scope.row.totalScore < 80,
                'text-red-600 font-medium': scope.row.totalScore < 60
              }">{{ scope.row.totalScore }}</span>
              <span class="text-gray-400 text-xs ml-1">/{{ scope.row.totalScore }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="rank" label="排名" width="100">
          <template #default="scope">
            {{ scope.row.rank }}/{{ scope.row.totalStudents }}
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.totalScore)" size="small">
              {{ getStatusText(scope.row.totalScore) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <div class="flex gap-2">
              <el-button type="primary" size="small" plain @click="viewExamDetail(scope.row.examId)">
                查看详情
              </el-button>
              <el-button type="success" size="small" plain @click="reviewExam(scope.row.examId)">
                错题复习
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination flex justify-center mt-4">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="totalExamPages"
          :page-size="pageSize"
          @current-change="handlePageChange"
        />
      </div>
    </div>
    
    <!-- 科目详情对话框 -->
    <el-dialog v-model="showSubjectDetails" title="科目详细分析" width="80%" destroy-on-close>
      <div class="subject-details">
        <el-tabs>
          <el-tab-pane v-for="subject in subjectDistribution" :key="subject.name" :label="subject.name">
            <div class="p-4">
              <div class="flex justify-between mb-6">
                <div class="stats-card p-4 bg-gray-50 rounded-lg flex-1 mr-4">
                  <div class="text-lg font-medium">{{ subject.average }}</div>
                  <div class="text-sm text-gray-500">平均分</div>
                </div>
                <div class="stats-card p-4 bg-gray-50 rounded-lg flex-1 mr-4">
                  <div class="text-lg font-medium">{{ subject.highest }}</div>
                  <div class="text-sm text-gray-500">最高分</div>
                </div>
                <div class="stats-card p-4 bg-gray-50 rounded-lg flex-1 mr-4">
                  <div class="text-lg font-medium">{{ subject.lowest }}</div>
                  <div class="text-sm text-gray-500">最低分</div>
                </div>
                <div class="stats-card p-4 bg-gray-50 rounded-lg flex-1">
                  <div class="text-lg font-medium">{{ subject.examCount }}</div>
                  <div class="text-sm text-gray-500">考试次数</div>
                </div>
              </div>
              
              <h3 class="font-medium mb-2">知识点掌握情况</h3>
              <div class="knowledge-points space-y-3 mb-6">
                <div v-for="(point, index) in subject.knowledgePoints" :key="index" class="knowledge-point">
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
              
              <h3 class="font-medium mb-2">学习建议</h3>
              <div class="suggestions p-3 bg-blue-50 rounded-lg text-sm">
                {{ subject.suggestions }}
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { get } from '../../net'
import { useAuthStore } from '../../stores/counter'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const loading = ref(true)

// 考试成绩数据
const examScores = ref([])
const examDetail = ref(null)

// 筛选和分页
const searchQuery = ref('')
const filterSubject = ref('all')
const filterStatus = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)

// 统计数据
const averageScore = computed(() => {
  if (examScores.value.length === 0) return '0';
  const sum = examScores.value.reduce((acc, exam) => acc + (parseFloat(exam.totalScore) || 0), 0);
  return (sum / examScores.value.length).toFixed(1);
})

const highestScore = computed(() => {
  if (examScores.value.length === 0) return { score: '0', subject: '无数据' };
  const highest = examScores.value.reduce((max, exam) => 
    (parseFloat(exam.totalScore) || 0) > (parseFloat(max.totalScore) || 0) ? exam : max, 
    { totalScore: 0 }
  );
  return { score: highest.totalScore, subject: highest.examTitle };
})

const totalExams = computed(() => examScores.value.length)

const completedExams = computed(() => 
  examScores.value.filter(exam => exam.status === '已批改' || exam.status === '已提交').length
)

const passRate = computed(() => {
  const completed = examScores.value.filter(exam => exam.status === '已批改' || exam.status === '已提交');
  if (completed.length === 0) return '0';
  const passed = completed.filter(exam => (parseFloat(exam.totalScore) || 0) >= 60);
  return Math.round((passed.length / completed.length) * 100);
})

const excellentRate = computed(() => {
  const completed = examScores.value.filter(exam => exam.status === '已批改' || exam.status === '已提交');
  if (completed.length === 0) return '0';
  const excellent = completed.filter(exam => (parseFloat(exam.totalScore) || 0) >= 90);
  return Math.round((excellent.length / completed.length) * 100);
})

// 趋势数据
const trendTimeRange = ref('all')
const trendSubject = ref('all')

const trendData = computed(() => {
  let filteredScores = examScores.value.filter(exam => 
    (trendSubject.value === 'all' || exam.courseName === trendSubject.value) &&
    (exam.status === '已批改' || exam.status === '已提交')
  );
  
  // 按时间范围筛选
  if (trendTimeRange.value === 'month') {
    const oneMonthAgo = new Date();
    oneMonthAgo.setMonth(oneMonthAgo.getMonth() - 1);
    filteredScores = filteredScores.filter(exam => new Date(exam.submittedAt) >= oneMonthAgo);
  } else if (trendTimeRange.value === 'semester') {
    const sixMonthsAgo = new Date();
    sixMonthsAgo.setMonth(sixMonthsAgo.getMonth() - 6);
    filteredScores = filteredScores.filter(exam => new Date(exam.submittedAt) >= sixMonthsAgo);
  }
  
  // 按日期排序
  filteredScores.sort((a, b) => new Date(a.submittedAt) - new Date(b.submittedAt));
  
  // 限制最多显示10个点
  if (filteredScores.length > 10) {
    const step = Math.ceil(filteredScores.length / 10);
    filteredScores = filteredScores.filter((_, index) => index % step === 0).slice(0, 10);
  }
  
  return filteredScores.map(exam => ({
    date: formatDate(exam.submittedAt),
    score: parseFloat(exam.totalScore) || 0
  }));
})

const trendAnalysis = computed(() => {
  if (trendData.value.length <= 1) {
    return '数据点不足，无法分析趋势';
  }
  
  const scores = trendData.value.map(p => p.score);
  const firstScore = scores[0];
  const lastScore = scores[scores.length - 1];
  const avgScore = scores.reduce((sum, score) => sum + score, 0) / scores.length;
  
  if (lastScore > firstScore) {
    return `成绩呈上升趋势，从${firstScore}分提高到${lastScore}分，平均分为${avgScore.toFixed(1)}分`;
  } else if (lastScore < firstScore) {
    return `成绩呈下降趋势，从${firstScore}分下降到${lastScore}分，平均分为${avgScore.toFixed(1)}分`;
  } else {
    return `成绩保持稳定，平均分为${avgScore.toFixed(1)}分`;
  }
})

// 科目分布数据
const subjectDistribution = computed(() => {
  const subjects = {};
  
  examScores.value.forEach(exam => {
    if (!exam.courseName) return;
    
    if (!subjects[exam.courseName]) {
      subjects[exam.courseName] = {
        name: exam.courseName,
        scores: [],
        color: getRandomColor()
      };
    }
    
    if (exam.status === '已批改' || exam.status === '已提交') {
      subjects[exam.courseName].scores.push(parseFloat(exam.totalScore) || 0);
    }
  });
  
  return Object.values(subjects).map(subject => {
    const scores = subject.scores;
    return {
      name: subject.name,
      average: scores.length > 0 ? Math.round(scores.reduce((sum, score) => sum + score, 0) / scores.length) : 0,
      highest: scores.length > 0 ? Math.max(...scores) : 0,
      lowest: scores.length > 0 ? Math.min(...scores) : 0,
      examCount: scores.length,
      color: subject.color,
      knowledgePoints: generateKnowledgePoints(subject.name),
      suggestions: generateSuggestions(subject.name, scores)
    };
  }).sort((a, b) => b.average - a.average);
})

// 筛选后的考试列表
const filteredExams = computed(() => {
  return examScores.value.filter(exam => {
    const subjectMatch = filterSubject.value === 'all' || exam.courseName === filterSubject.value;
    const statusMatch = filterStatus.value === 'all' || exam.status === filterStatus.value;
    const searchMatch = 
      exam.examTitle.toLowerCase().includes(searchQuery.value.toLowerCase()) || 
      exam.courseName.toLowerCase().includes(searchQuery.value.toLowerCase());
    
    return subjectMatch && statusMatch && searchMatch;
  }).sort((a, b) => new Date(b.submittedAt) - new Date(a.submittedAt));
})

// 总页数
const totalExamPages = computed(() => Math.ceil(filteredExams.value.length / pageSize.value) * 10)

// 当前页的考试
const currentPageExams = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return filteredExams.value.slice(start, end);
})

// 科目选项
const subjectOptions = computed(() => {
  const subjects = new Set(examScores.value.map(e => e.courseName).filter(Boolean));
  return [
    { label: '全部科目', value: 'all' },
    ...Array.from(subjects).map(s => ({ label: s, value: s }))
  ];
})

// 状态选项
const statusOptions = [
  { label: '全部状态', value: 'all' },
  { label: '已批改', value: '已批改' },
  { label: '已提交', value: '已提交' },
  { label: '进行中', value: '进行中' }
]

// 时间范围选项
const timeRangeOptions = [
  { label: '全部时间', value: 'all' },
  { label: '近一个月', value: 'month' },
  { label: '本学期', value: 'semester' }
]

// 科目详情对话框
const showSubjectDetails = ref(false)

// 获取成绩颜色
const getScoreColor = (score) => {
  if (score >= 90) return '#10b981'; // 绿色
  if (score >= 80) return '#3b82f6'; // 蓝色
  if (score >= 60) return '#f59e0b'; // 黄色
  return '#ef4444'; // 红色
}

// 获取知识点掌握程度颜色
const getKnowledgePointColor = (mastery) => {
  if (mastery >= 90) return '#10b981';
  if (mastery >= 70) return '#3b82f6';
  if (mastery >= 50) return '#f59e0b';
  return '#ef4444';
}

// 获取状态类型
const getStatusType = (score) => {
  if (score >= 90) return 'success';
  if (score >= 80) return 'primary';
  if (score >= 60) return 'warning';
  return 'danger';
}

// 获取状态文本
const getStatusText = (score) => {
  if (score >= 90) return '优秀';
  if (score >= 80) return '良好';
  if (score >= 60) return '及格';
  return '不及格';
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return `${date.getMonth() + 1}/${date.getDate()}`;
}

// 随机颜色生成
const getRandomColor = () => {
  const colors = [
    '#3b82f6', '#10b981', '#f59e0b', '#ef4444', 
    '#8b5cf6', '#ec4899', '#06b6d4', '#84cc16'
  ];
  return colors[Math.floor(Math.random() * colors.length)];
}

// 生成知识点数据（模拟）
const generateKnowledgePoints = (subject) => {
  // 这里可以根据科目生成不同的知识点
  return [
    { name: '基础概念', mastery: Math.floor(Math.random() * 30) + 70 },
    { name: '公式应用', mastery: Math.floor(Math.random() * 40) + 60 },
    { name: '问题解决', mastery: Math.floor(Math.random() * 50) + 50 },
    { name: '高级应用', mastery: Math.floor(Math.random() * 60) + 40 }
  ];
}

// 生成学习建议（模拟）
const generateSuggestions = (subject, scores) => {
  if (scores.length === 0) return '暂无数据，无法生成学习建议';
  
  const avgScore = scores.reduce((sum, score) => sum + score, 0) / scores.length;
  
  if (avgScore >= 90) {
    return `你在${subject}科目表现优秀，建议可以尝试更具挑战性的内容，或帮助其他同学提高。`;
  } else if (avgScore >= 80) {
    return `你在${subject}科目掌握良好，可以针对性地复习一些细节知识点，进一步提高成绩。`;
  } else if (avgScore >= 60) {
    return `你在${subject}科目已达到基本要求，但仍有提升空间，建议多做练习，巩固基础知识。`;
  } else {
    return `你在${subject}科目需要加强学习，建议回顾基础概念，多与老师沟通，制定针对性的学习计划。`;
  }
}

// 查看考试详情
const viewExamDetail = (examId) => {
  router.push(`/student/scores/detail?examId=${examId}`);
}

// 错题复习
const reviewExam = (examId) => {
  router.push(`/student/mistakes?examId=${examId}`);
}

// 分页处理
const handlePageChange = (page) => {
  currentPage.value = page;
}

// 获取成绩列表
const fetchScores = () => {
  loading.value = true;
  get('/api/student/exams/scores', { studentId: authStore.user.userId }, 
    (message, data) => {
      examScores.value = data || [];
      loading.value = false;
      
      // 如果URL中有examId参数，获取该考试的详情
      const examId = route.query.examId;
      if (examId) {
        fetchExamDetail(examId);
      }
    },
    (message) => {
      ElMessage.error(message || '获取成绩列表失败');
      loading.value = false;
    }
  );
}

// 获取考试详情
const fetchExamDetail = (examId) => {
  const submission = examScores.value.find(s => s.examId == examId);
  if (submission) {
    get(`/api/student/exams/scores/${submission.id}`, {}, 
      (message, data) => {
        examDetail.value = data;
      }
    );
  }
}

onMounted(() => {
  fetchScores();
})
</script>

<style scoped>
.scores-page {
  min-height: calc(100vh - 64px);
  background-color: #f8fafc;
}

/* 卡片样式 */
.overview-card, .score-trend, .subject-distribution, .score-details {
  transition: all 0.3s ease;
}

.overview-card:hover, .score-trend:hover, .subject-distribution:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.05);
}

/* 成绩趋势图表 */
.score-bar {
  transition: height 1s ease, background-color 0.3s ease;
}

.trend-point:hover .score-bar {
  opacity: 0.8;
}

/* 表格样式 */
.custom-table :deep(.el-table__header) {
  background-color: #f8fafc;
}

.custom-table :deep(.el-table__row) {
  cursor: pointer;
  transition: all 0.2s ease;
}

.custom-table :deep(.el-table__row:hover) {
  background-color: #f1f5f9;
}

.custom-table :deep(.el-table__header-cell) {
  font-weight: 600;
  color: #4b5563;
}

/* 动画效果 */
.overview-cards, .score-trend, .subject-distribution, .score-details {
  animation: fadeInUp 0.6s ease-out;
  animation-fill-mode: both;
}

.score-trend {
  animation-delay: 0.2s;
}

.subject-distribution {
  animation-delay: 0.3s;
}

.score-details {
  animation-delay: 0.4s;
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