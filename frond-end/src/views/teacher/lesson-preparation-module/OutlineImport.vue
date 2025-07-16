<template>
  <div class="container mx-auto px-4 py-6">
    <h1 class="text-2xl font-bold mb-6">课程大纲导入</h1>
    
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- 上传区域 -->
      <div class="bg-white rounded-lg shadow-md p-6">
        <h2 class="text-xl font-semibold mb-4">上传课程大纲</h2>
        <p class="text-gray-600 mb-4">
          支持上传 PDF 或 Word 格式的课程大纲文件，系统将通过 AI 分析提取结构化内容
        </p>
        
        <div 
          class="border-2 border-dashed border-gray-300 rounded-lg p-8 text-center hover:border-blue-500 transition-colors"
          :class="{ 'border-blue-500 bg-blue-50': isDragging }"
          @dragover.prevent="isDragging = true"
          @dragleave.prevent="isDragging = false"
          @drop.prevent="handleFileDrop"
        >
          <div v-if="!file">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 mx-auto text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
            </svg>
            <p class="mt-2 text-sm text-gray-600">拖放文件到此处，或</p>
            <button 
              class="mt-2 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
              @click="triggerFileInput"
            >
              选择文件
            </button>
            <input 
              ref="fileInput" 
              type="file" 
              class="hidden" 
              accept=".pdf,.doc,.docx" 
              @change="handleFileChange" 
            />
          </div>
          
          <div v-else class="text-left">
            <div class="flex items-center justify-between">
              <div class="flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-blue-500 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
                <div>
                  <p class="font-medium">{{ file.name }}</p>
                  <p class="text-sm text-gray-500">{{ formatFileSize(file.size) }}</p>
                </div>
              </div>
              <button 
                class="text-red-500 hover:text-red-700"
                @click="removeFile"
              >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
            
            <div class="mt-4">
              <button 
                class="w-full px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                @click="uploadFile"
                :disabled="isUploading"
              >
                <span v-if="!isUploading">解析大纲</span>
                <span v-else class="flex items-center justify-center">
                  <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  正在解析...
                </span>
              </button>
            </div>
          </div>
        </div>
        
        <div class="mt-4 text-sm text-gray-500">
          <p>支持的文件格式：PDF, DOC, DOCX</p>
          <p>最大文件大小：10MB</p>
        </div>
      </div>
      
      <!-- 解析结果 -->
      <div class="bg-white rounded-lg shadow-md p-6">
        <h2 class="text-xl font-semibold mb-4">解析结果</h2>
        
        <div v-if="!parseResult" class="text-center py-12 text-gray-500">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 mx-auto mb-4 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
          </svg>
          <p>上传并解析大纲后，结果将显示在这里</p>
        </div>
        
        <div v-else>
          <div class="mb-4 p-3 bg-green-50 border border-green-200 rounded-md text-green-700">
            <div class="flex items-start">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <p>大纲解析成功！系统已提取关键结构和内容</p>
            </div>
          </div>
          
          <div class="border border-gray-200 rounded-md overflow-hidden">
            <div class="bg-gray-50 px-4 py-2 border-b border-gray-200">
              <h3 class="font-medium">课程大纲结构</h3>
            </div>
            <div class="p-4 max-h-96 overflow-y-auto">
              <ul class="outline-tree">
                <li v-for="(item, index) in parseResult.outline" :key="index" class="mb-2">
                  <div class="flex items-center">
                    <span class="font-medium">{{ item.title }}</span>
                  </div>
                  <ul v-if="item.children && item.children.length" class="pl-6 mt-1">
                    <li v-for="(child, childIndex) in item.children" :key="`${index}-${childIndex}`" class="mb-1">
                      <div class="flex items-center">
                        <span>{{ child.title }}</span>
                      </div>
                      <ul v-if="child.children && child.children.length" class="pl-6 mt-1">
                        <li v-for="(grandchild, grandchildIndex) in child.children" :key="`${index}-${childIndex}-${grandchildIndex}`" class="text-sm text-gray-600">
                          {{ grandchild.title }}
                        </li>
                      </ul>
                    </li>
                  </ul>
                </li>
              </ul>
            </div>
          </div>
          
          <div class="mt-4">
            <h3 class="font-medium mb-2">课程信息</h3>
            <div class="grid grid-cols-2 gap-4">
              <div class="bg-gray-50 p-3 rounded-md">
                <p class="text-sm text-gray-500">课程名称</p>
                <p class="font-medium">{{ parseResult.courseInfo.name }}</p>
              </div>
              <div class="bg-gray-50 p-3 rounded-md">
                <p class="text-sm text-gray-500">适用年级</p>
                <p class="font-medium">{{ parseResult.courseInfo.grade }}</p>
              </div>
              <div class="bg-gray-50 p-3 rounded-md">
                <p class="text-sm text-gray-500">总课时</p>
                <p class="font-medium">{{ parseResult.courseInfo.totalHours }} 课时</p>
              </div>
              <div class="bg-gray-50 p-3 rounded-md">
                <p class="text-sm text-gray-500">学科</p>
                <p class="font-medium">{{ parseResult.courseInfo.subject }}</p>
              </div>
            </div>
          </div>
          
          <div class="mt-6 flex justify-end">
            <button 
              class="px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 mr-3"
              @click="resetForm"
            >
              重新上传
            </button>
            <button 
              class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
              @click="goToLessonDesigner"
            >
              前往教案设计
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const fileInput = ref(null)
const file = ref(null)
const isDragging = ref(false)
const isUploading = ref(false)
const parseResult = ref(null)

