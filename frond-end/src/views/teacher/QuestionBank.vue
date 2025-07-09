<script setup>
import {onMounted, reactive, ref, computed, nextTick} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {get, post} from '@/net';

// 批次选中
const selectedBatchId = ref(null);

// 题库列表
const questionList = ref([]); // 全量题目
const displayList = ref([]);  // 当前显示列表（全部或某批次）

const filteredQuestions = computed(() => displayList.value);

// 直接由后端批次接口返回列表

// 批次题目缓存，避免重复请求
const batchCache = reactive(new Map());

const loadingBatch = ref(false);

const selectBatch = async (batchId) => {
  selectedBatchId.value = batchId;

  // 若已缓存则直接显示
  if (batchCache.has(batchId)) {
    displayList.value = batchCache.get(batchId);
    return;
  }

  loadingBatch.value = true;
  await new Promise((resolve, reject) => {
    get('/api/teacher/Ai/GetQuestionById', { questionId: batchId }, (msg, data) => {
      const list = Array.isArray(data) ? data : [];
      const mapped = list.map(q => {
        // 后端返回 questionType (中文) / difficulty 数字 / options JSON 字符串
        const internalType = (() => {
          switch (q.questionType) {
            case '选择题': return 'CHOICE';
            case '填空题': return 'FILL';
            case '判断题': return 'JUDGE';
            case '简答题': return 'SHORT';
            default: return 'CHOICE';
          }
        })();

        let optionsArr = [];
        if (internalType === 'CHOICE' && q.options) {
          try {
            const arr = JSON.parse(q.options);
            optionsArr = arr.map(str => {
              const match = str.match(/^([A-D])\.\s*(.*)$/i);
              return match ? { value: match[1], label: match[2] } : { value: '', label: str };
            });
          } catch {}
        }

        return {
          id: q.id || Date.now(),
          questionId: q.questionId,
          type: internalType,
          typeLabel: typeLabelMap[internalType] || internalType,
          level: q.difficulty === 1 ? 'EASY' : q.difficulty === 5 ? 'HARD' : 'MEDIUM',
          title: q.content,
          options: optionsArr,
          answer: q.answer,
          chapter: '',
          subject: q.createdBy || 'AI'
        };
      });
      batchCache.set(batchId, mapped);
      displayList.value = mapped;
      resolve();
    }, (failMsg) => {
      ElMessage.error(failMsg);
      reject(new Error(failMsg));
    }, (err) => {
      ElMessage.error('获取批次题目失败');
      reject(err);
    });
  }).finally(() => loadingBatch.value = false);
};

// 批次列表
const loadBatchList = () => {
  get('/api/teacher/Ai/Batches', null, (msg, data) => {
    batchList.value = (Array.isArray(data) ? data : []).map(b => ({
      id: b.questionId,
      name: b.batchName,
      count: b.count || 0
    }));

    // 默认选择首条批次并加载其题目列表
    if (batchList.value.length > 0) {
      // 使用 nextTick 确保响应式数据已更新再调用
      nextTick(() => selectBatch(batchList.value[0].id));
    }
  }, (fail)=>ElMessage.error(fail), ()=>ElMessage.error('获取批次失败'));
};

// 筛选条件
const filters = reactive({
  subject: '',
  chapter: '',
  difficulty: '',
  type: ''
});

// 生成表单
const generationForm = reactive({
  subject: '',          // 学科
  count: 5,            // 题目数量
  knowledgePoints: [], // 知识点数组，标签形式
  level: 'MEDIUM',     // 难度：EASY/MEDIUM/HARD
  type: 'CHOICE',      // 题型：CHOICE/FILL/JUDGE/SHORT
  requirements: ''
});

// 题目类型选项
const questionTypes = [
  { label: '选择题', value: 'CHOICE' },
  { label: '填空题', value: 'FILL' },
  { label: '判断题', value: 'JUDGE' },
  { label: '简答题', value: 'SHORT' }
];

const typeLabelMap = {
  'CHOICE': '选择题',
  'FILL': '填空题',
  'JUDGE': '判断题',
  'SHORT': '简答题'
};

