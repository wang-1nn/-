<script setup>
defineProps({
  announcements: {
    type: Array,
    default: () => []
  }
});
</script>

<template>
  <div class="announcements-card">
    <div class="card-header">
      <h3>通知公告</h3>
      <button class="view-all-btn">
        全部
        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
        </svg>
      </button>
    </div>
    
    <div class="announcement-list">
      <div 
        v-for="item in announcements" 
        :key="item.id"
        class="announcement-item"
      >
        <div class="announcement-badge new">{{ item.courseName }}</div>
        <div class="announcement-content">
          <div class="announcement-title">{{ item.title }}</div>
          <div class="announcement-date">{{ new Date(item.timestamp).toLocaleDateString() }}</div>
        </div>
      </div>
      
      <div v-if="!announcements.length" class="empty-announcements">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5.882V19.24a1.76 1.76 0 01-3.417.592l-2.147-6.15M18 13a3 3 0 100-6M5.436 13.683A4.001 4.001 0 017 6h1.832c4.1 0 7.625-1.234 9.168-3v14c-1.543-1.766-5.067-3-9.168-3H7a3.988 3.988 0 01-1.564-.317z" />
        </svg>
        <p class="mt-2 text-gray-500">暂无公告</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.announcements-card {
  @apply bg-white rounded-xl shadow-sm p-5 border border-gray-100 hover:shadow-md transition-all duration-300;
}

.card-header {
  @apply flex justify-between items-center mb-5;
}

.card-header h3 {
  @apply text-lg font-semibold text-gray-800;
}

.view-all-btn {
  @apply text-sm text-blue-600 hover:text-blue-800 font-medium flex items-center transition-colors duration-200;
}

.announcement-list {
  @apply space-y-4;
}

.announcement-item {
  @apply flex p-4 rounded-lg border border-gray-200 hover:border-blue-200 hover:bg-blue-50 transition-all duration-200 cursor-pointer;
}

.announcement-badge {
  @apply bg-gray-100 text-gray-800 text-xs px-3 py-1 rounded-full h-min mr-4 font-medium;
}

.announcement-badge.new {
  @apply bg-red-100 text-red-800;
}

.announcement-content {
  @apply flex-1;
}

.announcement-title {
  @apply text-sm font-medium;
}

.announcement-date {
  @apply text-xs text-gray-500 mt-2;
}

.empty-announcements {
  @apply flex flex-col items-center justify-center py-10 text-center;
}
</style>