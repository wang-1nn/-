package org.example.backend.entity.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 讨论回复实体类，对应数据库中的discussion_replies表
 */
@Data
public class DiscussionReply {
    private Long id;                // 回复ID
    private Long discussionId;      // 讨论ID
    private Long userId;            // 回复用户ID
    private String content;         // 回复内容
    private LocalDateTime replyTime;  // 回复时间
    private Integer likeCount;      // 点赞数量
    private Long parentId;          // 父回复ID（用于嵌套回复）
    private Integer status;         // 状态：0-禁用，1-正常
} 