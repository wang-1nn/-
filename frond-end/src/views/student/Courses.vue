<script setup>
import { ref, computed, onMounted, reactive, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight, BookCheck, Clock, Layers, TrendingUp } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/counter'
import { ElMessage } from 'element-plus'
import { get, post } from '@/net'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const showWelcome = ref(true)
const cardsVisible = ref(false)

// ===== 数据获取 & 状态 =====
const studentName = computed(() => authStore.user?.name || '同学')
const courses = ref([])

const search = ref('')
const category = ref('all')
const sortOrder = ref('default') // 默认排序
const viewMode = ref('grid') // 网格或列表视图

// 动画相关
const animationDelay = 0.05 // 卡片之间的动画延迟（秒）
const getAnimationDelay = (index) => `${index * animationDelay}s`

// ===== 学习统计 Computed =====
const inProgressCourses = computed(() => courses.value.filter(c => c.progress > 0 && c.progress < 100).length)
const completedCourses = computed(() => courses.value.filter(c => c.progress === 100).length)
const averageProgress = computed(() => {
  if (courses.value.length === 0) return 0
  const total = courses.value.reduce((sum, c) => sum + c.progress, 0)
  return Math.round(total / courses.value.length)
})
const lastStudiedCourse = computed(() => {
  return [...courses.value]
    .filter(c => c.lastVisitTime)
    .sort((a, b) => new Date(b.lastVisitTime) - new Date(a.lastVisitTime))[0]
})

// ===== 课程列表 Computed (过滤与排序) =====
const filteredAndSortedCourses = computed(() => {
  let filtered = courses.value.filter(c => {
    const matchCat = category.value === 'all' || 
                    (category.value === 'major' && c.courseType === '专业课') ||
                    (category.value === 'elective' && c.courseType === '选修课')
    const matchSearch = c.title.toLowerCase().includes(search.value.toLowerCase()) || 
                       (c.teacherName && c.teacherName.toLowerCase().includes(search.value.toLowerCase()))
    
    // 增加进行中和已完成的筛选逻辑
    if (category.value === 'in-progress') return matchSearch && c.progress > 0 && c.progress < 100;
    if (category.value === 'completed') return matchSearch && c.progress === 100;
    if (category.value === 'favorite') return matchSearch && c.isFavorite;

    return matchCat && matchSearch
  })

  // 排序
  switch (sortOrder.value) {
    case 'progress_asc':
      return filtered.sort((a, b) => a.progress - b.progress)
    case 'progress_desc':
      return filtered.sort((a, b) => b.progress - a.progress)
    case 'recent':
      return filtered.sort((a, b) => {
        if (!a.lastVisitTime) return 1
        if (!b.lastVisitTime) return -1
        return new Date(b.lastVisitTime) - new Date(a.lastVisitTime)
      })
    case 'name_asc':
      return filtered.sort((a, b) => a.title.localeCompare(b.title))
    default:
      return filtered // 默认不排序或按ID排序
  }
})

// 获取课程进度颜色
const getProgressColor = (progress) => {
  if (progress === 100) return '#67c23a' // 完成
  if (progress >= 60) return '#6366f1' // 良好进度
  if (progress >= 30) return '#f59e0b' // 中等进度
  return '#3b82f6' // 开始阶段
}

// 切换收藏状态
const toggleFavorite = (course, event) => {
  event.stopPropagation()
  
  post('/api/favorites/toggle', 
    { 
      userId: authStore.user?.userId,
      courseId: course.id
    }, 
    (message, data) => {
      course.isFavorite = data.isFavorite
  ElMessage({
        message: course.isFavorite ? `已将 ${course.title} 添加到收藏` : `已取消收藏 ${course.title}`,
    type: 'success',
    duration: 1500
  })
    },
    (message) => {
      ElMessage.error(message || '操作失败')
    }
  )
}

