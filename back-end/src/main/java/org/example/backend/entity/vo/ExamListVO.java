package org.example.backend.entity.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 考试列表视图对象
 */
@Data
public class ExamListVO {
    private Long id;                    // 考试ID
    private String title;               // 考试标题
    private String examType;            // 考试类型：作业、测验、考试
    private String status;              // 考试状态：即将开始、进行中、已结束、草稿
    private LocalDateTime startTime;    // 开始时间
    private LocalDateTime endTime;      // 结束时间
    private String courseName;          // 课程名称
    private String className;           // 班级名称
    private String subject;             // 学科
    private Integer participantCount;   // 参与人数
    private Integer submissionCount;    // 提交人数
    private LocalDateTime createdAt;    // 创建时间
    private Long offeringId;           // 开课ID
    private Long courseId;             // 课程ID
    private Long classId;              // 班级ID
}
