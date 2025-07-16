<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import ContentContainer from '../components/common/ContentContainer.vue';

const router = useRouter();
const showLayoutGuide = ref(false);

const navigateTo = (path) => {
  router.push(path);
};

const toggleLayoutGuide = () => {
  showLayoutGuide.value = !showLayoutGuide.value;
};
</script>

<template>
  <ContentContainer width="wide">
    <div class="teacher-home">
      <h1 class="text-2xl font-bold mb-6">教师平台</h1>
      
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <!-- 教师工作台卡片 -->
        <div class="card cursor-pointer" @click="navigateTo('/teacher/dashboard')">
          <div class="card-icon bg-blue-100 text-blue-500">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-8 h-8">
              <path stroke-linecap="round" stroke-linejoin="round" d="M3.75 6A2.25 2.25 0 016 3.75h2.25A2.25 2.25 0 0110.5 6v2.25a2.25 2.25 0 01-2.25 2.25H6a2.25 2.25 0 01-2.25-2.25V6zM3.75 15.75A2.25 2.25 0 016 13.5h2.25a2.25 2.25 0 012.25 2.25V18a2.25 2.25 0 01-2.25 2.25H6A2.25 2.25 0 013.75 18v-2.25zM13.5 6a2.25 2.25 0 012.25-2.25H18A2.25 2.25 0 0120.25 6v2.25A2.25 2.25 0 0118 10.5h-2.25a2.25 2.25 0 01-2.25-2.25V6zM13.5 15.75a2.25 2.25 0 012.25-2.25H18a2.25 2.25 0 012.25 2.25V18A2.25 2.25 0 0118 20.25h-2.25A2.25 2.25 0 0113.5 18v-2.25z" />
            </svg>
          </div>
          <h2 class="text-lg font-medium mt-4">教师工作台</h2>
          <p class="text-gray-500 mt-2">查看课程概况、任务和通知</p>
        </div>
        
        <!-- 智能备课卡片 -->
        <div class="card cursor-pointer" @click="navigateTo('/teacher/lesson-preparation-module/workbench')">
          <div class="card-icon bg-green-100 text-green-500">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-8 h-8">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 18v-5.25m0 0a6.01 6.01 0 001.5-.189m-1.5.189a6.01 6.01 0 01-1.5-.189m3.75 7.478a12.06 12.06 0 01-4.5 0m3.75 2.383a14.406 14.406 0 01-3 0M14.25 18v-.192c0-.983.658-1.823 1.508-2.316a7.5 7.5 0 10-7.517 0c.85.493 1.509 1.333 1.509 2.316V18" />
            </svg>
          </div>
          <h2 class="text-lg font-medium mt-4">智能备课</h2>
          <p class="text-gray-500 mt-2">使用AI助手快速生成教案</p>
        </div>
        
        <!-- 学情分析卡片 -->
        <div class="card cursor-pointer" @click="navigateTo('/teacher/analytics-module/analysis')">
          <div class="card-icon bg-purple-100 text-purple-500">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-8 h-8">
              <path stroke-linecap="round" stroke-linejoin="round" d="M7.5 14.25v2.25m3-4.5v4.5m3-6.75v6.75m3-9v9M6 20.25h12A2.25 2.25 0 0020.25 18V6A2.25 2.25 0 0018 3.75H6A2.25 2.25 0 003.75 6v12A2.25 2.25 0 006 20.25z" />
            </svg>
          </div>
          <h2 class="text-lg font-medium mt-4">学情分析</h2>
          <p class="text-gray-500 mt-2">查看学生成绩和学习表现</p>
        </div>
        
        <!-- 开发者资源卡片 -->
        <div class="card cursor-pointer" @click="toggleLayoutGuide">
          <div class="card-icon bg-orange-100 text-orange-500">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-8 h-8">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 6.042A8.967 8.967 0 006 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 016 18c2.305 0 4.408.867 6 2.292m0-14.25a8.966 8.966 0 016-2.292c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0018 18c-2.305 0-4.408.867-6 2.292m0-14.25v14.25" />
            </svg>
          </div>
          <h2 class="text-lg font-medium mt-4">开发者资源</h2>
          <p class="text-gray-500 mt-2">查看组件和布局指南</p>
        </div>
      </div>
      
      <!-- 布局指南对话框 -->
      <div v-if="showLayoutGuide" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white rounded-lg max-w-2xl w-full max-h-[80vh] overflow-auto p-6">
          <div class="flex justify-between items-center mb-4">
            <h2 class="text-xl font-bold">页面布局指南</h2>
            <button @click="toggleLayoutGuide" class="text-gray-500 hover:text-gray-700">
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-6 h-6">
                <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
          
          <div class="prose">
            <h3>内容容器组件</h3>
            <p>使用<code>ContentContainer</code>组件包装页面内容，确保一致的宽度和间距。</p>
            
            <pre><code>&lt;ContentContainer width="wide"&gt;
  &lt;!-- 页面内容 --&gt;
&lt;/ContentContainer&gt;</code></pre>
            
            <h3>宽度类型</h3>
            <ul>
              <li><strong>full</strong>: 100%宽度</li>
              <li><strong>wide</strong>: 85%宽度，最大1440px（推荐用于数据展示页面）</li>
              <li><strong>narrow</strong>: 65%宽度，最大1200px（推荐用于表单和详情页面）</li>
              <li><strong>fixed</strong>: 固定宽度，通过fixedWidth属性设置</li>
            </ul>
            
            <p>完整的布局指南请查看：<code>docs/layout-guide.md</code></p>
            
            <h3>组件库</h3>
            <p>我们提供了一系列UI组件，位于<code>@/components/common</code>目录：</p>
            <ul>
              <li>Button - 按钮组件</li>
              <li>Card - 卡片组件</li>
              <li>Input - 输入框组件</li>
              <li>Alert - 提示组件</li>
              <li>Badge - 徽章组件</li>
              <li>Tabs - 标签页组件</li>
              <li>ContentContainer - 内容容器组件</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </ContentContainer>
</template>

<style scoped>
.teacher-home {
  @apply pb-10;
}

.card {
  @apply bg-white rounded-xl p-6 shadow-sm border border-gray-200 transition-all duration-200;
}

.card:hover {
  @apply shadow-md transform -translate-y-1;
}

.card-icon {
  @apply w-16 h-16 rounded-full flex items-center justify-center;
}
</style>