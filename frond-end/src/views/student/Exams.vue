<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Calendar, Clock, BookOpen, Tag, Filter } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, post } from '../../net'
import { useAuthStore } from '../../stores/counter'

const router = useRouter()
const loading = ref(true)
const authStore = useAuthStore()

// 考试数据
const exams = ref([])

// 筛选状态
const search = ref('')
const status = ref('all')
const subject = ref('all')
const category = ref('all')
const viewMode = ref('grid') // grid 或 list

// 计算属性：筛选后的考试列表
const filtered = computed(() => {
  return exams.value.map(exam => {
    // 创建考试对象的副本，避免修改原始数据
    const e = {...exam};
    
    // 将后端返回的状态映射到前端状态
    if (e.status === '未开始') e.status = 'upcoming';
    else if (e.status === '进行中') e.status = 'ongoing';
    else if (e.status === '已结束') e.status = 'finished';
    
    return e;
  }).filter(e => {
    // 状态筛选
    const statusMatch = status.value === 'all' || e.status === status.value;
    
    // 科目筛选
    const subjectMatch = subject.value === 'all' || e.courseName === subject.value;
    
    // 类别筛选
    const categoryMatch = category.value === 'all' || e.examType === category.value;
    
    // 搜索筛选
    const searchMatch = 
      e.title.toLowerCase().includes(search.value.toLowerCase()) || 
      e.courseName.toLowerCase().includes(search.value.toLowerCase());
    
    return statusMatch && subjectMatch && categoryMatch && searchMatch;
  }).sort((a, b) => {
    // 排序：进行中 > 未开始 > 已结束，同状态按日期排序
    if (a.status !== b.status) {
      const statusOrder = { 'ongoing': 0, 'upcoming': 1, 'finished': 2 };
      return statusOrder[a.status] - statusOrder[b.status];
    }
    return new Date(a.startTime) - new Date(b.startTime);
  });
})

// 状态选项
const statusOptions = [
  { label: '全部', value: 'all' },
  { label: '未开始', value: 'upcoming' },
  { label: '进行中', value: 'ongoing' },
  { label: '已结束', value: 'finished' }
]

// 科目选项（从考试数据中提取）
const subjectOptions = computed(() => {
  const subjects = new Set(exams.value.map(e => e.courseName));
  return [
    { label: '全部科目', value: 'all' },
    ...Array.from(subjects).map(s => ({ label: s, value: s }))
  ];
})

// 类别选项（从考试数据中提取）
const categoryOptions = computed(() => {
  const categories = new Set(exams.value.map(e => e.examType));
  return [
    { label: '全部类型', value: 'all' },
    ...Array.from(categories).map(c => ({ label: c, value: c }))
  ];
})

// 状态对应的样式
const statusStyle = {
  upcoming: {
    color: 'bg-yellow-100 text-yellow-800',
    icon: 'bg-gradient-to-r from-yellow-400 to-yellow-600',
    badge: 'bg-yellow-100 text-yellow-800 border border-yellow-200',
    text: '未开始'
  },
  ongoing: {
    color: 'bg-green-100 text-green-700',
    icon: 'bg-gradient-to-r from-green-400 to-green-600',
    badge: 'bg-green-100 text-green-700 border border-green-200',
    text: '进行中'
  },
  finished: {
    color: 'bg-gray-100 text-gray-600',
    icon: 'bg-gradient-to-r from-gray-400 to-gray-600',
    badge: 'bg-gray-100 text-gray-600 border border-gray-200',
    text: '已完成'
  },
  // 添加后端返回的状态映射
  '未开始': {
    color: 'bg-yellow-100 text-yellow-800',
    icon: 'bg-gradient-to-r from-yellow-400 to-yellow-600',
    badge: 'bg-yellow-100 text-yellow-800 border border-yellow-200',
    text: '未开始'
  },
  '进行中': {
    color: 'bg-green-100 text-green-700',
    icon: 'bg-gradient-to-r from-green-400 to-green-600',
    badge: 'bg-green-100 text-green-700 border border-green-200',
    text: '进行中'
  },
  '已结束': {
    color: 'bg-gray-100 text-gray-600',
    icon: 'bg-gradient-to-r from-gray-400 to-gray-600',
    badge: 'bg-gray-100 text-gray-600 border border-gray-200',
    text: '已结束'
  }
}

