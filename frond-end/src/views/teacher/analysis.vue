<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import BaseCard from '@/components/common/BaseCard.vue';
import * as echarts from 'echarts';

// 班级列表
const classes = [
  { id: 1, name: '计算机科学与技术1班' },
  { id: 2, name: '计算机科学与技术2班' },
  { id: 3, name: '软件工程1班' },
  { id: 4, name: '软件工程2班' }
];

// 当前选中的班级
const currentClass = ref(1);

// 当前选中的学期
const currentSemester = ref('2023-2024-1');

// 学期选项
const semesters = [
  { value: '2023-2024-1', label: '2023-2024学年第一学期' },
  { value: '2023-2024-2', label: '2023-2024学年第二学期' }
];

// 考试列表
const examList = ref([
  { id: 1, name: '高等数学期中考试', date: '2023-10-25' },
  { id: 2, name: '线性代数期末考试', date: '2024-01-10' },
  { id: 3, name: '数据结构单元测验', date: '2023-10-15' }
]);

// 当前选中的考试
const currentExam = ref(1);

// 成绩分布数据
const scoreDistribution = ref({
  excellent: 8,  // 优秀：90-100
  good: 15,      // 良好：80-89
  average: 12,   // 中等：70-79
  pass: 8,       // 及格：60-69
  fail: 5        // 不及格：0-59
});

// 知识点掌握情况
const knowledgePoints = ref([
  { name: '函数极限', mastery: 85 },
  { name: '导数计算', mastery: 78 },
  { name: '微分应用', mastery: 65 },
  { name: '不定积分', mastery: 72 },
  { name: '定积分', mastery: 60 },
  { name: '微分方程', mastery: 55 }
]);

// 学习行为数据
const learningBehaviors = ref({
  attendanceRate: 95,
  homeworkCompletionRate: 88,
  classParticipationRate: 75,
  onlineResourceAccessRate: 82
});

// 学生成绩排名
const studentRankings = ref([
  { id: 1, name: '张三', score: 95, rank: 1, change: 0 },
  { id: 2, name: '李四', score: 92, rank: 2, change: 2 },
  { id: 3, name: '王五', score: 88, rank: 3, change: -1 },
  { id: 4, name: '赵六', score: 86, rank: 4, change: 1 },
  { id: 5, name: '钱七', score: 85, rank: 5, change: 3 },
  { id: 6, name: '孙八', score: 82, rank: 6, change: -2 },
  { id: 7, name: '周九', score: 78, rank: 7, change: 0 },
  { id: 8, name: '吴十', score: 75, rank: 8, change: -3 },
  { id: 9, name: '郑十一', score: 72, rank: 9, change: 2 },
  { id: 10, name: '王十二', score: 68, rank: 10, change: -1 }
]);

// 成绩趋势数据
const scoreTrends = ref({
  examNames: ['第一次测验', '第二次测验', '期中考试', '第三次测验', '期末考试'],
  classAvg: [72, 75, 78, 76, 80],
  topStudentAvg: [88, 90, 92, 89, 94],
  bottomStudentAvg: [55, 58, 62, 60, 65]
});

// 学生学习时长数据
const studyTimeData = ref({
  weekdays: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
  onlineTime: [120, 90, 110, 85, 95, 150, 180],
  offlineTime: [300, 320, 280, 310, 290, 150, 120]
});

// 学生出勤率
const attendanceData = ref({
  months: ['9月', '10月', '11月', '12月'],
  rates: [98, 96, 94, 92]
});

// 图表实例
const scoreDistributionChart = ref(null);
const knowledgePointsChart = ref(null);
const scoreTrendsChart = ref(null);
const studyTimeChart = ref(null);
const attendanceChart = ref(null);
const behaviorRadarChart = ref(null);

