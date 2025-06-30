<template>
  <div class="flex flex-col h-full p-4 bg-white border-r border-slate-200 dark:bg-slate-800 dark:border-slate-700">
    <h3 class="mb-4 text-lg font-semibold text-slate-800 dark:text-slate-200">教学大纲</h3>

    <!-- File Uploader -->
    <div class="mb-4">
      <el-upload
        drag
        action="#"
        :http-request="() => {}"
        :before-upload="handleBeforeUpload"
        :show-file-list="false"
        class="w-full"
      >
        <div class="p-4 text-center">
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            拖拽文件到此处或 <em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持 PDF, DOCX 文件
            </div>
          </template>
        </div>
      </el-upload>
    </div>

    <!-- Loading/Parsing State -->
    <div v-if="isParsing" class="flex flex-col items-center justify-center p-4 mb-4 rounded-md bg-slate-100 dark:bg-slate-700">
        <el-progress type="circle" :percentage="parsingProgress" class="mb-2" />
        <p class="text-sm text-slate-600 dark:text-slate-300">正在解析大纲...</p>
    </div>


    <!-- Outline Tree -->
    <div class="flex-1 overflow-y-auto">
        <el-tree
            v-if="!isParsing && outlineData.length"
            :data="outlineData"
            :props="defaultProps"
            @node-click="handleNodeClick"
            default-expand-all
            highlight-current
            class="bg-transparent"
        />
        <div v-else-if="!isParsing && !outlineData.length" class="flex items-center justify-center h-full">
            <p class="text-slate-500">上传文件后，此处将显示大纲结构。</p>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { ElUpload, ElIcon, ElTree, ElProgress } from 'element-plus';
import { UploadFilled } from '@element-plus/icons-vue';

const emit = defineEmits(['parsed-outline']);

const isParsing = ref(false);
const parsingProgress = ref(0);
const outlineData = ref([]);
const defaultProps = {
  children: 'children',
  label: 'label',
};

const handleBeforeUpload = (file) => {
  console.log('File selected:', file.name);
  isParsing.value = true;
  parsingProgress.value = 0;
  
  // Mock parsing process
  const interval = setInterval(() => {
    parsingProgress.value += 10;
    if (parsingProgress.value >= 100) {
      clearInterval(interval);
      isParsing.value = false;
      // Mock data after "parsing"
      outlineData.value = [
        {
          id: 1,
          label: '第一章：课程介绍',
          children: [
            { id: 2, label: '1.1 什么是人工智能' },
            { id: 3, label: '1.2 发展历史' },
          ],
        },
        {
          id: 4,
          label: '第二章：搜索算法',
          children: [
            { id: 5, label: '2.1 深度优先搜索' },
            { id: 6, label: '2.2 广度优先搜索' },
          ],
        },
      ];
    }
  }, 200);

  return false; // Prevent actual upload
};

const handleNodeClick = (data) => {
  // We only emit for leaf nodes in this example
  if (!data.children || data.children.length === 0) {
    emit('parsed-outline', data);
  }
};
</script>

<style>
/* Customizing element-plus components */
.el-tree {
    --el-tree-node-hover-bg-color: #f0f4ff; /* A light blue */
}
.dark .el-tree {
    --el-tree-text-color: #cbd5e1; /* slate-300 */
    --el-tree-node-hover-bg-color: #334155; /* slate-700 */
}
.dark .el-tree-node__content:hover {
    background-color: var(--el-tree-node-hover-bg-color);
}
.dark .el-tree-node.is-current > .el-tree-node__content {
    background-color: #4f46e5; /* indigo-600 */
}
.el-upload-dragger {
    width: 100% !important;
}
</style> 