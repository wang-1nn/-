package org.example.backend.entity.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 错题收集实体类，对应数据库中的mistake_collection表
 */
@Data
public class MistakeCollection {
    private Long id;                // 收集ID
    private Long studentId;         // 学生ID
    private Long questionId;        // 题目ID
    private Long examId;            // 考试ID
    private LocalDateTime addedAt;  // 添加时间
    private Integer reviewCount;    // 复习次数
    private Integer masteryLevel;   // 掌握水平
} 