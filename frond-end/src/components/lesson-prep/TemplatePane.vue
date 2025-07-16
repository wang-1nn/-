<template>
  <div class="flex flex-col h-full p-4 bg-white/95 rounded-2xl shadow-lg border border-indigo-200">
    <!-- 模板选择区域 -->
    <div class="flex items-center justify-between mb-3">
      <el-select v-model="selectedId" size="small" @change="apply" class="w-3/4">
        <el-option v-for="tpl in presets" :key="tpl.id" :label="tpl.name" :value="tpl.id" />
      </el-select>
      <div class="flex gap-1">
        <el-tooltip content="保存为自定义模板" placement="top">
          <el-button size="small" type="primary" plain circle @click="saveTemplate">
            <el-icon><Plus /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip content="AI优化模板" placement="top">
          <el-button size="small" type="success" plain circle @click="enhanceWithAI">
            <el-icon><MagicStick /></el-icon>
          </el-button>
        </el-tooltip>
      </div>
    </div>

    <!-- 模板说明区域 -->
    <div class="mb-3 p-3 bg-indigo-50 rounded-lg text-sm text-slate-700">
      <p>{{ currentTemplate.description }}</p>
    </div>

    <!-- 可编辑 Markdown 和预览切换 -->
    <div class="flex items-center mb-3 border-b pb-2">
      <span
          :class="[
          'px-4 py-2 text-sm cursor-pointer rounded-t transition-all',
          editorMode === 'edit' ? 'text-indigo-600 font-medium border-b-2 border-indigo-500' : 'text-slate-500 hover:text-indigo-500'
        ]"
          @click="editorMode = 'edit'"
      >
        <el-icon class="mr-1"><EditPen /></el-icon> 编辑
      </span>
      <span
          :class="[
          'px-4 py-2 text-sm cursor-pointer rounded-t transition-all',
          editorMode === 'preview' ? 'text-indigo-600 font-medium border-b-2 border-indigo-500' : 'text-slate-500 hover:text-indigo-500'
        ]"
          @click="editorMode = 'preview'"
      >
        <el-icon class="mr-1"><View /></el-icon> 预览
      </span>
      <span
          :class="[
          'px-4 py-2 text-sm cursor-pointer rounded-t transition-all',
          editorMode === 'split' ? 'text-indigo-600 font-medium border-b-2 border-indigo-500' : 'text-slate-500 hover:text-indigo-500'
        ]"
          @click="editorMode = 'split'"
      >
        <el-icon class="mr-1"><SwitchButton /></el-icon> 分屏
      </span>
    </div>

    <!-- 编辑器/预览区域 -->
    <div class="relative flex-1 flex flex-col overflow-hidden">
      <!-- 编辑器工具栏 -->
      <div v-if="editorMode !== 'preview'" class="absolute right-2 top-2 z-10 flex space-x-1">
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
                <el-dropdown-item divided command="objectives">教学目标</el-dropdown-item>
                <el-dropdown-item command="keypoints">教学重点难点</el-dropdown-item>
                <el-dropdown-item command="materials">教学资源</el-dropdown-item>
                <el-dropdown-item command="activities">教学活动</el-dropdown-item>
                <el-dropdown-item command="assessment">评价方式</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-tooltip>
      </div>

      <!-- 编辑器/预览布局 -->
      <div class="flex-1 flex overflow-hidden" style="min-height: 0">
        <!-- 编辑模式 -->
        <el-input
            v-if="editorMode !== 'preview'"
            v-model="mdText"
            type="textarea"
            :rows="20"
            class="flex-1 template-editor"
            resize="none"
            spellcheck="false"
            :style="{
            flex: editorMode === 'split' ? '1' : '1 1 100%',
            fontSize: '14px',
            lineHeight: '1.6',
            fontFamily: 'Consolas, Monaco, monospace'
          }"
        />

        <!-- 预览模式 -->
        <div
            v-if="editorMode !== 'edit'"
            class="markdown-preview bg-white p-4 overflow-auto rounded-lg border border-slate-200"
            :style="{
            flex: editorMode === 'split' ? '1' : '1 1 100%',
            maxHeight: '100%',
            fontSize: '14px',
            lineHeight: '1.7'
          }"
            v-html="html"
        ></div>
      </div>
    </div>

    <!-- AI 辅助功能弹窗 -->
    <el-dialog v-model="showAIDialog" title="AI 辅助模板优化" width="500px">
      <el-form label-position="top">
        <el-form-item label="优化需求">
          <el-input
              v-model="aiPrompt"
              type="textarea"
              :rows="3"
              placeholder="请描述您想要的模板优化需求，例如：'增加更多互动环节'或'优化教学评价部分'"
          ></el-input>
        </el-form-item>
        <el-form-item label="优化侧重点">
          <el-checkbox-group v-model="aiOptions">
            <el-checkbox label="structure">优化结构</el-checkbox>
            <el-checkbox label="activities">增加教学活动</el-checkbox>
            <el-checkbox label="resources">丰富教学资源</el-checkbox>
            <el-checkbox label="assessment">完善评价方式</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <div v-if="isAIProcessing" class="text-center p-4">
        <el-progress type="circle" :percentage="aiProgress"></el-progress>
        <p class="mt-2 text-sm text-slate-500">AI正在优化模板...</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAIDialog = false">取消</el-button>
          <el-button type="primary" :loading="isAIProcessing" @click="generateAITemplate">
            开始优化
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 保存模板弹窗 -->
    <el-dialog v-model="showSaveDialog" title="保存自定义模板" width="500px">
      <el-form label-position="top">
        <el-form-item label="模板名称">
          <el-input v-model="saveTemplateForm.name" placeholder="输入模板名称"></el-input>
        </el-form-item>
        <el-form-item label="模板描述">
          <el-input
              v-model="saveTemplateForm.description"
              type="textarea"
              :rows="3"
              placeholder="输入模板描述"
          ></el-input>
        </el-form-item>
        <el-form-item label="模板类别">
          <el-select v-model="saveTemplateForm.category" class="w-full">
            <el-option label="基础教案" value="basic"></el-option>
            <el-option label="项目式学习" value="pbl"></el-option>
            <el-option label="翻转课堂" value="flipped"></el-option>
            <el-option label="游戏化教学" value="gamified"></el-option>
            <el-option label="探究式教学" value="inquiry"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showSaveDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmSaveTemplate">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { marked } from 'marked';
