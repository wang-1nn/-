package org.example.backend.entity.pojo;

import lombok.Data;

/**
 * 开课实体类，对应数据库中的course_offerings表
 */
@Data
public class CourseOffering {
    private Long id;           // 开课ID
    private Long courseId;     // 课程ID
    private Long classId;      // 班级ID
    private String semester;   // 学期
    private Integer status;    // 开课状态: 0-未开始, 1-进行中, 2-已结束
} 