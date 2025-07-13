<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Search, CirclePlus, Bookmark, Clock, BarChart2 } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(true)
const packages = ref([
  { 
    id: 1, 
    title: '高数-函数极限', 
    count: 10, 
    completed: 4, 
    category: 'math',
    difficulty: 'medium',
    estimatedTime: 25,
    lastPracticed: '2023-10-26',
    tags: ['微积分', '函数'],
    description: '包含函数极限相关概念和计算题目'
  },
  { 
    id: 2, 
    title: '线代-矩阵运算', 
    count: 15, 
    completed: 0, 
    category: 'math',
    difficulty: 'hard',
    estimatedTime: 35,
    lastPracticed: null,
    tags: ['线性代数', '矩阵'],
    description: '矩阵的基本运算和性质练习题'
  },
  { 
    id: 3, 
    title: '数据结构-链表', 
    count: 12, 
    completed: 12, 
    category: 'cs',
    difficulty: 'medium',
    estimatedTime: 30,
    lastPracticed: '2023-10-20',
    tags: ['数据结构', '链表'],
    description: '链表操作和基本算法练习'
  },
  { 
    id: 4, 
    title: 'Python基础语法', 
    count: 20, 
    completed: 8, 
    category: 'cs',
    difficulty: 'easy',
    estimatedTime: 20,
    lastPracticed: '2023-10-25',
    tags: ['编程', 'Python'],
    description: 'Python语言基础语法练习题'
  },
  { 
    id: 5, 
    title: '概率论基础', 
    count: 18, 
    completed: 0, 
    category: 'math',
    difficulty: 'hard',
    estimatedTime: 40,
    lastPracticed: null,
    tags: ['概率', '统计'],
    description: '概率论基本概念和应用题'
  },
])

const filter = ref('all')
const search = ref('')
const categoryFilter = ref('all')
const difficultyFilter = ref('all')
const sortBy = ref('default')

// 根据状态、分类、难度与搜索关键字过滤
const filtered = computed(() =>
  packages.value
    .filter(p => {
      // 状态过滤
      if (filter.value === 'unfinished') return p.completed < p.count
      if (filter.value === 'finished') return p.completed === p.count
      if (filter.value === 'inprogress') return p.completed > 0 && p.completed < p.count
      if (filter.value === 'notstarted') return p.completed === 0
      return true
    })
    .filter(p => {
      // 分类过滤
      if (categoryFilter.value === 'all') return true
      return p.category === categoryFilter.value
    })
    .filter(p => {
      // 难度过滤
      if (difficultyFilter.value === 'all') return true
      return p.difficulty === difficultyFilter.value
    })
    .filter(p => {
      // 搜索过滤
      if (!search.value) return true
      return p.title.toLowerCase().includes(search.value.toLowerCase()) ||
             p.description.toLowerCase().includes(search.value.toLowerCase()) ||
             p.tags.some(tag => tag.toLowerCase().includes(search.value.toLowerCase()))
    })
    .sort((a, b) => {
      // 排序
      if (sortBy.value === 'name') {
        return a.title.localeCompare(b.title)
      } else if (sortBy.value === 'progress') {
        return (b.completed / b.count) - (a.completed / a.count)
      } else if (sortBy.value === 'recent') {
        if (!a.lastPracticed) return 1
        if (!b.lastPracticed) return -1
        return new Date(b.lastPracticed) - new Date(a.lastPracticed)
      } else if (sortBy.value === 'difficulty') {
        const difficultyOrder = { 'easy': 1, 'medium': 2, 'hard': 3 }
        return difficultyOrder[b.difficulty] - difficultyOrder[a.difficulty]
      }
      return a.id - b.id // 默认按ID排序
    })
)

// 获取难度对应的颜色和文本
const getDifficultyInfo = (difficulty) => {
  switch (difficulty) {
    case 'easy':
      return { color: 'success', text: '简单', bgClass: 'bg-green-100 text-green-700' }
    case 'medium':
      return { color: 'warning', text: '中等', bgClass: 'bg-amber-100 text-amber-700' }
    case 'hard':
      return { color: 'danger', text: '困难', bgClass: 'bg-red-100 text-red-700' }
    default:
      return { color: 'info', text: '未知', bgClass: 'bg-gray-100 text-gray-700' }
  }
}

