/**
 * 布局工具函数和常量
 */

// 内容容器宽度常量
export const CONTENT_WIDTH = {
  FULL: 'full',   // 100%
  WIDE: 'wide',   // 85%, 最大1440px
  NARROW: 'narrow', // 65%, 最大1200px
  FIXED: 'fixed'  // 固定宽度
};

// 内容容器内边距常量
export const CONTENT_PADDING = {
  NONE: 'none',
  SMALL: 'small',
  DEFAULT: 'default',
  LARGE: 'large'
};

/**
 * 生成容器样式对象
 * @param {String} width - 容器宽度类型，参考CONTENT_WIDTH常量
 * @param {String|Number} fixedWidth - 固定宽度值，当width为FIXED时使用
 * @returns {Object} - 样式对象
 */
export function generateContainerStyle(width = CONTENT_WIDTH.WIDE, fixedWidth = '1200px') {
  const styles = {};
  
  if (width === CONTENT_WIDTH.FIXED) {
    styles.maxWidth = typeof fixedWidth === 'number' ? `${fixedWidth}px` : fixedWidth;
    styles.width = '100%';
    styles.marginLeft = 'auto';
    styles.marginRight = 'auto';
  }
  
  return styles;
}

/**
 * 获取推荐的内容宽度类型
 * @param {String} pageType - 页面类型
 * @returns {String} - 推荐的宽度类型
 */
export function getRecommendedWidth(pageType) {
  switch (pageType) {
    case 'dashboard':
    case 'table':
    case 'list':
      return CONTENT_WIDTH.WIDE; // 较宽的内容区，适合数据展示
      
    case 'form':
    case 'detail':
    case 'article':
      return CONTENT_WIDTH.NARROW; // 较窄的内容区，适合表单和详情
      
    case 'fullscreen':
      return CONTENT_WIDTH.FULL; // 全屏内容，适合地图、画布等
      
    default:
      return CONTENT_WIDTH.WIDE; // 默认使用较宽的内容区
  }
} 