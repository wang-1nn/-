<script setup>
import { ref, onMounted, reactive } from 'vue';
import { get } from '@/net';
import LucideIcon from '@/components/icons/LucideIcon.vue';

const loading = ref(true);
const teacherInfo = reactive({
  name: '',
  department: '',
  courseCount: 0,
  studentCount: 0,
  latestLoginTime: ''
});

// 课程数据
const courses = ref([]);

// 近期任务
const tasks = ref([]);

// 学生活跃度数据
const studentActivity = reactive({
  labels: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
  data: [0, 0, 0, 0, 0, 0, 0]
});

// 获取仪表盘数据
const fetchDashboardData = () => {
  loading.value = true;
  
  get('/api/teacher/dashboard', null, 
    (message, data) => {
      // 教师基本信息
      if (data.teacherInfo) {
        teacherInfo.name = data.teacherInfo.name;
        teacherInfo.department = data.teacherInfo.department;
        teacherInfo.courseCount = data.teacherInfo.courseCount;
        teacherInfo.studentCount = data.teacherInfo.studentCount;
        teacherInfo.latestLoginTime = data.teacherInfo.latestLoginTime;
      }
      
      // 课程信息
      courses.value = data.courses || [];
      
      // 近期任务
      tasks.value = data.tasks || [];
      
      // 学生活跃度
      if (data.studentActivity) {
        studentActivity.data = data.studentActivity;
      }
      
      loading.value = false;
    },
    () => {
      // 加载失败时显示模拟数据
      mockDashboardData();
      loading.value = false;
    }
  );
};

// 生成模拟数据
const mockDashboardData = () => {
  teacherInfo.name = '张三';
  teacherInfo.department = '计算机科学与技术';
  teacherInfo.courseCount = 4;
  teacherInfo.studentCount = 120;
  teacherInfo.latestLoginTime = '2023-07-15 08:30';
  
  courses.value = [
    { id: 1, name: '数据结构', code: 'CS-201', students: 35, progress: 68, status: 'active' },
    { id: 2, name: '算法分析', code: 'CS-301', students: 28, progress: 42, status: 'active' },
    { id: 3, name: '操作系统', code: 'CS-302', students: 32, progress: 15, status: 'active' },
    { id: 4, name: '计算机网络', code: 'CS-401', students: 25, progress: 90, status: 'active' }
  ];
  
  tasks.value = [
    { id: 1, title: '审阅数据结构作业', course: '数据结构', deadline: '今天 23:59', priority: 'high' },
    { id: 2, title: '准备操作系统课件', course: '操作系统', deadline: '明天 10:00', priority: 'medium' },
    { id: 3, title: '发布算法分析测验', course: '算法分析', deadline: '后天 08:00', priority: 'medium' },
    { id: 4, title: '批改计算机网络实验报告', course: '计算机网络', deadline: '周五 18:00', priority: 'low' }
  ];
  
  studentActivity.data = [65, 59, 80, 81, 56, 40, 70];
};

// 获取任务优先级样式
const getTaskPriorityStyle = (priority) => {
  switch (priority) {
    case 'high':
      return { icon: 'alert-circle', color: 'text-red-500', bg: 'bg-red-50', border: 'border-red-200' };
    case 'medium':
      return { icon: 'alert-triangle', color: 'text-amber-500', bg: 'bg-amber-50', border: 'border-amber-200' };
    case 'low':
      return { icon: 'info', color: 'text-blue-500', bg: 'bg-blue-50', border: 'border-blue-200' };
    default:
      return { icon: 'help-circle', color: 'text-gray-500', bg: 'bg-gray-50', border: 'border-gray-200' };
  }
};

// 计算进度条颜色
const getProgressColor = (progress) => {
  if (progress < 30) return 'bg-red-500';
  if (progress < 70) return 'bg-amber-500';
  return 'bg-green-500';
};

onMounted(() => {
  fetchDashboardData();
});
</script>

