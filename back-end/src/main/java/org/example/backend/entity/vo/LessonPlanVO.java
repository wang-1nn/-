package org.example.backend.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 教案视图对象
 */
@Data
public class LessonPlanVO {

    private Long id;
    private String title;
    private String subject;
    private String grade;
    private String content;
    private String markdownContent;
    private String templateType;
    private String status;
    private Integer duration;
    private String difficultyLevel;
    private List<String> tags;
    private Boolean isAiGenerated;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 关联的教学资源
     */
    private List<TeachingResourceVO> resources;

    /**
     * 教学资源视图对象
     */
    @Data
    public static class TeachingResourceVO {
        private Long id;
        private String title;
        private String description;
        private String resourceType;
        private String url;
        private List<String> tags;
        private Double qualityScore;
    }
}

/**
 * 教案列表项视图对象
 */
@Data
class LessonPlanListVO {
    private Long id;
    private String title;
    private String subject;
    private String grade;
    private String status;
    private String templateType;
    private Integer duration;
    private Boolean isAiGenerated;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}

/**
 * 教案保存请求对象
 */
@Data
class SaveLessonPlanRequest {
    private String title;
    private String subject;
    private String grade;
    private String content;
    private String markdownContent;
    private String outlineContent;
    private String templateType;
    private String status;
    private Integer duration;
    private String difficultyLevel;
    private List<String> tags;
    private List<Long> resourceIds; // 关联的资源ID列表
}
