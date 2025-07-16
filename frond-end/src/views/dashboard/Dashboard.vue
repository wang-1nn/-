<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { get, post } from '../../net'; 

// This should come from a user store, e.g., Pinia
const uid = 1; 

// Reactive state for all dashboard data
const summary = ref(null);
const announcements = ref([]);
const tasks = ref([]);
const studentActivityData = reactive({ labels: [], values: [] });
const resourceAccessData = reactive({ labels: [], values: [] });
const chartType = ref('activity');
const loading = ref(true);

// Fetch all data from the backend
const fetchData = () => {
  loading.value = true;
  
  // 记录已完成的请求数量
  let completedRequests = 0;
  const totalRequests = 5;
  
  // 检查是否所有请求都已完成
  const checkAllRequestsCompleted = () => {
    completedRequests++;
    if (completedRequests === totalRequests) {
      loading.value = false;
    }
  };
  
  // 获取教师工作台概览数据
  get('/api/teacher/dashboard/summary', { uid }, 
    (message, data) => {
      summary.value = data;
      checkAllRequestsCompleted();
    },
    (message) => {
      ElMessage.warning(`获取概览数据失败: ${message}`);
      checkAllRequestsCompleted();
    },
    (error) => {
      console.error('Error fetching summary:', error);
      checkAllRequestsCompleted();
    }
  );
  
  // 获取通知公告列表
  get('/api/teacher/dashboard/announcements', { uid }, 
    (message, data) => {
      announcements.value = data;
      checkAllRequestsCompleted();
    },
    (message) => {
      ElMessage.warning(`获取公告失败: ${message}`);
      checkAllRequestsCompleted();
    },
    (error) => {
      console.error('Error fetching announcements:', error);
      checkAllRequestsCompleted();
    }
  );
  
  // 获取待办任务列表
  get('/api/teacher/dashboard/pending-tasks', { uid }, 
    (message, data) => {
      tasks.value = data;
      checkAllRequestsCompleted();
    },
    (message) => {
      ElMessage.warning(`获取任务失败: ${message}`);
      checkAllRequestsCompleted();
    },
    (error) => {
      console.error('Error fetching tasks:', error);
      checkAllRequestsCompleted();
    }
  );
  
  // 获取学生活动数据
  get('/api/teacher/dashboard/student-activity', { uid }, 
    (message, data) => {
      studentActivityData.labels = data.labels;
      studentActivityData.values = data.values;
      checkAllRequestsCompleted();
    },
    (message) => {
      ElMessage.warning(`获取学生活动数据失败: ${message}`);
      checkAllRequestsCompleted();
    },
    (error) => {
      console.error('Error fetching student activity:', error);
      checkAllRequestsCompleted();
    }
  );
  
  // 获取资源访问数据
  get('/api/teacher/dashboard/resource-access', { uid }, 
    (message, data) => {
      resourceAccessData.labels = data.labels;
      resourceAccessData.values = data.values;
      checkAllRequestsCompleted();
    },
    (message) => {
      ElMessage.warning(`获取资源访问数据失败: ${message}`);
      checkAllRequestsCompleted();
    },
    (error) => {
      console.error('Error fetching resource access:', error);
      checkAllRequestsCompleted();
    }
  );
};

onMounted(fetchData);

// Task management
const getPriorityClass = (priority) => {
  switch (priority) {
    case 'high': return 'bg-red-100 text-red-800';
    case 'medium': return 'bg-yellow-100 text-yellow-800';
    case 'low': return 'bg-green-100 text-green-800';
    default: return 'bg-gray-100 text-gray-800';
  }
};

const completeTask = (task) => {
  // 选择正确的接口
  // task.completed表示用户已经勾选了任务（将其标记为已完成）或取消了勾选（将其标记为未完成）
  const endpoint = task.completed 
    ? `/api/teacher/dashboard/tasks/${task.id}/complete` // 用户勾选→调用完成接口
    : `/api/teacher/dashboard/tasks/${task.id}/uncomplete`; // 用户取消勾选→调用取消完成接口
  
  post(endpoint, 
    {},  // 无需传递参数，任务ID已在URL中
    // 成功回调
    (message) => {
      ElMessage({
        message: task.completed ? `任务"${task.title}"已标记为完成` : `任务"${task.title}"已标记为未完成`,
        type: task.completed ? 'success' : 'info',
      });
    },
    // 失败回调
    (message) => {
      ElMessage.error(`任务状态更新失败: ${message}`);
      task.completed = !task.completed; // 恢复原状态
    },
    // 错误回调
    (error) => {
      console.error('Error updating task status:', error);
      task.completed = !task.completed; // 恢复原状态
    },
    true // 使用JSON格式
  );
};