// 初始化图表
onMounted(() => {
  initScoreDistributionChart();
  initKnowledgePointsChart();
  initScoreTrendsChart();
  initStudyTimeChart();
  initAttendanceChart();
  initBehaviorRadarChart();
  
  // 窗口大小变化时重绘图表
  window.addEventListener('resize', handleResize);
});

// 处理窗口大小变化
const handleResize = () => {
  scoreDistributionChart.value?.resize();
  knowledgePointsChart.value?.resize();
  scoreTrendsChart.value?.resize();
  studyTimeChart.value?.resize();
  attendanceChart.value?.resize();
  behaviorRadarChart.value?.resize();
};

// 初始化成绩分布图表
const initScoreDistributionChart = () => {
  const chartDom = document.getElementById('scoreDistributionChart');
  if (!chartDom) return;
  
  scoreDistributionChart.value = echarts.init(chartDom);
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: 0,
      data: ['优秀', '良好', '中等', '及格', '不及格']
    },
    series: [
      {
        name: '成绩分布',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '18',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: scoreDistribution.value.excellent, name: '优秀', itemStyle: { color: '#91cc75' } },
          { value: scoreDistribution.value.good, name: '良好', itemStyle: { color: '#5470c6' } },
          { value: scoreDistribution.value.average, name: '中等', itemStyle: { color: '#fac858' } },
          { value: scoreDistribution.value.pass, name: '及格', itemStyle: { color: '#ee6666' } },
          { value: scoreDistribution.value.fail, name: '不及格', itemStyle: { color: '#73c0de' } }
        ]
      }
    ]
  };
  
  option && scoreDistributionChart.value.setOption(option);
};

// 初始化知识点掌握情况图表
const initKnowledgePointsChart = () => {
  const chartDom = document.getElementById('knowledgePointsChart');
  if (!chartDom) return;
  
  knowledgePointsChart.value = echarts.init(chartDom);
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: '{b}: {c}%'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      max: 100,
      axisLabel: {
        formatter: '{value}%'
      }
    },
    yAxis: {
      type: 'category',
      data: knowledgePoints.value.map(point => point.name)
    },
    series: [
      {
        name: '掌握程度',
        type: 'bar',
        data: knowledgePoints.value.map(point => ({
          value: point.mastery,
          itemStyle: {
            color: point.mastery >= 80 ? '#91cc75' : 
                   point.mastery >= 60 ? '#5470c6' : '#ee6666'
          }
        })),
        label: {
          show: true,
          position: 'right',
          formatter: '{c}%'
        }
      }
    ]
  };
  
  option && knowledgePointsChart.value.setOption(option);
};

// 初始化成绩趋势图表
const initScoreTrendsChart = () => {
  const chartDom = document.getElementById('scoreTrendsChart');
  if (!chartDom) return;
  
  scoreTrendsChart.value = echarts.init(chartDom);
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['班级平均', '优秀学生平均', '学困生平均']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: scoreTrends.value.examNames
    },
    yAxis: {
      type: 'value',
      min: 50,
      max: 100
    },
    series: [
      {
        name: '班级平均',
        type: 'line',
        data: scoreTrends.value.classAvg,
        smooth: true
      },
      {
        name: '优秀学生平均',
        type: 'line',
        data: scoreTrends.value.topStudentAvg,
        smooth: true
      },
      {
        name: '学困生平均',
        type: 'line',
        data: scoreTrends.value.bottomStudentAvg,
        smooth: true
      }
    ]
  };
  
  option && scoreTrendsChart.value.setOption(option);
};

// 初始化学习时长图表
const initStudyTimeChart = () => {
  const chartDom = document.getElementById('studyTimeChart');
  if (!chartDom) return;
  
  studyTimeChart.value = echarts.init(chartDom);
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['线上学习', '线下学习']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: studyTimeData.value.weekdays
    },
    yAxis: {
      type: 'value',
      name: '分钟'
    },
    series: [
      {
        name: '线上学习',
        type: 'bar',
        stack: 'total',
        emphasis: {
          focus: 'series'
        },
        data: studyTimeData.value.onlineTime
      },
      {
        name: '线下学习',
        type: 'bar',
        stack: 'total',
        emphasis: {
          focus: 'series'
        },
        data: studyTimeData.value.offlineTime
      }
    ]
  };
  
  option && studyTimeChart.value.setOption(option);
};

