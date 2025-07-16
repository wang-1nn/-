<script setup>
import { reactive, ref } from 'vue';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  classes: {
    type: Array,
    default: () => []
  },
  groups: {
    type: Array,
    default: () => []
  },
  currentClassId: {
    type: Number,
    default: null
  }
});

const emit = defineEmits(['close', 'add-student']);

// 新学生表单数据
const newStudent = reactive({
  name: '',
  studentId: '',
  gender: '男',
  age: 20,
  phone: '',
  email: '',
  classId: null,
  group: ''
});

// 表单验证错误
const errors = reactive({
  name: '',
  studentId: '',
  classId: ''
});

// 初始化表单
const initForm = () => {
  newStudent.name = '';
  newStudent.studentId = '';
  newStudent.gender = '男';
  newStudent.age = 20;
  newStudent.phone = '';
  newStudent.email = '';
  newStudent.classId = props.currentClassId;
  newStudent.group = props.groups[0] || '';
  
  errors.name = '';
  errors.studentId = '';
  errors.classId = '';
};

// 表单验证
const validateForm = () => {
  let isValid = true;
  
  if (!newStudent.name.trim()) {
    errors.name = '请输入学生姓名';
    isValid = false;
  } else {
    errors.name = '';
  }
  
  if (!newStudent.studentId.trim()) {
    errors.studentId = '请输入学号';
    isValid = false;
  } else {
    errors.studentId = '';
  }
  
  if (!newStudent.classId) {
    errors.classId = '请选择班级';
    isValid = false;
  } else {
    errors.classId = '';
  }
  
  return isValid;
};

// 添加学生
const addStudent = () => {
  if (validateForm()) {
    emit('add-student', { ...newStudent });
    closeDialog();
  }
};

// 关闭对话框
const closeDialog = () => {
  initForm();
  emit('close');
};
</script>

<template>
  <div v-if="visible" class="dialog-overlay" @click="closeDialog">
    <div class="dialog-container" @click.stop>
      <div class="dialog-header">
        <h3>添加新学生</h3>
        <button class="close-btn" @click="closeDialog">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
      
      <div class="dialog-content">
        <div class="info-grid">
          <div class="info-group">
            <label>姓名 <span class="required">*</span></label>
            <input 
              type="text" 
              v-model="newStudent.name" 
              placeholder="请输入学生姓名"
              :class="{'input-error': errors.name}"
            />
            <p v-if="errors.name" class="error-text">{{ errors.name }}</p>
          </div>
          
          <div class="info-group">
            <label>学号 <span class="required">*</span></label>
            <input 
              type="text" 
              v-model="newStudent.studentId" 
              placeholder="请输入学号"
              :class="{'input-error': errors.studentId}"
            />
            <p v-if="errors.studentId" class="error-text">{{ errors.studentId }}</p>
          </div>
          
          <div class="info-group">
            <label>性别</label>
            <div class="gender-selector">
              <button 
                :class="['gender-btn', newStudent.gender === '男' ? 'active' : '']"
                @click="newStudent.gender = '男'"
              >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
                男
              </button>
              <button 
                :class="['gender-btn', newStudent.gender === '女' ? 'active' : '']"
                @click="newStudent.gender = '女'"
              >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
                女
              </button>
            </div>
          </div>
          
          <div class="info-group">
            <label>年龄</label>
            <div class="number-input">
              <button 
                class="number-decrement"
                @click="newStudent.age = Math.max(16, newStudent.age - 1)"
              >-</button>
              <input type="number" v-model.number="newStudent.age" min="16" max="30" />
              <button 
                class="number-increment"
                @click="newStudent.age = Math.min(30, newStudent.age + 1)"
              >+</button>
            </div>
          </div>
          
          <div class="info-group">
            <label>手机号</label>
            <input type="tel" v-model="newStudent.phone" placeholder="请输入手机号" />
          </div>
          
          <div class="info-group">
            <label>邮箱</label>
            <input type="email" v-model="newStudent.email" placeholder="请输入邮箱" />
          </div>
          
          <div class="info-group">
            <label>班级 <span class="required">*</span></label>
            <div class="custom-select">
              <select 
                v-model="newStudent.classId"
                :class="{'input-error': errors.classId}"
              >
                <option value="" disabled>请选择班级</option>
                <option 
                  v-for="cls in classes" 
                  :key="cls.id"
                  :value="cls.id"
                >
                  {{ cls.name }}
                </option>
              </select>
              <div class="select-arrow"></div>
            </div>
            <p v-if="errors.classId" class="error-text">{{ errors.classId }}</p>
          </div>
          
          <div class="info-group">
            <label>分组</label>
            <div class="custom-select">
              <select v-model="newStudent.group">
                <option value="" disabled>请选择分组</option>
                <option 
                  v-for="group in groups" 
                  :key="group"
                  :value="group"
                >
                  {{ group }}
                </option>
              </select>
              <div class="select-arrow"></div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="dialog-footer">
        <button class="cancel-btn" @click="closeDialog">取消</button>
        <button class="submit-btn" @click="addStudent">添加</button>
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
  @apply bg-white rounded-xl shadow-lg w-full max-w-2xl mx-4 overflow-hidden;
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

.info-grid {
  @apply grid grid-cols-1 md:grid-cols-2 gap-4;
}

.info-group {
  @apply space-y-1;
}

.info-group label {
  @apply block text-sm font-medium text-gray-600;
}

.required {
  @apply text-red-500;
}

.info-group input, .custom-select select {
  @apply w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all duration-200;
}

.input-error {
  @apply border-red-500 focus:ring-red-500;
}

.error-text {
  @apply text-red-500 text-xs mt-1;
}

.gender-selector {
  @apply flex gap-2;
}

.gender-btn {
  @apply flex-1 flex items-center justify-center gap-2 px-3 py-2 border border-gray-300 rounded-lg text-gray-700 transition-all duration-200;
}

.gender-btn.active {
  @apply bg-blue-50 border-blue-500 text-blue-700 font-medium;
}

.custom-select {
  @apply relative;
}

.custom-select select {
  @apply appearance-none cursor-pointer;
}

.select-arrow {
  @apply absolute top-1/2 right-3 transform -translate-y-1/2 pointer-events-none;
}

.select-arrow::before {
  content: '';
  @apply block w-2 h-2 border-r border-b border-gray-500 transform rotate-45;
}

.number-input {
  @apply flex items-center;
}

.number-input input {
  @apply flex-1 text-center;
}

.number-decrement, .number-increment {
  @apply w-8 h-8 flex items-center justify-center bg-gray-100 hover:bg-gray-200 text-gray-700 font-medium transition-colors duration-150;
}

.number-decrement {
  @apply rounded-l-lg border border-gray-300;
}

.number-increment {
  @apply rounded-r-lg border border-gray-300 border-l-0;
}

.dialog-footer {
  @apply flex justify-end space-x-2 p-5 bg-gray-50;
}

.cancel-btn {
  @apply px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors duration-200;
}

.submit-btn {
  @apply px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors duration-200;
}
</style>