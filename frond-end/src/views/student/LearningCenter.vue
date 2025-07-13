<script setup>
import { ref, onMounted, computed, watch, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElTabs, ElTabPane, ElProgress } from 'element-plus'
import { get, post } from '@/net'
import { useAuthStore } from '@/stores/counter'
import { TrendingUp, Clock, BookCheck, Award, Target } from 'lucide-vue-next'
import LearningNotes from '@/components/student/learning/LearningNotes.vue'
import ChapterRating from '@/components/student/learning/ChapterRating.vue'
import LearningStatistics from '@/components/student/learning/LearningStatistics.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const loading = ref(true)
const chapterLoading = ref(false)
const contentLoading = ref(false)

// 获取路由参数
const courseId = ref(route.params.courseId ? route.params.courseId.toString() : null)
const chapterId = ref(route.params.chapterId ? route.params.chapterId.toString() : null)
const chapterTitle = ref(route.query.title || '章节学习')

// 学习内容数据
const courseInfo = ref({
  title: '',
  teacherName: '',
  image: ''
})

// 所有章节列表
const allChapters = ref([])

// 当前章节信息
const chapterInfo = ref({
  id: null,
  title: '',
  description: '',
  contents: []
})

const currentContent = ref(null)
const contentIndex = ref(0)

// 学习进度
const progress = ref(0)
const lastPosition = ref('')
const studyStartTime = ref(null)
const activeTime = ref(0) // 活跃学习时间（秒）
const timeTracker = ref(null) // 计时器

// 章节展开状态
const expandedChapters = ref({})

// 添加标签页控制
const activeTab = ref('content')

// 获取课程信息
const fetchCourseInfo = () => {
  if (!courseId.value) return
  
  loading.value = true // 设置整体页面的加载状态
  
  get(`/api/student/courses/${courseId.value}`, 
    { userId: authStore.user?.userId }, 
    (message, data) => {
      courseInfo.value = {
        title: data.title,
        teacherName: data.teacherName,
        image: data.image
      }
      
      // 获取所有章节
      fetchAllChapters()
    },
    (message) => {
      ElMessage.error(message || '获取课程信息失败')
      loading.value = false // 失败时取消加载状态
    },
    (error) => {
      console.error("获取课程信息异常:", error)
      ElMessage.error("获取课程信息异常，请稍后重试")
      loading.value = false // 异常时取消加载状态
    }
  )
}

// 获取所有章节
const fetchAllChapters = () => {
  if (!courseId.value) {
    loading.value = false // 如果没有课程ID，直接取消加载状态
    return
  }
  
  get(`/api/student/courses/${courseId.value}/chapters`, 
    null, 
    (message, data) => {
      allChapters.value = data || []
      
      // 如果有章节ID，则展开该章节
      if (chapterId.value) {
        expandedChapters.value[chapterId.value] = true
      }
      
      // 获取当前章节内容
      if (chapterId.value) {
        fetchChapterContents()
      } else if (allChapters.value.length > 0) {
        // 如果没有指定章节，则默认选择第一个章节
        navigateToChapter(allChapters.value[0].id, allChapters.value[0].title)
      } else {
        // 没有章节，取消加载状态
        loading.value = false
      }
    },
    (message) => {
      ElMessage.error(message || '获取课程章节失败')
      // 失败时取消加载状态
      loading.value = false
    },
    (error) => {
      console.error("获取课程章节异常:", error)
      ElMessage.error("获取课程章节异常，请稍后重试")
      // 异常时取消加载状态
      loading.value = false
    }
  )
}

