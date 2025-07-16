package org.example.backend.mapper;

import org.apache.ibatis.annotations.*;
import org.example.backend.entity.pojo.Answer;
import org.example.backend.entity.pojo.Exam;
import org.example.backend.entity.pojo.Question;
import org.example.backend.entity.pojo.Submission;
import org.example.backend.entity.vo.ExamVO;
import org.example.backend.entity.vo.ExamListVO;
import org.example.backend.entity.vo.QuestionSelectionVO;
import org.example.backend.entity.vo.CourseClassVO;
import org.example.backend.entity.pojo.Question;

import java.util.List;
import java.util.Map;

/**
 * 考试相关数据库操作接口
 */
@Mapper
public interface ExamMapper {
    
    /**
     * 获取学生可参加的考试列表
     *
     * @param studentId 学生ID
     * @return 考试列表
     */
    List<Exam> findExamsByStudentId(@Param("studentId") Long studentId);
    
    /**
     * 获取考试详情
     *
     * @param examId 考试ID
     * @param studentId 学生ID
     * @return 考试详情
     */
    Exam findExamById(@Param("examId") Long examId, @Param("studentId") Long studentId);
    
    /**
     * 获取考试的题目列表
     *
     * @param examId 考试ID
     * @return 题目列表
     */
    List<Question> findQuestionsByExamId(Long examId);
    
    /**
     * 获取学生的考试提交记录
     *
     * @param studentId 学生ID
     * @param examId 考试ID
     * @return 提交记录
     */
    Submission findSubmissionByStudentAndExam(@Param("studentId") Long studentId, @Param("examId") Long examId);
    
    /**
     * 创建考试提交记录
     *
     * @param submission 提交记录
     * @return 影响行数
     */
    int createSubmission(Submission submission);
    
    /**
     * 更新考试提交记录
     *
     * @param submission 提交记录
     * @return 影响行数
     */
    int updateSubmission(Submission submission);
    
    /**
     * 获取学生的答案列表
     *
     * @param submissionId 提交记录ID
     * @return 答案列表
     */
    List<Answer> findAnswersBySubmissionId(Long submissionId);
    
    /**
     * 保存学生答案
     *
     * @param answer 答案
     * @return 影响行数
     */
    int saveAnswer(Answer answer);
    
    /**
     * 更新学生答案
     *
     * @param answer 答案
     * @return 影响行数
     */
    int updateAnswer(Answer answer);
    
    /**
     * 获取学生的成绩列表
     *
     * @param studentId 学生ID
     * @return 成绩列表
     */
    List<Map<String, Object>> findScoresByStudentId(Long studentId);
    
    /**
     * 获取学生的成绩详情
     *
     * @param submissionId 提交记录ID
     * @return 成绩详情
     */
    Map<String, Object> findScoreDetailBySubmissionId(Long submissionId);
    
    /**
     * 获取学生正在进行中的考试
     *
     * @param studentId 学生ID
     * @return 考试列表
     */
    List<Exam> findOngoingExamsByStudentId(@Param("studentId") Long studentId);
    
    /**
     * 获取学生已完成的考试成绩，按提交时间排序
     *
     * @param studentId 学生ID
     * @param limit 限制返回的记录数
     * @return 成绩列表
     */
    List<Map<String, Object>> findCompletedScoresByStudentId(@Param("studentId") Long studentId, @Param("limit") int limit);

    // ==================== 教师端方法 ====================

