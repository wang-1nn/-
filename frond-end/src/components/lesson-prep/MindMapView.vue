<template>
  <div ref="mapRef" class="w-full h-full"></div>
</template>

<script setup lang="ts">
import { onMounted, watch, ref, onBeforeUnmount } from 'vue'
// @ts-ignore
import MindElixir, { Emitter } from 'mind-elixir'
import { bus } from '@/utils/eventBus'

interface OutlineNode {
  id: number | string
  label: string
  path: string[]
  meta?: Record<string, unknown>
  children?: OutlineNode[] // for compatibility
}

const props = defineProps<{ outline: OutlineNode[] }>()

const mapRef = ref<HTMLDivElement | null>(null)
let mind: any = null

const buildTree = (nodes: OutlineNode[]): any => {
  // convert array of nodes with path arrays to MindElixir data tree
  const root: any = { id: 'root', topic: '教学大纲', children: [] }
  for (const node of nodes) {
    let current = root
    node.path.forEach((seg, idx) => {
      let child = (current.children || []).find((c: any) => c.topic === seg)
      if (!child) {
        child = { id: `${node.id}-${idx}`, topic: seg, children: [] }
        current.children = current.children || []
        current.children.push(child)
      }
      current = child
    })
    // attach final node meta
    current.id = node.id
    current.data = node // keep ref
  }
  return root
}

const initMap = () => {
  if (!mapRef.value) return
  if (!props.outline.length) return
  const data = buildTree(props.outline)

  mind = new MindElixir({
    el: mapRef.value,
    direction: MindElixir.LEFT,
    draggable: false,
    contextMenu: false,
    toolBar: false,
    keyboard: false,
    locale: 'zh_CN',
    overflowHidden: true,
  })
  mind.init(data)
  mind.bus.addListener('selectNode', (node: any) => {
    if (!node) return
    const outlineNode = node.data as OutlineNode | undefined
    if (outlineNode) {
      bus.emit('node-click', outlineNode as any)
    }
  })
}

onMounted(() => {
  initMap()
})

watch(
  () => props.outline,
  (val) => {
    if (val && val.length && mind) {
      const data = buildTree(val)
      mind.refresh(data)
    }
  }
)

onBeforeUnmount(() => {
  mind?.destroy?.()
})
</script>

<style scoped>
/* 让画布填满 */
:host {
  display: block;
  width: 100%;
  height: 100%;
}
</style> 