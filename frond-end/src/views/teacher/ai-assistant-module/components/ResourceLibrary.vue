<script setup>
import { ref, onMounted, watch, computed } from 'vue';

const props = defineProps({
  resources: {
    type: Array,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  },
  themeColor: {
    type: String,
    default: 'blue'
  }
});

const emit = defineEmits(['preview', 'edit', 'delete', 'view-details', 'upload', 'duplicate', 'share', 'export']);

// 状态变量
const searchQuery = ref('');
const selectedCategory = ref('all');
const selectedTag = ref('all');
const sortBy = ref('date'); // 'date' | 'name' | 'type'
const sortOrder = ref('desc'); // 'asc' | 'desc'
const viewMode = ref('grid'); // 'grid' | 'list'
const showUploadDialog = ref(false);
const showImportDialog = ref(false);
const showExportDialog = ref(false);
const selectedResources = ref([]);
const uploadFile = ref(null);
const uploadTitle = ref('');
const uploadType = ref('lesson');
const uploadTags = ref([]);
const newTagInput = ref('');
const uploading = ref(false);
const fileInputRef = ref(null);

// 所有标签
const allTags = computed(() => {
  const tagSet = new Set();
  props.resources.forEach(resource => {
    if (resource.tags && Array.isArray(resource.tags)) {
      resource.tags.forEach(tag => tagSet.add(tag));
    }
  });
  return Array.from(tagSet);
});

// 资源类型列表
const resourceTypes = [
  { id: 'all', name: '全部' },
  { id: 'lesson', name: '教案' },
  { id: 'questions', name: '习题' },
  { id: 'quiz', name: '测试' },
  { id: 'presentation', name: '课件' },
  { id: 'material', name: '素材' }
];

// 过滤后的资源
const filteredResources = computed(() => {
  // 先过滤
  let result = props.resources.filter(resource => {
    // 搜索过滤
    const matchesSearch = searchQuery.value === '' || 
      resource.title.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      (resource.tags && resource.tags.some(tag => tag.toLowerCase().includes(searchQuery.value.toLowerCase())));
    
    // 分类过滤
    const matchesCategory = selectedCategory.value === 'all' || 
      resource.type === selectedCategory.value;
      
    // 标签过滤
    const matchesTag = selectedTag.value === 'all' ||
      (resource.tags && resource.tags.includes(selectedTag.value));
    
    return matchesSearch && matchesCategory && matchesTag;
  });
  
  // 再排序
  result.sort((a, b) => {
    let comparison = 0;
    if (sortBy.value === 'date') {
      comparison = new Date(a.createdAt) - new Date(b.createdAt);
    } else if (sortBy.value === 'name') {
      comparison = a.title.localeCompare(b.title);
    } else if (sortBy.value === 'type') {
      comparison = a.type.localeCompare(b.type);
    }
    
    return sortOrder.value === 'desc' ? -comparison : comparison;
  });
  
  return result;
});

// 切换排序方式
const toggleSort = (field) => {
  if (sortBy.value === field) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc';
  } else {
    sortBy.value = field;
    sortOrder.value = 'desc';
  }
};

// 重置过滤器
const resetFilters = () => {
  searchQuery.value = '';
  selectedCategory.value = 'all';
  selectedTag.value = 'all';
  sortBy.value = 'date';
  sortOrder.value = 'desc';
};

// 切换视图模式
const toggleViewMode = () => {
  viewMode.value = viewMode.value === 'grid' ? 'list' : 'grid';
};

// 添加标签
const addTag = () => {
  if (newTagInput.value.trim() && !uploadTags.value.includes(newTagInput.value.trim())) {
    uploadTags.value.push(newTagInput.value.trim());
    newTagInput.value = '';
  }
};

// 删除标签
const removeTag = (tag) => {
  const index = uploadTags.value.indexOf(tag);
  if (index !== -1) {
    uploadTags.value.splice(index, 1);
  }
};

