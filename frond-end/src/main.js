import '@/assets/css/main.css'
import '@/assets/css/base.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Antd from 'ant-design-vue';
import 'ant-design-vue/dist/reset.css';
import axios  from "axios";
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'

axios.defaults.baseURL = 'http://localhost:8080'
axios.defaults.withCredentials = true
//后端基础url 之后在请求时只用填写路径 Axios会自动以该url为基础添加路径
const app = createApp(App)

app.use(createPinia())
app.use(router).use(ElementPlus).use(Antd)
app.config.globalProperties.$axios = axios

app.mount('#app')