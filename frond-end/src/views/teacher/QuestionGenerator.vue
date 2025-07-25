<template>
  <div class="container mx-auto px-4 py-6">
    <h1 class="text-2xl font-bold mb-6">智能题目生成</h1>
    
    <div class="grid grid-cols-1 lg:grid-cols-4 gap-6">
      <!-- 左侧配置面板 -->
      <div class="bg-white rounded-lg shadow-md p-6 lg:col-span-1">
        <h2 class="text-lg font-semibold mb-4">生成配置</h2>
        
        <div class="space-y-4">
          <div class="form-group">
            <label class="block text-sm font-medium text-gray-700 mb-1">知识点</label>
            <div class="relative">
              <input 
                type="text" 
                v-model="generationConfig.topic" 
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="输入知识点，如"变量声明""
              />
              <button 
                v-if="generationConfig.topic" 
                class="absolute right-2 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                @click="generationConfig.topic = ''"
              >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
          </div>
          
          <div class="form-group">
            <label class="block text-sm font-medium text-gray-700 mb-1">题型选择</label>
            <div class="grid grid-cols-2 gap-2">
              <div 
                v-for="type in questionTypes" 
                :key="type.value" 
                class="flex items-center"
              >
                <input 
                  type="checkbox" 
                  :id="`type-${type.value}`" 
                  v-model="generationConfig.selectedTypes" 
                  :value="type.value"
                  class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                />
                <label :for="`type-${type.value}`" class="ml-2 block text-sm text-gray-700">
                  {{ type.label }}
                </label>
              </div>
            </div>
          </div>
          
          <div class="form-group">
            <label class="block text-sm font-medium text-gray-700 mb-1">难度设置</label>
            <div class="flex items-center space-x-2">
              <input 
                type="range" 
                v-model="generationConfig.difficulty" 
                min="1" 
                max="5" 
                step="1"
                class="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer"
              />
              <span class="text-sm font-medium text-gray-700">{{ difficultyText }}</span>
            </div>
          </div>
          
          <div class="form-group">
            <label class="block text-sm font-medium text-gray-700 mb-1">生成数量</label>
            <div class="flex items-center">
              <button 
                class="px-2 py-1 border border-gray-300 rounded-l-md bg-gray-50 text-gray-600 hover:bg-gray-100"
                @click="decreaseCount"
              >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4" />
                </svg>
              </button>
              <input 
                type="number" 
                v-model="generationConfig.count" 
                min="1" 
                max="20"
                class="w-16 text-center px-2 py-1 border-t border-b border-gray-300 focus:outline-none focus:ring-0"
              />
              <button 
                class="px-2 py-1 border border-gray-300 rounded-r-md bg-gray-50 text-gray-600 hover:bg-gray-100"
                @click="increaseCount"
              >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
                </svg>
              </button>
            </div>
          </div>
          
          <div class="form-group">
            <label class="block text-sm font-medium text-gray-700 mb-1">额外要求</label>
            <textarea 
              v-model="generationConfig.requirements" 
              rows="3" 
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="输入特殊要求，如"包含实际应用场景""
            ></textarea>
          </div>
          
          <button 
            class="w-full px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
            @click="generateQuestions"
            :disabled="isGenerating || !isFormValid"
          >
            <span v-if="isGenerating" class="flex items-center justify-center">
              <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              生成中...
            </span>
            <span v-else>生成题目</span>
          </button>
        </div>
      </div>
      
      <!-- 右侧题目列表 -->
      <div class="lg:col-span-3">
        <div class="bg-white rounded-lg shadow-md p-6">
          <div class="flex justify-between items-center mb-4">
            <h2 class="text-lg font-semibold">题目列表</h2>
            <div class="flex space-x-2">
              <button 
                class="px-3 py-1.5 bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200 focus:outline-none flex items-center"
                @click="toggleSelectAll"
              >
                <input 
                  type="checkbox" 
                  :checked="isAllSelected" 
                  class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded mr-2"
                />
                {{ isAllSelected ? '取消全选' : '全选' }}
              </button>
              <button 
                class="px-3 py-1.5 bg-red-50 text-red-700 rounded-md hover:bg-red-100 focus:outline-none"
                @click="deleteSelected"
                :disabled="selectedQuestions.length === 0"
              >
                删除选中
              </button>
              <button 
                class="px-3 py-1.5 bg-green-50 text-green-700 rounded-md hover:bg-green-100 focus:outline-none"
                @click="saveToQuestionBank"
                :disabled="questions.length === 0"
              >
                保存到题库
              </button>
            </div>
          </div>
          
          <!-- 题目筛选 -->
          <div class="flex flex-wrap gap-2 mb-4">
            <button 
              class="px-3 py-1 rounded-full text-sm font-medium"
              :class="activeFilter === 'all' ? 'bg-blue-100 text-blue-800' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'"
              @click="setFilter('all')"
            >
              全部
            </button>
            <button 
              v-for="type in questionTypes" 
              :key="`filter-${type.value}`"
              class="px-3 py-1 rounded-full text-sm font-medium"
              :class="activeFilter === type.value ? 'bg-blue-100 text-blue-800' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'"
              @click="setFilter(type.value)"
            >
              {{ type.label }}
            </button>
          </div>
          
          <!-- 题目表格 -->
          <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200">
              <thead class="bg-gray-50">
                <tr>
                  <th scope="col" class="w-12 px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    选择
                  </th>
                  <th scope="col" class="w-16 px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    序号
                  </th>
                  <th scope="col" class="w-24 px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    题型
                  </th>
                  <th scope="col" class="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    题目内容
                  </th>
                  <th scope="col" class="w-24 px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    难度
                  </th>
                  <th scope="col" class="w-24 px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    操作
                  </th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-gray-200">
                <tr v-if="filteredQuestions.length === 0">
                  <td colspan="6" class="px-3 py-4 text-center text-gray-500">
                    <div v-if="isGenerating" class="flex flex-col items-center py-8">
                      <svg class="animate-spin h-8 w-8 text-blue-500 mb-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                      </svg>
                      <p>正在生成题目，请稍候...</p>
                    </div>
                    <div v-else class="py-8">
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 mx-auto text-gray-300 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                      </svg>
                      <p>暂无题目，请点击左侧"生成题目"按钮</p>
                    </div>
                  </td>
                </tr>
                <tr 
                  v-for="(question, index) in filteredQuestions" 
                  :key="question.id"
                  :class="{'bg-blue-50': question.isEditing}"
                >
                  <td class="px-3 py-4 whitespace-nowrap">
                    <input 
                      type="checkbox" 
                      v-model="question.selected" 
                      class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                    />
                  </td>
                  <td class="px-3 py-4 whitespace-nowrap">
                    <span class="text-sm text-gray-900">{{ index + 1 }}</span>
                  </td>
                  <td class="px-3 py-4 whitespace-nowrap">
                    <span 
                      class="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full"
                      :class="getTypeClass(question.type)"
                    >
                      {{ getTypeLabel(question.type) }}
                    </span>
                  </td>
                  <td class="px-3 py-4">
                    <div v-if="question.isEditing">
                      <textarea 
                        v-model="question.content" 
                        rows="3" 
                        class="w-full px-2 py-1 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                      ></textarea>
                      <div class="mt-2">
                        <label class="block text-xs font-medium text-gray-700 mb-1">答案</label>
                        <textarea 
                          v-model="question.answer" 
                          rows="2" 
                          class="w-full px-2 py-1 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                        ></textarea>
                      </div>
                    </div>
                    <div v-else>
                      <div class="text-sm text-gray-900 whitespace-pre-line">{{ question.content }}</div>
                      <div v-if="showAnswer" class="mt-2 text-sm text-gray-500 whitespace-pre-line">
                        <span class="font-medium">答案：</span>{{ question.answer }}
                      </div>
                    </div>
                  </td>
                  <td class="px-3 py-4 whitespace-nowrap">
                    <div v-if="question.isEditing" class="flex items-center">
                      <select 
                        v-model="question.difficulty" 
                        class="block w-full pl-3 pr-10 py-1 text-base border-gray-300 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm rounded-md"
                      >
                        <option value="1">简单</option>
                        <option value="2">较简单</option>
                        <option value="3">中等</option>
                        <option value="4">较难</option>
                        <option value="5">困难</option>
                      </select>
                    </div>
                    <div v-else class="flex items-center">
                      <div class="flex">
                        <svg 
                          v-for="i in 5" 
                          :key="i" 
                          xmlns="http://www.w3.org/2000/svg" 
                          class="h-4 w-4" 
                          :class="i <= question.difficulty ? 'text-yellow-400' : 'text-gray-300'"
                          fill="currentColor" 
                          viewBox="0 0 24 24"
                        >
                          <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z" />
                        </svg>
                      </div>
                    </div>
                  </td>
                  <td class="px-3 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <div class="flex space-x-2">
                      <button 
                        v-if="question.isEditing" 
                        class="text-green-600 hover:text-green-900"
                        @click="saveEdit(question)"
                      >
                        保存
                      </button>
                      <button 
                        v-else 
                        class="text-blue-600 hover:text-blue-900"
                        @click="startEdit(question)"
                      >
                        编辑
                      </button>
                      <button 
                        class="text-red-600 hover:text-red-900"
                        @click="deleteQuestion(question)"
                      >
                        删除
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          
          <!-- 控制栏 -->
          <div class="mt-4 flex justify-between items-center">
            <div class="flex items-center">
              <input 
                type="checkbox" 
                id="show-answers" 
                v-model="showAnswer" 
                class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              />
              <label for="show-answers" class="ml-2 block text-sm text-gray-700">
                显示答案
              </label>
            </div>
            <div class="text-sm text-gray-500">
              共 {{ filteredQuestions.length }} 道题目
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'

