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
const collapsed = ref(true);
const isHovering = ref(false);
// 悬停展开
const handleSidebarMouseEnter = () => {
  if (collapsed.value) {
    isHovering.value = true;
  }
};

const handleSidebarMouseLeave = () => {
  isHovering.value = false;
};

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
      { title: '我的课程', path: '/student/courses', icon: 'el-icon-notebook-1' },
      { title: '智能助手', path: '/student/ai-assistant', icon: 'el-icon-chat-dot-round' }
    ]
  },
  {
    title: '考试中心',
    icon: 'el-icon-edit-outline',
    children: [
      { title: '考试中心', path: '/student/exams', icon: 'el-icon-document-checked' }
    ]
  },
  {
    path: '/student/mistakes',
    title: '错题本',
    icon: 'el-icon-warning-outline'
  },
  {
    title: '个人中心',
    icon: 'el-icon-user',
    children: [
      { title: '我的成绩', path: '/student/scores', icon: 'el-icon-data-analysis' },
      { title: '学习轨迹', path: '/student/learning-timeline', icon: 'el-icon-time' },
      { title: '我的成就', path: '/student/achievements', icon: 'el-icon-medal' }
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
    <aside
      :class="[
        'sidebar',
        collapsed && !isHovering ? 'sidebar-collapsed' : 'sidebar-expanded'
      ]"
      @mouseenter="handleSidebarMouseEnter"
      @mouseleave="handleSidebarMouseLeave"
    >
      <div class="sidebar-header">
        <div class="logo-container">
          <img src="@/assets/logo.svg" alt="Logo" class="logo" />
          <span v-show="!collapsed || isHovering" class="logo-text">智慧学习平台</span>
        </div>
        <button @click="toggleSidebar" class="collapse-btn">
          <i :class="collapsed && !isHovering ? 'el-icon-d-arrow-right' : 'el-icon-d-arrow-left'"></i>
        </button>
      </div>
      <!-- 折叠时提示 -->
      <transition name="fade">
        <div v-if="collapsed && isHovering" class="hint px-4 pb-3 text-xs text-white/70">移动鼠标可快速浏览菜单</div>
      </transition>

      <div class="sidebar-menu">
        <el-menu
          :default-active="activeTab"
          :collapse="collapsed && !isHovering"
          :unique-opened="true"
          router
          class="el-menu-vertical"
          background-color="transparent"
          text-color="rgba(255, 255, 255, 0.85)"
          active-text-color="#ffffff"
        >
          <template v-for="(menuGroup, index) in menuItems" :key="'menu-group-' + index">
            <!-- 带子菜单的项目 -->
            <el-sub-menu v-if="menuGroup.children" :index="String(index)">
              <template #title>
                <i :class="menuGroup.icon"></i>
                <span>{{ menuGroup.title }}</span>
              </template>
              <el-menu-item 
                v-for="(menuItem, idx) in menuGroup.children"
                :key="index + '-' + idx"
                :index="menuItem.path"
                @click="addTab(menuItem.path, menuItem.title)"
              >
                <i :class="menuItem.icon"></i>
                <span>{{ menuItem.title }}</span>
              </el-menu-item>
            </el-sub-menu>
            
            <!-- 直接链接的项目 -->
            <el-menu-item 
              v-else 
              :index="menuGroup.path"
              @click="addTab(menuGroup.path, menuGroup.title)"
            >
              <i :class="menuGroup.icon"></i>
              <span>{{ menuGroup.title }}</span>
            </el-menu-item>
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
                  :src="authStore.user?.avatar || '/avatars/default-avatar.png'" 
                  alt="用户头像" 
                  class="user-avatar"
                />
                <span class="user-name ml-2">{{ authStore.user?.real_name || '学生用户' }}</span>
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
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #a78bfa 100%);
  border-radius: 0 16px 16px 0;
  overflow: hidden;
}

