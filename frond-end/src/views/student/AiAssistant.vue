<script setup>
import { ref, reactive, onMounted, nextTick, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Send, Bot, User, Sparkles } from 'lucide-vue-next'
import { get } from '@/net'
import { useAuthStore } from '@/stores/counter'

// ====== 状态 ======
// ===== 获取当前登录用户信息 =====
const authStore = useAuthStore()
// 直接使用用户ID，不需要添加前缀
const uid = computed(() => authStore.user?.id || authStore.user?.userId || 0)
const conversations = ref([]) // 会话列表
const currentConv = reactive({ id: 'new', messages: [] })
const userInput = ref('')
const loading = ref(false)
const inputRef = ref(null)
const showSuggestions = ref(false)

// 常用问题建议
const suggestions = [
  '如何提高学习效率？',
  '帮我解释一下这道数学题',
  '推荐一些记忆方法',
  '如何制定学习计划？'
]

// 滚动到底部
const scrollBottom = () => {
  nextTick(() => {
    const container = document.querySelector('.messages-container')
    if (container) container.scrollTop = container.scrollHeight
  })
}

// 监听消息变化自动滚动到底部（放在方法定义后）
watch(() => currentConv.messages.length, () => {
  scrollBottom()
})

// 默认欢迎语
currentConv.messages.push({ 
  role: 'assistant', 
  content: '你好，我是你的学习助手！👋 我可以帮你解答问题、提供学习建议和资源推荐。有什么我能帮到你的吗？', 
  time: new Date().toLocaleTimeString() 
})

// 自动调节文本框高度
const adjustHeight = () => {
  if (!inputRef.value) return
  inputRef.value.style.height = 'auto'
  inputRef.value.style.height = Math.min(inputRef.value.scrollHeight, 150) + 'px'
}
watch(userInput, () => nextTick(adjustHeight))

// 使用建议问题
const useSuggestion = (text) => {
  userInput.value = text
  showSuggestions.value = false
  nextTick(() => sendMessage())
}

// 发送消息
const sendMessage = () => {
  const text = userInput.value.trim()
  if (!text) return
  currentConv.messages.push({ role: 'user', content: text, time: new Date().toLocaleTimeString() })
  userInput.value = ''
  adjustHeight()
  scrollBottom()
  requestAssistant(text)
}

// 处理输入框键盘事件：Enter发送，Shift+Enter换行
const handleKeyDown = (e) => {
  if (e.key === 'Enter') {
    if (e.shiftKey) return // 允许换行
    e.preventDefault()
    sendMessage()
  }
}

// 与后端交互 (接入流式响应API)
const requestAssistant = async (question) => {
  loading.value = true

  try {
    // 获取用户ID，确保是数字格式
    const userId = authStore.user?.id || authStore.user?.userId
    if (!userId) {
      throw new Error('未获取到用户ID，请重新登录')
    }

    // 构建API请求URL，使用查询参数传递message和uid
    const url = `/api/student/chat?message=${encodeURIComponent(question)}&uid=${encodeURIComponent(userId)}`
    
    // 发起请求，指定接收事件流
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Accept': 'text/event-stream',
        'Authorization': `Bearer ${localStorage.getItem('authToken')}`
      }
    })

    if (!response.ok || !response.body) {
      throw new Error('网络错误或后端无流式响应')
    }

    // 标记已添加assistant消息
    let messageAdded = false
    let accumulated = ''

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')

    // 创建一个新的消息
    currentConv.messages.push({ role: 'assistant', content: '', time: new Date().toLocaleTimeString() })
    messageAdded = true
    loading.value = false // 首块到来即可结束loading
    
    const messageIndex = currentConv.messages.length - 1

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      const chunk = decoder.decode(value, { stream: true })

      // 处理SSE格式的数据
      const lines = chunk.split('\n')
      for (let line of lines) {
        if (!line.trim()) continue
        
        // 如果是SSE格式的数据，去掉前缀
        if (line.startsWith('data:')) {
          line = line.substring(5).trim()
        }
        
        // 追加到累积内容
        accumulated += line

        // 更新消息内容
        currentConv.messages[messageIndex].content = accumulated
        
        // 实时滚动
        scrollBottom()
      }
    }

    // 若没有收到任何内容
    if (accumulated === '') {
      currentConv.messages[messageIndex].content = '抱歉，暂时无法回答~'
    }
  } catch (err) {
    console.error(err)
    ElMessage.error(err.message || '获取回答失败')
    loading.value = false
    
    // 添加错误消息
    currentConv.messages.push({ 
      role: 'assistant', 
      content: `发生错误: ${err.message || '获取回答失败'}`, 
      time: new Date().toLocaleTimeString() 
    })
  }
}

