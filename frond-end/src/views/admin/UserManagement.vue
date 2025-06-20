<script setup>
import { ref, onMounted, reactive } from 'vue';
import { get, post, put, del } from '@/net';
import LucideIcon from '@/components/icons/LucideIcon.vue';

// 用户列表数据
const users = ref([]);
const loading = ref(true);
const totalUsers = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 弹窗控制
const showUserModal = ref(false);
const modalTitle = ref('添加用户');
const isEditing = ref(false);

// 搜索条件
const searchQuery = ref('');
const filterRole = ref('all');

// 表单数据
const formUser = reactive({
  id: null,
  username: '',
  password: '',
  name: '',
  email: '',
  phone: '',
  roleId: 3, // 默认为学生
  status: 1
});

// 表单验证
const formErrors = reactive({
  username: '',
  password: '',
  name: '',
  email: ''
});

// 角色映射
const roleOptions = [
  { label: '管理员', value: 1 },
  { label: '教师', value: 2 },
  { label: '学生', value: 3 }
];

// 状态映射
const statusOptions = [
  { label: '正常', value: 1 },
  { label: '禁用', value: 0 }
];

// 获取用户列表
const fetchUsers = () => {
  loading.value = true;
  get('/api/users', { 
    page: currentPage.value,
    size: pageSize.value,
    query: searchQuery.value,
    role: filterRole.value
  }, 
  (message, data) => {
    users.value = data.list;
    totalUsers.value = data.total;
    loading.value = false;
  },
  () => {
    loading.value = false;
  });
};

// 页面加载时获取数据
onMounted(() => {
  fetchUsers();
});

// 添加用户
const addUser = () => {
  modalTitle.value = '添加用户';
  isEditing.value = false;
  resetForm();
  showUserModal.value = true;
};

// 编辑用户
const editUser = (user) => {
  modalTitle.value = '编辑用户';
  isEditing.value = true;
  Object.assign(formUser, user);
  showUserModal.value = true;
};

// 删除用户
const deleteUser = (userId) => {
  if (confirm('确定要删除该用户吗？')) {
    del(`/api/users/${userId}`, null,
      () => {
        fetchUsers();
      }
    );
  }
};

// 重置表单
const resetForm = () => {
  formUser.id = null;
  formUser.username = '';
  formUser.password = '';
  formUser.name = '';
  formUser.email = '';
  formUser.phone = '';
  formUser.roleId = 3;
  formUser.status = 1;
  
  // 清除验证错误
  Object.keys(formErrors).forEach(key => {
    formErrors[key] = '';
  });
};

// 提交表单
const submitForm = () => {
  // 验证表单
  let isValid = true;
  
  if (!formUser.username) {
    formErrors.username = '请输入用户名';
    isValid = false;
  }
  
  if (!isEditing.value && !formUser.password) {
    formErrors.password = '请输入密码';
    isValid = false;
  }
  
  if (!formUser.name) {
    formErrors.name = '请输入姓名';
    isValid = false;
  }
  
  if (!isValid) {
    return;
  }
  
  if (isEditing.value) {
    // 编辑用户
    put(`/api/users/${formUser.id}`, formUser,
      () => {
        showUserModal.value = false;
        fetchUsers();
      }
    );
  } else {
    // 添加用户
    post('/api/users', formUser,
      () => {
        showUserModal.value = false;
        fetchUsers();
      }
    );
  }
};

// 分页变化
const handlePageChange = (page) => {
  currentPage.value = page;
  fetchUsers();
};

// 搜索
const handleSearch = () => {
  currentPage.value = 1;
  fetchUsers();
};

// 重置搜索
const resetSearch = () => {
  searchQuery.value = '';
  filterRole.value = 'all';
  currentPage.value = 1;
  fetchUsers();
};
</script>