.sidebar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url("data:image/svg+xml,%3Csvg width='120' height='120' viewBox='0 0 120 120' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M9 0h2v20H9V0zm25.134.84l1.732 1-10 17.32-1.732-1 10-17.32zm-20 20l1.732 1-10 17.32-1.732-1 10-17.32zM58.16 4.134l1 1.732-17.32 10-1-1.732 17.32-10zm-40 40l1 1.732-17.32 10-1-1.732 17.32-10zM80 9v2H60V9h20zM20 69v2H0v-2h20zm79.32-55l-1 1.732-17.32-10L82 4l17.32 10zm-80 80l-1 1.732-17.32-10L2 84l17.32 10zm96.546-75.84l-1.732 1-10-17.32 1.732-1 10 17.32zm-100 100l-1.732 1-10-17.32 1.732-1 10 17.32zM38.16 24.134l1 1.732-17.32 10-1-1.732 17.32-10zM60 29v2H40v-2h20zm19.32 5l-1 1.732-17.32-10L62 24l17.32 10zm16.546 4.16l-1.732 1-10-17.32 1.732-1 10 17.32zM111 40h-2V20h2v20zm3.134.84l1.732 1-10 17.32-1.732-1 10-17.32zM40 49v2H20v-2h20zm19.32 5l-1 1.732-17.32-10L42 44l17.32 10zm16.546 4.16l-1.732 1-10-17.32 1.732-1 10 17.32zM91 60h-2V40h2v20zm3.134.84l1.732 1-10 17.32-1.732-1 10-17.32zm24.026 3.294l1 1.732-17.32 10-1-1.732 17.32-10zM39.32 74l-1 1.732-17.32-10L22 64l17.32 10zm16.546 4.16l-1.732 1-10-17.32 1.732-1 10 17.32zM71 80h-2V60h2v20zm3.134.84l1.732 1-10 17.32-1.732-1 10-17.32zm24.026 3.294l1 1.732-17.32 10-1-1.732 17.32-10zM120 89v2h-20v-2h20zm-84.134 9.16l-1.732 1-10-17.32 1.732-1 10 17.32zM51 100h-2V80h2v20zm3.134.84l1.732 1-10 17.32-1.732-1 10-17.32zm24.026 3.294l1 1.732-17.32 10-1-1.732 17.32-10zM100 109v2H80v-2h20zm19.32 5l-1 1.732-17.32-10 1-1.732 17.32 10zM31 120h-2v-20h2v20z' fill='%23ffffff' fill-opacity='0.03' fill-rule='evenodd'/%3E%3C/svg%3E");
  opacity: 0.08;
  z-index: -1;
}

.sidebar-collapsed {
  width: 70px;
}

.sidebar-header {
  @apply flex items-center justify-between p-4 border-b border-opacity-20;
  height: 64px;
  border-color: rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(5px);
}

.logo-container {
  @apply flex items-center;
  overflow: hidden;
}

.logo {
  @apply w-8 h-8;
  min-width: 32px;
  filter: brightness(0) invert(1);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
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
  border-radius: 10px;
  margin: 4px 8px;
  position: relative;
  overflow: hidden;
}

:deep(.el-submenu__title)::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, rgba(255,255,255,0) 0%, rgba(255,255,255,0.1) 50%, rgba(255,255,255,0) 100%);
  transform: translateX(-100%);
  transition: transform 0.6s;
}

:deep(.el-submenu__title:hover)::before {
  transform: translateX(100%);
}

:deep(.el-submenu__title:hover) {
  background-color: rgba(255, 255, 255, 0.12) !important;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

:deep(.el-menu-item) {
  @apply transition-all duration-200;
  border-radius: 10px;
  margin: 4px 8px;
  position: relative;
  overflow: hidden;
}

:deep(.el-menu-item)::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, rgba(255,255,255,0) 0%, rgba(255,255,255,0.1) 50%, rgba(255,255,255,0) 100%);
  transform: translateX(-100%);
  transition: transform 0.6s;
}

:deep(.el-menu-item:hover)::before {
  transform: translateX(100%);
}

:deep(.el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.12) !important;
  transform: translateX(2px);
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(255,255,255,0.15) 0%, rgba(255,255,255,0) 100%) !important;
  position: relative;
  transform: translateX(3px);
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08);
}

