package org.example.backend.entity.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 课程详情数据传输对象
 */
@Data
public class CourseDetailDTO {
    /**
     * 课程ID
     */
    private Long id;
    
    /**
     * 课程标题
     */
    private String title;
    
    /**
     * 课程描述
     */
    private String description;
    
    /**
     * 所属学科
     */
    private String subject;
    
    /**
     * 教师ID
     */
    private Long teacherId;
    
    /**
     * 教师姓名
     */
    private String teacherName;
    
    /**
     * 教师头像
     */
    private String teacherAvatar;
    
    /**
     * 课程图片URL
     */
    private String image;
    
    /**
     * 课程类别（专业课/选修）
     */
    private String courseType;
    
    /**
     * 学习进度
     */
    private BigDecimal progress;
    
    /**
     * 所属院系
     */
    private String department;
    
    /**
     * 学分
     */
    private Integer credits;
    
    /**
     * 开始日期
     */
    private LocalDateTime startDate;
    
    /**
     * 结束日期
     */
    private LocalDateTime endDate;
    
    /**
     * 课程表（上课时间和地点）
     */
    private String schedule;
    
    /**
     * 课程公告
     */
    private List<AnnouncementDTO> announcements;
    
    /**
     * 课程章节列表
     */
    private List<Map<String, Object>> chapters;
    
    /**
     * 当前章节ID
     */
    private Long currentChapterId;
    
    /**
     * 当前学习位置
     */
    private String lastPosition;
    
    /**
     * 最近访问时间
     */
    private LocalDateTime lastVisitTime;
    
    /**
     * 是否收藏
     */
    private Boolean isFavorite;
    
    /**
     * 课程公告数据传输对象
     */
    @Data
    public static class AnnouncementDTO {
        /**
         * 公告ID
         */
        private Long id;
        
        /**
         * 公告标题
         */
        private String title;
        
        /**
         * 公告内容
         */
        private String content;
        
        /**
         * 发布时间
         */
        private LocalDateTime publishTime;
        
        /**
         * 发布者姓名
         */
        private String publisherName;
    }
} 