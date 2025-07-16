<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import ExamStatistics from './components/ExamStatistics.vue';
import QuestionDetail from './components/QuestionDetail.vue';

const route = useRoute();
const router = useRouter();

const examId = ref(route.params.id);
const loading = ref(true);
const exam = ref(null);
const students = ref([]);
const activeTab = ref('overview');
const sortBy = ref('name');
const sortOrder = ref('asc');

// 模拟的标签页
const tabs = [
  { id: 'overview', name: '概览' },
  { id: 'students', name: '学生成绩' },
  { id: 'questions', name: '题目分析' }
];

// 获取考试数据
const fetchExamData = async () => {
  loading.value = true;
  
  try {
    // 模拟API延迟
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    // 模拟考试数据
    exam.value = {
      id: examId.value,
      title: '第一次月考：计算机基础',
      course: '计算机导论',
      status: 'completed',
      startTime: '2023-10-15T10:00:00',
      endTime: '2023-10-15T11:30:00',
      duration: 90,
      questionCount: 5,
      passingScore: 60,
      totalScore: 100,
      averageScore: 76.5,
      highestScore: 98,
      lowestScore: 45,
      passRate: 85,
      standardDeviation: 12.3,
      scoreDistribution: {
        '0-59': 9,
        '60-69': 12,
        '70-79': 15,
        '80-89': 18,
        '90-100': 6
      },
      questions: [
        {
          id: '1',
          number: 1,
          type: 'single',
          content: '以下哪项不是计算机的基本组成部分？',
          options: [
            { id: 1, content: 'CPU' },
            { id: 2, content: '内存' },
            { id: 3, content: '操作系统' },
            { id: 4, content: '输入/输出设备' }
          ],
          answer: [3],
          analysis: '计算机的基本组成部分包括硬件（CPU、内存、输入/输出设备等）和软件。操作系统是软件的一种，不是基本组成部分。',
          score: 20,
          difficulty: 2,
          correctRate: 75,
          discrimination: 0.65
        },
        {
          id: '2',
          number: 2,
          type: 'multiple',
          content: '以下哪些是操作系统的功能？',
          options: [
            { id: 1, content: '资源管理' },
            { id: 2, content: '程序执行' },
            { id: 3, content: '设备驱动' },
            { id: 4, content: '网页设计' }
          ],
          answer: [1, 2, 3],
          analysis: '操作系统的主要功能包括资源管理、程序执行和设备驱动等。网页设计是应用软件的功能，不属于操作系统。',
          score: 20,
          difficulty: 3,
          correctRate: 60,
          discrimination: 0.72
        },
        {
          id: '3',
          number: 3,
          type: 'true_false',
          content: 'RAM是非易失性存储器，断电后数据仍然保持。',
          options: [
            { id: 1, content: '正确' },
            { id: 2, content: '错误' }
          ],
          answer: [2],
          analysis: 'RAM（随机存取存储器）是易失性存储器，断电后数据会丢失。非易失性存储器是ROM、硬盘等。',
          score: 20,
          difficulty: 1,
          correctRate: 85,
          discrimination: 0.45
        },
        {
          id: '4',
          number: 4,
          type: 'fill',
          content: '计算机中用于存储计算过程中产生的临时数据的存储器是________。',
          options: [],
          answer: ['内存', 'RAM', '主存'],
          analysis: '计算机中用于存储计算过程中产生的临时数据的存储器是内存（RAM，随机存取存储器，也称为主存）。',
          score: 20,
          difficulty: 2,
          correctRate: 92,
          discrimination: 0.38
        },
        {
          id: '5',
          number: 5,
          type: 'short',
          content: '简述计算机中CPU的主要功能。',
          options: [],
          answer: ['CPU（中央处理器）是计算机的核心部件，主要功能包括执行指令、运算控制、数据处理等。它负责接收输入设备输入的数据，对其进行加工处理，然后将结果输出到输出设备。CPU的性能直接影响计算机的整体性能。'],
          analysis: 'CPU（中央处理器）是计算机的核心，主要功能包括：\n1. 执行指令：从内存中取出指令并执行\n2. 数据处理：进行算术和逻辑运算\n3. 控制系统：协调计算机各部分工作\n4. 与内存交互：读取和存储数据',
          score: 20,
          difficulty: 4,
          correctRate: 68,
          discrimination: 0.85
        }
      ],
      questionAnalysis: [
        {
          number: 1,
          type: '单选题',
          difficulty: 2,
          correctRate: 75,
          discrimination: 0.65
        },
        {
          number: 2,
          type: '多选题',
          difficulty: 3,
          correctRate: 60,
          discrimination: 0.72
        },
        {
          number: 3,
          type: '判断题',
          difficulty: 1,
          correctRate: 85,
          discrimination: 0.45
        },
        {
          number: 4,
          type: '填空题',
          difficulty: 2,
          correctRate: 92,
          discrimination: 0.38
        },
        {
          number: 5,
          type: '简答题',
          difficulty: 4,
          correctRate: 68,
          discrimination: 0.85
        }
      ]
    };
    
    // 模拟学生成绩数据
    students.value = Array(60).fill().map((_, index) => {
      const id = 10001 + index;
      const score = Math.floor(Math.random() * 56) + 45; // 45-100之间的随机分数
      
      return {
        id: `S${id}`,
        name: `学生${id}`,
        class: `计科${Math.floor(Math.random() * 4) + 1}班`,
        score: score,
        status: score >= exam.value.passingScore ? 'pass' : 'fail',
        submitTime: new Date(2023, 9, 15, 10 + Math.floor(Math.random() * 90) / 60, Math.floor(Math.random() * 60)).toISOString(),
        duration: Math.floor(Math.random() * exam.value.duration * 0.8) + Math.floor(exam.value.duration * 0.2)
      };
    });
    
  } catch (error) {
    console.error('加载考试数据失败', error);
    // 处理错误
  } finally {
    loading.value = false;
  }
};

