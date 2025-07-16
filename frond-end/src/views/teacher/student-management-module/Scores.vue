<script setup>
import {reactive, ref, watch} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseButton from '@/components/common/BaseButton.vue';

// 班级数据
const classes = ref([
  { id: 1, name: '高一(1)班' },
  { id: 2, name: '高一(2)班' },
  { id: 3, name: '高一(3)班' },
  { id: 4, name: '高二(1)班' },
  { id: 5, name: '高二(2)班' },
]);

// 当前选中的班级
const selectedClass = ref(null);

// 科目列表
const subjects = ref([
  { id: 1, name: '语文' },
  { id: 2, name: '数学' },
  { id: 3, name: '英语' },
  { id: 4, name: '物理' },
  { id: 5, name: '化学' },
  { id: 6, name: '生物' },
]);

// 当前选中的科目
const selectedSubject = ref(null);

// 考试类型
const examTypes = ref([
  { id: 1, name: '期中考试' },
  { id: 2, name: '期末考试' },
  { id: 3, name: '单元测试' },
  { id: 4, name: '随堂测验' },
]);

// 当前选中的考试类型
const selectedExamType = ref(null);

// 学生列表
const students = ref([]);
const loading = ref(false);

// 是否显示成绩分析
const showAnalysis = ref(false);

// 成绩分析数据
const analysisData = reactive({
  average: 0,
  highest: 0,
  lowest: 0,
  passRate: 0,
  excellentRate: 0,
  distribution: []
});

