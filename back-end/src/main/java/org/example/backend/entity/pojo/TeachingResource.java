package org.example.backend.entity.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 教学资源实体类，对应数据库中的teaching_resources表
 */
@Data
public class TeachingResource {
    private Long id;                    // 资源ID
    private String title;               // 资源标题
    private String description;         // 资源描述
    private String resourceType;        // 资源类型: video, document, image, link, exercise
    private String subject;             // 学科
    private String grade;               // 年级
    private String url;                 // 资源链接
    private String filePath;            // 文件路径
    private Long fileSize;              // 文件大小(字节)
    private List<String> tags;          // 标签
    private String difficultyLevel;     // 难度等级
    private Double qualityScore;        // 质量评分(0-5)
    private Integer downloadCount;      // 下载次数
    private Integer viewCount;          // 查看次数
    private Boolean isPublic;           // 是否公开
    private Long uploaderId;            // 上传者ID

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;    // 创建时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;    // 更新时间

}