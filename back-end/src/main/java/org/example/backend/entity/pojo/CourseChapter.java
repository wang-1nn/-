package org.example.backend.entity.pojo;

import lombok.Data;

import java.util.Date;

/**
 * 课程章节实体类，对应数据库中的course_chapters表
 */
@Data
public class CourseChapter {
    private Long id;             // 章节ID
    private Long courseId;       // 课程ID
    private String title;        // 章节标题
    private String description;  // 章节描述
    private Integer orderNum;    // 排序序号
    private Integer duration;    // 预计学习时长（分钟）
    private Integer status;      // 状态：0-不可见，1-可见
    private Date createdAt;  // 创建时间
}