// 格式化上次练习时间
const formatLastPracticed = (dateStr) => {
  if (!dateStr) return '尚未练习'
  const date = new Date(dateStr)
  const now = new Date()
  const diffDays = Math.floor((now - date) / (1000 * 60 * 60 * 24))
  
  if (diffDays === 0) return '今天'
  if (diffDays === 1) return '昨天'
  if (diffDays < 7) return `${diffDays}天前`
  
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

// 开始练习
const startPractice = (p) => {
  router.push(`/student/practice?pid=${p.id}`)
}

// 创建新练习包
const createNewPackage = () => {
  ElMessageBox.prompt('请输入练习包名称', '创建新练习包', {
    confirmButtonText: '创建',
    cancelButtonText: '取消',
    inputPlaceholder: '例如：高数-导数计算'
  }).then(({ value }) => {
    if (value) {
      ElMessage.success(`练习包"${value}"创建成功！`)
      // 这里可以添加创建逻辑
    }
  }).catch(() => {})
}

// 收藏练习包
const toggleFavorite = (p, event) => {
  event.stopPropagation()
  p.favorite = !p.favorite
  ElMessage({
    message: p.favorite ? `已收藏"${p.title}"` : `已取消收藏"${p.title}"`,
    type: 'success',
    duration: 1500
  })
}

onMounted(() => {
  // 模拟加载
  setTimeout(() => {
    loading.value = false
  }, 600)
})

// 监听过滤器变化，添加动画效果
watch([filter, categoryFilter, difficultyFilter, search, sortBy], () => {
  // 可以在这里添加过滤变化时的动画逻辑
})
</script>

<template>
  <div class="practice-wrapper" v-loading="loading">
    <!-- 顶部工具栏：状态筛选 + 搜索框 -->
    <div
      class="toolbar sticky top-0 z-20 flex flex-wrap gap-3 items-center bg-white/80 backdrop-blur-lg px-4 py-3 rounded-xl shadow-md mb-6"
    >
      <div class="flex flex-wrap gap-2 items-center">
        <div class="filter-group">
          <el-radio-group v-model="filter" size="small">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button label="inprogress">进行中</el-radio-button>
            <el-radio-button label="notstarted">未开始</el-radio-button>
            <el-radio-button label="finished">已完成</el-radio-button>
          </el-radio-group>
        </div>
        
        <div class="ml-0 md:ml-4">
          <el-select v-model="categoryFilter" size="small" placeholder="分类" class="w-24">
            <el-option label="全部" value="all" />
            <el-option label="数学" value="math" />
            <el-option label="计算机" value="cs" />
          </el-select>
        </div>
        
        <div class="ml-2">
          <el-select v-model="difficultyFilter" size="small" placeholder="难度" class="w-24">
            <el-option label="全部" value="all" />
            <el-option label="简单" value="easy" />
            <el-option label="中等" value="medium" />
            <el-option label="困难" value="hard" />
          </el-select>
        </div>
      </div>
      
      <div class="flex items-center ml-auto gap-2">
        <el-select v-model="sortBy" size="small" placeholder="排序" class="w-28">
          <el-option label="默认排序" value="default" />
          <el-option label="按名称" value="name" />
          <el-option label="按进度" value="progress" />
          <el-option label="按难度" value="difficulty" />
          <el-option label="最近练习" value="recent" />
        </el-select>
        
        <el-input
          v-model="search"
          placeholder="搜索练习包…"
          size="small"
          clearable
          :prefix-icon="Search"
          class="w-40 md:w-60"
        />
        
        <el-button type="primary" size="small" @click="createNewPackage" class="ml-2">
          <CirclePlus :size="16" class="mr-1" />
          新建
        </el-button>
      </div>
    </div>

    <transition-group name="stagger" tag="div" class="pkg-grid gap-6">
      <div v-for="p in filtered" :key="p.id" class="pkg-card group" @click="startPractice(p)">
        <div class="p-5 flex flex-col gap-4 h-full relative z-10 select-none">
          <!-- 卡片头部 -->
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
              <div class="icon-wrapper w-10 h-10 rounded-xl flex items-center justify-center text-white text-lg font-semibold"
                   :class="`bg-${p.category === 'math' ? 'blue' : 'purple'}-600`">
                {{ p.title.charAt(0) }}
              </div>
              <h3 class="font-medium text-lg truncate flex-1">{{ p.title }}</h3>
            </div>
            <button @click.stop="toggleFavorite(p, $event)" class="favorite-btn">
              <Bookmark :size="18" :class="p.favorite ? 'text-yellow-500 fill-yellow-500' : 'text-gray-400'" />
            </button>
          </div>
          
          <!-- 标签 -->
          <div class="flex flex-wrap gap-2">
            <span v-for="(tag, index) in p.tags" :key="index" 
                  class="text-xs px-2 py-1 rounded-full bg-gray-100 text-gray-600">
              {{ tag }}
            </span>
            <span :class="['text-xs px-2 py-1 rounded-full', getDifficultyInfo(p.difficulty).bgClass]">
              {{ getDifficultyInfo(p.difficulty).text }}
            </span>
          </div>
          
          <!-- 描述 -->
          <p class="text-sm text-gray-500 line-clamp-2">{{ p.description }}</p>
          
          <!-- 进度条 -->
          <div class="flex items-center gap-3 mt-1">
            <el-progress type="circle" :width="60" :stroke-width="6"
              :percentage="Math.round((p.completed/p.count)*100)"
              :status="p.completed===p.count ? 'success' : ''"
              :color="p.completed===p.count ? '#10b981' : '#6366f1'" />
            <div class="text-xs text-gray-600">
              <p class="mb-1">已做 {{ p.completed }}/{{ p.count }}</p>
              <p v-if="p.completed!==p.count" class="text-indigo-500 font-medium">
                进度 {{ Math.round((p.completed/p.count)*100) }}%
              </p>
              <p v-else class="text-green-600 font-medium">已完成</p>
            </div>
          </div>
          
          <!-- 底部信息 -->
          <div class="flex items-center justify-between mt-auto pt-3 border-t border-gray-100">
            <div class="flex items-center text-xs text-gray-500">
              <Clock :size="14" class="mr-1" />
              约{{ p.estimatedTime }}分钟
            </div>
            <div class="flex items-center text-xs text-gray-500">
              <BarChart2 :size="14" class="mr-1" />
              {{ formatLastPracticed(p.lastPracticed) }}
            </div>
          </div>
          
          <!-- 操作按钮 -->
          <div class="action-buttons absolute bottom-5 right-5 opacity-0 group-hover:opacity-100 transition-opacity">
            <el-button v-if="p.completed!==p.count" type="primary" size="small" @click.stop="startPractice(p)" class="action-btn">
              {{ p.completed===0 ? '开始' : '继续' }}
            </el-button>
            <el-button v-else type="success" size="small" @click.stop="startPractice(p)" class="action-btn">
              重新练习
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 创建新练习包卡片 -->
      <div key="new" class="pkg-card new-pkg-card group" @click="createNewPackage">
        <div class="p-5 flex flex-col items-center justify-center h-full gap-4 text-center">
          <div class="w-16 h-16 rounded-full bg-blue-100 flex items-center justify-center">
            <CirclePlus :size="32" class="text-blue-600" />
          </div>
          <h3 class="font-medium text-lg">创建新练习包</h3>
          <p class="text-sm text-gray-500">自定义题目和练习内容</p>
        </div>
      </div>
    </transition-group>

    <div v-if="filtered.length===0 && !loading" class="empty-state flex flex-col items-center justify-center py-16">
      <div class="w-20 h-20 rounded-full bg-gray-100 flex items-center justify-center mb-4">
        <Search :size="32" class="text-gray-400" />
      </div>
      <h3 class="text-lg font-medium text-gray-600">未找到练习包</h3>
      <p class="text-sm text-gray-500 mt-2 mb-4">尝试调整筛选条件或创建新的练习包</p>
      <el-button type="primary" @click="createNewPackage">创建新练习包</el-button>
    </div>
  </div>
</template>

<style scoped>
.practice-wrapper { 
  @apply p-0; 
}

.pkg-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
}

