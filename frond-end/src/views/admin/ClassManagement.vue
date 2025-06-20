<script setup>
import { ref, onMounted, reactive } from 'vue';
import { get, post, put, del } from '@/net';
import LucideIcon from '@/components/icons/LucideIcon.vue';

// 班级列表数据
const classes = ref([]);
const loading = ref(true);
const totalClasses = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 弹窗控制
const showClassModal = ref(false);
const showStudentsModal = ref(false);
const modalTitle = ref('添加班级');
const isEditing = ref(false);

// 当前选中的班级
const selectedClass = ref(null);

// 搜索条件
const searchQuery = ref('');

// 表单数据
const formClass = reactive({
  id: null,
  name: '',
  gradeLevel: '',
  teacherId: null,
  description: ''
});

// 表单验证
const formErrors = reactive({
  name: '',
  gradeLevel: '',
  teacherId: ''
});

// 教师列表
const teachers = ref([]);

// 班级学生列表
const classStudents = ref([]);
const availableStudents = ref([]);
const selectedStudents = ref([]);

// 获取班级列表
const fetchClasses = () => {
  loading.value = true;
  get('/api/classes', { 
    page: currentPage.value,
    size: pageSize.value,
    query: searchQuery.value
  }, 
  (message, data) => {
    classes.value = data.list;
    totalClasses.value = data.total;
    loading.value = false;
  },
  () => {
    loading.value = false;
  });
};

// 获取教师列表
const fetchTeachers = () => {
  get('/api/teachers', null, 
    (message, data) => {
      teachers.value = data;
    }
  );
};

// 获取班级学生
const fetchClassStudents = (classId) => {
  get(`/api/classes/${classId}/students`, null, 
    (message, data) => {
      classStudents.value = data;
    }
  );
};

// 获取可分配的学生
const fetchAvailableStudents = () => {
  get('/api/students/available', null, 
    (message, data) => {
      availableStudents.value = data;
    }
  );
};

// 页面加载时获取数据
onMounted(() => {
  fetchClasses();
  fetchTeachers();
});

// 添加班级
const addClass = () => {
  modalTitle.value = '添加班级';
  isEditing.value = false;
  resetForm();
  showClassModal.value = true;
};

// 编辑班级
const editClass = (classItem) => {
  modalTitle.value = '编辑班级';
  isEditing.value = true;
  Object.assign(formClass, classItem);
  showClassModal.value = true;
};

// 删除班级
const deleteClass = (classId) => {
  if (confirm('确定要删除该班级吗？')) {
    del(`/api/classes/${classId}`, null,
      () => {
        fetchClasses();
      }
    );
  }
};

// 管理班级学生
const manageStudents = (classItem) => {
  selectedClass.value = classItem;
  fetchClassStudents(classItem.id);
  fetchAvailableStudents();
  showStudentsModal.value = true;
};

// 添加学生到班级
const addStudentsToClass = () => {
  if (selectedStudents.value.length === 0 || !selectedClass.value) {
    return;
  }
  
  post(`/api/classes/${selectedClass.value.id}/students`, {
    studentIds: selectedStudents.value
  }, 
  () => {
    fetchClassStudents(selectedClass.value.id);
    selectedStudents.value = [];
  });
};

// 从班级移除学生
const removeStudentFromClass = (studentId) => {
  if (!selectedClass.value) return;
  
  del(`/api/classes/${selectedClass.value.id}/students/${studentId}`, null,
    () => {
      fetchClassStudents(selectedClass.value.id);
    }
  );
};

// 重置表单
const resetForm = () => {
  formClass.id = null;
  formClass.name = '';
  formClass.gradeLevel = '';
  formClass.teacherId = null;
  formClass.description = '';
  
  // 清除验证错误
  Object.keys(formErrors).forEach(key => {
    formErrors[key] = '';
  });
};

// 提交表单
const submitForm = () => {
  // 验证表单
  let isValid = true;
  
  if (!formClass.name) {
    formErrors.name = '请输入班级名称';
    isValid = false;
  }
  
  if (!formClass.gradeLevel) {
    formErrors.gradeLevel = '请选择年级';
    isValid = false;
  }
  
  if (!isValid) {
    return;
  }
  
  if (isEditing.value) {
    // 编辑班级
    put(`/api/classes/${formClass.id}`, formClass,
      () => {
        showClassModal.value = false;
        fetchClasses();
      }
    );
  } else {
    // 添加班级
    post('/api/classes', formClass,
      () => {
        showClassModal.value = false;
        fetchClasses();
      }
    );
  }
};

// 分页变化
const handlePageChange = (page) => {
  currentPage.value = page;
  fetchClasses();
};

// 搜索
const handleSearch = () => {
  currentPage.value = 1;
  fetchClasses();
};

// 重置搜索
const resetSearch = () => {
  searchQuery.value = '';
  currentPage.value = 1;
  fetchClasses();
};

