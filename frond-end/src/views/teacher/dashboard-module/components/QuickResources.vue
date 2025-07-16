<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue';
import { get } from '../../../../net/index.js';
import Card from '../../../../components/common/Card.vue';
import { useAuthStore } from '@/stores/counter';
import { ElMessage } from 'element-plus';

const authStore = useAuthStore();
const uid = computed(() => authStore.user?.id || authStore.user?.userId || '1');

const resources = ref([]);
const loading = ref(false);

// 获取快速资源数据
const fetchResources = () => {
  loading.value = true;

  // 这里使用模拟数据，因为后端API可能还未实现
  // 实际项目中应该调用后端API
  // get('/api/teacher/dashboard/resources', { uid: uid.value }, ...)

  // 模拟API调用延迟
  setTimeout(() => {
    resources.value = [
      { id: 1, title: '《赤壁赋》教学课件.pptx', type: 'ppt', size: '15.2 MB', lastModified: '2天前', isPinned: true },
      { id: 2, title: '函数与导数章节测验.docx', type: 'doc', size: '1.1 MB', lastModified: '5小时前', isPinned: true },
      { id: 3, title: '宋明理学知识点梳理.xmind', type: 'mindmap', size: '876 KB', lastModified: '昨天', isPinned: false },
      { id: 4, title: '高三上学期教学大纲.pdf', type: 'pdf', size: '2.3 MB', lastModified: '1周前', isPinned: false },
      { id: 5, title: '实验报告模板.docx', type: 'doc', size: '34 KB', lastModified: '3周前', isPinned: false },
    ];
    loading.value = false;
  }, 500);
};

// 刷新数据
const refreshResources = () => {
  fetchResources();
};

// 监听全局刷新事件
const handleGlobalRefresh = () => {
  refreshResources();
};

onMounted(() => {
  fetchResources();
  // 监听全局刷新事件
  window.addEventListener('refresh-dashboard-components', handleGlobalRefresh);
});

// 组件卸载时移除事件监听
onUnmounted(() => {
  window.removeEventListener('refresh-dashboard-components', handleGlobalRefresh);
});

const getIconClass = (type) => {
  switch (type) {
    case 'ppt': return 'text-red-500';
    case 'doc': return 'text-blue-500';
    case 'pdf': return 'text-orange-500';
    case 'mindmap': return 'text-green-500';
    default: return 'text-gray-500';
  }
};

const getIcon = (type) => {
  switch (type) {
    case 'ppt': return `<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 4h16v16H4z" /><path d="M4 12h16" /><path d="M12 4v16" /></svg>`;
    case 'doc': return `<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" /><polyline points="14 2 14 8 20 8" /><line x1="16" y1="13" x2="8" y2="13" /><line x1="16" y1="17" x2="8" y2="17" /><polyline points="10 9 9 9 8 9" /></svg>`;
    case 'pdf': return `<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" /><polyline points="14 2 14 8 20 8" /><path d="M10 12h1" /><path d="M13 12h1" /><path d="M10 18h4" /><path d="M10 15h4" /></svg>`;
    default: return `<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z" /><polyline points="13 2 13 9 20 9" /></svg>`;
  }
}

</script>

<template>
  <Card>
    <template #header>
      <div class="flex justify-between items-center">
        <h3 class="text-lg font-semibold">教学资源快捷入口</h3>
        <button
            @click="refreshResources"
            :disabled="loading"
            class="text-sm text-slate-500 hover:text-slate-700 disabled:opacity-50"
            title="刷新资源列表"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" :class="{ 'animate-spin': loading }">
            <path d="M21.5 2v6h-6M2.5 22v-6h6M2 11.5a10 10 0 0 1 18.8-4.3M22 12.5a10 10 0 0 1-18.8 4.2"/>
          </svg>
        </button>
      </div>
    </template>

    <!-- 加载状态 -->
    <div v-if="loading" class="text-center py-8">
      <svg class="animate-spin h-8 w-8 text-blue-500 mx-auto mb-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
      </svg>
      <p class="text-slate-500">正在加载资源...</p>
    </div>

    <!-- 资源列表 -->
    <div v-else class="divide-y divide-gray-200">
      <div
          v-for="resource in resources"
          :key="resource.id"
          class="flex items-center justify-between p-3 hover:bg-gray-50 transition-colors"
      >
        <div class="flex items-center min-w-0">
          <div :class="getIconClass(resource.type)" class="mr-3" v-html="getIcon(resource.type)"></div>
          <div class="min-w-0">
            <p class="text-sm font-medium text-gray-900 truncate">{{ resource.title }}</p>
            <p class="text-xs text-gray-500">{{ resource.size }} &middot; {{ resource.lastModified }}</p>
          </div>
        </div>
        <div class="ml-4">
          <button v-if="resource.isPinned" class="text-yellow-400 hover:text-yellow-500">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="currentColor" stroke="currentColor" stroke-width="1"><path d="M12 2l2.35 7.18h7.65l-6.18 4.45 2.36 7.17-6.18-4.45-6.18 4.45 2.36-7.17-6.18-4.45h7.65z"/></svg>
          </button>
        </div>
      </div>
    </div>
  </Card>
</template>