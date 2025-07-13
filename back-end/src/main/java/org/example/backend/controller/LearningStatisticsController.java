package org.example.backend.controller;

import org.example.backend.entity.dto.LearningStatisticsDetailDTO;
import org.example.backend.entity.pojo.LearningStatisticsDetail;
import org.example.backend.service.LearningStatisticsService;
import org.example.backend.util.RestBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 学习统计控制器
 */
@RestController
@RequestMapping("/api/statistics")
public class LearningStatisticsController {
    
    @Autowired
    private LearningStatisticsService learningStatisticsService;
    
    /**
     * 记录学习时长
     * 
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param chapterId 章节ID
     * @param duration 学习时长（秒）
     * @return 结果
     */
    @PostMapping("/learning/time")
    public RestBean<Void> recordLearningTime(
            @RequestParam Long studentId,
            @RequestParam Long courseId,
            @RequestParam Long chapterId,
            @RequestParam Integer duration) {
        boolean success = learningStatisticsService.recordLearningTime(studentId, courseId, chapterId, duration);
        return success ? RestBean.success("记录学习时长成功") : RestBean.failure(400, "记录学习时长失败");
    }
    
    /**
     * 记录章节访问
     * 
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param chapterId 章节ID
     * @return 结果
     */
    @PostMapping("/learning/visit")
    public RestBean<Void> recordChapterVisit(
            @RequestParam Long studentId,
            @RequestParam Long courseId,
            @RequestParam Long chapterId) {
        boolean success = learningStatisticsService.recordChapterVisit(studentId, courseId, chapterId);
        return success ? RestBean.success("记录章节访问成功") : RestBean.failure(400, "记录章节访问失败");
    }
    
    /**
     * 标记章节为已完成
     * 
     * @param studentId 学生ID
     * @param chapterId 章节ID
     * @return 结果
     */
    @PostMapping("/learning/complete")
    public RestBean<Void> markChapterAsCompleted(
            @RequestParam Long studentId, @RequestParam Long chapterId) {
        boolean success = learningStatisticsService.markChapterAsCompleted(studentId, chapterId);
        return success ? RestBean.success("标记章节完成成功") : RestBean.failure(400, "标记章节完成失败");
    }
    
    /**
     * 获取学生的章节学习统计
     * 
     * @param studentId 学生ID
     * @param chapterId 章节ID
     * @return 学习统计详情
     */
    @GetMapping("/student/chapter")
    public RestBean<LearningStatisticsDetail> getStudentChapterStatistics(
            @RequestParam Long studentId, @RequestParam Long chapterId) {
        LearningStatisticsDetail statistics = learningStatisticsService.getStudentChapterStatistics(studentId, chapterId);
        return statistics != null ? RestBean.success("获取成功", statistics) : RestBean.success("未找到学习统计", null);
    }
    
    /**
     * 获取学生在课程中的学习统计
     * 
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 学习统计详情DTO列表
     */
    @GetMapping("/student/course")
    public RestBean<List<LearningStatisticsDetailDTO>> getStudentCourseStatistics(
            @RequestParam Long studentId, @RequestParam Long courseId) {
        List<LearningStatisticsDetailDTO> statistics = learningStatisticsService.getStudentCourseStatistics(studentId, courseId);
        return RestBean.success("获取成功", statistics);
    }
    
    /**
     * 获取学生的课程完成进度百分比
     * 
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 完成百分比
     */
    @GetMapping("/student/completion")
    public RestBean<Double> getCourseCompletionPercentage(
            @RequestParam Long studentId, @RequestParam Long courseId) {
        Double percentage = learningStatisticsService.getCourseCompletionPercentage(studentId, courseId);
        return RestBean.success("获取成功", percentage);
    }
    
    /**
     * 获取课程的学习统计概要
     * 
     * @param courseId 课程ID
     * @return 学习统计概要
     */
    @GetMapping("/course/{courseId}")
    public RestBean<Map<String, Object>> getCourseStatisticsSummary(@PathVariable Long courseId) {
        Map<String, Object> summary = learningStatisticsService.getCourseStatisticsSummary(courseId);
        return RestBean.success("获取成功", summary);
    }
    
    /**
     * 获取学习时间的格式化表示
     * 
     * @param seconds 学习时间（秒）
     * @return 格式化的时间字符串
     */
    @GetMapping("/format-time")
    public RestBean<String> formatLearningTime(@RequestParam Integer seconds) {
        String formattedTime = learningStatisticsService.formatLearningTime(seconds);
        return RestBean.success("获取成功", formattedTime);
    }
} 