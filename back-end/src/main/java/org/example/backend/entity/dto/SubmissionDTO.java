package org.example.backend.entity.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考试提交记录数据传输对象
 */
@Data
public class SubmissionDTO {
    private Long id;              // 提交记录ID
    private Long examId;          // 考试ID
    private String examTitle;     // 考试标题
    private String examType;      // 考试类型
    private LocalDateTime startTime;    // 开始时间
    private LocalDateTime endTime;      // 结束时间
    private LocalDateTime submittedAt;  // 提交时间
    private BigDecimal totalScore;      // 总得分
    private BigDecimal maxScore;        // 满分
    private String status;        // 状态：进行中、已提交、已批改
    private Integer duration;     // 考试用时（分钟）
    private String courseName;    // 课程名称
    private String className;     // 班级名称
} 