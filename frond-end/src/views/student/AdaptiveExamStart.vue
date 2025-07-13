<template>
  <div class="adaptive-exam-start-page">
    <!-- 全屏加载遮罩 -->
    <div v-if="loading && currentStep === 2" class="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center">
      <div class="bg-white p-8 rounded-lg shadow-lg text-center">
        <el-spinner size="large" class="mb-4"></el-spinner>
        <p class="text-lg font-medium">{{ loadingMessage }}</p>
        <p class="text-sm text-gray-500 mt-2">{{ generationProgress }}</p>
        <div class="mt-4 w-full">
          <el-progress :percentage="progressPercentage" :stroke-width="8"></el-progress>
        </div>
      </div>
    </div>
    
    <!-- 顶部导航栏 -->
    <header class="exam-header sticky top-0 left-0 right-0 z-30 bg-white border-b border-gray-200 shadow-sm">
      <div class="container mx-auto px-4 py-3 flex justify-between items-center">
        <div class="flex items-center">
          <el-button icon="el-icon-back" circle size="small" class="mr-3" @click="goBack"></el-button>
          <h1 class="text-lg font-bold mr-4">自适应智能测试</h1>
          <el-tag size="small" type="info">{{ selectedSubject }}</el-tag>
        </div>
      </div>
    </header>
    
    <div class="container mx-auto px-4 py-6">
      <!-- 步骤条 -->
      <el-steps :active="currentStep" finish-status="success" class="mb-8">
        <el-step title="选择科目" icon="el-icon-collection-tag"></el-step>
        <el-step title="设置参数" icon="el-icon-setting"></el-step>
        <el-step title="开始测试" icon="el-icon-video-play"></el-step>
      </el-steps>
      
      <!-- 步骤内容 -->
      <div class="step-content bg-white rounded-xl shadow-md p-6">
        <!-- 步骤1：选择科目 -->
        <div v-if="currentStep === 0" class="subject-selection">
          <h2 class="text-xl font-semibold mb-6 flex items-center">
            <Icon icon="tabler:book" class="mr-2 text-indigo-500" />
            选择测试科目
          </h2>
          
          <div v-if="isLoading" class="flex justify-center py-10">
            <el-spinner size="medium"></el-spinner>
            <span class="ml-2 text-gray-500">正在加载科目列表...</span>
          </div>
          
          <div v-else class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
            <div v-for="subject in subjects" :key="subject.id"
                 class="subject-card p-4 border rounded-lg cursor-pointer transition-all"
                 :class="{'border-indigo-500 bg-indigo-50': selectedSubjectId === subject.id, 
                         'hover:border-gray-300': selectedSubjectId !== subject.id}"
                 @click="selectSubject(subject)">
              <div class="flex items-center">
                <div class="w-10 h-10 rounded-lg flex items-center justify-center mr-3"
                     :class="getSubjectIconBg(subject.name)">
                  <Icon :icon="getSubjectIcon(subject.name)" :class="getSubjectIconClass(subject.name)" />
                </div>
                <div>
                  <div class="font-medium">{{ subject.name }}</div>
                  <div class="text-xs text-gray-500">{{ subject.description || '该科目的测试' }}</div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="flex justify-end mt-6">
            <el-button type="primary" @click="nextStep" :disabled="!selectedSubjectId">
              下一步
              <Icon icon="tabler:arrow-right" class="ml-1" />
            </el-button>
          </div>
        </div>
        
        <!-- 步骤2：设置参数 -->
        <div v-if="currentStep === 1" class="test-settings">
          <h2 class="text-xl font-semibold mb-6 flex items-center">
            <Icon icon="tabler:settings" class="mr-2 text-indigo-500" />
            测试参数设置
          </h2>
          
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <!-- 左侧：基本设置 -->
            <div class="basic-settings space-y-4">
              <div class="setting-item">
                <div class="flex justify-between mb-2">
                  <label class="text-sm font-medium flex items-center">
                    <Icon icon="tabler:question-mark" class="mr-1 text-indigo-500" />
                    题目数量
                  </label>
                  <span class="text-sm text-indigo-600 font-medium">{{ settings.questionCount }}题</span>
                </div>
                <el-slider v-model="settings.questionCount" :min="10" :max="50" :step="5" show-stops></el-slider>
                <div class="text-xs text-gray-500">推荐：20-30题，可以准确评估能力水平</div>
              </div>
              
              <div class="setting-item">
                <div class="flex justify-between mb-2">
                  <label class="text-sm font-medium flex items-center">
                    <Icon icon="tabler:clock" class="mr-1 text-indigo-500" />
                    时间限制
                  </label>
                  <span class="text-sm text-indigo-600 font-medium">{{ settings.timeLimit }}分钟</span>
                </div>
                <el-slider v-model="settings.timeLimit" :min="15" :max="90" :step="15" show-stops></el-slider>
                <div class="text-xs text-gray-500">推荐：45分钟，足够完成测试但不会太疲劳</div>
              </div>
              
              <div class="setting-item">
                <label class="text-sm font-medium flex items-center mb-2">
                  <Icon icon="tabler:stairs-up" class="mr-1 text-indigo-500" />
                  起始难度
                </label>
                <el-radio-group v-model="settings.startingDifficulty" class="w-full flex justify-between">
                  <el-radio-button label="easy">简单</el-radio-button>
                  <el-radio-button label="medium">中等</el-radio-button>
                  <el-radio-button label="hard">困难</el-radio-button>
                </el-radio-group>
                <div class="text-xs text-gray-500 mt-1">推荐：中等，系统会根据你的表现动态调整</div>
              </div>
            </div>
            
            <!-- 右侧：高级设置 -->
            <div class="advanced-settings space-y-4">
              <div class="setting-item">
                <label class="text-sm font-medium flex items-center mb-2">
                  <Icon icon="tabler:adjustments" class="mr-1 text-indigo-500" />
                  知识点覆盖
                </label>
                
                <!-- 已添加的知识点 -->
                <div class="mb-3 flex flex-wrap gap-2">
                  <el-tag
                    v-for="topic in customTopics"
                    :key="topic.id"
                    closable
                    @close="removeCustomTopic(topic.id)"
                  >
                    {{ topic.name }}
                  </el-tag>
                </div>
                
                <!-- 自定义添加知识点 -->
                <div class="flex mb-3">
                  <el-input
                    v-model="newTopicName"
                    placeholder="输入自定义知识点"
                    class="flex-grow mr-2"
                    size="small"
                    @keyup.enter="addCustomTopic"
                  ></el-input>
                  <el-button size="small" @click="addCustomTopic" :disabled="!newTopicName.trim()">
                    添加
                  </el-button>
                </div>
                
                <!-- 默认知识点选择 -->
                <el-collapse class="mb-2">
                  <el-collapse-item title="选择预设知识点" name="1">
                <el-checkbox-group v-model="settings.topics" class="w-full flex flex-col gap-2">
                  <el-checkbox v-for="topic in availableTopics" :key="topic.id" :label="topic.id">
                    {{ topic.name }}
                  </el-checkbox>
                </el-checkbox-group>
                  </el-collapse-item>
                </el-collapse>
                
                <div class="text-xs text-gray-500 mt-1">您可以添加自定义知识点或选择预设知识点</div>
              </div>
              
              <div class="setting-item">
                <label class="text-sm font-medium flex items-center mb-2">
                  <Icon icon="tabler:settings-automation" class="mr-1 text-indigo-500" />
                  高级选项
                </label>
                <div class="space-y-2">
                  <el-checkbox v-model="settings.showHints">允许查看提示（会影响评分）</el-checkbox>
                  <el-checkbox v-model="settings.showFeedback">答题后立即显示正误</el-checkbox>
                  <el-checkbox v-model="settings.adaptiveDifficulty">启用自适应难度调整</el-checkbox>
                </div>
              </div>
            </div>
          </div>
          
          <div class="flex justify-between mt-8">
            <el-button @click="prevStep">
              <Icon icon="tabler:arrow-left" class="mr-1" />
              上一步
            </el-button>
            <el-button type="primary" @click="nextStep">
              下一步
              <Icon icon="tabler:arrow-right" class="ml-1" />
            </el-button>
          </div>
        </div>
        
        <!-- 步骤3：开始测试 -->
        <div v-if="currentStep === 2" class="start-test">
          <div class="text-center py-8">
            <div class="mb-6">
              <Icon icon="tabler:brain" class="text-6xl text-indigo-500 mb-4" />
              <h2 class="text-2xl font-bold mb-2">准备开始测试</h2>
              <p class="text-gray-500">您即将开始 {{ selectedSubject }} 的自适应智能测试</p>
            </div>
            
            <div class="test-summary bg-gray-50 max-w-md mx-auto p-4 rounded-lg mb-8">
              <div class="grid grid-cols-2 gap-4">
                <div class="summary-item text-left">
                  <div class="text-sm text-gray-500">科目</div>
                  <div class="font-medium">{{ selectedSubject }}</div>
                </div>
                <div class="summary-item text-left">
                  <div class="text-sm text-gray-500">题目数量</div>
                  <div class="font-medium">{{ settings.questionCount }}题</div>
                </div>
                <div class="summary-item text-left">
                  <div class="text-sm text-gray-500">时间限制</div>
                  <div class="font-medium">{{ settings.timeLimit }}分钟</div>
                </div>
                <div class="summary-item text-left">
                  <div class="text-sm text-gray-500">起始难度</div>
                  <div class="font-medium">{{ getDifficultyText(settings.startingDifficulty) }}</div>
                </div>
              </div>
              
              <div class="mt-4 text-left">
                <div class="text-sm text-gray-500 mb-1">已选择的知识点</div>
                <div class="flex flex-wrap gap-2 mt-1">
                  <el-tag v-for="topic in allSelectedTopics" :key="topic.id" size="small">{{ topic.name }}</el-tag>
                </div>
              </div>
            </div>
            
            <div class="tips text-left max-w-md mx-auto mb-8">
              <h3 class="font-medium mb-2 flex items-center">
                <Icon icon="tabler:bulb" class="mr-1 text-amber-500" />
                测试提示
              </h3>
              <ul class="text-sm text-gray-600 space-y-2 list-disc pl-5">
                <li>系统会根据您的答题情况动态调整难度</li>
                <li>请在安静环境下专注完成测试</li>
                <li>测试结束后将生成详细的能力分析报告</li>
                <li>如需暂停，可点击右上角的暂停按钮</li>
              </ul>
            </div>
            
            <div class="flex justify-center gap-4">
              <el-button @click="prevStep">
                <Icon icon="tabler:arrow-left" class="mr-1" />
                返回设置
              </el-button>
              <el-button type="primary" @click="startTest" size="large" :disabled="loading">
                <Icon v-if="!loading" icon="tabler:player-play" class="mr-1" />
                <el-spinner v-else size="small" class="mr-1" />
                {{ loading ? '创建测试中...' : '开始测试' }}
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Icon } from '@iconify/vue'
import { get, post } from '@/net'

