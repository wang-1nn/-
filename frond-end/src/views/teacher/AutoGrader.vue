<template>
  <div class="container mx-auto px-4 py-6">
    <h1 class="text-2xl font-bold mb-6">自动批改</h1>
    
    <div class="grid grid-cols-1 lg:grid-cols-4 gap-6">
      <!-- 左侧批改队列 -->
      <div class="bg-white rounded-lg shadow-md p-6 lg:col-span-1">
        <h2 class="text-lg font-semibold mb-4">批改队列</h2>
        
        <!-- 上传区域 -->
        <div 
          class="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center hover:border-blue-500 transition-colors mb-6"
          :class="{ 'border-blue-500 bg-blue-50': isDragging }"
          @dragover.prevent="isDragging = true"
          @dragleave.prevent="isDragging = false"
          @drop.prevent="handleFileDrop"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-10 w-10 mx-auto text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
          </svg>
          <p class="mt-2 text-sm text-gray-600">拖放文件到此处，或</p>
          <button 
            class="mt-2 px-3 py-1.5 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
            @click="triggerFileInput"
          >
            选择文件
          </button>
          <input 
            ref="fileInput" 
            type="file" 
            multiple
            class="hidden" 
            accept=".pdf,.doc,.docx,.txt" 
            @change="handleFileChange" 
          />
          <p class="mt-2 text-xs text-gray-500">支持批量上传，每个文件最大 10MB</p>
        </div>
        
        <!-- 批改配置 -->
        <div class="mb-6">
          <h3 class="text-md font-medium mb-2">批改设置</h3>
          
          <div class="space-y-3">
            <div class="form-group">
              <label class="block text-sm font-medium text-gray-700 mb-1">批改模式</label>
              <select 
                v-model="gradingConfig.mode" 
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="standard">标准评分（按答案批改）</option>
                <option value="rubric">评分标准（按评分细则）</option>
                <option value="ai">AI 智能评分（综合评价）</option>
              </select>
            </div>
            
            <div class="form-group">
              <label class="block text-sm font-medium text-gray-700 mb-1">评分标准</label>
              <select 
                v-model="gradingConfig.standard" 
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="100">百分制（0-100分）</option>
                <option value="10">十分制（0-10分）</option>
                <option value="5">五分制（0-5分）</option>
                <option value="letter">等级制（A-F）</option>
              </select>
            </div>
            
            <div class="form-group">
              <label class="block text-sm font-medium text-gray-700 mb-1">参考答案/评分标准</label>
              <textarea 
                v-model="gradingConfig.reference" 
                rows="4" 
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="输入参考答案或评分标准..."
              ></textarea>
            </div>
          </div>
        </div>
        
        <!-- 批改按钮 -->
        <button 
          class="w-full px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
          @click="startGrading"
          :disabled="isGrading || queuedFiles.length === 0"
        >
          <span v-if="isGrading" class="flex items-center justify-center">
            <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            批改中...
          </span>
          <span v-else>开始批改 ({{ queuedFiles.length }})</span>
        </button>
      </div>
      
      <!-- 右侧批改结果 -->
      <div class="lg:col-span-3">
        <div class="bg-white rounded-lg shadow-md p-6">
          <h2 class="text-lg font-semibold mb-4">批改结果</h2>
          
          <!-- 批改状态标签 -->
          <div class="flex flex-wrap gap-2 mb-4">
            <button 
              class="px-3 py-1 rounded-full text-sm font-medium"
              :class="activeTab === 'all' ? 'bg-blue-100 text-blue-800' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'"
              @click="setActiveTab('all')"
            >
              全部 ({{ gradedFiles.length + queuedFiles.length }})
            </button>
            <button 
              class="px-3 py-1 rounded-full text-sm font-medium"
              :class="activeTab === 'pending' ? 'bg-yellow-100 text-yellow-800' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'"
              @click="setActiveTab('pending')"
            >
              待批改 ({{ queuedFiles.length }})
            </button>
            <button 
              class="px-3 py-1 rounded-full text-sm font-medium"
              :class="activeTab === 'completed' ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'"
              @click="setActiveTab('completed')"
            >
              已完成 ({{ gradedFiles.length }})
            </button>
          </div>
          
          <!-- 文件列表 -->
          <div class="overflow-hidden border border-gray-200 rounded-lg">
            <table class="min-w-full divide-y divide-gray-200">
              <thead class="bg-gray-50">
                <tr>
                  <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    文件名
                  </th>
                  <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    状态
                  </th>
                  <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    分数
                  </th>
                  <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    操作
                  </th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-gray-200">
                <tr v-if="displayFiles.length === 0">
                  <td colspan="4" class="px-4 py-4 text-center text-gray-500">
                    <p>暂无文件，请上传需要批改的文件</p>
                  </td>
                </tr>
                <tr v-for="file in displayFiles" :key="file.id" @click="selectFile(file)" :class="{'bg-blue-50': selectedFile && selectedFile.id === file.id}">
                  <td class="px-4 py-4 whitespace-nowrap">
                    <div class="flex items-center">
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                      </svg>
                      <div class="text-sm font-medium text-gray-900">{{ file.name }}</div>
                    </div>
                  </td>
                  <td class="px-4 py-4 whitespace-nowrap">
                    <span 
                      class="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full"
                      :class="{
                        'bg-yellow-100 text-yellow-800': file.status === 'pending',
                        'bg-blue-100 text-blue-800': file.status === 'processing',
                        'bg-green-100 text-green-800': file.status === 'completed'
                      }"
                    >
                      {{ 
                        file.status === 'pending' ? '待批改' : 
                        file.status === 'processing' ? '批改中' : '已完成'
                      }}
                    </span>
                  </td>
                  <td class="px-4 py-4 whitespace-nowrap">
                    <div v-if="file.status === 'completed'" class="text-sm text-gray-900">
                      {{ formatScore(file.score) }}
                    </div>
                    <div v-else class="text-sm text-gray-500">
                      -
                    </div>
                  </td>
                  <td class="px-4 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <button 
                      v-if="file.status === 'completed'"
                      class="text-blue-600 hover:text-blue-900 mr-3"
                      @click.stop="viewDetails(file)"
                    >
                      查看详情
                    </button>
                    <button 
                      class="text-red-600 hover:text-red-900"
                      @click.stop="removeFile(file)"
                    >
                      删除
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          
          <!-- 批改详情 -->
          <div v-if="selectedFile && selectedFile.status === 'completed'" class="mt-6 border border-gray-200 rounded-lg p-4">
            <h3 class="text-md font-medium mb-3">批改详情</h3>
            
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
              <div class="bg-gray-50 p-3 rounded-md">
                <p class="text-sm text-gray-500">文件名</p>
                <p class="font-medium">{{ selectedFile.name }}</p>
              </div>
              <div class="bg-gray-50 p-3 rounded-md">
                <p class="text-sm text-gray-500">分数</p>
                <p class="font-medium">{{ formatScore(selectedFile.score) }}</p>
              </div>
            </div>
            
            <div class="mb-4">
              <h4 class="text-sm font-medium text-gray-700 mb-2">评语</h4>
              <div class="bg-gray-50 p-3 rounded-md">
                <p class="text-sm whitespace-pre-line">{{ selectedFile.feedback }}</p>
              </div>
            </div>
            
            <div>
              <h4 class="text-sm font-medium text-gray-700 mb-2">详细点评</h4>
              <div class="bg-gray-50 p-3 rounded-md">
                <div v-for="(comment, index) in selectedFile.comments" :key="index" class="mb-2 last:mb-0">
                  <div class="flex items-start">
                    <div 
                      class="w-5 h-5 rounded-full flex items-center justify-center text-xs font-medium mr-2 mt-0.5"
                      :class="{
                        'bg-red-100 text-red-800': comment.type === 'error',
                        'bg-yellow-100 text-yellow-800': comment.type === 'warning',
                        'bg-green-100 text-green-800': comment.type === 'success'
                      }"
                    >
                      {{ index + 1 }}
                    </div>
                    <div>
                      <p class="text-sm">{{ comment.content }}</p>
                      <p v-if="comment.suggestion" class="text-xs text-gray-500 mt-1">建议：{{ comment.suggestion }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'

// 文件上传状态
const fileInput = ref(null)
const isDragging = ref(false)
const isGrading = ref(false)
const activeTab = ref('all')
const selectedFile = ref(null)

// 批改配置
const gradingConfig = reactive({
  mode: 'standard',
  standard: '100',
  reference: ''
})

// 文件队列
const queuedFiles = ref([])
const gradedFiles = ref([])

// 显示的文件列表
const displayFiles = computed(() => {
  if (activeTab.value === 'all') {
    return [...gradedFiles.value, ...queuedFiles.value]
  } else if (activeTab.value === 'pending') {
    return queuedFiles.value
  } else {
    return gradedFiles.value
  }
})

// 触发文件选择
const triggerFileInput = () => {
  fileInput.value.click()
}

// 处理文件选择
const handleFileChange = (event) => {
  const files = Array.from(event.target.files)
  addFilesToQueue(files)
  fileInput.value.value = ''
}

// 处理文件拖放
const handleFileDrop = (event) => {
  isDragging.value = false
  const files = Array.from(event.dataTransfer.files)
  addFilesToQueue(files)
}

// 添加文件到队列
const addFilesToQueue = (files) => {
  files.forEach(file => {
    const fileObj = {
      id: Date.now() + Math.random().toString(36).substr(2, 9),
      file: file,
      name: file.name,
      size: file.size,
      status: 'pending',
      score: null,
      feedback: '',
      comments: []
    }
    
    queuedFiles.value.push(fileObj)
  })
}

// 开始批改
const startGrading = async () => {
  if (queuedFiles.value.length === 0) return
  
  isGrading.value = true
  
  try {
    // 处理每个文件
    for (const file of queuedFiles.value) {
      // 更新状态为处理中
      file.status = 'processing'
      
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1500))
      
      // 生成模拟结果
      const score = Math.floor(Math.random() * 41) + 60 // 60-100分
      const feedback = generateFeedback(score)
      const comments = generateComments()
      
      // 更新文件状态
      file.status = 'completed'
      file.score = score
      file.feedback = feedback
      file.comments = comments
      
      // 移动到已批改列表
      gradedFiles.value.push(file)
    }
    
    // 清空队列
    queuedFiles.value = []
    
  } catch (error) {
    console.error('批改过程中出错:', error)
    alert('批改过程中出错，请重试')
  } finally {
    isGrading.value = false
  }
}

