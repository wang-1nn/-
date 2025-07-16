<script setup>
import { ref } from 'vue';

const props = defineProps({
  classes: {
    type: Array,
    required: true
  },
  modelValue: {
    type: Number,
    required: true
  }
});

const emit = defineEmits(['update:modelValue']);

const handleClassChange = (classId) => {
  emit('update:modelValue', classId);
};
</script>

<template>
  <div class="class-selector">
    <div class="class-selector-label">选择班级</div>
    <div class="class-list">
      <button
        v-for="item in classes"
        :key="item.id"
        :class="['class-item', { active: modelValue === item.id }]"
        @click="handleClassChange(item.id)"
      >
        {{ item.name }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.class-selector {
  @apply mb-6;
}

.class-selector-label {
  @apply text-sm text-gray-500 mb-2 font-medium;
}

.class-list {
  @apply flex flex-wrap gap-2;
}

.class-item {
  @apply px-4 py-2 bg-white border border-gray-200 rounded-lg text-gray-700 text-sm transition-all duration-200 hover:border-blue-300;
}

.class-item.active {
  @apply bg-blue-50 border-blue-500 text-blue-700 font-medium;
}
</style>