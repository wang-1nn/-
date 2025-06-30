<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/counter';
import { post } from '@/net';
import { ElMessage } from 'element-plus';

// 状态管理
const authStore = useAuthStore();
const router = useRouter();
const loading = ref(false);
const activeTab = ref('account'); // account 或 qrcode
const rememberMe = ref(false);

// 表单数据
const form = reactive({
  username: '',
  password: '',
  role: 'teacher', // 'teacher', 'student', 'admin'
});

// 表单校验规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 5, max: 20, message: '密码长度为5-20个字符', trigger: 'blur' }
  ]
};

// 登录方法
const handleLogin = async (formEl) => {
  if (!formEl) return;
  
  await formEl.validate(async (valid, fields) => {
    if (valid) {
      loading.value = true;
      try {
        // 实际项目应该调用真实的登录接口
        // const response = await post('/auth/login', form);
        
        // 模拟登录，根据角色返回不同的用户信息
        const userInfo = mockLogin(form);
        
        // 登录成功
        authStore.login(userInfo);
        
        // 根据角色跳转到不同的首页
        if (form.role === 'admin') {
          router.push('/admin/dashboard');
        } else if (form.role === 'teacher') {
          router.push('/teacher/dashboard');
        } else {
          router.push('/student/dashboard');
        }
        
        ElMessage.success('登录成功！');
      } catch (error) {
        console.error('登录失败:', error);
        ElMessage.error('登录失败，请检查用户名和密码');
      } finally {
        loading.value = false;
      }
    } else {
      console.log('表单校验失败', fields);
    }
  });
};

// 模拟登录，根据角色返回不同的用户信息
const mockLogin = (form) => {
  const { username, role } = form;
  
  let roleId;
  switch (role) {
    case 'admin':
      roleId = 1;
      break;
    case 'teacher':
      roleId = 2;
      break;
    case 'student':
      roleId = 3;
      break;
    default:
      roleId = 2;
  }
  
  return {
    id: Math.floor(Math.random() * 1000),
    username,
    name: `${role === 'admin' ? '管理员' : role === 'teacher' ? '教师' : '学生'}_${username}`,
    roleId,
    role,
    token: `mock_token_${role}_${Date.now()}`,
    avatar: `https://i.pravatar.cc/150?u=${username}`
  };
};

// 切换标签页
const handleTabChange = (tab) => {
  activeTab.value = tab;
};

// 切换记住我
const toggleRememberMe = () => {
  rememberMe.value = !rememberMe.value;
};

// 表单引用
const formRef = ref(null);

// 切换角色
const roleOptions = [
  { label: '教师', value: 'teacher' },
  { label: '学生', value: 'student' },
  { label: '管理员', value: 'admin' }
];

// 示例账号快速填充
const fillDemoAccount = (role) => {
  form.role = role;
  form.username = `demo_${role}`;
  form.password = 'password';
};
</script>

