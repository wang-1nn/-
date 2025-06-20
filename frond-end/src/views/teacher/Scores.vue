<script setup>
import { reactive, ref, onMounted } from 'vue'
import LucideIcon from '@/components/icons/LucideIcon.vue'

const loading = ref(true)

const overview = reactive({
  avgScore: 83,
  passRate: 92,
  highErrorTopics: [
    { topic: '递归', errorRate: 35 },
    { topic: '红黑树', errorRate: 28 },
    { topic: '拓扑排序', errorRate: 22 }
  ]
})

onMounted(() => {
  // TODO 调用数据接口
  setTimeout(()=> loading.value = false, 900)
})
</script>

<template>
  <div class="page-container">
    <h1 class="text-2xl font-semibold text-gray-800 mb-6 flex items-center"><LucideIcon name="activity" size="24" class="mr-2 text-emerald-600"/> 学情数据分析</h1>

    <div v-if="loading" class="card py-10 flex justify-center text-gray-500"><LucideIcon name="loader-2" class="animate-spin mr-2"/>加载中...</div>

    <div v-else class="grid gap-6 lg:grid-cols-3">
      <div class="card flex flex-col items-center justify-center text-center">
        <LucideIcon name="star" size="32" class="text-yellow-500 mb-2"/>
        <div class="text-3xl font-bold text-gray-800">{{ overview.avgScore }}</div>
        <div class="text-sm text-gray-500 mt-1">平均分</div>
      </div>

      <div class="card flex flex-col items-center justify-center text-center">
        <LucideIcon name="check-circle" size="32" class="text-green-500 mb-2"/>
        <div class="text-3xl font-bold text-gray-800">{{ overview.passRate }}%</div>
        <div class="text-sm text-gray-500 mt-1">通过率</div>
      </div>

      <div class="card">
        <h2 class="text-lg font-medium text-gray-800 mb-3">高频错误知识点</h2>
        <ul class="space-y-3">
          <li v-for="(t, idx) in overview.highErrorTopics" :key="idx" class="flex justify-between items-center">
            <span>{{ t.topic }}</span>
            <div class="flex items-center">
              <div class="h-2 w-32 bg-gray-200 rounded-full mr-2 overflow-hidden">
                <div class="h-full bg-red-500" :style="{ width: t.errorRate + '%' }"></div>
              </div>
              <span class="text-sm text-gray-500">{{ t.errorRate }}%</span>
            </div>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<style scoped>
</style> 