const deleteTask = (taskId) => {
  ElMessageBox.confirm('确定要删除此任务吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    // 使用专用的删除任务接口
    post(`/api/teacher/dashboard/tasks/${taskId}/delete`, 
      {},  // 无需传递额外参数
      // 成功回调
      (message) => {
        const taskIndex = tasks.value.findIndex(t => t.id === taskId);
        if (taskIndex !== -1) {
          ElMessage.success(`任务"${tasks.value[taskIndex].title}"已删除`);
          tasks.value.splice(taskIndex, 1);
        }
      },
      // 失败回调
      (message) => {
        ElMessage.error(`删除任务失败: ${message}`);
      },
      // 错误回调
      (error) => {
        console.error('Error deleting task:', error);
        ElMessage.error('删除任务时发生错误');
      },
      true // 使用JSON格式
    );
  }).catch(() => {});
};

const newTask = reactive({ title: '', deadline: '', priority: 'medium' });
const showNewTaskDialog = ref(false);

const addNewTask = () => {
  if (!newTask.title || !newTask.deadline) {
    ElMessage.warning('请填写完整的任务信息');
    return;
  }
  
  // 创建新任务对象
  const taskData = {
    uid: uid.toString(), // 转为字符串，确保与后端期望的类型一致
    title: newTask.title,
    deadline: newTask.deadline,
    priority: newTask.priority
  };
  
  post('/api/teacher/dashboard/tasks', 
    taskData,
    // 成功回调
    (message, data) => {
      // 将新任务添加到列表最前面
      tasks.value.unshift(data);
      
      // 重置表单
      newTask.title = '';
      newTask.deadline = '';
      newTask.priority = 'medium';
      
      // 关闭对话框
      showNewTaskDialog.value = false;
      
      ElMessage.success('任务添加成功');
    },
    // 失败回调
    (message) => {
      ElMessage.error(`添加任务失败: ${message}`);
    },
    // 错误回调
    (error) => {
      console.error('Error adding task:', error);
      ElMessage.error('添加任务时发生错误');
    },
    true // 使用JSON格式
  );
};

const uncompletedTasksCount = computed(() => {
  return tasks.value.filter(task => !task.completed).length;
});
</script>

