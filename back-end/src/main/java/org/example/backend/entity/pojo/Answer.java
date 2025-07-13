package org.example.backend.entity.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生答案实体类，对应数据库中的answers表
 */
@Data
public class Answer {
    private Long id;                // 答案ID
    private Long submissionId;      // 提交记录ID
    private Long questionId;      // 题目ID
    private String userAnswer;      // 用户答案
    private BigDecimal score;       // 得分
    private Boolean isCorrect;      // 是否正确
    private String comment;         // 评语
    private LocalDateTime answeredAt;  // 作答时间
    private Long gradedBy;        // 批改人ID
    private LocalDateTime gradedAt;    // 批改时间
}
