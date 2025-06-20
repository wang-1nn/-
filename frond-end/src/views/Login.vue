<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { post } from '@/net';
import { useAuthStore } from '@/stores/counter.js';

const router = useRouter();
const auth = useAuthStore();
const loginForm = reactive({
  username: '',
  password: '',
  role: ''
});
const loading = ref(false);
const errorMessage = ref('');

// 根据roleId跳转到不同页面
const handleRoleRedirect = (roleId) => {
  switch(roleId) {
    case 1:
      router.push('/admin/dashboard');
      break;
    case 2:
      router.push('/teacher/dashboard');
      break;
    case 3:
      router.push('/student/dashboard');
      break;
    default:
      errorMessage.value = '未知的用户角色';
  }
};

const login = () => {
  if (!loginForm.username || !loginForm.password || !loginForm.role) {
    errorMessage.value = '请输入用户名、密码并选择角色';
    return;
  }
  loading.value = true;
  errorMessage.value = '';
  post('/api/auth/login', {username:loginForm.username,password:loginForm.password,role:loginForm.role},
    (message, data) => {
      auth.login(data)
      // 根据roleId跳转到不同页面
      handleRoleRedirect(data.roleId);
    }, 
    (message) => {
      loading.value = false;
      errorMessage.value = message || '登录失败，请检查用户名和密码';
    },
    () => {
      loading.value = false;
      errorMessage.value = '网络错误，请稍后再试';
    }
  );
};
</script>

<template>
  <div class="min-h-screen bg-gray-100 flex items-center justify-center px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full bg-white rounded-lg shadow-md p-8">
      <div class="text-center">
        <h1 class="text-3xl font-bold text-gray-800">EduBrain</h1>
        <p class="mt-2 text-gray-600">智能教育平台</p>
      </div>
      
      <form @submit.prevent="login" class="mt-8 space-y-6">
        <div v-if="errorMessage" class="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-md text-sm">
          {{ errorMessage }}
        </div>
        
        <div>
          <label for="username" class="block text-sm font-medium text-gray-700 mb-1">用户名</label>
          <input 
            id="username"
            type="text" 
            v-model="loginForm.username" 
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" 
            placeholder="请输入用户名"
            required
          />
        </div>
        
        <div>
          <label for="password" class="block text-sm font-medium text-gray-700 mb-1">密码</label>
          <input 
            id="password"
            type="password" 
            v-model="loginForm.password" 
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" 
            placeholder="请输入密码"
            required
          />
        </div>
        
        <div>
          <label for="role" class="block text-sm font-medium text-gray-700 mb-1">角色</label>
          <select id="role" v-model="loginForm.role" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" required>
            <option value="" disabled>请选择角色</option>
            <option value="1">管理员</option>
            <option value="2">教师</option>
            <option value="3">学生</option>
          </select>
        </div>
        
        <div class="flex items-center justify-between">
          <div class="flex items-center">
            <input 
              id="remember-me" 
              name="remember-me" 
              type="checkbox" 
              class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
            />
            <label for="remember-me" class="ml-2 block text-sm text-gray-700">记住我</label>
          </div>
          
          <div class="text-sm">
            <a href="#" class="text-blue-600 hover:text-blue-500">忘记密码?</a>
          </div>
        </div>
        
        <div>
          <button 
            type="submit" 
            class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            :disabled="loading"
          >
            <span v-if="loading">登录中...</span>
            <span v-else>登录</span>
          </button>
        </div>
      </form>
      
      <div class="mt-6 text-center">
        <p class="text-sm text-gray-600">
          还没有账号? 
          <a href="#" class="text-blue-600 hover:text-blue-500">立即注册</a>
        </p>
      </div>
    </div>
  </div>
</template> 