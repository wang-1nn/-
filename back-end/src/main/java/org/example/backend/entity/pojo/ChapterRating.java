package org.example.backend.entity.pojo;

import lombok.Data;

import java.util.Date;

/**
 * 章节评价实体类，对应数据库中的chapter_ratings表
 */
@Data
public class ChapterRating {
    private Long id;             // 评价ID
    private Long studentId;      // 学生ID
    private Long courseId;       // 课程ID
    private Long chapterId;      // 章节ID
    private Integer rating;      // 评分(1-5)
    private String comment;      // 评价内容
    private Date createdAt;  // 创建时间
    private Date updatedAt;  // 更新时间
}
