<template>
  <div class="relative w-full h-full">
    <div ref="editorRef" class="w-full h-full"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue';
import * as monaco from 'monaco-editor';

// Due to vite-plugin-monaco-editor, we don't need to configure the worker manually.
// It handles the setup.

const props = defineProps({
  content: {
    type: String,
    default: '',
  }
});

const emit = defineEmits(['update:content']);

const editorRef = ref(null);
let editor = null;
const isDark = ref(document.documentElement.classList.contains('dark'));

onMounted(() => {
  if (editorRef.value) {
    editor = monaco.editor.create(editorRef.value, {
      value: props.content,
      language: 'markdown',
      theme: isDark.value ? 'vs-dark' : 'vs',
      wordWrap: 'on',
      minimap: { enabled: false },
      automaticLayout: true,
      fontSize: 14,
      scrollBeyondLastLine: false,
      padding: {
        top: 16,
        bottom: 16,
      },
      lineNumbers: 'off',
    });

    editor.onDidChangeModelContent(() => {
      emit('update:content', editor.getValue());
    });
  }
});

// Watch for prop changes to update editor content from outside
watch(() => props.content, (newValue) => {
  if (editor && newValue !== editor.getValue()) {
    editor.setValue(newValue);
  }
});

onUnmounted(() => {
  editor?.dispose();
});

// Method to insert text at cursor position
const insert = (text) => {
  if (!editor) return;
  const selection = editor.getSelection();
  const op = {
    range: selection,
    text: text,
    forceMoveMarkers: true,
  };
  editor.executeEdits('insert-text', [op]);
  editor.focus();
};

defineExpose({
  insert,
});
</script> 