// 初始化出勤率图表
const initAttendanceChart = () => {
  const chartDom = document.getElementById('attendanceChart');
  if (!chartDom) return;
  
  attendanceChart.value = echarts.init(chartDom);
  
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: '{b}: {c}%'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: attendanceData.value.months
    },
    yAxis: {
      type: 'value',
      min: 80,
      max: 100,
      axisLabel: {
        formatter: '{value}%'
      }
    },
    series: [
      {
        data: attendanceData.value.rates,
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          width: 4
        },
        itemStyle: {
          color: '#5470c6'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              {
                offset: 0,
                color: 'rgba(84, 112, 198, 0.5)'
              },
              {
                offset: 1,
                color: 'rgba(84, 112, 198, 0.1)'
              }
            ]
          }
        }
      }
    ]
  };
  
  option && attendanceChart.value.setOption(option);
};

// 初始化学习行为雷达图
const initBehaviorRadarChart = () => {
  const chartDom = document.getElementById('behaviorRadarChart');
  if (!chartDom) return;
  
  behaviorRadarChart.value = echarts.init(chartDom);
  
  const option = {
    tooltip: {
      trigger: 'item'
    },
    radar: {
      indicator: [
        { name: '出勤率', max: 100 },
        { name: '作业完成率', max: 100 },
        { name: '课堂参与度', max: 100 },
        { name: '在线资源访问率', max: 100 }
      ]
    },
    series: [
      {
        name: '学习行为',
        type: 'radar',
        data: [
          {
            value: [
              learningBehaviors.value.attendanceRate,
              learningBehaviors.value.homeworkCompletionRate,
              learningBehaviors.value.classParticipationRate,
              learningBehaviors.value.onlineResourceAccessRate
            ],
            name: '班级平均',
            areaStyle: {
              color: 'rgba(84, 112, 198, 0.3)'
            }
          }
        ]
      }
    ]
  };
  
  option && behaviorRadarChart.value.setOption(option);
};

// 切换班级
const handleClassChange = (classId) => {
  currentClass.value = classId;
  ElMessage.success(`已切换到${classes.find(c => c.id === classId)?.name}`);
  
  // 模拟数据更新
  setTimeout(() => {
    // 更新图表数据
    initScoreDistributionChart();
    initKnowledgePointsChart();
    initScoreTrendsChart();
    initStudyTimeChart();
    initAttendanceChart();
    initBehaviorRadarChart();
  }, 500);
};

// 切换考试
const handleExamChange = (examId) => {
  currentExam.value = examId;
  ElMessage.success(`已切换到${examList.value.find(e => e.id === examId)?.name}`);
  
  // 模拟数据更新
  setTimeout(() => {
    // 更新图表数据
    initScoreDistributionChart();
    initKnowledgePointsChart();
  }, 500);
};
</script>

