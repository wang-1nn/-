<template>
  <div class="lesson-prep-workbench flex flex-col h-full bg-slate-50">
    <!-- 模型状态提示条 -->
    <ModelStatusToast :auto-hide="true" :hide-delay="5000" />

    <!-- 步骤进度 -->
    <StepsProgress
        v-model:currentStep="currentStep"
        :scrollProgress="scrollProgress"
        class="mb-2 px-10 pt-4"
    />

    <!-- 主布局区域 -->
    <div class="flex h-full gap-8 px-10 py-6 w-full">
      <!-- 左侧区域 - 步骤控制 -->
      <div class="w-[420px] flex-shrink-0 flex flex-col gap-4">
        <!-- 第一步：上传大纲 -->
        <div v-show="currentStep === 0" class="flex flex-col h-full gap-4">
          <div class="bg-white p-4 rounded-xl shadow-sm">
            <h3 class="text-lg font-medium text-slate-700 mb-3">上传教学大纲</h3>
            <OutlineUploader
                @file-selected="handleFileSelected"
                @upload-complete="handleUploadComplete"
                @upload-progress="handleUploadProgress"
            />
          </div>
        </div>

        <!-- 第二步：选择模板 -->
        <div v-show="currentStep === 1" class="flex flex-col h-full gap-4">
          <div class="bg-white p-4 rounded-xl shadow-sm">
            <h3 class="text-lg font-medium text-slate-700 mb-3">选择教案模板</h3>
            <TemplateSelector
                v-model="selectedTemplate"
                :templates="availableTemplates"
                @template-selected="handleTemplateSelected"
            />

            <div class="mt-4 flex justify-between">
              <el-button @click="currentStep = 0">
                <el-icon class="mr-1"><ArrowLeft /></el-icon>
                返回上传
              </el-button>
              <el-button
                  type="primary"
                  :disabled="!selectedTemplate"
                  @click="currentStep = 2"
              >
                下一步
                <el-icon class="ml-1"><ArrowRight /></el-icon>
              </el-button>
            </div>
          </div>
        </div>

        <!-- 第三步：生成教案 -->
        <div v-show="currentStep === 2" class="flex flex-col h-full gap-4">
          <div class="bg-white p-4 rounded-xl shadow-sm">
            <h3 class="text-lg font-medium text-slate-700 mb-3">生成教案</h3>

            <!-- 显示选中的模板信息 -->
            <div v-if="selectedTemplate" class="mb-4 p-3 bg-blue-50 rounded-lg">
              <div class="flex items-center">
                <el-icon class="text-blue-600 mr-2"><Document /></el-icon>
                <span class="font-medium text-blue-800">{{ selectedTemplate.name }}</span>
              </div>
              <p class="text-sm text-blue-600 mt-1">{{ selectedTemplate.description }}</p>
            </div>

            <GenerationControl
                :is-generating="isGenerating"
                :generation-progress="generationProgress"
                :selected-template="selectedTemplate"
                @start-generation="handleGenerateLessonPlan"
            />

            <div class="mt-4 flex justify-between">
              <el-button @click="currentStep = 1">
                <el-icon class="mr-1"><ArrowLeft /></el-icon>
                返回模板选择
              </el-button>
              <el-button
                  v-if="lessonContent"
                  type="success"
                  @click="handleSaveLessonPlan"
              >
                <el-icon class="mr-1"><Download /></el-icon>
                保存教案
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧渲染区域 -->
      <div class="flex-1 flex flex-col min-w-0">
        <!-- 第一步：大纲上传和预览 -->
        <div v-if="currentStep === 0" class="flex-1 flex flex-col">
          <!-- 如果没有大纲内容，显示空状态 -->
          <div v-if="!outlineMarkdown && !outlineLoading" class="flex-1 flex items-center justify-center">
            <div class="text-center text-slate-400">
              <el-icon class="text-5xl mb-4"><Document /></el-icon>
              <p class="text-lg">请在左侧上传教学大纲文件</p>
              <p class="text-sm">支持 PDF、Word 文档格式</p>
            </div>
          </div>

          <!-- 显示解析后的大纲内容 -->
          <div v-else class="flex-1 flex flex-col">
            <div class="flex items-center justify-between mb-3">
              <h3 class="text-lg font-medium text-slate-700">大纲预览</h3>
              <div v-if="outlineMarkdown && !outlineLoading" class="flex items-center space-x-2">
                <el-button type="primary" @click="currentStep = 1">
                  下一步: 选择模板
                  <el-icon class="ml-1"><ArrowRight /></el-icon>
                </el-button>
              </div>
            </div>

            <!-- 使用PreviewPane显示大纲内容 -->
            <PreviewPane class="flex-1" />
          </div>
        </div>

        <!-- 第二步：模板选择预览 -->
        <div v-else-if="currentStep === 1" class="flex-1 flex flex-col">
          <div class="flex items-center justify-between mb-3">
            <h3 class="text-lg font-medium text-slate-700">模板预览</h3>
          </div>

          <!-- 显示选中模板的预览 -->
          <div v-if="selectedTemplate" class="flex-1 bg-white rounded-lg shadow-sm p-6">
            <TemplatePreview :template="selectedTemplate" :outline="outlineMarkdown" />
          </div>
          <div v-else class="flex-1 flex items-center justify-center">
            <div class="text-center text-slate-400">
              <el-icon class="text-5xl mb-4"><Document /></el-icon>
              <p class="text-lg">请在左侧选择教案模板</p>
              <p class="text-sm">选择适合的模板风格</p>
            </div>
          </div>
        </div>

        <!-- 第三步：教案生成和预览 -->
        <div v-else-if="currentStep === 2" class="flex-1 flex flex-col">
          <div class="flex items-center justify-between mb-3">
            <h3 class="text-lg font-medium text-slate-700">
              {{ lessonContent ? '教案预览' : '准备生成教案' }}
            </h3>
          </div>

          <!-- 显示生成的教案内容 -->
          <div v-if="lessonContent" class="flex-1">
            <PreviewPane class="flex-1" />
          </div>
          <div v-else class="flex-1 flex items-center justify-center">
            <div class="text-center text-slate-400">
              <el-icon class="text-5xl mb-4"><Reading /></el-icon>
              <p class="text-lg">点击左侧"生成教案"按钮开始生成</p>
              <p class="text-sm">基于您的大纲和选择的模板生成教案</p>
            </div>
          </div>
        </div>

        <!-- 未选择大纲时的初始状态 -->
        <div v-else class="flex-1 flex items-center justify-center bg-white rounded-xl shadow-sm">
          <div class="text-center p-8">
            <div class="text-6xl text-indigo-100 mb-4 flex justify-center">
              <el-icon><Files /></el-icon>
            </div>
            <h3 class="text-xl text-slate-700 mb-2">开始您的智能备课</h3>
            <p class="text-slate-500 mb-6">请从左侧上传或选择教学大纲</p>

            <el-button type="primary">
              <el-icon class="mr-1"><UploadFilled /></el-icon>
              上传大纲
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 教案预览抽屉 -->
    <el-drawer
        v-model="showPreviewDrawer"
        :with-header="false"
        size="40%"
        direction="rtl"
        @opened="() => { currentStep = 2 }"
        @closed="() => { currentStep = 1 }"
    >
      <div class="p-6 h-full overflow-y-auto prose">
        <h2 class="mb-4">教案预览</h2>
        <div v-if="!lessonContent" class="text-slate-500 text-center flex items-center justify-center h-full">
          <el-icon class="mr-1"><Document /></el-icon>暂无内容，请先生成教案。
        </div>
        <div v-else v-dompurify-html="lessonContent"></div>
      </div>
    </el-drawer>

    <!-- 教案编辑弹窗 -->
    <LessonPlanEditor
        v-model:visible="showEditor"
        v-model="editorContent"
        @save="handleSaveLesson"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Document, ArrowRight, ArrowLeft, Download, Reading } from '@element-plus/icons-vue';
