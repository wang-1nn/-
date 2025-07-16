<template>
  <div class="outline-uploader flex flex-col items-center justify-center bg-white rounded-lg border-2 border-dashed border-indigo-300 p-6">
    <div
        class="uploader-container relative w-full h-full flex flex-col items-center justify-center"
        :class="{ 'dragging': isDragging }"
        @dragenter.prevent="handleDragEnter"
        @dragover.prevent="handleDragOver"
        @dragleave.prevent="handleDragLeave"
        @drop.prevent="handleDrop"
        @click="triggerFileInput"
    >
      <!-- 上传图标 -->
      <div class="upload-icon-container flex items-center justify-center w-16 h-16 mb-4 bg-indigo-100 rounded-full">
        <el-icon class="text-3xl text-indigo-600">
          <component :is="uploadIcon" />
        </el-icon>
      </div>

      <!-- 上传说明文本 -->
      <h3 class="text-lg font-medium text-slate-700 mb-2">
        {{ fileSelected ? '文件已选择' : '上传教学大纲' }}
      </h3>

      <p v-if="!fileSelected" class="text-sm text-slate-500 mb-4 text-center">
        拖放文件到这里，或点击选择文件<br>
        支持 PDF, Word 文档格式 (PDF, DOC, DOCX)
      </p>

      <p v-else class="text-sm text-slate-700 mb-4 flex items-center">
        <component :is="fileIcon" class="mr-2" />
        {{ selectedFile.name }}
        <span class="ml-2 text-xs text-slate-500">({{ formatFileSize(selectedFile.size) }})</span>
      </p>

      <!-- 上传按钮 -->
      <div v-if="!fileSelected" class="flex items-center space-x-4">
        <el-button size="small" type="primary" @click.stop="triggerFileInput">
          选择文件
        </el-button>

        <span class="text-xs text-slate-400">最大 10MB</span>
      </div>

      <div v-else-if="!uploadCompleted" class="flex items-center space-x-3">
        <el-button size="small" type="primary" @click.stop="handleUpload" :loading="uploading">
          {{ uploading ? '解析中...' : '开始解析' }}
        </el-button>

        <el-button size="small" @click.stop="resetFileSelection">
          重选文件
        </el-button>
      </div>

      <!-- 解析完成后的状态 -->
      <div v-else class="flex items-center space-x-3">
        <div class="flex items-center text-green-600">
          <el-icon class="mr-2"><CircleCheck /></el-icon>
          <span class="text-sm font-medium">解析完成</span>
        </div>

        <el-button size="small" @click.stop="resetFileSelection">
          重新上传
        </el-button>
      </div>

      <!-- 隐藏的文件输入 -->
      <input
          ref="fileInput"
          type="file"
          accept=".pdf,.doc,.docx"
          class="hidden"
          @change="handleFileChange"
      />

      <!-- 进度条 (解析中) -->
      <div v-if="uploading" class="w-full mt-6">
        <div class="flex items-center justify-between mb-1">
          <span class="text-xs text-slate-500">解析进度</span>
          <span class="text-xs text-slate-500">{{ parseProgress }}%</span>
        </div>
        <el-progress
            :percentage="parseProgress"
            :stroke-width="6"
            color="#818cf8"
        />
        <p class="text-xs text-slate-500 mt-1">{{ parseStatus }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, defineEmits, onMounted } from 'vue';
import { UploadFilled, Document, CircleCheck } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

const emit = defineEmits(['file-selected', 'upload-complete', 'upload-error', 'upload-progress']);

// 拖拽状态
const isDragging = ref(false);

// 文件相关
const fileInput = ref(null);
const selectedFile = ref(null);
const fileSelected = computed(() => !!selectedFile.value);

// 上传状态
const uploading = ref(false);
const uploadCompleted = ref(false);
const parseProgress = ref(0);
const parseStatus = ref('正在准备解析文件...');

// 上传图标
const uploadIcon = computed(() => {
  if (uploading.value) {
    return 'Loading';
  }
  return 'UploadFilled';
});

// 文件图标
const fileIcon = computed(() => {
  if (!selectedFile.value) return 'Document';

  const extension = selectedFile.value.name.split('.').pop().toLowerCase();

  if (extension === 'pdf') return 'PDF';
  if (['doc', 'docx'].includes(extension)) return 'Document';

  return 'Files';
});

// 触发文件选择框
const triggerFileInput = () => {
  if (!uploading.value) {
    fileInput.value.click();
  }
};