import DOMPurify from 'dompurify';
import hljs from 'highlight.js';
import { bus } from '@/utils/eventBus';
import { Plus, Operation, Document, EditPen, View, SwitchButton, MagicStick } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

// --- onMounted/onUnmounted in Vue 3 setup ---
import { onMounted, onUnmounted } from 'vue';

onMounted(() => {
  bus.on('insert-text', handleInsertText);
});

onUnmounted(() => {
  bus.off('insert-text', handleInsertText);
});

const handleInsertText = (text) => {
  insertSnippet(null, text);
};


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
  {
    id: 'differentiated',
    name: '分层教学',
    md: '# 分层教学设计\n\n## 学情分析\n- A组（优秀水平）：\n- B组（中等水平）：\n- C组（需要帮助）：\n\n## 共同教学目标\n\n## 分层教学目标\n- A组目标：\n- B组目标：\n- C组目标：\n\n## 分层教学活动\n### 基础活动（全体参与）\n\n### A组拓展活动\n\n### B组巩固活动\n\n### C组辅助活动\n\n## 分层评价',
    description: '根据学生不同水平和需求设计的差异化教学方案。'
  },
  {
    id: 'interdisciplinary',
    name: '跨学科整合',
    md: '# 跨学科整合教学设计\n\n## 核心议题\n\n## 学科整合点\n- 学科一：\n- 学科二：\n- 学科三：\n\n## 整合目标\n\n## 教学资源\n\n## 学习活动\n\n## 综合评价',
    description: '打破学科界限，融合多学科知识和方法的综合教学设计。'
  },
];

const selectedId = ref(presets[0].id);
const mdText = ref(presets[0].md);
const html = ref('');
const editorMode = ref('edit'); // 'edit', 'preview', 'split'

// AI辅助相关状态
const showAIDialog = ref(false);
const aiPrompt = ref('');
const aiOptions = ref(['structure', 'activities']);
const isAIProcessing = ref(false);
const aiProgress = ref(0);

// 保存模板相关状态
const showSaveDialog = ref(false);
const saveTemplateForm = ref({
  name: '',
  description: '',
  category: 'basic'
});

// 获取当前选中的模板信息
const currentTemplate = computed(() => {
  return presets.find(t => t.id === selectedId.value) || presets[0];
});

