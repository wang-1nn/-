<template>
  <div class="lesson-plan-history">
    <!-- 头部工具栏 -->
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-xl font-semibold text-gray-800">教案历史</h2>
      <div class="flex items-center gap-4">
        <!-- 筛选器 -->
        <el-select v-model="filters.status" placeholder="状态筛选" clearable size="small" style="width: 120px">
          <el-option label="草稿" value="draft" />
          <el-option label="已发布" value="published" />
          <el-option label="已归档" value="archived" />
        </el-select>

        <el-select v-model="filters.subject" placeholder="学科筛选" clearable size="small" style="width: 120px">
          <el-option label="数学" value="数学" />
          <el-option label="语文" value="语文" />
          <el-option label="英语" value="英语" />
          <el-option label="物理" value="物理" />
          <el-option label="化学" value="化学" />
        </el-select>

        <el-button @click="refreshList" :loading="loading" size="small" type="primary">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 教案列表 -->
    <div v-loading="loading" class="lesson-plan-list">
      <div v-if="lessonPlans.length === 0" class="empty-state text-center py-12">
        <el-icon size="48" class="text-gray-400 mb-4"><Document /></el-icon>
        <p class="text-gray-500">暂无教案记录</p>
        <p class="text-gray-400 text-sm mt-2">开始创建您的第一个教案吧</p>
      </div>

      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div
            v-for="plan in lessonPlans"
            :key="plan.id"
            class="lesson-plan-card bg-white rounded-lg border border-gray-200 hover:shadow-md transition-shadow cursor-pointer"
            @click="viewLessonPlan(plan)"
        >
          <div class="p-4">
            <!-- 标题和状态 -->
            <div class="flex justify-between items-start mb-3">
              <h3 class="font-medium text-gray-900 line-clamp-2">{{ plan.title }}</h3>
              <el-tag :type="getStatusType(plan.status)" size="small">
                {{ getStatusText(plan.status) }}
              </el-tag>
            </div>

            <!-- 基本信息 -->
            <div class="space-y-2 text-sm text-gray-600 mb-4">
              <div class="flex items-center gap-2">
                <el-icon><School /></el-icon>
                <span>{{ plan.subject || '未设置' }} · {{ plan.grade || '未设置' }}</span>
              </div>
              <div class="flex items-center gap-2">
                <el-icon><Clock /></el-icon>
                <span>{{ plan.duration || 45 }}分钟</span>
              </div>
              <div class="flex items-center gap-2">
                <el-icon><Calendar /></el-icon>
                <span>{{ formatDate(plan.updatedAt) }}</span>
              </div>
            </div>

            <!-- AI标识 -->
            <div v-if="plan.isAiGenerated" class="flex items-center gap-1 text-xs text-blue-600 mb-3">
              <el-icon><MagicStick /></el-icon>
              <span>AI生成</span>
            </div>

            <!-- 操作按钮 -->
            <div class="flex justify-between items-center">
              <div class="flex gap-2">
                <el-button @click.stop="editLessonPlan(plan)" size="small" type="primary" link>
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button @click.stop="duplicateLessonPlan(plan)" size="small" type="success" link>
                  <el-icon><CopyDocument /></el-icon>
                  复制
                </el-button>
              </div>
              <el-dropdown @command="handleCommand" trigger="click">
                <el-button size="small" type="info" link>
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :command="{action: 'export', plan}">
                      <el-icon><Download /></el-icon>
                      导出
                    </el-dropdown-item>
                    <el-dropdown-item :command="{action: 'archive', plan}" v-if="plan.status !== 'archived'">
                      <el-icon><FolderOpened /></el-icon>
                      归档
                    </el-dropdown-item>
                    <el-dropdown-item :command="{action: 'delete', plan}" divided>
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="flex justify-center mt-6">
      <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </div>

    <!-- 教案预览弹窗 -->
    <el-dialog v-model="showPreview" title="教案预览" width="80%" top="5vh">
      <div v-if="selectedPlan" class="prose max-w-none" v-html="selectedPlan.content"></div>
      <template #footer>
        <el-button @click="showPreview = false">关闭</el-button>
        <el-button type="primary" @click="editLessonPlan(selectedPlan)">编辑</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Document, Refresh, School, Clock, Calendar, MagicStick,
  Edit, CopyDocument, MoreFilled, Download, FolderOpened, Delete
} from '@element-plus/icons-vue';
import { useAuthStore } from '@/stores/counter.js';
import { lessonPlanAPI, LessonPlanStatus } from '@/api/lessonPlan.js';

