package org.example.backend.service;

import org.example.backend.entity.vo.teacher.TeacherDashboardVO;

import java.util.List;

/**
 * 教师端AI服务接口
 * 提供基于AI的智能分析和建议功能
 */
public interface TeacherAIService {

    /**
     * 生成智能日程安排
     * @param teacherId 教师ID
     * @return 智能日程列表
     */
    List<TeacherDashboardVO.ScheduleItem> generateSmartSchedule(Long teacherId);

    /**
     * 分析学生表现亮点
     * @param teacherId 教师ID
     * @return 学生表现亮点列表
     */
    List<TeacherDashboardVO.StudentHighlight> analyzeStudentHighlights(Long teacherId);

    /**
     * 分析学生潜在风险
     * @param teacherId 教师ID
     * @return 学生风险预警列表
     */
    List<TeacherDashboardVO.StudentRisk> analyzeStudentRisks(Long teacherId);

    /**
     * 获取互动中心数据
     * @param teacherId 教师ID
     * @return 互动数据列表
     */
    List<TeacherDashboardVO.InteractionItem> getInteractionHub(Long teacherId);

    /**
     * 生成教学建议
     * @param teacherId 教师ID
     * @param courseId 课程ID
     * @return AI生成的教学建议
     */
    String generateTeachingSuggestions(Long teacherId, Long courseId);

    /**
     * 分析班级整体表现
     * @param teacherId 教师ID
     * @param courseId 课程ID
     * @return AI分析的班级表现报告
     */
    String analyzeClassPerformance(Long teacherId, Long courseId);

    /**
     * 生成个性化学习建议
     * @param teacherId 教师ID
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return AI生成的个性化学习建议
     */
    String generatePersonalizedAdvice(Long teacherId, Long studentId, Long courseId);

    /**
     * 预测学生学习趋势
     * @param teacherId 教师ID
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return AI预测的学习趋势分析
     */
    String predictLearningTrend(Long teacherId, Long studentId, Long courseId);

    /**
     * 生成作业批改建议
     * @param teacherId 教师ID
     * @param submissionId 作业提交ID
     * @return AI生成的批改建议
     */
    String generateGradingSuggestions(Long teacherId, Long submissionId);
}
