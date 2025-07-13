package org.example.backend.entity.pojo;

import lombok.Data;

import java.util.Date;

/**
 * 章节内容实体类，对应数据库中的chapter_contents表
 */
@Data
public class ChapterContent {
    private Long id;             // 内容ID
    private Long chapterId;      // 章节ID
    private String title;        // 内容标题
    private String contentType;  // 内容类型：VIDEO-视频, DOCUMENT-文档, QUIZ-测验
    private String contentUrl;   // 内容URL（视频或文档链接）
    private String contentText;  // 内容文本（富文本内容）
    private Integer duration;    // 时长（秒，适用于视频）
    private Integer orderNum;    // 排序序号
    private Integer status;      // 状态：0-不可见，1-可见
    private Date createdAt;  // 创建时间
}