    /**
     * 创建考试
     */
    @Insert("""
            INSERT INTO exams(offering_id, creator_id, title, exam_type, start_time, end_time)
            VALUES(#{offeringId}, #{creatorId}, #{title}, #{examType}, #{startTime}, #{endTime})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertExam(Exam exam);

    /**
     * 获取教师的考试列表
     */
    @Select("""
            SELECT e.id, e.title, e.exam_type, e.start_time, e.end_time,
                   c.title as course_name, cl.name as class_name,
                   u.username as creator_name
            FROM exams e
            JOIN course_offerings co ON e.offering_id = co.id
            JOIN courses c ON co.course_id = c.id
            JOIN classes cl ON co.class_id = cl.id
            JOIN users u ON e.creator_id = u.user_id
            WHERE e.creator_id = #{teacherId}
            ORDER BY e.start_time DESC
            """)
    List<ExamVO> getExamsByTeacher(@Param("teacherId") Long teacherId);

    /**
     * 根据科目获取题目列表
     */
    @Select("""
            <script>
            SELECT q.question_id, q.content, q.question_type, q.subject, q.difficulty,
                   q.answer, q.analysis, q.is_ai_generated, q.created_at
            FROM questions q
            WHERE 
                q.subject = #{subject}         
            ORDER BY q.question_id DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<QuestionSelectionVO> getQuestionsForSelection(
            @Param("subject") String subject,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset
    );

    /**
     * 获取教师所教的课程和班级
     */
    @Select("""
            SELECT DISTINCT c.id as course_id, c.title as course_name, c.subject
            FROM courses c
            JOIN course_offerings co ON c.id = co.course_id
            WHERE c.teacher_id = #{teacherId}

            ORDER BY c.title
            """)
    List<CourseClassVO> getTeacherCourses(@Param("teacherId") Long teacherId);

    /**
     * 根据课程ID获取班级列表
     */
    @Select("""
            SELECT cl.id as class_id, cl.name as class_name, cl.grade, cl.major,
                   0 as student_count
            FROM classes cl
            JOIN course_offerings co ON cl.id = co.class_id
            WHERE co.course_id = #{courseId}
            """)
    List<CourseClassVO.ClassInfo> getClassesByCourse(@Param("courseId") Long courseId);

    /**
     * 获取开课ID
     */
    @Select("""
            SELECT id FROM course_offerings
            WHERE course_id = #{courseId} AND class_id = #{classId}
            """)
    Long getOfferingId(@Param("courseId") Long courseId, @Param("classId") Long classId);

    /**
     * 统计题目数量
     */
    @Select("""
            <script>
            SELECT COUNT(*) FROM questions q
            WHERE q.created_by = #{createdBy}
            <if test="subject != null and subject != ''">
                AND q.subject = #{subject}
            </if>
            </script>
            """)
    Integer countQuestions(@Param("createdBy") String createdBy,
                          @Param("subject") String subject);

    /**
     * 获取教师创建的题目的所有学科列表
     */
    @Select("""
            SELECT DISTINCT q.subject
            FROM questions q
            WHERE q.created_by = #{createdBy}
            AND q.subject IS NOT NULL
            AND q.subject != ''
            ORDER BY q.subject
            """)
    List<String> getQuestionSubjects(@Param("createdBy") String createdBy);

    /**
     * 获取教师的考试列表
     */
    @Select("""
            <script>
            SELECT
                e.id,
                e.title,
                e.exam_type,
                e.start_time,
                e.end_time,
                c.title as course_name,
                cl.name as class_name,
                c.subject,
                co.id as offering_id,
                co.course_id,
                co.class_id,
                CASE
                    WHEN e.start_time IS NULL OR e.end_time IS NULL THEN '草稿'
                    WHEN NOW() &lt; e.start_time THEN '即将开始'
                    WHEN NOW() BETWEEN e.start_time AND e.end_time THEN '进行中'
                    ELSE '已结束'
                END as status,
                (SELECT COUNT(*) FROM student_exams se WHERE se.exam_id = e.id) as submission_count,
                e.start_time as created_at
            FROM exams e
            INNER JOIN course_offerings co ON e.offering_id = co.id
            INNER JOIN courses c ON co.course_id = c.id
            INNER JOIN classes cl ON co.class_id = cl.id
            WHERE c.teacher_id = #{teacherId}
            <if test="status != null and status != ''">
                <choose>
                    <when test="status == '即将开始'">
                        AND e.start_time IS NOT NULL AND e.end_time IS NOT NULL AND NOW() &lt; e.start_time
                    </when>
                    <when test="status == '进行中'">
                        AND e.start_time IS NOT NULL AND e.end_time IS NOT NULL AND NOW() BETWEEN e.start_time AND e.end_time
                    </when>
                    <when test="status == '已结束'">
                        AND e.start_time IS NOT NULL AND e.end_time IS NOT NULL AND NOW() &gt; e.end_time
                    </when>
                    <when test="status == '草稿'">
                        AND (e.start_time IS NULL OR e.end_time IS NULL)
                    </when>
                </choose>
            </if>
            <if test="keyword != null and keyword != ''">
                AND (e.title LIKE CONCAT('%', #{keyword}, '%')
                     OR c.title LIKE CONCAT('%', #{keyword}, '%')
                     OR cl.name LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY
                CASE
                    WHEN e.start_time IS NULL THEN 0
                    ELSE 1
                END,
                e.start_time DESC,
                e.id DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<ExamListVO> getTeacherExams(
            @Param("teacherId") Long teacherId,
            @Param("status") String status,
            @Param("keyword") String keyword,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset
    );

    /**
     * 统计教师的考试数量
     */
    @Select("""
            <script>
            SELECT COUNT(*)
            FROM exams e
            INNER JOIN course_offerings co ON e.offering_id = co.id
            INNER JOIN courses c ON co.course_id = c.id
            INNER JOIN classes cl ON co.class_id = cl.id
            WHERE c.teacher_id = #{teacherId}
            <if test="status != null and status != ''">
                <choose>
                    <when test="status == '即将开始'">
                        AND e.start_time IS NOT NULL AND e.end_time IS NOT NULL AND NOW() &lt; e.start_time
                    </when>
                    <when test="status == '进行中'">
                        AND e.start_time IS NOT NULL AND e.end_time IS NOT NULL AND NOW() BETWEEN e.start_time AND e.end_time
                    </when>
                    <when test="status == '已结束'">
                        AND e.start_time IS NOT NULL AND e.end_time IS NOT NULL AND NOW() &gt; e.end_time
                    </when>
                    <when test="status == '草稿'">
                        AND (e.start_time IS NULL OR e.end_time IS NULL)
                    </when>
                </choose>
            </if>
            <if test="keyword != null and keyword != ''">
                AND (e.title LIKE CONCAT('%', #{keyword}, '%')
                     OR c.title LIKE CONCAT('%', #{keyword}, '%')
                     OR cl.name LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    Integer countTeacherExams(
            @Param("teacherId") Long teacherId,
            @Param("status") String status,
            @Param("keyword") String keyword
    );

    /**
     * 根据考试ID获取考试详情（包含课程和班级信息）
     */
    @Select("""
            SELECT
                e.id,
                e.title,
                e.exam_type,
                e.start_time,
                e.end_time,
                e.offering_id,
                e.creator_id,
                c.id as course_id,
                c.title as course_name,
                c.subject,
                cl.id as class_id,
                cl.name as class_name,
                CASE
                    WHEN e.start_time IS NULL OR e.end_time IS NULL THEN '草稿'
                    WHEN NOW() < e.start_time THEN '即将开始'
                    WHEN NOW() BETWEEN e.start_time AND e.end_time THEN '进行中'
                    ELSE '已结束'
                END as status
            FROM exams e
            INNER JOIN course_offerings co ON e.offering_id = co.id
            INNER JOIN courses c ON co.course_id = c.id
            INNER JOIN classes cl ON co.class_id = cl.id
            WHERE e.id = #{examId} AND c.teacher_id = #{teacherId}
            """)
    ExamVO getExamDetail(@Param("examId") Long examId, @Param("teacherId") Long teacherId);

    /**
     * 获取考试的题目列表
     */
    @Select("""
            SELECT
                q.id,
                q.question_id,
                q.exam_id,
                q.question_type,
                q.content,
                q.options,
                q.answer,
                q.analysis,
                q.score,
                q.difficulty,
                q.subject,
                q.created_by,
                q.is_ai_generated,
                q.created_at
            FROM questions q
            WHERE q.exam_id = #{examId}
            ORDER BY q.created_at ASC
            """)
    List<Question> getExamQuestions(@Param("examId") String examId);

    /**
     * 删除考试
     */
    @Delete("DELETE FROM exams WHERE id = #{examId} AND creator_id = #{teacherId}")
    Integer deleteExam(@Param("examId") Long examId, @Param("teacherId") Long teacherId);

    /**
     * 删除考试相关的题目
     */
    @Delete("DELETE FROM questions WHERE exam_id = #{examId}")
    Integer deleteExamQuestions(@Param("examId") String examId);
}