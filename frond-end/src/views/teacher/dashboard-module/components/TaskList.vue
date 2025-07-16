<script setup>
import { computed } from 'vue';

const props = defineProps({
  tasks: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['complete-task', 'delete-task']);

// 未完成任务的数量
const uncompletedTasksCount = computed(() => {
  return props.tasks.filter(task => !task.completed).length;
});

// 获取优先级的样式类
const getPriorityClass = (priority) => {
  switch (priority) {
    case 'high': return 'bg-red-100 text-red-800';
    case 'medium': return 'bg-yellow-100 text-yellow-800';
    case 'low': return 'bg-green-100 text-green-800';
    default: return 'bg-gray-100 text-gray-800';
  }
};

// 处理任务完成状态变化
const handleTaskComplete = (task) => {
  emit('complete-task', task);
};

// 处理删除任务
const handleDeleteTask = (taskId) => {
  emit('delete-task', taskId);
};
</script>

<template>
  <div class="task-list-card h-full">
    <div class="card-header">
      <div class="flex items-center">
        <h3>近期任务</h3>
        <span class="task-counter">{{ uncompletedTasksCount }}</span>
      </div>
      <button class="add-task-btn" @click="$emit('show-add-task-dialog')">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
        <span>添加任务</span>
      </button>
    </div>
    
    <div class="task-list">
      <div 
        v-for="task in tasks" 
        :key="task.id"
        :class="['task-item', {'task-completed': task.completed}]"
      >
        <div class="task-left">
          <label class="custom-checkbox">
            <input 
              type="checkbox" 
              :checked="task.completed" 
              @change="handleTaskComplete(task)" 
              class="sr-only"
            />
            <span class="checkbox-indicator"></span>
          </label>
          <div class="task-info">
            <div class="task-title">{{ task.title }}</div>
            <div class="task-deadline">截止日期: {{ task.deadline }}</div>
          </div>
        </div>
        
        <div class="task-right">
          <span :class="['priority-badge', getPriorityClass(task.priority)]">
            {{ task.priority === 'high' ? '紧急' : task.priority === 'medium' ? '普通' : '低' }}
          </span>
          <div class="task-menu">
            <button class="task-delete-btn" @click="handleDeleteTask(task.id)">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          </div>
        </div>
      </div>
      
      <div v-if="tasks.length === 0" class="empty-tasks">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
        </svg>
        <p class="text-gray-500 mt-2">暂无任务</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.task-list-card {
  @apply bg-white rounded-xl shadow-sm p-5 border border-gray-100 hover:shadow-md transition-all duration-300;
}

.card-header {
  @apply flex justify-between items-center mb-5;
}

.card-header h3 {
  @apply text-lg font-semibold text-gray-800;
}

.task-counter {
  @apply ml-2 text-sm bg-blue-100 text-blue-700 px-2.5 py-0.5 rounded-full font-medium;
}

.add-task-btn {
  @apply flex items-center text-sm bg-blue-50 text-blue-700 px-3 py-1.5 rounded-lg hover:bg-blue-100 transition-colors duration-200 font-medium;
}

.add-task-btn svg {
  @apply mr-1;
}

.task-list {
  @apply space-y-3 max-h-[450px] overflow-auto pr-2 custom-scrollbar;
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

.custom-checkbox {
  @apply relative flex items-center justify-center w-5 h-5 mr-3;
}

.checkbox-indicator {
  @apply block w-5 h-5 rounded border border-gray-300 bg-white transition-all duration-200;
}

input:checked + .checkbox-indicator {
  @apply bg-blue-600 border-blue-600;
}

input:checked + .checkbox-indicator::after {
  content: '';
  @apply absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 block w-2 h-3 border-r-2 border-b-2 border-white rotate-45;
}

.task-info {
  @apply ml-1;
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

.task-menu {
  @apply flex items-center;
}

.task-delete-btn {
  @apply p-1 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded-full transition-colors duration-200;
}

.empty-tasks {
  @apply flex flex-col items-center justify-center py-10 text-center;
}

/* 自定义滚动条 */
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  @apply bg-transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  @apply bg-gray-300 rounded-full hover:bg-gray-400;
}
</style>