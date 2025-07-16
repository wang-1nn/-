package org.example.backend.entity.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 题目选择VO
 */
@Data
public class QuestionSelectionVO {
    private String questionId;
    private String content;
    private String questionType;
    private String subject;
    private Integer difficulty;
    private String answer;
    private String analysis;
    private Boolean isAiGenerated;
    private LocalDateTime createdAt;
}
