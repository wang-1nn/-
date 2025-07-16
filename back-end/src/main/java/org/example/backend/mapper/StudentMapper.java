package org.example.backend.mapper;

import org.apache.ibatis.annotations.*;
import org.example.backend.entity.dto.CourseDTO;
import org.example.backend.entity.dto.CourseDetailDTO;
import org.example.backend.entity.dto.CourseResourceDTO;
import org.example.backend.entity.dto.DiscussionDTO;
import org.example.backend.entity.dto.QuizDTO;
import org.example.backend.entity.dto.LearningNoteDTO;
import org.example.backend.entity.dto.ChapterRatingDTO;
import org.example.backend.entity.dto.LearningStatisticsDTO;
import org.example.backend.entity.pojo.TodoTask;
import org.example.backend.entity.pojo.LearningNote;
import org.example.backend.entity.pojo.ChapterRating;
import org.example.backend.entity.pojo.LearningStatisticsDetail;
import org.example.backend.entity.vo.DashboardDataVO.CourseProgressVO;
import org.example.backend.entity.vo.DashboardDataVO.RecommendationVO;
import org.example.backend.entity.vo.DashboardWidgetsVO.ExamScoreVO;
import org.example.backend.entity.vo.DashboardWidgetsVO.DailyStudyVO;
import org.example.backend.entity.vo.DashboardWidgetsVO.TaskItemVO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 学生相关数据库操作接口
 */
@Mapper
public interface StudentMapper {
    
    /**
     * 计算学生已选课程数量
     *
     * @param userId 学生ID
     * @return 课程数量
     */
    @Select("SELECT COUNT(*) FROM enrollments e " +
            "WHERE e.student_id = #{userId}")
    Integer countEnrolledCourses(@Param("userId") String userId);
    
    /**
     * 计算学生总学习时长（小时）
     *
     * @param userId 学生ID
     * @return 总学习时长（小时）
     */
    @Select("SELECT IFNULL(SUM(duration)/3600.0, 0) FROM learning_records " +
            "WHERE user_id = #{userId}")
    Float calculateLearningHours(@Param("userId") String userId);
    
    /**
     * 计算学生连续学习天数
     *
     * @param userId 学生ID
     * @return 连续天数
     */
    @Select({
            "WITH daily_records AS (",
            "  SELECT DISTINCT DATE(start_time) AS study_date",
            "  FROM learning_records",
            "  WHERE user_id = #{userId}",
            "  ORDER BY study_date DESC",
            ")",
            "SELECT COUNT(*) FROM (",
            "  SELECT study_date, ",
            "         ROW_NUMBER() OVER (ORDER BY study_date DESC) AS row_num,",
            "         DATEDIFF(CURRENT_DATE, study_date) AS days_ago",
            "  FROM daily_records",
            ") t",
            "WHERE days_ago = row_num - 1"
    })
    Integer calculateStreakDays(@Param("userId") String userId);
    
    /**
     * 计算学生平均成绩
     *
     * @param userId 学生ID
     * @return 平均成绩
     */
    @Select("SELECT IFNULL(ROUND(AVG(score)), 0) FROM student_exams " +
            "WHERE user_id = #{userId} AND status = 3")
    Integer calculateAverageScore(@Param("userId") String userId);
    
    /**
     * 查询学生课程学习进度
     *
     * @param userId 学生ID
     * @return 课程进度列表
     */
    @Select({
            "SELECT c.id, c.title AS name, ",
            "  IFNULL(cp.completion_percentage, 0) AS progress, ",
            "  CASE ",
            "    WHEN DATEDIFF(CURRENT_DATE, cp.updated_at) = 0 THEN '今天' ",
            "    WHEN DATEDIFF(CURRENT_DATE, cp.updated_at) = 1 THEN '昨天' ",
            "    WHEN DATEDIFF(CURRENT_DATE, cp.updated_at) <= 7 THEN CONCAT(DATEDIFF(CURRENT_DATE, cp.updated_at), '天前') ",
            "    ELSE DATE_FORMAT(cp.updated_at, '%m-%d') ",
            "  END AS lastStudy, ",
            "  CONCAT('bg-', ",
            "    CASE c.id % 5 ",
            "      WHEN 0 THEN 'blue' ",
            "      WHEN 1 THEN 'purple' ",
            "      WHEN 2 THEN 'green' ",
            "      WHEN 3 THEN 'amber' ",
            "      ELSE 'pink' ",
            "    END, '-500') AS color ",
            "FROM courses c ",
            "INNER JOIN course_offerings co ON c.id = co.course_id ",
            "INNER JOIN users student ON student.user_id = #{userId} ",
            "LEFT JOIN course_progress cp ON cp.course_id = c.id AND cp.student_id = #{userId} ",
            "WHERE co.class_id = student.classId ",
            "ORDER BY cp.updated_at DESC"
    })
    List<CourseProgressVO> findCourseProgress(@Param("userId") String userId);
    
