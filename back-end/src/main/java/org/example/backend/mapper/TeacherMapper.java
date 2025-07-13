package org.example.backend.mapper;


import org.apache.ibatis.annotations.*;
import org.example.backend.entity.pojo.AiChatMemory;
import org.example.backend.entity.pojo.TodoTask;
import org.example.backend.entity.vo.AnnouncementVO;

import java.util.List;
import java.util.Map;

@Mapper
public interface TeacherMapper {

    // ========== Dashboard: Summary Stats ==========
    @Select("SELECT real_name FROM users WHERE user_id = #{userId}")
    String findUsernameByUserId(@Param("userId") int userId);

    /**
     * 根据教师ID统计其创建的课程总数
     * @param teacherId 教师ID
     * @return 课程数量
     */
    @Select("SELECT COUNT(*) FROM courses WHERE teacher_id = #{teacherId}")
    Integer countCoursesByTeacherId(@Param("teacherId") int teacherId);

    @Select("SELECT COUNT(*) FROM courses WHERE teacher_id = #{teacherId} AND created_at >= CURDATE() - INTERVAL 7 DAY")
    Integer countCoursesTrendByTeacherId(@Param("teacherId") int teacherId);

    @Select("SELECT COUNT(DISTINCT co.course_id) FROM course_offerings co JOIN courses c ON co.course_id = c.id WHERE c.teacher_id = #{teacherId} AND co.status = 1")
    Integer countActiveCoursesByTeacherId(@Param("teacherId") int teacherId);

    /**
     * 根据教师ID统计其所有课程下的学生总数（去重）
     * @param teacherId 教师ID
     * @return 学生总数
     */
    @Select("SELECT COUNT(DISTINCT e.student_id) FROM courses c JOIN course_offerings co ON c.id = co.course_id JOIN enrollments e ON co.id = e.offering_id WHERE c.teacher_id = #{teacherId}")
    Integer countStudentsByTeacherId(@Param("teacherId") int teacherId);

    @Select("SELECT COUNT(DISTINCT u.user_id) FROM users u JOIN enrollments e ON u.user_id = e.student_id JOIN course_offerings co ON e.offering_id = co.id JOIN courses c ON co.course_id = c.id WHERE c.teacher_id = #{teacherId} AND u.last_login >= CURDATE() - INTERVAL 30 DAY")
    Integer countActiveStudentsByTeacherId(@Param("teacherId") int teacherId);

    @Select("SELECT count(*) FROM lesson_plans WHERE creator_id = #{teacherId}")
    Integer countPlansByTeacherId(@Param("teacherId") int teacherId);

    @Select("SELECT count(*) FROM lesson_plans WHERE creator_id = #{teacherId} AND created_at >= CURDATE() - INTERVAL 7 DAY")
    Integer countPlansTrendByTeacherId(@Param("teacherId") int teacherId);

    @Select("SELECT count(*) FROM teaching_resources WHERE created_by = #{teacherId}")
    Integer countResourcesByTeacherId(@Param("teacherId") int teacherId);

    @Select("SELECT count(*) FROM teaching_resources WHERE created_by = #{teacherId} AND created_at >= CURDATE() - INTERVAL 7 DAY")
    Integer countResourcesTrendByTeacherId(@Param("teacherId") int teacherId);


    // ========== Dashboard: Announcements ==========
    @Select("SELECT p.id, p.title, p.content, p.created_at as timestamp, c.title as courseName " +
            "FROM lesson_plans p JOIN courses c ON p.course_id = c.id " +
            "WHERE p.creator_id = #{teacherId} ORDER BY p.created_at DESC LIMIT 5")
    List<AnnouncementVO> findAnnouncementsByTeacherId(@Param("teacherId") int teacherId);

    // ========== Dashboard: Tasks (ToDo) ==========
    @Select("SELECT * FROM todo_tasks WHERE user_id = #{teacherId} ORDER BY due_date ASC")
    List<TodoTask> findTasksByTeacherId(@Param("teacherId") int teacherId);

    @Insert("INSERT INTO todo_tasks(user_id, task_type, title, status, due_date, created_at) " +
            "VALUES(#{userId}, #{taskType}, #{title}, #{status}, #{dueDate}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertTask(TodoTask task);

    @Update("UPDATE todo_tasks SET status = #{status} WHERE id = #{taskId}")
    void updateTaskStatus(@Param("taskId") long taskId, @Param("status") String status);

    @Delete("DELETE FROM todo_tasks WHERE id = #{taskId}")
    void deleteTask(@Param("taskId") long taskId);


    // ========== Dashboard: Charts ==========
    @Select("SELECT DATE(start_time) as activity_date, COUNT(*) as activity_count FROM learning_records lr " +
            "JOIN enrollments e ON lr.user_id = e.student_id " +
            "JOIN course_offerings co ON e.offering_id = co.id " +
            "JOIN courses c ON co.course_id = c.id " +
            "WHERE c.teacher_id = #{teacherId} AND lr.start_time >= CURDATE() - INTERVAL 6 DAY " +
            "GROUP BY activity_date ORDER BY activity_date ASC")
    List<Map<String, Object>> getStudentActivityLast7Days(@Param("teacherId") int teacherId);

    @Select("SELECT module, COUNT(*) as access_count FROM usage_statistics WHERE user_id = #{teacherId} GROUP BY module ORDER BY access_count DESC")
    List<Map<String, Object>> getResourceAccessCountByModule(@Param("teacherId") int teacherId);


    @Select("SELECT * FROM spring_ai_chat_memory WHERE conversation_id = #{uid}")
    List<AiChatMemory> getChatHistory(String uid);
}
