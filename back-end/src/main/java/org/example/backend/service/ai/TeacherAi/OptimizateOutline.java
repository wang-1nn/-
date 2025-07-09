package org.example.backend.service.ai.TeacherAi;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpSyncClient;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.entity.pojo.MarkdownResponse;

import org.example.backend.util.Ingestor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.*;

@Slf4j
@Service
public class OptimizateOutline {

    @Autowired
    OpenAiChatModel chatModel;

    @Autowired
    JdbcChatMemoryRepository chatMemoryRepository;

    ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .enable(JsonParser.Feature.ALLOW_COMMENTS)
            .enable(JsonParser.Feature.ALLOW_TRAILING_COMMA)
            // 反序列化时遇到未知字段不抛异常
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public String parseAiHtml(String raw) {
        if (raw == null || raw.isBlank()) return "";

        // 1) 去掉 "data:"
        String chunk = raw.startsWith("data:") ? raw.substring(5) : raw;
        chunk = chunk.trim();

        // 2) 去掉 ```html / ```json / ``` 包裹
        if (chunk.startsWith("```")) {
            int lf = chunk.indexOf('\n');
            if (lf > 0) chunk = chunk.substring(lf + 1);
            int end = chunk.lastIndexOf("```");
            if (end > 0) chunk = chunk.substring(0, end).trim();
        }

        // 3) 若是 {"html": "..."} 结构取字段
        if (chunk.startsWith("{")) {
            try {
                JsonNode node = objectMapper.readTree(chunk);
                String html = node.path("html").asText(null);
                if (html != null && !html.isBlank()) chunk = html.trim();
            } catch (Exception ignore) { }
        }

        return chunk;      // 已是纯 HTML 片段
    }

    @PostConstruct
    void init() {
        // 1. 内存（窗口大小 10）
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10)
                .build();

        // 2. ChatClient，默认挂载记忆顾问
        this.chatClient = ChatClient.builder(chatModel)// 挂载mcp至chatclient
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultSystem("你是一个自定义的rag应用请始终保持用中文回答问")
                .build();

    }

    /**
     * 基于教师消息与对话上下文生成回答。
     *
     * @param message         教师输入的问题或指令
     * @param conversationId  对话唯一 ID，用于 ChatMemory
     * @return LLM 返回的回答文本
     */
    /**
     * 调用 LLM，返回一次性 Markdown 字符串
     */
    public Flux<String> chat(String message, String conversationId) {

        /* ---------- System Prompt ---------- */
        final String SYSTEM_PROMPT = """
                final String SYSTEM_PROMPT = ""\"
                你是一名资深教学设计专家。请在 *不改变原始大纲知识顺序* 的前提下，
                优化其结构与命名，使之符合高校/中小学课程「主题页」标准，并 **仅输出纯 HTML 正文**，
                绝不能出现 JSON、Markdown、代码围栏、YAML 或任何说明文字。
                       
                ◆ 结构要求 \s
                1. 最外层禁止使用 <html>/<head>/<body>，直接输出可嵌入 <div> 的片段；UTF-8。 \s
                2. 一级标题 → <h1>，顺序固定： \s
                   ① 课程信息 ② 教学目标 ③ 课程内容 ④ 参考材料 ⑤ 前置条件 \s
                   ⑥ 课程要求 ⑦ 课程评价 ⑧ 授课计划 ⑨ 学习建议（缺失请补全） \s
                3. 二/三级标题 → <h2>/<h3>；保持原章节顺序。 \s
                4. 列表用 <ul>/<ol>，学时可写在标题 ()： \s
                   <h3>第 1 章 绪论（4 h）</h3> \s
                5. 元信息（info / objectives / contact_hours / reading / assessment / details …） \s
                   仅写在末级条目，可用 <table> 或 <ul>。 \s
                6. 排版硬规则 \s
                   • 中英文或数字之间留 1 空格。 \s
                   • 列表 / 表格上下各留 1 个空行（以 <br/> 控制）。 \s
                7. 禁止输出额外文字（如 “以下是大纲”）。 \s
                8. 若排版不符合规范，请直接输出 FAIL。
                       
                示例片段（勿复制）：
                       
                <h1>课程信息</h1>
                       
                <h2>课程及编号</h2>
                <ul>
                  <li><strong>课程代码</strong>：PHY202</li>
                  <li><strong>课程名称</strong>：大学物理 II</li>
                </ul>
                       
                <h2>学时与学期</h2>
                <ul>
                  <li><strong>总学时</strong>：50 h</li>
                  <li><strong>开设学期</strong>：2025-2026 秋</li>
                </ul>
                       
                <h1>教学目标</h1>
                <ul>
                  <li><strong>认知目标</strong>：理解牛顿运动定律</li>
                  <li><strong>能力目标</strong>：培养实验建模能力</li>
                </ul>
                       
                请严格遵守以上规范，**流式输出完整 HTML。**
                ""\";
                       
        """;

        /* ---------- 调用 LLM ---------- */
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content()                 // Flux<String> (HTML 片段)
                .map(this::parseAiHtml);   // 仅清洗
    }

}
