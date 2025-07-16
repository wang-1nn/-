<script setup>
import { ref, computed, onMounted } from 'vue';
import { Chart, registerables } from 'chart.js';

// 注册Chart.js组件
Chart.register(...registerables);

const props = defineProps({
  examData: {
    type: Object,
    required: true
  }
});

const chartRef = ref(null);
const scoreChart = ref(null);
const distributionChart = ref(null);

// 获取及格率和不及格率
const passRate = computed(() => {
  return props.examData.passRate || 0;
});

const failRate = computed(() => {
  return 100 - passRate.value;
});

// 计算分数段分布
const scoreDistribution = computed(() => {
  const distribution = {
    '0-59': 0,
    '60-69': 0,
    '70-79': 0,
    '80-89': 0,
    '90-100': 0
  };

  if (props.examData.scoreDistribution) {
    return props.examData.scoreDistribution;
  }

  return distribution;
});

// 计算标准差
const standardDeviation = computed(() => {
  return props.examData.standardDeviation || '-';
});

// 初始化图表
onMounted(() => {
  initScoreChart();
  initDistributionChart();
});

// 初始化成绩数据图表
const initScoreChart = () => {
  const ctx = document.getElementById('scoreChart');
  
  if (scoreChart.value) {
    scoreChart.value.destroy();
  }
  
  scoreChart.value = new Chart(ctx, {
    type: 'pie',
    data: {
      labels: ['及格', '不及格'],
      datasets: [{
        data: [passRate.value, failRate.value],
        backgroundColor: [
          'rgba(75, 192, 192, 0.7)',
          'rgba(255, 99, 132, 0.7)',
        ],
        borderColor: [
          'rgba(75, 192, 192, 1)',
          'rgba(255, 99, 132, 1)',
        ],
        borderWidth: 1
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'bottom'
        }
      }
    }
  });
};

// 初始化成绩分布图表
const initDistributionChart = () => {
  const ctx = document.getElementById('distributionChart');
  
  if (distributionChart.value) {
    distributionChart.value.destroy();
  }

  const distribution = scoreDistribution.value;
  
  distributionChart.value = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: Object.keys(distribution),
      datasets: [{
        label: '学生数量',
        data: Object.values(distribution),
        backgroundColor: 'rgba(54, 162, 235, 0.7)',
        borderColor: 'rgba(54, 162, 235, 1)',
        borderWidth: 1
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        y: {
          beginAtZero: true,
          ticks: {
            precision: 0
          }
        }
      },
      plugins: {
        legend: {
          display: false
        }
      }
    }
  });
};
</script>

<template>
  <div class="exam-statistics">
    <div class="statistics-header">
      <h3 class="text-lg font-medium">考试统计</h3>
      <p class="text-sm text-gray-500">{{ props.examData.title }} - {{ props.examData.course }}</p>
    </div>
    
    <div class="stats-summary">
      <div class="stat-card">
        <div class="stat-title">平均分</div>
        <div class="stat-value">{{ props.examData.averageScore || '-' }}</div>
      </div>
      
      <div class="stat-card">
        <div class="stat-title">最高分</div>
        <div class="stat-value">{{ props.examData.highestScore || '-' }}</div>
      </div>
      
      <div class="stat-card">
        <div class="stat-title">最低分</div>
        <div class="stat-value">{{ props.examData.lowestScore || '-' }}</div>
      </div>
      
      <div class="stat-card">
        <div class="stat-title">标准差</div>
        <div class="stat-value">{{ standardDeviation }}</div>
      </div>
    </div>
    
    <div class="charts-container">
      <div class="chart-card">
        <h4 class="chart-title">及格率</h4>
        <div class="chart-wrapper">
          <canvas id="scoreChart"></canvas>
        </div>
        <div class="chart-info">
          <div class="info-item">
            <span class="info-dot bg-green-500"></span>
            <span class="info-label">及格：</span>
            <span class="info-value">{{ passRate }}%</span>
          </div>
          <div class="info-item">
            <span class="info-dot bg-red-400"></span>
            <span class="info-label">不及格：</span>
            <span class="info-value">{{ failRate }}%</span>
          </div>
        </div>
      </div>
      
      <div class="chart-card">
        <h4 class="chart-title">分数分布</h4>
        <div class="chart-wrapper">
          <canvas id="distributionChart"></canvas>
        </div>
      </div>
    </div>
    
    <div class="question-analysis" v-if="props.examData.questionAnalysis">
      <h4 class="section-title">题目分析</h4>
      <div class="question-table">
        <table>
          <thead>
            <tr>
              <th>题号</th>
              <th>类型</th>
              <th>难度</th>
              <th>得分率</th>
              <th>区分度</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(question, index) in props.examData.questionAnalysis" :key="index">
              <td>{{ question.number }}</td>
              <td>{{ question.type }}</td>
              <td>
                <div class="difficulty">
                  <div 
                    class="difficulty-bar"
                    :style="{ width: `${question.difficulty * 20}%` }"
                  ></div>
                </div>
              </td>
              <td>{{ question.correctRate }}%</td>
              <td>{{ question.discrimination }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.exam-statistics {
  @apply bg-white rounded-xl p-6 shadow-sm;
}

.statistics-header {
  @apply mb-6;
}

.stats-summary {
  @apply grid grid-cols-2 sm:grid-cols-4 gap-4 mb-8;
}

.stat-card {
  @apply bg-gray-50 rounded-lg p-4 text-center;
}

.stat-title {
  @apply text-sm text-gray-500 mb-1;
}

.stat-value {
  @apply text-xl font-semibold text-gray-800;
}

.charts-container {
  @apply grid grid-cols-1 md:grid-cols-2 gap-6 mb-8;
}

.chart-card {
  @apply bg-gray-50 rounded-lg p-4;
}

.chart-title {
  @apply text-sm font-medium text-gray-700 mb-4 text-center;
}

.chart-wrapper {
  @apply h-64 mb-4;
}

.chart-info {
  @apply flex flex-col space-y-1;
}

.info-item {
  @apply flex items-center text-sm;
}

.info-dot {
  @apply h-3 w-3 rounded-full mr-2;
}

.info-label {
  @apply text-gray-500 mr-1;
}

.info-value {
  @apply text-gray-700 font-medium;
}

.section-title {
  @apply text-lg font-medium mb-4;
}

.question-table {
  @apply overflow-x-auto;
}

.question-table table {
  @apply w-full;
}

.question-table th {
  @apply px-4 py-2 bg-gray-50 text-left text-sm font-medium text-gray-700;
}

.question-table td {
  @apply px-4 py-3 border-t border-gray-100 text-sm;
}

.difficulty {
  @apply bg-gray-200 h-2 rounded-full w-full overflow-hidden;
}

.difficulty-bar {
  @apply h-full bg-blue-500 rounded-full;
}
</style>