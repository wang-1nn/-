package org.example.backend.entity.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程进度实体类，对应数据库中的course_progress表
 */
@Data
public class CourseProgress {
    private Long id;                    // 进度ID
    private Long studentId;             // 学生ID
    private Long courseId;              // 课程ID
    private Long chapterId;             // 章节ID
    private String lastPosition;        // 上次位置
    private BigDecimal completionPercentage;  // 完成百分比
    private LocalDateTime updatedAt;    // 更新时间
} 