<script setup>
import { ref } from 'vue';
import draggable from 'vuedraggable';

const props = defineProps({
  blocks: {
    type: Array,
    required: true
  }
});

const emit = defineEmits(['update:blocks', 'add-block', 'remove-block']);

// 定义不同块类型的配置
const blockTypes = [
  { 
    id: 'introduction',
    name: '导入环节',
    icon: 'M12 6v6m0 0v6m0-6h6m-6 0H6',
    color: 'blue' 
  },
  { 
    id: 'content',
    name: '教学内容',
    icon: 'M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253',
    color: 'green' 
  },
  { 
    id: 'activity',
    name: '教学活动',
    icon: 'M13 10V3L4 14h7v7l9-11h-7z',
    color: 'purple' 
  },
  { 
    id: 'assessment',
    name: '评价环节',
    icon: 'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z',
    color: 'amber' 
  }
];

// 添加教案块
const addBlock = (type) => {
  const blockId = Date.now().toString();
  
  const newBlock = {
    id: blockId,
    type: type,
    title: '',
    content: '',
    duration: 10,
  };
  
  emit('add-block', newBlock);
};

// 删除教案块
const removeBlock = (index) => {
  emit('remove-block', index);
};

// 更新块内容
const updateBlockField = (index, field, value) => {
  const updatedBlocks = [...props.blocks];
  updatedBlocks[index][field] = value;
  emit('update:blocks', updatedBlocks);
};

// 获取块类型名称
const getBlockTypeName = (type) => {
  const blockType = blockTypes.find(bt => bt.id === type);
  return blockType ? blockType.name : '未知类型';
};

// 获取块类型颜色
const getBlockTypeColor = (type) => {
  const blockType = blockTypes.find(bt => bt.id === type);
  return blockType ? blockType.color : 'gray';
};
</script>

<template>
  <div class="lesson-content-builder">
    <div class="content-header">
      <h2>教案内容设计</h2>
      <div class="block-type-buttons">
        <button 
          v-for="blockType in blockTypes" 
          :key="blockType.id"
          :class="[`block-type-btn`, `${blockType.color}-btn`]"
          @click="addBlock(blockType.id)"
          :title="`添加${blockType.name}`"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="blockType.icon" />
          </svg>
          <span>{{ blockType.name }}</span>
        </button>
      </div>
    </div>
    
    <!-- 拖拽区域 -->
    <draggable 
      :list="blocks"
      @update="$emit('update:blocks', blocks)"
      group="lesson-blocks" 
      item-key="id"
      handle=".drag-handle"
      ghost-class="ghost-block"
      animation="200"
      @start="drag = true"
      @end="drag = false"
    >
      <template #item="{element, index}">
        <div 
          class="lesson-block"
          :class="[`block-${getBlockTypeColor(element.type)}`]"
        >
          <div class="block-header">
            <div class="block-header-left">
              <div class="drag-handle">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 8h16M4 16h16" />
                </svg>
              </div>
              <span class="block-type-badge">
                {{ getBlockTypeName(element.type) }}
              </span>
            </div>
            
            <div class="block-header-right">
              <div class="duration-control">
                <span>时长:</span>
                <div class="duration-input">
                  <button 
                    class="duration-decrement"
                    @click="updateBlockField(index, 'duration', Math.max(1, element.duration - 1))"
                  >-</button>
                  <input 
                    type="number" 
                    :value="element.duration" 
                    @input="updateBlockField(index, 'duration', parseInt($event.target.value) || 1)"
                    min="1"
                    max="60"
                  />
                  <button 
                    class="duration-increment"
                    @click="updateBlockField(index, 'duration', Math.min(60, element.duration + 1))"
                  >+</button>
                </div>
                <span>分钟</span>
              </div>
              
              <button 
                class="block-delete-btn"
                @click="removeBlock(index)"
              >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                </svg>
              </button>
            </div>
          </div>
          
          <div class="block-content">
            <div class="form-group">
              <label>标题</label>
              <input 
                type="text" 
                :value="element.title" 
                @input="updateBlockField(index, 'title', $event.target.value)"
                placeholder="输入环节标题"
              />
            </div>
            
            <div class="form-group">
              <label>内容描述</label>
              <textarea 
                :value="element.content" 
                @input="updateBlockField(index, 'content', $event.target.value)"
                rows="3"
                placeholder="输入内容描述"
              ></textarea>
            </div>
          </div>
          
          <!-- 时间轴可视化 -->
          <div class="duration-visualizer">
            <div 
              class="duration-progress"
              :class="[`progress-${getBlockTypeColor(element.type)}`]"
              :style="{ width: `${Math.min(100, element.duration / 60 * 100)}%` }"
            ></div>
          </div>
        </div>
      </template>
    </draggable>
    
    <!-- 空状态 -->
    <div v-if="blocks.length === 0" class="empty-blocks">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
      </svg>
      <p>点击上方按钮添加教案内容，或从左侧大纲中选择内容添加</p>
    </div>
  </div>
