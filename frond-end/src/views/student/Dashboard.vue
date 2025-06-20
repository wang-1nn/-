<script setup>
import { ref, reactive, onMounted } from 'vue'
import { get } from '@/net'
import LucideIcon from '@/components/icons/LucideIcon.vue'

// 页面加载状态
const loading = ref(true)

// 学生数据
const studentInfo = reactive({
  name: '',
  courseCount: 0,
  completedTasks: 0,
  upcomingTasks: 0,
  averageScore: 0,
  streakDays: 0
})

// 我的课程
const courses = ref([])

// 待办任务
const tasks = ref([])

// 获取仪表盘数据
const fetchDashboardData = () => {
  loading.value = true
  get('/api/student/dashboard', null,
    (msg, data) => {
      if (data.studentInfo) {
        Object.assign(studentInfo, data.studentInfo)
      }
      courses.value = data.courses || []
      tasks.value = data.tasks || []
      loading.value = false
    },
    () => {
      // mock 数据
      mockData()
      loading.value = false
    }
  )
}

const mockData = () => {
  studentInfo.name = '李四'
  studentInfo.courseCount = 6
  studentInfo.completedTasks = 42
  studentInfo.upcomingTasks = 3
  studentInfo.averageScore = 88
  studentInfo.streakDays = 7

  courses.value = [
    { id: 1, name: '高等数学', progress: 80, teacher: '王老师' },
    { id: 2, name: '大学英语', progress: 60, teacher: '刘老师' },
    { id: 3, name: 'C++ 程序设计', progress: 45, teacher: '张老师' },
    { id: 4, name: '线性代数', progress: 70, teacher: '李老师' }
  ]

  tasks.value = [
    { id: 1, title: '完成高数作业 5', due: '今天 23:59', course: '高等数学' },
    { id: 2, title: '预习线代章节 3', due: '明天 08:00', course: '线性代数' },
    { id: 3, title: 'C++ 上机实验报告', due: '周五 18:00', course: 'C++ 程序设计' }
  ]
}

// 进度条颜色
const progressColor = (p) => {
  if (p < 40) return 'bg-red-500'
  if (p < 70) return 'bg-amber-500'
  return 'bg-emerald-500'
}

onMounted(() => {
  fetchDashboardData()
})
</script>

<template>
  <div class="student-dashboard page-container">
    <div class="flex justify-between items-center mb-6">
      <div>
        <h1 class="text-2xl font-semibold text-gray-800 mb-1">学习主页</h1>
        <p class="text-sm text-gray-500">欢迎回来，{{ studentInfo.name }}！</p>
      </div>
      <button @click="fetchDashboardData" class="btn-outline flex items-center">
        <LucideIcon name="refresh-cw" size="16" :class="{ 'animate-spin': loading }" />
        <span class="ml-2">刷新</span>
      </button>
    </div>

    <!-- 统计卡片 -->
    <div class="grid gap-4 grid-cols-1 md:grid-cols-2 xl:grid-cols-4 mb-8">
      <div class="card-hover flex items-center">
        <div class="p-3 rounded-md bg-indigo-100 text-indigo-600 mr-3">
          <LucideIcon name="book-open" size="20" />
        </div>
        <div>
          <div class="text-xl font-semibold text-gray-800">{{ studentInfo.courseCount }}</div>
          <div class="text-sm text-gray-500">我的课程</div>
        </div>
      </div>

      <div class="card-hover flex items-center">
        <div class="p-3 rounded-md bg-green-100 text-green-600 mr-3">
          <LucideIcon name="check-circle" size="20" />
        </div>
        <div>
          <div class="text-xl font-semibold text-gray-800">{{ studentInfo.completedTasks }}</div>
          <div class="text-sm text-gray-500">已完成任务</div>
        </div>
      </div>

      <div class="card-hover flex items-center">
        <div class="p-3 rounded-md bg-amber-100 text-amber-600 mr-3">
          <LucideIcon name="clock" size="20" />
        </div>
        <div>
          <div class="text-xl font-semibold text-gray-800">{{ studentInfo.upcomingTasks }}</div>
          <div class="text-sm text-gray-500">待办任务</div>
        </div>
      </div>

      <div class="card-hover flex items-center">
        <div class="p-3 rounded-md bg-purple-100 text-purple-600 mr-3">
          <LucideIcon name="award" size="20" />
        </div>
        <div>
          <div class="text-xl font-semibold text-gray-800">{{ studentInfo.averageScore }} 分</div>
          <div class="text-sm text-gray-500">平均成绩</div>
        </div>
      </div>
    </div>

    <div class="grid gap-6 lg:grid-cols-3">
      <!-- 我的课程 -->
      <div class="lg:col-span-2 card">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-lg font-medium text-gray-800">我的课程</h2>
          <router-link to="/student/courses" class="text-sm text-indigo-600 hover:text-indigo-800 flex items-center">
            查看全部 <LucideIcon name="chevron-right" size="16" class="ml-1" />
          </router-link>
        </div>

        <transition-group name="list" tag="div" class="space-y-4">
          <div v-for="course in courses" :key="course.id" class="p-4 rounded-lg border border-gray-200 hover:shadow-sm transition-shadow">
            <div class="flex justify-between items-center mb-2">
              <div class="font-semibold text-gray-800">{{ course.name }}</div>
              <span class="text-xs text-gray-500">{{ course.teacher }}</span>
            </div>
            <div class="h-2 bg-gray-100 rounded-full overflow-hidden mb-2">
              <div :class="['h-full', progressColor(course.progress)]" :style="{ width: `${course.progress}%` }"></div>
            </div>
            <div class="text-xs text-gray-500">进度 {{ course.progress }}%</div>
          </div>
        </transition-group>
      </div>

      <!-- 待办任务 -->
      <div class="card h-fit">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-lg font-medium text-gray-800">待办任务</h2>
        </div>
        <div v-if="tasks.length === 0" class="text-center text-sm text-gray-500 py-6">
          <LucideIcon name="inbox" size="20" class="mb-2 mx-auto text-gray-400" />
          暂无任务
        </div>
        <transition-group name="fade" tag="div" class="space-y-3">
          <div v-for="task in tasks" :key="task.id" class="flex items-start p-3 rounded-md hover:bg-gray-50 cursor-pointer transition-colors">
            <LucideIcon name="circle" size="14" class="mt-1 text-amber-500 mr-2 flex-shrink-0" />
            <div class="flex-1">
              <div class="text-sm font-medium text-gray-800">{{ task.title }}</div>
              <div class="text-xs text-gray-500 flex justify-between mt-1">
                <span>{{ task.course }}</span>
                <span class="flex items-center"><LucideIcon name="clock" size="12" class="mr-0.5" /> {{ task.due }}</span>
              </div>
            </div>
          </div>
        </transition-group>
      </div>
    </div>
  </div>
</template>

<style scoped>
/***** 过渡动画 *****/
.fade-enter-from, .fade-leave-to { opacity: 0; transform: translateY(6px); }
.fade-enter-active, .fade-leave-active { transition: all 0.3s ease; }

.list-enter-from { opacity: 0; transform: translateY(8px); }
.list-enter-active { transition: all 0.3s ease; }
.list-leave-to { opacity: 0; transform: translateY(-8px); }
.list-leave-active { transition: all 0.2s ease; position: absolute; }
</style> 