// 导出成绩
const exportScores = () => {
  // 实际应用中应实现导出功能
  alert('成绩导出功能尚未实现');
};

// 返回考试列表
const backToList = () => {
  router.push('/teacher/exam-assessment-module/exams');
};

// 切换标签
const changeTab = (tabId) => {
  activeTab.value = tabId;
};

// 排序学生
const sortStudents = (key) => {
  if (sortBy.value === key) {
    // 如果已经按此键排序，则反转排序方向
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc';
  } else {
    // 否则，按新键排序，默认升序
    sortBy.value = key;
    sortOrder.value = 'asc';
  }
};

// 计算排序后的学生列表
const sortedStudents = computed(() => {
  return [...students.value].sort((a, b) => {
    let comparison = 0;
    
    switch (sortBy.value) {
      case 'name':
        comparison = a.name.localeCompare(b.name);
        break;
      case 'class':
        comparison = a.class.localeCompare(b.class);
        break;
      case 'score':
        comparison = a.score - b.score;
        break;
      case 'duration':
        comparison = a.duration - b.duration;
        break;
      case 'submitTime':
        comparison = new Date(a.submitTime) - new Date(b.submitTime);
        break;
      default:
        comparison = a.name.localeCompare(b.name);
    }
    
    return sortOrder.value === 'asc' ? comparison : -comparison;
  });
});

// 初始化
onMounted(() => {
  fetchExamData();
});
</script>

