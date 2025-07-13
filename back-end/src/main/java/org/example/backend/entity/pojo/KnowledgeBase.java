package org.example.backend.entity.pojo;

import java.time.LocalDateTime;

/**
 * 知识库实体类，对应数据库中的knowledge_bases表
 */
public class KnowledgeBase {
    private String knowledgeBaseId;  // 知识库唯一标识
    private String title;            // 知识库标题
    private String description;      // 知识库描述
    private String subjectId;        // 所属学科ID
    private String createdBy;        // 创建者ID
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间
    private Integer status;          // 状态: 1-正常，0-禁用

    // 默认构造函数
    public KnowledgeBase() {
    }

    // 带参构造函数
    public KnowledgeBase(String knowledgeBaseId, String title, String subjectId, String createdBy, LocalDateTime createdAt) {
        this.knowledgeBaseId = knowledgeBaseId;
        this.title = title;
        this.subjectId = subjectId;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    // Getter和Setter方法
    public String getKnowledgeBaseId() {
        return knowledgeBaseId;
    }

    public void setKnowledgeBaseId(String knowledgeBaseId) {
        this.knowledgeBaseId = knowledgeBaseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "KnowledgeBase{" +
                "knowledgeBaseId='" + knowledgeBaseId + '\'' +
                ", title='" + title + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
} 