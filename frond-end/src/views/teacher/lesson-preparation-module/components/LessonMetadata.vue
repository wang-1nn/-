<script setup>
import { reactive } from 'vue';

const props = defineProps({
  lessonPlan: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['update:lessonPlan']);

// 表单验证
const errors = reactive({
  title: '',
  grade: '',
  subject: '',
  duration: ''
});

// 验证单个字段
const validateField = (field) => {
  if (!props.lessonPlan[field] && field !== 'duration') {
    errors[field] = `请填写${field === 'title' ? '教案标题' : field === 'grade' ? '适用年级' : '学科'}`;
    return false;
  } else if (field === 'duration' && (props.lessonPlan.duration <= 0 || isNaN(props.lessonPlan.duration))) {
    errors[field] = '请输入有效的课时数';
    return false;
  } else {
    errors[field] = '';
    return true;
  }
};

// 更新表单值
const updateValue = (field, value) => {
  const updatedPlan = { ...props.lessonPlan };
  updatedPlan[field] = value;
  emit('update:lessonPlan', updatedPlan);
  validateField(field);
};
</script>

<template>
  <div class="lesson-metadata">
    <h2>教案信息</h2>
    
    <div class="form-grid">
      <div class="form-group">
        <label for="lesson-title">教案标题</label>
        <input 
          id="lesson-title"
          type="text" 
          :value="lessonPlan.title" 
          @input="updateValue('title', $event.target.value)"
          placeholder="输入教案标题"
          :class="{'error': errors.title}"
        />
        <p v-if="errors.title" class="error-text">{{ errors.title }}</p>
      </div>
      
      <div class="form-group">
        <label for="lesson-grade">适用年级</label>
        <input 
          id="lesson-grade"
          type="text" 
          :value="lessonPlan.grade" 
          @input="updateValue('grade', $event.target.value)"
          placeholder="输入适用年级"
          :class="{'error': errors.grade}"
        />
        <p v-if="errors.grade" class="error-text">{{ errors.grade }}</p>
      </div>
      
      <div class="form-group">
        <label for="lesson-subject">学科</label>
        <input 
          id="lesson-subject"
          type="text" 
          :value="lessonPlan.subject" 
          @input="updateValue('subject', $event.target.value)"
          placeholder="输入学科"
          :class="{'error': errors.subject}"
        />
        <p v-if="errors.subject" class="error-text">{{ errors.subject }}</p>
      </div>
      
      <div class="form-group">
        <label for="lesson-duration">总课时</label>
        <div class="number-input">
          <button 
            class="number-decrement"
            @click="updateValue('duration', Math.max(1, lessonPlan.duration - 1))"
          >-</button>
          <input 
            id="lesson-duration"
            type="number" 
            :value="lessonPlan.duration" 
            @input="updateValue('duration', parseInt($event.target.value) || 1)"
            min="1"
            :class="{'error': errors.duration}"
          />
          <button 
            class="number-increment"
            @click="updateValue('duration', lessonPlan.duration + 1)"
          >+</button>
        </div>
        <p v-if="errors.duration" class="error-text">{{ errors.duration }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.lesson-metadata {
  @apply bg-white rounded-lg shadow-sm p-6 border border-gray-200;
}

.lesson-metadata h2 {
  @apply text-lg font-medium text-gray-800 mb-4;
}

.form-grid {
  @apply grid grid-cols-1 md:grid-cols-2 gap-6;
}

.form-group {
  @apply space-y-1;
}

.form-group label {
  @apply block text-sm font-medium text-gray-600 mb-1;
}

.form-group input {
  @apply w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all duration-200;
}

.form-group input.error {
  @apply border-red-500 focus:ring-red-500;
}

.error-text {
  @apply text-red-500 text-xs mt-1;
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
</style>