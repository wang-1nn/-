<template>
  <div class="test-workbench">
    <h1>智能备课工作台测试</h1>
    <LessonPrepWorkbenchNew />
  </div>
</template>

<script setup>
import LessonPrepWorkbenchNew from './components/LessonPrepWorkbenchNew.vue';
</script>

<style scoped>
.test-workbench {
  padding: 20px;
  min-height: 100vh;
  background-color: #f5f5f5;
}

h1 {
  text-align: center;
  margin-bottom: 20px;
  color: #333;
}
</style>
