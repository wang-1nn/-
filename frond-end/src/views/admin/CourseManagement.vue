<script setup>
import { ref, onMounted, reactive } from 'vue';
import { get, post, put, del } from '@/net';
import LucideIcon from '@/components/icons/LucideIcon.vue';

// 课程列表数据
const courses = ref([]);
const loading = ref(true);
const totalCourses = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 弹窗控制
const showCourseModal = ref(false);
const modalTitle = ref('添加课程');
const isEditing = ref(false);

// 搜索条件
const searchQuery = ref('');
const filterSubject = ref('all');

// 表单数据
const formCourse = reactive({
  id: null,
  name: '',
  code: '',
  subject: '',
  description: '',
  teacherId: null,
  status: 1,
  creditHours: 0
});

// 表单验证
const formErrors = reactive({
  name: '',
  code: '',
  subject: '',
  teacherId: ''
});

// 教师列表
const teachers = ref([]);

// 状态映射
const statusOptions = [
  { label: '正常', value: 1 },
  { label: '禁用', value: 0 }
];

// 学科列表
const subjects = [
  '语文', '数学', '英语', '物理', '化学', '生物', 
  '历史', '地理', '政治', '信息技术', '音乐', '美术', '体育'
];

// 获取课程列表
const fetchCourses = () => {
  loading.value = true;
  get('/api/courses', { 
    page: currentPage.value,
    size: pageSize.value,
    query: searchQuery.value,
    subject: filterSubject.value === 'all' ? '' : filterSubject.value
  }, 
  (message, data) => {
    courses.value = data.list;
    totalCourses.value = data.total;
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

// 页面加载时获取数据
onMounted(() => {
  fetchCourses();
  fetchTeachers();
});

// 添加课程
const addCourse = () => {
  modalTitle.value = '添加课程';
  isEditing.value = false;
  resetForm();
  showCourseModal.value = true;
};

// 编辑课程
const editCourse = (course) => {
  modalTitle.value = '编辑课程';
  isEditing.value = true;
  Object.assign(formCourse, course);
  showCourseModal.value = true;
};

// 删除课程
const deleteCourse = (courseId) => {
  if (confirm('确定要删除该课程吗？')) {
    del(`/api/courses/${courseId}`, null,
      () => {
        fetchCourses();
      }
    );
  }
};

// 重置表单
const resetForm = () => {
  formCourse.id = null;
  formCourse.name = '';
  formCourse.code = '';
  formCourse.subject = '';
  formCourse.description = '';
  formCourse.teacherId = null;
  formCourse.status = 1;
  formCourse.creditHours = 0;
  
  // 清除验证错误
  Object.keys(formErrors).forEach(key => {
    formErrors[key] = '';
  });
};

// 提交表单
const submitForm = () => {
  // 验证表单
  let isValid = true;
  
  if (!formCourse.name) {
    formErrors.name = '请输入课程名称';
    isValid = false;
  }
  
  if (!formCourse.code) {
    formErrors.code = '请输入课程代码';
    isValid = false;
  }
  
  if (!formCourse.subject) {
    formErrors.subject = '请选择学科';
    isValid = false;
  }
  
  if (!formCourse.teacherId) {
    formErrors.teacherId = '请选择任课教师';
    isValid = false;
  }
  
  if (!isValid) {
    return;
  }
  
  if (isEditing.value) {
    // 编辑课程
    put(`/api/courses/${formCourse.id}`, formCourse,
      () => {
        showCourseModal.value = false;
        fetchCourses();
      }
    );
  } else {
    // 添加课程
    post('/api/courses', formCourse,
      () => {
        showCourseModal.value = false;
        fetchCourses();
      }
    );
  }
};

// 分页变化
const handlePageChange = (page) => {
  currentPage.value = page;
  fetchCourses();
};

// 搜索
const handleSearch = () => {
  currentPage.value = 1;
  fetchCourses();
};

// 重置搜索
const resetSearch = () => {
  searchQuery.value = '';
  filterSubject.value = 'all';
  currentPage.value = 1;
  fetchCourses();
};

// 生成随机课程代码
const generateCourseCode = () => {
  const prefix = formCourse.subject ? formCourse.subject.substring(0, 2).toUpperCase() : 'CO';
  const random = Math.floor(Math.random() * 10000).toString().padStart(4, '0');
  formCourse.code = `${prefix}${random}`;
};
</script>

<template>
  <div class="content-section">
    <div class="content-header">
      <h1 class="content-title">课程管理</h1>
      <button @click="addCourse" class="btn btn-primary flex items-center">
        <LucideIcon name="plus" size="16" class="mr-1" />
        添加课程
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
              placeholder="搜索课程名称或代码" 
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
          <label class="form-label">学科筛选</label>
          <select v-model="filterSubject" class="form-select">
            <option value="all">所有学科</option>
            <option v-for="subject in subjects" :key="subject" :value="subject">
              {{ subject }}
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
    
    <!-- 课程列表 -->
    <div class="bg-white rounded-lg shadow-sm overflow-hidden">
      <div class="table-container">
        <table class="table">
          <thead class="table-header">
            <tr>
              <th class="table-header-cell">课程代码</th>
              <th class="table-header-cell">课程名称</th>
              <th class="table-header-cell">学科</th>
              <th class="table-header-cell">任课教师</th>
              <th class="table-header-cell">学分</th>
              <th class="table-header-cell">状态</th>
              <th class="table-header-cell">操作</th>
            </tr>
          </thead>
          <tbody v-if="!loading">
            <tr v-for="course in courses" :key="course.id" class="table-row">
              <td class="table-cell font-mono">{{ course.code }}</td>
              <td class="table-cell font-medium">{{ course.name }}</td>
              <td class="table-cell">
                <span class="px-2 py-1 rounded-full text-xs bg-blue-50 text-blue-700">
                  {{ course.subject }}
                </span>
              </td>
              <td class="table-cell">{{ course.teacherName || '未分配' }}</td>
              <td class="table-cell text-center">{{ course.creditHours }}</td>
              <td class="table-cell">
                <span 
                  class="px-2 py-1 rounded-full text-xs"
                  :class="course.status ? 'bg-green-50 text-green-700' : 'bg-gray-50 text-gray-700'"
                >
                  {{ course.status ? '正常' : '禁用' }}
                </span>
              </td>
              <td class="table-cell">
                <div class="flex space-x-2">
                  <button @click="editCourse(course)" class="btn-icon" title="编辑">
                    <LucideIcon name="edit" size="16" />
                  </button>
                  <button @click="deleteCourse(course.id)" class="btn-icon text-red-500" title="删除">
                    <LucideIcon name="trash-2" size="16" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
          <tbody v-else>
            <tr>
              <td colspan="7" class="py-8 text-center text-gray-500">
                <div class="flex flex-col items-center">
                  <LucideIcon name="loader-2" size="24" class="animate-spin mb-2" />
                  <span>加载中...</span>
                </div>
              </td>
            </tr>
          </tbody>
          <tbody v-if="!loading && courses.length === 0">
            <tr>
              <td colspan="7" class="py-8 text-center text-gray-500">
                <div class="flex flex-col items-center">
                  <LucideIcon name="inbox" size="24" class="mb-2" />
                  <span>暂无数据</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <!-- 分页 -->
      <div class="flex justify-between items-center px-6 py-3 border-t">
        <div class="text-sm text-gray-600">
          共 {{ totalCourses }} 条记录
        </div>
        <div class="flex space-x-1">
          <button 
            v-for="page in Math.ceil(totalCourses / pageSize) || 1" 
            :key="page"
            @click="handlePageChange(page)"
            class="w-8 h-8 flex items-center justify-center rounded"
            :class="currentPage === page ? 'bg-blue-50 text-blue-600 font-medium' : 'hover:bg-gray-50'"
          >
            {{ page }}
          </button>
        </div>
      </div>
    </div>
    
    <!-- 课程表单模态框 -->
    <div v-if="showCourseModal" class="modal-overlay">
      <div class="modal-container">
        <div class="modal-header">
          <h3>{{ modalTitle }}</h3>
          <button @click="showCourseModal = false" class="text-gray-400 hover:text-gray-600">
            <LucideIcon name="x" size="20" />
          </button>
        </div>
        <div class="modal-body">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div class="form-group">
              <label class="form-label required">课程名称</label>
              <input type="text" v-model="formCourse.name" class="form-input" :class="{ 'error': formErrors.name }" />
              <div v-if="formErrors.name" class="form-error">{{ formErrors.name }}</div>
            </div>
            
            <div class="form-group">
              <label class="form-label required">课程代码</label>
              <div class="flex space-x-2">
                <input type="text" v-model="formCourse.code" class="form-input flex-1" :class="{ 'error': formErrors.code }" />
                <button @click="generateCourseCode" class="btn btn-outline flex-shrink-0 whitespace-nowrap" title="生成课程代码">
                  <LucideIcon name="refresh-cw" size="16" class="mr-1" />
                  生成
                </button>
              </div>
              <div v-if="formErrors.code" class="form-error">{{ formErrors.code }}</div>
            </div>
            
            <div class="form-group">
              <label class="form-label required">学科</label>
              <select v-model="formCourse.subject" class="form-select" :class="{ 'error': formErrors.subject }">
                <option value="" disabled selected>请选择学科</option>
                <option v-for="subject in subjects" :key="subject" :value="subject">
                  {{ subject }}
                </option>
              </select>
              <div v-if="formErrors.subject" class="form-error">{{ formErrors.subject }}</div>
            </div>
            
            <div class="form-group">
              <label class="form-label required">任课教师</label>
              <select v-model="formCourse.teacherId" class="form-select" :class="{ 'error': formErrors.teacherId }">
                <option value="" disabled selected>请选择教师</option>
                <option v-for="teacher in teachers" :key="teacher.id" :value="teacher.id">
                  {{ teacher.name }}
                </option>
              </select>
              <div v-if="formErrors.teacherId" class="form-error">{{ formErrors.teacherId }}</div>
            </div>
            
            <div class="form-group">
              <label class="form-label">学分</label>
              <input type="number" v-model="formCourse.creditHours" min="0" class="form-input" />
            </div>
            
            <div class="form-group">
              <label class="form-label">状态</label>
              <select v-model="formCourse.status" class="form-select">
                <option v-for="option in statusOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
            </div>
            
            <div class="form-group md:col-span-2">
              <label class="form-label">课程描述</label>
              <textarea v-model="formCourse.description" rows="4" class="form-textarea"></textarea>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showCourseModal = false" class="btn btn-outline">取消</button>
          <button @click="submitForm" class="btn btn-primary">提交</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.form-label.required::after {
  content: '*';
  margin-left: 4px;
  color: #f43f5e;
}

.table-container {
  overflow-x: auto;
}

.table {
  width: 100%;
  border-collapse: collapse;
}

.table-header {
  background-color: #f8fafc;
}

.table-header-cell {
  padding: 0.75rem 1rem;
  text-align: left;
  font-size: 0.875rem;
  font-weight: 500;
  color: #64748b;
  border-bottom: 1px solid #e2e8f0;
}

.table-row {
  border-bottom: 1px solid #e2e8f0;
}

.table-row:hover {
  background-color: #f8fafc;
}

.table-cell {
  padding: 0.75rem 1rem;
  font-size: 0.875rem;
  color: #334155;
}

.btn-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 0.375rem;
  background-color: transparent;
  transition: background-color 0.2s;
}

.btn-icon:hover {
  background-color: #f1f5f9;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 50;
}

.modal-container {
  background-color: white;
  border-radius: 0.5rem;
  width: 90%;
  max-width: 700px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid #e2e8f0;
}

.modal-header h3 {
  font-size: 1.125rem;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.modal-body {
  padding: 1.5rem;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  padding: 1rem 1.5rem;
  border-top: 1px solid #e2e8f0;
  gap: 0.75rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-label {
  display: block;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
  font-weight: 500;
  color: #475569;
}

.form-input,
.form-select,
.form-textarea {
  width: 100%;
  border: 1px solid #e2e8f0;
  border-radius: 0.375rem;
  padding: 0.5rem 0.75rem;
  font-size: 0.875rem;
  transition: border-color 0.2s;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: #93c5fd;
  box-shadow: 0 0 0 3px rgba(147, 197, 253, 0.25);
}

.form-input.error,
.form-select.error,
.form-textarea.error {
  border-color: #f43f5e;
}

.form-error {
  margin-top: 0.25rem;
  font-size: 0.75rem;
  color: #f43f5e;
}
</style>