<script setup>
import { ref, reactive, onMounted, nextTick, computed, watch } from 'vue';
import ChatSuggestions from './ChatSuggestions.vue';

const props = defineProps({
  initialContext: {
    type: String,
    default: ''
  },
  teacherInfo: {
    type: Object,
    default: () => ({ name: '', subject: '', grade: '' })
  }
});

const emit = defineEmits(['message-sent', 'context-updated', 'request-generate']);

// 消息列表
const messages = ref([]);
// 当前输入
const userInput = ref('');
// 加载状态
const loading = ref(false);
// 聊天窗口引用
const chatWindow = ref(null);
// 是否显示建议
const showSuggestions = ref(true);
// 是否显示语音输入按钮
const showVoiceInput = ref(false);
// 是否正在录音
const isRecording = ref(false);

// 上下文菜单状态
const contextMenu = reactive({
  show: false,
  x: 0,
  y: 0,
  message: null
});

// 获取最近的消息用于生成建议
const recentMessages = computed(() => {
  return messages.value.slice(-5);
});

// 聊天功能
const functionalities = [
  { 
    id: 'lesson-plan', 
    name: '教案生成', 
    prompt: '请帮我设计一个关于[主题]的教案，面向[年级]学生，时长[时间]分钟。',
    icon: 'document-text'
  },
  { 
    id: 'question-gen', 
    name: '习题生成', 
    prompt: '请生成[数量]道关于[主题]的[题型]练习题，适合[年级]学生。',
    icon: 'document-question'
  },
  { 
    id: 'explanation', 
    name: '概念解释', 
    prompt: '请用简单易懂的语言解释[概念]，使[年级]学生能够理解。',
    icon: 'light-bulb'
  },
  { 
    id: 'feedback', 
    name: '作业反馈', 
    prompt: '请针对这份学生作业提供建设性反馈：[粘贴作业内容]',
    icon: 'chat'
  },
  {
    id: 'lesson-activity',
    name: '教学活动',
    prompt: '请设计一个关于[主题]的有趣课堂活动，适合[年级]学生。',
    icon: 'users'
  }
];

// 初始化
onMounted(() => {
  // 添加欢迎消息
  messages.value.push({
    id: Date.now(),
    sender: 'assistant',
    content: '欢迎使用AI教学助手！我可以帮您设计教案、生成习题、解释概念和提供作业反馈。请告诉我您需要什么帮助？',
    timestamp: new Date(),
    saved: false
  });
  
  // 如果有初始上下文，添加
  if (props.initialContext) {
    messages.value.push({
      id: Date.now() + 1,
      sender: 'context',
      content: props.initialContext,
      timestamp: new Date(),
      saved: false
    });
  }
  
  // 检查是否支持语音输入
  if ('webkitSpeechRecognition' in window || 'SpeechRecognition' in window) {
    showVoiceInput.value = true;
  }
});

// 发送消息
const sendMessage = () => {
  if (!userInput.value.trim()) return;
  
  const message = {
    id: Date.now(),
    sender: 'user',
    content: userInput.value,
    timestamp: new Date(),
    saved: false
  };
  
  messages.value.push(message);
  
  emit('message-sent', message.content);
  
  // 清空输入框
  userInput.value = '';
  
  // 模拟AI回复
  simulateResponse();
  
  // 滚动到底部
  scrollToBottom();
  
  // 显示建议
  showSuggestions.value = true;
};

// 模拟AI回复
const simulateResponse = () => {
  loading.value = true;
  
  // 模拟延迟
  setTimeout(() => {
    const responseMessage = {
      id: Date.now(),
      sender: 'assistant',
      content: generateResponse(messages.value[messages.value.length - 1].content),
      timestamp: new Date(),
      saved: false
    };
    
    messages.value.push(responseMessage);
    loading.value = false;
    
    // 滚动到底部
    scrollToBottom();
  }, 1500);
};

