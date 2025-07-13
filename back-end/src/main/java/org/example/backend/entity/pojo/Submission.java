package org.example.backend.entity.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考试提交记录实体类，对应数据库中的submissions表
 */
@Data
public class Submission {
    private Long id;                // 提交记录ID
    private Long examId;            // 考试ID
    private Long studentId;         // 学生ID
    private Integer status;         // 状态：0-进行中，1-已提交未批改，2-已批改
    private LocalDateTime submittedAt;  // 提交时间
    private BigDecimal totalScore;  // 总分
    String StatusText;
} 