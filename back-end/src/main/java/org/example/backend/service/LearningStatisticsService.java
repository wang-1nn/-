package org.example.backend.service;

import org.example.backend.entity.dto.LearningStatisticsDetailDTO;
import org.example.backend.entity.pojo.LearningStatisticsDetail;

import java.util.List;
import java.util.Map;

/**
 * 学习统计服务接口
 */
public interface LearningStatisticsService {
    
    /**
     * 记录学习时长
     * 
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param chapterId 章节ID
     * @param duration 学习时长（秒）
     * @return 是否成功
     */
    boolean recordLearningTime(Long studentId, Long courseId, Long chapterId, Integer duration);
    
    /**
     * 记录章节访问
     * 
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param chapterId 章节ID
     * @return 是否成功
     */
    boolean recordChapterVisit(Long studentId, Long courseId, Long chapterId);
    
    /**
     * 标记章节为已完成
     * 
     * @param studentId 学生ID
     * @param chapterId 章节ID
     * @return 是否成功
     */
    boolean markChapterAsCompleted(Long studentId, Long chapterId);
    
    /**
     * 获取学生的章节学习统计
     * 
     * @param studentId 学生ID
     * @param chapterId 章节ID
     * @return 学习统计详情
     */
    LearningStatisticsDetail getStudentChapterStatistics(Long studentId, Long chapterId);
    
    /**
     * 获取学生在课程中的学习统计
     * 
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 学习统计详情DTO列表
     */
    List<LearningStatisticsDetailDTO> getStudentCourseStatistics(Long studentId, Long courseId);
    
    /**
     * 获取学生的课程完成进度百分比
     * 
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 完成百分比（0-100）
     */
    Double getCourseCompletionPercentage(Long studentId, Long courseId);
    
    /**
     * 获取课程的学习统计概要
     * 
     * @param courseId 课程ID
     * @return 学习统计概要
     */
    Map<String, Object> getCourseStatisticsSummary(Long courseId);
    
    /**
     * 获取学生学习时间的格式化表示
     * 
     * @param seconds 学习时间（秒）
     * @return 格式化的时间字符串（例如：2小时30分钟）
     */
    String formatLearningTime(Integer seconds);
} 