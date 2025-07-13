<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { get } from '@/net';
import ScoreOverview from '@/components/student/widgets/ScoreOverview.vue';
import StudyTimeWidget from '@/components/student/widgets/StudyTimeWidget.vue';
import ReminderWidget from '@/components/student/widgets/ReminderWidget.vue';
import { 
  ArrowTopRightOnSquareIcon, 
  AcademicCapIcon, 
  ClockIcon, 
  FireIcon,
  BookOpenIcon,
  ChartBarIcon
} from '@heroicons/vue/24/outline';

import {useAuthStore} from "@/stores/counter.js";
const authStore = useAuthStore()
const userId = authStore.user.userId
const router = useRouter();
const username = ref('同学');
const isLoading = ref(true);
const showWelcome = ref(true);

// 学习统计数据
const stats = ref([]);

// 学习建议
const recommendations = ref([]);

// 课程学习进度
const courses = ref([]);

// 根据统计项ID获取对应的图标组件
const getStatIcon = (id) => {
  switch (id) {
    case 1: return BookOpenIcon; // 已学课程
    case 2: return ClockIcon;    // 学习时长
    case 3: return FireIcon;     // 连续学习
    case 4: return ChartBarIcon; // 平均成绩
    default: return AcademicCapIcon;
  }
};

// 获取用户ID - 实际项目中应该从存储中获取已登录用户ID




// 从后端加载仪表盘数据
const loadDashboardData = () => {
  isLoading.value = true;

  
  get('/api/student/dashboard', { userId }, 
    (message, data) => {
      // 请求成功
      if (data) {
        username.value = data.username || '同学';
        stats.value = data.stats || [];
        courses.value = data.courses || [];
        recommendations.value = data.recommendations || [];
      }
      isLoading.value = false;
    }, 
    (message) => {
      // 请求失败
      console.error('获取仪表盘数据失败:', message);
      isLoading.value = false;
      // 在生产环境可以添加错误提示
    }
  );
};

// 单独加载学习统计数据 - 如需局部更新可使用
const loadLearningStats = () => {

  
  get('/api/student/dashboard/stats', { userId }, 
    (message, data) => {
      if (data) {
        stats.value = data;
      }
    }
  );
};

// 单独加载课程进度数据
const loadCourseProgress = () => {

  
  get('/api/student/dashboard/courses', { userId }, 
    (message, data) => {
      if (data) {
        courses.value = data;
      }
    }
  );
};

// 单独加载学习建议数据
const loadRecommendations = () => {

  
  get('/api/student/dashboard/recommendations', { userId }, 
    (message, data) => {
      if (data) {
        recommendations.value = data;
      }
    }
  );
};

// 页面加载时获取数据
onMounted(() => {
  // 加载仪表盘数据
  loadDashboardData();
  
  // 3秒后隐藏欢迎信息
  setTimeout(() => {
    showWelcome.value = false;
  }, 3000);
});

// 导航到指定页面
const navigateTo = (path) => {
  router.push(path);
};
</script>

