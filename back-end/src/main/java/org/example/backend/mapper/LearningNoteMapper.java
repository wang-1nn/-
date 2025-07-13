package org.example.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.backend.entity.dto.LearningNoteDTO;
import org.example.backend.entity.pojo.LearningNote;

import java.util.List;

/**
 * 学习笔记Mapper接口
 */
@Mapper
public interface LearningNoteMapper {
    
    /**
     * 插入学习笔记
     * 
     * @param note 学习笔记
     * @return 影响的行数
     */
    int insert(LearningNote note);
    
    /**
     * 更新学习笔记
     * 
     * @param note 学习笔记
     * @return 影响的行数
     */
    int update(LearningNote note);
    
    /**
     * 根据ID删除学习笔记
     * 
     * @param id 笔记ID
     * @return 影响的行数
     */
    int deleteById(Long id);
    
    /**
     * 根据ID查询学习笔记
     * 
     * @param id 笔记ID
     * @return 学习笔记
     */
    LearningNote selectById(Long id);
    
    /**
     * 根据学生ID和内容ID查询笔记
     * 
     * @param studentId 学生ID
     * @param contentId 内容ID
     * @return 学习笔记列表
     */
    @Select("SELECT * FROM learning_notes WHERE student_id = #{studentId} AND content_id = #{contentId} ORDER BY created_at DESC")
    List<LearningNote> findByStudentAndContent(@Param("studentId") Long studentId, @Param("contentId") Long contentId);
    
    /**
     * 根据学生ID和章节ID查询笔记
     * 
     * @param studentId 学生ID
     * @param chapterId 章节ID
     * @return 学习笔记列表
     */
    @Select("SELECT * FROM learning_notes WHERE student_id = #{studentId} AND chapter_id = #{chapterId} ORDER BY created_at DESC")
    List<LearningNote> findByStudentAndChapter(@Param("studentId") Long studentId, @Param("chapterId") Long chapterId);
    
    /**
     * 根据学生ID和课程ID查询笔记
     * 
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 学习笔记DTO列表
     */
    List<LearningNoteDTO> getStudentNotesByCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
    
    /**
     * 根据学生ID查询所有笔记
     * 
     * @param studentId 学生ID
     * @return 学习笔记DTO列表
     */
    List<LearningNoteDTO> getStudentAllNotes(@Param("studentId") Long studentId);
} 