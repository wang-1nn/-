<script setup>
import {nextTick, onMounted, ref, watch, computed} from 'vue';
import {ElMessage} from 'element-plus';
import BaseCard from '@/components/common/BaseCard.vue';
import {Bot, Menu, MessageSquare, Plus, Send, Trash2, User, X} from 'lucide-vue-next';
import {marked} from 'marked';
import DOMPurify from 'dompurify';
import { post, get } from '@/net';

// 用户ID，实际应用中应从登录状态或路由参数获取
const userId = ref('1'); // 假设当前教师ID为1

// 历史会话列表
const conversationHistory = ref([]);

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

// 计算属性：检查当前会话中是否有空的助手消息
const hasEmptyAssistantMessage = computed(() => {
  return currentConversation.value.messages.some(message => 
    message.role === 'assistant' && message.content === ''
  );
});

// 是否显示历史对话侧边栏 (默认在移动端隐藏)
const showSidebar = ref(window.innerWidth > 768);

// 用户头像URL（可以是用户实际头像或默认头像）
// 这里使用默认头像，实际应用中应从用户配置或服务器获取真实头像
const userAvatarUrl = ref(null); 

// 助手头像URL
const assistantAvatarUrl = ref('/favicon.ico'); // 使用网站图标作为助手头像

// 发送消息动画
const messageSent = ref(false);

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

// 从后端获取聊天历史
const getChatHistory = () => {
  get('/api/teacher/getChatHistory', 
    { uid: userId.value },
    (message, data) => {
      if (data) {
        processHistoryData(data);
      }
    },
    (message) => {
      console.error('获取聊天历史失败:', message);
      ElMessage.error('获取聊天历史失败');
    }
  );
};

// 处理历史数据并转换为会话格式
const processHistoryData = (historyData) => {
  if (!historyData || historyData.length === 0) return;

  // 按照conversationId对消息进行分组
  const conversations = {};
  historyData.forEach(item => {
    if (!conversations[item.conversationId]) {
      conversations[item.conversationId] = {
        id: item.conversationId,
        title: '历史对话',
        createdAt: new Date(item.timestamp).toLocaleDateString(),
        messages: []
      };
    }
    
    conversations[item.conversationId].messages.push({
      role: item.type.toLowerCase(), // 转换为小写(USER -> user, ASSISTANT -> assistant)
      content: item.content,
      time: new Date(item.timestamp).toLocaleTimeString()
    });
  });
  
  // 将分组后的会话转换为数组
  const historyList = Object.values(conversations);
  
  // 为每个会话设置标题和预览内容
  historyList.forEach(conv => {
    // 查找第一条用户消息作为标题和预览
    const firstUserMessage = conv.messages.find(msg => msg.role === 'user');
    if (firstUserMessage) {
      conv.title = firstUserMessage.content.length > 20 ? 
                  firstUserMessage.content.substring(0, 20) + '...' : 
                  firstUserMessage.content;
      conv.preview = firstUserMessage.content;
    }
  });
  
  // 按时间倒序排列
  historyList.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
  
  // 更新历史会话列表
  conversationHistory.value = historyList;
};