    /**
     * 查询学生个性化学习建议
     *
     * @param userId 学生ID
     * @return 学习建议列表
     */
    @Select({
            "SELECT ROW_NUMBER() OVER() AS id, ",
            "  c.title, ",
            "  CASE ",
            "    WHEN cp.completion_percentage < 30 THEN CONCAT('您的', c.title, '课程进度较低，建议加强学习') ",
            "    WHEN DATEDIFF(CURRENT_DATE, cp.updated_at) > 7 THEN CONCAT('您已经一周没有学习', c.title, '，建议及时复习') ",
            "    WHEN se.score IS NOT NULL AND se.score < 60 THEN CONCAT('根据您最近的考试成绩，建议复习', c.title) ",
            "    ELSE CONCAT(c.title, '的学习状态良好，继续保持') ",
            "  END AS description, ",
            "  IFNULL(cp.completion_percentage, 0) AS progress ",
            "FROM courses c ",
            "INNER JOIN course_offerings co ON c.id = co.course_id ",
            "INNER JOIN users student ON student.user_id = #{userId} ",
            "LEFT JOIN course_progress cp ON cp.course_id = c.id AND cp.student_id = #{userId} ",
            "LEFT JOIN student_exams se ON se.user_id = #{userId} AND se.exam_id IN ",
            "  (SELECT id FROM exams WHERE offering_id = co.id) ",
            "WHERE co.class_id = student.classId ",
            "ORDER BY ",
            "  CASE ",
            "    WHEN cp.completion_percentage < 30 THEN 1 ",
            "    WHEN DATEDIFF(CURRENT_DATE, cp.updated_at) > 7 THEN 2 ",
            "    WHEN se.score IS NOT NULL AND se.score < 60 THEN 3 ",
            "    ELSE 4 ",
            "  END, ",
            "  cp.updated_at DESC ",
            "LIMIT 3"
    })
    List<RecommendationVO> findLearningRecommendations(@Param("userId") String userId);

    /**
     * 获取学生最近考试成绩及历史记录
     *
     * @param userId 学生ID
     * @return 最近的考试成绩
     */
    @Select("SELECT score FROM student_exams " +
            "WHERE user_id = #{userId} AND status = 3 " +
            "ORDER BY submit_time DESC LIMIT 1")
    Integer findLatestExamScore(@Param("userId") String userId);

    /**
     * 获取学生最近考试成绩的变化趋势
     *
     * @param userId 学生ID
     * @return 分数变化，正数表示增加，负数表示减少
     */
    @Select({
            "SELECT (latest.score - previous.score) AS score_trend FROM ",
            "(SELECT score FROM student_exams WHERE user_id = #{userId} AND status = 3 ORDER BY submit_time DESC LIMIT 1) latest, ",
            "(SELECT score FROM student_exams WHERE user_id = #{userId} AND status = 3 ORDER BY submit_time DESC LIMIT 1 OFFSET 1) previous"
    })
    Integer findScoreTrend(@Param("userId") String userId);

    /**
     * 获取学生近期考试成绩历史
     *
     * @param userId 学生ID
     * @return 近期考试成绩列表
     */
    @Select({
            "SELECT e.title AS name, ",
            "   SUBSTRING_INDEX(e.title, '期', 1) AS short_name, ",
            "   se.score ",
            "FROM student_exams se ",
            "JOIN exams e ON se.exam_id = e.id ",
            "WHERE se.user_id = #{userId} AND se.status = 3 ",
            "ORDER BY se.submit_time DESC ",
            "LIMIT 5"
    })
    List<ExamScoreVO> findRecentExamScores(@Param("userId") String userId);

    /**
     * 获取本周学习时长（小时）
     *
     * @param userId 学生ID
     * @return 本周学习时长
     */
    @Select("SELECT IFNULL(SUM(duration)/3600.0, 0) FROM learning_records " +
            "WHERE user_id = #{userId} " +
            "AND YEARWEEK(start_time) = YEARWEEK(NOW())")
    Float calculateWeeklyLearningHours(@Param("userId") String userId);

    /**
     * 获取上周学习时长（小时）
     *
     * @param userId 学生ID
     * @return 上周学习时长
     */
    @Select("SELECT IFNULL(SUM(duration)/3600.0, 0) FROM learning_records " +
            "WHERE user_id = #{userId} " +
            "AND YEARWEEK(start_time) = YEARWEEK(DATE_SUB(NOW(), INTERVAL 1 WEEK))")
    Float calculateLastWeekLearningHours(@Param("userId") String userId);

    /**
     * 获取每日学习时长数据
     *
     * @param userId 学生ID
     * @return 本周每日学习时长
     */
    @Select({
            "SELECT ",
            "  CASE DAYOFWEEK(start_time) ",
            "    WHEN 1 THEN '周日' ",
            "    WHEN 2 THEN '周一' ",
            "    WHEN 3 THEN '周二' ",
            "    WHEN 4 THEN '周三' ",
            "    WHEN 5 THEN '周四' ",
            "    WHEN 6 THEN '周五' ",
            "    WHEN 7 THEN '周六' ",
            "  END AS day, ",
            "  IFNULL(SUM(duration)/3600.0, 0) AS hours ",
            "FROM learning_records ",
            "WHERE user_id = #{userId} ",
            "AND YEARWEEK(start_time) = YEARWEEK(NOW()) ",
            "GROUP BY DAYOFWEEK(start_time), CASE DAYOFWEEK(start_time) ",
            "    WHEN 1 THEN '周日' ",
            "    WHEN 2 THEN '周一' ",
            "    WHEN 3 THEN '周二' ",
            "    WHEN 4 THEN '周三' ",
            "    WHEN 5 THEN '周四' ",
            "    WHEN 6 THEN '周五' ",
            "    WHEN 7 THEN '周六' ",
            "  END ",
            "ORDER BY DAYOFWEEK(start_time)"
    })
    List<DailyStudyVO> findWeeklyStudyTime(@Param("userId") String userId);

