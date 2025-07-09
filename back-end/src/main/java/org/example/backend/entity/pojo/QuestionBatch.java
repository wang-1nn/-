package org.example.backend.entity.pojo;

import lombok.Data;

@Data
public class QuestionBatch {
    private String questionId; // 批次 ID，与 questions.question_id 对应
    private String batchName;  // 例如 "数学-函数练习"

    /**
     * 该批次下题目的数量
     * <p>
     * 由 SQL 聚合查询自动填充，非插入必填字段。
     */
    private Integer count;

} 