// 获取章节内容
const fetchChapterContents = () => {
  if (!courseId.value || !chapterId.value) {
    loading.value = false // 如果没有必要参数，直接取消加载状态
    return
  }
  
  chapterLoading.value = true
  
  // 确保传递用户ID参数
  const userId = authStore.user?.userId
  if (!userId) {
    ElMessage.error('获取用户信息失败，请重新登录')
    chapterLoading.value = false
    loading.value = false // 取消整体加载状态
    return
  }
  
  get(`/api/student/courses/${courseId.value}/chapters/${chapterId.value}`, 
    { userId: userId }, 
    (message, data) => {
      console.log("章节内容数据:", data)
      chapterInfo.value = data || { title: chapterTitle.value, contents: [] }
      
      // 如果有内容，显示第一个
      if (chapterInfo.value.contents && chapterInfo.value.contents.length > 0) {
        switchContent(0)
      } else {
        currentContent.value = null
        contentIndex.value = 0
        console.warn("章节内容列表为空")
        ElMessage.warning("该章节暂无学习内容")
      }
      
      // 设置进度
      progress.value = calculateProgress()
      
      // 请求完成后取消加载状态
      chapterLoading.value = false
      loading.value = false // 取消整体加载状态
      
      // 记录学习开始时间
      studyStartTime.value = new Date()
      
      // 开始计时
      startTimeTracking()
    },
    (message) => {
      console.error("获取章节内容失败:", message)
      ElMessage.error(message || "获取章节内容失败")
      chapterLoading.value = false  // 失败时也要取消加载状态
      loading.value = false // 取消整体加载状态
    },
    (error) => {
      console.error("获取章节内容异常:", error)
      ElMessage.error("获取章节内容异常，请稍后重试")
      chapterLoading.value = false  // 异常时也要取消加载状态
      loading.value = false // 取消整体加载状态
    }
  )
}

// 切换到指定章节
const navigateToChapter = (id, title) => {
  // 记录当前学习时间
  recordStudyTime()
  
  // 使用replace而不是push，防止导航堆栈累积
  router.replace({
    name: 'LearningCenter',
    params: { courseId: courseId.value.toString(), chapterId: id.toString() },
    query: { title: title }
  })
}

// 切换学习内容
const switchContent = (index) => {
  contentLoading.value = true
  
  try {
  if (index >= 0 && index < chapterInfo.value.contents.length) {
    // 记录上一个内容的学习时间
    if (currentContent.value && currentContent.value.id !== chapterInfo.value.contents[index].id) {
      recordStudyTime()
    }
    
    currentContent.value = chapterInfo.value.contents[index]
    contentIndex.value = index
    lastPosition.value = `${contentIndex.value + 1}/${chapterInfo.value.contents.length}`
    
    // 重置学习开始时间
    studyStartTime.value = new Date()
    activeTime.value = 0
    
    // 更新学习进度
    updateProgress()
  }
  } catch (error) {
    console.error("切换内容异常:", error)
    ElMessage.error("切换内容异常，请稍后重试")
  } finally {
    // 无论成功还是失败，都确保取消加载状态
  contentLoading.value = false
  }
}

// 下一个内容
const nextContent = () => {
  if (contentIndex.value < chapterInfo.value.contents.length - 1) {
    switchContent(contentIndex.value + 1)
  } else {
    // 当前章节已完成，查找下一个章节
    const currentChapterIndex = allChapters.value.findIndex(chapter => chapter.id == chapterId.value)
    if (currentChapterIndex < allChapters.value.length - 1) {
      // 有下一章节，跳转到下一章节
      const nextChapter = allChapters.value[currentChapterIndex + 1]
      ElMessage.success('已完成当前章节，即将进入下一章节')
      navigateToChapter(nextChapter.id, nextChapter.title)
    } else {
      // 已是最后一章节
      ElMessage.success('恭喜您，已完成本课程所有章节学习！')
      progress.value = 100
      updateProgress()
    }
  }
}

