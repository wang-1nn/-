<script setup>
import { ref, computed } from 'vue';

const props = defineProps({
  outlineData: {
    type: Object,
    default: null
  },
  expandAll: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['select-node', 'add-to-lesson-plan', 'toggle-unit', 'toggle-topic']);

// 展开的单元和主题
const expandedUnits = ref({});
const expandedTopics = ref({});
const selectedNode = ref('');

// 切换单元展开/折叠状态
const toggleUnit = (unitIndex) => {
  expandedUnits.value[unitIndex] = !expandedUnits.value[unitIndex];
  emit('toggle-unit', unitIndex, expandedUnits.value[unitIndex]);
};

// 切换主题展开/折叠状态
const toggleTopic = (unitIndex, topicIndex) => {
  const key = `${unitIndex}-${topicIndex}`;
  expandedTopics.value[key] = !expandedTopics.value[key];
  emit('toggle-topic', unitIndex, topicIndex, expandedTopics.value[key]);
};

// 选择节点
const selectNode = (nodeId) => {
  selectedNode.value = nodeId;
  emit('select-node', nodeId);
};

// 添加到教案
const addToLessonPlan = (item) => {
  emit('add-to-lesson-plan', item);
};
</script>

<template>
  <div class="outline-tree">
    <div class="tree-header">
      <h2>课程大纲</h2>
      <button 
        class="expand-toggle-btn"
        @click="$emit('update:expandAll', !expandAll)"
      >
        {{ expandAll ? '全部收起' : '全部展开' }}
      </button>
    </div>
    
    <div class="tree-container">
      <template v-if="outlineData?.outline?.length">
        <div v-for="(unit, unitIndex) in outlineData.outline" :key="`unit-${unitIndex}`" class="tree-unit">
          <div 
            class="tree-node unit-node"
            :class="{'selected-node': selectedNode === `unit-${unitIndex}`}"
            @click="toggleUnit(unitIndex); selectNode(`unit-${unitIndex}`)"
          >
            <div class="node-content">
              <div 
                class="expand-icon"
                :class="{'expanded': expandedUnits[unitIndex]}"
              >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                </svg>
              </div>
              <span class="node-title">{{ unit.title }}</span>
            </div>
            
            <button 
              class="add-to-plan-btn"
              @click.stop="addToLessonPlan(unit)"
              title="添加到教案"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
              </svg>
            </button>
          </div>
          
          <div v-if="expandedUnits[unitIndex] || expandAll" class="unit-children">
            <div 
              v-for="(topic, topicIndex) in unit.children" 
              :key="`unit-${unitIndex}-topic-${topicIndex}`" 
              class="tree-topic"
            >
              <div 
                class="tree-node topic-node"
                :class="{'selected-node': selectedNode === `unit-${unitIndex}-topic-${topicIndex}`}"
                @click="toggleTopic(unitIndex, topicIndex); selectNode(`unit-${unitIndex}-topic-${topicIndex}`)"
              >
                <div class="node-content">
                  <div 
                    class="expand-icon"
                    :class="{'expanded': expandedTopics[`${unitIndex}-${topicIndex}`]}"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                    </svg>
                  </div>
                  <span class="node-title">{{ topic.title }}</span>
                </div>
                
                <button 
                  class="add-to-plan-btn"
                  @click.stop="addToLessonPlan(topic)"
                  title="添加到教案"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
                  </svg>
                </button>
              </div>
              
              <div v-if="expandedTopics[`${unitIndex}-${topicIndex}`] || expandAll" class="topic-children">
                <div 
                  v-for="(subtopic, subtopicIndex) in topic.children" 
                  :key="`unit-${unitIndex}-topic-${topicIndex}-subtopic-${subtopicIndex}`" 
                  class="tree-node subtopic-node"
                  :class="{'selected-node': selectedNode === `unit-${unitIndex}-topic-${topicIndex}-subtopic-${subtopicIndex}`}"
                  @click="selectNode(`unit-${unitIndex}-topic-${topicIndex}-subtopic-${subtopicIndex}`)"
                >
                  <div class="node-content">
                    <div class="node-spacer"></div>
                    <span class="node-title">{{ subtopic.title }}</span>
                  </div>
                  
                  <button 
                    class="add-to-plan-btn"
                    @click.stop="addToLessonPlan(subtopic)"
                    title="添加到教案"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
      
      <div v-else class="empty-outline">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
        <p class="mt-2 text-gray-500">未找到大纲数据</p>
        <button 
          class="import-outline-btn mt-2"
          @click="$emit('import-outline')"
        >
          前往导入大纲
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.outline-tree {
  @apply bg-white rounded-lg shadow-sm h-full border border-gray-200 flex flex-col;
}

.tree-header {
  @apply flex justify-between items-center p-4 border-b border-gray-100;
}

.tree-header h2 {
  @apply text-lg font-medium text-gray-800;
}

.expand-toggle-btn {
  @apply text-sm text-blue-600 hover:text-blue-800 font-medium;
}

.tree-container {
  @apply flex-1 overflow-y-auto p-2 space-y-1;
  max-height: calc(100vh - 220px);
}

.tree-node {
  @apply flex items-center justify-between py-2 px-3 rounded-lg cursor-pointer hover:bg-gray-50 transition-colors duration-150;
}

.node-content {
  @apply flex items-center flex-1;
}

.expand-icon {
  @apply w-6 h-6 flex items-center justify-center text-gray-500 transition-transform duration-200;
}

.expand-icon.expanded {
  @apply rotate-90;
}

.node-title {
  @apply text-sm;
}

.add-to-plan-btn {
  @apply w-6 h-6 rounded-full flex items-center justify-center text-blue-500 hover:text-blue-700 hover:bg-blue-50 opacity-0 group-hover:opacity-100 transition-all duration-200;
}

.tree-node:hover .add-to-plan-btn {
  @apply opacity-100;
}

.unit-node {
  @apply font-medium;
}

.topic-node {
  @apply text-gray-700;
}

.subtopic-node {
  @apply text-gray-600 text-sm;
}

.unit-children {
  @apply pl-4 space-y-1 mt-1;
}

.topic-children {
  @apply pl-4 space-y-1 mt-1;
}

.selected-node {
  @apply bg-blue-50 border-l-4 border-blue-500 pl-2;
}

.node-spacer {
  @apply w-6;
}

.empty-outline {
  @apply flex flex-col items-center justify-center py-10 text-center;
}

.import-outline-btn {
  @apply text-sm text-blue-600 hover:text-blue-800 font-medium underline;
}
</style>