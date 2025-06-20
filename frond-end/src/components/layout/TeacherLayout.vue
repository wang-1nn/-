<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/counter.js';
import LucideIcon from '@/components/icons/LucideIcon.vue';

const router = useRouter();
const auth = useAuthStore();
const isSidebarOpen = ref(true);

// 检查本地存储的侧边栏状态
onMounted(() => {
  const savedState = localStorage.getItem('teacherSidebarOpen');
  if (savedState !== null) {
    isSidebarOpen.value = savedState === 'true';
  }
});

// 用户信息
const userInfo = computed(() => auth.user);

// 用户名
const username = computed(() => {
  return userInfo.value ? userInfo.value.username : '教师';
});

// 侧边栏切换
const toggleSidebar = () => {
  isSidebarOpen.value = !isSidebarOpen.value;
  localStorage.setItem('teacherSidebarOpen', isSidebarOpen.value);
};

// 退出登录
const logout = () => {
  auth.logout();
  router.push('/login');
};

// 菜单项
const menuItems = [
  { path: '/teacher/dashboard', name: '工作台', icon: 'layout-dashboard' },
  { path: '/teacher/courses', name: '课程管理', icon: 'book-open' },
  // { path: '/teacher/classes', name: '班级管理', icon: 'school' },
  // { path: '/teacher/students', name: '学生管理', icon: 'users' },
  // { path: '/teacher/teaching-plans', name: '教学计划', icon: 'clipboard' },
  // { path: '/teacher/resources', name: '教学资源', icon: 'folder' },
  // { path: '/teacher/exams', name: '考试管理', icon: 'file-text' },
  // { path: '/teacher/questions', name: '题库管理', icon: 'help-circle' },
  // { path: '/teacher/scores', name: '成绩分析', icon: 'bar-chart-2' },
  { path: '/teacher/ai-assistant', name: '智能助手', icon: 'brain' }
];
</script>

<template>
  <div class="app-layout">
    <!-- 顶部导航栏 -->
    <header class="navbar">
      <div class="flex items-center space-x-2">
        <button @click="toggleSidebar" class="p-2 rounded-lg hover:bg-gray-100 lg:hidden">
          <LucideIcon name="menu" />
        </button>
        <div class="logo-container">
          <h1 class="logo">EduBrain</h1>
          <span class="text-xs bg-blue-100 text-blue-800 px-2 py-0.5 rounded">教师</span>
        </div>
      </div>
      
      <div class="flex items-center space-x-4">
        <div class="text-sm text-gray-600">欢迎, {{ username }}</div>
        <button @click="logout" class="btn-outline text-sm px-3 py-1 flex items-center">
          <LucideIcon name="log-out" size="16" class="mr-1" />
          退出登录
        </button>
      </div>
    </header>
    
    <div class="flex">
      <!-- 侧边栏 -->
      <aside 
        class="sidebar transition-all duration-300"
        :class="{ 'sidebar-collapsed': !isSidebarOpen }"
      >
        <div class="sidebar-header flex items-center justify-between px-4 py-3">
          <h2 
            class="text-lg font-semibold text-gray-800 transition-opacity duration-300"
            :class="{ 'opacity-0': !isSidebarOpen }"
          >
            教师工作台
          </h2>
          <button 
            @click="toggleSidebar" 
            class="p-1.5 rounded-lg hover:bg-gray-100 hidden lg:block"
            :title="isSidebarOpen ? '收起菜单' : '展开菜单'"
          >
            <LucideIcon 
              :name="isSidebarOpen ? 'chevron-left' : 'chevron-right'" 
              size="18" 
            />
          </button>
        </div>
        
        <nav class="sidebar-menu">
          <router-link 
            v-for="item in menuItems" 
            :key="item.path" 
            :to="item.path" 
            class="sidebar-menu-item group" 
            active-class="active"
          >
            <span class="sidebar-menu-icon">
              <LucideIcon :name="item.icon" :size="isSidebarOpen ? 18 : 20" />
            </span>
            <span 
              class="transition-opacity duration-300"
              :class="{ 'opacity-0 w-0': !isSidebarOpen }"
            >
              {{ item.name }}
            </span>
            
            <!-- 悬浮提示 -->
            <div 
              v-if="!isSidebarOpen" 
              class="tooltip-text opacity-0 group-hover:opacity-100"
            >
              {{ item.name }}
            </div>
          </router-link>
        </nav>
      </aside>
      
      <!-- 主内容区 -->
      <main 
        class="main-content transition-all duration-300"
        :class="{ 'main-content-expanded': !isSidebarOpen }"
      >
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
.sidebar-collapsed {
  width: 60px;
}

.main-content-expanded {
  margin-left: 60px;
}

.tooltip-text {
  position: absolute;
  left: 100%;
  top: 50%;
  transform: translateY(-50%);
  background-color: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 5px 8px;
  border-radius: 4px;
  font-size: 12px;
  white-space: nowrap;
  margin-left: 10px;
  pointer-events: none;
  z-index: 100;
  transition: opacity 0.15s ease;
}

.tooltip-text::before {
  content: '';
  position: absolute;
  top: 50%;
  right: 100%;
  margin-top: -5px;
  border-width: 5px;
  border-style: solid;
  border-color: transparent rgba(0, 0, 0, 0.8) transparent transparent;
}
</style> 