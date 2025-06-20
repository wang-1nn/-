<template>
  <div>
    <slot :open="() => (show = true)"></slot>

    <transition name="fade">
      <div v-if="show" class="fixed inset-0 z-40 flex items-center justify-center bg-black/40" @click.self="show=false">
        <div class="bg-white rounded-lg shadow-lg w-full max-w-sm p-6">
          <h3 class="text-lg font-medium text-gray-800 mb-4">{{ title }}</h3>
          <div class="flex justify-end space-x-2">
            <button class="btn-outline" @click="show=false">取消</button>
            <button class="btn-danger" @click="confirm">确认</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>
<script setup>
import { ref } from 'vue'
const props=defineProps({ title:String })
const emit=defineEmits(['confirm'])
const show=ref(false)
const confirm=()=>{ emit('confirm');show.value=false }
</script>
<style scoped>
.fade-enter-from,.fade-leave-to{opacity:0}
.fade-enter-active,.fade-leave-active{transition:opacity .25s}
</style> 