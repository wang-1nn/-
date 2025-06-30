<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { post } from '@/net';
import * as monaco from 'monaco-editor';

// 编辑器实例引用
const monacoEditor = ref(null);
const editorInstance = ref(null);
const editorContainer = ref(null);
const resultContainer = ref(null);

// 课程大纲树
const syllabusTree = ref([]);

// 流式生成状态
const streamingStatus = ref({
  isStreaming: false,
  progress: 0,
  totalTokens: 0,
  elapsedTime: 0,
  estimatedTime: 0
});

// 大纲编辑显示状态
const showSyllabusEditor = ref(false);

// 编辑器内容
const editorContent = ref(`---
title: "高级编程技术教案"
subject: "计算机科学与技术"
grade: "大学二年级"
duration: "90分钟"
created_by: "AI助手"
bloom_levels: ["记忆", "理解", "应用", "分析"]
keywords: ["算法", "数据结构", "程序设计"]
---

# 教学目标

1. 掌握基本算法设计方法
2. 理解时间复杂度与空间复杂度的概念
3. 能够分析算法性能并进行优化
4. 应用所学知识解决实际编程问题

# 教学重点

- 常见算法设计策略
- 算法复杂度分析方法
- 数据结构选择对算法效率的影响

# 教学难点

- 复杂算法的设计与实现
- 算法优化技巧掌握
- 理解递归算法的执行流程

# 教学准备

- 多媒体教室
- 示例代码库
- 在线编程平台账号
- 算法可视化工具

# 教学过程

## 导入环节 (10分钟)

1. 复习上节课内容
2. 引入本节课程主题：高效算法设计
3. 展示一个实际问题，引发学生思考

## 新课讲解 (40分钟)

1. 详细讲解分治算法设计方法
   - 原理与适用场景
   - 经典案例分析：归并排序
   - 代码实现与讲解

2. 动态规划基础
   - 问题特征识别
   - 状态转移方程构建
   - 经典案例：背包问题

## 实践环节 (30分钟)

1. 学生分组完成算法设计任务
2. 教师巡回指导
3. 小组间交流与点评

## 总结评价 (10分钟)

1. 知识点回顾
2. 布置课后作业
3. 预告下节课内容

# 板书设计

[此处可插入板书内容]

# 课后作业

1. 完成5道算法设计题目
2. 阅读相关论文一篇并提交心得
3. 优化课堂实践中的算法`);

// 生成结果
const generatedContent = ref('');

// 选项卡
const activeTab = ref('prompt');

// 生成中状态
const generating = ref(false);

// 大纲编辑
const syllabusRawContent = ref(`# 高级编程技术课程大纲

## 第一章：算法基础
- 1.1 算法概念与特性
- 1.2 时间复杂度与空间复杂度
- 1.3 算法分析方法
- 1.4 递归算法

## 第二章：基本算法设计方法
- 2.1 分治法
- 2.2 动态规划
- 2.3 贪心算法
- 2.4 回溯法

## 第三章：高级数据结构
- 3.1 平衡树
- 3.2 堆与优先队列
- 3.3 图的表示与遍历
- 3.4 并查集

## 第四章：算法优化技术
- 4.1 空间-时间权衡
- 4.2 算法剪枝技术
- 4.3 启发式算法
- 4.4 并行算法设计
`);

// 教案模板选项
const templateOptions = ref([
  { label: '标准教案模板', value: 'standard' },
  { label: '项目式学习模板', value: 'pbl' },
  { label: '翻转课堂模板', value: 'flipped' },
  { label: 'STEM整合教学模板', value: 'stem' }
]);
const selectedTemplate = ref('standard');

// 初始化编辑器
onMounted(() => {
  if (editorContainer.value) {
    editorInstance.value = monaco.editor.create(editorContainer.value, {
      value: editorContent.value,
      language: 'markdown',
      theme: 'vs',
      automaticLayout: true,
      minimap: { enabled: true },
      lineNumbers: 'on',
      wordWrap: 'on',
      fontSize: 14
    });

    // 监听编辑器内容变化
    editorInstance.value.onDidChangeModelContent(() => {
      editorContent.value = editorInstance.value.getValue();
    });
  }

  // 解析大纲为树形结构
  parseSyllabusToTree();
});