// 难度选项
const levelOptions = [
  { label: '简单', value: 'EASY' },
  { label: '中等', value: 'MEDIUM' },
  { label: '困难', value: 'HARD' }
];

// 正在生成状态
const generating = ref(false);

// 生成进度
const generationProgress = ref(0);

// 编辑对话框
const editDialogVisible = ref(false);
const editForm = reactive({
  id: null,
  title: '',
  answer: ''
});

const saveEdit = () => {
  const idx = questionList.value.findIndex(q => q.id === editForm.id);
  if (idx !== -1) {
    questionList.value[idx].title = editForm.title;
    questionList.value[idx].answer = editForm.answer;
    ElMessage.success('保存成功');
  }
  editDialogVisible.value = false;
};

// 详情对话框可见状态
const detailDialogVisible = ref(false);
const currentQuestion = ref(null);

// 是否显示生成表单
const showGenerationForm = ref(false);

// 批次列表
const batchList = ref([
  {
    id: 1,
    name: '面向对象程序设计练习',
    createTime: '2023-06-15 10:30:22',
    count: 15,
    subject: '计算机科学'
  },
  {
    id: 2,
    name: '数据结构基础测试',
    createTime: '2023-06-12 14:20:45',
    count: 20,
    subject: '计算机科学'
  },
  {
    id: 3,
    name: '网络协议综合练习',
    createTime: '2023-05-28 09:15:30',
    count: 10,
    subject: '计算机网络'
  }
]);

// 监听页面加载
onMounted(() => {
  loadQuestionList();
  loadBatchList();
});

// 加载题库列表（对接后端）
const loadQuestionList = async () => {
  try {
    await new Promise((resolve, reject) => {
      get('/api/teacher/Ai/Questions', null, (msg, data) => {
        resolve({ success:true, data });
      }, (failMsg)=>{
        reject(new Error(failMsg));
      }, (err)=>{
        reject(err);
      });
    }).then(({data})=>{
      const list = Array.isArray(data) ? data : [];
      questionList.value = list.map(q => ({
        ...q,
        typeLabel: typeLabelMap[q.type] || q.type,
        level: q.level || (q.difficulty === 1 ? 'EASY' : q.difficulty === 3 ? 'MEDIUM' : 'HARD')
      }));

      displayList.value = questionList.value;

      // 构建批次
      
    });
  } catch (e) {
    console.error('获取题库失败:', e);
    ElMessage.error('获取题库数据失败，请稍后重试');
  }
};

// 生成模拟题目数据
// 已弃用：不再使用本地模拟数据

// 根据题型生成标题
const getQuestionTitleByType = (type) => {
  switch (type) {
    case 'CHOICE':
      return '下列关于算法复杂度的描述中，哪一项是正确的？';
    case 'FILL':
      return '归并排序的空间复杂度为____。';
    case 'JUDGE':
      return '快速排序的最坏时间复杂度是O(n²)。';
    case 'SHORT':
      return '简述动态规划的基本原理和适用条件。';
    default:
      return '示例题目';
  }
};

// 开始批量生成题目（SSE 流式对接后端）
const startGenerateQuestions = async () => {
  if (generating.value) return;

  generating.value = true;
  generationProgress.value = 0;

  try {
    const payload = {
      count: generationForm.count,
      subject: generationForm.subject,
      knowledgePoints: generationForm.knowledgePoints,
      level: generationForm.level,
      type: generationForm.type
    };

    const token = localStorage.getItem('authToken');
    const sseHeaders = {
      'Content-Type': 'application/json',
      'Accept': 'text/event-stream'
    };
    if (token) sseHeaders['Authorization'] = `Bearer ${token}`;

    const res = await fetch('/api/teacher/Ai/GenerateQuestions', {
      method: 'POST',
      headers: sseHeaders,
      body: JSON.stringify(payload)
    });

    if (!res.ok) throw new Error('生成接口请求失败');

    const reader = res.body.getReader();
    const decoder = new TextDecoder('utf-8');
    let buffer = '';
    let received = 0;

    // 读取 SSE 流
    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      buffer += decoder.decode(value, { stream: true });

      const lines = buffer.split('\n');
      buffer = lines.pop(); // 保留最后一个不完整行

      for (const line of lines) {
        const trimmed = line.trim();
        if (!trimmed || !trimmed.startsWith('data:')) continue;
        const jsonStr = trimmed.replace(/^data:\s*/, '');

        if (jsonStr === '[DONE]' || jsonStr === '[done]') {
          generationProgress.value = 100;
          continue;
        }

        let obj;
        try { obj = JSON.parse(jsonStr); } catch (e) { continue; }

        if (obj.error) {
          ElMessage.error(obj.error);
          continue;
        }

        questionList.value.unshift({
          ...obj,
          typeLabel: typeLabelMap[obj.type] || obj.type,
          level: obj.level || 'MEDIUM'
        });

        received += 1;
        generationProgress.value = Math.min(100, (received / generationForm.count) * 100);
      }
    }

    ElMessage.success(`成功生成 ${received} 道题目`);
  } catch (e) {
    console.error(e);
    ElMessage.error('生成失败，请稍后重试');
  } finally {
    generating.value = false;
    showGenerationForm.value = false;

    // 重新加载题库及批次，确保页面数据最新
    await loadQuestionList();
    loadBatchList();
  }
};

