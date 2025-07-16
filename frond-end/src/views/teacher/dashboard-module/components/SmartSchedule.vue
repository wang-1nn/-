<script setup>
import { ref, onMounted, computed } from 'vue';
import { get } from '../../../../net/index.js';
import Card from '../../../../components/common/Card.vue';
import { useAuthStore } from '@/stores/counter';
import { ElMessage } from 'element-plus';

const authStore = useAuthStore();
const uid = computed(() => authStore.user?.id || authStore.user?.userId || '1');

const today = new Date();
const formattedDate = `${today.getFullYear()}年${today.getMonth() + 1}月${today.getDate()}日`;

const scheduleItems = ref([]);
const loading = ref(false);

// 获取智能日程数据
const fetchScheduleData = () => {
  loading.value = true;
  get('/api/teacher/dashboard/smart-schedule', { uid: uid.value },
      (message, data) => {
        if (data) {
          scheduleItems.value = data;
        }
        loading.value = false;
      },
      (message) => {
        ElMessage.warning(`获取智能日程失败: ${message}`);
        // 加载默认数据
        scheduleItems.value = [
          { id: 1, time: '08:00 - 09:35', title: '高等数学 II', location: '教A-301', type: 'class' },
          { id: 2, time: '10:00 - 11:30', title: '大学物理 I', location: '实验楼-210', type: 'class' },
          { id: 3, time: '12:00', title: '午餐会议', location: '教师食堂', type: 'meeting' },
          { id: 4, time: '14:00 - 15:30', title: '教研室周会', location: '办公楼-502', type: 'meeting' },
          { id: 5, time: '17:00', title: '批改「数据结构」期中作业', location: '在线', type: 'deadline' },
          { id: 6, time: '20:00', title: '准备「计算机组成原理」课件', location: '在线', type: 'deadline' },
        ];
        loading.value = false;
      }
  );
};

// 刷新数据
const refreshSchedule = () => {
  fetchScheduleData();
};

// 监听全局刷新事件
const handleGlobalRefresh = () => {
  refreshSchedule();
};

onMounted(() => {
  fetchScheduleData();
  // 监听全局刷新事件
  window.addEventListener('refresh-dashboard-components', handleGlobalRefresh);
});

// 组件卸载时移除事件监听
import { onUnmounted } from 'vue';
onUnmounted(() => {
  window.removeEventListener('refresh-dashboard-components', handleGlobalRefresh);
});

const getDotClass = (type) => {
  switch (type) {
    case 'class':
      return 'bg-blue-500 text-white';
    case 'meeting':
      return 'bg-green-500 text-white';
    case 'deadline':
      return 'bg-yellow-500 text-white';
    default:
      return 'bg-gray-400 text-white';
  }
};

const getIcon = (type) => {
  switch (type) {
    case 'class':
      return `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M4 19.5v-15A2.5 2.5 0 0 1 6.5 2H20v20H6.5a2.5 2.5 0 0 1 0-5H20"></path></svg>`; // Book icon
    case 'meeting':
      return `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>`; // Users icon
    case 'deadline':
      return `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z"></path><polyline points="12 6 12 12 16 14"></polyline></svg>`; // Clock icon
    default:
      return '';
  }
};
</script>

<template>
  <Card>
    <template #header>
      <div class="flex justify-between items-center">
        <h3 class="text-lg font-semibold">智能日程 - {{ formattedDate }}</h3>
        <div class="flex items-center gap-2">
          <button
              @click="refreshSchedule"
              :disabled="loading"
              class="text-sm text-slate-500 hover:text-slate-700 disabled:opacity-50"
              title="刷新智能日程"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" :class="{ 'animate-spin': loading }">
              <path d="M21.5 2v6h-6M2.5 22v-6h6M2 11.5a10 10 0 0 1 18.8-4.3M22 12.5a10 10 0 0 1-18.8 4.2"/>
            </svg>
          </button>
          <div class="text-sm text-slate-500">
            AI 为您推荐
          </div>
        </div>
      </div>
    </template>
    <!-- 加载状态 -->
    <div v-if="loading" class="text-center py-8">
      <svg class="animate-spin h-8 w-8 text-blue-500 mx-auto mb-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
      </svg>
      <p class="text-slate-500">AI正在为您生成智能日程...</p>
    </div>

    <!-- 日程内容 -->
    <div v-else-if="scheduleItems.length > 0" class="relative pl-6">
      <!-- Vertical timeline -->
      <div class="absolute left-6 top-0 h-full w-0.5 bg-gray-200" aria-hidden="true"></div>

      <ul class="relative space-y-8">
        <li v-for="item in scheduleItems" :key="item.id" class="flex items-start">
          <!-- Dot on the timeline -->
          <div
              class="absolute left-0 top-1 flex h-6 w-6 items-center justify-center rounded-full ring-8 ring-white"
              :class="getDotClass(item.type)"
              v-html="getIcon(item.type)"
          >
          </div>

          <!-- Content -->
          <div class="ml-6 w-full">
            <div class="rounded-lg bg-gray-50 border border-gray-200 p-4 hover:border-gray-300 hover:shadow-sm transition-all">
              <div class="flex items-center justify-between">
                <p class="font-bold text-gray-800">{{ item.title }}</p>
                <span class="text-sm font-semibold text-gray-700 bg-white border border-gray-200 rounded-full px-3 py-1">{{ item.time }}</span>
              </div>
              <p v-if="item.location" class="mt-2 text-sm text-gray-600 flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="mr-1.5"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg>
                {{ item.location }}
              </p>
            </div>
          </div>
        </li>
      </ul>
    </div>

    <div v-else class="text-center py-8 text-gray-500">
      <svg xmlns="http://www.w3.org/2000/svg" class="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
      </svg>
      <p class="mt-2 text-sm font-medium">今日暂无安排</p>
      <p class="text-xs">享受一个轻松的日子吧！</p>
    </div>
  </Card>
</template>