// 选择/取消选择资源
const toggleSelectResource = (resource) => {
  const index = selectedResources.value.findIndex(r => r.id === resource.id);
  if (index === -1) {
    selectedResources.value.push(resource);
  } else {
    selectedResources.value.splice(index, 1);
  }
};

// 检查资源是否被选中
const isResourceSelected = (resource) => {
  return selectedResources.value.findIndex(r => r.id === resource.id) !== -1;
};

// 批量操作
const batchExport = () => {
  if (selectedResources.value.length === 0) {
    alert('请先选择要导出的资源');
    return;
  }
  
  emit('export', selectedResources.value);
};

// 批量删除
const batchDelete = () => {
  if (selectedResources.value.length === 0) {
    alert('请先选择要删除的资源');
    return;
  }
  
  if (confirm(`确定要删除选中的 ${selectedResources.value.length} 个资源吗？`)) {
    selectedResources.value.forEach(resource => {
      emit('delete', resource.id);
    });
    selectedResources.value = [];
  }
};

// 复制资源
const duplicateResource = (resource) => {
  emit('duplicate', resource);
};

// 分享资源
const shareResource = (resource) => {
  // 创建一个临时输入框，用于复制链接
  const tempInput = document.createElement('input');
  const shareUrl = `${window.location.origin}/share/resource/${resource.id}`;
  
  tempInput.value = shareUrl;
  document.body.appendChild(tempInput);
  tempInput.select();
  document.execCommand('copy');
  document.body.removeChild(tempInput);
  
  alert(`资源分享链接已复制到剪贴板！\n${shareUrl}`);
  
  emit('share', { resource, url: shareUrl });
};

// 获取资源类型图标
const getResourceIcon = (type) => {
  if (type === 'lesson') {
    return `
      <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
      </svg>
    `;
  } else {
    return `
      <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
    `;
  }
};

// 格式化日期
const formatDate = (date) => {
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  });
};

// 根据类型获取资源名称
const getResourceTypeName = (type) => {
  return type === 'lesson' ? '教案' : '习题集';
};

// 打开上传对话框
const openUploadDialog = () => {
  resetUploadForm();
  showUploadDialog.value = true;
};

// 关闭上传对话框
const closeUploadDialog = () => {
  showUploadDialog.value = false;
  resetUploadForm();
};

// 重置上传表单
const resetUploadForm = () => {
  uploadTitle.value = '';
  uploadType.value = 'lesson';
  uploadFile.value = null;
  if (fileInputRef.value) {
    fileInputRef.value.value = '';
  }
};

// 处理文件选择
const handleFileSelect = (event) => {
  const file = event.target.files[0];
  if (file) {
    uploadFile.value = file;
    // 如果没有设置标题，则使用文件名作为标题（去除扩展名）
    if (!uploadTitle.value) {
      uploadTitle.value = file.name.replace(/\.[^/.]+$/, "");
    }
  }
};

// 提交上传
const submitUpload = async () => {
  // 表单验证
  if (!uploadTitle.value.trim()) {
    alert('请输入资源标题');
    return;
  }
  
  if (!uploadFile.value) {
    alert('请选择要上传的文件');
    return;
  }
  
  uploading.value = true;
  
  try {
    // 读取文件内容
    const content = await readFileContent(uploadFile.value);
    
    // 创建资源对象
    const resource = {
      title: uploadTitle.value,
      type: uploadType.value,
      content: content,
      fileName: uploadFile.value.name
    };
    
    // 发送到父组件处理上传
    emit('upload', resource);
    
    // 关闭对话框
    closeUploadDialog();
  } catch (error) {
    console.error('上传失败', error);
    alert('上传失败: ' + error.message);
  } finally {
    uploading.value = false;
  }
};

// 读取文件内容为文本
const readFileContent = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = (e) => resolve(e.target.result);
    reader.onerror = (e) => reject(new Error('文件读取失败'));
    reader.readAsText(file);
  });
};

// 触发文件选择对话框
const triggerFileSelect = () => {
  if (fileInputRef.value) {
    fileInputRef.value.click();
  }
};
</script>

