package org.example.backend.entity.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 教案实体类，对应数据库中的lesson_plans表
 */
@Data
public class LessonPlan {
    private Long id;                // 教案ID
    private Long courseId;          // 所属课程ID
    private Long creatorId;         // 创建者ID
    private String title;           // 教案标题
    private String content;         // 教案内容 (Markdown)
    private Boolean isAiGenerated;  // 是否AI生成
    private LocalDateTime createdAt;  // 创建时间
} 