// 上一个内容
const prevContent = () => {
  if (contentIndex.value > 0) {
    switchContent(contentIndex.value - 1)
  } else {
    // 当前是第一个内容，查找上一个章节的最后一个内容
    const currentChapterIndex = allChapters.value.findIndex(chapter => chapter.id == chapterId.value)
    if (currentChapterIndex > 0) {
      // 有上一章节，跳转到上一章节
      const prevChapter = allChapters.value[currentChapterIndex - 1]
      ElMessage.info('正在返回上一章节')
      navigateToChapter(prevChapter.id, prevChapter.title)
    }
  }
}

// 计算当前进度百分比
const calculateProgress = () => {
  if (!chapterInfo.value.contents || chapterInfo.value.contents.length === 0) {
    return 0
  }
  return Math.round(((contentIndex.value + 1) / chapterInfo.value.contents.length) * 100)
}

// 更新学习进度
const updateProgress = () => {
  // 计算进度百分比
  progress.value = calculateProgress()
  lastPosition.value = `${contentIndex.value + 1}/${chapterInfo.value.contents.length}`
  
  // 确保userId是数字类型
  const userId = parseInt(authStore.user?.userId)
  if (isNaN(userId)) {
    console.error('无效的用户ID')
    return
  }
  
  // 发送进度更新请求
  post(`/api/student/courses/${courseId.value}/progress`, 
    {
      studentId: userId,
    chapterId: chapterId.value,
    lastPosition: lastPosition.value,
    completionPercentage: progress.value
  }, 
  (message, data) => {
    console.log('进度已更新:', progress.value)
  },
  (message) => {
    console.error('更新进度失败:', message)
    },
    (error) => {
      console.error('更新进度异常:', error)
    }
  )
}

// 开始计时
const startTimeTracking = () => {
  // 清除之前的计时器
  if (timeTracker.value) {
    clearInterval(timeTracker.value)
  }
  
  // 每10秒更新一次活跃时间
  timeTracker.value = setInterval(() => {
    activeTime.value += 10
    
    // 每分钟记录一次学习时间
    if (activeTime.value % 60 === 0) {
      recordStudyTime(false) // 不重置计时器
    }
  }, 10000)
}

// 记录学习时间
const recordStudyTime = (resetTimer = true) => {
  if (!studyStartTime.value || !courseId.value || !currentContent.value) return
  
  const endTime = new Date()
  const durationSeconds = Math.round((endTime - studyStartTime.value) / 1000)
  
  if (durationSeconds < 5) return // 忽略过短的学习时间
  
  // 确保userId是字符串类型
  const userId = authStore.user?.userId.toString()
  if (!userId) {
    console.error('无效的用户ID')
    return
  }
  
  post('/api/student/learning-records', 
    {
      userId: userId,
      courseId: courseId.value.toString(),
    contentType: currentContent.value?.contentType || 'CHAPTER',
      contentId: (currentContent.value?.id || chapterId.value).toString(),
    startTime: studyStartTime.value.toISOString(),
    endTime: endTime.toISOString(),
    duration: durationSeconds,
    progress: progress.value
    },
    (message, data) => {
      console.log('学习记录已保存, 时长:', durationSeconds, '秒')
      
      if (resetTimer) {
        studyStartTime.value = new Date()
        activeTime.value = 0
      }
    },
    (message) => {
      console.error('保存学习记录失败:', message)
      
      if (resetTimer) {
        studyStartTime.value = new Date()
        activeTime.value = 0
      }
    },
    (error) => {
      console.error('保存学习记录异常:', error)
  
  if (resetTimer) {
    studyStartTime.value = new Date()
    activeTime.value = 0
  }
    }
  )
}

// 切换章节展开状态
const toggleChapter = (chapterId) => {
  expandedChapters.value[chapterId] = !expandedChapters.value[chapterId]
}

// 返回课程详情
const backToCourse = () => {
  // 记录学习时间
  recordStudyTime()
  
  // 清除计时器
  if (timeTracker.value) {
    clearInterval(timeTracker.value)
  }
  
  // 尝试使用原生历史记录返回
  if (window.history.length > 1) {
    router.go(-1) // 返回上一页
  } else {
    // 如果没有历史记录，则直接导航到课程详情页
    router.replace({
    name: 'student-course-detail',
      params: { courseId: courseId.value.toString() }
  })
  }
}

