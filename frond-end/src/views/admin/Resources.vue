<script setup>
import { ref } from 'vue'
import LucideIcon from '@/components/icons/LucideIcon.vue'

const loading = ref(true)
const resources = ref([])

setTimeout(() => {
  resources.value = [
    { id: 'r1', title: '链表讲义', type: '文档', course: '数据结构', createdAt: '2025-04-01' },
    { id: 'r2', title: '堆排序 PPT', type: 'PPT', course: '算法分析', createdAt: '2025-04-15' }
  ]
  loading.value = false
}, 900)

const exportResource = (r) => {
  alert('导出 ' + r.title)
}
</script>

<template>
  <div class="page-container">
    <h1 class="text-2xl font-semibold text-gray-800 mb-6 flex items-center"><LucideIcon name="folder" size="24" class="mr-2 text-indigo-600"/> 课件资源管理</h1>

    <div class="card">
      <div v-if="loading" class="py-10 flex justify-center text-gray-500"><LucideIcon name="loader-2" class="animate-spin mr-2"/>加载中...</div>

      <div v-else>
        <table class="min-w-full text-sm">
          <thead class="bg-gray-50 text-gray-600 uppercase text-xs">
            <tr>
              <th class="px-6 py-3 text-left">标题</th>
              <th class="px-6 py-3 text-left">类型</th>
              <th class="px-6 py-3 text-left">课程</th>
              <th class="px-6 py-3 text-left">上传时间</th>
              <th class="px-6 py-3">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in resources" :key="r.id" class="border-b hover:bg-gray-50">
              <td class="px-6 py-4">{{ r.title }}</td>
              <td class="px-6 py-4">{{ r.type }}</td>
              <td class="px-6 py-4">{{ r.course }}</td>
              <td class="px-6 py-4">{{ r.createdAt }}</td>
              <td class="px-6 py-4">
                <button class="btn-outline text-xs" @click="exportResource(r)"><LucideIcon name="download" size="14" class="mr-1"/>导出</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
</style> 