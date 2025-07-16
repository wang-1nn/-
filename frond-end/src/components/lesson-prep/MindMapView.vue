<template>
  <div class="flex flex-col h-full">
    <div v-if="!isD3Available" class="flex-1 flex items-center justify-center bg-slate-50 p-4 text-center">
      <div>
        <el-icon class="text-4xl mb-3 text-amber-500"><Warning /></el-icon>
        <h3 class="text-lg font-medium mb-2">思维导图加载失败</h3>
        <p class="text-slate-500">无法加载D3.js库或绘制思维导图</p>
      </div>
    </div>
    <div v-else class="flex-1 mind-map-container" ref="mindMapContainer"></div>

    <!-- 缩放控制 -->
    <div class="flex justify-center items-center space-x-2 p-2 bg-white border-t border-slate-200">
      <el-button size="small" circle @click="zoomOut" :disabled="!isD3Available">
        <el-icon><ZoomOut /></el-icon>
      </el-button>
      <el-slider
          v-model="zoomLevel"
          :min="20"
          :max="200"
          :step="10"
          :format-tooltip="formatZoom"
          class="w-40"
          :disabled="!isD3Available"
      />
      <el-button size="small" circle @click="zoomIn" :disabled="!isD3Available">
        <el-icon><ZoomIn /></el-icon>
      </el-button>
      <el-button size="small" @click="resetView" :disabled="!isD3Available">
        <el-icon><RefreshRight /></el-icon>重置视图
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import { ZoomIn, ZoomOut, RefreshRight, Warning } from '@element-plus/icons-vue';
import * as d3 from 'd3';

const props = defineProps({
  data: {
    type: Array,
    required: true,
    default: () => []
  }
});

const mindMapContainer = ref(null);
const zoomLevel = ref(100);
let svg = null;
let g = null;
let zoom = null;
let root = null;
let diagonal = null;
let i = 0;
const isD3Available = ref(true);

// 格式化缩放提示
const formatZoom = (val) => {
  return `${val}%`;
};

// 缩放控制
const zoomIn = () => {
  if (!isD3Available.value) return;
  zoomLevel.value = Math.min(zoomLevel.value + 10, 200);
  applyZoom();
};

const zoomOut = () => {
  if (!isD3Available.value) return;
  zoomLevel.value = Math.max(zoomLevel.value - 10, 20);
  applyZoom();
};

// 应用缩放
const applyZoom = () => {
  if (!isD3Available.value) return;
  if (svg && zoom && mindMapContainer.value) {
    try {
      const scale = zoomLevel.value / 100;
      svg.transition().duration(300).call(
          zoom.transform,
          d3.zoomIdentity.translate(mindMapContainer.value.clientWidth / 2, mindMapContainer.value.clientHeight / 2).scale(scale)
      );
    } catch (error) {
      console.error("缩放应用错误:", error);
    }
  }
};

// 重置视图
const resetView = () => {
  if (!isD3Available.value) return;
  zoomLevel.value = 100;
  if (svg && zoom && mindMapContainer.value) {
    try {
      svg.transition().duration(300).call(
          zoom.transform,
          d3.zoomIdentity.translate(mindMapContainer.value.clientWidth / 2, mindMapContainer.value.clientHeight / 2).scale(1)
      );
    } catch (error) {
      console.error("重置视图错误:", error);
    }
  }
};

// 初始化思维导图
const initMindMap = () => {
  try {
    if (!mindMapContainer.value || !props.data || !props.data.length) {
      return;
    }

    // 清除现有内容
    d3.select(mindMapContainer.value).selectAll('*').remove();

    // 设置尺寸
    const width = mindMapContainer.value.clientWidth || 500;
    const height = mindMapContainer.value.clientHeight || 400;

    // 创建 SVG
    svg = d3.select(mindMapContainer.value)
        .append('svg')
        .attr('width', '100%')
        .attr('height', '100%')
        .attr('viewBox', `0 0 ${width} ${height}`)
        .attr('class', 'mindmap-svg');

    // 创建容器组
    g = svg.append('g')
        .attr('transform', `translate(${width / 2},${height / 2})`);

    // 设置缩放行为
    zoom = d3.zoom()
        .scaleExtent([0.2, 2])
        .on('zoom', (event) => {
          g.attr('transform', event.transform);
          // 更新缩放级别
          zoomLevel.value = Math.round(event.transform.k * 100);
        });

    svg.call(zoom);

    // 设置树布局
    const tree = d3.tree()
        .size([2 * Math.PI, Math.min(width, height) / 3])
        .separation((a, b) => (a.parent === b.parent ? 1 : 2) / a.depth);

    // 设置连线路径生成器
    diagonal = d3.linkRadial()
        .angle(d => d.x)
        .radius(d => d.y);

    // 处理数据
    const sampleData = {
      label: "教学大纲",
      children: [
        {
          label: "教学目标",
          children: [
            { label: "知识目标" },
            { label: "能力目标" },
            { label: "素质目标" }
          ]
        },
        {
          label: "教学重点",
          children: [
            { label: "核心概念" },
            { label: "关键方法" }
          ]
        },
        {
          label: "教学难点",
          children: [
            { label: "疑难解析" },
            { label: "常见错误" }
          ]
        },
        {
          label: "教学过程",
          children: [
            { label: "导入环节" },
            { label: "讲授环节" },
            { label: "练习环节" },
            { label: "总结环节" }
          ]
        }
      ]
    };

    // 使用样例数据或传入的数据
    const dataToUse = props.data.length ? props.data[0] : sampleData;

    root = d3.hierarchy(dataToUse);
    root.x0 = Math.PI;
    root.y0 = 0;

    // 递归处理节点
    i = 0;
    root.descendants().forEach(d => {
      d.id = i++;
      d._children = d.children;
    });

    // 绘制思维导图
    update(root);
  } catch (error) {
    console.error("思维导图初始化失败:", error);
    isD3Available.value = false;
  }
};

