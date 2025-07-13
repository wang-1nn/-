package org.example.backend.entity.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程讨论实体类，对应数据库中的course_discussions表
 */
@Data
public class CourseDiscussion {
    private Long id;                // 讨论ID
    private Long courseId;          // 课程ID
    private Long userId;            // 发起人ID
    private String title;           // 讨论标题
    private String content;         // 讨论内容
    private LocalDateTime publishTime;  // 发布时间
    private LocalDateTime lastReplyTime;  // 最后回复时间
    private Integer replyCount;     // 回复数量
    private Integer viewCount;      // 浏览数量
    private Integer likeCount;      // 点赞数量
    private String tags;            // 标签，逗号分隔
    private Boolean isSticky;       // 是否置顶
    private Integer status;         // 状态：0-禁用，1-正常
} 