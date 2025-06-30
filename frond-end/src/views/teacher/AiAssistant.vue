<script setup>
import {nextTick, onMounted, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import BaseCard from '@/components/common/BaseCard.vue';
import {Bot, Menu, MessageSquare, Plus, Send, Trash2, User, X} from 'lucide-vue-next';
import {marked} from 'marked';
import DOMPurify from 'dompurify';

// 历史会话列表
const conversationHistory = ref([
  {
    id: '1',
    title: '教学设计方法',
    createdAt: '2023-06-10',
    preview: '如何设计一堂生动的课程？'
  },
  {
    id: '2',
    title: '班级管理技巧',
    createdAt: '2023-06-12',
    preview: '有哪些提高学生参与度的方法？'
  },
  {
    id: '3',
    title: '教学资源推荐',
    createdAt: '2023-06-15',
    preview: '请推荐一些教学资源'
  }
]);

// 当前会话
const currentConversation = ref({
  id: 'new',
  messages: [
    {
      role: 'assistant',
      content: '您好！我是您的智能教学助手，有什么可以帮助您的吗？',
      time: new Date().toLocaleTimeString()
    }
  ]
});

// 用户输入
const userInput = ref('');
const inputTextarea = ref(null);

// 是否正在加载回复
const loading = ref(false);

// 是否显示历史对话侧边栏 (默认在移动端隐藏)
const showSidebar = ref(window.innerWidth > 768);

// 自动调整文本域高度
const adjustTextareaHeight = () => {
  if (!inputTextarea.value) return;
  inputTextarea.value.style.height = 'auto';
  inputTextarea.value.style.height = `${Math.min(inputTextarea.value.scrollHeight, 150)}px`;
};

// 监听输入变化，自动调整高度
watch(userInput, () => {
  nextTick(adjustTextareaHeight);
});

// 将Markdown转换为HTML
const renderMarkdown = (text) => {
  if (!text) return '';
  const rawHtml = marked(text);
  return DOMPurify.sanitize(rawHtml);
};

// 发送消息
const sendMessage = async () => {
  if (!userInput.value.trim()) return;
  
  // 添加用户消息到对话历史
  currentConversation.value.messages.push({
    role: 'user',
    content: userInput.value,
    time: new Date().toLocaleTimeString()
  });
  
  // 清空输入框
  const userQuestion = userInput.value;
  userInput.value = '';
  
  // 重置输入框高度
  if (inputTextarea.value) {
    inputTextarea.value.style.height = 'auto';
  }
  
  // 显示加载状态
  loading.value = true;
  
  try {
    // 模拟API请求延迟
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    // 根据问题生成回答
    let response = '';
    
    if (userQuestion.includes('课程') || userQuestion.includes('教学')) {
      response = '设计生动的课程可以考虑以下几点：\n\n**1. 明确学习目标**\n设定清晰、具体、可衡量的学习目标，让学生知道他们将要学习什么。\n\n**2. 增加互动环节**\n设计问答、小组讨论、角色扮演等互动活动，提高学生参与度。\n\n**3. 使用多样化的教学媒体**\n结合视频、图片、音频等多媒体资源，丰富教学内容。\n\n**4. 设计情景化的教学案例**\n将知识点融入真实场景，增强学习的关联性和应用性。\n\n**5. 适当加入小组讨论和合作学习**\n培养学生的团队协作能力和批判性思维。';
    } else if (userQuestion.includes('学生') || userQuestion.includes('管理')) {
      response = '提高学生参与度的方法包括：\n\n**1. 设计开放性问题**\n提出没有标准答案的问题，鼓励学生思考和表达。\n\n**2. 使用小组合作学习**\n将学生分组完成任务，促进相互学习和支持。\n\n**3. 采用游戏化教学**\n引入竞赛、积分、徽章等游戏元素，增加学习乐趣。\n\n**4. 建立积分奖励机制**\n对积极参与的学生给予适当的奖励和肯定。\n\n**5. 创设真实的问题情境**\n提供与实际生活相关的问题，增强学习动机。';
    } else if (userQuestion.includes('资源') || userQuestion.includes('材料')) {
      response = '您可以在以下平台获取优质教学资源：\n\n**1. [国家教育资源公共服务平台](https://www.eduyun.cn/)**\n提供各学科教学资源和教育应用。\n\n**2. [学科网](https://www.zxxk.com/)**\n丰富的教案、课件、试题资源。\n\n**3. [教师教育网](https://www.teacherln.com/)**\n教师专业发展和教学研究资源。\n\n**4. [智慧教育平台](https://www.smartedu.cn/)**\n数字化教学工具和资源整合平台。\n\n**5. 各大出版社教育资源网站**\n如人教社、北师大出版社等提供的配套资源。';
    } else {
      response = '感谢您的提问！这是一个很好的问题。建议您可以：\n\n**1. 参考相关教育理论**\n了解布鲁姆分类法、建构主义等教育理论，指导教学实践。\n\n**2. 结合学生特点进行分析**\n考虑学生的认知水平、学习风格和兴趣爱好。\n\n**3. 尝试多样化的教学方法**\n如翻转课堂、项目式学习、探究式学习等。\n\n**4. 与同行交流经验**\n参加教研活动，分享和学习优秀教学经验。\n\n**5. 持续反思和改进**\n定期评估教学效果，不断调整和优化教学策略。';
    }
    
    // 添加助手回复到对话历史
    currentConversation.value.messages.push({
      role: 'assistant',
      content: response,
      time: new Date().toLocaleTimeString()
    });

    // 如果是新会话，创建新的会话记录
    if (currentConversation.value.id === 'new' && currentConversation.value.messages.length > 2) {
      const newId = Date.now().toString();
      const newTitle = userQuestion.length > 20 ? userQuestion.substring(0, 20) + '...' : userQuestion;
      
      conversationHistory.value.unshift({
        id: newId,
        title: newTitle,
        createdAt: new Date().toLocaleDateString(),
        preview: userQuestion
      });
      
      currentConversation.value.id = newId;
      currentConversation.value.title = newTitle;
    }
  } catch (error) {
    ElMessage.error('获取回复失败，请稍后再试');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 开始新对话
const startNewChat = () => {
  currentConversation.value = {
    id: 'new',
    messages: [
      {
        role: 'assistant',
        content: '您好！我是您的智能教学助手，有什么可以帮助您的吗？',
        time: new Date().toLocaleTimeString()
      }
    ]
  };
  
  // 在移动端模式下，开始新对话后隐藏侧边栏
  if (window.innerWidth <= 768) {
    showSidebar.value = false;
  }
};

// 加载历史对话
const loadConversation = (conversation) => {
  // 模拟加载历史对话
  currentConversation.value = {
    id: conversation.id,
    title: conversation.title,
    messages: [
      {
        role: 'assistant',
        content: '您好！我是您的智能教学助手，有什么可以帮助您的吗？',
        time: new Date().toLocaleTimeString()
      },
      {
        role: 'user',
        content: conversation.preview,
        time: new Date().toLocaleTimeString()
      },
      {
        role: 'assistant',
        content: '以下是我的回答...\n\n**1. 第一点**\n这是第一点的详细解释，可能包含一些专业知识和建议。\n\n**2. 第二点**\n这是第二点的内容，提供了具体的方法和步骤。\n\n**3. 第三点**\n这是第三点的补充信息，可能包含一些实用的资源链接和工具推荐。',
        time: new Date().toLocaleTimeString()
      }
    ]
  };
  
  // 在移动端模式下，选择历史对话后隐藏侧边栏
  if (window.innerWidth <= 768) {
    showSidebar.value = false;
  }
};

// 删除历史对话
const deleteConversation = (id, event) => {
  event.stopPropagation();
  conversationHistory.value = conversationHistory.value.filter(conv => conv.id !== id);
  
  // 如果删除的是当前对话，开始新对话
  if (currentConversation.value.id === id) {
    startNewChat();
  }
  
  ElMessage.success('对话已删除');
};

// 切换侧边栏显示
const toggleSidebar = () => {
  showSidebar.value = !showSidebar.value;
};

// 监听回车键发送消息
const handleKeyDown = (e) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault();
    sendMessage();
  } else if (e.key === 'Enter' && e.shiftKey) {
    // 换行
  }
};

// 自动滚动到最新消息
const chatContainer = ref(null);
const scrollToBottom = () => {
  if (chatContainer.value) {
    setTimeout(() => {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
    }, 100);
  }
};

// 监听对话历史变化，自动滚动
watch(() => currentConversation.value.messages, scrollToBottom, { deep: true });

// 监听窗口大小变化
onMounted(() => {
  scrollToBottom();
  adjustTextareaHeight();
  
  window.addEventListener('resize', () => {
    if (window.innerWidth > 768) {
      showSidebar.value = true;
    } else {
      showSidebar.value = false;
    }
  });
});
</script>

<template>
  <div class="ai-assistant-container">
    <div class="chat-layout">
      <!-- 侧边栏: 历史对话 -->
      <div class="sidebar" :class="{ 'sidebar-hidden': !showSidebar }">
        <div class="sidebar-header">
          <h2 class="text-lg font-medium text-primary-700">智能教学助手</h2>
        </div>
        
        <button @click="startNewChat" class="new-chat-button">
          <Plus class="w-4 h-4 mr-2" />
          新对话
        </button>
        
        <div class="history-list">
          <div 
            v-for="conversation in conversationHistory" 
            :key="conversation.id"
            class="history-item"
            :class="{ 'active': currentConversation.id === conversation.id }"
            @click="loadConversation(conversation)"
          >
            <div class="history-avatar">
              <MessageSquare class="w-4 h-4" />
            </div>
            <div class="history-content">
              <div class="history-title">{{ conversation.title }}</div>
              <div class="history-date">{{ conversation.createdAt }}</div>
            </div>
            <button class="history-delete" @click="deleteConversation(conversation.id, $event)">
              <Trash2 class="w-4 h-4" />
            </button>
          </div>
        </div>
        
        <div class="sidebar-footer">
          <div class="text-xs text-gray-500 px-4 py-2">
            智能助手 v1.0
          </div>
        </div>
      </div>

      <!-- 主聊天区域 -->
      <div class="main-chat">
        <!-- 移动端顶栏 -->
        <div class="mobile-header">
          <button @click="toggleSidebar" class="menu-button">
            <Menu v-if="!showSidebar" class="w-5 h-5" />
            <X v-else class="w-5 h-5" />
          </button>
          <div class="mobile-title">智能教学助手</div>
          <button @click="startNewChat" class="mobile-new-chat">
            <Plus class="w-5 h-5" />
          </button>
        </div>
        
        <!-- 对话内容区域 -->
        <div class="chat-messages" ref="chatContainer">
          <div class="messages-container">
            <div 
              v-for="(message, index) in currentConversation.messages" 
              :key="index" 
              :class="[
                'message-item', 
                message.role === 'user' ? 'user-message' : 'assistant-message'
              ]"
            >
              <div class="message-role">
                <div class="role-avatar" :class="message.role === 'user' ? 'user-avatar' : 'assistant-avatar'">
                  <User v-if="message.role === 'user'" class="w-4 h-4" />
                  <Bot v-else class="w-4 h-4" />
                </div>
                <div class="message-time">{{ message.time }}</div>
              </div>
              <div class="message-content" :class="message.role === 'user' ? 'user-bubble' : 'assistant-bubble'">
                <div v-if="message.role === 'user'">{{ message.content }}</div>
                <div v-else v-html="renderMarkdown(message.content)" class="markdown-content"></div>
              </div>
            </div>
            
            <!-- 加载指示器 -->
            <div v-if="loading" class="message-item assistant-message loading-message">
              <div class="message-role">
                <div class="role-avatar assistant-avatar">
                  <Bot class="w-4 h-4" />
                </div>
                <div class="message-time">{{ new Date().toLocaleTimeString() }}</div>
              </div>
              <div class="message-content assistant-bubble">
                <div class="typing-container">
                  <div class="typing-indicator">
                    <div class="dot"></div>
                    <div class="dot"></div>
                    <div class="dot"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 输入区域 -->
        <div class="chat-input-area">
          <BaseCard shadow="always" padding="small" class="input-card">
            <div class="input-container">
              <textarea
                v-model="userInput"
                placeholder="输入您的问题或需求..."
                @keydown="handleKeyDown"
                class="chat-textarea"
                rows="1"
                ref="inputTextarea"
              ></textarea>
              <button 
                @click="sendMessage"
                :disabled="!userInput.trim() || loading"
                class="send-button"
                :class="{ 'send-button-active': userInput.trim() }"
              >
                <Send class="w-5 h-5" />
              </button>
            </div>
            <div class="input-footer">
              <p class="disclaimer">智能助手可能会产生错误信息，请以教学大纲为准。按 Shift + Enter 换行</p>
            </div>
          </BaseCard>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ai-assistant-container {
  @apply h-full w-full;
  background-color: #f8fafc;
  padding: 0.75rem;
  max-width: 100%;
  margin: 0 auto;
}