// 题型定义
const questionTypes = [
  { value: 'single', label: '单选题' },
  { value: 'multiple', label: '多选题' },
  { value: 'blank', label: '填空题' },
  { value: 'shortAnswer', label: '简答题' },
  { value: 'programming', label: '编程题' },
  { value: 'matching', label: '匹配题' },
]

// 生成配置
const generationConfig = reactive({
  topic: '',
  selectedTypes: ['single', 'blank'],
  difficulty: 3,
  count: 5,
  requirements: ''
})

// 状态
const isGenerating = ref(false)
const showAnswer = ref(true)
const activeFilter = ref('all')
const questions = ref([])

// 计算难度文本
const difficultyText = computed(() => {
  switch (parseInt(generationConfig.difficulty)) {
    case 1: return '简单'
    case 2: return '较简单'
    case 3: return '中等'
    case 4: return '较难'
    case 5: return '困难'
    default: return '中等'
  }
})

// 表单验证
const isFormValid = computed(() => {
  return generationConfig.topic.trim() !== '' && 
         generationConfig.selectedTypes.length > 0 && 
         generationConfig.count > 0
})

// 过滤后的题目
const filteredQuestions = computed(() => {
  if (activeFilter.value === 'all') {
    return questions.value
  } else {
    return questions.value.filter(q => q.type === activeFilter.value)
  }
})