/* 卡片动画 */
.stagger-enter-from { 
  opacity: 0;
  transform: translateY(20px) scale(.95);
} 

.stagger-enter-active {
  transition: all .4s cubic-bezier(.22,1.02,.38,.98);
  transition-delay: calc(var(--el-transition-duration) * var(--el-transition-delay, 0));
} 

.stagger-enter-to { 
  opacity: 1;
  transform: translateY(0) scale(1);
} 

.stagger-leave-active {
  display: none;
}

/* 卡片样式 */
.pkg-card { 
  @apply bg-white rounded-xl shadow-md transition-all duration-300 cursor-pointer overflow-hidden relative; 
  transform-style: preserve-3d; 
  min-height: 280px;
}

.pkg-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(99,102,241,0.05) 0%, rgba(224,231,255,0.1) 100%);
  opacity: 0;
  transition: opacity .35s;
  z-index: 0;
}

.pkg-card:hover::before {
  opacity: 1;
}

.pkg-card:hover { 
  transform: translateY(-8px) scale(1.02); 
  box-shadow: 0 20px 30px rgba(0,0,0,0.1);
}

.pkg-card:active {
  transform: translateY(-2px) scale(.99);
  transition-duration: 100ms;
}

/* 新建卡片样式 */
.new-pkg-card {
  border: 2px dashed #e2e8f0;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.new-pkg-card:hover {
  border-color: #6366f1;
  background: linear-gradient(135deg, #f5f7ff 0%, #e0e7ff 100%);
}

/* 工具栏样式 */
.toolbar {
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255,255,255,0.5);
}

/* 动画效果 */
.action-btn {
  transform: translateY(10px);
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.pkg-card:hover .action-btn {
  transform: translateY(0);
}

.favorite-btn {
  @apply p-2 rounded-full transition-colors;
}

.favorite-btn:hover {
  @apply bg-gray-100;
}

/* 响应式调整 */
@media (max-width: 640px) {
  .pkg-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  }
}
</style> 