:deep(.el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 3px;
  background: linear-gradient(to bottom, #f0f9ff, #dbeafe, #bfdbfe);
  border-radius: 0 2px 2px 0;
  box-shadow: 0 0 6px rgba(219, 234, 254, 0.5);
}

/* 收起状态下居中显示图标 */
:deep(.el-menu--collapse .el-submenu__title) {
  padding: 0 16px !important;
  display: flex;
  justify-content: center;
}

:deep(.el-menu--collapse .el-menu-item) {
  padding: 0 16px !important;
  display: flex;
  justify-content: center;
}

/* 主容器样式 */
.main-container {
  @apply flex-1 flex flex-col;
  margin-left: 70px;
  transition: margin-left 0.3s ease-in-out;
}

.sidebar:not(.sidebar-collapsed) + .main-container {
  margin-left: 240px;
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

/* ===== 顶部导航增强 ===== */
.app-header {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.3s ease;
}

.app-header:hover {
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.08);
}

/* 搜索框聚焦高亮 */
:deep(.header-search .el-input__wrapper) {
  transition: border-color 0.2s, box-shadow 0.2s;
}
:deep(.header-search .el-input.is-focus .el-input__wrapper) {
  border-color: #6366f1 !important;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.15);
}

/* 通知铃铛动画 */
@keyframes bell-ring {
  0% { transform: rotate(0); }
  10% { transform: rotate(-15deg); }
  20% { transform: rotate(12deg); }
  30% { transform: rotate(-10deg); }
  40% { transform: rotate(8deg); }
  50% { transform: rotate(-6deg); }
  60% { transform: rotate(4deg); }
  70% { transform: rotate(-2deg); }
  80%, 100% { transform: rotate(0);
  }
}

.notification-badge .el-button:hover i {
  animation: bell-ring 1s ease-in-out;
  transform-origin: top center;
}

/* 用户信息悬停高亮 */
.user-info {
  transition: background-color 0.2s;
  padding: 4px 8px;
  border-radius: 6px;
}
.user-info:hover {
  background-color: rgba(99, 102, 241, 0.08);
}

.user-dropdown-menu {
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08);
  animation: dropdown-fade 0.25s ease forwards;
  transform-origin: top right;
}
@keyframes dropdown-fade {
  0% { opacity: 0; transform: translateY(-8px) scale(0.95); }
  100% { opacity: 1; transform: translateY(0) scale(1); }
}

/* ===== 标签页美化 ===== */
.tab-item {
  transition: background-color 0.2s, transform 0.2s, box-shadow 0.2s;
}
.tab-item:hover {
  transform: translateY(-2px);
  background-color: rgba(99, 102, 241, 0.05);
}
.tab-item.active {
  background: linear-gradient(135deg, #c4b5fd 0%, #a78bfa 100%);
  color: #4c1d95;
  box-shadow: 0 2px 6px rgba(99, 102, 241, 0.2);
}
.tab-close {
  transition: opacity 0.2s;
}
.tab-item:hover .tab-close {
  opacity: 1 !important;
}
/* ===== End ===== */

/* ===== 侧边栏动画与配色 ===== */
.sidebar{
  @apply h-full bg-gradient-to-br text-white flex flex-col overflow-hidden transition-[width] duration-300 ease-in-out;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #a78bfa 100%);
  width: 220px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.06), 0 0 1px rgba(0, 0, 0, 0.1);
}
.sidebar-collapsed{ width: 64px; }
/* 额外 class 方便过渡时区分 */
.sidebar-expanded{ width: 220px; }

/* 菜单 hover & active 效果 */
:deep(.el-menu-vertical .el-menu-item){
  transition: all .3s ease;
  border-radius: 0.5rem;
  margin: 0 6px;
}
:deep(.el-menu-vertical .el-menu-item:hover){
  background-color: rgba(255,255,255,.12);
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
:deep(.el-menu-vertical .el-menu-item.is-active){
  background-image: linear-gradient(135deg, rgba(255,255,255,0.15) 0%, rgba(255,255,255,0.08) 100%);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

/* Sub-menu title hover */
:deep(.el-sub-menu__title){
  margin: 0 4px;
  border-radius: 0.5rem;
  transition: all .3s ease;
}
:deep(.el-sub-menu__title:hover){
  background-color: rgba(255,255,255,.12);
  transform: translateY(-2px);
}

/* 折叠提示 */
.fade-enter-from, .fade-leave-to{ opacity:0; transform:translateY(-2px);} 
.fade-enter-active, .fade-leave-active{ transition:all .2s ease; }

/* 侧边栏底部装饰 */
.sidebar::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 150px;
  background: linear-gradient(to top, rgba(0,0,0,0.1) 0%, rgba(0,0,0,0) 100%);
  z-index: -1;
  pointer-events: none;
}

/* ******************** */
</style> 