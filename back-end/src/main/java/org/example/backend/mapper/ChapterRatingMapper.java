package org.example.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.backend.entity.dto.ChapterRatingDTO;
import org.example.backend.entity.pojo.ChapterRating;

import java.util.List;

/**
 * 章节评价Mapper接口
 */
@Mapper
public interface ChapterRatingMapper {
    
    /**
     * 插入章节评价
     * 
     * @param rating 章节评价
     * @return 影响的行数
     */
    int insert(ChapterRating rating);
    
    /**
     * 更新章节评价
     * 
     * @param rating 章节评价
     * @return 影响的行数
     */
    int update(ChapterRating rating);
    
    /**
     * 根据ID删除章节评价
     * 
     * @param id 评价ID
     * @return 影响的行数
     */
    int deleteById(Long id);
    
    /**
     * 根据学生ID和章节ID查询评价
     * 
     * @param studentId 学生ID
     * @param chapterId 章节ID
     * @return 章节评价
     */
    @Select("SELECT * FROM chapter_ratings WHERE student_id = #{studentId} AND chapter_id = #{chapterId}")
    ChapterRating findByStudentAndChapter(@Param("studentId") Long studentId, @Param("chapterId") Long chapterId);
    
    /**
     * 根据章节ID查询评价列表
     * 
     * @param chapterId 章节ID
     * @return 评价列表
     */
    @Select("SELECT * FROM chapter_ratings WHERE chapter_id = #{chapterId} ORDER BY created_at DESC")
    List<ChapterRating> findByChapterId(@Param("chapterId") Long chapterId);
    
    /**
     * 根据章节ID获取评价详情列表（包含学生信息）
     * 
     * @param chapterId 章节ID
     * @return 评价DTO列表
     */
    List<ChapterRatingDTO> getChapterRatingDetails(@Param("chapterId") Long chapterId);
    
    /**
     * 根据学生ID查询该学生的所有评价
     * 
     * @param studentId 学生ID
     * @return 评价DTO列表
     */
    List<ChapterRatingDTO> getStudentRatings(@Param("studentId") Long studentId);
} 