<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue';
import ChatWindow from './components/ChatWindow.vue';
import ResourceLibrary from './components/ResourceLibrary.vue';
import ResourceGenerator from './components/ResourceGenerator.vue';
import ResourceViewer from './components/ResourceViewer.vue';
import ResourceRecommender from './components/ResourceRecommender.vue';
import AssignmentGrader from './components/AssignmentGrader.vue';

// 主要状态变量
const loading = ref(false);
const activeTab = ref('chat'); // 'chat', 'library', 'generator', 'viewer', 'recommender', 'grader'
const savedResources = ref([]);
const resourceBeingViewed = ref(null);
const generatingType = ref(null); // 'lesson', 'questions', 'quiz', 'presentation'
const aiContext = ref(''); // 当前AI对话上下文
const searchTerm = ref('');
const themeColor = ref('blue'); // 'blue', 'purple', 'teal', 'green'
const showResourceRecommender = ref(false);
const showAssignmentGrader = ref(false);

// 用户偏好设置
const userPreferences = reactive({
  autoSave: true,
  showSuggestions: true,
  enableVoiceInput: true,
  enableMarkdown: true,
  aiResponseStyle: 'balanced' // 'concise', 'balanced', 'detailed'
});

// 模拟的教师信息
const teacherInfo = reactive({
  name: '李老师',
  subject: '数学',
  grade: '高中一年级',
  currentUnit: '函数与导数',
  avatar: 'https://randomuser.me/api/portraits/women/44.jpg'
});

// 初始化
onMounted(() => {
  loadSavedResources();
  loadUserPreferences();
});

// 加载已保存资源
const loadSavedResources = async () => {
  loading.value = true;
  
  try {
    // 模拟API延迟
    await new Promise(resolve => setTimeout(resolve, 500));
    
    // 模拟数据
    savedResources.value = [
      {
        id: '1',
        title: '函数极值 - 教案',
        type: 'lesson',
        createdAt: new Date(2023, 9, 15),
        content: '# 函数极值 - 教案\n\n## 基本信息\n- **学科**: 数学\n- **年级**: 高中一年级\n- **课时**: 45分钟\n- **教学主题**: 函数极值\n\n## 教学目标\n1. 理解函数极值的概念和几何意义\n2. 掌握求函数极值的方法\n3. 能够应用极值解决实际问题',
        tags: ['函数', '极值', '教学设计']
      },
      {
        id: '2',
        title: '二次函数 - 习题集',
        type: 'questions',
        createdAt: new Date(2023, 9, 10),
        content: '# 二次函数 - 习题集\n\n## 基本信息\n- **学科**: 数学\n- **主题**: 二次函数\n- **适用年级**: 高中一年级\n- **难度**: 中等\n- **题目数量**: 10题',
        tags: ['二次函数', '习题', '测试']
      },
      {
        id: '3',
        title: '导数应用 - 教案',
        type: 'lesson',
        createdAt: new Date(2023, 9, 5),
        content: '# 导数应用 - 教案\n\n## 基本信息\n- **学科**: 数学\n- **年级**: 高中一年级\n- **课时**: 45分钟\n- **教学主题**: 导数应用',
        tags: ['导数', '应用', '教学设计']
      },
      {
        id: '4',
        title: '函数与导数 - 课件',
        type: 'presentation',
        createdAt: new Date(2023, 10, 2),
        content: '# 函数与导数 - 课件\n\n## 课件内容\n- 函数概念复习\n- 导数的定义与几何意义\n- 导数公式与计算\n- 导数应用实例',
        tags: ['函数', '导数', '课件']
      },
      {
        id: '5',
        title: '高一数学月考 - 试卷',
        type: 'quiz',
        createdAt: new Date(2023, 10, 15),
        content: '# 高一数学月考 - 试卷\n\n## 试卷结构\n- 选择题：40分\n- 填空题：20分\n- 解答题：40分\n\n## 考察内容\n- 函数基础\n- 三角函数\n- 数列\n- 导数初步',
        tags: ['月考', '试卷', '综合测试']
      }
    ];
  } catch (error) {
    console.error('加载资源失败', error);
  } finally {
    loading.value = false;
  }
};

