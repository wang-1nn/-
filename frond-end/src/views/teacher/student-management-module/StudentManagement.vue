<script setup>
import { computed, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';

// 导入自定义组件
import ClassSelector from './components/ClassSelector.vue';
import StudentTable from './components/StudentTable.vue';
import StudentDetailDialog from './components/StudentDetailDialog.vue';
import NewStudentDialog from './components/NewStudentDialog.vue';
import GroupManagementDialog from './components/GroupManagementDialog.vue';

// 模拟数据
// 班级列表
const classes = [
  { id: 1, name: '计算机科学与技术1班' },
  { id: 2, name: '计算机科学与技术2班' },
  { id: 3, name: '软件工程1班' },
  { id: 4, name: '软件工程2班' }
];

// 当前选中的班级
const currentClass = ref(1);

// 学生列表
const studentList = ref([
  {
    id: 1,
    name: '张三',
    studentId: '20230101',
    gender: '男',
    age: 20,
    phone: '13800138001',
    email: 'zhangsan@example.com',
    class: '计算机科学与技术1班',
    classId: 1,
    group: '第一组',
    attendance: 98,
    score: 92,
    tags: ['班长', '优秀学生']
  },
  {
    id: 2,
    name: '李四',
    studentId: '20230102',
    gender: '男',
    age: 21,
    phone: '13800138002',
    email: 'lisi@example.com',
    class: '计算机科学与技术1班',
    classId: 1,
    group: '第二组',
    attendance: 95,
    score: 85,
    tags: ['组长']
  },
  {
    id: 3,
    name: '王五',
    studentId: '20230103',
    gender: '男',
    age: 20,
    phone: '13800138003',
    email: 'wangwu@example.com',
    class: '计算机科学与技术1班',
    classId: 1,
    group: '第一组',
    attendance: 92,
    score: 78,
    tags: []
  },
  {
    id: 4,
    name: '赵六',
    studentId: '20230104',
    gender: '女',
    age: 19,
    phone: '13800138004',
    email: 'zhaoliu@example.com',
    class: '计算机科学与技术1班',
    classId: 1,
    group: '第三组',
    attendance: 100,
    score: 95,
    tags: ['优秀学生']
  },
  {
    id: 5,
    name: '钱七',
    studentId: '20230105',
    gender: '女',
    age: 20,
    phone: '13800138005',
    email: 'qianqi@example.com',
    class: '计算机科学与技术1班',
    classId: 1,
    group: '第二组',
    attendance: 96,
    score: 88,
    tags: []
  },
  {
    id: 6,
    name: '孙八',
    studentId: '20230201',
    gender: '男',
    age: 21,
    phone: '13800138006',
    email: 'sunba@example.com',
    class: '计算机科学与技术2班',
    classId: 2,
    group: '第一组',
    attendance: 94,
    score: 82,
    tags: ['组长']
  },
  {
    id: 7,
    name: '周九',
    studentId: '20230202',
    gender: '男',
    age: 20,
    phone: '13800138007',
    email: 'zhoujiu@example.com',
    class: '计算机科学与技术2班',
    classId: 2,
    group: '第一组',
    attendance: 90,
    score: 75,
    tags: []
  },
  {
    id: 8,
    name: '吴十',
    studentId: '20230301',
    gender: '女',
    age: 19,
    phone: '13800138008',
    email: 'wushi@example.com',
    class: '软件工程1班',
    classId: 3,
    group: '第一组',
    attendance: 98,
    score: 90,
    tags: ['班长', '优秀学生']
  }
]);

// 搜索关键词
const searchKeyword = ref('');

// 过滤后的学生列表
const filteredStudents = computed(() => {
  let result = [...studentList.value];
  
  // 按班级过滤
  if (currentClass.value) {
    result = result.filter(student => student.classId === currentClass.value);
  }
  
  // 按关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    result = result.filter(student => 
      student.name.toLowerCase().includes(keyword) || 
      student.studentId.toLowerCase().includes(keyword) ||
      student.group.toLowerCase().includes(keyword)
    );
  }
  
  return result;
});

// 学生详情对话框
const showStudentDetail = ref(false);
const currentStudent = ref(null);

// 查看学生详情
const viewStudentDetail = (student) => {
  currentStudent.value = { ...student };
  showStudentDetail.value = true;
};

// 新增学生对话框
const showNewStudentDialog = ref(false);

// 分组管理
const groupList = ref(['第一组', '第二组', '第三组', '第四组']);
const showGroupManagementDialog = ref(false);

// 获取分组的学生数量
const getStudentCountInGroup = (group) => {
  return studentList.value.filter(s => s.group === group).length;
};