    /**
     * 获取学生待办任务列表
     *
     * @param userId 学生ID
     * @return 待办任务列表
     */
    @Select({
            "SELECT ",
            "  t.id, ",
            "  t.title, ",
            "  CASE ",
            "    WHEN DATE(t.due_date) = CURDATE() THEN CONCAT('今天 ', DATE_FORMAT(t.due_date, '%H:%i')) ",
            "    WHEN DATE(t.due_date) = DATE_ADD(CURDATE(), INTERVAL 1 DAY) THEN CONCAT('明天 ', DATE_FORMAT(t.due_date, '%H:%i')) ",
            "    WHEN DATE(t.due_date) = DATE_ADD(CURDATE(), INTERVAL 2 DAY) THEN CONCAT('后天 ', DATE_FORMAT(t.due_date, '%H:%i')) ",
            "    WHEN YEARWEEK(t.due_date) = YEARWEEK(NOW()) THEN CONCAT('本周', ",
            "      CASE DAYOFWEEK(t.due_date) ",
            "        WHEN 1 THEN '日' ",
            "        WHEN 2 THEN '一' ",
            "        WHEN 3 THEN '二' ",
            "        WHEN 4 THEN '三' ",
            "        WHEN 5 THEN '四' ",
            "        WHEN 6 THEN '五' ",
            "        WHEN 7 THEN '六' ",
            "      END) ",
            "    WHEN YEARWEEK(t.due_date) = YEARWEEK(DATE_ADD(NOW(), INTERVAL 1 WEEK)) THEN CONCAT('下周', ",
            "      CASE DAYOFWEEK(t.due_date) ",
            "        WHEN 1 THEN '日' ",
            "        WHEN 2 THEN '一' ",
            "        WHEN 3 THEN '二' ",
            "        WHEN 4 THEN '三' ",
            "        WHEN 5 THEN '四' ",
            "        WHEN 6 THEN '五' ",
            "        WHEN 7 THEN '六' ",
            "      END) ",
            "    ELSE DATE_FORMAT(t.due_date, '%m-%d') ",
            "  END AS due, ",
            "  t.status = 'COMPLETED' AS completed, ",
            "  DATEDIFF(t.due_date, NOW()) <= 1 AS urgent ",
            "FROM todo_tasks t ",
            "WHERE t.user_id = #{userId} ",
            "ORDER BY t.status, t.due_date ",
            "LIMIT 5"
    })
    List<TaskItemVO> findTodoTasks(@Param("userId") String userId);
    