// 加载用户偏好
const loadUserPreferences = () => {
  // 实际应用中，这里应该从本地存储或API加载
  const savedPrefs = localStorage.getItem('aiAssistantPreferences');
  if (savedPrefs) {
    try {
      const prefs = JSON.parse(savedPrefs);
      Object.assign(userPreferences, prefs);
    } catch (e) {
      console.error('加载偏好设置失败', e);
    }
  }
};

// 保存用户偏好
const saveUserPreferences = () => {
  localStorage.setItem('aiAssistantPreferences', JSON.stringify(userPreferences));
};

// 切换主题色
const changeTheme = (color) => {
  themeColor.value = color;
};
  
// 过滤的资源
const filteredResources = computed(() => {
  if (!searchTerm.value) return savedResources.value;
  
  const term = searchTerm.value.toLowerCase();
  return savedResources.value.filter(r => 
    r.title.toLowerCase().includes(term) || 
    r.tags?.some(tag => tag.toLowerCase().includes(term)) ||
    r.content.toLowerCase().includes(term)
  );
});

// 添加新资源
const addResource = (resource) => {
  const newResource = {
    id: Date.now().toString(),
    createdAt: new Date(),
    ...resource
  };
  
  savedResources.value.unshift(newResource);
};

// 开始生成资源
const startGenerating = (type) => {
  generatingType.value = type;
  activeTab.value = 'generator';
};

// 保存生成的资源
const saveGeneratedResource = (resource) => {
  addResource({
    title: resource.title || `${teacherInfo.subject} - ${resource.type === 'lesson' ? '教案' : '习题集'}`,
    type: resource.type,
    content: resource.content,
    tags: resource.tags || []
  });
  
  activeTab.value = 'library';
  generatingType.value = null;
};

// 查看资源
const viewResource = (resource) => {
  resourceBeingViewed.value = resource;
  activeTab.value = 'viewer';
};

// 删除资源
const deleteResource = (resourceId) => {
  const index = savedResources.value.findIndex(r => r.id === resourceId);
  if (index !== -1) {
    savedResources.value.splice(index, 1);
  }
};

// 编辑资源
const editResource = (resourceId, updatedData) => {
  const index = savedResources.value.findIndex(r => r.id === resourceId);
  if (index !== -1) {
    savedResources.value[index] = {
      ...savedResources.value[index],
      ...updatedData
    };
  }
};

// 从AI对话中更新上下文
const updateContext = (context) => {
  aiContext.value = context;
};

// 格式化日期
const formatDate = (date) => {
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  });
};

// 处理作业反馈保存
const feedbackSaved = (feedback) => {
  console.log('作业反馈已保存:', feedback);
  // 实际应用中这里应该将反馈保存到后端
  
  // 添加到对话中
  aiContext.value += `\n\n批改了${feedback.studentName}的作业，成绩为${feedback.score}分，评语：${feedback.overallComment}`;
  
  // 关闭批改器
  showAssignmentGrader.value = false;
};

// 从推荐器生成资源
const handleRecommendedResource = (recommendations) => {
  console.log('已选择的推荐资源:', recommendations);
  // 实际应用中这里应该生成资源
  
  // 关闭推荐器
  showResourceRecommender.value = false;
  
  // 跳转到资源库
  activeTab.value = 'library';
};

// 监听偏好设置变化
watch(userPreferences, () => {
  saveUserPreferences();
}, { deep: true });
</script>

