package org.example.backend.service;

import org.example.backend.entity.dto.ChapterRatingDTO;
import org.example.backend.entity.pojo.ChapterRating;

import java.util.List;

/**
 * 章节评价服务接口
 */
public interface ChapterRatingService {
    
    /**
     * 提交章节评价
     * 
     * @param rating 评价信息
     * @return 创建的评价ID
     */
    Long submitRating(ChapterRating rating);
    
    /**
     * 更新章节评价
     * 
     * @param rating 评价信息
     * @return 是否成功
     */
    boolean updateRating(ChapterRating rating);
    
    /**
     * 删除章节评价
     * 
     * @param ratingId 评价ID
     * @return 是否成功
     */
    boolean deleteRating(Long ratingId);
    
    /**
     * 获取学生对章节的评价
     * 
     * @param studentId 学生ID
     * @param chapterId 章节ID
     * @return 章节评价
     */
    ChapterRating getStudentRating(Long studentId, Long chapterId);
    
    /**
     * 根据章节ID获取评价列表
     * 
     * @param chapterId 章节ID
     * @return 评价列表
     */
    List<ChapterRating> getRatingsByChapter(Long chapterId);
    
    /**
     * 根据章节ID获取评价详情列表（包含学生信息）
     * 
     * @param chapterId 章节ID
     * @return 评价DTO列表
     */
    List<ChapterRatingDTO> getChapterRatingDetails(Long chapterId);
    
    /**
     * 获取章节的平均评分
     * 
     * @param chapterId 章节ID
     * @return 平均评分
     */
    Double getChapterAverageRating(Long chapterId);
    
    /**
     * 根据学生ID获取该学生的所有评价
     * 
     * @param studentId 学生ID
     * @return 评价DTO列表
     */
    List<ChapterRatingDTO> getStudentRatings(Long studentId);
} 