<script setup>
import { ref } from 'vue'
import LucideIcon from '@/components/icons/LucideIcon.vue'

const tasks=ref([
  {id:1,title:'链表反转编程题',status:'pending',due:'今天 21:00'},
  {id:2,title:'操作系统进程同步选择题',status:'done',due:'已完成'}
])

const toggle=(task)=>{
  if(task.status==='pending'){
    task.status='done'
    task.due='已完成'
  }
}
</script>
<template>
  <div class="page-container">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-semibold text-gray-800 flex items-center"><LucideIcon name="edit" size="24" class="mr-2 text-amber-600"/>练习与纠错</h1>
    </div>

    <div class="card space-y-3">
      <div v-for="t in tasks" :key="t.id" @click="toggle(t)" class="task-row flex justify-between items-center p-3 rounded-md hover:bg-gray-50 cursor-pointer select-none">
        <div class="flex items-center">
          <LucideIcon :name="t.status==='done'?'check-circle':'circle'" :class="t.status==='done'?'text-green-600':'text-amber-500'" size="16" class="mr-2 transition-transform duration-300" :style="t.status==='done'? 'transform:scale(1.2)':'transform:scale(1)'"/>
          <span :class="['transition-colors', {'line-through text-gray-400':t.status==='done'}]">{{t.title}}</span>
        </div>
        <span class="text-xs text-gray-500">{{t.due}}</span>
      </div>
    </div>
  </div>
</template>
<style scoped>
.task-row{transition:background-color .3s ease;}
.task-row.done{background-color:#f0fdf4;}

.fade-enter-active,.fade-leave-active{transition:opacity .2s ease;}
.fade-enter-from,.fade-leave-to{opacity:0;}
</style> 