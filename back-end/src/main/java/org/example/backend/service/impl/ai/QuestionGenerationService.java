package org.example.backend.service.impl.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.entity.dto.GeneratedQuestionDTO;
import org.example.backend.entity.dto.QuestionGenerationRequest;
import org.example.backend.entity.pojo.Question;
import org.example.backend.mapper.QuestionMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.*;

/**
 * 高质量题目生成服务
 * 使用Spring AI实现智能题目生成
 */
@Slf4j
@Service
public class QuestionGenerationService {

    @Autowired
    private OpenAiChatModel chatModel;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private QuestionPromptTemplate promptTemplate;


    private ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    void init() {
        // 配置专门用于题目生成的ChatClient，使用高质量的Prompt模板
        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem(promptTemplate.buildSystemPrompt())
                .build();
    }

    /**
     * 生成题目（流式响应）
     */
    public Flux<String> generateQuestions(QuestionGenerationRequest request) {
        try {
            String userPrompt = buildUserPrompt(request);
            log.info("开始生成题目，知识点：{}，数量：{}", request.getTopic(), request.getCount());

            return chatClient.prompt()
                    .user(userPrompt)
                    .stream()
                    .content()
                    .doOnNext(chunk -> log.debug("收到AI响应块：{}", chunk))
                    .doOnComplete(() -> log.info("题目生成完成"))
                    .doOnError(error -> log.error("题目生成失败：{}", error.getMessage()));

        } catch (Exception e) {
            log.error("题目生成服务异常：{}", e.getMessage(), e);
            return Flux.error(new RuntimeException("题目生成失败：" + e.getMessage()));
        }
    }

    /**
     * 构建用户提示词（使用专业的Prompt模板）
     */
    private String buildUserPrompt(QuestionGenerationRequest request) {
        return promptTemplate.buildUserPrompt(request);
    }



    /**
     * 解析AI生成的题目并保存到数据库
     */
    public List<GeneratedQuestionDTO> parseAndSaveQuestions(String aiResponse, QuestionGenerationRequest request) {
        try {
            log.info("开始解析AI响应，响应长度: {}", aiResponse.length());
            log.info("AI响应前100个字符: {}", aiResponse.length() > 100 ? aiResponse.substring(0, 100) + "..." : aiResponse);

            List<GeneratedQuestionDTO> generatedQuestions = new ArrayList<>();

            try {
                // 使用简单直接的JSON解析，避免复杂的递归逻辑
                List<Map<String, Object>> questionMaps = parseAiResponseSimple(aiResponse);
                log.info("成功解析出{}个题目对象", questionMaps.size());

                for (int i = 0; i < questionMaps.size(); i++) {
                    Map<String, Object> questionMap = questionMaps.get(i);
                    try {
                        log.debug("处理第{}个题目: {}", i + 1, questionMap);

                        GeneratedQuestionDTO dto = convertToDTO(questionMap);

                        // 保存到数据库
                        Question question = convertToEntity(dto, request);
                        questionMapper.insertQuestion(question);

                        dto.setId(question.getQuestionId());
                        generatedQuestions.add(dto);

                        log.info("成功保存第{}个题目，ID: {}", i + 1, question.getQuestionId());

                    } catch (Exception e) {
                        log.error("处理第{}个题目失败：{}", i + 1, e.getMessage(), e);
                        // 继续处理其他题目，不中断整个流程
                    }
                }

            } catch (Exception e) {
                log.error("JSON解析失败，尝试简单解析: {}", e.getMessage());

                // 如果复杂解析失败，尝试创建一个默认题目
                GeneratedQuestionDTO defaultDto = createDefaultQuestion(aiResponse, request);
                if (defaultDto != null) {
                    Question question = convertToEntity(defaultDto, request);
                    questionMapper.insertQuestion(question);
                    defaultDto.setId(question.getQuestionId());
                    generatedQuestions.add(defaultDto);
                    log.info("创建了默认题目作为备用");
                }
            }

            if (generatedQuestions.isEmpty()) {
                throw new RuntimeException("没有成功解析出任何题目，请检查AI响应格式");
            }

            log.info("成功生成并保存{}道题目", generatedQuestions.size());
            return generatedQuestions;

        } catch (Exception e) {
            log.error("解析AI响应失败：{}", e.getMessage(), e);
            log.error("完整AI响应内容: {}", aiResponse);
            throw new RuntimeException("解析题目失败：" + e.getMessage());
        }
    }

