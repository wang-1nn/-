<script setup>
import { ref, computed } from 'vue';

const props = defineProps({
  classData: {
    type: Object,
    required: true
  },
  compareWithPrevious: {
    type: Boolean,
    default: false
  }
});

// 计算平均分比上次的变化
const averageScoreChange = computed(() => {
  if (!props.compareWithPrevious || !props.classData.previousAverageScore) return null;
  
  const change = props.classData.averageScore - props.classData.previousAverageScore;
  return {
    value: change.toFixed(1),
    isPositive: change > 0,
    isNegative: change < 0
  };
});

// 计算及格率比上次的变化
const passRateChange = computed(() => {
  if (!props.compareWithPrevious || !props.classData.previousPassRate) return null;
  
  const change = props.classData.passRate - props.classData.previousPassRate;
  return {
    value: change.toFixed(1),
    isPositive: change > 0,
    isNegative: change < 0
  };
});

// 计算优秀率比上次的变化
const excellentRateChange = computed(() => {
  if (!props.compareWithPrevious || !props.classData.previousExcellentRate) return null;
  
  const change = props.classData.excellentRate - props.classData.previousExcellentRate;
  return {
    value: change.toFixed(1),
    isPositive: change > 0,
    isNegative: change < 0
  };
});

// 计算班级排名比上次的变化
const rankChange = computed(() => {
  if (!props.compareWithPrevious || !props.classData.previousRank) return null;
  
  const change = props.classData.previousRank - props.classData.rank; // 注意这里的逻辑反转，排名上升是正向变化
  return {
    value: Math.abs(change),
    isPositive: change > 0,
    isNegative: change < 0
  };
});

// 获取评价文字
const getEvaluation = () => {
  const score = props.classData.averageScore;
  
  if (score >= 90) return '优秀';
  if (score >= 80) return '良好';
  if (score >= 70) return '中等';
  if (score >= 60) return '及格';
  return '不及格';
};
</script>

<template>
  <div class="performance-card">
    <div class="card-header">
      <h3 class="class-name">{{ classData.name }}</h3>
      <div class="class-meta">
        <span class="student-count">{{ classData.studentCount }}人</span>
        <span v-if="classData.rank" class="rank">班级排名：第{{ classData.rank }}名</span>
      </div>
    </div>
    
    <div class="stats-grid">
      <div class="stat-item">
        <div class="stat-label">平均分</div>
        <div class="stat-value">{{ classData.averageScore }}</div>
        <div v-if="averageScoreChange" class="stat-change" :class="{
          'positive': averageScoreChange.isPositive,
          'negative': averageScoreChange.isNegative
        }">
          {{ averageScoreChange.isPositive ? '+' : '' }}{{ averageScoreChange.value }}
        </div>
      </div>
      
      <div class="stat-item">
        <div class="stat-label">及格率</div>
        <div class="stat-value">{{ classData.passRate }}%</div>
        <div v-if="passRateChange" class="stat-change" :class="{
          'positive': passRateChange.isPositive,
          'negative': passRateChange.isNegative
        }">
          {{ passRateChange.isPositive ? '+' : '' }}{{ passRateChange.value }}%
        </div>
      </div>
      
      <div class="stat-item">
        <div class="stat-label">优秀率</div>
        <div class="stat-value">{{ classData.excellentRate }}%</div>
        <div v-if="excellentRateChange" class="stat-change" :class="{
          'positive': excellentRateChange.isPositive,
          'negative': excellentRateChange.isNegative
        }">
          {{ excellentRateChange.isPositive ? '+' : '' }}{{ excellentRateChange.value }}%
        </div>
      </div>
      
      <div class="stat-item">
        <div class="stat-label">班级评价</div>
        <div class="stat-value">{{ getEvaluation() }}</div>
        <div v-if="rankChange" class="stat-change" :class="{
          'positive': rankChange.isPositive,
          'negative': rankChange.isNegative
        }">
          {{ rankChange.isPositive ? '↑' : '↓' }}{{ rankChange.value }}
        </div>
      </div>
    </div>
    
    <div class="grade-distribution">
      <div class="distribution-label">分数分布</div>
      <div class="distribution-bars">
        <div 
          v-for="(count, range) in classData.scoreDistribution" 
          :key="range"
          class="bar-item"
        >
          <div class="bar-label">{{ range }}</div>
          <div class="bar-container">
            <div 
              class="bar-fill"
              :class="getBarColor(range)"
              :style="{ width: getBarWidth(count) }"
            ></div>
          </div>
          <div class="bar-value">{{ count }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  methods: {
    // 获取分数段对应的颜色
    getBarColor(range) {
      if (range === '90-100') return 'excellent';
      if (range === '80-89') return 'good';
      if (range === '70-79') return 'average';
      if (range === '60-69') return 'pass';
      return 'fail';
    },
    
    // 计算柱状图宽度比例
    getBarWidth(count) {
      // 获取分数分布中的最大值
      const maxCount = Math.max(...Object.values(this.classData.scoreDistribution));
      return maxCount > 0 ? `${(count / maxCount * 100).toFixed(1)}%` : '0%';
    }
  }
}
</script>

<style scoped>
.performance-card {
  @apply bg-white rounded-xl p-6 shadow-sm;
}

.card-header {
  @apply mb-4;
}

.class-name {
  @apply text-lg font-medium text-gray-800 mb-1;
}

.class-meta {
  @apply flex items-center gap-4 text-sm text-gray-600;
}

.rank {
  @apply font-medium;
}

.stats-grid {
  @apply grid grid-cols-2 sm:grid-cols-4 gap-4 mb-6;
}

.stat-item {
  @apply bg-gray-50 rounded-lg p-3 text-center;
}

.stat-label {
  @apply text-sm text-gray-500 mb-1;
}

.stat-value {
  @apply text-xl font-semibold text-gray-800;
}

.stat-change {
  @apply text-xs font-medium mt-1;
}

.positive {
  @apply text-green-600;
}

.negative {
  @apply text-red-600;
}

.grade-distribution {
  @apply border-t border-gray-100 pt-4;
}

.distribution-label {
  @apply text-sm font-medium text-gray-700 mb-3;
}

.distribution-bars {
  @apply space-y-2;
}

.bar-item {
  @apply flex items-center gap-2;
}

.bar-label {
  @apply text-xs text-gray-500 w-12;
}

.bar-container {
  @apply flex-1 bg-gray-100 rounded-full h-3 overflow-hidden;
}

.bar-fill {
  @apply h-full rounded-full;
}

.bar-fill.excellent {
  @apply bg-green-500;
}

.bar-fill.good {
  @apply bg-blue-500;
}

.bar-fill.average {
  @apply bg-yellow-500;
}

.bar-fill.pass {
  @apply bg-orange-400;
}

.bar-fill.fail {
  @apply bg-red-500;
}

.bar-value {
  @apply text-xs text-gray-600 w-6 text-right;
}
</style>