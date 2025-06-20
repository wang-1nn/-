<template>
  <div class="table-container" :class="{ 'opacity-50 pointer-events-none': loading }">
    <transition-group tag="table" name="fade" class="table min-w-full text-sm">
      <thead class="table-header" v-if="columns.length">
        <tr>
          <th v-for="c in columns" :key="c.key" class="table-header-cell">{{ c.label }}</th>
        </tr>
      </thead>
      <tbody class="table-body">
        <tr v-for="row in rows" :key="row[rowKey] || row.id" class="table-row">
          <td v-for="c in columns" :key="c.key + (row[rowKey]||row.id)" class="table-cell">
            <slot :name="c.slot || c.key" :row="row" :value="row[c.key]">
              {{ row[c.key] }}
            </slot>
          </td>
        </tr>
      </tbody>
    </transition-group>
    <div v-if="loading" class="flex justify-center py-8 text-gray-400">
      <LucideIcon name="loader-2" class="animate-spin" size="24" />
    </div>
    <div v-else-if="!rows.length" class="flex flex-col items-center py-8 text-gray-400">
      <LucideIcon name="inbox" size="24" class="mb-1" />暂无数据
    </div>
  </div>
</template>

<script setup>
import LucideIcon from '@/components/icons/LucideIcon.vue'
import { defineProps } from 'vue'

defineProps({
  columns: { type: Array, default: () => [] },
  rows: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  rowKey: { type: String, default: 'id' }
})
</script>

<style scoped>
.fade-enter-from, .fade-leave-to { opacity: 0; transform: translateY(6px); }
.fade-enter-active, .fade-leave-active { transition: all 0.3s ease; }
</style> 