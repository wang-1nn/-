package org.example.backend.service.impl;

import org.example.backend.entity.vo.TeacherDashboardVOs;
import org.example.backend.entity.vo.teacher.TeacherDashboardVO;
import org.example.backend.mapper.TeacherDashboardMapper;
import org.example.backend.service.TeacherDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 教师仪表盘服务实现类
 */
@Service
public class TeacherDashboardServiceImpl implements TeacherDashboardService {

    @Autowired
    private TeacherDashboardMapper teacherDashboardMapper;

    @Override
    public TeacherDashboardVO.DashboardSummary getDashboardSummary(Long teacherId) {
        TeacherDashboardVO.DashboardSummary summary = new TeacherDashboardVO.DashboardSummary();

        // 获取教师姓名
        String teacherName = teacherDashboardMapper.getTeacherName(teacherId);
        summary.setTeacherName(teacherName);

        // 获取课程数量
        Integer courseCount = teacherDashboardMapper.countCoursesByTeacherId(teacherId);
        summary.setCourseCount(courseCount != null ? courseCount : 0);

        // 获取学生总数
        Integer studentCount = teacherDashboardMapper.countStudentsByTeacherId(teacherId);
        summary.setStudentCount(studentCount != null ? studentCount : 0);

        // 获取待批改作业数
        Integer pendingGrading = teacherDashboardMapper.countPendingAssignments(teacherId);
        summary.setPendingGradingCount(pendingGrading != null ? pendingGrading : 0);

        // 获取临近截止任务数
        Integer upcomingDeadlines = teacherDashboardMapper.countUpcomingDeadlines(teacherId);
        summary.setUpcomingDeadlines(upcomingDeadlines != null ? upcomingDeadlines : 0);

        // 获取活跃课程数
        Integer activeCourses = teacherDashboardMapper.countActiveCourses(teacherId);
        summary.setActiveCourses(activeCourses != null ? activeCourses : 0);

        // 获取本周新增学生数
        Integer newStudentsThisWeek = teacherDashboardMapper.countNewStudentsThisWeek(teacherId);
        summary.setNewStudentsThisWeek(newStudentsThisWeek != null ? newStudentsThisWeek : 0);

        // 设置学期信息（简化处理）
        summary.setCurrentWeek(8);
        summary.setTotalWeeks(18);

        return summary;
    }

    @Override
    public List<TeacherDashboardVO.Announcement> getAnnouncements(Long teacherId, Integer limit) {
        return teacherDashboardMapper.getAnnouncementsForTeacher(teacherId, limit);
    }

    @Override
    public List<TeacherDashboardVO.Task> getTasks(Long teacherId, String status) {
        return teacherDashboardMapper.getTasksByTeacherId(teacherId, status);
    }

    @Override
    @Transactional
    public TeacherDashboardVO.Task createTask(TeacherDashboardVO.CreateTaskRequest request) {
        TeacherDashboardVO.Task task = new TeacherDashboardVO.Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setTaskType(request.getTaskType());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
        task.setStatus("PENDING");
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        // 插入数据库并获取生成的ID
        teacherDashboardMapper.insertTask(request.getTeacherId(), task);

        return task;
    }

    @Override
    @Transactional
    public void updateTaskStatus(Long taskId, TeacherDashboardVO.UpdateTaskRequest request) {
        teacherDashboardMapper.updateTaskStatus(taskId, request.getStatus(), request.getDescription(),
                request.getPriority(), request.getDueDate());
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId) {
        teacherDashboardMapper.deleteTask(taskId);
    }

    @Override
    public TeacherDashboardVO.ChartData<String, Integer> getStudentActivity(Long teacherId, Integer days) {
        List<TeacherDashboardVO.StudentActivityData> activityData =
                teacherDashboardMapper.getStudentActivityData(teacherId, days);

        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        // 生成最近N天的日期标签
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(DateTimeFormatter.ofPattern("MM-dd"));
            labels.add(dateStr);

            // 查找对应日期的活动数据
            Integer count = activityData.stream()
                    .filter(data -> data.getDate().equals(date.toString()))
                    .mapToInt(TeacherDashboardVO.StudentActivityData::getCount)
                    .findFirst()
                    .orElse(0);
            values.add(count);
        }

