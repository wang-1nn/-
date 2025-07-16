<script setup>
import { ref, onMounted } from 'vue';
import { Chart, registerables } from 'chart.js';

// 注册Chart.js组件
Chart.register(...registerables);

const props = defineProps({
  competencies: {
    type: Array,
    required: true
  },
  classAverage: {
    type: Array,
    required: true
  },
  gradeAverage: {
    type: Array,
    default: () => []
  },
  maxValue: {
    type: Number,
    default: 100
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
  
  // 准备数据
  const data = {
    labels: props.competencies.map(item => item.name),
    datasets: [
      {
        label: '本班平均',
        data: props.classAverage,
        backgroundColor: 'rgba(54, 162, 235, 0.2)',
        borderColor: 'rgba(54, 162, 235, 1)',
        borderWidth: 2,
        pointBackgroundColor: 'rgba(54, 162, 235, 1)',
        pointRadius: 4
      }
    ]
  };
  
  // 如果有年级平均值，添加到数据集
  if (props.gradeAverage.length > 0) {
    data.datasets.push({
      label: '年级平均',
      data: props.gradeAverage,
      backgroundColor: 'rgba(255, 99, 132, 0.2)',
      borderColor: 'rgba(255, 99, 132, 1)',
      borderWidth: 2,
      pointBackgroundColor: 'rgba(255, 99, 132, 1)',
      pointRadius: 4
    });
  }

  chart.value = new Chart(ctx, {
    type: 'radar',
    data: data,
    options: {
      scales: {
        r: {
          angleLines: {
            display: true,
            color: 'rgba(0, 0, 0, 0.1)'
          },
          suggestedMin: 0,
          suggestedMax: props.maxValue,
          ticks: {
            stepSize: 20,
            backdropColor: 'transparent'
          }
        }
      },
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            boxWidth: 12,
            usePointStyle: true,
            pointStyle: 'circle'
          }
        },
        tooltip: {
          callbacks: {
            label: function(context) {
              const label = context.dataset.label || '';
              const value = context.raw || 0;
              return `${label}: ${value}`;
            }
          }
        }
      },
      elements: {
        line: {
          tension: 0.1
        }
      },
      responsive: true,
      maintainAspectRatio: false
    }
  });
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