<template>
  <div class="dashboard-container">
    <!-- 加载状态 -->
    <div v-if="isLoading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <p class="mt-4 text-gray-600">加载中，请稍候...</p>
    </div>
    
    <!-- 欢迎提示 -->
    <transition name="fade">
      <div v-if="showWelcome" class="welcome-banner">
        <div class="welcome-content">
          <h2 class="text-2xl font-bold">欢迎回来，{{ username }}！</h2>
          <p class="text-gray-600">今天是学习的好日子，继续保持！</p>
        </div>
      </div>
    </transition>
    
    <!-- 个人学习概览 -->
    <div class="stats-overview">
      <div v-for="stat in stats" :key="stat.id" class="stat-card" :class="stat.color">
        <component :is="stat.icon || getStatIcon(stat.id)" class="stat-icon" />
        <div class="stat-info">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-title">{{ stat.title }}</div>
        </div>
      </div>
    </div>
    
    <!-- 主要内容区域 -->
    <div class="dashboard-grid">
      <!-- 成绩总览 -->
      <div class="dashboard-card" data-aos="fade-up" data-aos-delay="100">
        <div class="card-header">
          <h3 class="card-title">成绩总览</h3>
          <button class="card-action" @click="navigateTo('/student/scores')">
            <ArrowTopRightOnSquareIcon class="h-4 w-4" />
          </button>
        </div>
        <div class="card-content">
          <ScoreOverview />
        </div>
      </div>

      <!-- 学习时长分析 -->
      <div class="dashboard-card" data-aos="fade-up" data-aos-delay="200">
        <div class="card-header">
          <h3 class="card-title">学习时长分析</h3>
          <button class="card-action" @click="navigateTo('/student/learning-timeline')">
            <ArrowTopRightOnSquareIcon class="h-4 w-4" />
          </button>
        </div>
        <div class="card-content">
          <StudyTimeWidget />
        </div>
      </div>

      <!-- 学习计划 -->
      <div class="dashboard-card" data-aos="fade-up" data-aos-delay="300">
        <div class="card-header">
          <h3 class="card-title">学习计划</h3>
          <button class="card-action" @click="navigateTo('/student/tasks')">
            <ArrowTopRightOnSquareIcon class="h-4 w-4" />
          </button>
        </div>
        <div class="card-content">
          <ReminderWidget />
        </div>
      </div>

      <!-- 学习进度 -->
      <div class="dashboard-card" data-aos="fade-up" data-aos-delay="400">
        <div class="card-header">
          <h3 class="card-title">课程学习进度</h3>
          <button class="card-action" @click="navigateTo('/student/courses')">
            <ArrowTopRightOnSquareIcon class="h-4 w-4" />
          </button>
        </div>
        <div class="card-content">
          <div class="course-progress-container">
            <div v-for="course in courses" :key="course.id" class="course-item" @click="navigateTo(`/student/courses/${course.id}`)">
              <div class="course-info">
                <div class="course-name">{{ course.name }}</div>
                <div class="course-meta">上次学习: {{ course.lastStudy }}</div>
              </div>
              <div class="progress-wrapper">
                <div class="progress-bar-bg">
                  <div class="progress-bar-fill" :class="course.color" :style="`width: ${course.progress}%`"></div>
                </div>
                <div class="progress-value">{{ course.progress }}%</div>
              </div>
            </div>
            
            <!-- 无课程数据提示 -->
            <div v-if="courses.length === 0 && !isLoading" class="no-data-tip">
              <p>暂无课程数据</p>
              <button class="explore-btn" @click="navigateTo('/student/courses')">去选课</button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 学习建议 -->
    <div class="recommendations-section">
      <h3 class="section-title">个性化学习建议</h3>
      <div class="recommendations-grid">
        <div v-for="rec in recommendations" :key="rec.id" class="recommendation-card" @click="navigateTo('/student/courses')">
          <h4 class="recommendation-title">{{ rec.title }}</h4>
          <p class="recommendation-desc">{{ rec.description }}</p>
          <div class="progress-container">
            <div class="progress-bar" :style="`width: ${rec.progress}%`"></div>
            <span class="progress-text">{{ rec.progress }}%</span>
          </div>
        </div>
        
        <!-- 无建议数据提示 -->
        <div v-if="recommendations.length === 0 && !isLoading" class="no-data-tip recommendation-card">
          <p>暂无学习建议</p>
          <button class="explore-btn" @click="loadRecommendations">刷新</button>
        </div>
      </div>
    </div>
    
    <!-- 刷新按钮 -->
    <button @click="loadDashboardData" class="refresh-btn" v-show="!isLoading">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
      </svg>
      刷新数据
    </button>
  </div>
</template>

<style scoped>
.dashboard-container {
  padding: 1.5rem;
  min-height: 100vh;
  background-color: #f3f4f6;
  position: relative;
}

/* 加载动画 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.9);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 50;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 3px solid rgba(79, 70, 229, 0.2);
  border-radius: 50%;
  border-top-color: #4f46e5;
  animation: spin 1s ease-in-out infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 欢迎横幅 */
.welcome-banner {
  background: linear-gradient(135deg, #4f46e5, #818cf8);
  border-radius: 1rem;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  color: white;
  box-shadow: 0 10px 15px -3px rgba(79, 70, 229, 0.2);
  position: relative;
  overflow: hidden;
}

.welcome-banner::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  transform: scale(0);
  animation: pulse 3s ease-in-out infinite;
}

