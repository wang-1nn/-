<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import BaseTable from '@/components/BaseTable.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import LucideIcon from '@/components/icons/LucideIcon.vue'

const router = useRouter()
const loading = ref(true)
const dialog = ref(null)
const tab = ref('mistakes') // mistakes | reports

const mistakes = ref([])
const reports = ref([])

onMounted(()=>{
  const stored = JSON.parse(localStorage.getItem('mistakes')||'[]')
  if(stored.length) mistakes.value=stored
  else{
    mistakes.value=[{id:1,course:'物理',question:'光的干涉条件?',correct:'相干光',date:'2025-05-18'}]
  }

  const storedReports = JSON.parse(localStorage.getItem('practiceReports')||'[]')
  reports.value = storedReports
  loading.value=false
})

const columns=[
 {key:'id',label:'ID'},
 {key:'course',label:'课程'},
 {key:'question',label:'题目'},
 {key:'date',label:'日期'},
 {key:'action',label:'操作'}
]

const reportColumns=[
  {key:'id',label:'ID'},
  {key:'subject',label:'科目'},
  {key:'qty',label:'题量'},
  {key:'score',label:'得分'},
  {key:'date',label:'日期'},
  {key:'action',label:'操作'}
]

const detail=ref(null)
const view=(row)=>{ detail.value=row }
const remove=(row)=>{
 dialog.value.open('确认删除该错题记录?',()=>{
   mistakes.value=mistakes.value.filter(m=>m.id!==row.id)
 })
}

const redo=(row)=>{
  router.push({name:'student-practice',query:{redoId:row.id}})
}
</script>
<template>
<div class="page-container">
  <h1 class="text-2xl font-semibold mb-6 flex items-center"><LucideIcon name="file-x" size="24" class="mr-2"/>错题本</h1>

  <!-- Tabs -->
  <div class="mb-4 flex space-x-4">
    <button :class="['px-4 py-1 rounded', tab==='mistakes' ? 'bg-indigo-600 text-white' : 'bg-gray-200']" @click="tab='mistakes'">错题</button>
    <button :class="['px-4 py-1 rounded', tab==='reports' ? 'bg-indigo-600 text-white' : 'bg-gray-200']" @click="tab='reports'">练习报告</button>
  </div>

  <!-- 错题表格 -->
  <BaseTable v-if="tab==='mistakes'" :columns="columns" :rows="mistakes" :loading="loading">
    <template #question="{row}">
      <button class="text-indigo-600 hover:underline" @click="redo(row)">{{ row.question }}</button>
    </template>
    <template #action="{row}">
      <button class="text-indigo-600 text-xs mr-2" @click="view(row)">查看</button>
      <button class="text-red-500 text-xs" @click="remove(row)">删除</button>
    </template>
  </BaseTable>

  <!-- 练习报告表格 -->
  <BaseTable v-else :columns="reportColumns" :rows="reports" :loading="loading">
    <template #cell-action="{row}">
      <button class="text-indigo-600 text-xs" @click="view(row)">查看</button>
    </template>
  </BaseTable>
  <!-- 详情弹窗 -->
  <transition name="fade">
    <div v-if="detail" class="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg shadow p-6 w-full max-w-lg relative">
        <button class="absolute top-2 right-3 text-gray-500" @click="detail=null">✕</button>
        <h3 class="text-lg font-semibold mb-4 flex items-center"><LucideIcon name="search" size="18" class="mr-1"/>详情</h3>
        <template v-if="tab==='mistakes'">
          <p class="mb-2"><span class="font-medium">课程：</span>{{ detail?.course }}</p>
          <p class="mb-2"><span class="font-medium">题目：</span>{{ detail?.question }}</p>
          <p class="text-sm text-gray-500">日期：{{ detail?.date }}</p>
        </template>
        <template v-else>
          <p class="mb-2"><span class="font-medium">科目：</span>{{ detail?.subject }}</p>
          <p class="mb-2"><span class="font-medium">题量：</span>{{ detail?.qty }}</p>
          <p class="mb-2"><span class="font-medium">得分：</span>{{ detail?.score }}</p>
          <p class="text-sm text-gray-500">日期：{{ detail?.date }}</p>
        </template>
      </div>
    </div>
  </transition>
  <ConfirmDialog ref="dialog" />
</div>
</template>
<style scoped>
.fade-enter-active,.fade-leave-active{transition:opacity .2s ease;}
.fade-enter-from,.fade-leave-to{opacity:0;}
</style> 