<template>
  <div class="content-section">
    <div class="content-header">
      <h1 class="content-title">用户管理</h1>
      <button @click="addUser" class="btn btn-primary flex items-center">
        <LucideIcon name="user-plus" size="16" class="mr-1" />
        添加用户
      </button>
    </div>
    
    <!-- 搜索栏 -->
    <div class="bg-white p-4 rounded-lg shadow-sm mb-6">
      <div class="flex flex-wrap gap-4">
        <div class="flex-1 min-w-[200px]">
          <label class="form-label">关键词搜索</label>
          <div class="relative">
            <input 
              type="text" 
              v-model="searchQuery" 
              placeholder="搜索用户名、姓名或邮箱" 
              class="form-input pl-9"
              @keyup.enter="handleSearch"
            />
            <LucideIcon 
              name="search" 
              size="18" 
              class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
            />
          </div>
        </div>
        
        <div class="w-40">
          <label class="form-label">用户角色</label>
          <select v-model="filterRole" class="form-select">
            <option value="all">所有角色</option>
            <option v-for="role in roleOptions" :key="role.value" :value="role.value">
              {{ role.label }}
            </option>
          </select>
        </div>
        
        <div class="flex items-end space-x-2">
          <button @click="handleSearch" class="btn btn-primary">
            <LucideIcon name="search" size="16" class="mr-1" />
            搜索
          </button>
          <button @click="resetSearch" class="btn btn-outline">
            <LucideIcon name="refresh-cw" size="16" class="mr-1" />
            重置
          </button>
        </div>
      </div>
    </div>
    
    <!-- 用户列表 -->
    <div class="bg-white rounded-lg shadow-sm overflow-hidden">
      <div class="table-container">
        <table class="table">
          <thead class="table-header">
            <tr>
              <th class="table-header-cell">ID</th>
              <th class="table-header-cell">用户名</th>
              <th class="table-header-cell">姓名</th>
              <th class="table-header-cell">角色</th>
              <th class="table-header-cell">邮箱</th>
              <th class="table-header-cell">状态</th>
              <th class="table-header-cell text-right">操作</th>
            </tr>
          </thead>
          <tbody class="table-body">
            <tr v-if="loading" class="table-row">
              <td colspan="7" class="table-cell text-center py-8">
                <div class="flex justify-center">
                  <LucideIcon name="loader-2" class="animate-spin text-indigo-500" />
                </div>
              </td>
            </tr>
            <tr v-else-if="users.length === 0" class="table-row">
              <td colspan="7" class="table-cell text-center py-8 text-gray-500">
                没有找到用户数据
              </td>
            </tr>
            <tr v-for="user in users" :key="user.id" class="table-row">
              <td class="table-cell">{{ user.id }}</td>
              <td class="table-cell font-medium text-gray-800">{{ user.username }}</td>
              <td class="table-cell">{{ user.name }}</td>
              <td class="table-cell">
                <span 
                  class="badge"
                  :class="{
                    'badge-primary': user.roleId === 1,
                    'badge-secondary': user.roleId === 2,
                    'badge-success': user.roleId === 3
                  }"
                >
                  {{ roleOptions.find(r => r.value === user.roleId)?.label }}
                </span>
              </td>
              <td class="table-cell">{{ user.email }}</td>
              <td class="table-cell">
                <span 
                  class="badge"
                  :class="{
                    'badge-success': user.status === 1,
                    'badge-danger': user.status === 0
                  }"
                >
                  {{ user.status === 1 ? '正常' : '禁用' }}
                </span>
              </td>
              <td class="table-cell text-right">
                <div class="flex justify-end space-x-2">
                  <button 
                    @click="editUser(user)" 
                    class="p-1 rounded hover:bg-gray-100 text-blue-600"
                    title="编辑"
                  >
                    <LucideIcon name="edit" size="16" />
                  </button>
                  <button 
                    @click="deleteUser(user.id)" 
                    class="p-1 rounded hover:bg-gray-100 text-red-600"
                    title="删除"
                  >
                    <LucideIcon name="trash-2" size="16" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <!-- 分页 -->
      <div class="px-6 py-4 border-t border-gray-100 flex items-center justify-between">
        <div class="text-sm text-gray-500">
          共 {{ totalUsers }} 条记录
        </div>
        <div class="flex space-x-1">
          <button 
            v-for="page in Math.ceil(totalUsers / pageSize)"
            :key="page"
            @click="handlePageChange(page)"
            class="px-3 py-1 text-sm rounded"
            :class="currentPage === page 
              ? 'bg-indigo-600 text-white' 
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'"
          >
            {{ page }}
          </button>
        </div>
      </div>
    </div>
    
    <!-- 添加/编辑用户弹窗 -->
    <div v-if="showUserModal" class="fixed inset-0 z-50 overflow-y-auto">
      <div class="flex items-center justify-center min-h-screen px-4">
        <div class="fixed inset-0 bg-black bg-opacity-25" @click="showUserModal = false"></div>
        
        <div class="bg-white rounded-lg shadow-xl w-full max-w-md p-6 z-10">
          <div class="flex justify-between items-center mb-4">
            <h3 class="text-lg font-semibold text-gray-900">{{ modalTitle }}</h3>
            <button @click="showUserModal = false" class="text-gray-400 hover:text-gray-500">
              <LucideIcon name="x" size="20" />
            </button>
          </div>
          
          <form @submit.prevent="submitForm">
            <div class="space-y-4">
              <div>
                <label class="form-label">用户名</label>
                <input 
                  type="text" 
                  v-model="formUser.username" 
                  class="form-input" 
                  :class="{ 'border-red-500': formErrors.username }"
                />
                <div v-if="formErrors.username" class="form-error">{{ formErrors.username }}</div>
              </div>
              
              <div v-if="!isEditing">
                <label class="form-label">密码</label>
                <input 
                  type="password" 
                  v-model="formUser.password" 
                  class="form-input"
                  :class="{ 'border-red-500': formErrors.password }"
                />
                <div v-if="formErrors.password" class="form-error">{{ formErrors.password }}</div>
              </div>
              
              <div>
                <label class="form-label">姓名</label>
                <input 
                  type="text" 
                  v-model="formUser.name" 
                  class="form-input"
                  :class="{ 'border-red-500': formErrors.name }"
                />
                <div v-if="formErrors.name" class="form-error">{{ formErrors.name }}</div>
              </div>
              
              <div>
                <label class="form-label">邮箱</label>
                <input 
                  type="email" 
                  v-model="formUser.email" 
                  class="form-input"
                  :class="{ 'border-red-500': formErrors.email }"
                />
                <div v-if="formErrors.email" class="form-error">{{ formErrors.email }}</div>
              </div>
              
              <div>
                <label class="form-label">手机号</label>
                <input type="tel" v-model="formUser.phone" class="form-input" />
              </div>
              
              <div>
                <label class="form-label">角色</label>
                <select v-model="formUser.roleId" class="form-select">
                  <option v-for="role in roleOptions" :key="role.value" :value="role.value">
                    {{ role.label }}
                  </option>
                </select>
              </div>
              
              <div>
                <label class="form-label">状态</label>
                <select v-model="formUser.status" class="form-select">
                  <option v-for="status in statusOptions" :key="status.value" :value="status.value">
                    {{ status.label }}
                  </option>
                </select>
              </div>
            </div>
            
            <div class="mt-6 flex justify-end space-x-3">
              <button 
                type="button" 
                class="btn btn-outline" 
                @click="showUserModal = false"
              >
                取消
              </button>
              <button type="submit" class="btn btn-primary">
                保存
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template> 