import { useAuthStore } from '@/stores/counter.js';
import { bus } from '@/utils/eventBus.js';
import { post } from '@/net/index.js'; // 导入网络请求方法
import { lessonPlanAPI } from '@/api/lessonPlan.js'; // 导入教案API
import { aiAPI } from '@/utils/apiUtils.js'; // 导入AI API工具

// 导入组件
// import ModelStatusToast from './workbench/ModelStatusToast.vue'; // 已移除，对用户价值不大
import StepsProgress from './workbench/StepsProgress.vue';
import OutlineUploader from './workbench/OutlineUploader.vue';
import GenerationControl from './workbench/GenerationControl.vue';
import TemplateSelector from './workbench/TemplateSelector.vue';
import TemplatePreview from './workbench/TemplatePreview.vue';
import PreviewPane from '@/components/lesson-prep/PreviewPane.vue';

// 导入文档处理服务
import DocumentProcessor from './workbench/services/DocumentProcessor';

// 获取用户信息
const user = useAuthStore().user;
const userId = computed(() => user ? (user.id || user.userId) : null);

// ======= 基本状态管理 =======
const currentStep = ref(0); // 0: 上传大纲, 1: 选择模板, 2: 生成教案
const scrollProgress = ref(0);
const showPreviewDrawer = ref(false);
const showEditor = ref(false);
const editorContent = ref('');
const lessonContent = ref('');

