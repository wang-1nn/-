<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const props = defineProps({
  // 布局类型: default, dashboard, auth
  type: {
    type: String,
    default: 'default',
    validator: (value) => ['default', 'dashboard', 'auth'].includes(value)
  },
  // 侧边栏菜单项
  navItems: {
    type: Array,
    default: () => []
  },
  // 是否默认收起侧边栏
  collapsedDefault: {
    type: Boolean,
    default: false
  },
  // 顶部右侧操作区
  hasHeaderActions: {
    type: Boolean,
    default: true
  },
  // 顶部标题
  title: {
    type: String,
    default: ''
  },
  // 是否展示页面标题
  showPageTitle: {
    type: Boolean,
    default: true
  },
  // 是否显示面包屑
  showBreadcrumb: {
    type: Boolean,
    default: true
  },
  // 用户信息
  user: {
    type: Object,
    default: () => ({
      name: '用户',
      avatar: '',
      role: ''
    })
  },
  // 是否启用标签页
  enableTabs: {
    type: Boolean,
    default: true
  }
});

const emit = defineEmits(['toggle-sidebar', 'tab-change', 'logout']);

const route = useRoute();
const router = useRouter();

// 侧边栏状态
const sidebarCollapsed = ref(props.collapsedDefault);
const isSidebarHovered = ref(false);

// 是否为移动视图
const isMobile = ref(false);
const checkIsMobile = () => {
  isMobile.value = window.innerWidth < 768;
  if (isMobile.value) {
    sidebarCollapsed.value = true;
  }
};

// 切换侧边栏
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value;
  emit('toggle-sidebar', sidebarCollapsed.value);
};

// 处理侧边栏鼠标事件
const handleSidebarMouseEnter = () => {
  isSidebarHovered.value = true;
  if (!isMobile.value && props.type === 'dashboard') {
    sidebarCollapsed.value = false;
  }
};

const handleSidebarMouseLeave = () => {
  isSidebarHovered.value = false;
  if (!isMobile.value && props.collapsedDefault && props.type === 'dashboard') {
    sidebarCollapsed.value = true;
  }
};

// 标签页相关
const activePath = ref(route.path);
const openedTabs = ref([]);

// 计算布局类
const layoutClass = computed(() => [
  'ui-layout',
  `ui-layout-${props.type}`,
  sidebarCollapsed.value ? 'ui-layout-collapsed' : '',
  isMobile.value ? 'ui-layout-mobile' : ''
]);

// 计算内容类
const contentClass = computed(() => [
  'ui-layout-content',
  props.showPageTitle ? '' : 'ui-layout-content-no-title'
]);

// 初始化视图
onMounted(() => {
  window.addEventListener('resize', checkIsMobile);
  checkIsMobile();

  // 初始化标签页
  if (props.enableTabs) {
    initTabs();
  }

  return () => {
    window.removeEventListener('resize', checkIsMobile);
  };
});

// 监听路由变化
watch(() => route.path, (newPath) => {
  activePath.value = newPath;
  if (props.enableTabs) {
    addTab(newPath, route.meta.title || '未命名页面');
  }

  // 在移动视图下自动收起侧边栏
  if (isMobile.value) {
    sidebarCollapsed.value = true;
  }
});

// 初始化标签页
const initTabs = () => {
  // 添加当前路由标签
  addTab(route.path, route.meta.title || '未命名页面');
};

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
  if (activePath.value === targetPath) {
    // 如果关闭的是最后一个标签，则激活前一个标签
    const nextActiveIndex = targetIndex === openedTabs.value.length - 1
        ? targetIndex - 1
        : targetIndex + 1;

    activePath.value = openedTabs.value[nextActiveIndex].path;
    switchTab(activePath.value);
  }

  // 移除标签
  openedTabs.value.splice(targetIndex, 1);
};

// 切换标签
const switchTab = (path) => {
  activePath.value = path;
  emit('tab-change', path);
  router.push(path);
};

// 处理登出
const handleLogout = () => {
  emit('logout');
};

// 检查菜单项是否激活
const isMenuItemActive = (item) => {
  if (item.children && item.children.length) {
    return item.children.some(child => child.path === route.path);
  }
  return item.path === route.path;
};

