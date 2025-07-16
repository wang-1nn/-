<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { get, post } from '../../../net/index.js';
import Card from '../../../components/common/Card.vue';
import Button from '../../../components/common/Button.vue';
import ContentContainer from '../../../components/common/ContentContainer.vue';
import { useAuthStore } from '@/stores/counter';
import { ElMessage } from 'element-plus';

// 导入新组件
import SmartSchedule from './components/SmartSchedule.vue';
import ClassSnapshots from './components/ClassSnapshots.vue';
import InteractionHub from './components/InteractionHub.vue';
import QuickResources from './components/QuickResources.vue';
import StudentHighlights from './components/StudentHighlights.vue';
import AIAssistant from './components/AIAssistant.vue';

// 获取用户信息
const authStore = useAuthStore();
// 获取用户ID
const uid = computed(() => authStore.user?.id || authStore.user?.userId || '1');

// 仪表盘状态
const summary = ref({
  teacherName: '加载中...',
  courseCount: 0,
  studentCount: 0,
  pendingGradingCount: 0,
  upcomingDeadlines: 0,
  activeCourses: 0,
  newStudentsThisWeek: 0,
  currentWeek: 0,
  totalWeeks: 0
});
const announcements = ref([]);
const tasks = ref([]);
const pendingGrading = ref([]);
const classSnapshots = ref([]);
const loading = ref(true);

// 获取仪表盘概览数据
const fetchSummaryData = () => {
  get('/api/teacher/dashboard/summary', { uid: uid.value },
      (message, data) => {
        if (data) {
          summary.value = {
            teacherName: data.teacherName || '老师',
            courseCount: data.courseCount || 0,
            studentCount: data.studentCount || 0,
            pendingGradingCount: data.pendingGradingCount || 0,
            upcomingDeadlines: data.upcomingDeadlines || 0,
            activeCourses: data.activeCourses || 0,
            newStudentsThisWeek: data.newStudentsThisWeek || 0,
            currentWeek: data.currentWeek || 0,
            totalWeeks: data.totalWeeks || 0
          };
        }
      },
      (message) => {
        ElMessage.warning(`获取概览数据失败: ${message}`);
      }
  );
};

// 获取通知公告
const fetchAnnouncements = () => {
  get('/api/teacher/dashboard/announcements', { uid: uid.value, limit: 10 },
      (message, data) => {
        if (data) {
          announcements.value = data.map(item => ({
            id: item.id,
            title: item.title,
            content: item.content,
            type: item.type,
            isImportant: item.isImportant,
            publishTime: item.publishTime,
            course: item.course,
            isRead: item.isRead
          }));
        }
      },
      (message) => {
        ElMessage.warning(`获取通知公告失败: ${message}`);
      }
  );
};

// 获取待办任务
const fetchTasks = () => {
  get('/api/teacher/dashboard/pending-tasks', { uid: uid.value, status: 'all' },
      (message, data) => {
        if (data) {
          tasks.value = data.map(item => ({
            id: item.id,
            title: item.title,
            description: item.description,
            taskType: item.taskType,
            priority: item.priority,
            dueDate: item.dueDate,
            status: item.status,
            completed: item.status === 'COMPLETED'
          }));
        }
      },
      (message) => {
        ElMessage.warning(`获取任务列表失败: ${message}`);
      }
  );
};

// 获取待批改作业
const fetchPendingGrading = () => {
  get('/api/teacher/dashboard/pending-grading', { uid: uid.value, limit: 10 },
      (message, data) => {
        if (data) {
          pendingGrading.value = data.map(item => ({
            id: item.submissionId,
            assignmentId: item.assignmentId,
            title: item.assignmentTitle,
            courseName: item.courseName,
            studentName: item.studentName,
            submitTime: item.submitTime,
            type: item.type,
            isLate: item.isLate
          }));
        }
      },
      (message) => {
        ElMessage.warning(`获取待批改作业失败: ${message}`);
      }
  );
};