// 获取课程列表
const fetchCourses = () => {
  loading.value = true
  
  get('/api/student/courses', 
    { userId: authStore.user?.userId },
    (message, data) => {
      console.log("课程列表数据:", data);
      courses.value = data
      loading.value = false
      
      // 欢迎消息显示一段时间后消失
      setTimeout(() => {
        showWelcome.value = false
      }, 3000)
      
      // 课程卡片逐个显示
      nextTick(() => {
        cardsVisible.value = true
        
        // 重新初始化AOS以应用到新加载的元素
        setTimeout(() => {
          if (window.AOS) {
            window.AOS.refresh()
          }
        }, 100)
      })
    },
    (message) => {
      ElMessage.error(message || '获取课程列表失败')
      loading.value = false
    }
  )
}

onMounted(() => {
  loading.value = true
  fetchCourses()
})

// 跳转到课程详情页
const goToCourse = (courseId) => {
  router.push(`/student/course/${courseId}`)
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '未学习'
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}月${date.getDate()}日`
}
</script>

<template>
  <div class="courses-page-wrapper" v-loading="loading">
    <!-- 欢迎提示 -->
    <transition name="welcome">
      <div v-if="showWelcome" class="welcome-message">
        <div class="welcome-icon">👋</div>
        <div class="welcome-text">欢迎回来，{{ studentName }}！</div>
      </div>
    </transition>
    
    <!-- 英雄区域 -->
    <div class="hero-section" data-aos="fade-down">
      <div class="hero-content">
        <h1 class="hero-title">我的课程</h1>
        <p class="hero-subtitle">继续你的学习之旅，不断进步。</p>
        <button v-if="lastStudiedCourse" @click="goToCourse(lastStudiedCourse.id)" class="hero-cta">
          <Layers :size="18" class="mr-2" />
          继续学习: {{ lastStudiedCourse.title }}
          <ArrowRight :size="16" class="ml-auto" />
        </button>
      </div>
      <div class="hero-stats">
        <div class="stat-card" data-aos="zoom-in" data-aos-delay="100">
          <BookCheck :size="24" class="text-green-500" />
          <div class="stat-value">{{ completedCourses }}</div>
          <div class="stat-label">已完成课程</div>
        </div>
        <div class="stat-card" data-aos="zoom-in" data-aos-delay="200">
          <Clock :size="24" class="text-blue-500" />
          <div class="stat-value">{{ inProgressCourses }}</div>
          <div class="stat-label">在学课程</div>
        </div>
        <div class="stat-card" data-aos="zoom-in" data-aos-delay="300">
          <TrendingUp :size="24" class="text-purple-500" />
          <div class="stat-value">{{ averageProgress }}%</div>
          <div class="stat-label">平均进度</div>
        </div>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar" data-aos="fade-up">
      <div class="filter-group">
        <el-radio-group v-model="category" size="small">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="major">专业课</el-radio-button>
          <el-radio-button label="elective">选修</el-radio-button>
          <el-radio-button label="in-progress">进行中</el-radio-button>
          <el-radio-button label="completed">已完成</el-radio-button>
          <el-radio-button label="favorite">收藏</el-radio-button>
        </el-radio-group>
      </div>
      <div class="actions-group">
        <el-input v-model="search" placeholder="搜索课程..." size="small" clearable prefix-icon="el-icon-search" class="w-48" />
        <el-select v-model="sortOrder" size="small" class="w-36">
          <el-option label="默认排序" value="default" />
          <el-option label="按进度升序" value="progress_asc" />
          <el-option label="按进度降序" value="progress_desc" />
          <el-option label="最近学习" value="recent" />
          <el-option label="按名称排序" value="name_asc" />
        </el-select>
        <div class="view-toggle">
          <button @click="viewMode = 'grid'" :class="{'active': viewMode === 'grid'}">
            <i class="fas fa-th-large"></i>
          </button>
          <button @click="viewMode = 'list'" :class="{'active': viewMode === 'list'}">
            <i class="fas fa-list"></i>
          </button>
        </div>
      </div>
    </div>

    <!-- 课程网格视图 -->
    <transition-group 
      name="stagger" 
      tag="div" 
      class="course-grid" 
      v-if="viewMode === 'grid' && cardsVisible"
      data-aos="fade-up"
    >
      <div 
        v-for="(course, index) in filteredAndSortedCourses" 
        :key="course.id" 
        class="course-card group" 
        @click="goToCourse(course.id)"
        :style="{'animation-delay': getAnimationDelay(index)}"
        :data-aos="index % 2 === 0 ? 'fade-right' : 'fade-left'"
        :data-aos-delay="index * 50"
      >
        <div class="thumb" :style="{ backgroundImage: course.image ? `url(${course.image})` : 'url(https://placehold.co/600x400/e2e8f0/475569?text=Course)' }">
          <div class="overlay"></div>
          <div class="progress" :style="{ width: course.progress + '%', backgroundColor: getProgressColor(course.progress) }"></div>
          <span v-if="course.courseType === '专业课'" class="category-badge major">专业课</span>
          <span v-else class="category-badge elective">选修</span>
          
          <!-- 收藏按钮 -->
          <button @click="toggleFavorite(course, $event)" class="favorite-btn">
            <i :class="['fas', course.isFavorite ? 'fa-heart text-red-500' : 'fa-heart text-white/60']"></i>
          </button>
          
          <!-- 最近学习标记 -->
          <div v-if="course.lastVisitTime" class="last-visited">
            <i class="fas fa-clock mr-1"></i> {{ formatDate(course.lastVisitTime) }}
          </div>
        </div>
        <div class="info">
          <h3 class="title">{{ course.title }}</h3>
          <p class="teacher">
            <i class="fas fa-user-tie mr-1 text-gray-400"></i> {{ course.teacherName }}
          </p>
          <div class="progress-bar-wrapper">
            <el-progress 
              :percentage="course.progress" 
              :stroke-width="8" 
              :color="getProgressColor(course.progress)" 
            />
            <div class="progress-text">
              {{ course.progress === 100 ? '已完成' : `已完成 ${course.progress}%` }}
            </div>
          </div>
        </div>
      </div>
    </transition-group>
    
    <!-- 课程列表视图 -->
    <div 
      class="course-list-view" 
      v-if="viewMode === 'list' && cardsVisible"
      data-aos="fade-up"
    >
      <div 
        v-for="(course, index) in filteredAndSortedCourses" 
        :key="course.id" 
        class="course-list-item" 
        @click="goToCourse(course.id)"
        :style="{'animation-delay': getAnimationDelay(index)}"
        :data-aos="'fade-up'"
        :data-aos-delay="index * 50"
      >
        <div class="list-thumb" :style="{ backgroundImage: course.image ? `url(${course.image})` : 'url(https://placehold.co/600x400/e2e8f0/475569?text=Course)' }">
          <div v-if="course.progress === 100" class="completed-badge">
            <i class="fas fa-check"></i>
          </div>
        </div>
        <div class="list-info">
          <div class="list-title-row">
            <h3 class="list-title">{{ course.title }}</h3>
            <button @click="toggleFavorite(course, $event)" class="list-favorite-btn">
              <i :class="['fas', course.isFavorite ? 'fa-heart text-red-500' : 'fa-heart text-gray-300']"></i>
            </button>
          </div>
          <div class="list-meta">
            <span class="list-teacher">
              <i class="fas fa-user-tie mr-1"></i> {{ course.teacherName }}
            </span>
                          <span v-if="course.courseType === '专业课'" class="list-category major">专业课</span>
            <span v-else class="list-category elective">选修</span>
            <span class="list-last-visit" v-if="course.lastVisitTime">
              <i class="fas fa-clock mr-1"></i> {{ formatDate(course.lastVisitTime) }}
            </span>
          </div>
          <div class="list-progress-wrapper">
            <el-progress 
              :percentage="course.progress" 
              :stroke-width="6" 
              :color="getProgressColor(course.progress)" 
            />
            <div class="list-progress-text">{{ course.progress }}%</div>
          </div>
        </div>
        <div class="list-action">
          <button class="list-action-btn">
            <i class="fas fa-chevron-right"></i>
          </button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div 
      v-if="!loading && filteredAndSortedCourses.length === 0" 
      class="empty-state"
      data-aos="zoom-in"
    >
      <i class="fas fa-search text-gray-300 text-5xl mb-3"></i>
      <p class="text-gray-500 mb-2">没有找到匹配的课程</p>
      <p class="text-gray-400 text-sm">尝试调整筛选条件</p>
    </div>
  </div>
</template>

<style scoped>
/* ===== 页面整体 ===== */
.courses-page-wrapper {
  @apply p-6 bg-gray-50 min-h-screen;
}

/* ===== 欢迎提示 ===== */
.welcome-message {
  @apply fixed top-6 right-6 bg-white rounded-full shadow-lg py-3 px-5 flex items-center gap-3 z-20;
  animation: bounce-in 0.5s ease-out;
}
.welcome-icon {
  @apply text-2xl animate-wave;
}
.welcome-text {
  @apply font-medium;
}
@keyframes bounce-in {
  0% { transform: scale(0.5); opacity: 0; }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); opacity: 1; }
}
@keyframes wave {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(-15deg); }
  75% { transform: rotate(15deg); }
}
.animate-wave {
  animation: wave 1s infinite;
}
.welcome-enter-active, .welcome-leave-active {
  transition: all 0.5s ease;
}
.welcome-enter-from, .welcome-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* ===== 英雄区域 ===== */
.hero-section {
  @apply bg-gradient-to-br from-indigo-500 to-purple-600 text-white rounded-2xl p-8 mb-8 flex flex-col md:flex-row items-center justify-between shadow-2xl shadow-purple-200;
  animation: slide-in-down 0.6s ease-out;
}
.hero-content { @apply mb-6 md:mb-0; }
.hero-title { @apply text-3xl font-bold mb-2; }
.hero-subtitle { @apply text-indigo-200 mb-6; }
.hero-cta {
  @apply bg-white/20 hover:bg-white/30 text-white font-semibold py-2 px-4 rounded-lg flex items-center transition-all duration-300 transform hover:scale-105 hover:shadow-lg;
  backdrop-filter: blur(4px);
}
.hero-stats { @apply grid grid-cols-3 gap-4 text-center w-full md:w-auto; }
.stat-card {
  @apply bg-white/10 p-4 rounded-xl backdrop-blur-sm flex flex-col items-center justify-center transition-transform duration-300 hover:transform hover:scale-105;
  min-width: 100px;
}
.stat-value { @apply text-2xl font-bold; }
.stat-label { @apply text-xs text-indigo-200; }

@keyframes slide-in-down {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ===== 工具栏 ===== */
.toolbar {
  @apply flex flex-wrap gap-4 items-center justify-between bg-white/80 backdrop-blur-lg p-3 rounded-xl shadow-md mb-8 sticky top-4 z-10;
  animation: fade-in 0.8s ease-out;
}
.filter-group .el-radio-button :deep(.el-radio-button__inner) {
  border: none;
  background-color: transparent;
}
.filter-group .el-radio-button.is-active :deep(.el-radio-button__inner) {
  @apply bg-indigo-100 text-indigo-600 shadow-sm;
}
.actions-group { @apply flex items-center gap-3; }
.view-toggle {
  @apply flex rounded-md overflow-hidden border;
}
.view-toggle button {
  @apply px-2 py-1 text-sm bg-white text-gray-600 transition-colors duration-200;
}
.view-toggle button.active {
  @apply bg-blue-500 text-white;
}

@keyframes fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* ===== 课程网格与卡片 ===== */
.course-grid {
  @apply grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6;
}
.course-card {
  @apply bg-white rounded-2xl shadow-lg overflow-hidden transition-all duration-300 transform hover:-translate-y-2 hover:shadow-2xl cursor-pointer;
  animation: fade-in-up 0.5s ease-out forwards;
  opacity: 0;
}
/* 卡片动画 */
.stagger-enter-active { transition: all .4s ease; }
.stagger-enter-from { opacity: 0; transform: translateY(30px); }
.stagger-enter-to { opacity: 1; transform: translateY(0); }

@keyframes fade-in-up {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.thumb {
  @apply h-40 bg-cover bg-center relative overflow-hidden;
}
.thumb::after {
  content: '';
  @apply absolute inset-0 bg-black/10 opacity-0 transition-opacity duration-300;
}
.course-card:hover .thumb::after {
  @apply opacity-100;
}
.overlay {
  @apply absolute inset-0 bg-gradient-to-t from-black/60 to-transparent;
}
.progress {
  @apply absolute bottom-0 left-0 h-1.5 transition-all duration-500 ease-out;
}
.category-badge {
  @apply absolute top-3 left-3 text-xs font-semibold px-2 py-1 rounded-full text-white backdrop-blur-sm;
}
.category-badge.major { @apply bg-sky-500/80; }
.category-badge.elective { @apply bg-amber-500/80; }

.favorite-btn {
  @apply absolute top-3 right-3 w-8 h-8 rounded-full flex items-center justify-center bg-black/20 backdrop-blur-sm hover:bg-black/30 transition-all duration-200;
}

.last-visited {
  @apply absolute bottom-3 right-3 text-xs bg-black/30 text-white px-2 py-1 rounded-full backdrop-blur-sm;
}

.info {
  @apply p-5 flex flex-col;
  min-height: 140px;
}
.title {
  @apply text-lg font-bold text-gray-800 mb-1 truncate group-hover:text-indigo-600 transition-colors;
}
.teacher {
  @apply text-sm text-gray-500 mb-4 flex items-center;
}
.progress-bar-wrapper {
  @apply mt-auto relative;
}
.progress-text {
  @apply text-xs text-gray-500 mt-1 text-right;
}

/* ===== 列表视图 ===== */
.course-list-view {
  @apply space-y-4;
}
.course-list-item {
  @apply bg-white rounded-xl shadow-md overflow-hidden flex cursor-pointer hover:shadow-lg transition-all duration-300;
  animation: fade-in-up 0.5s ease-out forwards;
  opacity: 0;
}
.list-thumb {
  @apply w-24 h-24 bg-cover bg-center relative;
}
.completed-badge {
  @apply absolute inset-0 bg-green-500/80 flex items-center justify-center text-white text-xl;
}
.list-info {
  @apply p-4 flex-1;
}
.list-title-row {
  @apply flex justify-between items-start;
}
.list-title {
  @apply text-lg font-semibold;
}
.list-favorite-btn {
  @apply p-1;
}
.list-meta {
  @apply flex flex-wrap gap-3 text-sm text-gray-500 my-1;
}
.list-category {
  @apply px-2 py-0.5 rounded-full text-xs font-medium;
}
.list-category.major { @apply bg-sky-100 text-sky-700; }
.list-category.elective { @apply bg-amber-100 text-amber-700; }
.list-progress-wrapper {
  @apply mt-2 flex items-center gap-3;
}
.list-progress-text {
  @apply text-sm font-medium;
}
.list-action {
  @apply flex items-center pr-4;
}
.list-action-btn {
  @apply w-8 h-8 rounded-full flex items-center justify-center text-gray-400 hover:bg-gray-100 hover:text-gray-600;
}

/* ===== 空状态 ===== */
.empty-state {
  @apply text-center py-20 flex flex-col items-center;
  animation: fade-in 1s ease-out;
}

/* 添加鼠标悬停效果 */
.course-card:hover {
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  transform: translateY(-8px) scale(1.01);
}

.course-card:hover .title {
  color: #4f46e5;
}

.course-card:hover .progress {
  height: 3px;
}

.course-list-item {
  transition: all 0.3s ease;
}

.course-list-item:hover {
  transform: translateX(5px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
}

/* 添加脉冲动画效果 */
@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(99, 102, 241, 0.4);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(99, 102, 241, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(99, 102, 241, 0);
  }
}

.hero-cta {
  animation: pulse 2s infinite;
}

/* 改进响应式布局 */
@media (max-width: 640px) {
  .course-grid {
    grid-template-columns: 1fr;
  }
  
  .hero-section {
    padding: 1.5rem;
  }
  
  .hero-stats {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .stat-card {
    flex-direction: row;
    justify-content: flex-start;
    gap: 1rem;
    padding: 0.75rem;
  }
  
  .welcome-message {
    width: 90%;
    left: 5%;
    right: 5%;
  }
}
</style> 