// 检查子菜单项是否激活
const isSubMenuActive = (path) => {
  return path === route.path;
};

// 当前面包屑项
const breadcrumbItems = computed(() => {
  const items = [];
  // 根据当前路由计算面包屑
  if (props.showBreadcrumb) {
    // 首页
    items.push({
      title: '首页',
      path: props.type === 'dashboard' ? '/teacher/dashboard' : '/'
    });

    // 查找当前路由对应的菜单项
    if (props.navItems && props.navItems.length) {
      for (const item of props.navItems) {
        if (item.children && item.children.length) {
          const activeChild = item.children.find(child => child.path === route.path);
          if (activeChild) {
            items.push({
              title: item.title,
              path: item.path || null
            });
            items.push({
              title: activeChild.title,
              path: activeChild.path
            });
            break;
          }
        } else if (item.path === route.path) {
          items.push({
            title: item.title,
            path: item.path
          });
          break;
        }
      }
    }
  }
  return items;
});
</script>

<template>
  <div :class="layoutClass">
    <!-- 侧边栏 -->
    <aside
        class="ui-sidebar"
        @mouseenter="handleSidebarMouseEnter"
        @mouseleave="handleSidebarMouseLeave"
        v-if="type !== 'auth'"
    >
      <div class="ui-sidebar-logo">
        <slot name="logo">
          <div class="ui-logo">
            <span v-if="!sidebarCollapsed || isSidebarHovered" class="ui-logo-text">教师助手</span>
          </div>
        </slot>
      </div>

      <!-- 导航菜单 -->
      <nav class="ui-nav">
        <div v-for="(item, index) in navItems" :key="index" class="ui-nav-item-wrapper">
          <!-- 有子菜单的菜单项 -->
          <template v-if="item.children && item.children.length">
            <div class="ui-nav-group">
              <div
                  class="ui-nav-group-title"
                  :class="{ 'ui-nav-active': isMenuItemActive(item) }"
              >
                <span class="ui-nav-icon" v-if="item.icon" v-html="item.icon"></span>
                <span v-else class="ui-nav-icon-placeholder"></span>
                <span class="ui-nav-text" v-if="!sidebarCollapsed || isSidebarHovered">{{ item.title }}</span>
              </div>

              <div class="ui-nav-group-items" v-if="!sidebarCollapsed || isSidebarHovered">
                <router-link
                    v-for="(child, childIndex) in item.children"
                    :key="childIndex"
                    :to="child.path"
                    class="ui-nav-item ui-nav-child"
                    :class="{ 'ui-nav-active': isSubMenuActive(child.path) }"
                >
                  <span class="ui-nav-icon" v-if="child.icon" v-html="child.icon"></span>
                  <span v-else class="ui-nav-icon-placeholder"></span>
                  <span class="ui-nav-text">{{ child.title }}</span>
                </router-link>
              </div>
            </div>
          </template>

          <!-- 没有子菜单的菜单项 -->
          <router-link
              v-else
              :to="item.path"
              class="ui-nav-item"
              :class="{ 'ui-nav-active': isSubMenuActive(item.path) }"
          >
            <span class="ui-nav-icon" v-if="item.icon" v-html="item.icon"></span>
            <span v-else class="ui-nav-icon-placeholder"></span>
            <span class="ui-nav-text" v-if="!sidebarCollapsed || isSidebarHovered">{{ item.title }}</span>
          </router-link>
        </div>
      </nav>
    </aside>

    <!-- 主内容区 -->
    <div class="ui-layout-main">
      <!-- 顶部栏 -->
      <header class="ui-header" v-if="type !== 'auth'">
        <div class="ui-header-left">
          <!-- 侧边栏切换按钮 -->
          <button @click="toggleSidebar" class="ui-sidebar-toggle">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="3" y1="12" x2="21" y2="12"></line>
              <line x1="3" y1="6" x2="21" y2="6"></line>
              <line x1="3" y1="18" x2="21" y2="18"></line>
            </svg>
          </button>

          <!-- 面包屑 -->
          <div class="ui-breadcrumb" v-if="showBreadcrumb && breadcrumbItems.length > 0">
            <div v-for="(item, index) in breadcrumbItems" :key="index" class="ui-breadcrumb-item">
              <router-link v-if="item.path && index < breadcrumbItems.length - 1" :to="item.path" class="ui-breadcrumb-link">
                {{ item.title }}
              </router-link>
              <span v-else class="ui-breadcrumb-text" :class="{ 'ui-breadcrumb-current': index === breadcrumbItems.length - 1 }">
                {{ item.title }}
              </span>
              <span v-if="index < breadcrumbItems.length - 1" class="ui-breadcrumb-separator">/</span>
            </div>
          </div>
        </div>

        <div class="ui-header-right" v-if="hasHeaderActions">
          <!-- 头部操作区插槽 -->
          <slot name="header-actions"></slot>

          <!-- 用户信息 -->
          <div class="ui-user-menu">
            <div class="ui-user-info">
              <img v-if="user.avatar" :src="user.avatar" alt="Avatar" class="ui-user-avatar" />
              <div v-else class="ui-user-avatar ui-user-avatar-placeholder">{{ user.name ? user.name.charAt(0).toUpperCase() : 'U' }}</div>
              <span class="ui-user-name">{{ user.name }}</span>
            </div>
            <div class="ui-user-dropdown">
              <button @click="handleLogout" class="ui-dropdown-item">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                  <polyline points="16 17 21 12 16 7"></polyline>
                  <line x1="21" y1="12" x2="9" y2="12"></line>
                </svg>
                <span>退出登录</span>
              </button>
            </div>
          </div>
        </div>
      </header>

      <!-- 标签页 -->
      <div class="ui-tabs" v-if="type === 'dashboard' && enableTabs">
        <div class="ui-tabs-list">
          <div
              v-for="tab in openedTabs"
              :key="tab.path"
              class="ui-tab"
              :class="{ 'ui-tab-active': activePath === tab.path }"
              @click="switchTab(tab.path)"
          >
            <span class="ui-tab-text">{{ tab.title }}</span>
            <button
                v-if="openedTabs.length > 1"
                class="ui-tab-close"
                @click.stop="closeTab(tab.path)"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          </div>
        </div>
      </div>

      <!-- 页面标题 -->
      <div class="ui-page-header" v-if="showPageTitle && type === 'dashboard'">
        <slot name="page-header">
          <h1 class="ui-page-title">{{ title || route.meta.title || '未命名页面' }}</h1>
          <div class="ui-page-actions">
            <slot name="page-actions"></slot>
          </div>
        </slot>
      </div>

      <!-- 内容区 -->
      <div :class="contentClass">
        <slot></slot>
      </div>

      <!-- 页脚 -->
      <footer class="ui-footer" v-if="type !== 'auth'">
        <slot name="footer">
          <div class="ui-footer-content">
            <p>© {{ new Date().getFullYear() }} 教师智能助手系统 - 版权所有</p>
          </div>
        </slot>
      </footer>
    </div>
  </div>