// 获取班级快照
const fetchClassSnapshots = () => {
  get('/api/teacher/dashboard/class-snapshots', { uid: uid.value },
      (message, data) => {
        if (data) {
          classSnapshots.value = data.map(item => ({
            courseId: item.courseId,
            courseName: item.courseName,
            totalStudents: item.totalStudents,
            activeStudents: item.activeStudents,
            atRiskStudents: item.atRiskStudents,
            averageScore: item.averageScore,
            passRate: item.passRate,
            excellentRate: item.excellentRate
          }));
        }
      },
      (message) => {
        ElMessage.warning(`获取班级快照失败: ${message}`);
      }
  );
};

// 获取所有数据
const fetchData = () => {
  loading.value = true;

  // 并行获取所有数据
  Promise.all([
    fetchSummaryData(),
    fetchAnnouncements(),
    fetchTasks(),
    fetchPendingGrading(),
    fetchClassSnapshots()
  ]).finally(() => {
    loading.value = false;
  });
};

// 刷新所有组件数据
const refreshAllComponents = () => {
  // 触发子组件刷新
  window.dispatchEvent(new CustomEvent('refresh-dashboard-components'));
};

// 初始化数据
onMounted(fetchData);

// 任务管理
const getPriorityClass = (priority) => {
  switch (priority) {
    case 'high': return 'bg-red-100 text-red-800';
    case 'medium': return 'bg-yellow-100 text-yellow-800';
    case 'low': return 'bg-blue-100 text-blue-800';
    default: return 'bg-gray-100 text-gray-800';
  }
};

const getPriorityText = (priority) => {
  switch (priority) {
    case 'high': return '高';
    case 'medium': return '中';
    case 'low': return '低';
    default: return '普通';
  }
};

const toggleTaskCompletion = (task) => {
  // 更新任务状态 - 使用专门的完成/取消完成接口
  const endpoint = task.completed
      ? `/api/teacher/dashboard/tasks/${task.id}/uncomplete`
      : `/api/teacher/dashboard/tasks/${task.id}/complete`;

  post(endpoint, {},
      (message) => {
        // 更新本地任务状态
        task.completed = !task.completed;
        task.status = task.completed ? 'COMPLETED' : 'PENDING';
        ElMessage.success(`任务已${task.completed ? '完成' : '恢复'}`);
      },
      (message) => {
        ElMessage.error(`更新任务状态失败: ${message}`);
      }
  );
};

const deleteTask = (taskId) => {
  // 删除任务 - 使用专门的删除接口
  post(`/api/teacher/dashboard/tasks/${taskId}/delete`, {},
      (message) => {
        // 从列表中移除
        tasks.value = tasks.value.filter(task => task.id !== taskId);
        ElMessage.success('任务已删除');
      },
      (message) => {
        ElMessage.error(`删除任务失败: ${message}`);
      }
  );
};

// 新任务
const newTask = reactive({ title: '', deadline: '', priority: 'medium' });
const showNewTaskForm = ref(false);

const addNewTask = () => {
  if (!newTask.title || !newTask.deadline) {
    ElMessage.warning('请填写任务标题和截止日期');
    return;
  }

  // 准备请求数据
  const request = {
    teacherId: uid.value,
    title: newTask.title,
    description: '',
    taskType: 'GENERAL',
    priority: newTask.priority,
    dueDate: new Date(newTask.deadline).toISOString()
  };

  // 发送API请求
  post('/api/teacher/dashboard/tasks', request,
      (message, data) => {
        if (data) {
          // 添加到任务列表
          const task = {
            id: data.id || Date.now(),
            title: data.title,
            description: data.description,
            taskType: data.taskType,
            priority: data.priority,
            dueDate: data.dueDate,
            deadline: newTask.deadline, // 保持前端显示格式
            status: data.status,
            completed: false
          };
          tasks.value.unshift(task);

          // 重置表单
          newTask.title = '';
          newTask.deadline = '';
          newTask.priority = 'medium';
          showNewTaskForm.value = false;

          ElMessage.success('任务添加成功');
        }
      },
      (message) => {
        ElMessage.error(`添加任务失败: ${message}`);
      }
  );
};

