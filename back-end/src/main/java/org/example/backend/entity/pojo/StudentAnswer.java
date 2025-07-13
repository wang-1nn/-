package org.example.backend.entity.pojo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生答案实体类，对应数据库中的student_answers表
 */
public class StudentAnswer {
    private Integer id;           // 记录ID
    private String userId;        // 学生ID
    private String examId;        // 考核ID
    private String questionId;    // 题目ID
    private String answer;        // 学生答案
    private BigDecimal score;     // 得分
    private String feedback;      // 反馈信息
    private LocalDateTime submitTime; // 提交时间

    // 默认构造函数
    public StudentAnswer() {
    }

    // 带参构造函数
    public StudentAnswer(String userId, String examId, String questionId, String answer, LocalDateTime submitTime) {
        this.userId = userId;
        this.examId = examId;
        this.questionId = questionId;
        this.answer = answer;
        this.submitTime = submitTime;
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

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public LocalDateTime getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(LocalDateTime submitTime) {
        this.submitTime = submitTime;
    }

    @Override
    public String toString() {
        return "StudentAnswer{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", examId='" + examId + '\'' +
                ", questionId='" + questionId + '\'' +
                ", score=" + score +
                '}';
    }
} 