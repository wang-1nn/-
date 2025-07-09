package org.example.backend.entity.pojo;

import lombok.Data;

@Data
public class CourseOfferings {
    private Long id;
    private Long courseId;
    private Long classId;
    private String semester;
    private Integer status;
} 