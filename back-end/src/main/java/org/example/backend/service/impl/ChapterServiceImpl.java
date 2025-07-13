package org.example.backend.service.impl;

import org.example.backend.entity.dto.ChapterContentDTO;
import org.example.backend.entity.dto.CourseChapterDTO;
import org.example.backend.mapper.ChapterContentMapper;
import org.example.backend.mapper.CourseChapterMapper;
import org.example.backend.mapper.LearningStatisticsDetailMapper;
import org.example.backend.service.ChapterService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 章节服务实现类
 */
@Service
public class ChapterServiceImpl implements ChapterService {
    
    @Autowired
    private CourseChapterMapper courseChapterMapper;
    
    @Autowired
    private ChapterContentMapper chapterContentMapper;
    
    @Autowired
    private LearningStatisticsDetailMapper learningStatisticsDetailMapper;
    
    @Override
    @Transactional
    public Long createChapter(CourseChapter chapter) {
        // 设置创建时间
        chapter.setCreatedAt(new Date());
        
        // 如果状态为空，默认设为可见
        if (chapter.getStatus() == null) {
            chapter.setStatus(1);
        }
        
        // 如果排序序号为空，默认设为0
        if (chapter.getOrderNum() == null) {
            chapter.setOrderNum(0);
        }
        
        // 保存章节信息
        courseChapterMapper.insert(chapter);
        
        return chapter.getId();
    }
    
