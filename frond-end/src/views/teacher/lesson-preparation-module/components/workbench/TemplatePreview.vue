<template>
  <div class="template-preview">
    <!-- 模板信息头部 -->
    <div class="preview-header">
      <div class="flex items-center space-x-3">
        <div class="template-icon">
          <el-icon class="text-2xl" :class="getTemplateIconClass(template.style)">
            <component :is="getTemplateIcon(template.style)" />
          </el-icon>
        </div>
        <div>
          <h3 class="template-name">{{ template.name }}</h3>
          <p class="template-description">{{ template.description }}</p>
        </div>
      </div>

      <el-tag :type="getTemplateTagType(template.style)" effect="light">
        {{ getTemplateStyleName(template.style) }}
      </el-tag>
    </div>

    <!-- 模板结构预览 -->
    <div class="preview-content">
      <h4 class="section-title">模板结构预览</h4>

      <div class="template-structure">
        <div v-if="template.style === 'interactive'" class="structure-content">
          <div class="structure-item">
            <h5>📚 教学目标</h5>
            <p>明确知识、能力、情感三维目标</p>
          </div>
          <div class="structure-item">
            <h5>🎯 教学重难点</h5>
            <p>突出重点，突破难点</p>
          </div>
          <div class="structure-item">
            <h5>💬 互动导入</h5>
            <p>问题导入，激发学生兴趣</p>
          </div>
          <div class="structure-item">
            <h5>🤝 师生互动</h5>
            <p>讨论交流，合作探究</p>
          </div>
          <div class="structure-item">
            <h5>📝 实践练习</h5>
            <p>巩固知识，提升能力</p>
          </div>
          <div class="structure-item">
            <h5>📋 总结反思</h5>
            <p>梳理知识，反思学习</p>
          </div>
        </div>

        <div v-else-if="template.style === 'inquiry'" class="structure-content">
          <div class="structure-item">
            <h5>🔍 问题提出</h5>
            <p>创设情境，提出探究问题</p>
          </div>
          <div class="structure-item">
            <h5>💭 假设形成</h5>
            <p>引导学生提出假设</p>
          </div>
          <div class="structure-item">
            <h5>🧪 探究验证</h5>
            <p>设计实验，验证假设</p>
          </div>
          <div class="structure-item">
            <h5>📊 数据分析</h5>
            <p>收集数据，分析结果</p>
          </div>
          <div class="structure-item">
            <h5>💡 结论形成</h5>
            <p>得出结论，总结规律</p>
          </div>
        </div>

        <div v-else-if="template.style === 'project'" class="structure-content">
          <div class="structure-item">
            <h5>🎯 项目介绍</h5>
            <p>明确项目目标和要求</p>
          </div>
          <div class="structure-item">
            <h5>👥 团队组建</h5>
            <p>分组合作，明确分工</p>
          </div>
          <div class="structure-item">
            <h5>📋 任务分解</h5>
            <p>将项目分解为具体任务</p>
          </div>
          <div class="structure-item">
            <h5>🛠️ 项目实施</h5>
            <p>动手实践，解决问题</p>
          </div>
          <div class="structure-item">
            <h5>📢 成果展示</h5>
            <p>展示项目成果，分享经验</p>
          </div>
        </div>

        <div v-else class="structure-content">
          <div class="structure-item">
            <h5>📚 教学目标</h5>
            <p>知识与技能、过程与方法、情感态度价值观</p>
          </div>
          <div class="structure-item">
            <h5>🎯 教学重难点</h5>
            <p>明确教学重点和难点</p>
          </div>
          <div class="structure-item">
            <h5>🔄 复习导入</h5>
            <p>复习旧知，导入新课</p>
          </div>
          <div class="structure-item">
            <h5>📖 新课讲授</h5>
            <p>系统讲解，逐步深入</p>
          </div>
          <div class="structure-item">
            <h5>✏️ 课堂练习</h5>
            <p>及时练习，巩固知识</p>
          </div>
          <div class="structure-item">
            <h5>📝 课堂小结</h5>
            <p>总结归纳，布置作业</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 适用说明 -->
    <div class="preview-tips">
      <h4 class="section-title">适用说明</h4>
      <div class="tips-content">
        <p v-if="template.style === 'interactive'">
          适合需要大量师生互动的课程，如语言类、社会科学类课程。
          强调学生参与度，培养沟通表达能力。
        </p>
        <p v-else-if="template.style === 'inquiry'">
          适合理科类课程，如物理、化学、生物等。
          培养学生的科学思维和探究能力。
        </p>
        <p v-else-if="template.style === 'project'">
          适合综合性较强的课程，如信息技术、综合实践等。
          培养学生的实践能力和团队协作精神。
        </p>
        <p v-else>
          适合知识性较强的课程，如数学、历史等。
          注重知识的系统性和逻辑性。
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue';
import {
  ChatDotRound,
  Search,
  Folder,
  Document
} from '@element-plus/icons-vue';

const props = defineProps({
  template: {
    type: Object,
    required: true
  },
  outline: {
    type: String,
    default: ''
  }
});

// 获取模板图标
const getTemplateIcon = (style) => {
  const iconMap = {
    interactive: ChatDotRound,
    inquiry: Search,
    project: Folder,
    traditional: Document
  };
  return iconMap[style] || Document;
};

// 获取模板图标样式类
const getTemplateIconClass = (style) => {
  const classMap = {
    interactive: 'text-blue-600',
    inquiry: 'text-green-600',
    project: 'text-purple-600',
    traditional: 'text-orange-600'
  };
  return classMap[style] || 'text-gray-600';
};

// 获取模板标签类型
const getTemplateTagType = (style) => {
  const typeMap = {
    interactive: 'primary',
    inquiry: 'success',
    project: 'warning',
    traditional: 'info'
  };
  return typeMap[style] || 'info';
};

// 获取模板风格名称
const getTemplateStyleName = (style) => {
  const nameMap = {
    interactive: '互动式',
    inquiry: '探究式',
    project: '项目式',
    traditional: '传统式'
  };
  return nameMap[style] || '未知';
};
</script>

<style scoped>
.template-preview {
  @apply space-y-6;
}

.preview-header {
  @apply flex items-center justify-between p-4 bg-gray-50 rounded-lg;
}

.template-icon {
  @apply w-12 h-12 flex items-center justify-center bg-white rounded-lg shadow-sm;
}

.template-name {
  @apply text-lg font-semibold text-gray-900;
}

.template-description {
  @apply text-sm text-gray-600;
}

.preview-content {
  @apply space-y-4;
}

.section-title {
  @apply text-base font-medium text-gray-900 border-b border-gray-200 pb-2;
}

.structure-content {
  @apply space-y-3;
}

.structure-item {
  @apply p-3 bg-gray-50 rounded-lg;
}

.structure-item h5 {
  @apply text-sm font-medium text-gray-900 mb-1;
}

.structure-item p {
  @apply text-xs text-gray-600;
}

.preview-tips {
  @apply space-y-3;
}

.tips-content {
  @apply p-3 bg-blue-50 border border-blue-200 rounded-lg;
}

.tips-content p {
  @apply text-sm text-blue-800;
}
</style>
