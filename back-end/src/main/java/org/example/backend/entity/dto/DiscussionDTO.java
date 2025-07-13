package org.example.backend.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 课程讨论数据传输对象
 */
@Data
public class DiscussionDTO {
    private Long id;
    private Long courseId;
    private Long userId;
    private String userName;
    private String userAvatar;
    private String title;
    private String content;
    private LocalDateTime publishTime;
    private LocalDateTime lastReplyTime;
    private Integer replyCount;
    private Integer viewCount;
    private Integer likeCount;
    private Boolean isSticky;
    private Boolean isTeacher;
    private String tags;
    private List<String> tagList;
    private List<ReplyDTO> replies = new ArrayList<>();

    /**
     * 讨论回复数据传输对象
     */
    @Data
    public static class ReplyDTO {
        private Long id;
        private Long discussionId;
        private Long userId;
        private String userName;
        private String userAvatar;
        private String content;
        private LocalDateTime replyTime;
        private Integer likeCount;
        private Long parentId;
        private Boolean isTeacher;
    }
} 