// 加载学生列表
const loadStudents = async () => {
  if (!selectedClass.value || !selectedSubject.value || !selectedExamType.value) {
    return;
  }
  
  loading.value = true;
  
  try {
    // 模拟API请求延迟
    await new Promise(resolve => setTimeout(resolve, 800));
    
    // 生成模拟学生数据
    const mockStudents = Array(30).fill().map((_, index) => ({
      id: index + 1,
      studentId: `ST${10000 + index}`,
      name: `学生${index + 1}`,
      score: Math.floor(Math.random() * 40) + 60, // 60-100的随机分数
      status: Math.random() > 0.2 ? '已录入' : '未录入'
    }));
    
    students.value = mockStudents;
    calculateAnalysis();
  } catch (error) {
    ElMessage.error('加载学生列表失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 计算成绩分析数据
const calculateAnalysis = () => {
  if (students.value.length === 0) return;
  
  const scores = students.value.map(s => s.score).filter(s => s !== null && s !== undefined);
  
  if (scores.length === 0) return;
  
  // 计算平均分
  analysisData.average = Math.round(scores.reduce((a, b) => a + b, 0) / scores.length * 10) / 10;
  
  // 最高分和最低分
  analysisData.highest = Math.max(...scores);
  analysisData.lowest = Math.min(...scores);
  
  // 及格率和优秀率
  const passCount = scores.filter(score => score >= 60).length;
  const excellentCount = scores.filter(score => score >= 90).length;
  
  analysisData.passRate = Math.round(passCount / scores.length * 1000) / 10;
  analysisData.excellentRate = Math.round(excellentCount / scores.length * 1000) / 10;
  
  // 分数分布
  analysisData.distribution = [
    { range: '90-100', count: scores.filter(s => s >= 90 && s <= 100).length },
    { range: '80-89', count: scores.filter(s => s >= 80 && s < 90).length },
    { range: '70-79', count: scores.filter(s => s >= 70 && s < 80).length },
    { range: '60-69', count: scores.filter(s => s >= 60 && s < 70).length },
    { range: '0-59', count: scores.filter(s => s < 60).length }
  ];
};

// 更新成绩
const updateScore = (row, newScore) => {
  // 验证分数
  if (newScore < 0 || newScore > 100) {
    ElMessage.warning('分数必须在0-100之间');
    return;
  }
  
  row.score = newScore;
  row.status = '已录入';
  
  // 重新计算分析数据
  calculateAnalysis();
};

// 批量导入成绩
const importScores = () => {
  ElMessageBox.alert('此功能将支持Excel成绩单导入，正在开发中...', '批量导入', {
    confirmButtonText: '确定'
  });
};

// 导出成绩
const exportScores = () => {
  if (students.value.length === 0) {
    ElMessage.warning('暂无数据可导出');
    return;
  }
  
  ElMessage.success('成绩单导出成功');
};

// 保存成绩
const saveScores = () => {
  if (students.value.length === 0) {
    ElMessage.warning('暂无成绩可保存');
    return;
  }
  
  // 模拟API请求
  setTimeout(() => {
    ElMessage.success('成绩保存成功');
  }, 500);
};

// 提交审核
const submitForReview = () => {
  if (students.value.length === 0) {
    ElMessage.warning('暂无成绩可提交');
    return;
  }
  
  const notEnteredCount = students.value.filter(s => s.status === '未录入').length;
  
  if (notEnteredCount > 0) {
    ElMessageBox.confirm(`还有${notEnteredCount}名学生的成绩未录入，确定要提交吗？`, '提示', {
      confirmButtonText: '确定提交',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      // 模拟API请求
      setTimeout(() => {
        ElMessage.success('成绩已提交审核');
      }, 500);
    }).catch(() => {});
  } else {
    // 模拟API请求
    setTimeout(() => {
      ElMessage.success('成绩已提交审核');
    }, 500);
  }
};

// 监听筛选条件变化
const handleFilterChange = () => {
  students.value = [];
  showAnalysis.value = false;
  if (selectedClass.value && selectedSubject.value && selectedExamType.value) {
    loadStudents();
  }
};

// 切换成绩分析显示
const toggleAnalysis = () => {
  if (students.value.length === 0) {
    ElMessage.warning('请先选择班级、科目和考试类型');
    return;
  }
  showAnalysis.value = !showAnalysis.value;
};

watch(selectedClass, handleFilterChange);
watch(selectedSubject, handleFilterChange);
watch(selectedExamType, handleFilterChange);
</script>

<template>
  <div class="scores-page">
    <BaseCard class="mb-6 transition-all duration-300">
      <template #header>
        <div class="flex justify-between items-center">
          <h2 class="text-xl font-bold">成绩录入</h2>
          <div class="flex space-x-2">
            <BaseButton type="primary" @click="saveScores" icon="el-icon-check">保存成绩</BaseButton>
            <BaseButton type="success" @click="submitForReview" icon="el-icon-upload2">提交审核</BaseButton>
          </div>
        </div>
      </template>

      <!-- 筛选栏 -->
      <div class="flex flex-wrap gap-4 mb-6">
        <div class="w-60">
          <el-select
            v-model="selectedClass"
            placeholder="选择班级"
            clearable
            style="width: 100%"
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
          <el-select
            v-model="selectedSubject"
            placeholder="选择科目"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="item in subjects"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </div>
        <div class="w-60">
          <el-select
            v-model="selectedExamType"
            placeholder="选择考试类型"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="item in examTypes"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </div>
        
        <div class="flex-1 flex justify-end gap-2">
          <BaseButton @click="importScores" icon="el-icon-upload">批量导入</BaseButton>
          <BaseButton @click="exportScores" icon="el-icon-download">导出成绩</BaseButton>
          <BaseButton 
            @click="toggleAnalysis" 
            :type="showAnalysis ? 'primary' : 'default'" 
            icon="el-icon-data-analysis"
          >
            {{ showAnalysis ? '隐藏分析' : '成绩分析' }}
          </BaseButton>
        </div>
      </div>

      <!-- 成绩分析 -->
      <div v-if="showAnalysis && students.length > 0" class="mb-6 bg-blue-50 p-4 rounded-xl transition-all duration-300 transform">
        <h3 class="text-lg font-bold mb-4 text-blue-700">成绩分析</h3>
        <div class="grid grid-cols-5 gap-4">
          <div class="bg-white p-3 rounded-lg shadow-sm">
            <div class="text-sm text-gray-500">平均分</div>
            <div class="text-xl font-bold text-blue-600">{{ analysisData.average }}</div>
          </div>
          <div class="bg-white p-3 rounded-lg shadow-sm">
            <div class="text-sm text-gray-500">最高分</div>
            <div class="text-xl font-bold text-green-600">{{ analysisData.highest }}</div>
          </div>
          <div class="bg-white p-3 rounded-lg shadow-sm">
            <div class="text-sm text-gray-500">最低分</div>
            <div class="text-xl font-bold text-red-600">{{ analysisData.lowest }}</div>
          </div>
          <div class="bg-white p-3 rounded-lg shadow-sm">
            <div class="text-sm text-gray-500">及格率</div>
            <div class="text-xl font-bold text-indigo-600">{{ analysisData.passRate }}%</div>
          </div>
          <div class="bg-white p-3 rounded-lg shadow-sm">
            <div class="text-sm text-gray-500">优秀率</div>
            <div class="text-xl font-bold text-purple-600">{{ analysisData.excellentRate }}%</div>
          </div>
        </div>
        
        <div class="mt-4 bg-white p-4 rounded-lg shadow-sm">
          <h4 class="text-md font-bold mb-2 text-gray-700">分数段分布</h4>
          <div class="flex items-end h-24">
            <div 
              v-for="(item, index) in analysisData.distribution" 
              :key="index"
              class="flex-1 mx-1 group relative"
            >
              <div 
                class="w-full bg-blue-500 rounded-t-md transition-all duration-300 hover:bg-blue-600"
                :style="{height: `${(item.count / students.length) * 100}%`}"
              ></div>
              <div class="text-xs text-center mt-1">{{ item.range }}</div>
              <div class="absolute bottom-full mb-1 left-1/2 transform -translate-x-1/2 bg-gray-800 text-white text-xs rounded px-2 py-1 opacity-0 group-hover:opacity-100 transition-opacity duration-200">
                {{ item.count }}人 ({{ Math.round(item.count / students.length * 100) }}%)
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 学生成绩表格 -->
      <el-table
        :data="students"
        v-loading="loading"
        border
        style="width: 100%"
        :empty-text="selectedClass && selectedSubject && selectedExamType ? '暂无学生数据' : '请选择班级、科目和考试类型'"
        row-class-name="transition-colors duration-200 hover:bg-blue-50"
      >
        <el-table-column type="index" width="60" align="center" />
        <el-table-column prop="studentId" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column label="成绩" width="180">
          <template #default="{ row }">
            <el-input-number 
              v-model="row.score" 
              :min="0" 
              :max="100" 
              @change="(val) => updateScore(row, val)"
              size="small"
              class="transition-all duration-200"
            />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag 
              :type="row.status === '已录入' ? 'success' : 'info'"
              class="transition-all duration-300"
            >
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button 
              size="small" 
              type="primary" 
              @click="row.status = '已录入'"
              :disabled="row.status === '已录入'"
              class="transition-all duration-200"
            >
              确认
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="row.score = null; row.status = '未录入'"
              class="transition-all duration-200"
            >
              重置
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </BaseCard>
  </div>
</template>

<style scoped>
.scores-page {
  @apply transition-all duration-300;
}

:deep(.el-input-number .el-input__inner) {
  @apply text-center;
}

:deep(.el-table) {
  @apply rounded-lg overflow-hidden shadow-sm;
  --el-table-border-color: theme('colors.gray.200');
}

:deep(.el-table__header) {
  @apply bg-gray-50;
}

:deep(.el-table th) {
  @apply bg-gray-100 text-gray-700;
}

/* 按钮动画效果 */
:deep(.el-button) {
  @apply transition-all duration-300 transform hover:scale-105;
}
</style> 