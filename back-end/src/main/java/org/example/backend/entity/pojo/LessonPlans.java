package org.example.backend.entity.pojo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LessonPlans {
    private Long id;
    private Long courseId;
    private Long creatorId;
    private String title;
    private String content;
    private Boolean isAiGenerated;
    private LocalDateTime createdAt;
} 