// 格式化内容类型
const formatContentType = (type) => {
  switch (type) {
    case 'VIDEO': return '视频'
    case 'DOCUMENT': return '文档'
    case 'QUIZ': return '测验'
    default: return '内容'
  }
}

// 获取内容图标
const getContentIcon = (type) => {
  switch (type) {
    case 'VIDEO': return 'fas fa-video'
    case 'DOCUMENT': return 'fas fa-file-alt'
    case 'QUIZ': return 'fas fa-tasks'
    default: return 'fas fa-file'
  }
}

// 格式化学习时间
const formatLearningTime = (seconds) => {
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  
  if (hours > 0) {
    return `${hours}小时${minutes % 60}分钟`
  } else if (minutes > 0) {
    return `${minutes}分钟`
  } else {
    return `${seconds}秒`
  }
}

// 监听路由变化
watch(() => route.params, (newParams) => {
  if (newParams.courseId !== courseId.value || newParams.chapterId !== chapterId.value) {
    // 记录当前学习时间
    recordStudyTime()
    
    // 确保将路由参数转换为字符串类型
    courseId.value = newParams.courseId ? newParams.courseId.toString() : null
    chapterId.value = newParams.chapterId ? newParams.chapterId.toString() : null
    chapterTitle.value = route.query.title || '章节学习'
    
    if (courseId.value) {
      fetchCourseInfo()
    }
  }
}, { immediate: true })

// 组件卸载前记录学习时间
onBeforeUnmount(() => {
  recordStudyTime()
  
  // 清除计时器
  if (timeTracker.value) {
    clearInterval(timeTracker.value)
  }
})

onMounted(() => {
  // 添加页面可见性变化监听
  document.addEventListener('visibilitychange', handleVisibilityChange)
  
  if (courseId.value) {
    fetchCourseInfo()
  } else {
    loading.value = false
  }
})

onBeforeUnmount(() => {
  // 移除页面可见性变化监听
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})

// 处理页面可见性变化
const handleVisibilityChange = () => {
  if (document.hidden) {
    // 页面不可见时记录学习时间
    recordStudyTime()
    
    // 清除计时器
    if (timeTracker.value) {
      clearInterval(timeTracker.value)
    }
  } else {
    // 页面可见时重新开始计时
    studyStartTime.value = new Date()
    startTimeTracking()
  }
}

// 计算章节完成状态
const getChapterStatus = (chapter) => {
  // 这里应该根据后端返回的数据判断章节完成状态
  // 暂时简单返回，实际应用中可能需要更复杂的逻辑
  return {
    isCompleted: chapter.progress === 100,
    progress: chapter.progress || 0
  }
}

// 计算是否为当前章节
const isCurrentChapter = (id) => {
  return id == chapterId.value
}

// 计算是否为当前内容
const isCurrentContent = (id) => {
  return currentContent.value && currentContent.value.id == id
}
</script>

