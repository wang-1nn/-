<template>
  <div class="steps-container">
    <!-- 步骤进度指示器 -->
    <el-steps :active="currentStep" align-center finish-status="success" class="mb-2 animate-in">
      <el-step v-for="step in steps" :key="step.id" :title="step.title" :icon="step.icon" />
    </el-steps>

    <!-- 滚动进度条 -->
    <el-progress
        v-if="currentStep >= 1"
        :percentage="scrollProgress"
        :show-text="false"
        :stroke-width="4"
        class="mb-2 w-full"
        color="#6366f1"
    />
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits } from 'vue';
import { UploadFilled, Document, Reading } from '@element-plus/icons-vue';

const props = defineProps({
  currentStep: {
    type: Number,
    default: 0
  },
  scrollProgress: {
    type: Number,
    default: 0
  },
  customSteps: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['update:currentStep']);

// 重构的步骤配置
const defaultSteps = [
  { id: 0, title: '上传大纲', icon: UploadFilled },
  { id: 1, title: '选择模板', icon: Document },
  { id: 2, title: '生成教案', icon: Reading }
];

// 使用自定义步骤或默认步骤
const steps = ref(props.customSteps.length > 0 ? props.customSteps : defaultSteps);

// 暴露方法
defineExpose({
  nextStep: () => {
    if (props.currentStep < steps.value.length - 1) {
      emit('update:currentStep', props.currentStep + 1);
    }
  },
  prevStep: () => {
    if (props.currentStep > 0) {
      emit('update:currentStep', props.currentStep - 1);
    }
  },
  goToStep: (stepIndex) => {
    if (stepIndex >= 0 && stepIndex < steps.value.length) {
      emit('update:currentStep', stepIndex);
    }
  }
});
</script>

<style scoped>
.steps-container {
  transition: all 0.3s ease;
}

/* 步骤动画 */
.animate-in {
  animation: fadeInUp 0.5s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>