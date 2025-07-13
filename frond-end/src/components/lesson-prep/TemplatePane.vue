<template>
  <div class="flex flex-col h-full p-4 bg-white/95 dark:bg-slate-800/90 rounded-2xl shadow-lg border border-indigo-200 dark:border-indigo-700">
    <!-- 模板选择区域 -->
    <div class="flex items-center justify-between mb-3">
      <el-select v-model="selectedId" size="small" @change="apply" class="w-3/4">
        <el-option v-for="tpl in presets" :key="tpl.id" :label="tpl.name" :value="tpl.id" />
      </el-select>
      <el-tooltip content="保存为自定义模板" placement="top">
        <el-button size="small" type="primary" plain circle @click="saveTemplate">
          <el-icon><Plus /></el-icon>
        </el-button>
      </el-tooltip>
    </div>
    
    <!-- 模板说明区域 -->
    <div class="mb-3 p-2 bg-indigo-50 dark:bg-indigo-900/30 rounded text-xs text-slate-600 dark:text-slate-300">
      <p>{{ currentTemplate.description }}</p>
    </div>

    <!-- 可编辑 Markdown -->
    <div class="relative flex-1 flex flex-col">
      <div class="absolute right-2 top-2 z-10 flex space-x-1">
        <el-tooltip content="格式化" placement="top">
          <el-button size="small" circle @click="formatMarkdown">
            <el-icon><Operation /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip content="插入常用结构" placement="top">
          <el-dropdown trigger="click" @command="insertSnippet">
            <el-button size="small" circle>
              <el-icon><Document /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="heading">标题</el-dropdown-item>
                <el-dropdown-item command="list">列表</el-dropdown-item>
                <el-dropdown-item command="table">表格</el-dropdown-item>
                <el-dropdown-item command="quote">引用</el-dropdown-item>
                <el-dropdown-item command="code">代码块</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-tooltip>
      </div>
      <el-input 
        v-model="mdText" 
        type="textarea" 
        :rows="20" 
        class="flex-1 template-editor" 
        resize="none"
        spellcheck="false"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { marked } from 'marked';
import DOMPurify from 'dompurify';
import hljs from 'highlight.js';
import { bus } from '@/utils/eventBus';
import { Plus, Operation, Document } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

// 扩展预设模板
const presets = [
  { 
    id: 'standard', 
    name: '标准教学教案', 
    md: '# 标准教学教案\n\n## 教学目标\n- 知识目标：\n- 能力目标：\n- 情感目标：\n\n## 教学重点\n\n## 教学难点\n\n## 教学过程\n\n### 一、导入新课\n\n### 二、讲授新知\n\n### 三、巩固练习\n\n### 四、总结作业',
    description: '标准教学教案模板，包含教学目标、重难点和完整教学过程。'
  },
  { 
    id: 'pbl', 
    name: '项目式学习', 
    md: '# PBL 项目式学习教案\n\n## 项目背景\n> 项目背景描述\n\n## 学习目标\n\n## 项目任务\n- 任务一：\n- 任务二：\n\n## 实施过程\n1. 问题提出\n2. 方案设计\n3. 实施与调整\n4. 成果展示\n\n## 评价方式\n- 过程评价：\n- 结果评价：',
    description: '项目式学习教案，强调真实情境中的问题解决和成果产出。'
  },
  { 
    id: 'flipped', 
    name: '翻转课堂', 
    md: '# 翻转课堂教学设计\n\n## 课前准备\n- 学习资源：\n- 预习任务：\n\n## 课堂活动\n1. 知识检测（15分钟）\n2. 问题讨论（20分钟）\n3. 深度学习（30分钟）\n4. 成果展示（15分钟）\n\n## 课后延伸\n- 拓展资源：\n- 实践任务：',
    description: '翻转课堂模式，学生先自主学习，课堂专注于问题解决和互动。'
  },
  { 
    id: 'gamified', 
    name: '游戏化教学', 
    md: '# 游戏化教学设计\n\n## 游戏主题\n\n## 游戏规则\n\n## 关卡设计\n1. 第一关：\n   - 任务：\n   - 奖励：\n2. 第二关：\n   - 任务：\n   - 奖励：\n\n## 积分与奖励机制\n\n## 教学反思',
    description: '将教学内容游戏化，通过关卡、积分和奖励提高学习动机。'
  },
  { 
    id: 'inquiry', 
    name: '探究式教学', 
    md: '# 探究式教学设计\n\n## 探究问题\n\n## 探究假设\n\n## 实验/调查设计\n\n## 数据收集与分析\n\n## 结论与反思\n\n## 拓展探究',
    description: '基于科学探究过程的教学设计，培养学生的科学思维和研究能力。'
  },
  { 
    id: 'cooperative', 
    name: '合作学习', 
    md: '# 合作学习教学设计\n\n## 学习目标\n\n## 分组策略\n- 分组方式：\n- 角色分配：\n\n## 合作任务\n\n## 小组活动流程\n1. 任务分解\n2. 个人思考\n3. 小组讨论\n4. 成果整合\n5. 展示分享\n\n## 评价方式\n- 小组评价：\n- 个人评价：',
    description: '强调小组合作与互助的教学模式，培养沟通与协作能力。'
  },
];

