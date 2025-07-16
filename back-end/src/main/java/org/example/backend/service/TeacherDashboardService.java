package org.example.backend.service;

import org.example.backend.entity.vo.TeacherDashboardVOs;
import org.example.backend.entity.vo.teacher.TeacherDashboardVO;

import java.util.List;

/**
 * 教师仪表盘服务接口
 * 提供教师工作台所需的各种数据服务
 */
public interface TeacherDashboardService {

    /**
     * 获取教师仪表盘概览数据
     * @param teacherId 教师ID
     * @return 概览统计数据
     */
    TeacherDashboardVO.DashboardSummary getDashboardSummary(Long teacherId);

    /**
     * 获取通知公告列表
     * @param teacherId 教师ID
     * @param limit 限制数量
     * @return 通知公告列表
     */
    List<TeacherDashboardVO.Announcement> getAnnouncements(Long teacherId, Integer limit);

    /**
     * 获取待办任务列表
     * @param teacherId 教师ID
     * @param status 任务状态过滤
     * @return 任务列表
     */
    List<TeacherDashboardVO.Task> getTasks(Long teacherId, String status);

    /**
     * 创建新任务
     * @param request 任务创建请求
     * @return 创建的任务信息
     */
    TeacherDashboardVO.Task createTask(TeacherDashboardVO.CreateTaskRequest request);

    /**
     * 更新任务状态
     * @param taskId 任务ID
     * @param request 更新请求
     */
    void updateTaskStatus(Long taskId, TeacherDashboardVO.UpdateTaskRequest request);

    /**
     * 删除任务
     * @param taskId 任务ID
     */
    void deleteTask(Long taskId);

    /**
     * 获取学生活动统计数据
     * @param teacherId 教师ID
     * @param days 统计天数
     * @return 学生活动统计图表数据
     */
    TeacherDashboardVO.ChartData<String, Integer> getStudentActivity(Long teacherId, Integer days);

    /**
     * 获取资源访问统计数据
     * @param teacherId 教师ID
     * @param days 统计天数
     * @return 资源访问统计图表数据
     */
    TeacherDashboardVO.ChartData<String, Integer> getResourceAccess(Long teacherId, Integer days);

    /**
     * 获取课程统计数据
     * @param teacherId 教师ID
     * @return 课程统计数据
     */
    TeacherDashboardVO.CourseStats getCourseStats(Long teacherId);

    /**
     * 获取待批改作业列表
     * @param teacherId 教师ID
     * @param limit 限制数量
     * @return 待批改作业列表
     */
    List<TeacherDashboardVO.PendingGrading> getPendingGrading(Long teacherId, Integer limit);

    /**
     * 获取班级快照数据
     * @param teacherId 教师ID
     * @return 班级快照数据列表
     */
    List<TeacherDashboardVO.ClassSnapshot> getClassSnapshots(Long teacherId);

    /**
     * 获取最近学生表现数据
     * @param teacherId 教师ID
     * @param limit 限制数量
     * @return 学生表现数据
     */
    List<TeacherDashboardVO.StudentPerformance> getRecentStudentPerformance(Long teacherId, Integer limit);

    /**
     * 获取教学进度数据
     * @param teacherId 教师ID
     * @return 教学进度数据
     */
    List<TeacherDashboardVO.TeachingProgress> getTeachingProgress(Long teacherId);

    /**
     * 获取最近活动数据
     * @param teacherId 教师ID
     * @param limit 限制数量
     * @return 最近活动数据
     */
    List<TeacherDashboardVO.RecentActivity> getRecentActivities(Long teacherId, Integer limit);

    /**
     * 标记通知为已读
     * @param announcementId 通知ID
     * @param teacherId 教师ID
     */
    void markAnnouncementAsRead(Long announcementId, Long teacherId);

    /**
     * 获取教师课程列表
     * @param teacherId 教师ID
     * @return 课程列表
     */
    List<TeacherDashboardVO.CourseInfo> getCourses(Long teacherId);

    /**
     * 获取课程学生列表
     * @param teacherId 教师ID
     * @param courseId 课程ID
     * @return 学生列表
     */
    List<TeacherDashboardVO.StudentInfo> getCourseStudents(Long teacherId, Long courseId);

    /**
     * 获取教师作业列表
     * @param teacherId 教师ID
     * @param courseId 课程ID（可选）
     * @return 作业列表
     */
    List<TeacherDashboardVO.AssignmentInfo> getAssignments(Long teacherId, Long courseId);

    /**
     * 获取成绩统计
     * @param teacherId 教师ID
     * @param courseId 课程ID
     * @return 成绩统计数据
     */
    TeacherDashboardVO.GradeStats getGradeStats(Long teacherId, Long courseId);

    /**
     * 发布课程章节
     * @param teacherId 教师ID
     * @param chapterId 章节ID
     */
    void publishChapter(Long teacherId, Long chapterId);

    /**
     * 发布作业
     * @param teacherId 教师ID
     * @param assignmentId 作业ID
     */
    void publishAssignment(Long teacherId, Long assignmentId);

    /**
     * 批改作业
     * @param teacherId 教师ID
     * @param submissionId 提交ID
     * @param grade 分数
     * @param feedback 反馈
     */
    void gradeAssignment(Long teacherId, Long submissionId, Double grade, String feedback);

    /**
     * 获取教学日历数据
     * @param teacherId 教师ID
     * @param month 月份，格式：YYYY-MM
     * @return 教学日历事件列表
     */
    List<TeacherDashboardVOs.CalendarEventVO> getTeachingCalendar(Long teacherId, String month);

    /**
     * 获取工作负载分析
     * @param teacherId 教师ID
     * @return 工作负载数据
     */
    TeacherDashboardVOs.WorkloadVO getWorkload(Long teacherId);

}
