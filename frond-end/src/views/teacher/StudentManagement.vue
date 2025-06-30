<script setup>
import {computed, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import BaseCard from '@/components/common/BaseCard.vue';

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
const newStudent = reactive({
  name: '',
  studentId: '',
  gender: '男',
  age: 20,
  phone: '',
  email: '',
  classId: currentClass.value,
  group: '第一组'
});

// 打开新增学生对话框
const openNewStudentDialog = () => {
  Object.assign(newStudent, {
    name: '',
    studentId: '',
    gender: '男',
    age: 20,
    phone: '',
    email: '',
    classId: currentClass.value,
    group: '第一组'
  });
  
  showNewStudentDialog.value = true;
};

// 添加新学生
const addNewStudent = () => {
  if (!newStudent.name || !newStudent.studentId || !newStudent.classId) {
    ElMessage.warning('请填写学生的必要信息');
    return;
  }
  
  // 检查学号是否重复
  const isDuplicate = studentList.value.some(s => s.studentId === newStudent.studentId);
  if (isDuplicate) {
    ElMessage.error('学号已存在，请检查后重试');
    return;
  }
  
  // 创建新学生
  const student = {
    id: Date.now(),
    name: newStudent.name,
    studentId: newStudent.studentId,
    gender: newStudent.gender,
    age: newStudent.age,
    phone: newStudent.phone,
    email: newStudent.email,
    classId: newStudent.classId,
    class: classes.find(c => c.id === newStudent.classId)?.name || '',
    group: newStudent.group,
    attendance: 100,
    score: 0,
    tags: []
  };
  
  studentList.value.push(student);
  showNewStudentDialog.value = false;
  ElMessage.success('学生添加成功');
};

// 编辑学生信息
const updateStudent = () => {
  if (!currentStudent.value) return;
  
  const index = studentList.value.findIndex(s => s.id === currentStudent.value.id);
  if (index !== -1) {
    // 更新班级名称
    currentStudent.value.class = classes.find(c => c.id === currentStudent.value.classId)?.name || '';
    
    studentList.value[index] = { ...currentStudent.value };
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

// 导入学生
const importStudents = () => {
  ElMessage.info('导入功能开发中...');
};

// 导出学生
const exportStudents = () => {
  ElMessage.success('学生数据导出成功');
};

// 分组管理对话框
const showGroupManagementDialog = ref(false);
const groupList = ref(['第一组', '第二组', '第三组', '第四组']);
const newGroupName = ref('');

// 打开分组管理对话框
const openGroupManagementDialog = () => {
  showGroupManagementDialog.value = true;
};

// 添加新分组
const addNewGroup = () => {
  if (!newGroupName.value) {
    ElMessage.warning('请输入分组名称');
    return;
  }
  
  if (groupList.value.includes(newGroupName.value)) {
    ElMessage.warning('分组名称已存在');
    return;
  }
  
  groupList.value.push(newGroupName.value);
  newGroupName.value = '';
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

// 表格多选
const multipleSelection = ref([]);
const handleSelectionChange = (selection) => {
  multipleSelection.value = selection;
};

// 添加标签
const addTag = (student, tag) => {
  if (!student.tags.includes(tag)) {
    student.tags.push(tag);
    ElMessage.success(`已为${student.name}添加"${tag}"标签`);
  }
};

// 移除标签
const removeTag = (student, tag) => {
  student.tags = student.tags.filter(t => t !== tag);
  ElMessage.success(`已移除${student.name}的"${tag}"标签`);
};

// 常用标签
const commonTags = ['优秀学生', '班长', '组长', '需要关注', '进步明显'];

// 切换班级
const handleClassChange = (classId) => {
  currentClass.value = classId;
};
</script>

<template>
  <div class="student-management-container">
    <BaseCard class="mb-6">
      <template #header>
        <div class="flex justify-between items-center">
          <h2 class="text-xl font-bold flex items-center">
            <i class="el-icon-user mr-2 text-blue-500"></i>
            学生管理
          </h2>
          <div class="flex gap-2">
            <el-button type="primary" @click="openNewStudentDialog" icon="el-icon-plus">
              添加学生
            </el-button>
            <el-dropdown trigger="click">
              <el-button type="info" icon="el-icon-more">
                更多操作
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="importStudents">
                    <i class="el-icon-upload2 mr-1"></i> 导入学生
                  </el-dropdown-item>
                  <el-dropdown-item @click="exportStudents">
                    <i class="el-icon-download mr-1"></i> 导出学生
                  </el-dropdown-item>
                  <el-dropdown-item @click="openGroupManagementDialog">
                    <i class="el-icon-s-grid mr-1"></i> 分组管理
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </template>
      
      <!-- 筛选区域 -->
      <div class="filter-section flex flex-wrap gap-4 mb-6 items-center">
        <div class="w-60">
          <el-select
            v-model="currentClass"
            placeholder="选择班级"
            style="width: 100%"
            @change="handleClassChange"
          >
            <el-option
              v-for="item in classes"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </div>
        
        <div class="w-60">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索学生姓名/学号/分组"
            prefix-icon="el-icon-search"
            clearable
          />
        </div>
        
        <div v-if="multipleSelection.length > 0" class="ml-auto flex items-center">
          <span class="text-gray-500 mr-2">已选择 {{ multipleSelection.length }} 名学生</span>
          <el-dropdown trigger="click">
            <el-button type="primary" size="small">
              批量操作 <i class="el-icon-arrow-down"></i>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>
                  <div class="font-bold text-gray-500 px-2 py-1">设置分组</div>
                </el-dropdown-item>
                <el-dropdown-item 
                  v-for="group in groupList" 
                  :key="group"
                  @click="batchSetGroup(group)"
                >
                  {{ group }}
                </el-dropdown-item>
                <el-dropdown-item divided>
                  <div class="font-bold text-gray-500 px-2 py-1">添加标签</div>
                </el-dropdown-item>
                <el-dropdown-item 
                  v-for="tag in commonTags" 
                  :key="tag"
                >
                  {{ tag }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      
      <!-- 学生列表 -->
      <el-table
        :data="filteredStudents"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
        row-class-name="transition-colors duration-200 hover:bg-blue-50"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="name" label="姓名" width="100">
          <template #default="{ row }">
            <div class="flex items-center">
              <el-avatar :size="24" class="mr-2">{{ row.name.charAt(0) }}</el-avatar>
              <span class="cursor-pointer hover:text-blue-500" @click="viewStudentDetail(row)">
                {{ row.name }}
              </span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="studentId" label="学号" width="120" />
        
        <el-table-column prop="gender" label="性别" width="80" />
        
        <el-table-column prop="class" label="班级" width="180" />
        
        <el-table-column prop="group" label="分组" width="100" />
        
        <el-table-column prop="attendance" label="出勤率" width="100">
          <template #default="{ row }">
            {{ row.attendance }}%
          </template>
        </el-table-column>
        
        <el-table-column prop="score" label="平均成绩" width="100" />
        
        <el-table-column label="标签" min-width="180">
          <template #default="{ row }">
            <div class="flex flex-wrap gap-1">
              <el-tag 
                v-for="tag in row.tags" 
                :key="tag" 
                closable
                size="small"
                @close="removeTag(row, tag)"
              >
                {{ tag }}
              </el-tag>
              
              <el-dropdown v-if="row.tags.length < 3" trigger="click" size="small">
                <el-button class="ml-1" type="info" size="small" icon="el-icon-plus" circle plain></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item 
                      v-for="tag in commonTags.filter(t => !row.tags.includes(t))" 
                      :key="tag"
                      @click="addTag(row, tag)"
                    >
                      {{ tag }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              icon="el-icon-edit" 
              @click="viewStudentDetail(row)"
              class="mr-1"
            >
              编辑
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              icon="el-icon-delete" 
              @click="deleteStudent(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </BaseCard>
    
    <!-- 学生详情对话框 -->
    <el-dialog
      v-model="showStudentDetail"
      :title="currentStudent ? `${currentStudent.name}的详细信息` : '学生详情'"
      width="60%"
      :before-close="() => showStudentDetail = false"
    >
      <div v-if="currentStudent" class="student-detail">
        <el-form :model="currentStudent" label-width="100px">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <el-form-item label="姓名">
              <el-input v-model="currentStudent.name" />
            </el-form-item>
            
            <el-form-item label="学号">
              <el-input v-model="currentStudent.studentId" disabled />
            </el-form-item>
            
            <el-form-item label="性别">
              <el-select v-model="currentStudent.gender" style="width: 100%">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="年龄">
              <el-input-number v-model="currentStudent.age" :min="16" :max="30" style="width: 100%" />
            </el-form-item>
            
            <el-form-item label="手机号">
              <el-input v-model="currentStudent.phone" />
            </el-form-item>
            
            <el-form-item label="邮箱">
              <el-input v-model="currentStudent.email" />
            </el-form-item>
            
            <el-form-item label="班级">
              <el-select v-model="currentStudent.classId" style="width: 100%">
                <el-option
                  v-for="item in classes"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="分组">
              <el-select v-model="currentStudent.group" style="width: 100%">
                <el-option
                  v-for="group in groupList"
                  :key="group"
                  :label="group"
                  :value="group"
                />
              </el-select>
            </el-form-item>
          </div>
          
          <el-form-item label="标签">
            <div class="flex flex-wrap gap-2">
              <el-tag 
                v-for="tag in currentStudent.tags" 
                :key="tag" 
                closable
                @close="currentStudent.tags = currentStudent.tags.filter(t => t !== tag)"
              >
                {{ tag }}
              </el-tag>
              
              <el-dropdown trigger="click">
                <el-button type="info" size="small" icon="el-icon-plus" plain>添加标签</el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item 
                      v-for="tag in commonTags.filter(t => !currentStudent.tags.includes(t))" 
                      :key="tag"
                      @click="currentStudent.tags.push(tag)"
                    >
                      {{ tag }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </el-form-item>
          
          <el-divider />
          
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <el-form-item label="出勤率">
              <el-slider v-model="currentStudent.attendance" :step="1" :max="100" show-input />
            </el-form-item>
            
            <el-form-item label="平均成绩">
              <el-slider v-model="currentStudent.score" :step="1" :max="100" show-input />
            </el-form-item>
          </div>
        </el-form>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showStudentDetail = false">取消</el-button>
          <el-button type="primary" @click="updateStudent">保存</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 新增学生对话框 -->
    <el-dialog
      v-model="showNewStudentDialog"
      title="添加新学生"
      width="50%"
      :before-close="() => showNewStudentDialog = false"
    >
      <el-form :model="newStudent" label-width="100px">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <el-form-item label="姓名" required>
            <el-input v-model="newStudent.name" placeholder="请输入学生姓名" />
          </el-form-item>
          
          <el-form-item label="学号" required>
            <el-input v-model="newStudent.studentId" placeholder="请输入学号" />
          </el-form-item>
          
          <el-form-item label="性别">
            <el-select v-model="newStudent.gender" style="width: 100%">
              <el-option label="男" value="男" />
              <el-option label="女" value="女" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="年龄">
            <el-input-number v-model="newStudent.age" :min="16" :max="30" style="width: 100%" />
          </el-form-item>
          
          <el-form-item label="手机号">
            <el-input v-model="newStudent.phone" placeholder="请输入手机号" />
          </el-form-item>
          
          <el-form-item label="邮箱">
            <el-input v-model="newStudent.email" placeholder="请输入邮箱" />
          </el-form-item>
          
          <el-form-item label="班级" required>
            <el-select v-model="newStudent.classId" style="width: 100%">
              <el-option
                v-for="item in classes"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="分组">
            <el-select v-model="newStudent.group" style="width: 100%">
              <el-option
                v-for="group in groupList"
                :key="group"
                :label="group"
                :value="group"
              />
            </el-select>
          </el-form-item>
        </div>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showNewStudentDialog = false">取消</el-button>
          <el-button type="primary" @click="addNewStudent">添加</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 分组管理对话框 -->
    <el-dialog
      v-model="showGroupManagementDialog"
      title="分组管理"
      width="40%"
      :before-close="() => showGroupManagementDialog = false"
    >
      <div class="mb-4 flex">
        <el-input v-model="newGroupName" placeholder="请输入新分组名称" class="mr-2" />
        <el-button type="primary" @click="addNewGroup">添加分组</el-button>
      </div>
      
      <el-table :data="groupList" border style="width: 100%">
        <el-table-column prop="" label="分组名称">
          <template #default="{ row }">
            {{ row }}
          </template>
        </el-table-column>
        
        <el-table-column label="学生数量" width="100" align="center">
          <template #default="{ row }">
            {{ studentList.filter(s => s.group === row).length }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button 
              type="danger" 
              size="small" 
              icon="el-icon-delete" 
              circle
              @click="deleteGroup(row)"
              :disabled="groupList.length <= 1 || row === '第一组'"
            />
          </template>
        </el-table-column>
      </el-table>
      
      <div class="mt-4 text-gray-500 text-sm">
        注意：删除分组后，该分组的学生将被移动到第一组。第一组不可删除。
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.student-management-container {
  @apply p-6;
}

.filter-section {
  @apply mb-6;
}

:deep(.el-table) {
  @apply rounded-lg overflow-hidden shadow-sm;
  --el-table-border-color: theme('colors.gray.200');
}

:deep(.el-table th) {
  @apply bg-gray-100 text-gray-700;
}

:deep(.el-button) {
  @apply transition-all duration-300;
}

:deep(.el-tag) {
  @apply transition-all duration-200;
}

:deep(.el-avatar) {
  @apply bg-blue-500;
}
</style>