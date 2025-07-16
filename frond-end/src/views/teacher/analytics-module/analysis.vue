<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import StudentProgressChart from './components/StudentProgressChart.vue';
import ClassPerformanceCard from './components/ClassPerformanceCard.vue';
import CompetencyRadarChart from './components/CompetencyRadarChart.vue';

// 状态变量
const loading = ref(true);
const activeTab = ref('performance');
const selectedClass = ref('');
const selectedExam = ref('');
const selectedStudent = ref('');
const selectedTimePeriod = ref('semester');

// 模拟的标签页
const tabs = [
  { id: 'performance', name: '班级表现' },
  { id: 'students', name: '学生进步' },
  { id: 'competency', name: '能力分析' }
];

// 模拟的班级数据
const classes = ref([]);
// 模拟的考试数据
const exams = ref([]);
// 模拟的学生数据
const students = ref([]);

// 班级表现数据
const classPerformanceData = reactive({
  classes: []
});

// 学生进步数据
const studentProgressData = reactive({
  students: []
});

// 能力分析数据
const competencyData = reactive({
  competencies: [
    { id: 1, name: '知识掌握' },
    { id: 2, name: '问题解决' },
    { id: 3, name: '创新思维' },
    { id: 4, name: '团队合作' },
    { id: 5, name: '沟通表达' },
    { id: 6, name: '学习能力' }
  ],
  classAverage: [78, 65, 60, 82, 70, 75],
  gradeAverage: [72, 68, 58, 75, 68, 70]
});

// 时间周期选项
const timePeriods = [
  { id: 'week', name: '本周' },
  { id: 'month', name: '本月' },
  { id: 'semester', name: '本学期' }
];

// 初始化数据
onMounted(async () => {
  await Promise.all([
    fetchClasses(),
    fetchExams(),
    fetchStudents()
  ]);
  
  loading.value = false;
  
  // 默认选择第一个班级和考试
  if (classes.value.length > 0) {
    selectedClass.value = classes.value[0].id;
  }
  
  if (exams.value.length > 0) {
    selectedExam.value = exams.value[0].id;
  }
  
  if (students.value.length > 0) {
    selectedStudent.value = students.value[0].id;
  }
  
  // 加载初始数据
  loadClassPerformance();
  loadStudentProgress();
});

// 获取班级数据
const fetchClasses = async () => {
  // 模拟API延迟
  await new Promise(resolve => setTimeout(resolve, 300));
  
  classes.value = [
    { id: '1', name: '高一(1)班' },
    { id: '2', name: '高一(2)班' },
    { id: '3', name: '高一(3)班' },
    { id: '4', name: '高一(4)班' }
  ];
};

// 获取考试数据
const fetchExams = async () => {
  // 模拟API延迟
  await new Promise(resolve => setTimeout(resolve, 300));
  
  exams.value = [
    { id: '1', name: '期中考试' },
    { id: '2', name: '期末考试' },
    { id: '3', name: '单元测试1' },
    { id: '4', name: '单元测试2' }
  ];
};

// 获取学生数据
const fetchStudents = async () => {
  // 模拟API延迟
  await new Promise(resolve => setTimeout(resolve, 300));
  
  students.value = [
    { id: '1', name: '张三', class: '高一(1)班' },
    { id: '2', name: '李四', class: '高一(1)班' },
    { id: '3', name: '王五', class: '高一(2)班' },
    { id: '4', name: '赵六', class: '高一(2)班' },
    { id: '5', name: '钱七', class: '高一(3)班' }
  ];
};

// 加载班级表现数据
const loadClassPerformance = async () => {
  // 模拟API延迟
  await new Promise(resolve => setTimeout(resolve, 500));
  
  // 模拟数据
  classPerformanceData.classes = [
    {
      id: '1',
      name: '高一(1)班',
      studentCount: 45,
      rank: 1,
      previousRank: 2,
      averageScore: 86.5,
      previousAverageScore: 82.3,
      passRate: 95,
      previousPassRate: 92,
      excellentRate: 42,
      previousExcellentRate: 36,
      scoreDistribution: {
        '90-100': 12,
        '80-89': 18,
        '70-79': 8,
        '60-69': 5,
        '0-59': 2
      }
    },
    {
      id: '2',
      name: '高一(2)班',
      studentCount: 46,
      rank: 2,
      previousRank: 1,
      averageScore: 83.2,
      previousAverageScore: 85.1,
      passRate: 93,
      previousPassRate: 96,
      excellentRate: 37,
      previousExcellentRate: 42,
      scoreDistribution: {
        '90-100': 10,
        '80-89': 15,
        '70-79': 12,
        '60-69': 6,
        '0-59': 3
      }
    },
    {
      id: '3',
      name: '高一(3)班',
      studentCount: 44,
      rank: 3,
      previousRank: 3,
      averageScore: 79.8,
      previousAverageScore: 77.5,
      passRate: 89,
      previousPassRate: 86,
      excellentRate: 30,
      previousExcellentRate: 27,
      scoreDistribution: {
        '90-100': 8,
        '80-89': 14,
        '70-79': 10,
        '60-69': 7,
        '0-59': 5
      }
    },
    {
      id: '4',
      name: '高一(4)班',
      studentCount: 45,
      rank: 4,
      previousRank: 4,
      averageScore: 75.6,
      previousAverageScore: 73.2,
      passRate: 84,
      previousPassRate: 82,
      excellentRate: 22,
      previousExcellentRate: 20,
      scoreDistribution: {
        '90-100': 6,
        '80-89': 10,
        '70-79': 15,
        '60-69': 7,
        '0-59': 7
      }
    }
  ];
};

