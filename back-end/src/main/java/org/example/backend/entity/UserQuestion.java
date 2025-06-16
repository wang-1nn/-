package org.example.backend.entity;

import java.time.LocalDateTime;

/**
 * 学生提问记录实体类，对应数据库中的user_questions表
 */
public class UserQuestion {
    private Integer id;           // 问题ID
    private String userId;        // 学生ID
    private String courseId;      // 相关课程ID
    private String question;      // 问题内容
    private String answer;        // 回答内容
    private LocalDateTime askedAt;   // 提问时间
    private LocalDateTime answeredAt; // 回答时间
    private Integer status;       // 状态: 0-未回答，1-已回答

    // 默认构造函数
    public UserQuestion() {
    }

    // 带参构造函数
    public UserQuestion(String userId, String question, LocalDateTime askedAt) {
        this.userId = userId;
        this.question = question;
        this.askedAt = askedAt;
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

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public LocalDateTime getAskedAt() {
        return askedAt;
    }

    public void setAskedAt(LocalDateTime askedAt) {
        this.askedAt = askedAt;
    }

    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(LocalDateTime answeredAt) {
        this.answeredAt = answeredAt;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "UserQuestion{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", question='" + question + '\'' +
                ", status=" + status +
                '}';
    }
} 