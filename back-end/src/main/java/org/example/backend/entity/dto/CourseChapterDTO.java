package org.example.backend.entity.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 课程章节数据传输对象
 */
@Data
public class CourseChapterDTO {
    private Long id;              // 章节ID
    private Long courseId;        // 课程ID
    private String title;         // 章节标题
    private String description;   // 章节描述
    private Integer orderNum;     // 排序序号
    private Integer duration;     // 预计学习时长（分钟）
    private Integer status;       // 状态：0-不可见，1-可见
    private Date createdAt;       // 创建时间
    
    // 扩展字段
    private List<ChapterContentDTO> contents; // 章节内容列表
    private Double completionPercentage;      // 完成百分比
    private Boolean isCompleted;              // 是否已完成
    private Integer totalStudentCount;        // 学习该章节的学生总数
    private Double averageRating;             // 平均评分
} 