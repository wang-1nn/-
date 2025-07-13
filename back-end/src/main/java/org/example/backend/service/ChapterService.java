package org.example.backend.service;

import org.example.backend.entity.dto.ChapterContentDTO;
import org.example.backend.entity.dto.CourseChapterDTO;
import org.example.backend.entity.pojo.ChapterContent;
import org.example.backend.entity.pojo.CourseChapter;

import java.util.List;

/**
 * 章节服务接口
 */
public interface ChapterService {
    
    /**
     * 创建章节
     * 
     * @param chapter 章节信息
     * @return 创建的章节ID
     */
    Long createChapter(CourseChapter chapter);
    
    /**
     * 更新章节信息
     * 
     * @param chapter 章节信息
     * @return 是否成功
     */
    boolean updateChapter(CourseChapter chapter);
    
    /**
     * 删除章节
     * 
     * @param chapterId 章节ID
     * @return 是否成功
     */
    boolean deleteChapter(Long chapterId);
    
    /**
     * 获取章节详情
     * 
     * @param chapterId 章节ID
     * @return 章节详情DTO
     */
    CourseChapterDTO getChapterDetail(Long chapterId);
    
    /**
     * 根据课程ID获取章节列表
     * 
     * @param courseId 课程ID
     * @return 章节列表
     */
    List<CourseChapter> getChaptersByCourse(Long courseId);
    
    /**
     * 根据课程ID获取章节详情列表
     * 
     * @param courseId 课程ID
     * @return 章节详情DTO列表
     */
    List<CourseChapterDTO> getChapterDetailsByCourse(Long courseId);
    
    /**
     * 获取学生在课程中的章节学习进度
     * 
     * @param courseId 课程ID
     * @param studentId 学生ID
     * @return 章节学习进度DTO列表
     */
    List<CourseChapterDTO> getStudentChapterProgress(Long courseId, Long studentId);
    
    /**
     * 添加章节内容
     * 
     * @param content 内容信息
     * @return 创建的内容ID
     */
    Long addChapterContent(ChapterContent content);
    
    /**
     * 更新章节内容
     * 
     * @param content 内容信息
     * @return 是否成功
     */
    boolean updateChapterContent(ChapterContent content);
    
    /**
     * 删除章节内容
     * 
     * @param contentId 内容ID
     * @return 是否成功
     */
    boolean deleteChapterContent(Long contentId);
    
    /**
     * 根据章节ID获取内容列表
     * 
     * @param chapterId 章节ID
     * @return 内容列表
     */
    List<ChapterContent> getContentsByChapter(Long chapterId);
    
    /**
     * 获取章节内容详情
     * 
     * @param contentId 内容ID
     * @return 内容详情DTO
     */
    ChapterContentDTO getContentDetail(Long contentId);
    
    /**
     * 获取学生对章节内容的学习进度
     * 
     * @param chapterId 章节ID
     * @param studentId 学生ID
     * @return 内容学习进度DTO列表
     */
    List<ChapterContentDTO> getStudentContentProgress(Long chapterId, Long studentId);
    
    /**
     * 记录学生学习章节的行为
     * 
     * @param studentId 学生ID
     * @param chapterId 章节ID
     * @param contentId 内容ID
     * @param duration 学习时长（秒）
     * @return 是否成功
     */
    boolean recordLearningBehavior(Long studentId, Long chapterId, Long contentId, Integer duration);
    
    /**
     * 标记章节为已完成
     * 
     * @param studentId 学生ID
     * @param chapterId 章节ID
     * @return 是否成功
     */
    boolean markChapterAsCompleted(Long studentId, Long chapterId);
} 