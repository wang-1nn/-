<script setup>
import { ref } from 'vue';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  groups: {
    type: Array,
    default: () => []
  },
  studentCount: {
    type: Function,
    default: () => 0
  }
});

const emit = defineEmits(['close', 'add-group', 'delete-group']);

const newGroupName = ref('');

// 添加新分组
const addGroup = () => {
  if (!newGroupName.value.trim()) return;
  
  // 检查重名
  if (props.groups.includes(newGroupName.value.trim())) {
    // 可以添加错误提示
    return;
  }
  
  emit('add-group', newGroupName.value.trim());
  newGroupName.value = '';
};

// 删除分组
const deleteGroup = (group) => {
  emit('delete-group', group);
};

// 关闭对话框
const closeDialog = () => {
  newGroupName.value = '';
  emit('close');
};
</script>

<template>
  <div v-if="visible" class="dialog-overlay" @click="closeDialog">
    <div class="dialog-container" @click.stop>
      <div class="dialog-header">
        <h3>分组管理</h3>
        <button class="close-btn" @click="closeDialog">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
      
      <div class="dialog-content">
        <div class="add-group-form">
          <input 
            type="text" 
            v-model="newGroupName" 
            placeholder="请输入新分组名称" 
            class="add-group-input"
            @keyup.enter="addGroup"
          />
          <button class="add-group-btn" @click="addGroup">
            添加分组
          </button>
        </div>
        
        <div class="groups-list">
          <div 
            v-for="(group, index) in groups" 
            :key="group"
            class="group-item"
          >
            <div class="group-info">
              <span class="group-name">{{ group }}</span>
              <span class="group-count">{{ studentCount(group) }}人</span>
            </div>
            <button 
              class="group-delete-btn" 
              @click="deleteGroup(group)"
              :disabled="index === 0 || groups.length <= 1"
              :title="index === 0 ? '第一个分组不可删除' : groups.length <= 1 ? '至少保留一个分组' : ''"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          </div>
        </div>
        
        <div class="group-note">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <p>注意：删除分组后，该分组的学生将被移动到第一组。第一组不可删除。</p>
        </div>
      </div>
      
      <div class="dialog-footer">
        <button class="submit-btn" @click="closeDialog">完成</button>
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

.add-group-form {
  @apply flex gap-2 mb-4;
}

.add-group-input {
  @apply flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all duration-200;
}

.add-group-btn {
  @apply px-3 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors duration-200 whitespace-nowrap;
}

.groups-list {
  @apply border border-gray-200 rounded-lg overflow-hidden;
}

.group-item {
  @apply flex items-center justify-between px-4 py-3 hover:bg-gray-50 transition-colors duration-150;
}

.group-item:not(:last-child) {
  @apply border-b border-gray-200;
}

.group-info {
  @apply flex items-center;
}

.group-name {
  @apply font-medium text-gray-700;
}

.group-count {
  @apply ml-2 text-sm bg-blue-100 text-blue-700 px-2 py-0.5 rounded-full;
}

.group-delete-btn {
  @apply p-1.5 rounded-full text-gray-400 hover:text-red-500 hover:bg-red-50 transition-colors duration-150;
}

.group-delete-btn:disabled {
  @apply opacity-50 cursor-not-allowed hover:text-gray-400 hover:bg-transparent;
}

.group-note {
  @apply flex items-start mt-4 p-3 bg-blue-50 rounded-lg text-sm text-blue-800;
}

.group-note svg {
  @apply flex-shrink-0 mt-0.5 mr-2;
}

.dialog-footer {
  @apply flex justify-end p-5 bg-gray-50;
}

.submit-btn {
  @apply px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors duration-200;
}
</style>