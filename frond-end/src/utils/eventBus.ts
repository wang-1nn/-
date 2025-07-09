import mitt from 'mitt';

export type Events = {
  'node-click': unknown; // 传递点击的节点数据
  'outline-loaded': unknown; // 解析完成后的大纲数据
  'md-ready': string; // 完整的Markdown内容
  'md-chunk': string; // Markdown块
  'preview-loading': boolean; // 预览加载状态
  'outline-update': string; // 大纲更新
  'template-update': string; // 模板更新
  'open-editor': string; // 打开编辑窗口，携带完整教案字符串
};

const emitter = mitt<Events>();

export const bus = emitter; 