    @Override
    @Transactional
    public boolean updateChapter(CourseChapter chapter) {
        // 更新章节信息
        return courseChapterMapper.update(chapter) > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteChapter(Long chapterId) {
        // 删除章节相关的所有内容
        chapterContentMapper.deleteByChapterId(chapterId);
        
        // 删除章节
        return courseChapterMapper.deleteById(chapterId) > 0;
    }
    
    @Override
    public CourseChapterDTO getChapterDetail(Long chapterId) {
        // 查询章节基本信息
        CourseChapter chapter = courseChapterMapper.findById(chapterId);
        
        if (chapter == null) {
            return null;
        }
        
        // 转换为DTO
        CourseChapterDTO chapterDTO = new CourseChapterDTO();
        BeanUtils.copyProperties(chapter, chapterDTO);
        
        // 获取章节的平均评分
        Double averageRating = courseChapterMapper.getAverageRating(chapterId);
        chapterDTO.setAverageRating(averageRating);
        
        // 获取学习该章节的学生总数
        Integer totalStudentCount = courseChapterMapper.getStudentCount(chapterId);
        chapterDTO.setTotalStudentCount(totalStudentCount);
        
        // 获取章节内容列表
        List<ChapterContent> contents = chapterContentMapper.findByChapterId(chapterId);
        
        // 转换为DTO列表
        List<ChapterContentDTO> contentDTOs = contents.stream().map(content -> {
            ChapterContentDTO contentDTO = new ChapterContentDTO();
            BeanUtils.copyProperties(content, contentDTO);
            return contentDTO;
        }).collect(Collectors.toList());
        
        chapterDTO.setContents(contentDTOs);
        
        return chapterDTO;
    }
    
    @Override
    public List<CourseChapter> getChaptersByCourse(Long courseId) {
        // 查询课程下的所有章节
        return courseChapterMapper.findByCourseId(courseId);
    }
    
    @Override
    public List<CourseChapterDTO> getChapterDetailsByCourse(Long courseId) {
        // 查询课程下的所有章节及其详细信息
        return courseChapterMapper.getChapterDetailsByCourseId(courseId);
    }
    
    @Override
    public List<CourseChapterDTO> getStudentChapterProgress(Long courseId, Long studentId) {
        // 查询学生在课程中的章节学习进度
        return courseChapterMapper.getStudentChapterProgress(courseId, studentId);
    }
    
    @Override
    @Transactional
    public Long addChapterContent(ChapterContent content) {
        // 设置创建时间
        content.setCreatedAt(new Date());
        
        // 如果状态为空，默认设为可见
        if (content.getStatus() == null) {
            content.setStatus(1);
        }
        
        // 如果排序序号为空，默认设为0
        if (content.getOrderNum() == null) {
            content.setOrderNum(0);
        }
        
        // 保存内容信息
        chapterContentMapper.insert(content);
        
        return content.getId();
    }
    
    @Override
    @Transactional
    public boolean updateChapterContent(ChapterContent content) {
        // 更新内容信息
        return chapterContentMapper.update(content) > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteChapterContent(Long contentId) {
        // 删除内容
        return chapterContentMapper.deleteById(contentId) > 0;
    }
    
    @Override
    public List<ChapterContent> getContentsByChapter(Long chapterId) {
        // 查询章节下的所有内容
        return chapterContentMapper.findByChapterId(chapterId);
    }
    
    @Override
    public ChapterContentDTO getContentDetail(Long contentId) {
        // 查询内容基本信息
        ChapterContent content = chapterContentMapper.selectById(contentId);
        
        if (content == null) {
            return null;
        }
        
        // 转换为DTO
        ChapterContentDTO contentDTO = new ChapterContentDTO();
        BeanUtils.copyProperties(content, contentDTO);
        
        return contentDTO;
    }
    
    @Override
    public List<ChapterContentDTO> getStudentContentProgress(Long chapterId, Long studentId) {
        // 查询学生对章节内容的学习进度
        return chapterContentMapper.getStudentContentProgress(chapterId, studentId);
    }
    
    @Override
    @Transactional
    public boolean recordLearningBehavior(Long studentId, Long chapterId, Long contentId, Integer duration) {
        // 查询学生的章节学习统计记录
        LearningStatisticsDetail detail = learningStatisticsDetailMapper.findByStudentAndChapter(studentId, chapterId);
        
        if (detail == null) {
            // 如果不存在，则创建新记录
            detail = new LearningStatisticsDetail();
            detail.setStudentId(studentId);
            detail.setChapterId(chapterId);
            
            // 查询章节所属的课程ID
            CourseChapter chapter = courseChapterMapper.findById(chapterId);
            if (chapter == null) {
                return false;
            }
            
            detail.setCourseId(chapter.getCourseId());
            detail.setTotalTime(duration);
            detail.setVisitCount(1);
            detail.setLastVisitTime(new Date());
            detail.setCompletionStatus(0);
            detail.setUpdatedAt(new Date());
            
            // 保存学习统计记录
            return learningStatisticsDetailMapper.insert(detail) > 0;
        } else {
            // 更新学习时长
            learningStatisticsDetailMapper.updateDuration(detail.getId(), duration);
            
            // 更新访问次数和最后访问时间
            learningStatisticsDetailMapper.updateVisitCount(detail.getId());
            
            return true;
        }
    }
    
    @Override
    @Transactional
    public boolean markChapterAsCompleted(Long studentId, Long chapterId) {
        // 查询学生的章节学习统计记录
        LearningStatisticsDetail detail = learningStatisticsDetailMapper.findByStudentAndChapter(studentId, chapterId);
        
        if (detail == null) {
            // 如果不存在，则创建新记录并标记为已完成
            detail = new LearningStatisticsDetail();
            detail.setStudentId(studentId);
            detail.setChapterId(chapterId);
            
            // 查询章节所属的课程ID
            CourseChapter chapter = courseChapterMapper.findById(chapterId);
            if (chapter == null) {
                return false;
            }
            
            detail.setCourseId(chapter.getCourseId());
            detail.setTotalTime(0);
            detail.setVisitCount(1);
            detail.setLastVisitTime(new Date());
            detail.setCompletionStatus(1);
            detail.setUpdatedAt(new Date());
            
            // 保存学习统计记录
            return learningStatisticsDetailMapper.insert(detail) > 0;
        } else {
            // 标记为已完成
            return learningStatisticsDetailMapper.markAsCompleted(detail.getId()) > 0;
        }
    }
} 