<script setup>
import { ref, computed } from 'vue';

const props = defineProps({
  exams: {
    type: Array,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['view-exam', 'edit-exam', 'delete-exam', 'duplicate-exam']);

// 搜索关键字
const searchKeyword = ref('');

// 过滤后的考试列表
const filteredExams = computed(() => {
  if (!searchKeyword.value) return props.exams;
  
  const keyword = searchKeyword.value.toLowerCase();
  return props.exams.filter(exam => 
    exam.title.toLowerCase().includes(keyword) || 
    exam.course.toLowerCase().includes(keyword)
  );
});

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN', { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 获取状态标签样式
const getStatusClass = (status) => {
  switch (status) {
    case 'upcoming': return 'status-upcoming';
    case 'ongoing': return 'status-ongoing';
    case 'completed': return 'status-completed';
    case 'draft': return 'status-draft';
    default: return 'status-draft';
  }
};

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'upcoming': return '即将开始';
    case 'ongoing': return '进行中';
    case 'completed': return '已结束';
    case 'draft': return '草稿';
    default: return '草稿';
  }
};

// 处理查看考试详情
const handleViewExam = (exam) => {
  emit('view-exam', exam);
};

// 处理编辑考试
const handleEditExam = (exam, event) => {
  event.stopPropagation();
  emit('edit-exam', exam);
};

// 处理删除考试
const handleDeleteExam = (exam, event) => {
  event.stopPropagation();
  emit('delete-exam', exam);
};

// 处理复制考试
const handleDuplicateExam = (exam, event) => {
  event.stopPropagation();
  emit('duplicate-exam', exam);
};
</script>

<template>
  <div class="exam-list">
    <!-- 搜索区域 -->
    <div class="search-bar">
      <div class="search-input">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 search-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
        </svg>
        <input 
          v-model="searchKeyword" 
          type="text" 
          placeholder="搜索考试名称或课程..." 
        />
        <button 
          v-if="searchKeyword" 
          class="clear-search" 
          @click="searchKeyword = ''"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>加载考试列表中...</p>
    </div>
    
    <!-- 考试列表 -->
    <div v-else-if="filteredExams.length" class="exams-grid">
      <div 
        v-for="exam in filteredExams" 
        :key="exam.id"
        class="exam-card"
        @click="handleViewExam(exam)"
      >
        <div class="exam-header">
          <span :class="['status-badge', getStatusClass(exam.status)]">
            {{ getStatusText(exam.status) }}
          </span>
          <div class="exam-actions">
            <button 
              class="action-btn edit-btn" 
              title="编辑"
              @click="handleEditExam(exam, $event)"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
            </button>
            <button 
              class="action-btn duplicate-btn" 
              title="复制"
              @click="handleDuplicateExam(exam, $event)"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7v8a2 2 0 002 2h6M8 7V5a2 2 0 012-2h4.586a1 1 0 01.707.293l4.414 4.414a1 1 0 01.293.707V15a2 2 0 01-2 2h-2M8 7H6a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2v-2" />
              </svg>
            </button>
            <button 
              class="action-btn delete-btn" 
              title="删除"
              @click="handleDeleteExam(exam, $event)"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          </div>
        </div>
        
        <h3 class="exam-title">{{ exam.title }}</h3>
        <div class="exam-course">{{ exam.course }}</div>
        
        <div class="exam-info">
          <div class="info-item">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span>{{ exam.duration }}分钟</span>
          </div>
          
          <div class="info-item">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z" />
            </svg>
            <span>{{ exam.questionCount }}题</span>
          </div>
          
          <div class="info-item">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
            </svg>
            <span>{{ exam.studentCount || 0 }}人</span>
          </div>
        </div>
        
        <div class="exam-date">
          <div v-if="exam.startTime" class="date-info">
            <div class="date-label">开始时间</div>
            <div class="date-value">{{ formatDate(exam.startTime) }}</div>
          </div>
          
          <div v-if="exam.endTime" class="date-info">
            <div class="date-label">结束时间</div>
            <div class="date-value">{{ formatDate(exam.endTime) }}</div>
          </div>
        </div>
        
        <div v-if="exam.status === 'completed'" class="exam-results">
          <div class="result-item">
            <div class="result-label">平均分</div>
            <div class="result-value">{{ exam.averageScore || '—' }}</div>
          </div>
          
          <div class="result-item">
            <div class="result-label">最高分</div>
            <div class="result-value">{{ exam.highestScore || '—' }}</div>
          </div>
          
          <div class="result-item">
            <div class="result-label">最低分</div>
            <div class="result-value">{{ exam.lowestScore || '—' }}</div>
          </div>
          
          <div class="result-item">
            <div class="result-label">通过率</div>
            <div class="result-value">{{ exam.passRate || '—' }}</div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 空状态 -->
    <div v-else class="empty-state">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
      </svg>
      <h3>{{ searchKeyword ? '未找到匹配的考试' : '暂无考试' }}</h3>
      <p>{{ searchKeyword ? '尝试其他搜索词或清除搜索' : '点击"新建考试"按钮创建您的第一个考试' }}</p>
    </div>
  </div>