// 模拟解析结果
const mockParseResult = {
  outline: [
    {
      title: '第一单元：数据类型与变量',
      children: [
        {
          title: '1.1 基本数据类型',
          children: [
            { title: '整数类型' },
            { title: '浮点类型' },
            { title: '字符类型' },
            { title: '布尔类型' }
          ]
        },
        {
          title: '1.2 变量声明与赋值',
          children: [
            { title: '变量命名规则' },
            { title: '变量初始化' },
            { title: '常量定义' }
          ]
        }
      ]
    },
    {
      title: '第二单元：控制结构',
      children: [
        {
          title: '2.1 条件语句',
          children: [
            { title: 'if-else 语句' },
            { title: 'switch-case 语句' }
          ]
        },
        {
          title: '2.2 循环语句',
          children: [
            { title: 'for 循环' },
            { title: 'while 循环' },
            { title: 'do-while 循环' }
          ]
        }
      ]
    },
    {
      title: '第三单元：函数',
      children: [
        {
          title: '3.1 函数定义与调用',
          children: [
            { title: '函数声明' },
            { title: '参数传递' },
            { title: '返回值' }
          ]
        },
        {
          title: '3.2 函数高级特性',
          children: [
            { title: '递归函数' },
            { title: '函数重载' },
            { title: '内联函数' }
          ]
        }
      ]
    }
  ],
  courseInfo: {
    name: '程序设计基础',
    grade: '高中一年级',
    totalHours: 36,
    subject: '信息技术'
  }
}

// 触发文件选择
const triggerFileInput = () => {
  fileInput.value.click()
}

// 处理文件选择
const handleFileChange = (event) => {
  const selectedFile = event.target.files[0]
  if (selectedFile) {
    file.value = selectedFile
  }
}

// 处理文件拖放
const handleFileDrop = (event) => {
  isDragging.value = false
  const droppedFile = event.dataTransfer.files[0]
  if (droppedFile && (droppedFile.type === 'application/pdf' || 
                      droppedFile.type === 'application/msword' || 
                      droppedFile.type === 'application/vnd.openxmlformats-officedocument.wordprocessingml.document')) {
    file.value = droppedFile
  } else {
    alert('请上传 PDF 或 Word 格式的文件')
  }
}

// 移除文件
const removeFile = () => {
  file.value = null
  fileInput.value.value = ''
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 上传并解析文件
const uploadFile = () => {
  if (!file.value) return
  
  isUploading.value = true
  
  // 模拟上传和解析过程
  setTimeout(() => {
    parseResult.value = mockParseResult
    isUploading.value = false
  }, 2000)
  
  // 实际实现应该是这样的（需要后端支持）:
  /*
  const formData = new FormData()
  formData.append('file', file.value)
  
  axios.post('/api/outline/parse', formData)
    .then(response => {
      parseResult.value = response.data
      isUploading.value = false
    })
    .catch(error => {
      console.error('上传失败:', error)
      alert('上传失败，请重试')
      isUploading.value = false
    })
  */
}

// 重置表单
const resetForm = () => {
  file.value = null
  parseResult.value = null
  fileInput.value.value = ''
}

// 前往教案设计页面
const goToLessonDesigner = () => {
  // 将解析结果存储到状态管理或本地存储
  localStorage.setItem('outlineData', JSON.stringify(parseResult.value))
  
  // 导航到教案设计页面
  router.push('/teacher/lesson-designer')
}
</script>

<style scoped>
.outline-tree {
  list-style-type: none;
  padding-left: 0;
}

.outline-tree ul {
  list-style-type: none;
}
</style> 