<template>
  <div class="analysis-container">
    <!-- 筛选区域 -->
    <div class="filter-section mb-6 flex flex-wrap gap-4 items-center">
      <div class="w-60">
        <el-select
          v-model="currentClass"
          placeholder="选择班级"
          style="width: 100%"
        >
          <el-option
            v-for="item in classes"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </div>
      
      <div class="w-60">
        <el-select
          v-model="currentSemester"
          placeholder="选择学期"
          style="width: 100%"
        >
          <el-option
            v-for="item in semesters"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </div>
      
      <div class="w-60">
        <el-select
          v-model="currentExam"
          placeholder="选择考试"
          style="width: 100%"
        >
          <el-option
            v-for="item in examList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </div>
      
      <el-button type="primary" icon="el-icon-refresh" @click="handleExamChange(currentExam)">
        刷新数据
      </el-button>
    </div>
    
    <!-- 统计数据卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-6">
      <BaseCard class="stat-card">
        <div class="flex items-center">
          <div class="stat-icon bg-blue-100 text-blue-500">
            <i class="el-icon-user"></i>
          </div>
          <div class="ml-4">
            <div class="text-gray-500 text-sm">班级总人数</div>
            <div class="text-2xl font-bold">48</div>
            <div class="text-xs text-gray-400 mt-1">较上学期 +2</div>
          </div>
        </div>
      </BaseCard>
      
      <BaseCard class="stat-card">
        <div class="flex items-center">
          <div class="stat-icon bg-green-100 text-green-500">
            <i class="el-icon-data-analysis"></i>
          </div>
          <div class="ml-4">
            <div class="text-gray-500 text-sm">平均分</div>
            <div class="text-2xl font-bold">78.5</div>
            <div class="text-xs text-green-500 mt-1">较上次考试 +3.2</div>
          </div>
        </div>
      </BaseCard>
      
      <BaseCard class="stat-card">
        <div class="flex items-center">
          <div class="stat-icon bg-yellow-100 text-yellow-500">
            <i class="el-icon-medal"></i>
          </div>
          <div class="ml-4">
            <div class="text-gray-500 text-sm">优秀率</div>
            <div class="text-2xl font-bold">16.7%</div>
            <div class="text-xs text-yellow-500 mt-1">较上次考试 +2.1%</div>
          </div>
        </div>
      </BaseCard>
      
      <BaseCard class="stat-card">
        <div class="flex items-center">
          <div class="stat-icon bg-red-100 text-red-500">
            <i class="el-icon-warning"></i>
          </div>
          <div class="ml-4">
            <div class="text-gray-500 text-sm">不及格率</div>
            <div class="text-2xl font-bold">10.4%</div>
            <div class="text-xs text-green-500 mt-1">较上次考试 -2.6%</div>
          </div>
        </div>
      </BaseCard>
    </div>
    
    <!-- 图表区域 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
      <!-- 成绩分布 -->
      <BaseCard>
        <template #header>
          <h3 class="text-lg font-bold">成绩分布</h3>
        </template>
        <div id="scoreDistributionChart" class="chart-container"></div>
      </BaseCard>
      
      <!-- 知识点掌握情况 -->
      <BaseCard>
        <template #header>
          <h3 class="text-lg font-bold">知识点掌握情况</h3>
        </template>
        <div id="knowledgePointsChart" class="chart-container"></div>
      </BaseCard>
      
      <!-- 成绩趋势 -->
      <BaseCard>
        <template #header>
          <h3 class="text-lg font-bold">成绩趋势</h3>
        </template>
        <div id="scoreTrendsChart" class="chart-container"></div>
      </BaseCard>
      
      <!-- 学习行为分析 -->
      <BaseCard>
        <template #header>
          <h3 class="text-lg font-bold">学习行为分析</h3>
        </template>
        <div id="behaviorRadarChart" class="chart-container"></div>
      </BaseCard>
      
      <!-- 学习时长分析 -->
      <BaseCard>
        <template #header>
          <h3 class="text-lg font-bold">学习时长分析</h3>
        </template>
        <div id="studyTimeChart" class="chart-container"></div>
      </BaseCard>
      
      <!-- 出勤率分析 -->
      <BaseCard>
        <template #header>
          <h3 class="text-lg font-bold">出勤率分析</h3>
        </template>
        <div id="attendanceChart" class="chart-container"></div>
      </BaseCard>
    </div>
    
    <!-- 学生排名表格 -->
    <BaseCard class="mb-6">
      <template #header>
        <div class="flex justify-between items-center">
          <h3 class="text-lg font-bold">学生成绩排名</h3>
          <el-button type="primary" size="small" icon="el-icon-download">
            导出数据
          </el-button>
        </div>
      </template>
      
      <el-table :data="studentRankings" border style="width: 100%">
        <el-table-column prop="rank" label="排名" width="80" align="center" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="score" label="分数" width="100" align="center" />
        <el-table-column label="排名变化" width="120" align="center">
          <template #default="{ row }">
            <div class="flex items-center justify-center">
              <template v-if="row.change > 0">
                <i class="el-icon-top text-green-500 mr-1"></i>
                <span class="text-green-500">{{ row.change }}</span>
              </template>
              <template v-else-if="row.change < 0">
                <i class="el-icon-bottom text-red-500 mr-1"></i>
                <span class="text-red-500">{{ Math.abs(row.change) }}</span>
              </template>
              <template v-else>
                <span class="text-gray-400">-</span>
              </template>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="primary" size="small" icon="el-icon-view" class="mr-1">
              查看详情
            </el-button>
            <el-button type="info" size="small" icon="el-icon-message">
              发送反馈
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </BaseCard>
    
    <!-- 学习建议 -->
    <BaseCard>
      <template #header>
        <h3 class="text-lg font-bold">AI学习建议</h3>
      </template>
      
      <div class="p-4 bg-blue-50 rounded-lg border border-blue-100">
        <h4 class="font-bold text-blue-700 mb-2">班级整体情况分析</h4>
        <p class="text-gray-700 mb-4">
          本次考试班级整体表现良好，平均分78.5分，较上次考试提高3.2分。优秀率16.7%，不及格率10.4%。
          知识点掌握情况中，函数极限和导数计算掌握较好，微分方程掌握较弱。建议加强微分方程和定积分的教学。
        </p>
        
        <h4 class="font-bold text-blue-700 mb-2">教学建议</h4>
        <ul class="list-disc pl-6 text-gray-700 mb-4">
          <li>针对微分方程和定积分知识点，增加课堂练习和讲解时间</li>
          <li>组织小组学习，让优秀学生带动学困生</li>
          <li>针对不及格学生进行一对一辅导</li>
          <li>增加实际应用案例，提高学生学习兴趣</li>
        </ul>
        
        <h4 class="font-bold text-blue-700 mb-2">学生分层指导</h4>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div class="p-3 bg-green-50 rounded border border-green-100">
            <h5 class="font-bold text-green-700 mb-1">优秀学生 (90分以上)</h5>
            <p class="text-sm text-gray-600">
              增加拓展题目，培养创新思维，鼓励参加数学竞赛
            </p>
          </div>
          <div class="p-3 bg-yellow-50 rounded border border-yellow-100">
            <h5 class="font-bold text-yellow-700 mb-1">中等学生 (70-89分)</h5>
            <p class="text-sm text-gray-600">
              巩固基础知识，增加典型题目练习，提高解题能力
            </p>
          </div>
          <div class="p-3 bg-red-50 rounded border border-red-100">
            <h5 class="font-bold text-red-700 mb-1">学困生 (70分以下)</h5>
            <p class="text-sm text-gray-600">
              查漏补缺，一对一辅导，增加基础题目练习次数
            </p>
          </div>
        </div>
      </div>
    </BaseCard>
  </div>
</template>

<style scoped>
.analysis-container {
  @apply p-6;
}

.chart-container {
  height: 300px;
  width: 100%;
}

.stat-card {
  @apply transition-all duration-300 hover:shadow-md;
}

.stat-icon {
  @apply w-12 h-12 rounded-full flex items-center justify-center text-xl;
}

:deep(.el-table) {
  @apply rounded-lg overflow-hidden shadow-sm;
  --el-table-border-color: theme('colors.gray.200');
}

:deep(.el-table th) {
  @apply bg-gray-100 text-gray-700;
}

:deep(.el-button) {
  @apply transition-all duration-300;
}
</style>