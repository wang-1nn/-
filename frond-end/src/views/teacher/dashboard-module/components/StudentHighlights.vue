<script setup>
import { ref, onMounted, computed } from 'vue';
import { get } from '../../../../net/index.js';
import Card from '../../../../components/common/Card.vue';
import { useAuthStore } from '@/stores/counter';
import { ElMessage } from 'element-plus';

const authStore = useAuthStore();
const uid = computed(() => authStore.user?.id || authStore.user?.userId || '1');

const highlights = ref([]);
const risks = ref([]);
const loading = ref(false);

// 获取学生表现亮点
const fetchHighlights = () => {
  loading.value = true;
  get('/api/teacher/dashboard/student-highlights', { uid: uid.value },
      (message, data) => {
        if (data) {
          highlights.value = data;
        }
        loading.value = false;
      },
      (message) => {
        ElMessage.warning(`获取学生亮点失败: ${message}`);
        // 加载默认数据
        highlights.value = [
          { id: 1, text: '在「期中模拟考」中，张伟、王静等5名同学取得了98分以上的优异成绩。' },
          { id: 2, text: '李娜同学在「诗歌鉴赏」单元的讨论中，连续发表了3个高质量的主题帖。' },
        ];
        loading.value = false;
      }
  );
};

// 获取学生潜在风险
const fetchRisks = () => {
  loading.value = true;
  get('/api/teacher/dashboard/student-risks', { uid: uid.value },
      (message, data) => {
        if (data) {
          risks.value = data;
        }
        loading.value = false;
      },
      (message) => {
        ElMessage.warning(`获取学生风险失败: ${message}`);
        // 加载默认数据
        risks.value = [
          { id: 1, text: '陈浩同学已经连续2次未提交「数学周测」作业，需要重点关注。' },
          { id: 2, text: '高三(2)班在「完形填空」模块的平均正确率较上周下降了15%。' },
          { id: 3, text: '孙悦同学最近7天的平台活跃度明显低于平均水平。' },
        ];
        loading.value = false;
      }
  );
};

// 刷新数据
const refreshData = () => {
  fetchHighlights();
  fetchRisks();
};

// 监听全局刷新事件
const handleGlobalRefresh = () => {
  refreshData();
};

onMounted(() => {
  fetchHighlights();
  fetchRisks();
  // 监听全局刷新事件
  window.addEventListener('refresh-dashboard-components', handleGlobalRefresh);
});

// 组件卸载时移除事件监听
import { onUnmounted } from 'vue';
onUnmounted(() => {
  window.removeEventListener('refresh-dashboard-components', handleGlobalRefresh);
});
</script>

<template>
  <Card>
    <template #header>
      <div class="flex justify-between items-center">
        <h3 class="text-lg font-semibold">学生表现亮点与风险</h3>
        <div class="flex items-center gap-2">
          <button
              @click="refreshData"
              :disabled="loading"
              class="text-sm text-slate-500 hover:text-slate-700 disabled:opacity-50"
              title="刷新分析数据"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" :class="{ 'animate-spin': loading }">
              <path d="M21.5 2v6h-6M2.5 22v-6h6M2 11.5a10 10 0 0 1 18.8-4.3M22 12.5a10 10 0 0 1-18.8 4.2"/>
            </svg>
          </button>
          <div class="text-sm text-slate-500">
            AI 智能分析
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
      <p class="text-slate-500">AI正在分析学生表现...</p>
    </div>

    <!-- 内容 -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <!-- 亮点 -->
      <div>
        <h3 class="text-lg font-semibold text-green-700 mb-3 flex items-center">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="mr-2"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"></path></svg>
          表现亮点
        </h3>
        <ul class="space-y-3">
          <li v-for="item in highlights" :key="item.id" class="flex items-start">
            <svg class="h-5 w-5 text-green-500 shrink-0 mt-0.5 mr-2" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
            </svg>
            <span class="text-sm text-gray-700">{{ item.text }}</span>
          </li>
        </ul>
      </div>

      <!-- 风险 -->
      <div>
        <h3 class="text-lg font-semibold text-red-700 mb-3 flex items-center">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="mr-2"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>
          潜在风险
        </h3>
        <ul class="space-y-3">
          <li v-for="item in risks" :key="item.id" class="flex items-start">
            <svg class="h-5 w-5 text-red-500 shrink-0 mt-0.5 mr-2" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
              <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
            </svg>
            <span class="text-sm text-gray-700">{{ item.text }}</span>
          </li>
        </ul>
      </div>
    </div>
  </Card>
</template>