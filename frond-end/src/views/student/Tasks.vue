<template>
  <div class="tasks-wrapper">
    <!-- 顶部统计卡片 -->
    <div class="stats-section grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
      <div class="stat-card bg-white rounded-xl shadow-md p-4 flex items-center">
        <div class="stat-icon bg-blue-100 p-3 rounded-lg">
          <i class="el-icon-date text-blue-600 text-xl"></i>
        </div>
        <div class="ml-4">
          <div class="text-sm text-gray-500">今日计划</div>
          <div class="text-xl font-semibold">{{ todayTasks.length }}</div>
        </div>
      </div>
      
      <div class="stat-card bg-white rounded-xl shadow-md p-4 flex items-center">
        <div class="stat-icon bg-green-100 p-3 rounded-lg">
          <i class="el-icon-check text-green-600 text-xl"></i>
        </div>
        <div class="ml-4">
          <div class="text-sm text-gray-500">已完成</div>
          <div class="text-xl font-semibold">{{ completedCount }}</div>
        </div>
      </div>
      
      <div class="stat-card bg-white rounded-xl shadow-md p-4 flex items-center">
        <div class="stat-icon bg-amber-100 p-3 rounded-lg">
          <i class="el-icon-timer text-amber-600 text-xl"></i>
        </div>
        <div class="ml-4">
          <div class="text-sm text-gray-500">待完成</div>
          <div class="text-xl font-semibold">{{ pendingCount }}</div>
        </div>
      </div>
      
      <div class="stat-card bg-white rounded-xl shadow-md p-4 flex items-center">
        <div class="stat-icon bg-purple-100 p-3 rounded-lg">
          <i class="el-icon-medal text-purple-600 text-xl"></i>
        </div>
        <div class="ml-4">
          <div class="text-sm text-gray-500">完成率</div>
          <div class="text-xl font-semibold">{{ completionRate }}%</div>
        </div>
      </div>
    </div>
    
    <!-- 主要内容区 -->
    <div class="main-content grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- 左侧：任务列表 -->
      <div class="lg:col-span-2">
        <div class="bg-white rounded-xl shadow-md p-5">
          <div class="flex justify-between items-center mb-6">
            <h3 class="text-lg font-medium flex items-center">
              <i class="el-icon-notebook-1 text-blue-600 mr-2"></i>
              学习计划
            </h3>
            <el-button type="primary" size="small" @click="createNewTask">
              <i class="el-icon-plus mr-1"></i>
              新建计划
            </el-button>
          </div>
          
          <!-- 过滤器 -->
          <div class="filters flex flex-wrap gap-3 mb-5">
            <el-radio-group v-model="filter" size="small">
              <el-radio-button label="all">全部</el-radio-button>
              <el-radio-button label="today">今日</el-radio-button>
              <el-radio-button label="week">本周</el-radio-button>
              <el-radio-button label="completed">已完成</el-radio-button>
            </el-radio-group>
            
            <el-select v-model="typeFilter" size="small" placeholder="类型" class="w-24 ml-auto">
              <el-option label="全部" value="all" />
              <el-option label="练习" value="practice" />
              <el-option label="复习" value="review" />
              <el-option label="考试" value="exam" />
            </el-select>
          </div>
          
          <!-- 任务列表 -->
          <transition-group name="list" tag="div" class="task-list space-y-3">
            <div v-for="task in filteredTasks" :key="task.id" 
                 class="task-item p-4 border border-gray-100 rounded-lg hover:bg-gray-50 transition-colors"
                 :class="{'border-l-4 border-l-blue-500': isToday(task.date)}">
              <div class="flex items-center justify-between">
                <div class="flex items-center">
                  <el-checkbox 
                    v-model="task.completed" 
                    @change="toggleTaskStatus(task)"
                    :disabled="task.completed"
                  ></el-checkbox>
                  <div :class="['ml-3', task.completed ? 'line-through text-gray-400' : '']">
                    <div class="font-medium">{{ task.title }}</div>
                    <div class="text-xs text-gray-500 mt-1">{{ formatDate(task.date) }} · {{ task.duration }}分钟</div>
                  </div>
                </div>
                
                <div class="flex items-center">
                  <el-tag size="small" :type="getTaskTypeColor(task.type)" class="mr-2">
                    {{ getTaskTypeName(task.type) }}
                  </el-tag>
                  <el-dropdown trigger="click">
                    <i class="el-icon-more cursor-pointer p-2 hover:bg-gray-100 rounded-full"></i>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item @click="editTask(task)">
                          <i class="el-icon-edit mr-1"></i> 编辑
                        </el-dropdown-item>
                        <el-dropdown-item @click="deleteTask(task)">
                          <i class="el-icon-delete mr-1"></i> 删除
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </div>
              
              <div v-if="task.description" class="mt-2 text-sm text-gray-600 pl-8">
                {{ task.description }}
              </div>
              
              <div v-if="task.completed" class="mt-2 text-xs text-green-600 pl-8">
                完成于 {{ formatTime(task.completedAt) }}
              </div>
            </div>
          </transition-group>
          
          <!-- 空状态 -->
          <div v-if="filteredTasks.length === 0" class="empty-state text-center py-10">
            <div class="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <i class="el-icon-document text-gray-400 text-2xl"></i>
            </div>
            <h4 class="text-gray-500">暂无学习计划</h4>
            <p class="text-sm text-gray-400 mt-2 mb-4">创建一个新的学习计划来提高学习效率</p>
            <el-button type="primary" size="small" @click="createNewTask">创建计划</el-button>
          </div>
        </div>
      </div>
      
      <!-- 右侧：日历 -->
      <div class="calendar-section">
        <div class="bg-white rounded-xl shadow-md p-5">
          <h3 class="text-lg font-medium mb-4 flex items-center">
            <i class="el-icon-calendar text-purple-600 mr-2"></i>
            学习日历
          </h3>
          
          <el-calendar class="custom-calendar">
            <template #dateCell="{ data }">
              <div class="calendar-day">
                <div class="day-number">{{ data.day.split('-').slice(2).join('') }}</div>
                <div v-if="getTasksForDate(data.day).length > 0" class="task-indicator">
                  <div class="task-dot" :class="{'completed': allTasksCompletedForDate(data.day)}"></div>
                  <span class="task-count">{{ getTasksForDate(data.day).length }}</span>
                </div>
              </div>
            </template>
          </el-calendar>
        </div>
        
        <!-- 即将到来的任务 -->
        <div class="bg-white rounded-xl shadow-md p-5 mt-6">
          <h3 class="text-lg font-medium mb-4 flex items-center">
            <i class="el-icon-alarm-clock text-amber-600 mr-2"></i>
            即将到来
          </h3>
          
          <div class="space-y-3">
            <div v-for="task in upcomingTasks" :key="`upcoming-${task.id}`" 
                 class="upcoming-task p-3 border-l-2 border-amber-400 bg-amber-50 rounded-r-lg">
              <div class="font-medium">{{ task.title }}</div>
              <div class="text-xs text-gray-500 mt-1">{{ formatDate(task.date) }} · {{ task.duration }}分钟</div>
            </div>
          </div>
          
          <div v-if="upcomingTasks.length === 0" class="text-center py-4 text-gray-400 text-sm">
            未来7天内没有计划
          </div>
        </div>
      </div>
    </div>
    
    <!-- 任务表单对话框 -->
    <el-dialog
      :title="editingTask ? '编辑学习计划' : '创建学习计划'"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form :model="taskForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="taskForm.title" placeholder="输入计划标题"></el-input>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="taskForm.type" class="w-full">
            <el-option label="练习" value="practice"></el-option>
            <el-option label="复习" value="review"></el-option>
            <el-option label="考试" value="exam"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="taskForm.date" type="date" placeholder="选择日期" class="w-full"></el-date-picker>
        </el-form-item>
        <el-form-item label="时长(分钟)">
          <el-input-number v-model="taskForm.duration" :min="5" :max="180" :step="5"></el-input-number>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="taskForm.description" type="textarea" :rows="3" placeholder="添加计划描述（可选）"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveTask">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 状态
