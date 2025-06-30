<script setup>
import {computed, onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {useRouter} from 'vue-router';

const router = useRouter();

// 统计数据
const stats = reactive({
  courses: {
    total: 12,
    active: 8,
    trend: '+2'
  },
  students: {
    total: 320,
    active: 295,
    trend: '+15'
  },
  plans: {
    total: 45,
    completed: 32,
    trend: '+5'
  },
  resources: {
    total: 156,
    downloads: 842,
    trend: '+68'
  }
});

// 近期任务
const recentTasks = ref([
  {
    id: 1,
    title: '完成《数据结构》期末试卷出题',
    deadline: '2023-07-10',
    priority: 'high',
    completed: false
  },
  {
    id: 2,
    title: '批改《算法设计》作业',
    deadline: '2023-07-08',
    priority: 'medium', 
    completed: true
  },
  {
    id: 3,
    title: '更新《面向对象编程》教学资料',
    deadline: '2023-07-15',
    priority: 'medium',
    completed: false
  },
  {
    id: 4,
    title: '准备下周《网络协议》课程',
    deadline: '2023-07-12',
    priority: 'high',
    completed: false
  },
  {
    id: 5,
    title: '参加教研室例会',
    deadline: '2023-07-07',
    priority: 'low',
    completed: false
  }
]);

// 图表类型
const chartType = ref('activity');

// 学生在线情况数据
const studentActivityData = reactive({
  days: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
  values: [120, 132, 101, 134, 90, 30, 20]
});

// 资源访问统计
const resourceTypeData = reactive({
  types: ['教案', '课件', '习题', '视频', '文档'],
  values: [35, 28, 42, 15, 30]
});

// 当前教学周
const currentWeek = ref(12);

// 根据任务优先级获取类名
const getPriorityClass = (priority) => {
  switch (priority) {
    case 'high': return 'bg-red-100 text-red-800';
    case 'medium': return 'bg-yellow-100 text-yellow-800';
    case 'low': return 'bg-green-100 text-green-800';
    default: return 'bg-gray-100 text-gray-800';
  }
};

// 完成任务
const completeTask = (task) => {
  task.completed = !task.completed;
  ElMessage({
    message: task.completed ? `任务"${task.title}"已标记为完成` : `任务"${task.title}"已标记为未完成`,
    type: task.completed ? 'success' : 'info'
  });
};

// 删除任务
const deleteTask = (taskId) => {
  ElMessageBox.confirm('确定要删除此任务吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const taskIndex = recentTasks.value.findIndex(task => task.id === taskId);
    if (taskIndex !== -1) {
      const taskTitle = recentTasks.value[taskIndex].title;
      recentTasks.value.splice(taskIndex, 1);
      ElMessage.success(`任务"${taskTitle}"已删除`);
    }
  }).catch(() => {});
};

// 添加新任务
const newTask = reactive({
  title: '',
  deadline: '',
  priority: 'medium'
});

const showNewTaskDialog = ref(false);

const addNewTask = () => {
  if (!newTask.title || !newTask.deadline) {
    ElMessage.warning('请填写完整的任务信息');
    return;
  }
  
  const task = {
    id: Date.now(),
    title: newTask.title,
    deadline: newTask.deadline,
    priority: newTask.priority,
    completed: false
  };
  
  recentTasks.value.unshift(task);
  
  // 重置表单
  newTask.title = '';
  newTask.deadline = '';
  newTask.priority = 'medium';
  
  showNewTaskDialog.value = false;
  ElMessage.success('任务添加成功');
};

// 刷新数据
const refreshData = () => {
  ElMessage({
    message: '数据已刷新',
    type: 'success'
  });
};

// 计算未完成任务数量
const uncompletedTasksCount = computed(() => {
  return recentTasks.value.filter(task => !task.completed).length;
});

// 页面加载时初始化图表
onMounted(() => {
  // 这里可以添加图表初始化代码
  // 在实际项目中可以使用ECharts或其他图表库
});
</script>

