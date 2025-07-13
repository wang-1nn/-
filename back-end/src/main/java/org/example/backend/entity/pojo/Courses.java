package org.example.backend.entity.pojo;

import java.util.Date;

/**
 * 课程实体类，对应数据库中的courses表
 */
public class Courses {
    private Long id;           // 课程唯一ID
    private Long teacherId;    // 创建教师ID
    private String title;      // 课程标题
    private String subject;    // 所属学科
    private String description; // 课程描述
    private Date createdAt;    // 创建时间
    private String image;      // 图片URL

    // 非数据库字段，用于前端展示
    private String teacherName; // 教师姓名
    private Integer studentCount; // 学生数量
    private Integer chapterCount; // 章节数量
    private Double progress;    // 学习进度（百分比）
    private String lastStudy;   // 上次学习时间

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    public Integer getChapterCount() {
        return chapterCount;
    }

    public void setChapterCount(Integer chapterCount) {
        this.chapterCount = chapterCount;
    }

    public Double getProgress() {
        return progress;
    }

    public void setProgress(Double progress) {
        this.progress = progress;
    }

    public String getLastStudy() {
        return lastStudy;
    }

    public void setLastStudy(String lastStudy) {
        this.lastStudy = lastStudy;
    }

    @Override
    public String toString() {
        return "Courses{" +
                "id=" + id +
                ", teacherId=" + teacherId +
                ", title='" + title + '\'' +
                ", subject='" + subject + '\'' +
                '}';
    }
} 