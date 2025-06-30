<script setup>
import {onMounted, ref, watch} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import {useAuthStore} from '@/stores/counter';
import {ElMessage, ElMessageBox} from 'element-plus';

// 路由相关
const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

// 侧边栏状态
const collapsed = ref(false);
const toggleSidebar = () => {
  collapsed.value = !collapsed.value;
};

// 标签页相关
const activeTab = ref(route.path);
const openedTabs = ref([]);

// 初始化标签页
onMounted(() => {
  // 如果当前路由不是 dashboard，则添加 dashboard 作为首个标签
  if (route.path !== '/student/dashboard') {
    addTab('/student/dashboard', '学习中心');
  }
  
  // 添加当前路由标签
  addTab(route.path, route.meta.title || '未命名页面');
});

// 监听路由变化，动态添加标签页
watch(() => route.path, (newPath) => {
  const title = route.meta.title || '未命名页面';
  addTab(newPath, title);
  activeTab.value = newPath;
});

// 添加标签页
const addTab = (path, title) => {
  // 检查标签是否已存在，避免重复添加
  const isExists = openedTabs.value.some(tab => tab.path === path);
  if (!isExists) {
    openedTabs.value.push({
      path,
      title
    });
  }
};

// 关闭标签页
const closeTab = (targetPath) => {
  // 不能关闭最后一个标签
  if (openedTabs.value.length <= 1) return;
  
  // 获取要关闭的标签索引
  const targetIndex = openedTabs.value.findIndex(tab => tab.path === targetPath);
  
  // 如果关闭的是当前激活的标签，则需要激活相邻的标签
  if (activeTab.value === targetPath) {
    // 如果关闭的是最后一个标签，则激活前一个标签
    const nextActiveIndex = targetIndex === openedTabs.value.length - 1 
      ? targetIndex - 1 
      : targetIndex + 1;
    
    activeTab.value = openedTabs.value[nextActiveIndex].path;
    router.push(activeTab.value);
  }
  
  // 移除标签
  openedTabs.value.splice(targetIndex, 1);
};

// 切换标签
const switchTab = (path) => {
  activeTab.value = path;
  router.push(path);
};

// 学生菜单
const menuItems = [
  {
    title: '学习中心',
    icon: 'el-icon-house',
    children: [
      { title: '学习首页', path: '/student/dashboard', icon: 'el-icon-monitor' },
      { title: '智能助手', path: '/student/ai-assistant', icon: 'el-icon-chat-dot-round' }
    ]
  },
  {
    title: '课程学习',
    icon: 'el-icon-reading',
    children: [
      { title: '我的课程', path: '/student/courses', icon: 'el-icon-notebook-1' },
      { title: '学习资源', path: '/student/resources', icon: 'el-icon-document' },
    ]
  },
  {
    title: '测验与练习',
    icon: 'el-icon-edit-outline',
    children: [
      { title: '实时练习', path: '/student/practice', icon: 'el-icon-coordinate' },
      { title: '我的考核', path: '/student/exams', icon: 'el-icon-document-checked' },
      { title: '错题本', path: '/student/mistakes', icon: 'el-icon-warning-outline' }
    ]
  },
  {
    title: '个人中心',
    icon: 'el-icon-user',
    children: [
      { title: '学习记录', path: '/student/tasks', icon: 'el-icon-time' },
      { title: '我的成绩', path: '/student/scores', icon: 'el-icon-data-analysis' },
    ]
  }
];

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    authStore.logout();
    router.push('/login');
    ElMessage.success('退出成功');
  }).catch(() => {});
};

// 用户下拉菜单
const userDropdownVisible = ref(false);
const toggleUserDropdown = () => {
  userDropdownVisible.value = !userDropdownVisible.value;
};
</script>

