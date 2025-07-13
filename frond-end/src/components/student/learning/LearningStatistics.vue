<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElProgress } from 'element-plus'
import { get } from '@/net'

const props = defineProps({
  userId: {
    type: Number,
    required: true
  },
  courseId: {
    type: Number,
    required: true
  }
})

const loading = ref(false)
const statistics = ref({
  courseId: null,
  courseTitle: '',
  totalChapters: 0,
  completedChapters: 0,
  totalTime: 0,
  averageRating: 0,
  lastStudyTime: null,
  chapterStats: []
})

// 获取学习统计
const fetchStatistics = () => {
  loading.value = true
  
  get(`/api/student/courses/${props.courseId}/statistics`, 
    { studentId: props.userId }, 
    (message, data) => {
      statistics.value = data || {
        courseId: props.courseId,
        courseTitle: '',
        totalChapters: 0,
        completedChapters: 0,
        totalTime: 0,
        averageRating: 0,
        lastStudyTime: null,
        chapterStats: []
      }
      loading.value = false
    },
    (message) => {
      ElMessage.error(message || '获取学习统计失败')
      loading.value = false
    }
  )
}

// 格式化时间（秒转为时分秒）
const formatDuration = (seconds) => {
  if (!seconds) return '0分钟'
  
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  
  if (hours > 0) {
    return `${hours}小时${minutes > 0 ? ` ${minutes}分钟` : ''}`
  } else if (minutes > 0) {
    return `${minutes}分钟`
  } else {
    return `${seconds}秒`
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '暂无记录'
  
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 计算完成百分比
const completionPercentage = () => {
  if (statistics.value.totalChapters === 0) return 0
  return Math.round((statistics.value.completedChapters / statistics.value.totalChapters) * 100)
}

// 监听属性变化
watch(() => props.courseId, () => {
  if (props.courseId) {
    fetchStatistics()
  }
}, { immediate: true })

onMounted(() => {
  if (props.courseId) {
    fetchStatistics()
  }
})
</script>

<template>
  <div class="learning-statistics-component" v-loading="loading">
    <div class="stats-header">
      <h3 class="text-lg font-medium mb-4">学习统计</h3>
    </div>
    
    <!-- 总体统计 -->
    <div class="overall-stats bg-white p-4 rounded-lg shadow-sm mb-6">
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div class="stat-item text-center p-3 bg-blue-50 rounded-lg">
          <div class="text-sm text-gray-600 mb-1">总学习时长</div>
          <div class="text-xl font-bold text-blue-700">{{ formatDuration(statistics.totalTime) }}</div>
        </div>
        
        <div class="stat-item text-center p-3 bg-green-50 rounded-lg">
          <div class="text-sm text-gray-600 mb-1">完成章节</div>
          <div class="text-xl font-bold text-green-700">{{ statistics.completedChapters }}/{{ statistics.totalChapters }}</div>
        </div>
        
        <div class="stat-item text-center p-3 bg-amber-50 rounded-lg">
          <div class="text-sm text-gray-600 mb-1">平均评分</div>
          <div class="text-xl font-bold text-amber-700">{{ statistics.averageRating }}</div>
        </div>
        
        <div class="stat-item text-center p-3 bg-purple-50 rounded-lg">
          <div class="text-sm text-gray-600 mb-1">最近学习</div>
          <div class="text-sm font-medium text-purple-700">{{ formatDate(statistics.lastStudyTime) }}</div>
        </div>
      </div>
      
      <div class="mt-4">
        <div class="flex justify-between text-sm mb-1">
          <span>课程完成度</span>
          <span>{{ completionPercentage() }}%</span>
        </div>
        <el-progress :percentage="completionPercentage()" :stroke-width="10"></el-progress>
      </div>
    </div>
    
    <!-- 章节统计 -->
    <div class="chapter-stats">
      <h4 class="text-base font-medium mb-3">章节学习详情</h4>
      
      <div v-if="statistics.chapterStats.length === 0" class="text-center py-8 text-gray-400">
        暂无章节学习记录
      </div>
      
      <div v-else class="space-y-4">
        <div v-for="(chapter, index) in statistics.chapterStats" :key="index" class="chapter-stat-item bg-white p-4 rounded-lg shadow-sm">
          <div class="flex justify-between items-center mb-2">
            <h5 class="font-medium">{{ chapter.chapterTitle }}</h5>
            <span :class="[
              'text-xs px-2 py-1 rounded-full',
              chapter.isCompleted ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
            ]">
              {{ chapter.isCompleted ? '已完成' : '学习中' }}
            </span>
          </div>
          
          <div class="grid grid-cols-2 md:grid-cols-3 gap-3 text-sm">
            <div class="stat-detail">
              <span class="text-gray-500">学习时长：</span>
              <span class="font-medium">{{ formatDuration(chapter.totalTime) }}</span>
            </div>
            
            <div class="stat-detail">
              <span class="text-gray-500">访问次数：</span>
              <span class="font-medium">{{ chapter.visitCount || 0 }}次</span>
            </div>
            
            <div class="stat-detail">
              <span class="text-gray-500">评分：</span>
              <span class="font-medium">{{ chapter.rating || '-' }}</span>
            </div>
          </div>
          
          <div v-if="chapter.lastVisitTime" class="text-xs text-gray-500 mt-2">
            最近访问：{{ formatDate(chapter.lastVisitTime) }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.learning-statistics-component {
  padding: 1rem;
}

.stat-item {
  transition: transform 0.2s ease;
}

.stat-item:hover {
  transform: translateY(-2px);
}

.chapter-stat-item {
  transition: all 0.2s ease;
  border: 1px solid transparent;
}

.chapter-stat-item:hover {
  border-color: #e2e8f0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}
</style> 