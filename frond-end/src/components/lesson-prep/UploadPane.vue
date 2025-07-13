<template>
  <div class="flex flex-col gap-4 p-6 bg-white/90 dark:bg-slate-800/90 h-full rounded-2xl shadow-lg border border-indigo-200 dark:border-indigo-700 backdrop-blur">
    <!-- 上传区域 -->
    <el-upload
      drag
      :show-file-list="false"
      action="#"
      :http-request="() => {}"
      :before-upload="beforeUpload"
      class="w-full flex-1 upload-area rounded-xl border-2 border-dashed border-indigo-300 dark:border-indigo-600 flex items-center justify-center"
    >
      <div class="text-center">
        <el-icon class="el-icon--upload text-indigo-500 text-4xl"><UploadFilled /></el-icon>
        <p class="mt-2">拖拽文件到此处或 <span class="text-indigo-500">点击上传</span></p>
        <p class="text-xs text-slate-400 mt-1">支持 PDF / DOCX</p>
      </div>
    </el-upload>

    <el-progress v-if="isParsing" :percentage="progress" :show-text="false" />
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { UploadFilled } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { bus } from '@/utils/eventBus';

const isParsing = ref(false);
const progress   = ref(0);

function beforeUpload(file) {
  isParsing.value = true;

  const formData = new FormData();
  formData.append('message', '请解析并优化下列教学大纲');
  formData.append('conversationId', 'tmp');
  formData.append('files', file);

  fetch('/api/teacher/Ai/OptimizateOutline', {
    method: 'POST',
    body: formData,
  })
    .then(r => r.text())
    .then(md => {
      bus.emit('md-ready', md);
      isParsing.value = false;
    })
    .catch(err => {
      console.error(err);
      ElMessage.error('上传失败');
      isParsing.value = false;
    });

  return false; // 阻止 el-upload 默认上传
}
</script>

<style scoped>
.upload-area {
  transition: border-color 0.3s;
}
.upload-area:hover {
  border-color: #4f46e5;
}
</style> 