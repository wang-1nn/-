<script setup>
import { ref } from 'vue'
import BaseTable from '@/components/BaseTable.vue'
import LucideIcon from '@/components/icons/LucideIcon.vue'

const loading=ref(true)
const scores=ref([])
setTimeout(()=>{
 scores.value=[{id:1,course:'数学',score:95,rank:2,date:'2025-05-18'}]
 loading.value=false
},600)
const columns=[
 {key:'id',label:'ID'},
 {key:'course',label:'课程'},
 {key:'score',label:'成绩'},
 {key:'rank',label:'班级排名'},
 {key:'date',label:'日期'}
]
</script>
<template>
<div class="page-container">
  <h1 class="text-2xl font-semibold mb-6 flex items-center"><LucideIcon name="bar-chart" size="24" class="mr-2"/>我的成绩</h1>
  <BaseTable :columns="columns" :rows="scores" :loading="loading"/>
</div>
</template>
<style scoped></style> 