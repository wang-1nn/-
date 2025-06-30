<script setup>
import {computed, nextTick, onMounted, ref, watch} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import {useAuthStore} from '@/stores/counter';
import {ElMessage, ElMessageBox} from 'element-plus';

// 路由相关
const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

// 侧边栏状态
const collapsed = ref(true); // 默认收起
const isHovering = ref(false);

const toggleSidebar = () => {
  collapsed.value = !collapsed.value;
};

const handleSidebarMouseEnter = () => {
  isHovering.value = true;
  if (!isMobile.value) {
    collapsed.value = false;
  }
};

const handleSidebarMouseLeave = () => {
  isHovering.value = false;
  if (!isMobile.value) {
    collapsed.value = true;
  }
};

// 是否处于移动视图
const isMobile = ref(window.innerWidth < 768);
const checkMobile = () => {
  isMobile.value = window.innerWidth < 768;
  if (isMobile.value) collapsed.value = true;
};

// 监听窗口大小变化
onMounted(() => {
  window.addEventListener('resize', checkMobile);
  checkMobile(); // 初始检查

  // 监听侧边栏状态变化，设置子菜单悬停行为
  watch(collapsed, (newVal) => {
    nextTick(() => {
      setupHoverSubmenus();
    });
  });

  // 设置子菜单在hover时自动打开
  const setupHoverSubmenus = () => {
    // 获取所有子菜单
    const submenus = document.querySelectorAll('.el-submenu');
    
    // 移除之前的事件监听
    document.removeEventListener('mouseover', handleMouseOver);
    
    // 为每个子菜单添加鼠标进入事件
    submenus.forEach(submenu => {
      // 移除之前的事件监听
      const oldElement = submenu;
      const newElement = oldElement.cloneNode(true);
      if (oldElement.parentNode) {
        oldElement.parentNode.replaceChild(newElement, oldElement);
      }
      
      // 添加鼠标进入事件
      newElement.addEventListener('mouseenter', () => {
        if (collapsed.value) {
          const submenuTitle = newElement.querySelector('.el-submenu__title');
          if (submenuTitle) {
            submenuTitle.click();
          }
        }
      });
    });
    
    // 添加全局鼠标移动事件，处理子菜单的自动关闭
    document.addEventListener('mouseover', handleMouseOver);
  };
  
  // 处理鼠标移动事件，用于自动关闭子菜单
  const handleMouseOver = (e) => {
    if (collapsed.value) {
      const popup = document.querySelector('.el-menu--popup');
      const sidebar = document.querySelector('.sidebar');
      
      if (popup && sidebar) {
        const popupRect = popup.getBoundingClientRect();
        const sidebarRect = sidebar.getBoundingClientRect();
        
        // 检查鼠标是否在弹出菜单或侧边栏内
        const isInPopup = e.clientX >= popupRect.left && 
                        e.clientX <= popupRect.right && 
                        e.clientY >= popupRect.top && 
                        e.clientY <= popupRect.bottom;
        
        const isInSidebar = e.clientX >= sidebarRect.left && 
                          e.clientX <= sidebarRect.right && 
                          e.clientY >= sidebarRect.top && 
                          e.clientY <= sidebarRect.bottom;
        
        // 如果鼠标既不在弹出菜单内也不在侧边栏内，则关闭弹出菜单
        if (!isInPopup && !isInSidebar) {
          popup.style.display = 'none';
        }
      }
    }
  };
  
  // 初始设置
  setTimeout(setupHoverSubmenus, 500);

  return () => {
    window.removeEventListener('resize', checkMobile);
    document.removeEventListener('mouseover', handleMouseOver);
  };
});

// 标签页相关
const activeTab = ref(route.path);
const openedTabs = ref([]);

// 初始化标签页
onMounted(() => {
  // 如果当前路由不是 dashboard，则添加 dashboard 作为首个标签
  if (route.path !== '/teacher/dashboard') {
    addTab('/teacher/dashboard', '工作台');
  }

  // 添加当前路由标签
  addTab(route.path, route.meta.title || '未命名页面');
});

