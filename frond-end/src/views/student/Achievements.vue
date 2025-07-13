<template>
  <div class="achievements-page p-6">
    <div class="page-header mb-6">
      <h1 class="text-2xl font-bold mb-2">我的成就</h1>
      <p class="text-gray-500">展示您在学习过程中获得的所有成就和徽章</p>
    </div>

    <!-- 成就概览卡片 -->
    <div class="achievements-overview grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
      <div class="stat-card bg-white rounded-xl shadow-sm p-4 flex items-center">
        <div class="stat-icon bg-purple-100 p-3 rounded-lg">
          <i class="el-icon-medal text-purple-600 text-xl"></i>
        </div>
        <div class="ml-4">
          <div class="text-sm text-gray-500">已获成就</div>
          <div class="text-xl font-semibold">{{ earnedAchievements.length }}/{{ totalAchievements }}</div>
        </div>
      </div>
      
      <div class="stat-card bg-white rounded-xl shadow-sm p-4 flex items-center">
        <div class="stat-icon bg-amber-100 p-3 rounded-lg">
          <i class="el-icon-trophy text-amber-600 text-xl"></i>
        </div>
        <div class="ml-4">
          <div class="text-sm text-gray-500">稀有成就</div>
          <div class="text-xl font-semibold">{{ rareAchievements }}</div>
        </div>
      </div>
      
      <div class="stat-card bg-white rounded-xl shadow-sm p-4 flex items-center">
        <div class="stat-icon bg-blue-100 p-3 rounded-lg">
          <i class="el-icon-collection-tag text-blue-600 text-xl"></i>
        </div>
        <div class="ml-4">
          <div class="text-sm text-gray-500">成就点数</div>
          <div class="text-xl font-semibold">{{ achievementPoints }}</div>
        </div>
      </div>
      
      <div class="stat-card bg-white rounded-xl shadow-sm p-4 flex items-center">
        <div class="stat-icon bg-green-100 p-3 rounded-lg">
          <i class="el-icon-rank text-green-600 text-xl"></i>
        </div>
        <div class="ml-4">
          <div class="text-sm text-gray-500">班级排名</div>
          <div class="text-xl font-semibold">{{ classRank }}/{{ totalStudents }}</div>
        </div>
      </div>
    </div>

    <!-- 成就筛选和搜索 -->
    <div class="filter-bar flex flex-wrap gap-3 items-center bg-white rounded-xl shadow-sm p-4 mb-6">
      <div class="flex flex-wrap gap-2 items-center">
        <el-radio-group v-model="filter" size="small">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="earned">已获得</el-radio-button>
          <el-radio-button label="locked">未获得</el-radio-button>
        </el-radio-group>
        
        <div class="ml-0 md:ml-4">
          <el-select v-model="categoryFilter" size="small" placeholder="类别" class="w-28">
            <el-option label="全部类别" value="all" />
            <el-option v-for="category in categories" :key="category.value" :label="category.label" :value="category.value" />
          </el-select>
        </div>
      </div>
      
      <div class="ml-auto">
        <el-input
          v-model="search"
          placeholder="搜索成就…"
          size="small"
          clearable
          prefix-icon="el-icon-search"
          class="w-40 md:w-60"
        />
      </div>
    </div>

    <!-- 成就展示 -->
    <div class="achievements-grid grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div v-for="achievement in filteredAchievements" :key="achievement.id" 
           class="achievement-card bg-white rounded-xl shadow-sm overflow-hidden"
           :class="{'opacity-70': !achievement.earned}">
        <!-- 成就顶部条 -->
        <div class="achievement-header h-2" :style="`background-color: ${achievement.color}`"></div>
        
        <div class="p-5">
          <div class="flex items-start">
            <!-- 成就图标 -->
            <div class="achievement-icon w-14 h-14 rounded-lg flex items-center justify-center mr-4"
                 :class="`bg-${achievement.colorName}-100`">
              <i :class="`el-icon-${achievement.icon} text-${achievement.colorName}-600 text-2xl`"></i>
            </div>
            
            <div class="flex-1">
              <!-- 成就标题和稀有度 -->
              <div class="flex justify-between items-start mb-2">
                <h3 class="text-base font-medium">{{ achievement.title }}</h3>
                <span v-if="achievement.rarity" class="text-xs px-2 py-0.5 rounded-full"
                      :class="getRarityClass(achievement.rarity)">
                  {{ getRarityLabel(achievement.rarity) }}
                </span>
              </div>
              
              <!-- 成就描述 -->
              <p class="text-sm text-gray-600 mb-3">{{ achievement.description }}</p>
              
              <!-- 成就状态 -->
              <div class="flex justify-between items-center">
                <div v-if="achievement.earned" class="flex items-center text-xs text-green-600">
                  <i class="el-icon-circle-check mr-1"></i>
                  {{ achievement.earnedDate }}
                </div>
                <div v-else class="text-xs text-gray-500">
                  <i class="el-icon-lock mr-1"></i>
                  未解锁
                </div>
                
                <div class="text-xs text-gray-500">
                  {{ achievement.points }} 点数
                </div>
              </div>
            </div>
          </div>
          
          <!-- 进度条（如果有） -->
          <div v-if="achievement.progress !== undefined && !achievement.earned" class="mt-4">
            <div class="flex justify-between text-xs text-gray-500 mb-1">
              <span>进度</span>
              <span>{{ achievement.progress }}/{{ achievement.target }}</span>
            </div>
            <el-progress :percentage="(achievement.progress / achievement.target) * 100" :stroke-width="8" :show-text="false" />
          </div>
        </div>
      </div>
    </div>
    
    <!-- 空状态 -->
    <div v-if="filteredAchievements.length === 0" class="empty-state flex flex-col items-center justify-center py-16 bg-white rounded-xl shadow-sm">
      <div class="w-20 h-20 rounded-full bg-gray-100 flex items-center justify-center mb-4">
        <i class="el-icon-search text-4xl text-gray-400"></i>
      </div>
      <h3 class="text-lg font-medium text-gray-600">未找到成就</h3>
      <p class="text-sm text-gray-500 mt-2">尝试调整筛选条件或搜索其他内容</p>
    </div>
    
    <!-- 下一个目标 -->
    <div class="next-goal bg-white rounded-xl shadow-sm p-6 mt-6">
      <h2 class="text-lg font-semibold mb-4">下一个目标</h2>
      
      <div class="next-achievement flex items-center">
        <div class="achievement-icon w-16 h-16 rounded-lg flex items-center justify-center mr-5"
             :class="`bg-${nextGoal.colorName}-100`">
          <i :class="`el-icon-${nextGoal.icon} text-${nextGoal.colorName}-600 text-3xl`"></i>
        </div>
        
        <div class="flex-1">
          <div class="flex justify-between items-start mb-2">
            <h3 class="text-lg font-medium">{{ nextGoal.title }}</h3>
            <span class="text-xs px-2 py-0.5 rounded-full"
                  :class="getRarityClass(nextGoal.rarity)">
              {{ getRarityLabel(nextGoal.rarity) }}
            </span>
          </div>
          
          <p class="text-sm text-gray-600 mb-3">{{ nextGoal.description }}</p>
          
          <div class="flex justify-between items-center">
            <div class="text-xs text-gray-500">
              {{ nextGoal.points }} 点数
            </div>
            
            <el-button size="small" type="primary">查看详情</el-button>
          </div>
          
          <div class="mt-3">
            <div class="flex justify-between text-xs text-gray-500 mb-1">
              <span>进度</span>
              <span>{{ nextGoal.progress }}/{{ nextGoal.target }}</span>
            </div>
            <el-progress :percentage="(nextGoal.progress / nextGoal.target) * 100" :stroke-width="8" :show-text="false" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

