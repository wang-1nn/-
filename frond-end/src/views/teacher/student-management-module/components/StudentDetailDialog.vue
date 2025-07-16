<script setup>
import { ref, watch } from 'vue';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  student: {
    type: Object,
    default: null
  },
  classes: {
    type: Array,
    default: () => []
  },
  groups: {
    type: Array,
    default: () => []
  },
  commonTags: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['close', 'update', 'add-tag', 'remove-tag']);

const localStudent = ref(null);

// 当父组件传入的student改变时，更新本地数据
watch(() => props.student, (newVal) => {
  if (newVal) {
    localStudent.value = JSON.parse(JSON.stringify(newVal));
  }
}, { immediate: true });

// 更新学生信息
const updateStudent = () => {
  emit('update', localStudent.value);
};

// 关闭对话框
const closeDialog = () => {
  emit('close');
};

// 添加标签
const addTag = (tag) => {
  if (!localStudent.value.tags.includes(tag)) {
    localStudent.value.tags.push(tag);
  }
};

// 移除标签
const removeTag = (tag) => {
  const index = localStudent.value.tags.indexOf(tag);
  if (index !== -1) {
    localStudent.value.tags.splice(index, 1);
  }
};

// 格式化小数
const formatDecimal = (value) => {
  return parseFloat(value).toFixed(1);
};
</script>

<template>
  <div v-if="visible && student" class="dialog-overlay" @click="closeDialog">
    <div class="dialog-container" @click.stop>
      <div class="dialog-header">
        <div class="flex items-center">
          <div class="student-detail-avatar">{{ student.name.charAt(0) }}</div>
          <h3>{{ student.name }}的详细信息</h3>
        </div>
        <button class="close-btn" @click="closeDialog">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
      
      <div class="dialog-content">
        <div class="info-section">
          <h4 class="section-title">基本信息</h4>
          
          <div class="info-grid">
            <div class="info-group">
              <label>姓名</label>
              <input type="text" v-model="localStudent.name" />
            </div>
            
            <div class="info-group">
              <label>学号</label>
              <input type="text" v-model="localStudent.studentId" disabled class="disabled-input" />
            </div>
            
            <div class="info-group">
              <label>性别</label>
              <div class="custom-select">
                <select v-model="localStudent.gender">
                  <option value="男">男</option>
                  <option value="女">女</option>
                </select>
                <div class="select-arrow"></div>
              </div>
            </div>
            
            <div class="info-group">
              <label>年龄</label>
              <div class="number-input">
                <button 
                  class="number-decrement"
                  @click="localStudent.age = Math.max(16, localStudent.age - 1)"
                >-</button>
                <input type="number" v-model.number="localStudent.age" min="16" max="30" />
                <button 
                  class="number-increment"
                  @click="localStudent.age = Math.min(30, localStudent.age + 1)"
                >+</button>
              </div>
            </div>
            
            <div class="info-group">
              <label>手机号</label>
              <input type="tel" v-model="localStudent.phone" />
            </div>
            
            <div class="info-group">
              <label>邮箱</label>
              <input type="email" v-model="localStudent.email" />
            </div>
            
            <div class="info-group">
              <label>班级</label>
              <div class="custom-select">
                <select v-model="localStudent.classId">
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
            </div>
            
            <div class="info-group">
              <label>分组</label>
              <div class="custom-select">
                <select v-model="localStudent.group">
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
        
        <div class="info-section">
          <h4 class="section-title">学习表现</h4>
          
          <div class="info-grid">
            <div class="info-group col-span-2">
              <label>出勤率</label>
              <div class="range-slider">
                <input 
                  type="range" 
                  v-model.number="localStudent.attendance" 
                  min="0" 
                  max="100" 
                  step="1" 
                  class="slider"
                />
                <div class="slider-value">{{ localStudent.attendance }}%</div>
              </div>
            </div>
            
            <div class="info-group col-span-2">
              <label>平均成绩</label>
              <div class="range-slider">
                <input 
                  type="range" 
                  v-model.number="localStudent.score" 
                  min="0" 
                  max="100" 
                  step="0.5" 
                  class="slider score-slider"
                />
                <div 
                  class="slider-value"
                  :class="{
                    'high-score': localStudent.score >= 90,
                    'medium-score': localStudent.score >= 75 && localStudent.score < 90,
                    'low-score': localStudent.score >= 60 && localStudent.score < 75,
                    'fail-score': localStudent.score < 60
                  }"
                >
                  {{ formatDecimal(localStudent.score) }}
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="info-section">
          <h4 class="section-title">标签管理</h4>
          
          <div class="tags-container">
            <div class="current-tags">
              <div 
                v-for="tag in localStudent.tags" 
                :key="tag"
                class="student-tag"
              >
                {{ tag }}
                <button 
                  class="tag-remove-btn"
                  @click="removeTag(tag)"
                >
                  &times;
                </button>
              </div>
              
              <div v-if="localStudent.tags.length === 0" class="no-tags">
                暂无标签
              </div>
            </div>
            
            <div class="tags-divider">
              <span>可选标签</span>
            </div>
            
            <div class="available-tags">
              <button 
                v-for="tag in commonTags.filter(t => !localStudent.tags.includes(t))" 
                :key="tag"
                class="available-tag"
                @click="addTag(tag)"
              >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                </svg>
                {{ tag }}
              </button>
            </div>
          </div>
        </div>
      </div>
      
      <div class="dialog-footer">
        <button class="cancel-btn" @click="closeDialog">取消</button>
        <button class="submit-btn" @click="updateStudent">保存</button>
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
  @apply bg-white rounded-xl shadow-lg w-full max-w-4xl mx-4 overflow-hidden;
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

