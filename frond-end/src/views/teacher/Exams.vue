<script setup>
import {computed, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import BaseCard from '@/components/common/BaseCard.vue';
import {useRouter} from 'vue-router';

const router = useRouter();

// 考试类型
const examTypes = [
  { value: 'midterm', label: '期中考试' },
  { value: 'final', label: '期末考试' },
  { value: 'quiz', label: '单元测验' },
  { value: 'practice', label: '课后练习' }
];

// 学期选项
const semesters = [
  { value: '2023-2024-1', label: '2023-2024学年第一学期' },
  { value: '2023-2024-2', label: '2023-2024学年第二学期' }
];

// 科目列表
const subjects = [
  { id: 1, name: '高等数学' },
  { id: 2, name: '线性代数' },
  { id: 3, name: '概率论与数理统计' },
  { id: 4, name: '数据结构' },
  { id: 5, name: '计算机网络' }
];

// 班级列表
const classes = [
  { id: 1, name: '计算机科学与技术1班' },
  { id: 2, name: '计算机科学与技术2班' },
  { id: 3, name: '软件工程1班' },
  { id: 4, name: '软件工程2班' },
  { id: 5, name: '人工智能1班' }
];

// 筛选条件
const filters = reactive({
  examType: '',
  semester: '2023-2024-1',
  subject: '',
  status: ''
});

// 考试状态选项
const statusOptions = [
  { value: 'draft', label: '草稿' },
  { value: 'published', label: '已发布' },
  { value: 'in_progress', label: '进行中' },
  { value: 'finished', label: '已结束' },
  { value: 'graded', label: '已评分' }
];

// 考试列表
const examList = ref([
  {
    id: 1,
    title: '高等数学期中考试',
    subject: '高等数学',
    examType: 'midterm',
    semester: '2023-2024-1',
    status: 'published',
    duration: 120,
    totalScore: 100,
    questionCount: 15,
    examDate: '2023-10-25',
    classes: ['计算机科学与技术1班', '计算机科学与技术2班'],
    createTime: '2023-09-15'
  },
  {
    id: 2,
    title: '线性代数期末考试',
    subject: '线性代数',
    examType: 'final',
    semester: '2023-2024-1',
    status: 'draft',
    duration: 120,
    totalScore: 100,
    questionCount: 12,
    examDate: '2024-01-10',
    classes: ['计算机科学与技术1班', '软件工程1班'],
    createTime: '2023-09-20'
  },
  {
    id: 3,
    title: '数据结构单元测验',
    subject: '数据结构',
    examType: 'quiz',
    semester: '2023-2024-1',
    status: 'finished',
    duration: 60,
    totalScore: 50,
    questionCount: 10,
    examDate: '2023-10-15',
    classes: ['计算机科学与技术2班', '软件工程2班'],
    createTime: '2023-09-25'
  },
  {
    id: 4,
    title: '计算机网络课后练习',
    subject: '计算机网络',
    examType: 'practice',
    semester: '2023-2024-1',
    status: 'in_progress',
    duration: 45,
    totalScore: 30,
    questionCount: 8,
    examDate: '2023-11-05',
    classes: ['软件工程1班', '软件工程2班'],
    createTime: '2023-10-01'
  }
]);

// 过滤后的考试列表
const filteredExams = computed(() => {
  let result = [...examList.value];
  
  if (filters.examType) {
    result = result.filter(exam => exam.examType === filters.examType);
  }
  
  if (filters.semester) {
    result = result.filter(exam => exam.semester === filters.semester);
  }
  
  if (filters.subject) {
    result = result.filter(exam => exam.subject === filters.subject);
  }
  
  if (filters.status) {
    result = result.filter(exam => exam.status === filters.status);
  }
  
  return result;
});

// 新建考试对话框
const showNewExamDialog = ref(false);
const newExam = reactive({
  title: '',
  subject: '',
  examType: 'midterm',
  semester: '2023-2024-1',
  duration: 120,
  totalScore: 100,
  examDate: '',
  selectedClasses: []
});

// 打开新建考试对话框
const openNewExamDialog = () => {
  Object.assign(newExam, {
    title: '',
    subject: '',
    examType: 'midterm',
    semester: '2023-2024-1',
    duration: 120,
    totalScore: 100,
    examDate: '',
    selectedClasses: []
  });
  
  showNewExamDialog.value = true;
};

// 创建新考试
const createNewExam = () => {
  if (!newExam.title || !newExam.subject || !newExam.examDate || newExam.selectedClasses.length === 0) {
    ElMessage.warning('请填写完整的考试信息');
    return;
  }
  
  // 创建新考试
  const exam = {
    id: Date.now(),
    title: newExam.title,
    subject: newExam.subject,
    examType: newExam.examType,
    semester: newExam.semester,
    status: 'draft',
    duration: newExam.duration,
    totalScore: newExam.totalScore,
    questionCount: 0,
    examDate: newExam.examDate,
    classes: newExam.selectedClasses.map(id => classes.find(c => c.id === id)?.name || ''),
    createTime: new Date().toISOString().split('T')[0]
  };
  
  examList.value.unshift(exam);
  showNewExamDialog.value = false;
  ElMessage.success('考试创建成功');
  
  // 跳转到考试编辑页面
  router.push(`/teacher/exam-editor?id=${exam.id}`);
};

// 删除考试
const deleteExam = (exam) => {
  ElMessageBox.confirm(`确定要删除"${exam.title}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    examList.value = examList.value.filter(e => e.id !== exam.id);
    ElMessage.success('删除成功');
  }).catch(() => {});
};

// 复制考试
const duplicateExam = (exam) => {
  const newExam = {
    ...exam,
    id: Date.now(),
    title: `${exam.title} (副本)`,
    status: 'draft',
    createTime: new Date().toISOString().split('T')[0]
  };
  
  examList.value.unshift(newExam);
  ElMessage.success('复制成功');
};

// 编辑考试
const editExam = (exam) => {
  router.push(`/teacher/exam-editor?id=${exam.id}`);
};

// 预览考试
const previewExam = (exam) => {
  ElMessage.info('预览功能开发中...');
};

// 发布考试
const publishExam = (exam) => {
  if (exam.questionCount === 0) {
    ElMessage.warning('考试题目为空，无法发布');
    return;
  }
  
  ElMessageBox.confirm(`确定要发布"${exam.title}"吗？发布后学生将可以看到此考试。`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const index = examList.value.findIndex(e => e.id === exam.id);
    if (index !== -1) {
      examList.value[index].status = 'published';
      ElMessage.success('发布成功');
    }
  }).catch(() => {});
};

// 根据状态获取标签类型
const getStatusType = (status) => {
  switch (status) {
    case 'draft': return 'info';
    case 'published': return 'primary';
    case 'in_progress': return 'warning';
    case 'finished': return 'success';
    case 'graded': return 'success';
    default: return 'info';
  }
};

// 获取状态显示文本
const getStatusText = (status) => {
  const option = statusOptions.find(opt => opt.value === status);
  return option ? option.label : status;
};

// 获取考试类型显示文本
const getExamTypeText = (type) => {
  const option = examTypes.find(opt => opt.value === type);
  return option ? option.label : type;
};

// 获取学期显示文本
const getSemesterText = (semester) => {
  const option = semesters.find(opt => opt.value === semester);
  return option ? option.label : semester;
};
</script>

<template>
  <div class="exams-container">
    <BaseCard class="mb-6">
      <template #header>
        <div class="flex justify-between items-center">
          <h2 class="text-xl font-bold flex items-center">
            <i class="el-icon-edit mr-2 text-blue-500"></i>
            考核设计
          </h2>
          <el-button type="primary" @click="openNewExamDialog" icon="el-icon-plus">
            新建考试
          </el-button>
        </div>
      </template>
      
      <!-- 筛选区域 -->
      <div class="filter-section flex flex-wrap gap-4 mb-6">
        <div class="w-48">
          <el-select
            v-model="filters.examType"
            placeholder="考试类型"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="item in examTypes"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </div>
        
        <div class="w-60">
          <el-select
            v-model="filters.semester"
            placeholder="学期"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="item in semesters"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </div>
        
        <div class="w-48">
          <el-select
            v-model="filters.subject"
            placeholder="科目"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="item in subjects"
              :key="item.id"
              :label="item.name"
              :value="item.name"
            />
          </el-select>
    </div>

        <div class="w-48">
          <el-select
            v-model="filters.status"
            placeholder="状态"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </div>
      </div>

      <!-- 考试列表 -->
      <div class="exams-list">
        <div v-if="filteredExams.length === 0" class="empty-state">
          <i class="el-icon-document text-5xl text-gray-300"></i>
          <p class="text-gray-500 mt-4">暂无考试</p>
          <el-button type="primary" size="small" @click="openNewExamDialog" class="mt-2">
            创建考试
          </el-button>
        </div>
        
        <el-table
          v-else
          :data="filteredExams"
          border
          style="width: 100%"
          row-class-name="transition-colors duration-200 hover:bg-blue-50"
        >
          <el-table-column prop="title" label="考试名称" min-width="180">
            <template #default="{ row }">
              <div class="flex items-center">
                <i class="el-icon-document-checked text-blue-500 mr-2"></i>
                <span class="cursor-pointer hover:text-blue-500" @click="editExam(row)">
                  {{ row.title }}
                </span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="subject" label="科目" width="120" />
          
          <el-table-column prop="examType" label="类型" width="120">
            <template #default="{ row }">
              {{ getExamTypeText(row.examType) }}
            </template>
          </el-table-column>
          
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="examDate" label="考试日期" width="120" />
          
          <el-table-column prop="duration" label="时长" width="80">
            <template #default="{ row }">
              {{ row.duration }} 分钟
            </template>
          </el-table-column>
          
          <el-table-column prop="totalScore" label="总分" width="80" />
          
          <el-table-column prop="questionCount" label="题量" width="80" />
          
          <el-table-column label="操作" width="240">
            <template #default="{ row }">
              <el-button 
                type="primary" 
                size="small" 
                icon="el-icon-edit" 
                @click="editExam(row)"
                class="mr-1"
              >
                编辑
              </el-button>
              <el-button 
                type="success" 
                size="small" 
                icon="el-icon-view" 
                @click="previewExam(row)"
                class="mr-1"
              >
                预览
              </el-button>
              <el-dropdown size="small" trigger="click">
                <el-button type="info" size="small" icon="el-icon-more">
                  更多
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-if="row.status === 'draft'" @click="publishExam(row)">
                      <i class="el-icon-upload2 mr-1"></i> 发布
                    </el-dropdown-item>
                    <el-dropdown-item @click="duplicateExam(row)">
                      <i class="el-icon-copy-document mr-1"></i> 复制
                    </el-dropdown-item>
                    <el-dropdown-item @click="deleteExam(row)">
                      <i class="el-icon-delete mr-1"></i> 删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </BaseCard>
    
    <!-- 新建考试对话框 -->
    <el-dialog
      v-model="showNewExamDialog"
      title="新建考试"
      width="50%"
      :before-close="() => showNewExamDialog = false"
    >
      <el-form :model="newExam" label-width="100px">
        <el-form-item label="考试名称" required>
          <el-input v-model="newExam.title" placeholder="请输入考试名称" />
        </el-form-item>
        <el-form-item label="科目" required>
          <el-select v-model="newExam.subject" placeholder="请选择科目" style="width: 100%">
            <el-option
              v-for="item in subjects"
              :key="item.id"
              :label="item.name"
              :value="item.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="考试类型">
          <el-select v-model="newExam.examType" placeholder="请选择考试类型" style="width: 100%">
            <el-option
              v-for="item in examTypes"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学期">
          <el-select v-model="newExam.semester" placeholder="请选择学期" style="width: 100%">
            <el-option
              v-for="item in semesters"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="考试日期" required>
          <el-date-picker
            v-model="newExam.examDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="考试时长">
          <el-input-number v-model="newExam.duration" :min="10" :step="5" style="width: 100%" />
          <div class="text-xs text-gray-500 mt-1">单位：分钟</div>
        </el-form-item>
        <el-form-item label="总分">
          <el-input-number v-model="newExam.totalScore" :min="10" :step="5" style="width: 100%" />
        </el-form-item>
        <el-form-item label="参与班级" required>
          <el-checkbox-group v-model="newExam.selectedClasses">
            <el-checkbox 
              v-for="cls in classes" 
              :key="cls.id" 
              :label="cls.id"
              border
              class="mb-2 mr-2"
            >
              {{ cls.name }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showNewExamDialog = false">取消</el-button>
          <el-button type="primary" @click="createNewExam">创建并进入编辑</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.exams-container {
  @apply p-6;
}

.filter-section {
  @apply mb-6;
}

.empty-state {
  @apply flex flex-col items-center justify-center py-16;
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

:deep(.el-checkbox.is-bordered) {
  @apply transition-all duration-200;
}

:deep(.el-checkbox.is-bordered:hover) {
  @apply border-blue-400;
}

:deep(.el-checkbox.is-bordered.is-checked) {
  @apply border-blue-500 bg-blue-50;
}
</style> 