    /**
     * 添加待办任务
     *
     * @param task 待办任务对象
     * @return 影响的行数
     */
    @Insert("INSERT INTO todo_tasks (user_id, title, status, due_date, task_type, created_at) " +
            "VALUES (#{userId}, #{title}, #{status}, #{dueDate}, #{taskType}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int addTodoTask(TodoTask task);
    
    /**
     * 更新待办任务状态
     *
     * @param taskId 任务ID
     * @param status 新状态
     * @return 影响的行数
     */
    @Update("UPDATE todo_tasks SET status = #{status} WHERE id = #{taskId}")
    int updateTodoTaskStatus(@Param("taskId") Long taskId, @Param("status") String status);
    
    /**
     * 根据ID查询待办任务
     *
     * @param taskId 任务ID
     * @return 任务对象
     */
    @Select("SELECT * FROM todo_tasks WHERE id = #{taskId}")
    TodoTask findTodoTaskById(@Param("taskId") Long taskId);

    /**
     * 获取学生课程列表
     *
     * @param userId 学生ID
     * @return 课程列表
     */
    @Select({
            "SELECT DISTINCT",
            "  c.id, ",
            "  c.title, ",
            "  c.description, ",
            "  c.subject, ",
            "  c.teacher_id AS teacherId, ",
            "  u.real_name AS teacherName, ",
            "  c.image, ",
            "  CASE ",
            "    WHEN c.subject IN ('数学', '物理', '计算机科学', '英语') THEN '专业课' ",
            "    ELSE '选修课' ",
            "  END AS courseType, ",
            "  IFNULL(cp.completion_percentage, 0) AS progress, ",
            "  (SELECT MAX(lr.start_time) FROM learning_records lr WHERE lr.user_id = #{userId} AND lr.course_id = c.id) AS lastVisitTime, ",
            "  EXISTS(SELECT 1 FROM course_favorites cf WHERE cf.user_id = #{userId} AND cf.course_id = c.id) AS isFavorite ", 
            "FROM courses c ",
            "INNER JOIN course_offerings co ON c.id = co.course_id ",
            "INNER JOIN users u ON c.teacher_id = u.user_id ",
            "INNER JOIN users student ON student.user_id = #{userId} ",
            "LEFT JOIN (",
            "  SELECT course_id, student_id, MAX(completion_percentage) as completion_percentage ",
            "  FROM course_progress ",
            "  GROUP BY course_id, student_id",
            ") cp ON cp.course_id = c.id AND cp.student_id = #{userId} ",
            "WHERE co.class_id = student.classId ",
            "ORDER BY lastVisitTime DESC"
    })
    List<CourseDTO> findStudentCourses(@Param("userId") Long userId);

    /**
     * 获取课程详情
     *
     * @param userId 学生ID
     * @param courseId 课程ID
     * @return 课程详情
     */
    @Select({
            "SELECT ",
            "  c.id, ",
            "  c.title, ",
            "  c.description, ",
            "  c.subject, ",
            "  c.teacher_id AS teacherId, ",
            "  u.real_name AS teacherName, ",
            "  u.avatar AS teacherAvatar, ",
            "  c.image, ",
            "  CASE ",
            "    WHEN c.subject IN ('数学', '物理', '计算机科学', '英语') THEN '专业课' ",
            "    ELSE '选修课' ",
            "  END AS courseType, ",
            "  IFNULL(cp.completion_percentage, 0) AS progress, ",
            "  c.subject AS department, ", 
            "  co.id AS credits, ",
            "  (SELECT MIN(start_time) FROM exams WHERE offering_id = co.id) AS startDate, ",
            "  (SELECT MAX(end_time) FROM exams WHERE offering_id = co.id) AS endDate, ",
            "  co.semester AS schedule, ", 
            "  cp.chapter_id AS currentChapterId, ",
            "  cp.last_position AS lastPosition, ",
            "  (SELECT MAX(lr.start_time) FROM learning_records lr WHERE lr.user_id = #{userId} AND lr.course_id = c.id) AS lastVisitTime, ",
            "  EXISTS(SELECT 1 FROM course_favorites cf WHERE cf.user_id = #{userId} AND cf.course_id = c.id) AS isFavorite ", 
            "FROM courses c ",
            "INNER JOIN course_offerings co ON c.id = co.course_id ",
            "INNER JOIN users u ON c.teacher_id = u.user_id ",
            "INNER JOIN users student ON student.user_id = #{userId} ",
            "LEFT JOIN course_progress cp ON cp.course_id = c.id AND cp.student_id = #{userId} ",
            "WHERE c.id = #{courseId} AND co.class_id = student.classId"
    })
    CourseDetailDTO findCourseDetail(@Param("userId") Long userId, @Param("courseId") Long courseId);

    /**
     * 获取课程资源列表
     *
     * @param courseId 课程ID
     * @return 资源列表
     */
    @Select({
            "SELECT ",
            "  tr.resource_id AS resourceId, ",
            "  tr.course_id AS courseId, ",
            "  tr.title, ",
            "  tr.resource_type AS resourceType, ",
            "  CASE tr.resource_type ",
            "    WHEN 'DOCUMENT' THEN 'file-text' ",
            "    WHEN 'PRESENTATION' THEN 'file-presentation' ",
            "    WHEN 'VIDEO' THEN 'video' ",
            "    WHEN 'EXERCISE' THEN 'file-question' ",
            "    WHEN 'CODE' THEN 'file-code' ",
            "    ELSE 'file' ",
            "  END AS icon, ",
            "  tr.file_path AS filePath, ",
            "  tr.url, ",
            "  tr.created_by AS createdBy, ",
            "  u.real_name AS creatorName, ",
            "  tr.created_at AS createdAt, ",
            "  1024 AS fileSize, ", // 文件大小暂时固定，可以后续扩展
            "  10 AS downloadCount, ", // 下载次数暂时固定，可以后续扩展
            "  tr.is_ai_generated AS isAiGenerated ",
            "FROM teaching_resources tr ",
            "INNER JOIN users u ON tr.created_by = u.user_id ",
            "WHERE tr.course_id = #{courseId} ",
            "ORDER BY tr.created_at DESC"
    })
    List<CourseResourceDTO> findCourseResources(@Param("courseId") Long courseId);

    /**
     * 获取课程讨论列表
     *
     * @param courseId 课程ID
     * @return 讨论列表
     */
    @Select({
            "SELECT ",
            "  cd.id, ",
            "  cd.course_id AS courseId, ",
            "  cd.user_id AS userId, ",
            "  u.real_name AS userName, ",
            "  u.avatar AS userAvatar, ",
            "  cd.title, ",
            "  cd.content, ",
            "  cd.publish_time AS publishTime, ",
            "  cd.last_reply_time AS lastReplyTime, ",
            "  cd.reply_count AS replyCount, ",
            "  cd.view_count AS viewCount, ",
            "  cd.like_count AS likeCount, ",
            "  cd.is_sticky AS isSticky, ",
            "  cd.tags, ",
            "  CASE WHEN u.role_id = 2 THEN TRUE ELSE FALSE END AS isTeacher ",
            "FROM course_discussions cd ",
            "INNER JOIN users u ON cd.user_id = u.user_id ",
            "WHERE cd.course_id = #{courseId} AND cd.status = 1 ",
            "ORDER BY cd.is_sticky DESC, IFNULL(cd.last_reply_time, '1970-01-01') DESC, cd.publish_time DESC"
    })
    @Options(flushCache = Options.FlushCachePolicy.TRUE)
    List<DiscussionDTO> findCourseDiscussions(@Param("courseId") Long courseId);
    
    /**
     * 获取课程公告列表
     *
     * @param courseId 课程ID
     * @return 公告列表
     */
    @Select({
            "SELECT ",
            "  cd.id, ",
            "  cd.course_id AS courseId, ",
            "  u.real_name AS publisherName, ",
            "  cd.title, ",
            "  cd.content, ",
            "  cd.publish_time AS publishTime ",
            "FROM course_discussions cd ",
            "INNER JOIN users u ON cd.user_id = u.user_id ",
            "WHERE cd.course_id = #{courseId} AND cd.status = 1 ",
            "  AND u.role_id = 2 ",  // 仅教师发布的
            "  AND (cd.tags LIKE '%公告%' OR cd.is_sticky = 1) ", // 带有公告标签或置顶的
            "ORDER BY cd.publish_time DESC ",
            "LIMIT 5"
    })
    List<CourseDetailDTO.AnnouncementDTO> findCourseAnnouncements(@Param("courseId") Long courseId);

    /**
     * 获取讨论详情
     *
     * @param discussionId 讨论ID
     * @return 讨论详情
     */
    @Select({
            "SELECT ",
            "  cd.id, ",
            "  cd.course_id AS courseId, ",
            "  cd.user_id AS userId, ",
            "  u.real_name AS userName, ",
            "  u.avatar AS userAvatar, ",
            "  cd.title, ",
            "  cd.content, ",
            "  cd.publish_time AS publishTime, ",
            "  cd.last_reply_time AS lastReplyTime, ",
            "  cd.reply_count AS replyCount, ",
            "  cd.view_count AS viewCount, ",
            "  cd.like_count AS likeCount, ",
            "  cd.is_sticky AS isSticky, ",
            "  cd.tags ",
            "FROM course_discussions cd ",
            "INNER JOIN users u ON cd.user_id = u.user_id ",
            "WHERE cd.id = #{discussionId} AND cd.status = 1"
    })
    DiscussionDTO findDiscussionDetail(@Param("discussionId") Long discussionId);

    /**
     * 获取讨论回复列表
     *
     * @param discussionId 讨论ID
     * @return 回复列表
     */
    @Select({
            "SELECT ",
            "  dr.id, ",
            "  dr.discussion_id AS discussionId, ",
            "  dr.user_id AS userId, ",
            "  u.real_name AS userName, ",
            "  u.avatar AS userAvatar, ",
            "  dr.content, ",
            "  dr.reply_time AS replyTime, ",
            "  dr.like_count AS likeCount, ",
            "  dr.parent_id AS parentId, ",
            "  CASE WHEN u.role_id = 2 THEN TRUE ELSE FALSE END AS isTeacher ",
            "FROM discussion_replies dr ",
            "INNER JOIN users u ON dr.user_id = u.user_id ",
            "WHERE dr.discussion_id = #{discussionId} AND dr.status = 1 ",
            "ORDER BY dr.parent_id IS NULL DESC, dr.reply_time ASC"
    })
    List<DiscussionDTO.ReplyDTO> findDiscussionReplies(@Param("discussionId") Long discussionId);

    /**
     * 获取课程测验列表
     *
     * @param userId 学生ID
     * @param courseId 课程ID
     * @return 测验列表
     */
    @Select({
            "SELECT ",
            "  e.id, ",
            "  co.course_id AS courseId, ",
            "  e.title, ",
            "  e.exam_type AS quizType, ",
            "  CASE ",
            "    WHEN NOW() < e.start_time THEN 0 ", // 未开始
            "    WHEN NOW() BETWEEN e.start_time AND e.end_time THEN 1 ", // 进行中
            "    WHEN NOW() > e.end_time AND (se.id IS NULL OR se.status < 2) THEN 2 ", // 已结束但未提交
            "    ELSE 3 ", // 已提交
            "  END AS status, ",
            "  e.start_time AS startTime, ",
            "  e.end_time AS endTime, ",
            "  TIMESTAMPDIFF(MINUTE, e.start_time, e.end_time) AS duration, ",
            "  (SELECT SUM(q.score) FROM questions q WHERE q.exam_id = e.id) AS totalScore, ",
            "  se.score, ",
            "  (SELECT COUNT(*) FROM questions q WHERE q.exam_id = e.id) AS questionCount, ",
            "  CASE WHEN se.id IS NULL THEN 0 ",
            "       ELSE CASE WHEN se.status >= 2 THEN (SELECT COUNT(*) FROM questions q WHERE q.exam_id = e.id) ELSE 0 END ",
            "  END AS completedCount ",
            "FROM exams e ",
            "INNER JOIN course_offerings co ON e.offering_id = co.id ",
            "INNER JOIN users student ON student.user_id = #{userId} ",
            "LEFT JOIN student_exams se ON e.id = se.exam_id AND se.user_id = #{userId} ",
            "WHERE co.course_id = #{courseId} AND co.class_id = student.classId ",
            "ORDER BY e.start_time DESC"
    })
    List<QuizDTO> findCourseQuizzes(@Param("userId") Long userId, @Param("courseId") Long courseId);

    /**
     * 获取测验详情
     *
     * @param userId 学生ID
     * @param quizId 测验ID
     * @return 测验详情
     */
    @Select({
            "SELECT ",
            "  e.id, ",
            "  co.course_id AS courseId, ",
            "  e.title, ",
            "  e.exam_type AS quizType, ",
            "  CASE ",
            "    WHEN NOW() < e.start_time THEN 0 ", // 未开始
            "    WHEN NOW() BETWEEN e.start_time AND e.end_time THEN 1 ", // 进行中
            "    WHEN NOW() > e.end_time AND (se.id IS NULL OR se.status < 2) THEN 2 ", // 已结束但未提交
            "    ELSE 3 ", // 已提交
            "  END AS status, ",
            "  e.start_time AS startTime, ",
            "  e.end_time AS endTime, ",
            "  TIMESTAMPDIFF(MINUTE, e.start_time, e.end_time) AS duration, ",
            "  (SELECT SUM(q.score) FROM questions q WHERE q.exam_id = e.id) AS totalScore, ",
            "  se.score, ",
            "  (SELECT COUNT(*) FROM questions q WHERE q.exam_id = e.id) AS questionCount, ",
            "  CASE WHEN se.id IS NULL THEN 0 ",
            "       ELSE CASE WHEN se.status >= 2 THEN (SELECT COUNT(*) FROM questions q WHERE q.exam_id = e.id) ELSE 0 END ",
            "  END AS completedCount ",
            "FROM exams e ",
            "INNER JOIN course_offerings co ON e.offering_id = co.id ",
            "INNER JOIN users student ON student.user_id = #{userId} ",
            "LEFT JOIN student_exams se ON e.id = se.exam_id AND se.user_id = #{userId} ",
            "WHERE e.id = #{quizId} AND co.class_id = student.classId"
    })
    QuizDTO findQuizDetail(@Param("userId") Long userId, @Param("quizId") Long quizId);

    /**
     * 获取测验问题列表
     *
     * @param quizId 测验ID
     * @return 问题列表
     */
    @Select({
            "SELECT ",
            "  q.id, ",
            "  #{quizId} AS quizId, ",
            "  q.question_type AS questionType, ",
            "  q.content, ",
            "  q.options, ",
            "  q.answer, ",
            "  NULL AS studentAnswer, ",
            "  q.score, ",
            "  NULL AS obtainedScore, ",
            "  q.analysis, ",
            "  q.difficulty ",
            "FROM questions q ",
            "WHERE q.exam_id = #{quizId} ",
            "ORDER BY q.id"
    })
    List<QuizDTO.QuizQuestionDTO> findQuizQuestions(@Param("quizId") Long quizId);

    /**
     * 更新学生课程学习进度
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param chapterId 章节ID
     * @param lastPosition 当前学习位置
     * @param completionPercentage 完成百分比
     * @return 影响的行数
     */
    @Insert({
            "<script>",
            "INSERT INTO course_progress (student_id, course_id, chapter_id, last_position, completion_percentage, updated_at) ",
            "VALUES (#{studentId}, #{courseId}, #{chapterId}, #{lastPosition}, #{completionPercentage}, NOW()) ",
            "ON DUPLICATE KEY UPDATE ",
            "  chapter_id = #{chapterId}, ",
            "  last_position = #{lastPosition}, ",
            "  completion_percentage = #{completionPercentage}, ",
            "  updated_at = NOW()",
            "</script>"
    })
    int updateCourseProgress(@Param("studentId") Long studentId, @Param("courseId") Long courseId, 
                            @Param("chapterId") Long chapterId, @Param("lastPosition") String lastPosition, 
                            @Param("completionPercentage") java.math.BigDecimal completionPercentage);

    /**
     * 添加学习记录
     *
     * @param userId 学生ID
     * @param courseId 课程ID
     * @param contentType 内容类型
     * @param contentId 内容ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param duration 时长（秒）
     * @param progress 进度（百分比）
     * @return 影响的行数
     */
    @Insert({
            "INSERT INTO learning_records (user_id, course_id, content_type, content_id, start_time, end_time, duration, progress) ",
            "VALUES (#{userId}, #{courseId}, #{contentType}, #{contentId}, #{startTime}, #{endTime}, #{duration}, #{progress})"
    })
    int addLearningRecord(@Param("userId") String userId, @Param("courseId") String courseId, 
                         @Param("contentType") String contentType, @Param("contentId") String contentId, 
                         @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, 
                         @Param("duration") Integer duration, @Param("progress") java.math.BigDecimal progress);

    /**
     * 创建新讨论
     *
     * @param userId 用户ID
     * @param courseId 课程ID
     * @param title 讨论标题
     * @param content 讨论内容
     * @return 影响行数
     */
    @Insert({
            "INSERT INTO course_discussions (course_id, user_id, title, content, publish_time) ",
            "VALUES (#{courseId}, #{userId}, #{title}, #{content}, NOW())"
    })
    int createDiscussion(@Param("userId") Long userId, @Param("courseId") Long courseId, 
                         @Param("title") String title, @Param("content") String content);

    /**
     * 回复讨论
     *
     * @param userId 用户ID
     * @param discussionId 讨论ID
     * @param content 回复内容
     * @param parentId 父回复ID（可为null）
     * @return 影响行数
     */
    @Insert({
            "INSERT INTO discussion_replies (discussion_id, user_id, content, reply_time, parent_id) ",
            "VALUES (#{discussionId}, #{userId}, #{content}, NOW(), #{parentId})"
    })
    int replyToDiscussion(@Param("userId") Long userId, @Param("discussionId") Long discussionId, 
                          @Param("content") String content, @Param("parentId") Long parentId);

    /**
     * 更新讨论的回复数量和最后回复时间
     *
     * @param discussionId 讨论ID
     * @return 影响行数
     */
    @Update({
            "UPDATE course_discussions ",
            "SET reply_count = (SELECT COUNT(*) FROM discussion_replies WHERE discussion_id = #{discussionId}), ",
            "    last_reply_time = NOW() ",
            "WHERE id = #{discussionId}"
    })
    int updateDiscussionReplyInfo(@Param("discussionId") Long discussionId);

    /**
     * 检查用户是否已点赞讨论
     *
     * @param userId 用户ID
     * @param discussionId 讨论ID
     * @return 是否已点赞
     */
    @Select({
            "SELECT COUNT(*) > 0 ",
            "FROM discussion_likes ",
            "WHERE user_id = #{userId} AND discussion_id = #{discussionId}"
    })
    boolean checkDiscussionLike(@Param("userId") Long userId, @Param("discussionId") Long discussionId);

    /**
     * 添加讨论点赞
     *
     * @param userId 用户ID
     * @param discussionId 讨论ID
     * @return 影响行数
     */
    @Insert({
            "INSERT INTO discussion_likes (user_id, discussion_id, created_at) ",
            "VALUES (#{userId}, #{discussionId}, NOW())"
    })
    int addDiscussionLike(@Param("userId") Long userId, @Param("discussionId") Long discussionId);

    /**
     * 移除讨论点赞
     *
     * @param userId 用户ID
     * @param discussionId 讨论ID
     * @return 影响行数
     */
    @Delete({
            "DELETE FROM discussion_likes ",
            "WHERE user_id = #{userId} AND discussion_id = #{discussionId}"
    })
    int removeDiscussionLike(@Param("userId") Long userId, @Param("discussionId") Long discussionId);

    /**
     * 增加讨论点赞数
     *
     * @param discussionId 讨论ID
     * @return 影响行数
     */
    @Update({
            "UPDATE course_discussions ",
            "SET like_count = like_count + 1 ",
            "WHERE id = #{discussionId}"
    })
    int increaseDiscussionLikeCount(@Param("discussionId") Long discussionId);

    /**
     * 减少讨论点赞数
     *
     * @param discussionId 讨论ID
     * @return 影响行数
     */
    @Update({
            "UPDATE course_discussions ",
            "SET like_count = GREATEST(like_count - 1, 0) ",
            "WHERE id = #{discussionId}"
    })
    int decreaseDiscussionLikeCount(@Param("discussionId") Long discussionId);

    /**
     * 增加讨论浏览量
     *
     * @param discussionId 讨论ID
     * @return 影响行数
     */
    @Update({
            "UPDATE course_discussions ",
            "SET view_count = view_count + 1 ",
            "WHERE id = #{discussionId}"
    })
    int incrementDiscussionViewCount(@Param("discussionId") Long discussionId);
    
    /**
     * 获取课程章节列表
     *
     * @param courseId 课程ID
     * @param studentId 学生ID
     * @return 章节列表
     */
    @Select({
            "SELECT ",
            "  cc.id, ",
            "  cc.course_id AS courseId, ",
            "  cc.title, ",
            "  cc.description, ",
            "  cc.order_num AS orderNum, ",
            "  cc.duration, ",
            "  cc.status, ",
            "  (SELECT COUNT(*) FROM chapter_contents WHERE chapter_id = cc.id AND status = 1) AS contentCount, ",
            "  EXISTS(SELECT 1 FROM learning_statistics_detail WHERE chapter_id = cc.id AND student_id = #{studentId} AND is_completed = 1) AS isCompleted, ",
            "  IFNULL((SELECT SUM(duration) FROM learning_statistics_detail WHERE chapter_id = cc.id AND student_id = #{studentId}), 0) AS studiedDuration, ",
            "  (SELECT MAX(last_visit_time) FROM learning_statistics_detail WHERE chapter_id = cc.id AND student_id = #{studentId}) AS lastStudyTime ",
            "FROM course_chapters cc ",
            "WHERE cc.course_id = #{courseId} AND cc.status = 1 ",
            "ORDER BY cc.order_num ASC"
    })
    List<Map<String, Object>> findCourseChapters(@Param("courseId") Long courseId, @Param("studentId") Long studentId);
    
    /**
     * 获取章节内容列表
     *
     * @param chapterId 章节ID
     * @return 内容列表
     */
    @Select({
            "SELECT ",
            "  ct.id, ",
            "  ct.chapter_id AS chapterId, ",
            "  ct.title, ",
            "  ct.content_type AS contentType, ",
            "  ct.content_url AS contentUrl, ",
            "  ct.content_text AS contentText, ",
            "  ct.duration, ",
            "  ct.order_num AS orderNum ",
            "FROM chapter_contents ct ",
            "WHERE ct.chapter_id = #{chapterId} AND ct.status = 1 ",
            "ORDER BY ct.order_num ASC"
    })
    List<Map<String, Object>> findChapterContents(@Param("chapterId") Long chapterId);

    /**
     * 获取学生的学习笔记列表
     *
     * @param studentId 学生ID
     * @param courseId 课程ID (可选)
     * @param chapterId 章节ID (可选)
     * @return 笔记列表
     */
    @Select({
            "<script>",
            "SELECT ",
            "  ln.id, ",
            "  ln.student_id AS userId, ",
            "  u.real_name AS userName, ",
            "  u.avatar AS userAvatar, ",
            "  ln.course_id AS courseId, ",
            "  c.title AS courseTitle, ",
            "  ln.chapter_id AS chapterId, ",
            "  cc.title AS chapterTitle, ",
            "  ln.content_id AS contentId, ",
            "  ct.title AS contentTitle, ",
            "  ln.note_content AS noteContent, ",
            "  ln.time_point AS noteTime, ",
            "  ln.created_at AS createdAt, ",
            "  ln.updated_at AS updatedAt ",
            "FROM learning_notes ln ",
            "INNER JOIN users u ON ln.student_id = u.user_id ",
            "INNER JOIN courses c ON ln.course_id = c.id ",
            "LEFT JOIN course_chapters cc ON ln.chapter_id = cc.id ",
            "LEFT JOIN chapter_contents ct ON ln.content_id = ct.id ",
            "WHERE ln.student_id = #{studentId} ",
            "<if test='courseId != null'>",
            "  AND ln.course_id = #{courseId} ",
            "</if>",
            "<if test='chapterId != null'>",
            "  AND ln.chapter_id = #{chapterId} ",
            "</if>",
            "ORDER BY ln.created_at DESC",
            "</script>"
    })
    List<LearningNoteDTO> findLearningNotes(@Param("studentId") Long studentId, 
                                          @Param("courseId") Long courseId,
                                          @Param("chapterId") Long chapterId);

    /**
     * 添加学习笔记
     *
     * @param note 笔记对象
     * @return 影响的行数
     */
    @Insert("INSERT INTO learning_notes (student_id, course_id, chapter_id, content_id, note_content, time_point, created_at, updated_at) " +
            "VALUES (#{studentId}, #{courseId}, #{chapterId}, #{contentId}, #{noteContent}, #{timePoint}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int addLearningNote(LearningNote note);

    /**
     * 更新学习笔记
     *
     * @param note 笔记对象
     * @return 影响的行数
     */
    @Update("UPDATE learning_notes SET note_content = #{noteContent}, time_point = #{timePoint}, updated_at = NOW() " +
            "WHERE id = #{id} AND student_id = #{studentId}")
    int updateLearningNote(LearningNote note);

    /**
     * 删除学习笔记
     *
     * @param noteId 笔记ID
     * @param userId 用户ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM learning_notes WHERE id = #{noteId} AND student_id = #{userId}")
    int deleteLearningNote(@Param("noteId") Long noteId, @Param("userId") Long userId);

    /**
     * 获取章节评价
     *
     * @param studentId 学生ID
     * @param chapterId 章节ID
     * @return 评价对象
     */
    @Select("SELECT * FROM chapter_ratings WHERE student_id = #{studentId} AND chapter_id = #{chapterId}")
    ChapterRating findChapterRating(@Param("studentId") Long studentId, @Param("chapterId") Long chapterId);

    /**
     * 添加或更新章节评价
     *
     * @param rating 评价对象
     * @return 影响的行数
     */
    @Insert({
            "INSERT INTO chapter_ratings (student_id, course_id, chapter_id, rating, comment, created_at, updated_at) ",
            "VALUES (#{studentId}, #{courseId}, #{chapterId}, #{rating}, #{feedback}, NOW(), NOW()) ",
            "ON DUPLICATE KEY UPDATE ",
            "  rating = #{rating}, ",
            "  comment = #{feedback}, ",
            "  updated_at = NOW()"
    })
    int saveChapterRating(ChapterRating rating);

    /**
     * 获取章节评价列表
     *
     * @param chapterId 章节ID
     * @return 评价列表
     */
    @Select({
            "SELECT ",
            "  cr.id, ",
            "  cr.student_id AS userId, ",
            "  u.real_name AS userName, ",
            "  u.avatar AS userAvatar, ",
            "  cr.course_id AS courseId, ",
            "  cr.chapter_id AS chapterId, ",
            "  cc.title AS chapterTitle, ",
            "  cr.rating, ",
            "  cr.comment AS feedback, ",
            "  cr.created_at AS createdAt ",
            "FROM chapter_ratings cr ",
            "INNER JOIN users u ON cr.student_id = u.user_id ",
            "INNER JOIN course_chapters cc ON cr.chapter_id = cc.id ",
            "WHERE cr.chapter_id = #{chapterId} ",
            "ORDER BY cr.created_at DESC"
    })
    List<ChapterRatingDTO> findChapterRatings(@Param("chapterId") Long chapterId);

    /**
     * 获取章节的平均评分
     *
     * @param chapterId 章节ID
     * @return 平均评分
     */
    @Select("SELECT IFNULL(ROUND(AVG(rating)), 0) FROM chapter_ratings WHERE chapter_id = #{chapterId}")
    Integer getChapterAverageRating(@Param("chapterId") Long chapterId);

    /**
     * 更新学习统计详情
     *
     * @param stats 统计对象
     * @return 影响的行数
     */
    @Insert({
            "INSERT INTO learning_statistics_detail ",
            "(student_id, course_id, chapter_id, duration, visit_count, last_visit_time, is_completed, updated_at) ",
            "VALUES (#{stats.studentId}, #{stats.courseId}, #{stats.chapterId}, #{stats.totalTime}, #{stats.visitCount}, #{stats.lastVisitTime}, ",
            "  #{stats.completionStatus}, NOW()) ",
            "ON DUPLICATE KEY UPDATE ",
            "  duration = duration + #{stats.totalTime}, ",
            "  visit_count = visit_count + #{stats.visitCount}, ",
            "  last_visit_time = #{stats.lastVisitTime}, ",
            "  is_completed = #{stats.completionStatus}, ",
            "  updated_at = NOW()"
    })
    int updateLearningStatistics(@Param("stats") LearningStatisticsDetail stats);

    /**
     * 获取课程学习统计
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 学习统计数据
     */
    @Select({
            "SELECT ",
            "  c.id AS courseId, ",
            "  c.title AS courseTitle, ",
            "  (SELECT COUNT(*) FROM course_chapters WHERE course_id = #{courseId}) AS totalChapters, ",
            "  (SELECT COUNT(*) FROM learning_statistics_detail WHERE student_id = #{studentId} AND course_id = #{courseId} AND is_completed = 1) AS completedChapters, ",
            "  IFNULL((SELECT SUM(duration) FROM learning_statistics_detail WHERE student_id = #{studentId} AND course_id = #{courseId}), 0) AS totalTime, ",
            "  (SELECT IFNULL(ROUND(AVG(rating)), 0) FROM chapter_ratings WHERE student_id = #{studentId} AND course_id = #{courseId}) AS averageRating, ",
            "  (SELECT MAX(last_visit_time) FROM learning_statistics_detail WHERE student_id = #{studentId} AND course_id = #{courseId}) AS lastStudyTime ",
            "FROM courses c ",
            "WHERE c.id = #{courseId}"
    })
    LearningStatisticsDTO getCourseStatistics(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    /**
     * 获取课程章节学习统计
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 章节统计列表
     */
    @Select({
            "SELECT ",
            "  cc.id AS chapterId, ",
            "  cc.title AS chapterTitle, ",
            "  IFNULL(lsd.duration, 0) AS totalTime, ",
            "  IFNULL(lsd.visit_count, 0) AS visitCount, ",
            "  lsd.last_visit_time AS lastVisitTime, ",
            "  IFNULL(lsd.is_completed, 0) = 1 AS isCompleted, ",
            "  IFNULL((SELECT rating FROM chapter_ratings WHERE student_id = #{studentId} AND chapter_id = cc.id), 0) AS rating ",
            "FROM course_chapters cc ",
            "LEFT JOIN learning_statistics_detail lsd ON cc.id = lsd.chapter_id AND lsd.student_id = #{studentId} ",
            "WHERE cc.course_id = #{courseId} ",
            "ORDER BY cc.order_num"
    })
    List<LearningStatisticsDTO.ChapterStatDTO> getChapterStatistics(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
} 