// 解析大纲文本为树形结构
const parseSyllabusToTree = () => {
  const lines = syllabusRawContent.value.split('\n').filter(line => line.trim());
  
  const tree = [];
  let currentChapter = null;
  
  lines.forEach(line => {
    if (line.startsWith('# ')) {
      // 这是课程标题，忽略
    } else if (line.startsWith('## ')) {
      // 这是章节标题
      currentChapter = {
        id: tree.length + 1,
        label: line.substring(3).trim(),
        children: []
      };
      tree.push(currentChapter);
    } else if (line.startsWith('- ') && currentChapter) {
      // 这是小节
      const sectionText = line.substring(2).trim();
      // 检查是否有编号（如 1.1）
      const match = sectionText.match(/^(\d+\.\d+)\s+(.+)$/);
      
      if (match) {
        currentChapter.children.push({
          id: `${currentChapter.id}-${currentChapter.children.length + 1}`,
          label: `${match[1]} ${match[2]}`,
          content: sectionText
        });
      } else {
        currentChapter.children.push({
          id: `${currentChapter.id}-${currentChapter.children.length + 1}`,
          label: sectionText,
          content: sectionText
        });
      }
    }
  });
  
  syllabusTree.value = tree;
};

// 保存大纲编辑
const saveSyllabusEdit = () => {
  parseSyllabusToTree();
  showSyllabusEditor.value = false;
  ElMessage.success('大纲已更新');
};

// 开始生成教案
const startGeneration = async () => {
  if (generating.value) return;
  
  generating.value = true;
  streamingStatus.value.isStreaming = true;
  streamingStatus.value.progress = 0;
  streamingStatus.value.estimatedTime = 60; // 预计60秒完成
  streamingStatus.value.elapsedTime = 0;
  generatedContent.value = '';
  
  try {
    // 这里模拟SSE流式响应，实际项目中应使用EventSource或WebSocket
    const promptContent = editorInstance.value.getValue();
    const selectedChapters = getSelectedNodes();
    
    // 模拟流式生成
    let progress = 0;
    let startTime = Date.now();
    
    const timer = setInterval(() => {
      progress += Math.random() * 2;
      if (progress > 100) progress = 100;
      
      streamingStatus.value.progress = progress;
      streamingStatus.value.elapsedTime = Math.floor((Date.now() - startTime) / 1000);
      
      // 模拟增量内容生成
      generatedContent.value += generateIncrementalContent();
      resultContainer.value.scrollTop = resultContainer.value.scrollHeight;
      
      if (progress >= 100) {
        clearInterval(timer);
        generating.value = false;
        streamingStatus.value.isStreaming = false;
        ElMessage.success('教案生成完成！');
      }
    }, 500);
    
    // 这里应该是实际API调用:
    /*
    const response = await post('/api/teacher/generate-lesson-plan', {
      prompt: promptContent,
      template: selectedTemplate.value,
      chapters: selectedChapters
    }, null, null, null, true);
    */
    
  } catch (error) {
    console.error('生成失败:', error);
    ElMessage.error('生成失败，请稍后重试');
    generating.value = false;
    streamingStatus.value.isStreaming = false;
  }
};

