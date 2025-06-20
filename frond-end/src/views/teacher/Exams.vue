<script setup>
import { ref } from 'vue'
import LucideIcon from '@/components/icons/LucideIcon.vue'
import { get, post } from '@/net'

const loading = ref(true)
const generating = ref(false)
const exams = ref([])

const fetchExams = () => {
  loading.value = true
  // TODO: 调用后端接口获取考核
  setTimeout(() => {
    exams.value = [
      { id: 'ex1', title: '链表与树 章节测验', type: '测验', createdAt: '2025-05-10', total: 100, ai: true },
      { id: 'ex2', title: '期中编程大作业', type: '作业', createdAt: '2025-04-01', total: 100, ai: false }
    ]
    loading.value = false
  }, 800)
}

const generateExam = () => {
  generating.value = true
  setTimeout(() => {
    exams.value.unshift({ id: Date.now(), title: '自动生成测验', type: '测验', createdAt: new Date().toISOString().slice(0,10), total: 100, ai: true })
    generating.value = false
  }, 1500)
}

fetchExams()
</script>

<template>
  <div class="page-container">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-semibold text-gray-800 flex items-center"><LucideIcon name="layers" size="24" class="mr-2 text-purple-600"/>考核设计</h1>
      <button class="btn-primary" @click="generateExam">
        <LucideIcon name="sparkles" size="18" class="mr-1"/>
        AI 生成考核
        <LucideIcon v-if="generating" name="loader-2" size="16" class="animate-spin ml-1"/>
      </button>
    </div>

    <div class="card">
      <div v-if="loading" class="py-10 flex justify-center text-gray-500">
        <LucideIcon name="loader-2" size="24" class="animate-spin mr-2"/>加载中...
      </div>

      <div v-else>
        <table class="min-w-full text-sm">
          <thead class="bg-gray-50 text-gray-600 uppercase text-xs">
            <tr>
              <th class="px-6 py-3 text-left">标题</th>
              <th class="px-6 py-3 text-left">类型</th>
              <th class="px-6 py-3 text-left">创建时间</th>
              <th class="px-6 py-3 text-left">总分</th>
              <th class="px-6 py-3 text-left">来源</th>
              <th class="px-6 py-3">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="ex in exams" :key="ex.id" class="border-b hover:bg-gray-50">
              <td class="px-6 py-4">{{ ex.title }}</td>
              <td class="px-6 py-4">{{ ex.type }}</td>
              <td class="px-6 py-4">{{ ex.createdAt }}</td>
              <td class="px-6 py-4">{{ ex.total }}</td>
              <td class="px-6 py-4">
                <span v-if="ex.ai" class="badge-warning">AI</span>
                <span v-else class="badge-secondary">手动</span>
              </td>
              <td class="px-6 py-4 space-x-2 text-right">
                <button class="btn-outline text-xs">查看</button>
                <button class="btn-outline text-xs">编辑</button>
                <button class="btn-outline text-xs text-red-600">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.badge-warning { @apply inline-flex items-center px-2 py-0.5 text-xs rounded-full bg-amber-100 text-amber-800; }
.badge-secondary { @apply inline-flex items-center px-2 py-0.5 text-xs rounded-full bg-gray-100 text-gray-800; }
</style> 