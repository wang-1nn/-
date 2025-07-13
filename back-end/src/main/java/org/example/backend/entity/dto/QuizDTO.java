package org.example.backend.entity.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程测验数据传输对象
 */
@Data
public class QuizDTO {
    /**
     * 测验ID
     */
    private Long id;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 测验标题
     */
    private String title;
    
    /**
     * 测验类型（测验、考试、作业）
     */
    private String quizType;
    
    /**
     * 测验状态
     * 0: 未开始, 1: 进行中, 2: 已结束, 3: 已提交
     */
    private Integer status;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 时长（分钟）
     */
    private Integer duration;
    
    /**
     * 总分数
     */
    private Integer totalScore;
    
    /**
     * 已获得分数
     */
    private Integer score;
    
    /**
     * 问题数量
     */
    private Integer questionCount;
    
    /**
     * 已完成问题数量
     */
    private Integer completedCount;
    
    /**
     * 问题列表
     */
    private List<QuizQuestionDTO> questions;
    
    /**
     * 测验问题数据传输对象
     */
    @Data
    public static class QuizQuestionDTO {
        /**
         * 问题ID
         */
        private Long id;
        
        /**
         * 测验ID
         */
        private Long quizId;
        
        /**
         * 问题类型（单选、多选、判断、填空、简答）
         */
        private String questionType;
        
        /**
         * 问题内容
         */
        private String content;
        
        /**
         * 选项列表（用于选择题）
         */
        private List<String> options;
        
        /**
         * 答案
         */
        private String answer;
        
        /**
         * 学生答案
         */
        private String studentAnswer;
        
        /**
         * 分数
         */
        private Integer score;
        
        /**
         * 获得分数
         */
        private Integer obtainedScore;
        
        /**
         * 解析
         */
        private String analysis;
        
        /**
         * 难度（1-5）
         */
        private Integer difficulty;
    }
} 