package org.example.backend.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 考试实体类，对应数据库中的exams表
 */
@Data
public class Exam {
    private Long id;              // 考试ID
    private Long offeringId;      // 开课班级ID
    private Long creatorId;       // 创建者ID
    private String title;         // 考试标题
    private String examType;      // 考试类型：作业、测验、考试
    private LocalDateTime startTime;  // 开始时间
    private LocalDateTime endTime;    // 结束时间
    
    // 非数据库字段，用于前端展示
    private String status;        // 状态：未开始、进行中、已结束
    private String courseName;    // 课程名称
    private String className;     // 班级名称
    private String creatorName;   // 创建者姓名
    private Integer questionCount; // 题目数量
    private Integer totalScore;   // 总分
    private Integer duration;     // 时长（分钟）
} 