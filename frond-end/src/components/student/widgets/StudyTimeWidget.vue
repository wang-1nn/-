<script setup>
import { ref, onMounted, nextTick, watch, getCurrentInstance } from 'vue';
import { get } from '@/net';
import {useAuthStore} from "@/stores/counter.js";
const authStore = useAuthStore()
const userId = authStore.user.userId
const { proxy } = getCurrentInstance();
const echarts = proxy.$echarts;

const weekdays = ref(['周一', '周二', '周三', '周四', '周五', '周六', '周日']);
const dailyStudyData = ref([]);
const weeklyHours = ref(0);
const weeklyTrend = ref(0);
const isLoading = ref(true);
let chartInstance = null;

// 初始化ECharts图表
const initChart = () => {
  try {
    // 延迟初始化，确保DOM元素已经渲染
    setTimeout(() => {
      const chartDom = document.getElementById('studyTimeChart');
      if (!chartDom) {
        console.error('找不到图表DOM元素');
        return;
      }
      
      // 如果已存在实例，先销毁
      if (chartInstance) {
        chartInstance.dispose();
      }
      
      // 创建新实例
      chartInstance = echarts.init(chartDom);
      console.log('ECharts实例已创建');
      
      // 设置基本配置
      const option = {
        tooltip: {
          trigger: 'axis',
          formatter: function(params) {
            const data = params[0];
            return `${data.name}: ${data.value.toFixed(2)}小时`;
          }
        },
        grid: {
          left: '5%',
          right: '5%',
          bottom: '10%',
          top: '10%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: weekdays.value,
          axisLine: {
            lineStyle: {
              color: '#6b7280'
            }
          },
          axisLabel: {
            color: '#6b7280',
            fontSize: 10
          }
        },
        yAxis: {
          type: 'value',
          axisLine: {
            show: false
          },
          axisLabel: {
            formatter: '{value}h',
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
            name: '学习时长',
            type: 'bar',
            barWidth: '50%',
            data: [0, 0, 0, 0, 0, 0, 0], // 初始数据
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
            }
          }
        ]
      };
      
      chartInstance.setOption(option);
      console.log('ECharts基础配置已设置');
      
      // 如果已有数据，立即更新图表
      if (dailyStudyData.value.length > 0) {
        updateChart();
      }
      
      // 监听窗口大小变化
      window.addEventListener('resize', () => {
        if (chartInstance) {
          chartInstance.resize();
        }
      });
    }, 300); // 延迟300ms确保DOM已渲染
  } catch (error) {
    console.error('初始化图表时出错:', error);
  }
};

// 更新图表数据
const updateChart = () => {
  try {
    if (!chartInstance) {
      console.error('更新图表失败：ECharts实例不存在');
      // 如果实例不存在，尝试重新初始化
      initChart();
      return;
    }
    
    // 准备数据，确保按周一到周日的顺序
    const orderedData = [];
    const dayMap = {};
    
    // 将后端数据转换为以day为键的映射
    dailyStudyData.value.forEach(item => {
      dayMap[item.day] = item.hours || 0;
    });
    
    // 按照固定顺序提取数据
    weekdays.value.forEach(day => {
      orderedData.push(dayMap[day] || 0);
    });
    
    // 更新图表数据
    chartInstance.setOption({
      series: [{
        data: orderedData
      }]
    });
    
    console.log('图表数据已更新:', orderedData);
  } catch (error) {
    console.error('更新图表数据时出错:', error);
  }
};

// 从后端加载学习时长数据
const loadStudyTimeData = () => {
  isLoading.value = true;
  
  // 获取用户ID
  get('/api/student/dashboard/study-time', { userId }, 
    (message, data) => {
      console.log('获取到学习时长数据:', data);
      
      if (data) {
        weeklyHours.value = data.weeklyHours || 0;
        weeklyTrend.value = data.weeklyTrend || 0;
        
        if (data.dailyStudy && data.dailyStudy.length > 0) {
          // 保存学习时长数据
          dailyStudyData.value = data.dailyStudy;
          console.log('每日学习数据已更新:', dailyStudyData.value);
          
          // 确保DOM已渲染后更新图表
          nextTick(() => {
            updateChart();
          });
        }
      } else {
        console.warn('获取到的数据为空');
      }
      
      isLoading.value = false;
    }, 
    (message) => {
      console.error('获取学习时长数据失败:', message);
      isLoading.value = false;
    }
  );
};

// 监听dailyStudyData变化，自动更新图表
watch(dailyStudyData, () => {
  nextTick(() => {
    updateChart();
  });
}, { deep: true });

// 页面加载时初始化图表并获取数据
onMounted(() => {
  console.log('组件已挂载');
  
  // 确保DOM已渲染后初始化图表
  nextTick(() => {
    console.log('开始初始化图表');
    initChart();
    
    // 初始化完成后加载数据
    console.log('开始加载数据');
    loadStudyTimeData();
  });
});
</script>

<template>
  <div class="study-time-widget">
    <!-- 加载状态 -->
    <div v-if="isLoading" class="loading-overlay">
      <div class="loading-spinner"></div>
    </div>
    
    <div v-else class="widget-content">
      <div class="stats-container">
        <div class="stat-item">
          <div class="stat-value">{{ weeklyHours.toFixed(1) }}</div>
          <div class="stat-label">本周学习时长(小时)</div>
        </div>
        <div class="stat-item">
          <div class="stat-value" :class="{'text-green-500': weeklyTrend > 0, 'text-red-500': weeklyTrend < 0}">
            {{ weeklyTrend > 0 ? '+' : '' }}{{ weeklyTrend.toFixed(1) }}
          </div>
          <div class="stat-label">较上周</div>
        </div>
      </div>
      <div class="chart-container">
        <div id="studyTimeChart" class="chart-area" style="width: 100%; height: 150px;"></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.study-time-widget {
  height: 100%;
  display: flex;
  flex-direction: column;
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

.stats-container {
  display: flex;
  justify-content: space-between;
  margin-bottom: 1rem;
  padding: 0 0.5rem;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: #4f46e5;
}

.stat-label {
  font-size: 0.75rem;
  color: #6b7280;
}

.chart-container {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  min-height: 180px;
}

.chart-area {
  width: 100%;
  height: 100%;
}

.text-red-500 {
  color: #ef4444;
}

.text-green-500 {
  color: #10b981;
}
</style> 