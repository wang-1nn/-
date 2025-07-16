package org.example.backend.service;

import org.example.backend.entity.vo.teacher.TeacherDashboardVO;

/**
 * 教师端与学生端联动服务接口
 * 处理教师操作对学生端的影响，以及学生行为对教师端的反馈
 */
public interface TeacherStudentSyncService {

    /**
     * 发布课程章节并通知学生
     * @param teacherId 教师ID
     * @param chapterId 章节ID
     */
    void publishChapterAndNotify(Long teacherId, Long chapterId);

    /**
     * 发布作业并通知学生
     * @param teacherId 教师ID
     * @param assignmentId 作业ID
     */
    void publishAssignmentAndNotify(Long teacherId, Long assignmentId);

    /**
     * 批改作业并通知学生
     * @param teacherId 教师ID
     * @param submissionId 提交ID
     * @param grade 分数
     * @param feedback 反馈
     */
    void gradeAssignmentAndNotify(Long teacherId, Long submissionId, Double grade, String feedback);

    /**
     * 发布考试并通知学生
     * @param teacherId 教师ID
     * @param examId 考试ID
     */
    void publishExamAndNotify(Long teacherId, Long examId);

    /**
     * 发布通知公告
     * @param teacherId 教师ID
     * @param title 标题
     * @param content 内容
     * @param type 类型
     * @param targetAudience 目标受众
     * @param courseId 课程ID（可选）
     * @param isImportant 是否重要
     */
    void publishAnnouncement(Long teacherId, String title, String content, String type,
                             String targetAudience, Long courseId, Boolean isImportant);

    /**
     * 记录学生学习行为并更新教师统计数据
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param chapterId 章节ID
     * @param behaviorType 行为类型
     * @param duration 持续时间（秒）
     */
    void recordLearningBehavior(Long studentId, Long courseId, Long chapterId,
                                String behaviorType, Integer duration);

    /**
     * 更新学生学习进度并通知教师
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param chapterId 章节ID
     * @param progressPercentage 完成百分比
     */
    void updateStudentProgress(Long studentId, Long courseId, Long chapterId, Double progressPercentage);

    /**
     * 学生提交作业并通知教师
     * @param studentId 学生ID
     * @param assignmentId 作业ID
     * @param content 内容
     * @param fileUrls 文件URL列表
     */
    void submitAssignmentAndNotify(Long studentId, Long assignmentId, String content, String[] fileUrls);

    /**
     * 学生提交考试并通知教师
     * @param studentId 学生ID
     * @param examId 考试ID
     * @param answers 答案内容
     */
    void submitExamAndNotify(Long studentId, Long examId, String answers);

    /**
     * 检查学生学习预警并通知教师
     * @param studentId 学生ID
     * @param courseId 课程ID
     */
    void checkStudentLearningAlert(Long studentId, Long courseId);

    /**
     * 获取教师课程的学生活动统计
     * @param teacherId 教师ID
     * @param courseId 课程ID
     * @param days 天数
     * @return 学生活动统计
     */
    TeacherDashboardVO.ChartData<String, Integer> getStudentActivityStats(Long teacherId, Long courseId, Integer days);

    /**
     * 获取教师课程的学生进度统计
     * @param teacherId 教师ID
     * @param courseId 课程ID
     * @return 学生进度统计
     */
    TeacherDashboardVO.ChartData<String, Integer> getStudentProgressStats(Long teacherId, Long courseId);

    /**
     * 获取教师课程的学生成绩统计
     * @param teacherId 教师ID
     * @param courseId 课程ID
     * @return 学生成绩统计
     */
    TeacherDashboardVO.GradeStats getStudentGradeStats(Long teacherId, Long courseId);
}
