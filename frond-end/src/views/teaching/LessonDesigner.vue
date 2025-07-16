<template>
  <div class="container mx-auto px-4 py-6">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold">智能备课设计</h1>
      <div class="flex space-x-3">
        <button 
          class="px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2"
          @click="saveAsDraft"
        >
          保存草稿
        </button>
        <button 
          class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
          @click="publishLesson"
        >
          发布教案
        </button>
      </div>
    </div>
    
    <div class="grid grid-cols-1 lg:grid-cols-4 gap-6">
      <!-- 左侧大纲面板 -->
      <div class="bg-white rounded-lg shadow-md p-4 lg:col-span-1">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-lg font-semibold">课程大纲</h2>
          <button 
            class="text-blue-600 hover:text-blue-800 text-sm"
            @click="expandAll = !expandAll"
          >
            {{ expandAll ? '全部收起' : '全部展开' }}
          </button>
        </div>
        
        <div class="outline-tree max-h-[calc(100vh-220px)] overflow-y-auto pr-2">
          <template v-if="outlineData">
            <div v-for="(unit, unitIndex) in outlineData.outline" :key="`unit-${unitIndex}`" class="mb-3">
              <div 
                class="flex items-center justify-between p-2 rounded-md cursor-pointer hover:bg-gray-100"
                :class="{'bg-blue-50 border-l-4 border-blue-500': selectedNode === `unit-${unitIndex}`}"
                @click="toggleUnit(unitIndex); selectNode(`unit-${unitIndex}`)"
              >
                <div class="flex items-center">
                  <svg 
                    xmlns="http://www.w3.org/2000/svg" 
                    class="h-4 w-4 mr-1 transition-transform" 
                    :class="{'rotate-90': expandedUnits[unitIndex]}"
                    fill="none" 
                    viewBox="0 0 24 24" 
                    stroke="currentColor"
                  >
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                  </svg>
                  <span class="font-medium">{{ unit.title }}</span>
                </div>
                <button 
                  class="text-blue-600 hover:text-blue-800"
                  @click.stop="addToLessonPlan(unit)"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
                  </svg>
                </button>
              </div>
              
              <div v-if="expandedUnits[unitIndex] || expandAll" class="pl-6 mt-1">
                <div 
                  v-for="(topic, topicIndex) in unit.children" 
                  :key="`unit-${unitIndex}-topic-${topicIndex}`" 
                  class="mb-2"
                >
                  <div 
                    class="flex items-center justify-between p-2 rounded-md cursor-pointer hover:bg-gray-100"
                    :class="{'bg-blue-50 border-l-4 border-blue-500': selectedNode === `unit-${unitIndex}-topic-${topicIndex}`}"
                    @click="toggleTopic(unitIndex, topicIndex); selectNode(`unit-${unitIndex}-topic-${topicIndex}`)"
                  >
                    <div class="flex items-center">
                      <svg 
                        xmlns="http://www.w3.org/2000/svg" 
                        class="h-4 w-4 mr-1 transition-transform" 
                        :class="{'rotate-90': expandedTopics[`${unitIndex}-${topicIndex}`]}"
                        fill="none" 
                        viewBox="0 0 24 24" 
                        stroke="currentColor"
                      >
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                      </svg>
                      <span>{{ topic.title }}</span>
                    </div>
                    <button 
                      class="text-blue-600 hover:text-blue-800"
                      @click.stop="addToLessonPlan(topic)"
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
                      </svg>
                    </button>
                  </div>
                  
                  <div v-if="expandedTopics[`${unitIndex}-${topicIndex}`] || expandAll" class="pl-6 mt-1">
                    <div 
                      v-for="(subtopic, subtopicIndex) in topic.children" 
                      :key="`unit-${unitIndex}-topic-${topicIndex}-subtopic-${subtopicIndex}`" 
                      class="flex items-center justify-between p-2 rounded-md cursor-pointer hover:bg-gray-100"
                      :class="{'bg-blue-50 border-l-4 border-blue-500': selectedNode === `unit-${unitIndex}-topic-${topicIndex}-subtopic-${subtopicIndex}`}"
                      @click="selectNode(`unit-${unitIndex}-topic-${topicIndex}-subtopic-${subtopicIndex}`)"
                    >
                      <span class="text-sm">{{ subtopic.title }}</span>
                      <button 
                        class="text-blue-600 hover:text-blue-800"
                        @click.stop="addToLessonPlan(subtopic)"
                      >
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
                        </svg>
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </template>
          <div v-else class="text-center py-8 text-gray-500">
            <p>未找到大纲数据</p>
            <button 
              class="mt-2 text-blue-600 hover:text-blue-800 underline"
              @click="goToOutlineImport"
            >
              前往导入大纲
            </button>
          </div>
        </div>
      </div>
      
      <!-- 右侧教案设计区 -->
      <div class="lg:col-span-3">
        <!-- 教案信息 -->
        <div class="bg-white rounded-lg shadow-md p-6 mb-6">
          <h2 class="text-lg font-semibold mb-4">教案信息</h2>
          
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="form-group">
              <label class="block text-sm font-medium text-gray-700 mb-1">教案标题</label>
              <input 
                type="text" 
                v-model="lessonPlan.title" 
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="输入教案标题"
              />
            </div>
            <div class="form-group">
              <label class="block text-sm font-medium text-gray-700 mb-1">适用年级</label>
              <input 
                type="text" 
                v-model="lessonPlan.grade" 
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="输入适用年级"
              />
            </div>
            <div class="form-group">
              <label class="block text-sm font-medium text-gray-700 mb-1">学科</label>
              <input 
                type="text" 
                v-model="lessonPlan.subject" 
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="输入学科"
              />
            </div>
            <div class="form-group">
              <label class="block text-sm font-medium text-gray-700 mb-1">总课时</label>
              <input 
                type="number" 
                v-model="lessonPlan.duration" 
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="输入课时数"
                min="1"
              />
            </div>
          </div>
        </div>
        
        <!-- 教案内容时间轴 -->
        <div class="bg-white rounded-lg shadow-md p-6">
          <div class="flex justify-between items-center mb-4">
            <h2 class="text-lg font-semibold">教案内容设计</h2>
            <div class="flex space-x-2">
              <button 
                class="px-3 py-1 bg-blue-100 text-blue-700 rounded-md hover:bg-blue-200 focus:outline-none"
                @click="addLessonBlock('introduction')"
              >
                添加导入环节
              </button>
              <button 
                class="px-3 py-1 bg-green-100 text-green-700 rounded-md hover:bg-green-200 focus:outline-none"
                @click="addLessonBlock('content')"
              >
                添加教学内容
              </button>
              <button 
                class="px-3 py-1 bg-purple-100 text-purple-700 rounded-md hover:bg-purple-200 focus:outline-none"
                @click="addLessonBlock('activity')"
              >
                添加活动
              </button>
              <button 
                class="px-3 py-1 bg-amber-100 text-amber-700 rounded-md hover:bg-amber-200 focus:outline-none"
                @click="addLessonBlock('assessment')"
              >
                添加评价
              </button>
            </div>
          </div>
          
          <!-- 时间轴 -->
          <div class="relative">
            <!-- 时间标尺 -->
            <div class="flex border-b border-gray-300 mb-4 pb-2">
              <div class="w-24 flex-shrink-0"></div>
              <div class="flex-grow flex">
                <div v-for="i in 12" :key="i" class="flex-1 text-center text-xs text-gray-500">
                  {{ i * 5 }}分钟
                </div>
              </div>
            </div>
            
            <!-- 拖拽区域 -->
            <draggable 
              v-model="lessonPlan.blocks" 
              group="lesson-blocks" 
              item-key="id"
              handle=".drag-handle"
              ghost-class="ghost"
              chosen-class="chosen"
              class="space-y-3"
            >
              <template #item="{element, index}">
                <div 
                  class="lesson-block relative border rounded-lg p-4"
                  :class="{
                    'border-blue-300 bg-blue-50': element.type === 'introduction',
                    'border-green-300 bg-green-50': element.type === 'content',
                    'border-purple-300 bg-purple-50': element.type === 'activity',
                    'border-amber-300 bg-amber-50': element.type === 'assessment'
                  }"
                >
                  <div class="flex items-center justify-between mb-2">
                    <div class="flex items-center">
                      <div class="drag-handle cursor-move mr-2">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 8h16M4 16h16" />
                        </svg>
                      </div>
                      <span class="font-medium">
                        {{ 
                          element.type === 'introduction' ? '导入环节' : 
                          element.type === 'content' ? '教学内容' : 
                          element.type === 'activity' ? '教学活动' : '评价环节' 
                        }}
                      </span>
                    </div>
                    <div class="flex items-center space-x-2">
                      <div class="flex items-center">
                        <span class="text-sm text-gray-600 mr-2">时长:</span>
                        <input 
                          type="number" 
                          v-model="element.duration" 
                          class="w-16 px-2 py-1 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500"
                          min="1"
                          max="60"
                        />
                        <span class="text-sm text-gray-600 ml-1">分钟</span>
                      </div>
                      <button 
                        class="text-red-500 hover:text-red-700"
                        @click="removeLessonBlock(index)"
                      >
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                        </svg>
                      </button>
                    </div>
                  </div>
                  
                  <div class="form-group mb-3">
                    <label class="block text-sm font-medium text-gray-700 mb-1">标题</label>
                    <input 
                      type="text" 
                      v-model="element.title" 
                      class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                      placeholder="输入环节标题"
                    />
                  </div>
                  
                  <div class="form-group">
                    <label class="block text-sm font-medium text-gray-700 mb-1">内容描述</label>
                    <textarea 
                      v-model="element.content" 
                      rows="3"
                      class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                      placeholder="输入内容描述"
                    ></textarea>
                  </div>
                  
                  <!-- 时间轴可视化 -->
                  <div class="mt-3 h-2 bg-gray-200 rounded-full overflow-hidden">
                    <div 
                      class="h-full rounded-full"
                      :class="{
                        'bg-blue-500': element.type === 'introduction',
                        'bg-green-500': element.type === 'content',
                        'bg-purple-500': element.type === 'activity',
                        'bg-amber-500': element.type === 'assessment'
                      }"
                      :style="{ width: `${Math.min(100, element.duration / 60 * 100)}%` }"
                    ></div>
                  </div>
                </div>
              </template>
            </draggable>
            
            <!-- 空状态 -->
            <div v-if="lessonPlan.blocks.length === 0" class="text-center py-12 border-2 border-dashed border-gray-300 rounded-lg">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 mx-auto text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
              </svg>
              <p class="mt-2 text-gray-500">点击上方按钮添加教案内容，或从左侧大纲中选择内容添加</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import draggable from 'vuedraggable'

