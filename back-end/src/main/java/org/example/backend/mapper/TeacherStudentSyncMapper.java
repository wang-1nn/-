package org.example.backend.mapper;

import org.apache.ibatis.annotations.*;
import org.example.backend.entity.vo.teacher.TeacherDashboardVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 教师端与学生端联动数据访问接口
 */
@Mapper
public interface TeacherStudentSyncMapper {

    // ========== 课程章节相关 ==========

    /**
     * 根据章节ID获取课程ID
     */
    @Select("SELECT course_id FROM course_chapters WHERE id = #{chapterId}")
    Long getCourseIdByChapter(@Param("chapterId") Long chapterId);

    /**
     * 获取章节标题
     */
    @Select("SELECT title FROM course_chapters WHERE id = #{chapterId}")
    String getChapterTitle(@Param("chapterId") Long chapterId);

    /**
     * 获取课程标题
     */
    @Select("SELECT title FROM courses WHERE id = #{courseId}")
    String getCourseTitle(@Param("courseId") Long courseId);

    /**
     * 获取课程的所有学生ID
     */
    @Select("SELECT DISTINCT e.student_id FROM course_offerings co " +
            "JOIN enrollments e ON co.id = e.offering_id " +
            "WHERE co.course_id = #{courseId}")
    List<Long> getStudentIdsByCourse(@Param("courseId") Long courseId);

    /**
     * 为章节初始化学生进度记录
     */
    @Insert("<script>" +
            "INSERT INTO course_progress (student_id, course_id, chapter_id, completion_percentage, completion_status) " +
            "VALUES " +
            "<foreach collection='studentIds' item='studentId' separator=','>" +
            "(#{studentId}, #{courseId}, #{chapterId}, 0.0, 'not_started')" +
            "</foreach>" +
            "</script>")
    void initStudentProgressForChapter(@Param("chapterId") Long chapterId, @Param("studentIds") List<Long> studentIds);

    /**
     * 更新课程章节计数
     */
    @Update("UPDATE courses SET chapter_count = (SELECT COUNT(*) FROM course_chapters WHERE course_id = #{courseId}) " +
            "WHERE id = #{courseId}")
    void updateCourseChapterCount(@Param("courseId") Long courseId);

    // ========== 作业相关 ==========

    /**
     * 根据作业ID获取课程ID
     */
    @Select("SELECT course_id FROM assignments WHERE id = #{assignmentId}")
    Long getCourseIdByAssignment(@Param("assignmentId") Long assignmentId);

    /**
     * 获取作业标题
     */
    @Select("SELECT title FROM assignments WHERE id = #{assignmentId}")
    String getAssignmentTitle(@Param("assignmentId") Long assignmentId);

    /**
     * 获取作业截止时间
     */
    @Select("SELECT due_date FROM assignments WHERE id = #{assignmentId}")
    LocalDateTime getAssignmentDueDate(@Param("assignmentId") Long assignmentId);

    /**
     * 根据提交ID获取学生ID
     */
    @Select("SELECT student_id FROM assignment_submissions WHERE id = #{submissionId}")
    Long getStudentIdBySubmission(@Param("submissionId") Long submissionId);

    /**
     * 根据提交ID获取作业ID
     */
    @Select("SELECT assignment_id FROM assignment_submissions WHERE id = #{submissionId}")
    Long getAssignmentIdBySubmission(@Param("submissionId") Long submissionId);

    /**
     * 根据作业ID获取教师ID
     */
    @Select("SELECT teacher_id FROM assignments WHERE id = #{assignmentId}")
    Long getTeacherIdByAssignment(@Param("assignmentId") Long assignmentId);

    /**
     * 提交作业
     */
    @Insert("INSERT INTO assignment_submissions (assignment_id, student_id, content, file_urls, submit_time, status, is_late) " +
            "VALUES (#{assignmentId}, #{studentId}, #{content}, #{fileUrls}, NOW(), 'submitted', " +
            "(SELECT CASE WHEN NOW() > due_date THEN 1 ELSE 0 END FROM assignments WHERE id = #{assignmentId}))")
    @Options(useGeneratedKeys = true, keyProperty = "submissionId")
    Long submitAssignment(@Param("studentId") Long studentId,
                          @Param("assignmentId") Long assignmentId,
                          @Param("content") String content,
                          @Param("fileUrls") String[] fileUrls);

