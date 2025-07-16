package org.example.backend.service.impl;

import org.example.backend.entity.vo.teacher.TeacherDashboardVO;
import org.example.backend.mapper.TeacherDashboardMapper;
import org.example.backend.mapper.TeacherStudentSyncMapper;
import org.example.backend.service.TeacherStudentSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 教师端与学生端联动服务实现类
 */
@Service
public class TeacherStudentSyncServiceImpl implements TeacherStudentSyncService {

    @Autowired
    private TeacherStudentSyncMapper syncMapper;

    @Autowired
    private TeacherDashboardMapper dashboardMapper;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void publishChapterAndNotify(Long teacherId, Long chapterId) {
        // 验证教师权限
        if (!dashboardMapper.isChapterOwner(teacherId, chapterId)) {
            throw new RuntimeException("无权限操作该章节");
        }

        // 1. 发布章节
        dashboardMapper.publishChapter(chapterId);

        // 2. 获取相关课程和学生
        Long courseId = syncMapper.getCourseIdByChapter(chapterId);
        List<Long> studentIds = syncMapper.getStudentIdsByCourse(courseId);

        // 3. 创建学生进度记录
        syncMapper.initStudentProgressForChapter(chapterId, studentIds);

        // 4. 创建通知
        String chapterTitle = syncMapper.getChapterTitle(chapterId);
        String courseTitle = syncMapper.getCourseTitle(courseId);
        String notificationTitle = "新章节发布: " + chapterTitle;
        String notificationContent = "课程《" + courseTitle + "》发布了新章节《" + chapterTitle + "》，请及时学习。";

        syncMapper.createAnnouncement(notificationTitle, notificationContent, "course",
                "students", courseId, teacherId, false);

        // 5. 更新课程章节计数
        syncMapper.updateCourseChapterCount(courseId);

        // 6. 发布事件通知前端
        // 这里可以使用WebSocket或其他实时通知机制
        // eventPublisher.publishEvent(new ChapterPublishedEvent(chapterId, courseId, studentIds));
    }

    @Override
    @Transactional
    public void publishAssignmentAndNotify(Long teacherId, Long assignmentId) {
        // 验证教师权限
        if (!dashboardMapper.isAssignmentOwner(teacherId, assignmentId)) {
            throw new RuntimeException("无权限操作该作业");
        }

        // 1. 发布作业
        dashboardMapper.publishAssignment(assignmentId);

        // 2. 获取相关课程和学生
        Long courseId = syncMapper.getCourseIdByAssignment(assignmentId);
        List<Long> studentIds = syncMapper.getStudentIdsByCourse(courseId);

        // 3. 创建学生提交记录
        dashboardMapper.createAssignmentSubmissions(assignmentId);

        // 4. 创建通知
        String assignmentTitle = syncMapper.getAssignmentTitle(assignmentId);
        String courseTitle = syncMapper.getCourseTitle(courseId);
        LocalDateTime dueDate = syncMapper.getAssignmentDueDate(assignmentId);

        String notificationTitle = "新作业发布: " + assignmentTitle;
        String notificationContent = "课程《" + courseTitle + "》发布了新作业《" + assignmentTitle +
                "》，截止日期: " + dueDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) +
                "，请及时完成。";

        syncMapper.createAnnouncement(notificationTitle, notificationContent, "course",
                "students", courseId, teacherId, true);

        // 5. 创建截止日期提醒任务
        syncMapper.createDeadlineReminder(assignmentId, dueDate);

