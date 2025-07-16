<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { get } from '../../../../net/index.js';
import Card from '../../../../components/common/Card.vue';
import Button from '../../../../components/common/Button.vue';
import { useAuthStore } from '@/stores/counter';
import { ElMessage } from 'element-plus';

const router = useRouter();
const authStore = useAuthStore();
const uid = computed(() => authStore.user?.id || authStore.user?.userId || '1');

const classes = ref([]);
const loading = ref(false);

// 获取班级快照数据
const fetchClassSnapshots = () => {
  loading.value = true;
  get('/api/teacher/dashboard/class-snapshots', { uid: uid.value },
      (message, data) => {
        if (data) {
          classes.value = data.map(item => ({
            id: item.courseId,
            name: item.courseName,
            students: item.totalStudents || 0,
            atRiskStudents: item.atRiskStudents || 0,
            averageScore: item.averageScore || 0,
            passRate: item.passRate || 0,
            excellentRate: item.excellentRate || 0,
            scoreDistribution: { '优': 0, '良': 0, '中': 0, '差': 0 } // 可以根据需要计算
          }));
        }
        loading.value = false;
      },
      (message) => {
        ElMessage.warning(`获取班级快照失败: ${message}`);
        // 加载默认数据
        classes.value = [
          {
            id: 1,
            name: '高三(1)班 - 语文',
            students: 45,
            atRiskStudents: 2,
            averageScore: 88.5,
            passRate: 97.8,
            excellentRate: 45.5,
            scoreDistribution: { '优': 20, '良': 18, '中': 5, '差': 2 }
          },
          {
            id: 2,
            name: '高三(2)班 - 语文',
            students: 48,
            atRiskStudents: 1,
            averageScore: 91.2,
            passRate: 100,
            excellentRate: 55.0,
            scoreDistribution: { '优': 26, '良': 19, '中': 3, '差': 0 }
          },
          {
            id: 3,
            name: '高二(5)班 - 历史',
            students: 52,
            atRiskStudents: 8,
            averageScore: 76.3,
            passRate: 84.6,
            excellentRate: 25.0,
            scoreDistribution: { '优': 13, '良': 20, '中': 11, '差': 8 }
          },
          {
            id: 4,
            name: '高一(3)班 - 政治',
            students: 50,
            atRiskStudents: 3,
            averageScore: 82.0,
            passRate: 92.0,
            excellentRate: 34.0,
            scoreDistribution: { '优': 17, '良': 22, '中': 8, '差': 3 }
          }
        ];
        loading.value = false;
      }
  );
};

// 刷新数据
const refreshData = () => {
  fetchClassSnapshots();
};

// 监听全局刷新事件
const handleGlobalRefresh = () => {
  refreshData();
};

onMounted(() => {
  fetchClassSnapshots();
  // 监听全局刷新事件
  window.addEventListener('refresh-dashboard-components', handleGlobalRefresh);
});

// 组件卸载时移除事件监听
onUnmounted(() => {
  window.removeEventListener('refresh-dashboard-components', handleGlobalRefresh);
});

const getDistributionBarWidth = (value, total) => {
  if (total === 0) return '0%';
  return `${(value / total) * 100}%`;
};

const goToAnalytics = () => {
  // 假设路由名称或路径是 '/teacher/analytics'
  // 这需要根据你的实际路由配置进行调整
  router.push('/teacher/analytics');
};
</script>

<template>
  <Card>
    <template #header>
      <div class="flex justify-between items-center w-full">
        <h3 class="text-lg font-semibold text-slate-800">班级快照</h3>
        <div class="flex items-center gap-2">
          <button
              @click="refreshData"
              :disabled="loading"
              class="text-sm text-slate-500 hover:text-slate-700 disabled:opacity-50"
              title="刷新班级数据"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" :class="{ 'animate-spin': loading }">
              <path d="M21.5 2v6h-6M2.5 22v-6h6M2 11.5a10 10 0 0 1 18.8-4.3M22 12.5a10 10 0 0 1-18.8 4.2"/>
            </svg>
          </button>
          <button
              @click="goToAnalytics"
              class="group inline-flex items-center text-sm font-semibold text-primary-600 hover:text-primary-700 transition-colors"
          >
            查看学情分析
            <svg
                xmlns="http://www.w3.org/2000/svg"
                class="h-4 w-4 ml-1 transform transition-transform duration-200 group-hover:translate-x-1"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7l5 5m0 0l-5 5m5-5H6" />
            </svg>
          </button>
        </div>
      </div>
    </template>

    <!-- 加载状态 -->
    <div v-if="loading" class="text-center py-8">
      <svg class="animate-spin h-8 w-8 text-blue-500 mx-auto mb-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
      </svg>
      <p class="text-slate-500">正在获取班级数据...</p>
    </div>
    <!-- 班级数据 -->
    <div v-else class="space-y-4 max-h-[28rem] overflow-y-auto pr-2">
      <div
          v-for="c in classes"
          :key="c.id"
          class="p-4 border border-gray-200 rounded-lg bg-white hover:shadow-md transition-shadow duration-200"
      >
        <div class="flex justify-between items-start mb-3">
          <div>
            <p class="font-bold text-lg text-slate-800">{{ c.name }}</p>
            <p class="text-sm text-slate-500">{{ c.students }} 名学生</p>
          </div>
          <div v-if="c.atRiskStudents > 0" class="flex items-center text-xs text-red-600 bg-red-100 rounded-full px-2 py-1 font-semibold">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.21 3.03-1.742 3.03H4.42c-1.532 0-2.492-1.696-1.742-3.03l5.58-9.92zM10 13a1 1 0 110-2 1 1 0 010 2zm-1-4a1 1 0 011-1h.01a1 1 0 110 2H10a1 1 0 01-1-1z" clip-rule="evenodd" />
            </svg>
            {{ c.atRiskStudents }} 名学生待关注
          </div>
        </div>

        <div class="grid grid-cols-3 gap-4 text-center mb-4">
          <div>
            <p class="text-xl font-bold text-blue-600">{{ c.averageScore.toFixed(1) }}</p>
            <p class="text-xs text-slate-500">平均分</p>
          </div>
          <div>
            <p class="text-xl font-bold text-green-600">{{ c.passRate.toFixed(1) }}%</p>
            <p class="text-xs text-slate-500">及格率</p>
          </div>
          <div>
            <p class="text-xl font-bold text-orange-500">{{ c.excellentRate.toFixed(1) }}%</p>
            <p class="text-xs text-slate-500">优秀率</p>
          </div>
        </div>

        <div>
          <p class="text-xs text-slate-400 mb-1">分数段分布</p>
          <div class="w-full bg-gray-200 rounded-full h-2.5 flex overflow-hidden">
            <div class="h-2.5 bg-green-500" :style="{ width: getDistributionBarWidth(c.scoreDistribution['优'], c.students) }" title="优秀"></div>
            <div class="h-2.5 bg-blue-500" :style="{ width: getDistributionBarWidth(c.scoreDistribution['良'], c.students) }" title="良好"></div>
            <div class="h-2.5 bg-yellow-500" :style="{ width: getDistributionBarWidth(c.scoreDistribution['中'], c.students) }" title="中等"></div>
            <div class="h-2.5 bg-red-500" :style="{ width: getDistributionBarWidth(c.scoreDistribution['差'], c.students) }" title="需努力"></div>
          </div>
        </div>

        <div class="flex justify-end space-x-2 mt-4">
          <Button size="sm" variant="secondary">班级主页</Button>
          <Button size="sm" variant="primary">进入批改</Button>
        </div>
      </div>
    </div>
  </Card>
</template>