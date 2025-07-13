package org.example.backend.entity.pojo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 自适应考试结果实体类
 */
@Data
public class AdaptiveExamResult {

    Long UserId;
    Double AbilityLevel;
    private Long id;
    private Long examId;
    private Long studentId;
    private double score;
    private Integer totalScore;
    private Integer correctCount;
    private Integer totalCount;
    private Date startTime;
    private Date endTime;
    private Integer duration; // 实际用时（秒）
    private String analysisReport; // JSON格式的分析报告
    int TotalQuestions;
    String TopicScores;
    String Strengths;
    String Weaknesses;
    String Analysis;
    String Recommendations;
    LocalDateTime CreatedAt;


} 