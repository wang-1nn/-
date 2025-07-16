package org.example.backend.mapper;

import org.apache.ibatis.annotations.*;
import org.example.backend.entity.pojo.LessonPlan;
import org.example.backend.entity.pojo.LessonTemplate;
import org.example.backend.entity.pojo.TeachingResource;

import java.util.List;

/**
 * 教案相关数据访问接口
 */
@Mapper
public interface LessonPlanMapper {

    // ========================================
    // 教案相关操作
    // ========================================

    /**
     * 保存教案
     */
    @Insert("""
        INSERT INTO lesson_plans (teacher_id, title, subject, grade, content, markdown_content, 
                                 outline_content, template_type, status, duration, difficulty_level, tags)
        VALUES (#{teacherId}, #{title}, #{subject}, #{grade}, #{content}, #{markdownContent}, 
                #{outlineContent}, #{templateType}, #{status}, #{duration}, #{difficultyLevel}, #{tags,typeHandler=org.example.backend.config.JsonTypeHandler})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLessonPlan(LessonPlan lessonPlan);

    /**
     * 更新教案
     */
    @Update("""
        UPDATE lesson_plans 
        SET title = #{title}, subject = #{subject}, grade = #{grade}, content = #{content}, 
            markdown_content = #{markdownContent}, template_type = #{templateType}, 
            status = #{status}, duration = #{duration}, difficulty_level = #{difficultyLevel}, 
            tags = #{tags,typeHandler=org.example.backend.config.JsonTypeHandler}, updated_at = NOW()
        WHERE id = #{id} AND teacher_id = #{teacherId}
        """)
    int updateLessonPlan(LessonPlan lessonPlan);

    /**
     * 根据ID获取教案
     */
    @Select("""
        SELECT * FROM lesson_plans 
        WHERE id = #{id} AND teacher_id = #{teacherId}
        """)
    @Results({
            @Result(property = "tags", column = "tags", typeHandler = org.example.backend.config.JsonTypeHandler.class)
    })
    LessonPlan getLessonPlanById(@Param("id") Long id, @Param("teacherId") Long teacherId);

    /**
     * 获取教师的教案列表
     */
    @Select("""
        <script>
        SELECT * FROM lesson_plans 
        WHERE teacher_id = #{teacherId}
        <if test="status != null and status != ''">
            AND status = #{status}
        </if>
        <if test="subject != null and subject != ''">
            AND subject = #{subject}
        </if>
        ORDER BY updated_at DESC
        LIMIT #{offset}, #{limit}
        </script>
        """)
    @Results({
            @Result(property = "tags", column = "tags", typeHandler = org.example.backend.config.JsonTypeHandler.class)
    })
    List<LessonPlan> getLessonPlansByTeacher(@Param("teacherId") Long teacherId,
                                             @Param("status") String status,
                                             @Param("subject") String subject,
                                             @Param("offset") int offset,
                                             @Param("limit") int limit);

    /**
     * 获取教案总数
     */
    @Select("""
        <script>
        SELECT COUNT(*) FROM lesson_plans 
        WHERE teacher_id = #{teacherId}
        <if test="status != null and status != ''">
            AND status = #{status}
        </if>
        <if test="subject != null and subject != ''">
            AND subject = #{subject}
        </if>
        </script>
        """)
    int countLessonPlansByTeacher(@Param("teacherId") Long teacherId,
                                  @Param("status") String status,
                                  @Param("subject") String subject);

    /**
     * 删除教案
     */
    @Delete("DELETE FROM lesson_plans WHERE id = #{id} AND teacher_id = #{teacherId}")
    int deleteLessonPlan(@Param("id") Long id, @Param("teacherId") Long teacherId);

    // ========================================
    // 教案模板相关操作
    // ========================================

    /**
     * 获取可用模板列表
     */
    @Select("""
        SELECT * FROM lesson_templates 
        WHERE is_public = TRUE OR teacher_id = #{teacherId} OR is_system = TRUE
        ORDER BY is_system DESC, usage_count DESC, created_at DESC
        """)
    List<LessonTemplate> getAvailableTemplates(@Param("teacherId") Long teacherId);

    /**
     * 根据类型获取模板
     */
    @Select("""
        SELECT * FROM lesson_templates 
        WHERE template_type = #{templateType} 
        AND (is_public = TRUE OR teacher_id = #{teacherId} OR is_system = TRUE)
        ORDER BY is_system DESC, usage_count DESC
        LIMIT 1
        """)
    LessonTemplate getTemplateByType(@Param("templateType") String templateType,
                                     @Param("teacherId") Long teacherId);

    /**
     * 保存自定义模板
     */
    @Insert("""
        INSERT INTO lesson_templates (teacher_id, name, description, template_type, content, is_public)
        VALUES (#{teacherId}, #{name}, #{description}, #{templateType}, #{content}, #{isPublic})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertTemplate(LessonTemplate template);

    /**
     * 增加模板使用次数
     */
    @Update("UPDATE lesson_templates SET usage_count = usage_count + 1 WHERE id = #{templateId}")
    int incrementTemplateUsage(@Param("templateId") Long templateId);

    // ========================================
    // 教学资源相关操作
    // ========================================

    /**
     * 获取推荐的教学资源
     */
    @Select("""
        <script>
        SELECT * FROM teaching_resources 
        WHERE is_public = TRUE
        <if test="subject != null and subject != ''">
            AND subject = #{subject}
        </if>
        <if test="grade != null and grade != ''">
            AND grade LIKE CONCAT('%', #{grade}, '%')
        </if>
        <if test="resourceType != null and resourceType != ''">
            AND resource_type = #{resourceType}
        </if>
        ORDER BY quality_score DESC, view_count DESC
        LIMIT #{limit}
        </script>
        """)
    @Results({
            @Result(property = "tags", column = "tags", typeHandler = org.example.backend.config.JsonTypeHandler.class)
    })
    List<TeachingResource> getRecommendedResources(@Param("subject") String subject,
                                                   @Param("grade") String grade,
                                                   @Param("resourceType") String resourceType,
                                                   @Param("limit") int limit);

    /**
     * 关联教案和资源
     */
    @Insert("INSERT IGNORE INTO lesson_plan_resources (lesson_plan_id, resource_id) VALUES (#{lessonPlanId}, #{resourceId})")
    int linkLessonPlanResource(@Param("lessonPlanId") Long lessonPlanId, @Param("resourceId") Long resourceId);

    /**
     * 获取教案关联的资源
     */
    @Select("""
        SELECT r.* FROM teaching_resources r
        INNER JOIN lesson_plan_resources lpr ON r.id = lpr.resource_id
        WHERE lpr.lesson_plan_id = #{lessonPlanId}
        """)
    @Results({
            @Result(property = "tags", column = "tags", typeHandler = org.example.backend.config.JsonTypeHandler.class)
    })
    List<TeachingResource> getResourcesByLessonPlan(@Param("lessonPlanId") Long lessonPlanId);
}
