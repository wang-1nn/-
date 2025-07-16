<script setup>
import { reactive, ref } from 'vue';

const emit = defineEmits(['add-task', 'close']);

defineProps({
  visible: {
    type: Boolean,
    default: false
  }
});

// 新任务表单数据
const newTask = reactive({
  title: '',
  deadline: '',
  priority: 'medium'
});

// 表单验证错误
const errors = reactive({
  title: '',
  deadline: ''
});

// 重置表单
const resetForm = () => {
  newTask.title = '';
  newTask.deadline = '';
  newTask.priority = 'medium';
  
  errors.title = '';
  errors.deadline = '';
};

// 验证表单
const validateForm = () => {
  let isValid = true;
  
  if (!newTask.title.trim()) {
    errors.title = '请输入任务名称';
    isValid = false;
  } else {
    errors.title = '';
  }
  
  if (!newTask.deadline) {
    errors.deadline = '请选择截止日期';
    isValid = false;
  } else {
    errors.deadline = '';
  }
  
  return isValid;
};

// 添加新任务
const addNewTask = () => {
  if (validateForm()) {
    emit('add-task', { ...newTask });
    resetForm();
  }
};

// 关闭对话框
const closeDialog = () => {
  resetForm();
  emit('close');
};
</script>

<template>
  <div v-if="visible" class="dialog-overlay" @click="closeDialog">
    <div class="dialog-container" @click.stop>
      <div class="dialog-header">
        <h3>添加新任务</h3>
        <button class="close-btn" @click="closeDialog">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
      
      <div class="dialog-content">
        <div class="form-group">
          <label for="task-title">任务名称</label>
          <input 
            id="task-title"
            type="text" 
            v-model="newTask.title" 
            placeholder="请输入任务名称"
            :class="{'input-error': errors.title}"
          />
          <p v-if="errors.title" class="error-text">{{ errors.title }}</p>
        </div>
        
        <div class="form-group">
          <label for="task-deadline">截止日期</label>
          <input 
            id="task-deadline"
            type="date" 
            v-model="newTask.deadline"
            :class="{'input-error': errors.deadline}"
          />
          <p v-if="errors.deadline" class="error-text">{{ errors.deadline }}</p>
        </div>
        
        <div class="form-group">
          <label>优先级</label>
          <div class="priority-selector">
            <button 
              :class="['priority-btn', newTask.priority === 'high' ? 'high-selected' : '']"
              @click="newTask.priority = 'high'"
            >
              紧急
            </button>
            <button 
              :class="['priority-btn', newTask.priority === 'medium' ? 'medium-selected' : '']"
              @click="newTask.priority = 'medium'"
            >
              普通
            </button>
            <button 
              :class="['priority-btn', newTask.priority === 'low' ? 'low-selected' : '']"
              @click="newTask.priority = 'low'"
            >
              低
            </button>
          </div>
        </div>
      </div>
      
      <div class="dialog-footer">
        <button class="cancel-btn" @click="closeDialog">取消</button>
        <button class="submit-btn" @click="addNewTask">确定</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dialog-overlay {
  @apply fixed inset-0 bg-black bg-opacity-30 flex items-center justify-center z-50;
  backdrop-filter: blur(2px);
}

.dialog-container {
  @apply bg-white rounded-xl shadow-lg w-full max-w-md mx-4 overflow-hidden;
  animation: dialog-appear 0.3s ease;
}

@keyframes dialog-appear {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.dialog-header {
  @apply flex justify-between items-center p-5 border-b border-gray-200;
}

.dialog-header h3 {
  @apply text-lg font-semibold text-gray-800;
}

.close-btn {
  @apply p-1 rounded-full hover:bg-gray-100 text-gray-500 hover:text-gray-700 transition-colors duration-200;
}

.dialog-content {
  @apply p-5;
}

.form-group {
  @apply mb-4;
}

.form-group label {
  @apply block text-sm font-medium text-gray-700 mb-1;
}

.form-group input {
  @apply w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all duration-200;
}

.input-error {
  @apply border-red-500 focus:ring-red-500;
}

.error-text {
  @apply text-red-500 text-xs mt-1;
}

.priority-selector {
  @apply flex space-x-2;
}

.priority-btn {
  @apply flex-1 py-2 border border-gray-300 rounded-md text-sm font-medium transition-all duration-200;
}

.high-selected {
  @apply bg-red-100 text-red-800 border-red-300;
}

.medium-selected {
  @apply bg-yellow-100 text-yellow-800 border-yellow-300;
}

.low-selected {
  @apply bg-green-100 text-green-800 border-green-300;
}

.dialog-footer {
  @apply flex justify-end space-x-2 p-5 bg-gray-50;
}

.cancel-btn {
  @apply px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 transition-colors duration-200;
}

.submit-btn {
  @apply px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700 transition-colors duration-200;
}
</style>