// 选择文件
const selectFile = (file) => {
  selectedFile.value = file
}

// 查看详情
const viewDetails = (file) => {
  selectedFile.value = file
}

// 移除文件
const removeFile = (file) => {
  if (file.status === 'pending' || file.status === 'processing') {
    const index = queuedFiles.value.findIndex(f => f.id === file.id)
    if (index !== -1) {
      queuedFiles.value.splice(index, 1)
    }
  } else {
    const index = gradedFiles.value.findIndex(f => f.id === file.id)
    if (index !== -1) {
      gradedFiles.value.splice(index, 1)
    }
  }
  
  if (selectedFile.value && selectedFile.value.id === file.id) {
    selectedFile.value = null
  }
}

// 设置活动标签
const setActiveTab = (tab) => {
  activeTab.value = tab
}

// 格式化分数
const formatScore = (score) => {
  if (gradingConfig.standard === 'letter') {
    if (score >= 90) return 'A'
    if (score >= 80) return 'B'
    if (score >= 70) return 'C'
    if (score >= 60) return 'D'
    return 'F'
  } else if (gradingConfig.standard === '10') {
    return (score / 10).toFixed(1)
  } else if (gradingConfig.standard === '5') {
    return (score / 20).toFixed(1)
  } else {
    return score
  }
}