<template>
  <div class="login-container">
    <div class="login-content">
      <div class="login-left">
        <div class="login-branding">
          <div class="login-logo">
            <img src="@/assets/logo.svg" alt="Logo" />
          </div>
          <h1 class="login-title">智慧教学实训平台</h1>
          <p class="login-subtitle">基于开源大语言模型的智能实训教学系统</p>
        </div>
        <div class="login-features">
          <div class="feature-item">
            <div class="feature-icon">
              <i class="el-icon-edit-outline"></i>
            </div>
            <div class="feature-text">
              <h3>智能备课与设计</h3>
              <p>根据课程大纲自动生成教案、课件与考核内容</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <i class="el-icon-data-analysis"></i>
            </div>
            <div class="feature-text">
              <h3>学情分析与可视化</h3>
              <p>自动分析学习数据，提供个性化学习建议</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <i class="el-icon-chat-dot-round"></i>
            </div>
            <div class="feature-text">
              <h3>实时学习助手</h3>
              <p>提供全时在线练习与个性化指导</p>
            </div>
          </div>
        </div>
      </div>
      
      <div class="login-right">
        <div class="login-form-container">
          <div class="login-header">
            <h2 class="login-welcome">欢迎使用</h2>
            <p class="login-desc">请使用您的账号登录系统</p>
          </div>
          
          <div class="login-tabs">
            <div 
              :class="['tab-item', { active: activeTab === 'account' }]"
              @click="handleTabChange('account')"
            >
              账号密码登录
            </div>
            <div 
              :class="['tab-item', { active: activeTab === 'qrcode' }]"
              @click="handleTabChange('qrcode')"
            >
              扫码登录
            </div>
          </div>
          
          <div v-if="activeTab === 'account'" class="login-form-content">
            <el-form 
              ref="formRef"
              :model="form" 
              :rules="rules"
              label-position="top"
              @keyup.enter="handleLogin(formRef)"
            >
              <!-- 角色选择 -->
              <el-form-item label="登录角色">
                <el-radio-group v-model="form.role" class="role-selector">
                  <el-radio-button 
                    v-for="option in roleOptions" 
                    :key="option.value" 
                    :label="option.value"
                    :class="{ 'role-teacher': option.value === 'teacher', 'role-student': option.value === 'student', 'role-admin': option.value === 'admin' }"
                  >
                    {{ option.label }}
                  </el-radio-button>
                </el-radio-group>
              </el-form-item>
              
              <!-- 用户名 -->
              <el-form-item label="用户名" prop="username">
                <el-input 
                  v-model="form.username" 
                  placeholder="请输入用户名" 
                  prefix-icon="el-icon-user"
                />
              </el-form-item>
              
              <!-- 密码 -->
              <el-form-item label="密码" prop="password">
                <el-input 
                  v-model="form.password" 
                  type="password" 
                  placeholder="请输入密码" 
                  prefix-icon="el-icon-lock"
                  show-password
                />
              </el-form-item>
              
              <!-- 记住我和忘记密码 -->
              <div class="login-options">
                <el-checkbox v-model="rememberMe">记住我</el-checkbox>
                <el-link type="primary">忘记密码？</el-link>
              </div>
              
              <!-- 登录按钮 -->
              <el-button 
                type="primary" 
                :loading="loading" 
                class="login-button"
                @click="handleLogin(formRef)"
              >
                {{ loading ? '登录中...' : '登 录' }}
              </el-button>
            </el-form>
            
            <!-- 示例账号 -->
            <div class="demo-accounts">
              <p class="demo-title">示例账号快速登录:</p>
              <div class="demo-buttons">
                <el-button 
                  size="small" 
                  @click="fillDemoAccount('teacher')"
                >
                  教师体验账号
                </el-button>
                <el-button 
                  size="small" 
                  @click="fillDemoAccount('student')"
                >
                  学生体验账号
                </el-button>
                <el-button 
                  size="small" 
                  @click="fillDemoAccount('admin')"
                >
                  管理员体验账号
                </el-button>
              </div>
            </div>
            
            <!-- 其他登录方式 -->
            <div class="other-login">
              <div class="divider">
                <span>其他登录方式</span>
              </div>
              <div class="social-login">
                <el-button circle icon="el-icon-message"></el-button>
                <el-button circle icon="el-icon-phone"></el-button>
                <el-button circle icon="el-icon-user"></el-button>
              </div>
            </div>
          </div>
          
          <!-- 扫码登录 -->
          <div v-else class="login-qrcode">
            <div class="qrcode-container">
              <div class="qrcode-image">
                <!-- 实际项目中应当生成真实的二维码 -->
                <div class="mock-qrcode"></div>
              </div>
              <p class="qrcode-tip">请使用微信或企业微信扫码登录</p>
            </div>
          </div>
          
        </div>
      </div>
    </div>
    
    <!-- 页脚 -->
    <div class="login-footer">
      <p>© {{ new Date().getFullYear() }} 智慧教学实训平台 版权所有</p>
    </div>
  </div>
</template>

<style scoped>
/* 登录页整体布局 */
.login-container {
  @apply min-h-screen w-full flex items-center justify-center p-4;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.login-content {
  @apply flex rounded-xl overflow-hidden shadow-2xl w-full max-w-6xl bg-white;
  height: 600px;
}

/* 左侧品牌区域 */
.login-left {
  @apply w-1/2 p-10 flex flex-col justify-between text-white relative overflow-hidden;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
}

.login-left::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M11 18c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm48 25c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm-43-7c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm63 31c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM34 90c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm56-76c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM12 86c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm28-65c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm23-11c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-6 60c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm29 22c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zM32 63c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm57-13c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-9-21c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM60 91c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM35 41c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM12 60c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2z' fill='%23ffffff' fill-opacity='0.05' fill-rule='evenodd'/%3E%3C/svg%3E");
  z-index: 0;
}

.login-branding {
  @apply relative z-10;
}

.login-logo {
  @apply w-20 h-20 bg-white bg-opacity-10 rounded-2xl p-4 mb-6 flex items-center justify-center;
  backdrop-filter: blur(10px);
}

.login-logo img {
  @apply w-full h-full;
  filter: brightness(0) invert(1);
}