<template>
  <div class="teacher-dashboard">
    <!-- 页面头部 -->
    <div class="dashboard-header">
      <div>
        <h1 class="dashboard-title">教师仪表盘</h1>
        <p class="dashboard-subtitle">欢迎回来，{{ teacherInfo.name }}</p>
      </div>
      <div class="header-actions">
        <button @click="fetchDashboardData" class="refresh-button">
          <LucideIcon name="refresh-cw" size="16" :class="{ 'animate-spin': loading }" />
          <span class="ml-2">刷新</span>
        </button>
      </div>
    </div>
    
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon-container bg-blue-100">
          <LucideIcon name="book-open" class="text-blue-600" size="20" />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ teacherInfo.courseCount }}</div>
          <div class="stat-label">我的课程</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon-container bg-purple-100">
          <LucideIcon name="users" class="text-purple-600" size="20" />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ teacherInfo.studentCount }}</div>
          <div class="stat-label">学生总数</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon-container bg-green-100">
          <LucideIcon name="check-circle" class="text-green-600" size="20" />
        </div>
        <div class="stat-content">
          <div class="stat-value">42</div>
          <div class="stat-label">已完成任务</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon-container bg-amber-100">
          <LucideIcon name="clock" class="text-amber-600" size="20" />
        </div>
        <div class="stat-content">
          <div class="stat-value">8</div>
          <div class="stat-label">待办任务</div>
        </div>
      </div>
    </div>
    
    <!-- 主要内容区域 -->
    <div class="dashboard-content">
      <!-- 课程列表 -->
      <div class="course-list-container">
        <div class="panel-header">
          <h2 class="panel-title">我的课程</h2>
          <router-link to="/teacher/course-management" class="view-all-link">
            查看全部
            <LucideIcon name="chevron-right" size="16" />
          </router-link>
        </div>
        
        <div v-if="loading" class="panel-loading">
          <LucideIcon name="loader-2" size="24" class="animate-spin" />
        </div>
        
        <div v-else-if="courses.length === 0" class="panel-empty">
          <LucideIcon name="book" size="24" class="text-gray-400 mb-2" />
          <p>您还没有创建任何课程</p>
          <router-link to="/teacher/course-management" class="empty-action-button">
            创建课程
          </router-link>
        </div>
        
        <div v-else class="courses-grid">
          <div v-for="course in courses" :key="course.id" class="course-card">
            <div class="course-card-header">
              <h3 class="course-name">{{ course.name }}</h3>
              <span class="course-code">{{ course.code }}</span>
            </div>
            
            <div class="course-students">
              <LucideIcon name="users" size="16" class="text-gray-500" />
              <span>{{ course.students }} 名学生</span>
            </div>
            
            <div class="course-progress-container">
              <div class="course-progress-info">
                <span>课程进度</span>
                <span>{{ course.progress }}%</span>
              </div>
              <div class="course-progress-bar">
                <div 
                  class="course-progress-fill" 
                  :class="getProgressColor(course.progress)"
                  :style="{ width: `${course.progress}%` }"
                ></div>
              </div>
            </div>
            
            <div class="course-actions">
              <router-link :to="`/teacher/course-management?id=${course.id}`" class="course-action-button">
                <LucideIcon name="external-link" size="16" />
                <span>管理</span>
              </router-link>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧边栏 -->
      <div class="dashboard-sidebar">
        <!-- 教师信息卡片 -->
        <div class="teacher-info-card">
          <div class="teacher-info-header">
            <div class="teacher-avatar">{{ teacherInfo.name.charAt(0) }}</div>
            <div class="teacher-details">
              <h3 class="teacher-name">{{ teacherInfo.name }}</h3>
              <p class="teacher-department">{{ teacherInfo.department }}</p>
            </div>
          </div>
          
          <div class="teacher-info-body">
            <div class="teacher-info-item">
              <LucideIcon name="clock" size="16" class="text-gray-500" />
              <span>上次登录: {{ teacherInfo.latestLoginTime }}</span>
            </div>
          </div>
        </div>
        
        <!-- 近期任务 -->
        <div class="tasks-panel">
          <div class="panel-header">
            <h2 class="panel-title">近期任务</h2>
          </div>
          
          <div v-if="loading" class="panel-loading">
            <LucideIcon name="loader-2" size="24" class="animate-spin" />
          </div>
          
          <div v-else-if="tasks.length === 0" class="panel-empty">
            <LucideIcon name="check-circle" size="24" class="text-gray-400 mb-2" />
            <p>没有待办任务</p>
          </div>
          
          <div v-else class="tasks-list">
            <div 
              v-for="task in tasks" 
              :key="task.id" 
              class="task-item"
              :class="getTaskPriorityStyle(task.priority).border"
            >
              <div class="task-priority-indicator" :class="getTaskPriorityStyle(task.priority).bg">
                <LucideIcon 
                  :name="getTaskPriorityStyle(task.priority).icon" 
                  size="16" 
                  :class="getTaskPriorityStyle(task.priority).color" 
                />
              </div>
              
              <div class="task-content">
                <h4 class="task-title">{{ task.title }}</h4>
                <div class="task-meta">
                  <span class="task-course">{{ task.course }}</span>
                  <span class="task-deadline">
                    <LucideIcon name="clock" size="14" />
                    {{ task.deadline }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 学生活跃度图表 -->
        <div class="activity-chart-panel">
          <div class="panel-header">
            <h2 class="panel-title">学生活跃度</h2>
          </div>
          
          <div class="activity-chart">
            <div class="chart-container">
              <div class="chart-bars">
                <div 
                  v-for="(value, index) in studentActivity.data" 
                  :key="index"
                  class="chart-bar-container"
                >
                  <div 
                    class="chart-bar" 
                    :style="{ height: `${(value / Math.max(...studentActivity.data)) * 100}%` }"
                  ></div>
                  <div class="chart-label">{{ studentActivity.labels[index] }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.teacher-dashboard {
  padding: 1.5rem;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.dashboard-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 0.25rem;
}

.dashboard-subtitle {
  font-size: 0.875rem;
  color: #64748b;
}

.refresh-button {
  display: flex;
  align-items: center;
  padding: 0.5rem 0.75rem;
  font-size: 0.875rem;
  color: #475569;
  background-color: white;
  border: 1px solid #e2e8f0;
  border-radius: 0.375rem;
  transition: all 0.2s;
}

.refresh-button:hover {
  background-color: #f8fafc;
  border-color: #cbd5e1;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.stat-card {
  background-color: white;
  border-radius: 0.5rem;
  padding: 1.25rem;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
}

.stat-icon-container {
  width: 40px;
  height: 40px;
  border-radius: 0.375rem;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 1rem;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1e293b;
  line-height: 1.2;
}

.stat-label {
  font-size: 0.875rem;
  color: #64748b;
}

.dashboard-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 1.5rem;
}

.course-list-container,
.teacher-info-card,
.tasks-panel,
.activity-chart-panel {
  background-color: white;
  border-radius: 0.5rem;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid #e5e7eb;
}

.panel-title {
  font-size: 1rem;
  font-weight: 600;
  color: #1e293b;
}

.view-all-link {
  display: flex;
  align-items: center;
  font-size: 0.875rem;
  color: #3b82f6;
  transition: color 0.2s;
}

.view-all-link:hover {
  color: #2563eb;
}

.panel-loading,
.panel-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 0;
  color: #64748b;
}

