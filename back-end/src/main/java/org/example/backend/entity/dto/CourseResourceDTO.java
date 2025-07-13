package org.example.backend.entity.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 课程资源数据传输对象
 */
@Data
public class CourseResourceDTO {
    /**
     * 资源ID
     */
    private String resourceId;
    
    /**
     * 课程ID
     */
    private String courseId;
    
    /**
     * 资源标题
     */
    private String title;
    
    /**
     * 资源类型: 文档、PPT、视频等
     */
    private String resourceType;
    
    /**
     * 资源图标
     */
    private String icon;
    
    /**
     * 文件存储路径
     */
    private String filePath;
    
    /**
     * 外部URL链接
     */
    private String url;
    
    /**
     * 上传者ID
     */
    private String createdBy;
    
    /**
     * 上传者姓名
     */
    private String creatorName;
    
    /**
     * 上传时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 文件大小（KB）
     */
    private Long fileSize;
    
    /**
     * 下载次数
     */
    private Integer downloadCount;
    
    /**
     * 是否AI生成
     */
    private Boolean isAiGenerated;
} 