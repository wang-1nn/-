package org.example.backend.entity.pojo;

import lombok.Data;

/**
 * 选课实体类，对应数据库中的enrollments表
 */
@Data
public class Enrollment {
    private Long id;           // 选课ID
    private Long studentId;    // 学生ID
    private Long offeringId;   // 开课ID
} 