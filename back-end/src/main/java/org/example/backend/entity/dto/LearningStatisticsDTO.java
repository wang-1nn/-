package org.example.backend.entity.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class LearningStatisticsDTO {
    private Long courseId;
    private String courseTitle;
    private Integer totalChapters;
    private Integer completedChapters;
    private Integer totalTime; // 总学习时间（秒）
    private Integer averageRating; // 平均评分
    private LocalDateTime lastStudyTime;
    private List<ChapterStatDTO> chapterStats;
    
    @Data
    public static class ChapterStatDTO {
        private Long chapterId;
        private String chapterTitle;
        private Integer totalTime; // 章节学习时间（秒）
        private Integer visitCount;
        private LocalDateTime lastVisitTime;
        private Boolean isCompleted;
        private Integer rating; // 用户评分
    }
} 