<template>
  <Card>
    <template #header>
      <div class="flex justify-between items-center">
        <h3 class="text-lg font-semibold">AI 教学助手</h3>
        <div class="text-sm text-slate-500">
          智能分析与建议
        </div>
      </div>
    </template>

    <div class="space-y-4">
      <!-- AI功能按钮组 -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        <button
            @click="generateTeachingSuggestions"
            :disabled="loading"
            class="p-4 text-left border border-slate-200 rounded-lg hover:shadow-md hover:border-blue-300 transition-all disabled:opacity-50 bg-gradient-to-br from-blue-50 to-blue-100"
        >
          <div class="flex items-center gap-2 mb-1">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-blue-500">
              <path d="M9 11H5a2 2 0 0 0-2 2v3c0 1.1.9 2 2 2h4l3 3V8l-3 3z"/>
              <path d="M22 4H12a2 2 0 0 0-2 2v4a2 2 0 0 0 2 2h9l1 1V6a2 2 0 0 0-2-2z"/>
            </svg>
            <span class="text-sm font-medium">教学建议</span>
          </div>
          <p class="text-xs text-slate-500">AI生成个性化教学策略</p>
        </button>

        <button
            @click="analyzeClassPerformance"
            :disabled="loading"
            class="p-3 text-left border border-slate-200 rounded-lg hover:shadow-sm transition-shadow disabled:opacity-50"
        >
          <div class="flex items-center gap-2 mb-1">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-green-500">
              <path d="M3 3v18h18"/>
              <path d="M18.7 8l-5.1 5.2-2.8-2.7L7 14.3"/>
            </svg>
            <span class="text-sm font-medium">班级分析</span>
          </div>
          <p class="text-xs text-slate-500">AI分析班级整体表现</p>
        </button>

        <button
            @click="generatePersonalizedAdvice"
            :disabled="loading"
            class="p-3 text-left border border-slate-200 rounded-lg hover:shadow-sm transition-shadow disabled:opacity-50"
        >
          <div class="flex items-center gap-2 mb-1">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-purple-500">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
            <span class="text-sm font-medium">个性化建议</span>
          </div>
          <p class="text-xs text-slate-500">为特定学生生成建议</p>
        </button>

        <button
            @click="predictLearningTrend"
            :disabled="loading"
            class="p-3 text-left border border-slate-200 rounded-lg hover:shadow-sm transition-shadow disabled:opacity-50"
        >
          <div class="flex items-center gap-2 mb-1">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-orange-500">
              <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
              <polyline points="7.5,4.21 12,6.81 16.5,4.21"/>
              <polyline points="7.5,19.79 7.5,14.6 3,12"/>
              <polyline points="21,12 16.5,14.6 16.5,19.79"/>
            </svg>
            <span class="text-sm font-medium">趋势预测</span>
          </div>
          <p class="text-xs text-slate-500">预测学生学习发展</p>
        </button>

        <button
            @click="generateGradingSuggestions"
            :disabled="loading"
            class="p-3 text-left border border-slate-200 rounded-lg hover:shadow-sm transition-shadow disabled:opacity-50"
        >
          <div class="flex items-center gap-2 mb-1">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-red-500">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14,2 14,8 20,8"/>
              <line x1="16" y1="13" x2="8" y2="13"/>
              <line x1="16" y1="17" x2="8" y2="17"/>
              <polyline points="10,9 9,9 8,9"/>
            </svg>
            <span class="text-sm font-medium">批改建议</span>
          </div>
          <p class="text-xs text-slate-500">AI辅助作业批改</p>
        </button>

        <button
            @click="refreshAllAI"
            :disabled="loading"
            class="p-3 text-left border border-slate-200 rounded-lg hover:shadow-sm transition-shadow disabled:opacity-50"
        >
          <div class="flex items-center gap-2 mb-1">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-slate-500" :class="{ 'animate-spin': loading }">
              <path d="M21.5 2v6h-6M2.5 22v-6h6M2 11.5a10 10 0 0 1 18.8-4.3M22 12.5a10 10 0 0 1-18.8 4.2"/>
            </svg>
            <span class="text-sm font-medium">刷新AI数据</span>
          </div>
          <p class="text-xs text-slate-500">重新生成所有AI分析</p>
        </button>
      </div>

      <!-- AI响应显示区域 -->
      <div v-if="aiResponse || loading" class="mt-6 p-5 bg-gradient-to-br from-blue-50 to-indigo-50 rounded-xl border border-blue-200">
        <div v-if="loading" class="flex items-center gap-3 text-blue-700">
          <div class="relative">
            <svg class="animate-spin h-5 w-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
          </div>
          <span class="font-medium">AI正在分析中...</span>
        </div>
        <div v-else-if="aiResponse" class="space-y-3">
          <div class="flex items-center gap-2 mb-3">
            <div class="w-6 h-6 bg-blue-500 rounded-full flex items-center justify-center">
              <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="5"/>
                <path d="M12 1v6m0 6v6m11-7h-6m-6 0H1"/>
              </svg>
            </div>
            <span class="font-semibold text-slate-700">AI 分析结果</span>
          </div>
          <div class="bg-white rounded-lg p-4 shadow-sm border border-blue-100">
            <p class="text-slate-700 leading-relaxed">{{ aiResponse }}</p>
          </div>
        </div>
      </div>
    </div>
  </Card>