<template>
  <div class="dashboard-container" v-loading="loading">
    <div class="flex justify-between items-center mb-6">
      <h1 class="page-title">教师工作台</h1>
      <div class="flex items-center space-x-2">
        <el-button type="primary" size="small" round icon="el-icon-refresh" @click="fetchData">刷新数据</el-button>
        <el-button type="success" size="small" round icon="el-icon-plus" @click="showNewTaskDialog = true">添加任务</el-button>
      </div>
    </div>
    
    <template v-if="summary">
      <!-- 概览统计卡片 -->
      <div class="stats-container">
        <div class="stat-card">
          <div class="stat-icon bg-blue-100 text-blue-600"><i class="el-icon-reading"></i></div>
          <div class="stat-content">
            <div class="stat-title">我的课程</div>
            <div class="stat-value">{{ summary.courses.total }}</div>
            <div class="stat-detail">
              <span>活跃: {{ summary.courses.active }}</span>
              <span class="stat-trend">+{{ summary.courses.trend }}</span>
            </div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon bg-green-100 text-green-600"><i class="el-icon-user"></i></div>
          <div class="stat-content">
            <div class="stat-title">学生人数</div>
            <div class="stat-value">{{ summary.students.total }}</div>
            <div class="stat-detail">
              <span>活跃: {{ summary.students.active }}</span>
            </div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon bg-purple-100 text-purple-600"><i class="el-icon-document-checked"></i></div>
          <div class="stat-content">
            <div class="stat-title">教学计划</div>
            <div class="stat-value">{{ summary.plans.total }}</div>
            <div class="stat-detail">
              <span class="stat-trend">+{{ summary.plans.trend }}</span>
            </div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon bg-orange-100 text-orange-600"><i class="el-icon-folder-opened"></i></div>
          <div class="stat-content">
            <div class="stat-title">教学资源</div>
            <div class="stat-value">{{ summary.resources.total }}</div>
            <div class="stat-detail">
              <span class="stat-trend">+{{ summary.resources.trend }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- 左侧区域 -->
        <div class="lg:col-span-2 space-y-6">
          <!-- 当前教学周进度 -->
          <div class="semester-progress-card">
            <div class="card-header">
              <h3>当前学期进度</h3>
              <span class="week-indicator">第{{ summary.currentWeek }}周 / 共{{ summary.totalWeeks }}周</span>
            </div>
            <el-progress :percentage="(summary.currentWeek/summary.totalWeeks)*100" :format="() => `${summary.currentWeek}/${summary.totalWeeks}周`" stroke-width="12" />
          </div>
          
          <!-- 数据统计图表 -->
          <div class="charts-card">
            <div class="card-header">
              <h3>数据统计</h3>
              <el-radio-group v-model="chartType" size="small">
                <el-radio-button label="activity">学生活跃度</el-radio-button>
                <el-radio-button label="resources">资源访问</el-radio-button>
              </el-radio-group>
            </div>
            <div class="chart-container">
              <div v-if="chartType === 'activity'" class="chart-placeholder">
                 <div class="chart-mock">
                    <div v-for="(value, index) in studentActivityData.values" :key="index" :style="{height: `${(value / Math.max(...studentActivityData.values, 1)) * 150}px`}" class="chart-bar">
                      <div class="chart-bar-tooltip">{{ value }}</div>
                    </div>
                 </div>
                 <div class="chart-labels">
                    <span v-for="(day, index) in studentActivityData.labels" :key="index">{{ day }}</span>
                 </div>
              </div>
              <div v-else class="chart-placeholder">
                  <div class="chart-mock">
                    <div v-for="(value, index) in resourceAccessData.values" :key="index" :style="{height: `${(value / Math.max(...resourceAccessData.values, 1)) * 150}px`}" class="chart-bar resource-bar">
                        <div class="chart-bar-tooltip">{{ value }}</div>
                    </div>
                  </div>
                  <div class="chart-labels">
                    <span v-for="(type, index) in resourceAccessData.labels" :key="index">{{ type }}</span>
                  </div>
              </div>
            </div>
          </div>
          
          <!-- 通知公告 -->
          <div class="announcements-card">
            <div class="card-header">
              <h3>通知公告</h3>
              <el-button type="text">全部</el-button>
            </div>
            <div class="announcement-list">
              <div class="announcement-item" v-for="item in announcements" :key="item.id">
                <div class="announcement-badge new">{{ item.courseName }}</div>
                <div class="announcement-content">
                  <div class="announcement-title">{{ item.title }}</div>
                  <div class="announcement-date">{{ new Date(item.timestamp).toLocaleDateString() }}</div>
                </div>
              </div>
               <div v-if="!announcements.length" class="empty-tasks">
                 <p class="text-gray-500 mt-2">暂无公告</p>
               </div>
            </div>
          </div>
        </div>
        
        <!-- 右侧区域 -->
        <div class="space-y-6">
          <!-- 近期任务 -->
          <div class="task-list-card h-full">
            <div class="card-header">
              <h3>近期任务 <span class="task-counter">{{ uncompletedTasksCount }}</span></h3>
            </div>
            <div class="task-list">
              <div v-for="task in tasks" :key="task.id" :class="['task-item', {'task-completed': task.completed}]">
                <div class="task-left">
                  <el-checkbox v-model="task.completed" @change="() => completeTask(task)"></el-checkbox>
                  <div class="task-info">
                    <div class="task-title">{{ task.title }}</div>
                    <div class="task-deadline">截止日期: {{ task.deadline }}</div>
                  </div>
                </div>
                <div class="task-right">
                  <span :class="['priority-badge', getPriorityClass(task.priority)]">
                    {{ task.priority === 'high' ? '紧急' : task.priority === 'medium' ? '普通' : '低' }}
                  </span>
                  <el-dropdown trigger="click" size="small">
                    <span class="el-dropdown-link cursor-pointer"><i class="el-icon-more"></i></span>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item @click="deleteTask(task.id)">删除</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </div>
              <div v-if="tasks.length === 0" class="empty-tasks">
                <i class="el-icon-document-checked text-4xl text-gray-300"></i>
                <p class="text-gray-500 mt-2">暂无任务</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
    
    <!-- 添加任务对话框 -->
    <el-dialog title="添加新任务" v-model="showNewTaskDialog" width="30%" :before-close="() => showNewTaskDialog = false">
      <el-form :model="newTask" label-width="80px">
        <el-form-item label="任务名称"><el-input v-model="newTask.title" placeholder="请输入任务名称"></el-input></el-form-item>
        <el-form-item label="截止日期"><el-date-picker v-model="newTask.deadline" type="date" placeholder="选择日期" format="YYYY-MM-DD" value-format="YYYY-MM-DD" style="width: 100%"></el-date-picker></el-form-item>
        <el-form-item label="优先级"><el-select v-model="newTask.priority" placeholder="请选择" style="width: 100%"><el-option label="紧急" value="high"></el-option><el-option label="普通" value="medium"></el-option><el-option label="低" value="low"></el-option></el-select></el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showNewTaskDialog = false">取消</el-button>
          <el-button type="primary" @click="addNewTask">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.dashboard-container {
  @apply p-6;
}

.page-title {
  @apply text-2xl font-bold text-gray-800;
}

/* 统计卡片样式 */
.stats-container {
  @apply grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5 mb-6;
}

.stat-card {
  @apply bg-white rounded-xl shadow-md p-5 flex items-center hover:shadow-lg transition-shadow duration-300 border border-gray-100;
  transform: translateY(0);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-3px);
}