const router = useRouter()
const outlineData = ref(null)
const expandAll = ref(false)
const expandedUnits = reactive({})
const expandedTopics = reactive({})
const selectedNode = ref('')

// 教案数据
const lessonPlan = reactive({
  title: '',
  grade: '',
  subject: '',
  duration: 1,
  blocks: []
})

// 加载大纲数据
onMounted(() => {
  // 从本地存储获取大纲数据
  const savedOutlineData = localStorage.getItem('outlineData')
  if (savedOutlineData) {
    outlineData.value = JSON.parse(savedOutlineData)
    
    // 预填充教案信息
    if (outlineData.value.courseInfo) {
      lessonPlan.title = outlineData.value.courseInfo.name || ''
      lessonPlan.grade = outlineData.value.courseInfo.grade || ''
      lessonPlan.subject = outlineData.value.courseInfo.subject || ''
      lessonPlan.duration = outlineData.value.courseInfo.totalHours || 1
    }
  }
})

// 切换单元展开/折叠
const toggleUnit = (unitIndex) => {
  expandedUnits[unitIndex] = !expandedUnits[unitIndex]
}

// 切换主题展开/折叠
const toggleTopic = (unitIndex, topicIndex) => {
  const key = `${unitIndex}-${topicIndex}`
  expandedTopics[key] = !expandedTopics[key]
}