// 加载学生进步数据
const loadStudentProgress = async () => {
  // 模拟API延迟
  await new Promise(resolve => setTimeout(resolve, 500));
  
  // 模拟数据
  const generateRandomScores = () => {
    const baseScore = 60 + Math.random() * 20;
    return Array(6).fill().map((_, i) => {
      return Math.min(100, Math.max(0, baseScore + (Math.random() - 0.3) * 20 + i * 2)).toFixed(1);
    });
  };
  
  studentProgressData.students = [
    {
      id: '1',
      name: '张三',
      class: '高一(1)班',
      scores: generateRandomScores()
    },
    {
      id: '2',
      name: '李四',
      class: '高一(1)班',
      scores: generateRandomScores()
    },
    {
      id: '3',
      name: '王五',
      class: '高一(2)班',
      scores: generateRandomScores()
    },
    {
      id: '4',
      name: '赵六',
      class: '高一(2)班',
      scores: generateRandomScores()
    },
    {
      id: '5',
      name: '钱七',
      class: '高一(3)班',
      scores: generateRandomScores()
    }
  ];
};

// 切换标签
const changeTab = (tabId) => {
  activeTab.value = tabId;
};

// 班级变更
const handleClassChange = () => {
  // 在实际应用中，此处应该重新加载相应班级的数据
  console.log('选择班级：', selectedClass.value);
};

// 考试变更
const handleExamChange = () => {
  // 在实际应用中，此处应该重新加载相应考试的数据
  console.log('选择考试：', selectedExam.value);
};

// 学生变更
const handleStudentChange = () => {
  // 在实际应用中，此处应该重新加载相应学生的数据
  console.log('选择学生：', selectedStudent.value);
};

// 时间周期变更
const handleTimePeriodChange = () => {
  // 在实际应用中，此处应该根据所选时间周期重新加载数据
  console.log('选择时间周期：', selectedTimePeriod.value);
};

// 过滤显示的学生进步数据
const filteredStudentProgressData = computed(() => {
  if (!selectedStudent.value) {
    return studentProgressData.students;
  }
  
  // 过滤选中的学生
  const student = studentProgressData.students.find(s => s.id === selectedStudent.value);
  
  return student ? [student] : [];
});
</script>

