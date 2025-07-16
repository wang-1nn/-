<template>
  <div class="outline-preview-container">
    <!-- 标签页导航 -->
    <el-tabs v-model="activeView" class="outline-tabs">
      <el-tab-pane label="思维导图" name="mindmap">
        <div class="h-full flex items-center justify-center bg-white rounded-lg shadow-inner p-2">
          <div v-if="loading" class="flex flex-col items-center justify-center h-64">
            <el-icon class="text-3xl text-indigo-500 mb-4"><Loading /></el-icon>
            <p class="text-slate-500">加载思维导图中...</p>
          </div>
          <div v-else-if="!outline.length" class="flex flex-col items-center justify-center h-64">
            <el-icon class="text-5xl text-slate-300 mb-4"><Document /></el-icon>
            <p class="text-slate-500">请先导入或选择大纲</p>
          </div>
          <MindMapView v-else :outline="outline" class="w-full h-96" />
        </div>
      </el-tab-pane>
      <el-tab-pane label="大纲文本" name="text">
        <div class="h-full bg-white rounded-lg shadow-inner p-6 overflow-y-auto">
          <div v-if="loading" class="flex flex-col items-center justify-center h-64">
            <el-icon class="text-3xl text-indigo-500 mb-4"><Loading /></el-icon>
            <p class="text-slate-500">加载大纲文本中...</p>
          </div>
          <div v-else-if="!outline.length" class="flex flex-col items-center justify-center h-64">
            <el-icon class="text-5xl text-slate-300 mb-4"><Document /></el-icon>
            <p class="text-slate-500">请先导入或选择大纲</p>
          </div>
          <OutlineText v-else :outline="outline" />
        </div>
      </el-tab-pane>
      <el-tab-pane label="智能分析" name="analysis">
        <div class="h-full bg-white rounded-lg shadow-inner p-6">
          <div v-if="loading" class="flex flex-col items-center justify-center h-64">
            <el-icon class="text-3xl text-indigo-500 mb-4"><Loading /></el-icon>
            <p class="text-slate-500">分析大纲中...</p>
          </div>
          <div v-else-if="!outline.length" class="flex flex-col items-center justify-center h-64">
            <el-icon class="text-5xl text-slate-300 mb-4"><Document /></el-icon>
            <p class="text-slate-500">请先导入或选择大纲</p>
          </div>
          <div v-else class="outline-analysis">
            <h3 class="text-lg font-medium text-slate-800 mb-4">大纲智能分析</h3>
            
            <el-card class="mb-4">
              <template #header>
                <div class="flex items-center">
                  <el-icon class="mr-2 text-indigo-500"><DataAnalysis /></el-icon>
                  <span>结构分析</span>
                </div>
              </template>
              <div class="flex gap-4 text-center">
                <div class="flex-1 p-3 bg-blue-50 rounded">
                  <div class="text-2xl font-bold text-blue-600">{{ outlineStats.chapterCount }}</div>
                  <div class="text-sm text-slate-600">章节数</div>
                </div>
                <div class="flex-1 p-3 bg-green-50 rounded">
                  <div class="text-2xl font-bold text-green-600">{{ outlineStats.sectionCount }}</div>
                  <div class="text-sm text-slate-600">小节数</div>
                </div>
                <div class="flex-1 p-3 bg-purple-50 rounded">
                  <div class="text-2xl font-bold text-purple-600">{{ outlineStats.depth }}</div>
                  <div class="text-sm text-slate-600">最大深度</div>
                </div>
              </div>
            </el-card>
            
            <el-card class="mb-4">
              <template #header>
                <div class="flex items-center">
                  <el-icon class="mr-2 text-indigo-500"><Document /></el-icon>
                  <span>内容覆盖分析</span>
                </div>
              </template>
              <div class="flex flex-col gap-3">
                <div v-for="(item, index) in coverageAnalysis" :key="index">
                  <div class="flex justify-between mb-1">
                    <span>{{ item.name }}</span>
                    <span>{{ item.value }}%</span>
                  </div>
                  <el-progress :percentage="item.value" :color="item.color" :stroke-width="8" />
                </div>
              </div>
            </el-card>
            
            <el-card>
              <template #header>
                <div class="flex items-center">
                  <el-icon class="mr-2 text-indigo-500"><ChatLineRound /></el-icon>
                  <span>改进建议</span>
                </div>
              </template>
              <ul class="list-disc pl-5 text-slate-700 space-y-2">
                <li v-for="(suggestion, index) in outlineSuggestions" :key="index">{{ suggestion }}</li>
              </ul>
            </el-card>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 解析信息 (仅当有数据时显示) -->
    <div v-if="meta && Object.keys(meta).length" class="outline-meta mt-4 p-4 bg-white rounded-lg shadow">
      <div class="flex flex-wrap gap-4 text-sm">
        <div class="flex items-center">
          <el-icon class="mr-2 text-slate-400"><Document /></el-icon>
          <span class="text-slate-600">{{ meta.fileName }}</span>
        </div>
        <div class="flex items-center">
          <el-icon class="mr-2 text-slate-400"><Files /></el-icon>
          <span class="text-slate-600">{{ formatFileSize(meta.fileSize) }}</span>
        </div>
        <div class="flex items-center">
          <el-icon class="mr-2 text-slate-400"><Document /></el-icon>
          <span class="text-slate-600">{{ meta.pageCount }} 页</span>
        </div>
        <div class="flex items-center">
          <el-icon class="mr-2 text-slate-400"><Check /></el-icon>
          <span class="text-slate-600">解析质量: 
            <span :class="parseQualityClass">{{ meta.parseQuality }}%</span>
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { Loading, Document, DataAnalysis, ChatLineRound, Check, Files } from '@element-plus/icons-vue';
import MindMapView from '@/components/lesson-prep/MindMapView.vue';
import OutlineText from '@/components/lesson-prep/OutlineText.vue';

