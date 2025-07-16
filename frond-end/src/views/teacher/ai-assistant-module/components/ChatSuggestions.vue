<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue';

const props = defineProps({
  subject: {
    type: String,
    default: ''
  },
  grade: {
    type: String,
    default: ''
  },
  recentMessages: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['select-suggestion']);

// 状态
const suggestions = ref([]);
const isLoading = ref(false);
const currentCategory = ref('common');

// 建议类别
const categories = [
  { id: 'common', name: '常用', icon: 'star' },
  { id: 'lesson', name: '教学', icon: 'book' },
  { id: 'exam', name: '考试', icon: 'clipboard' },
  { id: 'feedback', name: '反馈', icon: 'chat' },
  { id: 'concept', name: '概念', icon: 'lightbulb' }
];

// 预设建议
const predefinedSuggestions = {
  common: [
    '请为我生成一节课的教学计划',
    '如何教授复杂概念更易于学生理解？',
    '请给我一些激发学生学习热情的方法',
    '如何评估学生的学习效果？',
    '我需要一些课堂管理的建议'
  ],
  lesson: [
    '请帮我设计一个关于[主题]的教案',
    '如何设计一个有互动性的[主题]课堂活动？',
    '请设计一个适合小组合作学习的教学环节',
    '如何将项目式学习融入[主题]的教学中？',
    '请为我的[主题]课程设计一个创新的导入环节'
  ],
  exam: [
    '请生成一套关于[主题]的习题',
    '帮我设计一个能力测试而非知识测试的考题',
    '请提供一些开放性题目的评分标准',
    '如何设计能测试高阶思维能力的试题？',
    '请帮我分析这份试卷的难度分布是否合理'
  ],
  feedback: [
    '如何给学习困难的学生提供建设性反馈？',
    '请帮我写一段针对优秀作业的评语',
    '如何在批改作业时提供有效的指导？',
    '请提供一些鼓励性评语的模板',
    '如何进行有效的形成性评价？'
  ],
  concept: [
    '请用简单易懂的方式解释[概念]',
    '如何用生活实例解释抽象的[概念]？',
    '请提供一个可视化[概念]的方法',
    '如何通过类比法解释[概念]？',
    '请设计一个帮助理解[概念]的课堂实验'
  ]
};

// 初始化
onMounted(() => {
  loadSuggestions();
});

// 加载建议
const loadSuggestions = async () => {
  isLoading.value = true;
  
  try {
    // 在实际应用中，这里可能会调用API获取个性化建议
    // 目前使用预设建议 + 智能替换
    
    let currentSuggestions = [...predefinedSuggestions[currentCategory.value]];
    
    // 如果有学科和年级信息，进行智能替换
    if (props.subject && props.grade) {
      currentSuggestions = currentSuggestions.map(suggestion => {
        return suggestion
          .replace('[主题]', props.subject)
          .replace('[年级]', props.grade);
      });
    }
    
    // 如果有最近的消息，可以基于对话上下文生成更智能的建议
    if (props.recentMessages && props.recentMessages.length > 0) {
      // 实际应用中，这里可能会调用API进行智能分析
      // 此处仅做简单示例
      const lastUserMessage = props.recentMessages
        .filter(m => m.sender === 'user')
        .pop();
      
      if (lastUserMessage && lastUserMessage.content.includes('教案')) {
        currentSuggestions.push(`继续完善刚才的教案，增加更详细的活动设计`);
        currentSuggestions.push(`为这个教案添加课堂评估方案`);
      } else if (lastUserMessage && lastUserMessage.content.includes('习题')) {
        currentSuggestions.push(`增加几道思考题作为拓展`);
        currentSuggestions.push(`为这些习题提供详细的解析`);
      }
    }
    
    suggestions.value = currentSuggestions;
  } catch (error) {
    console.error('加载建议失败', error);
    suggestions.value = [];
  } finally {
    isLoading.value = false;
  }
};

// 监听类别变化
watch(currentCategory, () => {
  loadSuggestions();
});

// 选择建议
const selectSuggestion = (suggestion) => {
  emit('select-suggestion', suggestion);
};

// 获取图标
const getCategoryIcon = (iconName) => {
  switch (iconName) {
    case 'star':
      return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
      </svg>`;
    case 'book':
      return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
        <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path>
      </svg>`;
    case 'clipboard':
      return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"></path>
        <rect x="8" y="2" width="8" height="4" rx="1" ry="1"></rect>
      </svg>`;
    case 'chat':
      return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
      </svg>`;
    case 'lightbulb':
      return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M9 18h6"></path>
        <path d="M10 22h4"></path>
        <path d="M12 2v4"></path>
        <path d="M12 6a6 6 0 0 1 0 12"></path>
        <path d="M12 6a6 6 0 0 0 0 12"></path>
      </svg>`;
    default:
      return '';
  }
};
</script>

<template>
  <div class="suggestions-wrapper">
    <div class="tabs">
      <button
        v-for="category in categories"
        :key="category.id"
        class="tab"
        :class="{ active: currentCategory === category.id }"
        @click="currentCategory = category.id"
        :title="category.name"
      >
        <div class="tab-icon" v-html="getCategoryIcon(category.icon)"></div>
        <span class="tab-name">{{ category.name }}</span>
      </button>
    </div>
    
    <div class="suggestions-content">
      <div v-if="isLoading" class="loading-indicator">
        <div class="spinner"></div>
        <span>加载建议中...</span>
      </div>
      
      <div v-else class="suggestions-list">
        <button
          v-for="(suggestion, index) in suggestions"
          :key="index"
          class="suggestion-item"
          @click="selectSuggestion(suggestion)"
        >
          {{ suggestion }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.suggestions-wrapper {
  @apply w-full bg-white border-t border-gray-200;
}

.tabs {
  @apply flex border-b border-gray-200 bg-gray-50 overflow-x-auto;
}

.tab {
  @apply flex items-center px-3 md:px-4 py-2 md:py-2.5 text-xs md:text-sm text-gray-600 border-b-2 border-transparent cursor-pointer transition-colors whitespace-nowrap;
}

.tab:hover {
  @apply text-gray-900 bg-gray-100;
}

.tab.active {
  @apply text-blue-600 border-blue-600 font-medium bg-white;
  box-shadow: 0 1px 0 white;
}

.tab-icon {
  @apply flex items-center justify-center mr-1.5 md:mr-2;
}

.tab-icon svg {
  @apply h-4 w-4 md:h-5 md:w-5;
  color: currentColor;
}

.tab-name {
  @apply font-medium;
}

.suggestions-content {
  @apply p-3 md:p-4;
}

.loading-indicator {
  @apply flex items-center justify-center p-3 md:p-4 text-gray-500 text-xs md:text-sm;
}

.spinner {
  @apply w-3 h-3 md:w-4 md:h-4 border-2 border-gray-300 border-t-blue-600 rounded-full animate-spin mr-2;
}

.suggestions-list {
  @apply flex flex-wrap gap-2;
}

.suggestion-item {
  @apply px-3 md:px-4 py-1.5 md:py-2 bg-blue-50 text-blue-700 hover:bg-blue-100 rounded-md md:rounded-lg text-xs md:text-sm transition-colors text-left shadow-sm hover:shadow-md border border-blue-100;
}

@media (max-width: 640px) {
  .tab {
    @apply px-2 py-1.5 flex-1 justify-center;
  }
  
  .tab-name {
    @apply hidden;
  }
  
  .tab-icon {
    @apply mr-0;
  }
  
  .tab-icon svg {
    @apply h-5 w-5;
  }
  
  .suggestions-list {
    @apply flex-col gap-1.5;
  }
  
  .suggestion-item {
    @apply text-xs py-1.5 px-3;
  }
  
  .suggestions-content {
    @apply p-2;
  }
}
</style> 