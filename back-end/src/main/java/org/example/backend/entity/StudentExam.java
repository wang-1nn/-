package org.example.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生考核记录实体类，对应数据库中的student_exams表
 */
public class StudentExam {
    private Integer id;            // 记录ID
    private String userId;         // 学生ID
    private String examId;         // 考核ID
    private LocalDateTime startTime;  // 开始作答时间
    private LocalDateTime submitTime; // 提交时间
    private BigDecimal score;      // 得分
    private Integer status;        // 状态: 0-未开始，1-进行中，2-已提交，3-已批改

    // 默认构造函数
    public StudentExam() {
    }

    // 带参构造函数
    public StudentExam(String userId, String examId) {
        this.userId = userId;
        this.examId = examId;
    }

    // Getter和Setter方法
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getExamId() {
        return examId;
    }

    public void setExamId(String examId) {
        this.examId = examId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(LocalDateTime submitTime) {
        this.submitTime = submitTime;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "StudentExam{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", examId='" + examId + '\'' +
                ", score=" + score +
                ", status=" + status +
                '}';
    }
} 