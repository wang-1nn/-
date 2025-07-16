package org.example.backend.mapper;

import org.apache.ibatis.annotations.*;
import org.example.backend.entity.vo.TeacherDashboardVOs;
import org.example.backend.entity.vo.teacher.TeacherDashboardVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 教师仪表盘数据访问接口
 */
@Mapper
public interface TeacherDashboardMapper {

    // ========== 概览统计相关 ==========

    /**
     * 获取教师姓名
     */
    @Select("SELECT real_name FROM users WHERE user_id = #{teacherId}")
    String getTeacherName(@Param("teacherId") Long teacherId);

    /**
     * 统计教师课程数量
     */
    @Select("SELECT COUNT(*) FROM courses WHERE teacher_id = #{teacherId}")
    Integer countCoursesByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * 统计教师学生总数（去重）
     */
    @Select("SELECT COUNT(DISTINCT e.student_id) FROM courses c " +
            "JOIN course_offerings co ON c.id = co.course_id " +
            "JOIN enrollments e ON co.id = e.offering_id " +
            "WHERE c.teacher_id = #{teacherId}")
    Integer countStudentsByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * 统计待批改作业数
     */
    @Select("SELECT COUNT(*) FROM assignments a " +
            "JOIN assignment_submissions s ON a.id = s.assignment_id " +
            "WHERE a.teacher_id = #{teacherId} AND s.status = 'submitted' AND s.grade IS NULL")
    Integer countPendingAssignments(@Param("teacherId") Long teacherId);

    /**
     * 统计临近截止任务数（3天内）
     */
    @Select("SELECT COUNT(*) FROM todo_tasks " +
            "WHERE user_id = #{teacherId} AND status = 'PENDING' " +
            "AND due_date BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 3 DAY)")
    Integer countUpcomingDeadlines(@Param("teacherId") Long teacherId);

    /**
     * 统计活跃课程数
     */
    @Select("SELECT COUNT(*) FROM courses c " +
            "WHERE c.teacher_id = #{teacherId} AND c.is_published = 1 " +
            "AND EXISTS (SELECT 1 FROM course_offerings co " +
            "            JOIN enrollments e ON co.id = e.offering_id " +
            "            WHERE co.course_id = c.id)")
    Integer countActiveCourses(@Param("teacherId") Long teacherId);

    /**
     * 统计本周新增学生数
     */
    @Select("SELECT COUNT(DISTINCT e.student_id) FROM courses c " +
            "JOIN course_offerings co ON c.id = co.course_id " +
            "JOIN enrollments e ON co.id = e.offering_id " +
            "WHERE c.teacher_id = #{teacherId} " +
            "AND e.id >= (SELECT COALESCE(MAX(id), 0) FROM enrollments WHERE id <= " +
            "  (SELECT COALESCE(MAX(id), 0) - 50 FROM enrollments))")
    Integer countNewStudentsThisWeek(@Param("teacherId") Long teacherId);

    // ========== 通知公告相关 ==========

    /**
     * 获取教师相关的通知公告
     */
    @Select("SELECT a.id, a.title, a.content, a.type, a.is_important, " +
            "a.publish_time, a.expire_time, a.status, " +
            "CASE WHEN a.course_id IS NOT NULL THEN c.title ELSE '系统通知' END as course, " +
            "CASE WHEN nr.id IS NOT NULL THEN 1 ELSE 0 END as isRead " +
            "FROM announcements a " +
            "LEFT JOIN courses c ON a.course_id = c.id " +
            "LEFT JOIN notification_reads nr ON a.id = nr.announcement_id AND nr.user_id = #{teacherId} " +
            "WHERE (a.target_audience IN ('all', 'teachers') " +
            "   OR (a.target_audience = 'course' AND c.teacher_id = #{teacherId})) " +
            "AND a.status = 'published' " +
            "AND (a.expire_time IS NULL OR a.expire_time > NOW()) " +
            "ORDER BY a.is_important DESC, a.publish_time DESC " +
            "LIMIT #{limit}")
    List<TeacherDashboardVO.Announcement> getAnnouncementsForTeacher(@Param("teacherId") Long teacherId,
                                                                     @Param("limit") Integer limit);

