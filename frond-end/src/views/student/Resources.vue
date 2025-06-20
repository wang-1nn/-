<script setup>
import { ref } from 'vue'
import LucideIcon from '@/components/icons/LucideIcon.vue'

const loading=ref(true)
const list=ref([])
setTimeout(()=>{
 list.value=[{id:'r1',title:'链表讲义.pdf',type:'文档',course:'数据结构'},{id:'r2',title:'进程同步视频',type:'视频',course:'操作系统'}]
 loading.value=false
},800)
</script>
<template>
  <div class="page-container">
    <h1 class="text-2xl font-semibold text-gray-800 mb-6 flex items-center"><LucideIcon name="folder" size="24" class="mr-2 text-indigo-600"/> 学习资源</h1>
    <div class="card">
      <div v-if="loading" class="py-10 flex justify-center text-gray-500"><LucideIcon name="loader-2" class="animate-spin mr-2"/>加载中...</div>
      <div v-else class="space-y-3">
        <div v-for="r in list" :key="r.id" class="flex justify-between items-center p-3 rounded-md hover:bg-gray-50 transition-colors">
          <div class="flex items-center">
            <LucideIcon :name="r.type==='视频'?'video':'file-text'" size="16" class="mr-2 text-gray-500"/>
            <div>
              <div class="font-medium">{{r.title}}</div>
              <div class="text-xs text-gray-500">{{r.course}}</div>
            </div>
          </div>
          <button class="btn-outline text-xs"><LucideIcon name="download" size="14" class="mr-1"/>下载</button>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped></style> 