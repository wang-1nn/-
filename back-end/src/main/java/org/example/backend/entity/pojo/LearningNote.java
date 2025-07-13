package org.example.backend.entity.pojo;

import lombok.Data;

import java.util.Date;

/**
 * 学习笔记实体类，对应数据库中的learning_notes表
 */
@Data
public class LearningNote {
    private Long id;             // 笔记ID
    private Long studentId;      // 学生ID
    private Long courseId;       // 课程ID
    private Long chapterId;      // 章节ID
    private Long contentId;      // 内容ID
    private String noteContent;  // 笔记内容
    private String timePoint;    // 视频时间点(HH:MM:SS)
    private Date createdAt;  // 创建时间
    private Date updatedAt;  // 更新时间
}
