<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import ExamList from './components/ExamList.vue';
import { getTeacherExams, deleteExam } from '@/api/examManagement';
import { useAuthStore } from '@/stores/counter';

const router = useRouter();
const authStore = useAuthStore();
const uid = computed(() => authStore.user?.userId || '1');

const loading = ref(true);
const exams = ref([]);
const activeTab = ref('all');
const searchKeyword = ref('');

// 考试状态过滤标签
const tabs = [
  { id: 'all', name: '全部' },
  { id: '即将开始', name: '即将开始' },
  { id: '进行中', name: '进行中' },
  { id: '已结束', name: '已结束' },
  { id: '草稿', name: '草稿' }
];

// 获取考试数据
const fetchExams = () => {
  loading.value = true;
  const status = activeTab.value === 'all' ? null : activeTab.value;
  const keyword = searchKeyword.value.trim() || null;

  getTeacherExams(
    uid.value,
    status,
    keyword,
    1, // page
    50, // size - 获取更多数据
    (message, data) => {
      console.log('获取考试列表成功:', data);
      exams.value = data || [];
      loading.value = false;
    },
    (message) => {
      console.error('获取考试列表失败:', message);
      exams.value = [];
      loading.value = false;
    }
  );
};

// 按标签过滤考试（现在由后端处理，前端直接返回）
const filteredExams = computed(() => {
  return exams.value;
});

// 切换标签
const changeTab = (tabId) => {
  activeTab.value = tabId;
  fetchExams(); // 重新获取数据
};

// 搜索考试
const searchExams = () => {
  fetchExams();
};

// 创建新考试
const createExam = () => {
  router.push('/teacher/exam-assessment-module/exam/new');
};

// 查看考试详情
const viewExam = (exam) => {
  if (exam.status === 'completed') {
    // 已完成的考试查看统计结果
    router.push(`/teacher/exam-assessment-module/result/${exam.id}`);
  } else {
    // 其他状态查看详情（暂时跳转到编辑页面）
    router.push(`/teacher/exam-assessment-module/exam-editor/${exam.id}`);
  }
};

// 编辑考试
const editExam = (exam) => {
  router.push(`/teacher/exam-assessment-module/exam-editor/${exam.id}`);
};

// 删除考试
const deleteExamHandler = (exam) => {
  if (confirm(`确定要删除考试"${exam.title}"吗？此操作不可撤销。`)) {
    deleteExam(
      exam.id,
      uid.value,
      (message) => {
        console.log('删除考试成功:', message);
        // 从列表中移除
        exams.value = exams.value.filter(item => item.id !== exam.id);
        alert('删除考试成功！');
      },
      (message) => {
        console.error('删除考试失败:', message);
        alert('删除考试失败: ' + message);
      }
    );
  }
};

// 复制考试
const duplicateExam = async (exam) => {
  try {
    // 模拟API请求
    await new Promise(resolve => setTimeout(resolve, 500));
    
    // 创建新考试对象（复制）
    const newExam = {
      ...exam,
      id: `new_${Date.now()}`, // 临时ID
      title: `${exam.title} (复制)`,
      status: 'draft',
      startTime: '',
      endTime: '',
      studentCount: 0,
      averageScore: null,
      highestScore: null,
      lowestScore: null,
      passRate: null
    };
    
    // 添加到列表
    exams.value.unshift(newExam);
  } catch (error) {
    console.error('复制失败', error);
    // 处理错误
  }
};

// 获取考试数据
onMounted(() => {
  fetchExams();
});
</script>

<template>
  <div class="exams-page">
    <div class="page-header">
      <h2 class="page-title">考试管理</h2>
      <div class="header-actions">
        <div class="search-box">
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索考试名称或课程..."
            @keyup.enter="searchExams"
            class="search-input"
          />
          <button @click="searchExams" class="search-btn">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </button>
        </div>
        <button class="create-btn" @click="createExam">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
          </svg>
          新建考试
        </button>
      </div>
    </div>
    
    <div class="filter-tabs">
      <div 
        v-for="tab in tabs" 
        :key="tab.id"
        class="tab-item"
        :class="{ 'active': activeTab === tab.id }"
        @click="changeTab(tab.id)"
      >
        {{ tab.name }}
      </div>
    </div>
    
    <ExamList
      :exams="filteredExams"
      :loading="loading"
      @view-exam="viewExam"
      @edit-exam="editExam"
      @delete-exam="deleteExamHandler"
      @duplicate-exam="duplicateExam"
    />
  </div>
</template>

<style scoped>
.exams-page {
  @apply max-w-7xl mx-auto px-4;
}

.page-header {
  @apply flex justify-between items-center mb-6;
}

.page-title {
  @apply text-2xl font-bold text-gray-800;
}

.header-actions {
  @apply flex items-center gap-4;
}

.search-box {
  @apply flex items-center border border-gray-300 rounded-lg overflow-hidden;
}

.search-input {
  @apply px-3 py-2 border-none outline-none min-w-64;
}

.search-btn {
  @apply px-3 py-2 bg-gray-50 hover:bg-gray-100 transition-colors;
}

.create-btn {
  @apply flex items-center px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors;
}

.filter-tabs {
  @apply flex flex-wrap gap-2 mb-6;
}

.tab-item {
  @apply px-4 py-2 rounded-lg text-gray-600 bg-white border border-gray-200 cursor-pointer hover:bg-gray-50 transition-colors;
}

.tab-item.active {
  @apply bg-blue-600 text-white border-blue-600;
}

@media (max-width: 640px) {
  .page-header {
    @apply flex-col items-start gap-4;
  }
  
  .create-btn {
    @apply w-full justify-center;
  }
  
  .filter-tabs {
    @apply overflow-x-auto pb-2 flex-nowrap;
    scrollbar-width: none;
    -ms-overflow-style: none;
  }
  
  .filter-tabs::-webkit-scrollbar {
    display: none;
  }
}
</style>