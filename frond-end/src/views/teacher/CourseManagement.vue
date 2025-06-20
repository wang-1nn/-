<script setup>
import { ref, onMounted, computed } from 'vue';
import { get, post, put } from '@/net';
import { useAuthStore } from '@/stores/counter.js';
import LucideIcon from '@/components/icons/LucideIcon.vue';

// 认证信息
const auth = useAuthStore();
const userId = computed(() => auth.user?.id);

// 课程列表数据
const courses = ref([]);
const loading = ref(true);
const totalCourses = ref(0);
const currentPage = ref(1);
const pageSize = ref(6);

// 搜索条件
const searchQuery = ref('');
const filterStatus = ref('all');

// 标签数据
const selectedCourse = ref(null);
const showAddStudentModal = ref(false);
const showCourseDetailModal = ref(false);
const studentsNotInCourse = ref([]);
const selectedStudents = ref([]);

// 获取教师课程
const fetchCourses = async () => {
  loading.value = true;
  get(`/api/teachers/${userId.value}/courses`, {
    page: currentPage.value,
    size: pageSize.value,
    query: searchQuery.value,
    status: filterStatus.value === 'all' ? '' : filterStatus.value
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

// 获取未选课的学生
const fetchStudentsNotInCourse = (courseId) => {
  get(`/api/courses/${courseId}/students/available`, null, 
    (message, data) => {
      studentsNotInCourse.value = data;
    }
  );
};

// 添加学生到课程
const addStudentsToCourse = () => {
  if (selectedCourse.value && selectedStudents.value.length > 0) {
    post(`/api/courses/${selectedCourse.value.id}/students`, {
      studentIds: selectedStudents.value
    }, 
    () => {
      showAddStudentModal.value = false;
      selectedStudents.value = [];
      // 刷新课程信息
      fetchCourseDetail(selectedCourse.value.id);
    });
  }
};

// 获取课程详情
const fetchCourseDetail = (courseId) => {
  get(`/api/courses/${courseId}`, null, 
    (message, data) => {
      selectedCourse.value = data;
    }
  );
};

// 查看课程详情
const viewCourseDetail = (course) => {
  fetchCourseDetail(course.id);
  showCourseDetailModal.value = true;
};

// 移除学生
const removeStudent = (studentId) => {
  if (confirm('确定要将该学生从课程中移除吗？')) {
    post(`/api/courses/${selectedCourse.value.id}/students/remove`, {
      studentId
    }, 
    () => {
      // 刷新课程信息
      fetchCourseDetail(selectedCourse.value.id);
    });
  }
};

// 添加学生
const showAddStudent = (course) => {
  selectedCourse.value = course;
  fetchCourseDetail(course.id);
  fetchStudentsNotInCourse(course.id);
  showAddStudentModal.value = true;
};

// 搜索
const handleSearch = () => {
  currentPage.value = 1;
  fetchCourses();
};

// 重置搜索
const resetSearch = () => {
  searchQuery.value = '';
  filterStatus.value = 'all';
  currentPage.value = 1;
  fetchCourses();
};

// 页面加载时获取数据
onMounted(() => {
  fetchCourses();
});

// 分页变化
const handlePageChange = (page) => {
  currentPage.value = page;
  fetchCourses();
};
</script>

<template>
  <div class="content-section">
    <div class="content-header mb-6">
      <h1 class="content-title">我的课程</h1>
    </div>
    
    <!-- 搜索栏 -->
    <div class="bg-white p-4 rounded-lg shadow-sm mb-6">
      <div class="flex flex-wrap gap-4">
        <div class="flex-1 min-w-[200px]">
          <label class="form-label">课程搜索</label>
          <div class="relative">
            <input 
              type="text" 
              v-model="searchQuery" 
              placeholder="搜索课程名称" 
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
          <label class="form-label">课程状态</label>
          <select v-model="filterStatus" class="form-select">
            <option value="all">所有状态</option>
            <option value="1">正常</option>
            <option value="0">停课</option>
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
    
    <!-- 课程卡片列表 -->
    <div v-if="loading" class="flex justify-center py-10">
      <div class="flex flex-col items-center">
        <LucideIcon name="loader-2" size="32" class="animate-spin mb-2 text-blue-600" />
        <span class="text-gray-500">正在加载课程...</span>
      </div>
    </div>
    
    <div v-else-if="courses.length === 0" class="bg-white rounded-lg shadow-sm p-10 text-center">
      <div class="flex flex-col items-center">
        <LucideIcon name="book-x" size="48" class="text-gray-400 mb-2" />
        <h3 class="text-lg font-medium text-gray-700 mb-2">暂无课程数据</h3>
        <p class="text-gray-500 max-w-md mx-auto">您目前没有任何课程。请联系管理员分配课程。</p>
      </div>
    </div>
    
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div v-for="course in courses" :key="course.id" class="bg-white rounded-lg shadow-sm overflow-hidden flex flex-col">
        <div class="bg-blue-50 p-4">
          <div class="flex justify-between items-start">
            <h3 class="text-lg font-semibold text-gray-800 truncate">{{ course.name }}</h3>
            <span 
              class="px-2 py-1 rounded-full text-xs"
              :class="course.status ? 'bg-green-50 text-green-700' : 'bg-gray-50 text-gray-700'"
            >
              {{ course.status ? '正常' : '停课' }}
            </span>
          </div>
          <div class="mt-1 text-sm text-gray-600">{{ course.code }}</div>
        </div>
        
        <div class="p-4 flex-grow">
          <div class="mb-4">
            <div class="flex items-center text-sm text-gray-500 mb-2">
              <LucideIcon name="book-open" size="16" class="mr-2" />
              <span>{{ course.subject }}</span>
            </div>
            <div class="flex items-center text-sm text-gray-500 mb-2">
              <LucideIcon name="users" size="16" class="mr-2" />
              <span>{{ course.studentCount || 0 }} 名学生</span>
            </div>
            <div class="flex items-center text-sm text-gray-500">
              <LucideIcon name="clock" size="16" class="mr-2" />
              <span>{{ course.creditHours || 0 }} 学分</span>
            </div>
          </div>
          
          <p class="text-sm text-gray-600 line-clamp-2">
            {{ course.description || '暂无课程描述' }}
          </p>
        </div>
        
        <div class="border-t px-4 py-3 bg-gray-50 flex justify-between">
          <button @click="viewCourseDetail(course)" class="btn btn-outline btn-sm">
            <LucideIcon name="eye" size="14" class="mr-1" />
            查看详情
          </button>
          <button @click="showAddStudent(course)" class="btn btn-primary btn-sm">
            <LucideIcon name="user-plus" size="14" class="mr-1" />
            添加学生
          </button>
        </div>
      </div>
    </div>
    
    <!-- 分页 -->
    <div v-if="totalCourses > 0" class="flex justify-center mt-6">
      <div class="flex space-x-1">
        <button 
          v-for="page in Math.ceil(totalCourses / pageSize) || 1" 
          :key="page"
          @click="handlePageChange(page)"
          class="w-8 h-8 flex items-center justify-center rounded"
          :class="currentPage === page ? 'bg-blue-100 text-blue-600 font-medium' : 'bg-white text-gray-600 hover:bg-gray-50'"
        >
          {{ page }}
        </button>
      </div>
    </div>
    
    <!-- 添加学生模态框 -->
    <div v-if="showAddStudentModal" class="modal-overlay">
      <div class="modal-container max-w-xl">
        <div class="modal-header">
          <h3>添加学生到课程</h3>
          <button @click="showAddStudentModal = false" class="text-gray-400 hover:text-gray-600">
            <LucideIcon name="x" size="20" />
          </button>
        </div>
        <div class="modal-body">
          <div class="mb-4">
            <div class="text-sm font-medium mb-1">课程:</div>
            <div class="text-gray-800 text-lg">{{ selectedCourse?.name }}</div>
          </div>
          
          <div v-if="studentsNotInCourse.length === 0" class="text-center py-6">
            <LucideIcon name="users-x" size="32" class="mx-auto mb-2 text-gray-400" />
            <p class="text-gray-500">没有可添加的学生</p>
          </div>
          
          <div v-else>
            <div class="mb-4">
              <div class="flex items-center justify-between mb-2">
                <label class="form-label mb-0">可选学生列表</label>
                <div class="text-sm text-gray-500">
                  已选择 {{ selectedStudents.length }} 名学生
                </div>
              </div>
              
              <div class="border rounded-md p-4 max-h-64 overflow-y-auto">
                <div v-for="student in studentsNotInCourse" :key="student.id" class="flex items-center py-2 border-b last:border-0">
                  <input 
                    type="checkbox" 
                    :id="`student_${student.id}`" 
                    :value="student.id" 
                    v-model="selectedStudents"
                    class="form-checkbox mr-2"
                  />
                  <label :for="`student_${student.id}`" class="flex-1 cursor-pointer">
                    <div class="font-medium">{{ student.name }}</div>
                    <div class="text-sm text-gray-500">{{ student.username }}</div>
                  </label>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showAddStudentModal = false" class="btn btn-outline">取消</button>
          <button 
            @click="addStudentsToCourse" 
            class="btn btn-primary"
            :disabled="selectedStudents.length === 0"
          >
            添加所选学生
          </button>
        </div>
      </div>
    </div>
    
    <!-- 课程详情模态框 -->
    <div v-if="showCourseDetailModal" class="modal-overlay">
      <div class="modal-container max-w-4xl">
        <div class="modal-header">
          <h3>课程详情</h3>
          <button @click="showCourseDetailModal = false" class="text-gray-400 hover:text-gray-600">
            <LucideIcon name="x" size="20" />
          </button>
        </div>
        <div class="modal-body">
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
            <!-- 左侧课程信息 -->
            <div class="md:col-span-1">
              <div class="bg-blue-50 p-4 rounded-lg mb-4">
                <h2 class="font-semibold text-xl text-gray-800">{{ selectedCourse?.name }}</h2>
                <div class="text-sm text-gray-600 mt-1">{{ selectedCourse?.code }}</div>
              </div>
              
              <div class="bg-white p-4 rounded-lg border">
                <div class="mb-3 pb-3 border-b">
                  <div class="text-xs text-gray-500 mb-1">学科</div>
                  <div class="font-medium">{{ selectedCourse?.subject }}</div>
                </div>
                <div class="mb-3 pb-3 border-b">
                  <div class="text-xs text-gray-500 mb-1">学分</div>
                  <div class="font-medium">{{ selectedCourse?.creditHours || 0 }}</div>
                </div>
                <div class="mb-3 pb-3 border-b">
                  <div class="text-xs text-gray-500 mb-1">状态</div>
                  <div>
                    <span 
                      class="px-2 py-1 rounded-full text-xs"
                      :class="selectedCourse?.status ? 'bg-green-50 text-green-700' : 'bg-gray-50 text-gray-700'"
                    >
                      {{ selectedCourse?.status ? '正常' : '停课' }}
                    </span>
                  </div>
                </div>
                <div>
                  <div class="text-xs text-gray-500 mb-1">课程描述</div>
                  <div class="text-sm text-gray-700">{{ selectedCourse?.description || '暂无描述' }}</div>
                </div>
              </div>
            </div>
            
            <!-- 右侧学生列表 -->
            <div class="md:col-span-2">
              <div class="flex items-center justify-between mb-4">
                <h3 class="text-lg font-medium text-gray-800">选课学生 ({{ selectedCourse?.students?.length || 0 }})</h3>
                <button @click="showAddStudent(selectedCourse)" class="btn btn-primary btn-sm">
                  <LucideIcon name="user-plus" size="14" class="mr-1" />
                  添加学生
                </button>
              </div>
              
              <div v-if="!selectedCourse?.students || selectedCourse.students.length === 0" class="text-center py-10 bg-white rounded-lg border">
                <LucideIcon name="users-x" size="32" class="mx-auto mb-2 text-gray-400" />
                <p class="text-gray-500">该课程暂无学生</p>
              </div>
              
              <div v-else class="bg-white rounded-lg border overflow-hidden">
                <table class="w-full">
                  <thead class="bg-gray-50">
                    <tr>
                      <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">学生</th>
                      <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">用户名</th>
                      <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-gray-200">
                    <tr v-for="student in selectedCourse.students" :key="student.id" class="hover:bg-gray-50">
                      <td class="px-4 py-3">
                        <div class="font-medium text-gray-800">{{ student.name }}</div>
                      </td>
                      <td class="px-4 py-3 text-gray-600">{{ student.username }}</td>
                      <td class="px-4 py-3">
                        <button @click="removeStudent(student.id)" class="text-red-500 hover:text-red-700">
                          <LucideIcon name="user-minus" size="16" />
                        </button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showCourseDetailModal = false" class="btn btn-outline">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.content-section {
  padding: 1rem;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.content-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1e293b;
}

.form-label {
  display: block;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
  font-weight: 500;
  color: #475569;
}

.form-input,
.form-select {
  width: 100%;
  border: 1px solid #e2e8f0;
  border-radius: 0.375rem;
  padding: 0.5rem 0.75rem;
  font-size: 0.875rem;
  transition: border-color 0.2s;
}

.form-checkbox {
  width: 1rem;
  height: 1rem;
  border-radius: 0.25rem;
  border: 1px solid #cbd5e1;
  accent-color: #3b82f6;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 0.375rem;
  font-weight: 500;
  padding: 0.5rem 1rem;
  transition: all 0.2s;
}

.btn-sm {
  padding: 0.25rem 0.5rem;
  font-size: 0.875rem;
}

.btn-primary {
  background-color: #3b82f6;
  color: white;
}

.btn-primary:hover {
  background-color: #2563eb;
}

.btn-primary:disabled {
  background-color: #93c5fd;
  cursor: not-allowed;
}

.btn-outline {
  background-color: white;
  border: 1px solid #e2e8f0;
  color: #475569;
}

.btn-outline:hover {
  background-color: #f8fafc;
  border-color: #cbd5e1;
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

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>