// 生成模拟回复
const generateResponse = (question) => {
  if (question.toLowerCase().includes('教案') || question.toLowerCase().includes('lesson')) {
    return `以下是针对您要求的教案设计：

## 教学目标
- 理解核心概念和原理
- 能够应用所学知识解决实际问题
- 培养学生的批判性思维能力

## 教学流程
1. **导入环节**（5分钟）
   - 通过提问引入主题
   - 展示与主题相关的图片或视频

2. **讲授环节**（20分钟）
   - 介绍关键概念
   - 讲解原理和方法
   - 使用直观示例说明

3. **练习环节**（15分钟）
   - 小组讨论解决问题
   - 完成相关练习

4. **总结环节**（5分钟）
   - 回顾主要知识点
   - 布置课后作业

## 教学资源
- PPT演示文稿
- 学习工作单
- 多媒体素材

您可以根据具体主题进一步调整和完善这个教案框架。`;
  } else if (question.toLowerCase().includes('习题') || question.toLowerCase().includes('题目')) {
    return `以下是几道练习题供您参考：

1. **选择题**：以下哪项是正确的？
   A. 选项一
   B. 选项二
   C. 选项三
   D. 选项四
   
   *答案：B，解析：选项二正确，因为...*

2. **填空题**：请在空白处填入合适的内容：___________

   *答案：正确答案，解析：该处应填写...*

3. **解答题**：请解释以下现象并给出理由。

   *参考答案：该现象的解释是...*

4. **应用题**：根据所学知识，分析以下情景并提出解决方案。

   *参考答案：可以采取以下步骤解决...*

如果需要更多题目或调整难度，请告诉我。`;
  } else if (question.toLowerCase().includes('解释') || question.toLowerCase().includes('概念')) {
    return `这个概念可以这样理解：

## 基本定义
这是指一种特定的现象或原理，它在特定领域中有明确的含义和应用。

## 通俗解释
想象一下，这就像是日常生活中我们经常遇到的某种情况。比如说...

## 具体例子
在实际应用中，我们可以看到这样的例子：
1. 例子一：...
2. 例子二：...

## 为什么重要
理解这个概念很重要，因为它帮助我们：
- 更好地理解相关知识体系
- 解决特定类型的问题
- 发展批判性思维能力

希望这个解释对您有所帮助！如果需要更深入的解释或其他方面的信息，请告诉我。`;
  } else if (question.toLowerCase().includes('反馈') || question.toLowerCase().includes('评价')) {
    return `关于这份作业的反馈如下：

## 优点
- 整体结构清晰，逻辑连贯
- 关键概念理解准确
- 表达方式流畅自然

## 需要改进的地方
- 部分论点缺乏足够的支持证据
- 某些概念的应用可以更加深入
- 建议增加更多实例来说明观点

## 具体建议
1. 在第二部分补充更多的实例
2. 深化对核心概念的分析
3. 注意格式和引用规范

## 总体评价
这是一份不错的作业，展示了良好的基础知识和思考能力。按照以上建议修改后，质量将会进一步提高。

希望这些反馈有助于改进作业！`;
  } else if (question.toLowerCase().includes('活动') || question.toLowerCase().includes('互动')) {
    return `以下是一个课堂活动设计：

## 概念图谱竞赛

### 准备材料
- 大幅海报纸或白板
- 彩色记号笔
- 计时器
- 便利贴

### 活动流程
1. **分组准备** (5分钟)
   - 将学生分成4-5人小组
   - 为每组分发材料

2. **概念讲解** (5分钟)
   - 教师简要介绍核心概念
   - 明确任务目标和评分标准

3. **小组创作** (15分钟)
   - 各小组围绕中心概念创建概念图谱
   - 标明概念之间的联系和关系

4. **展示评价** (10分钟)
   - 各小组轮流展示并讲解自己的概念图谱
   - 其他小组可提问和补充

5. **总结反思** (5分钟)
   - 教师总结各组亮点
   - 引导学生反思学习收获

### 活动目标
- 加深对概念的理解
- 培养团队协作能力
- 锻炼表达和讲解能力

这个活动可以根据具体学科和年级灵活调整难度和复杂度。`;
  } else {
    return `感谢您的问题！作为您的AI教学助手，我可以帮您：

1. **设计教案** - 为不同年级、不同学科创建结构化的教案
2. **生成练习题** - 创建各种类型和难度的习题
3. **解释概念** - 以适合学生理解的方式解释复杂概念
4. **提供作业反馈** - 分析学生作业并给出建设性意见
5. **设计教学活动** - 创建互动性强的课堂活动

您可以直接告诉我您需要什么具体帮助，或者使用下方的功能按钮选择一项服务。`;
  }
};

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick();
  if (chatWindow.value) {
    chatWindow.value.scrollTop = chatWindow.value.scrollHeight;
  }
};