.empty-action-button {
  margin-top: 1rem;
  padding: 0.5rem 1rem;
  font-size: 0.875rem;
  color: white;
  background-color: #3b82f6;
  border-radius: 0.375rem;
  transition: background-color 0.2s;
}

.empty-action-button:hover {
  background-color: #2563eb;
}

.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1rem;
  padding: 1.25rem;
}

.course-card {
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
  padding: 1.25rem;
  transition: all 0.2s;
}

.course-card:hover {
  border-color: #cbd5e1;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

.course-card-header {
  margin-bottom: 0.75rem;
}

.course-name {
  font-size: 1rem;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 0.25rem;
}

.course-code {
  font-size: 0.75rem;
  color: #64748b;
  background-color: #f1f5f9;
  padding: 0.125rem 0.375rem;
  border-radius: 0.25rem;
}

.course-students {
  display: flex;
  align-items: center;
  font-size: 0.875rem;
  color: #64748b;
  margin-bottom: 0.75rem;
}

.course-students svg {
  margin-right: 0.375rem;
}

.course-progress-container {
  margin-bottom: 1rem;
}

.course-progress-info {
  display: flex;
  justify-content: space-between;
  font-size: 0.75rem;
  color: #64748b;
  margin-bottom: 0.375rem;
}

.course-progress-bar {
  height: 0.5rem;
  background-color: #e5e7eb;
  border-radius: 9999px;
  overflow: hidden;
}

.course-progress-fill {
  height: 100%;
  border-radius: 9999px;
}

.course-actions {
  display: flex;
  justify-content: flex-end;
}

.course-action-button {
  display: flex;
  align-items: center;
  font-size: 0.875rem;
  color: #3b82f6;
  padding: 0.375rem 0.75rem;
  border: 1px solid #e5e7eb;
  border-radius: 0.375rem;
  transition: all 0.2s;
}

.course-action-button:hover {
  background-color: #f0f9ff;
  border-color: #93c5fd;
}

.course-action-button svg {
  margin-right: 0.375rem;
}

.dashboard-sidebar {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.teacher-info-card {
  margin-bottom: 0;
}

.teacher-info-header {
  display: flex;
  align-items: center;
  padding: 1.25rem;
  border-bottom: 1px solid #e5e7eb;
}

.teacher-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: #3b82f6;
  color: white;
  font-size: 1.25rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 1rem;
}

.teacher-name {
  font-size: 1.125rem;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 0.125rem;
}

.teacher-department {
  font-size: 0.875rem;
  color: #64748b;
}

.teacher-info-body {
  padding: 1.25rem;
}

.teacher-info-item {
  display: flex;
  align-items: center;
  font-size: 0.875rem;
  color: #64748b;
}

.teacher-info-item svg {
  margin-right: 0.5rem;
}

.tasks-list {
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.task-item {
  display: flex;
  border: 1px solid;
  border-radius: 0.375rem;
  overflow: hidden;
}

.task-priority-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2.5rem;
  padding: 0.75rem 0;
}

.task-content {
  flex: 1;
  padding: 0.75rem 1rem;
}

.task-title {
  font-size: 0.875rem;
  font-weight: 500;
  color: #1e293b;
  margin-bottom: 0.375rem;
}

.task-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.75rem;
  color: #64748b;
}

.task-course {
  background-color: #f1f5f9;
  padding: 0.125rem 0.375rem;
  border-radius: 0.25rem;
}

.task-deadline {
  display: flex;
  align-items: center;
}

.task-deadline svg {
  margin-right: 0.25rem;
}

.activity-chart {
  padding: 1.25rem;
}

.chart-container {
  height: 150px;
}

.chart-bars {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  height: 100%;
}

.chart-bar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.chart-bar {
  width: 24px;
  background-color: #3b82f6;
  border-radius: 3px 3px 0 0;
  transition: height 0.3s ease;
}

.chart-label {
  margin-top: 0.5rem;
  font-size: 0.75rem;
  color: #64748b;
}

@media (max-width: 1024px) {
  .dashboard-content {
    grid-template-columns: 1fr;
  }
  
  .dashboard-sidebar {
    order: -1;
  }
}

@media (max-width: 640px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .courses-grid {
    grid-template-columns: 1fr;
  }
}
</style> 