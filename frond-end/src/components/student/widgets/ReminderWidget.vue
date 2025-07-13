<script setup>
import { ref, onMounted } from 'vue';
import { get } from '@/net';
import {useAuthStore} from "@/stores/counter.js";

const authStore = useAuthStore()
const userId = authStore.user.userId
const plans = ref([]);
const isLoading = ref(true);

// 从后端加载学习计划数据
const loadStudyPlans = () => {
  isLoading.value = true;
  
  // 获取用户ID
  
  get('/api/student/dashboard/todo-tasks', { userId }, 
    (message, data) => {
      if (data && data.tasks) {
        plans.value = data.tasks;
      }
      isLoading.value = false;
    }, 
    (message) => {
      console.error('获取学习计划数据失败:', message);
      isLoading.value = false;
    }
  );
};

// 页面加载时获取数据
onMounted(() => {
  loadStudyPlans();
});
</script>

<template>
  <div class="study-plan-widget">
    <!-- 加载状态 -->
    <div v-if="isLoading" class="loading-overlay">
      <div class="loading-spinner"></div>
    </div>
    
    <div v-else class="study-plan-content">
      <h3 class="widget-title">学习计划</h3>
      <div class="plan-list">
        <div v-for="plan in plans" :key="plan.id" class="plan-item" :class="{ 'completed': plan.completed }">
          <div class="plan-status">
            <div class="status-indicator" :class="{ 'completed': plan.completed }"></div>
          </div>
          <div class="plan-info">
            <div class="plan-title">{{ plan.title }}</div>
            <div class="plan-due" :class="{ 'urgent': plan.urgent }">{{ plan.due }}</div>
          </div>
        </div>
        
        <!-- 没有计划时显示提示 -->
        <div v-if="plans.length === 0" class="no-plans-tip">
          暂无学习计划
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.study-plan-widget {
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(255, 255, 255, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 10;
}

.loading-spinner {
  width: 30px;
  height: 30px;
  border: 3px solid #e5e7eb;
  border-top: 3px solid #4f46e5;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.study-plan-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 0.5rem;
}

.widget-title {
  font-size: 1rem;
  font-weight: 600;
  color: #4f46e5;
  margin-top: 0;
  margin-bottom: 1rem;
  text-align: center;
}

.plan-list {
  flex-grow: 1;
  overflow-y: auto;
}

.plan-item {
  display: flex;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid #e5e7eb;
}

.plan-item:last-child {
  border-bottom: none;
}

.plan-status {
  margin-right: 0.75rem;
}

.status-indicator {
  width: 0.75rem;
  height: 0.75rem;
  border-radius: 50%;
  background-color: #e5e7eb;
  border: 2px solid #4f46e5;
}

.status-indicator.completed {
  background-color: #4f46e5;
}

.plan-info {
  flex-grow: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.plan-title {
  font-size: 0.875rem;
  color: #374151;
}

.completed .plan-title {
  text-decoration: line-through;
  color: #9ca3af;
}

.plan-due {
  font-size: 0.75rem;
  color: #6b7280;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  background-color: #f3f4f6;
  white-space: nowrap;
  margin-left: 0.5rem;
}

.plan-due.urgent {
  color: #ef4444;
  background-color: #fee2e2;
}

.no-plans-tip {
  padding: 2rem 0;
  text-align: center;
  color: #9ca3af;
  font-size: 0.875rem;
}
</style>