// 使用功能提示
const usePrompt = (functionality) => {
  userInput.value = functionality.prompt
    .replace('[主题]', props.teacherInfo.subject || '相关主题')
    .replace('[年级]', props.teacherInfo.grade || '学生')
    .replace('[时间]', '45')
    .replace('[数量]', '5')
    .replace('[题型]', '多样化');
  
  // 聚焦输入框
  document.querySelector('.input-area textarea').focus();
};

// 使用建议
const useSuggestion = (suggestion) => {
  userInput.value = suggestion;
  // 隐藏建议面板
  showSuggestions.value = false;
  // 聚焦输入框
  document.querySelector('.input-area textarea').focus();
};

// 切换建议面板
const toggleSuggestions = () => {
  showSuggestions.value = !showSuggestions.value;
};

// 右键菜单
const showContextMenu = (event, message) => {
  event.preventDefault();
  
  contextMenu.show = true;
  contextMenu.x = event.clientX;
  contextMenu.y = event.clientY;
  contextMenu.message = message;
};

// 关闭右键菜单
const closeContextMenu = () => {
  contextMenu.show = false;
};

// 保存消息
const saveMessage = (message) => {
  message.saved = true;
  closeContextMenu();
};

// 复制消息内容
const copyMessageContent = (message) => {
  navigator.clipboard.writeText(message.content)
    .then(() => {
      // 复制成功
      alert('内容已复制到剪贴板');
    })
    .catch(err => {
      // 复制失败
      console.error('无法复制内容: ', err);
    });
  
  closeContextMenu();
};

// 删除消息
const deleteMessage = (message) => {
  const index = messages.value.findIndex(m => m.id === message.id);
  if (index !== -1) {
    messages.value.splice(index, 1);
  }
  closeContextMenu();
};


// 格式化时间
const formatTime = (date) => {
  return new Date(date).toLocaleTimeString('zh-CN', { 
    hour: '2-digit', 
    minute: '2-digit'
  });
};

// 开始语音输入
const startVoiceInput = () => {
  if (!('webkitSpeechRecognition' in window) && !('SpeechRecognition' in window)) {
    alert('您的浏览器不支持语音识别功能');
    return;
  }
  
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
  const recognition = new SpeechRecognition();
  
  recognition.lang = 'zh-CN';
  recognition.continuous = false;
  recognition.interimResults = false;
  
  recognition.onstart = () => {
    isRecording.value = true;
  };
  
  recognition.onresult = (event) => {
    const transcript = event.results[0][0].transcript;
    userInput.value = transcript;
  };
  
  recognition.onerror = (event) => {
    console.error('语音识别错误:', event.error);
    isRecording.value = false;
  };
  
  recognition.onend = () => {
    isRecording.value = false;
  };
  
  recognition.start();
};

// 扩展输入框
const textareaRef = ref(null);
const autoResizeTextarea = () => {
  const textarea = textareaRef.value;
  if (textarea) {
    textarea.style.height = 'auto';
    textarea.style.height = `${textarea.scrollHeight}px`;
  }
};

// 监听输入变化自动调整高度
watch(() => userInput.value, () => {
  nextTick(() => autoResizeTextarea());
});
</script>