<template>
  <section class="learning-center-wrapper p-4 md:p-6" v-loading="loading">
    <!-- 顶部导航栏 -->
    <div class="bg-white rounded-xl shadow-md p-4 mb-6 flex items-center">
      <button @click="backToCourse" class="flex items-center text-gray-600 hover:text-blue-600 transition-colors">
        <i class="fas fa-arrow-left mr-2"></i> 返回课程
      </button>
      
      <div class="mx-4 text-gray-300">|</div>
      
      <div class="text-gray-700 font-medium truncate">
        {{ courseInfo.title }} <span class="text-gray-400 mx-1">></span> {{ chapterInfo.title || chapterTitle }}
      </div>
      
      <div class="ml-auto flex items-center gap-4">
        <div class="text-sm text-gray-500 hidden md:block">
          学习时间: {{ formatLearningTime(activeTime.value) }}
        </div>
        
        <div class="flex items-center">
          <span class="text-sm text-gray-500 mr-2">进度: {{ progress }}%</span>
          <el-progress :percentage="progress" :show-text="false" :stroke-width="8" class="w-24"></el-progress>
        </div>
      </div>
    </div>
    
    <!-- 主要内容区 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
      <!-- 左侧章节导航 -->
      <div class="md:col-span-1">
        <div class="bg-white rounded-xl shadow-md p-4 sticky top-6">
          <h3 class="text-lg font-medium mb-4">课程章节</h3>
          
          <div class="chapters-list space-y-1">
            <div v-for="chapter in allChapters" :key="chapter.id" class="chapter-item">
              <!-- 章节标题 -->
              <div 
                :class="['chapter-header p-2 rounded-lg cursor-pointer transition-colors flex items-center justify-between',
                  isCurrentChapter(chapter.id) ? 'bg-blue-50 text-blue-600' : 'hover:bg-gray-50']"
                @click="toggleChapter(chapter.id)"
              >
                <div class="flex items-center">
                  <div :class="['chapter-icon w-6 h-6 rounded-full flex items-center justify-center mr-2',
                    isCurrentChapter(chapter.id) ? 'bg-blue-100' : 'bg-gray-100']">
                    <i class="fas fa-book"></i>
                  </div>
                  <div class="text-sm font-medium truncate">{{ chapter.title }}</div>
                </div>
                
                <div class="flex items-center">
                  <!-- 章节进度 -->
                  <div v-if="getChapterStatus(chapter).progress > 0" class="mr-2 text-xs">
                    {{ getChapterStatus(chapter).progress }}%
                  </div>
                  
                  <!-- 展开/折叠图标 -->
                  <i :class="['fas', expandedChapters[chapter.id] ? 'fa-chevron-down' : 'fa-chevron-right']"></i>
                </div>
              </div>
              
              <!-- 章节内容列表 -->
              <div v-if="expandedChapters[chapter.id]" class="chapter-contents pl-8 mt-1 space-y-1">
                <div v-if="isCurrentChapter(chapter.id) && chapterLoading" class="py-2 text-center text-gray-400">
                  <i class="fas fa-spinner fa-spin mr-1"></i> 加载中...
                </div>
                
                <template v-else-if="isCurrentChapter(chapter.id) && chapterInfo.contents && chapterInfo.contents.length > 0">
                  <div 
                    v-for="(content, index) in chapterInfo.contents" 
                    :key="content.id"
                    :class="['content-item py-1 px-2 rounded-lg cursor-pointer transition-colors flex items-center',
                      index === contentIndex ? 'bg-blue-50 text-blue-600' : 'hover:bg-gray-50']"
                    @click="switchContent(index)"
                  >
                    <div :class="['content-icon w-5 h-5 rounded-full flex items-center justify-center mr-2',
                      index === contentIndex ? 'bg-blue-100' : 'bg-gray-100']">
                      <i :class="[getContentIcon(content.contentType), 
                        index === contentIndex ? 'text-blue-600' : 'text-gray-600']"></i>
                    </div>
                    
                    <div class="flex-1 overflow-hidden">
                      <div class="text-xs truncate">{{ content.title }}</div>
      </div>
      
                    <div v-if="index < contentIndex" class="text-green-500 ml-1">
                      <i class="fas fa-check-circle text-xs"></i>
                    </div>
                  </div>
                </template>
                
                <div v-else-if="isCurrentChapter(chapter.id)" class="py-2 text-center text-gray-400 text-xs">
                  暂无内容
          </div>
                
                <div v-else class="py-2 text-center text-gray-400 text-xs">
                  <button @click="navigateToChapter(chapter.id, chapter.title)" class="text-blue-500 hover:underline">
                    点击加载章节内容
                  </button>
        </div>
      </div>
    </div>
    
            <div v-if="allChapters.length === 0" class="text-center py-4 text-gray-400">
              暂无章节
            </div>
        </div>
        </div>
      </div>
      
      <!-- 右侧内容显示 -->
      <div class="md:col-span-3">
        <div class="bg-white rounded-xl shadow-md p-4 min-h-[500px]">
          <!-- 标签页切换 -->
          <el-tabs v-model="activeTab" class="mb-4">
            <el-tab-pane label="学习内容" name="content"></el-tab-pane>
            <el-tab-pane label="学习笔记" name="notes"></el-tab-pane>
            <el-tab-pane label="章节评价" name="rating"></el-tab-pane>
            <el-tab-pane label="学习统计" name="statistics"></el-tab-pane>
          </el-tabs>
          
          <!-- 内容标签页 -->
          <div v-if="activeTab === 'content'" v-loading="contentLoading">
            <div v-if="currentContent" class="content-display">
              <h2 class="text-xl font-bold mb-4">{{ currentContent.title }}</h2>
              
              <!-- 视频内容 -->
              <div v-if="currentContent.contentType === 'VIDEO'" class="video-container mb-4">
                <video 
                  v-if="currentContent.contentUrl" 
                  :src="currentContent.contentUrl" 
                  controls 
                  class="w-full rounded-lg"
                  style="max-height: 400px;"
                ></video>
                <div v-else class="bg-gray-100 rounded-lg p-8 text-center text-gray-500">
                  <i class="fas fa-video text-3xl mb-2"></i>
                  <p>视频资源暂不可用</p>
                </div>
              </div>
              
              <!-- 文档内容 -->
              <div v-else-if="currentContent.contentType === 'DOCUMENT'" class="document-container mb-4">
                <div v-if="currentContent.contentText" 
                     class="content-text p-4 border rounded-lg" 
                     v-html="currentContent.contentText"></div>
                <div v-else-if="currentContent.contentUrl" class="mb-4">
                  <a :href="currentContent.contentUrl" target="_blank" 
                     class="text-blue-600 hover:underline flex items-center">
                    <i class="fas fa-external-link-alt mr-1"></i>
                    在新窗口中查看文档
                  </a>
                </div>
                <div v-else class="bg-gray-100 rounded-lg p-8 text-center text-gray-500">
                  <i class="fas fa-file-alt text-3xl mb-2"></i>
                  <p>文档内容暂不可用</p>
                </div>
              </div>
              
              <!-- 测验内容 -->
              <div v-else-if="currentContent.contentType === 'QUIZ'" class="quiz-container mb-4">
                <div class="bg-blue-50 border border-blue-200 rounded-lg p-4 text-center">
                  <i class="fas fa-tasks text-blue-600 text-3xl mb-2"></i>
                  <p class="text-blue-800">此处为测验内容，请点击下方按钮开始测验</p>
                  <button class="mt-4 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors">
                    开始测验
                  </button>
      </div>
    </div>

              <!-- 其他类型内容 -->
              <div v-else class="generic-container mb-4">
                <div v-if="currentContent.contentText" 
                     class="content-text p-4 border rounded-lg" 
                     v-html="currentContent.contentText"></div>
                <div v-else class="bg-gray-100 rounded-lg p-8 text-center text-gray-500">
                  <i class="fas fa-file text-3xl mb-2"></i>
                  <p>内容暂不可用</p>
                </div>
    </div>
    
              <!-- 内容导航 -->
              <div class="flex justify-between mt-8 border-t pt-6">
                <button 
                  @click="prevContent" 
                  class="px-4 py-2 border rounded-lg hover:bg-gray-50 transition-colors flex items-center"
                  :disabled="contentIndex === 0 && allChapters.findIndex(c => c.id == chapterId.value) === 0"
                  :class="{'opacity-50 cursor-not-allowed': contentIndex === 0 && allChapters.findIndex(c => c.id == chapterId.value) === 0}"
                >
                  <i class="fas fa-arrow-left mr-2"></i> 上一节
                </button>
                
                <button 
                  @click="nextContent" 
                  class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center"
                >
                  <span v-if="contentIndex < chapterInfo.contents.length - 1">下一节</span>
                  <span v-else-if="allChapters.findIndex(c => c.id == chapterId.value) < allChapters.length - 1">下一章节</span>
                  <span v-else>完成学习</span>
                  <i class="fas fa-arrow-right ml-2"></i>
                </button>
              </div>
              
              <!-- 学习进度指示器 -->
              <div class="mt-6 pt-4 border-t">
                <div class="flex items-center justify-between text-sm text-gray-500 mb-2">
                  <div>当前进度</div>
                  <div>{{ contentIndex + 1 }} / {{ chapterInfo.contents.length }}</div>
                </div>
                <el-progress :percentage="progress" :stroke-width="10" :format="() => `${progress}%`"></el-progress>
              </div>
            </div>
            
            <div v-else class="flex items-center justify-center h-64 text-gray-400">
              <div class="text-center">
                <i class="fas fa-book-open text-4xl mb-2"></i>
                <p>暂无学习内容</p>
                <p class="text-sm mt-2">请从左侧选择要学习的章节</p>
              </div>
            </div>
          </div>
          
          <!-- 笔记标签页 -->
          <div v-else-if="activeTab === 'notes'">
            <LearningNotes 
              :userId="authStore.user?.userId" 
              :courseId="Number(courseId)" 
              :chapterId="chapterId ? Number(chapterId) : null"
              :contentId="currentContent?.id"
              :contentType="currentContent?.contentType"
            />
          </div>
          
          <!-- 评价标签页 -->
          <div v-else-if="activeTab === 'rating'">
            <ChapterRating 
              v-if="chapterId"
              :userId="authStore.user?.userId" 
              :courseId="Number(courseId)" 
              :chapterId="Number(chapterId)"
            />
            <div v-else class="flex items-center justify-center h-64 text-gray-400">
              <div class="text-center">
                <i class="fas fa-star text-4xl mb-2"></i>
                <p>请先选择章节</p>
                <p class="text-sm mt-2">从左侧选择要评价的章节</p>
              </div>
            </div>
          </div>
          
          <!-- 统计标签页 -->
          <div v-else-if="activeTab === 'statistics'">
            <LearningStatistics 
              :userId="authStore.user?.userId" 
              :courseId="Number(courseId)"
            />
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.learning-center-wrapper {
  min-height: calc(100vh - 64px);
  background-color: #f8fafc;
}

.content-text :deep(h1),
.content-text :deep(h2),
.content-text :deep(h3),
.content-text :deep(h4),
.content-text :deep(h5),
.content-text :deep(h6) {
  margin-top: 1rem;
  margin-bottom: 0.5rem;
  font-weight: bold;
}

.content-text :deep(h1) {
  font-size: 1.5rem;
}

.content-text :deep(h2) {
  font-size: 1.25rem;
}

.content-text :deep(p) {
  margin-bottom: 1rem;
}

.content-text :deep(ul),
.content-text :deep(ol) {
  margin-bottom: 1rem;
  padding-left: 1.5rem;
}

.content-text :deep(li) {
  margin-bottom: 0.25rem;
}

.content-text :deep(pre) {
  background-color: #f8f9fa;
  padding: 1rem;
  border-radius: 0.375rem;
  overflow-x: auto;
  margin-bottom: 1rem;
}

.content-text :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 0.375rem;
  margin: 1rem 0;
}

/* 添加平滑过渡效果 */
.chapter-contents {
  overflow: hidden;
  transition: max-height 0.3s ease-in-out;
}

/* 添加内容切换动画 */
.content-display {
  animation: fade-in 0.3s ease-out;
}

@keyframes fade-in {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style> 