// 筛选和搜索
const filter = ref('all')
const categoryFilter = ref('all')
const search = ref('')

// 成就统计
const totalAchievements = 25
const rareAchievements = 3
const achievementPoints = 450
const classRank = 5
const totalStudents = 120

// 成就类别
const categories = [
  { label: '学习成就', value: 'learning' },
  { label: '考试成就', value: 'exam' },
  { label: '参与成就', value: 'participation' },
  { label: '连续成就', value: 'streak' },
  { label: '社交成就', value: 'social' }
]

// 成就数据
const achievements = ref([
  {
    id: 1,
    title: '学习达人',
    description: '连续30天完成学习任务',
    category: 'streak',
    rarity: 'rare',
    points: 50,
    earned: true,
    earnedDate: '2023-10-15',
    icon: 'medal',
    colorName: 'purple',
    color: '#8b5cf6'
  },
  {
    id: 2,
    title: '微积分精通',
    description: '微积分测验获得满分',
    category: 'exam',
    rarity: 'uncommon',
    points: 30,
    earned: true,
    earnedDate: '2023-10-10',
    icon: 'trophy',
    colorName: 'amber',
    color: '#f59e0b'
  },
  {
    id: 3,
    title: '勤学好问',
    description: '在讨论区发布10个有价值的问题',
    category: 'participation',
    rarity: 'common',
    points: 20,
    earned: true,
    earnedDate: '2023-10-05',
    icon: 'question',
    colorName: 'blue',
    color: '#3b82f6'
  },
  {
    id: 4,
    title: '知识分享者',
    description: '帮助5位同学解答问题',
    category: 'social',
    rarity: 'common',
    points: 15,
    earned: true,
    earnedDate: '2023-09-28',
    icon: 'share',
    colorName: 'green',
    color: '#10b981'
  },
  {
    id: 5,
    title: '线性代数专家',
    description: '线性代数测验获得满分',
    category: 'exam',
    rarity: 'uncommon',
    points: 30,
    earned: false,
    icon: 'first-aid-kit',
    colorName: 'blue',
    color: '#3b82f6'
  },
  {
    id: 6,
    title: '学习狂人',
    description: '单日学习时间超过8小时',
    category: 'learning',
    rarity: 'uncommon',
    points: 25,
    earned: true,
    earnedDate: '2023-09-20',
    icon: 'time',
    colorName: 'green',
    color: '#10b981'
  },
  {
    id: 7,
    title: '完美出勤',
    description: '一学期全勤，无缺课记录',
    category: 'streak',
    rarity: 'rare',
    points: 40,
    earned: false,
    progress: 12,
    target: 16,
    icon: 'date',
    colorName: 'amber',
    color: '#f59e0b'
  },
  {
    id: 8,
    title: '讨论活跃者',
    description: '在讨论区发表50条评论',
    category: 'social',
    rarity: 'common',
    points: 15,
    earned: false,
    progress: 32,
    target: 50,
    icon: 'chat-dot-round',
    colorName: 'blue',
    color: '#3b82f6'
  },
  {
    id: 9,
    title: '概率统计新手',
    description: '完成概率统计第一次测验',
    category: 'exam',
    rarity: 'common',
    points: 10,
    earned: true,
    earnedDate: '2023-09-15',
    icon: 'data-analysis',
    colorName: 'purple',
    color: '#8b5cf6'
  }
])

