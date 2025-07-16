<script setup>
import { ref, onMounted, computed } from 'vue';
import { get } from '../../../../net/index.js';
import Card from '../../../../components/common/Card.vue';
import Button from '../../../../components/common/Button.vue';
import { useAuthStore } from '@/stores/counter';
import { ElMessage } from 'element-plus';

const authStore = useAuthStore();
const uid = computed(() => authStore.user?.id || authStore.user?.userId || '1');

const interactionItems = ref([]);
const loading = ref(false);

// 获取互动中心数据
const fetchInteractionData = () => {
  loading.value = true;
  get('/api/teacher/dashboard/interaction-hub', { uid: uid.value },
      (message, data) => {
        if (data) {
          interactionItems.value = data;
        }
        loading.value = false;
      },
      (message) => {
        ElMessage.warning(`获取互动数据失败: ${message}`);
        // 加载默认数据
        interactionItems.value = [
          {
            id: 1,
            type: 'assignment',
            student: '李晓明',
            title: '提交了「文言文阅读理解」作业',
            course: '高三(1)班 - 语文',
            timestamp: '2小时前'
          },
          {
            id: 2,
            type: 'question',
            student: '王芳',
            title: '在「函数与导数」章节提出了问题',
            course: '高三(2)班 - 数学',
            timestamp: '5小时前'
          },
          {
            id: 3,
            type: 'forum',
            student: '赵刚',
            title: '在「宋明理学」讨论区发表了新主题',
            course: '高二(5)班 - 历史',
            timestamp: '昨天'
          },
          {
            id: 4,
            type: 'assignment',
            student: '刘悦',
            title: '提交了「近代史纲要」论文',
            course: '高二(5)班 - 历史',
            timestamp: '昨天'
          },
        ];
        loading.value = false;
      }
  );
};

// 刷新数据
const refreshData = () => {
  fetchInteractionData();
};

// 监听全局刷新事件
const handleGlobalRefresh = () => {
  refreshData();
};

onMounted(() => {
  fetchInteractionData();
  // 监听全局刷新事件
  window.addEventListener('refresh-dashboard-components', handleGlobalRefresh);
});

// 组件卸载时移除事件监听
import { onUnmounted } from 'vue';
onUnmounted(() => {
  window.removeEventListener('refresh-dashboard-components', handleGlobalRefresh);
});

const getIcon = (type) => {
  switch (type) {
    case 'assignment':
      return `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-blue-500"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline></svg>`;
    case 'question':
      return `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-green-500"><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"></path><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>`;
    case 'forum':
      return `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-purple-500"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path></svg>`;
    default:
      return '';
  }
};
</script>

<template>
  <Card>
    <template #header>
      <div class="flex justify-between items-center">
        <h3 class="text-lg font-semibold">互动中心</h3>
        <div class="flex items-center gap-2">
          <button
              @click="refreshData"
              :disabled="loading"
              class="text-sm text-slate-500 hover:text-slate-700 disabled:opacity-50"
              title="刷新互动数据"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" :class="{ 'animate-spin': loading }">
              <path d="M21.5 2v6h-6M2.5 22v-6h6M2 11.5a10 10 0 0 1 18.8-4.3M22 12.5a10 10 0 0 1-18.8 4.2"/>
            </svg>
          </button>
          <div class="text-sm text-slate-500">
            实时互动
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
      <p class="text-slate-500">正在获取互动数据...</p>
    </div>

    <!-- 内容 -->
    <div v-else class="flow-root">
      <ul role="list" class="-mb-4">
        <li v-for="(item, index) in interactionItems" :key="item.id">
          <div class="relative pb-4">
            <span v-if="index !== interactionItems.length - 1" class="absolute top-2 left-2.5 -ml-px h-full w-0.5 bg-gray-200" aria-hidden="true"></span>
            <div class="relative flex items-start space-x-3">
              <div class="relative">
                <div class="h-5 w-5 rounded-full bg-gray-100 flex items-center justify-center ring-4 ring-white" v-html="getIcon(item.type)">
                </div>
              </div>
              <div class="min-w-0 flex-1">
                <div>
                  <p class="text-sm">
                    <span class="font-semibold text-gray-700">{{ item.student }}</span>
                    {{ item.title }}
                  </p>
                  <p class="text-xs text-gray-500 mt-0.5">
                    来自: {{ item.course }} &middot; {{ item.timestamp }}
                  </p>
                </div>
                <div class="mt-2 flex justify-end">
                  <Button size="sm" variant="secondary">查看详情</Button>
                </div>
              </div>
            </div>
          </div>
        </li>
      </ul>
    </div>
  </Card>
</template>