// 更新视图
const update = (source) => {
  if (!isD3Available.value) return;

  try {
    const duration = 750;
    const nodes = root.descendants();
    const links = root.links();

    // 重新计算树布局
    const tree = d3.tree()
        .size([2 * Math.PI, Math.min(mindMapContainer.value.clientWidth || 500, mindMapContainer.value.clientHeight || 400) / 3])
        .separation((a, b) => (a.parent === b.parent ? 1 : 2) / a.depth);
    tree(root);

    // 更新节点
    const node = g.selectAll('g.node')
        .data(nodes, d => d.id);

    // 添加新节点
    const nodeEnter = node.enter().append('g')
        .attr('class', 'node')
        .attr('transform', d => `rotate(${source.x0 * 180 / Math.PI - 90}) translate(${source.y0},0)`)
        .attr('cursor', 'pointer')
        .on('click', (event, d) => {
          d.children = d.children ? null : d._children;
          update(d);
        });

    // 添加节点圆圈
    nodeEnter.append('circle')
        .attr('r', 5)
        .attr('fill', d => d._children && !d.children ? '#6366f1' : '#fff')
        .attr('stroke', '#6366f1')
        .attr('stroke-width', 2);

    // 添加节点文本
    nodeEnter.append('text')
        .attr('dy', '0.31em')
        .attr('x', d => d.x < Math.PI ? 8 : -8)
        .attr('text-anchor', d => d.x < Math.PI ? 'start' : 'end')
        .attr('transform', d => d.x >= Math.PI ? 'rotate(180)' : null)
        .text(d => d.data.label)
        .attr('fill', '#4b5563')
        .attr('font-size', '12px')
        .attr('font-family', 'sans-serif')
        .clone(true).lower()
        .attr('stroke', 'white')
        .attr('stroke-width', 3);

    // 更新现有节点
    const nodeUpdate = node.merge(nodeEnter)
        .transition()
        .duration(duration)
        .attr('transform', d => `rotate(${d.x * 180 / Math.PI - 90}) translate(${d.y},0)`);

    nodeUpdate.select('circle')
        .attr('r', 5)
        .attr('fill', d => d._children && !d.children ? '#6366f1' : '#fff');

    // 删除退出的节点
    const nodeExit = node.exit()
        .transition()
        .duration(duration)
        .attr('transform', `rotate(${source.x * 180 / Math.PI - 90}) translate(${source.y},0)`)
        .remove();

    nodeExit.select('circle')
        .attr('r', 1e-6);

    nodeExit.select('text')
        .style('fill-opacity', 1e-6);

    // 更新连接线
    const link = g.selectAll('path.link')
        .data(links, d => d.target.id);

    // 添加新连接线
    const linkEnter = link.enter().append('path')
        .attr('class', 'link')
        .attr('d', d => {
          const o = { x: source.x0, y: source.y0 };
          return diagonal({ source: o, target: o });
        })
        .attr('fill', 'none')
        .attr('stroke', '#d1d5db')
        .attr('stroke-width', 1.5);

    // 更新现有连接线
    link.merge(linkEnter)
        .transition()
        .duration(duration)
        .attr('d', diagonal);

    // 删除退出的连接线
    link.exit()
        .transition()
        .duration(duration)
        .attr('d', d => {
          const o = { x: source.x, y: source.y };
          return diagonal({ source: o, target: o });
        })
        .remove();

    // 保存节点位置
    nodes.forEach(d => {
      d.x0 = d.x;
      d.y0 = d.y;
    });
  } catch (error) {
    console.error("思维导图更新失败:", error);
    isD3Available.value = false;
  }
};

// 监听窗口大小变化
const handleResize = () => {
  if (isD3Available.value) {
    initMindMap();
  }
};

// 监听数据变化
watch(() => props.data, (newVal) => {
  if (newVal && newVal.length && isD3Available.value) {
    initMindMap();
  }
}, { deep: true });

onMounted(() => {
  initMindMap();
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
});
</script>

<style scoped>
.mind-map-container {
  overflow: hidden;
  background-color: #f8fafc;
  border-radius: 0.5rem;
}

.mindmap-svg {
  user-select: none;
}

:deep(.node) text {
  font-size: 12px;
  font-weight: 500;
}

:deep(.node circle) {
  transition: fill 0.3s;
}

:deep(.link) {
  transition: stroke 0.3s;
}

:deep(.node:hover) circle {
  fill: #818cf8;
}
</style> 