const props = defineProps({
  outline: {
    type: Array,
    default: () => []
  },
  meta: {
    type: Object,
    default: () => ({})
  },
  loading: {
    type: Boolean,
    default: false
  }
});

// 活动视图
const activeView = ref('mindmap');

// 解析质量的样式
const parseQualityClass = computed(() => {
  const quality = props.meta?.parseQuality || 0;
  if (quality >= 90) return 'text-green-500 font-medium';
  if (quality >= 70) return 'text-blue-500 font-medium';
  if (quality >= 50) return 'text-yellow-500 font-medium';
  return 'text-red-500 font-medium';
});

// 大纲统计数据
const outlineStats = computed(() => {
  if (!props.outline || !props.outline.length) {
    return { chapterCount: 0, sectionCount: 0, depth: 0 };
  }
  
  let chapterCount = props.outline.length;
  let sectionCount = 0;
  let maxDepth = 1;
  
  // 递归计算统计数据
  const calculateStats = (nodes, currentDepth = 1) => {
    if (!nodes || !nodes.length) return;
    
    for (const node of nodes) {
      if (node.children && node.children.length) {
        sectionCount += node.children.length;
        maxDepth = Math.max(maxDepth, currentDepth + 1);
        calculateStats(node.children, currentDepth + 1);
      }
    }
  };
  
  calculateStats(props.outline);
  
  return { 
    chapterCount,
    sectionCount, 
    depth: maxDepth
  };
});

// 内容覆盖分析
const coverageAnalysis = ref([
  { name: '学习目标', value: 85, color: '#6366f1' },
  { name: '教学重点', value: 92, color: '#10b981' },
  { name: '教学难点', value: 75, color: '#f97316' },
  { name: '教学活动', value: 63, color: '#8b5cf6' },
  { name: '评估方式', value: 45, color: '#ef4444' }
]);

// 大纲改进建议
const outlineSuggestions = ref([
  '考虑为教学难点增加更多支持性材料和练习',
  '评估方式部分需要补充多样化的评价方式',
  '建议增加与实际应用相关的案例或项目',
  '教学活动可以更加丰富，增加互动性和参与度',
  '可考虑添加学习资源和拓展阅读推荐'
]);

// 辅助函数：格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B';
  
  const units = ['B', 'KB', 'MB', 'GB'];
  let i = 0;
  while (bytes >= 1024 && i < units.length - 1) {
    bytes /= 1024;
    i++;
  }
  
  return `${bytes.toFixed(2)} ${units[i]}`;
};

// 根据大纲数据更新分析内容
watch(() => props.outline, (newOutline) => {
  if (newOutline && newOutline.length) {
    // 这里可以添加实际的大纲分析逻辑
    // 现在使用模拟数据，实际项目中可以根据大纲内容动态计算
    
    // 模拟动态更新分析数据
    coverageAnalysis.value = [
      { name: '学习目标', value: Math.floor(Math.random() * 30) + 70, color: '#6366f1' },
      { name: '教学重点', value: Math.floor(Math.random() * 20) + 80, color: '#10b981' },
      { name: '教学难点', value: Math.floor(Math.random() * 40) + 60, color: '#f97316' },
      { name: '教学活动', value: Math.floor(Math.random() * 50) + 50, color: '#8b5cf6' },
      { name: '评估方式', value: Math.floor(Math.random() * 60) + 40, color: '#ef4444' }
    ];
  }
}, { deep: true });

// 当组件挂载时，如果已有大纲数据，则初始化分析
onMounted(() => {
  if (props.outline && props.outline.length) {
    // 初始化分析
  }
});
</script>

<style scoped>
.outline-preview-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.outline-tabs {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.outline-tabs :deep(.el-tabs__content) {
  flex: 1;
  overflow: auto;
  padding: 10px 0;
}

.outline-tabs :deep(.el-tab-pane) {
  height: 100%;
}

/* 添加滚动条样式 */
.outline-preview-container :deep(::-webkit-scrollbar) {
  width: 6px;
  height: 6px;
}

.outline-preview-container :deep(::-webkit-scrollbar-thumb) {
  background: #a5b4fc;
  border-radius: 4px;
}

.outline-preview-container :deep(::-webkit-scrollbar-track) {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
}
</style>