function render() {
  html.value = DOMPurify.sanitize(marked.parse(mdText.value, { gfm: true }));
  // 代码高亮
  setTimeout(() => {
    document.querySelectorAll('.markdown-preview pre code').forEach(el => hljs.highlightElement(el));
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
function insertSnippet(type, customText = '') {
  const snippets = {
    heading: '## 标题\\n\\n',
    list: '- 项目一\\n- 项目二\\n- 项目三\\n\\n',
    table: '| 表头1 | 表头2 | 表头3 |\\n| ----- | ----- | ----- |\\n| 内容1 | 内容2 | 内容3 |\\n| 内容4 | 内容5 | 内容6 |\\n\\n',
    quote: '> 引用内容\\n> 多行引用\\n\\n',
    code: '```javascript\\n// 代码示例\\nfunction example() {\\n  console.log("Hello");\\n}\\n```\\n\\n',
    objectives: '## 教学目标\\n\\n### 知识目标\\n- 理解和掌握核心概念\\n- 熟悉基本原理\\n\\n### 能力目标\\n- 培养分析问题和解决问题的能力\\n- 提升实践操作技能\\n\\n### 情感目标\\n- 培养学习兴趣和积极态度\\n- 形成正确的价值观\\n\\n',
    keypoints: '## 教学重难点\\n\\n### 重点\\n1. 核心概念的理解\\n2. 基本原理的应用\\n\\n### 难点\\n1. 复杂问题的分析\\n2. 实际情境中的应用\\n\\n',
    materials: '## 教学资源\\n\\n### 教材资源\\n- 教科书第X章第X节\\n- 补充阅读材料\\n\\n### 数字资源\\n- 教学视频\\n- 互动课件\\n- 在线测验\\n\\n### 实物资源\\n- 实验器材\\n- 模型\\n\\n',
    activities: '## 教学活动\\n\\n### 引入活动（10分钟）\\n- 情境导入\\n- 提问引导\\n\\n### 探究活动（20分钟）\\n- 小组讨论\\n- 案例分析\\n\\n### 实践活动（15分钟）\\n- 操作演示\\n- 学生实践\\n\\n### 总结活动（5分钟）\\n- 知识梳理\\n- 学生反馈\\n\\n',
    assessment: '## 评价方式\\n\\n### 形成性评价\\n- 课堂表现：30%\\n- 作业完成：20%\\n- 小组合作：20%\\n\\n### 终结性评价\\n- 期末测试：30%\\n\\n### 评价标准\\n1. 优秀：熟练掌握所有知识点，能创新应用\\n2. 良好：掌握大部分知识点，能正确应用\\n3. 合格：基本掌握知识点，能简单应用\\n4. 待改进：知识点掌握不全，应用有困难\\n\\n'
  };

  const snippet = customText || (snippets[type] || '');

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

// 打开AI辅助对话框
function enhanceWithAI() {
  aiPrompt.value = '';
  aiOptions.value = ['structure', 'activities'];
  showAIDialog.value = true;
}

// 生成AI辅助模板
function generateAITemplate() {
  if (!aiPrompt.value) {
    ElMessage.warning('请输入优化需求');
    return;
  }

  isAIProcessing.value = true;
  aiProgress.value = 0;

  // 模拟AI处理进度
  const progressInterval = setInterval(() => {
    aiProgress.value += 10;
    if (aiProgress.value >= 100) {
      clearInterval(progressInterval);
      aiProgress.value = 100;

      // 模拟AI增强模板
      setTimeout(() => {
        // 根据不同的优化选项，增强模板内容
        let enhancedContent = mdText.value;

        // 基于当前模板内容和用户需求，模拟AI增强
        if (aiOptions.value.includes('structure')) {
          enhancedContent = enhanceStructure(enhancedContent);
        }

        if (aiOptions.value.includes('activities')) {
          enhancedContent = enhanceActivities(enhancedContent);
        }

        if (aiOptions.value.includes('resources')) {
          enhancedContent = enhanceResources(enhancedContent);
        }

        if (aiOptions.value.includes('assessment')) {
          enhancedContent = enhanceAssessment(enhancedContent);
        }

        mdText.value = enhancedContent;
        render();
        isAIProcessing.value = false;
        showAIDialog.value = false;
        ElMessage.success('模板优化完成');
      }, 500);
    }
  }, 100);
}

// 保存模板
function saveTemplate() {
  saveTemplateForm.value = {
    name: '自定义模板',
    description: '自定义教学模板',
    category: 'basic'
  };
  showSaveDialog.value = true;
}

// 确认保存模板
function confirmSaveTemplate() {
  // 创建新的自定义模板
  const newTemplate = {
    id: `custom-${Date.now()}`,
    name: saveTemplateForm.value.name,
    md: mdText.value,
    description: saveTemplateForm.value.description
  };

  // 添加到预设模板列表
  presets.push(newTemplate);

  // 选中新模板
  selectedId.value = newTemplate.id;

  // 关闭对话框
  showSaveDialog.value = false;
  ElMessage.success('模板保存成功');
}

// 结构增强模板函数
function enhanceStructure(content) {
  // 这里是模拟AI增强结构的逻辑
  if (!content.includes('## 教学环境')) {
    content += '\n\n## 教学环境\n\n### 物理环境\n- 标准教室布置\n- 多媒体设备需求\n\n### 数字环境\n- 网络要求\n- 软件平台\n\n';
  }

  if (!content.includes('## 教学准备')) {
    content += '\n\n## 教学准备\n\n### 教师准备\n- 教学内容研究\n- 教学资源准备\n\n### 学生准备\n- 预习要求\n- 准备材料\n\n';
  }

  return content;
}

// 活动增强模板函数
function enhanceActivities(content) {
  // 这里是模拟AI增强教学活动的逻辑
  if (!content.includes('## 互动环节设计')) {
    content += '\n\n## 互动环节设计\n\n### 头脑风暴\n- 活动目的：激发创意思维\n- 实施方法：限时自由发散思考\n- 预期效果：收集多样化想法\n\n### 辩论讨论\n- 活动目的：深化理解，培养批判思维\n- 实施方法：分组正反方辩论\n- 预期效果：多角度思考问题\n\n### 角色扮演\n- 活动目的：情境化学习\n- 实施方法：模拟真实场景\n- 预期效果：提升应用能力\n\n';
  }

  return content;
}

// 资源增强模板函数
function enhanceResources(content) {
  // 这里是模拟AI增强教学资源的逻辑
  if (!content.includes('## 多媒体资源')) {
    content += '\n\n## 多媒体资源\n\n### 视频资源\n- 教学视频：《核心概念讲解》\n- 案例视频：《实际应用案例》\n\n### 图片资源\n- 知识点思维导图\n- 流程示意图\n\n### 音频资源\n- 情境音效\n- 背景音乐\n\n### 交互式资源\n- 在线测验\n- 互动游戏\n\n';
  }

  return content;
}

// 评价增强模板函数
function enhanceAssessment(content) {
  // 这里是模拟AI增强评价方式的逻辑
  if (!content.includes('## 多元评价')) {
    content += '\n\n## 多元评价\n\n### 表现性评价\n- 作品展示\n- 实践操作\n- 口头汇报\n\n### 档案袋评价\n- 学习过程记录\n- 阶段性成果\n- 反思日志\n\n### 同伴互评\n- 评价表设计\n- 互评流程\n- 互评标准\n\n### 自我评价\n- 自评量表\n- 学习反思\n- 目标设定\n\n';
  }

  return content;
}

render();

// 监听 textarea input
watch(mdText, (v)=>{
  bus.emit('template-update', v);
  // 当编辑器内容变化时，如果当前是预览或分屏模式，需要重新渲染
  if (editorMode.value !== 'edit') {
    render();
  }
});

// 监听编辑器模式变化
watch(editorMode, (mode) => {
  if (mode !== 'edit') {
    render();
  }
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

/* 预览区样式 */
.markdown-preview {
  font-size: 0.9rem;
  line-height: 1.6;
}

.markdown-preview :deep(h1) {
  font-size: 1.8rem;
  color: #4f46e5;
  margin-bottom: 1rem;
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 0.5rem;
}

.markdown-preview :deep(h2) {
  font-size: 1.4rem;
  color: #4338ca;
  margin-top: 1.5rem;
  margin-bottom: 0.75rem;
}

.markdown-preview :deep(h3) {
  font-size: 1.2rem;
  color: #6366f1;
  margin-top: 1.25rem;
  margin-bottom: 0.5rem;
}

.markdown-preview :deep(ul), .markdown-preview :deep(ol) {
  padding-left: 1.5rem;
  margin-bottom: 1rem;
}

.markdown-preview :deep(li) {
  margin-bottom: 0.25rem;
}

.markdown-preview :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 1rem;
}

.markdown-preview :deep(th), .markdown-preview :deep(td) {
  border: 1px solid #e5e7eb;
  padding: 0.5rem;
}

.markdown-preview :deep(th) {
  background-color: #f3f4f6;
}

.markdown-preview :deep(blockquote) {
  border-left: 4px solid #818cf8;
  padding-left: 1rem;
  color: #4b5563;
  margin: 1rem 0;
}

.markdown-preview :deep(pre) {
  background-color: #f3f4f6;
  padding: 1rem;
  border-radius: 0.25rem;
  overflow-x: auto;
}

.markdown-preview :deep(code) {
  font-family: 'JetBrains Mono', monospace;
}
</style> 