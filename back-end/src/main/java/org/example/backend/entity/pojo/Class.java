package org.example.backend.entity.pojo;

import lombok.Data;

/**
 * 班级实体类，对应数据库中的classes表
 */
@Data
public class Class {
    private Long id;           // 班级唯一ID
    private String name;       // 班级名称
    private String grade;      // 年级
    private String major;      // 专业
} 