// 常用标签
const commonTags = ['优秀学生', '班长', '组长', '需要关注', '进步明显'];

// 表格多选
const multipleSelection = ref([]);
const handleSelectionChange = (selection) => {
  multipleSelection.value = selection;
};

// 编辑学生信息
const updateStudent = (updatedStudent) => {
  const index = studentList.value.findIndex(s => s.id === updatedStudent.id);
  if (index !== -1) {
    // 更新班级名称
    updatedStudent.class = classes.find(c => c.id === updatedStudent.classId)?.name || '';
    
    studentList.value[index] = { ...updatedStudent };
    showStudentDetail.value = false;
    ElMessage.success('学生信息更新成功');
  }
};

// 删除学生
const deleteStudent = (student) => {
  ElMessageBox.confirm(`确定要删除学生"${student.name}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    studentList.value = studentList.value.filter(s => s.id !== student.id);
    ElMessage.success('删除成功');
  }).catch(() => {});
};

// 添加新学生
const addNewStudent = (student) => {
  // 检查学号是否重复
  const isDuplicate = studentList.value.some(s => s.studentId === student.studentId);
  if (isDuplicate) {
    ElMessage.error('学号已存在，请检查后重试');
    return;
  }
  
  // 创建新学生
  const newStudent = {
    id: Date.now(),
    name: student.name,
    studentId: student.studentId,
    gender: student.gender,
    age: student.age,
    phone: student.phone,
    email: student.email,
    classId: student.classId,
    class: classes.find(c => c.id === student.classId)?.name || '',
    group: student.group,
    attendance: 100,
    score: 0,
    tags: []
  };
  
  studentList.value.push(newStudent);
  showNewStudentDialog.value = false;
  ElMessage.success('学生添加成功');
};

// 批量设置分组
const batchSetGroup = (group) => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请先选择学生');
    return;
  }
  
  multipleSelection.value.forEach(student => {
    const index = studentList.value.findIndex(s => s.id === student.id);
    if (index !== -1) {
      studentList.value[index].group = group;
    }
  });
  
  ElMessage.success(`已将${multipleSelection.value.length}名学生分配到"${group}"`);
  multipleSelection.value = [];
};

// 添加标签
const addTagToStudent = (student, tag = null) => {
  if (!tag) {
    // 显示添加标签对话框
    return;
  }
  
  if (!student.tags.includes(tag)) {
    const index = studentList.value.findIndex(s => s.id === student.id);
    if (index !== -1) {
      studentList.value[index].tags.push(tag);
      ElMessage.success(`已为${student.name}添加"${tag}"标签`);
    }
  }
};

// 移除标签
const removeTagFromStudent = (student, tag) => {
  const index = studentList.value.findIndex(s => s.id === student.id);
  if (index !== -1) {
    studentList.value[index].tags = studentList.value[index].tags.filter(t => t !== tag);
    ElMessage.success(`已移除${student.name}的"${tag}"标签`);
  }
};

// 添加新分组
const addNewGroup = (groupName) => {
  if (groupList.value.includes(groupName)) {
    ElMessage.warning('分组名称已存在');
    return;
  }
  
  groupList.value.push(groupName);
  ElMessage.success('分组添加成功');
};

// 删除分组
const deleteGroup = (group) => {
  ElMessageBox.confirm(`确定要删除"${group}"分组吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 将该分组的学生移动到第一组
    studentList.value.forEach(student => {
      if (student.group === group) {
        student.group = groupList.value[0] || '未分组';
      }
    });
    
    // 删除分组
    groupList.value = groupList.value.filter(g => g !== group);
    ElMessage.success('删除成功');
  }).catch(() => {});
};

// 导入学生
const importStudents = () => {
  ElMessage.info('导入功能开发中...');
};

// 导出学生
const exportStudents = () => {
  ElMessage.success('学生数据导出成功');
};

// 选择班级
const handleClassChange = (classId) => {
  currentClass.value = classId;
  // 清空当前的选择
  multipleSelection.value = [];
};
</script>

