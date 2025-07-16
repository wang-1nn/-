<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';

const route = useRoute();
const router = useRouter();
const examId = computed(() => route.params.examId);
const isEdit = computed(() => !!examId.value);

// 表单数据
const formData = reactive({
  title: '',
  courseId: '',
  description: '',
  startTime: '',
  endTime: '',
  duration: 90,
  totalScore: 100,
  passingScore: 60,
  showScore: true,
  shuffleQuestions: false,
  allowReview: true,
  status: 'draft'
});

// 课程列表
const courseOptions = ref([
  { label: 'Java程序设计', value: '1' },
  { label: '计算机网络', value: '2' },
  { label: '数据结构', value: '3' },
  { label: '操作系统', value: '4' },
]);

// 题目列表
const questionList = ref([]);

// 加载中状态
const loading = ref(false);
const saving = ref(false);

// 添加题目抽屉可见状态
const addQuestionDrawerVisible = ref(false);

// 获取考试详情
const getExamDetail = async () => {
  if (!isEdit.value) return;

  loading.value = true;
  try {
    // 这里应该是实际的API请求
    // 模拟API请求延迟
    await new Promise(resolve => setTimeout(resolve, 800));

    // 模拟数据
    Object.assign(formData, {
      title: `${isEdit.value ? '编辑' : '新建'}考试 - ${examId.value || ''}`,
      courseId: '1',
      description: '这是一个示例考试描述，描述考试目的、范围和注意事项。',
      startTime: new Date(Date.now() + 24 * 60 * 60 * 1000),
      endTime: new Date(Date.now() + 26 * 60 * 60 * 1000),
      duration: 120,
      totalScore: 100,
      passingScore: 60,
      showScore: true,
      shuffleQuestions: true,
      allowReview: true,
      status: 'draft'
    });

    // 模拟题目数据
    questionList.value = Array.from({ length: 5 }, (_, i) => ({
      id: i + 1,
      type: ['single_choice', 'multiple_choice', 'true_false', 'fill_blank', 'short_answer'][i % 5],
      content: `这是第 ${i + 1} 题的题目内容。`,
      score: 10,
      difficulty: ['easy', 'medium', 'hard'][i % 3],
      options: i < 3 ? [
        { label: '选项A', value: 'A' },
        { label: '选项B', value: 'B' },
        { label: '选项C', value: 'C' },
        { label: '选项D', value: 'D' }
      ] : [],
      answer: i === 0 ? 'A' : i === 1 ? ['A', 'C'] : i === 2 ? 'true' : '答案内容'
    }));

  } catch (error) {
    console.error('获取考试详情失败:', error);
    ElMessage.error('获取考试详情失败');
  } finally {
    loading.value = false;
  }
};

// 保存考试
const saveExam = async () => {
  // 表单验证
  if (!formData.title) {
    ElMessage.warning('请输入考试标题');
    return;
  }

  if (!formData.courseId) {
    ElMessage.warning('请选择所属课程');
    return;
  }

  if (!formData.startTime || !formData.endTime) {
    ElMessage.warning('请设置开始和结束时间');
    return;
  }

  if (questionList.value.length === 0) {
    ElMessage.warning('请至少添加一道题目');
    return;
  }

  saving.value = true;
  try {
    // 模拟API请求
    await new Promise(resolve => setTimeout(resolve, 1000));

    ElMessage.success(`${isEdit.value ? '更新' : '创建'}考试成功`);
    router.push('/teacher/exams');
  } catch (error) {
    console.error('保存考试失败:', error);
    ElMessage.error('保存考试失败');
  } finally {
    saving.value = false;
  }
};

// 预览考试
const previewExam = () => {
  ElMessage.info('预览功能开发中');
};

// 发布考试
const publishExam = () => {
  ElMessageBox.confirm('发布后学生将能够看到此考试，确认发布吗？', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 模拟API请求
    setTimeout(() => {
      ElMessage.success('发布成功');
      router.push('/teacher/exams');
    }, 500);
  }).catch(() => {});
};

// 添加题目
const addQuestion = () => {
  addQuestionDrawerVisible.value = true;
};

// 删除题目
const removeQuestion = (index) => {
  ElMessageBox.confirm('确定要删除此题目吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    questionList.value.splice(index, 1);
    ElMessage.success('删除成功');
  }).catch(() => {});
};

// 修改题目顺序
const moveQuestion = (index, direction) => {
  if (direction === 'up' && index > 0) {
    const temp = questionList.value[index];
    questionList.value.splice(index, 1);
    questionList.value.splice(index - 1, 0, temp);
  } else if (direction === 'down' && index < questionList.value.length - 1) {
    const temp = questionList.value[index];
    questionList.value.splice(index, 1);
    questionList.value.splice(index + 1, 0, temp);
  }
};

