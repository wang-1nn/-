<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElImageViewer, ElMessage } from 'element-plus'
import { get, post } from '@/net'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/counter'

const route = useRoute()
const authStore = useAuthStore()
const courseId = route.params.courseId
const loading = ref(false)
const resources = ref([])

// 搜索和过滤
const query = ref('')
const type = ref('all')
const sort = ref('date')
const viewMode = ref('grid') // grid 或 list

// 预览相关
const previewUrl = ref(null)
const previewType = ref(null)
const previewTitle = ref('')

// 获取课程资源列表
const fetchResources = () => {
  loading.value = true
  
  get(`/api/student/courses/${courseId}/resources`, null,
    (message, data) => {
      resources.value = data || []
      loading.value = false
    },
    (message) => {
      ElMessage.error(message || '获取课程资源失败')
      loading.value = false
    }
  )
}

// 过滤资源
const filtered = computed(() => {
  let result = resources.value.filter(r => {
    const matchType = type.value === 'all' || r.resourceType?.toLowerCase() === type.value
    const matchSearch = r.title?.toLowerCase().includes(query.value.toLowerCase())
    return matchType && matchSearch
  })
  
  // 排序
  if (sort.value === 'date') {
    result.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
  } else if (sort.value === 'downloads') {
    result.sort((a, b) => b.downloadCount - a.downloadCount)
  } else if (sort.value === 'name') {
    result.sort((a, b) => a.title.localeCompare(b.title))
  }
  
  return result
})

// 资源类型图标映射
const iconMap = {
  pdf: '📄',
  video: '🎬',
  ppt: '📊',
  doc: '📃',
  link: '🔗',
  img: '🖼️',
  zip: '📦'
}

