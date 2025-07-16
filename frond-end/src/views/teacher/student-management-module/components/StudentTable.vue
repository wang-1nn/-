<script setup>
import { ref } from 'vue';

const props = defineProps({
  students: {
    type: Array,
    required: true
  }
});

const emit = defineEmits(['view-student', 'delete-student', 'selection-change', 'add-tag', 'remove-tag']);

// 表格多选
const multipleSelection = ref([]);

// 处理表格选择变化
const handleSelectionChange = (selection) => {
  multipleSelection.value = selection;
  emit('selection-change', selection);
};

// 获取学生头像背景色（根据姓名生成随机颜色）
const getAvatarColor = (name) => {
  const colors = [
    'bg-blue-500', 'bg-green-500', 'bg-purple-500', 
    'bg-pink-500', 'bg-yellow-500', 'bg-red-500', 
    'bg-indigo-500', 'bg-teal-500'
  ];
  
  // 使用姓名的第一个字符的Unicode值作为索引
  const index = name.charCodeAt(0) % colors.length;
  return colors[index];
};
</script>

<template>
  <div class="student-table">
    <table class="w-full">
      <thead>
        <tr>
          <th class="selection-cell">
            <input 
              type="checkbox" 
              class="custom-checkbox"
              @change="$event => $emit('selection-all', $event.target.checked)"
            >
          </th>
          <th>姓名</th>
          <th>学号</th>
          <th>性别</th>
          <th>班级</th>
          <th>分组</th>
          <th>出勤率</th>
          <th>平均成绩</th>
          <th>标签</th>
          <th>操作</th>
        </tr>
      </thead>
      
      <tbody>
        <tr 
          v-for="student in students" 
          :key="student.id"
          class="student-row"
        >
          <td class="selection-cell">
            <input 
              type="checkbox" 
              :checked="multipleSelection.includes(student)" 
              @change="$event => {
                if ($event.target.checked) {
                  multipleSelection.push(student);
                } else {
                  const index = multipleSelection.indexOf(student);
                  if (index !== -1) multipleSelection.splice(index, 1);
                }
                handleSelectionChange(multipleSelection);
              }"
              class="custom-checkbox"
            >
          </td>
          
          <td>
            <div class="student-name" @click="$emit('view-student', student)">
              <div :class="['student-avatar', getAvatarColor(student.name)]">
                {{ student.name.charAt(0) }}
              </div>
              <span>{{ student.name }}</span>
            </div>
          </td>
          
          <td>{{ student.studentId }}</td>
          <td>{{ student.gender }}</td>
          <td>{{ student.class }}</td>
          <td>{{ student.group }}</td>
          
          <td>
            <div class="attendance-indicator">
              <div class="attendance-bar">
                <div 
                  class="attendance-progress" 
                  :style="{width: `${student.attendance}%`}"
                  :class="{'high': student.attendance >= 90, 'medium': student.attendance >= 75 && student.attendance < 90, 'low': student.attendance < 75}"
                ></div>
              </div>
              <span class="attendance-value">{{ student.attendance }}%</span>
            </div>
          </td>
          
          <td>
            <span 
              :class="{
                'text-green-600': student.score >= 90,
                'text-blue-600': student.score >= 75 && student.score < 90,
                'text-yellow-600': student.score >= 60 && student.score < 75,
                'text-red-600': student.score < 60
              }"
              class="font-medium"
            >
              {{ student.score }}
            </span>
          </td>
          
          <td>
            <div class="student-tags">
              <span 
                v-for="tag in student.tags" 
                :key="tag"
                class="student-tag"
              >
                {{ tag }}
                <button 
                  class="tag-remove-btn"
                  @click="$emit('remove-tag', student, tag)"
                >
                  &times;
                </button>
              </span>
              
              <button 
                v-if="student.tags.length < 3"
                class="add-tag-btn"
                @click="$emit('add-tag', student)"
              >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                </svg>
              </button>
            </div>
          </td>
          
          <td>
            <div class="student-actions">
              <button 
                class="edit-btn"
                @click="$emit('view-student', student)"
              >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                </svg>
                编辑
              </button>
              
              <button 
                class="delete-btn"
                @click="$emit('delete-student', student)"
              >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                </svg>
                删除
              </button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
    
    <div v-if="students.length === 0" class="empty-table">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
      </svg>
      <p class="text-gray-500 mt-2">未找到符合条件的学生</p>
    </div>
  </div>
</template>

<style scoped>
.student-table {
  @apply w-full overflow-x-auto rounded-lg border border-gray-200 bg-white;
}

table {
  @apply min-w-full border-collapse;
}

th {
  @apply bg-gray-50 px-4 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider border-b border-gray-200;
}

.student-row {
  @apply hover:bg-blue-50 transition-colors duration-150;
}

.student-row:not(:last-child) {
  @apply border-b border-gray-100;
}

td {
  @apply px-4 py-3 text-sm text-gray-700;
}

.selection-cell {
  @apply w-10;
}

.custom-checkbox {
  @apply rounded text-blue-600 focus:ring-blue-500 h-4 w-4;
}

.student-name {
  @apply flex items-center space-x-2 cursor-pointer hover:text-blue-600 transition-colors duration-150;
}

.student-avatar {
  @apply w-8 h-8 rounded-full flex items-center justify-center text-white text-sm font-medium;
}

.attendance-indicator {
  @apply flex items-center space-x-2;
}

.attendance-bar {
  @apply w-16 h-2 bg-gray-100 rounded-full overflow-hidden;
}

.attendance-progress {
  @apply h-full rounded-full;
}

.attendance-progress.high {
  @apply bg-green-500;
}

.attendance-progress.medium {
  @apply bg-yellow-500;
}

.attendance-progress.low {
  @apply bg-red-500;
}

.attendance-value {
  @apply text-xs text-gray-600;
}

.student-tags {
  @apply flex flex-wrap gap-1;
}

.student-tag {
  @apply bg-gray-100 text-gray-700 text-xs px-2 py-1 rounded-full flex items-center;
}

.tag-remove-btn {
  @apply ml-1 text-gray-500 hover:text-red-500 transition-colors duration-150;
}

.add-tag-btn {
  @apply w-5 h-5 rounded-full bg-gray-200 flex items-center justify-center hover:bg-gray-300 transition-colors duration-150;
}

.student-actions {
  @apply flex space-x-2;
}

.edit-btn {
  @apply flex items-center space-x-1 px-2 py-1 bg-blue-50 text-blue-700 text-xs rounded hover:bg-blue-100 transition-colors duration-150;
}

.edit-btn svg {
  @apply mr-1;
}

.delete-btn {
  @apply flex items-center space-x-1 px-2 py-1 bg-red-50 text-red-700 text-xs rounded hover:bg-red-100 transition-colors duration-150;
}

.delete-btn svg {
  @apply mr-1;
}

.empty-table {
  @apply py-12 flex flex-col items-center justify-center;
}
</style>