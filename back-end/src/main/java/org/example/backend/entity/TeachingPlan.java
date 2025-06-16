package org.example.backend.entity;

import java.time.LocalDateTime;

/**
 * 教案实体类，对应数据库中的teaching_plans表
 */
public class TeachingPlan {
    private String planId;             // 教案唯一标识
    private String courseId;           // 所属课程ID
    private String title;              // 教案标题
    private String content;            // 教案内容
    private String objectives;         // 教学目标
    private String keyPoints;          // 教学重点
    private String difficultPoints;    // 教学难点
    private String timeAllocation;     // 教学时间分配
    private String createdBy;          // 创建者ID
    private LocalDateTime createdAt;   // 创建时间
    private Boolean isAiGenerated;     // 是否AI生成

    // 默认构造函数
    public TeachingPlan() {
    }

    // 带参构造函数
    public TeachingPlan(String planId, String courseId, String title, String createdBy, LocalDateTime createdAt) {
        this.planId = planId;
        this.courseId = courseId;
        this.title = title;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    // Getter和Setter方法
    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getObjectives() {
        return objectives;
    }

    public void setObjectives(String objectives) {
        this.objectives = objectives;
    }

    public String getKeyPoints() {
        return keyPoints;
    }

    public void setKeyPoints(String keyPoints) {
        this.keyPoints = keyPoints;
    }

    public String getDifficultPoints() {
        return difficultPoints;
    }

    public void setDifficultPoints(String difficultPoints) {
        this.difficultPoints = difficultPoints;
    }

    public String getTimeAllocation() {
        return timeAllocation;
    }

    public void setTimeAllocation(String timeAllocation) {
        this.timeAllocation = timeAllocation;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsAiGenerated() {
        return isAiGenerated;
    }

    public void setIsAiGenerated(Boolean aiGenerated) {
        isAiGenerated = aiGenerated;
    }

    @Override
    public String toString() {
        return "TeachingPlan{" +
                "planId='" + planId + '\'' +
                ", courseId='" + courseId + '\'' +
                ", title='" + title + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", createdAt=" + createdAt +
                ", isAiGenerated=" + isAiGenerated +
                '}';
    }
} 