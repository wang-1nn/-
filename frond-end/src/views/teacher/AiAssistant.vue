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

// 教学资源生成
const generatingResource = ref(false);
const resourceType = ref('lesson');
const resourceSubject = ref('');
const resourceGrade = ref('');
const resourceTitle = ref('');
const resourceContent = ref('');
const showResourceModal = ref(false);

// 常见问题
const commonQuestions = [
  '帮我设计一节关于力学基本定律的物理课',
  '如何提高学生的课堂参与度？',
  '设计一个小组合作学习活动',
  '如何处理课堂上学生的不当行为？',
  '请提供一些课堂教学评估方法',
  '如何有效地使用教育技术提升教学效果？'
];

// 消息类型
const AI_MESSAGE = 'ai';
const USER_MESSAGE = 'user';
const SYSTEM_MESSAGE = 'system';

// 资源类型选项
const resourceTypeOptions = [
  { value: 'lesson', label: '课程教案' },
  { value: 'quiz', label: '课堂测验' },
  { value: 'homework', label: '课后作业' },
  { value: 'presentation', label: '教学演示' },
  { value: 'activity', label: '课堂活动' }
];

// 学科选项
const subjectOptions = [
  '语文', '数学', '英语', '物理', '化学', '生物', 
  '历史', '地理', '政治', '信息技术', '音乐', '美术', '体育'
];

// 年级选项
const gradeOptions = [
  '小学一年级', '小学二年级', '小学三年级', '小学四年级', '小学五年级', '小学六年级',
  '初中一年级', '初中二年级', '初中三年级',
  '高中一年级', '高中二年级', '高中三年级'
];

