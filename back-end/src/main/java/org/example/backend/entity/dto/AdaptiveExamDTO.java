package org.example.backend.entity.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 自适应测试DTO
 */
@Data
public class AdaptiveExamDTO {
    private Long id;
    private Long userId;
    private String subject;
    private String difficulty;
    private String startingDifficulty;
    private Integer questionCount;
    private Integer currentQuestion;
    private Integer timeLimit;
    private String[] topics;
    private String status;
    private Double score;
    private Double abilityLevel;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<QuestionDTO> questions; // 存储生成的题目列表
    private Integer remainingTime; // 剩余时间（秒）
} 