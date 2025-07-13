package org.example.backend.entity.dto;

import lombok.Data;

import java.util.Date;

/**
 * 学习笔记数据传输对象
 */
@Data
public class LearningNoteDTO {
    private Long id;              // 笔记ID
    private Long studentId;       // 学生ID
    private String studentName;   // 学生姓名
    private Long courseId;        // 课程ID
    private String courseTitle;   // 课程标题
    private Long chapterId;       // 章节ID
    private String chapterTitle;  // 章节标题
    private Long contentId;       // 内容ID
    private String contentTitle;  // 内容标题
    private String noteContent;   // 笔记内容
    private String timePoint;     // 视频时间点(HH:MM:SS)
    private Date createdAt;       // 创建时间
    private Date updatedAt;       // 更新时间
} 