<script setup>
import { ref } from 'vue';
import {
  BookOpenIcon,
  ClipboardDocumentListIcon,
  DocumentTextIcon,
  AcademicCapIcon,
  PresentationChartBarIcon,
  ChatBubbleLeftRightIcon
} from '@heroicons/vue/24/outline';

const links = ref([
  { title: '学习资料库', icon: BookOpenIcon, url: '#', bgColor: 'bg-blue-100 text-blue-600' },
  { title: '考试中心', icon: ClipboardDocumentListIcon, url: '#', bgColor: 'bg-purple-100 text-purple-600' },
  { title: '课程表', icon: DocumentTextIcon, url: '#', bgColor: 'bg-green-100 text-green-600' },
  { title: '成绩单', icon: AcademicCapIcon, url: '#', bgColor: 'bg-amber-100 text-amber-600' },
  { title: '学习分析', icon: PresentationChartBarIcon, url: '#', bgColor: 'bg-pink-100 text-pink-600' },
  { title: '讨论区', icon: ChatBubbleLeftRightIcon, url: '#', bgColor: 'bg-indigo-100 text-indigo-600' }
]);
</script>

<template>
  <div class="quick-links-widget">
    <div class="links-grid">
      <a v-for="(link, index) in links" :key="index" :href="link.url" class="link-item">
        <div class="link-icon" :class="link.bgColor">
          <component :is="link.icon" class="h-5 w-5" />
        </div>
        <div class="link-title">{{ link.title }}</div>
      </a>
    </div>
  </div>
</template>

<style scoped>
.quick-links-widget {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.links-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.75rem;
}

.link-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 1rem 0.5rem;
  border-radius: 0.5rem;
  background-color: #f9fafb;
  transition: all 0.2s ease;
  text-decoration: none;
}

.link-item:hover {
  background-color: #f3f4f6;
  transform: translateY(-2px);
}

.link-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 0.5rem;
  margin-bottom: 0.5rem;
}

.link-title {
  font-size: 0.75rem;
  font-weight: 500;
  color: #4b5563;
  text-align: center;
}
</style> 