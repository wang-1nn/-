package org.example.backend.entity.dto;

import lombok.Data;

/**
 * 答案结果DTO
 */
@Data
public class AnswerResultDTO {
    private Long questionId;
    private Boolean isCorrect;
    private Double score;
    private String correctAnswer;
    private String explanation;
    private Boolean isLastQuestion;
    private Long nextQuestionId;
    private Double currentAbilityLevel;
} 