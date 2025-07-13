package org.example.backend.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程收藏实体类
 */
@Data
public class CourseFavorite {
    /**
     * 收藏ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 收藏时间
     */
    private LocalDateTime createdAt;
} 