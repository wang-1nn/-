<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue';
import { post, get } from '@/net';
import { useAuthStore } from '@/stores/counter.js';
import LucideIcon from '@/components/icons/LucideIcon.vue';

// 认证信息
const auth = useAuthStore();
const user = computed(() => auth.user);

// 聊天历史
const chatHistory = ref([]);
const loading = ref(false);
const messageInput = ref('');
const chatContainer = ref(null);

// 常见问题
const commonQuestions = [
  '如何提高数学学习效率？',
  '请推荐一些适合我学习的英语资料',
  '请推荐一些学习方法',
  '如何记忆历史知识点？',
  '物理公式怎样才能记得更牢？',
  '学习遇到困难怎么办？'
];

// 消息类型
const AI_MESSAGE = 'ai';
const USER_MESSAGE = 'user';
const SYSTEM_MESSAGE = 'system';

// 加载聊天历史
const loadChatHistory = () => {
  get('/api/ai-assistant/history', null, 
    (message, data) => {
      if (data && data.length > 0) {
        chatHistory.value = data;
        scrollToBottom();
      } else {
        // 如果没有历史记录，添加欢迎消息
        addSystemMessage(`欢迎 ${user.value.name}！我是您的 AI 学习助手，有任何学习问题都可以问我。`, true);
      }
    }
  );
};

// 发送消息
const sendMessage = async () => {
  if (!messageInput.value.trim()) return;
  
  const userMessage = messageInput.value.trim();
  addUserMessage(userMessage);
  messageInput.value = '';
  
  // 显示AI正在思考
  addAiThinkingMessage();
  
  // 发送到后端
  post('/api/ai-assistant/chat', { message: userMessage }, 
    (message, data) => {
      // 移除思考中的消息
      chatHistory.value.pop();
      // 添加AI回复
      addAiMessage(data.response);
    },
    (errorMsg) => {
      // 移除思考中的消息
      chatHistory.value.pop();
      // 添加错误消息
      addSystemMessage('抱歉，处理您的请求时遇到了问题。请稍后再试。');
    }
  );
};

// 添加用户消息
const addUserMessage = (text) => {
  chatHistory.value.push({
    type: USER_MESSAGE,
    content: text,
    timestamp: new Date().toISOString()
  });
  scrollToBottom();
};

// 添加AI消息
const addAiMessage = (text) => {
  chatHistory.value.push({
    type: AI_MESSAGE,
    content: text,
    timestamp: new Date().toISOString()
  });
  scrollToBottom();
};

// 添加系统消息
const addSystemMessage = (text, isInitial = false) => {
  if (isInitial && chatHistory.value.length > 0) return;
  
  chatHistory.value.push({
    type: SYSTEM_MESSAGE,
    content: text,
    timestamp: new Date().toISOString()
  });
  scrollToBottom();
};

// 添加AI思考中的消息
const addAiThinkingMessage = () => {
  chatHistory.value.push({
    type: AI_MESSAGE,
    content: '思考中...',
    thinking: true,
    timestamp: new Date().toISOString()
  });
  scrollToBottom();
};

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick();
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
  }
};

// 点击常见问题
const askCommonQuestion = (question) => {
  messageInput.value = question;
  sendMessage();
};

// 清空聊天记录
const clearChat = () => {
  if (confirm('确定要清空所有聊天记录吗？')) {
    post('/api/ai-assistant/clear-history', null, 
      () => {
        chatHistory.value = [];
        addSystemMessage('聊天记录已清空。有什么可以帮助您的？', true);
      }
    );
  }
};

// 格式化时间
const formatTime = (timestamp) => {
  const date = new Date(timestamp);
  return date.toLocaleTimeString('zh-CN', { 
    hour: '2-digit', 
    minute: '2-digit'
  });
};

// 页面加载时获取聊天历史
onMounted(() => {
  loadChatHistory();
});
</script>

