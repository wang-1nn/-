package org.example.backend.entity.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 题目DTO
 */
@Data
public class QuestionDTO {
    private Long id;
    private String questionType; // SINGLE_CHOICE, MULTI_CHOICE, SHORT_ANSWER
    private String content;
    private List<Map<String, String>> options; // 选项列表，每个选项包含id和text
    private String topic;
    private Integer difficulty;
    private Double score;
    private Integer timeLimit; // 建议作答时间(秒)
    private Boolean showHint;
    private String hint;
} 