package org.example.backend.entity.pojo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 自适应考试实体类
 */
@Data
public class AdaptiveExam {
    Double AbilityLevel;
    Long id;
    private Long UserId;
    private Long studentId;
    private String title;
    private String subject;
    private Integer questionCount;
    private Integer totalScore;
    private Integer duration; // 考试时长（分钟）
    private LocalDateTime  startTime;
    private LocalDateTime endTime;
    private String status; // CREATED, IN_PROGRESS, COMPLETED
    private double score;
    private List<AdaptiveQuestion> questions;

    LocalDateTime CreatedAt;
    String Topics;
    int TimeLimit;
    int CurrentQuestion;
    String StartingDifficulty;
    String Difficulty;

} 