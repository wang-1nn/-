export interface OutlineNode {
  /** 树节点标题 */
  label: string;
  /** 附带的 meta 信息，可为任意类型 */
  meta?: Record<string, unknown>;
}

/**
 * 根据节点信息生成标准教学教案片段
 */
function standard(node: OutlineNode): string {
  return [
    `### ${node.label}`,
    '',
    '- **info:**',
    '  - **course_name:** ${1:课程名}',
    '  - **course_code:** ${2:代码}',
    '',
    '#### 描述',
    '${3:在这里填写描述}',
    '',
    '#### 教学目标',
    '- ${4:目标一}',
    '- ${5:目标二}',
    '',
    '#### 活动设计',
    '1. ${6:活动步骤}',
    '',
    '#### 评估方式',
    '${7:评估方式}',
    '',
    '```json',
    JSON.stringify(node.meta ?? {}, null, 2),
    '```',
    ''
  ].join('\n');
}

/** 项目式学习 (PBL) 教案模板 */
function pbl(node: OutlineNode): string {
  return [
    `### ${node.label}`,
    '',
    '> **PBL 要点**：',
    '> 1. 问题情境',
    '> 2. 过程任务',
    '> 3. 结果展示',
    '',
    '#### 问题情境',
    '${1:描述具体问题情境}',
    '',
    '#### 过程任务',
    '1. ${2:任务一}',
    '2. ${3:任务二}',
    '',
    '#### 结果展示',
    '${4:展示形式}',
    '',
    '```json',
    JSON.stringify(node.meta ?? {}, null, 2),
    '```',
    ''
  ].join('\n');
}

/** 翻转课堂模板 */
function flipped(node: OutlineNode): string {
  return [
    `### ${node.label}`,
    '',
    '#### 课前学习',
    '- ${1:预习资料与任务}',
    '',
    '#### 课堂活动',
    '1. ${2:活动一}',
    '2. ${3:活动二}',
    '',
    '#### 课后作业',
    '- ${4:作业内容}',
    '',
    '```json',
    JSON.stringify(node.meta ?? {}, null, 2),
    '```',
    ''
  ].join('\n');
}

/** 游戏化教学模板 */
function gamified(node: OutlineNode): string {
  return [
    `### ${node.label}`,
    '',
    '#### 故事情节',
    '${1:设置课程故事背景}',
    '',
    '#### 关卡设计',
    '1. ${2:关卡一}',
    '2. ${3:关卡二}',
    '',
    '#### 激励机制',
    '- ${4:积分/徽章/排行榜}',
    '',
    '```json',
    JSON.stringify(node.meta ?? {}, null, 2),
    '```',
    ''
  ].join('\n');
}

/** 探究式教学模板 */
function inquiry(node: OutlineNode): string {
  return [
    `### ${node.label}`,
    '',
    '#### 探究问题',
    '${1:核心问题}',
    '',
    '#### 探究过程',
    '1. ${2:提出假设}',
    '2. ${3:实验/调研}',
    '3. ${4:数据分析}',
    '',
    '#### 结论与反思',
    '${5:结论}',
    '',
    '```json',
    JSON.stringify(node.meta ?? {}, null, 2),
    '```',
    ''
  ].join('\n');
}

export const templates = {
  standard,
  pbl,
  flipped,
  gamified,
  inquiry,
};

export type TemplateId = keyof typeof templates; 