// ======= 大纲相关状态 =======
const outline = ref([]);
const outlineMeta = ref({});
const outlineLoading = ref(false);
const outlineMarkdown = ref('');

// ======= 生成相关状态 =======
const isGenerating = ref(false);
const generationProgress = ref(0);
const templateContent = ref('');
const outlineContent = ref('');
const conversationId = ref(''); // 用于保存会话ID

// ======= 文件处理 =======
const selectedFile = ref(null);

// ======= 模板相关状态 =======
const selectedTemplate = ref(null);
const availableTemplates = ref([
  {
    id: 'interactive',
    name: '互动式教学模板',
    description: '注重师生互动，包含讨论环节和实践活动',
    style: 'interactive'
  },
  {
    id: 'inquiry',
    name: '探究式学习模板',
    description: '引导学生主动探索，培养探究能力',
    style: 'inquiry'
  },
  {
    id: 'project',
    name: '项目式学习模板',
    description: '基于项目的实践性学习，培养综合能力',
    style: 'project'
  },
  {
    id: 'traditional',
    name: '传统讲授模板',
    description: '经典的讲授式教学，适合知识传授',
    style: 'traditional'
  }
]);

// 监听模板和大纲内容更新
bus.on('template-update', (t) => {
  templateContent.value = t;
  console.log('模板内容更新:', t?.substring(0, 100) + '...');
});
bus.on('outline-update', (o) => {
  outlineContent.value = o;
  console.log('大纲内容更新:', o?.substring(0, 100) + '...');
});

// 监听流式内容更新
bus.on('md-ready', (md) => { lessonContent.value = md || ''; });
bus.on('md-chunk', (chunk) => { lessonContent.value += chunk || ''; });

// 打开预览
const openPreview = () => {
  showPreviewDrawer.value = true;
};

// 打开编辑器
function openEditor() {
  editorContent.value = lessonContent.value;
  showEditor.value = true;
}

// 处理文件选择
const handleFileSelected = (file) => {
  if (!file) {
    selectedFile.value = null;
    return;
  }

  selectedFile.value = file;
};

// 处理文件上传完成
const handleUploadComplete = async (result) => {
  if (result.status !== 'success' || !selectedFile.value) {
    return;
  }

  outlineLoading.value = true;

  try {
    // 创建FormData对象
    const formData = new FormData();
    formData.append('message', '请优化以下教学大纲'); // 默认消息
    formData.append('files', selectedFile.value); // 添加文件

    // 使用用户ID作为会话ID
    if (userId.value) {
      formData.append('conversationId', userId.value.toString());
      conversationId.value = userId.value.toString();
    }

    let receivedText = '';
    let isFirstChunk = true;

    // 使用新的AI API工具处理流式响应
    await aiAPI.optimizeOutline(
        formData,
        // onChunk - 处理每个数据块
        (data) => {
          // 确保正确提取内容
          const content = data.content || data || '';
          if (content) {
            // 收到第一个字符时显示解析成功
            if (isFirstChunk) {
              ElMessage.success('解析成功，正在优化大纲...');
              isFirstChunk = false;
              // 通知右侧开始显示内容
              bus.emit('outline-chunk', content);
            } else {
              // 后续内容继续流式显示
              bus.emit('outline-chunk', content);
            }
            receivedText += content;
            // 更新进度
            generationProgress.value = Math.min(95, generationProgress.value + 5);
          }
        },
        // onComplete - 处理完成
        () => {
          console.log('大纲优化完成');
          // 通知流式显示完成
          bus.emit('outline-complete');
        },
        // onError - 处理错误
        (error) => {
          throw error;
        }
    );

    // 解析返回的数据
    try {
      // 尝试解析JSON响应
      const data = JSON.parse(receivedText);

      // 处理大纲数据
      outline.value = data.outline || [];
      outlineMeta.value = data.meta || {
        fileName: selectedFile.value.name,
        fileSize: selectedFile.value.size
      };

      // 保存会话ID，用于后续请求
      if (data.conversationId) {
        conversationId.value = data.conversationId;
      }

      // 转换为Markdown
      outlineMarkdown.value = DocumentProcessor.outlineToMarkdown(outline.value);

      // 通知大纲内容更新
      bus.emit('outline-update', outlineMarkdown.value);

      ElMessage.success('大纲优化成功');
    } catch (parseError) {
      // 如果不是JSON，则直接使用文本内容
      console.log('收到非JSON响应:', receivedText);

      // 简单处理文本内容作为大纲
      outlineMarkdown.value = receivedText;
      bus.emit('outline-update', outlineMarkdown.value);

      ElMessage.success('大纲优化成功');
    }

    // 确保大纲内容已更新到所有相关状态
    outlineContent.value = receivedText;
    outlineMarkdown.value = receivedText;

    // 通知大纲内容更新
    bus.emit('outline-update', receivedText);

    // 上传成功后自动跳转到模板选择
    setTimeout(() => {
      currentStep.value = 1; // 切换到模板选择步骤
      ElMessage.success('大纲解析完成，请选择教案模板');
      console.log('步骤切换完成，大纲内容:', outlineContent.value);
    }, 1000);
  } catch (error) {
    console.error('处理大纲失败:', error);
    ElMessage.error('处理大纲失败，请重试');
    outlineLoading.value = false;
  } finally {
    setTimeout(() => {
      outlineLoading.value = false;
    }, 1000);
  }
};

