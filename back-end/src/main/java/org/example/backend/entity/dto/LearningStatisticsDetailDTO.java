package org.example.backend.entity.dto;

import lombok.Data;

import java.util.Date;

/**
 * 学习统计详情数据传输对象
 */
@Data
public class LearningStatisticsDetailDTO {
    private Long id;              // 统计详情ID
    private Long studentId;       // 学生ID
    private Long courseId;        // 课程ID
    private String courseTitle;   // 课程标题
    private Long chapterId;       // 章节ID
    private String chapterTitle;  // 章节标题
    private Integer duration;     // 学习时长（秒）
    private String formattedDuration; // 格式化的学习时长（例如：2小时30分钟）
    private Integer visitCount;   // 访问次数
    private Date lastVisitTime;   // 最后访问时间
    private Boolean isCompleted;  // 是否完成
    private Double completionPercentage; // 完成百分比
    private Date updatedAt;       // 更新时间
} 