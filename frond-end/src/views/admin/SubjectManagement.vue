<script setup>
import { ref } from 'vue'
import LucideIcon from '@/components/icons/LucideIcon.vue'
import BaseTable from '@/components/BaseTable.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'

const loading = ref(true)
const subjects = ref([])
setTimeout(()=>{
  subjects.value=[{subject_id:'SUB1',subject_name:'数据结构',description:'数据结构基础'}]
  loading.value=false
},800)

const columns=[
  {key:'subject_id',label:'ID'},
  {key:'subject_name',label:'学科名称'},
  {key:'description',label:'描述'},
  {key:'actions',label:'操作',slot:'actions'}
]
const addSubject=()=>{}
const deleteSubject=s=>{subjects.value=subjects.value.filter(i=>i.subject_id!==s.subject_id)}
</script>
<template>
 <div class="page-container">
  <h1 class="text-2xl font-semibold mb-6 flex items-center"><LucideIcon name="book" size="24" class="mr-2 text-indigo-600"/> 学科管理</h1>
  <div class="flex justify-between mb-4">
    <button class="btn-primary" @click="addSubject"><LucideIcon name="plus" size="18" class="mr-1"/>新增学科</button>
  </div>
  <BaseTable :columns="columns" :rows="subjects" :loading="loading">
    <template #actions="{row}">
      <button class="btn-outline text-xs mr-1">编辑</button>
      <ConfirmDialog title="确认删除?" @confirm="() => deleteSubject(row)">
        <template #activator="{open}">
          <button class="btn-outline text-xs text-red-600" @click="open">删除</button>
        </template>
      </ConfirmDialog>
    </template>
  </BaseTable>
 </div>
</template>
<style scoped></style> 