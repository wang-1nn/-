<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElTabs, ElTabPane } from 'element-plus'
import Resources from './Resources.vue'
import DiscussionPanel from '../../components/student/course/DiscussionPanel.vue'
import QuizList from '../../components/student/course/QuizList.vue'
import { get, post } from '@/net'
import { useAuthStore } from '@/stores/counter'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const courseId = ref(route.params.courseId ? route.params.courseId.toString() : null)
const activeTab = ref('resources')
const loading = ref(true)
const chaptersLoading = ref(false)

// 课程数据
const course = ref({
  title: '',
  teacher: '',
  department: '',
  credit: 0,
  startDate: '',
  endDate: '',
  progress: 0,
  schedule: [],
  announcements: [],
  description: '',
  image: '',
  chapters: [] // 添加章节数组
})

// 收藏状态
const isFavorite = ref(false)

// 获取课程详情
const fetchCourseDetail = () => {
  loading.value = true
  
  get(`/api/student/courses/${courseId.value}`, 
    { userId: authStore.user?.userId }, 
    (message, data) => {
      console.log("课程详情数据:", data);
      course.value = {
        ...course.value,
        ...data,
        teacher: data.teacherName,
        department: data.department,
        credit: data.credits
      }
      isFavorite.value = data.isFavorite || false
      loading.value = false
      
      // 获取课程章节
      fetchChapters()
    },
    (message) => {
      ElMessage.error(message || '获取课程详情失败')
      loading.value = false
    }
  )
}

// 获取课程章节
const fetchChapters = () => {
  chaptersLoading.value = true
  
  get(`/api/student/courses/${courseId.value}/chapters`, 
    null, 
    (message, data) => {
      console.log("课程章节数据:", data);
      course.value.chapters = data || []
      chaptersLoading.value = false
    },
    (message) => {
      ElMessage.error(message || '获取课程章节失败')
      chaptersLoading.value = false
    }
  )
}

const toggleFavorite = () => {
  post('/api/favorites/toggle', 
    { 
      userId: authStore.user?.userId,
      courseId: courseId.value
    }, 
    (message, data) => {
      isFavorite.value = data.isFavorite
  ElMessage.success(isFavorite.value ? '已添加到收藏' : '已取消收藏')
    },
    (message) => {
      ElMessage.error(message || '操作失败')
    },undefined,false
  )
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '暂无日期'
  const date = new Date(dateStr)
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
}

// 计算剩余天数
const remainingDays = computed(() => {
  if (!course.value.endDate) return 0
  const today = new Date()
  const end = new Date(course.value.endDate)
  const diffTime = end - today
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24))
})

// 跳转到学习页面
const goToLearning = (chapterId, chapterTitle) => {
  router.replace({
    name: 'LearningCenter',
    params: { courseId: courseId.value.toString(), chapterId: chapterId.toString() },
    query: { title: chapterTitle }
  })
}

onMounted(() => {
  fetchCourseDetail()
})

// 监听路由变化，确保切换课程时重新加载数据
watch(() => route.params.courseId, (newId, oldId) => {
  if (newId && newId !== oldId) {
    courseId.value = newId ? newId.toString() : null
    fetchCourseDetail()
  }
}, { immediate: true })

// 计算章节完成状态
const getChapterStatus = (chapter) => {
  // 这里应该根据后端返回的数据判断章节完成状态
  // 暂时简单返回，实际应用中可能需要更复杂的逻辑
  return {
    isCompleted: chapter.progress === 100,
    progress: chapter.progress || 0
  }
}
</script>