<template>
  <div class="chat-container">
    <!-- 聊天窗口 -->
    <div class="messages-container" ref="chatWindow">
      <div 
        v-for="message in messages" 
        :key="message.id" 
        :class="[
          'message', 
          message.sender === 'user' ? 'user-message' : 
          message.sender === 'assistant' ? 'assistant-message' : 
          'context-message'
        ]"
        @contextmenu="showContextMenu($event, message)"
      >
        <div class="message-sender" v-if="message.sender !== 'context'">
          {{ message.sender === 'user' ? '你' : 'AI助教' }}
        </div>
        
        <div class="message-content" v-if="message.sender !== 'context'">
          {{ message.content }}
        </div>
        
        <div class="context-content" v-else>
          <div class="context-header">上下文信息</div>
          <div class="context-text">{{ message.content }}</div>
        </div>
        
        <div class="message-meta">
          <span class="message-time">{{ formatTime(message.timestamp) }}</span>
          <span class="message-saved" v-if="message.saved">已保存</span>
        </div>
      </div>
      
      <!-- 加载指示器 -->
      <div class="loading-indicator" v-if="loading">
        <div class="dot"></div>
        <div class="dot"></div>
        <div class="dot"></div>
      </div>
    </div>
    
    <!-- 聊天建议 -->
    <div v-if="showSuggestions && messages.length > 0 && !loading" class="suggestions-container">
      <ChatSuggestions 
        :subject="teacherInfo.subject"
        :grade="teacherInfo.grade"
        :recentMessages="recentMessages"
        @select-suggestion="useSuggestion"
      />
    </div>
    
    <!-- 输入区域 -->
    <div class="input-area">
      <div class="textarea-wrapper">
        <textarea 
          ref="textareaRef"
          v-model="userInput" 
          placeholder="输入您的问题或需求..."
          @keydown.enter.ctrl.prevent="sendMessage"
          rows="1"
          @input="autoResizeTextarea"
        ></textarea>
        
        <button 
          v-if="showVoiceInput"
          class="voice-btn" 
          :class="{ 'recording': isRecording }"
          @click="startVoiceInput"
          :title="isRecording ? '正在录音' : '语音输入'"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path v-if="!isRecording" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11a7 7 0 01-7 7m0 0a7 7 0 01-7-7m7 7v4m0 0H8m4 0h4m-4-8a3 3 0 01-3-3V5a3 3 0 116 0v6a3 3 0 01-3 3z" />
            <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </button>
      </div>
      
      <div class="input-actions">
        <div class="actions-left">
          <div class="functionalities">
            <button 
              v-for="func in functionalities" 
              :key="func.id"
              class="func-btn"
              @click="usePrompt(func)"
              :title="func.name"
            >
              <svg v-if="func.icon === 'document-text'" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
              
              <svg v-if="func.icon === 'document-question'" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              
              <svg v-if="func.icon === 'light-bulb'" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
              </svg>
              
              <svg v-if="func.icon === 'chat'" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
              </svg>
              
              <svg v-if="func.icon === 'users'" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
              </svg>
            </button>
          </div>
          
          <button 
            class="suggestion-toggle-btn"
            @click="toggleSuggestions"
            :title="showSuggestions ? '隐藏建议' : '显示建议'"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path v-if="showSuggestions" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
              <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 15l7-7 7 7" />
            </svg>
            {{ showSuggestions ? '收起建议' : '查看建议' }}
          </button>
        </div>
        
        <div class="actions-right">
          <div class="generate-buttons">
            <button
              class="generate-btn lesson-btn"
              title="生成完整教案"
              @click="requestGenerate('lesson')"
            >
              生成教案
            </button>
            <button
              class="generate-btn question-btn"
              title="生成习题集"
              @click="requestGenerate('questions')"
            >
              生成习题
            </button>
          </div>

          <button class="send-btn" @click="sendMessage" :disabled="!userInput.trim()">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
            </svg>
          </button>
        </div>
      </div>
      
      <div class="input-tips">
        按 Ctrl+Enter 发送消息
      </div>
    </div>
    
    <!-- 上下文菜单 -->
    <div 
      v-if="contextMenu.show"
      class="context-menu"
      :style="{
        top: `${contextMenu.y}px`,
        left: `${contextMenu.x}px`
      }"
      @click.stop
    >
      <div 
        class="context-menu-item"
        @click="saveMessage(contextMenu.message)"
        v-if="!contextMenu.message.saved"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z" />
        </svg>
        保存消息
      </div>
      
      <div 
        class="context-menu-item"
        @click="copyMessageContent(contextMenu.message)"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7v8a2 2 0 002 2h6M8 7V5a2 2 0 012-2h4.586a1 1 0 01.707.293l4.414 4.414a1 1 0 01.293.707V15a2 2 0 01-2 2h-2M8 7H6a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2v-2" />
        </svg>
        复制内容
      </div>
      
      <div 
        class="context-menu-item delete"
        @click="deleteMessage(contextMenu.message)"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
        </svg>
        删除消息
      </div>
    </div>
    
    <!-- 点击其他地方关闭上下文菜单 -->
    <div 
      v-if="contextMenu.show"
      class="context-menu-backdrop"
      @click="closeContextMenu"
    ></div>
  </div>
</template>

<style scoped>
.chat-container {
  @apply flex flex-col h-full relative;
}

.messages-container {
  @apply flex-1 overflow-y-auto p-4 space-y-4;
  max-height: calc(100vh - 200px);
  padding-bottom: 1rem;
}

.message {
  @apply rounded-lg p-3 md:p-4 max-w-[85%] relative transition-all duration-300 hover:shadow-md;
  font-size: 0.95rem;
}