    /**
     * 创建截止日期提醒任务
     */
    @Insert("INSERT INTO todo_tasks (user_id, task_type, title, description, priority, due_date, status) " +
            "SELECT teacher_id, 'REMINDER', CONCAT('作业截止提醒: ', title), " +
            "CONCAT('作业《', title, '》即将截止'), 'medium', #{dueDate}, 'PENDING' " +
            "FROM assignments WHERE id = #{assignmentId}")
    void createDeadlineReminder(@Param("assignmentId") Long assignmentId, @Param("dueDate") LocalDateTime dueDate);

    // ========== 考试相关 ==========

    /**
     * 验证考试所有权
     */
    @Select("SELECT COUNT(*) > 0 FROM exams WHERE id = #{examId} AND teacher_id = #{teacherId}")
    boolean isExamOwner(@Param("teacherId") Long teacherId, @Param("examId") Long examId);

    /**
     * 发布考试
     */
    @Update("UPDATE exams SET is_published = 1, updated_at = NOW() WHERE id = #{examId}")
    void publishExam(@Param("examId") Long examId);

    /**
     * 根据考试ID获取课程ID
     */
    @Select("SELECT course_id FROM exams WHERE id = #{examId}")
    Long getCourseIdByExam(@Param("examId") Long examId);

    /**
     * 获取考试标题
     */
    @Select("SELECT title FROM exams WHERE id = #{examId}")
    String getExamTitle(@Param("examId") Long examId);

    /**
     * 获取考试开始时间
     */
    @Select("SELECT start_time FROM exams WHERE id = #{examId}")
    LocalDateTime getExamStartTime(@Param("examId") Long examId);

    /**
     * 获取考试结束时间
     */
    @Select("SELECT end_time FROM exams WHERE id = #{examId}")
    LocalDateTime getExamEndTime(@Param("examId") Long examId);

    /**
     * 根据考试ID获取教师ID
     */
    @Select("SELECT teacher_id FROM exams WHERE id = #{examId}")
    Long getTeacherIdByExam(@Param("examId") Long examId);

    /**
     * 检查考试是否自动批改
     */
    @Select("SELECT auto_grade FROM exams WHERE id = #{examId}")
    boolean isExamAutoGraded(@Param("examId") Long examId);

    /**
     * 提交考试
     */
    @Insert("INSERT INTO submissions (exam_id, user_id, answers, submit_time, status) " +
            "VALUES (#{examId}, #{studentId}, #{answers}, NOW(), 'submitted')")
    @Options(useGeneratedKeys = true, keyProperty = "submissionId")
    Long submitExam(@Param("studentId") Long studentId,
                    @Param("examId") Long examId,
                    @Param("answers") String answers);

    // ========== 通知公告相关 ==========

    /**
     * 创建通知公告
     */
    @Insert("INSERT INTO announcements (title, content, type, target_audience, course_id, publisher_id, is_important, status) " +
            "VALUES (#{title}, #{content}, #{type}, #{targetAudience}, #{courseId}, #{publisherId}, #{isImportant}, 'published')")
    void createAnnouncement(@Param("title") String title,
                            @Param("content") String content,
                            @Param("type") String type,
                            @Param("targetAudience") String targetAudience,
                            @Param("courseId") Long courseId,
                            @Param("publisherId") Long publisherId,
                            @Param("isImportant") Boolean isImportant);

    /**
     * 创建个人通知
     */
    @Insert("INSERT INTO announcements (title, content, type, target_audience, publisher_id, is_important, status) " +
            "VALUES (#{title}, #{content}, #{type}, 'students', #{publisherId}, #{isImportant}, 'published'); " +
            "INSERT INTO notification_reads (announcement_id, user_id, read_at) " +
            "VALUES (LAST_INSERT_ID(), #{targetUserId}, NULL)")
    void createPersonalAnnouncement(@Param("title") String title,
                                    @Param("content") String content,
                                    @Param("type") String type,
                                    @Param("targetUserId") Long targetUserId,
                                    @Param("publisherId") Long publisherId,
                                    @Param("isImportant") Boolean isImportant);

    // ========== 学习行为相关 ==========

    /**
     * 记录学习行为
     */
    @Insert("INSERT INTO learning_behaviors (user_id, course_id, chapter_id, behavior_type, duration, behavior_time) " +
            "VALUES (#{studentId}, #{courseId}, #{chapterId}, #{behaviorType}, #{duration}, NOW())")
    void recordLearningBehavior(@Param("studentId") Long studentId,
                                @Param("courseId") Long courseId,
                                @Param("chapterId") Long chapterId,
                                @Param("behaviorType") String behaviorType,
                                @Param("duration") Integer duration);

