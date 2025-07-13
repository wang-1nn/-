package org.example.backend.entity.dto;

import lombok.Data;

import java.util.Date;

/**
 * 章节评价数据传输对象
 */
@Data
public class ChapterRatingDTO {
    private Long id;              // 评价ID
    private Long userId;          // 学生ID
    private String userName;      // 学生姓名
    private String userAvatar;    // 用户头像
    private Long courseId;        // 课程ID
    private Long chapterId;       // 章节ID
    private String chapterTitle;  // 章节标题
    private Integer rating;       // 评分(1-5)
    private String feedback;      // 评价内容
    private Date createdAt;       // 创建时间
    private Date updatedAt;       // 更新时间
} 