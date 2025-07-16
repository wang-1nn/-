<script setup>
import {computed, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import BaseCard from '@/components/common/BaseCard.vue';

// 学期选项
const semesters = [
  { value: '2023-2024-1', label: '2023-2024学年第一学期' },
  { value: '2023-2024-2', label: '2023-2024学年第二学期' },
  { value: '2024-2025-1', label: '2024-2025学年第一学期' }
];

// 当前选中的学期
const currentSemester = ref('2023-2024-1');

// 科目列表
const subjects = ref([
  { id: 1, name: '高等数学', grade: '大一', hours: 64 },
  { id: 2, name: '线性代数', grade: '大一', hours: 48 },
  { id: 3, name: '概率论与数理统计', grade: '大二', hours: 56 },
  { id: 4, name: '数据结构', grade: '大二', hours: 64 },
  { id: 5, name: '计算机网络', grade: '大三', hours: 48 }
]);

// 当前选中的科目
const currentSubject = ref(null);

// 教学计划列表
const teachingPlans = ref([
  {
    id: 1,
    title: '高等数学教学计划',
    subject: '高等数学',
    semester: '2023-2024-1',
    status: '进行中',
    progress: 65,
    createTime: '2023-08-15',
    updateTime: '2023-09-05'
  },
  {
    id: 2,
    title: '线性代数教学计划',
    subject: '线性代数',
    semester: '2023-2024-1',
    status: '进行中',
    progress: 40,
    createTime: '2023-08-18',
    updateTime: '2023-09-02'
  },
  {
    id: 3,
    title: '概率论与数理统计教学计划',
    subject: '概率论与数理统计',
    semester: '2023-2024-2',
    status: '未开始',
    progress: 0,
    createTime: '2023-08-20',
    updateTime: '2023-08-20'
  },
  {
    id: 4,
    title: '数据结构教学计划',
    subject: '数据结构',
    semester: '2023-2024-1',
    status: '已完成',
    progress: 100,
    createTime: '2023-07-15',
    updateTime: '2023-08-30'
  }
]);

// 过滤后的教学计划
const filteredPlans = computed(() => {
  let result = [...teachingPlans.value];
  
  // 按学期过滤
  if (currentSemester.value) {
    result = result.filter(plan => plan.semester === currentSemester.value);
  }
  
  // 按科目过滤
  if (currentSubject.value) {
    result = result.filter(plan => plan.subject === currentSubject.value);
  }
  
  return result;
});

// 计划详情对话框
const showPlanDetail = ref(false);
const currentPlan = ref(null);

// 查看计划详情
const viewPlanDetail = (plan) => {
  currentPlan.value = { ...plan };
  showPlanDetail.value = true;
};

// 新建计划对话框
const showNewPlanDialog = ref(false);
const newPlan = reactive({
  title: '',
  subject: '',
  semester: currentSemester.value,
  content: ''
});

// 打开新建计划对话框
const openNewPlanDialog = () => {
  newPlan.title = '';
  newPlan.subject = '';
  newPlan.semester = currentSemester.value;
  newPlan.content = '';
  showNewPlanDialog.value = true;
};

// 创建新计划
const createNewPlan = () => {
  if (!newPlan.title || !newPlan.subject || !newPlan.semester) {
    ElMessage.warning('请填写完整的计划信息');
    return;
  }
  
  // 创建新计划
  const plan = {
    id: Date.now(),
    title: newPlan.title,
    subject: newPlan.subject,
    semester: newPlan.semester,
    status: '未开始',
    progress: 0,
    createTime: new Date().toISOString().split('T')[0],
    updateTime: new Date().toISOString().split('T')[0]
  };
  
  teachingPlans.value.unshift(plan);
  showNewPlanDialog.value = false;
  ElMessage.success('教学计划创建成功');
};

// 删除计划
const deletePlan = (plan) => {
  ElMessageBox.confirm(`确定要删除"${plan.title}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    teachingPlans.value = teachingPlans.value.filter(p => p.id !== plan.id);
    ElMessage.success('删除成功');
  }).catch(() => {});
};

// 编辑计划
const editPlan = (plan) => {
  currentPlan.value = { ...plan };
  showPlanDetail.value = true;
};

// 更新计划
const updatePlan = () => {
  if (!currentPlan.value) return;
  
  const index = teachingPlans.value.findIndex(p => p.id === currentPlan.value.id);
  if (index !== -1) {
    teachingPlans.value[index] = {
      ...currentPlan.value,
      updateTime: new Date().toISOString().split('T')[0]
    };
    
    showPlanDetail.value = false;
    ElMessage.success('计划更新成功');
  }
};

// 复制计划
const duplicatePlan = (plan) => {
  const newPlan = {
    ...plan,
    id: Date.now(),
    title: `${plan.title} (副本)`,
    status: '未开始',
    progress: 0,
    createTime: new Date().toISOString().split('T')[0],
    updateTime: new Date().toISOString().split('T')[0]
  };
  
  teachingPlans.value.unshift(newPlan);
  ElMessage.success('复制成功');
};

// 根据状态获取标签类型
const getStatusType = (status) => {
  switch (status) {
    case '进行中': return 'primary';
    case '已完成': return 'success';
    case '未开始': return 'info';
    default: return 'info';
  }
};

// 示例周计划数据
const weekPlans = [
  { week: 1, content: '第一章：函数与极限', hours: 6, status: '已完成' },
  { week: 2, content: '第二章：导数与微分', hours: 8, status: '已完成' },
  { week: 3, content: '第三章：微分中值定理与导数应用', hours: 6, status: '进行中' },
  { week: 4, content: '第四章：不定积分', hours: 6, status: '未开始' },
  { week: 5, content: '第五章：定积分', hours: 8, status: '未开始' },
  { week: 6, content: '第六章：定积分的应用', hours: 6, status: '未开始' }
];

// 示例教学目标
const teachingGoals = [
  '理解函数极限的概念和性质',
  '掌握函数连续性的判断方法',
  '熟练运用导数计算公式',
  '理解微分中值定理及其应用',
  '掌握定积分和不定积分的计算方法',
  '能够应用积分解决实际问题'
];

// 示例教学重点
const teachingFocus = [
  '函数极限的ε-δ定义',
  '导数的几何意义及应用',
  '微分中值定理',
  '不定积分的计算方法',
  '定积分的计算及应用'
];

// 示例教学难点
const teachingDifficulties = [
  '函数极限的严格证明',
  '复合函数的求导',
  '微分中值定理的应用',
  '分部积分法和换元积分法',
  '定积分在几何和物理中的应用'
];

// 示例教学资源
const teachingResources = [
  { name: '高等数学教材', type: '教材', author: '同济大学数学系' },
  { name: '高等数学习题集', type: '习题', author: '同济大学数学系' },
  { name: '高等数学公式手册', type: '参考资料', author: '数学教研室' },
  { name: '高等数学课件', type: '课件', author: '张教授' }
];
</script>

<template>
  <div class="teaching-plans-container">
    <BaseCard class="mb-6">
      <template #header>
        <div class="flex justify-between items-center">
          <h2 class="text-xl font-bold flex items-center">
            <i class="el-icon-notebook-1 mr-2 text-blue-500"></i>
            教学计划管理
          </h2>
          <el-button type="primary" @click="openNewPlanDialog" icon="el-icon-plus">
            新建教学计划
          </el-button>
        </div>
      </template>
      
      <!-- 筛选区域 -->
      <div class="filter-section flex flex-wrap gap-4 mb-6">
        <div class="w-60">
          <el-select
            v-model="currentSemester"
            placeholder="选择学期"
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
        
        <div class="w-60">
          <el-select
            v-model="currentSubject"
            placeholder="选择科目"
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
      </div>
      
      <!-- 计划列表 -->
      <div class="plans-list">
        <div v-if="filteredPlans.length === 0" class="empty-state">
          <i class="el-icon-document text-5xl text-gray-300"></i>
          <p class="text-gray-500 mt-4">暂无教学计划</p>
          <el-button type="primary" size="small" @click="openNewPlanDialog" class="mt-2">
            创建计划
          </el-button>
        </div>
        
        <el-table
          v-else
          :data="filteredPlans"
          border
          style="width: 100%"
          row-class-name="transition-colors duration-200 hover:bg-blue-50"
        >
          <el-table-column prop="title" label="计划名称" min-width="180">
            <template #default="{ row }">
              <div class="flex items-center">
                <i class="el-icon-document-checked text-blue-500 mr-2"></i>
                <span class="cursor-pointer hover:text-blue-500" @click="viewPlanDetail(row)">
                  {{ row.title }}
                </span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="subject" label="科目" width="150" />
          
          <el-table-column prop="semester" label="学期" width="180">
            <template #default="{ row }">
              {{ semesters.find(s => s.value === row.semester)?.label || row.semester }}
            </template>
          </el-table-column>
          
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="progress" label="进度" width="180">
            <template #default="{ row }">
              <el-progress 
                :percentage="row.progress" 
                :status="row.progress === 100 ? 'success' : ''"
              />
            </template>
          </el-table-column>
          
          <el-table-column prop="updateTime" label="更新时间" width="120" />
          
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <el-button 
                type="primary" 
                size="small" 
                icon="el-icon-edit" 
                @click="editPlan(row)"
                class="mr-1"
              >
                编辑
              </el-button>
              <el-dropdown size="small" trigger="click">
                <el-button type="info" size="small" icon="el-icon-more">
                  更多
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="duplicatePlan(row)">
                      <i class="el-icon-copy-document mr-1"></i> 复制
                    </el-dropdown-item>
                    <el-dropdown-item @click="deletePlan(row)">
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
    
    <!-- 计划详情对话框 -->
    <el-dialog
      v-model="showPlanDetail"
      :title="currentPlan?.title || '计划详情'"
      width="70%"
      :before-close="() => showPlanDetail = false"
    >
      <div v-if="currentPlan" class="plan-detail">
        <!-- 基本信息 -->
        <div class="mb-6">
          <h3 class="text-lg font-bold mb-4">基本信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="计划名称">
              <el-input v-model="currentPlan.title" size="small" />
            </el-descriptions-item>
            <el-descriptions-item label="科目">{{ currentPlan.subject }}</el-descriptions-item>
            <el-descriptions-item label="学期">
              {{ semesters.find(s => s.value === currentPlan.semester)?.label || currentPlan.semester }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-select v-model="currentPlan.status" size="small">
                <el-option label="未开始" value="未开始" />
                <el-option label="进行中" value="进行中" />
                <el-option label="已完成" value="已完成" />
              </el-select>
            </el-descriptions-item>
            <el-descriptions-item label="进度">
              <el-slider v-model="currentPlan.progress" :step="5" show-input />
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ currentPlan.createTime }}</el-descriptions-item>
          </el-descriptions>
        </div>
        
        <!-- 教学目标 -->
        <div class="mb-6">
          <h3 class="text-lg font-bold mb-4">教学目标</h3>
          <div class="bg-gray-50 p-4 rounded-lg">
            <el-tag
              v-for="(goal, index) in teachingGoals"
              :key="index"
              class="mr-2 mb-2"
              effect="plain"
            >
              {{ goal }}
            </el-tag>
          </div>
        </div>
        
        <!-- 教学重点与难点 -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
          <div>
            <h3 class="text-lg font-bold mb-4">教学重点</h3>
            <ul class="list-disc pl-6">
              <li v-for="(item, index) in teachingFocus" :key="index" class="mb-2">
                {{ item }}
              </li>
            </ul>
          </div>
          <div>
            <h3 class="text-lg font-bold mb-4">教学难点</h3>
            <ul class="list-disc pl-6">
              <li v-for="(item, index) in teachingDifficulties" :key="index" class="mb-2">
                {{ item }}
              </li>
            </ul>
          </div>
        </div>
        
        <!-- 周计划 -->
        <div class="mb-6">
          <h3 class="text-lg font-bold mb-4">周计划安排</h3>
          <el-table :data="weekPlans" border style="width: 100%">
            <el-table-column prop="week" label="周次" width="80" align="center" />
            <el-table-column prop="content" label="教学内容" />
            <el-table-column prop="hours" label="课时" width="80" align="center" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <!-- 教学资源 -->
        <div class="mb-6">
          <h3 class="text-lg font-bold mb-4">教学资源</h3>
          <el-table :data="teachingResources" border style="width: 100%">
            <el-table-column prop="name" label="资源名称" />
            <el-table-column prop="type" label="类型" width="120" />
            <el-table-column prop="author" label="作者/来源" width="150" />
            <el-table-column label="操作" width="100">
              <template #default>
                <el-button type="primary" size="small" icon="el-icon-download">
                  下载
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showPlanDetail = false">取消</el-button>
          <el-button type="primary" @click="updatePlan">保存</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 新建计划对话框 -->
    <el-dialog
      v-model="showNewPlanDialog"
      title="新建教学计划"
      width="50%"
      :before-close="() => showNewPlanDialog = false"
    >
      <el-form :model="newPlan" label-width="80px">
        <el-form-item label="计划名称">
          <el-input v-model="newPlan.title" placeholder="请输入计划名称" />
        </el-form-item>
        <el-form-item label="科目">
          <el-select v-model="newPlan.subject" placeholder="请选择科目" style="width: 100%">
            <el-option
              v-for="item in subjects"
              :key="item.id"
              :label="item.name"
              :value="item.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学期">
          <el-select v-model="newPlan.semester" placeholder="请选择学期" style="width: 100%">
            <el-option
              v-for="item in semesters"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="计划内容">
          <el-input
            v-model="newPlan.content"
            type="textarea"
            :rows="5"
            placeholder="请输入计划内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showNewPlanDialog = false">取消</el-button>
          <el-button type="primary" @click="createNewPlan">创建</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.teaching-plans-container {
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

:deep(.el-descriptions__label) {
  @apply bg-gray-50;
}

:deep(.el-tag) {
  @apply transition-all duration-200;
}

:deep(.el-button) {
  @apply transition-all duration-300;
}
</style> 