package org.example.backend.entity.pojo;

import lombok.Data;

@Data
public class Questions {
    private Long id;
    private Long creatorId;
    private Long courseId;
    private String questionType;
    private String content;
    private String answer;
    private String analysis;
    private Integer difficulty;
    private Boolean isAiGenerated;
} 