// 显示建议
const toggleSuggestions = () => {
  showSuggestions.value = !showSuggestions.value
}

// ===== 加载历史记录 =====
onMounted(() => {
  // 初始渲染后聚焦输入框并滚动到底部
  nextTick(() => {
    if (inputRef.value) inputRef.value.focus()
    scrollBottom()
  })
  
  // 如果用户已登录，尝试加载历史记录
  if (authStore.isLoggedIn && uid.value) {
    loading.value = true
    get('/api/student/chat/history', 
      { uid: uid.value }, 
      (message, data) => {
        if (data && Array.isArray(data) && data.length > 0) {
          // 处理历史记录
          data.forEach(item => {
            if (item.role && item.content) {
              currentConv.messages.push({
                role: item.role,
                content: item.content,
                time: new Date(item.timestamp || Date.now()).toLocaleTimeString()
              })
            }
          })
          scrollBottom()
        }
        loading.value = false
      }, 
      (message) => {
        console.error('获取历史记录失败:', message)
        loading.value = false
      }, 
      (error) => {
        console.error('获取历史记录异常:', error)
        loading.value = false
      }
    )
  }
})
</script>

<template>
  <div class="assistant-container">
    <div class="assistant-wrapper">
      <!-- 顶部标题栏 -->
      <div class="assistant-header">
        <div class="flex items-center">
          <Bot class="mr-2 text-indigo-500" size="24" />
          <h2 class="text-lg font-semibold">智能学习助手</h2>
        </div>
        <div class="assistant-actions">
          <button class="action-btn" @click="toggleSuggestions">
            <Sparkles size="18" class="text-amber-500" />
            <span class="ml-1 text-sm">常见问题</span>
          </button>
        </div>
      </div>

      <!-- 常见问题建议 -->
      <transition name="slide-down">
        <div v-if="showSuggestions" class="suggestions-container">
          <div class="suggestions-grid">
            <div 
              v-for="(suggestion, idx) in suggestions" 
              :key="idx" 
              class="suggestion-item"
              @click="useSuggestion(suggestion)"
            >
              <span>{{ suggestion }}</span>
            </div>
          </div>
        </div>
      </transition>

      <!-- 聊天消息区域 -->
      <div class="messages-container" :class="{ 'pb-24': true }">
        <transition-group name="list" tag="div">
          <div v-for="(msg, idx) in currentConv.messages" :key="idx" :class="['message-item', msg.role]">
            <div class="avatar">
              <div class="avatar-icon">
                <User v-if="msg.role === 'user'" size="16" />
                <Bot v-else size="16" />
              </div>
            </div>
            <div class="bubble" v-html="msg.content"></div>
            <span class="time text-[10px] text-gray-400 mt-1" v-if="idx===currentConv.messages.length-1">{{ msg.time }}</span>
          </div>
        </transition-group>
        <div v-if="loading" class="loading-dots"><span></span><span></span><span></span></div>
      </div>

      <!-- 输入栏 -->
      <div class="input-bar">
        <textarea 
          ref="inputRef" 
          v-model="userInput" 
          placeholder="输入问题，Enter 发送；Shift+Enter 换行" 
          @keydown="handleKeyDown"
        ></textarea>
        <button class="send-btn" @click="sendMessage">
          <Send size="18" />
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.assistant-container {
  @apply h-full w-full flex justify-center;
}

.assistant-wrapper {
  @apply h-full flex flex-col bg-gradient-to-b from-indigo-50 to-white relative w-full max-w-4xl;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.assistant-header {
  @apply flex justify-between items-center px-6 py-4 bg-white/80 backdrop-blur-lg;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.03);
}

.assistant-actions {
  @apply flex items-center;
}

.action-btn {
  @apply flex items-center px-3 py-1.5 rounded-full bg-indigo-50 hover:bg-indigo-100 text-indigo-600 transition-all duration-200;
}

.suggestions-container {
  @apply bg-white/90 backdrop-blur-md p-4 border-b border-indigo-100;
  animation: slideDown 0.3s ease-out forwards;
}

.suggestions-grid {
  @apply grid grid-cols-1 sm:grid-cols-2 gap-2;
}