.login-title {
  @apply text-3xl font-bold mb-2;
}

.login-subtitle {
  @apply text-white text-opacity-80;
}

.login-features {
  @apply space-y-6 relative z-10;
}

.feature-item {
  @apply flex items-start space-x-4;
}

.feature-icon {
  @apply w-10 h-10 rounded-lg bg-white bg-opacity-10 flex items-center justify-center text-xl;
  backdrop-filter: blur(5px);
}

.feature-text h3 {
  @apply text-lg font-semibold mb-1;
}

.feature-text p {
  @apply text-sm text-white text-opacity-70;
}

/* 右侧表单区域 */
.login-right {
  @apply w-1/2 p-10 flex items-center justify-center;
  background-color: #ffffff;
}

.login-form-container {
  @apply w-full max-w-md;
}

.login-header {
  @apply mb-8 text-center;
}

.login-welcome {
  @apply text-2xl font-bold text-gray-800 mb-2;
}

.login-desc {
  @apply text-gray-500;
}

.login-tabs {
  @apply flex mb-6 border-b border-gray-200;
}

.tab-item {
  @apply px-4 py-2 text-gray-500 cursor-pointer border-b-2 border-transparent transition-all duration-200;
}

.tab-item:hover {
  @apply text-gray-700;
}

.tab-item.active {
  @apply text-blue-600 border-blue-600 font-medium;
}

.login-form-content {
  @apply space-y-4;
}

/* 角色选择器样式 */
.role-selector {
  @apply w-full flex;
}

:deep(.el-radio-button) {
  @apply flex-1;
}

:deep(.el-radio-button__inner) {
  @apply w-full flex justify-center transition-all duration-200;
}

:deep(.role-teacher .el-radio-button__inner.is-active) {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%) !important;
  border-color: #1e3c72 !important;
  box-shadow: -1px 0 0 0 #1e3c72 !important;
}

:deep(.role-student .el-radio-button__inner.is-active) {
  background: linear-gradient(135deg, #134e5e 0%, #71b280 100%) !important;
  border-color: #134e5e !important;
  box-shadow: -1px 0 0 0 #134e5e !important;
}

:deep(.role-admin .el-radio-button__inner.is-active) {
  background: linear-gradient(135deg, #232526 0%, #414345 100%) !important;
  border-color: #232526 !important;
  box-shadow: -1px 0 0 0 #232526 !important;
}

/* 登录选项 */
.login-options {
  @apply flex justify-between items-center;
}

/* 登录按钮 */
.login-button {
  @apply w-full h-12 text-base mt-4;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%) !important;
  border: none !important;
  transition: transform 0.2s, box-shadow 0.2s;
}

.login-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(45, 82, 152, 0.3);
}

.login-button:active {
  transform: translateY(0);
}

.login-qrcode {
  @apply py-8;
}

.qrcode-container {
  @apply flex flex-col items-center;
}

.qrcode-image {
  @apply mb-4;
}

.mock-qrcode {
  @apply w-48 h-48 bg-gray-100 border-2 border-gray-300 rounded-lg flex items-center justify-center;
  background-image: repeating-linear-gradient(
    45deg, 
    #e5e7eb 25%, 
    transparent 25%, 
    transparent 75%, 
    #e5e7eb 75%, 
    #e5e7eb
  ),
  repeating-linear-gradient(
    45deg, 
    #e5e7eb 25%, 
    #f3f4f6 25%, 
    #f3f4f6 75%, 
    #e5e7eb 75%, 
    #e5e7eb
  );
  background-position: 0 0, 8px 8px;
  background-size: 16px 16px;
}

.mock-qrcode::after {
  content: "二维码";
  @apply text-gray-400;
}

.qrcode-tip {
  @apply text-gray-500;
}

.demo-accounts {
  @apply mt-6 pt-4 border-t border-gray-100;
}

.demo-title {
  @apply text-sm text-gray-500 mb-2;
}

.demo-buttons {
  @apply flex flex-wrap gap-2;
}

.other-login {
  @apply mt-8;
}

.divider {
  @apply flex items-center;
}

.divider::before,
.divider::after {
  content: '';
  @apply flex-1 border-t border-gray-200;
}

.divider span {
  @apply px-2 text-sm text-gray-500;
}

.social-login {
  @apply flex justify-center mt-4 gap-4;
}

.login-footer {
  @apply py-4 text-center text-gray-500 text-sm;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .login-content {
    @apply flex-col;
    height: auto;
  }
  
  .login-left, .login-right {
    @apply w-full;
  }
  
  .login-left {
    @apply py-8;
  }
}
</style> 