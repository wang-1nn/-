package org.example.backend.entity.pojo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Courses {
    private Long id;
    private Long teacherId;
    private String title;
    private String subject;
    private String description;
    private LocalDateTime createdAt;
} 