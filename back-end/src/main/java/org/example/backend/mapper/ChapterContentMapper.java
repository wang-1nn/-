package org.example.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.backend.entity.dto.ChapterContentDTO;
import org.example.backend.entity.pojo.ChapterContent;

import java.util.List;

/**
 * 章节内容Mapper接口
 */
@Mapper
public interface ChapterContentMapper {
    
    /**
     * 插入章节内容
     * 
     * @param content 章节内容
     * @return 影响的行数
     */
    int insert(ChapterContent content);
    
    /**
     * 更新章节内容
     * 
     * @param content 章节内容
     * @return 影响的行数
     */
    int update(ChapterContent content);
    
    /**
     * 根据ID删除章节内容
     * 
     * @param id 内容ID
     * @return 影响的行数
     */
    int deleteById(Long id);
    
    /**
     * 根据章节ID删除章节所有内容
     * 
     * @param chapterId 章节ID
     * @return 影响的行数
     */
    int deleteByChapterId(Long chapterId);
    
    /**
     * 根据ID查询章节内容
     * 
     * @param id 内容ID
     * @return 章节内容
     */
    ChapterContent selectById(Long id);
    
    /**
     * 根据章节ID查询章节内容列表，按排序序号升序排列
     * 
     * @param chapterId 章节ID
     * @return 内容列表
     */
    @Select("SELECT * FROM chapter_contents WHERE chapter_id = #{chapterId} AND status = 1 ORDER BY order_num ASC")
    List<ChapterContent> findByChapterId(@Param("chapterId") Long chapterId);
    
    /**
     * 获取学生的内容学习进度
     * 
     * @param chapterId 章节ID
     * @param studentId 学生ID
     * @return 章节内容DTO列表
     */
    List<ChapterContentDTO> getStudentContentProgress(@Param("chapterId") Long chapterId, @Param("studentId") Long studentId);

    /**
     * 根据章节ID查询章节内容列表，返回Map格式，用于调试
     * 
     * @param chapterId 章节ID
     * @return 内容列表
     */
    @Select({
            "SELECT ",
            "  id, ",
            "  chapter_id AS chapterId, ",
            "  title, ",
            "  content_type AS contentType, ",
            "  content_url AS contentUrl, ",
            "  content_text AS contentText, ",
            "  duration, ",
            "  order_num AS orderNum, ",
            "  status, ",
            "  created_at AS createdAt ",
            "FROM chapter_contents ",
            "WHERE chapter_id = #{chapterId} ",
            "ORDER BY order_num ASC"
    })
    List<java.util.Map<String, Object>> selectChapterContents(@Param("chapterId") Long chapterId);
} 