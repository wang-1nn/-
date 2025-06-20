<script setup>
import { ref } from 'vue'
import LucideIcon from '@/components/icons/LucideIcon.vue'
import { post } from '@/net'

const step = ref(1) // 1 选择课程与资料 2 生成与预览
const selectedCourse = ref('')
const fileInput = ref(null)
const uploading = ref(false)
const planGenerating = ref(false)
const planContent = ref('')

const courses = [
  { id: 'course-1', name: '数据结构' },
  { id: 'course-2', name: '算法分析' },
  { id: 'course-3', name: '操作系统' }
]

const handleFileUpload = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  uploading.value = true
  // TODO: 调用后端上传接口，此处模拟
  setTimeout(() => {
    uploading.value = false
  }, 1200)
}

const generatePlan = () => {
  if (!selectedCourse.value) return
  planGenerating.value = true
  // TODO: 调用后端AI生成接口，此处使用 mock 数据
  setTimeout(() => {
    planContent.value = `# ${courses.find(c=>c.id===selectedCourse.value).name} 教学计划\n\n## 教学目标\n- 掌握核心概念\n- 提升问题解决能力\n\n## 时间分配\n| 环节 | 时长 |\n|---|---|\n| 知识讲解 | 40min |\n| 上机实训 | 40min |\n| 课堂讨论 | 10min |\n\n## 练习与指导\n1. 栈与队列实现\n2. 链表反转\n\n## 小结与作业\n- 复习链表操作\n- 完成 PTA 习题`;
    planGenerating.value = false
    step.value = 2
  }, 1500)
}

const savePlan = () => {
  // TODO 保存到后端
  alert('已保存教学计划！')
}

</script>

<template>
  <div class="page-container">
    <h1 class="text-2xl font-semibold text-gray-800 mb-6 flex items-center">
      <LucideIcon name="file-text" size="24" class="mr-2 text-indigo-600" /> 教学计划设计
    </h1>

    <div v-if="step === 1" class="card w-full max-w-2xl mx-auto">
      <h2 class="text-lg font-medium text-gray-800 mb-4">1. 选择课程 & 上传资料</h2>
      <!-- 选择课程 -->
      <label class="form-label">课程</label>
      <select v-model="selectedCourse" class="form-select mb-4">
        <option value="" disabled>请选择课程</option>
        <option v-for="c in courses" :key="c.id" :value="c.id">{{ c.name }}</option>
      </select>

      <!-- 知识库上传 -->
      <label class="form-label">本地知识库文档 (.pdf / .docx / .txt)</label>
      <input type="file" accept=".pdf,.doc,.docx,.txt" ref="fileInput" @change="handleFileUpload" class="form-input" />
      <p v-if="uploading" class="text-sm text-gray-500 mt-2 flex items-center"><LucideIcon name="loader-2" class="animate-spin mr-1" size="16" /> 正在上传...</p>

      <button class="btn-primary mt-6" :disabled="!selectedCourse || uploading" @click="generatePlan">
        <LucideIcon name="sparkles" size="18" class="mr-1" /> 生成教学计划
      </button>
    </div>

    <div v-else-if="step === 2" class="card">
      <h2 class="text-lg font-medium text-gray-800 mb-4 flex items-center">
        <LucideIcon name="book-open" class="mr-2 text-green-600" size="20" /> 教学计划预览
      </h2>

      <div v-if="planGenerating" class="py-10 flex flex-col items-center text-gray-500">
        <LucideIcon name="loader-2" size="24" class="animate-spin mb-2" /> 正在生成教学计划...
      </div>

      <div v-else class="prose max-w-none" v-html="planContent.replace(/\n/g,'<br/>')"></div>

      <div class="flex justify-end mt-6 space-x-2">
        <button class="btn-outline" @click="step = 1">返回修改</button>
        <button class="btn-success" @click="savePlan">保存计划</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.prose {
  @apply text-sm leading-relaxed;
}
</style> 