package org.example.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考核实体类，对应数据库中的exams表
 */
public class Exam {
    private String examId;         // 考核唯一标识
    private String courseId;       // 所属课程ID
    private String title;          // 考核标题
    private String examType;       // 考核类型: 作业、测验、考试
    private LocalDateTime startTime; // 开始时间
    private LocalDateTime endTime;   // 结束时间
    private BigDecimal totalScore;   // 总分
    private BigDecimal passingScore; // 及格分数
    private String createdBy;      // 创建者ID
    private Boolean isAiGenerated; // 是否AI生成
    private Integer status;        // 状态: 0-未开始，1-进行中，2-已结束

    // 默认构造函数
    public Exam() {
    }

    // 带参构造函数
    public Exam(String examId, String courseId, String title, String examType, String createdBy) {
        this.examId = examId;
        this.courseId = courseId;
        this.title = title;
        this.examType = examType;
        this.createdBy = createdBy;
    }

    // Getter和Setter方法
    public String getExamId() {
        return examId;
    }

    public void setExamId(String examId) {
        this.examId = examId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExamType() {
        return examType;
    }

    public void setExamType(String examType) {
        this.examType = examType;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public BigDecimal getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }

    public BigDecimal getPassingScore() {
        return passingScore;
    }

    public void setPassingScore(BigDecimal passingScore) {
        this.passingScore = passingScore;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Boolean getIsAiGenerated() {
        return isAiGenerated;
    }

    public void setIsAiGenerated(Boolean aiGenerated) {
        isAiGenerated = aiGenerated;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Exam{" +
                "examId='" + examId + '\'' +
                ", courseId='" + courseId + '\'' +
                ", title='" + title + '\'' +
                ", examType='" + examType + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", status=" + status +
                '}';
    }
} 