// 监听路由变化，动态添加标签页
watch(() => route.path, (newPath) => {
  const title = route.meta.title || '未命名页面';
  addTab(newPath, title);
  activeTab.value = newPath;

  // 在移动视图下自动收起侧边栏
  if (isMobile.value) {
    collapsed.value = true;
  }
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

// 系统菜单
const menuItems = [
  {
    title: '教师工作台',
    icon: 'el-icon-house',
    children: [
      { title: '仪表盘', path: '/teacher/dashboard', icon: 'el-icon-data-board' },
      { title: '智能助手', path: '/teacher/ai-assistant', icon: 'el-icon-chat-dot-round' }
    ]
  },
  {
    title: '智能备课',
    icon: 'el-icon-edit-outline',
    children: [
      { title: '智能备-工作台', path: '/teacher/lesson-prep-workbench', icon: 'el-icon-cpu' },
      { title: '教学计划', path: '/teacher/teaching-plans', icon: 'el-icon-notebook-1' }
    ]
  },
  {
    title: '题库与考核',
    icon: 'el-icon-document-checked',
    children: [
      { title: '题目生成', path: '/teacher/question-generator', icon: 'el-icon-magic-stick' },
      { title: '题库管理', path: '/teacher/question-bank', icon: 'el-icon-collection' },
      { title: '考核设计', path: '/teacher/exams', icon: 'el-icon-edit' },
      { title: '自动批改', path: '/teacher/auto-grader', icon: 'el-icon-check' }
    ]
  },
  {
    title: '学情分析',
    icon: 'el-icon-data-analysis',
    children: [
      { title: '成绩录入', path: '/teacher/scores', icon: 'el-icon-tickets' },
      { title: '学情分析', path: '/teacher/analysis', icon: 'el-icon-data-line' }
    ]
  },
  {
    title: '学生管理',
    icon: 'el-icon-user',
    children: [
      { title: '学生管理', path: '/teacher/students', icon: 'el-icon-user-solid' },
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

// 关闭所有标签页，只保留首页
const closeAllTabs = () => {
  const dashboard = openedTabs.value.find(tab => tab.path === '/teacher/dashboard');
  if (dashboard) {
    openedTabs.value = [dashboard];
    activeTab.value = '/teacher/dashboard';
    router.push('/teacher/dashboard');
  }
};

// 关闭其他标签页
const closeOtherTabs = () => {
  const currentTab = openedTabs.value.find(tab => tab.path === activeTab.value);
  if (currentTab) {
    openedTabs.value = [currentTab];
  }
};

// 当前时间
const currentTime = ref(new Date());
const formattedTime = computed(() => {
  const hours = currentTime.value.getHours().toString().padStart(2, '0');
  const minutes = currentTime.value.getMinutes().toString().padStart(2, '0');
  return `${hours}:${minutes}`;
});

const formattedDate = computed(() => {
  const year = currentTime.value.getFullYear();
  const month = (currentTime.value.getMonth() + 1).toString().padStart(2, '0');
  const day = currentTime.value.getDate().toString().padStart(2, '0');
  return `${year}-${month}-${day}`;
});

// 更新时间
setInterval(() => {
  currentTime.value = new Date();
}, 60000); // 每分钟更新一次
</script>

<template>
  <div class="app-container">
    <!-- 侧边栏 -->
    <aside
      :class="['sidebar', collapsed && !isHovering ? 'sidebar-collapsed' : '']"
      @mouseenter="handleSidebarMouseEnter"
      @mouseleave="handleSidebarMouseLeave"
    >
      <div class="sidebar-header">
        <div class="logo-container">
          <div class="custom-logo">
            <i class="el-icon-reading"></i>
          </div>
          <span v-show="!collapsed || isHovering" class="logo-text">智慧教学平台</span>
        </div>
        <button @click="toggleSidebar" class="collapse-btn">
          <i :class="collapsed && !isHovering ? 'el-icon-d-arrow-right' : 'el-icon-d-arrow-left'"></i>
        </button>
      </div>

      <div class="sidebar-menu">
        <el-menu
          :default-active="activeTab"
          :collapse="collapsed && !isHovering"
          :unique-opened="false"
          router
          class="el-menu-vertical"
          background-color="transparent"
          text-color="rgba(255, 255, 255, 0.85)"
          active-text-color="#ffffff"
        >
          <el-sub-menu 
            v-for="(menuGroup, index) in menuItems" 
            :key="`menu-group-${index}`" 
            :index="String(index)"
            popper-class="sidebar-submenu-popper"
          >
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
        </el-menu>
      </div>
    </aside>

    <!-- 主内容区 -->
    <div class="main-container">
      <!-- 顶部导航 -->
      <header class="app-header">
        <div class="header-left">
          <button @click="toggleSidebar" class="menu-toggle">
            <i class="el-icon-menu"></i>
          </button>
          <div class="breadcrumb">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/teacher/dashboard' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item>{{ route.meta.title || '未命名页面' }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-time ml-4">
            <div class="time">{{ formattedTime }}</div>
            <div class="date text-xs text-gray-500">{{ formattedDate }}</div>
          </div>
        </div>

        <div class="header-right">
          <div class="header-search">
            <el-input
              placeholder="全局搜索..."
              prefix-icon="el-icon-search"
              size="small"
              class="rounded-full transition-all duration-300 focus:shadow-md"
            />
          </div>

          <div class="header-actions">
            <div class="action-item">
              <el-badge :value="3" class="notification-badge">
                <el-button
                  icon="el-icon-bell"
                  circle
                  class="transition-all duration-300 transform hover:rotate-12 hover:scale-110"
                ></el-button>
              </el-badge>
            </div>

            <div class="user-dropdown relative">
              <div class="user-info cursor-pointer flex items-center" @click="toggleUserDropdown">
                <div class="avatar-container">
                  <img
                    src="https://i.pravatar.cc/100?img=8"
                    alt="用户头像"
                    class="user-avatar"
                  />
                  <div class="user-status online"></div>
                </div>
                <div class="user-details ml-2">
                  <div class="user-name">{{ authStore.user?.name || '教师用户' }}</div>
                  <div class="user-role text-xs text-gray-500">教师</div>
                </div>
                <i class="el-icon-caret-bottom ml-1 transition-transform duration-300" :class="{ 'transform rotate-180': userDropdownVisible }"></i>
              </div>

              <transition name="dropdown">
                <div v-show="userDropdownVisible" class="user-dropdown-menu">
                  <div class="dropdown-item" @click="router.push('/teacher/profile')">
                    <i class="el-icon-user"></i>
                    <span>个人中心</span>
                  </div>
                  <div class="dropdown-item" @click="router.push('/teacher/settings')">
                    <i class="el-icon-setting"></i>
                    <span>账号设置</span>
                  </div>
                  <div class="dropdown-divider"></div>
                  <div class="dropdown-item text-red-500" @click="handleLogout">
                    <i class="el-icon-switch-button"></i>
                    <span>退出登录</span>
                  </div>
                </div>
              </transition>
            </div>
          </div>
        </div>
      </header>

      <!-- 标签页导航 -->
      <div class="tabs-container">
        <div class="tabs-nav">
          <transition-group name="tab-transition" tag="div" class="flex">
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
          </transition-group>
        </div>
        <div class="tabs-actions">
          <el-dropdown trigger="click">
            <span class="el-dropdown-link cursor-pointer">
              <i class="el-icon-arrow-down"></i>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="closeOtherTabs">关闭其他标签页</el-dropdown-item>
                <el-dropdown-item @click="closeAllTabs">关闭所有标签页</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 内容区域 -->
      <main class="page-container">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <keep-alive>
              <component :is="Component" />
            </keep-alive>
          </transition>
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
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  background: linear-gradient(135deg, #2c3e50 0%, #4a6990 100%);
  border-radius: 0 16px 16px 0;
  overflow: hidden; /* 防止内容溢出 */
}

.sidebar-collapsed {
  width: 70px;
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

.custom-logo {
  @apply flex items-center justify-center rounded-md bg-white bg-opacity-20;
  width: 36px;
  height: 36px;
  min-width: 36px;
  transition: transform 0.3s;
}

.custom-logo i {
  font-size: 22px;
  color: white;
}

.custom-logo:hover {
  transform: rotate(360deg);
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
  border-radius: 8px;
  margin: 4px 8px;
}

:deep(.el-submenu__title:hover) {
  background-color: rgba(255, 255, 255, 0.15) !important;
}

:deep(.el-menu-item) {
  @apply transition-all duration-200;
  border-radius: 8px;
  margin: 4px 8px;
}

:deep(.el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.15) !important;
  transform: translateX(4px);
}

:deep(.el-menu-item.is-active) {
  background-color: rgba(255, 255, 255, 0.2) !important;
  position: relative;
  transform: translateX(4px);
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

/* 收起状态下的图标样式 */
:deep(.el-menu--collapse .el-submenu__title) {
  padding: 0 16px !important;
  display: flex;
  justify-content: center;
}

:deep(.el-menu--collapse .el-submenu__title i) {
  margin-right: 0;
  font-size: 20px;
}

:deep(.el-menu--collapse .el-menu-item) {
  padding: 0 16px !important;
  display: flex;
  justify-content: center;
}

:deep(.el-menu--collapse .el-menu-item i) {
  margin-right: 0;
  font-size: 20px;
}

/* 选中项的高亮效果 */
:deep(.el-menu-item.is-active) {
  background-color: rgba(255, 255, 255, 0.2) !important;
  position: relative;
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
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

:deep(.el-menu--collapse .el-menu-item.is-active) {
  transform: none;
}

:deep(.el-submenu.is-active > .el-submenu__title) {
  color: #ffffff !important;
  font-weight: 500;
}

/* 修改子菜单弹出样式 */
:deep(.el-menu--popup) {
  background: linear-gradient(135deg, #2c3e50 0%, #4a6990 100%);
  border-radius: 8px;
  min-width: 160px;
  padding: 5px;
}

:deep(.el-menu--popup .el-menu-item) {
  color: rgba(255, 255, 255, 0.85);
  border-radius: 6px;
  height: 40px;
  line-height: 40px;
}

:deep(.el-menu--popup .el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.15) !important;
}

:deep(.el-menu--popup .el-menu-item.is-active) {
  background-color: rgba(255, 255, 255, 0.2) !important;
  color: #ffffff;
}

/* 主容器样式 */
.main-container {
  @apply flex-1 flex flex-col;
  margin-left: 70px; /* 默认侧边栏收起状态 */
  transition: margin-left 0.3s ease-in-out;
}

.sidebar:not(.sidebar-collapsed) + .main-container {
  margin-left: 240px;
}

@media (max-width: 768px) {
  .sidebar {
    transform: translateX(-100%);
    width: 240px;
  }
  
  .sidebar-collapsed {
    transform: translateX(-100%);
  }
  
  .main-container {
    margin-left: 0 !important;
  }
}

/* 头部导航样式 */
.app-header {
  @apply flex justify-between items-center px-4 md:px-6 bg-white shadow-sm rounded-lg;
  height: 64px;
  position: sticky;
  top: 8px;
  z-index: 1000;
  margin: 8px;
  transition: all 0.3s;
}

.app-header:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.header-left, .header-right {
  @apply flex items-center;
}

.menu-toggle {
  @apply p-2 rounded-full mr-2 text-gray-600 hover:bg-gray-100 md:hidden transition-all duration-200;
}

.header-time {
  @apply flex flex-col items-center justify-center ml-4 hidden md:flex;
}

.header-time .time {
  @apply font-semibold text-gray-700;
}

.header-search {
  @apply mr-6 w-48 md:w-64 transition-all duration-300;
}

.header-search:focus-within {
  @apply w-64 md:w-80;
}

.header-actions {
  @apply flex items-center space-x-4;
}

.action-item {
  @apply relative;
}

.notification-badge {
  margin-right: 8px;
}

.user-avatar {
  @apply w-10 h-10 rounded-full border-2 border-gray-200 object-cover;
  transition: transform 0.3s;
}

.avatar-container {
  @apply relative;
}

.user-status {
  @apply absolute bottom-0 right-0 w-3 h-3 rounded-full border-2 border-white;
}

.user-status.online {
  @apply bg-green-500;
}

.user-details {
  @apply hidden md:block;
}

.user-name {
  @apply text-sm font-medium;
}

.user-dropdown {
  @apply relative;
}

.user-dropdown-menu {
  @apply absolute right-0 top-full mt-2 w-48 bg-white rounded-lg shadow-lg py-2 z-10;
  transform-origin: top right;
}

.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.3s;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.dropdown-item {
  @apply flex items-center px-4 py-2 hover:bg-gray-100 cursor-pointer transition-colors duration-150;
}

.dropdown-item i {
  @apply mr-2;
}

.dropdown-divider {
  @apply my-2 border-t border-gray-100;
}

/* 标签页导航 */
.tabs-container {
  @apply flex justify-between items-center px-2 bg-white border-b border-gray-200 shadow-sm;
  height: 40px;
  margin: 0 8px;
  border-radius: 0 0 8px 8px;
}

.tabs-nav {
  @apply flex-1 flex items-center overflow-x-auto;
  scrollbar-width: none;
}

.tabs-nav::-webkit-scrollbar {
  display: none;
}

.tab-item {
  @apply flex items-center px-4 py-1 mx-1 text-sm text-gray-700 rounded-md cursor-pointer transition-all duration-200;
  height: 32px;
  border: 1px solid transparent;
}

.tab-item:hover {
  @apply bg-gray-100;
}

.tab-item.active {
  @apply bg-blue-50 text-blue-600 border-blue-200;
}

.tab-title {
  max-width: 120px;
  @apply truncate mr-2;
}

.tab-close {
  @apply opacity-0 hover:bg-red-100 hover:text-red-600 rounded-full p-1 transition-opacity duration-200;
}

.tab-item:hover .tab-close {
  @apply opacity-100;
}

.tabs-actions {
  @apply px-2;
}

/* 内容区域 */
.page-container {
  @apply p-4 md:p-6 flex-1 overflow-auto;
}

/* 页面切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* 标签动画 */
.tab-transition-enter-active,
.tab-transition-leave-active {
  transition: all 0.3s ease;
}

.tab-transition-enter-from,
.tab-transition-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

.tab-transition-move {
  transition: transform 0.3s ease;
}
</style>