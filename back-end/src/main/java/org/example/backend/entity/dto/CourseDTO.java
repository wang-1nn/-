package org.example.backend.entity.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程列表数据传输对象
 */
@Data
public class CourseDTO {
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
     * 最近访问时间
     */
    private LocalDateTime lastVisitTime;
    
    /**
     * 是否收藏
     */
    private Boolean isFavorite;
} 