// 跳转到考试页面
const goExam = (ex) => {
  if (ex.submissionStatus === '已批改' || ex.submissionStatus === '已提交') {
    // 查看成绩
    router.push(`/student/scores?examId=${ex.id}`);
  } else {
    // 检查考试状态
    if (ex.status === 'upcoming') {
      ElMessageBox.confirm(`确定要提前进入考试"${ex.title}"吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        startExam(ex.id);
      }).catch(() => {});
    } else {
      // 开始考试
      startExam(ex.id);
    }
  }
}

// 开始考试
const startExam = (examId) => {
  post(`/api/student/exams/${examId}/start?studentId=${authStore.user.userId}`, {}, 
    (message, data) => {
      if (data) {
        router.push(`/student/exam/${examId}?submissionId=${data}`);
      }
    },
    (message) => {
      ElMessage.error(message || '无法开始考试');
    }
  );
}

// 查看考试详情
const viewExamDetails = (ex) => {
  ElMessageBox.alert(
    `<div class="exam-details">
      <p><strong>考试科目:</strong> ${ex.courseName}</p>
      <p><strong>考试班级:</strong> ${ex.className}</p>
      <p><strong>考试时间:</strong> ${formatDateTime(ex.startTime)} - ${formatDateTime(ex.endTime)}</p>
      <p><strong>考试时长:</strong> ${ex.duration}分钟</p>
      <p><strong>总分:</strong> ${ex.totalScore || 100}分</p>
      <p><strong>考试状态:</strong> ${statusStyle[ex.status].text}</p>
      <p><strong>提交状态:</strong> ${ex.submissionStatus}</p>
      ${ex.userScore !== null ? `<p><strong>得分:</strong> ${ex.userScore}分</p>` : ''}
    </div>`,
    `${ex.title} - 详细信息`,
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭',
      customClass: 'exam-detail-dialog'
    }
  );
}

// 重置筛选条件
const resetFilters = () => {
  search.value = '';
  status.value = 'all';
  subject.value = 'all';
  category.value = 'all';
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '';
  const date = new Date(dateTime);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
}

// 格式化日期
const formatDate = (dateTime) => {
  if (!dateTime) return '';
  const date = new Date(dateTime);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}

// 格式化时间
const formatTime = (dateTime) => {
  if (!dateTime) return '';
  const date = new Date(dateTime);
  return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
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

// 获取考试列表
const fetchExams = () => {
  loading.value = true;
  get('/api/student/exams/list', { studentId: authStore.user.userId }, 
    (message, data) => {
      exams.value = data || [];
      loading.value = false;
    },
    (message) => {
      ElMessage.error(message || '获取考试列表失败');
      loading.value = false;
    }
  );
}

onMounted(() => {
  fetchExams();
})
</script>

<template>
  <div class="exams-wrapper" v-loading="loading">
    <!-- 顶部工具栏 -->
    <div class="toolbar sticky top-0 z-20 bg-white/80 backdrop-blur-lg px-4 py-3 rounded-xl shadow-md mb-6">
      <div class="flex flex-wrap gap-3 justify-between items-center mb-3">
        <div class="title text-xl font-semibold flex items-center">
          <BookOpen :size="24" class="text-indigo-600 mr-2" />
          考试列表
        </div>
        <div class="flex gap-2 items-center">
          <el-input 
            v-model="search" 
            placeholder="搜索考试..." 
            size="small" 
            clearable 
            class="w-52"
          >
            <template #prefix>
              <Search :size="16" class="text-gray-400" />
            </template>
          </el-input>
          
          <div class="view-toggle flex border rounded overflow-hidden">
            <button 
              @click="viewMode = 'grid'" 
              :class="['px-2 py-1 text-sm', viewMode === 'grid' ? 'bg-indigo-500 text-white' : 'bg-white text-gray-600']"
            >
              <i class="el-icon-menu"></i>
            </button>
            <button 
              @click="viewMode = 'list'" 
              :class="['px-2 py-1 text-sm', viewMode === 'list' ? 'bg-indigo-500 text-white' : 'bg-white text-gray-600']"
            >
              <i class="el-icon-document"></i>
            </button>
          </div>
        </div>
      </div>
      
      <div class="filters flex flex-wrap gap-3 items-center">
        <div class="flex items-center gap-1 text-gray-500 text-sm">
          <Filter :size="14" />
          筛选:
        </div>
        
        <el-select v-model="status" size="small" placeholder="状态" class="w-28">
          <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
        
        <el-select v-model="subject" size="small" placeholder="科目" class="w-32">
          <el-option v-for="opt in subjectOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
        
        <el-select v-model="category" size="small" placeholder="类型" class="w-32">
          <el-option v-for="opt in categoryOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
        
        <el-button type="info" size="small" plain @click="resetFilters" class="ml-auto">
          重置筛选
        </el-button>
      </div>
    </div>

    <!-- 网格视图 -->
    <transition-group v-if="viewMode === 'grid'" name="fade" tag="div" class="exam-grid gap-6">
      <div v-for="ex in filtered" :key="ex.id" class="exam-card group">
        <div class="p-6 flex flex-col gap-4 h-full relative z-10">
          <!-- 卡片头部 -->
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
              <div class="icon w-10 h-10 rounded-xl flex items-center justify-center text-white text-lg shrink-0"
                :class="statusStyle[ex.status].icon">
                📝
              </div>
              <h3 class="font-medium text-lg truncate flex-1">{{ ex.title }}</h3>
            </div>
            <span :class="['badge px-2 py-1 text-xs rounded-full', statusStyle[ex.status].badge]">
              {{ statusStyle[ex.status].text }}
            </span>
          </div>
          
          <!-- 考试信息 -->
          <div class="exam-info space-y-2 text-sm">
            <div class="flex items-center text-gray-600">
              <Tag :size="14" class="mr-2 text-gray-500" />
              <span>{{ ex.courseName }}</span>
            </div>
            <div class="flex items-center text-gray-600">
              <Calendar :size="14" class="mr-2 text-gray-500" />
              <span>{{ formatDate(ex.startTime) }}</span>
              <span v-if="ex.status === 'upcoming'" class="ml-2 text-xs text-indigo-500 font-medium">
                ({{ getRemainingTime(ex.startTime) }})
              </span>
            </div>
            <div class="flex items-center text-gray-600">
              <Clock :size="14" class="mr-2 text-gray-500" />
              <span>{{ formatTime(ex.startTime) }} - {{ formatTime(ex.endTime) }} ({{ ex.duration }}分钟)</span>
            </div>
          </div>
          
          <!-- 标签 -->
          <div class="flex flex-wrap gap-2 mt-1">
            <span class="text-xs px-2 py-1 rounded-full bg-indigo-50 text-indigo-600 border border-indigo-100">
              {{ ex.examType }}
            </span>
            <span v-if="ex.submissionStatus === '已提交'" class="text-xs px-2 py-1 rounded-full bg-blue-50 text-blue-600 border border-blue-100">
              已提交
            </span>
            <span v-if="ex.submissionStatus === '已批改'" class="text-xs px-2 py-1 rounded-full bg-green-50 text-green-600 border border-green-100">
              已批改
            </span>
          </div>
          
          <!-- 分数或操作按钮 -->
          <div class="mt-auto pt-3 border-t border-gray-100 flex justify-between items-center">
            <div v-if="ex.submissionStatus === '已批改'" class="font-semibold text-lg">
              <span :class="{
                'text-green-600': ex.userScore >= 90,
                'text-blue-600': ex.userScore >= 80 && ex.userScore < 90,
                'text-amber-600': ex.userScore >= 60 && ex.userScore < 80,
                'text-red-600': ex.userScore < 60
              }">{{ ex.userScore }}</span>
              <span class="text-gray-500 text-sm">/{{ ex.totalScore || 100 }}</span>
            </div>
            <div v-else class="text-sm text-gray-500">
              {{ ex.questionCount || '-' }}题 / {{ ex.totalScore || 100 }}分
            </div>
            
            <div class="flex gap-2">
              <el-button type="info" size="small" plain @click.stop="viewExamDetails(ex)">
                详情
              </el-button>
              <el-button 
                :type="ex.status === 'ongoing' ? 'danger' : ex.status === 'upcoming' ? 'primary' : 'success'" 
                size="small" 
                @click.stop="goExam(ex)"
              >
                {{ ex.submissionStatus === '已批改' ? '查看成绩' : ex.submissionStatus === '已提交' ? '查看详情' : '进入考试' }}
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </transition-group>

    <!-- 列表视图 -->
    <div v-if="viewMode === 'list'" class="exam-list bg-white rounded-xl shadow-md overflow-hidden">
      <el-table :data="filtered" style="width: 100%" :border="false" class="custom-table">
        <el-table-column label="考试名称" min-width="200">
          <template #default="scope">
            <div class="flex items-center">
              <div class="w-8 h-8 rounded-lg flex items-center justify-center text-white text-sm mr-3"
                :class="statusStyle[scope.row.status].icon">
                📝
              </div>
              <div>
                <div class="font-medium">{{ scope.row.title }}</div>
                <div class="text-xs text-gray-500">{{ scope.row.courseName }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="考试时间" width="180">
          <template #default="scope">
            <div>{{ formatDate(scope.row.startTime) }}</div>
            <div class="text-xs text-gray-500">{{ formatTime(scope.row.startTime) }} - {{ formatTime(scope.row.endTime) }}</div>
          </template>
        </el-table-column>
        
        <el-table-column label="时长" width="80">
          <template #default="scope">
            {{ scope.row.duration }}分钟
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <span :class="['px-2 py-1 text-xs rounded-full', statusStyle[scope.row.status].badge]">
              {{ statusStyle[scope.row.status].text }}
            </span>
          </template>
        </el-table-column>
        
        <el-table-column label="分数" width="100" align="center">
          <template #default="scope">
            <span v-if="scope.row.submissionStatus === '已批改'" :class="{
              'text-green-600 font-medium': scope.row.userScore >= 90,
              'text-blue-600 font-medium': scope.row.userScore >= 80 && scope.row.userScore < 90,
              'text-amber-600 font-medium': scope.row.userScore >= 60 && scope.row.userScore < 80,
              'text-red-600 font-medium': scope.row.userScore < 60
            }">
              {{ scope.row.userScore }}/{{ scope.row.totalScore || 100 }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <div class="flex gap-2">
              <el-button type="info" size="small" plain @click.stop="viewExamDetails(scope.row)">
                详情
              </el-button>
              <el-button 
                :type="scope.row.status === 'ongoing' ? 'danger' : scope.row.status === 'upcoming' ? 'primary' : 'success'" 
                size="small" 
                @click.stop="goExam(scope.row)"
              >
                {{ scope.row.submissionStatus === '已批改' ? '查看成绩' : scope.row.submissionStatus === '已提交' ? '查看详情' : '进入考试' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 空状态 -->
    <div v-if="filtered.length === 0 && !loading" class="empty-state flex flex-col items-center justify-center py-16">
      <div class="w-20 h-20 rounded-full bg-gray-100 flex items-center justify-center mb-4">
        <BookOpen :size="32" class="text-gray-400" />
      </div>
      <h3 class="text-lg font-medium text-gray-600">未找到考试</h3>
      <p class="text-sm text-gray-500 mt-2">尝试调整筛选条件或搜索其他内容</p>
    </div>
  </div>
</template>

<style scoped>
.exams-wrapper { 
  @apply p-0; 
}

.exam-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
}

/* 动画效果 */
.fade-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
} 

.fade-enter-to {
  opacity: 1;
  transform: translateY(0) scale(1);
} 

.fade-enter-active {
  transition: all 0.4s cubic-bezier(0.22, 1.02, 0.38, 0.98);
  transition-delay: calc(var(--el-transition-duration) * var(--el-transition-delay, 0));
}

/* 卡片样式 */
.exam-card { 
  @apply bg-white rounded-xl shadow-md transition-all duration-300 cursor-pointer overflow-hidden relative; 
  min-height: 280px;
}

.exam-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(99,102,241,0.05) 0%, rgba(224,231,255,0.1) 100%);
  opacity: 0;
  transition: opacity .35s;
  z-index: 0;
}

.exam-card:hover::before {
  opacity: 1;
}

.exam-card:hover { 
  transform: translateY(-8px) scale(1.02); 
  box-shadow: 0 20px 30px rgba(0,0,0,0.1);
}

/* 工具栏样式 */
.toolbar {
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255,255,255,0.5);
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

.custom-table :deep(.el-table--border), 
.custom-table :deep(.el-table--border th.el-table__cell),
.custom-table :deep(.el-table--border td.el-table__cell) {
  border-color: #f1f5f9;
}

/* 响应式调整 */
@media (max-width: 640px) {
  .exam-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  }
}
</style> 