const router = useRouter()
const currentStep = ref(0)
const selectedSubjectId = ref(null)
const selectedSubject = ref('')
const isLoading = ref(false)
const loading = ref(false)
const loadingMessage = ref('')
const generationProgress = ref('正在准备题目，请稍候...')
const progressPercentage = ref(0)
const progressTimer = ref(null)

// 科目列表，将从API获取
const subjects = ref([])

// 自定义知识点相关
const newTopicName = ref('')
const customTopics = ref([])
let nextCustomTopicId = 1000 // 自定义知识点ID起始值

// 可用的知识点
const availableTopics = ref([])

// 测试设置
const settings = ref({
  questionCount: 20,
  timeLimit: 45,
  startingDifficulty: 'medium',
  topics: [],
  showHints: false,
  showFeedback: true,
  adaptiveDifficulty: true
})

// 计算属性：所有选中的知识点（预设+自定义）
const allSelectedTopics = computed(() => {
  const selectedPresetTopics = availableTopics.value.filter(t => settings.value.topics.includes(t.id));
  return [...selectedPresetTopics, ...customTopics.value];
})

// 从API获取科目列表
const fetchSubjects = async () => {
  isLoading.value = true
  try {
    // 使用封装好的get方法请求科目列表
    get('/api/adaptive-exams/subjects', null, 
      (message, data) => {
        subjects.value = data || []
        if (!data || data.length === 0) {
          ElMessage.warning('未找到可用的科目，使用默认数据')
          subjects.value = getDefaultSubjects()
        }
      },
      (message) => {
        ElMessage.warning(message || '获取科目列表失败，使用默认数据')
        subjects.value = getDefaultSubjects()
      },
      () => {
        ElMessage.error('获取科目列表出错，使用默认数据')
        subjects.value = getDefaultSubjects()
      }
    )
  } catch (error) {
    console.error('获取科目列表出错：', error)
    ElMessage.error('获取科目列表出错，使用默认数据')
    subjects.value = getDefaultSubjects()
  } finally {
    isLoading.value = false
  }
}