</template>

<style scoped>
.exam-list {
  @apply w-full;
}

.search-bar {
  @apply mb-6;
}

.search-input {
  @apply relative;
}

.search-input input {
  @apply w-full pl-10 pr-10 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all duration-200;
}

.search-icon {
  @apply absolute left-3 top-1/2 -translate-y-1/2 text-gray-400;
}

.clear-search {
  @apply absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600;
}

.loading-state {
  @apply flex flex-col items-center justify-center py-12;
}

.loading-spinner {
  @apply w-10 h-10 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin mb-4;
}

.exams-grid {
  @apply grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6;
}

.exam-card {
  @apply bg-white rounded-xl border border-gray-200 shadow-sm p-5 hover:shadow-md transition-all duration-300 cursor-pointer;
}

.exam-header {
  @apply flex justify-between items-center mb-3;
}

.status-badge {
  @apply text-xs px-2 py-1 rounded-full font-medium;
}

.status-upcoming {
  @apply bg-blue-100 text-blue-800;
}

.status-ongoing {
  @apply bg-green-100 text-green-800;
}

.status-completed {
  @apply bg-gray-100 text-gray-800;
}

.status-draft {
  @apply bg-amber-100 text-amber-800;
}

.exam-actions {
  @apply flex space-x-1;
}

.action-btn {
  @apply p-1.5 rounded-full bg-white text-gray-500 hover:bg-gray-100 transition-colors duration-200;
}

.delete-btn:hover {
  @apply text-red-600 bg-red-50;
}

.edit-btn:hover {
  @apply text-blue-600 bg-blue-50;
}

.duplicate-btn:hover {
  @apply text-purple-600 bg-purple-50;
}

.exam-title {
  @apply text-lg font-medium text-gray-800 mb-1;
}

.exam-course {
  @apply text-sm text-gray-500 mb-3;
}

.exam-info {
  @apply flex flex-wrap items-center gap-4 text-sm text-gray-600 mb-3;
}

.info-item {
  @apply flex items-center;
}

.info-item svg {
  @apply mr-1;
}

.exam-date {
  @apply flex flex-col space-y-2 border-t border-gray-100 pt-3 mb-3;
}

.date-info {
  @apply flex justify-between items-center text-sm;
}

.date-label {
  @apply text-gray-500;
}

.date-value {
  @apply text-gray-700 font-medium;
}

.exam-results {
  @apply grid grid-cols-2 gap-3 border-t border-gray-100 pt-3;
}

.result-item {
  @apply flex flex-col;
}

.result-label {
  @apply text-xs text-gray-500;
}

.result-value {
  @apply text-sm font-medium;
}

.empty-state {
  @apply flex flex-col items-center justify-center text-center py-16;
}

.empty-state svg {
  @apply text-gray-300 mb-4;
}

.empty-state h3 {
  @apply text-lg font-medium text-gray-700 mb-2;
}

.empty-state p {
  @apply text-gray-500 max-w-md;
}
</style>