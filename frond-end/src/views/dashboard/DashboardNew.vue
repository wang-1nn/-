<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { get, post } from '../../net';
import Card from '../../components/common/Card.vue';
import Button from '../../components/common/Button.vue';
import Alert from '../../components/common/Alert.vue';
import Badge from '../../components/common/Badge.vue';
import Tabs from '../../components/common/Tabs.vue';
import TabPane from '../../components/common/TabPane.vue';
import TabNav from '../../components/common/TabNav.vue';

// 用户ID，应该从全局状态管理中获取
const uid = 1; 

// 仪表盘状态
const summary = ref(null);
const announcements = ref([]);
const tasks = ref([]);
const studentActivityData = reactive({ labels: [], values: [] });
const resourceAccessData = reactive({ labels: [], values: [] });
const chartType = ref('activity');
const loading = ref(true);

// 获取所有数据
const fetchData = () => {
  loading.value = true;
  
  // 模拟数据，实际项目中应使用真实API
  setTimeout(() => {
    // 模拟概览数据
    summary.value = {
      courseCount: 5,
      studentCount: 120,
      pendingGradingCount: 45,
      upcomingDeadlines: 3,
    };
    
    // 模拟通知公告
    announcements.value = [
      { id: 1, title: '关于期末考试安排的通知', date: '2023-12-15', important: true },
      { id: 2, title: '教学管理系统更新公告', date: '2023-12-10', important: false },
      { id: 3, title: '教研室会议通知', date: '2023-12-08', important: true },
    ];
    
    // 模拟任务数据
    tasks.value = [
      { id: 1, title: '高三数学期末试卷批改', deadline: '2023-12-20', priority: 'high', completed: false },
      { id: 2, title: '备课组教研活动', deadline: '2023-12-18', priority: 'medium', completed: false },
      { id: 3, title: '提交教学计划', deadline: '2023-12-25', priority: 'low', completed: true },
    ];
    
    // 模拟图表数据
    studentActivityData.labels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
    studentActivityData.values = [23, 34, 45, 56, 37, 18, 12];
    
    resourceAccessData.labels = ['课件', '作业', '练习', '视频', '试卷'];
    resourceAccessData.values = [42, 28, 35, 18, 22];
    
    loading.value = false;
  }, 800);
};

// 初始化数据
onMounted(fetchData);

// 任务管理
const getPriorityClass = (priority) => {
  switch (priority) {
    case 'high': return 'ui-badge-error';
    case 'medium': return 'ui-badge-warning';
    case 'low': return 'ui-badge-success';
    default: return 'ui-badge-secondary';
  }
};

const getPriorityText = (priority) => {
  switch (priority) {
    case 'high': return '高';
    case 'medium': return '中';
    case 'low': return '低';
    default: return '未设置';
  }
};

// 完成任务
const completeTask = (task) => {
  task.completed = !task.completed;
  // 实际项目中应调用API
};

// 删除任务
const deleteTask = (taskId) => {
  // 实际项目中应弹窗确认并调用API
  tasks.value = tasks.value.filter(task => task.id !== taskId);
};

// 新任务
const newTask = reactive({ title: '', deadline: '', priority: 'medium' });
const showNewTaskForm = ref(false);

// 添加新任务
const addNewTask = () => {
  if (!newTask.title || !newTask.deadline) {
    // 显示错误提示
    return;
  }
  
  // 添加任务
  const task = {
    id: Date.now(),
    title: newTask.title,
    deadline: newTask.deadline,
    priority: newTask.priority,
    completed: false
  };
  
  tasks.value.unshift(task);
  
  // 重置表单
  newTask.title = '';
  newTask.deadline = '';
  newTask.priority = 'medium';
  showNewTaskForm.value = false;
};

// 未完成任务数量
const uncompletedTasksCount = computed(() => {
  return tasks.value.filter(task => !task.completed).length;
});

// 显示的图表类型
const activeChart = ref('activity');
</script>