// 查看题目详情
const viewQuestionDetail = (question) => {
  currentQuestion.value = question;
  detailDialogVisible.value = true;
};

// 删除题目
const deleteQuestion = (question) => {
  ElMessageBox.confirm('确定要删除这道题目吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    post(`/api/teacher/Ai/Questions/${question.id}/delete`, {}, async () => {
      ElMessage.success('删除成功');
      // 删除成功后刷新题库和批次列表
      await loadQuestionList();
      loadBatchList();
    }, (failMsg) => {
      ElMessage.error(failMsg || '删除失败');
    }, () => {
      ElMessage.error('删除请求发生错误');
    });
  }).catch(() => {/* 用户取消 */});
};

// 编辑题目
const editQuestion = (question) => {
  editForm.id = question.id;
  editForm.title = question.title;
  editForm.answer = typeof question.answer === 'string' ? question.answer : JSON.stringify(question.answer);
  editDialogVisible.value = true;
};

// 添加到试卷
const addToExam = (question) => {
  // 实际项目中应该打开试卷选择对话框
  ElMessage.success('已添加到试卷');
};

// 重新生成题目
const regenerateQuestion = (question) => {
  ElMessageBox.confirm('确定要重新生成这道题目吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    // 实际项目中应该发起API请求重新生成
    ElMessage.success('重新生成请求已提交');
  }).catch(() => {});
};
</script>