<template>
  <div class="analysis-page">
    <div class="page-header">
      <h2 class="page-title">学情分析</h2>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>加载分析数据中...</p>
    </div>
    
    <template v-else>
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
      
      <!-- 班级表现 -->
      <div v-if="activeTab === 'performance'" class="tab-content">
        <div class="filters">
          <div class="filter-group">
            <label>选择考试</label>
            <select v-model="selectedExam" @change="handleExamChange">
              <option v-for="exam in exams" :key="exam.id" :value="exam.id">
                {{ exam.name }}
              </option>
            </select>
          </div>
        </div>
        
        <div class="class-performance-grid">
          <ClassPerformanceCard 
            v-for="classData in classPerformanceData.classes" 
            :key="classData.id"
            :classData="classData"
            :compareWithPrevious="true"
          />
        </div>
      </div>
      
      <!-- 学生进步 -->
      <div v-else-if="activeTab === 'students'" class="tab-content">
        <div class="filters">
          <div class="filter-group">
            <label>选择班级</label>
            <select v-model="selectedClass" @change="handleClassChange">
              <option v-for="classItem in classes" :key="classItem.id" :value="classItem.id">
                {{ classItem.name }}
              </option>
            </select>
          </div>
          
          <div class="filter-group">
            <label>选择学生</label>
            <select v-model="selectedStudent" @change="handleStudentChange">
              <option value="">所有学生</option>
              <option v-for="student in students" :key="student.id" :value="student.id">
                {{ student.name }}
              </option>
            </select>
          </div>
          
          <div class="filter-group">
            <label>时间周期</label>
            <select v-model="selectedTimePeriod" @change="handleTimePeriodChange">
              <option v-for="period in timePeriods" :key="period.id" :value="period.id">
                {{ period.name }}
              </option>
            </select>
          </div>
        </div>
        
        <div class="progress-chart-container">
          <div class="chart-header">
            <h3 class="chart-title">学生成绩趋势</h3>
          </div>
          
          <div class="chart">
            <StudentProgressChart 
              :studentData="filteredStudentProgressData"
              :period="selectedTimePeriod"
            />
          </div>
        </div>
      </div>
      
      <!-- 能力分析 -->
      <div v-else-if="activeTab === 'competency'" class="tab-content">
        <div class="filters">
          <div class="filter-group">
            <label>选择班级</label>
            <select v-model="selectedClass" @change="handleClassChange">
              <option v-for="classItem in classes" :key="classItem.id" :value="classItem.id">
                {{ classItem.name }}
              </option>
            </select>
          </div>
          
          <div class="filter-group">
            <label>选择考试</label>
            <select v-model="selectedExam" @change="handleExamChange">
              <option v-for="exam in exams" :key="exam.id" :value="exam.id">
                {{ exam.name }}
              </option>
            </select>
          </div>
        </div>
        
        <div class="competency-chart-container">
          <div class="chart-header">
            <h3 class="chart-title">学科能力雷达图</h3>
          </div>
          
          <div class="chart">
            <CompetencyRadarChart 
              :competencies="competencyData.competencies"
              :classAverage="competencyData.classAverage"
              :gradeAverage="competencyData.gradeAverage"
            />
          </div>
          
          <div class="competency-legend">
            <div v-for="(comp, index) in competencyData.competencies" :key="comp.id" class="legend-item">
              <div class="legend-name">{{ comp.name }}</div>
              <div class="legend-values">
                <div class="legend-value">
                  <span class="value-label">班级均值:</span>
                  <span class="value-number class-value">{{ competencyData.classAverage[index] }}</span>
                </div>
                
                <div class="legend-value">
                  <span class="value-label">年级均值:</span>
                  <span class="value-number grade-value">{{ competencyData.gradeAverage[index] }}</span>
                </div>
                
                <div class="legend-value">
                  <span class="value-label">差异:</span>
                  <span 
                    class="value-number" 
                    :class="{
                      'positive': competencyData.classAverage[index] > competencyData.gradeAverage[index],
                      'negative': competencyData.classAverage[index] < competencyData.gradeAverage[index]
                    }"
                  >
                    {{ (competencyData.classAverage[index] - competencyData.gradeAverage[index]).toFixed(1) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.analysis-page {
  @apply max-w-7xl mx-auto px-4;
}

.page-header {
  @apply mb-6;
}

.page-title {
  @apply text-2xl font-bold text-gray-800;
}

.loading-state {
  @apply flex flex-col items-center justify-center py-12;
}

.loading-spinner {
  @apply w-10 h-10 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin mb-4;
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

.filters {
  @apply flex flex-wrap gap-4 mb-6;
}

.filter-group {
  @apply min-w-[180px];
}

.filter-group label {
  @apply block text-sm font-medium text-gray-700 mb-1;
}

.filter-group select {
  @apply w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all duration-200;
}

.class-performance-grid {
  @apply grid grid-cols-1 md:grid-cols-2 gap-6;
}

.progress-chart-container, .competency-chart-container {
  @apply bg-white rounded-xl p-6 shadow-sm;
}

.chart-header {
  @apply mb-4;
}

.chart-title {
  @apply text-lg font-medium text-gray-800;
}

.competency-legend {
  @apply grid grid-cols-2 sm:grid-cols-3 gap-4 mt-6;
}

.legend-item {
  @apply bg-gray-50 rounded-lg p-3;
}

.legend-name {
  @apply font-medium text-gray-700 mb-2;
}

.legend-values {
  @apply space-y-1;
}

.legend-value {
  @apply flex justify-between items-center;
}

.value-label {
  @apply text-sm text-gray-500;
}

.value-number {
  @apply font-medium;
}

.class-value {
  @apply text-blue-600;
}

.grade-value {
  @apply text-red-600;
}

.positive {
  @apply text-green-600;
}

.negative {
  @apply text-red-600;
}

@media (max-width: 640px) {
  .tabs {
    @apply overflow-x-auto pb-2 flex-nowrap;
    scrollbar-width: none;
    -ms-overflow-style: none;
  }
  
  .tabs::-webkit-scrollbar {
    display: none;
  }
  
  .competency-legend {
    @apply grid-cols-1;
  }
}
</style>