// 选中的题目
const selectedQuestions = computed(() => {
  return questions.value.filter(q => q.selected)
})

// 是否全部选中
const isAllSelected = computed(() => {
  return questions.value.length > 0 && questions.value.every(q => q.selected)
})

// 增减生成数量
const increaseCount = () => {
  if (generationConfig.count < 20) {
    generationConfig.count++
  }
}

const decreaseCount = () => {
  if (generationConfig.count > 1) {
    generationConfig.count--
  }
}

// 设置过滤器
const setFilter = (filter) => {
  activeFilter.value = filter
}

// 全选/取消全选
const toggleSelectAll = () => {
  const newValue = !isAllSelected.value
  questions.value.forEach(q => q.selected = newValue)
}

// 删除选中题目
const deleteSelected = () => {
  if (selectedQuestions.value.length === 0) return
  
  if (confirm(`确定要删除选中的 ${selectedQuestions.value.length} 道题目吗？`)) {
    questions.value = questions.value.filter(q => !q.selected)
  }
}

// 删除单个题目
const deleteQuestion = (question) => {
  const index = questions.value.findIndex(q => q.id === question.id)
  if (index !== -1) {
    questions.value.splice(index, 1)
  }
}

// 开始编辑
const startEdit = (question) => {
  // 先关闭其他正在编辑的题目
  questions.value.forEach(q => {
    if (q.id !== question.id) {
      q.isEditing = false
    }
  })
  
  question.isEditing = true
}