// 默认科目数据（备用）
const getDefaultSubjects = () => {
  return [
    { id: 1, name: '数学', description: '数学基础知识与应用' },
    { id: 2, name: '英语', description: '英语词汇、语法与阅读' },
    { id: 3, name: '物理', description: '物理学基本原理与应用' },
    { id: 4, name: '化学', description: '化学元素与反应原理' },
    { id: 5, name: '生物', description: '生物学基础与生命科学' },
    { id: 6, name: '计算机', description: '计算机科学与编程基础' }
  ]
}

// 根据科目名称获取图标
const getSubjectIcon = (subjectName) => {
  const iconMap = {
    '数学': 'tabler:math',
    '英语': 'tabler:language',
    '物理': 'tabler:atom',
    '化学': 'tabler:flask',
    '生物': 'tabler:dna',
    '计算机': 'tabler:device-laptop'
  }
  return iconMap[subjectName] || 'tabler:book'
}

// 根据科目名称获取图标颜色类
const getSubjectIconClass = (subjectName) => {
  const classMap = {
    '数学': 'text-blue-600',
    '英语': 'text-green-600',
    '物理': 'text-purple-600',
    '化学': 'text-amber-600',
    '生物': 'text-emerald-600',
    '计算机': 'text-indigo-600'
  }
  return classMap[subjectName] || 'text-gray-600'
}

