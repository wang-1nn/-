package org.example.backend.entity.pojo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Exams {
    private Long id;
    private Long offeringId;
    private Long creatorId;
    private String title;
    private String examType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
} 