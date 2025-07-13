package org.example.backend.entity.pojo;

import lombok.Data;

import java.util.Date;

/**
 * 学习统计详情实体类，对应数据库中的learning_statistics_detail表
 */
@Data
public class LearningStatisticsDetail {
    private Long id;                 // 统计详情ID
    private Long studentId;          // 学生ID
    private Long courseId;           // 课程ID
    private Long chapterId;          // 章节ID
    private Integer duration;        // 学习时长（秒）
    private Integer visitCount;      // 访问次数
    private Date lastVisitTime;  // 最后访问时间
    private Boolean isCompleted;     // 是否完成(0-否，1-是)
    private Date updatedAt; // 更新时间
    int TotalTime;
    int CompletionStatus;
}