.chat-layout {
  @apply flex h-full w-full relative;
  height: calc(100vh - 70px);
  overflow: hidden;
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
}

/* 侧边栏样式 */
.sidebar {
  @apply flex flex-col h-full bg-white border-r border-gray-200 transition-all duration-300 ease-in-out z-30;
  width: 280px;
}

.sidebar-hidden {
  @apply -ml-64;
  width: 0;
}

.sidebar-header {
  @apply p-3 border-b border-gray-200 flex justify-between items-center;
  height: 60px;
}

.new-chat-button {
  @apply flex items-center justify-center w-full my-2 mx-2 py-2 px-3 bg-primary-600 hover:bg-primary-700 text-white rounded-full text-sm transition-colors duration-200 font-medium;
}

.history-list {
  @apply flex-1 overflow-y-auto py-1 px-2;
}

.history-item {
  @apply flex items-center p-3 rounded-xl cursor-pointer hover:bg-gray-100 transition-colors duration-200 mb-1;
}

.history-item.active {
  @apply bg-primary-50 border-l-4 border-primary-600;
}

.history-avatar {
  @apply flex items-center justify-center w-8 h-8 rounded-full bg-primary-100 text-primary-600 mr-3 flex-shrink-0;
}

.history-content {
  @apply flex-1 overflow-hidden mr-1;
}