// 用户信息
const authStore = useAuthStore();
const uid = computed(() => authStore.user?.id || authStore.user?.userId || '1');

// 状态管理
const loading = ref(false);
const lessonPlans = ref([]);
const total = ref(0);
const showPreview = ref(false);
const selectedPlan = ref(null);

// 筛选和分页
const filters = reactive({
  status: '',
  subject: ''
});

const pagination = reactive({
  page: 1,
  size: 12
});

// 获取教案列表
const fetchLessonPlans = async () => {
  loading.value = true;
  try {
    const params = {
      uid: uid.value,
      status: filters.status,
      subject: filters.subject,
      page: pagination.page,
      size: pagination.size
    };

    const { data } = await lessonPlanAPI.getLessonPlans(params);
    lessonPlans.value = data.list || [];
    total.value = data.total || 0;
  } catch (error) {
    ElMessage.error('获取教案列表失败: ' + error.message);
  } finally {
    loading.value = false;
  }
};

// 查看教案详情
const viewLessonPlan = (plan) => {
  selectedPlan.value = plan;
  showPreview.value = true;
};

// 编辑教案
const editLessonPlan = (plan) => {
  // 触发编辑事件，由父组件处理
  emit('edit-lesson-plan', plan);
};

// 复制教案
const duplicateLessonPlan = async (plan) => {
  try {
    const newPlan = {
      ...plan,
      id: undefined,
      title: plan.title + ' (副本)',
      status: 'draft',
      createdAt: undefined,
      updatedAt: undefined
    };

    await lessonPlanAPI.saveLessonPlan(newPlan, uid.value);
    ElMessage.success('教案复制成功');
    fetchLessonPlans();
  } catch (error) {
    ElMessage.error('复制教案失败: ' + error.message);
  }
};

// 处理下拉菜单命令
const handleCommand = async ({ action, plan }) => {
  switch (action) {
    case 'export':
      exportLessonPlan(plan);
      break;
    case 'archive':
      await archiveLessonPlan(plan);
      break;
    case 'delete':
      await deleteLessonPlan(plan);
      break;
  }
};

// 归档教案
const archiveLessonPlan = async (plan) => {
  try {
    await lessonPlanAPI.updateLessonPlan(plan.id, { ...plan, status: 'archived' }, uid.value);
    ElMessage.success('教案已归档');
    fetchLessonPlans();
  } catch (error) {
    ElMessage.error('归档失败: ' + error.message);
  }
};

// 删除教案
const deleteLessonPlan = async (plan) => {
  try {
    await ElMessageBox.confirm('确定要删除这个教案吗？此操作不可恢复。', '确认删除', {
      type: 'warning'
    });

    await lessonPlanAPI.deleteLessonPlan(plan.id, uid.value);
    ElMessage.success('教案已删除');
    fetchLessonPlans();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + error.message);
    }
  }
};

// 导出教案
const exportLessonPlan = (plan) => {
  // 创建下载链接
  const content = plan.markdownContent || plan.content;
  const blob = new Blob([content], { type: 'text/markdown' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `${plan.title}.md`;
  a.click();
  URL.revokeObjectURL(url);
  ElMessage.success('教案导出成功');
};

// 工具函数
const getStatusType = (status) => {
  const types = {
    draft: '',
    published: 'success',
    archived: 'info'
  };
  return types[status] || '';
};

const getStatusText = (status) => {
  const texts = {
    draft: '草稿',
    published: '已发布',
    archived: '已归档'
  };
  return texts[status] || status;
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString('zh-CN');
};

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size;
  pagination.page = 1;
  fetchLessonPlans();
};

const handleCurrentChange = (page) => {
  pagination.page = page;
  fetchLessonPlans();
};

// 刷新列表
const refreshList = () => {
  fetchLessonPlans();
};

// 监听筛选条件变化
watch([() => filters.status, () => filters.subject], () => {
  pagination.page = 1;
  fetchLessonPlans();
});

// 组件事件
const emit = defineEmits(['edit-lesson-plan']);

// 初始化
onMounted(() => {
  fetchLessonPlans();
});
</script>

<style scoped>
.lesson-plan-card {
  transition: all 0.2s ease;
}

.lesson-plan-card:hover {
  transform: translateY(-2px);
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.prose {
  max-width: none;
}

.prose h1, .prose h2, .prose h3 {
  margin-top: 1.5em;
  margin-bottom: 0.5em;
}

.prose p {
  margin-bottom: 1em;
}
</style>
