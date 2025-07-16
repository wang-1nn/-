package org.example.backend.entity.vo;

import lombok.Data;
import org.example.backend.entity.pojo.Question;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 考试视图对象
 */
@Data
public class ExamVO {
    private Long id;
    private String title;
    private String description;
    private String examType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;
    private Double passingScore;
    private Boolean showResults;
    private String status; // upcoming, ongoing, completed, draft
    private LocalDateTime createdAt;
    
    // 关联信息
    private Long offeringId;
    private Long courseId;
    private String courseName;
    private String subject;
    private Long classId;
    private String className;
    private String creatorName;
    private List<String> classNames;

    // 题目信息
    private List<Question> questions;
    
    // 统计信息
    private Integer questionCount;
    private Integer totalScore;
    private Integer studentCount;
    private Double averageScore;
    private Double highestScore;
    private Double lowestScore;
    private Double passRate;
}