// 取消编辑
const cancelEdit = () => {
  ElMessageBox.confirm('确定要取消编辑吗？未保存的内容将丢失。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    router.push('/teacher/exams');
  }).catch(() => {});
};

// 获取题型显示文本
const getQuestionTypeText = (type) => {
  switch (type) {
    case 'single_choice': return '单选题';
    case 'multiple_choice': return '多选题';
    case 'true_false': return '判断题';
    case 'fill_blank': return '填空题';
    case 'short_answer': return '简答题';
    default: return '未知题型';
  }
};

// 页面加载获取数据
onMounted(() => {
  getExamDetail();
});
</script>

<template>
  <div class="exam-editor-container" v-loading="loading">
    <div class="page-header">
      <div class="page-title">
        <h1 class="text-2xl font-bold">{{ isEdit ? '编辑考试' : '创建新考试' }}</h1>
        <p class="text-sm text-gray-500 mt-1">
          {{ isEdit ? '修改考试内容与设置' : '创建新的考试并添加题目' }}
        </p>
      </div>

      <div class="header-actions">
        <el-button @click="cancelEdit">取消</el-button>
        <el-button type="primary" @click="previewExam">预览</el-button>
        <el-button
            type="success"
            @click="publishExam"
        >发布</el-button>
        <el-button
            type="primary"
            @click="saveExam"
            :loading="saving"
        >保存</el-button>
      </div>
    </div>

    <div class="editor-content">
      <el-row :gutter="20">
        <el-col :span="16">
          <!-- 考试表单 -->
          <div class="panel basic-info-panel">
            <div class="panel-header">
              <h2 class="panel-title">基本信息</h2>
            </div>

            <div class="panel-body">
              <el-form
                  :model="formData"
                  label-width="100px"
                  label-position="top"
              >
                <el-row :gutter="20">
                  <el-col :span="24">
                    <el-form-item label="考试标题" required>
                      <el-input v-model="formData.title" placeholder="请输入考试标题" />
                    </el-form-item>
                  </el-col>

                  <el-col :span="12">
                    <el-form-item label="所属课程" required>
                      <el-select v-model="formData.courseId" placeholder="请选择课程" style="width: 100%">
                        <el-option
                            v-for="item in courseOptions"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value"
                        />
                      </el-select>
                    </el-form-item>
                  </el-col>

                  <el-col :span="12">
                    <el-form-item label="考试时长(分钟)" required>
                      <el-input-number
                          v-model="formData.duration"
                          :min="10"
                          :max="240"
                          :step="5"
                          style="width: 100%"
                      />
                    </el-form-item>
                  </el-col>

                  <el-col :span="24">
                    <el-form-item label="考试说明">
                      <el-input
                          v-model="formData.description"
                          type="textarea"
                          :rows="3"
                          placeholder="请输入考试说明，如考试范围、注意事项等"
                      />
                    </el-form-item>
                  </el-col>

                  <el-col :span="12">
                    <el-form-item label="开始时间" required>
                      <el-date-picker
                          v-model="formData.startTime"
                          type="datetime"
                          placeholder="选择开始时间"
                          style="width: 100%"
                      />
                    </el-form-item>
                  </el-col>

                  <el-col :span="12">
                    <el-form-item label="结束时间" required>
                      <el-date-picker
                          v-model="formData.endTime"
                          type="datetime"
                          placeholder="选择结束时间"
                          style="width: 100%"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>
            </div>
          </div>

          <!-- 题目列表 -->
          <div class="panel question-panel mt-6">
            <div class="panel-header">
              <h2 class="panel-title">试题管理</h2>
              <div class="panel-actions">
                <el-button type="primary" size="small" @click="addQuestion">
                  <i class="el-icon-plus mr-1"></i> 添加题目
                </el-button>
              </div>
            </div>

            <div class="panel-body">
              <div v-if="questionList.length === 0" class="empty-questions">
                <el-empty description="暂无试题，请添加题目">
                  <el-button type="primary" @click="addQuestion">添加题目</el-button>
                </el-empty>
              </div>

              <div v-else class="question-list">
                <div
                    v-for="(question, index) in questionList"
                    :key="index"
                    class="question-item"
                >
                  <div class="question-header">
                    <div class="question-info">
                      <div class="question-type">
                        <el-tag size="small">{{ getQuestionTypeText(question.type) }}</el-tag>
                      </div>
                      <div class="question-score">
                        <span>{{ question.score }} 分</span>
                      </div>
                    </div>

                    <div class="question-actions">
                      <el-button-group>
                        <el-button type="text" size="small" @click="moveQuestion(index, 'up')" :disabled="index === 0">
                          <i class="el-icon-arrow-up"></i>
                        </el-button>
                        <el-button type="text" size="small" @click="moveQuestion(index, 'down')" :disabled="index === questionList.length - 1">
                          <i class="el-icon-arrow-down"></i>
                        </el-button>
                      </el-button-group>

                      <el-button type="text" size="small">编辑</el-button>
                      <el-button type="text" size="small" @click="removeQuestion(index)">删除</el-button>
                    </div>
                  </div>

                  <div class="question-content">
                    <div class="question-title">
                      {{ index + 1 }}. {{ question.content }}
                    </div>

                    <div v-if="['single_choice', 'multiple_choice'].includes(question.type)" class="question-options">
                      <div v-for="(option, optIndex) in question.options" :key="optIndex" class="option-item">
                        <span class="option-label">{{ option.value }}.</span>
                        <span class="option-content">{{ option.label }}</span>
                      </div>
                    </div>
                  </div>

                  <div class="question-answer">
                    <span class="answer-label">参考答案：</span>
                    <span class="answer-content">{{
                        Array.isArray(question.answer)
                            ? question.answer.join('、')
                            : question.answer
                      }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-col>

        <el-col :span="8">
          <!-- 考试设置 -->
          <div class="panel settings-panel">
            <div class="panel-header">
              <h2 class="panel-title">考试设置</h2>
            </div>

            <div class="panel-body">
              <el-form :model="formData" label-width="120px">
                <el-form-item label="总分值">
                  <el-input-number v-model="formData.totalScore" :min="1" :step="5" />
                </el-form-item>

                <el-form-item label="及格分数线">
                  <el-input-number
                      v-model="formData.passingScore"
                      :min="1"
                      :max="formData.totalScore"
                      :step="5"
                  />
                </el-form-item>

                <el-form-item label="显示分数">
                  <el-switch v-model="formData.showScore" />
                </el-form-item>

                <el-form-item label="随机题目顺序">
                  <el-switch v-model="formData.shuffleQuestions" />
                </el-form-item>

                <el-form-item label="允许查看答案">
                  <el-switch v-model="formData.allowReview" />
                </el-form-item>
              </el-form>
            </div>
          </div>

          <!-- 分数统计 -->
          <div class="panel score-panel mt-6">
            <div class="panel-header">
              <h2 class="panel-title">分数统计</h2>
            </div>

            <div class="panel-body">
              <div class="score-statistics">
                <div class="statistic-item">
                  <div class="statistic-label">题目总数</div>
                  <div class="statistic-value">{{ questionList.length }}</div>
                </div>

                <div class="statistic-item">
                  <div class="statistic-label">当前总分</div>
                  <div class="statistic-value">{{ questionList.reduce((sum, q) => sum + q.score, 0) }}</div>
                </div>

                <div class="statistic-item">
                  <div class="statistic-label">目标总分</div>
                  <div class="statistic-value">{{ formData.totalScore }}</div>
                </div>
              </div>

              <div class="score-progress mt-4">
                <div class="progress-label">完成度</div>
                <el-progress
                    :percentage="Math.min(100, questionList.reduce((sum, q) => sum + q.score, 0) / formData.totalScore * 100)"
                    :format="() => `${questionList.reduce((sum, q) => sum + q.score, 0)}/${formData.totalScore}`"
                />
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<style scoped>
.exam-editor-container {
  @apply p-6;
}

.page-header {
  @apply flex justify-between items-center mb-6;
}

/* 面板通用样式 */
.panel {
  @apply bg-white rounded-lg shadow-sm;
}

.panel-header {
  @apply p-4 border-b border-gray-200 flex items-center justify-between;
}

.panel-title {
  @apply text-lg font-medium;
}

.panel-body {
  @apply p-4;
}

/* 题目样式 */
.question-list {
  @apply space-y-6;
}

.question-item {
  @apply border border-gray-200 rounded-md p-4;
}

.question-header {
  @apply flex justify-between items-center mb-2 pb-2 border-b border-gray-100;
}

.question-info {
  @apply flex items-center gap-3;
}

.question-content {
  @apply mb-4;
}

.question-title {
  @apply text-base mb-2;
}

.question-options {
  @apply ml-6 mt-2 space-y-2;
}

.option-item {
  @apply flex;
}

.option-label {
  @apply mr-2 font-medium;
}

.question-answer {
  @apply text-sm bg-gray-50 p-2 rounded;
}

.answer-label {
  @apply font-medium;
}

/* 统计样式 */
.score-statistics {
  @apply grid grid-cols-3 gap-4;
}

.statistic-item {
  @apply text-center p-3 bg-gray-50 rounded-md;
}

.statistic-label {
  @apply text-sm text-gray-500;
}

.statistic-value {
  @apply text-lg font-bold mt-1;
}

/* 空状态 */
.empty-questions {
  @apply py-8;
}
</style>