// 处理上传进度
const handleUploadProgress = (progress) => {
  // 可以在这里处理上传进度
};

// 处理模板选择
const handleTemplateSelected = (template) => {
  selectedTemplate.value = template;
  console.log('选择模板:', template);
  ElMessage.success(`已选择模板：${template.name}`);
};

// 保存教案
const handleSaveLessonPlan = () => {
  if (!lessonContent.value) {
    ElMessage.warning('没有教案内容可保存');
    return;
  }

  try {
    // 创建下载链接
    const blob = new Blob([lessonContent.value], { type: 'text/html;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `教案_${selectedTemplate.value?.name || '默认'}_${new Date().toLocaleDateString()}.html`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);

    ElMessage.success('教案保存成功');
  } catch (error) {
    console.error('保存教案失败:', error);
    ElMessage.error('保存教案失败');
  }
};

// 处理生成教案
const handleGenerateLessonPlan = async (params) => {
  if (isGenerating.value) return;
  if (!outlineMarkdown.value && !outlineContent.value) {
    ElMessage.warning('请先导入教学大纲');
    return;
  }

  isGenerating.value = true;
  generationProgress.value = 0;
  lessonContent.value = '';

  // 通知预览组件加载状态
  bus.emit('preview-loading', true);

  try {
    // 准备请求数据
    const formData = new FormData();
    const outline = outlineContent.value || outlineMarkdown.value;

    console.log('生成教案 - 大纲内容:', outline?.substring(0, 100) + '...');
    console.log('生成教案 - 教学风格:', params?.styles);

    if (!outline) {
      ElMessage.warning('大纲内容为空，请先解析大纲');
      return;
    }

    // 构建消息内容，包含模板信息
    if (!selectedTemplate.value) {
      ElMessage.warning('请先选择教案模板');
      return;
    }

    const templateName = selectedTemplate.value.name;
    const templateStyle = selectedTemplate.value.style;
    const message = `请根据以下教学大纲，使用"${templateName}"风格生成教案：\n\n${outline}\n\n要求：\n1. 采用${templateStyle}教学方式\n2. 结构清晰，内容详实\n3. 包含教学目标、重难点、教学过程等完整内容`;

    formData.append('message', message);

    // 使用用户ID作为会话ID
    const convId = userId.value ? userId.value.toString() : conversationId.value;

    lessonContent.value = '';

    // 模拟进度更新
    const progressInterval = setInterval(() => {
      if (isGenerating.value) {
        generationProgress.value += Math.floor(Math.random() * 5) + 1;
        if (generationProgress.value >= 95) {
          clearInterval(progressInterval);
          generationProgress.value = 95; // 保持在95%，等待实际完成
        }
      } else {
        clearInterval(progressInterval);
      }
    }, 200);

    let isFirstChunk = true;

    // 使用新的AI API工具处理流式响应
    await aiAPI.createLesson(
        formData,
        convId,
        // onChunk - 处理每个数据块
        (data) => {
          // 确保正确提取内容
          const content = data.content || data || '';
          if (content) {
            // 收到第一个字符时显示生成成功
            if (isFirstChunk) {
              ElMessage.success('开始生成教案...');
              isFirstChunk = false;
              // 通知右侧开始显示教案内容
              bus.emit('lesson-chunk', content);
            } else {
              // 后续内容继续流式显示
              bus.emit('lesson-chunk', content);
            }
            lessonContent.value += content;
          }
        },
        // onComplete - 处理完成
        () => {
          clearInterval(progressInterval);
          generationProgress.value = 100;
          console.log('教案生成完成');
          // 通知流式显示完成
          bus.emit('lesson-complete');
          ElMessage.success('教案生成成功');
        },
        // onError - 处理错误
        (error) => {
          clearInterval(progressInterval);
          throw error;
        }
    );
  } catch (error) {
    console.error('生成教案失败:', error);
    ElMessage.error('生成教案失败，请重试');
  } finally {
    setTimeout(() => {
      isGenerating.value = false;
      bus.emit('preview-loading', false);
    }, 1000);
  }
};

// 保存教案
async function handleSaveLesson(content) {
  try {
    // 构建教案数据
    const lessonPlan = {
      title: extractTitleFromContent(content) || '未命名教案',
      subject: '', // 可以从界面获取
      grade: '',   // 可以从界面获取
      content: content,
      markdownContent: content, // 如果是markdown格式
      outlineContent: outlineMarkdown.value || '',
      templateType: 'standard',
      status: 'draft',
      duration: 45,
      isAiGenerated: true,
      tags: []
    };

    // 保存到服务器
    const { data } = await lessonPlanAPI.saveLessonPlan(lessonPlan, userId.value.toString());
    ElMessage.success('教案保存成功！');

    // 同时保存到本地文件作为备份
    const blob = new Blob([content], { type: 'text/html;charset=utf-8' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `lesson-plan-${Date.now()}.html`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    // 触发事件通知其他组件
    bus.emit('lesson-plan-saved', { id: data, ...lessonPlan });
  } catch (error) {
    ElMessage.error('保存教案失败: ' + error.message);

    // 如果服务器保存失败，至少保存到本地
    const blob = new Blob([content], { type: 'text/html;charset=utf-8' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `lesson-plan-${Date.now()}.html`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    ElMessage.info('已保存到本地文件');
  }
}

// 从内容中提取标题
function extractTitleFromContent(content) {
  if (!content) return null;

  // 尝试从HTML中提取h1标签
  const h1Match = content.match(/<h1[^>]*>(.*?)<\/h1>/i);
  if (h1Match) {
    return h1Match[1].replace(/<[^>]*>/g, '').trim();
  }

  // 尝试从Markdown中提取标题
  const mdMatch = content.match(/^#\s+(.+)$/m);
  if (mdMatch) {
    return mdMatch[1].trim();
  }

  // 返回内容的前50个字符作为标题
  const textContent = content.replace(/<[^>]*>/g, '').trim();
  return textContent.substring(0, 50) + (textContent.length > 50 ? '...' : '');
}

// 删除重复的方法定义

// ======= 生命周期钩子 =======
onMounted(() => {
  // 添加滚动监听
  const handleScroll = () => {
    if (!document.querySelector('.markdown-body')) return;

    const scrollElement = document.querySelector('.markdown-body');
    const scrollTop = scrollElement.scrollTop;
    const scrollHeight = scrollElement.scrollHeight;
    const clientHeight = scrollElement.clientHeight;

    if (scrollHeight <= clientHeight) {
      scrollProgress.value = 100;
    } else {
      scrollProgress.value = Math.round((scrollTop / (scrollHeight - clientHeight)) * 100);
    }
  };

  document.addEventListener('scroll', handleScroll, true);

  onUnmounted(() => {
    document.removeEventListener('scroll', handleScroll, true);
  });
});

// 监听bus事件，实现跨组件联动
bus.on('node-click', (_data) => {
  // 处理大纲节点点击
  if (currentStep.value === 0) {
    // 可能在这里实现大纲节点点击的操作
  }
});

// 清理事件监听
onUnmounted(() => {
  bus.off('node-click');
});
</script>

<style scoped>
/* 使用自定义滚动条 */
:deep(::-webkit-scrollbar) {
  width: 6px;
  height: 6px;
}

:deep(::-webkit-scrollbar-thumb) {
  background-color: #a5b4fc;
  border-radius: 4px;
}

:deep(::-webkit-scrollbar-track) {
  background-color: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
}

/* 添加渐变过渡效果 */
.lesson-prep-workbench {
  background: linear-gradient(to bottom right, #f1f5f9, #f8fafc);
}
</style>