// 根据科目名称获取图标背景类
const getSubjectIconBg = (subjectName) => {
  const bgMap = {
    '数学': 'bg-blue-100',
    '英语': 'bg-green-100',
    '物理': 'bg-purple-100',
    '化学': 'bg-amber-100',
    '生物': 'bg-emerald-100',
    '计算机': 'bg-indigo-100'
  }
  return bgMap[subjectName] || 'bg-gray-100'
}

// 添加自定义知识点
const addCustomTopic = () => {
  if (!newTopicName.value.trim()) return
  
  // 检查是否已存在相同名称的知识点
  const exists = customTopics.value.some(topic => 
    topic.name.toLowerCase() === newTopicName.value.trim().toLowerCase()
  )
  
  if (exists) {
    ElMessage.warning('已存在相同名称的知识点')
    return
  }
  
  // 添加新知识点
  customTopics.value.push({
    id: nextCustomTopicId++,
    name: newTopicName.value.trim()
  })
  
  newTopicName.value = ''
}

// 移除自定义知识点
const removeCustomTopic = (topicId) => {
  customTopics.value = customTopics.value.filter(topic => topic.id !== topicId)
}

// 根据选择的科目获取相应的知识点
const getTopicsBySubject = (subjectId) => {
  // 这里可以通过API获取科目对应的知识点
  // 目前使用本地数据作为示例
  if (subjectId === 1) { // 数学
    return [
      { id: 1, name: '代数基础' },
      { id: 2, name: '几何图形' },
      { id: 3, name: '函数与方程' },
      { id: 4, name: '概率统计' },
      { id: 5, name: '应用题' }
    ]
  } else if (subjectId === 2) { // 英语
    return [
      { id: 1, name: '词汇理解' },
      { id: 2, name: '语法结构' },
      { id: 3, name: '阅读理解' },
      { id: 4, name: '写作表达' },
      { id: 5, name: '听力理解' }
    ]
  } else {
    // 默认知识点
    return [
      { id: 1, name: '基础概念' },
      { id: 2, name: '理论应用' },
      { id: 3, name: '实验分析' },
      { id: 4, name: '问题解决' },
      { id: 5, name: '综合能力' }
    ]
  }
}

// 选择科目
const selectSubject = (subject) => {
  selectedSubjectId.value = subject.id
  selectedSubject.value = subject.name
  
  // 获取该科目对应的知识点
  availableTopics.value = getTopicsBySubject(subject.id)
  
  // 重置选中的知识点
  settings.value.topics = availableTopics.value.map(topic => topic.id)
  
  // 清空自定义知识点
  customTopics.value = []
  nextCustomTopicId = 1000
}

// 下一步
const nextStep = () => {
  if (currentStep.value === 0 && !selectedSubjectId.value) {
    ElMessage.warning('请选择一个科目')
    return
  }
  
  if (currentStep.value < 2) {
    currentStep.value++
  }
}

// 上一步
const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