        return new TeacherDashboardVO.ChartData<>(labels, values, "学生活动统计", "line");
    }

    @Override
    public TeacherDashboardVO.ChartData<String, Integer> getResourceAccess(Long teacherId, Integer days) {
        List<TeacherDashboardVO.ResourceAccessData> accessData =
                teacherDashboardMapper.getResourceAccessData(teacherId, days);

        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        for (TeacherDashboardVO.ResourceAccessData data : accessData) {
            labels.add(data.getResourceType());
            values.add(data.getAccessCount());
        }

        return new TeacherDashboardVO.ChartData<>(labels, values, "资源访问统计", "bar");
    }

    @Override
    public TeacherDashboardVO.CourseStats getCourseStats(Long teacherId) {
        return teacherDashboardMapper.getCourseStats(teacherId);
    }

    @Override
    public List<TeacherDashboardVO.PendingGrading> getPendingGrading(Long teacherId, Integer limit) {
        return teacherDashboardMapper.getPendingGrading(teacherId, limit);
    }

    @Override
    public List<TeacherDashboardVO.ClassSnapshot> getClassSnapshots(Long teacherId) {
        return teacherDashboardMapper.getClassSnapshots(teacherId);
    }

    @Override
    public List<TeacherDashboardVO.StudentPerformance> getRecentStudentPerformance(Long teacherId, Integer limit) {
        return teacherDashboardMapper.getRecentStudentPerformance(teacherId, limit);
    }

    @Override
    public List<TeacherDashboardVO.TeachingProgress> getTeachingProgress(Long teacherId) {
        return teacherDashboardMapper.getTeachingProgress(teacherId);
    }

    @Override
    public List<TeacherDashboardVO.RecentActivity> getRecentActivities(Long teacherId, Integer limit) {
        return teacherDashboardMapper.getRecentActivities(teacherId, limit);
    }

    @Override
    @Transactional
    public void markAnnouncementAsRead(Long announcementId, Long teacherId) {
        teacherDashboardMapper.markAnnouncementAsRead(announcementId, teacherId);
    }

    @Override
    public List<TeacherDashboardVO.CourseInfo> getCourses(Long teacherId) {
        return teacherDashboardMapper.getCoursesByTeacherId(teacherId);
    }

    @Override
    public List<TeacherDashboardVO.StudentInfo> getCourseStudents(Long teacherId, Long courseId) {
        // 验证教师权限
        if (!teacherDashboardMapper.isCourseOwner(teacherId, courseId)) {
            throw new RuntimeException("无权限访问该课程");
        }
        return teacherDashboardMapper.getStudentsByCourse(courseId);
    }

    @Override
    public List<TeacherDashboardVO.AssignmentInfo> getAssignments(Long teacherId, Long courseId) {
        if (courseId != null) {
            // 验证教师权限
            if (!teacherDashboardMapper.isCourseOwner(teacherId, courseId)) {
                throw new RuntimeException("无权限访问该课程");
            }
            return teacherDashboardMapper.getAssignmentsByCourse(courseId);
        } else {
            return teacherDashboardMapper.getAssignmentsByTeacher(teacherId);
        }
    }

    @Override
    public TeacherDashboardVO.GradeStats getGradeStats(Long teacherId, Long courseId) {
        // 验证教师权限
        if (!teacherDashboardMapper.isCourseOwner(teacherId, courseId)) {
            throw new RuntimeException("无权限访问该课程");
        }
        return teacherDashboardMapper.getGradeStatsByCourse(courseId);
    }

    @Override
    @Transactional
    public void publishChapter(Long teacherId, Long chapterId) {
        // 验证教师权限
        if (!teacherDashboardMapper.isChapterOwner(teacherId, chapterId)) {
            throw new RuntimeException("无权限操作该章节");
        }

        // 发布章节
        teacherDashboardMapper.publishChapter(chapterId);

        // 创建通知任务
        createChapterPublishNotification(teacherId, chapterId);
    }

    @Override
    @Transactional
    public void publishAssignment(Long teacherId, Long assignmentId) {
        // 验证教师权限
        if (!teacherDashboardMapper.isAssignmentOwner(teacherId, assignmentId)) {
            throw new RuntimeException("无权限操作该作业");
        }

        // 发布作业
        teacherDashboardMapper.publishAssignment(assignmentId);

        // 创建学生作业提交记录
        createAssignmentSubmissions(assignmentId);

        // 创建通知任务
        createAssignmentPublishNotification(teacherId, assignmentId);
    }

    @Override
    @Transactional
    public void gradeAssignment(Long teacherId, Long submissionId, Double grade, String feedback) {
        // 验证教师权限
        if (!teacherDashboardMapper.isSubmissionGradable(teacherId, submissionId)) {
            throw new RuntimeException("无权限批改该作业");
        }

        // 批改作业
        teacherDashboardMapper.gradeSubmission(submissionId, grade, feedback, teacherId);

        // 记录成绩
        recordGrade(submissionId, grade, teacherId);
    }

    // 私有辅助方法
    private void createChapterPublishNotification(Long teacherId, Long chapterId) {
        // 创建章节发布通知逻辑
        // 这里可以发送通知给学生
    }

    private void createAssignmentSubmissions(Long assignmentId) {
        // 为所有选课学生创建作业提交记录
        teacherDashboardMapper.createAssignmentSubmissions(assignmentId);
    }

    private void createAssignmentPublishNotification(Long teacherId, Long assignmentId) {
        // 创建作业发布通知逻辑
    }

    private void recordGrade(Long submissionId, Double grade, Long teacherId) {
        // 记录成绩到成绩表
        teacherDashboardMapper.recordGrade(submissionId, grade, teacherId);
    }

    @Override
    public List<TeacherDashboardVOs.CalendarEventVO> getTeachingCalendar(Long teacherId, String month) {
        // 解析月份参数
        String[] parts = month.split("-");
        int year = Integer.parseInt(parts[0]);
        int monthValue = Integer.parseInt(parts[1]);

        // 获取该月的教学日历事件
        return teacherDashboardMapper.getTeachingCalendar(teacherId, year, monthValue);
    }

    @Override
    public TeacherDashboardVOs.WorkloadVO getWorkload(Long teacherId) {
        TeacherDashboardVOs.WorkloadVO workload = new TeacherDashboardVOs.WorkloadVO();

        // 获取基础统计数据
        Integer totalCourses = teacherDashboardMapper.countCoursesByTeacherId(teacherId);
        Integer totalStudents = teacherDashboardMapper.countStudentsByTeacherId(teacherId);
        Integer pendingTasks = teacherDashboardMapper.countPendingTasksByTeacherId(teacherId);
        Integer upcomingDeadlines = teacherDashboardMapper.countUpcomingDeadlines(teacherId);

        workload.setTotalCourses(totalCourses);
        workload.setTotalStudents(totalStudents);
        workload.setPendingTasks(pendingTasks);
        workload.setUpcomingDeadlines(upcomingDeadlines);

        // 计算工作负载评分 (简单算法)
        double score = Math.min(100, (totalCourses * 10 + totalStudents * 0.5 + pendingTasks * 5 + upcomingDeadlines * 3));
        workload.setWorkloadScore(score);

        // 设置工作负载等级
        if (score < 30) {
            workload.setWorkloadLevel("low");
        } else if (score < 70) {
            workload.setWorkloadLevel("medium");
        } else {
            workload.setWorkloadLevel("high");
        }

        // 提供建议
        List<String> suggestions = new ArrayList<>();
        if (pendingTasks > 10) {
            suggestions.add("建议优先处理待办任务");
        }
        if (upcomingDeadlines > 5) {
            suggestions.add("注意即将到期的任务");
        }
        if (totalCourses > 5) {
            suggestions.add("考虑合理分配教学时间");
        }
        workload.setSuggestions(suggestions);

        return workload;
    }
}