<template>
  <div class="course-detail-wrapper p-4 md:p-6" v-loading="loading">
    <!-- 顶部封面 -->
    <div class="cover relative mb-6 rounded-2xl overflow-hidden shadow-lg">
      <img :src="course.image || 'https://placehold.co/800x400/e2e8f0/475569?text=Course+Image'" class="w-full h-48 md:h-64 object-cover" />
      <div class="absolute inset-0 bg-gradient-to-t from-black/70 to-transparent"></div>
      <div class="absolute bottom-0 left-0 w-full p-6 text-white">
        <div class="flex justify-between items-end">
          <div>
            <h2 class="text-2xl md:text-3xl font-bold mb-1">{{ course.title }}</h2>
            <p class="text-sm md:text-base opacity-90">{{ course.teacher }} • {{ course.department }}</p>
          </div>
          <div class="flex gap-3">
            <button @click="toggleFavorite" class="bg-white/20 hover:bg-white/30 backdrop-blur-sm text-white rounded-full p-2 transition duration-200">
              <i :class="['fa-star text-lg', isFavorite ? 'fas text-yellow-400' : 'far']"></i>
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 课程信息卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
      <!-- 基本信息 -->
      <div class="bg-white rounded-xl shadow-md p-5">
        <h3 class="text-lg font-semibold mb-4 flex items-center">
          <i class="fas fa-info-circle mr-2 text-blue-500"></i> 课程信息
        </h3>
        <div class="space-y-3 text-sm">
          <div class="flex justify-between">
            <span class="text-gray-500">学分：</span>
            <span class="font-medium">{{ course.credit }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-500">开始日期：</span>
            <span class="font-medium">{{ formatDate(course.startDate) }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-500">结束日期：</span>
            <span class="font-medium">{{ formatDate(course.endDate) }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-500">剩余天数：</span>
            <span class="font-medium">{{ remainingDays }}天</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-500">学习进度：</span>
            <span class="font-medium">{{ course.progress }}%</span>
          </div>
          <div class="mt-4 pt-3 border-t">
            <div class="w-full bg-gray-200 rounded-full h-2.5">
              <div class="bg-blue-600 h-2.5 rounded-full" :style="{width: (course.progress || 0) + '%'}"></div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 课程章节 -->
      <div class="bg-white rounded-xl shadow-md p-5" v-loading="chaptersLoading">
        <h3 class="text-lg font-semibold mb-4 flex items-center">
          <i class="fas fa-book-open mr-2 text-green-500"></i> 课程章节
        </h3>
        <div class="space-y-4">
          <div v-if="course.chapters && course.chapters.length > 0">
            <div v-for="chapter in course.chapters" :key="chapter.id" 
                class="flex items-start p-2 hover:bg-gray-50 rounded-lg cursor-pointer transition-colors"
                @click="goToLearning(chapter.id, chapter.title)">
              <div class="bg-green-100 text-green-700 rounded-lg p-2 mr-3 flex-shrink-0">
                <i class="fas fa-book"></i>
              </div>
              <div class="flex-1">
                <div class="font-medium">{{ chapter.title }}</div>
                <div class="text-sm text-gray-500">
                  {{ chapter.description || '点击开始学习' }}
                  <span v-if="chapter.duration" class="ml-2">
                    <i class="fas fa-clock text-xs"></i> {{ chapter.duration }}分钟
                  </span>
                </div>
              </div>
              <div class="ml-auto flex flex-col items-end">
                <i class="fas fa-chevron-right text-gray-400"></i>
                <div v-if="getChapterStatus(chapter).progress > 0" 
                     class="text-xs text-gray-500 mt-1">
                  {{ getChapterStatus(chapter).progress }}%
                </div>
              </div>
            </div>
          </div>
          <div v-else class="text-gray-400 text-center py-4">
            暂无章节内容
          </div>
        </div>
      </div>
      
      <!-- 课程公告 -->
      <div class="bg-white rounded-xl shadow-md p-5">
        <h3 class="text-lg font-semibold mb-4 flex items-center">
          <i class="fas fa-bullhorn mr-2 text-amber-500"></i> 课程公告
        </h3>
        <div class="space-y-3">
          <div v-if="course.announcements && course.announcements.length > 0">
            <div v-for="announcement in course.announcements" :key="announcement.id" 
                class="border-l-2 border-amber-400 pl-3 py-1">
              <div class="font-medium">{{ announcement.title }}</div>
              <div class="text-xs text-gray-500 mb-1">{{ formatDate(announcement.publishTime) }}</div>
              <div class="text-sm text-gray-600">{{ announcement.content }}</div>
            </div>
          </div>
          <div v-else class="text-gray-400 text-center py-4">
            暂无公告
          </div>
        </div>
      </div>
    </div>
    
    <!-- 课程描述 -->
    <div class="bg-white rounded-xl shadow-md p-5 mb-6">
      <h3 class="text-lg font-semibold mb-3 flex items-center">
        <i class="fas fa-book-open mr-2 text-purple-500"></i> 课程简介
      </h3>
      <p class="text-gray-600">{{ course.description || '暂无课程简介' }}</p>
    </div>

    <!-- Tabs -->
    <div class="bg-white rounded-xl shadow-md overflow-hidden">
      <div class="border-b">
        <div class="flex">
          <button 
            @click="activeTab = 'resources'" 
            class="px-6 py-3 text-center text-gray-600 hover:bg-gray-50 transition-colors"
            :class="{'text-blue-600 border-b-2 border-blue-500': activeTab === 'resources'}"
          >
            <i class="fas fa-book mr-2"></i> 学习资源
          </button>
          
          <button 
            @click="activeTab = 'discussion'" 
            class="px-6 py-3 text-center text-gray-600 hover:bg-gray-50 transition-colors"
            :class="{'text-blue-600 border-b-2 border-blue-500': activeTab === 'discussion'}"
          >
            <i class="fas fa-comments mr-2"></i> 课程讨论
          </button>
          
          <button 
            @click="activeTab = 'tests'" 
            class="px-6 py-3 text-center text-gray-600 hover:bg-gray-50 transition-colors"
            :class="{'text-blue-600 border-b-2 border-blue-500': activeTab === 'tests'}"
          >
            <i class="fas fa-tasks mr-2"></i> 练习 & 测验
          </button>
          
          <button 
            @click="activeTab = 'learning'" 
            class="px-6 py-3 text-center text-gray-600 hover:bg-gray-50 transition-colors"
            :class="{'text-blue-600 border-b-2 border-blue-500': activeTab === 'learning'}"
          >
            <i class="fas fa-graduation-cap mr-2"></i> 课程学习
          </button>
        </div>
      </div>
      
      <div class="p-4">
        <div v-if="activeTab === 'resources'">
          <Resources />
        </div>
        <div v-else-if="activeTab === 'discussion'">
          <DiscussionPanel />
        </div>
        <div v-else-if="activeTab === 'tests'">
          <QuizList />
        </div>
        <div v-else-if="activeTab === 'learning'">
          <!-- 课程学习内容 -->
          <div class="course-learning">
            <h3 class="text-lg font-medium mb-4">课程章节列表</h3>
            
            <div class="chapters-list space-y-4" v-loading="chaptersLoading">
              <div v-if="course.chapters && course.chapters.length > 0">
                <div v-for="chapter in course.chapters" :key="chapter.id" 
                    class="chapter-item bg-white border rounded-lg overflow-hidden shadow-sm hover:shadow-md transition-shadow">
                  <div class="chapter-header p-4 flex items-center justify-between cursor-pointer"
                       @click="goToLearning(chapter.id, chapter.title)">
                    <div class="flex items-center">
                      <div class="chapter-icon bg-blue-100 text-blue-600 rounded-full p-2 mr-3">
                        <i class="fas fa-book"></i>
                      </div>
                      <div>
                        <h4 class="font-medium text-lg">{{ chapter.title }}</h4>
                        <p class="text-sm text-gray-500">
                          {{ chapter.description || '点击开始学习' }}
                          <span v-if="chapter.duration" class="ml-2">
                            <i class="fas fa-clock text-xs"></i> {{ chapter.duration }}分钟
                          </span>
                        </p>
                      </div>
                    </div>
                    <div class="flex items-center">
                      <div v-if="getChapterStatus(chapter).progress > 0" class="mr-4">
                        <div class="text-sm text-gray-500">进度: {{ getChapterStatus(chapter).progress }}%</div>
                        <div class="w-24 bg-gray-200 rounded-full h-1.5 mt-1">
                          <div class="bg-blue-600 h-1.5 rounded-full" :style="{width: getChapterStatus(chapter).progress + '%'}"></div>
                        </div>
                      </div>
                      <button class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
                        开始学习
                      </button>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="text-center py-8 text-gray-400">
                <i class="fas fa-book-open text-4xl mb-2"></i>
                <p>暂无章节内容</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cover img{ transition:transform .8s ease; }
.cover:hover img{ transform:scale(1.05); }

/* 添加页面进入动画 */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.course-detail-wrapper {
  animation: fadeIn 0.3s ease-out;
}
</style> 