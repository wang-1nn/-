package org.example.backend.service.impl.ai.TeacherAi;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpSyncClient;
import jakarta.annotation.PostConstruct;
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
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class CreateLesson {
    @Autowired
    OpenAiChatModel chatModel;

    @Autowired
    JdbcChatMemoryRepository chatMemoryRepository;
    @Autowired
    List<McpSyncClient> mcpSyncClients;

    ChatClient chatClient;
    @Autowired
    VectorStore vectorStore;

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
        this.chatClient = ChatClient.builder(chatModel)
                .defaultToolCallbacks(new SyncMcpToolCallbackProvider(mcpSyncClients)) // 挂载mcp至chatclient
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultSystem("您是一名专业的教学设计助手，可以帮助教师设计和优化教案。")
                .build();

    }

    /**
     * 基于教师消息与对话上下文生成回答。
     *
     * @param message         教师输入的问题或指令
     * @param conversationId  对话唯一 ID，用于 ChatMemory
     * @return LLM 返回的回答文本
     */
    public Flux<String> chat(String message, String conversationId) {

        String SYSTEM_PROMPT = "你是一名资深教学设计专家。\n" +
                "任务：读取\"教学大纲\"和\"教案模板\"两段输入，在不改变大纲知识顺序的前提下，融合模板要求，生成一份完整可落地的教案。\n" +
                "\n" +
                "### 输出要求\n" +
                "1. 仅返回HTML字符串，禁止出现Markdown、JSON、代码围栏或说明文字。\n" +
                "2. 页面整体包含下列语义区块（缺失必须补全）：\n" +
                "   <section id=\"course-info\">课程信息</section>\n" +
                "   <section id=\"objectives\">教学目标</section>\n" +
                "   <section id=\"content\">课程内容</section>\n" +
                "   <section id=\"resources\">参考材料</section>\n" +
                "   <section id=\"prerequisites\">前置条件</section>\n" +
                "   <section id=\"requirements\">课程要求</section>\n" +
                "   <section id=\"assessment\">课程评价</section>\n" +
                "   <section id=\"schedule\">授课计划</section>\n" +
                "   <section id=\"suggestions\">学习建议</section>\n" +
                "3. 每个<section>内部结构须遵循模板，占位符请全部替换为实际内容：\n" +
                "   - 标题用H2；子标题用H3/H4。\n" +
                "   - 关键数据（学时、Bloom动词、评分权重等）放入<ul>/<ol>/<table>。\n" +
                "   - 学时写在标题圆括号内，例如：<h3>第2章 牛顿第二定律（4 h）</h3>\n" +
                "4. 所有中英文/数字之间留1个空格，确保可读性。\n" +
                "5. 不得输出任何额外行（如\"以下为教案\"）。若排版无法满足规范，请返回\"FAIL\"。\n" +
                "\n" +
                "### 输入格式\n" +
                "- 第一段：<outline>标签包裹的教学大纲纯文本。\n" +
                "- 第二段：<template>标签包裹的教案模板纯文本。\n" +
                "\n" +
                "请准确解析两段输入，根据模板生成符合以上HTML规范的教案。";

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