<template>
  <div class="app-container">
    <!-- 侧边栏 -->
    <aside :class="['sidebar', collapsed ? 'sidebar-collapsed' : '']">
      <div class="sidebar-header">
        <div class="logo-container">
          <img src="@/assets/logo.svg" alt="Logo" class="logo" />
          <span v-show="!collapsed" class="logo-text">智慧学习平台</span>
        </div>
        <button @click="toggleSidebar" class="collapse-btn">
          <i :class="collapsed ? 'el-icon-d-arrow-right' : 'el-icon-d-arrow-left'"></i>
        </button>
      </div>

      <div class="sidebar-menu">
        <el-menu
          :default-active="activeTab"
          :collapse="collapsed"
          :unique-opened="true"
          router
          class="el-menu-vertical"
          background-color="transparent"
          text-color="rgba(255, 255, 255, 0.85)"
          active-text-color="#ffffff"
        >
          <template v-for="(menuGroup, index) in menuItems">
            <el-sub-menu :index="String(index)" :key="`menu-group-${index}`">
              <template #title>
                <i :class="menuGroup.icon"></i>
                <span>{{ menuGroup.title }}</span>
              </template>
              <el-menu-item 
                v-for="(menuItem, idx) in menuGroup.children"
                :key="`${index}-${idx}`"
                :index="menuItem.path"
                @click="addTab(menuItem.path, menuItem.title)"
              >
                <i :class="menuItem.icon"></i>
                <span>{{ menuItem.title }}</span>
              </el-menu-item>
            </el-sub-menu>
          </template>
        </el-menu>
      </div>
    </aside>

    <!-- 主内容区 -->
    <div class="main-container">
      <!-- 顶部导航 -->
      <header class="app-header">
        <div class="header-left">
          <div class="breadcrumb">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/student/dashboard' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item>{{ route.meta.title || '未命名页面' }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
        </div>
        
        <div class="header-right">
          <div class="header-search">
            <el-input
              placeholder="搜索学习资源..."
              prefix-icon="el-icon-search"
              size="small"
            />
          </div>
          
          <div class="header-actions">
            <el-badge :value="2" class="notification-badge">
              <el-button icon="el-icon-bell" circle></el-button>
            </el-badge>
            
            <div class="user-dropdown relative">
              <div class="user-info cursor-pointer flex items-center" @click="toggleUserDropdown">
                <img 
                  src="https://i.pravatar.cc/100?img=5" 
                  alt="用户头像" 
                  class="user-avatar"
                />
                <span class="user-name ml-2">{{ authStore.user?.name || '学生用户' }}</span>
                <i class="el-icon-caret-bottom ml-1"></i>
              </div>
              
              <div v-show="userDropdownVisible" class="user-dropdown-menu">
                <div class="dropdown-item" @click="router.push('/student/profile')">
                  <i class="el-icon-user"></i>
                  <span>个人中心</span>
                </div>
                <div class="dropdown-item" @click="router.push('/student/settings')">
                  <i class="el-icon-setting"></i>
                  <span>账号设置</span>
                </div>
                <div class="dropdown-divider"></div>
                <div class="dropdown-item text-red-500" @click="handleLogout">
                  <i class="el-icon-switch-button"></i>
                  <span>退出登录</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </header>

      <!-- 标签页导航 -->
      <div class="tabs-container">
        <div class="tabs-nav">
          <div 
            v-for="tab in openedTabs"
            :key="tab.path"
            :class="['tab-item', { active: activeTab === tab.path }]"
            @click="switchTab(tab.path)"
          >
            <span class="tab-title">{{ tab.title }}</span>
            <span 
              v-if="openedTabs.length > 1" 
              class="tab-close"
              @click.stop="closeTab(tab.path)"
            >
              <i class="el-icon-close"></i>
            </span>
          </div>
        </div>
        <div class="tabs-actions">
          <el-dropdown trigger="click">
            <span class="el-dropdown-link cursor-pointer">
              <i class="el-icon-arrow-down"></i>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>关闭其他标签页</el-dropdown-item>
                <el-dropdown-item>关闭所有标签页</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 内容区域 -->
      <main class="page-container">
        <router-view v-slot="{ Component }">
          <keep-alive>
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </main>
    </div>
  </div>
</template>

<style scoped>
/* 整体布局 */
.app-container {
  @apply flex min-h-screen bg-gray-50;
}