<template>
  <div class="ai-assistant-container" :class="`theme-${themeColor}`">
    <!-- 顶部导航 -->
    <div class="assistant-header">
      <div class="header-left">
        <h2 class="header-title">
          <svg xmlns="http://www.w3.org/2000/svg" class="header-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
            <path d="M8 10h.01"></path>
            <path d="M12 10h.01"></path>
            <path d="M16 10h.01"></path>
          </svg>
          智能教学助手
        </h2>
      </div>
      <div class="header-right">
        <div class="user-info">
          <span class="user-greeting">{{ teacherInfo.grade }} {{ teacherInfo.subject }}</span>
          <div class="user-avatar">
            <img :src="teacherInfo.avatar" alt="Teacher Avatar">
          </div>
        </div>
      </div>
    </div>

    <!-- 主导航栏 -->
    <div class="main-navigation">
      <div class="nav-tabs">
        <button 
          class="nav-tab" 
          :class="{ active: activeTab === 'chat' }"
          @click="activeTab = 'chat'"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="tab-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
          </svg>
          <span class="tab-text">智能对话</span>
        </button>
        <button 
          class="nav-tab" 
          :class="{ active: activeTab === 'library' }"
          @click="activeTab = 'library'"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="tab-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
          </svg>
          <span class="tab-text">资源库</span>
        </button>
        <button 
          class="nav-tab"
          :class="{ active: showAssignmentGrader }"
          @click="showAssignmentGrader = true; showResourceRecommender = false;"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="tab-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
          </svg>
          <span class="tab-text">智能批改</span>
        </button>
        <button 
          class="nav-tab"
          :class="{ active: showResourceRecommender }"
          @click="showResourceRecommender = true; showAssignmentGrader = false;"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="tab-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          <span class="tab-text">资源推荐</span>
        </button>
        <button 
          class="nav-tab" 
          :class="{ active: activeTab === 'preferences' }"
          @click="activeTab = 'preferences'; showResourceRecommender = false; showAssignmentGrader = false;"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="tab-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
          </svg>
          <span class="tab-text">设置</span>
        </button>
      </div>
      
      <div class="quick-actions">
        <div class="search-box" v-if="activeTab === 'library'">
          <input 
            type="text" 
            v-model="searchTerm" 
            placeholder="搜索资源..." 
            class="search-input"
          />
          <button class="search-button">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </button>
        </div>
        
        <div class="action-buttons" v-if="activeTab === 'library'">
          <button class="action-button" @click="startGenerating('lesson')">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
            </svg>
            <span>创建教案</span>
          </button>
          
          <button class="action-button" @click="startGenerating('questions')">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span>创建习题</span>
          </button>
          
          <button class="action-button" @click="startGenerating('quiz')">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
            </svg>
            <span>创建考试</span>
          </button>
          
          <button class="action-button" @click="startGenerating('presentation')">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 12l3-3 3 3 4-4M8 21l4-4 4 4M3 4h18M4 4h16v12a1 1 0 01-1 1H5a1 1 0 01-1-1V4z" />
            </svg>
            <span>创建课件</span>
          </button>
        </div>
      </div>
    </div>

    <div class="assistant-main">
    <!-- 聊天界面 -->
    <div v-if="activeTab === 'chat' && !showResourceRecommender && !showAssignmentGrader" class="chat-container">
    <ChatWindow 
    :teacher-info="teacherInfo" 
    :initial-context="aiContext"
    @message-sent="updateContext"
    @request-generate="startGenerating"
    />
    </div>

      <!-- 资源库界面 -->
      <div v-else-if="activeTab === 'library'" class="library-container">
        <ResourceLibrary 
          :resources="filteredResources" 
          :loading="loading"
          @view-details="viewResource"
          @edit="editResource"
          @delete="deleteResource"
        />
            </div>
      
      <!-- 资源生成器界面 -->
      <div v-else-if="activeTab === 'generator'" class="generator-container">
        <ResourceGenerator 
          :type="generatingType" 
          :initial-context="aiContext"
          @close="activeTab = 'chat'"
          @save="saveGeneratedResource"
          @back="activeTab = 'library'"
        />
            </div>
      
      <!-- 资源查看器界面 -->
      <div v-else-if="activeTab === 'viewer'" class="viewer-container">
        <ResourceViewer 
          :resource="resourceBeingViewed"
          @close="activeTab = 'library'"
          @edit="editResource"
          @delete="(id) => { deleteResource(id); activeTab = 'library'; }"
        />
          </div>
          
      <!-- 资源推荐器界面 -->
      <div v-if="showResourceRecommender" class="recommender-container">
        <ResourceRecommender
          :teacher-info="teacherInfo"
          :ai-context="aiContext"
          :theme-color="themeColor"
          @generate-resource="handleRecommendedResource"
          @view-resource="viewResource"
          @close="showResourceRecommender = false"
        />
      </div>
      
      <!-- 作业批改界面 -->
      <div v-if="showAssignmentGrader" class="grader-container">
        <AssignmentGrader
          :teacher-info="teacherInfo"
          :theme-color="themeColor"
          @save-feedback="feedbackSaved"
          @close="showAssignmentGrader = false"
        />
      </div>
      
      <!-- 设置界面 -->
      <div v-else-if="activeTab === 'preferences' && !showResourceRecommender && !showAssignmentGrader" class="preferences-container">
        <div class="preferences-card">
          <h3 class="preferences-title">个性化设置</h3>
          
          <div class="preferences-section">
            <h4 class="section-title">基本设置</h4>
            
            <div class="preference-item">
              <div class="preference-label">
                <span>自动保存对话</span>
                <span class="preference-description">自动保存与AI的对话历史</span>
              </div>
              <label class="toggle-switch">
                <input type="checkbox" v-model="userPreferences.autoSave">
                <span class="toggle-slider"></span>
              </label>
            </div>
            
            <div class="preference-item">
              <div class="preference-label">
                <span>显示聊天建议</span>
                <span class="preference-description">在对话界面显示问题建议</span>
              </div>
              <label class="toggle-switch">
                <input type="checkbox" v-model="userPreferences.showSuggestions">
                <span class="toggle-slider"></span>
              </label>
            </div>
            
            <div class="preference-item">
              <div class="preference-label">
                <span>启用语音输入</span>
                <span class="preference-description">允许使用麦克风输入问题</span>
          </div>
              <label class="toggle-switch">
                <input type="checkbox" v-model="userPreferences.enableVoiceInput">
                <span class="toggle-slider"></span>
              </label>
        </div>

            <div class="preference-item">
              <div class="preference-label">
                <span>Markdown格式</span>
                <span class="preference-description">支持在对话中使用Markdown格式</span>
              </div>
              <label class="toggle-switch">
                <input type="checkbox" v-model="userPreferences.enableMarkdown">
                <span class="toggle-slider"></span>
              </label>
            </div>
          </div>
          
          <div class="preferences-section">
            <h4 class="section-title">AI 响应风格</h4>
            
            <div class="radio-group">
              <div class="radio-item">
                <input 
                  type="radio" 
                  id="concise" 
                  value="concise" 
                  v-model="userPreferences.aiResponseStyle"
            >
                <label for="concise">
                  <div class="radio-title">简洁</div>
                  <div class="radio-description">简短直接的回答，适合快速获取信息</div>
                </label>
        </div>

              <div class="radio-item">
                <input 
                  type="radio" 
                  id="balanced" 
                  value="balanced" 
                  v-model="userPreferences.aiResponseStyle"
                >
                <label for="balanced">
                  <div class="radio-title">平衡</div>
                  <div class="radio-description">适当详细的回答，平衡信息量和长度</div>
                </label>
              </div>
              
              <div class="radio-item">
                <input 
                  type="radio" 
                  id="detailed" 
                  value="detailed" 
                  v-model="userPreferences.aiResponseStyle"
                >
                <label for="detailed">
                  <div class="radio-title">详细</div>
                  <div class="radio-description">全面详尽的回答，包含更多解释和示例</div>
                </label>
              </div>
            </div>
          </div>
          
          <div class="preferences-section">
            <h4 class="section-title">界面主题</h4>
            
            <div class="theme-selector">
              <button 
                class="theme-option" 
                :class="{ active: themeColor === 'blue' }" 
                @click="changeTheme('blue')"
              >
                <span class="color-circle bg-blue-600"></span>
                <span>蓝色</span>
              </button>
              
              <button 
                class="theme-option" 
                :class="{ active: themeColor === 'purple' }" 
                @click="changeTheme('purple')"
              >
                <span class="color-circle bg-purple-600"></span>
                <span>紫色</span>
              </button>
              
              <button 
                class="theme-option" 
                :class="{ active: themeColor === 'teal' }" 
                @click="changeTheme('teal')"
              >
                <span class="color-circle bg-teal-600"></span>
                <span>青色</span>
              </button>
              
              <button 
                class="theme-option" 
                :class="{ active: themeColor === 'green' }" 
                @click="changeTheme('green')"
              >
                <span class="color-circle bg-green-600"></span>
                <span>绿色</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 主容器 */
