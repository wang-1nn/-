<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElRate } from 'element-plus'
import { get, post } from '@/net'
import {useAuthStore} from "@/stores/counter.js";
const authStore = useAuthStore()
const userId = authStore.user.userId
const props = defineProps({
  courseId: {
    type: Number,
    required: true
  },
  chapterId: {
    type: Number,
    required: true
  }
})

const loading = ref(false)
const submitting = ref(false)
const userRating = ref(null)
const feedback = ref('')
const averageRating = ref(0)
const ratings = ref([])
const showRatings = ref(false)

// 获取用户评价
const fetchUserRating = () => {
  loading.value = true
  
  get('/api/student/chapter-ratings/user', 
    {
      studentId: userId, // 修改这里，将userId改为studentId
      chapterId: props.chapterId
    }, 
    (message, data) => {
      if (data) {
        userRating.value = data.rating
        feedback.value = data.feedback || ''
      } else {
        userRating.value = null
        feedback.value = ''
      }
      loading.value = false
    },
    (message) => {
      ElMessage.error(message || '获取评价失败')
      loading.value = false
    }
  )
}

// 获取章节平均评分
const fetchAverageRating = () => {
  get(`/api/student/chapter-ratings/${props.chapterId}/average`, 
    null, 
    (message, data) => {
      averageRating.value = data || 0
    }
  )
}

// 获取章节评价列表
const fetchRatings = () => {
  get(`/api/student/chapter-ratings/${props.chapterId}`, 
    null, 
    (message, data) => {
      ratings.value = data || []
    }
  )
}

// 提交评价
const submitRating = () => {
  if (!userRating.value) {
    ElMessage.warning('请先进行评分')
    return
  }
  
  submitting.value = true
  
  const rating = {
    studentId: userId, // 修改这里，将userId改为studentId
    courseId: props.courseId,
    chapterId: props.chapterId,
    rating: userRating.value,
    feedback: feedback.value
  }
  
  post('/api/student/chapter-ratings', rating, 
    (message) => {
      ElMessage.success(message || '评价提交成功')
      submitting.value = false
      fetchAverageRating()
      fetchRatings()
    },
    (message) => {
      ElMessage.error(message || '评价提交失败')
      submitting.value = false
    }
  )
}

// 切换显示所有评价
const toggleRatings = () => {
  showRatings.value = !showRatings.value
  if (showRatings.value && ratings.value.length === 0) {
    fetchRatings()
  }
}

// 格式化时间
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 监听属性变化
watch(() => props.chapterId, () => {
  if (props.chapterId) {
    fetchUserRating()
    fetchAverageRating()
    showRatings.value = false
  }
}, { immediate: true })

onMounted(() => {
  if (props.chapterId) {
    fetchUserRating()
    fetchAverageRating()
  }
})
</script>

<template>
  <div class="chapter-rating-component" v-loading="loading">
    <div class="rating-header flex justify-between items-center mb-4">
      <h3 class="text-lg font-medium">章节评价</h3>
      <div class="average-rating flex items-center">
        <span class="text-sm text-gray-500 mr-2">平均评分:</span>
        <el-rate
          v-model="averageRating"
          disabled
          show-score
          text-color="#ff9900"
          score-template="{value}"
        />
      </div>
    </div>
    
    <!-- 用户评价表单 -->
    <div class="rating-form bg-gray-50 p-4 rounded-lg mb-4">
      <div class="mb-3">
        <label class="block text-sm text-gray-600 mb-2">您的评分</label>
        <el-rate
          v-model="userRating"
          allow-half
          show-text
          :texts="['很差', '较差', '一般', '不错', '很好']"
        />
      </div>
      
      <div class="mb-3">
        <label class="block text-sm text-gray-600 mb-2">反馈意见 (可选)</label>
        <textarea 
          v-model="feedback" 
          rows="3" 
          placeholder="请分享您对本章节的看法..." 
          class="w-full p-2 border rounded-lg text-sm"
        ></textarea>
      </div>
      
      <div class="flex justify-end">
        <button 
          @click="submitRating" 
          class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700 transition-colors"
          :disabled="submitting"
        >
          <i v-if="submitting" class="fas fa-spinner fa-spin mr-1"></i>
          {{ userRating ? '更新评价' : '提交评价' }}
        </button>
      </div>
    </div>
    
    <!-- 其他评价 -->
    <div class="other-ratings">
      <div class="flex justify-between items-center mb-2">
        <h4 class="text-base font-medium">其他学员评价</h4>
        <button 
          @click="toggleRatings" 
          class="text-sm text-blue-600 hover:text-blue-800"
        >
          {{ showRatings ? '收起评价' : '查看评价' }}
        </button>
      </div>
      
      <div v-if="showRatings" class="ratings-list space-y-3">
        <div v-if="ratings.length === 0" class="text-center py-4 text-gray-400">
          暂无其他评价
        </div>
        
        <div v-for="rating in ratings" :key="rating.id" class="rating-item bg-white p-3 rounded-lg border border-gray-100">
          <div class="flex justify-between items-start">
            <div class="user-info flex items-center">
              <img :src="rating.userAvatar || '/avatars/default.png'" class="w-8 h-8 rounded-full mr-2" />
              <span class="text-sm font-medium">{{ rating.userName }}</span>
            </div>
            
            <el-rate
              v-model="rating.rating"
              disabled
              text-color="#ff9900"
            />
          </div>
          
          <div v-if="rating.feedback" class="mt-2 text-sm text-gray-700">
            {{ rating.feedback }}
          </div>
          
          <div class="mt-1 text-xs text-gray-500">
            {{ formatDate(rating.createdAt) }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chapter-rating-component {
  padding: 1rem;
}

.rating-item {
  transition: all 0.2s ease;
}

.rating-item:hover {
  border-color: #e2e8f0;
}
</style> 