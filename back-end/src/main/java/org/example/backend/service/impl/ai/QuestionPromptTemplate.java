package org.example.backend.service.impl.ai;

import org.example.backend.entity.dto.QuestionGenerationRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 高质量题目生成Prompt模板
 * 基于教育测评理论和实践经验设计
 */
@Component
public class QuestionPromptTemplate {

    /**
     * 构建系统提示词
     */
    public String buildSystemPrompt() {
        return """
                # 角色定义
                你是一名资深的教育测评专家和题目设计师，拥有以下专业背景：
                - 教育学博士学位，专攻教育测量与评价
                - 15年以上的题目设计和考试命题经验
                - 熟悉布鲁姆教育目标分类法和现代测评理论
                - 精通各学科知识体系和教学大纲要求
                
                # 专业能力
                ## 题目设计原则
                1. **科学性**：确保题目内容准确无误，符合学科规范
                2. **有效性**：题目能够有效测量预期的学习目标
                3. **区分度**：能够区分不同能力水平的学生
                4. **适切性**：难度适合目标学生群体
                5. **公平性**：避免文化偏见和歧视性内容
                
                ## 认知层次设计
                根据布鲁姆分类法设计不同认知层次的题目：
                - **记忆层次**（难度1-2）：回忆基本事实、概念、定义
                - **理解层次**（难度2-3）：解释、举例、分类、总结
                - **应用层次**（难度3-4）：在新情境中运用知识和技能
                - **分析层次**（难度4-5）：分解、比较、推理、批判
                - **评价层次**（难度5）：判断、评估、论证
                - **创造层次**（难度5）：设计、构建、规划
                
                ## 题型设计标准
                ### 单选题（single）
                - 题干简洁明确，避免双重否定
                - 4个选项，长度相近，语法一致
                - 干扰项具有迷惑性但明确错误
                - 正确答案唯一且无争议
                
                ### 多选题（multiple）
                - 题干明确指出"多选"要求
                - 4-6个选项，2-3个正确答案
                - 避免"以上都对"类型选项
                - 正确答案组合具有逻辑性
                
                ### 填空题（blank）
                - 空格位置关键，不影响题意理解
                - 答案唯一或有限范围内
                - 避免过多空格导致歧义
                - 空格长度不暗示答案长度
                
                ### 简答题（shortAnswer）
                - 问题明确具体，避免过于宽泛
                - 答案要点清晰，便于评分
                - 设定合理的答题字数范围
                - 提供详细的评分标准
                
                ### 编程题（programming）
                - 问题描述清晰，包含输入输出示例
                - 提供测试用例和边界条件
                - 考虑多种解法的可能性
                - 注重算法思维和代码质量
                
                ### 匹配题（matching）
                - 左右两列数量不等，增加难度
                - 选项间有明确的对应关系
                - 避免明显的线索提示
                - 提供清晰的匹配规则
                
                # 输出要求
                ## 格式规范
                - 严格按照JSON格式输出
                - 不添加任何markdown标记或额外说明
                - 确保JSON语法正确，可被程序解析
                - 字段名称和结构完全按照要求
                
                ## 内容质量
                - 语言规范，表达准确
                - 逻辑清晰，层次分明
                - 难度适宜，梯度合理
                - 解析详细，有助于学习
                
                ## 创新性要求
                - 避免套用模板化表述
                - 结合实际应用场景
                - 体现学科特色和时代特征
                - 注重培养学生的核心素养
                """;
    }

    /**
     * 构建用户提示词
     */
    public String buildUserPrompt(QuestionGenerationRequest request) {
        StringBuilder prompt = new StringBuilder();

        // 任务描述
        prompt.append("# 题目生成任务\n\n");
        prompt.append("请根据以下具体要求，设计高质量的教学测评题目：\n\n");

        // 基本参数
        prompt.append("## 基本参数\n");
        prompt.append("- **知识点**：").append(request.getTopic()).append("\n");
        prompt.append("- **目标题型**：").append(getTypeDescriptions(request.getSelectedTypes())).append("\n");
        prompt.append("- **难度等级**：").append(request.getDifficultyDescription())
                .append("（").append(request.getDifficulty()).append("/5）\n");
        prompt.append("- **题目数量**：").append(request.getCount()).append("道\n");

        if (request.getRequirements() != null && !request.getRequirements().trim().isEmpty()) {
            prompt.append("- **特殊要求**：").append(request.getRequirements()).append("\n");
        }

        // 难度具体要求
        prompt.append("\n## 难度要求\n");
        prompt.append(getDifficultyRequirements(request.getDifficulty()));

        // 题型分布策略
        prompt.append("\n## 题型分布策略\n");
        prompt.append(getTypeDistribution(request));

        // 质量标准
        prompt.append("\n## 质量标准\n");
        prompt.append("1. **内容准确性**：确保所有知识点表述准确，无科学性错误\n");
        prompt.append("2. **语言规范性**：使用标准的学术语言，表达清晰简洁\n");
        prompt.append("3. **逻辑严密性**：题目逻辑清晰，答案推理过程合理\n");
        prompt.append("4. **实用性**：贴近实际应用，体现知识的实际价值\n");
        prompt.append("5. **创新性**：避免陈旧套路，体现新颖的考查角度\n");

        // 输出格式
        prompt.append("\n## 输出格式\n");
        prompt.append("请严格按照以下JSON格式输出，不要添加任何其他内容：\n\n");
        prompt.append("```json\n");
        prompt.append("[\n");
        prompt.append("  {\n");
        prompt.append("    \"type\": \"题型代码\",\n");
        prompt.append("    \"content\": \"题目内容（清晰完整的题干）\",\n");
        prompt.append("    \"options\": [{\"key\": \"A\", \"value\": \"选项内容\"}], // 仅选择题需要\n");
        prompt.append("    \"answer\": \"标准答案（简洁准确）\",\n");
        prompt.append("    \"analysis\": \"详细解析（包含解题思路、知识点说明、易错提醒）\",\n");
        prompt.append("    \"difficulty\": ").append(request.getDifficulty()).append(",\n");
        prompt.append("    \"topic\": \"").append(request.getTopic()).append("\"\n");
        prompt.append("  }\n");
        prompt.append("]\n");
        prompt.append("```\n");

        return prompt.toString();
    }