.suggestion-item {
  @apply px-4 py-3 bg-gradient-to-r from-indigo-50 to-purple-50 rounded-lg cursor-pointer hover:shadow-md hover:-translate-y-0.5 transition-all duration-200;
  border: 1px solid rgba(99, 102, 241, 0.1);
}

.suggestion-item:hover {
  background: linear-gradient(135deg, #EEF2FF 0%, #EDE9FE 100%);
  border-color: rgba(99, 102, 241, 0.3);
}

.assistant-wrapper::before {
  content: '';
  position: absolute;
  inset: 0;
  background: url('https://www.transparenttextures.com/patterns/cubes.png');
  opacity: .05;
  z-index: 0;
  pointer-events: none;
}

.messages-container {
  @apply flex-1 overflow-y-auto px-4 py-6 space-y-4;
  scroll-behavior: smooth;
  z-index: 1;
}

.message-item {
  @apply flex items-start gap-3;
}

.message-item.user { 
  flex-direction: row-reverse; 
}

.avatar {
  @apply flex items-center justify-center w-10 h-10 rounded-full;
}

.avatar-icon {
  @apply flex items-center justify-center w-8 h-8 rounded-full;
}

.message-item.user .avatar-icon {
  @apply bg-gradient-to-br from-indigo-500 to-purple-600 text-white;
}

.message-item.assistant .avatar-icon {
  @apply bg-gradient-to-br from-emerald-400 to-teal-500 text-white;
}

.bubble {
  @apply max-w-[75%] px-4 py-3 rounded-2xl shadow-lg transition-all duration-200 backdrop-blur-sm;
  word-break: break-word;
}

.message-item.user .bubble {
  @apply bg-gradient-to-r from-indigo-500 to-violet-600 text-white;
  animation: user-pop 0.25s ease-out;
}

.message-item.assistant .bubble {
  @apply bg-white/80 text-gray-800 border border-gray-200;
  animation: bot-pop 0.25s ease-out;
}

@keyframes user-pop {
  0% { transform: translateY(10px); opacity: 0; }
  100% { transform: translateY(0); opacity: 1; }
}

@keyframes bot-pop {
  0% { transform: translateY(10px); opacity: 0; }
  100% { transform: translateY(0); opacity: 1; }
}

@keyframes slideDown {
  0% { transform: translateY(-20px); opacity: 0; }
  100% { transform: translateY(0); opacity: 1; }
}

/* 列表过渡 */
.list-enter-from, .list-leave-to { opacity: 0; transform: translateY(10px); }
.list-enter-to, .list-leave-from { opacity: 1; transform: translateY(0); }
.list-enter-active, .list-leave-active { transition: all 0.25s ease-out; }

/* 建议下滑动画 */
.slide-down-enter-active, .slide-down-leave-active {
  transition: all 0.3s ease-out;
  max-height: 400px;
}
.slide-down-enter-from, .slide-down-leave-to {
  max-height: 0;
  opacity: 0;
  transform: translateY(-10px);
}

/* 输入栏 */
.input-bar {
  @apply fixed bottom-4 left-1/2 transform -translate-x-1/2 w-[92%] max-w-3xl flex bg-white/90 backdrop-blur-lg rounded-full shadow-2xl px-5 py-3 items-end gap-3;
  border: 1px solid rgba(99, 102, 241, 0.2);
}

.input-bar textarea {
  @apply flex-1 resize-none bg-transparent focus:outline-none text-sm leading-5 max-h-40;
}

.send-btn {
  @apply bg-gradient-to-r from-indigo-500 to-purple-600 hover:from-indigo-600 hover:to-purple-700 active:from-indigo-700 active:to-purple-800 text-white rounded-full p-3 transition-all duration-150 shadow-md;
}

.send-btn:active {
  transform: scale(.9);
}

.time {
  display: block;
}

.loading-dots {
  display: flex;
  gap: 4px;
  margin-top: 8px;
  padding-left: 44px;
}

.loading-dots span {
  width: 8px;
  height: 8px;
  background: #a5b4fc;
  border-radius: 50%;
  animation: blink 1.4s infinite;
}

.loading-dots span:nth-child(2) {
  animation-delay: 0.2s;
  background: #818cf8;
}

.loading-dots span:nth-child(3) {
  animation-delay: 0.4s;
  background: #6366f1;
}

@keyframes blink {
  0%, 80%, 100% { opacity: 0; transform: scale(0.8); }
  40% { opacity: 1; transform: scale(1); }
}
</style> 