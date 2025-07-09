<template>
  <el-dialog v-model="visible" width="80%" :close-on-click-modal="false" class="lesson-plan-editor" @close="closeDialog">
    <template #header>
      <span>教案编辑</span>
    </template>

    <MdEditor v-model="localContent" preview-theme="github" language="zh-CN" :preview="true" :toolbars="toolbars" height="70vh" />

    <template #footer>
      <el-button @click="closeDialog">取消</el-button>
      <el-button type="primary" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue';
import { MdEditor } from 'md-editor-v3';
import 'md-editor-v3/lib/style.css';
import { ElNotification } from 'element-plus';

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  visible: {
    type: Boolean,
    default: false
  }
});

const emits = defineEmits(['update:modelValue', 'update:visible', 'save']);

const localContent = ref(props.modelValue);
const visible = ref(props.visible);

watch(() => props.modelValue, (v) => {
  localContent.value = v ?? '';
});

watch(() => props.visible, (v) => {
  visible.value = v;
});

watch(localContent, (v) => {
  emits('update:modelValue', v);
});

watch(visible, (v) => {
  emits('update:visible', v);
});

function save() {
  emits('save', localContent.value);
  ElNotification.success({ title: '已保存', message: '教案已保存到本地文件。' });
}

function closeDialog() {
  visible.value = false;
}

const toolbars = computed(() => [
  'bold', 'italic', 'underline', 'strikeThrough', 'title', '-',
  'unorderedList', 'orderedList', 'task', '-',
  'code', 'quote', 'table', 'link', '-',
  'undo', 'redo', 'save', 'preview'
]);
</script>

<style scoped>
.lesson-plan-editor :deep(.md-editor-toolbar) {
  /* 调整工具栏样式，如果需要 */
}
</style> 