// 模拟内容增量生成（实际项目中会由SSE流提供）
const generateIncrementalContent = () => {
  const templates = [
    '正在分析课程大纲内容...\n',
    '提取教学重点与难点...\n',
    '设计教学活动流程...\n',
    '## 教学设计\n\n',
    '本节课采用项目驱动教学法，引导学生通过实际问题掌握算法设计技巧。\n\n',
    '### 教学重点\n\n',
    '- 分治算法的应用场景\n',
    '- 复杂度分析方法\n',
    '- 算法优化技巧\n\n',
    '### 教学方法\n\n',
    '采用"讲授-演示-实践-反馈"的教学模式，注重学生动手能力培养。\n\n',
    '## 教学过程\n\n',
    '### 1. 导入环节（10分钟）\n\n',
    '通过一个实际问题导入：如何在海量数据中快速查找特定元素？\n',
    '引导学生思考算法效率的重要性。\n\n',
    '### 2. 新课讲解（30分钟）\n\n',
    '详细讲解分治算法的基本思想与应用，以归并排序为例进行讲解。\n',
    '示范代码分析与复杂度计算。\n\n',
    '### 3. 学生实践（40分钟）\n\n',
    '学生分组完成算法设计与编码任务，教师巡回指导。\n\n',
    '### 4. 总结评价（10分钟）\n\n',
    '小组展示成果，教师点评，总结本节课知识要点。\n\n',
    '## 板书设计\n\n',
    '```\n分治法基本步骤：\n1. 分解 - 将原问题分解为子问题\n2. 解决 - 递归解决子问题\n3. 合并 - 将子问题的解合并为原问题的解\n```\n\n',
    '## 教学反思\n\n',
    '本课程需注意控制难度梯度，确保学生能够循序渐进地掌握算法设计方法。对于递归概念，应多准备可视化工具辅助学生理解。\n\n'
  ];
  
  const randomIndex = Math.floor(Math.random() * templates.length);
  return templates[randomIndex];
};

// 获取当前选中的章节
const getSelectedNodes = () => {
  // 实际应用中，这里应该返回树组件中选中的节点
  return ['2.1', '2.2'];
};

// 下载教案
const downloadLessonPlan = () => {
  const combinedContent = `# 教案设计文档\n\n${editorContent.value}\n\n# 生成结果\n\n${generatedContent.value}`;
  
  const blob = new Blob([combinedContent], { type: 'text/markdown' });
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = '教案设计.md';
  link.click();
  URL.revokeObjectURL(link.href);
  
  ElMessage.success('教案已下载');
};

// 复制到剪贴板
const copyToClipboard = (content) => {
  navigator.clipboard.writeText(content).then(() => {
    ElMessage.success('内容已复制到剪贴板');
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制');
  });
};
</script>