// 发送消息
const sendMessage = () => {
  if (!userInput.value.trim()) return;
  
  // 触发发送动画效果
  messageSent.value = true;
  setTimeout(() => {
    messageSent.value = false;
  }, 300);
  
  // 保存用户输入并清空输入框
  const userQuestion = userInput.value;
  userInput.value = '';
  
  // 重置输入框高度
  if (inputTextarea.value) {
    inputTextarea.value.style.height = 'auto';
  }
  
  // 添加用户消息到对话历史
  currentConversation.value.messages.push({
    role: 'user',
    content: userQuestion,
    time: new Date().toLocaleTimeString()
  });
  
  // 显示加载状态，使用思考中指示器
  loading.value = true;
  
  // 滚动到底部以显示用户的消息
  scrollToBottom();

  // 使用原生fetch处理SSE响应
  fetch(`/api/teacher/chat?message=${encodeURIComponent(userQuestion)}&uid=${encodeURIComponent(userId.value)}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({})
  })
  .then(response => {
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    // 标记是否已经添加了助手消息
    let messageAdded = false;
    let accumulatedContent = '';
    
    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    
    function processStream() {
      return reader.read().then(({ done, value }) => {
        if (done) {
          // 如果流结束了但还没有添加过消息（没有收到任何内容）
          if (!messageAdded) {
            currentConversation.value.messages.push({
              role: 'assistant',
              content: '收到您的消息了，但暂时没有回复。请稍后再试。',
              time: new Date().toLocaleTimeString()
            });
          }
          loading.value = false;
          return;
        }
        
        const chunk = decoder.decode(value, { stream: true });
        // 处理SSE数据
        const lines = chunk.split('\n');
        for (let line of lines) {
          if (line.startsWith('data:')) {
            try {
              const textContent = line.substring(5).trim();
              
              // 收到第一块数据时，创建助手消息
              if (!messageAdded) {
                currentConversation.value.messages.push({
                  role: 'assistant',
                  content: '',
                  time: new Date().toLocaleTimeString()
                });
                messageAdded = true;
                // 关闭loading状态，因为我们已经显示了真实的回复框
                loading.value = false;
              }
              
              // 累加内容
              accumulatedContent += textContent;
              
              // 更新助手消息的内容
              const messageIndex = currentConversation.value.messages.length - 1;
              currentConversation.value.messages[messageIndex].content = accumulatedContent;
              
              // 滚动到底部
              scrollToBottom();
            } catch (e) {
              console.error('处理SSE数据失败:', e);
            }
          }
        }
        
        return processStream();
      });
    }
    
    return processStream();
  })
  .then(() => {
    // 如果是新会话，创建会话记录
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
  })
  .catch(error => {
    console.error('获取回复失败:', error);
    ElMessage.error('获取回复失败，请稍后再试');
    
    // 添加错误消息
    currentConversation.value.messages.push({
      role: 'assistant',
      content: '抱歉，获取回复时出现错误，请稍后再试。',
      time: new Date().toLocaleTimeString()
    });
    loading.value = false;
  });
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
  currentConversation.value = JSON.parse(JSON.stringify(conversation));
  
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
      chatContainer.value.scrollTo({
        top: chatContainer.value.scrollHeight,
        behavior: 'smooth'
      });
    }, 100);
  }
};

// 监听对话历史变化，自动滚动
watch(() => currentConversation.value.messages, scrollToBottom, { deep: true });

// 初始化
onMounted(() => {
  scrollToBottom();
  adjustTextareaHeight();
  
  // 获取历史聊天记录
  getChatHistory();
  
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
                  <img 
                    v-if="message.role === 'user'" 
                    :src="userAvatarUrl" 
                    alt="用户头像"
                    class="avatar-image"
                    @error="$event.target.style.display='none'"
                  />
                  <img 
                    v-else-if="assistantAvatarUrl" 
                    :src="assistantAvatarUrl" 
                    alt="助手头像"
                    class="avatar-image"
                    @error="$event.target.style.display='none'"
                  />
                  <User v-if="message.role === 'user' && !userAvatarUrl" class="w-4 h-4" />
                  <Bot v-if="message.role !== 'user' && !assistantAvatarUrl" class="w-4 h-4" />
                </div>
                <div class="message-time">{{ message.time }}</div>
              </div>
              <div 
                class="message-content" 
                :class="[
                  message.role === 'user' ? 'user-bubble' : 'assistant-bubble',
                  message.content ? 'has-content' : ''
                ]"
              >
                <div v-if="message.role === 'user'">{{ message.content }}</div>
                <div v-else v-html="renderMarkdown(message.content)" class="markdown-content"></div>
              </div>
            </div>
            
            <!-- 加载指示器，只有当没有空消息时才显示 -->
            <div v-if="loading && !hasEmptyAssistantMessage" class="message-item assistant-message loading-message">
              <div class="message-role">
                <div class="role-avatar assistant-avatar">
                  <img 
                    v-if="assistantAvatarUrl" 
                    :src="assistantAvatarUrl" 
                    alt="助手头像"
                    class="avatar-image"
                    @error="$event.target.style.display='none'"
                  />
                  <Bot v-else class="w-4 h-4" />
                </div>
                <div class="message-time">{{ new Date().toLocaleTimeString() }}</div>
              </div>
              <div class="message-content assistant-bubble loading-bubble">
                <div class="thinking-container">
                  <span class="thinking-text">思考中</span>
                  <div class="thinking-dots">
                    <span class="dot"></span>
                    <span class="dot"></span>
                    <span class="dot"></span>
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
                :disabled="loading"
              ></textarea>
              <div class="input-actions">
                <button 
                  @click="sendMessage"
                  :disabled="!userInput.trim() || loading"
                  class="send-button"
                  :class="{ 
                    'send-button-active': userInput.trim(),
                    'send-button-pulse': userInput.trim() && !loading,
                    'send-animation': messageSent
                  }"
                  :title="loading ? '正在处理...' : '发送消息'"
                >
                  <Send class="w-5 h-5" />
                </button>
              </div>
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
  animation: fadeIn 0.3s ease-out;
  transition: all 0.3s ease;
}

.user-message {
  transform-origin: right center;
}

.assistant-message {
  transform-origin: left center;
}

.message-role {
  @apply flex items-center mb-2;
}

.role-avatar {
  @apply w-10 h-10 rounded-full flex items-center justify-center text-sm font-medium text-white overflow-hidden;
}

.avatar-image {
  @apply w-full h-full object-cover;
}

.user-avatar {
  @apply bg-primary-600;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.assistant-avatar {
  @apply bg-secondary-600;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
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
  transition: all 0.3s ease-out;
}

.message-content.has-content {
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.user-bubble {
  @apply bg-primary-600 text-white float-right rounded-tr-none shadow-soft-sm;
  min-width: 120px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  border-bottom-right-radius: 4px;
  background-image: linear-gradient(135deg, #4F46E5 0%, #6366F1 100%);
}

.assistant-bubble {
  @apply bg-white text-gray-800 float-left rounded-tl-none shadow-soft-sm border border-gray-200;
  min-width: 120px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  border-bottom-left-radius: 4px;
  border-top: 1px solid rgba(99, 102, 241, 0.1);
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

.loading-message {
  animation: fadeIn 0.3s ease-in-out;
}

.loading-bubble {
  background: linear-gradient(to right, #eef2ff, #dbeafe);
  min-width: 120px;
  border: 1px dashed rgba(99, 102, 241, 0.3);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.03);
  padding: 12px 18px;
  max-width: 160px;
  border-radius: 16px;
  border-top-left-radius: 4px;
  animation: pulse-light 2s infinite ease-in-out;
  display: inline-block;
}

.thinking-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.thinking-text {
  @apply text-sm font-medium;
  color: #4338ca;
  letter-spacing: 0.5px;
  position: relative;
}

.thinking-dots {
  display: flex;
  gap: 6px;
  align-items: center;
}

.dot {
  width: 6px;
  height: 6px;
  background-color: #4338ca;
  border-radius: 50%;
  opacity: 0.7;
  display: inline-block;
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

@keyframes pulse-light {
  0% {
    box-shadow: 0 0 0 0 rgba(99, 102, 241, 0.2);
  }
  70% {
    box-shadow: 0 0 0 8px rgba(99, 102, 241, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(99, 102, 241, 0);
  }
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
  display: flex;
  align-items: center;
}

.chat-textarea {
  @apply w-full py-4 px-5 pr-14 border border-gray-300 rounded-full outline-none resize-none transition-all text-sm;
  min-height: 50px;
  max-height: 150px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  border-radius: 24px;
}

.chat-textarea:focus {
  @apply border-primary-500 ring-1 ring-primary-200;
  transform: translateY(-1px);
}

.chat-textarea:disabled {
  @apply bg-gray-100 cursor-not-allowed;
}

.input-actions {
  position: absolute;
  right: 12px;
  bottom: 50%;
  transform: translateY(50%);
  display: flex;
  align-items: center;
  gap: 8px;
}

.send-button {
  @apply p-2 rounded-full text-gray-400 hover:bg-gray-100 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed;
  transform: scale(1);
  transition: all 0.2s ease;
  height: 38px;
  width: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.send-button:active {
  transform: scale(0.92);
}

.send-button-active {
  @apply text-white bg-primary-600 hover:bg-primary-700;
}

.send-button-pulse {
  animation: pulse 1.5s infinite;
}

.send-animation {
  animation: sendZoom 0.3s ease-out forwards;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(79, 70, 229, 0.4);
  }
  70% {
    box-shadow: 0 0 0 6px rgba(79, 70, 229, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(79, 70, 229, 0);
  }
}

@keyframes sendZoom {
  0% { transform: scale(1); }
  50% { transform: scale(0.85); opacity: 0.8; }
  100% { transform: scale(1); }
}

.input-footer {
  @apply mt-2;
}

.disclaimer {
  @apply text-xs text-gray-500 text-center;
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