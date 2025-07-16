/**
 * 文档处理服务
 * 提供教学大纲文档解析、处理和格式化功能
 */

// 模拟教学大纲解析结果
const mockOutlineData = {
  basic: [
    {
      id: 'chapter1',
      label: '第一章：概述',
      path: ['第一章：概述'],
      meta: {
        learningObjectives: ['理解基本概念', '掌握核心原理'],
        keyPoints: ['概念定义', '基本框架'],
        timeAllocation: '2学时'
      },
      children: [
        {
          id: 'section1.1',
          label: '1.1 基本概念',
          path: ['第一章：概述', '1.1 基本概念'],
          meta: {
            content: '核心概念和定义介绍'
          }
        },
        {
          id: 'section1.2',
          label: '1.2 发展历程',
          path: ['第一章：概述', '1.2 发展历程'],
          meta: {
            content: '历史沿革和发展脉络'
          }
        }
      ]
    },
    {
      id: 'chapter2',
      label: '第二章：核心理论',
      path: ['第二章：核心理论'],
      meta: {
        learningObjectives: ['掌握理论基础', '能够应用理论解决问题'],
        keyPoints: ['核心理论框架', '应用场景'],
        timeAllocation: '4学时'
      },
      children: [
        {
          id: 'section2.1',
          label: '2.1 理论基础',
          path: ['第二章：核心理论', '2.1 理论基础'],
          meta: {
            content: '理论来源和基本假设'
          }
        },
        {
          id: 'section2.2',
          label: '2.2 应用方法',
          path: ['第二章：核心理论', '2.2 应用方法'],
          meta: {
            content: '方法论和应用技巧'
          }
        }
      ]
    }
  ],
  
  detailed: [
    {
      id: 'unit1',
      label: '单元一：课程导论',
      path: ['单元一：课程导论'],
      meta: {
        learningObjectives: ['建立课程整体认知', '了解评价标准'],
        keyPoints: ['课程体系', '学习方法'],
        difficulty: '低',
        timeAllocation: '2学时',
        teachingMethods: ['讲授法', '讨论法']
      },
      children: [
        {
          id: 'lesson1.1',
          label: '1.1 课程介绍',
          path: ['单元一：课程导论', '1.1 课程介绍'],
          meta: {
            content: '课程目标、内容和评价标准介绍',
            activities: ['自我介绍', '课程说明'],
            materials: ['课程大纲', '教学计划']
          }
        }
      ]
    },
    {
      id: 'unit2',
      label: '单元二：基础知识',
      path: ['单元二：基础知识'],
      meta: {
        learningObjectives: ['掌握基础理论', '建立知识框架'],
        keyPoints: ['核心概念', '基本原理'],
        difficulty: '中',
        timeAllocation: '6学时',
        teachingMethods: ['讲授法', '案例教学法', '小组讨论']
      },
      children: [
        {
          id: 'lesson2.1',
          label: '2.1 基础概念',
          path: ['单元二：基础知识', '2.1 基础概念'],
          meta: {
            content: '关键术语和概念定义',
            activities: ['概念图绘制', '小组讨论'],
            materials: ['教材第1-2章', '补充阅读材料'],
            homework: '完成概念理解练习'
          }
        },
        {
          id: 'lesson2.2',
          label: '2.2 理论框架',
          path: ['单元二：基础知识', '2.2 理论框架'],
          meta: {
            content: '主要理论模型和框架',
            activities: ['案例分析', '模型应用演练'],
            materials: ['教材第3章', '学术论文选读'],
            homework: '理论应用分析报告'
          }
        }
      ]
    }
  ],
  
  'higher-ed': [
    // 高等教育大纲数据...
    {
      id: 'module1',
      label: '模块一：理论基础',
      path: ['模块一：理论基础'],
      meta: {
        learningOutcomes: ['批判性分析核心理论', '评估理论应用的局限性'],
        assessmentCriteria: ['理论理解深度', '批判性思维能力', '学术写作规范'],
        readingList: ['必读文献5篇', '推荐阅读10篇']
      }
    }
  ],
  
  'stem': [
    // STEM课程大纲数据...
    {
      id: 'unit1-stem',
      label: '单元一：问题导向设计',
      path: ['单元一：问题导向设计'],
      meta: {
        learningObjectives: ['设计解决实际问题的方案', '应用STEM知识进行创新'],
        labActivities: ['数据采集与分析', '原型设计与测试'],
        projectDeliverables: ['设计文档', '工作原型', '技术报告']
      }
    }
  ],
  
  'humanities': [
    // 人文学科大纲数据...
    {
      id: 'theme1',
      label: '主题一：文化视角',
      path: ['主题一：文化视角'],
      meta: {
        criticalQuestions: ['如何理解文化多样性?', '文化身份如何形成?'],
        textualAnalysis: ['主要文本3篇', '视听材料5个'],
        reflectiveAssignments: ['反思日志', '批判性评论']
      }
    }
  ]
};

/**
 * 解析文档内容为结构化大纲
 * @param {File} file - 上传的文档文件
 * @param {string} outlineType - 大纲模板类型
 * @returns {Promise<Object>} 解析后的大纲数据
 */
export const parseDocument = async (file, outlineType = 'basic') => {
  // 实际项目中，这里会调用后端API进行文档解析
  // 此处使用模拟数据
  
  return new Promise((resolve) => {
    // 模拟解析过程耗时
    setTimeout(() => {
      // 根据选择的大纲类型返回对应的模拟数据
      const outlineData = mockOutlineData[outlineType] || mockOutlineData.basic;
      resolve({
        outline: outlineData,
        meta: {
          fileName: file.name,
          fileSize: file.size,
          pageCount: Math.floor(Math.random() * 10) + 5,
          parseQuality: Math.floor(Math.random() * 30) + 70 // 70-100之间的解析质量评分
        }
      });
    }, 1500);
  });
};

