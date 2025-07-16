<script setup>
import { ref, onMounted } from 'vue';
import { Chart, registerables } from 'chart.js';

// 注册Chart.js组件
Chart.register(...registerables);

const props = defineProps({
  studentData: {
    type: Array,
    required: true
  },
  period: {
    type: String,
    default: 'semester'
  }
});

const chartRef = ref(null);
const chart = ref(null);

onMounted(() => {
  renderChart();
});

const renderChart = () => {
  if (chart.value) {
    chart.value.destroy();
  }

  const ctx = chartRef.value.getContext('2d');
  
  // 根据周期设置标签
  let labels = [];
  if (props.period === 'week') {
    labels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
  } else if (props.period === 'month') {
    labels = ['第1周', '第2周', '第3周', '第4周'];
  } else {
    labels = ['9月', '10月', '11月', '12月', '1月', '2月'];
  }
  
  // 提取各个学生的数据
  const datasets = props.studentData.map((student, index) => {
    // 为每个学生生成唯一的颜色
    const hue = (index * 137) % 360;
    const color = `hsl(${hue}, 70%, 60%)`;
    
    return {
      label: student.name,
      data: student.scores,
      borderColor: color,
      backgroundColor: `hsla(${hue}, 70%, 60%, 0.2)`,
      tension: 0.4,
      borderWidth: 2,
      pointRadius: 3,
      pointBackgroundColor: color
    };
  });

  chart.value = new Chart(ctx, {
    type: 'line',
    data: {
      labels: labels,
      datasets: datasets
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'top',
          labels: {
            boxWidth: 12,
            usePointStyle: true,
            pointStyle: 'circle'
          }
        },
        tooltip: {
          mode: 'index',
          intersect: false
        }
      },
      scales: {
        y: {
          min: 0,
          max: 100,
          ticks: {
            stepSize: 20
          },
          title: {
            display: true,
            text: '成绩'
          }
        },
        x: {
          title: {
            display: true,
            text: getTimeLabel()
          }
        }
      },
      interaction: {
        mode: 'nearest',
        intersect: false
      }
    }
  });
};

const getTimeLabel = () => {
  switch (props.period) {
    case 'week': return '日期';
    case 'month': return '周次';
    default: return '月份';
  }
};
</script>

<template>
  <div class="chart-container">
    <canvas ref="chartRef"></canvas>
  </div>
</template>

<style scoped>
.chart-container {
  @apply h-80 w-full;
}
</style>