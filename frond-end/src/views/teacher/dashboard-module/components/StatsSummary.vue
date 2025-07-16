<script setup>
defineProps({
  summary: {
    type: Object,
    required: true
  }
});
</script>

<template>
  <div class="stats-container">
    <div class="stat-card">
      <div class="stat-icon bg-blue-100 text-blue-600">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
        </svg>
      </div>
      <div class="stat-content">
        <div class="stat-title">我的课程</div>
        <div class="stat-value">{{ summary.courses.total }}</div>
        <div class="stat-detail">
          <span>活跃: {{ summary.courses.active }}</span>
          <span class="stat-trend">+{{ summary.courses.trend }}</span>
        </div>
      </div>
    </div>
    
    <div class="stat-card">
      <div class="stat-icon bg-green-100 text-green-600">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
        </svg>
      </div>
      <div class="stat-content">
        <div class="stat-title">学生人数</div>
        <div class="stat-value">{{ summary.students.total }}</div>
        <div class="stat-detail">
          <span>活跃: {{ summary.students.active }}</span>
        </div>
      </div>
    </div>
    
    <div class="stat-card">
      <div class="stat-icon bg-purple-100 text-purple-600">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
        </svg>
      </div>
      <div class="stat-content">
        <div class="stat-title">教学计划</div>
        <div class="stat-value">{{ summary.plans.total }}</div>
        <div class="stat-detail">
          <span class="stat-trend">+{{ summary.plans.trend }}</span>
        </div>
      </div>
    </div>
    
    <div class="stat-card">
      <div class="stat-icon bg-orange-100 text-orange-600">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 8h14M5 8a2 2 0 110-4h14a2 2 0 110 4M5 8v10a2 2 0 002 2h10a2 2 0 002-2V8m-9 4h4" />
        </svg>
      </div>
      <div class="stat-content">
        <div class="stat-title">教学资源</div>
        <div class="stat-value">{{ summary.resources.total }}</div>
        <div class="stat-detail">
          <span class="stat-trend">+{{ summary.resources.trend }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.stats-container {
  @apply grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5 mb-6;
}

.stat-card {
  @apply bg-white rounded-xl shadow-sm p-5 flex items-center hover:shadow-md transition-all duration-300 border border-gray-100;
  transform: translateY(0);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-icon {
  @apply w-14 h-14 rounded-full flex items-center justify-center text-xl mr-4;
}

.stat-content {
  @apply flex-1;
}

.stat-title {
  @apply text-sm text-gray-500 font-medium;
}

.stat-value {
  @apply text-2xl font-bold mt-1;
}

.stat-detail {
  @apply text-xs text-gray-500 flex justify-between mt-1;
}

.stat-trend {
  @apply text-green-500 font-medium;
}
</style>