<template>
  <div class="resource-library">
    <div class="library-header">
      <div class="search-filter">
        <div class="search-box">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          <input 
            v-model="searchQuery" 
            type="text" 
            placeholder="搜索资源..."
          />
        </div>
        
        <div class="filter-section">
          <select v-model="selectedCategory">
            <option value="all">全部类型</option>
            <option value="lesson">教案</option>
            <option value="questions">习题集</option>
          </select>
          
          <button class="reset-btn" @click="resetFilters">
            重置过滤
          </button>
        </div>
      </div>
      
      <div class="header-actions">
        <div class="sort-options">
          <div class="sort-label">排序方式:</div>
          <button 
            class="sort-btn" 
            :class="{ active: sortBy === 'date' }"
            @click="toggleSort('date')"
          >
            日期
            <svg v-if="sortBy === 'date'" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 15l7-7 7 7" v-if="sortOrder === 'asc'" />
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" v-else />
            </svg>
          </button>
          
          <button 
            class="sort-btn" 
            :class="{ active: sortBy === 'name' }"
            @click="toggleSort('name')"
          >
            名称
            <svg v-if="sortBy === 'name'" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 15l7-7 7 7" v-if="sortOrder === 'asc'" />
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" v-else />
            </svg>
          </button>
          
          <button 
            class="sort-btn" 
            :class="{ active: sortBy === 'type' }"
            @click="toggleSort('type')"
          >
            类型
            <svg v-if="sortBy === 'type'" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 15l7-7 7 7" v-if="sortOrder === 'asc'" />
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" v-else />
            </svg>
          </button>
        </div>
        
        <button class="upload-btn" @click="openUploadDialog">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
          </svg>
          上传资源
        </button>
      </div>
    </div>
    
    <div class="resources-container">
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载资源中...</p>
      </div>
      
      <div v-else-if="filteredResources.length === 0" class="empty-state">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
        </svg>
        <p>{{ searchQuery || selectedCategory !== 'all' ? '没有符合条件的资源' : '暂无已保存的资源' }}</p>
        <button class="upload-btn mt-4" @click="openUploadDialog">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
          </svg>
          上传资源
        </button>
      </div>
      
      <div v-else class="resource-grid">
        <div 
          v-for="resource in filteredResources" 
          :key="resource.id"
          class="resource-card"
        >
          <div class="resource-type-badge" :class="resource.type === 'lesson' ? 'lesson-badge' : 'questions-badge'">
            {{ getResourceTypeName(resource.type) }}
          </div>
          
          <div class="resource-icon" v-html="getResourceIcon(resource.type)"></div>
          
          <div class="resource-details">
            <h3 class="resource-title">{{ resource.title }}</h3>
            <p class="resource-date">创建于 {{ formatDate(resource.createdAt) }}</p>
          </div>
          
          <div class="resource-actions">
            <button 
              class="action-btn preview-btn"
              title="预览"
              @click="emit('preview', resource)"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
              </svg>
              预览
            </button>
            
            <button 
              class="action-btn edit-btn"
              title="编辑"
              @click="emit('edit', resource)"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
              编辑
            </button>
            
            <button 
              class="action-btn delete-btn"
              title="删除"
              @click="emit('delete', resource)"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
              删除
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 上传对话框 -->
    <div v-if="showUploadDialog" class="upload-dialog-overlay">
      <div class="upload-dialog">
        <div class="dialog-header">
          <h3>上传资源</h3>
          <button class="close-btn" @click="closeUploadDialog">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
        
        <div class="dialog-content">
          <div class="form-group">
            <label>资源标题 <span class="required">*</span></label>
            <input 
              type="text" 
              v-model="uploadTitle" 
              placeholder="请输入资源标题"
              :disabled="uploading"
            />
          </div>
          
          <div class="form-group">
            <label>资源类型 <span class="required">*</span></label>
            <select 
              v-model="uploadType"
              :disabled="uploading"
            >
              <option value="lesson">教案</option>
              <option value="questions">习题集</option>
            </select>
          </div>
          
          <div class="form-group">
            <label>选择文件 <span class="required">*</span></label>
            <div class="file-upload-container">
              <input 
                type="file" 
                ref="fileInputRef" 
                @change="handleFileSelect" 
                accept=".md,.txt,.docx,.pdf" 
                style="display:none"
                :disabled="uploading"
              />
              <button 
                class="file-select-btn" 
                @click="triggerFileSelect"
                :disabled="uploading"
              >
                选择文件
              </button>
              <span class="file-name">{{ uploadFile ? uploadFile.name : '未选择文件' }}</span>
            </div>
            <p class="file-hint">支持的文件类型：Markdown (.md)、文本文件 (.txt)、Word文档 (.docx)、PDF文件 (.pdf)</p>
          </div>
        </div>
        
        <div class="dialog-footer">
          <button 
            class="cancel-btn" 
            @click="closeUploadDialog"
            :disabled="uploading"
          >
            取消
          </button>
          <button 
            class="submit-btn" 
            @click="submitUpload"
            :disabled="uploading"
          >
            <div v-if="uploading" class="btn-spinner"></div>
            <span>{{ uploading ? '上传中...' : '上传' }}</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.resource-library {
  @apply flex flex-col h-full bg-white overflow-hidden;
  border-radius: 0;
}

