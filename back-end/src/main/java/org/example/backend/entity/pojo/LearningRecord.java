package org.example.backend.entity.pojo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学习记录实体类，对应数据库中的learning_records表
 */
public class LearningRecord {
    private Integer id;            // 记录ID
    private String userId;         // 学生ID
    private String courseId;       // 课程ID
    private String contentType;    // 内容类型: 视频、文档、问答等
    private String contentId;      // 具体内容ID
    private LocalDateTime startTime;  // 开始学习时间
    private LocalDateTime endTime;    // 结束学习时间
    private Integer duration;      // 学习时长(秒)
    private BigDecimal progress;   // 完成进度(百分比)

    // 默认构造函数
    public LearningRecord() {
    }

    // 带参构造函数
    public LearningRecord(String userId, String courseId, String contentType, LocalDateTime startTime) {
        this.userId = userId;
        this.courseId = courseId;
        this.contentType = contentType;
        this.startTime = startTime;
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

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
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

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public BigDecimal getProgress() {
        return progress;
    }

    public void setProgress(BigDecimal progress) {
        this.progress = progress;
    }

    @Override
    public String toString() {
        return "LearningRecord{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", courseId='" + courseId + '\'' +
                ", contentType='" + contentType + '\'' +
                ", duration=" + duration +
                ", progress=" + progress +
                '}';
    }
} 