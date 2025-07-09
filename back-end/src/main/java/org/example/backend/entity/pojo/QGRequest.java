package org.example.backend.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Question Generation Request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QGRequest {
    /** 学科 */
    private String subject;
    /** 知识点 / 章节 */
    private String knowledgePoint;
    /** 难度 */
    private DifficultyLevel level;
    /** 题型 */
    private QuestionType type;
    /** 题目数量 */
    private int count;
} 