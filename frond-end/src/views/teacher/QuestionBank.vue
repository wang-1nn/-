<script setup>
import { ref } from 'vue'
import BaseTable from '@/components/BaseTable.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import LucideIcon from '@/components/icons/LucideIcon.vue'

const loading=ref(true)
const dialog=ref(null)
const questions=ref([])
setTimeout(()=>{
 questions.value=[{id:1,subject:'数学',difficulty:'中',content:'求导公式?',updated:'2025-05-18'}]
 loading.value=false
},600)
const columns=[
 {key:'id',label:'ID'},
 {key:'subject',label:'科目'},
 {key:'difficulty',label:'难度'},
 {key:'content',label:'题目'},
 {key:'updated',label:'更新时间'},
 {key:'action',label:'操作'}
]
const remove=(row)=>{
 dialog.value.open('确认删除该题目?',()=>{
   questions.value=questions.value.filter(q=>q.id!==row.id)
 })
}
</script>
<template>
<div class="page-container">
  <h1 class="text-2xl font-semibold mb-6 flex items-center"><LucideIcon name="book-open" size="24" class="mr-2"/>题库管理</h1>
  <BaseTable :columns="columns" :rows="questions" :loading="loading">
    <template #cell-action="{row}">
      <button class="text-red-500 text-xs mr-2" @click="remove(row)">删除</button>
    </template>
  </BaseTable>
  <ConfirmDialog ref="dialog" />
</div>
</template>
<style scoped></style> 