/* 侧边栏样式 */
.sidebar {
  @apply flex flex-col transition-all duration-300 ease-in-out;
  height: 100vh;
  position: fixed;
  z-index: 1001;
  width: 240px;
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.1);
  background: linear-gradient(135deg, #134e5e 0%, #71b280 100%);
}

.sidebar-collapsed {
  width: 64px;
}

.sidebar-header {
  @apply flex items-center justify-between p-4 border-b border-opacity-20;
  height: 64px;
  border-color: rgba(255, 255, 255, 0.1);
}

.logo-container {
  @apply flex items-center;
  overflow: hidden;
}

.logo {
  @apply w-8 h-8;
  min-width: 32px;
  filter: brightness(0) invert(1);
}

.logo-text {
  @apply ml-3 font-semibold text-lg whitespace-nowrap text-white;
  transition: opacity 0.3s;
}

.collapse-btn {
  @apply p-2 rounded-full text-white opacity-70 hover:opacity-100 hover:bg-white hover:bg-opacity-10 transition-all duration-200;
}

/* 菜单样式覆盖 */
.el-menu {
  border-right: none !important;
}

.el-menu-vertical:not(.el-menu--collapse) {
  width: 100%;
}

/* 自定义菜单项样式 */
:deep(.el-submenu__title) {
  @apply transition-all duration-200;
}

:deep(.el-submenu__title:hover) {
  background-color: rgba(255, 255, 255, 0.1) !important;
}

:deep(.el-menu-item) {
  @apply transition-all duration-200;
}

:deep(.el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.1) !important;
}

:deep(.el-menu-item.is-active) {
  background-color: rgba(255, 255, 255, 0.2) !important;
  position: relative;
}

:deep(.el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 4px;
  background-color: #ffffff;
  border-radius: 0 2px 2px 0;
}

/* 主容器样式 */
.main-container {
  @apply flex-1 flex flex-col ml-60 transition-all duration-300 ease-in-out;
}

.sidebar-collapsed + .main-container {
  @apply ml-20;
}

/* 头部导航样式 */
.app-header {
  @apply flex justify-between items-center px-6 bg-white shadow-sm;
  height: 64px;
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-left, .header-right {
  @apply flex items-center;
}

.header-search {
  @apply mr-6 w-56;
}

.header-actions {
  @apply flex items-center space-x-4;
}

.notification-badge {
  margin-right: 8px;
}

.user-avatar {
  @apply w-10 h-10 rounded-full border-2 border-gray-200;
}

.user-name {
  @apply hidden md:block text-sm font-medium;
}

.user-dropdown {
  @apply relative;
}

.user-dropdown-menu {
  @apply absolute right-0 top-full mt-2 w-48 bg-white rounded-md shadow-lg py-2 z-10;
}

.dropdown-item {
  @apply flex items-center px-4 py-2 hover:bg-gray-100 cursor-pointer;
}

.dropdown-divider {
  @apply my-2 border-t border-gray-100;
}

/* 标签页导航 */
.tabs-container {
  @apply flex justify-between items-center px-2 bg-white border-b border-gray-200;
  height: 40px;
}

.tabs-nav {
  @apply flex-1 flex items-center overflow-x-auto;
  scrollbar-width: none;
}

.tabs-nav::-webkit-scrollbar {
  display: none;
}

.tab-item {
  @apply flex items-center px-3 py-1 mx-1 text-sm text-gray-700 rounded-md cursor-pointer;
  height: 32px;
  transition: all 0.2s;
}

.tab-item:hover {
  @apply bg-gray-100;
}

.tab-item.active {
  @apply bg-purple-50 text-purple-600;
}

.tab-title {
  max-width: 120px;
  @apply truncate mr-2;
}

.tab-close {
  @apply opacity-0 group-hover:opacity-100 hover:bg-red-100 hover:text-red-600 rounded-full p-1;
}

.tab-item:hover .tab-close {
  @apply opacity-100;
}

.tabs-actions {
  @apply px-2;
}

/* 内容区域 */
.page-container {
  @apply p-6 flex-1 overflow-auto;
}
</style> 