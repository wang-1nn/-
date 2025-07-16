<template>
  <div class="lesson-prep-main">
    <!-- 顶部导航 -->
    <div class="bg-white shadow-sm border-b">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center py-4">
          <div class="flex items-center space-x-4">
            <h1 class="text-2xl font-bold text-gray-900">智能备课工作台</h1>
            <div class="flex space-x-1">
              <button
                  v-for="tab in tabs"
                  :key="tab.key"
                  @click="activeTab = tab.key"
                  :class="[
                  'px-4 py-2 text-sm font-medium rounded-lg transition-colors',
                  activeTab === tab.key
                    ? 'bg-blue-100 text-blue-700'
                    : 'text-gray-500 hover:text-gray-700 hover:bg-gray-100'
                ]"
              >
                <el-icon class="mr-1">
                  <component :is="tab.icon" />
                </el-icon>
                {{ tab.label }}
              </button>
            </div>
          </div>

          <div class="flex items-center space-x-3">
            <!-- 快速操作 -->
            <el-button @click="createNewLesson" type="primary" size="default">
              <el-icon><Plus /></el-icon>
              新建教案
            </el-button>

            <el-dropdown @command="handleQuickAction">
              <el-button size="default">
                快速操作
                <el-icon class="ml-1"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="import-outline">
                    <el-icon><Upload /></el-icon>
                    导入大纲
                  </el-dropdown-item>
                  <el-dropdown-item command="template-center">
                    <el-icon><Collection /></el-icon>
                    模板中心
                  </el-dropdown-item>
                  <el-dropdown-item command="resource-library">
                    <el-icon><FolderOpened /></el-icon>
                    资源库
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
      <!-- 工作台视图 -->
      <div v-if="activeTab === 'workbench'" class="workbench-container">
        <LessonPrepWorkbenchNew
            @lesson-plan-created="handleLessonPlanCreated"
        />
      </div>

      <!-- 历史记录视图 -->
      <div v-else-if="activeTab === 'history'" class="history-container">
        <LessonPlanHistory
            @edit-lesson-plan="handleEditLessonPlan"
        />
      </div>

      <!-- 模板管理视图 -->
      <div v-else-if="activeTab === 'templates'" class="templates-container">
        <div class="text-center py-12">
          <el-icon size="48" class="text-gray-400 mb-4"><Collection /></el-icon>
          <h3 class="text-lg font-medium text-gray-900 mb-2">模板管理</h3>
          <p class="text-gray-500 mb-4">管理您的教案模板</p>
          <el-button type="primary">创建模板</el-button>
        </div>
      </div>

      <!-- 资源库视图 -->
      <div v-else-if="activeTab === 'resources'" class="resources-container">
        <div class="text-center py-12">
          <el-icon size="48" class="text-gray-400 mb-4"><FolderOpened /></el-icon>
          <h3 class="text-lg font-medium text-gray-900 mb-2">教学资源库</h3>
          <p class="text-gray-500 mb-4">浏览和管理教学资源</p>
          <el-button type="primary">上传资源</el-button>
        </div>
      </div>

      <!-- API测试视图 -->
      <div v-else-if="activeTab === 'test'" class="test-container">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <ApiConnectionTest />
          <ApiTestPanel />
        </div>
      </div>
    </div>

    <!-- 快速开始引导 -->
    <div v-if="showQuickStart" class="fixed bottom-6 right-6 z-50">
      <div class="bg-white rounded-lg shadow-lg border p-4 max-w-sm">
        <div class="flex items-start">
          <div class="flex-shrink-0">
            <el-icon class="text-blue-500" size="20"><InfoFilled /></el-icon>
          </div>
          <div class="ml-3 flex-1">
            <h4 class="text-sm font-medium text-gray-900">快速开始</h4>
            <p class="text-sm text-gray-500 mt-1">
              上传教学大纲，让AI帮您生成专业的教案
            </p>
            <div class="mt-3 flex space-x-2">
              <el-button @click="createNewLesson" size="small" type="primary">
                开始创建
              </el-button>
              <el-button @click="showQuickStart = false" size="small">
                关闭
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import {
  Plus, ArrowDown, Upload, Collection, FolderOpened,
  InfoFilled, Document, Clock, Setting
} from '@element-plus/icons-vue';
import LessonPrepWorkbenchNew from './components/LessonPrepWorkbenchNew.vue';
import LessonPlanHistory from './components/LessonPlanHistory.vue';
import ApiTestPanel from './components/ApiTestPanel.vue';
import ApiConnectionTest from './components/ApiConnectionTest.vue';

// 状态管理
const activeTab = ref('workbench');
const showQuickStart = ref(false);

// 标签页配置
const tabs = [
  { key: 'workbench', label: '智能工作台', icon: 'Setting' },
  { key: 'history', label: '教案历史', icon: 'Document' },
  { key: 'templates', label: '模板管理', icon: 'Collection' },
  { key: 'resources', label: '资源库', icon: 'FolderOpened' },
  { key: 'test', label: 'API测试', icon: 'Tools' }
];

// 创建新教案
const createNewLesson = () => {
  activeTab.value = 'workbench';
  ElMessage.info('开始创建新教案');
};

// 处理快速操作
const handleQuickAction = (command) => {
  switch (command) {
    case 'import-outline':
      activeTab.value = 'workbench';
      ElMessage.info('请在工作台中上传教学大纲');
      break;
    case 'template-center':
      activeTab.value = 'templates';
      break;
    case 'resource-library':
      activeTab.value = 'resources';
      break;
  }
};

// 处理教案创建完成
const handleLessonPlanCreated = (lessonPlan) => {
  ElMessage.success('教案创建成功！');
  // 可以切换到历史记录查看
  setTimeout(() => {
    activeTab.value = 'history';
  }, 1000);
};

// 处理编辑教案
const handleEditLessonPlan = (lessonPlan) => {
  // 切换到工作台并加载教案数据
  activeTab.value = 'workbench';
  ElMessage.info('正在加载教案到编辑器...');
  // 这里可以通过事件总线传递数据到工作台组件
};

// 检查是否显示快速开始引导
const checkQuickStart = () => {
  // 可以根据用户是否有教案记录来决定是否显示引导
  const hasShownGuide = localStorage.getItem('lesson-prep-guide-shown');
  if (!hasShownGuide) {
    showQuickStart.value = true;
    localStorage.setItem('lesson-prep-guide-shown', 'true');
  }
};

// 初始化
onMounted(() => {
  checkQuickStart();
});
</script>

<style scoped>
.lesson-prep-main {
  min-height: 100vh;
  background-color: #f8fafc;
}

.workbench-container,
.history-container,
.templates-container,
.resources-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.workbench-container {
  background: transparent;
  box-shadow: none;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .lesson-prep-main .max-w-7xl {
    padding-left: 1rem;
    padding-right: 1rem;
  }

  .tabs {
    flex-wrap: wrap;
    gap: 0.5rem;
  }

  .quick-actions {
    flex-direction: column;
    gap: 0.5rem;
  }
}

/* 动画效果 */
.workbench-container,
.history-container,
.templates-container,
.resources-container {
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
