<script setup>
import { ref } from 'vue'
import LucideIcon from '@/components/icons/LucideIcon.vue'

const siteName = ref('EduBrain')
const maintenance = ref(false)
const saving = ref(false)
const save = () => {
  saving.value = true
  setTimeout(()=> saving.value=false,800)
}
</script>
<template>
<div class="page-container max-w-3xl">
  <h1 class="text-2xl font-semibold mb-6 flex items-center"><LucideIcon name="settings" size="24" class="mr-2 text-gray-600"/> 系统设置</h1>
  <div class="bg-white rounded-lg shadow p-6 space-y-6">
    <div>
      <label class="block text-sm font-medium text-gray-700 mb-1">平台名称</label>
      <input v-model="siteName" class="form-input w-full"/>
    </div>
    <div class="flex items-center">
      <input type="checkbox" v-model="maintenance" id="maint" class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"/>
      <label for="maint" class="ml-2 text-sm text-gray-700">维护模式</label>
    </div>
    <button :disabled="saving" @click="save" class="btn-primary flex items-center"><LucideIcon name="save" size="16" class="mr-1"/> <span>{{ saving?'保存中...':'保存更改' }}</span></button>
  </div>
</div>
</template>
<style scoped></style> 