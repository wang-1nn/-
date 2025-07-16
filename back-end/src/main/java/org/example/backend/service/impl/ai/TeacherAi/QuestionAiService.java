package org.example.backend.service.impl.ai.TeacherAi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.example.backend.entity.dto.*;
import org.example.backend.entity.pojo.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import org.example.backend.mapper.QuestionMapper;

import java.util.UUID;
import org.example.backend.mapper.BatchMapper;

/**
 * 题目生成 AI 服务。根据请求参数批量生成题目，并以 JSON 字符串流的形式输出，
 * 方便前端通过 SSE 实时接收并解析。
 */
@Service
public class QuestionAiService {

    @Autowired
    private OpenAiChatModel chatModel;

    private ChatClient chatClient;

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private BatchMapper batchMapper;

    @PostConstruct
    void init() {
        // 无需记忆功能，直接构建 ChatClient
        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem("你是一名专业的教育测评专家，擅长针对知识点生成符合规范的考题，输出内容必须严格遵守 JSON 格式。")
                .build();
    }

    /**
     * 根据参数批量生成题目，返回 JSON 字符串流。每一条字符串对应一题。
     */
    public Flux<String> generate(QGRequest req) {
        // 为本次批量生成创建统一的批次 ID
        String batchId = UUID.randomUUID().toString();



        // 组合批次名称：学科+知识点(首个)+"练习"
        String batchName = req.getSubject();
        if (req.getKnowledgePoints() != null && !req.getKnowledgePoints().isEmpty()) {
            batchName += "-" + req.getKnowledgePoints().get(0);
        }
        batchName += "练习";

        // 插入批次表
        QuestionBatch qb = new QuestionBatch();
        qb.setQuestionId(batchId);
        qb.setBatchName(batchName);
        batchMapper.insertBatch(qb);

        int total = Math.max(1, req.getCount());
        return Flux.range(0, total)
                .flatMap(i -> generateSingle(req, batchId))
                .onErrorResume(e -> Mono.just("{\"error\":\"" + e.getMessage() + "\"}"));
    }

    private Mono<String> generateSingle(QGRequest req, String batchId) {
        return Mono.fromCallable(() -> {
            // 1️⃣  题型对应的 JSON 结构片段
            String bodyBlock = switch (req.getType()) {
                case CHOICE -> """
                        - 题干：简洁、单问
                        - 必须给出 **4** 个备选项，列表顺序固定 A-D  
                        - 正确答案返回选项字母（如 \"B\")
                        - 最终仅输出合法 JSON，不要加入 markdown、中文括号或多余文字
                        {
                          \"question\":\"...\",
                          \"options\":[\"A. ...\",\"B. ...\",\"C. ...\",\"D. ...\"],
                          \"answer\":\"A\",
                          \"explain\":\"...\"
                        }""";
                case JUDGE -> """
                        - 题干：陈述句  
                        - answer 字段只能是 **\"正确\"** 或 **\"错误\"**
                        {
                          \"question\":\"...\",
                          \"answer\":\"正确\",
                          \"explain\":\"...\"
                        }""";
                case FILL, SHORT -> """
                        {
                          \"question\":\"...\",
                          \"answer\":\"...\",
                          \"explain\":\"...\"
                        }""";
            };

            // 2️⃣ 拼装 Prompt 模板
            String knowledge = (req.getKnowledgePoints() == null || req.getKnowledgePoints().isEmpty()) ?
                    "综合知识点" : String.join("、", req.getKnowledgePoints());

            String rawTemplate = """
                    你是一名%s教师，现在请针对知识点「%s」设计一道 %s（%s 难度）。请严格按下述 JSON 模式输出：
                    %s
                    """.formatted(
                    req.getSubject(),
                    knowledge,
                    req.getType().cn(),
                    req.getLevel().cn(),
                    bodyBlock);

            PromptTemplate pt = PromptTemplate.builder()
                    .renderer(StTemplateRenderer.builder()
                            .startDelimiterToken('<').endDelimiterToken('>').build())
                    .template(rawTemplate)
                    .build();

            // 3️⃣ 调用 LLM，同步→Mono；失败自动重试
            GenQuestion entity = chatClient
                    .prompt(pt.render())
                    .call()
                    .entity(GenQuestion.class);

            // —— 持久化到数据库 ——
            Question q = new Question();
            q.setQuestionId(batchId);  // 批次 ID 保持一致
            q.setQuestionType(req.getType().cn());
            q.setContent(entity.getQuestion());
            if (req.getType() == QuestionType.CHOICE && entity.getOptions() != null) {
                // 将选项合并为 JSON 字符串存库
                q.setOptions(mapper.writeValueAsString(entity.getOptions()));
            }
            q.setAnswer(entity.getAnswer());
            q.setAnalysis(entity.getExplain());
            q.setDifficulty(switch (req.getLevel()) {
                case EASY -> 1;
                case MEDIUM -> 3;
                case HARD -> 5;
            });
            q.setSubject(req.getSubject()); // 设置学科字段
            q.setIsAiGenerated(true);
            q.setCreatedBy("AI");

            // 插入数据库
            questionMapper.insertQuestion(q);

            try {
                // 将 questionId 合并到返回 JSON，供前端分批渲染
                com.fasterxml.jackson.databind.node.ObjectNode node = mapper.valueToTree(entity);
                node.put("questionId", batchId);
                return mapper.writeValueAsString(node);
            } catch (JsonProcessingException e) {
                // 如果序列化失败，则直接返回 toString
                return entity.toString();
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
}