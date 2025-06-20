<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import LucideIcon from '@/components/icons/LucideIcon.vue'
import * as echarts from 'echarts'

const step = ref(0) // 0 config ,1 answer ,2 result
const config = reactive({ qty: 5, subject: '随机' })
const questions = ref([])
const answers = reactive({})
const score = ref(null)
const loading = ref(false)

// 图表相关
const chartRef = ref(null)
let chartIns = null

const buildStats = () => {
  const mistakes = JSON.parse(localStorage.getItem('mistakes') || '[]')
  if (!mistakes.length) return { labels: [], data: [] }
  const map = {}
  mistakes.forEach((m) => {
    map[m.course] = (map[m.course] || 0) + 1
  })
  const labels = Object.keys(map)
  const data = labels.map((l) => map[l])
  return { labels, data }
}

const renderChart = () => {
  if (!chartRef.value) return
  const { labels, data } = buildStats()
  if (!labels.length) return
  chartIns = chartIns || echarts.init(chartRef.value)
  chartIns.setOption({
    tooltip: {},
    xAxis: {
      type: 'category',
      data: labels,
    },
    yAxis: {
      type: 'value',
    },
    series: [
      {
        name: '错题数',
        type: 'bar',
        data,
        itemStyle: { color: '#6366f1' },
      },
    ],
  })
}

onMounted(() => {
  const route = useRoute()
  // 如果带有 redoId，直接进入答题模式
  if (route.query.redoId) {
    const mistakes = JSON.parse(localStorage.getItem('mistakes') || '[]')
    const target = mistakes.find((m) => String(m.id) === String(route.query.redoId))
    if (target) {
      config.subject = target.course
      config.qty = 1
      questions.value = [
        {
          id: target.id,
          stem: target.question,
          correct: target.correct || '',
        },
      ]
      step.value = 1
    }
  }
  // 初始渲染统计图
  renderChart()
})

// 当返回配置页面时刷新图表
watch(step, (val) => {
  if (val === 0) {
    setTimeout(renderChart, 200)
  }
})

const buildQuestions = () => {
  return Array.from({ length: config.qty }).map((_, i) => ({
    id: Date.now() + i,
    stem: `题目 ${i + 1}: 3 + ${i}= ?`,
    correct: `${3 + i}`,
  }))
}

const generate = () => {
  loading.value = true
  setTimeout(() => {
    questions.value = buildQuestions()
    step.value = 1
    loading.value = false
  }, 600)
}

const restart = () => {
  answers.value = {}
  score.value = null
  loading.value = true
  setTimeout(() => {
    questions.value = buildQuestions()
    step.value = 1
    loading.value = false
  }, 600)
}

const evaluate = () => {
  loading.value = true
  setTimeout(() => {
    let correctCnt = 0
    const mistakesArr = []
    questions.value.forEach((q) => {
      if (answers[q.id] == q.correct) correctCnt++
      else
        mistakesArr.push({
          id: q.id,
          course: config.subject,
          question: q.stem,
          correct: q.correct,
          date: new Date().toISOString().slice(0, 10),
        })
    })
    score.value = Math.round((correctCnt / questions.value.length) * 100)
    // 保存错题
    const stored = JSON.parse(localStorage.getItem('mistakes') || '[]')
    localStorage.setItem('mistakes', JSON.stringify([...stored, ...mistakesArr]))
    // 保存练习报告
    const reports = JSON.parse(localStorage.getItem('practiceReports') || '[]')
    reports.push({
      id: Date.now(),
      subject: config.subject,
      qty: config.qty,
      score: score.value,
      date: new Date().toISOString().slice(0, 10),
    })
    localStorage.setItem('practiceReports', JSON.stringify(reports))

    step.value = 2
    loading.value = false
  }, 600)
}

const reset = () => {
  step.value = 0
  questions.value = []
  Object.keys(answers).forEach((k) => delete answers[k])
  score.value = null
}
</script>
<template>
<div class="page-container">
  <h1 class="text-2xl font-semibold mb-6 flex items-center"><LucideIcon name="zap" size="24" class="mr-2 text-indigo-600"/>实时练习助手</h1>

  <!-- 薄弱点统计图 -->
  <div v-if="step===0" class="bg-white rounded-lg shadow p-6 mb-6">
    <h2 class="text-lg font-medium mb-4 flex items-center"><LucideIcon name="bar-chart-2" size="18" class="mr-1"/>薄弱科目概览</h2>
    <div ref="chartRef" class="w-full h-64 flex items-center justify-center text-gray-400" v-show="buildStats().labels.length"></div>
    <p v-if="!buildStats().labels.length" class="text-center text-sm text-gray-400">暂无错题数据，开始练习以生成统计</p>
  </div>

  <!-- 配置 -->
  <div v-if="step===0" class="bg-white rounded-lg shadow p-6 max-w-xl">
    <div class="grid grid-cols-2 gap-4 mb-6">
      <div>
        <label class="text-sm">题目数量</label>
        <input type="number" v-model.number="config.qty" min="1" max="20" class="form-input w-full"/>
      </div>
      <div>
        <label class="text-sm">科目</label>
        <input v-model="config.subject" class="form-input w-full"/>
      </div>
    </div>
    <button class="btn-primary" :disabled="loading" @click="generate">
      <span v-if="loading"><LucideIcon name="loader-2" class="animate-spin mr-1"/>生成中</span>
      <span v-else>开始练习</span>
    </button>
  </div>

  <!-- 答题 -->
  <div v-else-if="step===1" class="space-y-4 max-w-xl">
    <div v-for="q in questions" :key="q.id" class="bg-white shadow rounded-lg p-4">
      <p class="font-medium mb-2">{{ q.stem }}</p>
      <input v-model="answers[q.id]" placeholder="你的答案" class="form-input w-full"/>
    </div>
    <button class="btn-primary" :disabled="loading" @click="evaluate" >提交评测</button>
  </div>

  <!-- 结果 -->
  <div v-else class="text-center bg-white p-10 rounded-lg shadow max-w-xl mx-auto">
    <LucideIcon name="award" size="36" class="text-emerald-500 mb-4"/>
    <h2 class="text-3xl font-bold mb-2">{{ score }} 分</h2>
    <p class="text-gray-600 mb-6">已将错题自动加入错题本，并更新统计</p>
    <button class="btn-primary mr-2" @click="restart">再练一次</button>
  </div>
</div>
</template>
<style scoped></style> 