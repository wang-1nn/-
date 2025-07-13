package org.example.backend.entity.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 讨论点赞实体类，对应数据库中的discussion_likes表
 */
@Data
public class DiscussionLike {
    private Long id;                // 点赞ID
    private Long userId;            // 用户ID
    private Long discussionId;      // 讨论ID
    private LocalDateTime createdAt;  // 创建时间
} 