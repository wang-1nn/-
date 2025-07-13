<script setup>
import { ref, nextTick, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { get, post } from '@/net'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/counter'

const route = useRoute()
const authStore = useAuthStore()
const courseId = route.params.courseId
const messages = ref([])
const loading = ref(false)

// 新消息内容
const newMsg = ref('')
// 当前回复的消息ID
const replyingTo = ref(null)
// 排序方式
const sortBy = ref('time')
// 过滤选项
const filterOption = ref('all')
// 是否显示表情选择器
const showEmoji = ref(false)
// 表情列表
const emojis = ['👍', '👏', '🎉', '❤️', '😊', '🤔', '👨‍🎓', '📚', '🧮', '⚡']

// 获取课程讨论列表
const fetchDiscussions = () => {
  loading.value = true
  
  get(`/api/student/courses/${courseId}/discussions`, null, 
    (message, data) => {
      console.log("获取到的讨论数据:", data);
      messages.value = data || [];
      // 格式化时间和添加其他必要字段
      if (messages.value && messages.value.length > 0) {
        messages.value = messages.value.map(item => {
          return {
            ...item,
            user: item.userName || '未知用户',
            time: formatTime(item.publishTime),
            likes: item.likeCount || 0,
            role: item.isTeacher ? 'teacher' : 'student',
            replies: item.replies || []
          }
        });
        console.log("格式化后的讨论数据:", messages.value);
      } else {
        console.log("未获取到讨论数据或数据为空");
      }
      
      // 检查所有讨论的点赞状态
      messages.value.forEach(message => {
        if (message && message.id) {
          checkLikeStatus(message.id);
        }
      });
      
      loading.value = false
    },
    (message) => {
      console.error("获取课程讨论失败:", message);
      ElMessage.error(message || '获取课程讨论失败')
      loading.value = false
    }
  )
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '';
  
  const date = new Date(timeStr);
  const now = new Date();
  const diff = now - date;
  
  // 一天内显示"今天 HH:MM"
  if (diff < 24 * 60 * 60 * 1000 && date.getDate() === now.getDate()) {
    return `今天 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
  }
  
  // 一周内显示"周几"
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    const days = ['日', '一', '二', '三', '四', '五', '六'];
    return `周${days[date.getDay()]} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
  }
  
  // 其他显示日期
  return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
}

// 获取讨论详情
const fetchDiscussionDetail = (discussionId) => {
  loading.value = true
  
  get(`/api/student/discussions/${discussionId}`, null, 
    (message, data) => {
      // 更新讨论详情，包括回复列表
      const index = messages.value.findIndex(m => m.id === discussionId)
      if (index !== -1 && data) {
        // 格式化回复数据
        if (data.replies && data.replies.length > 0) {
          data.replies = data.replies.map(reply => {
            return {
              ...reply,
              user: reply.userName || '未知用户',
              time: formatTime(reply.replyTime),
              likes: reply.likeCount || 0,
              role: reply.isTeacher ? 'teacher' : 'student'
            }
          });
        }
        
        // 更新讨论数据
        messages.value[index] = {
          ...data,
          user: data.userName || '未知用户',
          time: formatTime(data.publishTime),
          likes: data.likeCount || 0,
          role: data.isTeacher ? 'teacher' : 'student'
        };
        
        // 检查点赞状态
        checkLikeStatus(discussionId);
      }
      loading.value = false
    },
    (message) => {
      ElMessage.error(message || '获取讨论详情失败')
      loading.value = false
    }
  )
}

// 检查点赞状态
const checkLikeStatus = (discussionId) => {
  if (!discussionId) return;
  
  get('/api/discussions/check-like', {
    userId: authStore.user?.userId,
    discussionId: discussionId
  }, 
  (message, data) => {
    const index = messages.value.findIndex(m => m.id === discussionId);
    if (index !== -1) {
      messages.value[index].isLiked = data?.isLiked || false;
    }
  },
  (error) => {
    console.error('检查点赞状态失败:', error);
  })
}

// 排序后的消息
const sortedMessages = computed(() => {
  let result = [...messages.value]
  
  // 根据过滤选项筛选
  if (filterOption.value === 'questions') {
    result = result.filter(m => m?.content?.includes('?') || m?.content?.includes('？'))
  } else if (filterOption.value === 'teacher') {
    result = result.filter(m => m?.role === 'teacher' || m?.role === 'assistant')
  }
  
  // 根据排序方式排序
  if (sortBy.value === 'time') {
    result.sort((a, b) => new Date(b?.publishTime || 0) - new Date(a?.publishTime || 0))
  } else if (sortBy.value === 'likes') {
    result.sort((a, b) => (b?.likeCount || 0) - (a?.likeCount || 0))
  }
  
  return result
})

// 发送消息
const send = () => {
  const txt = newMsg.value.trim()
  if (!txt) return
  
  loading.value = true
  
  // 构造请求数据
  const requestData = {
    userId: authStore.user?.userId,
    content: txt
  }
  
  if (replyingTo.value) {
    // 回复消息
    post(`/api/student/discussions/${replyingTo.value}/replies`, requestData,
      (message) => {
        // 重新获取讨论详情，包含新的回复
        fetchDiscussionDetail(replyingTo.value)
        newMsg.value = ''
        replyingTo.value = null
        ElMessage.success('回复成功')
        loading.value = false
      },
      (message) => {
        ElMessage.error(message || '回复失败')
        loading.value = false
      },
      undefined,
      true // 确保使用JSON格式
    )
  } else {
    // 发送新讨论
    requestData.title = txt.substring(0, Math.min(30, txt.length)) + (txt.length > 30 ? '...' : '')
    requestData.courseId = courseId
    
    post(`/api/student/courses/${courseId}/discussions`, requestData, 
      (message) => {
        // 重新获取讨论列表
        fetchDiscussions()
        newMsg.value = ''
        ElMessage.success('发布讨论成功')
        loading.value = false
      },
      (message) => {
        ElMessage.error(message || '发送失败')
        loading.value = false
      },
      undefined,
      true // 确保使用JSON格式
    )
  }
}

// 点赞
const likeMessage = (message) => {
  if (!message || !message.id) return;
  
  post(`/api/discussions/like`, { 
    userId: authStore.user?.userId,
    discussionId: message.id
  }, 
  (responseMessage, data) => {
    const isLiked = data?.isLiked;
    ElMessage({
      message: isLiked ? '点赞成功' : '取消点赞成功',
      type: 'success',
      duration: 1000
    })
    // 更新点赞状态和数量
    message.isLiked = isLiked;
    message.likeCount = isLiked ? 
      (message.likeCount || 0) + 1 : 
      Math.max((message.likeCount || 1) - 1, 0);
    message.likes = message.likeCount;
  },
  (errorMessage) => {
    ElMessage.error(errorMessage || '操作失败')
  })
}

// 回复消息
const replyTo = (messageId) => {
  replyingTo.value = messageId
  const message = messages.value.find(m => m.id === messageId)
  if (message) {
    newMsg.value = `@${message.userName || message.user || '用户'} `
  }
  
  nextTick(() => {
    document.querySelector('.message-input').focus()
  })
}

// 取消回复
const cancelReply = () => {
  replyingTo.value = null
  newMsg.value = ''
}

// 插入表情
const insertEmoji = (emoji) => {
  newMsg.value += emoji
  showEmoji.value = false
}

// 滚动到底部
const scrollToBottom = () => {
  const list = document.querySelector('.disc-list')
  if (list) list.scrollTop = list.scrollHeight
}

// 获取角色样式类
const getRoleClass = (role) => {
  switch (role) {
    case 'teacher': return 'teacher-message'
    case 'assistant': return 'assistant-message'
    default: return 'student-message'
  }
}

onMounted(() => {
  fetchDiscussions()
})
</script>

<template>
  <div class="discussion bg-white p-5 h-full flex flex-col">
    <!-- 标题和工具栏 -->
    <div class="flex justify-between items-center mb-4 pb-3 border-b">
      <h3 class="font-medium text-lg flex items-center">
        <i class="fas fa-comments text-blue-500 mr-2"></i> 课程讨论
      </h3>
      <div class="flex gap-3">
        <el-select v-model="filterOption" size="small" class="w-28">
          <el-option label="全部消息" value="all" />
          <el-option label="仅问题" value="questions" />
          <el-option label="教师回复" value="teacher" />
        </el-select>
        <el-select v-model="sortBy" size="small" class="w-28">
          <el-option label="最新优先" value="time" />
          <el-option label="热度优先" value="likes" />
        </el-select>
      </div>
    </div>
    
    <!-- 消息列表 -->
    <div class="disc-list flex-1 overflow-y-auto space-y-5 pr-1 pb-2">
      <template v-if="sortedMessages && sortedMessages.length > 0">
        <div v-for="message in sortedMessages" :key="message.id" class="message-container">
          <!-- 主消息 -->
          <div class="message-item" :class="getRoleClass(message.role)">
            <img :src="message.userAvatar || '/avatars/default.jpg'" class="avatar" />
            <div class="message-content">
              <div class="message-header">
                <span class="user-name">{{ message.userName || message.user }}</span>
                <span class="message-time">{{ message.time }}</span>
              </div>
              <div class="message-text">{{ message.content }}</div>
              <div class="message-actions">
                <button @click="likeMessage(message)" class="action-btn" :class="{'liked': message.isLiked}">
                  <i class="fas fa-thumbs-up"></i> {{ message.likeCount || 0 }}
                </button>
                <button @click="replyTo(message.id)" class="action-btn">
                  <i class="fas fa-reply"></i> 回复
                </button>
              </div>
            </div>
          </div>
          
          <!-- 回复消息 -->
          <div v-if="message.replies && message.replies.length > 0" class="replies-container">
            <div v-for="reply in message.replies" :key="reply.id" class="reply-item" :class="getRoleClass(reply.role)">
              <img :src="reply.userAvatar || '/avatars/default.jpg'" class="avatar avatar-sm" />
              <div class="message-content">
                <div class="message-header">
                  <span class="user-name">{{ reply.userName || reply.user }}</span>
                  <span class="message-time">{{ reply.time }}</span>
                </div>
                <div class="message-text">{{ reply.content }}</div>
                <div class="message-actions">
                  <button @click="likeMessage(reply)" class="action-btn" :class="{'liked': reply.isLiked}">
                    <i class="fas fa-thumbs-up"></i> {{ reply.likeCount || 0 }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
      <div v-else class="empty-state">
        <div class="text-center py-8 text-gray-400">
          <i class="fas fa-comments text-5xl mb-3 opacity-30"></i>
          <p class="text-lg">暂无讨论</p>
          <p class="text-sm mt-2">成为第一个发起讨论的人吧！</p>
        </div>
      </div>
    </div>
    
    <!-- 输入框 -->
    <div class="message-input-container mt-4 pt-4 border-t">
      <!-- 回复提示 -->
      <div v-if="replyingTo !== null" class="bg-blue-50 text-blue-600 p-2 rounded-md mb-2 flex justify-between items-center">
        <span>回复中...</span>
        <el-button type="text" size="small" @click="cancelReply">
          <i class="fas fa-times"></i>
        </el-button>
      </div>
      
      <div class="relative">
        <el-input
          v-model="newMsg"
          type="textarea"
          :rows="3"
          placeholder="在这里输入您的讨论内容..."
          resize="none"
          class="message-input"
        />
        
        <!-- 表情按钮 -->
        <div class="absolute right-2 bottom-2 flex items-center">
          <el-button 
            type="text" 
            @click="showEmoji = !showEmoji"
            circle
            :class="{ 'text-blue-500': showEmoji }"
          >
            <i class="fas fa-smile"></i>
          </el-button>
        </div>
        
        <!-- 表情选择器 -->
        <div v-if="showEmoji" class="emoji-picker">
          <button 
            v-for="emoji in emojis" 
            :key="emoji"
            class="emoji-item"
            @click="insertEmoji(emoji)"
          >
            {{ emoji }}
          </button>
        </div>
      </div>
      
      <div class="flex justify-end mt-3">
        <el-button 
          type="primary" 
          :loading="loading" 
          @click="send"
          :disabled="!newMsg.trim()"
        >
          {{ replyingTo !== null ? '回复' : '发布讨论' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.disc-list::-webkit-scrollbar{width:4px;height:4px;}
.disc-list::-webkit-scrollbar-thumb{background:#e5e7eb;border-radius:2px;}

.message-container {
  margin-bottom: 1.5rem;
}

.message-item {
  display: flex;
  gap: 0.75rem;
}

.replies-container {
  margin-left: 2.5rem;
  margin-top: 0.75rem;
  padding-left: 0.75rem;
  border-left: 2px solid #f0f0f0;
}

.reply-item {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.avatar {
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 50%;
  object-fit: cover;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.avatar-sm {
  width: 2rem;
  height: 2rem;
}

.message-content {
  flex: 1;
}

.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.25rem;
}

.user-name {
  font-weight: 500;
  font-size: 0.9rem;
}

.message-time {
  color: #9ca3af;
  font-size: 0.75rem;
}

.message-text {
  background-color: #f9fafb;
  padding: 0.75rem 1rem;
  border-radius: 0.75rem;
  font-size: 0.95rem;
  line-height: 1.5;
}

.message-actions {
  display: flex;
  gap: 1rem;
  margin-top: 0.5rem;
  padding-left: 0.25rem;
}

.action-btn {
  color: #6b7280;
  font-size: 0.8rem;
  display: flex;
  align-items: center;
  gap: 0.25rem;
  transition: color 0.2s;
}

.action-btn:hover {
  color: #4b5563;
}

.action-btn.liked {
  color: #3b82f6;
  font-weight: 500;
}

.action-btn.liked i {
  color: #3b82f6;
}

.message-input {
  width: 100%;
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
  padding: 0.75rem 1rem;
  font-size: 0.95rem;
  resize: none;
  outline: none;
  transition: border-color 0.2s;
}

.message-input:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
}

.tool-btn {
  background-color: #f3f4f6;
  color: #4b5563;
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
}

.tool-btn:hover {
  background-color: #e5e7eb;
}

.send-btn {
  background-color: #3b82f6;
  color: white;
  padding: 0 1rem;
  height: 2.5rem;
  border-radius: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
  font-weight: 500;
}

.send-btn:hover {
  background-color: #2563eb;
}

.send-btn:disabled {
  background-color: #93c5fd;
  cursor: not-allowed;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 0;
}

.reply-indicator {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #f3f4f6;
  padding: 0.5rem 1rem;
  border-radius: 0.5rem;
  margin-bottom: 0.5rem;
  font-size: 0.85rem;
  color: #4b5563;
}

.emoji-picker {
  position: absolute;
  bottom: 100%;
  right: 0;
  margin-bottom: 0.5rem;
  background-color: white;
  border-radius: 0.5rem;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  padding: 0.5rem;
  z-index: 10;
  display: flex;
  flex-wrap: wrap;
  width: 180px;
}

.emoji-item {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.2s;
  font-size: 1.2rem;
}

.emoji-item:hover {
  background-color: #f3f4f6;
}

/* 角色样式 */
.teacher-message .message-text {
  background-color: #ecf5ff;
  border-left: 3px solid #409eff;
}

.assistant-message .message-text {
  background-color: #f0f9eb;
  border-left: 3px solid #67c23a;
}

.student-message .message-text {
  background-color: #f9fafb;
}

/* 动画效果 */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.message-container {
  animation: fadeIn 0.3s ease-out;
}
</style>