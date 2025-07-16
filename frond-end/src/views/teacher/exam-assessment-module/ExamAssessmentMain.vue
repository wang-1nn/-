<template>
  <div class="exam-assessment-main">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">题库与考核管理</h1>
      <p class="page-description">智能题库管理、考试创建、自动批改一站式解决方案</p>
    </div>

    <!-- 功能模块卡片 -->
    <div class="modules-grid">
      <!-- 题库管理 -->
      <div class="module-card" @click="navigateTo('QuestionBank')">
        <div class="module-icon question-bank">
          <el-icon><Collection /></el-icon>
        </div>
        <div class="module-content">
          <h3 class="module-title">题库管理</h3>
          <p class="module-description">管理题目库，支持分类、筛选、编辑和批量操作</p>
          <div class="module-stats">
            <span class="stat-item">
              <el-icon><Document /></el-icon>
              {{ questionStats.total }} 道题目
            </span>
            <span class="stat-item">
              <el-icon><FolderOpened /></el-icon>
              {{ questionStats.categories }} 个分类
            </span>
          </div>
        </div>
        <div class="module-action">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>

      <!-- 智能题目生成 -->
      <div class="module-card" @click="navigateTo('QuestionGenerator')">
        <div class="module-icon question-generator">
          <el-icon><MagicStick /></el-icon>
        </div>
        <div class="module-content">
          <h3 class="module-title">智能题目生成</h3>
          <p class="module-description">AI驱动的题目生成，支持多种题型和难度配置</p>
          <div class="module-stats">
            <span class="stat-item">
              <el-icon><Cpu /></el-icon>
              AI智能生成
            </span>
            <span class="stat-item">
              <el-icon><Setting /></el-icon>
              多种配置
            </span>
          </div>
        </div>
        <div class="module-action">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>

      <!-- 考试管理 -->
      <div class="module-card" @click="navigateTo('Exams')">
        <div class="module-icon exams">
          <el-icon><Notebook /></el-icon>
        </div>
        <div class="module-content">
          <h3 class="module-title">考试管理</h3>
          <p class="module-description">创建、编辑和管理考试，支持多种考试模式</p>
          <div class="module-stats">
            <span class="stat-item">
              <el-icon><Calendar /></el-icon>
              {{ examStats.total }} 场考试
            </span>
            <span class="stat-item">
              <el-icon><Clock /></el-icon>
              {{ examStats.ongoing }} 场进行中
            </span>
          </div>
        </div>
        <div class="module-action">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>

      <!-- 自动批改 -->
      <div class="module-card" @click="navigateTo('AutoGraderExam')">
        <div class="module-icon auto-grader">
          <el-icon><EditPen /></el-icon>
        </div>
        <div class="module-content">
          <h3 class="module-title">自动批改</h3>
          <p class="module-description">智能批改系统，支持多种文件格式和批改模式</p>
          <div class="module-stats">
            <span class="stat-item">
              <el-icon><Upload /></el-icon>
              批量上传
            </span>
            <span class="stat-item">
              <el-icon><Check /></el-icon>
              智能评分
            </span>
          </div>
        </div>
        <div class="module-action">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
    </div>

    <!-- 快速操作区域 -->
    <div class="quick-actions">
      <h2 class="section-title">快速操作</h2>
      <div class="actions-grid">
        <el-button type="primary" size="large" @click="navigateTo('QuestionGenerator')">
          <el-icon><Plus /></el-icon>
          生成新题目
        </el-button>
        <el-button type="success" size="large" @click="navigateTo('ExamEditor', { id: 'new' })">
          <el-icon><Plus /></el-icon>
          创建新考试
        </el-button>
        <el-button type="warning" size="large" @click="navigateTo('AutoGraderExam')">
          <el-icon><Upload /></el-icon>
          批改作业
        </el-button>
      </div>
    </div>

    <!-- 最近活动 -->
    <div class="recent-activities">
      <h2 class="section-title">最近活动</h2>
      <div class="activity-list">
        <div v-for="activity in recentActivities" :key="activity.id" class="activity-item">
          <div class="activity-icon" :class="activity.type">
            <el-icon><component :is="getActivityIcon(activity.type)" /></el-icon>
          </div>
          <div class="activity-content">
            <p class="activity-title">{{ activity.title }}</p>
            <p class="activity-time">{{ formatTime(activity.time) }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import {
  Collection,
  Document,
  FolderOpened,
  MagicStick,
  Cpu,
  Setting,
  Notebook,
  Calendar,
  Clock,
  EditPen,
  Upload,
  Check,
  ArrowRight,
  Plus
} from '@element-plus/icons-vue';

const router = useRouter();

// 统计数据
const questionStats = ref({
  total: 1248,
  categories: 12
});

const examStats = ref({
  total: 25,
  ongoing: 3
});

// 最近活动
const recentActivities = ref([
  {
    id: 1,
    type: 'question',
    title: '新增了 15 道JavaScript题目',
    time: new Date(Date.now() - 2 * 60 * 60 * 1000)
  },
  {
    id: 2,
    type: 'exam',
    title: '创建了期中考试',
    time: new Date(Date.now() - 5 * 60 * 60 * 1000)
  },
  {
    id: 3,
    type: 'grade',
    title: '完成了作业批改',
    time: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000)
  }
]);

// 导航到指定页面
const navigateTo = (routeName, params = {}) => {
  router.push({ name: routeName, params });
};

// 获取活动图标
const getActivityIcon = (type) => {
  const iconMap = {
    question: Document,
    exam: Notebook,
    grade: Check
  };
  return iconMap[type] || Document;
};

// 格式化时间
const formatTime = (time) => {
  const now = new Date();
  const diff = now - time;
  const hours = Math.floor(diff / (1000 * 60 * 60));
  const days = Math.floor(hours / 24);

  if (days > 0) {
    return `${days}天前`;
  } else if (hours > 0) {
    return `${hours}小时前`;
  } else {
    return '刚刚';
  }
};

onMounted(() => {
  // 可以在这里加载统计数据
});
</script>

<style scoped>
.exam-assessment-main {
  padding: 24px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 8px;
}

.page-description {
  font-size: 16px;
  color: #6b7280;
}

.modules-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.module-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 16px;
}

.module-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.module-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.module-icon.question-bank {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.module-icon.question-generator {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.module-icon.exams {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.module-icon.auto-grader {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.module-content {
  flex: 1;
}

.module-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.module-description {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 12px;
}

.module-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #9ca3af;
}

.module-action {
  color: #9ca3af;
  font-size: 20px;
}

.quick-actions,
.recent-activities {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
}

.actions-grid {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.activity-list {
  space-y: 12px;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: white;
}

.activity-icon.question {
  background-color: #3b82f6;
}

.activity-icon.exam {
  background-color: #10b981;
}

.activity-icon.grade {
  background-color: #f59e0b;
}

.activity-content {
  flex: 1;
}

.activity-title {
  font-size: 14px;
  color: #1f2937;
  margin-bottom: 4px;
}

.activity-time {
  font-size: 12px;
  color: #9ca3af;
}
</style>