    /**
     * 获取题型描述
     */
    private String getTypeDescriptions(List<String> types) {
        return types.stream()
                .map(this::getTypeDescription)
                .collect(Collectors.joining("、"));
    }

    /**
     * 获取单个题型描述
     */
    private String getTypeDescription(String type) {
        switch (type) {
            case "single": return "单选题";
            case "multiple": return "多选题";
            case "blank": return "填空题";
            case "shortAnswer": return "简答题";
            case "programming": return "编程题";
            case "matching": return "匹配题";
            default: return "未知题型";
        }
    }

    /**
     * 获取难度要求说明
     */
    private String getDifficultyRequirements(Integer difficulty) {
        switch (difficulty) {
            case 1:
                return """
                       **简单难度要求**：
                       - 认知层次：记忆和基础理解
                       - 内容要求：基本概念、定义、事实性知识
                       - 思维要求：直接回忆，无需复杂推理
                       - 表述特点：直白明了，避免绕弯
                       - 答案特征：显而易见，学生容易识别
                       """;
            case 2:
                return """
                       **较简单难度要求**：
                       - 认知层次：理解和简单应用
                       - 内容要求：基本原理、简单计算、常见应用
                       - 思维要求：一步推理，简单分析
                       - 表述特点：清晰直接，逻辑简单
                       - 答案特征：通过基础知识可以得出
                       """;
            case 3:
                return """
                       **中等难度要求**：
                       - 认知层次：应用和分析
                       - 内容要求：综合运用多个知识点
                       - 思维要求：多步推理，中等分析能力
                       - 表述特点：需要仔细理解题意
                       - 答案特征：需要一定的思考和计算
                       """;
            case 4:
                return """
                       **较难难度要求**：
                       - 认知层次：分析和评价
                       - 内容要求：复杂问题，深度理解
                       - 思维要求：多层次推理，批判性思维
                       - 表述特点：可能包含隐含条件或陷阱
                       - 答案特征：需要深入分析和综合判断
                       """;
            case 5:
                return """
                       **困难难度要求**：
                       - 认知层次：评价和创造
                       - 内容要求：创新应用，综合性强
                       - 思维要求：高阶思维，创造性解决问题
                       - 表述特点：开放性强，多种解法
                       - 答案特征：需要创新思维和深度理解
                       """;
            default:
                return "中等难度，综合运用知识点";
        }
    }

    /**
     * 获取题型分布策略
     */
    private String getTypeDistribution(QuestionGenerationRequest request) {
        StringBuilder distribution = new StringBuilder();
        List<String> types = request.getSelectedTypes();
        int totalCount = request.getCount();

        distribution.append("按照以下策略分配题型数量：\n");

        for (int i = 0; i < types.size(); i++) {
            int count = totalCount / types.size() + (i < totalCount % types.size() ? 1 : 0);
            String typeDesc = getTypeDescription(types.get(i));
            distribution.append("- **").append(typeDesc).append("**：").append(count).append("道");

            // 添加题型特定要求
            switch (types.get(i)) {
                case "single":
                    distribution.append("（确保干扰项有效，避免明显错误选项）");
                    break;
                case "multiple":
                    distribution.append("（正确答案2-3个，避免全选或单选情况）");
                    break;
                case "blank":
                    distribution.append("（答案简洁明确，避免歧义）");
                    break;
                case "shortAnswer":
                    distribution.append("（设定合理字数，提供评分要点）");
                    break;
                case "programming":
                    distribution.append("（包含测试用例，考虑边界条件）");
                    break;
                case "matching":
                    distribution.append("（左右选项数量不等，增加挑战性）");
                    break;
            }
            distribution.append("\n");
        }

        return distribution.toString();
    }
}