<template>
  <div class="question-bank-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-title">
        <h2 class="text-xl font-bold">智能题库管理</h2>
        <span class="text-sm text-gray-500">利用人工智能高效生成、管理和组织题目</span>
      </div>
      <div class="page-actions">
        <el-button class="generate-btn" @click="showGenerationForm = true">
          <i class="el-icon-plus mr-1"></i>批量生成题目
        </el-button>
      </div>
    </div>
    
    <!-- 内容区域 -->
    <el-card class="main-content">
      <div class="content-layout">
        <!-- 左侧批次列表 -->
        <div class="batch-list-panel">
          <div class="panel-header">
            <h3 class="text-base font-medium">生成批次</h3>
            <el-button type="text" size="small" @click="() => {selectedBatchId = null; displayList.value = questionList.value;}">全部</el-button>
          </div>
          <div class="batch-list">
            <div 
              v-for="batch in batchList" 
              :key="batch.id"
              class="batch-item"
              :class="{ 'border-blue-500': selectedBatchId === batch.id }"
              @click="selectBatch(batch.id)"
            >
              <div class="item-header">
                <span class="item-title">{{ batch.name }}</span>
                <span class="item-badge">{{ batch.count }}题</span>
              </div>
              <div class="item-info">
                <span class="text-gray-500">{{ batch.subject }}</span>
                <span class="text-gray-400 text-xs">{{ batch.createTime }}</span>
              </div>
              <div class="item-actions">
                <el-button type="text" size="small">查看</el-button>
                <el-button type="text" size="small">编辑</el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧题库内容 -->
        <div class="question-list-panel">
          <!-- 筛选工具栏 -->
          <div class="filter-toolbar">
            <el-form :inline="true" :model="filters" size="small">
              <el-form-item>
                <el-input
                  v-model="filters.keyword"
                  placeholder="搜索题目"
                  prefix-icon="el-icon-search"
                />
              </el-form-item>
              <el-form-item>
                <el-select v-model="filters.subject" placeholder="学科">
                  <el-option label="全部" value=""></el-option>
                  <el-option label="计算机科学" value="计算机科学"></el-option>
                  <el-option label="计算机网络" value="计算机网络"></el-option>
                  <el-option label="数据库系统" value="数据库系统"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-select v-model="filters.type" placeholder="题型">
                  <el-option label="全部" value=""></el-option>
                  <el-option 
                    v-for="type in questionTypes" 
                    :key="type.value" 
                    :label="type.label" 
                    :value="type.value"
                  ></el-option>
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-select v-model="filters.difficulty" placeholder="难度">
                  <el-option label="全部" value=""></el-option>
                  <el-option 
                    v-for="diff in levelOptions" 
                    :key="diff.value" 
                    :label="diff.label" 
                    :value="diff.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-form>
          </div>
          
          <!-- 题目列表 -->
          <div class="question-list" v-loading="loadingBatch">
            <div 
              v-for="question in filteredQuestions" 
              :key="question.id"
              class="question-card"
              @click="viewQuestionDetail(question)"
            >
              <div class="question-header">
                <div class="question-type">
                  {{ question.typeLabel }}
                </div>
                <div class="question-difficulty" :class="{
                  'bg-green-100 text-green-800': question.level === 'EASY',
                  'bg-blue-100 text-blue-800': question.level === 'MEDIUM',
                  'bg-red-100 text-red-800': question.level === 'HARD',
                }">
                  {{ 
                    question.level === 'EASY' ? '简单' : 
                    question.level === 'MEDIUM' ? '中等' : 
                    '困难'
                  }}
                </div>
              </div>
              
              <div class="question-title">
                {{ question.title }}
              </div>
              
              <!-- 选择题选项 -->
              <div v-if="question.type === 'CHOICE'" class="question-options">
                <div v-for="option in question.options" :key="option.value" class="option-item">
                  <span class="option-label">{{ option.value }}.</span>
                  <span class="option-content">{{ option.label }}</span>
                </div>
              </div>

              <!-- 答案 -->
              <div class="question-answer">
                答案：{{ question.answer }}
              </div>
              
              <div class="question-footer">
                <div class="question-meta">
                  <span class="meta-item">{{ question.subject }}</span>
                  <span class="meta-item">{{ question.chapter }}</span>
                </div>
                
                <div class="question-actions">
                  <el-button type="text" size="small" @click.stop="editQuestion(question)">编辑</el-button>
                  <el-button type="text" size="small" @click.stop="addToExam(question)">加入试卷</el-button>
                  <el-button type="text" size="small" @click.stop="deleteQuestion(question)">删除</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
    
    <!-- 题目详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="题目详情"
      width="60%"
      class="question-detail-dialog"
      :destroy-on-close="true"
    >
      <div v-if="currentQuestion" class="question-detail">
        <div class="detail-header">
          <div class="detail-meta">
            <el-tag size="small" effect="plain">{{ currentQuestion.typeLabel }}</el-tag>
            <el-tag 
              size="small" 
              effect="plain" 
              :type="
                currentQuestion.level === 'EASY' ? 'success' : 
                currentQuestion.level === 'MEDIUM' ? 'primary' : 
                'danger'
              "
            >
              {{ 
                currentQuestion.level === 'EASY' ? '简单' : 
                currentQuestion.level === 'MEDIUM' ? '中等' : 
                '困难'
              }}
            </el-tag>
            <span class="text-gray-500 text-sm">{{ currentQuestion.subject }} / {{ currentQuestion.chapter }}</span>
          </div>
          <div class="detail-actions">
            <el-button size="small" @click="editQuestion(currentQuestion)">编辑</el-button>
            <el-button size="small" @click="regenerateQuestion(currentQuestion)">重新生成</el-button>
            <el-button type="primary" size="small" @click="addToExam(currentQuestion)">加入试卷</el-button>
          </div>
        </div>
        
        <div class="detail-content">
          <div class="detail-title">
            <div class="title-number">题目ID: {{ currentQuestion.id }}</div>
            <div class="title-content">{{ currentQuestion.title }}</div>
          </div>
          
          <!-- 选择题选项 -->
          <div v-if="currentQuestion.type === 'CHOICE'" class="detail-options">
            <div v-for="option in currentQuestion.options" :key="option.value" class="detail-option-item">
              <div class="option-label" :class="{ 
                'option-correct': 
                  (currentQuestion.type === 'CHOICE' && currentQuestion.answer === option.value)
              }">
                {{ option.value }}
              </div>
              <div class="option-content">{{ option.label }}</div>
            </div>
          </div>
          
          <!-- 答案 -->
          <div class="detail-answer">
            <div class="answer-label">参考答案</div>
            <div class="answer-content">
              <template v-if="currentQuestion.type === 'CHOICE'">
                {{ currentQuestion.answer }}
              </template>
              <template v-else-if="currentQuestion.type === 'JUDGE'">
                {{ currentQuestion.answer }}
              </template>
              <template v-else-if="currentQuestion.type === 'FILL'">
                {{ currentQuestion.answer }}
              </template>
              <template v-else-if="currentQuestion.type === 'SHORT'">
                {{ currentQuestion.answer }}
              </template>
              <template v-else>
                {{ currentQuestion.answer }}
              </template>
            </div>
          </div>
          
          <!-- 解析 -->
          <div class="detail-analysis">
            <div class="analysis-label">解析</div>
            <div class="analysis-content">
              这里是题目的详细解析，包含解题思路、知识点讲解等。
              <br><br>
              本题涉及的知识点：算法复杂度分析、排序算法特性、最优解法选择等。
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
    
    <!-- 题目编辑对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑题目" width="600px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="题干">
          <el-input v-model="editForm.title" type="textarea" autosize />
        </el-form-item>
        <el-form-item label="答案">
          <el-input v-model="editForm.answer" type="textarea" autosize />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 生成题目对话框 -->
    <el-dialog
      v-model="showGenerationForm"
      title="批量生成题目"
      width="600px"
      class="generation-dialog"
      top="10vh"
      modal-class="generation-dialog-modal"
    >
      <el-form
        :model="generationForm"
        label-position="top"
        size="large"
        class="dialog-form grid grid-cols-1 sm:grid-cols-2 gap-6"
      >
        <el-form-item label="学科">
          <el-input v-model="generationForm.subject" placeholder="请输入学科"></el-input>
        </el-form-item>
        <el-form-item label="题目数量">
          <el-input-number
            v-model="generationForm.count"
            :min="1"
            :max="20"
            controls-position="right"
            class="styled-number"
          />
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="generationForm.level" placeholder="请选择难度" style="width: 100%">
            <el-option 
              v-for="item in levelOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="题目类型">
          <el-select v-model="generationForm.type" placeholder="请选择题目类型" style="width: 100%">
            <el-option 
              v-for="item in questionTypes"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="知识点">
          <el-select
            v-model="generationForm.knowledgePoints"
            multiple
            collapse-tags
            allow-create
            filterable
            default-first-option
            :reserve-keyword="false"
            placeholder="输入后回车即可添加标签"
            style="width: 100%"
          >
          </el-select>
        </el-form-item>
        <el-form-item label="特殊要求" class="sm:col-span-2">
          <el-input
            v-model="generationForm.requirements"
            type="textarea"
            :rows="3"
            placeholder="有特殊要求可以在这里说明"
            class="styled-textarea"
          />
        </el-form-item>
      </el-form>
      
      <div v-if="generating" class="generation-progress">
        <el-progress :percentage="generationProgress" :format="format => `${format}%`"></el-progress>
        <div class="text-center text-sm text-gray-500 mt-2">
          正在生成题目，请稍候...
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showGenerationForm = false">取消</el-button>
          <el-button 
            type="primary" 
            :loading="generating"
            @click="startGenerateQuestions"
          >开始生成</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.question-bank-container {
  @apply max-w-7xl mx-auto px-6 py-8;
  background: linear-gradient(180deg, #f7f9fc 0%, #ffffff 60%);
}

.page-header {
  @apply flex justify-between items-center mb-6;
  animation: fadeSlide 0.6s ease;
}

@keyframes fadeSlide {
  0% { opacity: 0; transform: translateY(-10px); }
  100% { opacity: 1; transform: translateY(0); }
}

.page-title {
  @apply flex flex-col;
}

.main-content {
  min-height: calc(100vh - 200px);
  border: none;
  border-radius: 1rem;
  box-shadow: 0 6px 20px rgba(0,0,0,0.08);
}

.content-layout {
  @apply flex gap-6;
  height: calc(100vh - 230px);
}

/* 批次列表面板 */
.batch-list-panel {
  @apply w-72 bg-white rounded-xl shadow-md p-5 flex flex-col mr-6;
}

.panel-header {
  @apply flex justify-between items-center mb-4;
}

.batch-list {
  @apply space-y-4 overflow-auto;
  max-height: calc(100vh - 280px);
}

.batch-item {
  @apply border border-gray-200 rounded-xl p-4 cursor-pointer bg-white transition transform duration-300 ease-out hover:-translate-y-1 hover:shadow-lg hover:border-blue-400;
}

.item-header {
  @apply flex justify-between items-center mb-2;
}

.item-title {
  @apply font-medium;
}

.item-badge {
  @apply bg-blue-100 text-blue-800 text-xs px-2 py-1 rounded-full;
}

.item-info {
  @apply flex justify-between items-center mb-2;
}

.item-actions {
  @apply flex justify-end space-x-2;
}

/* 题库列表面板 */
.question-list-panel {
  @apply flex-1 overflow-hidden flex flex-col bg-white rounded-xl shadow-md p-6;
}

.filter-toolbar {
  @apply mb-4 pb-4 bg-white rounded-xl shadow-md px-4 border border-transparent flex items-center gap-4;
}

.filter-toolbar :deep(.el-input__wrapper) {
  @apply rounded-full shadow-sm border border-gray-200 hover:shadow-md focus-within:shadow-lg transition-all;
}

.filter-toolbar :deep(.el-input__inner) {
  @apply placeholder-gray-400 text-sm;
}

.filter-toolbar .el-form-item:first-child {
  @apply flex-1;
}

.question-list {
  @apply grid gap-5 overflow-y-auto;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  max-height: calc(100vh - 350px);
}

.question-card {
  @apply border border-transparent rounded-xl p-6 bg-white cursor-pointer transform transition duration-300 ease-out hover:-translate-y-1 hover:shadow-lg hover:border-blue-400 shadow-sm;
}

.question-card:hover {
  @apply scale-105;
  transition: transform 0.2s ease-in-out;
}

.question-header {
  @apply flex justify-between items-center mb-3;
}

.question-type {
  @apply bg-gray-100 text-gray-700 px-2 py-1 rounded text-xs;
}

.question-difficulty {
  @apply px-2 py-1 rounded text-xs;
}

.question-title {
  @apply text-base mb-3;
}

.question-options {
  @apply space-y-2 mb-3;
}

.option-item {
  @apply flex text-sm;
}

.option-label {
  @apply mr-2 font-medium;
}

.question-footer {
  @apply flex justify-between items-center mt-3 pt-3 border-t border-gray-200;
}

.question-meta {
  @apply flex items-center;
}

.meta-item {
  @apply text-xs text-gray-500 mr-2;
}

.question-actions {
  @apply opacity-0 transition-opacity;
}

.question-card:hover .question-actions {
  @apply opacity-100;
}

/* 详情抽屉 */
.question-detail {
  @apply p-6;
}

.detail-header {
  @apply flex justify-between items-center mb-6 pb-4 border-b border-gray-200;
}

.detail-meta {
  @apply space-x-2 flex items-center;
}

.detail-content {
  @apply space-y-6;
}

.detail-title {
  @apply space-y-2;
}

.title-number {
  @apply text-sm text-gray-500;
}

.title-content {
  @apply text-lg font-medium;
}

.detail-options {
  @apply space-y-3 mt-4;
}

.detail-option-item {
  @apply flex;
}

.option-label {
  @apply w-8 h-8 rounded-full flex items-center justify-center text-gray-700 border border-gray-300 mr-3;
}

.option-label.option-correct {
  @apply bg-green-500 text-white border-green-500;
}

.detail-answer {
  @apply bg-blue-50 p-4 rounded-md;
}

.answer-label {
  @apply text-blue-800 font-medium mb-2;
}

.detail-analysis {
  @apply bg-gray-50 p-4 rounded-md;
}

.analysis-label {
  @apply text-gray-700 font-medium mb-2;
}

.code-block {
  @apply bg-gray-100 p-3 rounded font-mono text-sm whitespace-pre-wrap;
}

/* 生成进度 */
.generation-progress {
  @apply mt-4;
}

/* —— 批量生成对话框美化 —— */
.generation-dialog :deep(.el-dialog) {
  /* 圆角 + 阴影 + 入场动画 */
  border-radius: 1.25rem;
  box-shadow: 0 20px 45px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  animation: dialogFadeSlide 0.4s ease-out;
  transform-origin: center top;
}

@keyframes dialogFadeSlide {
  0% {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.generation-dialog :deep(.el-dialog__footer) {
  @apply bg-gray-50 rounded-b-xl flex justify-end py-4 px-6;
}

/* 输入组件圆角与交互动画 */
.generation-dialog :deep(.el-input__wrapper),
.generation-dialog :deep(.el-textarea__inner),
.generation-dialog :deep(.el-input-number) {
  @apply rounded-lg shadow-sm transition-all duration-300 border border-gray-200 hover:shadow-md focus-within:ring-2 focus-within:ring-blue-400;
}

.generation-dialog :deep(.el-select .el-input__wrapper) {
  @apply rounded-lg;
}

.generation-dialog :deep(.el-input-number__increase),
.generation-dialog :deep(.el-input-number__decrease) {
  @apply transition-transform duration-200 hover:scale-110;
}

/* 生成按钮 */
.generate-btn {
  @apply bg-gradient-to-r from-blue-500 via-purple-500 to-pink-500 text-white border-none px-6 shadow-md hover:shadow-lg transition-all duration-300 ease-out rounded-full;
}

.generate-btn:hover {
  @apply -translate-y-0.5;
}

.generation-dialog :deep(.el-dialog__header) {
  @apply bg-gradient-to-r from-purple-500 to-pink-500 text-white rounded-t-xl;
}

.generation-dialog :deep(.el-dialog__body) {
  @apply bg-gray-50 rounded-b-xl p-8;
}

.generation-dialog :deep(.el-form-item) {
  @apply mb-4;
}

.generation-dialog :deep(.el-input__wrapper) {
  @apply rounded-lg;
}

/* ========== 生成对话框表单美化 ========== */

:global(.generation-dialog .dialog-form .el-form-item__label) {
  @apply font-medium text-gray-700 mb-1;
}

:global(.generation-dialog .el-input__wrapper),
:global(.generation-dialog .el-select .el-input__wrapper),
:global(.generation-dialog .el-textarea__inner),
:global(.generation-dialog .styled-number .el-input-number__decrease),
:global(.generation-dialog .styled-number .el-input-number__increase) {
  border-radius: 0.75rem !important; /* 12px */
  background-color: #f9fafb !important;
  border: 1px solid #e5e7eb !important;
  transition: box-shadow .2s ease, border-color .2s ease;
}

:global(.generation-dialog .el-input__wrapper:hover),
:global(.generation-dialog .el-select .el-input__wrapper:hover),
:global(.generation-dialog .el-textarea__inner:hover) {
  border-color: #cbd5e1 !important;
}

:global(.generation-dialog .el-input__wrapper.is-focus),
:global(.generation-dialog .el-select .el-input__wrapper.is-focus),
:global(.generation-dialog .el-textarea__inner:focus-within) {
  border-color: #6366f1 !important; /* indigo-500 */
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.15) !important;
}

/* 数字输入控件按钮悬浮动画 */
:global(.generation-dialog .styled-number .el-input-number__increase:hover),
:global(.generation-dialog .styled-number .el-input-number__decrease:hover) {
  background-color: #eef2ff !important; /* indigo-50 */
}
</style>