.ai-assistant-container {
  @apply flex flex-col h-full bg-gray-50;
  --primary-color: theme('colors.blue.600');
  --primary-dark: theme('colors.blue.700');
  --primary-light: theme('colors.blue.100');
  --primary-bg: theme('colors.blue.50');
  min-height: calc(100vh - 16px);
  max-width: 100%;
  margin: 0;
  border-radius: 0;
}

/* 主题变体 */
.ai-assistant-container.theme-blue {
  --primary-color: theme('colors.blue.600');
  --primary-dark: theme('colors.blue.700');
  --primary-light: theme('colors.blue.100');
  --primary-bg: theme('colors.blue.50');
}

.ai-assistant-container.theme-purple {
  --primary-color: theme('colors.purple.600');
  --primary-dark: theme('colors.purple.700');
  --primary-light: theme('colors.purple.100');
  --primary-bg: theme('colors.purple.50');
}

.ai-assistant-container.theme-teal {
  --primary-color: theme('colors.teal.600');
  --primary-dark: theme('colors.teal.700');
  --primary-light: theme('colors.teal.100');
  --primary-bg: theme('colors.teal.50');
}

.ai-assistant-container.theme-green {
  --primary-color: theme('colors.green.600');
  --primary-dark: theme('colors.green.700');
  --primary-light: theme('colors.green.100');
  --primary-bg: theme('colors.green.50');
}