.user-message {
  @apply bg-blue-100 ml-auto;
}

.assistant-message {
  @apply bg-gray-100 mr-auto;
}

.context-message {
  @apply bg-yellow-50 border border-yellow-200 mx-auto w-full;
}

.message-sender {
  @apply font-medium mb-1 text-xs md:text-sm;
}

.user-message .message-sender {
  @apply text-blue-700;
}

.assistant-message .message-sender {
  @apply text-gray-700;
}

.message-content {
  @apply text-gray-800 whitespace-pre-line text-sm md:text-base;
}

.context-content {
  @apply text-yellow-800;
}

.context-header {
  @apply text-xs md:text-sm font-medium text-yellow-600 mb-2;
}

.context-text {
  @apply text-yellow-900 text-sm;
}

.message-meta {
  @apply text-xs text-gray-500 mt-2 flex justify-between;
}

.message-saved {
  @apply text-green-600;
}

.loading-indicator {
  @apply flex space-x-1 p-3 bg-gray-100 rounded-lg mr-auto;
}

.dot {
  @apply h-2 w-2 bg-blue-600 rounded-full animate-bounce;
}

.dot:nth-child(2) {
  animation-delay: 0.2s;
}

.dot:nth-child(3) {
  animation-delay: 0.4s;
}

.suggestions-container {
  @apply px-3 md:px-4 py-2 border-t border-gray-200;
}

.input-area {
  @apply bg-white border-t border-gray-200 p-3 md:p-4;
}

.textarea-wrapper {
  @apply relative;
}

.textarea-wrapper textarea {
  @apply w-full px-3 py-2 md:px-4 md:py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all duration-200 mb-2 resize-none pr-10 text-sm md:text-base;
  max-height: 120px;
}

.voice-btn {
  @apply absolute right-3 top-2 md:top-3 p-1.5 rounded-full hover:bg-gray-100 text-gray-500 hover:text-gray-700 transition-all;
}

.voice-btn.recording {
  @apply text-red-600 bg-red-50 animate-pulse;
}

.input-actions {
  @apply flex flex-wrap justify-between items-center gap-y-2;
}

.actions-left {
  @apply flex flex-wrap items-center gap-2;
}

.functionalities {
  @apply flex flex-wrap space-x-1;
}

.func-btn {
  @apply p-1.5 md:p-2 bg-gray-100 text-gray-600 rounded-full hover:bg-gray-200 transition-colors;
}

.suggestion-toggle-btn {
  @apply flex items-center px-2 md:px-3 py-1 md:py-1.5 ml-1 md:ml-2 text-xs md:text-sm text-gray-600 hover:bg-gray-100 rounded-lg transition-colors;
}

.actions-right {
  @apply flex items-center space-x-2;
}

.generate-buttons {
  @apply flex space-x-2;
}

.generate-btn {
  @apply px-2 md:px-3 py-1 md:py-1.5 rounded-lg text-xs md:text-sm text-white transition-colors font-medium;
}

.lesson-btn {
  @apply bg-green-600 hover:bg-green-700;
}

.question-btn {
  @apply bg-purple-600 hover:bg-purple-700;
}

.send-btn {
  @apply p-2 bg-blue-600 text-white rounded-full hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed;
}

.input-tips {
  @apply text-xs text-gray-400 text-right mt-1;
}

.context-menu {
  @apply absolute z-50 bg-white rounded-lg shadow-lg py-1 w-48;
}

.context-menu-item {
  @apply flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 cursor-pointer;
}

.context-menu-item.delete {
  @apply text-red-600 hover:bg-red-50;
}

.context-menu-backdrop {
  @apply fixed inset-0 z-40;
}

@media (max-width: 640px) {
  .message {
    @apply max-w-[95%] p-3;
  }
  
  .message-content {
    @apply text-sm;
  }
  
  .generate-buttons {
    @apply hidden;
  }
  
  .suggestion-toggle-btn span {
    @apply hidden;
  }
  
  .input-actions {
    @apply flex-col items-start;
  }
  
  .actions-left {
    @apply mb-2 w-full justify-between;
  }
  
  .actions-right {
    @apply w-full justify-end;
  }
  
  .functionalities {
    @apply justify-center;
  }
  
  .func-btn {
    @apply p-1.5;
  }
  
  .func-btn svg {
    @apply h-4 w-4;
  }
  
  .input-tips {
    @apply hidden;
  }
}
</style>