// 资源类型颜色映射
const colorMap = {
  document: 'bg-red-100 text-red-600',
  video: 'bg-blue-100 text-blue-600',
  presentation: 'bg-amber-100 text-amber-600',
  document: 'bg-indigo-100 text-indigo-600',
  link: 'bg-green-100 text-green-600',
  image: 'bg-purple-100 text-purple-600',
  code: 'bg-gray-100 text-gray-600',
  exercise: 'bg-teal-100 text-teal-600'
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 下载资源
const downloadResource = (res, event) => {
  event.stopPropagation()
  
  // 直接使用文件URL进行下载
  if (res.url || res.filePath) {
    const downloadUrl = res.url || res.filePath
    
    // 创建一个临时的a标签来触发下载
    const a = document.createElement('a')
    a.href = downloadUrl
    a.download = res.title || 'download'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    
    ElMessage({
      message: `正在下载: ${res.title}`,
      type: 'success',
      duration: 1500
    })
    
    // 更新下载计数（仅UI显示，实际计数需要后端支持）
    res.downloadCount = (res.downloadCount || 0) + 1
  } else {
    ElMessage.error('下载链接不可用')
  }
}

// 打开预览
const openPreview = (res) => {
  if (res.resourceType === 'IMAGE') {
    previewUrl.value = res.url || res.filePath
    previewType.value = 'img'
    previewTitle.value = res.title
  } else if (['DOCUMENT', 'PRESENTATION', 'CODE'].includes(res.resourceType)) {
    previewUrl.value = res.url || res.filePath
    previewType.value = res.resourceType.toLowerCase()
    previewTitle.value = res.title
    // 实际项目中这里应该打开文档预览器
    ElMessage({
      message: `预览功能开发中: ${res.title}`,
      type: 'info',
      duration: 1500
    })
  } else if (res.resourceType === 'VIDEO') {
    previewUrl.value = res.url || res.filePath
    previewType.value = 'video'
    previewTitle.value = res.title
    // 实际项目中这里应该打开视频播放器
    ElMessage({
      message: `视频播放功能开发中: ${res.title}`,
      type: 'info',
      duration: 1500
    })
  } else if (res.url) {
    window.open(res.url, '_blank')
  } else {
    // 其他类型默认下载
    downloadResource(res, { stopPropagation: () => {} })
  }
}

// 关闭预览
const closePreview = () => {
  previewUrl.value = null
  previewType.value = null
}

onMounted(() => {
  fetchResources()
})
</script>

<template>
  <div class="resources-wrapper" v-loading="loading">
    <!-- 工具栏 -->
    <div class="toolbar flex flex-wrap gap-3 mb-6 justify-between items-center">
      <div class="title text-xl font-semibold flex items-center">
        <i class="fas fa-book text-blue-500 mr-2"></i> 学习资源
        <span class="text-sm text-gray-500 ml-2">({{ filtered.length }} 个资源)</span>
      </div>
      
      <div class="flex flex-wrap gap-2 items-center">
        <el-input 
          v-model="query" 
          placeholder="搜索资源..." 
          size="small" 
          clearable 
          prefix-icon="el-icon-search" 
          class="w-48 md:w-60"
        />
        
        <el-select v-model="type" size="small" class="w-28">
          <el-option label="全部" value="all" />
          <el-option label="文档" value="pdf" />
          <el-option label="PPT" value="ppt" />
          <el-option label="视频" value="video" />
          <el-option label="图片" value="img" />
          <el-option label="链接" value="link" />
          <el-option label="压缩包" value="zip" />
        </el-select>
        
        <el-select v-model="sort" size="small" class="w-28">
          <el-option label="按日期" value="date" />
          <el-option label="按热度" value="downloads" />
          <el-option label="按名称" value="name" />
        </el-select>
        
        <div class="view-toggle flex rounded-md overflow-hidden border">
          <button 
            @click="viewMode = 'grid'" 
            class="px-2 py-1 text-sm" 
            :class="viewMode === 'grid' ? 'bg-blue-500 text-white' : 'bg-white text-gray-600'"
          >
            <i class="fas fa-th-large"></i>
          </button>
          <button 
            @click="viewMode = 'list'" 
            class="px-2 py-1 text-sm" 
            :class="viewMode === 'list' ? 'bg-blue-500 text-white' : 'bg-white text-gray-600'"
          >
            <i class="fas fa-list"></i>
          </button>
        </div>
      </div>
    </div>

    <!-- 资源列表 - 网格视图 -->
    <transition-group name="fade" tag="div" class="grid gap-5 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4" v-if="viewMode === 'grid'">
      <div 
        v-for="res in filtered" 
        :key="res.resourceId" 
        class="res-card group"
        @click="openPreview(res)"
      >
        <div class="relative">
          <div class="icon-wrapper" :class="colorMap[res.resourceType?.toLowerCase()] || 'bg-blue-100 text-blue-600'">
            <i :class="['fas', 
              res.resourceType === 'DOCUMENT' ? 'fa-file-pdf' : 
              res.resourceType === 'VIDEO' ? 'fa-file-video' :
              res.resourceType === 'PRESENTATION' ? 'fa-file-powerpoint' :
              res.resourceType === 'CODE' ? 'fa-file-code' :
              res.resourceType === 'IMAGE' ? 'fa-file-image' :
              res.resourceType === 'EXERCISE' ? 'fa-file-alt' : 'fa-file'
            ]"></i>
          </div>
          
          <div class="actions opacity-0 group-hover:opacity-100">
            <button @click="downloadResource(res, $event)" class="action-btn">
              <i class="fas fa-download"></i>
            </button>
          </div>
        </div>
        
        <div class="info">
          <h3 class="title">{{ res.title }}</h3>
          <div class="meta">
            <span class="type-badge" :class="`type-${res.resourceType?.toLowerCase()}`">{{ res.resourceType?.toUpperCase() }}</span>
            <span>{{ res.size }}</span>
            <span>{{ formatDate(res.createdAt) }}</span>
          </div>
          <div class="downloads">
            <i class="fas fa-download text-gray-400 mr-1"></i> {{ res.downloadCount || 0 }}
          </div>
        </div>
      </div>
    </transition-group>
    
    <!-- 资源列表 - 列表视图 -->
    <div class="overflow-x-auto" v-if="viewMode === 'list'">
      <table class="min-w-full bg-white rounded-xl overflow-hidden shadow">
        <thead class="bg-gray-50">
          <tr>
            <th class="py-3 px-4 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">资源名称</th>
            <th class="py-3 px-4 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">类型</th>
            <th class="py-3 px-4 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">大小</th>
            <th class="py-3 px-4 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">上传日期</th>
            <th class="py-3 px-4 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">下载次数</th>
            <th class="py-3 px-4 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-for="res in filtered" :key="res.resourceId" class="hover:bg-gray-50 transition-colors duration-150">
            <td class="py-3 px-4">
              <div class="flex items-center">
                <i :class="['fas mr-2 text-lg',
                  res.resourceType === 'DOCUMENT' ? 'fa-file-pdf text-red-500' : 
                  res.resourceType === 'VIDEO' ? 'fa-file-video text-blue-500' :
                  res.resourceType === 'PRESENTATION' ? 'fa-file-powerpoint text-amber-500' :
                  res.resourceType === 'CODE' ? 'fa-file-code text-indigo-500' :
                  res.resourceType === 'IMAGE' ? 'fa-file-image text-purple-500' :
                  res.resourceType === 'EXERCISE' ? 'fa-file-alt text-teal-500' : 'fa-file'
                ]"></i>
                <span class="font-medium cursor-pointer hover:text-blue-500" @click="openPreview(res)">
                  {{ res.title }}
                </span>
              </div>
            </td>
            <td class="py-3 px-4">
              <span class="type-badge" :class="`type-${res.resourceType?.toLowerCase()}`">{{ res.resourceType?.toUpperCase() }}</span>
            </td>
            <td class="py-3 px-4 text-gray-500">{{ res.size }}</td>
            <td class="py-3 px-4 text-gray-500">{{ formatDate(res.createdAt) }}</td>
            <td class="py-3 px-4 text-gray-500">{{ res.downloadCount || 0 }}</td>
            <td class="py-3 px-4 text-right">
              <div class="flex justify-end gap-2">
                <button @click="downloadResource(res, $event)" class="table-action-btn">
                  <i class="fas fa-download"></i>
                </button>
                <button @click="openPreview(res)" class="table-action-btn">
                  <i class="fas fa-eye"></i>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && filtered.length === 0" class="empty-state">
      <i class="fas fa-search text-gray-300 text-5xl mb-3"></i>
      <p class="text-gray-500 mb-2">未找到资源</p>
      <p class="text-gray-400 text-sm">尝试使用不同的搜索条件</p>
    </div>

    <!-- 图片预览 -->
    <el-image-viewer 
      v-if="previewUrl && previewType === 'img'" 
      :url-list="[previewUrl]" 
      :teleported="false" 
      @close="closePreview"
    />
    
    <!-- 其他类型预览 (实际项目中可以接入相应的预览组件) -->
    <div v-if="previewUrl && previewType !== 'img' && previewType !== null" class="preview-placeholder">
      <!-- 这里可以根据不同类型接入不同的预览组件 -->
    </div>
  </div>
