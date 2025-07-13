package org.example.backend.entity.dto;

import lombok.Data;
import org.example.backend.entity.pojo.Question;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 考试详情数据传输对象，包含题目列表
 */
@Data
public class ExamDetailDTO {
    private Long id;              // 考试ID
    private String title;         // 考试标题
    private String examType;      // 考试类型：作业、测验、考试
    private LocalDateTime startTime;  // 开始时间
    private LocalDateTime endTime;    // 结束时间
    private String status;        // 状态：未开始、进行中、已结束
    private String courseName;    // 课程名称
    private String className;     // 班级名称
    private Integer totalScore;   // 总分
    private Integer duration;     // 时长（分钟）
    private List<Question> questions; // 题目列表
    
    // 学生相关信息
    private Long submissionId;    // 提交记录ID
    private Boolean isSubmitted;  // 是否已提交
    private Integer userScore;    // 学生得分
    private String submissionStatus; // 提交状态：未开始、进行中、已提交、已批改
    private LocalDateTime submittedAt;  // 提交时间
    
    // 考试规则
    private Boolean allowReview;  // 是否允许查看答案
    private Boolean shuffleQuestions; // 是否打乱题目顺序
}