</template>

<style scoped>
.lesson-content-builder {
  @apply bg-white rounded-lg shadow-sm p-6 border border-gray-200;
}

.content-header {
  @apply flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-5;
}

.content-header h2 {
  @apply text-lg font-medium text-gray-800;
}

.block-type-buttons {
  @apply flex flex-wrap gap-2;
}

.block-type-btn {
  @apply flex items-center px-3 py-1.5 rounded-md text-sm font-medium transition-colors duration-200;
}

.block-type-btn svg {
  @apply mr-1.5;
}

.blue-btn {
  @apply bg-blue-100 text-blue-700 hover:bg-blue-200;
}

.green-btn {
  @apply bg-green-100 text-green-700 hover:bg-green-200;
}

.purple-btn {
  @apply bg-purple-100 text-purple-700 hover:bg-purple-200;
}

.amber-btn {
  @apply bg-amber-100 text-amber-700 hover:bg-amber-200;
}

.blocks-container {
  @apply space-y-4 mt-4;
}

.lesson-block {
  @apply rounded-lg border p-4 transition-all duration-300;
}

.block-blue {
  @apply border-blue-300 bg-blue-50;
}

.block-green {
  @apply border-green-300 bg-green-50;
}

.block-purple {
  @apply border-purple-300 bg-purple-50;
}

.block-amber {
  @apply border-amber-300 bg-amber-50;
}

.ghost {
  @apply opacity-50 border-dashed;
}

.chosen {
  @apply shadow-md;
}

.block-header {
  @apply flex items-center justify-between mb-4;
}

.block-header-left {
  @apply flex items-center;
}

.block-header-right {
  @apply flex items-center gap-3;
}

.drag-handle {
  @apply cursor-move p-1 rounded-md hover:bg-white/50 mr-2;
}

.block-type-badge {
  @apply font-medium;
}

.duration-control {
  @apply flex items-center text-sm space-x-1;
}

.duration-input {
  @apply flex items-center;
}

.duration-input input {
  @apply w-12 text-center px-1 py-1 text-sm border border-gray-300 focus:outline-none focus:ring-1 focus:ring-blue-500;
}

.duration-decrement, .duration-increment {
  @apply flex items-center justify-center w-6 h-6 bg-white/60 hover:bg-white text-gray-700 border border-gray-300 transition-colors duration-150;
}

.duration-decrement {
  @apply rounded-l-md border-r-0;
}

.duration-increment {
  @apply rounded-r-md border-l-0;
}

.block-delete-btn {
  @apply p-1.5 rounded-full text-gray-400 hover:text-red-500 hover:bg-red-50 transition-colors duration-150;
}

.block-content {
  @apply space-y-3;
}

.form-group {
  @apply space-y-1;
}

.form-group label {
  @apply block text-sm font-medium text-gray-600;
}

.form-group input, .form-group textarea {
  @apply w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all duration-200;
}

.duration-visualizer {
  @apply h-2 bg-gray-200 rounded-full overflow-hidden mt-3;
}

.duration-progress {
  @apply h-full rounded-full;
}

.progress-blue {
  @apply bg-blue-500;
}

.progress-green {
  @apply bg-green-500;
}

.progress-purple {
  @apply bg-purple-500;
}

.progress-amber {
  @apply bg-amber-500;
}

.empty-blocks {
  @apply flex flex-col items-center justify-center py-12 border-2 border-dashed border-gray-300 rounded-lg text-center;
}

.empty-blocks svg {
  @apply text-gray-400 mb-2;
}

.empty-blocks p {
  @apply text-gray-500 max-w-sm;
}
</style>