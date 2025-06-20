<script setup>
import { ref } from 'vue'
import BaseTable from '@/components/BaseTable.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import LucideIcon from '@/components/icons/LucideIcon.vue'

const loading=ref(true)
const dialog=ref(null)
const students=ref([])
setTimeout(()=>{
 students.value=[{id:1,name:'张三',class:'高一(1)班',score:88}]
 loading.value=false
},600)
const columns=[
 {key:'id',label:'ID'},
 {key:'name',label:'姓名'},
 {key:'class',label:'班级'},
 {key:'score',label:'平均分'},
 {key:'action',label:'操作'}
]
const remove=(row)=>{
 dialog.value.open('确认移除该学生?',()=>{
   students.value=students.value.filter(s=>s.id!==row.id)
 })
}
</script>
<template>
<div class="page-container">
  <h1 class="text-2xl font-semibold mb-6 flex items-center"><LucideIcon name="user" size="24" class="mr-2"/>学生管理</h1>
  <BaseTable :columns="columns" :rows="students" :loading="loading">
    <template #cell-action="{row}">
      <button class="text-red-500 text-xs" @click="remove(row)">移除</button>
    </template>
  </BaseTable>
  <ConfirmDialog ref="dialog" />
</div>
</template>
<style scoped></style> 