const selectedId = ref(presets[0].id);
const mdText = ref(presets[0].md);
const html = ref('');

// 获取当前选中的模板信息
const currentTemplate = computed(() => {
  return presets.find(t => t.id === selectedId.value) || presets[0];
});

function render() {
  html.value = DOMPurify.sanitize(marked.parse(mdText.value));
  // 代码高亮
  setTimeout(() => {
    document.querySelectorAll('.markdown-body pre code').forEach(el => hljs.highlightElement(el));
  });
  bus.emit('template-update', mdText.value);
}

function apply() {
  const tpl = presets.find(t => t.id === selectedId.value);
  if (tpl) mdText.value = tpl.md;
  render();
}

// 格式化 Markdown
function formatMarkdown() {
  try {
    // 简单格式化：确保标题后有空行，列表项对齐等
    const formatted = mdText.value
      .replace(/^(#{1,6} .+)(?!\n\n)/gm, '$1\n\n')  // 标题后加空行
      .replace(/^( *)[-*+] /gm, '$1- ')             // 统一列表符号为 -
      .replace(/\n{3,}/g, '\n\n');                  // 最多保留两个连续空行
    
    mdText.value = formatted;
    ElMessage.success('格式化完成');
  } catch (err) {
    ElMessage.error('格式化失败');
  }
}

// 插入常用 Markdown 片段
function insertSnippet(type) {
  const snippets = {
    heading: '## 标题\n\n',
    list: '- 项目一\n- 项目二\n- 项目三\n\n',
    table: '| 表头1 | 表头2 | 表头3 |\n| ----- | ----- | ----- |\n| 内容1 | 内容2 | 内容3 |\n| 内容4 | 内容5 | 内容6 |\n\n',
    quote: '> 引用内容\n> 多行引用\n\n',
    code: '```javascript\n// 代码示例\nfunction example() {\n  console.log("Hello");\n}\n```\n\n'
  };
  
  const snippet = snippets[type] || '';
  
  // 获取当前光标位置
  const textarea = document.querySelector('.template-editor textarea');
  if (textarea) {
    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;
    
    // 在光标位置插入片段
    mdText.value = mdText.value.substring(0, start) + snippet + mdText.value.substring(end);
    
    // 插入后将光标移到插入内容之后
    setTimeout(() => {
      textarea.focus();
      textarea.selectionStart = textarea.selectionEnd = start + snippet.length;
    }, 0);
  } else {
    // 如果无法获取光标位置，则追加到末尾
    mdText.value += snippet;
  }
  
  render();
}

// 保存为自定义模板
function saveTemplate() {
  // 这里可以实现保存自定义模板的逻辑
  // 例如打开对话框让用户输入模板名称，然后保存到localStorage或发送到后端
  ElMessage.success('模板保存功能将在后续版本中提供');
}

render();

// 监听 textarea input
watch(mdText, (v)=>{
  bus.emit('template-update', v);
});
</script>

<style scoped>
/* 编辑器样式优化 */
.template-editor :deep(.el-textarea__inner) {
  font-family: 'JetBrains Mono', monospace;
  font-size: 0.9rem;
  line-height: 1.5;
  padding: 12px;
}

/* 工具按钮悬浮效果 */
.el-button.is-circle {
  transition: all 0.2s;
}
.el-button.is-circle:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
</style> 