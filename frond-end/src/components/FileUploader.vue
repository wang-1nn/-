<template>
  <div>
    <input type="file" class="hidden" ref="input" :accept="accept" @change="handle" multiple>
    <button class="btn-outline" @click="() => input.click()">
      <LucideIcon name="upload" size="16" class="mr-1"/> {{label}}
    </button>
    <transition-group name="fade" tag="ul" class="mt-2 space-y-1 text-xs text-gray-600">
      <li v-for="f in files" :key="f.name">{{f.name}}</li>
    </transition-group>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import LucideIcon from '@/components/icons/LucideIcon.vue'
const props=defineProps({label:{type:String,default:'上传文件'},accept:String})
const emit=defineEmits(['upload'])
const input=ref(null)
const files=ref([])
const handle=e=>{ files.value=[...e.target.files]; emit('upload',e.target.files)}
</script>
<style scoped>
.fade-enter-from,.fade-leave-to{opacity:0;transform:translateY(4px)}
.fade-enter-active,.fade-leave-active{transition:all .2s}
</style> 