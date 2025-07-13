package org.example.backend.entity.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatMemory {

    /**
     * 对话ID
     */
    private String conversationId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型：USER, ASSISTANT, SYSTEM, TOOL
     */
    private String type;

    /**
     * 消息时间戳
     */
    private LocalDateTime timestamp;
}