<template>
  <div class="dashboard-container">
    <div class="flex justify-between items-center mb-6">
      <h1 class="page-title">教师工作台</h1>
      <div class="flex items-center space-x-2">
        <el-button type="primary" size="small" round icon="el-icon-refresh" @click="refreshData">刷新数据</el-button>
        <el-button type="success" size="small" round icon="el-icon-plus" @click="showNewTaskDialog = true">添加任务</el-button>
      </div>
    </div>
    
    <!-- 概览统计卡片 -->
    <div class="stats-container">
      <div class="stat-card">
        <div class="stat-icon bg-blue-100 text-blue-600">
          <i class="el-icon-reading"></i>
        </div>
        <div class="stat-content">
          <div class="stat-title">我的课程</div>
          <div class="stat-value">{{ stats.courses.total }}</div>
          <div class="stat-detail">
            <span>活跃: {{ stats.courses.active }}</span>
            <span class="stat-trend">{{ stats.courses.trend }}</span>
          </div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon bg-green-100 text-green-600">
          <i class="el-icon-user"></i>
        </div>
        <div class="stat-content">
          <div class="stat-title">学生人数</div>
          <div class="stat-value">{{ stats.students.total }}</div>
          <div class="stat-detail">
            <span>活跃: {{ stats.students.active }}</span>
            <span class="stat-trend">{{ stats.students.trend }}</span>
          </div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon bg-purple-100 text-purple-600">
          <i class="el-icon-document-checked"></i>
        </div>
        <div class="stat-content">
          <div class="stat-title">教学计划</div>
          <div class="stat-value">{{ stats.plans.total }}</div>
          <div class="stat-detail">
            <span>已完成: {{ stats.plans.completed }}</span>
            <span class="stat-trend">{{ stats.plans.trend }}</span>
          </div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon bg-orange-100 text-orange-600">
          <i class="el-icon-folder-opened"></i>
        </div>
        <div class="stat-content">
          <div class="stat-title">教学资源</div>
          <div class="stat-value">{{ stats.resources.total }}</div>
          <div class="stat-detail">
            <span>下载: {{ stats.resources.downloads }}</span>
            <span class="stat-trend">{{ stats.resources.trend }}</span>
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
            <span class="week-indicator">第{{ currentWeek }}周 / 共18周</span>
          </div>
          <el-progress :percentage="(currentWeek/18)*100" :format="() => `${currentWeek}/18周`" stroke-width="12" />
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
                <div v-for="(value, index) in studentActivityData.values" :key="index" 
                     :style="{height: `${value}px`}" class="chart-bar">
                  <div class="chart-bar-tooltip">{{ value }}</div>
                </div>
              </div>
              <div class="chart-labels">
                <span v-for="(day, index) in studentActivityData.days" :key="index">{{ day }}</span>
              </div>
            </div>
            
            <div v-else class="chart-placeholder">
              <div class="chart-mock">
                <div v-for="(value, index) in resourceTypeData.values" :key="index" 
                     :style="{height: `${value * 3}px`}" class="chart-bar resource-bar">
                  <div class="chart-bar-tooltip">{{ value }}</div>
                </div>
              </div>
              <div class="chart-labels">
                <span v-for="(type, index) in resourceTypeData.types" :key="index">{{ type }}</span>
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
            <div class="announcement-item">
              <div class="announcement-badge new">新</div>
              <div class="announcement-content">
                <div class="announcement-title">关于组织2023学年第二学期期末考试的通知</div>
                <div class="announcement-date">2023-06-28</div>
              </div>
            </div>
            
            <div class="announcement-item">
              <div class="announcement-badge">公告</div>
              <div class="announcement-content">
                <div class="announcement-title">教务系统维护通知：系统将于本周六进行维护升级</div>
                <div class="announcement-date">2023-06-25</div>
              </div>
            </div>
            
            <div class="announcement-item">
              <div class="announcement-badge">公告</div>
              <div class="announcement-content">
                <div class="announcement-title">关于开展教学创新培训的通知</div>
                <div class="announcement-date">2023-06-20</div>
              </div>
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
            <el-dropdown trigger="click">
              <span class="el-dropdown-link cursor-pointer text-blue-500">
                <i class="el-icon-more"></i>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item icon="el-icon-plus" @click="showNewTaskDialog = true">添加任务</el-dropdown-item>
                  <el-dropdown-item icon="el-icon-delete">清空已完成</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          
          <div class="task-list">
            <div 
              v-for="task in recentTasks" 
              :key="task.id" 
              :class="['task-item', {'task-completed': task.completed}]"
            >
              <div class="task-left">
                <el-checkbox 
                  v-model="task.completed" 
                  @change="() => completeTask(task)"
                ></el-checkbox>
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
                  <span class="el-dropdown-link cursor-pointer">
                    <i class="el-icon-more"></i>
                  </span>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item>编辑</el-dropdown-item>
                      <el-dropdown-item @click="deleteTask(task.id)">删除</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
            
            <div v-if="recentTasks.length === 0" class="empty-tasks">
              <i class="el-icon-document-checked text-4xl text-gray-300"></i>
              <p class="text-gray-500 mt-2">暂无任务</p>
            </div>
          </div>
          
          <div class="flex justify-center mt-4">
            <el-button type="text" @click="showNewTaskDialog = true">添加新任务</el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 添加任务对话框 -->
    <el-dialog
      title="添加新任务"
      v-model="showNewTaskDialog"
      width="30%"
      :before-close="() => showNewTaskDialog = false"
    >
      <el-form :model="newTask" label-width="80px">
        <el-form-item label="任务名称">
          <el-input v-model="newTask.title" placeholder="请输入任务名称"></el-input>
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker
            v-model="newTask.deadline"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="newTask.priority" placeholder="请选择" style="width: 100%">
            <el-option label="紧急" value="high"></el-option>
            <el-option label="普通" value="medium"></el-option>
            <el-option label="低" value="low"></el-option>
          </el-select>
        </el-form-item>
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