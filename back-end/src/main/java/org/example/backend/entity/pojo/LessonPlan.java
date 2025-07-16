package org.example.backend.entity.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 教案实体类，对应数据库中的lesson_plans表
 */
@Data
public class LessonPlan {
    private Long id;                    // 教案ID
    private Long teacherId;             // 教师ID (重命名以保持一致性)
    private String title;               // 教案标题
    private String subject;             // 学科
    private String grade;               // 年级
    private String content;             // 教案内容 (HTML格式)
    private String markdownContent;     // 教案内容 (Markdown格式)
    private String outlineContent;      // 原始大纲内容
    private String templateType;        // 使用的模板类型
    private String status;              // 状态: draft, published, archived
    private Integer duration;           // 课程时长(分钟)
    private String difficultyLevel;     // 难度等级
    private List<String> tags;          // 标签
    private Boolean isAiGenerated;      // 是否AI生成

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;    // 创建时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;    // 更新时间

    // 关联的教学资源 (用于查询时填充)
    private List<TeachingResource> resources;
}