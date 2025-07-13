<script setup>
import { ref, onMounted, nextTick, getCurrentInstance } from 'vue';
import { get } from '@/net';
import {useAuthStore} from "@/stores/counter.js";
const authStore = useAuthStore()
const userId = authStore.user.userId
const { proxy } = getCurrentInstance();
const echarts = proxy.$echarts;

const score = ref(0);
const scorePercent = ref(0); 
const scoreTrend = ref(0); 
const isLoading = ref(true);
const scoreHistory = ref([]);
let chartInstance = null;

// 初始化成绩历史图表
const initScoreChart = () => {
  try {
    // 延迟初始化，确保DOM元素已经渲染
    setTimeout(() => {
      const chartDom = document.getElementById('scoreHistoryChart');
      if (!chartDom) {
        console.error('找不到成绩历史图表DOM元素');
        return;
      }
      
      // 如果已存在实例，先销毁
      if (chartInstance) {
        chartInstance.dispose();
      }
      
      // 创建新实例
      chartInstance = echarts.init(chartDom);
      console.log('成绩历史图表实例已创建');
      
      // 设置基本配置
      const option = {
        tooltip: {
          trigger: 'axis',
          formatter: function(params) {
            const data = params[0];
            return `${data.name}: ${data.value}分`;
          }
        },
        grid: {
          left: '10%',
          right: '10%',
          bottom: '20%',
          top: '15%',
          containLabel: false
        },
        xAxis: {
          type: 'category',
          data: [],
          axisLine: {
            lineStyle: {
              color: '#6b7280'
            }
          },
          axisLabel: {
            color: '#6b7280',
            fontSize: 10,
            interval: 0
          }
        },
        yAxis: {
          type: 'value',
          min: 0,
          max: 100,
          axisLine: {
            show: false
          },
          axisLabel: {
            formatter: '{value}',
            color: '#6b7280',
            fontSize: 10
          },
          splitLine: {
            lineStyle: {
              color: '#e5e7eb'
            }
          }
        },
        series: [
          {
            name: '考试成绩',
            type: 'bar',
            barWidth: '50%',
            data: [],
            itemStyle: {
              color: new echarts.graphic.LinearGradient(
                0, 0, 0, 1,
                [
                  {offset: 0, color: '#818cf8'},
                  {offset: 1, color: '#4f46e5'}
                ]
              )
            },
            emphasis: {
              itemStyle: {
                color: new echarts.graphic.LinearGradient(
                  0, 0, 0, 1,
                  [
                    {offset: 0, color: '#a5b4fc'},
                    {offset: 1, color: '#6366f1'}
                  ]
                )
              }
            },
            label: {
              show: true,
              position: 'top',
              formatter: '{c}',
              fontSize: 10,
              color: '#4f46e5'
            }
          }
        ]
      };
      
      chartInstance.setOption(option);
      console.log('成绩历史图表基础配置已设置');
      
      // 如果已有数据，立即更新图表
      if (scoreHistory.value.length > 0) {
        updateScoreChart();
      }
      
      // 监听窗口大小变化
      window.addEventListener('resize', () => {
        if (chartInstance) {
          chartInstance.resize();
        }
      });
    }, 300); // 延迟300ms确保DOM已渲染
  } catch (error) {
    console.error('初始化成绩历史图表时出错:', error);
  }
};

// 更新成绩历史图表
const updateScoreChart = () => {
  try {
    if (!chartInstance) {
      console.error('更新成绩历史图表失败：ECharts实例不存在');
      // 如果实例不存在，尝试重新初始化
      initScoreChart();
      return;
    }
    
    if (scoreHistory.value.length === 0) {
      console.warn('成绩历史数据为空，无法更新图表');
      return;
    }
    
    // 准备数据
    const xAxisData = scoreHistory.value.map(item => item.short_name);
    const seriesData = scoreHistory.value.map(item => item.score);
    
    // 更新图表数据
    chartInstance.setOption({
      xAxis: {
        data: xAxisData
      },
      series: [{
        data: seriesData
      }]
    });
    
    console.log('成绩历史图表数据已更新:', seriesData);
  } catch (error) {
    console.error('更新成绩历史图表数据时出错:', error);
  }
};

