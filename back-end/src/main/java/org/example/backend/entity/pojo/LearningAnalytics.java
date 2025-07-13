package org.example.backend.entity.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学习分析实体类，对应数据库中的learning_analytics表
 */
@Data
public class LearningAnalytics {
    private Long id;                // 分析ID
    private Long studentId;         // 学生ID
    private String subjectId;       // 学科ID
    private String strengthAreas;   // 强势领域，JSON格式
    private String weakAreas;       // 弱势领域，JSON格式
    private String learningStyle;   // 学习风格
    private LocalDateTime updatedAt;  // 更新时间
} 