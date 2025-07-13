package org.example.backend.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI配置类
 */
@Configuration
public class SpringAiConfig {

    /**
     * 配置ChatClient，用于与AI服务通信
     *
     * @param openAiChatModel Spring自动注入的OpenAI聊天模型
     * @return 配置好的ChatClient实例
     */
    @Bean
    public ChatClient chatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel)
            .build();
    }
} 