.library-header {
  @apply flex flex-col sm:flex-row justify-between items-start sm:items-center p-3 md:p-4 border-b border-gray-200 gap-3 md:gap-4;
}

.search-filter {
  @apply flex flex-col sm:flex-row items-start sm:items-center gap-2 md:gap-3 w-full;
}

.search-box {
  @apply flex items-center w-full sm:w-64 px-3 py-1.5 md:py-2 border border-gray-300 rounded-lg focus-within:ring-2 focus-within:ring-blue-500 focus-within:border-blue-500 transition-all duration-200;
}

.search-box svg {
  @apply text-gray-400 mr-2 h-4 w-4;
}

.search-box input {
  @apply flex-1 outline-none border-none bg-transparent text-sm;
}

.filter-section {
  @apply flex flex-wrap items-center gap-2 mt-2 sm:mt-0;
}

.filter-section select {
  @apply px-2 md:px-3 py-1.5 md:py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all duration-200 text-xs md:text-sm;
}

.reset-btn {
  @apply px-2 md:px-3 py-1.5 md:py-2 text-xs md:text-sm text-blue-600 hover:bg-blue-50 rounded-lg transition-colors;
}

.header-actions {
  @apply flex flex-col sm:flex-row items-start sm:items-center gap-2 md:gap-3 mt-2 sm:mt-0 w-full sm:w-auto;
}

.sort-options {
  @apply flex flex-wrap items-center gap-1 md:gap-2;
}

.sort-label {
  @apply text-xs md:text-sm text-gray-500;
}

.sort-btn {
  @apply flex items-center px-2 md:px-3 py-1 md:py-1.5 text-xs md:text-sm border border-gray-200 rounded-lg transition-colors;
}

.sort-btn.active {
  @apply bg-blue-50 border-blue-200 text-blue-600;
}

.sort-btn svg {
  @apply ml-1 h-3 w-3 md:h-4 md:w-4;
}

.upload-btn {
  @apply flex items-center px-3 md:px-4 py-1.5 md:py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium text-xs md:text-sm;
}

.resources-container {
  @apply flex-1 overflow-y-auto p-3 md:p-4;
}

.loading-state {
  @apply flex flex-col items-center justify-center py-8 md:py-12;
}

.loading-spinner {
  @apply w-8 md:w-10 h-8 md:h-10 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin mb-3 md:mb-4;
}

.empty-state {
  @apply flex flex-col items-center justify-center py-8 md:py-12 text-gray-400;
}

.empty-state p {
  @apply mt-3 md:mt-4 text-sm md:text-base;
}

.resource-grid {
  @apply grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 md:gap-4;
}

.resource-card {
  @apply bg-white rounded-lg border border-gray-200 shadow-sm hover:shadow-md transition-shadow duration-200 p-3 md:p-5 relative;
}