// 开始测试
const startTest = () => {
  ElMessage.success('准备开始测试...')
  
  // 设置加载状态
  loading.value = true
  loadingMessage.value = '正在创建测试，请稍候...'
  progressPercentage.value = 0
  generationProgress.value = '正在准备题目，请稍候...'
  
  // 启动进度条动画
  startProgressAnimation()
  
  // 准备测试数据
  const testData = {
    subjectName: selectedSubject.value,
    settings: {
      ...settings.value,
      // 合并预设知识点和自定义知识点
      customTopics: [
        ...customTopics.value.map(t => t.name),
        ...availableTopics.value
          .filter(t => settings.value.topics.includes(t.id))
          .map(t => t.name)
      ]
    }
  }
  
  // 调用API创建测试会话
  post('/api/adaptive-exams', testData,
    (message, data) => {
      // 成功回调
      stopProgressAnimation()
      const examId = data?.id || Date.now()
      
      // 如果返回了题目，显示生成完成的提示
      if (data?.questions && data.questions.length > 0) {
        progressPercentage.value = 100
        generationProgress.value = `已成功生成 ${data.questions.length} 道题目`
        loadingMessage.value = '题目生成完成！'
        
        // 使用对话框提示用户题目已生成完成
        ElMessageBox.confirm(
          `已成功生成 ${data.questions.length} 道题目，点击确认开始测试！`,
          '题目生成完成',
          {
            confirmButtonText: '开始测试',
            cancelButtonText: '取消',
            type: 'success',
          }
        ).then(() => {
          // 用户点击确认，跳转到测试页面
          loading.value = false
          router.push(`/student/adaptive-exam/${examId}`)
        }).catch(() => {
          // 用户点击取消，返回到设置页面
          loading.value = false
          currentStep.value = 1
        })
      } else {
        // 没有返回题目，直接跳转
        loading.value = false
        router.push(`/student/adaptive-exam/${examId}`)
      }
    },
    (message) => {
      // 失败回调
      stopProgressAnimation()
      ElMessage.warning(message || '创建测试失败')
      loading.value = false
    },
    () => {
      // 错误回调
      stopProgressAnimation()
      ElMessage.error('创建测试出错，请重试')
      loading.value = false
    }
  )
}

// 启动进度条动画
const startProgressAnimation = () => {
  // 清除可能存在的旧定时器
  if (progressTimer.value) {
    clearInterval(progressTimer.value)
  }
  
  // 创建新的进度动画
  progressTimer.value = setInterval(() => {
    // 缓慢增加进度，但不超过95%（留给真正完成的时刻）
    if (progressPercentage.value < 95) {
      // 开始快，后面慢
      const increment = progressPercentage.value < 30 ? 2 : 
                        progressPercentage.value < 60 ? 1 : 0.5
      
      progressPercentage.value += increment
      
      // 根据进度更新提示文本
      if (progressPercentage.value < 20) {
        generationProgress.value = '正在分析知识点...'
      } else if (progressPercentage.value < 40) {
        generationProgress.value = '正在生成题干...'
      } else if (progressPercentage.value < 60) {
        generationProgress.value = '正在生成选项和答案...'
      } else if (progressPercentage.value < 80) {
        generationProgress.value = '正在验证题目质量...'
      } else {
        generationProgress.value = '即将完成，请稍候...'
      }
    }
  }, 200)
}

// 停止进度条动画
const stopProgressAnimation = () => {
  if (progressTimer.value) {
    clearInterval(progressTimer.value)
    progressTimer.value = null
  }
}

// 返回上一页
const goBack = () => {
  ElMessageBox.confirm('确定要取消测试设置吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    router.push('/student/exams')
  }).catch(() => {})
}

// 获取难度文本
const getDifficultyText = (difficulty) => {
  const difficultyMap = {
    'easy': '简单',
    'medium': '中等',
    'hard': '困难'
  }
  return difficultyMap[difficulty] || '中等'
}

// 组件挂载时获取科目列表
onMounted(() => {
  fetchSubjects()
})

// 组件卸载前清理定时器
onBeforeUnmount(() => {
  stopProgressAnimation()
})
</script>

<style scoped>
.adaptive-exam-start-page {
  min-height: 100vh;
  background-color: #f8fafc;
}

.exam-header {
  backdrop-filter: blur(8px);
  background-color: rgba(255, 255, 255, 0.9);
}

.step-content {
  min-height: 400px;
}

/* 动画效果 */
.subject-card, .setting-item, .test-summary, .tips {
  animation: fadeIn 0.6s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}
</style> 