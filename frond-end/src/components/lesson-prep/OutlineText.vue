<template>
  <div class="prose" v-html="html" />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

interface OutlineNode {
  id: number | string
  label: string
  path: string[]
  meta?: Record<string, unknown>
  children?: OutlineNode[]
}

const props = defineProps<{ outline: OutlineNode[] }>()

function outlineToMd(nodes: OutlineNode[], depth = 1): string {
  if (!nodes || !nodes.length) return ''
  return nodes
      .map(n => {
        const prefix = '#'.repeat(depth)
        const metaPart = n.meta && Object.keys(n.meta).length
            ? `\n*${JSON.stringify(n.meta, null, 2)}*\n`
            : ''
        const childrenMd = n.children?.length ? outlineToMd(n.children, depth + 1) : ''
        return `${prefix} ${n.label}\n${metaPart}${childrenMd}`
      })
      .join('\n')
}

const html = computed(() => {
  const md = outlineToMd(props.outline)
  // 确保返回的是字符串类型
  const parsedMd = marked.parse(md) as string
  return DOMPurify.sanitize(parsedMd)
})
</script> 