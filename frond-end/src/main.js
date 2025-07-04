// 设置自定义 daisyUI 主题（需在加载 Tailwind 样式前执行）
if (!document.documentElement.getAttribute('data-theme')) {
  document.documentElement.setAttribute('data-theme', 'education');
}

import { autoAnimatePlugin } from '@formkit/auto-animate/vue'
import { MotionPlugin } from '@vueuse/motion'

import {createApp} from 'vue'
import {createPinia} from 'pinia'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import VueDOMPurifyHTML from 'vue-dompurify-html'
import VueVirtualScroller from 'vue3-virtual-scroller'
import 'vue3-virtual-scroller/dist/vue3-virtual-scroller.css'
import 'element-plus/dist/index.css'
import 'github-markdown-css'
import 'highlight.js/styles/github-dark.css'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import axios from 'axios'
import {useAuthStore} from '@/stores/counter.js'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

// 导入自定义样式
import './assets/css/base.css'
import './assets/css/main.css'
import './assets/css/style.css'

// 导入自定义全局组件
import BaseCard from '@/components/common/BaseCard.vue'
import BaseButton from '@/components/common/BaseButton.vue'

// Axios 基础配置
axios.defaults.baseURL = 'http://localhost:8080'
axios.defaults.withCredentials = true

const app = createApp(App)

const pinia = createPinia()
app.use(pinia)

const authStore = useAuthStore()
// authStore.initAuth() // initAuth 不存在，暂时注释掉

app.use(router)
app.use(ElementPlus, {
  locale: zhCn,
  size: 'default',
  zIndex: 2000
})
app.use(Antd)

// 注册 DOMPurify 插件，自动提供 v-dompurify-html 指令
app.use(VueDOMPurifyHTML)

// 全局注册虚拟滚动组件（<virtual-list> 等）
app.use(VueVirtualScroller)

// 挂载 axios
app.config.globalProperties.$axios = axios

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 注册全局组件
app.component('BaseCard', BaseCard)
app.component('BaseButton', BaseButton)

// Auto-Animate 指令 v-auto-animate
app.use(autoAnimatePlugin)

// VueUse Motion 动画插件（提供 v-motion 指令）
app.use(MotionPlugin)

app.mount('#app')
