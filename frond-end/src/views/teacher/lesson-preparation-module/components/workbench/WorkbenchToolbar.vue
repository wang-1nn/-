<template>
  <div class="flex items-center gap-4 w-full justify-between">
    <div class="flex items-center gap-3">
      <!-- 上传按钮 -->
      <div class="flex items-center gap-3">
        <el-button 
          class="upload-btn"
          size="small" 
          @click="fileInput.click()"
        >
          <el-icon class="mr-1"><UploadFilled /></el-icon>导入大纲
        </el-button>
        <input ref="fileInput" type="file" class="hidden" accept=".pdf,.doc,.docx" @change="onFileChange" />
        
        <!-- 大纲模板选择 -->
        <el-select 
          v-if="showTemplateSelector"
          v-model="selectedOutlineTemplate" 
          size="small" 
          placeholder="选择大纲模板"
          class="outline-selector"
          @change="handleOutlineTemplateChange"
        >
          <el-option 
            v-for="template in outlineTemplates" 
            :key="template.id" 
            :label="template.name" 
            :value="template.id" 
          />
        </el-select>
      </div>
    </div>

    <div class="flex items-center gap-3">
      <!-- 生成教案按钮 -->
      <el-button 
        v-motion="btnMotion"
        type="primary" 
        size="small"
        :loading="isGenerating"
        @click="handleGenerate" 
      >
        <el-icon class="mr-1"><MagicStick /></el-icon>生成教案
      </el-button>
      
      <!-- 编辑教案按钮 -->
      <el-button 
        size="small" 
        :disabled="!hasContent" 
        @click="handleOpenEditor"
      >
        <el-icon class="mr-1"><DocumentCopy /></el-icon>编辑教案
      </el-button>
      
      <!-- 保存按钮 -->
      <el-button 
        size="small" 
        type="success"
        :disabled="!hasContent" 
        @click="handleSave"
      >
        <el-icon class="mr-1"><Download /></el-icon>保存
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, defineEmits, defineProps } from 'vue';
import { UploadFilled, MagicStick, DocumentCopy, Download } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

const props = defineProps({
  hasContent: {
    type: Boolean,
    default: false
  },
  showTemplateSelector: {
    type: Boolean,
    default: true
  }
});

const emit = defineEmits([
  'file-change', 
  'generate', 
  'open-editor',
  'save',
  'template-change'
]);

// 文件选择
const fileInput = ref(null);
const isGenerating = ref(false);

// 模板相关
const selectedOutlineTemplate = ref('');
const outlineTemplates = ref([
  { id: 'basic', name: '基础教学大纲' },
  { id: 'detailed', name: '详细教学大纲' },
  { id: 'higher-ed', name: '高等教育大纲' },
  { id: 'stem', name: 'STEM课程大纲' },
  { id: 'humanities', name: '人文学科大纲' }
]);

// 按钮动画配置
const btnMotion = {
  initial: { scale: 1 },
  hover: { scale: 1.05 },
  tap: { scale: 0.95 },
};

// 处理文件变更
const onFileChange = (event) => {
  const file = event.target.files[0];
  if (!file) return;
  
  // 检查文件类型
  const validTypes = ['.pdf', '.doc', '.docx'];
  const fileExt = file.name.substring(file.name.lastIndexOf('.')).toLowerCase();
  
  if (!validTypes.includes(fileExt)) {
    ElMessage.error('请上传PDF或Word文档');
    return;
  }
  
  // 文件大小限制
  if (file.size > 10 * 1024 * 1024) { // 10MB
    ElMessage.error('文件不能超过10MB');
    return;
  }
  
  emit('file-change', file);
};

// 处理教案生成
const handleGenerate = async () => {
  isGenerating.value = true;
  try {
    await emit('generate');
  } finally {
    isGenerating.value = false;
  }
};

// 打开编辑器
const handleOpenEditor = () => {
  emit('open-editor');
};

// 处理保存
const handleSave = () => {
  emit('save');
};

// 处理大纲模板变更
const handleOutlineTemplateChange = () => {
  emit('template-change', selectedOutlineTemplate.value);
};
</script>

<style scoped>
.upload-btn, .outline-selector {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.upload-btn:hover, .outline-selector:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.upload-btn:active {
  transform: translateY(1px);
}
</style>