const filter = ref('all')
const typeFilter = ref('all')
const dialogVisible = ref(false)
const editingTask = ref(null)
const taskForm = ref({
  title: '',
  type: 'practice',
  date: new Date(),
  duration: 30,
  description: ''
})

// 模拟任务数据
const tasks = ref([
  {
    id: 1,
    title: '高数函数极限练习',
    type: 'practice',
    date: '2023-10-30',
    duration: 45,
    description: '完成课本P45-47习题',
    completed: true,
    completedAt: '2023-10-30T15:30:00'
  },
  {
    id: 2,
    title: '线性代数复习',
    type: 'review',
    date: '2023-10-31',
    duration: 60,
    description: '矩阵运算和行列式计算复习',
    completed: false,
    completedAt: null
  },
  {
    id: 3,
    title: '概率论期中考试',
    type: 'exam',
    date: '2023-11-05',
    duration: 120,
    description: '覆盖第1-5章内容',
    completed: false,
    completedAt: null
  },
  {
    id: 4,
    title: '数据结构错题复习',
    type: 'review',
    date: new Date().toISOString().split('T')[0],
    duration: 30,
    description: '链表和二叉树相关错题',
    completed: false,
    completedAt: null
  },
  {
    id: 5,
    title: 'Python编程练习',
    type: 'practice',
    date: new Date().toISOString().split('T')[0],
    duration: 45,
    description: '完成函数和类的练习题',
    completed: true,
    completedAt: new Date().toISOString()
  }
])

