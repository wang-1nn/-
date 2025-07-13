package org.example.backend.entity.pojo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学习统计实体类，对应数据库中的learning_statistics表
 */
@Data
public class LearningStatistics {
    private Long id;                 // 统计ID
    private Long studentId;          // 学生ID
    private Long courseId;           // 课程ID
    private Integer totalDuration;   // 总学习时长（秒）
    private Integer visitCount;      // 访问次数
    private LocalDate lastVisitDate; // 最后访问日期
    private Integer consecutiveDays; // 连续学习天数
    private LocalDateTime updatedAt; // 更新时间
} 