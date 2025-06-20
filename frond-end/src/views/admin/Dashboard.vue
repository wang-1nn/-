<script setup>
import { ref, onMounted, reactive } from 'vue';
import { get } from '@/net';
import LucideIcon from '@/components/icons/LucideIcon.vue';

// 仪表盘数据
const stats = reactive({
  totalUsers: 0,
  totalTeachers: 0,
  totalStudents: 0,
  totalClasses: 0,
  totalCourses: 0,
  activeUsers: 0,
  newUsersToday: 0,
  newUsersThisWeek: 0
});

// 系统状态
const systemStatus = ref('normal'); // normal, warning, error
const loading = ref(true);

// 最近活动
const recentActivities = ref([]);

// 用户类型分布
const userDistribution = reactive({
  labels: ['管理员', '教师', '学生'],
  data: [0, 0, 0]
});

// 周活跃用户
const weeklyActiveUsers = reactive({
  labels: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
  data: [0, 0, 0, 0, 0, 0, 0]
});

// 获取仪表盘数据
const fetchDashboardData = () => {
  loading.value = true;
  get('/api/admin/dashboard', null, 
    (message, data) => {
      // 填充统计数据
      stats.totalUsers = data.totalUsers || 0;
      stats.totalTeachers = data.totalTeachers || 0;
      stats.totalStudents = data.totalStudents || 0;
      stats.totalClasses = data.totalClasses || 0;
      stats.totalCourses = data.totalCourses || 0;
      stats.activeUsers = data.activeUsers || 0;
      stats.newUsersToday = data.newUsersToday || 0;
      stats.newUsersThisWeek = data.newUsersThisWeek || 0;
      
      // 系统状态
      systemStatus.value = data.systemStatus || 'normal';
      
      // 最近活动
      recentActivities.value = data.recentActivities || [];
      
      // 用户分布
      if (data.userDistribution) {
        userDistribution.data = [
          data.userDistribution.admins || 0,
          data.userDistribution.teachers || 0,
          data.userDistribution.students || 0
        ];
      }
      
      // 周活跃用户
      if (data.weeklyActiveUsers) {
        weeklyActiveUsers.data = data.weeklyActiveUsers;
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

// 生成模拟数据（在API不可用时使用）
const mockDashboardData = () => {
  stats.totalUsers = 286;
  stats.totalTeachers = 42;
  stats.totalStudents = 240;
  stats.totalClasses = 18;
  stats.totalCourses = 35;
  stats.activeUsers = 198;
  stats.newUsersToday = 5;
  stats.newUsersThisWeek = 23;
  
  systemStatus.value = 'normal';
  
  recentActivities.value = [
    { id: 1, type: 'user', action: '登录系统', user: '张老师', time: '10分钟前', icon: 'log-in' },
    { id: 2, type: 'class', action: '创建了新班级', user: '王管理员', time: '30分钟前', icon: 'users' },
    { id: 3, type: 'course', action: '添加了新课程', user: '李老师', time: '1小时前', icon: 'book-open' },
    { id: 4, type: 'user', action: '重置了密码', user: '赵同学', time: '2小时前', icon: 'key' },
    { id: 5, type: 'system', action: '系统维护', user: '系统', time: '昨天 14:30', icon: 'settings' }
  ];
  
  userDistribution.data = [4, 42, 240];
  
  weeklyActiveUsers.data = [120, 132, 101, 134, 90, 70, 85];
};

// 获取状态图标和颜色
const getStatusInfo = (status) => {
  switch (status) {
    case 'normal':
      return { icon: 'check-circle', color: 'text-green-500', bgColor: 'bg-green-100', label: '系统正常' };
    case 'warning':
      return { icon: 'alert-triangle', color: 'text-yellow-500', bgColor: 'bg-yellow-100', label: '系统警告' };
    case 'error':
      return { icon: 'alert-octagon', color: 'text-red-500', bgColor: 'bg-red-100', label: '系统错误' };
    default:
      return { icon: 'help-circle', color: 'text-gray-500', bgColor: 'bg-gray-100', label: '未知状态' };
  }
};

// 获取活动图标和颜色
const getActivityInfo = (type) => {
  switch (type) {
    case 'user':
      return { bgColor: 'bg-blue-100', textColor: 'text-blue-700' };
    case 'class':
      return { bgColor: 'bg-green-100', textColor: 'text-green-700' };
    case 'course':
      return { bgColor: 'bg-purple-100', textColor: 'text-purple-700' };
    case 'system':
      return { bgColor: 'bg-gray-100', textColor: 'text-gray-700' };
    default:
      return { bgColor: 'bg-gray-100', textColor: 'text-gray-700' };
  }
};

// 计算用户增长率
const calculateGrowthRate = () => {
  // 假设上周新增用户为18
  const lastWeekNewUsers = 18;
  const thisWeekNewUsers = stats.newUsersThisWeek;
  
  if (lastWeekNewUsers === 0) return 100;
  
  const growthRate = ((thisWeekNewUsers - lastWeekNewUsers) / lastWeekNewUsers) * 100;
  return Math.round(growthRate);
};

// 页面加载时获取数据
onMounted(() => {
  fetchDashboardData();
});
</script>

<template>
  <div class="dashboard-container">
    <div class="dashboard-header">
      <h1 class="dashboard-title">系统概况</h1>
      <div class="flex items-center">
        <div 
          class="status-indicator" 
          :class="getStatusInfo(systemStatus).bgColor"
        >
          <LucideIcon 
            :name="getStatusInfo(systemStatus).icon" 
            :class="getStatusInfo(systemStatus).color" 
            size="16"
          />
          <span class="ml-1 text-sm">{{ getStatusInfo(systemStatus).label }}</span>
        </div>
        
        <button @click="fetchDashboardData" class="btn-outline ml-4">
          <LucideIcon name="refresh-cw" size="16" :class="{ 'animate-spin': loading }" class="mr-1" />
          刷新数据
        </button>
      </div>
    </div>
    
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-card-icon bg-blue-100 text-blue-600">
          <LucideIcon name="users" size="20" />
        </div>
        <div class="stat-card-content">
          <div class="stat-card-value">{{ stats.totalUsers }}</div>
          <div class="stat-card-label">总用户数</div>
        </div>
        <div class="stat-card-trend">
          <LucideIcon name="user-plus" size="14" class="mr-1" />
          <span class="text-green-600">+{{ stats.newUsersToday }} 今日</span>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-card-icon bg-purple-100 text-purple-600">
          <LucideIcon name="user-check" size="20" />
        </div>
        <div class="stat-card-content">
          <div class="stat-card-value">{{ stats.totalTeachers }}</div>
          <div class="stat-card-label">教师数量</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-card-icon bg-indigo-100 text-indigo-600">
          <LucideIcon name="graduation-cap" size="20" />
        </div>
        <div class="stat-card-content">
          <div class="stat-card-value">{{ stats.totalStudents }}</div>
          <div class="stat-card-label">学生数量</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-card-icon bg-green-100 text-green-600">
          <LucideIcon name="school" size="20" />
        </div>
        <div class="stat-card-content">
          <div class="stat-card-value">{{ stats.totalClasses }}</div>
          <div class="stat-card-label">班级数量</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-card-icon bg-amber-100 text-amber-600">
          <LucideIcon name="book-open" size="20" />
        </div>
        <div class="stat-card-content">
          <div class="stat-card-value">{{ stats.totalCourses }}</div>
          <div class="stat-card-label">课程数量</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-card-icon bg-red-100 text-red-600">
          <LucideIcon name="activity" size="20" />
        </div>
        <div class="stat-card-content">
          <div class="stat-card-value">{{ stats.activeUsers }}</div>
          <div class="stat-card-label">活跃用户</div>
        </div>
        <div class="stat-card-trend">
          <span 
            :class="calculateGrowthRate() >= 0 ? 'text-green-600' : 'text-red-600'"
          >
            <LucideIcon 
              :name="calculateGrowthRate() >= 0 ? 'trending-up' : 'trending-down'" 
              size="14" 
              class="mr-1" 
            />
            {{ calculateGrowthRate() >= 0 ? '+' : '' }}{{ calculateGrowthRate() }}% 周增长率
          </span>
        </div>
      </div>
    </div>
    
    <!-- 图表和活动区域 -->
    <div class="dashboard-content">
      <!-- 左侧图表 -->
      <div class="dashboard-charts">
        <!-- 用户分布卡片 -->
        <div class="chart-card">
          <div class="chart-card-header">
            <h3 class="chart-card-title">用户类型分布</h3>
          </div>
          <div class="chart-card-body">
            <div class="flex items-center justify-center h-48">
              <!-- 这里应该放置实际的图表，这里简化为文本展示 -->
              <div class="distribution-chart">
                <div 
                  v-for="(value, index) in userDistribution.data" 
                  :key="`user-dist-${index}`"
                  class="distribution-item"
                >
                  <div 
                    class="distribution-bar" 
                    :style="{height: `${(value / Math.max(...userDistribution.data)) * 100}%`}"
                    :class="{
                      'bg-indigo-500': index === 0,
                      'bg-purple-500': index === 1,
                      'bg-blue-500': index === 2
                    }"
                  ></div>
                  <div class="distribution-label">{{ userDistribution.labels[index] }}</div>
                  <div class="distribution-value">{{ value }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 周活跃用户卡片 -->
        <div class="chart-card">
          <div class="chart-card-header">
            <h3 class="chart-card-title">本周活跃用户</h3>
          </div>
          <div class="chart-card-body">
            <div class="flex items-center justify-center h-48">
              <!-- 这里应该放置实际的图表，这里简化为文本展示 -->
              <div class="weekly-chart">
                <div 
                  v-for="(value, index) in weeklyActiveUsers.data" 
                  :key="`weekly-${index}`"
                  class="weekly-item"
                >
                  <div 
                    class="weekly-bar" 
                    :style="{height: `${(value / Math.max(...weeklyActiveUsers.data)) * 100}%`}"
                  ></div>
                  <div class="weekly-label">{{ weeklyActiveUsers.labels[index] }}</div>
                  <div class="weekly-value">{{ value }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧活动列表 -->
      <div class="dashboard-activities">
        <div class="activities-card">
          <div class="activities-header">
            <h3 class="activities-title">最近活动</h3>
          </div>
          <div class="activities-body">
            <div v-if="loading" class="flex justify-center py-8">
              <LucideIcon name="loader-2" size="24" class="animate-spin text-gray-400" />
            </div>
            
            <div v-else-if="recentActivities.length === 0" class="empty-activities">
              <LucideIcon name="calendar" size="24" class="text-gray-400 mb-2" />
              <p class="text-gray-500">暂无活动记录</p>
            </div>
            
            <div v-else class="activities-list">
              <div 
                v-for="activity in recentActivities" 
                :key="activity.id"
                class="activity-item"
              >
                <div 
                  class="activity-icon" 
                  :class="getActivityInfo(activity.type).bgColor"
                >
                  <LucideIcon 
                    :name="activity.icon" 
                    size="16" 
                    :class="getActivityInfo(activity.type).textColor" 
                  />
                </div>
                <div class="activity-content">
                  <div class="activity-header">
                    <span class="activity-user">{{ activity.user }}</span>
                    <span class="activity-action">{{ activity.action }}</span>
                  </div>
                  <div class="activity-time">{{ activity.time }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 系统状态卡片 -->
        <div class="system-card">
          <div class="system-header">
            <h3 class="system-title">系统状态</h3>
          </div>
          <div class="system-body">
            <div class="system-stats">
              <div class="system-stat-item">
                <div class="system-stat-icon bg-green-100 text-green-600">
                  <LucideIcon name="cpu" size="18" />
                </div>
                <div class="system-stat-content">
                  <div class="system-stat-label">CPU使用率</div>
                  <div class="system-stat-value">23%</div>
                </div>
              </div>
              
              <div class="system-stat-item">
                <div class="system-stat-icon bg-blue-100 text-blue-600">
                  <LucideIcon name="database" size="18" />
                </div>
                <div class="system-stat-content">
                  <div class="system-stat-label">内存使用率</div>
                  <div class="system-stat-value">42%</div>
                </div>
              </div>
              
              <div class="system-stat-item">
                <div class="system-stat-icon bg-purple-100 text-purple-600">
                  <LucideIcon name="hard-drive" size="18" />
                </div>
                <div class="system-stat-content">
                  <div class="system-stat-label">存储空间</div>
                  <div class="system-stat-value">68%</div>
                </div>
              </div>
              
              <div class="system-stat-item">
                <div class="system-stat-icon bg-amber-100 text-amber-600">
                  <LucideIcon name="clock" size="18" />
                </div>
                <div class="system-stat-content">
                  <div class="system-stat-label">运行时间</div>
                  <div class="system-stat-value">7天</div>
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
.dashboard-container {
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
}

.status-indicator {
  display: flex;
  align-items: center;
  padding: 0.375rem 0.75rem;
  border-radius: 0.375rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.stat-card {
  background-color: white;
  border-radius: 0.5rem;
  padding: 1.25rem;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: flex-start;
}

.stat-card-icon {
  width: 40px;
  height: 40px;
  border-radius: 0.375rem;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 1rem;
  flex-shrink: 0;
}

.stat-card-content {
  flex: 1;
}

.stat-card-value {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1e293b;
  line-height: 1.2;
}

.stat-card-label {
  font-size: 0.875rem;
  color: #64748b;
}

.stat-card-trend {
  font-size: 0.75rem;
  display: flex;
  align-items: center;
  margin-top: 0.5rem;
}

.dashboard-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

.dashboard-charts {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.chart-card {
  background-color: white;
  border-radius: 0.5rem;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.chart-card-header {
  padding: 1rem 1.25rem;
  border-bottom: 1px solid #e5e7eb;
}

.chart-card-title {
  font-size: 1rem;
  font-weight: 600;
  color: #1e293b;
}

.chart-card-body {
  padding: 1.25rem;
}

.dashboard-activities {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.activities-card,
.system-card {
  background-color: white;
  border-radius: 0.5rem;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.activities-header,
.system-header {
  padding: 1rem 1.25rem;
  border-bottom: 1px solid #e5e7eb;
}

.activities-title,
.system-title {
  font-size: 1rem;
  font-weight: 600;
  color: #1e293b;
}

.activities-body,
.system-body {
  padding: 1.25rem;
}

.empty-activities {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem 0;
}

.activities-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.activity-item {
  display: flex;
  align-items: flex-start;
}

.activity-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 0.75rem;
  flex-shrink: 0;
}

.activity-content {
  flex: 1;
}

.activity-header {
  display: flex;
  align-items: center;
  font-size: 0.875rem;
}

.activity-user {
  font-weight: 500;
  color: #1e293b;
  margin-right: 0.25rem;
}

.activity-action {
  color: #64748b;
}

.activity-time {
  font-size: 0.75rem;
  color: #94a3b8;
  margin-top: 0.25rem;
}

.system-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
}

.system-stat-item {
  display: flex;
  align-items: center;
}

.system-stat-icon {
  width: 36px;
  height: 36px;
  border-radius: 0.375rem;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 0.75rem;
  flex-shrink: 0;
}

.system-stat-content {
  flex: 1;
}

.system-stat-label {
  font-size: 0.75rem;
  color: #64748b;
}

.system-stat-value {
  font-size: 1rem;
  font-weight: 600;
  color: #1e293b;
}

/* 简易图表样式 */
.distribution-chart,
.weekly-chart {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 100%;
  width: 100%;
}

.distribution-item,
.weekly-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
  flex: 1;
}

.distribution-bar,
.weekly-bar {
  width: 30px;
  max-width: 80%;
  border-radius: 4px 4px 0 0;
  transition: height 0.3s ease;
  min-height: 4px;
}

.weekly-bar {
  background-color: #3b82f6;
}

.distribution-label,
.weekly-label {
  margin-top: 0.5rem;
  font-size: 0.75rem;
  color: #64748b;
}

.distribution-value,
.weekly-value {
  font-size: 0.875rem;
  font-weight: 500;
  color: #1e293b;
}

.btn-outline {
  display: inline-flex;
  align-items: center;
  padding: 0.5rem 0.75rem;
  border: 1px solid #e2e8f0;
  border-radius: 0.375rem;
  font-size: 0.875rem;
  color: #475569;
  background-color: white;
  transition: all 0.2s;
}

.btn-outline:hover {
  background-color: #f8fafc;
  border-color: #cbd5e1;
}

@media (max-width: 1024px) {
  .dashboard-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .system-stats {
    grid-template-columns: 1fr;
  }
}
</style> 