<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import LucideIcon from '@/components/icons/LucideIcon.vue'

const router = useRouter();
const loading=ref(true)
const exams=ref([])
setTimeout(()=>{
  exams.value=[
    {id:'e1',title:'链表章节测验',status:'ongoing',deadline:'今天 23:59'},
    {id:'e2',title:'期中考试',status:'notstart',deadline:'06-01 09:00'}
  ];
  loading.value=false
},800)
</script>
<template>
  <div class="page-container">
    <h1 class="text-2xl font-semibold text-gray-800 mb-6 flex items-center"><LucideIcon name="clipboard-list" size="24" class="mr-2 text-red-600"/> 我的考核</h1>

    <div class="card">
      <div v-if="loading" class="py-10 flex justify-center text-gray-500"><LucideIcon name="loader-2" class="animate-spin mr-2"/>加载中...</div>
      <div v-else>
        <table class="min-w-full text-sm">
          <thead class="bg-gray-50 text-gray-600 uppercase text-xs"><tr><th class="px-6 py-3 text-left">标题</th><th class="px-6 py-3 text-left">状态</th><th class="px-6 py-3 text-left">截止</th><th class="px-6 py-3">操作</th></tr></thead>
          <tbody>
            <tr v-for="ex in exams" :key="ex.id" class="border-b hover:bg-gray-50">
              <td class="px-6 py-4">{{ex.title}}</td>
              <td class="px-6 py-4">
                <span :class="ex.status==='done'?'badge-success':ex.status==='ongoing'?'badge-warning':'badge-secondary'">
                  {{ ex.status==='done'?'已完成':ex.status==='ongoing'?'进行中':'未开始' }}</span>
              </td>
              <td class="px-6 py-4">{{ex.deadline}}</td>
              <td class="px-6 py-4 text-right"><button class="btn-outline text-xs" @click="router.push(`/student/exams/${ex.id}`)">进入</button></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
<style scoped>
.badge-success{@apply inline-flex items-center px-2 py-0.5 text-xs rounded-full bg-green-100 text-green-800}
.badge-warning{@apply inline-flex items-center px-2 py-0.5 text-xs rounded-full bg-amber-100 text-amber-800}
.badge-secondary{@apply inline-flex items-center px-2 py-0.5 text-xs rounded-full bg-gray-100 text-gray-800}
</style> 