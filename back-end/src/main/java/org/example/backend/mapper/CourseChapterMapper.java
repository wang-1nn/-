package org.example.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.backend.entity.dto.CourseChapterDTO;
import org.example.backend.entity.pojo.CourseChapter;

import java.util.List;

/**
 * 课程章节Mapper接口
 */
@Mapper
public interface CourseChapterMapper {
    
    /**
     * 根据课程ID查询章节列表，按排序序号升序排列
     * 
     * @param courseId 课程ID
     * @return 章节列表
     */
    @Select("SELECT * FROM course_chapters WHERE course_id = #{courseId} AND status = 1 ORDER BY order_num ASC")
    List<CourseChapter> findByCourseId(@Param("courseId") Long courseId);
    
    /**
     * 获取章节的平均评分
     * 
     * @param chapterId 章节ID
     * @return 平均评分
     */
    @Select("SELECT AVG(rating) FROM chapter_ratings WHERE chapter_id = #{chapterId}")
    Double getAverageRating(@Param("chapterId") Long chapterId);
    
    /**
     * 获取学习该章节的学生人数
     * 
     * @param chapterId 章节ID
     * @return 学生人数
     */
    @Select("SELECT COUNT(DISTINCT student_id) FROM learning_statistics_detail WHERE chapter_id = #{chapterId}")
    Integer getStudentCount(@Param("chapterId") Long chapterId);
    
    /**
     * 根据课程ID获取所有章节及其内容的详细信息
     * 
     * @param courseId 课程ID
     * @return 章节DTO列表
     */
    List<CourseChapterDTO> getChapterDetailsByCourseId(@Param("courseId") Long courseId);
    
    /**
     * 获取学生在课程中的章节学习情况
     * 
     * @param courseId 课程ID
     * @param studentId 学生ID
     * @return 章节DTO列表
     */
    List<CourseChapterDTO> getStudentChapterProgress(@Param("courseId") Long courseId, @Param("studentId") Long studentId);
    
    /**
     * 根据ID查询章节
     * 
     * @param id 章节ID
     * @return 章节对象
     */
    CourseChapter findById(@Param("id") Long id);
    
    /**
     * 插入新章节
     * 
     * @param chapter 章节对象
     * @return 影响的行数
     */
    int insert(CourseChapter chapter);
    
    /**
     * 更新章节信息
     * 
     * @param chapter 章节对象
     * @return 影响的行数
     */
    int update(CourseChapter chapter);
    
    /**
     * 删除章节
     * 
     * @param id 章节ID
     * @return 影响的行数
     */
    int deleteById(@Param("id") Long id);
} 