// 生成反馈
const generateFeedback = (score) => {
  if (score >= 90) {
    return '这份作业表现出色，思路清晰，内容全面，论述有深度。能够很好地理解和应用所学知识，并有自己的见解。'
  } else if (score >= 80) {
    return '这份作业整体表现良好，内容较为完整，基本概念理解正确。有一些小的不足之处，但不影响整体质量。'
  } else if (score >= 70) {
    return '这份作业达到了基本要求，但内容深度和广度有待提高。部分概念理解不够透彻，论述可以更加充实。'
  } else {
    return '这份作业存在一些问题，内容不够完整，部分概念理解有误。建议重新学习相关内容，加强基础知识的掌握。'
  }
}

// 生成详细评论
const generateComments = () => {
  const commentTypes = ['success', 'warning', 'error']
  const count = Math.floor(Math.random() * 3) + 2 // 2-4条评论
  
  const comments = []
  for (let i = 0; i < count; i++) {
    const type = commentTypes[Math.floor(Math.random() * commentTypes.length)]
    
    let content = ''
    let suggestion = ''
    
    if (type === 'success') {
      content = '这一部分论述很有深度，展示了对知识点的良好理解。'
      suggestion = '可以进一步扩展，结合实际案例分析。'
    } else if (type === 'warning') {
      content = '这里的概念解释不够准确，需要更加严谨。'
      suggestion = '建议参考教材第X章相关内容，明确概念定义。'
    } else {
      content = '这部分存在明显错误，与课程内容不符。'
      suggestion = '需要重新学习相关知识点，纠正错误理解。'
    }
    
    comments.push({ type, content, suggestion })
  }
  
  return comments
}
</script>