</template>

<style scoped>
.ui-layout {
  @apply flex min-h-screen bg-slate-50;
  width: 100%;
}

/* 侧边栏 */
.ui-sidebar {
  @apply bg-white border-r border-slate-200 flex flex-col fixed h-full z-30 transition-all duration-300;
  width: var(--sidebar-width);
}

.ui-layout-collapsed .ui-sidebar {
  width: var(--sidebar-collapsed-width);
}

.ui-sidebar-logo {
  @apply flex items-center justify-center h-16 border-b border-slate-200 overflow-hidden;
}

.ui-logo {
  @apply flex items-center;
}

.ui-logo-text {
  @apply font-semibold text-lg text-slate-800 whitespace-nowrap;
}

/* 导航菜单 */
.ui-nav {
  @apply flex flex-col overflow-y-auto overflow-x-hidden flex-1 py-4;
}

.ui-nav-item-wrapper {
  @apply w-full;
}

.ui-nav-item {
  @apply flex items-center px-4 py-2 text-slate-600 hover:bg-slate-100 hover:text-primary-600 transition-colors duration-200 rounded-md mx-2 mb-1;
}

.ui-nav-active {
  @apply bg-primary-50 text-primary-600;
}

.ui-nav-group {
  @apply mb-1;
}

.ui-nav-group-title {
  @apply flex items-center px-4 py-2 text-slate-700 font-medium cursor-pointer hover:bg-slate-100 transition-colors duration-200 rounded-md mx-2;
}