// 从后端加载成绩数据
const loadScoreData = () => {
  isLoading.value = true;
  
  // 获取用户ID
  
  get('/api/student/dashboard/score-overview', { userId }, 
    (message, data) => {
      console.log('获取到成绩数据:', data);
      
      if (data) {
        score.value = data.score || 0;
        scorePercent.value = score.value * 0.85; // 调整为适合圆环的百分比
        scoreTrend.value = data.scoreTrend || 0;
        
        if (data.scoreHistory && data.scoreHistory.length > 0) {
          scoreHistory.value = data.scoreHistory;
          console.log('成绩历史数据已更新:', scoreHistory.value);
          
          // 确保DOM已渲染后更新图表
          nextTick(() => {
            updateScoreChart();
          });
        }
      } else {
        console.warn('获取到的数据为空');
      }
      
      isLoading.value = false;
    }, 
    (message) => {
      console.error('获取成绩数据失败:', message);
      isLoading.value = false;
    }
  );
};

// 页面加载时获取数据
onMounted(() => {
  console.log('成绩总览组件已挂载');
  
  // 确保DOM已渲染后初始化图表
  nextTick(() => {
    console.log('开始初始化成绩历史图表');
    initScoreChart();
    
    // 初始化完成后加载数据
    console.log('开始加载成绩数据');
    loadScoreData();
  });
});
</script>

<template>
  <div class="score-overview">
    <!-- 加载状态 -->
    <div v-if="isLoading" class="loading-overlay">
      <div class="loading-spinner"></div>
    </div>
    
    <div v-else class="widget-content">
    <div class="score-circle-container">
      <div class="score-circle">
        <div class="score-number">{{ score }}</div>
        <svg viewBox="0 0 36 36" class="score-chart">
          <path class="score-circle-bg"
                d="M18 2.0845
                  a 15.9155 15.9155 0 0 1 0 31.831
                  a 15.9155 15.9155 0 0 1 0 -31.831"
          />
          <path class="score-circle-fill"
                d="M18 2.0845
                  a 15.9155 15.9155 0 0 1 0 31.831
                  a 15.9155 15.9155 0 0 1 0 -31.831"
                :stroke-dasharray="`${scorePercent}, 100`"
          />
        </svg>
      </div>
      <div class="score-label">最近一次考试</div>
    </div>
    
    <div class="score-details">
      <div class="score-trend" :class="{'positive': scoreTrend > 0, 'negative': scoreTrend < 0}">
          <span class="trend-icon">{{ scoreTrend > 0 ? '↑' : (scoreTrend < 0 ? '↓' : '-') }}</span>
        <span class="trend-value">{{ Math.abs(scoreTrend) }}分</span>
      </div>
        <div class="score-comparison">
          {{ scoreTrend > 0 ? '较上次提升' : (scoreTrend < 0 ? '较上次下降' : '与上次持平') }}
        </div>
      </div>
      
      <div class="score-history">
        <div class="history-title">近期成绩</div>
        <div id="scoreHistoryChart" class="history-chart" style="width: 100%; height: 150px;"></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.score-overview {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 0.5rem;
  position: relative;
}

.widget-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(255, 255, 255, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 10;
}

.loading-spinner {
  width: 30px;
  height: 30px;
  border: 3px solid #e5e7eb;
  border-top: 3px solid #4f46e5;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.score-circle-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 1rem;
}

.score-circle {
  width: 120px;
  height: 120px;
  position: relative;
  margin-bottom: 0.5rem;
  display: flex;
  justify-content: center;
  align-items: center;
}

.score-number {
  position: absolute;
  font-size: 2.5rem;
  font-weight: 700;
  color: #4f46e5;
  z-index: 2;
}

.score-chart {
  transform: rotate(-90deg);
  width: 100%;
  height: 100%;
  position: absolute;
}

.score-circle-bg {
  fill: none;
  stroke: #e5e7eb;
  stroke-width: 2.8;
}

.score-circle-fill {
  fill: none;
  stroke: #4f46e5;
  stroke-width: 2.8;
  stroke-linecap: round;
  animation: progress 1s ease-out forwards;
}

@keyframes progress {
  0% {
    stroke-dasharray: 0 100;
  }
}

.score-label {
  font-size: 0.875rem;
  color: #6b7280;
}

.score-details {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 1rem;
}

.score-trend {
  font-size: 1.125rem;
  font-weight: 600;
  display: flex;
  align-items: center;
}

.score-trend.positive {
  color: #10b981;
}

.score-trend.negative {
  color: #ef4444;
}

.trend-icon {
  margin-right: 0.25rem;
}

.score-comparison {
  font-size: 0.75rem;
  color: #6b7280;
  margin-top: 0.25rem;
}

.score-history {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.history-title {
  font-size: 0.875rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.5rem;
  text-align: center;
}

.history-chart {
  flex-grow: 1;
  width: 100%;
  height: 100%;
  min-height: 150px;
}
</style> 