// 已获得的成就
const earnedAchievements = computed(() => {
  return achievements.value.filter(a => a.earned)
})

// 筛选成就
const filteredAchievements = computed(() => {
  let result = [...achievements.value]
  
  // 按获得状态筛选
  if (filter.value === 'earned') {
    result = result.filter(a => a.earned)
  } else if (filter.value === 'locked') {
    result = result.filter(a => !a.earned)
  }
  
  // 按类别筛选
  if (categoryFilter.value !== 'all') {
    result = result.filter(a => a.category === categoryFilter.value)
  }
  
  // 搜索
  if (search.value) {
    const searchLower = search.value.toLowerCase()
    result = result.filter(a => 
      a.title.toLowerCase().includes(searchLower) || 
      a.description.toLowerCase().includes(searchLower)
    )
  }
  
  return result
})

// 下一个目标成就
const nextGoal = {
  title: '完美出勤',
  description: '一学期全勤，无缺课记录',
  rarity: 'rare',
  points: 40,
  progress: 12,
  target: 16,
  icon: 'date',
  colorName: 'amber'
}

// 获取稀有度标签
const getRarityLabel = (rarity) => {
  const labels = {
    'common': '普通',
    'uncommon': '优秀',
    'rare': '稀有',
    'epic': '史诗',
    'legendary': '传说'
  }
  return labels[rarity] || '普通'
}

// 获取稀有度样式类
const getRarityClass = (rarity) => {
  const classes = {
    'common': 'bg-gray-100 text-gray-700',
    'uncommon': 'bg-green-100 text-green-700',
    'rare': 'bg-blue-100 text-blue-700',
    'epic': 'bg-purple-100 text-purple-700',
    'legendary': 'bg-amber-100 text-amber-700'
  }
  return classes[rarity] || 'bg-gray-100 text-gray-700'
}
</script>

<style scoped>
.achievements-page {
  min-height: calc(100vh - 64px);
  background-color: #f8fafc;
}

.achievement-card {
  transition: all 0.3s ease;
}

.achievement-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.05);
}

/* 动画效果 */
.achievements-grid {
  perspective: 1000px;
}

.achievement-card {
  animation: fadeIn 0.5s ease-out;
  animation-fill-mode: both;
}

.achievement-card:nth-child(3n+1) { animation-delay: 0.1s; }
.achievement-card:nth-child(3n+2) { animation-delay: 0.2s; }
.achievement-card:nth-child(3n+3) { animation-delay: 0.3s; }

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px) rotateX(5deg);
  }
  to {
    opacity: 1;
    transform: translateY(0) rotateX(0);
  }
}

/* 下一个目标悬浮效果 */
.next-goal {
  transition: all 0.3s ease;
}

.next-goal:hover {
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08);
}
</style> 