.student-detail-avatar {
  @apply w-10 h-10 rounded-full bg-blue-500 flex items-center justify-center text-white font-medium text-lg mr-3;
}

.close-btn {
  @apply p-1 rounded-full hover:bg-gray-100 text-gray-500 hover:text-gray-700 transition-colors duration-200;
}

.dialog-content {
  @apply p-5 max-h-[calc(100vh-200px)] overflow-y-auto;
}

.info-section {
  @apply mb-6;
}

.section-title {
  @apply text-base font-medium text-gray-700 mb-3 pb-2 border-b border-gray-100;
}

.info-grid {
  @apply grid grid-cols-1 md:grid-cols-2 gap-4;
}

.col-span-2 {
  grid-column: span 2 / span 2;
}

.info-group {
  @apply space-y-1;
}

.info-group label {
  @apply block text-sm font-medium text-gray-600;
}

.info-group input, .custom-select select {
  @apply w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all duration-200;
}

.disabled-input {
  @apply bg-gray-50 text-gray-500 cursor-not-allowed;
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

.range-slider {
  @apply relative pt-5 pb-8;
}

.slider {
  @apply w-full h-2 rounded-full appearance-none bg-gray-200 outline-none;
}

.slider::-webkit-slider-thumb {
  @apply appearance-none w-5 h-5 rounded-full bg-blue-500 cursor-pointer transition-all duration-150 hover:bg-blue-600;
}

.score-slider::-webkit-slider-thumb {
  @apply bg-gradient-to-r from-red-500 via-yellow-500 to-green-500;
}

.slider-value {
  @apply absolute top-0 right-0 text-sm font-medium text-gray-600;
}

.high-score {
  @apply text-green-600;
}

.medium-score {
  @apply text-blue-600;
}

.low-score {
  @apply text-yellow-600;
}

.fail-score {
  @apply text-red-600;
}

.tags-container {
  @apply space-y-3;
}

.current-tags {
  @apply flex flex-wrap gap-2;
}

.student-tag {
  @apply bg-blue-50 text-blue-700 px-3 py-1 rounded-full flex items-center text-sm;
}

.tag-remove-btn {
  @apply ml-2 text-blue-400 hover:text-blue-700 transition-colors duration-150;
}

.no-tags {
  @apply text-sm text-gray-500;
}

.tags-divider {
  @apply flex items-center py-2;
}

.tags-divider::before, .tags-divider::after {
  content: '';
  @apply flex-1 border-t border-gray-200;
}

.tags-divider span {
  @apply px-3 text-xs text-gray-500 font-medium;
}

.available-tags {
  @apply flex flex-wrap gap-2;
}

.available-tag {
  @apply flex items-center bg-gray-100 hover:bg-gray-200 text-gray-700 px-3 py-1 rounded-full text-sm transition-colors duration-150;
}

.available-tag svg {
  @apply mr-1;
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