    /**
     * 简单直接的AI响应解析，避免复杂递归
     */
    private List<Map<String, Object>> parseAiResponseSimple(String aiResponse) {
        try {
            // 清理响应内容
            String cleanedResponse = aiResponse
                    .replaceAll("(?m)^data:\\s*", "")
                    .replaceAll("(?m)^event:\\s*.*$", "")
                    .replaceAll("(?m)^id:\\s*.*$", "")
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*$", "")
                    .trim();

            log.debug("清理后的响应: {}", cleanedResponse);

            // 尝试直接解析为数组
            if (cleanedResponse.startsWith("[")) {
                return objectMapper.readValue(cleanedResponse, new TypeReference<List<Map<String, Object>>>() {});
            }

            // 尝试解析为单个对象
            if (cleanedResponse.startsWith("{")) {
                Map<String, Object> singleObject = objectMapper.readValue(cleanedResponse, new TypeReference<Map<String, Object>>() {});
                return List.of(singleObject);
            }

            // 如果都不是，抛出异常
            throw new RuntimeException("无法识别的JSON格式");

        } catch (Exception e) {
            log.error("简单解析失败: {}", e.getMessage());
            throw new RuntimeException("AI响应解析失败: " + e.getMessage());
        }
    }

    /**
     * 创建默认题目（当解析失败时的备用方案）
     */
    private GeneratedQuestionDTO createDefaultQuestion(String aiResponse, QuestionGenerationRequest request) {
        try {
            GeneratedQuestionDTO dto = new GeneratedQuestionDTO();
            dto.setType("single");
            dto.setContent("AI生成的题目（解析失败，显示原始内容）：" +
                    (aiResponse.length() > 200 ? aiResponse.substring(0, 200) + "..." : aiResponse));
            dto.setAnswer("请手动设置答案");
            dto.setAnalysis("此题目由于AI响应格式问题，需要手动编辑");
            dto.setDifficulty(request.getDifficulty() != null ? request.getDifficulty() : 3);
            dto.setTopic(request.getTopic());

            return dto;
        } catch (Exception e) {
            log.error("创建默认题目也失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从AI响应中提取JSON内容
     */
    private String extractJsonFromResponse(String response) {
        log.debug("原始AI响应: {}", response);

        // 移除SSE格式的data:前缀
        response = response.replaceAll("(?m)^data:\\s*", "");

        // 移除可能的markdown标记
        response = response.replaceAll("```json\\s*", "").replaceAll("```\\s*$", "");

        // 移除空行和多余的换行符
        response = response.replaceAll("\\n\\s*\\n", "\n").trim();

        // 如果响应包含多行，尝试合并JSON内容
        if (response.contains("\n")) {
            StringBuilder jsonBuilder = new StringBuilder();
            String[] lines = response.split("\n");

            for (String line : lines) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("event:") && !line.startsWith("id:")) {
                    jsonBuilder.append(line);
                }
            }
            response = jsonBuilder.toString();
        }

        // 查找JSON数组的开始和结束
        int start = response.indexOf('[');
        int end = response.lastIndexOf(']');

        if (start != -1 && end != -1 && end > start) {
            String jsonContent = response.substring(start, end + 1);
            log.debug("提取的JSON内容: {}", jsonContent);
            return jsonContent;
        }

        // 如果没有找到数组，尝试查找对象
        start = response.indexOf('{');
        end = response.lastIndexOf('}');

        if (start != -1 && end != -1 && end > start) {
            String jsonContent = response.substring(start, end + 1);
            log.debug("提取的JSON对象: {}", jsonContent);
            return jsonContent;
        }

        log.warn("无法从响应中提取有效的JSON内容: {}", response);
        return response.trim();
    }

    /**
     * 转换为DTO
     */
    private GeneratedQuestionDTO convertToDTO(Map<String, Object> questionMap) {
        log.debug("转换题目数据: {}", questionMap);

        GeneratedQuestionDTO dto = new GeneratedQuestionDTO();

        // 安全地获取字符串字段
        dto.setType(getStringValue(questionMap, "type"));
        dto.setContent(getStringValue(questionMap, "content"));
        dto.setAnswer(getStringValue(questionMap, "answer"));
        dto.setAnalysis(getStringValue(questionMap, "analysis"));
        dto.setTopic(getStringValue(questionMap, "topic"));

        // 安全地获取难度
        dto.setDifficulty(getIntegerValue(questionMap, "difficulty"));

        // 处理选项
        Object optionsObj = questionMap.get("options");
        if (optionsObj != null) {
            try {
                if (optionsObj instanceof List) {
                    dto.setOptions((List<Map<String, String>>) optionsObj);
                } else if (optionsObj instanceof String) {
                    // 如果选项是字符串，尝试解析为JSON
                    String optionsStr = (String) optionsObj;
                    if (optionsStr.startsWith("[") || optionsStr.startsWith("{")) {
                        List<Map<String, String>> parsedOptions = objectMapper.readValue(
                                optionsStr, new TypeReference<List<Map<String, String>>>() {});
                        dto.setOptions(parsedOptions);
                    }
                }
            } catch (Exception e) {
                log.warn("选项解析失败: {}", e.getMessage());
            }
        }

        log.debug("转换后的DTO: {}", dto);
        return dto;
    }

    /**
     * 安全地获取字符串值
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 安全地获取整数值
     */
    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }

        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                log.warn("无法解析整数值: {}", value);
                return null;
            }
        }

        return null;
    }

    /**
     * 转换为数据库实体
     */
    private Question convertToEntity(GeneratedQuestionDTO dto, QuestionGenerationRequest request) {
        Question question = new Question();
        question.setQuestionId(UUID.randomUUID().toString());
        question.setQuestionType(dto.getType());
        question.setContent(dto.getContent());
        question.setAnswer(dto.getAnswer());
        question.setAnalysis(dto.getAnalysis());
        question.setDifficulty(dto.getDifficulty());
        question.setScore(BigDecimal.valueOf(1.0));
        question.setCreatedBy(request.getCreatedBy());
        question.setIsAiGenerated(true);

        // 设置学科字段
        if (request.getSubject() != null && !request.getSubject().trim().isEmpty()) {
            question.setSubject(request.getSubject().trim());
        } else {
            // 如果没有明确指定学科，从知识点推导
            question.setSubject(deriveSubjectFromTopic(request.getTopic()));
        }

        // 处理选项（转换为JSON字符串）
        if (dto.getOptions() != null && !dto.getOptions().isEmpty()) {
            try {
                question.setOptions(objectMapper.writeValueAsString(dto.getOptions()));
            } catch (Exception e) {
                log.error("选项序列化失败：{}", e.getMessage());
            }
        }

        return question;
    }

    /**
     * 从知识点推导学科
     */
    private String deriveSubjectFromTopic(String topic) {
        if (topic == null || topic.trim().isEmpty()) {
            return "通用";
        }

        String lowerTopic = topic.toLowerCase();

        // 计算机相关
        if (lowerTopic.contains("java") || lowerTopic.contains("python") || lowerTopic.contains("编程") ||
            lowerTopic.contains("算法") || lowerTopic.contains("数据结构") || lowerTopic.contains("计算机") ||
            lowerTopic.contains("软件") || lowerTopic.contains("网络") || lowerTopic.contains("数据库") ||
            lowerTopic.contains("面向对象") || lowerTopic.contains("程序设计")) {
            return "计算机科学";
        }

        // 数学相关
        if (lowerTopic.contains("数学") || lowerTopic.contains("微积分") || lowerTopic.contains("线性代数") ||
            lowerTopic.contains("概率") || lowerTopic.contains("统计") || lowerTopic.contains("几何") ||
            lowerTopic.contains("代数") || lowerTopic.contains("函数")) {
            return "数学";
        }

        // 物理相关
        if (lowerTopic.contains("物理") || lowerTopic.contains("力学") || lowerTopic.contains("电磁") ||
            lowerTopic.contains("光学") || lowerTopic.contains("热学") || lowerTopic.contains("量子")) {
            return "物理";
        }

        // 化学相关
        if (lowerTopic.contains("化学") || lowerTopic.contains("有机") || lowerTopic.contains("无机") ||
            lowerTopic.contains("分子") || lowerTopic.contains("原子") || lowerTopic.contains("反应")) {
            return "化学";
        }

        // 英语相关
        if (lowerTopic.contains("英语") || lowerTopic.contains("english") || lowerTopic.contains("语法") ||
            lowerTopic.contains("词汇") || lowerTopic.contains("阅读") || lowerTopic.contains("写作")) {
            return "英语";
        }

        // 语文相关
        if (lowerTopic.contains("语文") || lowerTopic.contains("文学") || lowerTopic.contains("古诗") ||
            lowerTopic.contains("作文") || lowerTopic.contains("阅读理解") || lowerTopic.contains("汉语")) {
            return "语文";
        }

        // 历史相关
        if (lowerTopic.contains("历史") || lowerTopic.contains("古代") || lowerTopic.contains("近代") ||
            lowerTopic.contains("现代") || lowerTopic.contains("朝代") || lowerTopic.contains("战争")) {
            return "历史";
        }

        // 地理相关
        if (lowerTopic.contains("地理") || lowerTopic.contains("地球") || lowerTopic.contains("气候") ||
            lowerTopic.contains("地形") || lowerTopic.contains("人文") || lowerTopic.contains("自然")) {
            return "地理";
        }

        // 生物相关
        if (lowerTopic.contains("生物") || lowerTopic.contains("细胞") || lowerTopic.contains("基因") ||
            lowerTopic.contains("遗传") || lowerTopic.contains("生态") || lowerTopic.contains("植物") ||
            lowerTopic.contains("动物")) {
            return "生物";
        }

        // 默认返回通用
        return "通用";
    }
}