.resource-type-badge {
  @apply absolute top-2 md:top-3 right-2 md:right-3 text-xs px-2 py-0.5 md:py-1 rounded-full font-medium;
}

.lesson-badge {
  @apply bg-green-100 text-green-700;
}

.questions-badge {
  @apply bg-purple-100 text-purple-700;
}

.resource-icon {
  @apply h-10 w-10 md:h-12 md:w-12 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 mb-3 md:mb-4;
}

.resource-icon svg {
  @apply h-5 w-5 md:h-6 md:w-6;
}

.resource-details {
  @apply mb-3 md:mb-4;
}

.resource-title {
  @apply text-base md:text-lg font-medium text-gray-800 mb-1;
}

.resource-date {
  @apply text-xs md:text-sm text-gray-500;
}

.resource-actions {
  @apply flex flex-wrap gap-1 md:gap-2;
}

.action-btn {
  @apply flex items-center px-2 md:px-3 py-1 md:py-1.5 rounded-lg text-xs md:text-sm font-medium transition-colors;
}

.preview-btn {
  @apply text-blue-700 bg-blue-50 hover:bg-blue-100;
}

.edit-btn {
  @apply text-amber-700 bg-amber-50 hover:bg-amber-100;
}

.delete-btn {
  @apply text-red-700 bg-red-50 hover:bg-red-100;
}

.action-btn svg {
  @apply mr-1 h-4 w-4;
}

.upload-dialog-overlay {
  @apply fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4;
}

.upload-dialog {
  @apply bg-white rounded-xl shadow-lg w-full max-w-md overflow-hidden;
}

.dialog-header {
  @apply flex items-center justify-between border-b border-gray-200 px-4 md:px-6 py-3 md:py-4;
}

.dialog-header h3 {
  @apply text-base md:text-lg font-medium text-gray-800;
}

.close-btn {
  @apply text-gray-500 hover:text-gray-700 transition-colors;
}

.dialog-content {
  @apply p-4 md:p-6 space-y-3 md:space-y-4;
}

.form-group {
  @apply flex flex-col;
}

.form-group label {
  @apply text-xs md:text-sm font-medium text-gray-700 mb-1 flex items-center;
}

.form-group input,
.form-group select {
  @apply px-3 md:px-4 py-1.5 md:py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 text-sm;
}

.required {
  @apply text-red-500 ml-1;
}

.file-upload-container {
  @apply flex items-center gap-2;
}

.file-select-btn {
  @apply px-2 md:px-3 py-1.5 md:py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors text-xs md:text-sm;
}

.file-name {
  @apply text-xs md:text-sm text-gray-600 truncate max-w-[140px] md:max-w-[200px];
}

.file-hint {
  @apply text-xs text-gray-500 mt-1 md:mt-2;
}

.dialog-footer {
  @apply flex justify-end gap-2 md:gap-3 bg-gray-50 px-4 md:px-6 py-3 md:py-4;
}

.cancel-btn {
  @apply px-3 md:px-4 py-1.5 md:py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors text-xs md:text-sm;
}

.submit-btn {
  @apply flex items-center px-3 md:px-4 py-1.5 md:py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:bg-blue-400 text-xs md:text-sm;
}

.btn-spinner {
  @apply w-3 h-3 md:w-4 md:h-4 border-2 border-white border-t-transparent rounded-full animate-spin mr-1 md:mr-2;
}


@media (max-width: 640px) {
  .library-header {
    @apply flex-col items-stretch gap-2;
  }
  
  .search-filter {
    @apply flex-col items-stretch;
  }
  
  .search-box {
    @apply w-full;
  }
  
  .header-actions {
    @apply flex-col items-stretch;
  }
  
  .sort-options {
    @apply justify-between;
  }
  
  .resource-grid {
    @apply grid-cols-1;
  }
  
  .action-btn span {
    @apply text-xs;
  }
  
  .resource-card {
    @apply p-3;
  }
}
</style> 