<template>
  <div class="dashboard">
    <!-- 页面标题 -->
    <div class="dashboard-header mb-6">
      <h1 class="text-2xl font-bold text-slate-800">教师工作台</h1>
      <div class="flex items-center space-x-3">
        <Button 
          variant="secondary" 
          size="sm" 
          @click="fetchData"
          :loading="loading"
        >
          刷新数据
        </Button>
        <Button 
          variant="primary" 
          size="sm" 
          @click="showNewTaskForm = !showNewTaskForm"
        >
          {{ showNewTaskForm ? '取消' : '添加任务' }}
        </Button>
      </div>
    </div>
    
    <!-- 新任务表单 -->
    <Card 
      v-if="showNewTaskForm" 
      variant="default" 
      class="mb-6"
      title="添加新任务"
    >
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-1">任务标题</label>
          <input 
            v-model="newTask.title"
            type="text" 
            placeholder="输入任务标题" 
            class="form-input"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-1">截止日期</label>
          <input 
            v-model="newTask.deadline"
            type="date" 
            class="form-input"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-1">优先级</label>
          <select v-model="newTask.priority" class="form-select">
            <option value="high">高</option>
            <option value="medium">中</option>
            <option value="low">低</option>
          </select>
        </div>
      </div>
      
      <div class="mt-4 flex justify-end space-x-2">
        <Button variant="secondary" @click="showNewTaskForm = false">取消</Button>
        <Button variant="primary" @click="addNewTask">添加任务</Button>
      </div>
    </Card>
    
    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-6">
      <Card hover padding="small" class="h-full">
        <div class="flex items-center">
          <div class="rounded-full bg-primary-100 p-3 mr-4">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-primary-600">
              <path d="M4 19.5v-15A2.5 2.5 0 0 1 6.5 2H20v20H6.5a2.5 2.5 0 0 1 0-5H20"></path>
            </svg>
          </div>
          <div>
            <div class="text-sm text-slate-500">我的课程</div>
            <div class="text-2xl font-bold text-slate-800">{{ summary?.courseCount || 0 }}</div>
          </div>
        </div>
      </Card>
      
      <Card hover padding="small" class="h-full">
        <div class="flex items-center">
          <div class="rounded-full bg-blue-100 p-3 mr-4">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-blue-600">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
              <circle cx="9" cy="7" r="4"></circle>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
            </svg>
          </div>
          <div>
            <div class="text-sm text-slate-500">学生人数</div>
            <div class="text-2xl font-bold text-slate-800">{{ summary?.studentCount || 0 }}</div>
          </div>
        </div>
      </Card>
      
      <Card hover padding="small" class="h-full">
        <div class="flex items-center">
          <div class="rounded-full bg-yellow-100 p-3 mr-4">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-yellow-600">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
              <line x1="9" y1="9" x2="15" y2="9"></line>
              <line x1="9" y1="15" x2="15" y2="15"></line>
            </svg>
          </div>
          <div>
            <div class="text-sm text-slate-500">待批改</div>
            <div class="text-2xl font-bold text-slate-800">{{ summary?.pendingGradingCount || 0 }}</div>
          </div>
        </div>
      </Card>
      
      <Card hover padding="small" class="h-full">
        <div class="flex items-center">
          <div class="rounded-full bg-green-100 p-3 mr-4">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-green-600">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
              <line x1="16" y1="2" x2="16" y2="6"></line>
              <line x1="8" y1="2" x2="8" y2="6"></line>
              <line x1="3" y1="10" x2="21" y2="10"></line>
            </svg>
          </div>
          <div>
            <div class="text-sm text-slate-500">即将到期</div>
            <div class="text-2xl font-bold text-slate-800">{{ summary?.upcomingDeadlines || 0 }}</div>
          </div>
        </div>
      </Card>
    </div>
    
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- 左侧列 -->
      <div class="lg:col-span-2 space-y-6">
        <!-- 通知公告 -->
        <Card title="通知公告" class="h-full">
          <div v-if="announcements.length === 0" class="py-4 text-center text-slate-500">
            暂无公告
          </div>
          <div v-else class="space-y-3">
            <div v-for="announcement in announcements" :key="announcement.id" class="p-3 border-b border-slate-100 last:border-b-0 flex justify-between items-center">
              <div class="flex-1">
                <div class="flex items-center">
                  <Badge v-if="announcement.important" type="error" dot class="mr-2" />
                  <h3 class="text-slate-800 font-medium">{{ announcement.title }}</h3>
                </div>
                <div class="text-sm text-slate-500">{{ announcement.date }}</div>
              </div>
              <Button variant="ghost" size="sm">
                查看详情
              </Button>
            </div>
          </div>
        </Card>
        
        <!-- 数据图表 -->
        <Card title="数据概览" class="h-full">
          <div>
            <Tabs v-model="activeChart" type="button" class="mb-4">
              <template #nav>
                <TabNav 
                  :tab="{value: 'activity', label: '学生活动'}" 
                  :type="'button'" 
                />
                <TabNav 
                  :tab="{value: 'resource', label: '资源访问'}" 
                  :type="'button'" 
                />
              </template>
              
              <TabPane label="学生活动" value="activity">
                <div class="h-64 bg-slate-50 rounded flex items-center justify-center">
                  <p class="text-slate-500">图表区域 - 学生活动数据</p>
                </div>
              </TabPane>
              <TabPane label="资源访问" value="resource">
                <div class="h-64 bg-slate-50 rounded flex items-center justify-center">
                  <p class="text-slate-500">图表区域 - 资源访问数据</p>
                </div>
              </TabPane>
            </Tabs>
          </div>
        </Card>
      </div>
      
      <!-- 右侧列 -->
      <div class="space-y-6">
        <!-- 任务列表 -->
        <Card title="待办任务" class="h-full">
          <div class="flex justify-between items-center mb-4">
            <p class="text-sm text-slate-500">
              共 {{ tasks.length }} 项任务，{{ uncompletedTasksCount }} 项待完成
            </p>
          </div>
          
          <div v-if="tasks.length === 0" class="py-4 text-center text-slate-500">
            暂无任务
          </div>
          <div v-else class="space-y-2">
            <div 
              v-for="task in tasks" 
              :key="task.id" 
              class="p-3 border border-slate-200 rounded-md"
              :class="{'opacity-70': task.completed}"
            >
              <div class="flex justify-between">
                <div class="flex items-center">
                  <input 
                    type="checkbox" 
                    :checked="task.completed" 
                    @change="completeTask(task)" 
                    class="form-checkbox mr-3"
                  />
                  <div>
                    <span 
                      class="text-slate-800 font-medium"
                      :class="{'line-through text-slate-500': task.completed}"
                    >
                      {{ task.title }}
                    </span>
                    <div class="flex items-center text-sm mt-1">
                      <span class="text-slate-500 mr-2">截止日期: {{ task.deadline }}</span>
                      <Badge 
                        :type="task.priority === 'high' ? 'error' : task.priority === 'medium' ? 'warning' : 'success'" 
                        :content="getPriorityText(task.priority)" 
                        size="sm" 
                        outline
                      />
                    </div>
                  </div>
                </div>
                <button 
                  @click="deleteTask(task.id)" 
                  class="text-slate-400 hover:text-error-color"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="3 6 5 6 21 6"></polyline>
                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                  </svg>
                </button>
              </div>
            </div>
          </div>
          
          <div class="mt-4">
            <Button 
              v-if="!showNewTaskForm"
              variant="outline" 
              block 
              @click="showNewTaskForm = true"
            >
              添加新任务
            </Button>
          </div>
        </Card>
        
        <!-- 快速访问 -->
        <Card title="快速访问" class="h-full">
          <div class="grid grid-cols-2 gap-3">
            <Button variant="secondary" block>
              智能备课
            </Button>
            <Button variant="secondary" block>
              智能批改
            </Button>
            <Button variant="secondary" block>
              题库管理
            </Button>
            <Button variant="secondary" block>
              学情分析
            </Button>
          </div>
        </Card>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  @apply w-full max-w-full;
}

.dashboard-header {
  @apply flex justify-between items-center;
}
</style> 