// 保存编辑
const saveEdit = (question) => {
  question.isEditing = false
}

// 保存到题库
const saveToQuestionBank = () => {
  // 实际项目中应该发送到服务器
  alert(`已将 ${questions.value.length} 道题目保存到题库`)
}

// 获取题型样式
const getTypeClass = (type) => {
  switch (type) {
    case 'single': return 'bg-blue-100 text-blue-800'
    case 'multiple': return 'bg-indigo-100 text-indigo-800'
    case 'blank': return 'bg-green-100 text-green-800'
    case 'shortAnswer': return 'bg-yellow-100 text-yellow-800'
    case 'programming': return 'bg-purple-100 text-purple-800'
    case 'matching': return 'bg-pink-100 text-pink-800'
    default: return 'bg-gray-100 text-gray-800'
  }
}

// 获取题型标签
const getTypeLabel = (type) => {
  const found = questionTypes.find(t => t.value === type)
  return found ? found.label : '未知题型'
}

// 生成题目
const generateQuestions = async () => {
  if (!isFormValid.value) return
  
  isGenerating.value = true
  
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    // 生成题目
    const newQuestions = []
    
    for (let i = 0; i < generationConfig.count; i++) {
      // 随机选择一个题型
      const typeIndex = Math.floor(Math.random() * generationConfig.selectedTypes.length)
      const type = generationConfig.selectedTypes[typeIndex]
      
      let content = ''
      let answer = ''
      
      // 根据题型生成不同的内容
      switch (type) {
        case 'single':
          content = `关于${generationConfig.topic}，下列说法正确的是：\nA. 选项A\nB. 选项B\nC. 选项C\nD. 选项D`
          answer = '答案：B'
          break
        case 'multiple':
          content = `关于${generationConfig.topic}，下列说法正确的有：\nA. 选项A\nB. 选项B\nC. 选项C\nD. 选项D`
          answer = '答案：A, C'
          break
        case 'blank':
          content = `请填写${generationConfig.topic}的定义：___________`
          answer = '答案：这里是填空题答案'
          break
        case 'shortAnswer':
          content = `请简述${generationConfig.topic}的主要特点和应用场景。`
          answer = '答案：这里是简答题答案，包含多行内容...\n第二行内容\n第三行内容'
          break
        case 'programming':
          content = `请编写一个函数，实现${generationConfig.topic}的功能。`
          answer = '答案：\n```javascript\nfunction example() {\n  // 代码实现\n  return result;\n}\n```'
          break
        case 'matching':
          content = `请将左侧概念与右侧定义进行匹配：\n1. 概念A\n2. 概念B\n3. 概念C\nA. 定义A\nB. 定义B\nC. 定义C`
          answer = '答案：1-C, 2-A, 3-B'
          break
      }
      
      newQuestions.push({
        id: Date.now() + i,
        type,
        content,
        answer,
        difficulty: generationConfig.difficulty,
        selected: false,
        isEditing: false
      })
    }
    
    // 添加到题目列表
    questions.value = [...questions.value, ...newQuestions]
    
  } catch (error) {
    console.error('生成题目失败:', error)
    alert('生成题目失败，请重试')
  } finally {
    isGenerating.value = false
  }
}
</script>

<style scoped>
/* 自定义滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 10px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style> 