.history-title {
  @apply text-sm font-medium truncate text-gray-700;
}

.history-date {
  @apply text-xs text-gray-500 mt-0.5;
}

.history-delete {
  @apply text-gray-400 hover:text-danger-500 opacity-0 transition-opacity duration-200 p-1 rounded-md hover:bg-danger-50;
}

.history-item:hover .history-delete {
  @apply opacity-100;
}

.sidebar-footer {
  @apply mt-auto border-t border-gray-200 py-2;
}

/* 主聊天区域样式 */
.main-chat {
  @apply flex-1 flex flex-col h-full bg-white;
  width: calc(100% - 280px);
}

.mobile-header {
  @apply hidden items-center px-3 py-2 border-b border-gray-200 bg-white;
  height: 60px;
}

.menu-button, .mobile-new-chat {
  @apply p-1.5 text-gray-600 hover:bg-gray-100 rounded-md;
}

.mobile-title {
  @apply flex-1 text-center font-medium text-primary-700;
}

/* 消息区域样式 */
.chat-messages {
  @apply flex-1 overflow-y-auto;
  background-color: #f8fafc;
  scroll-behavior: smooth;
  padding: 1rem 0;
}

.messages-container {
  @apply w-full mx-auto px-4 py-3;
  max-width: 95%;
}