/**
 * 将大纲数据转换为Markdown格式
 * @param {Array} outlineData - 结构化大纲数据
 * @returns {string} Markdown文本
 */
export const outlineToMarkdown = (outlineData) => {
  if (!outlineData || !outlineData.length) {
    return '';
  }
  
  // 递归处理大纲节点
  const processNode = (node, depth = 1) => {
    if (!node) return '';
    
    const prefix = '#'.repeat(Math.min(depth, 6)); // 最多6级标题
    let md = `${prefix} ${node.label}\n\n`;
    
    // 处理节点元数据
    if (node.meta && Object.keys(node.meta).length > 0) {
      Object.entries(node.meta).forEach(([key, value]) => {
        if (Array.isArray(value)) {
          md += `**${key}**:\n`;
          value.forEach(item => {
            md += `- ${item}\n`;
          });
          md += '\n';
        } else if (typeof value === 'object') {
          md += `**${key}**:\n\`\`\`json\n${JSON.stringify(value, null, 2)}\n\`\`\`\n\n`;
        } else {
          md += `**${key}**: ${value}\n\n`;
        }
      });
    }
    
    // 处理子节点
    if (node.children && node.children.length) {
      node.children.forEach(child => {
        md += processNode(child, depth + 1);
      });
    }
    
    return md;
  };
  
  // 处理所有顶级节点
  return outlineData.map(node => processNode(node)).join('\n');
};

/**
 * 生成模拟的教案内容
 * @param {string} template - 教案模板内容
 * @param {string} outline - 大纲内容
 * @returns {Promise<string>} 生成的教案HTML内容
 */
export const generateLessonPlan = async (template, outline) => {
  // 实际项目中，这里会调用AI服务生成教案
  
  return new Promise((resolve) => {
    // 模拟生成过程，实际项目中替换为API调用
    setTimeout(() => {
      // 简单示例，实际生成的内容会更加丰富
      const content = `
<h1>教学设计方案</h1>

<h2>课程概述</h2>
<p>本课程旨在帮助学生掌握核心概念和理论框架，通过系统化的学习和实践活动，培养学生的分析能力和应用能力。</p>

<h2>教学目标</h2>
<ul>
  <li>理解基本概念和原理</li>
  <li>掌握理论应用方法</li>
  <li>培养批判性思维能力</li>
  <li>提高解决实际问题的能力</li>
</ul>

<h2>教学重难点</h2>
<h3>重点</h3>
<ul>
  <li>核心理论框架的理解与应用</li>
  <li>分析方法的掌握与运用</li>
</ul>

<h3>难点</h3>
<ul>
  <li>理论与实践的结合</li>
  <li>复杂问题的分析与解决</li>
</ul>

<h2>教学过程</h2>

<h3>一、导入新课（15分钟）</h3>
<p>通过案例引入，激发学生兴趣，建立对课程内容的初步认知。</p>
<h4>教学活动</h4>
<ul>
  <li>案例分析：展示一个与课程内容相关的真实案例</li>
  <li>问题讨论：引导学生思考案例中的关键问题</li>
</ul>

<h3>二、讲授新知（40分钟）</h3>
<p>系统讲解核心概念和理论，结合实例加深理解。</p>
<h4>教学活动</h4>
<ul>
  <li>概念讲解：清晰定义关键概念</li>
  <li>理论阐述：详细解释理论框架和应用场景</li>
  <li>示范应用：通过具体例子展示理论应用</li>
</ul>

<h3>三、巩固练习（30分钟）</h3>
<p>通过小组活动和实践练习，巩固所学知识。</p>
<h4>教学活动</h4>
<ul>
  <li>小组讨论：针对特定问题进行分组讨论</li>
  <li>应用练习：完成相关练习题或小项目</li>
  <li>成果分享：代表小组展示讨论成果</li>
</ul>

<h3>四、总结作业（5分钟）</h3>
<p>回顾本节课的主要内容，布置拓展性作业。</p>
<h4>教学活动</h4>
<ul>
  <li>内容回顾：总结本节课的关键点</li>
  <li>作业安排：布置与课程内容相关的实践作业</li>
</ul>

<h2>教学反思</h2>
<p>本教案设计注重理论与实践的结合，通过多样化的教学活动促进学生的主动参与和深度学习。在实际教学过程中，需要关注学生的反馈和理解程度，适时调整教学节奏和方式。</p>
      `;
      
      resolve(content);
    }, 3000);
  });
};

/**
 * 将大纲数据转化为思维导图可用的格式
 * @param {Array} outlineData - 结构化大纲数据
 * @returns {Object} 思维导图数据
 */
export const outlineToMindMap = (outlineData) => {
  if (!outlineData || !outlineData.length) {
    return { id: 'root', topic: '教学大纲', children: [] };
  }
  
  // 递归构建思维导图节点
  const buildMapNode = (node) => {
    const mapNode = {
      id: node.id,
      topic: node.label,
      data: node // 保留原始数据引用
    };
    
    if (node.children && node.children.length) {
      mapNode.children = node.children.map(buildMapNode);
    }
    
    return mapNode;
  };
  
  // 构建根节点
  return {
    id: 'root',
    topic: '教学大纲',
    children: outlineData.map(buildMapNode)
  };
};

export default {
  parseDocument,
  outlineToMarkdown,
  generateLessonPlan,
  outlineToMindMap
};