<template>
  <div class="lesson-plan-generator">
    <el-row :gutter="16" class="full-height">
      <!-- 左侧大纲区域 -->
      <el-col :span="5" class="full-height">
        <div class="panel outline-panel">
          <div class="panel-header">
            <h3 class="panel-title">课程大纲</h3>
            <div class="panel-actions">
              <el-button 
                type="primary" 
                size="small" 
                @click="showSyllabusEditor = true"
              >
                编辑大纲
              </el-button>
            </div>
          </div>
          
          <div class="panel-content">
            <el-tree
              :data="syllabusTree"
              show-checkbox
              node-key="id"
              default-expand-all
              highlight-current
              :props="{ label: 'label', children: 'children' }"
            >
              <template #default="{ data }">
                <div class="syllabus-node">
                  <span>{{ data.label }}</span>
                  <span class="node-actions" v-if="!data.children">
                    <el-button 
                      type="text" 
                      :icon="'el-icon-plus'"
                      @click.stop="() => {
                        const textContent = `# ${data.content}\n\n请基于此知识点生成详细教案`;
                        if (editorInstance.value) {
                          editorInstance.value.setValue(textContent);
                        }
                      }"
                    ></el-button>
                  </span>
                </div>
              </template>
            </el-tree>
          </div>
        </div>
      </el-col>
      
      <!-- 中间编辑区域 -->
      <el-col :span="9" class="full-height">
        <div class="panel editor-panel">
          <div class="panel-header">
            <h3 class="panel-title">教案编辑</h3>
            <div class="panel-actions">
              <el-select v-model="selectedTemplate" placeholder="选择模板" size="small" style="width: 150px">
                <el-option
                  v-for="item in templateOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
              <el-button 
                type="primary" 
                size="small" 
                :loading="generating"
                @click="startGeneration"
              >
                生成教案
              </el-button>
            </div>
          </div>
          
          <div class="panel-content">
            <el-tabs v-model="activeTab" class="editor-tabs">
              <el-tab-pane label="编写提示词" name="prompt">
                <div ref="editorContainer" class="monaco-editor"></div>
              </el-tab-pane>
              <el-tab-pane label="历史记录" name="history">
                <div class="history-list">
                  <el-empty description="暂无历史记录" />
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>
        </div>
      </el-col>
      
      <!-- 右侧生成结果 -->
      <el-col :span="10" class="full-height">
        <div class="panel result-panel">
          <div class="panel-header">
            <h3 class="panel-title">生成结果</h3>
            <div class="panel-actions">
              <el-button 
                type="text" 
                :icon="'el-icon-document-copy'" 
                @click="copyToClipboard(generatedContent)"
              >
                复制
              </el-button>
              <el-button 
                type="text" 
                :icon="'el-icon-download'" 
                @click="downloadLessonPlan"
              >
                下载
              </el-button>
            </div>
          </div>
          
          <div class="panel-content">
            <!-- 生成进度 -->
            <div v-if="streamingStatus.isStreaming" class="streaming-progress">
              <el-progress 
                :percentage="streamingStatus.progress" 
                :format="() => `${Math.floor(streamingStatus.progress)}%`"
                status="success"
              />
              <div class="streaming-info">
                <span>已用时间: {{ streamingStatus.elapsedTime }}s</span>
                <span>预计剩余: {{ Math.max(0, streamingStatus.estimatedTime - streamingStatus.elapsedTime) }}s</span>
              </div>
            </div>
            
            <!-- 生成结果 -->
            <div ref="resultContainer" class="result-content">
              <div v-if="generatedContent" class="markdown-preview">
                <!-- 实际项目中可以使用markdown-it库渲染markdown -->
                <pre>{{ generatedContent }}</pre>
              </div>
              <el-empty v-else description="生成的内容将显示在这里" />
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 大纲编辑对话框 -->
    <el-dialog
      v-model="showSyllabusEditor"
      title="编辑课程大纲"
      width="60%"
    >
      <el-form>
        <el-form-item>
          <el-input
            v-model="syllabusRawContent"
            type="textarea"
            :rows="20"
            placeholder="请输入课程大纲，使用Markdown格式"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showSyllabusEditor = false">取消</el-button>
          <el-button type="primary" @click="saveSyllabusEdit">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.lesson-plan-generator {
  height: calc(100vh - 120px); /* 减去header和tabs的高度 */
}

.full-height {
  height: 100%;
}

.panel {
  @apply bg-white rounded-md shadow-sm flex flex-col h-full;
}

.panel-header {
  @apply p-4 flex items-center justify-between border-b border-gray-200;
}

.panel-title {
  @apply text-base font-medium text-gray-700 m-0;
}

.panel-actions {
  @apply flex items-center gap-2;
}

.panel-content {
  @apply p-4 flex-1 overflow-auto;
}

/* 大纲面板 */
.outline-panel {
  @apply overflow-hidden;
}

.syllabus-node {
  @apply flex items-center justify-between w-full;
}

.node-actions {
  @apply opacity-0 transition-opacity;
}

.syllabus-node:hover .node-actions {
  @apply opacity-100;
}

/* 编辑器面板 */
.editor-panel {
  @apply overflow-hidden;
}

.monaco-editor {
  height: 100%;
  min-height: calc(100vh - 240px);
}

.editor-tabs {
  height: 100%;
}

.el-tabs__content {
  height: calc(100% - 40px);
  overflow: hidden;
}

/* 结果面板 */
.result-panel {
  @apply overflow-hidden;
}

.streaming-progress {
  @apply mb-4;
}

.streaming-info {
  @apply flex justify-between text-xs text-gray-500 mt-1;
}

.result-content {
  height: calc(100% - 20px);
  overflow: auto;
}

.markdown-preview {
  @apply text-sm whitespace-pre-wrap;
}
</style> 