// 处理文件选择
const handleFileChange = (event) => {
  const file = event.target.files[0];
  if (!file) return;

  // 验证文件类型
  const validTypes = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];
  if (!validTypes.includes(file.type)) {
    ElMessage.error('请上传PDF或Word文档文件');
    return;
  }

  // 验证文件大小 (10MB 限制)
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过10MB');
    return;
  }

  // 设置选中的文件
  selectedFile.value = file;
  emit('file-selected', file);
};

// 处理拖拽事件
const handleDragEnter = () => {
  if (!uploading.value) {
    isDragging.value = true;
  }
};

const handleDragOver = () => {
  if (!uploading.value) {
    isDragging.value = true;
  }
};

const handleDragLeave = () => {
  isDragging.value = false;
};

const handleDrop = (event) => {
  isDragging.value = false;
  if (uploading.value) return;

  const file = event.dataTransfer?.files[0];
  if (!file) return;

  // 验证文件类型
  const validExtensions = ['.pdf', '.doc', '.docx'];
  const fileName = file.name.toLowerCase();
  const isValidType = validExtensions.some(ext => fileName.endsWith(ext));

  if (!isValidType) {
    ElMessage.error('请上传PDF或Word文档文件');
    return;
  }

  // 验证文件大小
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过10MB');
    return;
  }

  // 设置选中的文件
  selectedFile.value = file;
  emit('file-selected', file);
};

// 重置文件选择
const resetFileSelection = () => {
  if (uploading.value) return;

  selectedFile.value = null;
  uploadCompleted.value = false;
  parseProgress.value = 0;
  parseStatus.value = '正在准备解析文件...';

  if (fileInput.value) {
    fileInput.value.value = '';
  }
  emit('file-selected', null);
};

// 处理文件上传
const handleUpload = async () => {
  if (uploading.value || !selectedFile.value) return;

  uploading.value = true;
  parseProgress.value = 0;
  parseStatus.value = '正在准备解析文件...';

  try {
    // 在实际项目中，这里会调用API上传并解析文件
    // 此处使用模拟进度

    // 模拟进度更新
    const interval = setInterval(() => {
      parseProgress.value += Math.floor(Math.random() * 10) + 1;

      if (parseProgress.value < 30) {
        parseStatus.value = '正在解析文件内容...';
      } else if (parseProgress.value < 60) {
        parseStatus.value = '识别教学章节结构...';
      } else if (parseProgress.value < 90) {
        parseStatus.value = '优化大纲格式...';
      } else {
        parseStatus.value = '完成解析，处理结果...';
      }

      if (parseProgress.value >= 100) {
        parseProgress.value = 100;
        parseStatus.value = '解析完成';
        clearInterval(interval);

        // 模拟API响应延迟
        setTimeout(() => {
          uploading.value = false;
          uploadCompleted.value = true;

          // 触发上传完成事件
          emit('upload-complete', {
            file: selectedFile.value,
            status: 'success'
          });

          // 成功提示
          ElMessage.success('大纲解析成功');
        }, 500);
      }

      // 触发进度事件
      emit('upload-progress', parseProgress.value);
    }, 200);
  } catch (error) {
    uploading.value = false;
    parseStatus.value = '解析失败';

    // 触发错误事件
    emit('upload-error', {
      file: selectedFile.value,
      error
    });

    // 错误提示
    ElMessage.error('文件解析失败，请重试');
  }
};

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes) return '0 B';

  const units = ['B', 'KB', 'MB', 'GB'];
  let i = 0;
  while (bytes >= 1024 && i < units.length - 1) {
    bytes /= 1024;
    i++;
  }

  return `${bytes.toFixed(2)} ${units[i]}`;
};

onMounted(() => {
  // 可以在这里添加初始化逻辑
});
</script>

<style scoped>
.outline-uploader {
  min-height: 300px;
  transition: all 0.3s ease;
}

.uploader-container {
  cursor: pointer;
  transition: all 0.3s ease;
}

.uploader-container:hover {
  background-color: rgba(99, 102, 241, 0.05);
}

.uploader-container.dragging {
  background-color: rgba(99, 102, 241, 0.1);
  border-color: #6366f1;
}

.upload-icon-container {
  transition: all 0.3s ease;
}

.upload-icon-container:hover {
  transform: translateY(-2px);
  background-color: rgba(99, 102, 241, 0.15);
}

.el-progress {
  margin: 16px 0;
}
</style>