/* 头部导航 */
.assistant-header {
  @apply flex justify-between items-center bg-white px-4 md:px-6 py-3 shadow-sm;
  border-bottom: 1px solid #f0f0f0;
}

.header-left {
  @apply flex items-center;
}

.header-title {
  @apply text-base md:text-lg font-medium text-gray-800 flex items-center;
}

.header-icon {
  @apply h-5 w-5 md:h-6 md:w-6 mr-2;
  color: var(--primary-color);
}

.header-right {
  @apply flex items-center;
}

.user-info {
  @apply flex items-center;
}

.user-greeting {
  @apply mr-3 text-xs md:text-sm text-gray-600 hidden sm:block;
}

.user-avatar {
  @apply w-7 h-7 md:w-8 md:h-8 rounded-full overflow-hidden;
  box-shadow: 0 0 0 2px var(--primary-color);
}

.user-avatar img {
  @apply w-full h-full object-cover;
}

/* 主导航 */
.main-navigation {
  @apply bg-white px-2 md:px-6 py-2 flex flex-wrap justify-between items-center gap-2 sticky top-0 z-10;
  border-bottom: 1px solid #f0f0f0;
}

.nav-tabs {
  @apply flex flex-wrap space-x-0 md:space-x-1 w-full md:w-auto;
}

.nav-tab {
  @apply px-2 md:px-4 py-2 rounded-md text-gray-600 flex items-center transition-colors text-xs md:text-sm flex-1 md:flex-initial justify-center;
}

.nav-tab:hover {
  @apply bg-gray-100;
}

.nav-tab.active {
  @apply font-medium;
  color: var(--primary-color);
  background-color: var(--primary-bg);
}

.tab-icon {
  @apply h-4 w-4 md:h-5 md:w-5 md:mr-2 mr-0;
}

.tab-text {
  @apply hidden md:block;
}

.quick-actions {
  @apply flex flex-wrap items-center space-x-2 w-full md:w-auto justify-center md:justify-end mt-2 md:mt-0;
}

.search-box {
  @apply relative flex items-center w-full md:w-auto mb-2 md:mb-0;
}

