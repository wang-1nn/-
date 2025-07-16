package org.example.backend.entity.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 教案模板实体类
 */
@Data
public class LessonTemplate {

    private Long id;

    /**
     * 创建者ID，NULL表示系统模板
     */
    private Long teacherId;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 模板类型
     */
    private String templateType;

    /**
     * 模板内容
     */
    private String content;

    /**
     * 是否公开
     */
    private Boolean isPublic;

    /**
     * 是否系统模板
     */
    private Boolean isSystem;

    /**
     * 使用次数
     */
    private Integer usageCount;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