    // ========== 任务管理相关 ==========

    /**
     * 获取教师任务列表
     */
    @Select("<script>" +
            "SELECT id, title, description, task_type, priority, due_date, status, created_at, updated_at " +
            "FROM todo_tasks WHERE user_id = #{teacherId} " +
            "<if test='status != \"all\"'>" +
            "AND status = UPPER(#{status}) " +
            "</if>" +
            "ORDER BY " +
            "CASE priority " +
            "  WHEN 'high' THEN 1 " +
            "  WHEN 'medium' THEN 2 " +
            "  WHEN 'low' THEN 3 " +
            "  ELSE 4 " +
            "END, due_date ASC" +
            "</script>")
    List<TeacherDashboardVO.Task> getTasksByTeacherId(@Param("teacherId") Long teacherId,
                                                      @Param("status") String status);

    /**
     * 插入新任务
     */
    @Insert("INSERT INTO todo_tasks (user_id, title, description, task_type, priority, due_date, status, created_at, updated_at) " +
            "VALUES (#{teacherId}, #{task.title}, #{task.description}, #{task.taskType}, #{task.priority}, " +
            "#{task.dueDate}, #{task.status}, #{task.createdAt}, #{task.updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "task.id")
    void insertTask(@Param("teacherId") Long teacherId, @Param("task") TeacherDashboardVO.Task task);

    /**
     * 更新任务状态
     */
    @Update("UPDATE todo_tasks SET status = #{status}, " +
            "description = COALESCE(#{description}, description), " +
            "priority = COALESCE(#{priority}, priority), " +
            "due_date = COALESCE(#{dueDate}, due_date), " +
            "updated_at = NOW() " +
            "WHERE id = #{taskId}")
    void updateTaskStatus(@Param("taskId") Long taskId,
                          @Param("status") String status,
                          @Param("description") String description,
                          @Param("priority") String priority,
                          @Param("dueDate") LocalDateTime dueDate);

    /**
     * 删除任务
     */
    @Delete("DELETE FROM todo_tasks WHERE id = #{taskId}")
    void deleteTask(@Param("taskId") Long taskId);

    // ========== 统计图表相关 ==========

    /**
     * 获取学生活动统计数据
     */
    @Select("SELECT DATE(lb.behavior_time) as date, COUNT(*) as count " +
            "FROM learning_behaviors lb " +
            "JOIN courses c ON lb.course_id = c.id " +
            "WHERE c.teacher_id = #{teacherId} " +
            "AND lb.behavior_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(lb.behavior_time) " +
            "ORDER BY date")
    List<TeacherDashboardVO.StudentActivityData> getStudentActivityData(@Param("teacherId") Long teacherId,
                                                                        @Param("days") Integer days);

    /**
     * 获取资源访问统计数据
     */
    @Select("SELECT tr.resource_type as resourceType, COUNT(*) as accessCount " +
            "FROM teaching_resources tr " +
            "JOIN courses c ON tr.course_id = c.id " +
            "WHERE c.teacher_id = #{teacherId} " +
            "AND tr.created_at >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY tr.resource_type " +
            "ORDER BY accessCount DESC")
    List<TeacherDashboardVO.ResourceAccessData> getResourceAccessData(@Param("teacherId") Long teacherId,
                                                                      @Param("days") Integer days);

    /**
     * 获取课程统计数据
     */
    @Select("SELECT " +
            "COUNT(*) as totalCourses, " +
            "COUNT(CASE WHEN c.is_published = 1 THEN 1 END) as publishedCourses, " +
            "COUNT(CASE WHEN EXISTS(SELECT 1 FROM course_offerings co " +
            "                       JOIN enrollments e ON co.id = e.offering_id " +
            "                       WHERE co.course_id = c.id) THEN 1 END) as activeCourses, " +
            "AVG(CASE WHEN EXISTS(SELECT 1 FROM course_offerings co WHERE co.course_id = c.id) THEN " +
            "  (SELECT COUNT(*) FROM course_offerings co2 " +
            "   JOIN enrollments e2 ON co2.id = e2.offering_id " +
            "   WHERE co2.course_id = c.id) " +
            "END) as avgStudentsPerCourse, " +
            "(SELECT COUNT(DISTINCT e.student_id) FROM course_offerings co " +
            " JOIN enrollments e ON co.id = e.offering_id " +
            " JOIN courses c2 ON co.course_id = c2.id " +
            " WHERE c2.teacher_id = #{teacherId}) as totalStudents, " +
            "(SELECT COUNT(*) FROM course_chapters ch " +
            " JOIN courses c3 ON ch.course_id = c3.id " +
            " WHERE c3.teacher_id = #{teacherId}) as totalChapters " +
            "FROM courses c WHERE c.teacher_id = #{teacherId}")
    TeacherDashboardVO.CourseStats getCourseStats(@Param("teacherId") Long teacherId);

    /**
     * 获取待批改作业列表
     */
    @Select("SELECT a.id as assignmentId, a.title as assignmentTitle, " +
            "c.title as courseName, s.submit_time, " +
            "u.real_name as studentName, s.id as submissionId, a.type, s.is_late " +
            "FROM assignments a " +
            "JOIN assignment_submissions s ON a.id = s.assignment_id " +
            "JOIN courses c ON a.course_id = c.id " +
            "JOIN users u ON s.student_id = u.user_id " +
            "WHERE a.teacher_id = #{teacherId} " +
            "AND s.status = 'submitted' AND s.grade IS NULL " +
            "ORDER BY s.submit_time ASC " +
            "LIMIT #{limit}")
    List<TeacherDashboardVO.PendingGrading> getPendingGrading(@Param("teacherId") Long teacherId,
                                                              @Param("limit") Integer limit);

    /**
     * 获取班级快照数据
     */
    @Select("SELECT c.id as courseId, c.title as courseName, " +
            "COUNT(DISTINCT e.student_id) as totalStudents, " +
            "COUNT(DISTINCT CASE WHEN cp.last_access_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) THEN e.student_id END) as activeStudents, " +
            "COUNT(DISTINCT CASE WHEN cp.completion_percentage < 50 THEN e.student_id END) as atRiskStudents, " +
            "AVG(CASE WHEN g.score IS NOT NULL THEN g.score END) as averageScore, " +
            "COUNT(DISTINCT CASE WHEN g.score >= 60 THEN g.student_id END) * 100.0 / NULLIF(COUNT(DISTINCT g.student_id), 0) as passRate, " +
            "COUNT(DISTINCT CASE WHEN g.score >= 90 THEN g.student_id END) * 100.0 / NULLIF(COUNT(DISTINCT g.student_id), 0) as excellentRate " +
            "FROM courses c " +
            "LEFT JOIN course_offerings co ON c.id = co.course_id " +
            "LEFT JOIN enrollments e ON co.id = e.offering_id " +
            "LEFT JOIN course_progress cp ON e.student_id = cp.student_id AND c.id = cp.course_id " +
            "LEFT JOIN grades g ON e.student_id = g.student_id AND c.id = g.course_id " +
            "WHERE c.teacher_id = #{teacherId} " +
            "GROUP BY c.id, c.title " +
            "ORDER BY c.title")
    List<TeacherDashboardVO.ClassSnapshot> getClassSnapshots(@Param("teacherId") Long teacherId);

    /**
     * 获取最近学生表现数据
     */
    @Select("SELECT u.real_name as studentName, c.title as courseName, " +
            "cp.completion_percentage as progress, cp.last_access_time, cp.study_time, " +
            "AVG(g.score) as averageScore, " +
            "CASE WHEN cp.completion_percentage >= 80 THEN 'excellent' " +
            "     WHEN cp.completion_percentage >= 60 THEN 'good' " +
            "     ELSE 'needs_improvement' END as status " +
            "FROM course_progress cp " +
            "JOIN users u ON cp.student_id = u.user_id " +
            "JOIN courses c ON cp.course_id = c.id " +
            "LEFT JOIN grades g ON cp.student_id = g.student_id AND cp.course_id = g.course_id " +
            "WHERE c.teacher_id = #{teacherId} " +
            "AND cp.last_access_time IS NOT NULL " +
            "GROUP BY cp.student_id, cp.course_id " +
            "ORDER BY cp.last_access_time DESC " +
            "LIMIT #{limit}")
    List<TeacherDashboardVO.StudentPerformance> getRecentStudentPerformance(@Param("teacherId") Long teacherId,
                                                                            @Param("limit") Integer limit);

    /**
     * 获取教学进度数据
     */
    @Select("SELECT c.id as courseId, c.title as courseName, " +
            "COUNT(ch.id) as totalChapters, " +
            "COUNT(CASE WHEN ch.is_published = 1 THEN 1 END) as publishedChapters, " +
            "COUNT(CASE WHEN ch.is_published = 1 THEN 1 END) * 100.0 / NULLIF(COUNT(ch.id), 0) as progressPercentage, " +
            "MAX(ch.updated_at) as lastUpdate " +
            "FROM courses c " +
            "LEFT JOIN course_chapters ch ON c.id = ch.course_id " +
            "WHERE c.teacher_id = #{teacherId} " +
            "GROUP BY c.id, c.title " +
            "ORDER BY c.title")
    List<TeacherDashboardVO.TeachingProgress> getTeachingProgress(@Param("teacherId") Long teacherId);

    /**
     * 获取最近活动数据
     */
    @Select("SELECT 'assignment_submission' as type, " +
            "CONCAT('学生提交了作业: ', a.title) as title, " +
            "CONCAT(u.real_name, ' 提交了作业') as description, " +
            "s.submit_time as timestamp, " +
            "u.real_name as relatedUser, " +
            "c.title as relatedCourse, " +
            "s.status " +
            "FROM assignment_submissions s " +
            "JOIN assignments a ON s.assignment_id = a.id " +
            "JOIN courses c ON a.course_id = c.id " +
            "JOIN users u ON s.student_id = u.user_id " +
            "WHERE a.teacher_id = #{teacherId} " +
            "AND s.submit_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
            "ORDER BY s.submit_time DESC " +
            "LIMIT #{limit}")
    List<TeacherDashboardVO.RecentActivity> getRecentActivities(@Param("teacherId") Long teacherId,
                                                                @Param("limit") Integer limit);

    /**
     * 标记通知为已读
     */
    @Insert("INSERT IGNORE INTO notification_reads (announcement_id, user_id, read_at) " +
            "VALUES (#{announcementId}, #{teacherId}, NOW())")
    void markAnnouncementAsRead(@Param("announcementId") Long announcementId,
                                @Param("teacherId") Long teacherId);

    // ========== 权限验证相关 ==========

    /**
     * 验证课程所有权
     */
    @Select("SELECT COUNT(*) > 0 FROM courses WHERE id = #{courseId} AND teacher_id = #{teacherId}")
    boolean isCourseOwner(@Param("teacherId") Long teacherId, @Param("courseId") Long courseId);

    /**
     * 验证章节所有权
     */
    @Select("SELECT COUNT(*) > 0 FROM course_chapters ch " +
            "JOIN courses c ON ch.course_id = c.id " +
            "WHERE ch.id = #{chapterId} AND c.teacher_id = #{teacherId}")
    boolean isChapterOwner(@Param("teacherId") Long teacherId, @Param("chapterId") Long chapterId);

    /**
     * 验证作业所有权
     */
    @Select("SELECT COUNT(*) > 0 FROM assignments WHERE id = #{assignmentId} AND teacher_id = #{teacherId}")
    boolean isAssignmentOwner(@Param("teacherId") Long teacherId, @Param("assignmentId") Long assignmentId);

    /**
     * 验证提交批改权限
     */
    @Select("SELECT COUNT(*) > 0 FROM assignment_submissions s " +
            "JOIN assignments a ON s.assignment_id = a.id " +
            "WHERE s.id = #{submissionId} AND a.teacher_id = #{teacherId}")
    boolean isSubmissionGradable(@Param("teacherId") Long teacherId, @Param("submissionId") Long submissionId);

    // ========== 课程管理相关 ==========

    /**
     * 获取教师课程列表
     */
    @Select("SELECT id, title, subject, description, is_published, student_count, chapter_count, created_at, updated_at " +
            "FROM courses WHERE teacher_id = #{teacherId} ORDER BY created_at DESC")
    List<TeacherDashboardVO.CourseInfo> getCoursesByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * 获取课程学生列表
     */
    @Select("SELECT u.user_id as id, u.username, u.real_name, u.email, " +
            "e.id as enrollmentDate, 'active' as status, " +
            "cp.completion_percentage as progress, cp.study_time, cp.last_access_time " +
            "FROM course_offerings co " +
            "JOIN enrollments e ON co.id = e.offering_id " +
            "JOIN users u ON e.student_id = u.user_id " +
            "LEFT JOIN course_progress cp ON e.student_id = cp.student_id AND co.course_id = cp.course_id " +
            "WHERE co.course_id = #{courseId} " +
            "ORDER BY u.real_name")
    List<TeacherDashboardVO.StudentInfo> getStudentsByCourse(@Param("courseId") Long courseId);

    /**
     * 获取课程作业列表
     */
    @Select("SELECT a.id, a.title, a.description, a.type, a.total_score, a.due_date, " +
            "a.allow_late_submission, a.is_published, a.created_at, " +
            "COUNT(s.id) as submissionCount, " +
            "COUNT(CASE WHEN s.grade IS NOT NULL THEN 1 END) as gradedCount " +
            "FROM assignments a " +
            "LEFT JOIN assignment_submissions s ON a.id = s.assignment_id " +
            "WHERE a.course_id = #{courseId} " +
            "GROUP BY a.id " +
            "ORDER BY a.created_at DESC")
    List<TeacherDashboardVO.AssignmentInfo> getAssignmentsByCourse(@Param("courseId") Long courseId);

    /**
     * 获取教师所有作业列表
     */
    @Select("SELECT a.id, a.title, a.description, a.type, a.total_score, a.due_date, " +
            "a.allow_late_submission, a.is_published, a.created_at, " +
            "COUNT(s.id) as submissionCount, " +
            "COUNT(CASE WHEN s.grade IS NOT NULL THEN 1 END) as gradedCount " +
            "FROM assignments a " +
            "LEFT JOIN assignment_submissions s ON a.id = s.assignment_id " +
            "WHERE a.teacher_id = #{teacherId} " +
            "GROUP BY a.id " +
            "ORDER BY a.created_at DESC")
    List<TeacherDashboardVO.AssignmentInfo> getAssignmentsByTeacher(@Param("teacherId") Long teacherId);

    /**
     * 获取课程成绩统计
     */
    @Select("SELECT c.title as courseName, " +
            "AVG(g.score) as averageScore, " +
            "MAX(g.score) as highestScore, " +
            "MIN(g.score) as lowestScore, " +
            "COUNT(DISTINCT g.student_id) as totalStudents, " +
            "COUNT(DISTINCT CASE WHEN g.score >= 60 THEN g.student_id END) as passedStudents, " +
            "COUNT(DISTINCT CASE WHEN g.score >= 60 THEN g.student_id END) * 100.0 / COUNT(DISTINCT g.student_id) as passRate " +
            "FROM courses c " +
            "LEFT JOIN grades g ON c.id = g.course_id " +
            "WHERE c.id = #{courseId} " +
            "GROUP BY c.id, c.title")
    TeacherDashboardVO.GradeStats getGradeStatsByCourse(@Param("courseId") Long courseId);

    // ========== 操作相关 ==========

    /**
     * 发布章节
     */
    @Update("UPDATE course_chapters SET is_published = 1, updated_at = NOW() WHERE id = #{chapterId}")
    void publishChapter(@Param("chapterId") Long chapterId);

    /**
     * 发布作业
     */
    @Update("UPDATE assignments SET is_published = 1, updated_at = NOW() WHERE id = #{assignmentId}")
    void publishAssignment(@Param("assignmentId") Long assignmentId);

    /**
     * 为作业创建学生提交记录
     */
    @Insert("INSERT INTO assignment_submissions (assignment_id, student_id, status) " +
            "SELECT #{assignmentId}, e.student_id, 'draft' " +
            "FROM assignments a " +
            "JOIN course_offerings co ON a.course_id = co.course_id " +
            "JOIN enrollments e ON co.id = e.offering_id " +
            "WHERE a.id = #{assignmentId}")
    void createAssignmentSubmissions(@Param("assignmentId") Long assignmentId);

    /**
     * 批改作业提交
     */
    @Update("UPDATE assignment_submissions SET grade = #{grade}, feedback = #{feedback}, " +
            "graded_by = #{teacherId}, graded_at = NOW(), status = 'graded' " +
            "WHERE id = #{submissionId}")
    void gradeSubmission(@Param("submissionId") Long submissionId,
                         @Param("grade") Double grade,
                         @Param("feedback") String feedback,
                         @Param("teacherId") Long teacherId);

    /**
     * 记录成绩
     */
    @Insert("INSERT INTO grades (student_id, course_id, item_type, item_id, score, graded_by) " +
            "SELECT s.student_id, a.course_id, 'assignment', a.id, #{grade}, #{teacherId} " +
            "FROM assignment_submissions s " +
            "JOIN assignments a ON s.assignment_id = a.id " +
            "WHERE s.id = #{submissionId}")
    void recordGrade(@Param("submissionId") Long submissionId,
                     @Param("grade") Double grade,
                     @Param("teacherId") Long teacherId);

    /**
     * 获取教学日历事件
     */
    @Select("SELECT 'assignment' as type, a.title, a.due_date as eventDate, c.course_name as courseName, " +
            "a.id as relatedId, a.description, 'high' as priority " +
            "FROM assignments a " +
            "JOIN courses c ON a.course_id = c.id " +
            "WHERE a.teacher_id = #{teacherId} " +
            "AND YEAR(a.due_date) = #{year} AND MONTH(a.due_date) = #{month} " +
            "UNION ALL " +
            "SELECT 'task' as type, t.title, t.due_date as eventDate, '' as courseName, " +
            "t.id as relatedId, t.description, t.priority " +
            "FROM todo_tasks t " +
            "WHERE t.user_id = #{teacherId} " +
            "AND YEAR(t.due_date) = #{year} AND MONTH(t.due_date) = #{month} " +
            "ORDER BY eventDate")
    List<TeacherDashboardVOs.CalendarEventVO> getTeachingCalendar(@Param("teacherId") Long teacherId,
                                                                  @Param("year") Integer year,
                                                                  @Param("month") Integer month);

    /**
     * 统计教师待办任务数
     */
    @Select("SELECT COUNT(*) FROM todo_tasks WHERE user_id = #{teacherId} AND status = 'PENDING'")
    Integer countPendingTasksByTeacherId(@Param("teacherId") Long teacherId);
}