    /**
     * 更新学习时长
     */
    @Update("UPDATE course_progress SET study_time = study_time + #{duration}, last_access_time = NOW() " +
            "WHERE student_id = #{studentId} AND course_id = #{courseId} AND chapter_id = #{chapterId}")
    void updateStudyTime(@Param("studentId") Long studentId,
                         @Param("courseId") Long courseId,
                         @Param("chapterId") Long chapterId,
                         @Param("duration") Integer duration);

    /**
     * 根据课程ID获取教师ID
     */
    @Select("SELECT teacher_id FROM courses WHERE id = #{courseId}")
    Long getTeacherIdByCourse(@Param("courseId") Long courseId);

    /**
     * 更新学生学习进度
     */
    @Update("UPDATE course_progress SET completion_percentage = #{progressPercentage}, " +
            "completion_status = CASE WHEN #{progressPercentage} >= 100 THEN 'completed' " +
            "                         WHEN #{progressPercentage} > 0 THEN 'in_progress' " +
            "                         ELSE 'not_started' END, " +
            "last_access_time = NOW() " +
            "WHERE student_id = #{studentId} AND course_id = #{courseId} AND chapter_id = #{chapterId}")
    void updateStudentProgress(@Param("studentId") Long studentId,
                               @Param("courseId") Long courseId,
                               @Param("chapterId") Long chapterId,
                               @Param("progressPercentage") Double progressPercentage);

    /**
     * 更新课程总进度
     */
    @Update("UPDATE course_progress cp1 SET completion_percentage = " +
            "(SELECT AVG(cp2.completion_percentage) FROM course_progress cp2 " +
            " WHERE cp2.student_id = #{studentId} AND cp2.course_id = #{courseId} AND cp2.chapter_id IS NOT NULL) " +
            "WHERE cp1.student_id = #{studentId} AND cp1.course_id = #{courseId} AND cp1.chapter_id IS NULL")
    void updateCourseProgress(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    // ========== 任务管理相关 ==========

    /**
     * 创建教师任务
     */
    @Insert("INSERT INTO todo_tasks (user_id, task_type, title, description, priority, due_date, status) " +
            "VALUES (#{teacherId}, #{taskType}, #{title}, #{description}, #{priority}, #{dueDate}, 'PENDING')")
    void createTeacherTask(@Param("teacherId") Long teacherId,
                           @Param("taskType") String taskType,
                           @Param("title") String title,
                           @Param("description") String description,
                           @Param("priority") String priority,
                           @Param("dueDate") LocalDateTime dueDate);

    // ========== 学生信息相关 ==========

    /**
     * 获取学生姓名
     */
    @Select("SELECT real_name FROM users WHERE user_id = #{studentId}")
    String getStudentName(@Param("studentId") Long studentId);

    /**
     * 获取学生课程进度
     */
    @Select("SELECT completion_percentage FROM course_progress " +
            "WHERE student_id = #{studentId} AND course_id = #{courseId} AND chapter_id IS NULL")
    Double getStudentCourseProgress(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    /**
     * 获取学生最后访问时间
     */
    @Select("SELECT MAX(last_access_time) FROM course_progress " +
            "WHERE student_id = #{studentId} AND course_id = #{courseId}")
    LocalDateTime getStudentLastAccessTime(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    // ========== 统计分析相关 ==========

    /**
     * 获取课程学生活动统计
     */
    @Select("SELECT DATE(behavior_time) as date, COUNT(*) as count " +
            "FROM learning_behaviors " +
            "WHERE course_id = #{courseId} " +
            "AND behavior_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(behavior_time) " +
            "ORDER BY date")
    List<TeacherDashboardVO.StudentActivityData> getStudentActivityStatsByCourse(@Param("courseId") Long courseId,
                                                                                 @Param("days") Integer days);

    /**
     * 获取学生进度分布
     */
    @Select("SELECT " +
            "COUNT(CASE WHEN completion_percentage BETWEEN 0 AND 20 THEN 1 END) as range1, " +
            "COUNT(CASE WHEN completion_percentage BETWEEN 21 AND 40 THEN 1 END) as range2, " +
            "COUNT(CASE WHEN completion_percentage BETWEEN 41 AND 60 THEN 1 END) as range3, " +
            "COUNT(CASE WHEN completion_percentage BETWEEN 61 AND 80 THEN 1 END) as range4, " +
            "COUNT(CASE WHEN completion_percentage BETWEEN 81 AND 100 THEN 1 END) as range5 " +
            "FROM course_progress " +
            "WHERE course_id = #{courseId} AND chapter_id IS NULL")
    int[] getStudentProgressDistribution(@Param("courseId") Long courseId);
}