.message-item {
  @apply mb-6 clear-both;
}

.message-role {
  @apply flex items-center mb-2;
}

.role-avatar {
  @apply w-10 h-10 rounded-full flex items-center justify-center text-sm font-medium text-white;
}

.user-avatar {
  @apply bg-primary-600;
}

.assistant-avatar {
  @apply bg-secondary-600;
}

.message-time {
  @apply text-xs text-gray-500 ml-2;
}

.user-message .message-role {
  @apply justify-end;
}

.user-message .message-time {
  @apply mr-2 ml-0;
}

.message-content {
  @apply px-6 py-4 rounded-2xl whitespace-pre-line text-sm max-w-[90%] clear-both;
  line-height: 1.6;
}

.user-bubble {
  @apply bg-primary-600 text-white float-right rounded-tr-none shadow-soft-sm;
  min-width: 120px;
}

.assistant-bubble {
  @apply bg-white text-gray-800 float-left rounded-tl-none shadow-soft-sm border border-gray-200;
  min-width: 120px;
}

.markdown-content {
  @apply max-w-none;
}

.markdown-content :deep(p) {
  @apply my-2;
}

.markdown-content :deep(h1) {
  @apply text-xl font-bold my-3;
}

.markdown-content :deep(h2) {
  @apply text-lg font-bold my-2;
}

.markdown-content :deep(h3) {
  @apply text-base font-bold my-2;
}

.markdown-content :deep(ul), .markdown-content :deep(ol) {
  @apply my-2 pl-6;
}

.markdown-content :deep(ul) {
  @apply list-disc;
}