<template>
  <div class="student-management">
    <div class="page-header">
      <h1>学生管理</h1>
      <div class="header-actions">
        <button class="add-btn" @click="showNewStudentDialog = true">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
          </svg>
          添加学生
        </button>
        
        <div class="actions-dropdown">
          <button class="more-btn">
            更多操作
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
            </svg>
          </button>
          <div class="dropdown-menu">
            <button class="dropdown-item" @click="importStudents">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" />
              </svg>
              导入学生
            </button>
            <button class="dropdown-item" @click="exportStudents">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
              </svg>
              导出学生
            </button>
            <button class="dropdown-item" @click="showGroupManagementDialog = true">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
              分组管理
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <div class="filter-section">
      <div class="flex-1">
        <ClassSelector 
          :classes="classes" 
          v-model="currentClass"
        />
      </div>
      
      <div class="search-box">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 search-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
        </svg>
        <input 
          type="text" 
          v-model="searchKeyword" 
          placeholder="搜索学生姓名/学号/分组"
          class="search-input"
        />
        <button 
          v-if="searchKeyword" 
          class="clear-search-btn"
          @click="searchKeyword = ''"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </div>
    
    <div class="student-action-bar" v-if="multipleSelection.length > 0">
      <div class="flex items-center">
        <span class="selection-count">已选择 {{ multipleSelection.length }} 名学生</span>
        <div class="actions-dropdown">
          <button class="batch-action-btn">
            批量操作
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
            </svg>
          </button>
          <div class="dropdown-menu">
            <div class="dropdown-section">
              <div class="dropdown-section-title">设置分组</div>
              <button 
                v-for="group in groupList" 
                :key="`group-${group}`"
                class="dropdown-item"
                @click="batchSetGroup(group)"
              >
                {{ group }}
              </button>
            </div>
            
            <div class="dropdown-section">
              <div class="dropdown-section-title">添加标签</div>
              <button 
                v-for="tag in commonTags" 
                :key="`tag-${tag}`"
                class="dropdown-item"
              >
                {{ tag }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 学生表格 -->
    <StudentTable 
      :students="filteredStudents"
      @view-student="viewStudentDetail"
      @delete-student="deleteStudent"
      @selection-change="handleSelectionChange"
      @add-tag="addTagToStudent"
      @remove-tag="removeTagFromStudent"
    />
    
    <!-- 学生详情对话框 -->
    <StudentDetailDialog
      :visible="showStudentDetail"
      :student="currentStudent"
      :classes="classes"
      :groups="groupList"
      :common-tags="commonTags"
      @close="showStudentDetail = false"
      @update="updateStudent"
    />
    
    <!-- 新增学生对话框 -->
    <NewStudentDialog
      :visible="showNewStudentDialog"
      :classes="classes"
      :groups="groupList"
      :current-class-id="currentClass"
      @close="showNewStudentDialog = false"
      @add-student="addNewStudent"
    />
    
    <!-- 分组管理对话框 -->
    <GroupManagementDialog
      :visible="showGroupManagementDialog"
      :groups="groupList"
      :student-count="getStudentCountInGroup"
      @close="showGroupManagementDialog = false"
      @add-group="addNewGroup"
      @delete-group="deleteGroup"
    />
  </div>
</template>

<style scoped>
.student-management {
  @apply p-6;
}

.page-header {
  @apply flex justify-between items-center mb-6;
}

.page-header h1 {
  @apply text-2xl font-bold text-gray-800;
}

.header-actions {
  @apply flex gap-2;
}

.add-btn {
  @apply flex items-center px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors duration-200 font-medium;
}

.add-btn svg {
  @apply mr-1;
}

.more-btn {
  @apply flex items-center px-4 py-2 bg-white border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors duration-200 font-medium;
}

.more-btn svg {
  @apply ml-1;
}

.actions-dropdown {
  @apply relative;
}

.dropdown-menu {
  @apply absolute right-0 mt-1 w-48 bg-white border border-gray-200 rounded-lg shadow-lg overflow-hidden z-10 hidden;
}

.actions-dropdown:hover .dropdown-menu,
.actions-dropdown:focus-within .dropdown-menu {
  @apply block;
}

.dropdown-section {
  @apply py-1;
}

.dropdown-section:not(:last-child) {
  @apply border-b border-gray-200;
}

.dropdown-section-title {
  @apply px-4 py-1 text-xs font-medium text-gray-500;
}

.dropdown-item {
  @apply flex items-center w-full px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 transition-colors duration-150 text-left;
}

.dropdown-item svg {
  @apply mr-2;
}

.filter-section {
  @apply flex flex-wrap items-center gap-4 mb-6;
}

.search-box {
  @apply relative w-60;
}

.search-icon {
  @apply absolute top-1/2 left-3 transform -translate-y-1/2 text-gray-400;
}

.search-input {
  @apply w-full pl-10 pr-10 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all duration-200;
}

.clear-search-btn {
  @apply absolute top-1/2 right-2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors duration-150;
}

.student-action-bar {
  @apply bg-blue-50 border border-blue-100 rounded-lg px-4 py-2 mb-4 flex justify-between items-center;
}

.selection-count {
  @apply text-sm text-blue-700 font-medium mr-4;
}

.batch-action-btn {
  @apply flex items-center px-3 py-1 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700 transition-colors duration-200 font-medium;
}
</style>