// 计算属性
const filteredTasks = computed(() => {
  return tasks.value
    .filter(task => {
      // 状态过滤
      if (filter.value === 'today') {
        return isToday(task.date);
      } else if (filter.value === 'week') {
        return isWithinWeek(task.date);
      } else if (filter.value === 'completed') {
        return task.completed;
      }
      return true;
    })
    .filter(task => {
      // 类型过滤
      if (typeFilter.value === 'all') return true;
      return task.type === typeFilter.value;
    })
    .sort((a, b) => {
      // 排序：日期升序，未完成优先
      if (a.date !== b.date) {
        return new Date(a.date) - new Date(b.date);
      }
      if (a.completed !== b.completed) {
        return a.completed ? 1 : -1;
      }
      return 0;
    });
})

const todayTasks = computed(() => {
  const today = new Date().toISOString().split('T')[0];
  return tasks.value.filter(task => task.date === today);
})

const completedCount = computed(() => {
  return tasks.value.filter(task => task.completed).length;
})

const pendingCount = computed(() => {
  return tasks.value.filter(task => !task.completed).length;
})

const completionRate = computed(() => {
  if (tasks.value.length === 0) return 0;
  return Math.round((completedCount.value / tasks.value.length) * 100);
})

const upcomingTasks = computed(() => {
  const today = new Date();
  const nextWeek = new Date();
  nextWeek.setDate(today.getDate() + 7);
  
  return tasks.value
    .filter(task => {
      const taskDate = new Date(task.date);
      return !task.completed && 
             taskDate >= today && 
             taskDate <= nextWeek;
    })
    .sort((a, b) => new Date(a.date) - new Date(b.date))
    .slice(0, 3);
})

// 辅助函数
const isToday = (dateStr) => {
  const today = new Date().toISOString().split('T')[0];
  return dateStr === today;
}

const isWithinWeek = (dateStr) => {
  const today = new Date();
  const taskDate = new Date(dateStr);
  const diffTime = taskDate - today;
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  return diffDays >= 0 && diffDays < 7;
}