// 加载聊天历史
const loadChatHistory = () => {
  get('/api/ai-assistant/history', { role: 'teacher' }, 
    (message, data) => {
      if (data && data.length > 0) {
        chatHistory.value = data;
        scrollToBottom();
      } else {
        // 如果没有历史记录，添加欢迎消息
        addSystemMessage(`欢迎 ${user.value.name} 老师！我是您的教学助手，可以帮助您准备教案、设计活动和解答教学疑问。`, true);
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
  post('/api/ai-assistant/chat', { 
    message: userMessage,
    role: 'teacher'
  }, 
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
  });
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
    post('/api/ai-assistant/clear-history', { role: 'teacher' }, 
      () => {
        chatHistory.value = [];
        addSystemMessage('聊天记录已清空。有什么可以帮助您的？', true);
      }
    );
  }
};

// 打开教学资源生成模态框
const openResourceGenerator = () => {
  resourceType.value = 'lesson';
  resourceSubject.value = '';
  resourceGrade.value = '';
  resourceTitle.value = '';
  resourceContent.value = '';
  showResourceModal.value = true;
};

// 生成教学资源
const generateResource = () => {
  if (!resourceSubject.value || !resourceGrade.value || !resourceTitle.value) {
    alert('请填写完整的资源信息');
    return;
  }
  
  generatingResource.value = true;
  
  // 构建提示词
  const prompt = `
    请帮我生成一份${getResourceTypeName(resourceType.value)}，
    学科：${resourceSubject.value}
    年级：${resourceGrade.value}
    主题：${resourceTitle.value}
  `;
  
  // 发送到后端
  post('/api/ai-assistant/generate-resource', {
    type: resourceType.value,
    subject: resourceSubject.value,
    grade: resourceGrade.value,
    title: resourceTitle.value,
    prompt: prompt
  }, 
  (message, data) => {
    resourceContent.value = data.content;
    generatingResource.value = false;
  },
  (errorMsg) => {
    alert('生成资源失败，请稍后再试');
    generatingResource.value = false;
  });
};

// 保存教学资源
const saveResource = () => {
  if (!resourceContent.value) return;
  
  post('/api/resources/save', {
    type: resourceType.value,
    subject: resourceSubject.value,
    grade: resourceGrade.value,
    title: resourceTitle.value,
    content: resourceContent.value
  }, 
  () => {
    alert('资源保存成功！');
    showResourceModal.value = false;
  },
  () => {
    alert('资源保存失败，请稍后再试');
  });
};

// 获取资源类型名称
const getResourceTypeName = (type) => {
  const option = resourceTypeOptions.find(opt => opt.value === type);
  return option ? option.label : '';
};

// 复制内容到剪贴板
const copyToClipboard = () => {
  if (!resourceContent.value) return;
  
  navigator.clipboard.writeText(resourceContent.value).then(() => {
    alert('内容已复制到剪贴板');
  }).catch(() => {
    alert('复制失败，请手动复制');
  });
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
          <h2 class="text-lg font-semibold flex items-center text-indigo-700">
            <LucideIcon name="brain" class="mr-2" />
            教学助手
          </h2>
          <p class="text-sm text-gray-600 mt-2">
            您的智能教学规划和教案设计伙伴
          </p>
        </div>
        
        <div class="p-4">
          <button 
            @click="openResourceGenerator" 
            class="w-full mb-4 py-2 px-4 bg-indigo-600 hover:bg-indigo-700 text-white rounded-md flex items-center justify-center transition-colors"
          >
            <LucideIcon name="file-plus" size="16" class="mr-2" />
            生成教学资源
          </button>
          
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
            <h2 class="text-lg font-semibold">教学助手</h2>
            <p class="text-sm opacity-80">助您备课、教学与评估</p>
          </div>
        </div>
        
        <!-- 聊天内容区 -->
        <div class="chat-messages" ref="chatContainer">
          <div v-if="chatHistory.length === 0" class="flex flex-col items-center justify-center h-full text-gray-400">
            <LucideIcon name="message-circle" size="48" class="mb-2" />
            <p>暂无聊天记录</p>
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
              class="bg-indigo-600 hover:bg-indigo-700 text-white rounded-full p-2 transition disabled:opacity-40" 
              :disabled="!messageInput.trim()"
            >
              <LucideIcon name="send" size="18" />
            </button>
          </div>
          <div class="text-xs text-gray-500 mt-2 text-center">
            教学助手可以帮您设计教案、教学活动和答疑解惑
          </div>
        </div>
      </div>
    </div>
    
    <!-- 教学资源生成模态框 -->
    <div v-if="showResourceModal" class="modal-overlay">
      <div class="modal-container max-w-3xl">
        <div class="modal-header">
          <h3>教学资源生成器</h3>
          <button @click="showResourceModal = false" class="text-gray-400 hover:text-gray-600">
            <LucideIcon name="x" size="20" />
          </button>
        </div>
        
        <div class="modal-body">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
            <div class="form-group">
              <label class="form-label">资源类型</label>
              <select v-model="resourceType" class="form-select">
                <option 
                  v-for="option in resourceTypeOptions" 
                  :key="option.value" 
                  :value="option.value"
                >
                  {{ option.label }}
                </option>
              </select>
            </div>
            
            <div class="form-group">
              <label class="form-label">学科</label>
              <select v-model="resourceSubject" class="form-select" required>
                <option value="" disabled selected>请选择学科</option>
                <option v-for="subject in subjectOptions" :key="subject" :value="subject">
                  {{ subject }}
                </option>
              </select>
            </div>
            
            <div class="form-group">
              <label class="form-label">年级</label>
              <select v-model="resourceGrade" class="form-select" required>
                <option value="" disabled selected>请选择年级</option>
                <option v-for="grade in gradeOptions" :key="grade" :value="grade">
                  {{ grade }}
                </option>
              </select>
            </div>
            
            <div class="form-group">
              <label class="form-label">主题/标题</label>
              <input 
                type="text" 
                v-model="resourceTitle" 
                class="form-input" 
                placeholder="输入资源主题或标题"
                required
              />
            </div>
          </div>
          
          <div class="flex justify-center mb-6">
            <button 
              @click="generateResource" 
              class="btn btn-primary px-6" 
              :disabled="generatingResource"
            >
              <LucideIcon 
                :name="generatingResource ? 'loader-2' : 'wand'" 
                :class="generatingResource ? 'animate-spin' : ''"
                size="16" 
                class="mr-2" 
              />
              {{ generatingResource ? '生成中...' : '开始生成' }}
            </button>
          </div>
          
          <div class="form-group">
            <label class="form-label flex justify-between items-center">
              <span>生成结果</span>
              <button 
                v-if="resourceContent" 
                @click="copyToClipboard"
                class="text-xs text-indigo-600 hover:text-indigo-800 flex items-center"
              >
                <LucideIcon name="copy" size="12" class="mr-1" />
                复制内容
              </button>
            </label>
            <textarea 
              v-model="resourceContent" 
              class="form-textarea h-64 font-mono text-sm"
              readonly
              placeholder="点击上方按钮生成教学资源..."
            ></textarea>
          </div>
        </div>
        
        <div class="modal-footer">
          <button @click="showResourceModal = false" class="btn btn-outline">关闭</button>
          <button 
            @click="saveResource" 
            class="btn btn-primary"
            :disabled="!resourceContent"
          >
            <LucideIcon name="save" size="16" class="mr-2" />
            保存资源
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ai-assistant-container {
  height: 100%;
  background-color: #f9fafb;
}

.chat-layout {
  display: flex;
  height: 100%;
  max-height: calc(100vh - 80px);
}

.chat-sidebar {
  width: 280px;
  background-color: white;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  max-width: calc(100% - 280px);
}

.chat-header {
  padding: 1rem;
  border-bottom: 1px solid #e5e7eb;
  background-color: white;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.chat-input {
  padding: 1rem;
  border-top: 1px solid #e5e7eb;
  background-color: #f9fafb;
}

.message {
  display: flex;
  gap: 0.75rem;
  max-width: 80%;
}

.message-ai {
  @apply w-full;
}

.message-user {
  @apply w-full justify-end flex-row-reverse;
}

.message-system {
  align-self: center;
  width: 100%;
  max-width: 600px;
}

.message-avatar {
  flex-shrink: 0;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.avatar-ai {
  background-color: #6366f1;
}

.avatar-user {
  background-color: #4f46e5;
}

.avatar-system {
  background-color: #9ca3af;
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.message-text {
  padding: 0.75rem 1rem;
  border-radius: 0.75rem;
  font-size: 0.9375rem;
  line-height: 1.5;
}

.message-ai .message-text {
  @apply bg-white border border-gray-200 rounded-lg px-4 py-2 max-w-md;
}

.message-user .message-text {
  background-color: #4f46e5;
  color: white;
  border-top-right-radius: 0;
}

.message-system .message-text {
  background-color: #f3f4f6;
  color: #4b5563;
  text-align: center;
}

.message-time {
  font-size: 0.75rem;
  color: #6b7280;
}

.message-user .message-time {
  text-align: right;
}

.send-button {
  background-color: #4f46e5;
  color: white;
  width: 40px;
  height: 40px;
  border-radius: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
}

.send-button:hover {
  background-color: #4338ca;
}

.send-button:disabled {
  background-color: #c7d2fe;
  cursor: not-allowed;
}

.common-question-btn {
  width: 100%;
  text-align: left;
  padding: 0.5rem;
  border-radius: 0.375rem;
  font-size: 0.875rem;
  color: #4b5563;
  background-color: #f3f4f6;
  transition: background-color 0.2s;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.common-question-btn:hover {
  background-color: #e5e7eb;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 50;
}

.modal-container {
  background-color: white;
  border-radius: 0.5rem;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h3 {
  font-size: 1.125rem;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.modal-body {
  padding: 1.5rem;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  padding: 1rem 1.5rem;
  border-top: 1px solid #e5e7eb;
  gap: 0.75rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-label {
  display: block;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
  font-weight: 500;
  color: #475569;
}

.form-input,
.form-select,
.form-textarea {
  width: 100%;
  border: 1px solid #e5e7eb;
  border-radius: 0.375rem;
  padding: 0.5rem 0.75rem;
  font-size: 0.875rem;
  transition: border-color 0.2s;
}

.form-textarea {
  resize: none;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: #a5b4fc;
  ring: 2px;
  ring-color: #c7d2fe;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 0.375rem;
  font-weight: 500;
  padding: 0.5rem 1rem;
  transition: all 0.2s;
}

.btn-primary {
  background-color: #4f46e5;
  color: white;
}

.btn-primary:hover {
  background-color: #4338ca;
}

.btn-primary:disabled {
  background-color: #c7d2fe;
  cursor: not-allowed;
}

.btn-outline {
  background-color: white;
  border: 1px solid #e5e7eb;
  color: #475569;
}

.btn-outline:hover {
  background-color: #f8fafc;
  border-color: #cbd5e1;
}

.dot-typing {
  position: relative;
  left: -9999px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #6366f1;
  color: #6366f1;
  box-shadow: 9984px 0 0 0 #6366f1, 9999px 0 0 0 #6366f1, 10014px 0 0 0 #6366f1;
  animation: dot-typing 1.5s infinite linear;
}

@keyframes dot-typing {
  0% {
    box-shadow: 9984px 0 0 0 #6366f1, 9999px 0 0 0 #6366f1, 10014px 0 0 0 #6366f1;
  }
  16.667% {
    box-shadow: 9984px -10px 0 0 #6366f1, 9999px 0 0 0 #6366f1, 10014px 0 0 0 #6366f1;
  }
  33.333% {
    box-shadow: 9984px 0 0 0 #6366f1, 9999px 0 0 0 #6366f1, 10014px 0 0 0 #6366f1;
  }
  50% {
    box-shadow: 9984px 0 0 0 #6366f1, 9999px -10px 0 0 #6366f1, 10014px 0 0 0 #6366f1;
  }
  66.667% {
    box-shadow: 9984px 0 0 0 #6366f1, 9999px 0 0 0 #6366f1, 10014px 0 0 0 #6366f1;
  }
  83.333% {
    box-shadow: 9984px 0 0 0 #6366f1, 9999px 0 0 0 #6366f1, 10014px -10px 0 0 #6366f1;
  }
  100% {
    box-shadow: 9984px 0 0 0 #6366f1, 9999px 0 0 0 #6366f1, 10014px 0 0 0 #6366f1;
  }
}

@media (max-width: 768px) {
  .chat-layout {
    flex-direction: column;
  }
  
  .chat-sidebar {
    width: 100%;
    max-height: 200px;
    border-right: none;
    border-bottom: 1px solid #e5e7eb;
  }
  
  .chat-main {
    max-width: 100%;
  }
  
  .message {
    max-width: 90%;
  }
}
</style> 