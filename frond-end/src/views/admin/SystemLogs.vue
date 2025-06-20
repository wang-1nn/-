<script setup>
import { ref } from 'vue'
import LucideIcon from '@/components/icons/LucideIcon.vue'
import BaseTable from '@/components/BaseTable.vue'

const loading = ref(true)
const logs = ref([])
const keyword = ref('')
setTimeout(()=>{
 logs.value=[{id:1,user:'admin',action:'删除课程',ip:'127.0.0.1',time:'2025-05-18 10:00',status:1}]
 loading.value=false
},800)

const columns=[
 {key:'id',label:'ID'},
 {key:'user',label:'用户'},
 {key:'action',label:'操作'},
 {key:'ip',label:'IP'},
 {key:'time',label:'时间'},
 {key:'status',label:'状态'}
]
</script>
<template>
  <div class="page-container">
    <h1 class="text-2xl font-semibold mb-6 flex items-center"><LucideIcon name="file-text" size="24" class="mr-2 text-gray-600"/> 系统日志</h1>
    <div class="flex items-center mb-4 space-x-2">
      <input v-model="keyword" placeholder="关键词搜索" class="form-input w-56"/>
      <button class="btn-outline text-xs"><LucideIcon name="download" size="14" class="mr-1"/>导出</button>
    </div>
    <BaseTable :columns="columns" :rows="logs.filter(l=>l.action.includes(keyword))" :loading="loading" />
  </div>
</template>
<style scoped></style> 