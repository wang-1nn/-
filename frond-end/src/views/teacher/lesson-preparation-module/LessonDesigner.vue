<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import draggable from 'vuedraggable';

// 导入自定义组件
import OutlineTree from './components/OutlineTree.vue';
import LessonMetadata from './components/LessonMetadata.vue';
import LessonContentBuilder from './components/LessonContentBuilder.vue';

const router = useRouter();
const outlineData = ref(null);
const expandAll = ref(false);
const selectedNode = ref('');

// 模拟数据 - 课程大纲
const mockOutlineData = {
  courseInfo: {
    id: 1,
    name: "计算机网络",
    grade: "大学三年级",
    subject: "计算机科学",
    totalHours: 48
  },
  outline: [
    {
      id: "unit-1",
      title: "计算机网络概述",
      children: [
        {
          id: "topic-1-1",
          title: "计算机网络的定义与分类",
          children: [
            { id: "subtopic-1-1-1", title: "计算机网络的定义" },
            { id: "subtopic-1-1-2", title: "计算机网络的分类" },
            { id: "subtopic-1-1-3", title: "计算机网络的发展历史" }
          ]
        },
        {
          id: "topic-1-2",
          title: "网络协议与标准化",
          children: [
            { id: "subtopic-1-2-1", title: "网络协议的概念" },
            { id: "subtopic-1-2-2", title: "OSI参考模型" },
            { id: "subtopic-1-2-3", title: "TCP/IP协议族" }
          ]
        }
      ]
    },
    {
      id: "unit-2",
      title: "物理层",
      children: [
        {
          id: "topic-2-1",
          title: "数据通信基础",
          children: [
            { id: "subtopic-2-1-1", title: "数据通信系统模型" },
            { id: "subtopic-2-1-2", title: "信道与信道容量" },
            { id: "subtopic-2-1-3", title: "数字信号与模拟信号" }
          ]
        },
        {
          id: "topic-2-2",
          title: "传输介质",
          children: [
            { id: "subtopic-2-2-1", title: "有线传输介质" },
            { id: "subtopic-2-2-2", title: "无线传输介质" }
          ]
        }
      ]
    },
    {
      id: "unit-3",
      title: "数据链路层",
      children: [
        {
          id: "topic-3-1",
          title: "差错控制",
          children: [
            { id: "subtopic-3-1-1", title: "差错检测编码" },
            { id: "subtopic-3-1-2", title: "差错纠正编码" }
          ]
        },
        {
          id: "topic-3-2",
          title: "介质访问控制",
          children: [
            { id: "subtopic-3-2-1", title: "信道划分介质访问控制" },
            { id: "subtopic-3-2-2", title: "随机访问介质访问控制" },
            { id: "subtopic-3-2-3", title: "轮询访问介质访问控制" }
          ]
        }
      ]
    }
  ]
};

// 教案数据
const lessonPlan = reactive({
  title: '',
  grade: '',
  subject: '',
  duration: 1,
  blocks: []
});

// 加载大纲数据
onMounted(() => {
  // 模拟从API获取数据
  setTimeout(() => {
    outlineData.value = mockOutlineData;
    
    // 预填充教案信息
    if (outlineData.value.courseInfo) {
      lessonPlan.title = outlineData.value.courseInfo.name || '';
      lessonPlan.grade = outlineData.value.courseInfo.grade || '';
      lessonPlan.subject = outlineData.value.courseInfo.subject || '';
      lessonPlan.duration = outlineData.value.courseInfo.totalHours || 1;
    }
  }, 300);
  
  /* 实际开发中使用API或本地存储
  // 从本地存储获取大纲数据
  const savedOutlineData = localStorage.getItem('outlineData');
  if (savedOutlineData) {
    outlineData.value = JSON.parse(savedOutlineData);
    
    // 预填充教案信息
    if (outlineData.value.courseInfo) {
      lessonPlan.title = outlineData.value.courseInfo.name || '';
      lessonPlan.grade = outlineData.value.courseInfo.grade || '';
      lessonPlan.subject = outlineData.value.courseInfo.subject || '';
      lessonPlan.duration = outlineData.value.courseInfo.totalHours || 1;
    }
  }
  */
});

// 添加到教案
const addToLessonPlan = (item) => {
  // 生成唯一ID
  const blockId = Date.now().toString();
  
  // 创建新的教案块
  const newBlock = {
    id: blockId,
    type: 'content',
    title: item.title || '',
    content: '',
    duration: 10,
  };
  
  lessonPlan.blocks.push(newBlock);
};

// 添加教案块
const addLessonBlock = (newBlock) => {
  lessonPlan.blocks.push(newBlock);
};

// 删除教案块
const removeLessonBlock = (index) => {
  lessonPlan.blocks.splice(index, 1);
};

// 保存为草稿
const saveAsDraft = () => {
  // 保存到本地存储或发送到服务器
  localStorage.setItem('lessonPlanDraft', JSON.stringify(lessonPlan));
  
  // 显示成功消息
  alert('教案已保存为草稿');
};

// 发布教案
const publishLesson = () => {
  // 发送到服务器
  alert('教案已发布');
};

// 前往大纲导入页面
const goToOutlineImport = () => {
  router.push('/teacher/outline-import');
};
</script>

<template>
  <div class="lesson-designer">
    <div class="page-header">
      <h1>智能备课设计</h1>
      <div class="header-actions">
        <button class="save-draft-btn" @click="saveAsDraft">保存草稿</button>
        <button class="publish-btn" @click="publishLesson">发布教案</button>
      </div>
    </div>
    
    <div class="designer-layout">
      <!-- 左侧大纲面板 -->
      <div class="outline-panel">
        <OutlineTree 
          :outline-data="outlineData"
          v-model:expand-all="expandAll"
          :selected-node="selectedNode"
          @select-node="selectedNode = $event"
          @add-to-lesson-plan="addToLessonPlan"
          @import-outline="goToOutlineImport"
        />
      </div>
      
      <!-- 右侧教案设计区 -->
      <div class="content-panel">
        <!-- 教案信息 -->
        <LessonMetadata v-model:lesson-plan="lessonPlan" />
        
        <!-- 教案内容时间轴 -->
        <LessonContentBuilder 
          v-model:blocks="lessonPlan.blocks"
          @add-block="addLessonBlock"
          @remove-block="removeLessonBlock"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.lesson-designer {
  @apply p-6;
}

.page-header {
  @apply flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6;
}

.page-header h1 {
  @apply text-2xl font-bold text-gray-800;
}

.header-actions {
  @apply flex flex-wrap gap-3;
}

.save-draft-btn {
  @apply px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors duration-200 font-medium;
}

.publish-btn {
  @apply px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors duration-200 font-medium;
}

.designer-layout {
  @apply grid grid-cols-1 lg:grid-cols-4 gap-6;
}

.outline-panel {
  @apply lg:col-span-1;
}

.content-panel {
  @apply lg:col-span-3 space-y-6;
}
</style>