.ui-nav-group-items {
  @apply ml-4 mt-1;
}

.ui-nav-child {
  @apply text-sm;
}

.ui-nav-icon {
  @apply w-5 h-5 mr-3 flex-shrink-0;
}

.ui-nav-icon-placeholder {
  @apply w-5 h-5 mr-3 flex-shrink-0;
}

.ui-nav-text {
  @apply truncate;
}

/* 主内容区 */
.ui-layout-main {
  @apply flex-1 flex flex-col min-h-screen transition-all duration-300;
  margin-left: var(--sidebar-width);
}

.ui-layout-collapsed .ui-layout-main {
  margin-left: var(--sidebar-collapsed-width);
}

.ui-layout-mobile .ui-layout-main {
  margin-left: 0;
}

/* 顶部栏 */
.ui-header {
  @apply bg-white border-b border-slate-200 h-16 flex items-center justify-between px-4 sticky top-0 z-20;
}

.ui-header-left {
  @apply flex items-center;
}

.ui-header-right {
  @apply flex items-center space-x-4;
}

.ui-sidebar-toggle {
  @apply p-2 rounded-md text-slate-500 hover:bg-slate-100 mr-4;
}

/* 面包屑 */
.ui-breadcrumb {
  @apply flex items-center;
}

.ui-breadcrumb-item {
  @apply flex items-center;
}

.ui-breadcrumb-link {
  @apply text-slate-500 hover:text-primary-600 text-sm;
}

.ui-breadcrumb-text {
  @apply text-slate-500 text-sm;
}

.ui-breadcrumb-current {
  @apply text-slate-800 font-medium;
}

.ui-breadcrumb-separator {
  @apply mx-2 text-slate-400 text-sm;
}

/* 用户菜单 */
.ui-user-menu {
  @apply relative;
}

.ui-user-info {
  @apply flex items-center cursor-pointer p-1 rounded-md hover:bg-slate-100;
}

.ui-user-avatar {
  @apply w-8 h-8 rounded-full bg-primary-500 flex items-center justify-center text-white font-medium text-sm mr-2;
}

.ui-user-name {
  @apply text-sm font-medium text-slate-700;
}

.ui-user-dropdown {
  @apply absolute right-0 top-full mt-1 bg-white shadow-lg rounded-md py-1 w-48 z-30 border border-slate-200;
  display: none;
}

.ui-user-menu:hover .ui-user-dropdown {
  display: block;
}

.ui-dropdown-item {
  @apply flex items-center w-full px-4 py-2 text-sm text-slate-700 hover:bg-slate-100 transition-colors duration-150;
}

.ui-dropdown-item svg {
  @apply mr-2;
}

/* 标签页 */
.ui-tabs {
  @apply bg-white border-b border-slate-200 overflow-x-auto;
}

.ui-tabs-list {
  @apply flex items-center h-10;
}

.ui-tab {
  @apply flex items-center px-4 h-full cursor-pointer bg-slate-50 text-slate-600 border-r border-slate-200 whitespace-nowrap;
}

.ui-tab-active {
  @apply bg-white text-primary-600 relative;
}

.ui-tab-active::after {
  content: '';
  @apply absolute bottom-0 left-0 right-0 h-0.5 bg-primary-600;
}

.ui-tab-text {
  @apply text-sm;
}

.ui-tab-close {
  @apply ml-2 p-0.5 rounded-full hover:bg-slate-200;
}

/* 页面标题 */
.ui-page-header {
  @apply flex justify-between items-center px-6 py-4;
}

.ui-page-title {
  @apply text-2xl font-bold text-slate-800;
}

.ui-page-actions {
  @apply flex items-center space-x-2;
}

/* 内容区 */
.ui-layout-content {
  @apply flex-1 p-6;
}

.ui-layout-content-no-title {
  @apply pt-0;
}

/* 页脚 */
.ui-footer {
  @apply py-4 px-6 bg-white border-t border-slate-200 text-slate-500 text-sm mt-auto;
}

/* 授权布局 */
.ui-layout-auth {
  @apply bg-slate-100;
}

.ui-layout-auth .ui-layout-main {
  margin-left: 0;
}
</style>