.markdown-content :deep(ol) {
  @apply list-decimal;
}

.markdown-content :deep(li) {
  @apply my-1;
}

.markdown-content :deep(strong) {
  @apply font-semibold text-gray-900;
}

.markdown-content :deep(a) {
  @apply text-primary-600 hover:text-primary-700 underline;
}

.markdown-content :deep(blockquote) {
  @apply pl-4 border-l-4 border-gray-300 italic my-2;
}

.markdown-content :deep(code) {
  @apply bg-gray-100 rounded px-1 py-0.5 text-sm;
}

.markdown-content :deep(pre) {
  @apply bg-gray-100 p-3 rounded my-3 overflow-x-auto;
}

.markdown-content :deep(pre code) {
  @apply bg-transparent p-0;
}

.markdown-content :deep(hr) {
  @apply my-4 border-t border-gray-200;
}

.loading-message .message-content {
  @apply bg-white float-left;
}

.typing-container {
  @apply min-w-[60px] flex items-center;
}

/* 输入区域样式 */
.chat-input-area {
  @apply p-4 bg-white sticky bottom-0;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}

.input-card {
  @apply overflow-visible;
  max-width: 95%;
  margin: 0 auto;
}

.input-container {
  @apply relative;
}

.chat-textarea {
  @apply w-full py-4 px-5 pr-14 border border-gray-300 rounded-full outline-none resize-none transition-all text-sm;
  min-height: 50px;
  max-height: 150px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.chat-textarea:focus {
  @apply border-primary-500 ring-1 ring-primary-200;
}

.send-button {
  @apply absolute right-4 bottom-3 p-2 rounded-full text-gray-400 hover:bg-gray-100 transition-colors duration-200 disabled:opacity-50 disabled:cursor-not-allowed;
}

.send-button-active {
  @apply text-primary-600 hover:bg-primary-50;
}

.input-footer {
  @apply mt-2;
}

.disclaimer {
  @apply text-xs text-gray-500 text-center;
}

.typing-indicator {
  @apply flex space-x-1 py-1;
}

.dot {
  @apply w-1.5 h-1.5 bg-gray-400 rounded-full;
  animation: bounce 1.4s infinite ease-in-out;
  animation-fill-mode: both;
}

.dot:nth-child(1) {
  animation-delay: -0.32s;
}

.dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes bounce {
  0%, 80%, 100% { 
    transform: scale(0);
  } 40% { 
    transform: scale(1.0);
  }
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .ai-assistant-container {
    padding: 0.5rem;
  }
  
  .chat-layout {
    height: calc(100vh - 70px);
  }
}

@media (max-width: 768px) {
  .mobile-header {
    @apply flex;
  }
  
  .sidebar {
    @apply fixed top-0 left-0 z-40 h-full shadow-lg;
  }
  
  .sidebar-hidden {
    @apply -ml-64;
    width: 0;
  }
  
  .main-chat {
    width: 100%;
  }
  
  .chat-input-area {
    @apply pb-6 px-3;
  }
  
  .messages-container {
    @apply px-3;
  }
  
  .ai-assistant-container {
    padding: 0;
  }
  
  .chat-layout {
    height: calc(100vh - 60px);
    border-radius: 0;
    box-shadow: none;
  }
}

/* 添加暗色模式支持 */
@media (prefers-color-scheme: dark) {
  .ai-assistant-container {
    background-color: #111827;
  }
  
  .sidebar, .main-chat {
    @apply bg-gray-900;
  }
  
  .sidebar-header, .mobile-header {
    @apply bg-gray-900 border-gray-700;
  }
  
  .history-item {
    @apply hover:bg-gray-800;
  }
  
  .history-item.active {
    @apply bg-gray-800 border-primary-500;
  }
  
  .history-title {
    @apply text-gray-200;
  }
  
  .chat-messages {
    background-color: #111827;
  }
  
  .assistant-bubble {
    @apply bg-gray-800 text-gray-200 border-gray-700;
  }
  
  .chat-textarea {
    @apply bg-gray-800 border-gray-700 text-gray-200;
  }
  
  .chat-textarea:focus {
    @apply border-primary-500 ring-primary-900;
  }
  
  .send-button-active {
    @apply hover:bg-gray-700;
  }
  
  .markdown-content :deep(strong) {
    @apply text-gray-200;
  }
}
</style> 