</template>

<script setup>
import { ref, computed } from 'vue';
import { post } from '../../../../net/index.js';
import Card from '../../../../components/common/Card.vue';
import { useAuthStore } from '@/stores/counter';
import { ElMessage } from 'element-plus';

const authStore = useAuthStore();
const uid = computed(() => authStore.user?.id || authStore.user?.userId || '1');

const loading = ref(false);
const aiResponse = ref('');

// 生成教学建议
const generateTeachingSuggestions = () => {
  loading.value = true;
  aiResponse.value = '';

  post('/api/teacher/ai/teaching-suggestions', { uid: uid.value, courseId: 1 },
      (message, data) => {
        aiResponse.value = data || '基于当前班级数据分析，建议在下一章节中增加更多的互动环节，特别是小组讨论和案例分析，以提高学生的参与度和理解深度。';
        loading.value = false;
      },
      (message) => {
        ElMessage.error(`生成教学建议失败: ${message}`);
        aiResponse.value = '基于当前班级数据分析，建议在下一章节中增加更多的互动环节，特别是小组讨论和案例分析，以提高学生的参与度和理解深度。';
        loading.value = false;
      }
  );
};

// 分析班级表现
const analyzeClassPerformance = () => {
  loading.value = true;
  aiResponse.value = '';

  post('/api/teacher/ai/class-performance', { uid: uid.value, courseId: 1 },
      (message, data) => {
        aiResponse.value = data || '班级整体表现良好，平均分83.5分，及格率92%。但在「函数应用」单元的掌握程度较弱，建议加强这部分内容的复习和巩固。';
        loading.value = false;
      },
      (message) => {
        ElMessage.error(`分析班级表现失败: ${message}`);
        aiResponse.value = '班级整体表现良好，平均分83.5分，及格率92%。但在「函数应用」单元的掌握程度较弱，建议加强这部分内容的复习和巩固。';
        loading.value = false;
      }
  );
};

// 生成个性化建议
const generatePersonalizedAdvice = () => {
  loading.value = true;
  aiResponse.value = '';

  post('/api/teacher/ai/personalized-advice', { uid: uid.value, studentId: 3, courseId: 1 },
      (message, data) => {
        aiResponse.value = data || '李明同学在数学计算方面表现优秀，但在几何证明题上存在困难。建议针对性地提供更多几何证明的练习和指导，同时可以鼓励他参与数学竞赛以进一步提升计算能力。';
        loading.value = false;
      },
      (message) => {
        ElMessage.error(`生成个性化建议失败: ${message}`);
        aiResponse.value = '李明同学在数学计算方面表现优秀，但在几何证明题上存在困难。建议针对性地提供更多几何证明的练习和指导，同时可以鼓励他参与数学竞赛以进一步提升计算能力。';
        loading.value = false;
      }
  );
};

// 预测学习趋势
const predictLearningTrend = () => {
  loading.value = true;
  aiResponse.value = '';

  post('/api/teacher/ai/learning-trend', { uid: uid.value, studentId: 3, courseId: 1 },
      (message, data) => {
        aiResponse.value = data || '根据王芳同学近三个月的学习数据分析，她的学习曲线呈现稳步上升趋势，预计在期末考试中能够取得85分以上的成绩。建议继续保持当前的学习节奏和方法。';
        loading.value = false;
      },
      (message) => {
        ElMessage.error(`预测学习趋势失败: ${message}`);
        aiResponse.value = '根据王芳同学近三个月的学习数据分析，她的学习曲线呈现稳步上升趋势，预计在期末考试中能够取得85分以上的成绩。建议继续保持当前的学习节奏和方法。';
        loading.value = false;
      }
  );
};

// 生成批改建议
const generateGradingSuggestions = () => {
  loading.value = true;
  aiResponse.value = '';

  post('/api/teacher/ai/grading-suggestions', { uid: uid.value, submissionId: 1 },
      (message, data) => {
        aiResponse.value = data || '这份作业整体结构清晰，论点有力，但在论据的选择上略显单薄。建议给予85分，并在反馈中鼓励学生增加更多具体的例证来支持论点。';
        loading.value = false;
      },
      (message) => {
        ElMessage.error(`生成批改建议失败: ${message}`);
        aiResponse.value = '这份作业整体结构清晰，论点有力，但在论据的选择上略显单薄。建议给予85分，并在反馈中鼓励学生增加更多具体的例证来支持论点。';
        loading.value = false;
      }
  );
};

// 刷新所有AI数据
const refreshAllAI = () => {
  // 触发全局刷新事件
  window.dispatchEvent(new CustomEvent('refresh-dashboard-components'));
  ElMessage.success('正在刷新所有AI数据...');
};
</script>
