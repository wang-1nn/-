package org.example.backend.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 前端请求批量生成题目的参数。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QGRequest {
    /** 题目数量 */
    private int count;

    /** 科目，例如 "物理" */
    private String subject;

    /** 具体知识点列表 */
    private List<String> knowledgePoints;

    /** 难度层次 */
    private DifficultyLevel level;

    /** 题型 */
    private QuestionType type;
} 