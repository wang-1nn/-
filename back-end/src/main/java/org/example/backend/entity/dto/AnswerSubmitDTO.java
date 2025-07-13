package org.example.backend.entity.dto;

import lombok.Data;

/**
 * 答案提交DTO
 */
@Data
public class AnswerSubmitDTO {
    private Long questionId;
    private String answer;      // 学生答案
    private Integer timeSpent;  // 作答时间(秒)
    private Boolean useHint;    // 是否使用了提示
} 