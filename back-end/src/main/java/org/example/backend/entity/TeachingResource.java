package org.example.backend.entity;

import java.time.LocalDateTime;

/**
 * 教学资源实体类，对应数据库中的teaching_resources表
 */
public class TeachingResource {
    private String resourceId;     // 资源唯一标识
    private String courseId;       // 所属课程ID
    private String title;          // 资源标题
    private String resourceType;   // 资源类型: 文档、PPT、视频等
    private String filePath;       // 文件存储路径
    private String url;            // 外部URL链接
    private String createdBy;      // 上传者ID
    private LocalDateTime createdAt; // 上传时间
    private Boolean isAiGenerated; // 是否AI生成

    // 默认构造函数
    public TeachingResource() {
    }

    // 带参构造函数
    public TeachingResource(String resourceId, String courseId, String title, String resourceType, String createdBy, LocalDateTime createdAt) {
        this.resourceId = resourceId;
        this.courseId = courseId;
        this.title = title;
        this.resourceType = resourceType;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    // Getter和Setter方法
    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
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

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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
        return "TeachingResource{" +
                "resourceId='" + resourceId + '\'' +
                ", courseId='" + courseId + '\'' +
                ", title='" + title + '\'' +
                ", resourceType='" + resourceType + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
} 