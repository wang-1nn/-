<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/counter.js';
import LucideIcon from '@/components/icons/LucideIcon.vue';
import FloatingAssistant from '@/components/FloatingAssistant.vue';

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();
const isSidebarOpen = ref(true);
const showMobileMenu = ref(false);

// 用户信息
const userInfo = computed(() => auth.user);

// 用户名
const username = computed(() => {
  return userInfo.value ? userInfo.value.username : '学生';
});

// 侧边栏切换
const toggleSidebar = () => {
  isSidebarOpen.value = !isSidebarOpen.value;
  localStorage.setItem('studentSidebarState', isSidebarOpen.value ? 'open' : 'closed');
};

// 移动端菜单切换
const toggleMobileMenu = () => {
  showMobileMenu.value = !showMobileMenu.value;
};

// 当前活跃路由
const isActive = (path) => {
  return route.path.startsWith(path);
};

// 退出登录
const logout = () => {
  auth.logout();
  router.push('/login');
};

// 从本地存储加载侧边栏状态
onMounted(() => {
  const savedState = localStorage.getItem('studentSidebarState');
  if (savedState) {
    isSidebarOpen.value = savedState === 'open';
  }
});

// 导航项定义
const navItems = [
  {
    name: '学习主页',
    path: '/student/dashboard',
    icon: 'layout-dashboard'
  },
  {
    name: '我的课程',
    path: '/student/courses',
    icon: 'book-open'
  },
  {
    name: '学习任务',
    path: '/student/tasks',
    icon: 'clipboard-list'
  },
  {
    name: '实时练习',
    path: '/student/practice',
    icon: 'zap'
  },
  {
    name: '学习资源',
    path: '/student/resources',
    icon: 'folder'
  },
  {
    name: '我的考试',
    path: '/student/exams',
    icon: 'file-text'
  },
  {
    name: '成绩查询',
    path: '/student/scores',
    icon: 'bar-chart-2'
  },
  {
    name: '错题本',
    path: '/student/mistakes',
    icon: 'x-circle'
  },
  {
    name: '智能助手',
    path: '/student/ai-assistant',
    icon: 'brain'
  },
  // 学习笔记功能已移除
];
</script>

<template>
  <div class="app-layout">
    <!-- 顶部导航栏 -->
    <header class="navbar bg-white shadow-sm px-4 py-2 sticky top-0 z-50">
      <div class="flex items-center justify-between w-full">
        <div class="flex items-center">
          <!-- 侧边栏切换按钮 -->
          <button @click="toggleSidebar" class="p-2 rounded-md text-gray-600 hover:bg-gray-100 focus:outline-none mr-2 hidden md:block">
            <LucideIcon name="menu" size="20" />
          </button>
          
          <!-- 移动端菜单按钮 -->
          <button @click="toggleMobileMenu" class="p-2 rounded-md text-gray-600 hover:bg-gray-100 focus:outline-none mr-2 md:hidden">
            <LucideIcon name="menu" size="20" />
          </button>
          
          <div class="logo-container">
            <h1 class="logo text-xl font-bold text-indigo-600">EduBrain</h1>
            <span class="text-xs bg-green-100 text-green-800 px-2 py-0.5 rounded">学生</span>
          </div>
        </div>
        
        <div class="flex items-center space-x-4">
          <!-- 通知图标 -->
          <button class="p-2 rounded-full hover:bg-gray-100 relative">
            <LucideIcon name="bell" size="20" class="text-gray-600" />
            <span class="absolute top-0 right-0 h-2 w-2 rounded-full bg-red-500"></span>
          </button>
          
          <div class="text-sm text-gray-600">欢迎, {{ username }}</div>
          <button @click="logout" class="btn-outline text-sm px-3 py-1 flex items-center">
            <LucideIcon name="log-out" size="16" class="mr-1" />
            退出登录
          </button>
        </div>
      </div>
    </header>
    
    <div class="flex">
      <!-- 侧边栏 - 桌面版 -->
      <aside 
        class="sidebar bg-white shadow-sm h-screen sticky top-0 transition-all duration-300 ease-in-out hidden md:block"
        :class="isSidebarOpen ? 'w-64' : 'w-20'"
      >
        <div class="sidebar-header flex items-center justify-between p-4 border-b border-gray-100">
          <h2 class="text-lg font-semibold text-gray-800" v-if="isSidebarOpen">学习中心</h2>
          <div v-else class="w-full flex justify-center">
            <LucideIcon name="school" class="text-indigo-600" size="24" />
          </div>
        </div>
        
        <nav class="p-3 space-y-1">
          <router-link 
            v-for="item in navItems" 
            :key="item.path"
            :to="item.path" 
            class="flex items-center px-3 py-2 rounded-md text-gray-700 hover:bg-indigo-50 hover:text-indigo-700 transition-colors duration-200 group"
            :class="{ 'bg-indigo-50 text-indigo-700': isActive(item.path) }"
          >
            <span class="sidebar-menu-icon flex-shrink-0">
              <LucideIcon :name="item.icon" :size="isSidebarOpen ? 18 : 20" />
            </span>
            <span v-if="isSidebarOpen" class="ml-3 text-sm">{{ item.name }}</span>
            
            <span 
              v-if="!isSidebarOpen" 
              class="absolute left-full ml-2 bg-gray-800 text-white text-xs px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity"
              style="pointer-events: none;"
            >
              {{ item.name }}
            </span>
          </router-link>
        </nav>
      </aside>
      
      <!-- 移动端侧边菜单 -->
      <div 
        v-if="showMobileMenu"
        class="fixed inset-0 z-50 md:hidden"
      >
        <!-- 背景遮罩 -->
        <div class="fixed inset-0 bg-gray-600 bg-opacity-75" @click="toggleMobileMenu"></div>
        
        <!-- 侧边栏内容 -->
        <div class="relative flex flex-col w-64 max-w-xs bg-white h-full">
          <div class="sidebar-header flex items-center justify-between p-4 border-b border-gray-100">
            <h2 class="text-lg font-semibold text-gray-800">学习中心</h2>
            <button @click="toggleMobileMenu" class="text-gray-500 hover:text-gray-700">
              <LucideIcon name="x" size="20" />
            </button>
          </div>
          
          <nav class="p-3 space-y-1 overflow-y-auto">
            <router-link 
              v-for="item in navItems" 
              :key="item.path"
              :to="item.path" 
              class="flex items-center px-3 py-2 rounded-md text-gray-700 hover:bg-indigo-50 hover:text-indigo-700 transition-colors duration-200"
              :class="{ 'bg-indigo-50 text-indigo-700': isActive(item.path) }"
              @click="toggleMobileMenu"
            >
              <span class="sidebar-menu-icon flex-shrink-0">
                <LucideIcon :name="item.icon" size="18" />
              </span>
              <span class="ml-3 text-sm">{{ item.name }}</span>
            </router-link>
          </nav>
        </div>
      </div>
      
      <!-- 主内容区 -->
      <main 
        class="main-content flex-grow p-6 bg-gray-50 overflow-x-hidden"
        :class="{ 'md:ml-20': !isSidebarOpen }"
      >
        <router-view />
        <FloatingAssistant />
      </main>
    </div>
  </div>
</template> 