@keyframes pulse {
  0% { transform: scale(0); opacity: 0.8; }
  100% { transform: scale(3); opacity: 0; }
}

.welcome-content {
  position: relative;
  z-index: 1;
}

/* 统计概览 */
.stats-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 1rem;
  border-radius: 0.75rem;
  background-color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 2.5rem;
  height: 2.5rem;
  padding: 0.5rem;
  border-radius: 0.5rem;
  margin-right: 1rem;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
}

.stat-title {
  font-size: 0.875rem;
  color: #6b7280;
}

/* 主要内容网格 */
.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.dashboard-card {
  background-color: white;
  border-radius: 0.75rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  height: 320px;
  transform: translateY(0);
}

.dashboard-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid #e5e7eb;
}

.card-title {
  font-size: 1rem;
  font-weight: 600;
  color: #374151;
}

.card-action {
  color: #9ca3af;
  transition: color 0.2s ease;
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.card-action:hover {
  color: #4f46e5;
  background-color: #f3f4f6;
}

.card-content {
  flex: 1;
  padding: 1rem;
  overflow: auto;
}

/* 课程进度组件 */
.course-progress-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  height: 100%;
}

.course-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 0.375rem;
  transition: background-color 0.2s ease;
}

.course-item:hover {
  background-color: #f9fafb;
}

.course-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.course-name {
  font-weight: 500;
  color: #374151;
}

.course-meta {
  font-size: 0.75rem;
  color: #6b7280;
}

.progress-wrapper {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.progress-bar-bg {
  flex-grow: 1;
  height: 0.5rem;
  background-color: #e5e7eb;
  border-radius: 9999px;
  overflow: hidden;
}

.progress-bar-fill {
  height: 100%;
  border-radius: 9999px;
  transition: width 1s ease;
}

.progress-value {
  font-size: 0.75rem;
  font-weight: 500;
  color: #6b7280;
  min-width: 2.5rem;
  text-align: right;
}

/* 学习建议区域 */
.recommendations-section {
  margin-top: 2rem;
}

.section-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 1rem;
}

.recommendations-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1rem;
}

.recommendation-card {
  background-color: white;
  border-radius: 0.75rem;
  padding: 1.25rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.2s ease;
}

.recommendation-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 15px rgba(0, 0, 0, 0.08);
}

.recommendation-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 0.5rem;
}

.recommendation-desc {
  font-size: 0.875rem;
  color: #6b7280;
  margin-bottom: 1rem;
}

.progress-container {
  height: 0.5rem;
  background-color: #e5e7eb;
  border-radius: 9999px;
  position: relative;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #4f46e5, #818cf8);
  border-radius: 9999px;
  transition: width 1s ease;
}

.progress-text {
  position: absolute;
  right: 0;
  top: -1.25rem;
  font-size: 0.75rem;
  color: #6b7280;
}

/* 无数据提示 */
.no-data-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #9ca3af;
  text-align: center;
  padding: 1rem;
}

.explore-btn {
  margin-top: 0.75rem;
  background-color: #4f46e5;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 0.375rem;
  font-size: 0.875rem;
  transition: background-color 0.2s ease;
}

.explore-btn:hover {
  background-color: #4338ca;
}

/* 刷新按钮 */
.refresh-btn {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background-color: #4f46e5;
  color: white;
  padding: 0.75rem 1rem;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  font-weight: 500;
  box-shadow: 0 4px 6px rgba(79, 70, 229, 0.25);
  transition: all 0.2s ease;
}

.refresh-btn:hover {
  background-color: #4338ca;
  transform: translateY(-2px);
  box-shadow: 0 6px 10px rgba(79, 70, 229, 0.3);
}

/* 过渡动画 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.5s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

/* 响应式调整 */
@media (min-width: 768px) {
  .dashboard-container {
    padding: 2rem;
  }
}

@media (min-width: 1024px) {
  .dashboard-container {
    padding: 2.5rem;
  }
}

@media (min-width: 1280px) {
  .dashboard-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-overview {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .recommendations-grid {
    grid-template-columns: 1fr;
  }
  
  .refresh-btn {
    bottom: 1rem;
    right: 1rem;
    padding: 0.5rem 0.75rem;
  }
}
</style> 