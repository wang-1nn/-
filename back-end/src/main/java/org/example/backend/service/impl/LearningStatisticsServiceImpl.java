package org.example.backend.service.impl;

import org.example.backend.entity.dto.LearningStatisticsDetailDTO;
import org.example.backend.entity.pojo.LearningStatisticsDetail;
import org.example.backend.mapper.CourseChapterMapper;
import org.example.backend.mapper.LearningStatisticsDetailMapper;
import org.example.backend.service.LearningStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学习统计服务实现类
 */
@Service
public class LearningStatisticsServiceImpl implements LearningStatisticsService {
    
    @Autowired
    private LearningStatisticsDetailMapper learningStatisticsDetailMapper;
    
    @Autowired
    private CourseChapterMapper courseChapterMapper;
    
    @Override
    @Transactional
    public boolean recordLearningTime(Long studentId, Long courseId, Long chapterId, Integer duration) {
        // 查询学生的章节学习统计记录
        LearningStatisticsDetail detail = learningStatisticsDetailMapper.findByStudentAndChapter(studentId, chapterId);
        
        if (detail == null) {
            // 如果不存在，则创建新记录
            detail = new LearningStatisticsDetail();
            detail.setStudentId(studentId);
            detail.setCourseId(courseId);
            detail.setChapterId(chapterId);
            detail.setTotalTime(duration);
            detail.setVisitCount(1);
            detail.setLastVisitTime(new Date());
            detail.setCompletionStatus(0);
            detail.setUpdatedAt(new Date());
            
            // 保存学习统计记录
            return learningStatisticsDetailMapper.insert(detail) > 0;
        } else {
            // 更新学习时长
            return learningStatisticsDetailMapper.updateDuration(detail.getId(), duration) > 0;
        }
    }
    
    @Override
    @Transactional
    public boolean recordChapterVisit(Long studentId, Long courseId, Long chapterId) {
        // 查询学生的章节学习统计记录
        LearningStatisticsDetail detail = learningStatisticsDetailMapper.findByStudentAndChapter(studentId, chapterId);
        
        if (detail == null) {
            // 如果不存在，则创建新记录
            detail = new LearningStatisticsDetail();
            detail.setStudentId(studentId);
            detail.setCourseId(courseId);
            detail.setChapterId(chapterId);
            detail.setTotalTime(0);
            detail.setVisitCount(1);
            detail.setLastVisitTime(new Date());
            detail.setCompletionStatus(0);
            detail.setUpdatedAt(new Date());
            
            // 保存学习统计记录
            return learningStatisticsDetailMapper.insert(detail) > 0;
        } else {
            // 更新访问次数
            return learningStatisticsDetailMapper.updateVisitCount(detail.getId()) > 0;
        }
    }
    
    @Override
    @Transactional
    public boolean markChapterAsCompleted(Long studentId, Long chapterId) {
        // 查询学生的章节学习统计记录
        LearningStatisticsDetail detail = learningStatisticsDetailMapper.findByStudentAndChapter(studentId, chapterId);
        
        if (detail == null) {
            return false;
        }
        
        // 标记为已完成
        return learningStatisticsDetailMapper.markAsCompleted(detail.getId()) > 0;
    }
    
    @Override
    public LearningStatisticsDetail getStudentChapterStatistics(Long studentId, Long chapterId) {
        // 查询学生的章节学习统计
        return learningStatisticsDetailMapper.findByStudentAndChapter(studentId, chapterId);
    }
    
    @Override
    public List<LearningStatisticsDetailDTO> getStudentCourseStatistics(Long studentId, Long courseId) {
        // 查询学生在课程中的学习统计详情
        List<LearningStatisticsDetailDTO> statistics = learningStatisticsDetailMapper.getStudentCourseStatistics(studentId, courseId);
        
        // 格式化学习时长
        for (LearningStatisticsDetailDTO stat : statistics) {
            stat.setFormattedDuration(formatLearningTime(stat.getDuration()));
        }
        
        return statistics;
    }
    
    @Override
    public Double getCourseCompletionPercentage(Long studentId, Long courseId) {
        // 获取学生在课程中的学习统计
        List<LearningStatisticsDetailDTO> statistics = getStudentCourseStatistics(studentId, courseId);
        
        if (statistics.isEmpty()) {
            return 0.0;
        }
        
        // 计算已完成的章节数
        long completedChapters = statistics.stream()
                .filter(LearningStatisticsDetailDTO::getIsCompleted)
                .count();
        
        // 计算完成百分比
        return (double) completedChapters / statistics.size() * 100;
    }
    
    @Override
    public Map<String, Object> getCourseStatisticsSummary(Long courseId) {
        // 查询课程的学习统计概要
        List<LearningStatisticsDetailDTO> statistics = learningStatisticsDetailMapper.getCourseStatisticsSummary(courseId);
        
        // 统计数据
        int totalStudents = 0;
        int totalCompletedStudents = 0;
        int totalDuration = 0;
        int totalVisits = 0;
        
        // 如果有统计数据，则计算各项指标
        if (!statistics.isEmpty()) {
            // 学习该课程的总人数
            totalStudents = statistics.size();
            
            // 完成该课程的人数
            totalCompletedStudents = (int) statistics.stream()
                    .filter(LearningStatisticsDetailDTO::getIsCompleted)
                    .count();
            
            // 总学习时长
            totalDuration = statistics.stream()
                    .mapToInt(LearningStatisticsDetailDTO::getDuration)
                    .sum();
            
            // 总访问次数
            totalVisits = statistics.stream()
                    .mapToInt(LearningStatisticsDetailDTO::getVisitCount)
                    .sum();
        }
        
        // 封装结果
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalStudents", totalStudents);
        summary.put("totalCompletedStudents", totalCompletedStudents);
        summary.put("completionRate", totalStudents > 0 ? (double) totalCompletedStudents / totalStudents * 100 : 0);
        summary.put("totalDuration", totalDuration);
        summary.put("formattedTotalDuration", formatLearningTime(totalDuration));
        summary.put("totalVisits", totalVisits);
        summary.put("averageDuration", totalStudents > 0 ? totalDuration / totalStudents : 0);
        summary.put("formattedAverageDuration", formatLearningTime(totalStudents > 0 ? totalDuration / totalStudents : 0));
        summary.put("statistics", statistics);
        
        return summary;
    }
    
    @Override
    public String formatLearningTime(Integer seconds) {
        if (seconds == null || seconds <= 0) {
            return "0分钟";
        }
        
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int remainingSeconds = seconds % 60;
        
        StringBuilder sb = new StringBuilder();
        
        if (hours > 0) {
            sb.append(hours).append("小时");
        }
        
        if (minutes > 0) {
            sb.append(minutes).append("分钟");
        }
        
        if (remainingSeconds > 0 && hours == 0) {
            sb.append(remainingSeconds).append("秒");
        }
        
        return sb.length() > 0 ? sb.toString() : "0分钟";
    }
} 