<template>
  <div class="ai-assistant-container">
    <div class="chat-layout">
      <!-- 侧边栏 -->
      <div class="chat-sidebar">
        <div class="p-4 border-b">
          <h2 class="text-lg font-semibold flex items-center text-blue-700">
            <LucideIcon name="brain" class="mr-2" />
            智能学习助手
          </h2>
          <p class="text-sm text-gray-600 mt-2">
            有任何学习问题，都可以向我提问
          </p>
        </div>
        
        <div class="p-4">
          <h3 class="text-sm font-medium text-gray-700 mb-3">常见问题</h3>
          <div class="space-y-2">
            <button 
              v-for="(question, index) in commonQuestions" 
              :key="index"
              @click="askCommonQuestion(question)"
              class="common-question-btn"
            >
              {{ question }}
            </button>
          </div>
        </div>
        
        <div class="mt-auto p-4 border-t">
          <button @click="clearChat" class="btn btn-outline w-full flex items-center justify-center">
            <LucideIcon name="trash-2" size="16" class="mr-2" />
            清空聊天记录
          </button>
        </div>
      </div>
      
      <!-- 聊天主区域 -->
      <div class="chat-main">
        <!-- 聊天头部 -->
        <div class="chat-header bg-gradient-to-r from-indigo-600 to-purple-600 text-white">
          <div>
            <h2 class="text-lg font-semibold">学习助手</h2>
            <p class="text-sm opacity-80">随时为您解答学习疑问</p>
          </div>
        </div>
        
        <!-- 聊天内容区 -->
        <div class="chat-messages" ref="chatContainer">
          <div v-if="chatHistory.length === 0" class="flex flex-col items-center justify-center h-full space-y-6">
            <div class="text-center">
              <LucideIcon name="star" size="48" class="text-indigo-500 mb-3" />
              <h3 class="text-xl font-semibold mb-1 text-gray-700">向 AI 提问</h3>
              <p class="text-sm text-gray-500">体验智能学习助手带来的高效辅导</p>
            </div>
            <div class="grid gap-3 md:grid-cols-2 lg:grid-cols-3">
              <button v-for="(q,index) in commonQuestions.slice(0,6)" :key="index" @click="askCommonQuestion(q)" class="px-4 py-3 rounded-lg bg-gray-100 hover:bg-gray-200 text-sm text-gray-700 text-left shadow-sm w-60">
                {{ q }}
              </button>
            </div>
          </div>
          
          <template v-else>
            <div 
              v-for="(message, index) in chatHistory" 
              :key="index"
              :class="[
                'message',
                `message-${message.type}`,
                { 'thinking': message.thinking }
              ]"
            >
              <div class="message-avatar">
                <div v-if="message.type === AI_MESSAGE" class="avatar avatar-ai">
                  <LucideIcon name="brain" size="18" />
                </div>
                <div v-else-if="message.type === USER_MESSAGE" class="avatar avatar-user">
                  <LucideIcon name="user" size="18" />
                </div>
                <div v-else class="avatar avatar-system">
                  <LucideIcon name="info" size="18" />
                </div>
              </div>
              
              <div class="message-content">
                <div class="message-text" :class="{ 'animate-pulse': message.thinking }">
                  <template v-if="message.thinking">
                    <div class="flex items-center">
                      <div class="dot-typing"></div>
                    </div>
                  </template>
                  <template v-else>
                    <div>{{ message.content }}</div>
                  </template>
                </div>
                <div class="message-time">{{ formatTime(message.timestamp) }}</div>
              </div>
            </div>
          </template>
        </div>
        
        <!-- 输入区域 -->
        <div class="chat-input">
          <div class="flex items-center bg-white rounded-full border px-3 py-2 shadow-sm">
            <input 
              type="text" 
              v-model="messageInput" 
              placeholder="输入您的问题..." 
              class="flex-grow px-3 py-1 outline-none"
              @keyup.enter="sendMessage"
            />
            <button 
              @click="sendMessage" 
              class="send-button bg-indigo-600 hover:bg-indigo-700 text-white rounded-full p-2 transition disabled:opacity-40" 
              :disabled="!messageInput.trim()"
            >
              <LucideIcon name="send" size="18" />
            </button>
          </div>
          <div class="text-xs text-gray-500 mt-2 text-center">
            AI助手将帮助解答您的学习问题，但请注意答案可能不完全准确
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ai-assistant-container { @apply h-full flex flex-col; }
.chat-layout{ @apply flex h-[calc(100vh-4rem)] bg-white rounded-lg shadow overflow-hidden; }
.chat-sidebar{ @apply w-64 border-r flex flex-col; }
.chat-main{ @apply flex-1 flex flex-col; }
.chat-header{ @apply px-6 py-4 flex items-center; }
.chat-messages{ @apply flex-1 p-6 overflow-y-auto space-y-4 bg-gray-50; }
.message{ @apply flex items-start space-x-3; }
.message-user{ @apply w-full justify-end flex-row-reverse; }
.message-user .message-content{ @apply bg-indigo-600 text-white rounded-lg px-4 py-2 max-w-md; }
.message-ai{ @apply w-full; }
.message-ai .message-content{ @apply bg-white border border-gray-200 rounded-lg px-4 py-2 max-w-md; }
.message-system .message-content{ @apply bg-yellow-50 border border-yellow-200 rounded-lg px-4 py-2 text-yellow-800 max-w-md; }
.message-time{ @apply mt-1 text-xs text-gray-400; }
.avatar{ @apply flex items-center justify-center h-8 w-8 rounded-full bg-indigo-100 text-indigo-600 shrink-0; }
.message-user .avatar{ @apply bg-blue-100 text-blue-600; }
.dot-typing{
  width:4px;height:4px;border-radius:4px;box-shadow:0 0 0 0 rgba(0,0,0,0.5),8px 0 0 0 rgba(0,0,0,0.5),16px 0 0 0 rgba(0,0,0,0.5);animation: dotPulse 1.5s infinite linear; }
@keyframes dotPulse{0%{box-shadow:0 0 0 0 rgba(0,0,0,0.5),8px 0 0 0 rgba(0,0,0,0.5),16px 0 0 0 rgba(0,0,0,0.5);}25%{box-shadow:0 0 0 0 rgba(0,0,0,0.2),8px 0 0 0 rgba(0,0,0,0.5),16px 0 0 0 rgba(0,0,0,0.5);}50%{box-shadow:0 0 0 0 rgba(0,0,0,0.5),8px 0 0 0 rgba(0,0,0,0.2),16px 0 0 0 rgba(0,0,0,0.5);}75%,100%{box-shadow:0 0 0 0 rgba(0,0,0,0.5),8px 0 0 0 rgba(0,0,0,0.5),16px 0 0 0 rgba(0,0,0,0.2);} }
</style> 