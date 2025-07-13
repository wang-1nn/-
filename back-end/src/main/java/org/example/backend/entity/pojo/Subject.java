package org.example.backend.entity.pojo;

/**
 * 学科实体类，对应数据库中的subjects表
 */
public class Subject {
    private String subjectId;      // 学科唯一标识
    private String subjectName;    // 学科名称
    private String description;    // 学科描述

    // 默认构造函数
    public Subject() {
    }

    // 带参构造函数
    public Subject(String subjectId, String subjectName) {
        this.subjectId = subjectId;
        this.subjectName = subjectName;
    }

    // Getter和Setter方法
    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Subject{" +
                "subjectId='" + subjectId + '\'' +
                ", subjectName='" + subjectName + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
} 