.search-input {
  @apply pl-3 pr-10 py-1.5 border rounded-lg text-sm focus:outline-none focus:ring-2 w-full md:w-64 transition-all;
  border-color: var(--primary-color);
  focus-ring-color: var(--primary-color);
}

.search-button {
  @apply absolute right-1 p-1 rounded-full text-gray-500;
}

.action-buttons {
  @apply flex flex-wrap justify-center md:justify-start gap-2;
}

.action-button {
  @apply flex items-center px-2 md:px-3 py-1.5 rounded-md text-xs md:text-sm border transition-colors;
  color: var(--primary-color);
  border-color: var(--primary-color);
}

.action-button svg {
  @apply mr-1 md:mr-1.5 h-4 w-4;
}

.action-button:hover {
  color: white;
  background-color: var(--primary-color);
}

/* 主内容区域 */
.assistant-main {
  @apply flex-1 overflow-hidden;
  height: calc(100vh - 140px);
}

/* 聊天容器 */
.chat-container {
  @apply h-full;
}

/* 资源库容器 */
.library-container {
  @apply h-full;
}

/* 资源生成器容器 */
.generator-container {
  @apply h-full;
}

/* 资源查看器容器 */
.viewer-container {
  @apply h-full;
}

/* 设置界面 */
.preferences-container {
  @apply p-4 md:p-6 h-full overflow-auto;
  background-color: #f9fafb;
}

.preferences-card {
  @apply bg-white rounded-xl shadow-sm p-4 md:p-6 max-w-3xl mx-auto;
}

.preferences-title {
  @apply text-lg md:text-xl font-semibold text-gray-800 mb-4 md:mb-6;
}

.preferences-section {
  @apply mb-6 md:mb-8;
}

.section-title {
  @apply text-sm md:text-base font-medium text-gray-700 mb-3 md:mb-4;
}

.preference-item {
  @apply flex justify-between items-center py-3 border-b border-gray-100;
}

.preference-label {
  @apply flex flex-col;
}

.preference-description {
  @apply text-xs text-gray-500 mt-1;
}

.toggle-switch {
  @apply relative inline-flex items-center cursor-pointer;
}

.toggle-switch input {
  @apply sr-only;
}

.toggle-slider {
  @apply w-11 h-6 bg-gray-300 rounded-full transition-colors;
}

.toggle-slider:before {
  content: "";
  @apply absolute left-1 top-1 bg-white w-4 h-4 rounded-full transition-transform;
}

.toggle-switch input:checked + .toggle-slider {
  background-color: var(--primary-color);
}

.toggle-switch input:checked + .toggle-slider:before {
  @apply transform translate-x-5;
}

.radio-group {
  @apply space-y-3;
}

.radio-item {
  @apply flex items-start;
}

.radio-item input {
  @apply mt-1 mr-2;
}

.radio-item label {
  @apply flex flex-col;
}

.radio-title {
  @apply font-medium text-gray-700;
}

.radio-description {
  @apply text-xs text-gray-500 mt-1;
}

.theme-selector {
  @apply flex flex-wrap gap-2;
}

.theme-option {
  @apply flex items-center px-4 py-2 border rounded-md transition-colors;
}

.theme-option.active {
  border-color: var(--primary-color);
  background-color: var(--primary-bg);
}

.color-circle {
  @apply w-4 h-4 rounded-full mr-2;
}

.bg-blue-600 {
  background-color: theme('colors.blue.600');
}

.bg-purple-600 {
  background-color: theme('colors.purple.600');
}

.bg-teal-600 {
  background-color: theme('colors.teal.600');
}

.bg-green-600 {
  background-color: theme('colors.green.600');
}

/* 动画效果 */
@keyframes fade-in {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.animate-fade-in {
  animation: fade-in 0.3s ease-out forwards;
}

/* 响应式调整 */
@media (max-width: 640px) {
  .action-buttons {
    @apply grid grid-cols-2 gap-2 w-full;
  }
  
  .action-button {
    @apply justify-center;
  }
  
  .preferences-section {
    @apply mb-4;
  }
}
</style>