.stat-icon {
  @apply w-14 h-14 rounded-full flex items-center justify-center text-xl mr-4;
}

.stat-content {
  @apply flex-1;
}

.stat-title {
  @apply text-sm text-gray-500 font-medium;
}

.stat-value {
  @apply text-2xl font-bold mt-1;
}

.stat-detail {
  @apply text-xs text-gray-500 flex justify-between mt-1;
}

.stat-trend {
  @apply text-green-500 font-medium;
}

/* 学期进度卡片 */
.semester-progress-card {
  @apply bg-white rounded-xl shadow-md p-5 border border-gray-100 hover:shadow-lg transition-all duration-300;
}

.week-indicator {
  @apply text-sm text-gray-500 font-medium;
}

/* 通用卡片样式 */
.card-header {
  @apply flex justify-between items-center mb-5;
}

.card-header h3 {
  @apply text-lg font-semibold text-gray-800;
}

.task-counter {
  @apply ml-2 text-sm bg-blue-100 text-blue-700 px-2 py-0.5 rounded-full;
}

/* 任务列表样式 */
.task-list-card {
  @apply bg-white rounded-xl shadow-md p-5 border border-gray-100 hover:shadow-lg transition-all duration-300;
}

.task-list {
  @apply space-y-3 max-h-[450px] overflow-auto pr-2;
}

.task-item {
  @apply flex justify-between items-center p-4 rounded-lg border border-gray-200 hover:border-blue-200 transition-all duration-200;
}

.task-completed {
  @apply bg-gray-50;
}

.task-completed .task-title {
  @apply line-through text-gray-400;
}

.task-left {
  @apply flex items-center;
}

.task-info {
  @apply ml-3;
}

.task-title {
  @apply text-sm font-medium;
}

.task-deadline {
  @apply text-xs text-gray-500 mt-1;
}

.task-right {
  @apply flex items-center gap-3;
}

.priority-badge {
  @apply text-xs px-2 py-1 rounded-full;
}

.empty-tasks {
  @apply flex flex-col items-center justify-center py-10 text-center;
}

/* 图表卡片 */
.charts-card {
  @apply bg-white rounded-xl shadow-md p-5 border border-gray-100 hover:shadow-lg transition-all duration-300;
}

.chart-container {
  @apply h-64;
}

/* 模拟图表样式，实际项目中应使用真实图表库 */
.chart-placeholder {
  @apply h-full flex flex-col;
}

.chart-mock {
  @apply flex justify-around items-end h-48 border-b border-gray-200 px-4;
}

.chart-bar {
  @apply w-10 bg-gradient-to-t from-blue-500 to-blue-400 rounded-t-md hover:from-blue-600 hover:to-blue-500 transition-colors duration-300 relative;
  max-height: 100%;
}

.resource-bar {
  @apply bg-gradient-to-t from-purple-500 to-purple-400 hover:from-purple-600 hover:to-purple-500;
}

.chart-bar-tooltip {
  @apply absolute -top-7 left-1/2 transform -translate-x-1/2 bg-gray-800 text-white text-xs py-1 px-2 rounded opacity-0 transition-opacity duration-200;
}

.chart-bar:hover .chart-bar-tooltip {
  @apply opacity-100;
}

.chart-labels {
  @apply flex justify-around pt-2;
}

.chart-labels span {
  @apply text-xs text-gray-500 font-medium;
}

/* 通知公告样式 */
.announcements-card {
  @apply bg-white rounded-xl shadow-md p-5 border border-gray-100 hover:shadow-lg transition-all duration-300;
}

.announcement-list {
  @apply space-y-4;
}

.announcement-item {
  @apply flex p-4 rounded-lg border border-gray-200 hover:border-blue-200 hover:bg-blue-50 transition-all duration-200 cursor-pointer;
}

.announcement-badge {
  @apply bg-gray-100 text-gray-800 text-xs px-3 py-1 rounded-full h-min mr-4 font-medium;
}

.announcement-badge.new {
  @apply bg-red-100 text-red-800;
}

.announcement-content {
  @apply flex-1;
}

.announcement-title {
  @apply text-sm font-medium;
}

.announcement-date {
  @apply text-xs text-gray-500 mt-2;
}
</style> 