        // 6. 发布事件通知前端
        // eventPublisher.publishEvent(new AssignmentPublishedEvent(assignmentId, courseId, studentIds));
    }

    @Override
    @Transactional
    public void gradeAssignmentAndNotify(Long teacherId, Long submissionId, Double grade, String feedback) {
        // 验证教师权限
        if (!dashboardMapper.isSubmissionGradable(teacherId, submissionId)) {
            throw new RuntimeException("无权限批改该作业");
        }

        // 1. 批改作业
        dashboardMapper.gradeSubmission(submissionId, grade, feedback, teacherId);

        // 2. 记录成绩
        dashboardMapper.recordGrade(submissionId, grade, teacherId);

        // 3. 获取相关信息
        Long studentId = syncMapper.getStudentIdBySubmission(submissionId);
        Long assignmentId = syncMapper.getAssignmentIdBySubmission(submissionId);
        String assignmentTitle = syncMapper.getAssignmentTitle(assignmentId);

        // 4. 创建通知
        String notificationTitle = "作业已批改: " + assignmentTitle;
        String notificationContent = "您的作业《" + assignmentTitle + "》已批改，得分: " + grade +
                "，教师反馈: " + (feedback != null ? feedback : "无");

        syncMapper.createPersonalAnnouncement(notificationTitle, notificationContent,
                "assignment", studentId, teacherId, false);

        // 5. 发布事件通知前端
        // eventPublisher.publishEvent(new AssignmentGradedEvent(submissionId, studentId, grade));
    }

    @Override
    @Transactional
    public void publishExamAndNotify(Long teacherId, Long examId) {
        // 验证教师权限
        if (!syncMapper.isExamOwner(teacherId, examId)) {
            throw new RuntimeException("无权限操作该考试");
        }

        // 1. 发布考试
        syncMapper.publishExam(examId);

        // 2. 获取相关课程和学生
        Long courseId = syncMapper.getCourseIdByExam(examId);
        List<Long> studentIds = syncMapper.getStudentIdsByCourse(courseId);

        // 3. 创建通知
        String examTitle = syncMapper.getExamTitle(examId);
        String courseTitle = syncMapper.getCourseTitle(courseId);
        LocalDateTime startTime = syncMapper.getExamStartTime(examId);
        LocalDateTime endTime = syncMapper.getExamEndTime(examId);

        String notificationTitle = "新考试发布: " + examTitle;
        String notificationContent = "课程《" + courseTitle + "》发布了新考试《" + examTitle +
                "》，开始时间: " + startTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) +
                "，结束时间: " + endTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) +
                "，请做好准备。";

        syncMapper.createAnnouncement(notificationTitle, notificationContent, "course",
                "students", courseId, teacherId, true);

        // 4. 发布事件通知前端
        // eventPublisher.publishEvent(new ExamPublishedEvent(examId, courseId, studentIds));
    }

    @Override
    @Transactional
    public void publishAnnouncement(Long teacherId, String title, String content, String type,
                                    String targetAudience, Long courseId, Boolean isImportant) {
        // 验证教师权限
        if (courseId != null && !dashboardMapper.isCourseOwner(teacherId, courseId)) {
            throw new RuntimeException("无权限为该课程发布公告");
        }

        // 创建公告
        syncMapper.createAnnouncement(title, content, type, targetAudience, courseId, teacherId, isImportant);

        // 发布事件通知前端
        // eventPublisher.publishEvent(new AnnouncementPublishedEvent(title, targetAudience, courseId));
    }

    @Override
    @Transactional
    public void recordLearningBehavior(Long studentId, Long courseId, Long chapterId,
                                       String behaviorType, Integer duration) {
        // 1. 记录学习行为
        syncMapper.recordLearningBehavior(studentId, courseId, chapterId, behaviorType, duration);

        // 2. 更新学习时长
        syncMapper.updateStudyTime(studentId, courseId, chapterId, duration);

        // 3. 获取教师ID
        Long teacherId = syncMapper.getTeacherIdByCourse(courseId);

        // 4. 更新教师统计数据缓存（可选）
        // cacheService.refreshTeacherStats(teacherId);
    }

    @Override
    @Transactional
    public void updateStudentProgress(Long studentId, Long courseId, Long chapterId, Double progressPercentage) {
        // 1. 更新学习进度
        syncMapper.updateStudentProgress(studentId, courseId, chapterId, progressPercentage);

        // 2. 更新课程总进度
        syncMapper.updateCourseProgress(studentId, courseId);

        // 3. 检查是否需要学习预警
        checkStudentLearningAlert(studentId, courseId);
    }

    @Override
    @Transactional
    public void submitAssignmentAndNotify(Long studentId, Long assignmentId, String content, String[] fileUrls) {
        // 1. 提交作业
        Long submissionId = syncMapper.submitAssignment(studentId, assignmentId, content, fileUrls);

        // 2. 获取相关信息
        Long teacherId = syncMapper.getTeacherIdByAssignment(assignmentId);
        String assignmentTitle = syncMapper.getAssignmentTitle(assignmentId);
        String studentName = syncMapper.getStudentName(studentId);

        // 3. 创建教师待办任务
        String taskTitle = "批改作业: " + assignmentTitle;
        String taskDescription = "学生 " + studentName + " 提交了作业《" + assignmentTitle + "》，请及时批改。";

        syncMapper.createTeacherTask(teacherId, "GRADING", taskTitle, taskDescription, "medium",
                LocalDateTime.now().plusDays(3));

        // 4. 发布事件通知前端
        // eventPublisher.publishEvent(new AssignmentSubmittedEvent(submissionId, teacherId));
    }

    @Override
    @Transactional
    public void submitExamAndNotify(Long studentId, Long examId, String answers) {
        // 1. 提交考试
        Long submissionId = syncMapper.submitExam(studentId, examId, answers);

        // 2. 获取相关信息
        Long teacherId = syncMapper.getTeacherIdByExam(examId);
        String examTitle = syncMapper.getExamTitle(examId);
        String studentName = syncMapper.getStudentName(studentId);

        // 3. 创建教师待办任务（如果需要人工批改）
        if (!syncMapper.isExamAutoGraded(examId)) {
            String taskTitle = "批改考试: " + examTitle;
            String taskDescription = "学生 " + studentName + " 提交了考试《" + examTitle + "》，请及时批改。";

            syncMapper.createTeacherTask(teacherId, "GRADING", taskTitle, taskDescription, "high",
                    LocalDateTime.now().plusDays(2));
        }

        // 4. 发布事件通知前端
        // eventPublisher.publishEvent(new ExamSubmittedEvent(submissionId, teacherId));
    }

    @Override
    public void checkStudentLearningAlert(Long studentId, Long courseId) {
        // 1. 获取学生进度
        Double progress = syncMapper.getStudentCourseProgress(studentId, courseId);
        LocalDateTime lastAccessTime = syncMapper.getStudentLastAccessTime(studentId, courseId);

        // 2. 获取教师ID
        Long teacherId = syncMapper.getTeacherIdByCourse(courseId);

        // 3. 检查是否需要预警
        boolean needAlert = false;
        String alertReason = "";

        // 进度过低预警
        if (progress != null && progress < 30.0) {
            needAlert = true;
            alertReason = "学习进度过低";
        }

        // 长时间未学习预警
        if (lastAccessTime != null && lastAccessTime.isBefore(LocalDateTime.now().minusDays(7))) {
            needAlert = true;
            alertReason += (alertReason.isEmpty() ? "" : "，") + "长时间未学习";
        }

        // 4. 创建预警通知
        if (needAlert) {
            String studentName = syncMapper.getStudentName(studentId);
            String courseTitle = syncMapper.getCourseTitle(courseId);

            String taskTitle = "学生学习预警: " + studentName;
            String taskDescription = "学生 " + studentName + " 在课程《" + courseTitle + "》中" + alertReason + "，请关注。";

            syncMapper.createTeacherTask(teacherId, "ALERT", taskTitle, taskDescription, "medium",
                    LocalDateTime.now().plusDays(1));
        }
    }

    @Override
    public TeacherDashboardVO.ChartData<String, Integer> getStudentActivityStats(Long teacherId, Long courseId, Integer days) {
        // 验证教师权限
        if (!dashboardMapper.isCourseOwner(teacherId, courseId)) {
            throw new RuntimeException("无权限访问该课程数据");
        }

        // 获取学生活动统计
        List<TeacherDashboardVO.StudentActivityData> activityData =
                syncMapper.getStudentActivityStatsByCourse(courseId, days);

        // 转换为图表数据
        List<String> labels = new java.util.ArrayList<>();
        List<Integer> values = new java.util.ArrayList<>();

        for (TeacherDashboardVO.StudentActivityData data : activityData) {
            labels.add(data.getDate());
            values.add(data.getCount());
        }

        return new TeacherDashboardVO.ChartData<>(labels, values, "学生活动统计", "line");
    }

    @Override
    public TeacherDashboardVO.ChartData<String, Integer> getStudentProgressStats(Long teacherId, Long courseId) {
        // 验证教师权限
        if (!dashboardMapper.isCourseOwner(teacherId, courseId)) {
            throw new RuntimeException("无权限访问该课程数据");
        }

        // 获取学生进度分布
        int[] progressRanges = syncMapper.getStudentProgressDistribution(courseId);

        // 转换为图表数据
        List<String> labels = java.util.Arrays.asList("0-20%", "21-40%", "41-60%", "61-80%", "81-100%");
        List<Integer> values = new java.util.ArrayList<>();

        for (int count : progressRanges) {
            values.add(count);
        }

        return new TeacherDashboardVO.ChartData<>(labels, values, "学生进度分布", "bar");
    }

    @Override
    public TeacherDashboardVO.GradeStats getStudentGradeStats(Long teacherId, Long courseId) {
        // 验证教师权限
        if (!dashboardMapper.isCourseOwner(teacherId, courseId)) {
            throw new RuntimeException("无权限访问该课程数据");
        }

        // 获取成绩统计
        return dashboardMapper.getGradeStatsByCourse(courseId);
    }
}
