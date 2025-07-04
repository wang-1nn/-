import mitt from 'mitt';

export type Events = {
  'node-click': unknown; // 传递点击的节点数据
  'outline-loaded': unknown; // 解析完成后的大纲数据
};

const emitter = mitt<Events>();

export const bus = emitter; 