// 年级选项
const gradeLevels = [
  '小学一年级', '小学二年级', '小学三年级', '小学四年级', '小学五年级', '小学六年级',
  '初中一年级', '初中二年级', '初中三年级',
  '高中一年级', '高中二年级', '高中三年级'
];
</script>

<template>
  <div class="content-section">
    <div class="content-header">
      <h1 class="content-title">班级管理</h1>
      <button @click="addClass" class="btn btn-primary flex items-center">
        <LucideIcon name="plus" size="16" class="mr-1" />
        添加班级
      </button>
    </div>
    
    <!-- 搜索栏 -->
    <div class="bg-white p-4 rounded-lg shadow-sm mb-6">
      <div class="flex flex-wrap gap-4">
        <div class="flex-1 min-w-[200px]">
          <label class="form-label">班级搜索</label>
          <div class="relative">
            <input 
              type="text" 
              v-model="searchQuery" 
              placeholder="搜索班级名称或年级" 
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
    
    <!-- 班级列表 -->
    <div class="bg-white rounded-lg shadow-sm overflow-hidden">
      <div class="table-container">
        <table class="table">
          <thead class="table-header">
            <tr>
              <th class="table-header-cell">ID</th>
              <th class="table-header-cell">班级名称</th>
              <th class="table-header-cell">年级</th>
              <th class="table-header-cell">班主任</th>
              <th class="table-header-cell">学生人数</th>
              <th class="table-header-cell">描述</th>
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
            <tr v-else-if="classes.length === 0" class="table-row">
              <td colspan="7" class="table-cell text-center py-8 text-gray-500">
                没有找到班级数据
              </td>
            </tr>
            <tr v-for="classItem in classes" :key="classItem.id" class="table-row">
              <td class="table-cell">{{ classItem.id }}</td>
              <td class="table-cell font-medium text-gray-800">{{ classItem.name }}</td>
              <td class="table-cell">{{ classItem.gradeLevel }}</td>
              <td class="table-cell">{{ classItem.teacherName || '未分配' }}</td>
              <td class="table-cell">{{ classItem.studentCount || 0 }}</td>
              <td class="table-cell truncate max-w-xs">{{ classItem.description || '-' }}</td>
              <td class="table-cell text-right">
                <div class="flex justify-end space-x-2">
                  <button 
                    @click="manageStudents(classItem)" 
                    class="p-1 rounded hover:bg-gray-100 text-indigo-600"
                    title="管理学生"
                  >
                    <LucideIcon name="users" size="16" />
                  </button>
                  <button 
                    @click="editClass(classItem)" 
                    class="p-1 rounded hover:bg-gray-100 text-blue-600"
                    title="编辑"
                  >
                    <LucideIcon name="edit" size="16" />
                  </button>
                  <button 
                    @click="deleteClass(classItem.id)" 
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
          共 {{ totalClasses }} 个班级
        </div>
        <div class="flex space-x-1">
          <button 
            v-for="page in Math.ceil(totalClasses / pageSize)"
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
    
    <!-- 添加/编辑班级弹窗 -->
    <div v-if="showClassModal" class="fixed inset-0 z-50 overflow-y-auto">
      <div class="flex items-center justify-center min-h-screen px-4">
        <div class="fixed inset-0 bg-black bg-opacity-25" @click="showClassModal = false"></div>
        
        <div class="bg-white rounded-lg shadow-xl w-full max-w-md p-6 z-10">
          <div class="flex justify-between items-center mb-4">
            <h3 class="text-lg font-semibold text-gray-900">{{ modalTitle }}</h3>
            <button @click="showClassModal = false" class="text-gray-400 hover:text-gray-500">
              <LucideIcon name="x" size="20" />
            </button>
          </div>
          
          <form @submit.prevent="submitForm">
            <div class="space-y-4">
              <div>
                <label class="form-label">班级名称</label>
                <input 
                  type="text" 
                  v-model="formClass.name" 
                  class="form-input" 
                  :class="{ 'border-red-500': formErrors.name }"
                  placeholder="例如：一班、二班"
                />
                <div v-if="formErrors.name" class="form-error">{{ formErrors.name }}</div>
              </div>
              
              <div>
                <label class="form-label">年级</label>
                <select 
                  v-model="formClass.gradeLevel" 
                  class="form-select"
                  :class="{ 'border-red-500': formErrors.gradeLevel }"
                >
                  <option value="" disabled>请选择年级</option>
                  <option v-for="grade in gradeLevels" :key="grade" :value="grade">
                    {{ grade }}
                  </option>
                </select>
                <div v-if="formErrors.gradeLevel" class="form-error">{{ formErrors.gradeLevel }}</div>
              </div>
              
              <div>
                <label class="form-label">班主任</label>
                <select v-model="formClass.teacherId" class="form-select">
                  <option :value="null">未分配</option>
                  <option v-for="teacher in teachers" :key="teacher.id" :value="teacher.id">
                    {{ teacher.name }}
                  </option>
                </select>
              </div>
              
              <div>
                <label class="form-label">班级描述</label>
                <textarea 
                  v-model="formClass.description" 
                  class="form-input min-h-[80px]"
                  placeholder="班级介绍或备注信息"
                ></textarea>
              </div>
            </div>
            
            <div class="mt-6 flex justify-end space-x-3">
              <button 
                type="button" 
                class="btn btn-outline" 
                @click="showClassModal = false"
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
    
    <!-- 管理学生弹窗 -->
    <div v-if="showStudentsModal" class="fixed inset-0 z-50 overflow-y-auto">
      <div class="flex items-center justify-center min-h-screen px-4">
        <div class="fixed inset-0 bg-black bg-opacity-25" @click="showStudentsModal = false"></div>
        
        <div class="bg-white rounded-lg shadow-xl w-full max-w-4xl p-6 z-10">
          <div class="flex justify-between items-center mb-4">
            <h3 class="text-lg font-semibold text-gray-900">
              管理班级学生 - {{ selectedClass?.name }}
            </h3>
            <button @click="showStudentsModal = false" class="text-gray-400 hover:text-gray-500">
              <LucideIcon name="x" size="20" />
            </button>
          </div>
          
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <!-- 已有学生列表 -->
            <div>
              <h4 class="text-sm font-medium text-gray-700 mb-2">当前班级学生</h4>
              <div class="border rounded-lg overflow-hidden">
                <div class="max-h-[300px] overflow-y-auto">
                  <table class="w-full">
                    <thead class="bg-gray-50">
                      <tr>
                        <th class="px-4 py-2 text-left text-xs font-medium text-gray-500">姓名</th>
                        <th class="px-4 py-2 text-left text-xs font-medium text-gray-500">学号</th>
                        <th class="px-4 py-2 text-right text-xs font-medium text-gray-500">操作</th>
                      </tr>
                    </thead>
                    <tbody class="divide-y divide-gray-100">
                      <tr v-if="classStudents.length === 0">
                        <td colspan="3" class="px-4 py-4 text-center text-sm text-gray-500">
                          暂无学生
                        </td>
                      </tr>
                      <tr v-for="student in classStudents" :key="student.id" class="hover:bg-gray-50">
                        <td class="px-4 py-2 text-sm text-gray-900">{{ student.name }}</td>
                        <td class="px-4 py-2 text-sm text-gray-500">{{ student.studentNumber }}</td>
                        <td class="px-4 py-2 text-right">
                          <button 
                            @click="removeStudentFromClass(student.id)" 
                            class="p-1 rounded text-red-600 hover:bg-red-50"
                            title="移除"
                          >
                            <LucideIcon name="user-minus" size="16" />
                          </button>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
            
            <!-- 可添加学生列表 -->
            <div>
              <h4 class="text-sm font-medium text-gray-700 mb-2">添加学生</h4>
              <div class="border rounded-lg overflow-hidden">
                <div class="max-h-[300px] overflow-y-auto">
                  <table class="w-full">
                    <thead class="bg-gray-50">
                      <tr>
                        <th class="px-4 py-2 text-left text-xs font-medium text-gray-500">选择</th>
                        <th class="px-4 py-2 text-left text-xs font-medium text-gray-500">姓名</th>
                        <th class="px-4 py-2 text-left text-xs font-medium text-gray-500">学号</th>
                      </tr>
                    </thead>
                    <tbody class="divide-y divide-gray-100">
                      <tr v-if="availableStudents.length === 0">
                        <td colspan="3" class="px-4 py-4 text-center text-sm text-gray-500">
                          没有可添加的学生
                        </td>
                      </tr>
                      <tr v-for="student in availableStudents" :key="student.id" class="hover:bg-gray-50">
                        <td class="px-4 py-2">
                          <input 
                            type="checkbox" 
                            :value="student.id" 
                            v-model="selectedStudents"
                            class="form-checkbox"
                          />
                        </td>
                        <td class="px-4 py-2 text-sm text-gray-900">{{ student.name }}</td>
                        <td class="px-4 py-2 text-sm text-gray-500">{{ student.studentNumber }}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
              
              <div class="mt-4 flex justify-end">
                <button 
                  @click="addStudentsToClass" 
                  class="btn btn-primary"
                  :disabled="selectedStudents.length === 0"
                >
                  <LucideIcon name="user-plus" size="16" class="mr-1" />
                  添加所选学生
                </button>
              </div>
            </div>
          </div>
          
          <div class="mt-6 flex justify-end">
            <button 
              type="button" 
              class="btn btn-outline" 
              @click="showStudentsModal = false"
            >
              关闭
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>