const formatDate = (dateStr) => {
  const date = new Date(dateStr);
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`;
}

const formatTime = (timeStr) => {
  if (!timeStr) return '';
  const date = new Date(timeStr);
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
}

const getTaskTypeColor = (type) => {
  switch (type) {
    case 'practice': return 'primary';
    case 'review': return 'warning';
    case 'exam': return 'danger';
    default: return 'info';
  }
}

const getTaskTypeName = (type) => {
  switch (type) {
    case 'practice': return '练习';
    case 'review': return '复习';
    case 'exam': return '考试';
    default: return '其他';
  }
}

const getTasksForDate = (dateStr) => {
  // 日历组件传入的日期格式可能不同，需要统一格式
  const formattedDate = new Date(dateStr).toISOString().split('T')[0];
  return tasks.value.filter(task => task.date === formattedDate);
}

const allTasksCompletedForDate = (dateStr) => {
  const dateTasks = getTasksForDate(dateStr);
  return dateTasks.length > 0 && dateTasks.every(task => task.completed);
}

// 操作函数
const toggleTaskStatus = (task) => {
  task.completedAt = task.completed ? new Date().toISOString() : null;
  ElMessage({
    message: task.completed ? '任务已完成！' : '任务已恢复为未完成状态',
    type: task.completed ? 'success' : 'info',
    duration: 1500
  });
}

const createNewTask = () => {
  editingTask.value = null;
  taskForm.value = {
    title: '',
    type: 'practice',
    date: new Date(),
    duration: 30,
    description: ''
  };
  dialogVisible.value = true;
}

const editTask = (task) => {
  editingTask.value = task;
  taskForm.value = {
    title: task.title,
    type: task.type,
    date: new Date(task.date),
    duration: task.duration,
    description: task.description || ''
  };
  dialogVisible.value = true;
}

const saveTask = () => {
  if (!taskForm.value.title) {
    ElMessage.warning('请输入计划标题');
    return;
  }
  
  const formattedDate = taskForm.value.date.toISOString().split('T')[0];
  
  if (editingTask.value) {
    // 更新任务
    Object.assign(editingTask.value, {
      title: taskForm.value.title,
      type: taskForm.value.type,
      date: formattedDate,
      duration: taskForm.value.duration,
      description: taskForm.value.description
    });
    
    ElMessage.success('计划已更新');
  } else {
    // 创建新任务
    const newTask = {
      id: tasks.value.length > 0 ? Math.max(...tasks.value.map(t => t.id)) + 1 : 1,
      title: taskForm.value.title,
      type: taskForm.value.type,
      date: formattedDate,
      duration: taskForm.value.duration,
      description: taskForm.value.description,
      completed: false,
      completedAt: null
    };
    
    tasks.value.push(newTask);
    ElMessage.success('计划已创建');
  }
  
  dialogVisible.value = false;
}

const deleteTask = (task) => {
  ElMessageBox.confirm('确定要删除这个学习计划吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const index = tasks.value.findIndex(t => t.id === task.id);
    if (index !== -1) {
      tasks.value.splice(index, 1);
      ElMessage.success('计划已删除');
    }
  }).catch(() => {});
}

onMounted(() => {
  // 初始化逻辑
})
</script>

<style scoped>
.tasks-wrapper {
  @apply p-0;
}

/* 统计卡片样式 */
.stat-card {
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.08);
}

/* 任务列表动画 */
.list-enter-active,
.list-leave-active {
  transition: all 0.5s ease;
}

.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

/* 日历样式 */
.custom-calendar :deep(.el-calendar-table .el-calendar-day) {
  height: 60px;
  padding: 4px;
}

.calendar-day {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.day-number {
  font-size: 14px;
  font-weight: 500;
}

.task-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
}

.task-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #6366f1;
}

.task-dot.completed {
  background-color: #10b981;
}

.task-count {
  font-size: 10px;
  color: #6b7280;
}

/* 任务项样式 */
.task-item {
  transition: all 0.2s ease;
}

.upcoming-task {
  transition: all 0.2s ease;
}

.upcoming-task:hover {
  transform: translateX(5px);
}
</style> 