const uncompletedTasksCount = computed(() => {
  return tasks.value.filter(task => !task.completed).length;
});

// 通知公告管理
const markAnnouncementAsRead = (announcementId) => {
  post(`/api/teacher/dashboard/announcements/${announcementId}/read`, { uid: uid.value },
      (message) => {
        // 更新本地状态
        const announcement = announcements.value.find(a => a.id === announcementId);
        if (announcement) {
          announcement.isRead = true;
        }
      },
      (message) => {
        ElMessage.error(`标记已读失败: ${message}`);
      }
  );
};

// 刷新数据
const refreshData = () => {
  fetchData();
  refreshAllComponents();
  ElMessage.success('数据已刷新');
};
</script>

<template>
  <ContentContainer>
    <div class="dashboard p-4 md:p-6">
      <!-- 页面标题 -->
      <div class="dashboard-header mb-6 flex justify-between items-center">
        <h1 class="text-3xl font-bold text-slate-800">教师工作台</h1>
        <div class="flex items-center space-x-3">
          <button
              @click="refreshData"
              :disabled="loading"
              class="inline-flex items-center justify-center px-4 py-2 text-sm font-semibold text-slate-700 bg-white border border-slate-200 rounded-full shadow-sm hover:bg-slate-50 hover:border-slate-300 active:bg-slate-100 disabled:opacity-60 disabled:cursor-not-allowed transition-colors duration-200"
          >
            <svg
                xmlns="http://www.w3.org/2000/svg"
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2.5"
                stroke-linecap="round"
                stroke-linejoin="round"
                class="mr-2"
                :class="{ 'animate-spin': loading }"
            >
              <path d="M23 4v6h-6"></path>
              <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"></path>
            </svg>
            {{ loading ? '正在刷新...' : '刷新' }}
          </button>
        </div>
      </div>

      <!-- 主内容区 -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">

        <!-- 左侧栏 -->
        <div class="lg:col-span-2 space-y-6">
          <!-- 统计卡片 -->
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <Card hover padding="small" class="h-full">
              <div class="flex items-center">
                <div class="rounded-full bg-primary-100 p-3 mr-4">
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-primary-600"><path d="M4 19.5v-15A2.5 2.5 0 0 1 6.5 2H20v20H6.5a2.5 2.5 0 0 1 0-5H20"></path></svg>
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
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-blue-600"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
                </div>
                <div>
                  <div class="text-sm text-slate-500">学生总数</div>
                  <div class="text-2xl font-bold text-slate-800">{{ summary?.studentCount || 0 }}</div>
                </div>
              </div>
            </Card>
            <Card hover padding="small" class="h-full">
              <div class="flex items-center">
                <div class="rounded-full bg-yellow-100 p-3 mr-4">
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-yellow-600"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><line x1="9" y1="9" x2="15" y2="9"></line><line x1="9" y1="15" x2="15" y2="15"></line></svg>
                </div>
                <div>
                  <div class="text-sm text-slate-500">待批改</div>
                  <div class="text-2xl font-bold text-slate-800">{{ summary?.pendingGradingCount || 0 }}</div>
                </div>
              </div>
            </Card>
            <Card hover padding="small" class="h-full">
              <div class="flex items-center">
                <div class="rounded-full bg-red-100 p-3 mr-4">
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-red-600"><path d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" /></svg>
                </div>
                <div>
                  <div class="text-sm text-slate-500">临近截止</div>
                  <div class="text-2xl font-bold text-slate-800">{{ summary?.upcomingDeadlines || 0 }}</div>
                </div>
              </div>
            </Card>
          </div>

          <!-- 智能日程 -->
          <SmartSchedule />

          <!-- 互动中心 -->
          <InteractionHub />

          <!-- 待办任务 -->
          <Card>
            <template #header>
              <div class="flex justify-between items-center">
                <h3 class="text-lg font-semibold">待办任务 ({{ uncompletedTasksCount }})</h3>
                <Button
                    variant="primary"
                    size="sm"
                    @click="showNewTaskForm = !showNewTaskForm"
                >
                  {{ showNewTaskForm ? '收起' : '添加任务' }}
                </Button>
              </div>
            </template>
            <!-- 新任务表单 -->
            <div v-if="showNewTaskForm" class="p-4 bg-gray-50 rounded-md mb-4">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-slate-700 mb-1">任务标题</label>
                  <input v-model="newTask.title" type="text" placeholder="输入任务标题" class="form-input w-full"/>
                </div>
                <div>
                  <label class="block text-sm font-medium text-slate-700 mb-1">截止日期</label>
                  <input v-model="newTask.deadline" type="date" class="form-input w-full"/>
                </div>
              </div>
              <div class="mt-4">
                <label class="block text-sm font-medium text-slate-700 mb-1">优先级</label>
                <select v-model="newTask.priority" class="form-select w-full">
                  <option value="high">高</option>
                  <option value="medium">中</option>
                  <option value="low">低</option>
                </select>
              </div>
              <div class="mt-4 flex justify-end space-x-2">
                <Button variant="secondary" @click="showNewTaskForm = false">取消</Button>
                <Button variant="primary" @click="addNewTask">确认添加</Button>
              </div>
            </div>

            <!-- 任务列表 -->
            <div class="space-y-4">
              <div v-if="tasks.length === 0" class="text-center py-6 text-slate-400">
                <svg xmlns="http://www.w3.org/2000/svg" width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" class="mx-auto mb-3"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                <p>暂无待办任务</p>
              </div>
              <div v-for="task in tasks" :key="task.id" class="flex items-start gap-3 p-3 bg-white rounded-md border border-slate-200 hover:shadow-sm transition-shadow">
                <input
                    type="checkbox"
                    :checked="task.completed"
                    @change="toggleTaskCompletion(task)"
                    class="mt-1 form-checkbox h-4 w-4 text-primary-600 focus:ring-primary-500"
                />
                <div class="flex-grow min-w-0">
                  <div class="flex flex-wrap gap-2 items-center mb-1">
                    <h4 class="font-medium text-slate-900" :class="{ 'line-through text-slate-500': task.completed }">{{ task.title }}</h4>
                    <span
                        :class="['text-xs font-semibold rounded-full px-2 py-1', getPriorityClass(task.priority)]"
                    >
                      {{ getPriorityText(task.priority) }}优先级
                    </span>
                  </div>
                  <p class="text-sm text-slate-500">截止日期: {{ task.deadline }}</p>
                </div>
                <button
                    @click="deleteTask(task.id)"
                    class="text-slate-400 hover:text-red-500 p-1"
                    title="删除任务"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18"></path><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path></svg>
                </button>
              </div>
            </div>
          </Card>
        </div>

        <!-- 右侧栏 -->
        <div class="space-y-6">
          <!-- 班级情况卡片 -->
          <ClassSnapshots />

          <!-- 快速访问资源 -->
          <QuickResources />

          <!-- 学生亮点 -->
          <StudentHighlights />

          <!-- 待批改作业列表 -->
          <Card>
            <template #header>
              <div class="flex justify-between items-center">
                <h3 class="text-lg font-semibold">待批改作业</h3>
                <Button variant="text" size="sm">查看全部</Button>
              </div>
            </template>
            <div class="space-y-4">
              <div v-if="pendingGrading.length === 0" class="text-center py-6 text-slate-400">
                <svg xmlns="http://www.w3.org/2000/svg" width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" class="mx-auto mb-3"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                <p>暂无待批改作业</p>
              </div>
              <div v-for="grading in pendingGrading" :key="grading.id"
                   class="flex items-start gap-3 p-3 rounded-md border border-slate-200 hover:shadow-sm transition-shadow">
                <div class="flex-shrink-0">
                  <div class="w-8 h-8 bg-yellow-100 rounded-full flex items-center justify-center">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-yellow-600">
                      <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                      <line x1="9" y1="9" x2="15" y2="9"></line>
                      <line x1="9" y1="15" x2="15" y2="15"></line>
                    </svg>
                  </div>
                </div>
                <div class="flex-grow">
                  <div class="flex justify-between items-start">
                    <div>
                      <h4 class="font-medium text-slate-900 mb-1">{{ grading.title }}</h4>
                      <p class="text-sm text-slate-600">{{ grading.courseName }}</p>
                      <p class="text-sm text-slate-500">学生：{{ grading.studentName }}</p>
                    </div>
                    <div class="text-right">
                      <div class="text-xs text-slate-500">
                        {{ grading.submitTime ? new Date(grading.submitTime).toLocaleDateString() : '' }}
                      </div>
                      <div v-if="grading.isLate" class="text-xs text-red-500 mt-1">迟交</div>
                    </div>
                  </div>
                  <div class="mt-2 flex gap-2">
                    <Button size="sm" variant="primary">批改</Button>
                    <Button size="sm" variant="secondary">查看详情</Button>
                  </div>
                </div>
              </div>
            </div>
          </Card>

          <!-- 通知公告 -->
          <Card>
            <template #header>
              <div class="flex justify-between items-center">
                <h3 class="text-lg font-semibold">通知公告</h3>
                <div class="flex items-center gap-2">
                  <Button variant="text" size="sm" @click="refreshData" title="刷新数据">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.5 2v6h-6M2.5 22v-6h6M2 11.5a10 10 0 0 1 18.8-4.3M22 12.5a10 10 0 0 1-18.8 4.2"/></svg>
                  </Button>
                  <Button variant="text" size="sm">查看全部</Button>
                </div>
              </div>
            </template>
            <div class="space-y-4">
              <div v-if="announcements.length === 0" class="text-center py-6 text-slate-400">
                <svg xmlns="http://www.w3.org/2000/svg" width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" class="mx-auto mb-3"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                <p>暂无通知公告</p>
              </div>
              <div v-for="announcement in announcements" :key="announcement.id"
                   class="flex items-start gap-3 p-3 rounded-md transition-all hover:bg-slate-50"
                   :class="{'opacity-70': announcement.isRead}">
                <div class="flex-shrink-0">
                  <div class="w-2 h-2 mt-2 rounded-full"
                       :class="announcement.isImportant ? 'bg-red-500' : 'bg-green-500'"></div>
                </div>
                <div class="flex-grow">
                  <div class="flex justify-between items-start">
                    <h4 class="font-medium text-slate-900 mb-1"
                        :class="{'text-slate-600': announcement.isRead}">
                      {{ announcement.title }}
                    </h4>
                    <button v-if="!announcement.isRead"
                            @click="markAnnouncementAsRead(announcement.id)"
                            class="text-xs text-blue-500 hover:text-blue-700 ml-2">
                      标为已读
                    </button>
                  </div>
                  <div class="flex justify-between text-xs text-slate-500">
                    <span>{{ announcement.course }}</span>
                    <span>{{ announcement.publishTime ? new Date(announcement.publishTime).toLocaleDateString() : '' }}</span>
                  </div>
                </div>
              </div>
            </div>
          </Card>
        </div>
      </div>
    </div>
  </ContentContainer>
</template>

<style scoped>
.dashboard {
  max-width: 1600px;
  margin: 0 auto;
}

.dashboard-header {
  margin-bottom: 1.5rem;
}

/* 表单样式 */
.form-input,
.form-select {
  @apply rounded-md border border-slate-300 px-3 py-2 shadow-sm focus:border-primary-500 focus:ring focus:ring-primary-500 focus:ring-opacity-30;
}

.form-checkbox {
  @apply rounded border-slate-300 text-primary-600 shadow-sm focus:border-primary-500 focus:ring focus:ring-primary-500 focus:ring-opacity-30;
}
</style>