<template>
  <div class="exam-result">
    <div class="page-header">
      <button class="back-btn" @click="backToList">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
        </svg>
        返回考试列表
      </button>
      
      <button class="export-btn" @click="exportScores">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
        </svg>
        导出成绩
      </button>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>加载考试数据中...</p>
    </div>
    
    <template v-else>
      <h2 class="exam-title">{{ exam.title }}</h2>
      <p class="exam-course">{{ exam.course }}</p>
      
      <!-- 标签页 -->
      <div class="tabs">
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
      
      <!-- 概览 -->
      <div v-if="activeTab === 'overview'" class="tab-content">
        <ExamStatistics :examData="exam" />
      </div>
      
      <!-- 学生成绩 -->
      <div v-else-if="activeTab === 'students'" class="tab-content">
        <div class="students-table">
          <table>
            <thead>
              <tr>
                <th @click="sortStudents('name')">
                  姓名
                  <span v-if="sortBy === 'name'" class="sort-icon">
                    {{ sortOrder === 'asc' ? '↑' : '↓' }}
                  </span>
                </th>
                <th @click="sortStudents('class')">
                  班级
                  <span v-if="sortBy === 'class'" class="sort-icon">
                    {{ sortOrder === 'asc' ? '↑' : '↓' }}
                  </span>
                </th>
                <th @click="sortStudents('score')">
                  分数
                  <span v-if="sortBy === 'score'" class="sort-icon">
                    {{ sortOrder === 'asc' ? '↑' : '↓' }}
                  </span>
                </th>
                <th>状态</th>
                <th @click="sortStudents('duration')">
                  用时
                  <span v-if="sortBy === 'duration'" class="sort-icon">
                    {{ sortOrder === 'asc' ? '↑' : '↓' }}
                  </span>
                </th>
                <th @click="sortStudents('submitTime')">
                  提交时间
                  <span v-if="sortBy === 'submitTime'" class="sort-icon">
                    {{ sortOrder === 'asc' ? '↑' : '↓' }}
                  </span>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="student in sortedStudents" :key="student.id">
                <td>{{ student.name }}</td>
                <td>{{ student.class }}</td>
                <td class="score">{{ student.score }}</td>
                <td>
                  <span 
                    class="status-badge"
                    :class="{ 'pass': student.status === 'pass', 'fail': student.status === 'fail' }"
                  >
                    {{ student.status === 'pass' ? '及格' : '不及格' }}
                  </span>
                </td>
                <td>{{ student.duration }}分钟</td>
                <td>{{ new Date(student.submitTime).toLocaleString() }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      
      <!-- 题目分析 -->
      <div v-else-if="activeTab === 'questions'" class="tab-content">
        <div class="questions-analysis">
          <div v-for="question in exam.questions" :key="question.id" class="question-item">
            <div class="question-header">
              <div class="question-meta">
                <span class="question-number">题目 {{ question.number }}</span>
                <span class="question-score">{{ question.score }}分</span>
              </div>
              
              <div class="question-stats">
                <div class="stat-item">
                  <span class="stat-label">正确率：</span>
                  <span class="stat-value">{{ question.correctRate }}%</span>
                </div>
                
                <div class="stat-item">
                  <span class="stat-label">区分度：</span>
                  <span class="stat-value">{{ question.discrimination }}</span>
                </div>
              </div>
            </div>
            
            <QuestionDetail 
              :question="question" 
              :showAnswer="true"
              :showAnalysis="true"
            />
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.exam-result {
  @apply max-w-7xl mx-auto px-4;
}

.page-header {
  @apply flex justify-between mb-6;
}

.back-btn {
  @apply flex items-center px-3 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors;
}

.export-btn {
  @apply flex items-center px-3 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors;
}

.loading-state {
  @apply flex flex-col items-center justify-center py-12;
}

.loading-spinner {
  @apply w-10 h-10 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin mb-4;
}

.exam-title {
  @apply text-2xl font-bold text-gray-800 mb-1;
}

.exam-course {
  @apply text-lg text-gray-600 mb-6;
}

.tabs {
  @apply flex border-b border-gray-200 mb-6 overflow-x-auto;
}

.tab-item {
  @apply px-4 py-2 text-gray-600 cursor-pointer hover:text-gray-800 transition-colors border-b-2 border-transparent whitespace-nowrap;
}

.tab-item.active {
  @apply text-blue-600 border-blue-600 font-medium;
}

.tab-content {
  @apply mb-8;
}

.students-table {
  @apply bg-white rounded-xl shadow-sm overflow-x-auto;
}

.students-table table {
  @apply w-full;
}

.students-table th {
  @apply px-4 py-3 bg-gray-50 text-left text-sm font-medium text-gray-700 cursor-pointer;
}

.students-table td {
  @apply px-4 py-3 border-t border-gray-100 text-sm;
}

.sort-icon {
  @apply ml-1;
}

.score {
  @apply font-medium;
}

.status-badge {
  @apply px-2 py-0.5 rounded-full text-xs font-medium;
}

.status-badge.pass {
  @apply bg-green-100 text-green-800;
}

.status-badge.fail {
  @apply bg-red-100 text-red-800;
}

.questions-analysis {
  @apply space-y-6;
}

.question-item {
  @apply bg-white rounded-xl p-6 shadow-sm;
}

.question-header {
  @apply flex justify-between items-center mb-4;
}

.question-meta {
  @apply flex items-center gap-4;
}

.question-number {
  @apply font-medium;
}

.question-score {
  @apply text-gray-600;
}

.question-stats {
  @apply flex gap-6;
}

.stat-item {
  @apply flex items-center;
}

.stat-label {
  @apply text-gray-500;
}

.stat-value {
  @apply font-medium ml-1;
}
</style>