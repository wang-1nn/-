<script setup>
import { ref } from 'vue'
import BaseTable from '@/components/BaseTable.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import LucideIcon from '@/components/icons/LucideIcon.vue'

const loading=ref(true)
const dialog=ref(null)
const classes=ref([])
setTimeout(()=>{
 classes.value=[{id:1,name:'高一(1)班',students:45,created:'2025-05-18'}]
 loading.value=false
},600)
const columns=[
 {key:'id',label:'ID'},
 {key:'name',label:'班级名称'},
 {key:'students',label:'学生数'},
 {key:'created',label:'创建时间'},
 {key:'action',label:'操作'}
]
const remove=(row)=>{
 dialog.value.open('确认删除该班级?',()=>{
   classes.value=classes.value.filter(c=>c.id!==row.id)
 })
}
</script>
<template>
<div class="page-container">
  <h1 class="text-2xl font-semibold mb-6 flex items-center"><LucideIcon name="users" size="24" class="mr-2"/>班级管理</h1>
  <BaseTable :columns="columns" :rows="classes" :loading="loading">
    <template #cell-action="{row}">
      <button class="text-red-500 text-xs" @click="remove(row)">删除</button>
    </template>
  </BaseTable>
  <ConfirmDialog ref="dialog" />
</div>
</template>
<style scoped></style> 