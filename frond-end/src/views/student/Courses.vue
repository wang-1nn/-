<script setup>
import { ref } from 'vue'
import LucideIcon from '@/components/icons/LucideIcon.vue'

const loading = ref(true)
const courses = ref([])

const lastClickTime = ref(null)

const handleClick=(course)=>{
  lastClickTime.value = new Date().toLocaleString()
  // TODO: 跳转课程详情
}

setTimeout(()=>{
  courses.value=[
    { id:'c1', name:'数据结构', progress:80, teacher:'王老师' },
    { id:'c2', name:'操作系统', progress:55, teacher:'李老师' }
  ]
  loading.value=false
},800)

const progressColor=p=>p<40?'bg-red-500':p<70?'bg-amber-500':'bg-emerald-500'
</script>
<template>
  <div class="page-container">
    <h1 class="text-2xl font-semibold text-gray-800 mb-6 flex items-center"><LucideIcon name="book-open" size="24" class="mr-2 text-indigo-600"/> 我的课程</h1>
    <div v-if="loading" class="card py-10 flex justify-center text-gray-500"><LucideIcon name="loader-2" class="animate-spin"/>加载中...</div>

    <div v-else class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
      <div v-for="c in courses" :key="c.id" class="card-hover cursor-pointer" @click="handleClick(c)">
        <div class="flex justify-between items-center mb-2"><span class="font-medium">{{c.name}}</span><span class="text-xs text-gray-500">{{c.teacher}}</span></div>
        <div class="h-2 bg-gray-100 rounded-full overflow-hidden mb-2"><div :class="['h-full',progressColor(c.progress)]" :style="{width:c.progress+'%'}"></div></div>
        <div class="text-xs text-gray-500">进度 {{c.progress}}%</div>
        <div v-if="lastClickTime" class="text-[11px] text-gray-400 mt-1">上次点击：{{ lastClickTime }}</div>
      </div>
    </div>
  </div>
</template>
<style scoped></style> 