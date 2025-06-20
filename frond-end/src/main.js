import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import axios from 'axios'
import { useAuthStore } from '@/stores/counter.js'

// 导入自定义样式
import './assets/css/base.css'
import './assets/css/main.css'
import './assets/css/style.css'

// Axios 基础配置
axios.defaults.baseURL = 'http://localhost:8080'
axios.defaults.withCredentials = true

const app = createApp(App)


const pinia = createPinia()
app.use(pinia)


const authStore = useAuthStore()
authStore.initAuth()


app.use(router)
app.use(ElementPlus)
app.use(Antd)

// 挂载 axios
app.config.globalProperties.$axios = axios


app.mount('#app')
