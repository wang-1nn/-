<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import LucideIcon from '@/components/icons/LucideIcon.vue'

const route = useRoute()
const router = useRouter()
const examId = route.params.examId

const loading = ref(true)
const exam = ref(null)

// 模拟加载考试详情
onMounted(()=>{
  setTimeout(()=>{
    exam.value={id:examId,title:'示例考试',description:'共 20 题，请在 60 分钟内完成。'}
    loading.value=false
  },800)
})

const startExam = ()=>{
  // TODO: 调用后端 API 获取试题并跳转至作答界面
  alert('开始考试 - TODO');
}
</script>
<template>
<div class="page-container">
  <button class="btn-outline mb-4 flex items-center" @click="router.back()"><LucideIcon name="arrow-left" size="16" class="mr-1"/>返回</button>
  <div v-if="loading" class="flex justify-center py-10 text-gray-500"><LucideIcon name="loader-2" class="animate-spin mr-2"/>加载考试信息...</div>
  <div v-else class="bg-white rounded-lg shadow p-6 max-w-2xl mx-auto">
    <h1 class="text-2xl font-semibold mb-2">{{ exam.title }}</h1>
    <p class="text-gray-600 mb-6">{{ exam.description }}</p>
    <button class="btn-primary" @click="startExam">开始答题</button>
  </div>
</div>
</template>
<style scoped></style> 