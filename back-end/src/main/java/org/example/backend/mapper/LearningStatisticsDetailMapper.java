package org.example.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.backend.entity.dto.LearningStatisticsDetailDTO;
import org.example.backend.entity.pojo.LearningStatisticsDetail;

import java.util.List;

/**
 * 学习统计详情Mapper接口
 */
@Mapper
public interface LearningStatisticsDetailMapper {
    
    /**
     * 插入学习统计详情
     * 
     * @param detail 学习统计详情
     * @return 影响的行数
     */
    int insert(LearningStatisticsDetail detail);
    
    /**
     * 更新学习统计详情
     * 
     * @param detail 学习统计详情
     * @return 影响的行数
     */
    int update(LearningStatisticsDetail detail);
    
    /**
     * 根据学生ID和章节ID查询学习统计详情
     * 
     * @param studentId 学生ID
     * @param chapterId 章节ID
     * @return 学习统计详情
     */
    @Select("SELECT * FROM learning_statistics_detail WHERE user_id = #{studentId} AND chapter_id = #{chapterId}")
    LearningStatisticsDetail findByStudentAndChapter(@Param("studentId") Long studentId, @Param("chapterId") Long chapterId);
    
    /**
     * 更新学习时长
     * 
     * @param id 统计详情ID
     * @param addDuration 增加的时长（秒）
     * @return 影响的行数
     */
    @Update("UPDATE learning_statistics_detail SET total_time = total_time + #{addDuration}, updated_at = NOW() WHERE id = #{id}")
    int updateDuration(@Param("id") Long id, @Param("addDuration") Integer addDuration);
    
    /**
     * 更新访问次数
     * 
     * @param id 统计详情ID
     * @return 影响的行数
     */
    @Update("UPDATE learning_statistics_detail SET visit_count = visit_count + 1, last_visit_time = NOW(), updated_at = NOW() WHERE id = #{id}")
    int updateVisitCount(@Param("id") Long id);
    
    /**
     * 标记章节为已完成
     * 
     * @param id 统计详情ID
     * @return 影响的行数
     */
    @Update("UPDATE learning_statistics_detail SET completion_status = 1, updated_at = NOW() WHERE id = #{id}")
    int markAsCompleted(@Param("id") Long id);
    
    /**
     * 获取学生在课程中的学习统计详情
     * 
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 学习统计详情DTO列表
     */
    List<LearningStatisticsDetailDTO> getStudentCourseStatistics(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
    
    /**
     * 获取课程的总体学习情况统计
     * 
     * @param courseId 课程ID
     * @return 学习统计详情DTO列表
     */
    List<LearningStatisticsDetailDTO> getCourseStatisticsSummary(@Param("courseId") Long courseId);
} 