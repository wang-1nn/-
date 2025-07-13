<script setup>
import { ref, computed } from 'vue';

const props = defineProps({
  courseDetail: {
    type: Object,
    required: true
  }
});

// 解析课程表信息
const scheduleInfo = computed(() => {
  if (!props.courseDetail.schedule) return null;
  
  // 假设格式为"周一3-4节 主楼301"
  const scheduleText = props.courseDetail.schedule;
  const dayMatch = scheduleText.match(/周(一|二|三|四|五|六|日)/);
  const periodMatch = scheduleText.match(/(\d+)-(\d+)节/);
  const locationMatch = scheduleText.match(/\s(.+)$/);
  
  return {
    day: dayMatch ? dayMatch[0] : '',
    startPeriod: periodMatch ? parseInt(periodMatch[1]) : 0,
    endPeriod: periodMatch ? parseInt(periodMatch[2]) : 0,
    location: locationMatch ? locationMatch[1] : ''
  };
});

// 生成一周的课程表
const weekSchedule = computed(() => {
  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
  const periods = Array.from({ length: 11 }, (_, i) => i + 1); // 1-11节课
  
  return {
    days,
    periods,
    schedule: scheduleInfo.value
  };
});

// 检查是否是当前课程的时间段
const isCurrentCourseTime = (day, period) => {
  if (!scheduleInfo.value) return false;
  
  return day === scheduleInfo.value.day && 
         period >= scheduleInfo.value.startPeriod && 
         period <= scheduleInfo.value.endPeriod;
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  
  const date = new Date(dateString);
  return `${date.getFullYear()}年${(date.getMonth() + 1)}月${date.getDate()}日`;
};
</script>

<template>
  <div class="course-schedule p-4">
    <div class="mb-6 grid grid-cols-2 gap-4">
      <div class="flex items-center">
        <div class="text-gray-500 w-20">开课时间:</div>
        <div class="font-medium">{{ formatDate(courseDetail.startDate) }}</div>
      </div>
      <div class="flex items-center">
        <div class="text-gray-500 w-20">结课时间:</div>
        <div class="font-medium">{{ formatDate(courseDetail.endDate) }}</div>
      </div>
      <div class="flex items-center">
        <div class="text-gray-500 w-20">课程学分:</div>
        <div class="font-medium">{{ courseDetail.credits }} 学分</div>
      </div>
      <div class="flex items-center">
        <div class="text-gray-500 w-20">所属院系:</div>
        <div class="font-medium">{{ courseDetail.department }}</div>
      </div>
    </div>
    
    <div class="weekly-schedule overflow-x-auto">
      <h3 class="text-lg font-semibold mb-4 flex items-center">
        <i class="fas fa-calendar-week mr-2 text-green-500"></i> 课程表
      </h3>
      
      <table class="min-w-full border-collapse">
        <thead>
          <tr>
            <th class="py-2 px-3 bg-gray-50 border text-sm font-medium text-gray-500">时间/日期</th>
            <th v-for="day in weekSchedule.days" :key="day" class="py-2 px-3 bg-gray-50 border text-sm font-medium text-gray-500">
              {{ day }}
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="period in weekSchedule.periods" :key="period">
            <td class="py-2 px-3 border text-sm text-center text-gray-500">第{{ period }}节</td>
            <td 
              v-for="day in weekSchedule.days" 
              :key="`${day}-${period}`" 
              class="py-2 px-3 border text-sm text-center"
              :class="{'bg-green-50 text-green-700': isCurrentCourseTime(day, period)}"
            >
              <template v-if="isCurrentCourseTime(day, period)">
                <div class="font-medium">{{ courseDetail.title }}</div>
                <div class="text-xs mt-1">{{ weekSchedule.schedule.location }}</div>
              </template>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <div class="mt-6 p-4 bg-blue-50 rounded-lg">
      <h3 class="font-medium text-blue-700 mb-2">学习进度</h3>
      <div class="flex items-center">
        <div class="flex-1 h-2 bg-gray-200 rounded-full overflow-hidden">
          <div 
            class="h-full bg-blue-500 rounded-full" 
            :style="`width: ${courseDetail.progress}%`"
          ></div>
        </div>
        <span class="ml-3 text-blue-700 font-medium">{{ courseDetail.progress }}%</span>
      </div>
      <div class="mt-2 text-sm text-blue-600">
        当前学习: {{ courseDetail.lastPosition }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.weekly-schedule {
  max-height: 500px;
  overflow-y: auto;
}

.weekly-schedule::-webkit-scrollbar {
  width: 4px;
  height: 4px;
}

.weekly-schedule::-webkit-scrollbar-thumb {
  background: #e5e7eb;
  border-radius: 2px;
}

.weekly-schedule::-webkit-scrollbar-track {
  background: #f9fafb;
}

@media (max-width: 768px) {
  .weekly-schedule {
    overflow-x: auto;
  }
}
</style> 