</template>

<style scoped>
.resources-wrapper { 
  @apply p-6;
}

/* 卡片动画 */
.fade-enter-from { opacity: 0; transform: translateY(10px); } 
.fade-enter-to { opacity: 1; transform: translateY(0); } 
.fade-enter-active { transition: all .25s ease; }

.res-card { 
  @apply bg-white rounded-xl overflow-hidden shadow-md hover:shadow-lg transition duration-200 cursor-pointer; 
}

.icon-wrapper {
  @apply flex items-center justify-center h-32 text-3xl transition-all duration-300;
}

.res-card:hover .icon-wrapper {
  transform: scale(1.05);
}

.info { 
  @apply p-4 border-t; 
}

.title { 
  @apply font-medium truncate mb-2; 
}

.meta {
  @apply flex flex-wrap gap-2 text-xs text-gray-500 mb-2;
}

.downloads {
  @apply text-xs text-gray-500 flex items-center;
}

.type-badge {
  @apply px-2 py-0.5 rounded-full text-xs font-medium;
}

.type-document {
  @apply bg-red-100 text-red-600;
}

.type-video {
  @apply bg-blue-100 text-blue-600;
}

.type-presentation {
  @apply bg-amber-100 text-amber-600;
}

.type-code {
  @apply bg-indigo-100 text-indigo-600;
}

.type-image {
  @apply bg-purple-100 text-purple-600;
}

.type-link {
  @apply bg-green-100 text-green-600;
}

.type-zip {
  @apply bg-gray-100 text-gray-600;
}

.type-exercise {
  @apply bg-teal-100 text-teal-600;
}

.actions {
  @apply absolute top-2 right-2 flex gap-1 transition-opacity duration-200;
}

.action-btn {
  @apply bg-white rounded-full w-8 h-8 flex items-center justify-center shadow-sm hover:bg-gray-100 transition-colors;
}

.table-action-btn {
  @apply p-1.5 rounded hover:bg-gray-100 transition-colors;
}

.empty-state {
  @apply flex flex-col items-center justify-center py-16;
}

.view-toggle button {
  @apply transition-colors duration-200;
}

/* 响应式调整 */
@media (max-width: 640px) {
  .toolbar {
    @apply flex-col items-start;
  }
  
  .toolbar > div {
    @apply w-full;
  }
}
</style> 