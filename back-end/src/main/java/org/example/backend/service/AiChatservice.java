package org.example.backend.service;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class AiChatservice {
    @Autowired
    OpenAiChatModel chatModel;          // 已由 Starter 创建

    @Autowired
    VectorStore vectorStore;

    @Autowired
    JdbcChatMemoryRepository chatMemoryRepository;

    /** 仅在真正需要时才构建 ChatMemory 和 ChatClient */
    private ChatClient chatClient;

    @PostConstruct
    void init() {
        // 1. 内存（窗口大小 10）
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10)
                .build();

        // 2. ChatClient，默认挂载记忆顾问
        this.chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultSystem("你是一个自定义的rag应用请始终保持用中文回答问")
                .build();
    }
    public Flux<String> chat(String message, String conversationId){
//        init();

        /* ---------- PromptTemplate ---------- */
        PromptTemplate tpl = PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder()
                        .startDelimiterToken('<')
                        .endDelimiterToken('>')
                        .build())
                .template("""
                    <query>
                    
                    以下是上下文信息（如果为空或不包含答案，表示可用上下文不足）：
                    ---------------------
                    <question_answer_context>
                    ---------------------
                    
                    请按以下逻辑回答问题：
                    
                    1. 如果上下文不为空且其中确实包含答案，请结合上下文内容作答。
                    2. 如果上下文为空，或上下文中未包含答案，请忽略上下文，直接使用您自身的知识回答，不要输出“我不知道”或提及“基于上下文”之类的表述。同时需要加入记忆
                        """)
                .build();

        /* ---------- 向量检索顾问 ---------- */
        QuestionAnswerAdvisor qa = QuestionAnswerAdvisor.builder(vectorStore)
                .promptTemplate(tpl)
                .build();

        /* ---------- 调用 ---------- */
            return chatClient.prompt()
                    .user(message)
                    .advisors(a -> a
                .param(ChatMemory.CONVERSATION_ID, conversationId)
                .advisors(qa))          // 将 QA 顾问加入当前调用
                .stream()
                    .content();
        }
}