// 选择节点
const selectNode = (nodeId) => {
  selectedNode.value = nodeId
}

// 添加到教案
const addToLessonBlock = (id) => {
  // 生成唯一ID
  const blockId = Date.now().toString()
  
  // 创建新的教案块
  return {
    id: blockId,
    type: 'content',
    title: '',
    content: '',
    duration: 10,
  }
}

// 添加内容到教案
const addToLessonPlan = (item) => {
  const newBlock = addToLessonBlock()
  newBlock.title = item.title
  lessonPlan.blocks.push(newBlock)
}

// 添加教案块
const addLessonBlock = (type) => {
  const blockId = Date.now().toString()
  
  lessonPlan.blocks.push({
    id: blockId,
    type: type,
    title: '',
    content: '',
    duration: 10,
  })
}

// 删除教案块
const removeLessonBlock = (index) => {
  lessonPlan.blocks.splice(index, 1)
}

// 保存为草稿
const saveAsDraft = () => {
  // 保存到本地存储或发送到服务器
  localStorage.setItem('lessonPlanDraft', JSON.stringify(lessonPlan))
  alert('教案已保存为草稿')
}

// 发布教案
const publishLesson = () => {
  // 发送到服务器
  alert('教案已发布')
}

// 前往大纲导入页面
const goToOutlineImport = () => {
  router.push('/teacher/outline-import')
}
</script>

<style scoped>
.ghost {
  opacity: 0.5;
  background: #c8ebfb;
}

.chosen {
  background: #f0f9ff;
}
</style> 