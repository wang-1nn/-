package org.example.backend.entity.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学习行为记录实体类，对应数据库中的learning_behaviors表
 */
@Data
public class LearningBehavior {
    private Long id;                // 行为ID
    private Long studentId;         // 学生ID
    private Long courseId;          // 课程ID
    private Long chapterId;         // 章节ID
    private Long contentId;         // 内容ID
    private String behaviorType;    // 行为类型：VIEW-查看, DOWNLOAD-下载, COMPLETE-完成, QUIZ-测验
    private Integer duration;       // 持续时间（秒）
    private BigDecimal progress;    // 进度百分比
    private BigDecimal score;       // 得分（适用于测验）
    private String deviceType;      // 设备类型
    private LocalDateTime createdAt;  // 创建时间
} 